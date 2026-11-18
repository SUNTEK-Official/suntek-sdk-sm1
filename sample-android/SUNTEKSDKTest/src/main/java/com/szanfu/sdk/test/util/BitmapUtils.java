package com.szanfu.sdk.test.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class BitmapUtils {


    /**
     * 将Bitmap转换为byte数组（默认使用PNG格式）
     * @param bitmap 源Bitmap
     * @return 字节数组，转换失败返回null
     */
    public static byte[] bitmapToBytes(Bitmap bitmap) {
        return bitmapToBytes(bitmap, CompressFormat.PNG, 100);
    }

    /**
     * 将Bitmap转换为JPEG格式的byte数组
     * @param bitmap 源Bitmap
     * @param quality 压缩质量 (0-100)
     * @return 字节数组，转换失败返回null
     */
    public static byte[] bitmapToJpeg(Bitmap bitmap, int quality) {
        return bitmapToBytes(bitmap, CompressFormat.JPEG, quality);
    }

    /**
     * 将Bitmap转换为PNG格式的byte数组（无损）
     * @param bitmap 源Bitmap
     * @return 字节数组，转换失败返回null
     */
    public static byte[] bitmapToPng(Bitmap bitmap) {
        return bitmapToBytes(bitmap, CompressFormat.PNG, 100);
    }

    /**
     * 核心转换方法
     * @param bitmap 源Bitmap
     * @param format 压缩格式
     * @param quality 压缩质量 (0-100)
     * @return 字节数组，转换失败返回null
     */
    public static byte[] bitmapToBytes(Bitmap bitmap, CompressFormat format, int quality) {
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        ByteArrayOutputStream outputStream = null;
        try {
            outputStream = new ByteArrayOutputStream();
            bitmap.compress(format, quality, outputStream);
            outputStream.flush();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (outputStream != null) outputStream.close();
            } catch (Exception ignored) {}
        }
    }



    /**
     * 计算byte[]数据转为Bitmap后的高度值（宽度固定为384px，高度按比例缩放并向上取整）
     *
     * @param data 图片的原始字节数组
     * @return 缩放后的Bitmap高度值（向上取整）。如果数据无效返回-1
     */
    public static int calculateScaledHeight(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }

 // 1: ( )
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);


        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;


        if (originalWidth <= 0 || originalHeight <= 0) {
            return -1;
        }

 // 3: ( )
 // = ( / ) *
        double heightRatio = (double) originalHeight / originalWidth;
        double scaledHeight = heightRatio * 384; // 384

 // 4: ( )
        return (int) Math.ceil(scaledHeight);
    }
}
