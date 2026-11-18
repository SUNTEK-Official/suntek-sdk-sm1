package com.szanfu.sdk.test.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.hardware.camera2.CameraDevice;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.huawei.hms.ml.scan.HmsScan;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.activity.camera2.Camera2Listener;
import com.szanfu.sdk.test.activity.camera2.Camera2Helper;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.LogUtils;

public class Camera2Activity extends Activity implements Camera2Listener {
    private Timer timer;
    private static final String TAG = "Camera2Activity";
    private static final int ACTION_REQUEST_PERMISSIONS = 1;
    public static final String BUNDLE_KEY_HW = "hms_scan";
    public static final String BUNDLE_KEY_AX = "anxin_scan";
    private Camera2Helper camera2Helper;
    TextureView textureView;

    private int displayOrientation;
    private boolean isMirrorPreview;
    private String openedCameraId;
    private int currentIndex = 0;
    private static final int PROCESS_INTERVAL = 30;

    private static ExecutorService imageProcessExecutor;
    public static final String CAMERA_ID = "camera_id";
    public static final String DECODE_LIB = "decode_lib";
    public static final String IS_FIXED_FOUCUS = "fixed_focus";
    private static final String[] NEEDED_PERMISSIONS = new String[]{
            Manifest.permission.CAMERA
    };
    private static MyHandler handler;

    private static long startTime;
    private long initDoneTime;
    private long startActivityTime;
    private long initCameraTimeUsedTime;
    public static final String SCAN_TIME = "time";
    public static final String SCAN_RESULT = "scanResult";

    private String cameraId = Camera2Helper.CAMERA_ID_BACK;
    private String decodeLib = Camera2Helper.DECODE_LIB_HW;
    private boolean isFixedFocus = false;
    Animation mTop2Bottom, mBottom2Top;
    private ImageView mIvScan;
    private Context mContext;

 // Handler
    private static class MyHandler extends Handler {
        private final WeakReference<Camera2Activity> camera2ActivityWeakReference;

        public MyHandler(Camera2Activity mineActivity) {
            camera2ActivityWeakReference = new WeakReference<>(mineActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Camera2Activity activity = camera2ActivityWeakReference.get();
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                return; // Activity
            }

            Bundle bundle = msg.getData();
            switch (msg.what) {
                case 1000:
                    HmsScan hmsScan = bundle.getParcelable(BUNDLE_KEY_HW);
                    if (hmsScan != null && !TextUtils.isEmpty(hmsScan.getOriginalValue())) {
                        LogUtils.i("----- 解析结果：" + hmsScan.getOriginalValue());
                        long endTime = Math.abs(System.currentTimeMillis() - startTime);
                        LogUtils.i("----- 扫码时间花费：" + endTime);
                        Intent intent = new Intent();
                        intent.putExtra(SCAN_RESULT, hmsScan);
                        intent.putExtra(SCAN_TIME, endTime);
                        intent.putExtra(INITCAMERTIME, activity.initCameraTimeUsedTime);
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();
                    }
                    break;
                case 2000:
                    String scancode = bundle.getString(BUNDLE_KEY_AX);
                    if (!TextUtils.isEmpty(scancode)) {
                        LogUtils.i("----- 解析结果：" + scancode);
                        long endTime = Math.abs(System.currentTimeMillis() - startTime);
                        LogUtils.i("----- 扫码时间花费：" + endTime);
                        Intent intent = new Intent();
                        intent.putExtra(SCAN_RESULT, scancode);
                        intent.putExtra(SCAN_TIME, endTime);
                        intent.putExtra(INITCAMERTIME, activity.initCameraTimeUsedTime);
                        activity.setResult(RESULT_OK, intent);
                        activity.finish();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
        setContentView(R.layout.activity_camera2);
        LogUtils.e("activity_camera2 ");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

 // ( , )
        if (imageProcessExecutor == null || imageProcessExecutor.isShutdown()) {
            imageProcessExecutor = Executors.newSingleThreadExecutor();
        }

        mContext = this;


        String decodeLib = getIntent().getStringExtra(Camera2Activity.DECODE_LIB);
        if (!TextUtils.isEmpty(decodeLib)) {
            this.decodeLib = decodeLib;
        }

        String cameraId = getIntent().getStringExtra(Camera2Activity.CAMERA_ID);
        if (!TextUtils.isEmpty(cameraId)) {
            this.cameraId = cameraId;
        }

        isFixedFocus = getIntent().getBooleanExtra(Camera2Activity.IS_FIXED_FOUCUS, false);
        initData();
        initAnimationView();

        startActivityTime = getIntent().getLongExtra("startActivityTime", 1);
        boolean hasFlash = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        LogUtils.e("是否支持闪光灯：" + hasFlash);


        initDoneTime = System.currentTimeMillis();
        initCameraTimeUsedTime = Math.abs(initDoneTime - startActivityTime);
        LogUtils.e("ScannerActivity 初始化完毕时间 " + initDoneTime);

 // Timer Thread.sleep
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {

                if (!isFinishing() && !isDestroyed()) {
                    finish();
                }
            }
        }, AppConfig.qrScanTimeOut);
    }

