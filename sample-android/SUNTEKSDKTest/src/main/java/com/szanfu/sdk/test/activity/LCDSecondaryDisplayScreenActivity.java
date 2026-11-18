package com.szanfu.sdk.test.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ISystemEncDecBinderService;
import com.szanfu.sdk.api.system.ISystemReadUserKeyBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.entity.LcdInfo;
import com.szanfu.sdk.test.util.CpuUsageUtil;
import com.szanfu.sdk.test.util.LcdUtils;
import com.szanfu.sdk.test.util.LedState;
import com.szanfu.sdk.test.util.LogUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
public class LCDSecondaryDisplayScreenActivity extends ListButtonBaseActivity {

    private final ExecutorService mLcdExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean mIsAnimationRunning = false;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

    private ImageView ivAnimationView;

    private Handler animationHandler = new Handler(android.os.Looper.getMainLooper());
    private Runnable animationRunnable;
    private int currentFrame = 0;
    private final int totalFrames = 50; // 00000 ~ 00049
    private final int delayMs = 40; // ( )
    private boolean isPlaying = false;

 // [ ]
    private Future<?> mCurrentAnimationFuture;

    private CpuUsageUtil cpuUtil;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private static final String TAG = "LCDSecondaryDisplayScreenActivity";
    private int iPrinterSuccess = 0;
    private int iPrinterError = 0;
    private int iPrinterCount = 0;
    private int iPrinterCountOrg = 1;

 // LCD
    public static final int LCD_INIT = 0; // LCD
    public static final int LCD_GET_DEV_STATUS = 1; // LCD
    public static final int LCD_DISPLAY_STRING = 2;
    public static final int LCD_CLEAR_SCREEN = 3;
    public static final int LCD_FILL_SCREEN = 4;
    public static final int LCD_DRAW_RECTANGLE = 5;
    public static final int LCD_DRAW_LINE = 6;
    public static final int LCD_DRAW_PICTURE = 7;
    public static final int LCD_REFRESH_SCREEN = 8;

    public static final int ANIMATION_1 = 9;
    public static final int ANIMATION_2 = 10;
    public static final int ANIMATION_3 = 11;
    public static final int ANIMATION_4 = 12; // 👈 : drable gif
    public static final int ANIMATION_5 = 13; // 👈 : drable gif
    public static final int ANIMATION_STOP = 14;

    int retDalUsbOpen = -1;
    int timeout_ms = 1000 * 5;



    private volatile boolean isThreadRunning = false;
    private Thread subThread;
    private final Object lock = new Object();

    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
 // Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int attemptCount = 0;
    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private ISystemReadUserKeyBinderService mISystemReadUserKeyBinderService;
    private ISystemEncDecBinderService mISystemEncDecBinderService;
    private final LcdUtils mLcdUtils = LcdUtils.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cpuUtil = CpuUsageUtil.getInstance();

        ivAnimationView = findViewById(R.id.iv_animation_view);
        mButtons = getResources().getStringArray(R.array.ALL_SYSTEM_LCD_SERVICE_LIST);
        setButtonDataSource(mButtons);
    }
    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            Log.e(TAG, "onServiceConnected: ");
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
            mISystemReadUserKeyBinderService = ISystemReadUserKeyBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM_READUSERKEY));
            mISystemEncDecBinderService = ISystemEncDecBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM_ENCDEC));
            mLcdUtils.init(mSystemBinderService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
            case LCD_INIT:
                lcdInit(itemName);
                break;
            case LCD_GET_DEV_STATUS:
                lcdGetDevLcdStatus(itemName);
                break;
            case LCD_DISPLAY_STRING:
                lcdDisplayStringFullScreen(itemName, "Welcome");
                // lcdDisplayQrUrl(itemName, "https://api.example.com/szanfu-admin/sn/appletslot?sn=H200990001");
                break;
            case LCD_CLEAR_SCREEN:
                lcdClearScreen(itemName);
                break;
            case LCD_FILL_SCREEN:
                lcdFillScreen(itemName);
                break;
            case LCD_DRAW_RECTANGLE:
                lcdDrawRectangle(itemName);
                break;
            case LCD_DRAW_LINE:
                lcdDrawLine(itemName);
                break;
            case LCD_DRAW_PICTURE:
                lcdDrawPicture(itemName);
                break;
            case LCD_REFRESH_SCREEN:
                lcdRefreshScreen(itemName);
                break;
            case ANIMATION_1:
                ivAnimationView.setVisibility(View.VISIBLE);
                startScrollAnimation();
                break;
            case ANIMATION_2:
                lcdDrawSinglePicture(itemName); // 👈 : 00000
                break;
            case ANIMATION_3:
                lcdDrawLocalPicture(itemName);
                break;
            case ANIMATION_4:
                lcdDrawGifAnimation(itemName,2,false);
                break;
            case ANIMATION_5:
                lcdDrawGifAnimation(itemName,2,true);
                break;
            case ANIMATION_STOP:
                stopLcdAnimation();
                lcdInit(itemName);
                break;
        }
    }


    // ======================
 // [ ] LcdUtils.displayBmpOnLcd animation_effect00000.bmp
