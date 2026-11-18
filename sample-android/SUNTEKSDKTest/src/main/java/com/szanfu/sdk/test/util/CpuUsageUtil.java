package com.szanfu.sdk.test.util;
import android.os.SystemClock;
import android.util.Log;

/**
 * Android 13+ 100% 可用 CPU 使用率工具类
 * 无权限、不崩溃、不读取 /proc/stat
 */
public class CpuUsageUtil {

    private static CpuUsageUtil instance;
    private long lastCpuTime = 0;
    private long lastTime = 0;

    private CpuUsageUtil() {}

    public static CpuUsageUtil getInstance() {
        if (instance == null) {
            instance = new CpuUsageUtil();
        }
        return instance;
    }

    /**
     * 获取 当前应用 CPU 使用率（最稳定、100%可用）
     * 必须在子线程调用
     */
    public float getAppCpuUsage() {
        try {
            long now = SystemClock.elapsedRealtime();
            long cpuTime = android.os.Process.getElapsedCpuTime();

            if (lastTime == 0 || lastCpuTime == 0) {
                lastTime = now;
                lastCpuTime = cpuTime;
                SystemClock.sleep(100);
                return getAppCpuUsage();
            }

            long timeDiff = now - lastTime;
            long cpuDiff = cpuTime - lastCpuTime;

            lastTime = now;
            lastCpuTime = cpuTime;

            if (timeDiff <= 0) return 0;

            float usage = (cpuDiff * 1.0f / timeDiff) * 100f;
            usage = Math.min(100, Math.max(0, usage));
            return Math.round(usage * 10) / 10.0f;

        } catch (Exception e) {
            Log.e("CpuUsageUtil", "获取CPU失败", e);
            return 0;
        }
    }

    public void reset() {
        lastCpuTime = 0;
        lastTime = 0;
    }
}