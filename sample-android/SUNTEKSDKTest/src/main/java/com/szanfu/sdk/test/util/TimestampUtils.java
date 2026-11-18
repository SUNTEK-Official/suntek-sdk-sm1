package com.szanfu.sdk.test.util;

import android.os.SystemClock;

public class TimestampUtils {

    /**
     * 方法1: 使用System.currentTimeMillis()获取时间戳
     * 适合计算日期时间相关的间隔
     */
    public static long calculateTimeDifferenceInSeconds(long startTimestamp, long endTimestamp) {

        long diffInMillis = endTimestamp - startTimestamp;

 // ( )
        long diffInSeconds = diffInMillis / 1000;

        return diffInSeconds;
    }

    /**
     * 方法2: 精确到小数秒的时间差
     */
    public static double calculateExactTimeDifferenceInSeconds(long startTimestamp, long endTimestamp) {
        long diffInMillis = endTimestamp - startTimestamp;
        return diffInMillis / 1000.0;
    }

    /**
     * 方法3: 使用SystemClock.elapsedRealtime()
     * 适合计算耗时、性能测量等，不受系统时间调整影响
     */
    public static long calculateElapsedTimeInSeconds() {
        long startTime = SystemClock.elapsedRealtime();


        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long endTime = SystemClock.elapsedRealtime();
        long diffInMillis = endTime - startTime;

        return diffInMillis / 1000;
    }

    /**
     * 方法4: 使用SystemClock.uptimeMillis()
     * 设备启动后的时间，不包含深度睡眠时间
     */
    public static long calculateUptimeDifferenceInSeconds(long startUptime, long endUptime) {
        long diffInMillis = endUptime - startUptime;
        return diffInMillis / 1000;
    }
}