// ======================
    private void lcdDrawSinglePicture(String itemName) {
        stopLcdAnimation();
        appendText(itemName + " 通过 displayBmpOnLcd 显示第一帧 BMP 图片：animation_effect00000.bmp");

        if (mLcdUtils.lcdGetDevLcdStatus() == null) {
            appendText("LCD 未初始化");
            return;
        }

        mLcdExecutor.execute(() -> {
            String resName = "animation_effect00000";
            String bmpFileName = resName + ".bmp";

            try {
                if (copyDrawableBmpToFiles(resName, bmpFileName)) {
                    File bmpFile = new File(getFilesDir(), bmpFileName);
                    mLcdUtils.displayBmpOnLcd(bmpFile.getAbsolutePath());
                    LogUtils.e("第一帧 BMP 图片显示成功");
                } else {
                    LogUtils.e("找不到 BMP 图片：" + resName);
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e("第一帧 BMP 图片显示异常：" + e.getMessage());
            }
        });
    }

    private boolean copyDrawableBmpToFiles(String resName, String fileName) {
        File targetFile = new File(getFilesDir(), fileName);
        if (targetFile.exists()) {
            return true;
        }

        int resId = getResources().getIdentifier(resName, "drawable", getPackageName());
        if (resId == 0) {
            return false;
        }

        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getResources().openRawResource(resId);
            outputStream = new FileOutputStream(targetFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignore) {
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException ignore) {
                }
            }
        }
    }


    /**
     * 通过 displayBmpOnLcd 轮播 drawable 目录下的 BMP 帧图片。
     * animation_effect00000.bmp ~ animation_effect00049.bmp
     *
     * @param itemName 日志标识
     */
    private void lcdDrawLocalPicture(String itemName) {
 // [ ]
        stopLcdAnimation();

        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        mIsAnimationRunning = true;
        appendText(itemName + " 开始通过 displayBmpOnLcd 轮播 BMP 图片...");

 // [ ] submit Future
        mCurrentAnimationFuture = mLcdExecutor.submit(() -> {
            try {
                int frameIndex = 0;
                final int totalFrames = 50;
                final int frameDelayMs = 50;
                File[] frameFiles = prepareBmpAnimationFrameFiles(totalFrames);

                if (frameFiles == null) {
                    mIsAnimationRunning = false;
                    LogUtils.e("BMP 轮播帧准备失败");
                    return;
                }

                while (mIsAnimationRunning && !Thread.currentThread().isInterrupted()) {
                    try {
                        mLcdUtils.displayBmpOnLcd(frameFiles[frameIndex].getAbsolutePath());
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtils.e("BMP 轮播异常：" + e.getMessage());
                    }

                    frameIndex++;
                    if (frameIndex >= frameFiles.length) {
                        frameIndex = 0;
                    }
                    sleepFrame(frameDelayMs);
                }
            } finally {
                mIsAnimationRunning = false;
            }
            LogUtils.e("displayBmpOnLcd BMP 轮播已停止");
        });
    }

    private File[] prepareBmpAnimationFrameFiles(int totalFrames) {
        File[] frameFiles = new File[totalFrames];

        for (int i = 0; i < totalFrames; i++) {
            String resName = String.format("animation_effect%05d", i);
            String bmpFileName = resName + ".bmp";

            if (!copyDrawableBmpToFiles(resName, bmpFileName)) {
                LogUtils.e("找不到 BMP 图片资源：" + resName);
                return null;
            }

            frameFiles[i] = new File(getFilesDir(), bmpFileName);
        }

        return frameFiles;
    }

    /**
     * 线程安全的帧等待方法
     * @param ms 等待时间（毫秒）
     */
    private void sleepFrame(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {

            mIsAnimationRunning = false;
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 停止副屏帧动画（线程安全）
     * 【优化】不再关闭线程池，只取消当前任务并设置标志位
     */
    private void stopLcdAnimation() {
        mIsAnimationRunning = false;
 // ( )
        if (mCurrentAnimationFuture != null && !mCurrentAnimationFuture.isDone()) {
            mCurrentAnimationFuture.cancel(true);
            mCurrentAnimationFuture = null;
        }
    }

    /**
     * 子线程向主线程UI更新日志
     */
    private void logToUI(String text) {
        mMainHandler.post(() -> appendText(text));
    }




    // ======================

// ======================
    private void startScrollAnimation() {
        if (isPlaying) return;


        ivAnimationView.setVisibility(View.VISIBLE);
        isPlaying = true;
        currentFrame = 0;

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) {
                    ivAnimationView.setVisibility(View.GONE);
                    return;
                }

 // animation_effect00000 ~ 00049
                String name = "animation_effect" + String.format("%05d", currentFrame);
                int resId = getResources().getIdentifier(name, "drawable", getPackageName());

                if (resId != 0) {
                    ivAnimationView.setImageResource(resId);
                }

                currentFrame++;
                if (currentFrame >= totalFrames) {

                    stopScrollAnimation();
                    return;
                }

                animationHandler.postDelayed(this, delayMs);
            }
        };

        animationHandler.post(animationRunnable);
    }

    // ======================

