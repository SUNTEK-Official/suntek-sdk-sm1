/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.szanfu.sdk.test.hmscorescan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.huawei.hms.hmsscankit.OnLightVisibleCallBack;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.hmsscankit.ScanUtil;
import com.huawei.hms.ml.scan.HmsScan;
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.ConstantData;
import com.szanfu.sdk.test.util.DataConversionUtils;
import com.szanfu.sdk.test.util.LogUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class HWDefinedActivity extends Activity {

    private FrameLayout frameLayout;
 // WeakReference RemoteView( )
    private WeakReference<RemoteView> remoteViewWeakRef;
    private ImageView backBtn;
    private ImageView imgBtn;
    private ImageView flushBtn;
    int mScreenWidth;
    int mScreenHeight;
    final int SCAN_FRAME_SIZE = 240;

    private int[] img = {R.drawable.flashlight_on, R.drawable.flashlight_off};
    private static final String TAG = "DefinedActivity";

    public static final String SCAN_RESULT = "scanResult";
    public static final int REQUEST_CODE_PHOTO = 0X1113;
    public static final String TIME = "time";
    public static final String INITCAMERTIME = "initCameraUsedtime";
    private long startTime;

 // WeakReference Activity( )
    private WeakReference<HWDefinedActivity> mActivityWeakRef;

    private Timer timer;
    private TimerTask scanTimeoutTask;
 // Handler( : Handler, , )
    private Handler mainHandler;
    private WeakReference<Handler> mainHandlerWeakRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startTime = System.currentTimeMillis();
 // WeakReference Activity
        mActivityWeakRef = new WeakReference<>(this);
 // 1: Handler, , Handler
        mainHandler = new Handler(Looper.getMainLooper());
        mainHandlerWeakRef = new WeakReference<>(mainHandler);

        HWDefinedActivity activity = mActivityWeakRef.get();
        if (activity == null) {
            finish();
            return;
        }

        boolean hasFlash = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        LogUtils.e("是否支持闪光灯：" + hasFlash);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        LogUtils.e("activity_defined ");
        setContentView(R.layout.activity_defined);


        frameLayout = findViewById(R.id.rim);
        flushBtn = findViewById(R.id.flush_btn);
        backBtn = findViewById(R.id.back_img);
        imgBtn = findViewById(R.id.img_btn);


        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;

        int scanFrameSize = (int) (SCAN_FRAME_SIZE * density);


        Rect rect = new Rect();
        rect.left = mScreenWidth / 2 - scanFrameSize / 2;
        rect.right = mScreenWidth / 2 + scanFrameSize / 2;
        rect.top = mScreenHeight / 2 - scanFrameSize / 2;
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2;

 // 3. RemoteView
 // Activity (this), setContext Activity
        RemoteView remoteView = new RemoteView.Builder()
                .setContext(this)
                .setBoundingBox(rect)
                .setFormat(HmsScan.ALL_SCAN_TYPE)
                .build();
        remoteViewWeakRef = new WeakReference<>(remoteView);

 // ( WeakReference )
        remoteView.setOnLightVisibleCallback(new LightVisibleCallback(this));
 // 180 ;
        String tempPlatform = DataConversionUtils.getCustomerType();
        if (ConstantData.PLATFORM_930_QUECTEL.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_930_MTK.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_820_MTK.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_930_QUECTEL.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_930_MTK_A14.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_820_GTH.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_820_MTK_PINPAD.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_930_CY.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_980_MTK.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_820_CY.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_820_COB.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
        }else if (ConstantData.PLATFORM_980_MTK_64.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_980_GMS.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_980_CY.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_980_CY.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
            remoteView.setRotation(180);
        }else if (ConstantData.PLATFORM_820_MTK_ANDROID_10.equals(tempPlatform)) {
            LogUtils.e("----------------------------------------");
//            remoteView.setRotation(180);
        }

 // ( WeakReference )
        remoteView.setOnResultCallback(new ScanResultCallback(this));

 // RemoteView
        remoteView.onCreate(savedInstanceState);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        frameLayout.addView(remoteView, params);


        setBackOperation();
        setPictureScanOperation();
        setFlashOperation();

 // 2: , RemoteView
        initScanTimeoutTimer();
    }

    /**
     * 弱引用包装的闪光灯可见性回调
     * 避免匿名内部类持有Activity强引用
     */
    private static class LightVisibleCallback implements OnLightVisibleCallBack {
        private final WeakReference<HWDefinedActivity> activityWeakRef;

        public LightVisibleCallback(HWDefinedActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        public void onVisibleChanged(boolean visible) {
            HWDefinedActivity activity = activityWeakRef.get();
            if (activity != null && activity.flushBtn != null) {
                activity.flushBtn.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    /**
     * 弱引用包装的扫描结果回调
     * 避免匿名内部类持有Activity强引用
     */
    private static class ScanResultCallback implements OnResultCallback {
        private final WeakReference<HWDefinedActivity> activityWeakRef;

        public ScanResultCallback(HWDefinedActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        public void onResult(HmsScan[] result) {
            HWDefinedActivity activity = activityWeakRef.get();
            if (activity == null) {
                return;
            }


            if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {
                activity.cancelScanTimeoutTimer();
                Intent intent = new Intent();
                intent.putExtra(SCAN_RESULT, result[0]);
                activity.setResult(RESULT_OK, intent);
                activity.finish();
            }
        }
    }

    /**
     * 初始化扫码超时定时任务（优化后：保证任务稳定启动和执行）
     */
    private void initScanTimeoutTimer() {

        cancelScanTimeoutTimer();

        HWDefinedActivity activity = mActivityWeakRef.get();
        if (activity == null) {
            LogUtils.e("initScanTimeoutTimer: Activity已回收，无法启动定时任务");
            return;
        }


        long timeoutMillis = AppConfig.qrScanTimeOut;
        if (timeoutMillis <= 0) {
            timeoutMillis = 10000;
            LogUtils.w("扫码超时时间设置无效（<=0），使用默认10秒超时");
        } else {
            LogUtils.i("扫码超时时间设置为：" + timeoutMillis + "ms");
        }

 // Timer
        timer = new Timer("ScanTimeoutTimer", true); // Timer
        scanTimeoutTask = new ScanTimeoutTask(this);
        timer.schedule(scanTimeoutTask, timeoutMillis);

        LogUtils.d("扫码超时定时任务已成功启动，超时时间：" + timeoutMillis + "ms");
    }

    /**
     * 弱引用包装的超时定时任务（优化：修复Handler获取逻辑，保证UI操作可执行）
     */
    private static class ScanTimeoutTask extends TimerTask {
        private final WeakReference<HWDefinedActivity> activityWeakRef;

        public ScanTimeoutTask(HWDefinedActivity activity) {
            this.activityWeakRef = new WeakReference<>(activity);
        }

        @Override
        public void run() {
            HWDefinedActivity activity = activityWeakRef.get();
 // 3: Activity , Handler
            if (activity == null || activity.isFinishing() || activity.isDestroyed()) {
                LogUtils.e("ScanTimeoutTask: Activity已销毁，无需执行关闭操作");
                return;
            }

            Handler handler = activity.mainHandlerWeakRef.get();
            if (handler == null) {
                LogUtils.e("ScanTimeoutTask: 主线程Handler为空，无法执行UI操作");
 // Activity( UI , , )
                activity.runOnUiThread(() -> {
                    if (!activity.isFinishing() && !activity.isDestroyed()) {
                        activity.handleScanTimeout();
                    }
                });
                return;
            }

 // Handler
            handler.post(() -> {
                HWDefinedActivity currentActivity = activityWeakRef.get();
                if (currentActivity != null && !currentActivity.isFinishing() && !currentActivity.isDestroyed()) {
                    currentActivity.handleScanTimeout();
                }
            });
        }
    }

    /**
     * 处理扫码超时逻辑（抽离为独立方法，便于维护和调试）
     */
    private void handleScanTimeout() {
        LogUtils.e("扫码超时触发，开始关闭扫码页面");
        cancelScanTimeoutTimer();
        setResult(RESULT_CANCELED);
        finish();
    }

    /**
     * 取消扫码超时定时任务（优化：增加判空，避免空指针异常）
     */
    private void cancelScanTimeoutTimer() {
        if (scanTimeoutTask != null) {
            scanTimeoutTask.cancel();
            scanTimeoutTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        LogUtils.d("扫码超时定时任务已取消");
    }

    /**
     * 设置返回按钮操作
     */
    private void setBackOperation() {
        backBtn.setOnClickListener(v -> {
            cancelScanTimeoutTimer();
            finish();
        });
    }

    /**
     * 设置图片扫码操作
     */
    private void setPictureScanOperation() {
        imgBtn.setOnClickListener(v -> {
            HWDefinedActivity activity = mActivityWeakRef.get();
            if (activity == null) {
                return;
            }
            Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
            startActivityForResult(pickIntent, REQUEST_CODE_PHOTO);
        });
    }

    /**
     * 设置闪光灯操作
     */
    private void setFlashOperation() {
        flushBtn.setOnClickListener(v -> {
            RemoteView remoteView = remoteViewWeakRef != null ? remoteViewWeakRef.get() : null;
            if (remoteView == null) {
                return;
            }
            boolean isLightOn = remoteView.getLightStatus();
            remoteView.switchLight();
            flushBtn.setImageResource(isLightOn ? img[1] : img[0]);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        RemoteView remoteView = remoteViewWeakRef != null ? remoteViewWeakRef.get() : null;
        if (remoteView != null) {
            remoteView.onStart();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        RemoteView remoteView = remoteViewWeakRef != null ? remoteViewWeakRef.get() : null;
        if (remoteView != null) {
            remoteView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        RemoteView remoteView = remoteViewWeakRef != null ? remoteViewWeakRef.get() : null;
        if (remoteView != null) {
            remoteView.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        cancelScanTimeoutTimer();

 // RemoteView
        RemoteView remoteView = remoteViewWeakRef != null ? remoteViewWeakRef.get() : null;
        if (remoteView != null) {
            remoteView.onDestroy();
        }

 // 4: Handler
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
            mainHandler = null;
        }


        if (remoteViewWeakRef != null) {
            remoteViewWeakRef.clear();
        }
        if (mActivityWeakRef != null) {
            mActivityWeakRef.clear();
        }
        if (mainHandlerWeakRef != null) {
            mainHandlerWeakRef.clear();
        }


        frameLayout = null;
        backBtn = null;
        imgBtn = null;
        flushBtn = null;
        remoteViewWeakRef = null;
        mActivityWeakRef = null;
        mainHandlerWeakRef = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        RemoteView remoteView = remoteViewWeakRef != null ? remoteViewWeakRef.get() : null;
        if (remoteView != null) {
            remoteView.onStop();
        }
    }

    @Override
    public void onBackPressed() {
        cancelScanTimeoutTimer();
        LogUtils.d("onBackPressed");
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            try {
                HWDefinedActivity activity = mActivityWeakRef.get();
                if (activity == null) {
                    return;
                }
 // (Bitmap , )
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), data.getData());
                if (bitmap == null) {
                    return;
                }


                HmsScan[] hmsScans = ScanUtil.decodeWithBitmap(
                        activity,
                        bitmap,
                        new HmsScanAnalyzerOptions.Creator().setPhotoMode(true).create()
                );

 // Bitmap
                bitmap.recycle();
                bitmap = null;


                if (hmsScans != null && hmsScans.length > 0 && hmsScans[0] != null && !TextUtils.isEmpty(hmsScans[0].getOriginalValue())) {
                    cancelScanTimeoutTimer();
                    Intent intent = new Intent();
                    intent.putExtra(SCAN_RESULT, hmsScans[0]);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } catch (IOException e) {
                LogUtils.e("图片扫码失败：" + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}