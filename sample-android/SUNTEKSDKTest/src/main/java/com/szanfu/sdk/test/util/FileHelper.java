package com.szanfu.sdk.test.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileHelper {


    public static void writeToFile(Context context, String fileName, String fileContents) {
        FileOutputStream outputStream = null;
        try {

            File file = new File(context.getFilesDir(), fileName);
            file.createNewFile();

            outputStream = new FileOutputStream(file);
            outputStream.write(fileContents.getBytes());
            LogUtils.e("szaf"+ "writeToFile: 000-----------------" );
            outputStream.close();
            LogUtils.e("szaf"+ "writeToFile: 11-----------------" );
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    LogUtils.e( "writeToFile: ---33" );
                    outputStream.close();
                    LogUtils.e( "writeToFile: ---44" );
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    public static String readFromFile(Context context, String fileName) {
        try {
//            fileName = "/E/"+fileName;
            Log.e("szaf", "readFromFile: -------->>>" +fileName);
            InputStream inputStream = context.openFileInput(fileName);
            Log.e("szaf", "readFromFile: --------2222>>>" +fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
            inputStream.close();
            Log.e("szaf", "readFromFile: ---------》》》"+stringBuilder.toString() );
            return stringBuilder.toString();
        } catch (IOException e) {
            Log.e("szaf", "readFromFile: ----------->>>>>>>"+e );
            e.printStackTrace();
            return null;
        }
    }

    public static void deleteLocalTxtFile(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {

                Log.e("szaf", "deleteLocalTxtFile: ---文件删除成功->>" );
            } else {
                Log.e("szaf", "deleteLocalTxtFile: --文件删除失败-->>" );

            }
        } else {

        }
    }
}