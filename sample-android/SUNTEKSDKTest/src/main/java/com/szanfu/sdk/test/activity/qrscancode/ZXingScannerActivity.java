package com.szanfu.sdk.test.activity.qrscancode;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResultType;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.LogUtils;
import com.wushaohong.zxing.ScannerView;
import com.wushaohong.zxing.camera.open.CameraFacing;
import com.wushaohong.zxing.common.Scanner;
import com.wushaohong.zxing.decode.QRDecode;

import java.io.FileNotFoundException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * author :
 * e-mail : 576218811@qq.com
 * date   : 2019/08/28
 * desc   : 摄像头扫描二维码页面 Activity
 * version: 1.0
 */
public class ZXingScannerActivity extends BaseScannerActivity {
    private Camera camera;
    private static final int CHOOSE_PICTURE_REQUEST_CODE = 10;
    private ImageView mIvBack;
    private TextView mTvAlbum;
    /**
     * 扫描控件
     */
    private ScannerView mScannerView;

    /**
     * 闪光灯提示布局
     */
    private LinearLayout mLlWeak;
    private ImageView mIvFlashLamp;
    private TextView mTvWeakTips;
    protected Context mContext;


    private Timer mScanTimer;
    private TimerTask mScanTimerTask;
 // Handler, UI
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, ZXingScannerActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        boolean hasFlash = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        LogUtils.e("是否支持闪光灯："+hasFlash);

        Window window = getWindow();

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        setContentView(R.layout.activity_scanner);
        initView();


        initScanTimeoutTimer();
    }

    /**
     * 初始化扫描超时定时器
     */
    private void initScanTimeoutTimer() {
 // ( )
        cancelScanTimer();

 // 10 (10000ms)
        int timeoutMillis = AppConfig.qrScanTimeOut > 0 ? AppConfig.qrScanTimeOut : 10000;
        LogUtils.e("二维码扫描超时时间：" + timeoutMillis + "ms");


        mScanTimer = new Timer();
        mScanTimerTask = new TimerTask() {
            @Override
            public void run() {

                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        LogUtils.e("二维码扫描超时，自动关闭页面");

                        finish();
                    }
                });
            }
        };


        mScanTimer.schedule(mScanTimerTask, timeoutMillis);
    }

    /**
     * 取消扫描超时定时器
     */
    private void cancelScanTimer() {
        if (mScanTimerTask != null) {
            mScanTimerTask.cancel();
            mScanTimerTask = null;
        }
        if (mScanTimer != null) {
            mScanTimer.cancel();
            mScanTimer.purge();
            mScanTimer = null;
        }
    }

    private void initView() {
        mIvBack = (ImageView) findViewById(R.id.iv_back);
        mIvBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        mTvAlbum = (TextView) findViewById(R.id.tv_album);
        mTvAlbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choosePicture();
            }
        });


        mScannerView = (ScannerView) findViewById(R.id.sv_scan);

        mScannerView.setOnScannerCompletionListener(this);

        mScannerView.setMediaResId(R.raw.beep);

        mScannerView.setDrawText("放入框内，自动扫描", true);

        mScannerView.setDrawTextColor(0xFF26CEFF);
        mScannerView.setScanMode(Scanner.ScanMode.QR_CODE_MODE);
        mScannerView.isScanFullScreen(false);

        mScannerView.setLaserFrameBoundColor(0xFF26CEFF);

        mScannerView.setLaserGridLineResId(R.mipmap.zfb_grid_scan_line);


        mScannerView.setCameraFacing(CameraFacing.BACK);

        mScannerView.setLaserFrameTopMargin(150);

        mScannerView.setLaserFrameCornerLength(25);

        mScannerView.setLaserLineHeight(10);

        mScannerView.setLaserFrameCornerWidth(3);

        mLlWeak = (LinearLayout) findViewById(R.id.ll_weak_light);
        mIvFlashLamp = (ImageView) findViewById(R.id.iv_flash_lamp);
        mTvWeakTips = (TextView) findViewById(R.id.tv_weak_light_tips);

        mLlWeak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                controlFlashLamp();
            }
        });
    }

    @Override
    protected void onResume() {
        mScannerView.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        mScannerView.onPause();
        mLlWeak.setVisibility(View.GONE);
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        cancelScanTimer();
 // Handler
        mMainHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    public static final String SCAN_RESULT = "scanResult";
    public static final String TIME = "time";
    public static final String INITCAMERTIME = "initCameraUsedtime";

    @Override
    void onScanResultActivity(Result result, ParsedResultType type, Bundle bundle) {

        cancelScanTimer();


        Intent intent = new Intent();
        intent.putExtra(SCAN_RESULT, result.getText());
        setResult(RESULT_OK, intent);
        ZXingScannerActivity.this.finish();
    }

    @Override
    public void onCheckWeakLight(boolean isWeakLight) {
        super.onCheckWeakLight(isWeakLight);
        boolean isOpen = mScannerView.isLightStatus();

        if (isOpen) {
            mLlWeak.setVisibility(View.VISIBLE);
        } else {
            mLlWeak.setVisibility(isWeakLight ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Activity.RESULT_OK == resultCode) {
            switch (requestCode) {
                case CHOOSE_PICTURE_REQUEST_CODE:
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            ContentResolver cr = this.getContentResolver();
                            try {
                                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                                QRDecode.decodeQR(bitmap, this);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 选择图片
     */
    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, CHOOSE_PICTURE_REQUEST_CODE);
    }

    /**
     * 控制闪光灯
     */
    private void controlFlashLamp() {
        boolean isOpen = mScannerView.isLightStatus();
        isOpen = !isOpen;
        mIvFlashLamp.setImageDrawable(getDrawable(isOpen ?
                R.drawable.ic_flashlight_open : R.drawable.ic_flashlight_close));
        mScannerView.toggleLight(isOpen);
    }

    @Override
    public void onBackPressed() {

        cancelScanTimer();
        super.onBackPressed();
    }
}