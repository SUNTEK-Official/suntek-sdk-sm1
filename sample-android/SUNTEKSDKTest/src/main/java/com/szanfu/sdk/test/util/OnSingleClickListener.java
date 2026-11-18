package com.szanfu.sdk.test.util;


import android.view.View;

import java.util.Calendar;

public abstract class OnSingleClickListener implements View.OnClickListener {
    public static final int MIN_CLICK_DELAY_TIME = 5000;

    private long lastClickTime = 0;

    protected abstract void onSingleClick(View v);

    @Override

    public void onClick(View v) {

        long currentTime = Calendar.getInstance().getTimeInMillis();

        if (currentTime - lastClickTime > MIN_CLICK_DELAY_TIME) {

            lastClickTime = currentTime;

            onSingleClick(v);

        }

    }
}