    void initCamera() {
        camera2Helper = new Camera2Helper.Builder()
                .cameraListener(this)
                .minPreviewSize(new Point(1280, 720))
                .specificCameraId(cameraId)
                .context(getApplicationContext())
                .previewOn(textureView)
                .previewViewSize(new Point(textureView.getWidth(), textureView.getHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .setDecodeLib(decodeLib)
                .setFixedFocus(isFixedFocus)
                .build();
        camera2Helper.start();
    }

    public void initData() {
        textureView = findViewById(R.id.texture_preview_camera);
        handler = new MyHandler(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initCamera();
        }
    }

    private boolean checkPermissions(String[] neededPermissions) {
        if (neededPermissions == null || neededPermissions.length == 0) {
            return true;
        }
        boolean allGranted = true;
        for (String neededPermission : neededPermissions) {
            allGranted &= ContextCompat.checkSelfPermission(this, neededPermission) == PackageManager.PERMISSION_GRANTED;
        }
        return allGranted;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            boolean isAllGranted = true;
            for (int grantResult : grantResults) {
                isAllGranted &= (grantResult == PackageManager.PERMISSION_GRANTED);
            }
            if (isAllGranted) {
                initCamera();
            } else {
                Toast.makeText(this, "权限被拒绝", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        if (camera2Helper != null) {
            camera2Helper.stop();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (camera2Helper != null) {
            camera2Helper.start();
        }
    }

    @Override
    public void onCameraClosed() {
        Log.i(TAG, "onCameraClosed: ");
    }

    @Override
    public void onCameraError(Exception e) {
        e.printStackTrace();
 // Activity
        if (!isFinishing()) {
            finish();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIvScan != null) {
            mIvScan.clearAnimation();
        }
    }

    @Override
    protected void onDestroy() {
 // ( )
        if (imageProcessExecutor != null && !imageProcessExecutor.isShutdown()) {
 // shutdown
            imageProcessExecutor.shutdownNow();
        }

 // Camera
        if (camera2Helper != null) {
            camera2Helper.release();
            camera2Helper = null;
        }

 // Timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

 // Handler
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }

        super.onDestroy();
    }

    @Override
    public void onPreview(byte[] y, byte[] u, byte[] v, Size previewSize, int stride) {

    }

    @Override
    public void onCameraOpened(CameraDevice cameraDevice, String cameraId, Size previewSize, int displayOrientation, boolean isMirror) {
        Log.i(TAG, "onCameraOpened:  previewSize = " + previewSize.getWidth() + "x" + previewSize.getHeight());
        this.displayOrientation = displayOrientation;
        this.isMirrorPreview = isMirror;
        this.openedCameraId = cameraId;
    }

    public void switchCamera(View view) {
        if (camera2Helper != null) {
            camera2Helper.switchCamera();
        }
    }

    public static final String INITCAMERTIME = "initCameraUsedtime";

    public static void send_message(Message message) {
        if (message != null && handler != null) {
            handler.sendMessage(message);
        }
    }

    private void initAnimationView() {
        mIvScan = findViewById(R.id.scan_line);

        mTop2Bottom = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 1.0f);

        mBottom2Top = new TranslateAnimation(
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.1f);

        mBottom2Top.setRepeatMode(Animation.RESTART);
        mBottom2Top.setInterpolator(new LinearInterpolator());
        mBottom2Top.setDuration(1500);
        mBottom2Top.setFillEnabled(true);
        mBottom2Top.setFillAfter(true);
        mBottom2Top.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIvScan != null) {
                    mIvScan.startAnimation(mTop2Bottom);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mTop2Bottom.setRepeatMode(Animation.RESTART);
        mTop2Bottom.setInterpolator(new LinearInterpolator());
        mTop2Bottom.setDuration(2000);
        mTop2Bottom.setFillEnabled(true);
        mTop2Bottom.setFillAfter(true);
        mTop2Bottom.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mIvScan != null) {
                    mIvScan.startAnimation(mTop2Bottom);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        if (mIvScan != null) {
            mIvScan.startAnimation(mTop2Bottom);
        }
    }

    @Override
    public void onBackPressed() {
        LogUtils.d("onBackPressed");
        super.onBackPressed();
    }
}