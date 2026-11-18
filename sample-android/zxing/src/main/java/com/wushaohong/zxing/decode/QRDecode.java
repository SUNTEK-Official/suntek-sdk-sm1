package com.wushaohong.zxing.decode;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.GlobalHistogramBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.wushaohong.zxing.OnScannerCompletionListener;
import com.wushaohong.zxing.camera.CameraManager;
import com.wushaohong.zxing.common.Scanner;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class QRDecode {

    public static final Map<DecodeHintType, Object> HINTS = new EnumMap<>(DecodeHintType.class);

    static {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        HINTS.put(DecodeHintType.POSSIBLE_FORMATS, formats);
        HINTS.put(DecodeHintType.CHARACTER_SET, "utf-8");
    }

    private QRDecode() {
    }

    /**
     * 解析二维码图片
     *
     * @param picturePath 图片地址
     * @param listener
     * @return
     */
    public static void decodeQR(String picturePath, OnScannerCompletionListener listener) {
        try {
            decodeQR(loadBitmap(picturePath), listener);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析二维码图片
     *
     * @param srcBitmap
     * @param listener
     * @return
     */
    public static void decodeQR(Bitmap srcBitmap, final OnScannerCompletionListener listener) {
        Result result = null;
        if (srcBitmap != null) {
            int width = srcBitmap.getWidth();
            int height = srcBitmap.getHeight();
            int[] pixels = new int[width * height];
            srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);
 // RGBLuminanceSource
            RGBLuminanceSource source = new RGBLuminanceSource(width, height, pixels);

            BinaryBitmap binaryBitmap = new BinaryBitmap(new GlobalHistogramBinarizer(source));
            QRCodeReader reader = new QRCodeReader();
            try {
                result = reader.decode(binaryBitmap, HINTS);
            } catch (NotFoundException e) {
                e.printStackTrace();
            } catch (ChecksumException e) {
                e.printStackTrace();
            } catch (FormatException e) {
                e.printStackTrace();
            }
        }
        if (listener != null) {
            listener.onScannerCompletion(result, Scanner.parseResult(result), srcBitmap);
        }
    }

    private static Bitmap loadBitmap(String picturePath) throws FileNotFoundException {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, opt);

        int picWidth = opt.outWidth;
        int picHeight = opt.outHeight;

        int screenWidth = CameraManager.MAX_FRAME_WIDTH;
        int screenHeight = CameraManager.MAX_FRAME_HEIGHT;
 // isSampleSize , 2 1/2
        opt.inSampleSize = 1;

        if (picWidth > picHeight) {
            if (picWidth > screenWidth)
                opt.inSampleSize = picWidth / screenWidth;
        } else {
            if (picHeight > screenHeight)
                opt.inSampleSize = picHeight / screenHeight;
        }
 // bitmap
        opt.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(picturePath, opt);
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + picturePath);
        }
        return bitmap;
    }
}
