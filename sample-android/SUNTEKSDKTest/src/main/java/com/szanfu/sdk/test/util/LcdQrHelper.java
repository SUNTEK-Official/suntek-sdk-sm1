package com.szanfu.sdk.test.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.szanfu.sdk.test.entity.LcdInfo;

import java.util.Hashtable;

public final class LcdQrHelper {

    private static final String TAG = "LcdQrHelper";

    private final Context appContext;
    private final LcdUtils lcdUtils;

    public LcdQrHelper(Context context, LcdUtils lcdUtils) {
        this.appContext = context.getApplicationContext();
        this.lcdUtils = lcdUtils;
    }

    /**
     * 只保留一个 url 参数
     * true = 显示成功
     * false = 显示失败
     */
    public boolean lcdDisplayQrUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "lcdDisplayQrUrl: url is empty");
            return false;
        }

        try {
            int initRet = lcdUtils.lcdInit();
            Log.i(TAG, "lcdInit ret=" + initRet);

            LcdInfo lcdInfo = lcdUtils.lcdGetDevLcdStatus();
            if (lcdInfo == null) {
                Log.e(TAG, "lcdDisplayQrUrl: lcdInfo is null");
                return false;
            }

            Log.i(TAG, "lcdInfo=" + lcdInfo);

            Bitmap bitmap = createQrFullScreenBitmap(url, lcdInfo.getWidth(), lcdInfo.getHeight());
            if (bitmap == null) {
                Log.e(TAG, "lcdDisplayQrUrl: createQrFullScreenBitmap failed");
                return false;
            }

            bitmap = ensureRgb565(bitmap);

            int clearRet = lcdUtils.lcdClearScreenWithResult();
            Log.i(TAG, "lcdClearScreen ret=" + clearRet);

            if (lcdInfo.getType() == 2) {

                lcdUtils.clearColorLcdCache(appContext);
                lcdUtils.drawBitmapForColorLcd(appContext, bitmap);
            } else {
                int drawRet = lcdUtils.lcdDrawPictureWithResult(bitmap);
                Log.i(TAG, "lcdDrawPictureWithResult ret=" + drawRet);
                if (drawRet != 0) {
                    return false;
                }
            }

            int refreshRet = lcdUtils.lcdRefreshScreenWithResult();
            Log.i(TAG, "lcdRefreshScreen ret=" + refreshRet);

            return refreshRet == 0;

        } catch (Exception e) {
            Log.e(TAG, "lcdDisplayQrUrl error: " + e.getMessage(), e);
            return false;
        }
    }

    private Bitmap ensureRgb565(Bitmap src) {
        if (src == null) return null;
        if (src.getConfig() == Bitmap.Config.RGB_565) {
            return src;
        }
        return src.copy(Bitmap.Config.RGB_565, false);
    }

    private Bitmap createQrFullScreenBitmap(String url, int screenWidth, int screenHeight) throws Exception {
        if (TextUtils.isEmpty(url) || screenWidth <= 0 || screenHeight <= 0) {
            return null;
        }

        int margin = 12;
        int qrSize = Math.min(screenWidth, screenHeight) - margin * 2;
        if (qrSize <= 0) {
            return null;
        }

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

        Bitmap qrBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.ARGB_8888);
        for (int y = 0; y < qrSize; y++) {
            for (int x = 0; x < qrSize; x++) {
                qrBitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        Bitmap fullBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(fullBitmap);
        canvas.drawColor(Color.WHITE);

        int left = (screenWidth - qrSize) / 2;
        int top = (screenHeight - qrSize) / 2;
        canvas.drawBitmap(qrBitmap, left, top, null);

        return fullBitmap;
    }
}