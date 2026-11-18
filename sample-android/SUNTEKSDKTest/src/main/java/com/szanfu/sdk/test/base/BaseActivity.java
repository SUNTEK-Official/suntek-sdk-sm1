package com.szanfu.sdk.test.base;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.test.util.LogUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseActivity extends Activity {
    private static final String SERVICE_CHECK_TAG = "SDK_BIND_CHECK";
    boolean isServiceBound = false;
    private ISdkServiceManager mSdkServiceManager = null;
    protected Context mContext;
    protected ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " onDestroy(), isServiceBound=" + isServiceBound);
        if(isServiceBound){
            unbindService();
        }
    }

    /**
     * 绑定SDK binder服务，调用SDK接口的前提。
     */
    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.szanfu.sdk.service");
        intent.setAction("com.szanfu.sdk.service.SDK_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    /**
     * 注销服务 Unbind from Service
     */
    private void unbindService(){
        if(isServiceBound){
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " calling unbindService(conn)");
            unbindService(conn);
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " unbindService(conn) called");
        } else {
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " unbindService() skipped, isServiceBound=false");
        }
    }

    /**
     * 服
     */
    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            mSdkServiceManager = null;
            LogUtils.d("SDK binder service disconnected.");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isServiceBound = true;
            mSdkServiceManager = ISdkServiceManager.Stub.asInterface(service);
            if (mSdkServiceManager != null) {
                try {
                    /**
                     * 监听SDK binder服务的死亡状态，如果挂掉，则重新进行绑定。
                     */
                    mSdkServiceManager.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            LogUtils.d("SDK binder service binderDied.");
                            bindService();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                BaseActivity.this.onServiceConnected(mSdkServiceManager);
            }
        }
    };
    public abstract void onServiceConnected(ISdkServiceManager sdkServiceManager);

    final protected void UI_Toast(final String text) {
        LogUtils.d( text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}


