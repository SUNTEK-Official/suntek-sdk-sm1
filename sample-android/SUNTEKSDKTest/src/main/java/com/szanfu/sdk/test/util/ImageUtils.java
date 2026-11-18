package com.szanfu.sdk.test.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtils {

    private static final String TAG = "ImageUtils";

    /**
     * 将byte[]保存为图片文件
     *
     * @param context  上下文
     * @param data     byte[]数据
     * @param fileName 保存的文件名（不包括路径）
     * @return 保存的文件路径，如果保存失败则返回null
     */
    public static String saveImage(Context context, byte[] data, String fileName) {
        if (data == null || data.length == 0) {
            Log.e(TAG, "Byte array is empty or null");
            return null;
        }


        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, "External storage directory not available");
            return null;
        }


        File imageFile = new File(storageDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            fos.write(data);
            fos.flush();
            Log.d(TAG, "Image saved to: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save image", e);
            return null;
        }
    }

    /**
     * 将byte[]转换为Bitmap
     *
     * @param data byte[]数据
     * @return Bitmap对象，如果转换失败则返回null
     */
    public static Bitmap byteArrayToBitmap(byte[] data) {
        if (data == null || data.length == 0) {
            Log.e(TAG, "Byte array is empty or null");
            return null;
        }

        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    /**
     * 将Bitmap保存为图片文件
     *
     * @param context  上下文
     * @param bitmap   Bitmap对象
     * @param fileName 保存的文件名（不包括路径）
     * @return 保存的文件路径，如果保存失败则返回null
     */
    public static String saveBitmap(Context context, Bitmap bitmap, String fileName) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap is null");
            return null;
        }


        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, "External storage directory not available");
            return null;
        }


        File imageFile = new File(storageDir, fileName);

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            Log.d(TAG, "Bitmap saved to: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap", e);
            return null;
        }
    }
}