package com.szanfu.sdk.test.util;

public class SingleClickUtil {
    private static long lastClickTime = 0;
    private static final long INTERVAL_TIME = 2000;

    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastClickTime < INTERVAL_TIME) {
            return true;
        }
        lastClickTime = currentTime;
        return false;
    }
}

