package com.szanfu.sdk.test.util;

import android.util.Log;
import android.view.View;

import java.util.Calendar;

public abstract class NoDoubleClickListener implements View.OnClickListener {

    public static final int MIN_CLICK_DELAY_TIME = 1000;

    private long lastClickTime = 0;

    protected abstract void onNoDoubleClick(View v);

    @Override

    public void onClick(View v) {

        long currentTime = Calendar.getInstance().getTimeInMillis();

        long time = currentTime - lastClickTime;
        Log.e("szaf", "onClick: " +time);

        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME|| currentTime - lastClickTime < -MIN_CLICK_DELAY_TIME) {

            lastClickTime = currentTime;

            onNoDoubleClick(v);

        }

    }

}
