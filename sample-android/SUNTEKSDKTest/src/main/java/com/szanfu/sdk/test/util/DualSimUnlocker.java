package com.szanfu.sdk.test.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.util.List;

/**
 * 双卡SIM卡解锁工具类
 * 提供SIM卡状态检查、PIN码解锁等功能
 */
public class DualSimUnlocker {
    private static final String TAG = "DualSimUnlocker";

    private Context mContext;
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;

    public DualSimUnlocker(Context context) {
        mContext = context.getApplicationContext();
        mSubscriptionManager = SubscriptionManager.from(mContext);
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 获取设备中的SIM卡数量
     */
/*    public int getSimCount() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            return mSubscriptionManager.getActiveSubscriptionInfoCount();
        } else {
 // Android
            return mTelephonyManager.getSimCount();
        }
    }*/

    /**
     * 检查指定SIM卡是否被PIN码锁定
     * @param slotIndex SIM卡槽索引 (0 或 1)
     * @return 是否被PIN码锁定
     */
    public boolean isSimLocked(int slotIndex) {
        int simState = getSimState(slotIndex);
        return simState == TelephonyManager.SIM_STATE_PIN_REQUIRED ||
                simState == TelephonyManager.SIM_STATE_PUK_REQUIRED ||
                simState == TelephonyManager.SIM_STATE_NETWORK_LOCKED;
    }

    /**
     * 获取指定SIM卡的状态
     * @param slotIndex SIM卡槽索引 (0 或 1)
     * @return SIM卡状态
     */
    public int getSimState(int slotIndex) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
 // Android 6.0+ SIM API
            TelephonyManager telephonyManager = mTelephonyManager.createForSubscriptionId(
                    getSubscriptionId(slotIndex));
            if (telephonyManager != null) {
                return telephonyManager.getSimState();
            }
        }

 // Android, SIM

        return mTelephonyManager.getSimState();
    }

    /**
     * 解锁指定SIM卡的PIN码
     * @param slotIndex SIM卡槽索引 (0 或 1)
     * @param pin PIN码
     * @return 是否解锁成功
     */
    public boolean unlockSim(int slotIndex, String pin) {
        try {
 // API, TelephonyManager PIN


            int subscriptionId = getSubscriptionId(slotIndex);
            if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
                Log.e(TAG, "Invalid subscription ID for slot: " + slotIndex);
                return false;
            }

 // ITelephony
            Class<?> telephonyClass = Class.forName("android.os.ServiceManager");
            java.lang.reflect.Method getServiceMethod = telephonyClass.getMethod("getService", String.class);
            android.os.IBinder binder = (android.os.IBinder) getServiceMethod.invoke(null, Context.TELEPHONY_SERVICE);

            Class<?> iTelephonyStubClass = Class.forName("com.android.internal.telephony.ITelephony$Stub");
            java.lang.reflect.Method asInterfaceMethod = iTelephonyStubClass.getMethod("asInterface", android.os.IBinder.class);
            Object iTelephony = asInterfaceMethod.invoke(null, binder);

 // supplyPinForSubscriber
            java.lang.reflect.Method supplyPinMethod = iTelephony.getClass().getMethod(
                    "supplyPinForSubscriber", int.class, String.class);
            boolean result = (Boolean) supplyPinMethod.invoke(iTelephony, subscriptionId, pin);

            Log.d(TAG, "SIM unlock result for slot " + slotIndex + ": " + result);
            return result;

        } catch (Exception e) {
            Log.e(TAG, "Error unlocking SIM: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * 获取指定卡槽的订阅ID
     * @param slotIndex SIM卡槽索引
     * @return 订阅ID，如果无效返回INVALID_SUBSCRIPTION_ID
     */
    private int getSubscriptionId(int slotIndex) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            @SuppressLint("MissingPermission") List<SubscriptionInfo> subscriptionInfos = mSubscriptionManager.getActiveSubscriptionInfoList();
            if (subscriptionInfos != null) {
                for (SubscriptionInfo info : subscriptionInfos) {
                    if (info.getSimSlotIndex() == slotIndex) {
                        return info.getSubscriptionId();
                    }
                }
            }
        }
        return SubscriptionManager.INVALID_SUBSCRIPTION_ID;
    }

    /**
     * 检查设备是否支持双卡
     */
//    public boolean isDualSimSupported() {
//        return getSimCount() >= 2;
//    }

    /**
     * 获取SIM卡运营商名称
     * @param slotIndex SIM卡槽索引
     * @return 运营商名称
     */
    public String getSimOperatorName(int slotIndex) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            TelephonyManager telephonyManager = mTelephonyManager.createForSubscriptionId(
                    getSubscriptionId(slotIndex));
            if (telephonyManager != null) {
                return telephonyManager.getSimOperatorName();
            }
        }
        return mTelephonyManager.getSimOperatorName();
    }
}
