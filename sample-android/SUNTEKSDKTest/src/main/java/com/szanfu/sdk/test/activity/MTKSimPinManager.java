package com.szanfu.sdk.test.activity;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.lang.reflect.Method;

/**
 * MTK平台SIM卡PIN码管理类
 * 使用反射调用MTK隐藏API
 */
public class MTKSimPinManager {
    private static final String TAG = "MTKSimPinManager";

    private Context context;
    private TelephonyManager telephonyManager;

    public MTKSimPinManager(Context context) {
        this.context = context;
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    /**
     * 检查PIN码是否启用
     */
    public boolean isPinEnabled(int slotId) {
        try {
            Method method = telephonyManager.getClass().getMethod("getSimState", int.class);
            int state = (int) method.invoke(telephonyManager, slotId);
            return state == TelephonyManager.SIM_STATE_PIN_REQUIRED;
        } catch (Exception e) {
            Log.e(TAG, "检查PIN状态失败", e);

 // API
            try {
                int state = telephonyManager.getSimState(slotId);
                return state == TelephonyManager.SIM_STATE_PIN_REQUIRED;
            } catch (Exception ex) {
                Log.e(TAG, "备用检查方法也失败", ex);
            }
        }
        return false;
    }

    /**
     * 验证PIN码
     */
    public boolean verifyPin(int slotId, String pin) {
        try {
 // 1: MTK API
            Method method = telephonyManager.getClass().getMethod(
                    "supplyPinReportResultForSubscriber",
                    int.class, String.class
            );
            int[] result = (int[]) method.invoke(telephonyManager, slotId, pin);
            return result != null && result.length > 0 && result[0] == 0;
        } catch (Exception e) {
            Log.e(TAG, "MTK PIN验证失败，尝试标准方法", e);

 // 2: API
            try {
                Method method = telephonyManager.getClass().getMethod(
                        "supplyPin",
                        String.class
                );
                return (boolean) method.invoke(telephonyManager, pin);
            } catch (Exception ex) {
                Log.e(TAG, "标准PIN验证也失败", ex);
            }
        }
        return false;
    }

    /**
     * 修改PIN码
     */
    public boolean changePin(int slotId, String oldPin, String newPin) {
        try {
 // 1: MTK API
            Method method = telephonyManager.getClass().getMethod(
                    "changeSimPinForSubscriber",
                    int.class, String.class, String.class
            );
            return (boolean) method.invoke(telephonyManager, slotId, oldPin, newPin);
        } catch (Exception e) {
            Log.e(TAG, "MTK PIN修改失败，尝试标准方法", e);

 // 2: API
            try {
                Method method = telephonyManager.getClass().getMethod(
                        "changeSimPin",
                        String.class, String.class
                );
                return (boolean) method.invoke(telephonyManager, oldPin, newPin);
            } catch (Exception ex) {
                Log.e(TAG, "标准PIN修改也失败", ex);
            }
        }
        return false;
    }

    /**
     * 使用PUK码解锁
     */
    public boolean unlockWithPuk(int slotId, String puk, String newPin) {
        try {
 // 1: MTK API
            Method method = telephonyManager.getClass().getMethod(
                    "supplyPukReportResultForSubscriber",
                    int.class, String.class, String.class
            );
            int[] result = (int[]) method.invoke(telephonyManager, slotId, puk, newPin);
            return result != null && result.length > 0 && result[0] == 0;
        } catch (Exception e) {
            Log.e(TAG, "MTK PUK解锁失败，尝试标准方法", e);

 // 2: API
            try {
                Method method = telephonyManager.getClass().getMethod(
                        "supplyPuk",
                        String.class, String.class
                );
                return (boolean) method.invoke(telephonyManager, puk, newPin);
            } catch (Exception ex) {
                Log.e(TAG, "标准PUK解锁也失败", ex);
            }
        }
        return false;
    }

    /**
     * 启用PIN锁定
     */
    public boolean enablePinLock(int slotId, String pin) {
        try {
 // 1: MTK API
            Method method = telephonyManager.getClass().getMethod(
                    "setSimPinLockForSubscriber",
                    int.class, String.class, boolean.class
            );
            return (boolean) method.invoke(telephonyManager, slotId, pin, true);
        } catch (Exception e) {
            Log.e(TAG, "MTK启用PIN锁定失败，尝试标准方法", e);

 // 2: API
            try {
                Method method = telephonyManager.getClass().getMethod(
                        "setSimPinLock",
                        String.class, boolean.class
                );
                return (boolean) method.invoke(telephonyManager, pin, true);
            } catch (Exception ex) {
                Log.e(TAG, "标准启用PIN锁定也失败", ex);
            }
        }
        return false;
    }

    /**
     * 禁用PIN锁定
     */
    public boolean disablePinLock(int slotId, String pin) {
        try {
 // 1: MTK API
            Method method = telephonyManager.getClass().getMethod(
                    "setSimPinLockForSubscriber",
                    int.class, String.class, boolean.class
            );
            return (boolean) method.invoke(telephonyManager, slotId, pin, false);
        } catch (Exception e) {
            Log.e(TAG, "MTK禁用PIN锁定失败，尝试标准方法", e);

 // 2: API
            try {
                Method method = telephonyManager.getClass().getMethod(
                        "setSimPinLock",
                        String.class, boolean.class
                );
                return (boolean) method.invoke(telephonyManager, pin, false);
            } catch (Exception ex) {
                Log.e(TAG, "标准禁用PIN锁定也失败", ex);
            }
        }
        return false;
    }

    /**
     * 获取剩余PIN码尝试次数
     */
    public int getPinRemainingAttempts(int slotId) {
        try {
 // 1: MTK API
            Method method = telephonyManager.getClass().getMethod(
                    "getSimRemainingAttemptsForSubscriber",
                    int.class
            );
            return (int) method.invoke(telephonyManager, slotId);
        } catch (Exception e) {
            Log.e(TAG, "MTK获取剩余次数失败，尝试备用方法", e);


            return 3;
        }
    }

    /**
     * 获取SIM卡信息
     */
    public String getSimInfo(int slotId) {
        try {
            StringBuilder info = new StringBuilder();

 // SIM
            Method getSimStateMethod = telephonyManager.getClass().getMethod("getSimState", int.class);
            int simState = (int) getSimStateMethod.invoke(telephonyManager, slotId);


//            String operatorName = telephonyManager.getSimOperatorNameForPhone(slotId);

 // ICCID
            Method getIccIdMethod = telephonyManager.getClass().getMethod("getSimSerialNumber", int.class);
            String iccid = (String) getIccIdMethod.invoke(telephonyManager, slotId);

            info.append("SIM卡槽: ").append(slotId + 1).append("\n");
            info.append("状态: ").append(getSimStateString(simState)).append("\n");
 // info.append(" : ").append(operatorName).append("\n");
            info.append("ICCID: ").append(iccid != null ? iccid : "未知");

            return info.toString();
        } catch (Exception e) {
            Log.e(TAG, "获取SIM卡信息失败", e);
            return "获取SIM卡信息失败";
        }
    }

    private String getSimStateString(int state) {
        switch (state) {
            case TelephonyManager.SIM_STATE_ABSENT:
                return "无SIM卡";
            case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                return "需要PIN码";
            case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                return "需要PUK码";
            case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                return "网络锁定";
            case TelephonyManager.SIM_STATE_READY:
                return "就绪";
            case TelephonyManager.SIM_STATE_NOT_READY:
                return "未就绪";
            case TelephonyManager.SIM_STATE_PERM_DISABLED:
                return "永久禁用";
            case TelephonyManager.SIM_STATE_CARD_IO_ERROR:
                return "卡IO错误";
            default:
                return "未知状态(" + state + ")";
        }
    }
}
