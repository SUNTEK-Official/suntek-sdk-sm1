package com.szanfu.sdk.test.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import com.szanfu.sdk.api.ISdkServiceManager;

/**
 * 优化内容：
 *
 * 服务连接稳定性增强：引入了死亡监听（linkToDeath），确保当服务异常中断时，能够自动重新绑定服务，避免因服务崩溃导致的连接丢失。
 *
 * 重试机制：在服务连接失败时，最多重试三次，每次间隔两秒，确保在网络波动或其他临时问题下仍能成功连接服务。
 *
 * 解绑优化：在解绑服务时，不仅解除绑定，还强制停止服务，确保系统资源释放，避免后台进程占用过多。
 *
 * 解绑后要稍微等个 1~2 秒再重新 bind（加点保护，系统清理 binder对象需要时间）。
 */
public class SDKServiceManager {

    private static final String TAG = "SDKServiceManager";
    private static final String CHECK_TAG = "SDK_BIND_CHECK";
    private static final String SERVICE_PACKAGE = "com.szanfu.sdk.service";
    private static final String SERVICE_ACTION = "com.szanfu.sdk.service.SDK_SERVICE";
    private static final int MAX_RETRY = 3;
    private static final long RETRY_DELAY_MS = 2000;

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private ISdkServiceManager mSdkServiceManager;
    private ServiceConnection mServiceConnection;
    private IBinder.DeathRecipient deathRecipient;
    private boolean isServiceBound = false;
    private int retryCount = 0;
    private OnServiceConnectListener onServiceConnectListener;

    public SDKServiceManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void setOnServiceConnectListener(OnServiceConnectListener listener) {
        this.onServiceConnectListener = listener;
    }

    public void bind() {
        if (isServiceBound) {
            Log.i(TAG, "Already bound, skip bind");
            return;
        }
        Log.i(TAG, "Trying to bind service...");
        Intent intent = new Intent();
        intent.setPackage(SERVICE_PACKAGE);
        intent.setAction(SERVICE_ACTION);

        mServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mSdkServiceManager = ISdkServiceManager.Stub.asInterface(service);
                isServiceBound = true;
                retryCount = 0;
                try {
                    // DeathRecipient to handle binder death
                    deathRecipient = () -> {
                        Log.e(TAG, "Binder died. Rebinding...");
                        unbind();
                        bind();
                    };
                    mSdkServiceManager.asBinder().linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    Log.e(TAG, "linkToDeath exception: " + e.getMessage());
                }

                if (onServiceConnectListener != null) {
                    onServiceConnectListener.onConnected(mSdkServiceManager);
                }
                Log.i(TAG, "Service connected successfully");
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                isServiceBound = false;
                mSdkServiceManager = null;
                if (onServiceConnectListener != null) {
                    onServiceConnectListener.onDisconnected();
                }
                Log.e(TAG, "Service disconnected");
            }
        };

        boolean isBind = context.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE | Context.BIND_NOT_FOREGROUND);
        if (!isBind) {
            Log.e(TAG, "bindService failed");
            if (retryCount < MAX_RETRY) {
                retryCount++;
                handler.postDelayed(this::bind, RETRY_DELAY_MS);
            } else {
                Log.e(TAG, "bindService retry over max times");
            }
        }
    }

    public void unbind() {
        Log.i(CHECK_TAG, "SDKServiceManager.unbind() called, isServiceBound=" + isServiceBound
                + ", hasConnection=" + (mServiceConnection != null)
                + ", hasService=" + (mSdkServiceManager != null));
        if (!isServiceBound) {
            Log.i(TAG, "Service not bound, skip unbind");
            Log.i(CHECK_TAG, "SDKServiceManager.unbind() skipped: service not bound");
            return;
        }

        try {
            if (mSdkServiceManager != null && deathRecipient != null) {
                try {
                    mSdkServiceManager.asBinder().unlinkToDeath(deathRecipient, 0);
                    Log.i(TAG, "unlinkToDeath success");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.i(CHECK_TAG, "calling context.unbindService()");
            context.unbindService(mServiceConnection);
            Log.i(TAG, "unbindService success");
            Log.i(CHECK_TAG, "context.unbindService() success");

            // Stop the service if it's no longer needed
            Intent intent = new Intent();
            intent.setPackage(SERVICE_PACKAGE);
            intent.setAction(SERVICE_ACTION);
            Log.i(CHECK_TAG, "calling context.stopService(), package=" + SERVICE_PACKAGE + ", action=" + SERVICE_ACTION);
            context.stopService(intent);
            Log.i(TAG, "stopService success");
            Log.i(CHECK_TAG, "context.stopService() called");
        } catch (Exception e) {
            Log.e(TAG, "unbindService error: " + e.getMessage());
            Log.e(CHECK_TAG, "SDKServiceManager.unbind() error: " + e.getMessage());
        }

        isServiceBound = false;
        mSdkServiceManager = null;
        mServiceConnection = null;
        deathRecipient = null;
        Log.i(CHECK_TAG, "SDKServiceManager.unbind() finished, isServiceBound=false");
    }

    public boolean isServiceBound() {
        return isServiceBound;
    }

    public ISdkServiceManager getService() {
        return mSdkServiceManager;
    }

    public interface OnServiceConnectListener {
        void onConnected(ISdkServiceManager sdkServiceManager);

        void onDisconnected();
    }
}