// ======================
    private void stopScrollAnimation() {
        isPlaying = false;
        animationHandler.removeCallbacks(animationRunnable);
        ivAnimationView.setVisibility(View.GONE);
    }

    // ======================
 // 50 Bitmap
// ======================
    private android.graphics.Bitmap[] getFullAnimationBitmaps() {
        android.graphics.Bitmap[] bitmaps = new android.graphics.Bitmap[totalFrames];

        for (int i = 0; i < totalFrames; i++) {
            String name = "animation_effect" + String.format("%05d", i);
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            if (resId != 0) {
                bitmaps[i] = android.graphics.BitmapFactory.decodeResource(getResources(), resId);
            }
        }
        return bitmaps;
    }


    @Override
    protected void onDestroy() {
        iPrinterCountOrg = iPrinterCount;
 // [ ]
        stopLcdAnimation();
        if (mLcdExecutor != null) {
            mLcdExecutor.shutdownNow();
        }
        mainHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Activity销毁，已清理线程");
        stopScrollAnimation();
        animationHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
 // onPause
        // stopUSBReadingThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
 // onResume

    }



    private void lcdInit(String itemName) {
        appendText(itemName);
        int ret = mLcdUtils.lcdInit();
        appendText(itemName + " Result: " + ret + "\n");
    }

    private void lcdGetDevLcdStatus(String itemName) {
        appendText(itemName);
        LcdInfo info = mLcdUtils.lcdGetDevLcdStatus();
        if (info == null) {
            appendErrorText(itemName, "LcdInfo is null (service not ready or lcdInit failed)");
            return;
        }
        appendText(itemName + " -> " + info.toString() + "\n");
    }


    /**
     * Displays a string on the screen with specific position, font size, and colors.
     * Valid font sizes: 12, 16, 24.
     */
    private void lcdDisplayString(String itemName) {
        appendText(itemName);

        String[] testStrings = {
                "10000000",
                "A very very long string exceeding width"
        };

        int[] fontSizes = {12, 16, 24};
        int[] fontColors = {0, 1};
        int[] bgColors = {0, 1};

        int[][] positions = {
                {0, 0},
                {64, 0},
                {0, 16},
                {64, 16}
        };


        int[] index = {0};

        Handler handler = new Handler();
        Runnable testTask = new Runnable() {
            @Override
            public void run() {
                if (index[0] >= fontSizes.length * positions.length * fontColors.length * bgColors.length * testStrings.length) {
                    appendText("LCD Display String Test Completed.\n");
                    return;
                }


                int testIndex = index[0];
                int fontSizeIndex = testIndex / (positions.length * fontColors.length * bgColors.length * testStrings.length);
                int posIndex = (testIndex / (fontColors.length * bgColors.length * testStrings.length)) % positions.length;
                int fontColorIndex = (testIndex / (bgColors.length * testStrings.length)) % fontColors.length;
                int bgColorIndex = (testIndex / testStrings.length) % bgColors.length;
                int textIndex = testIndex % testStrings.length;

                int fontSize = fontSizes[fontSizeIndex];
                int[] pos = positions[posIndex];
                int fontColor = fontColors[fontColorIndex];
                int bgColor = bgColors[bgColorIndex];
                String text = testStrings[textIndex];

                int xPos = pos[0];
                int yPos = pos[1];


                int maxChars = (128 - xPos) / (fontSize / 2);
                String displayText = text.length() > maxChars ? text.substring(0, maxChars) : text;

                appendText(String.format("Testing: FontSize=%d, X=%d, Y=%d, FontColor=%d, BgColor=%d, Text=%s\n",
                        fontSize, xPos, yPos, fontColor, bgColor, displayText));

                try {
                    lcdClearScreen("lcdClearScreen");
                    int result = mSystemBinderService.lcdDisplayString(xPos, yPos, displayText, fontSize, fontColor, bgColor);
                    Log.e(TAG, String.format("Result: %d | FontSize=%d, X=%d, Y=%d, Text=%s",
                            result, fontSize, xPos, yPos, displayText));

                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText("lcdDisplayString", e.getMessage());
                }


                index[0]++;
                handler.postDelayed(this, 1000);
            }
        };


        handler.post(testTask);

    }


    private LcdInfo requireLcdInfo(String itemName) {
        LcdInfo info = mLcdUtils.lcdGetDevLcdStatus();
        if (info == null) {
            appendText(itemName + " Result: lcdGetDevLcdStatus=null\n");
            return null;
        }
        return info;
    }


    /**
     * 全屏居中显示字符串（自动字号）
     */
    public void lcdDisplayStringFullScreen(String itemName, String text) {
        appendText(itemName);
        mLcdUtils.lcdClearScreenWithResult();

        LcdInfo lcdInfo = mLcdUtils.lcdGetDevLcdStatus();
        if (lcdInfo == null) {
            appendText(itemName + " Result: lcdInfo=null\n");
            return;
        }


        int screenWidth = lcdInfo.getWidth();
        int screenHeight = lcdInfo.getHeight();


        int fontSize = mLcdUtils.calculateOptimalFontSize(text, screenWidth, screenHeight);

        int textHeight = fontSize;
        int yPos = (screenHeight - textHeight) / 2;
        int xPos = 0;

        try {
            int result = mSystemBinderService.lcdDisplayString(
                    xPos,
                    yPos,
                    text,
                    fontSize,
                    0xFFFF,
                    0x0000
            );
            if (result != 0) {
                Log.e(TAG, "lcdDisplayStringFullScreen Error: " + result);
            }
        } catch (RemoteException e) {
            Log.e(TAG, "lcdDisplayStringFullScreen Error: " + e.getMessage());
        }
    }

    private void lcdClearScreen(String itemName) {
        appendText(itemName);
 // LcdUtils /
        int ret = mLcdUtils.lcdClearScreenWithResult();
        appendText(itemName + " Result: " + ret + "\n");
    }


    private void lcdFillScreen(String itemName) {
        appendText(itemName);

        mLcdUtils.lcdClearScreenWithResult();

        LcdInfo info = requireLcdInfo(itemName);
        if (info == null) return;

        int width = info.getWidth();
        int height = info.getHeight();


        int x1 = 0;
        int y1 = 0;
        int x2 = width - 1;
        int y2 = height - 1;

 // (type==1): ;
        int color = (info.getType() == 1) ? 0xFFFF : 0xF800;

        int ret = mLcdUtils.lcdFillScreen(x1, y1, x2, y2, color);
        appendText(itemName + " (" + width + "x" + height + ") Result: " + ret + "\n");
    }


    /**
     * 绘制矩形边框
     * 黑白屏：白色，边框 2 像素
     * 彩屏：蓝色，边框 4 像素
     * 黑白屏 0xFFFF
     * 彩屏
     * 0xF800;
     * 0x001F;
     * 0x07E0;
     */
    private void lcdDrawRectangle(String itemName) {
        appendText(itemName);
        mLcdUtils.lcdClearScreenWithResult();

        LcdInfo info = mLcdUtils.lcdGetDevLcdStatus();
        if (info == null) {
            appendText(itemName + " Result: lcdInfo=null\n");
            return;
        }

        int width = info.getWidth();
        int height = info.getHeight();

        int x1 = 0;
        int y1 = 0;
        int x2 = width - 1;
        int y2 = height - 1;


        int color = (info.getType() == 1) ? 0xFFFF : 0x001F;
 // 2px, 4px
        int thickness = (info.getType() == 1) ? 2 : 4;
        int ret = 0;

        for (int i = 0; i < thickness; i++) {
            int xx1 = x1 + i;
            int yy1 = y1 + i;
            int xx2 = x2 - i;
            int yy2 = y2 - i;


            if (xx1 >= xx2 || yy1 >= yy2) {
                break;
            }

            ret = mLcdUtils.lcdDrawRectangle(xx1, yy1, xx2, yy2, color);
            if (ret != 0) {
                break;
            }
        }

        appendText(itemName + " (" + width + "x" + height + ") thickness="
                + thickness + " Result: " + ret + "\n");
    }


    /**
     * 绘制直线
     * 彩屏
     * (0,0) ───────────▶ X (320)
     * │
     * │
     * ▼
     * Y (170)
     * 黑白屏
     * (0,0) ─────────────▶ X (128)
     * │
     * │
     * ▼
     * Y (32)
     */
    private void lcdDrawLine(String itemName) {
        appendText(itemName);


        mLcdUtils.lcdClearScreenWithResult();

        LcdInfo info = mLcdUtils.lcdGetDevLcdStatus();
        if (info == null) {
            appendText(itemName + " Result: lcdInfo=null\n");
            return;
        }

        int width = info.getWidth();
        int height = info.getHeight();


        int y = height / 2;

        int x1 = 0;
        int y1 = y;
        int x2 = width - 1;
        int y2 = y;


        int color = (info.getType() == 1) ? 0xFFFF : 0x07E0;

        int ret = mLcdUtils.lcdDrawLine(x1, y1, x2, y2, color);
        appendText(itemName + " (" + width + "x" + height + ") Result: " + ret + "\n");
    }


    private void lcdDrawPicture(String itemName) {
        appendText(itemName);

 // 1. ( )
        mLcdUtils.lcdClearScreenWithResult();

        LcdInfo lcdInfo = mLcdUtils.lcdGetDevLcdStatus();
        if (lcdInfo == null) {
            appendText(itemName + " Result: lcdInfo=null\n");
            return;
        }

        LogUtils.e("lcdGetDevLcdStatus: " + lcdInfo.toString());

        Bitmap bitmap;

        switch (lcdInfo.getType()) {
            case 1:

                bitmap = mLcdUtils.createTextBitmap(printMsg(), true, 16);
                int ret = mLcdUtils.lcdDrawPictureWithResult(bitmap);
                appendText(itemName + " Result: " + ret + "\n");
                break;

            case 2:
 // cache + BMP -> mLcdUtils.clearColorLcdCache
                bitmap = mLcdUtils.createTextBitmap(printMsg(), true, 48);
                mLcdUtils.drawBitmapForColorLcdWithCache(LCDSecondaryDisplayScreenActivity.this, bitmap);
                appendText(itemName + " Result: success\n");
                break;

            default:
                appendText(itemName + " Result: unknown lcd type\n");
                break;
        }
    }


    private void lcdRefreshScreen(String itemName) {
        appendText(itemName);
        int ret = mLcdUtils.lcdRefreshScreenWithResult();
        appendText(itemName + " Result: " + ret + "\n");
    }

    private void lcdDisplayQrUrl(String itemName, String url) {
        appendText(itemName);

        try {
            int initRet = mLcdUtils.lcdInit();
            appendText("lcdInit ret=" + initRet);

            LcdInfo info = mLcdUtils.lcdGetDevLcdStatus();
            if (info == null) {
                appendErrorText(itemName, "lcdInfo is null");
                return;
            }

            appendText("lcdInfo=" + info.toString());

            if (info.getType() != 2) {
                appendErrorText(itemName, "Current LCD size does not support QR display");
                return;
            }


            Bitmap bitmap = createQrFullScreenBitmap(url, info.getWidth(), info.getHeight());
            if (bitmap == null) {
                appendErrorText(itemName, "createQrFullScreenBitmap failed");
                return;
            }


           /* String savePath = mLcdUtils.saveBitmapToPng(
                    LCDSecondaryDisplayScreenActivity.this,
                    bitmap,
                    "qr_debug_" + System.currentTimeMillis() + ".png"
            );

            appendText("QR bitmap savePath = " + savePath);*/

            mLcdUtils.lcdClearScreenWithResult();

            if (info.getType() == 2) {
                mLcdUtils.clearColorLcdCache(LCDSecondaryDisplayScreenActivity.this);
                mLcdUtils.drawBitmapForColorLcd(LCDSecondaryDisplayScreenActivity.this, bitmap);

            } else {
                int ret = mLcdUtils.lcdDrawPictureWithResult(bitmap);
                appendText("lcdDrawPicture ret=" + ret);
            }

            int refreshRet = mLcdUtils.lcdRefreshScreenWithResult();
            appendText("lcdRefresh ret=" + refreshRet);

        } catch (Exception e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private Bitmap createQrFullScreenBitmap(String url, int screenWidth, int screenHeight) throws Exception {
        int qrSize = Math.min(screenWidth, screenHeight) - 20;
        if (qrSize <= 0) return null;

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        BitMatrix matrix = new QRCodeWriter().encode(
                url,
                BarcodeFormat.QR_CODE,
                qrSize,
                qrSize,
                hints
        );

        Bitmap qrBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565);
        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                qrBitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        Bitmap fullBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(fullBitmap);
        canvas.drawColor(Color.WHITE);

        int left = (screenWidth - qrSize) / 2;
        int top = (screenHeight - qrSize) / 2;
        canvas.drawBitmap(qrBitmap, left, top, null);

        return fullBitmap;
    }


    private StringBuffer sbMsg = new StringBuffer();

    private String printMsg() {
        sbMsg.setLength(0);
        sbMsg.append("Welcome 980");
        LogUtils.i("printMsg: " + sbMsg.toString());
        return sbMsg.toString();
    }






    /*
     *使用副屏播放资源目录下的GIF动画（visa.gif）
     * 效果等同于 lcdDrawLocalPicture，但仅依赖单个GIF文件实现动画效果
     *@param itemName
     *            Log tag for distinguishing animation log information
     *            日志标识，用于区分不同动画日志
     * @param iDelayTime
     *            Sleep interval per frame (unit: ms). Smaller value means faster animation.
     *            Recommended range: 10~30. Do NOT set to 0 to avoid high CPU usage.
     *            单帧绘制休眠间隔（单位ms），数值越小动画速度越快；建议区间10~30，禁止设置为0防止CPU高占用
     * @param bDirectionLeftRight
     *            Animation rolling direction control
     *            true  = Forward play, scroll from right to left
     *            false = Reverse play, scroll from left to right
     *            动画滚动方向控制
     *            true  = 正向播放，从右向左滚动
     *            false = 反向播放，从左向右滚动
     */
    private void lcdDrawGifAnimation(String itemName,int iDelayTime,boolean bDirectionLeftRight) {
 // ( )
        stopLcdAnimation();

        try { Thread.sleep(50); } catch (InterruptedException ignored) {}

        mIsAnimationRunning = true;
        appendText(itemName + " Start playing the GIF animation...");
        LogUtils.e("lcdDrawGifAnimation 开始");

        mCurrentAnimationFuture = mLcdExecutor.submit(() -> {
            LogUtils.e("GIF Thread startup");
            Movie movie = null;
            InputStream is = null;
            Bitmap frame = null;
            Canvas canvas = null;
            try {
 // 1. GIF ( res/drawable/visa.gif )
                is = getResources().openRawResource(R.drawable.visa);
                movie = Movie.decodeStream(is);
                if (movie == null) {
                    LogUtils.e("GIF Decoding failed. The resource may be damaged.");
                    mIsAnimationRunning = false;
                    return;
                }
                LogUtils.e("GIF Decoding successful, wide=" + movie.width() + " High=" + movie.height());
            } catch (Exception e) {
                LogUtils.e("GIF Loading exception: " + e.getMessage() + e);
                mIsAnimationRunning = false;
                return;
            } finally {
                if (is != null) try {
                    is.close();
                } catch (IOException ignored) {
                }
            }

 // 2. LCD
            LcdInfo lcdInfo = mLcdUtils.lcdGetDevLcdStatus();
            if (lcdInfo == null) {
                LogUtils.e("The LCD has not been initialized and cannot be played.  LCD 未初始化，无法播放");
                mIsAnimationRunning = false;
                return;
            }
            int lcdW = lcdInfo.getWidth();
            int lcdH = lcdInfo.getHeight();
            int lcdType = lcdInfo.getType(); // 1= , 2=
            LogUtils.e("LCD 尺寸: " + lcdW + "x" + lcdH + " 类型: " + lcdType);

 // 3. ( , )
            int gifW = movie.width();
            int gifH = movie.height();
            if (gifW <= 0 || gifH <= 0) {
                gifW = lcdW;
                gifH = lcdH;
            }
            float scale = Math.min((float) lcdW / gifW, (float) lcdH / gifH);
            int drawW = (int) (gifW * scale);
            int drawH = (int) (gifH * scale);
            int offsetX = (lcdW - drawW) / 2;
            int offsetY = (lcdH - drawH) / 2;


            long startTime = System.currentTimeMillis();
            int duration = movie.duration();
 // 1000ms
            if (duration <= 0) duration = 1000;
            LogUtils.e("GIF Total duration: " + duration + "ms");

 // ========== : Bitmap, Canvas ==========
            frame = Bitmap.createBitmap(lcdW, lcdH, Bitmap.Config.RGB_565);
            canvas = new Canvas(frame);

            while (mIsAnimationRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    long elapsed = System.currentTimeMillis() - startTime;
                    if (bDirectionLeftRight){
                        int time = (int) (elapsed % duration);
                        movie.setTime(time);
                    }else {

                        int time = duration - (int) (elapsed % duration);
                        movie.setTime(time <= 0 ? duration : time);
                    }


                    canvas.drawColor(Color.WHITE);
                    canvas.save();
                    canvas.translate(offsetX, offsetY);
                    canvas.scale(scale, scale);
                    movie.draw(canvas, 0, 0);
                    canvas.restore();

 // 5. ( )
                    boolean success = false;
                    if (lcdType == 2) {

                        mLcdUtils.clearColorLcdCache(LCDSecondaryDisplayScreenActivity.this);
                        mLcdUtils.drawBitmapForColorLcd(LCDSecondaryDisplayScreenActivity.this, frame);
                        success = true;
                        LogUtils.e("Color screen drawing completed");
                    } else {

                        int ret = mLcdUtils.lcdDrawPictureWithResult(frame);
                        if (ret == 0) {
                            success = true;
                        } else {
                            LogUtils.e("lcdDrawPictureWithResult Failure，ret=" + ret);
                        }
                    }


                    int refreshRet = mLcdUtils.lcdRefreshScreenWithResult();
                    if (refreshRet != 0) {
                        LogUtils.e("Failed to refresh the screen，ret=" + refreshRet);
                    }

 // 12ms
                    Thread.sleep(iDelayTime);
                } catch (InterruptedException e) {
                    LogUtils.e("GIF The playback thread has been interrupted.");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    LogUtils.e("GIF Playback error: " + e.getMessage() + e);
                    break;
                }
            }

 // Bitmap
            if (frame != null && !frame.isRecycled()) {
                frame.recycle();
            }
            mIsAnimationRunning = false;
            LogUtils.e("GIF animation has stopped.");
        });
    }
}