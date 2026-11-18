package com.szanfu.sdk.test.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class WatermarkUtils {
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard/Android/DeviceMSG.txt";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    /**
     * 添加水印
     *
     * @param activity     Activity
     * @param text         水印文字
     * @param textColor    文字颜色
     * @param textSize     文字大小
     * @param rotation    文字旋转角度
     * @param repeat       是否重复填满屏幕
     * @param position     水印位置 (1:左上角 2:右上角 3:左下角 4:右下角)
     */
    public static void addWatermark(Activity activity, String text, int textColor, int textSize,
                                    int rotation, boolean repeat, int position) {


/*
        String libVersion="";

        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        }else {
            String strMsg =    readFile();
            try {
                JsonArray jsonArray = JsonParser.parseString(strMsg).getAsJsonArray();
                JsonObject firstObject = jsonArray.get(0).getAsJsonObject();

                libVersion = firstObject.get("Lib_Version").getAsString();
                LogUtils.e("libVersion :" +libVersion );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */



        String strSystemCoreVersionCode = getVersionName(activity,"com.szanfu.core.server");
        String[] strSystem=strSystemCoreVersionCode.split("_");
        strSystemCoreVersionCode =strSystem[0];
        String strSDKServiceVersionCode = getVersionName(activity,"com.szanfu.sdk.service");
        String[] strSDK = strSDKServiceVersionCode.split("_");
        strSDKServiceVersionCode=strSDK[0];
        String strSDKTestVersionCode = getVersionName(activity,"com.szanfu.sdk.test");
        String[] strSDKTest = strSDKTestVersionCode.split("_");
        strSDKTestVersionCode = strSDKTest[0];
        String strShow = "VCore:"+ strSystemCoreVersionCode+
                "; VSDK:"+ strSDKServiceVersionCode+
//                "; VLib:"+libVersion+
                "; VTest:"+strSDKTestVersionCode;

        WatermarkView watermarkView = new WatermarkView(activity, strShow + text, textColor, 24, rotation, repeat, position);
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        rootView.addView(watermarkView);
    }

    /**
     * 移除水印
     */
    public static void removeWatermark(Activity activity) {
        ViewGroup rootView = activity.findViewById(android.R.id.content);
        for (int i = 0; i < rootView.getChildCount(); i++) {
            View view = rootView.getChildAt(i);
            if (view instanceof WatermarkView) {
                rootView.removeView(view);
                i--;
            }
        }
    }

    private static class WatermarkView extends View {
        private String mText;
        private int mTextColor;
        private int mTextSize;
        private int mRotation;
        private boolean mRepeat;
        private int mPosition;
        private Paint mPaint;

        public WatermarkView(Activity activity, String text, int textColor, int textSize,
                             int rotation, boolean repeat, int position) {
            super(activity);
            this.mText = text;
            this.mTextColor = textColor;
            this.mTextSize = textSize;
            this.mRotation = rotation;
            this.mRepeat = repeat;
            this.mPosition = position;

            init();
        }

        private void init() {
            mPaint = new Paint();
            mPaint.setColor(mTextColor);
            mPaint.setTextSize(mTextSize);
            mPaint.setAntiAlias(true);
            mPaint.setAlpha(80);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            int width = getWidth();
            int height = getHeight();

            if (mRepeat) {

                drawRepeatedWatermark(canvas, width, height);
            } else {

                drawSingleWatermark(canvas, width, height);
            }
        }

        private void drawSingleWatermark(Canvas canvas, int width, int height) {
            canvas.save();


            Rect textBounds = new Rect();
            mPaint.getTextBounds(mText, 0, mText.length(), textBounds);
            int textWidth = textBounds.width();
            int textHeight = textBounds.height();


            int x = 0;
            int y = 0;
            int padding = 20;

            switch (mPosition) {
                case 1:
                    x = padding;
                    y = padding + textHeight;
                    break;
                case 2:
                    x = width - textWidth - padding;
                    y = padding + textHeight;
                    break;
                case 3:
                    x = padding;
                    y = height - 60;
                    break;
                case 4:
                    x = width - textWidth - padding;
                    y = height - padding;
                    break;
            }


            canvas.rotate(mRotation, x + textWidth / 2f, y - textHeight / 2f);
            canvas.drawText(mText, x, y, mPaint);
            canvas.restore();
        }

        private void drawRepeatedWatermark(Canvas canvas, int width, int height) {
            canvas.save();

            Rect textBounds = new Rect();
            mPaint.getTextBounds(mText, 0, mText.length(), textBounds);
            int textWidth = textBounds.width();
            int textHeight = textBounds.height();


            int spacingX = textWidth * 2;
            int spacingY = textHeight * 3;


            int startX = 0;
            int startY = 0;




            for (int y = startY; y < height; y += spacingY) {
                for (int x = startX; x < width; x += spacingX) {
                    canvas.save();

                    canvas.rotate(mRotation, x + textWidth / 2f, y + textHeight / 2f);
                    canvas.drawText(mText, x, y + textHeight, mPaint);
                    canvas.restore();
                }
            }

            canvas.restore();
        }
    }




    public static String getVersionName(Context context, String packageName) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(packageName, 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/DeviceMSG.txt");
        if(!file.exists())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e("stringBuilder.toString() :"+stringBuilder.toString());
        return stringBuilder.toString();
    }
}
