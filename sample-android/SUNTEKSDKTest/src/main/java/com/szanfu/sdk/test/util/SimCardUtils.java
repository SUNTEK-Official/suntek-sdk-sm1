package com.szanfu.sdk.test.util;

import android.content.Context;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SimCardUtils {
    private static final String TAG = "SimCardUtils";

    /**
     * SIM卡信息类
     */
    public static class SimCardInfo {
        private int slotIndex;
        private String displayName;
        private String iccId;
        private Integer pin1RetryCount; // PIN
        private Integer puk1RetryCount; // PUK
        private Integer pin2RetryCount; // PIN2
        private Integer puk2RetryCount; // PUK2

 // getter setter
        public SimCardInfo(int slotIndex) {
            this.slotIndex = slotIndex;
        }

        public int getSlotIndex() {
            return slotIndex;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getIccId() {
            return iccId;
        }

        public void setIccId(String iccId) {
            this.iccId = iccId;
        }

        public Integer getPin1RetryCount() {
            return pin1RetryCount;
        }

        public void setPin1RetryCount(Integer pin1RetryCount) {
            this.pin1RetryCount = pin1RetryCount;
        }

        public Integer getPuk1RetryCount() {
            return puk1RetryCount;
        }

        public void setPuk1RetryCount(Integer puk1RetryCount) {
            this.puk1RetryCount = puk1RetryCount;
        }

        public Integer getPin2RetryCount() {
            return pin2RetryCount;
        }

        public void setPin2RetryCount(Integer pin2RetryCount) {
            this.pin2RetryCount = pin2RetryCount;
        }

        public Integer getPuk2RetryCount() {
            return puk2RetryCount;
        }

        public void setPuk2RetryCount(Integer puk2RetryCount) {
            this.puk2RetryCount = puk2RetryCount;
        }

        @Override
        public String toString() {
            return "SimCardInfo{" +
                    "slotIndex=" + slotIndex +
                    ", displayName='" + displayName + '\'' +
                    ", iccId='" + iccId + '\'' +
                    ", pin1RetryCount=" + pin1RetryCount +
                    ", puk1RetryCount=" + puk1RetryCount +
                    ", pin2RetryCount=" + pin2RetryCount +
                    ", puk2RetryCount=" + puk2RetryCount +
                    '}';
        }
    }

    /**
     * 获取所有SIM卡信息
     */
    public static List<SimCardInfo> getSimCardsInfo(Context context) {
        List<SimCardInfo> simCardInfos = new ArrayList<>();


        if (context.checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "缺少READ_PHONE_STATE权限");
            return simCardInfos;
        }

        SubscriptionManager subscriptionManager = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            subscriptionManager = (SubscriptionManager) context.getSystemService(
                    Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        }

        if (subscriptionManager == null) {
            Log.e(TAG, "无法获取SubscriptionManager");
            return simCardInfos;
        }

 // SIM
        List<SubscriptionInfo> subscriptionInfos = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
        }

        if (subscriptionInfos == null || subscriptionInfos.isEmpty()) {
            Log.w(TAG, "没有找到活跃的SIM卡");
            return simCardInfos;
        }

 // SIM
        for (SubscriptionInfo info : subscriptionInfos) {
            SimCardInfo simCardInfo = new SimCardInfo(info.getSimSlotIndex());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                simCardInfo.setDisplayName(info.getDisplayName().toString());
                simCardInfo.setIccId(info.getIccId());
            }

 // PIN/PUK
            getSimRetryCounts(context, info.getSubscriptionId(), simCardInfo);

            simCardInfos.add(simCardInfo);
        }

        return simCardInfos;
    }

    /**
     * 使用反射获取SIM卡重试次数
     */
    private static void getSimRetryCounts(Context context, int subId, SimCardInfo simCardInfo) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager == null) {
                Log.e(TAG, "无法获取TelephonyManager");
                return;
            }

 // API
            Class<?> telephonyClass = Class.forName("android.telephony.TelephonyManager");

 // ID TelephonyManager
            Method getMethod = telephonyClass.getMethod("createForSubscriptionId", int.class);
            TelephonyManager specificTelManager = (TelephonyManager) getMethod.invoke(telephonyManager, subId);

            if (specificTelManager != null) {
 // PIN1
                Method pin1Method = specificTelManager.getClass().getMethod("getSimPin1RetryCount");
                Integer pin1Retry = (Integer) pin1Method.invoke(specificTelManager);
                simCardInfo.setPin1RetryCount(pin1Retry);

 // PUK1
                Method puk1Method = specificTelManager.getClass().getMethod("getSimPuk1RetryCount");
                Integer puk1Retry = (Integer) puk1Method.invoke(specificTelManager);
                simCardInfo.setPuk1RetryCount(puk1Retry);

 // PIN2
                Method pin2Method = specificTelManager.getClass().getMethod("getSimPin2RetryCount");
                Integer pin2Retry = (Integer) pin2Method.invoke(specificTelManager);
                simCardInfo.setPin2RetryCount(pin2Retry);

 // PUK2
                Method puk2Method = specificTelManager.getClass().getMethod("getSimPuk2RetryCount");
                Integer puk2Retry = (Integer) puk2Method.invoke(specificTelManager);
                simCardInfo.setPuk2RetryCount(puk2Retry);
            }
        } catch (Exception e) {
            Log.e(TAG, "获取SIM卡重试次数失败: " + e.getMessage());
 // API
        }
    }

    /**
     * 打印SIM卡信息
     */
    public static void printSimCardsInfo(Context context) {
        List<SimCardInfo> simCards = getSimCardsInfo(context);

        if (simCards.isEmpty()) {
            Log.i(TAG, "没有找到SIM卡信息");
            return;
        }

        for (SimCardInfo info : simCards) {
            Log.i(TAG, "SIM卡 " + info.getSlotIndex() + ":");
            Log.i(TAG, "  名称: " + info.getDisplayName());
            Log.i(TAG, "  ICCID: " + info.getIccId());
            Log.i(TAG, "  PIN1重试次数: " +
                    (info.getPin1RetryCount() != null ? info.getPin1RetryCount() : "未知"));
            Log.i(TAG, "  PUK1重试次数: " +
                    (info.getPuk1RetryCount() != null ? info.getPuk1RetryCount() : "未知"));
            Log.i(TAG, "  PIN2重试次数: " +
                    (info.getPin2RetryCount() != null ? info.getPin2RetryCount() : "未知"));
            Log.i(TAG, "  PUK2重试次数: " +
                    (info.getPuk2RetryCount() != null ? info.getPuk2RetryCount() : "未知"));
        }
    }
}
