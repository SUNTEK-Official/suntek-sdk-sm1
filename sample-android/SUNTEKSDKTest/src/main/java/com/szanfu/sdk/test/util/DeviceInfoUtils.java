package com.szanfu.sdk.test.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Locale;

/**
 * 设备信息工具类
 * 需要权限：
 * <uses-permission android:name="android.permission.READ_PHONE_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
 * 部分信息需要AndroidManifest中添加权限声明
 */
public class DeviceInfoUtils {
    private static final String TAG = "DeviceInfo";

    /**
     * 打印完整的设备信息
     */
    public static void printDeviceInfo(Context context) {
        if (context == null) {
            LogUtils.d( "Context is null");
            return;
        }

        LogUtils.d( "========== 设备信息开始 ==========");

        printBasicDeviceInfo();
        printScreenInfo(context);
        printMemoryInfo(context);
        printStorageInfo(context);
        printNetworkInfo(context);
        printBatteryInfo(context);
        printSensorInfo(context);
        printCPUInfo();
        printSystemInfo(context);

        LogUtils.d( "========== 设备信息结束 ==========");
    }

    /**
     * 打印基本设备信息
     */
    @SuppressLint("HardwareIds")
    private static void printBasicDeviceInfo() {
        try {

            LogUtils.d( "品牌: " + Build.BRAND);
            LogUtils.d( "型号: " + Build.MODEL);
            LogUtils.d( "设备: " + Build.DEVICE);
            LogUtils.d( "产品: " + Build.PRODUCT);


            LogUtils.d( "硬件: " + Build.HARDWARE);
            LogUtils.d( "主板: " + Build.BOARD);
            LogUtils.d( "制造商: " + Build.MANUFACTURER);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
 // LogUtils.d( " : " + Build.getSerial());
            } else {
                LogUtils.d( "序列号: " + Build.SERIAL);
            }

 // ID( READ_PHONE_STATE )
            LogUtils.d( "设备指纹: " + Build.FINGERPRINT);
            LogUtils.d( "唯一ID: " + Build.ID);

        } catch (Exception e) {
            LogUtils.d( "获取基本设备信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印屏幕信息
     */
    private static void printScreenInfo(Context context) {
        try {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                DisplayMetrics metrics = new DisplayMetrics();
                windowManager.getDefaultDisplay().getMetrics(metrics);

                LogUtils.d( "屏幕密度: " + metrics.density + " (" + metrics.densityDpi + " dpi)");
                LogUtils.d( "屏幕宽度: " + metrics.widthPixels + "px");
                LogUtils.d( "屏幕高度: " + metrics.heightPixels + "px");
                LogUtils.d( "屏幕物理尺寸: " +
                        String.format(Locale.getDefault(), "%.1f英寸",
                                Math.sqrt(Math.pow(metrics.widthPixels / metrics.xdpi, 2) +
                                        Math.pow(metrics.heightPixels / metrics.ydpi, 2))));


                LogUtils.d( "X方向dpi: " + metrics.xdpi);
                LogUtils.d( "Y方向dpi: " + metrics.ydpi);
            }
        } catch (Exception e) {
            LogUtils.d( "获取屏幕信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印内存信息
     */
    private static void printMemoryInfo(Context context) {
        try {
            ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (activityManager != null) {
                ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
                activityManager.getMemoryInfo(memoryInfo);

                LogUtils.d( "总内存: " + Formatter.formatFileSize(context, memoryInfo.totalMem));
                LogUtils.d( "可用内存: " + Formatter.formatFileSize(context, memoryInfo.availMem));
                LogUtils.d( "低内存状态: " + memoryInfo.lowMemory);
                LogUtils.d( "内存阈值: " + Formatter.formatFileSize(context, memoryInfo.threshold));

 // ( Android 4.0+)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    android.app.ActivityManager.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(new int[]{android.os.Process.myPid()});
//                    if (memoryInfoArray.length > 0) {
 // LogUtils.d( " : " + memoryInfoArray[0].getTotalPrivateDirty() + " KB");
//                    }
//                }
            }
        } catch (Exception e) {
            LogUtils.d( "获取内存信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印存储信息
     */
    private static void printStorageInfo(Context context) {
        try {

            File internalStorage = Environment.getDataDirectory();
            StatFs statFs = new StatFs(internalStorage.getPath());
            long blockSize = statFs.getBlockSizeLong();
            long totalBlocks = statFs.getBlockCountLong();
            long availableBlocks = statFs.getAvailableBlocksLong();

            long totalInternalStorage = totalBlocks * blockSize;
            long freeInternalStorage = availableBlocks * blockSize;
            long usedInternalStorage = totalInternalStorage - freeInternalStorage;

            LogUtils.d( "内部存储总量: " + Formatter.formatFileSize(context, totalInternalStorage));
            LogUtils.d( "内部存储可用: " + Formatter.formatFileSize(context, freeInternalStorage));
            LogUtils.d( "内部存储已用: " + Formatter.formatFileSize(context, usedInternalStorage));

 // (SD )
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File externalStorage = Environment.getExternalStorageDirectory();
                statFs = new StatFs(externalStorage.getPath());
                blockSize = statFs.getBlockSizeLong();
                totalBlocks = statFs.getBlockCountLong();
                availableBlocks = statFs.getAvailableBlocksLong();

                long totalExternalStorage = totalBlocks * blockSize;
                long freeExternalStorage = availableBlocks * blockSize;

                LogUtils.d( "外部存储总量: " + Formatter.formatFileSize(context, totalExternalStorage));
                LogUtils.d( "外部存储可用: " + Formatter.formatFileSize(context, freeExternalStorage));
            } else {
                LogUtils.d( "外部存储: 未挂载");
            }

        } catch (Exception e) {
            LogUtils.d( "获取存储信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印网络信息
     */
    @SuppressLint("MissingPermission")
    private static void printNetworkInfo(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(
                            connectivityManager.getActiveNetwork());

                    if (capabilities != null) {
                        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            LogUtils.d( "网络类型: WiFi");
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            LogUtils.d( "网络类型: 移动网络");
                        } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                            LogUtils.d( "网络类型: 以太网");
                        }

                        LogUtils.d( "网络下行带宽: " + capabilities.getLinkDownstreamBandwidthKbps() + " Kbps");
                        LogUtils.d( "网络上��带宽: " + capabilities.getLinkUpstreamBandwidthKbps() + " Kbps");
                    }
                } else {
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo != null) {
                        LogUtils.d( "网络类型: " + networkInfo.getTypeName());
                        LogUtils.d( "网络状态: " + networkInfo.getState());
                        LogUtils.d( "是否连接: " + networkInfo.isConnected());
                    }
                }
            }

 // SIM ( READ_PHONE_STATE )
            TelephonyManager telephonyManager = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyManager != null) {
                if (context.checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                        == PackageManager.PERMISSION_GRANTED) {
                    LogUtils.d( "运营商: " + telephonyManager.getNetworkOperatorName());
                    LogUtils.d( "网络国家: " + telephonyManager.getNetworkCountryIso());
                    LogUtils.d( "网络类型: " + getNetworkTypeName(telephonyManager.getNetworkType()));
                }
            }

        } catch (Exception e) {
            LogUtils.d( "获取网络信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取网络类型名称
     */
    private static String getNetworkTypeName(int networkType) {
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "UMTS";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "HSPA";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE";
            case TelephonyManager.NETWORK_TYPE_NR:
                return "5G";
            default:
                return "未知: " + networkType;
        }
    }

    /**
     * 打印电池信息
     */
    private static void printBatteryInfo(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                BatteryManager batteryManager = (BatteryManager)
                        context.getSystemService(Context.BATTERY_SERVICE);
                if (batteryManager != null) {
                    int batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                    LogUtils.d( "电池电量: " + batteryLevel + "%");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        long capacity = batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
                        LogUtils.d( "电池容量: " + capacity + " mAh");
                    }
                }
            }
        } catch (Exception e) {
            LogUtils.d( "获取电池信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印传感器信息
     */
    private static void printSensorInfo(Context context) {
        try {
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                java.util.List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
                LogUtils.d( "传感器数量: " + sensors.size());

                int sensorCount = 0;
                for (Sensor sensor : sensors) {
                    if (sensorCount < 10) {
                        LogUtils.d( "传感器" + (sensorCount + 1) + ": " + sensor.getName());
                    }
                    sensorCount++;
                }
                if (sensorCount > 10) {
                    LogUtils.d( "... 还有" + (sensorCount - 10) + "个传感器未显示");
                }
            }
        } catch (Exception e) {
            LogUtils.d( "获取传感器信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印CPU信息
     */
    private static void printCPUInfo() {
        try {
 // CPU /proc/cpuinfo
            FileReader fr = new FileReader("/proc/cpuinfo");
            LineNumberReader lnr = new LineNumberReader(fr);
            String line;
            int cpuCount = 0;

            LogUtils.d( "CPU信息:");
            while ((line = lnr.readLine()) != null && cpuCount < 10) {
                if (line.contains("processor") || line.contains("Processor") ||
                        line.contains("model name") || line.contains("Hardware")) {
                    LogUtils.d( "  " + line.trim());
                    if (line.contains("processor")) {
                        cpuCount++;
                    }
                }
            }
            lnr.close();
            fr.close();

 // CPU
            LogUtils.d( "CPU架构: " + Build.CPU_ABI);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                LogUtils.d( "支持的ABI: " + String.join(", ", Build.SUPPORTED_ABIS));
            }

        } catch (IOException e) {
            LogUtils.d( "获取CPU信息失败: " + e.getMessage());
        }
    }

    /**
     * 打印系统信息
     */
    private static void printSystemInfo(Context context) {
        try {
 // Android
            LogUtils.d( "Android版本: " + Build.VERSION.RELEASE);
            LogUtils.d( "API Level: " + Build.VERSION.SDK_INT);
            LogUtils.d( "Android安全补丁: " + Build.VERSION.SECURITY_PATCH);
            LogUtils.d( "基带版本: " + Build.getRadioVersion());


            LogUtils.d( "构建ID: " + Build.DISPLAY);
            LogUtils.d( "构建时间: " + Build.TIME);
            LogUtils.d( "构建类型: " + Build.TYPE);
            LogUtils.d( "构建标签: " + Build.TAGS);


            LogUtils.d( "用户: " + Build.USER);
            LogUtils.d( "主机: " + Build.HOST);


            LogUtils.d( "系统语言: " + Locale.getDefault().getDisplayLanguage());
            LogUtils.d( "系统地区: " + Locale.getDefault().getCountry());


            LogUtils.d( "时区: " + java.util.TimeZone.getDefault().getID());

            // Android ID
            String androidId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            LogUtils.d( "Android ID: " + androidId);


            LogUtils.d( "是否Root: " + isDeviceRooted());

        } catch (Exception e) {
            LogUtils.d( "获取系统信息失败: " + e.getMessage());
        }
    }

    /**
     * 检查设备是否已Root
     */
    private static boolean isDeviceRooted() {
 // Root
        String[] rootPaths = {
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su"
        };

        for (String path : rootPaths) {
            if (new File(path).exists()) {
                return true;
            }
        }


        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[]{"which", "su"});
            java.io.BufferedReader in = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }

    /**
     * 获取设备信息字符串（用于日志或其他用途）
     */
    public static String getDeviceInfoString(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("设备信息:\n");
        sb.append("品牌: ").append(Build.BRAND).append("\n");
        sb.append("型号: ").append(Build.MODEL).append("\n");
        sb.append("Android版本: ").append(Build.VERSION.RELEASE).append("\n");
        sb.append("API Level: ").append(Build.VERSION.SDK_INT).append("\n");

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            DisplayMetrics metrics = new DisplayMetrics();
            windowManager.getDefaultDisplay().getMetrics(metrics);
            sb.append("屏幕分辨率: ").append(metrics.widthPixels).append("x").append(metrics.heightPixels).append("\n");
            sb.append("屏幕密度: ").append(metrics.densityDpi).append(" dpi\n");
        }

        return sb.toString();
    }
}
