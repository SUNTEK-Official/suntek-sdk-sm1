package com.szanfu.sdk.test.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * SDCard File Read/Write Utility Class (Android 13+)
 * SDCard 文件读写工具类（适配Android13+ 分区存储）
 *
 * No dangerous storage permissions required, automatic directory creation
 * 无需危险存储权限，自动创建目录，支持带路径文件名/纯文件名
 */
public class SdCardFileUtils {
    // Log tag

    private static final String TAG = "SdCardFileUtils";

    // Base storage directory: SDCard/Documents/MyAppFiles
 // SD / /MyAppFiles
    private static final String BASE_DIRECTORY = Environment.DIRECTORY_DOCUMENTS + "/MyAppFiles";

    /**
     * Write content to SDCard
     * 写入文件到SD卡
     *
     * @param context      Context context / 上下文
     * @param fileName     File name (can with path, e.g. test.txt / 2026/03/test.txt)
     *                     文件名（可带路径，如 test.txt / 2026/03/test.txt）
     * @param content      File content to write / 要写入的文件内容
     * @return true=success, false=failure / true=成功，false=失败
     */
    public static boolean writeFileToSdCard(Context context, String fileName, String content) {
        // Null parameter check

        if (context == null || fileName == null || fileName.isEmpty() || content == null) {
            Log.e(TAG, "Write failed: Parameter is null / 写入失败：参数为空");
            return false;
        }

        OutputStream outputStream = null;

        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri contentUri;


            String dirPath = "";
            String pureFileName = fileName;
            int lastSlashIndex = fileName.lastIndexOf('/');
            if (lastSlashIndex != -1) {
                dirPath = fileName.substring(0, lastSlashIndex);
                pureFileName = fileName.substring(lastSlashIndex + 1);
            }

            // Android 10+ use MediaStore (Scoped Storage)
 // Android 10+ MediaStore( )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();

 // ( , )
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, pureFileName);

 // ( )
                values.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");

 // (BASE_DIRECTORY + )
                String fullRelativePath = BASE_DIRECTORY + (dirPath.isEmpty() ? "" : "/" + dirPath);
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, fullRelativePath);

 // Uri
                contentUri = contentResolver.insert(MediaStore.Files.getContentUri("external"), values);

                if (contentUri == null) {
                    Log.e(TAG, "Write failed: Create file Uri failed / 写入失败：创建文件Uri失败");
                    return false;
                }


                outputStream = contentResolver.openOutputStream(contentUri);
            } else {
 // Android10
                java.io.File basePath = new File(Environment.getExternalStorageDirectory(), BASE_DIRECTORY);


                if (!basePath.exists()) {
                    boolean mkdirResult = basePath.mkdirs();
                    Log.d(TAG, "Create root dir: " + mkdirResult + " / 创建根目录：" + mkdirResult);
                }


                java.io.File targetFile = new File(basePath, fileName);


                java.io.File parentFile = targetFile.getParentFile();
                if (parentFile != null && !parentFile.exists()) {
                    boolean mkdirResult = parentFile.mkdirs();
                    Log.d(TAG, "Create multi-level dir: " + mkdirResult + " / 创建多级目录：" + mkdirResult);
                }

                outputStream = new FileOutputStream(targetFile);
            }

            if (outputStream == null) {
                Log.e(TAG, "Write failed: Output stream is null / 写入失败：输出流为空");
                return false;
            }


            outputStream.write(content.getBytes());
            outputStream.flush();

            Log.i(TAG, "Write success! File: " + fileName + " / 写入成功！文件：" + fileName);
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Write exception: " + e.getMessage() + " / 写入异常：" + e.getMessage(), e);
            return false;
        } finally {

            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "Close stream exception: " + e.getMessage() + " / 关闭流异常：" + e.getMessage());
                }
            }
        }
    }

    /**
     * Read file content from SDCard
     * 从SD卡读取文件内容
     *
     * @param context      Context / 上下文
     * @param fileName     File name (same as writing) / 文件名（与写入时一致）
     * @return File content / null = failure
     *         文件内容，null=读取失败
     */
    public static String readFileFromSdCard(Context context, String fileName) {

        if (context == null || fileName == null || fileName.isEmpty()) {
            Log.e(TAG, "Read failed: Parameter is null / 读取失败：参数为空");
            return null;
        }


        String dirPath = "";
        String pureFileName = fileName;
        int lastSlashIndex = fileName.lastIndexOf('/');
        if (lastSlashIndex != -1) {
            dirPath = fileName.substring(0, lastSlashIndex);
            pureFileName = fileName.substring(lastSlashIndex + 1);
        }

        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder content = new StringBuilder();

        try {
            Uri contentUri = MediaStore.Files.getContentUri("external");

 // (BASE_DIRECTORY + )
            String fullRelativePath = BASE_DIRECTORY + (dirPath.isEmpty() ? "" : "/" + dirPath);


            String selection = MediaStore.MediaColumns.RELATIVE_PATH + "=? AND "
                    + MediaStore.MediaColumns.DISPLAY_NAME + "=?";
            String[] selectionArgs = new String[]{fullRelativePath + "/", pureFileName}; // RELATIVE_PATH /

            ContentResolver contentResolver = context.getContentResolver();
            android.database.Cursor cursor = contentResolver.query(contentUri, null, selection, selectionArgs, null);


            if (cursor == null || !cursor.moveToFirst()) {
                Log.e(TAG, "Read failed: File not found / 读取失败：未找到文件 " + fileName);
                if (cursor != null) cursor.close();
                return null;
            }

 // Uri
            int uriIndex = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
            Uri fileUri = Uri.withAppendedPath(contentUri, String.valueOf(cursor.getLong(uriIndex)));
            cursor.close();


            inputStream = contentResolver.openInputStream(fileUri);
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));


            String line;
            while ((line = bufferedReader.readLine()) != null) {
                content.append(line).append("\n");
            }

            Log.i(TAG, "Read success! File: " + fileName + " / 读取成功！文件：" + fileName);
            return content.toString().trim();

        } catch (Exception e) {
            Log.e(TAG, "Read exception: " + e.getMessage() + " / 读取异常：" + e.getMessage(), e);
            return null;
        } finally {

            try {
                if (bufferedReader != null) bufferedReader.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Close stream exception: " + e.getMessage() + " / 关闭流异常：" + e.getMessage());
            }
        }
    }
}