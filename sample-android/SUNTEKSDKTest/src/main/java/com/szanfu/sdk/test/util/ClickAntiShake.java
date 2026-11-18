package com.szanfu.sdk.test.util;

import java.util.HashMap;
import java.util.Map;

/**
 * RecyclerView防狂点工具类（支持全局/单个Item控制）
 */
public class ClickAntiShake {

    private static long GLOBAL_LAST_TIME = 0;
    private static final long DEFAULT_DURATION = 1000;

 // Item ( )
    private static final Map<Long, Long> ITEM_LAST_TIME_MAP = new HashMap<>();


    /**
     * 全局防抖检查（默认500ms）
     */
    public static boolean isGlobalFastClick() {
        long current = System.currentTimeMillis();
        long difference = current - GLOBAL_LAST_TIME;
        LogUtils.e(" current difference:"+ difference);
        if (difference > DEFAULT_DURATION || difference < -DEFAULT_DURATION) {
            GLOBAL_LAST_TIME = current;
            return false;
        }
        return true;
    }

    /**
     * 全局防抖检查（自定义时间）
     */
    public static boolean isGlobalFastClick(long duration) {
        long current = System.currentTimeMillis();
        if (current - GLOBAL_LAST_TIME > duration) {
            GLOBAL_LAST_TIME = current;
            return false;
        }
        return true;
    }

 // Item -----------------//
    /**
     * 单个Item防抖检查（默认500ms）
     * @param uniqueId 每个Item的唯一标识（推荐使用数据id）
     */
    public static boolean isItemFastClick(long uniqueId) {
        long current = System.currentTimeMillis();
        Long last = ITEM_LAST_TIME_MAP.get(uniqueId);

        if (last == null || current - last > DEFAULT_DURATION) {
            ITEM_LAST_TIME_MAP.put(uniqueId, current);
            return false;
        }
        return true;
    }

    /**
     * 单个Item防抖检查（自定义时间）
     */
    public static boolean isItemFastClick(long uniqueId, long duration) {
        long current = System.currentTimeMillis();
        Long last = ITEM_LAST_TIME_MAP.get(uniqueId);

        if (last == null || current - last > duration) {
            ITEM_LAST_TIME_MAP.put(uniqueId, current);
            return false;
        }
        return true;
    }
}
