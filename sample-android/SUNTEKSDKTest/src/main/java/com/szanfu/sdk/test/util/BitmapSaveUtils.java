package com.szanfu.sdk.test.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapSaveUtils {

    private static final String TAG = "BitmapSaveUtils";

    /**
     * 将Bitmap保存为图片到应用的私有目录
     *
     * @param context  上下文
     * @param bitmap   要保存的Bitmap
     * @param fileName 文件名（带扩展名，如"image.jpg"）
     * @param format   图片格式（如Bitmap.CompressFormat.JPEG）
     * @param quality  图片质量（0-100）
     * @return 保存成功返回文件的绝对路径，失败返回null
     */
    public static String saveToPrivateStorage(Context context, Bitmap bitmap, String fileName, CompressFormat format, int quality) {
        if (context == null || bitmap == null || fileName == null) {
            Log.e(TAG, "Invalid parameters");
            return null;
        }


        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (storageDir == null) {
            Log.e(TAG, "Failed to get private storage directory");
            return null;
        }


        File imageFile = new File(storageDir, fileName);
        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(format, quality, fos);
            fos.flush();
            Log.d(TAG, "Bitmap saved to private storage: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap to private storage", e);
            return null;
        }
    }

    /**
     * 将Bitmap保存为图片到公共目录（相册）
     *
     * @param context  上下文
     * @param bitmap   要保存的Bitmap
     * @param fileName 文件名（带扩展名，如"image.jpg"）
     * @param format   图片格式（如Bitmap.CompressFormat.JPEG）
     * @param quality  图片质量（0-100）
     * @return 保存成功返回文件的Uri，失败返回null
     */
    public static Uri saveToPublicStorage(Context context, Bitmap bitmap, String fileName, CompressFormat format, int quality) {
        if (context == null || bitmap == null || fileName == null) {
            Log.e(TAG, "Invalid parameters");
            return null;
        }

 // ContentValues
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, getMimeType(format));
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

 // ContentResolver
        ContentResolver resolver = context.getContentResolver();
        Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        if (imageUri == null) {
            Log.e(TAG, "Failed to create Uri for image");
            return null;
        }


        try (OutputStream outputStream = resolver.openOutputStream(imageUri)) {
            if (outputStream == null) {
                Log.e(TAG, "Failed to open output stream");
                return null;
            }
            bitmap.compress(format, quality, outputStream);
            outputStream.flush();
            Log.d(TAG, "Bitmap saved to public storage: " + imageUri.toString());
            return imageUri;
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap to public storage", e);
            return null;
        }
    }

    /**
     * 根据CompressFormat获取MIME类型
     *
     * @param format 图片格式
     * @return MIME类型
     */
    private static String getMimeType(CompressFormat format) {
        switch (format) {
            case JPEG:
                return "image/jpeg";
            case PNG:
                return "image/png";
            case WEBP:
                return "image/webp";
            default:
                return "image/*";
        }
    }
}
