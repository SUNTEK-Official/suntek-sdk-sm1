package com.szanfu.sdk.test.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastThreadUtils {
    public static void showToast(final Context context, final String message) {
        if (Looper.myLooper() == Looper.getMainLooper()) {

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        } else {

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    public static void showToast(final Context context, final String message, final int duration) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, message, duration).show();
        } else {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, duration).show();
                }
            });
        }
    }
}


//ToastUtils.showToast(QRScanDivActivity.this,
//        "DEFINED_CODE ： " + iRunLocalCount + "/" + iNeedsRun);