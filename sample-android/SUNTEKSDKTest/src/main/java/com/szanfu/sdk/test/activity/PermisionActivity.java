package com.szanfu.sdk.test.activity;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ISystemEncDecBinderService;
import com.szanfu.sdk.api.system.ISystemReadUserKeyBinderService;
import com.szanfu.sdk.api.system.IWifiConnectCallback;
import com.szanfu.sdk.api.system.IWifiScanCallback;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.AsciiUtils;
import com.szanfu.sdk.test.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.jvm.internal.Ref;

public class PermisionActivity extends ListButtonBaseActivity {
 // Handler
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());

 // WiFi
    private IWifiScanCallback mWifiScanCallback = new IWifiScanCallback.Stub() {
        @Override
        public void onScanResult(List<String> ssidList) throws RemoteException {
 // AIDL Binder ! UI
            for (int i = 0; i < ssidList.size(); i++) {
                LogUtils.e("ssidList   : "+ssidList.get(i));
            }
            mMainHandler.post(() -> handleWifiScanResult(ssidList));
        }
    };

 // ( )
    public static final String POWER_SAVING_SCREEN_OFF = "screen_off_time";
    public static final String POWER_SAVING_CPU_LOWER = "cpu_lower_mode";
    public static final String POWER_SAVING_WIFI_SLEEP = "wifi_sleep_policy";
    private ISystemBinderService mSystemBinderService;
    private String[] mButtons;
    public static final int POSITION_LMK_OOMADJ = 0;
    /**
     *         <item> set Usb Device Permission No pop-up window  </item>
     *         <item> set Usb Device Permission Every time pop-up window  </item>
     *         <item> clearUsbDevicePermission </item>
     *         <item> set Usb Device Permission ALL No pop-up window  </item>
     *         <item> set Usb Device Permission ALL Every time pop-up window  </item>
     *         <item> clear ALL UsbDevicePermission  </item>
     */
    public static final int POSITION_SETUSBDEVICEDEFAULTPERMISSION_NO_POP_UP_WINDOW = 1;
    public static final int POSITION_CLEARUSBDEVICEPERMISSION_EVERY_TIME_POP_UP_WINDOW = 2;
    public static final int POSITION_CLEARUSBDEVICEPERMISSION = 3;

    public static final int POSITION_SETUSBDEVICEDEFAULTPERMISSION_ALL_NO_POP_UP_WINDOW = 4;
    public static final int POSITION_CLEARUSBDEVICEPERMISSION_ALL_EVERY_TIME_POP_UP_WINDOW = 5;
    public static final int POSITION_CLEAR_ALL_USBDEVICEPERMISSION = 6;
    public static final int POSITION_GRANTALLDANGEROUSPERMISSIONS = 7;
    public static final int POSITION_SET_GRANTAPPPOWERMANAGER = 8; // ( )
    public static final int POSITION_CANCLE_GRANTAPPPOWERMANAGER = 9; // ( )
    public static final int POSITION_SETNETWORKMANAGERPERMISSION = 10;
    public static final int POSITION_CANCLE_NETWORKMANAGERPERMISSION = 11;
    public static final int POSITION_GRANTREMOTECONTROLPERMISSIONS = 12;
    public static final int POSITION_REVOKEREMOTECONTROLPERMISSIONS = 13;
    public static final int POSITION_GRANTPERSISTENTALERTWINDOWPERMISSION = 14;
    public static final int POSITION_WRITE_SETTINGS = 15;

    public static final int POSITION_grantVendorSystemSdkPermission = 16;
    public static final int POSITION_revokeVendorSystemSdkPermission = 17;

    public static final int POSITION_GRANTVENDORUSERSDKPERMISSION = 18;
    public static final int POSITION_CHECKVENDORUSERSDKPERMISSION = 19;
    public static final int POSITION_REVOKEVENDORUSERSDKPERMISSION = 20;
    public static final int POSITION__GETDEVICEFULLINFO = 21; // 1.9
    public static final int POSITION__GETAPPFULLSTATS = 22; // 1.10
    public static final int POSITION_SHOWBRIGHTNESSDIALOG = 23; // 3.8.1
    public static final int POSITION_SHOWVOLUMEDIALOG = 24; // 3.8.2
    public static final int POSITION_SHOWFONTDIALOG = 25; // 3.8.3
    public static final int POSITION_SHOWLANGUAGE_DIALOG = 26; // 3.8.4
    public static final int POSITION_SCREEN_TIMEOUT = 27; // 3.8.5
    public static final int POSITION_SET_NETWORK_MODE = 28;
    public static final int POSITION_GET_NETWORK_DEVICE_INFO = 29;
    public static final int POSITION_CONNECT_WIFI = 30; // WiFi

    public static final int POSITION_SET_VPN_PROXY = 31; // VPN
    public static final int POSITION_CLEAR_VPN_PROXY = 32; // VPN
    public static final int POSITION_GRANT_VPN_PERMISSION = 33; // VPN
    public static final int POSITION_CHECK_VPN_PERMISSION = 34; // VPN
    public static final int POSITION_DEFAULT_35 = 35;     // ***********
    public static final int POSITION_WRITESAFEDISK = 36;
    public static final int POSITION_WRITESAFEDISK_FILE = 37;
    public static final int POSITION_READSAFEDISK = 38;
    public static final int POSITION_FINDSAFEDISK = 39;
    public static final int POSITION_FINDFILENAME_SAFEDISK = 40;
    public static final int OPEN_SYSTEM_SETTINGS = 41;
    public static final int OPEN_SYSTEM_GETDEVICEOSVERSION = 42;
    public static final int OPEN_SYSTEM_GETAPPVERSIONINFO = 43;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.ALL_SYSTEM_PERMISION_LIST);
        setButtonDataSource(mButtons);
    }
    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
            case POSITION_LMK_OOMADJ:
 // Enable/Disable the application anti-low-memory scanning function /
                /**
                 * 开启/关闭 应用防低内存查杀功能
                 * 作用：通过提升目标App进程优先级 / 加入LMK白名单，降低系统内存不足时被杀概率
                 *
                 * @param packageName 目标应用的包名（例如：com.szanfu.sdk.service）
                 * @param enable true=开启防杀（提升优先级），false=关闭防杀（恢复默认）
                 * @return 操作结果：
                 *         true = 调用成功（含：应用未运行时的兼容成功）
                 *         false = 调用失败（权限不足/系统异常）
                 *
                 * 注意：
                 * 1. 若目标应用未启动，会返回 true（无需处理，无查杀风险）
                 * 2. 必须拥有系统权限/系统签名才能正常生效
                 * 3. 错误日志：app not running 表示目标进程未启动
                 */
                try {
                    String  packageName = "com.szanfu.sdk.service";
                    boolean bAvoidAppLowMemoryKiller = mSystemBinderService.avoidAppLowMemoryKiller(packageName,true);

                    if (bAvoidAppLowMemoryKiller){
                       appendText(packageName +"Priority, to avoid being killed by low memory: "+bAvoidAppLowMemoryKiller );
                   }else {
                       appendText(packageName +"Priority, to avoid being killed by low memory: : "+bAvoidAppLowMemoryKiller );
                   }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_SETUSBDEVICEDEFAULTPERMISSION_NO_POP_UP_WINDOW:
 // Grant permanent authorization for a USB device to a certain application (without pop-up window) USB ( )
                //                   String  packageName = "com.szanfu.sdk.test";
                /**
                 * 为指定包名的应用设置USB设备默认权限
                 * @param packageName 目标应用包名
                 * @param usbDevicePath USB设备节点路径（如：/dev/bus/usb/001/002）
                 * @param permanentGrant true=永久授权无弹窗 false=每次使用弹窗授权
                 * @return 0成功 -1失败
                 */
                String  packageName = "com.szanfu.sdk.service";
                String usbPath = "/dev/bus/usb/001/002"; // USB
                boolean permanent = true;
                try {
                    int result = mSystemBinderService.setUsbDeviceDefaultPermission(packageName,usbPath,permanent);
                    if (result == 0) {
                        appendText( "Grant permanent authorization for a USB device to a certain application (without pop-up window) : Success ");
                    } else {

                        appendText( "Grant permanent authorization for a USB device to a certain application (without pop-up window) : ERROR ");
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_CLEARUSBDEVICEPERMISSION_EVERY_TIME_POP_UP_WINDOW:
 // Grant temporary authorization for a certain application (with a pop-up window each time it is used) ( )
                //                   String  packageName = "com.szanfu.sdk.test";
                /**
                 * 为指定包名的应用设置USB设备默认权限
                 * @param packageName 目标应用包名
                 * @param usbDevicePath USB设备节点路径（如：/dev/bus/usb/001/002）
                 * @param permanentGrant true=永久授权无弹窗 false=每次使用弹窗授权
                 * @return 0成功 -1失败
                 */
                String  everyTimePackageName = "com.szanfu.sdk.service";
                String everyTimeusbPath = "/dev/bus/usb/001/002"; // USB
                boolean everyTimepermanent = false;
                try {
                    int result = mSystemBinderService.setUsbDeviceDefaultPermission(everyTimePackageName,everyTimeusbPath,everyTimepermanent);
                    if (result == 0) {
                        appendText(everyTimePackageName+ getResources().getString(R.string.usb_permision_successful ));
                    } else {
                        appendText(everyTimePackageName+ getResources().getString(R.string.usb_permision_failed ));
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case  POSITION_CLEARUSBDEVICEPERMISSION:
 // Remove the USB permission for a certain application USB
                /**
                 * 清除指定应用的USB设备默认权限
                 * @param packageName 目标应用包名
                 * @param usbDevicePath USB设备节点路径
                 * @return 0成功 -1失败
                 */
                try {
                    String clearPackageName = "com.szanfu.sdk.service";
                    String clearusbPath = "/dev/bus/usb/001/002";

                    int result = mSystemBinderService.clearUsbDevicePermission(clearPackageName, clearusbPath);

                    if (result == 0) {
                        appendText(clearPackageName+ getResources().getString(R.string.clear_usb_permision_successful ));
                    } else {
                        appendText(clearPackageName+ getResources().getString(R.string.clear_usb_permision_failed ));
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_SETUSBDEVICEDEFAULTPERMISSION_ALL_NO_POP_UP_WINDOW:
 // Grant authorization for all USB devices to a certain application (without showing pop-up windows permanently) USB ( )
                /**
                 * 给指定包名应用 授权所有USB设备权限（全局）
                 * @param packageName 目标应用包名
                 * @param permanentGrant true=永久无弹窗  false=每次弹窗
                 * @return 0成功 -1失败
                 */
                try {
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.setUsbPermissionForAllDevices(pkg, true);

                    if (result == 0) {
                        appendText( getResources().getString(R.string.pop_up_windows_successful ));
 // USB
                    } else {
                        appendText( getResources().getString(R.string.pop_up_windows__failed ));

                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case  POSITION_CLEARUSBDEVICEPERMISSION_ALL_EVERY_TIME_POP_UP_WINDOW:
 // USB
                /**
                 * 给指定包名应用 授权所有USB设备权限（全局）
                 * @param packageName 目标应用包名
                 * @param permanentGrant true=永久无弹窗  false=每次弹窗
                 * @return 0成功 -1失败
                 */
                try {
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.setUsbPermissionForAllDevices(pkg, false);

                    if (result == 0) {
                        appendText( getResources().getString(R.string.all_pop_up_windows_successful ));
 // USB
                    } else {
                        appendText( getResources().getString(R.string.all_pop_up_windows__failed ));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_CLEAR_ALL_USBDEVICEPERMISSION:
 // USB
                /**
                 * 为指定应用自动授予所有危险级别权限（跳过系统授权弹窗）
                 * 作用：类似系统应用，自动通过所有动态危险权限申请
                 * @param packageName 目标应用包名
                 * @return 0 成功，-1 失败
                 */
                try {
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.clearUsbPermissionForAllDevices(pkg);
                    appendText(pkg+getResources().getString(R.string.clear_all_pop_up_windows_failed ) +result );
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case POSITION_GRANTALLDANGEROUSPERMISSIONS:
 // ( )
                /**
                 * 为指定应用自动授予所有危险级别权限（跳过系统授权弹窗）
                 * 作用：类似系统应用，自动通过所有动态危险权限申请
                 * @param packageName 目标应用包名
                 * @return 0 成功，-1 失败
                 */
                appendText(itemName+":");

                String[] packageNames = {
                        "com.szanfu.sdk.service"
                        , "com.crestwavetech.cashiersw"
                        , "com.crestwavetech.fiscalcore"
                        , "com.skytech.smartskypos"
                };
                for (String pkg : packageNames) {
                    try {
                        String setPermissionResult = mSystemBinderService.grantAllDangerousPermissions(pkg);
                        appendInfoText(pkg+" : ");
                        LogUtils.e(pkg+ "\n"+setPermissionResult);
                        appendText(pkg+ "\n"+setPermissionResult);
                    } catch (RemoteException e) {
                        appendErrorText(pkg + " set Network Manager permission error: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
                break;
            case POSITION_SET_GRANTAPPPOWERMANAGER:

                /**
                 * 给指定应用授权 PowerManager 能力（REBOOT 权限 + PowerManager 使用权限）
                 * @param packageName 目标应用包名
                 * @param enable true=授权  false=撤销授权
                 * @return 0成功  -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int ret =  mSystemBinderService.grantAppPowerManagerPermission(pkg, true);
                    if (ret == 0){
                        appendText(pkg+ getResources().getString(R.string.setpoewerManager_permissions_success ));
                    }else {
                        appendText(pkg+getResources().getString(R.string.setpoewerManager_permissions_failed ));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_CANCLE_GRANTAPPPOWERMANAGER:
 // (REBOOT + PowerManager)
                /**
                 * 给指定应用授权 PowerManager 能力（REBOOT 权限 + PowerManager 使用权限）
                 * @param packageName 目标应用包名
                 * @param enable true=授权  false=撤销授权
                 * @return 0成功  -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int ret = mSystemBinderService.grantAppPowerManagerPermission(pkg, false);
                    if (ret == 0){
                        appendText(pkg + getResources().getString(R.string.cancle_poewerManager_permissions_success ));
                    }else {
                        appendText(pkg + getResources().getString(R.string.cancle_poewerManager_permissions_failed ));
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_SETNETWORKMANAGERPERMISSION:
 // Network Manager =
                /**
                 * 授予指定应用 网络管理器权限（允许使用 EthernetManager / CONNECTIVITY_INTERNAL）
                 * @param packageName 目标应用包名
                 * @param enable true=授予权限  false=撤销权限
                 * @return 0成功 -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int ret = mSystemBinderService.setNetworkManagerPermission(pkg, true);
                    if (ret == 0){
                        appendText(pkg+" set Network Manager permission  success");
                    }else {
                        appendText(pkg+ " cancle Network Manager permission  fail");
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_CANCLE_NETWORKMANAGERPERMISSION:
 // Network Manager
                /**
                 * 授予指定应用 网络管理器权限（允许使用 EthernetManager / CONNECTIVITY_INTERNAL）
                 * @param packageName 目标应用包名
                 * @param enable true=授予权限  false=撤销权限
                 * @return 0成功 -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int ret = mSystemBinderService.setNetworkManagerPermission(pkg, false);
                    if (ret == 0){
                        appendText(pkg + " cancle Network Manager permission  success");
                    }else {
                        appendText(pkg + " cancle Network Manager permission  fail");
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case POSITION_GRANTREMOTECONTROLPERMISSIONS:

                /**
                 * 为指定应用授予远程控制所需的全套系统权限
                 * 包含：INJECT_EVENTS、CAPTURE_VIDEO_OUTPUT、READ_FRAME_BUFFER、ACCESS_SURFACE_FLINGER
                 * @param packageName 目标应用包名
                 * @return 0=成功，-1=失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int ret = mSystemBinderService.grantRemoteControlPermissions(pkg);
                    if (ret == 0){
                        appendText(pkg +  getResources().getString(R.string.Set_Remote_permissions_success ));
                    }else {
                        appendText(pkg + getResources().getString(R.string.Set_Remote_permissions_failed ));
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_REVOKEREMOTECONTROLPERMISSIONS:

                /**
                 * 撤销指定应用的远程控制权限
                 * @param packageName 目标应用包名
                 * @return 0=成功，-1=失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int ret = mSystemBinderService.revokeRemoteControlPermissions(pkg);
                    if (ret == 0){
                        appendText(pkg + getResources().getString(R.string.cancle_Remote_permissions_success ));
                    }else {
                        appendText(pkg +getResources().getString(R.string.cancle_Remote_permissions_failed ));
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_GRANTPERSISTENTALERTWINDOWPERMISSION:
 // com.xxx.test
 // ( , )
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.grantPersistentAlertWindowPermission(pkg);
                    if (result == 0) {

                        appendText(" Authorization "+pkg+getResources().getString(R.string.Set_permanent_floating_window_permissions_success ));
                    }else {
                        appendText(" Authorization "+pkg+getResources().getString(R.string.Set_permanent_floating_window_permissions_failed ));
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_WRITE_SETTINGS:
 // ==================== : WRITE_SETTINGS ====================
                /**
                 * 为应用永久授权 WRITE_SETTINGS 权限
                 * 作用：授权后应用可读写系统设置，无需用户弹窗确认，开机自动生效
                 * @param packageName 目标应用包名
                 * @return 0成功 -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.grantWriteSettingsPermission(pkg);
                    if (result == 0) {

                        appendText(" Authorization "+pkg+" WRITE_SETTINGS Permanent validity : Success" );
                    }else {
                        appendText(" Authorization "+pkg+" WRITE_SETTINGS Permanent validity :  Fail ");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_grantVendorSystemSdkPermission:
 // ==================== : Vendor System SDK ====================
                /**
                 * 授予应用 Vendor System SDK 权限
                 * 允许应用使用厂商 SDK 系统级方法
                 * @param packageName 目标应用包名
                 * @return 0成功 -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.grantVendorSystemSdkPermission(pkg);
                    if (result == 0) {

                        appendText(" Authorization "+pkg+" Vendor System SDK permission ：  Success" );
                    }else {
                        appendText(" Authorization "+pkg+" Vendor System SDK permission ：  Fail ");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_revokeVendorSystemSdkPermission:
 // ==================== : Vendor System SDK ====================
                /**
                 * 撤销应用 Vendor System SDK 权限
                 * @param packageName 目标应用包名
                 * @return 0成功 -1失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.revokeVendorSystemSdkPermission(pkg);
                    if (result == 0) {

                        appendText("  Revoke permissions "+pkg+ " Vendor System SDK permission ： Success" );
                    }else {
                        appendText("  Revoke permissions "+pkg+" Vendor System SDK permission ：  Fail ");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case POSITION_GRANTVENDORUSERSDKPERMISSION:
 // 1. Vendor User SDK
                /**
                 * 授予应用 Vendor User SDK 权限
                 * 允许应用调用厂商 SDK User 级别方法
                 * @param packageName 目标应用包名
                 * @return 0=成功 -1=失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.grantVendorUserSdkPermission(pkg);
                    if (result == 0) {

                        appendText(pkg+ " Authorization Vendor User SDK ： Success" );
                    }else {
                        appendText(pkg+" Authorization Vendor User SDK ： Fail ");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case POSITION_CHECKVENDORUSERSDKPERMISSION:
 // 2. Vendor User SDK
                /**
                 * 检查应用是否拥有 Vendor User SDK 权限
                 * @param packageName 目标应用包名
                 * @return 0=已授权 -1=未授权/失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.checkVendorUserSdkPermission(pkg);
                    if (result == 0) {

                        appendText("Check : "+pkg+ getResources().getString(R.string.check_permissions_success )+" Vendor User SDK permissions"  );
                    }else {
                        appendText("Check : "+pkg+  getResources().getString(R.string.check_permissions_failed )+" Vendor User SDK permissions"  );
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case POSITION_REVOKEVENDORUSERSDKPERMISSION:
 // 3. Vendor User SDK
                /**
                 * 撤销应用 Vendor User SDK 权限
                 * @param packageName 目标应用包名
                 * @return 0=成功 -1=失败
                 */
                try {
 // APP
                    String pkg = "com.szanfu.sdk.service";
                    int result = mSystemBinderService.revokeVendorUserSdkPermission(pkg);
                    if (result == 0) {

                        appendText(pkg+ " Cancle Vendor User SDK Permission granted successfully" );
                    }else {
                        appendText(pkg+"Cancle Vendor User SDK Permission failure ");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
 // 1.9 Equipment Monitoring
            case POSITION__GETDEVICEFULLINFO:
                try {
                    /**
                     * 获取设备综合监控信息
                     * @return JSON格式字符串，包含CPU/内存/序列号/系统版本/网络/应用/电池
                     */
                    String strDeviceFullInfo = mSystemBinderService.getDeviceMonitorInfo();
                    appendText("DeviceMonitorInfo  ： "+strDeviceFullInfo);
                    LogUtils.e("DeviceMonitorInfo  :\r\n" +strDeviceFullInfo );
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
 // 1.10 Applied Statistics
            case POSITION__GETAPPFULLSTATS:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String strAppFullStats = mSystemBinderService.getAppFullStats();
                            appendText("AppFullStats ： "+strAppFullStats);
                            LogUtils.e("1.10 Applied Statistics   应用统计 :" +strAppFullStats );
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                break;
            case POSITION_SHOWBRIGHTNESSDIALOG:
 // 3.8.1 Screen brightness
                showBrightnessDialog();
                break;
            case POSITION_SHOWVOLUMEDIALOG:
 // Volume adjustment pop-up window
                showVolumeDialog();
                break;
            case POSITION_SHOWFONTDIALOG:
 // 3.8.3
                showFontDialog();
                break;
            case POSITION_SHOWLANGUAGE_DIALOG:
                if (mSystemBinderService == null) {
 // SDK
                    appendText("SDK service is not connected");
                    return;
                }
                try {

                    String currentLang = mSystemBinderService.getSystemLanguage();
                    String[] langArray = mSystemBinderService.getSystemSupportLanguages();


                    List<String> showList = new ArrayList<>();
                    List<String> idList = new ArrayList<>();
                    int selectedPosition = 0;

                    for (int i = 0; i < langArray.length; i++) {
                        String[] split = langArray[i].split("\\|");
                        String name = split[0];
                        String id = split[1];
                        showList.add(name);
                        idList.add(id);

                        if (id.equals(currentLang)) {
                            selectedPosition = i;
                        }
                    }


                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.dialog_language_list);
                    dialog.setCancelable(true);

 // 20dp
                    Window window = dialog.getWindow();
                    if (window != null) {
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                        params.x = dp2px(20);
                        window.setAttributes(params);
                    }


                    ListView listView = dialog.findViewById(R.id.lv_language);
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                            android.R.layout.simple_list_item_single_choice, showList);
                    listView.setAdapter(adapter);
                    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                    listView.setItemChecked(selectedPosition, true);


                    listView.setOnItemClickListener((parent, view, pos, id) -> {
                        String targetId = idList.get(pos); // pos
                        try {
                            int result = mSystemBinderService.setSystemLanguage(targetId);
                            if (result == 0) {

                                appendText("Language switch successful:" + showList.get(pos)); // pos
                            } else {
 // Language switch failed
                                appendText("Language switch failed");
                            }
                            dialog.dismiss();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    });

                    dialog.show();

                } catch (RemoteException e) {
                    e.printStackTrace();

                    appendText("Failed to obtain language information : " + e.getMessage());
                }
                break;
            case POSITION_SCREEN_TIMEOUT:
                showScreenTimeoutDialog();
                break;
            case POSITION_SET_NETWORK_MODE:
                showNetworkModeDialog();
                break;

            case POSITION_GET_NETWORK_DEVICE_INFO:
                showNetworkAndDeviceInfoDialog();
                break;

            case POSITION_CONNECT_WIFI:
                showWifiListDialog();
                break;
            case POSITION_SET_VPN_PROXY:
 // List item position: Set VPN proxy : VPN
                showSetVpnDialog();
                break;
            case POSITION_CLEAR_VPN_PROXY:
 // List item position: Remove VPN proxy : VPN
                clearVpnProxy();
                break;
            case POSITION_GRANT_VPN_PERMISSION:
 // List item position: Grant VPN access permission : VPN
                grantVpnPermission();
                break;
            case POSITION_CHECK_VPN_PERMISSION:
 // List item position: Check VPN permissions : VPN
                checkVpnPermission();
                break;
            case POSITION_DEFAULT_35:
                break;

            case POSITION_WRITESAFEDISK:
                    /*
                    0	success 成功
                    -1	Parameter error / Write failure 参数错误 / 写入失败
                    -2	Insufficient disk space 磁盘空间不足
                    -5	Directory conflict (there is a file with the same name, unable to create directory) 目录冲突（存在同名文件，无法创建目录）
                    */
                String testData = "Hello, this is a test file! " + System.currentTimeMillis();
                byte[] dataBytes = testData.getBytes();
                try {
                    appendText("Write safeDisk : safeDisk"+"\r\n"+"testData : "+testData);
                    int retWrite = mSystemBinderService.apiWriteSafeDiskArea("safeDisk.txt", dataBytes);
                    appendInfoText( "retWrite : " +retWrite );
                    if (retWrite == 0) {
                        appendInfoText("apiWriteSafeDiskArea : success ");
                    } else if (retWrite == -1){
                        appendErrorText("apiWriteSafeDiskArea : fail ");
                    }else if ( retWrite ==  -2){
                        appendErrorText("apiWriteSafeDiskArea : Insufficient disk space ");
                    }else if ( retWrite ==  -5){
 // ( , )
                        appendErrorText("apiWriteSafeDiskArea ： Directory conflict (there is a file with the same name, unable to create directory) ");
                    }
                    appendInfoText("****************");

                    appendText("Write  safeDisk/data.txt"+"\r\n"+"testData : "+testData);
                    int retWriteData = mSystemBinderService.apiWriteSafeDiskArea("/safeDisk/data.txt", dataBytes);
                    appendInfoText( "retWriteData : " +retWriteData );
                    if (retWriteData == 0) {
                        appendInfoText(" apiWriteSafeDiskArea ： success ");
                    } else if (retWriteData == -1){
                        appendErrorText(" apiWriteSafeDiskArea ： fail ");
                    }else if ( retWriteData ==  -2){
                        appendErrorText(" apiWriteSafeDiskArea ： Insufficient disk space ");
                    }else if ( retWriteData ==  -5){
                        appendErrorText(" apiWriteSafeDiskArea ： Directory conflict (there is a file with the same name, unable to create directory) ");
                    }
                    appendInfoText("****************");

                    appendText("Write safeDisk2/data.txt "+"\r\n"+ "testData : "+testData);
                    int retWriteData2 = mSystemBinderService.apiWriteSafeDiskArea("/safeDisk2/data.txt", dataBytes);
                    appendInfoText( "retWriteData2 : " +retWriteData2 );
                    if (retWriteData2 == 0) {
                        appendInfoText(" apiWriteSafeDiskArea ： success ");
                    } else if (retWriteData2 == -1){
                        appendErrorText(" apiWriteSafeDiskArea ： fail ");
                    }else if ( retWriteData2 ==  -2){
                        appendErrorText(" apiWriteSafeDiskArea ： Insufficient disk space ");
                    }else if ( retWriteData2 ==  -5){
                        appendErrorText(" apiWriteSafeDiskArea ： Directory conflict (there is a file with the same name, unable to create directory) ");
                    }
                    appendInfoText("****************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_WRITESAFEDISK_FILE:
                /**
                 * 0	创建成功（已存在也返回 0）
                 * -1	参数错误 / 系统异常
                 * -2	磁盘空间不足
                 * -3	目录冲突：存在同名文件，无法创建目录
                 */
                try {
 // log
                    int result = mSystemBinderService.createSafeDiskDirectory("log");
                    appendText("createSafeDiskDirectory log: " +result);
                    if (result == 0){
                        appendText(" write : Succes");
                    }else if(result == -1 ){
                        appendText(" write fail ");
                    }else if (result == -2){
                        appendText(" Insufficient disk space ");
                    }
                    else if (result == -3){
                        appendText(" Directory conflict (there is a file with the same name, unable to create directory) ");
                    }
 // config/2025/05
                    int resultpath = mSystemBinderService.createSafeDiskDirectory("config/2025/05");
                    appendText("createSafeDiskDirectory config/2025/05 : "+resultpath );
                    if (resultpath == 0){
                        appendText(" write : Succes");
                    }else if(resultpath == -1 ){
                        appendText(" write fail ");
                    }else if (resultpath == -2){
                        appendText(" Insufficient disk space ");
                    }
                    else if (resultpath == -3){
                        appendText(" Directory conflict (there is a file with the same name, unable to create directory) ");
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }


                break;
            case POSITION_READSAFEDISK:
                /**
                 * 读取安全磁盘区(/afdata/)指定文件内容
                 * Read the content of the specified file in the secure disk area (/afdata/)
                 *
                 * @param fileName 【入参】文件相对路径/纯文件名
                 *        Input parameter: Relative file path or pure file name
                 *        数据类型：String
                 *        Data type: String
                 *        取值范围：
                 *        Value range:
                 *        1. 纯文件名格式：仅文件名称带后缀，例：safeDisk.txt、config.bin
                 *           Pure file name format: File name with suffix only, e.g. safeDisk.txt, config.bin
                 *        2. 多级相对路径格式：带子目录+文件名，例：/safeDisk/data.txt、log/2026/log.dat
                 *           Multi-level relative path format: Subdirectory + file name, e.g. /safeDisk/data.txt, log/2026/log.dat
                 *        限制：不可传入绝对路径、../、/ 等越路字符，文件名必须带后缀(.txt/.bin/.dat等)
                 *        Restriction: Absolute path, ../, / and other path traversal characters are not allowed, file name must have suffix (.txt/.bin/.dat etc.)
                 *
                 * @param readBuf 【出参】文件读取数据输出缓冲区（inout数组，调用方提前分配内存）
                 *        Output parameter: Output buffer for file read data (inout array, memory allocated by caller in advance)
                 *        数据类型：byte[]
                 *        Data type: byte[]
                 *        取值范围：数组长度自定义，建议4096字节，不足会截断文件内容
                 *        Value range: Custom array length, 4096 bytes recommended, file content will be truncated if buffer is insufficient
                 *
                 * @return 接口执行结果码
                 *         Interface execution result code
                 *         数据类型：int
                 *         Data type: int
                 *         返回值枚举说明 & 示例：
                 *         Return code description & example:
                 *         0  - 读取成功，文件数据已写入readBuf缓冲区
                 *              0  - Read success, file data written to readBuf buffer
                 *         -1 - 参数错误：fileName为空 / readBuf数组为null / 文件名称无后缀
                 *             -1 - Invalid parameter: fileName empty / readBuf null / file name without suffix
                 *         -2 - 文件不存在，目标路径下无对应文件
                 *             -2 - File not found, no matching file under target path
                 *         -3 - 路径冲突：目标路径是文件夹而非文件，无法读取
                 *             -3 - Path conflict: Target path is directory instead of file, read failed
                 *         -4 - IO读取异常：文件流打开/读取/关闭失败
                 *             -4 - IO exception: File stream open/read/close failed
                 *         -6 - 权限拒绝：调用应用未配置android.uid.mdm共享用户，无安全磁盘访问权限
                 *             -6 - Permission denied: Calling app without android.uid.mdm sharedUserId, no secure disk access permission
                 *         示例：ret == 0 读取成功；ret == -2 文件不存在
                 *         Example: ret == 0 means read success; ret == -2 means file not found
                 */
                byte[] outReadBuf = new byte[1024];
                try {
                    int retRead = mSystemBinderService.apiReadSafeDiskArea("safeDisk.txt", outReadBuf);
                    if (retRead == 0) {
                        appendText(" read success ");
                        String resultBytesToAsciiString = AsciiUtils.bytesToAsciiString(outReadBuf);
                        appendText(resultBytesToAsciiString);
                    } else {
                        appendText(" read fail : "+retRead);
                    }
                    byte[] dataBytesRead = new byte[1024];
                    byte[] data2bytesRead = new byte[1024];
                    int retReaddataBytesRead = mSystemBinderService.apiReadSafeDiskArea("/safeDisk/data.txt", dataBytesRead);
                    if (retReaddataBytesRead == 0){
                        appendInfoText("read Success : " + AsciiUtils.bytesToAsciiString(dataBytesRead));
                    }else {
                        appendErrorText("read Fail : "+retReaddataBytesRead);
                    }
                    int retReaddata2bytesRead = mSystemBinderService.apiReadSafeDiskArea("/safeDisk2/data.txt", data2bytesRead);
                    if (retReaddata2bytesRead == 0){
                        appendInfoText("read Success : " + AsciiUtils.bytesToAsciiString(data2bytesRead));
                    }else {
                        appendErrorText("read Fail : "+retReaddata2bytesRead);
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case POSITION_FINDSAFEDISK:
                /**
                 * 读取安全磁盘区文件
                 * @param fileName 文件名
                 * @param buf 输出缓冲区（inout参数）
                 * @return 0=成功，其他=失败
                 */
                try {
                    String strFindSafe = mSystemBinderService.apiFindSafeDiskArea();
                    LogUtils.e(" apiFindSafeDiskArea :" + strFindSafe);
                    if (!TextUtils.isEmpty(strFindSafe)) {
                        appendText(" apiFindSafeDiskArea :" + strFindSafe);
                    } else {
                        appendText("apiFindSafeDiskArea Fail : "+strFindSafe);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

            case POSITION_FINDFILENAME_SAFEDISK:
                /**
                 * Query the size (in bytes) of the specified file in the secure disk area.     查询安全磁盘区指定文件大小（单位：字节）
                 * @param fileName File name (such as test.txt / log/config.txt)                fileName 文件名（如 test.txt / log/config.txt）
                 * @return File size (bytes), -1 = File does not exist/failed, -6 = Permission denied     文件大小（字节），-1=文件不存在/失败，-6=权限拒绝
                 */
                long fileSize = 0;
                try {
                    fileSize = mSystemBinderService.getSafeDiskFileSize("/safeDisk/data.txt");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (fileSize == -6) {

                    appendText("SafeDisk : -6  "+ "No permission to access the secure disk area");
                } else if (fileSize == -1) {

                    Log.e("SafeDisk : ", "The file does not exist.");
                } else {
                    appendText("SafeDisk : "+ "File size : " + fileSize + " Byte");
                }

            break ;
            case OPEN_SYSTEM_SETTINGS:
                openSystemSettings();
                break;
            case OPEN_SYSTEM_GETDEVICEOSVERSION:
                getDeviceosVersion();
                break;
            case OPEN_SYSTEM_GETAPPVERSIONINFO:
                getAppVersionInfo();
                break;



        }
    }

    void getDeviceosVersion(){
        try {
            String deviceVersionMsg = mSystemBinderService.getDeviceosVersion();
            appendText(" The device version information is as follows: "+ deviceVersionMsg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
    void getAppVersionInfo(){
        String[] strPkg = {"com.szanfu.sdk.service","com.szanfu.core.server"};
        try {
            for (int i = 0; i < strPkg.length; i++) {
               appendText( strPkg[i]+" VersionMsg : "+ mSystemBinderService.getAppVersionInfo(strPkg[i]));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * 打开系统设置界面（等价 adb shell am start -n com.android.settings/.Settings）
     */
    private void openSystemSettings() {
        appendText("The system settings are being opened....");
        try {
            android.content.Intent intent = new android.content.Intent();
            intent.setComponent(new android.content.ComponentName(
                    "com.android.settings",
                    "com.android.settings.Settings"
            ));
            startActivity(intent);
            appendText("System settings have been successfully opened. ✅");
        } catch (Exception e) {
            e.printStackTrace();
            appendErrorText("Failed to open system settings", e.getMessage());
        }
    }


 // ==================== : VPN ( 20dp) ====================
    private void showSetVpnDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_vpn);
        dialog.setCancelable(true);

 // 20dp
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            params.x = dp2px(20);
            window.setAttributes(params);
        }

        EditText etIp = dialog.findViewById(R.id.et_proxy_ip);
        EditText etPort = dialog.findViewById(R.id.et_proxy_port);
        Button btnConfirm = dialog.findViewById(R.id.btn_confirm);

        btnConfirm.setOnClickListener(v -> {
            String ip = etIp.getText().toString().trim();
            String portStr = etPort.getText().toString().trim();

            if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(portStr)) {
 // IP/
                appendText("IP/Port cannot be empty");
                return;
            }

            int port = Integer.parseInt(portStr);
            try {
                int result = mSystemBinderService.setVpnGlobalProxy(ip, port, "localhost,127.0.0.1");
                if (result == 0) {
 // VPN
                    appendText("VPN proxy settings have been successfully configured. ：" + ip + ":" + port);
                } else {
 // VPN
                    appendText("VPN proxy settings failed");
                }
                dialog.dismiss();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

        dialog.show();
    }

 // VPN
    private void clearVpnProxy() {
        try {
            int result = mSystemBinderService.clearVpnGlobalProxy();
 // VPN
            appendText(result == 0 ? "VPN proxy cleared successfully" : "Failure to clear");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

 // VPN
    private void grantVpnPermission() {
        String pkg = "com.szanfu.sdk.service";
        try {
            int result = mSystemBinderService.grantVpnControlPermission(pkg);
 // VPN
            appendText(result == 0 ? "Authorization of VPN permissions was successful. " : "Authorization failed");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

 // VPN
    private void checkVpnPermission() {
        String pkg = "com.szanfu.sdk.service";
        try {
            int result = mSystemBinderService.checkVpnPermission(pkg);
 // VPN VPN
            appendText(result == 0 ? "Already have a VPN Permission " : "Does not have VPN access permission");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }



 // ==================== 1. ====================
    private void showNetworkModeDialog() {
        if (mSystemBinderService == null) return;
        try {
            List<String> modes = Arrays.asList("WiFi", "Ethernet", "SIM data");
            int current = mSystemBinderService.getCurrentNetworkMode();

            Dialog d = new Dialog(this);
            d.setContentView(R.layout.dialog_language_list);
            ListView lv = d.findViewById(R.id.lv_language);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, modes);
            lv.setAdapter(adapter);
            lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            lv.setItemChecked(current, true);

            lv.setOnItemClickListener((pp, v, pos, id) -> {
                try {
                    int r = mSystemBinderService.setNetworkMode(pos);
                    appendText("Switch network mode: " + modes.get(pos) + (r == 0 ? "Success" : "Failure"));
                    d.dismiss();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });

            setDialogRight20dp(d);
            d.show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

 // ==================== 2. + ====================
    private void showNetworkAndDeviceInfoDialog() {
        if (mSystemBinderService == null) return;
        try {
            String info = mSystemBinderService.getNetworkAndDeviceInfo();
            LogUtils.e("getNetworkAndDeviceInfo info : "+info);
            List<String> list = new ArrayList<>();
            list.add(info);

            Dialog d = new Dialog(this);
            d.setContentView(R.layout.dialog_language_list);
            ListView lv = d.findViewById(R.id.lv_language);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, list);
            lv.setAdapter(adapter);
            setDialogRight20dp(d);
            d.show();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


 // [ ]WiFi ( )
    /*private void showWifiListDialog() {
        if (mSystemBinderService == null) {
 // SDK , WiFi
            appendText("The SDK service is not connected, so the WiFi list cannot be obtained.");
            return;
        }

        try {
            List<String> wifiList = mSystemBinderService.getWifiList();
            if (wifiList == null || wifiList.isEmpty()) {
 // WiFi → 5
 // WiFi, 5
                appendText("No available WiFi was detected. The system will automatically switch to manual input in 5 seconds.");
                new android.os.Handler().postDelayed(() -> showConnectWifiDialog(null), 5000);
                return;
            }

 // " "
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            String currentSsid = wm.getConnectionInfo().getSSID();
            if (currentSsid != null && currentSsid.startsWith("\"") && currentSsid.endsWith("\"")) {
                currentSsid = currentSsid.substring(1, currentSsid.length() - 1);
            }

            List<String> displayList = new ArrayList<>();
            int selectedPos = -1;
            for (String ssid : wifiList) {
                if (ssid.equals(currentSsid)) {
                    displayList.add(ssid + " (Connected) ");
                    selectedPos = displayList.size() - 1;
                } else {
                    displayList.add(ssid);
                }
            }


            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_language_list);
            dialog.setCancelable(true);

 // 20dp
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                params.x = dp2px(20);
                window.setAttributes(params);
            }

            ListView listView = dialog.findViewById(R.id.lv_language);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_single_choice, displayList);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            if (selectedPos != -1) {
                listView.setItemChecked(selectedPos, true);
            }

 // WiFi → , SSID
            int finalSelectedPos = selectedPos;
            listView.setOnItemClickListener((parent, view, pos, id) -> {
                dialog.dismiss();
                String selected = wifiList.get(pos);
                showConnectWifiDialog(selected); // SSID
            });

            dialog.show();

        } catch (RemoteException e) {
            e.printStackTrace();
 // WiFi
            appendText("Failed to obtain the list of WiFi networks:" + e.getMessage());

            showConnectWifiDialog(null);
        }
    }*/

    /**
     * 【修改后】异步方式加载WiFi弹窗（替换旧showWifiListDialog）
     */
    private void showWifiListDialog() {
        if (mSystemBinderService == null) {
            appendText("The SDK service is not connected, so the WiFi list cannot be obtained.");
            return;
        }

        try {
            appendText("Start scanning WiFi...");

            mSystemBinderService.getWifiListCallback(mWifiScanCallback);
        } catch (RemoteException e) {
            e.printStackTrace();
            appendText("Failed to invoke wifi scan api:" + e.getMessage());
            showConnectWifiDialog(null);
        }
    }

    /**
     * 接收扫描完成结果，执行原有弹窗逻辑
     */
    private void handleWifiScanResult(List<String> wifiList) {
        if (wifiList == null || wifiList.isEmpty()) {
            appendText("No available WiFi was detected. The system will automatically switch to manual input in 5 seconds.");
            new Handler().postDelayed(() -> showConnectWifiDialog(null), 5000);
            return;
        }

 // " " ( )
        WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        String currentSsid = wm.getConnectionInfo().getSSID();
        if (currentSsid != null && currentSsid.startsWith("\"") && currentSsid.endsWith("\"")) {
            currentSsid = currentSsid.substring(1, currentSsid.length() - 1);
        }

        List<String> displayList = new ArrayList<>();
        int selectedPos = -1;
        for (String ssid : wifiList) {
            if (ssid.equals(currentSsid)) {
                displayList.add(ssid + " (Connected) ");
                selectedPos = displayList.size() - 1;
            } else {
                displayList.add(ssid);
            }
        }


        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_language_list);
        dialog.setCancelable(true);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            params.x = dp2px(20);
            window.setAttributes(params);
        }

        ListView listView = dialog.findViewById(R.id.lv_language);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_single_choice, displayList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (selectedPos != -1) {
            listView.setItemChecked(selectedPos, true);
        }

        int finalSelectedPos = selectedPos;
        listView.setOnItemClickListener((parent, view, pos, id) -> {
            dialog.dismiss();
            String selected = wifiList.get(pos);
            showConnectWifiDialog(selected);
        });

        dialog.show();
    }

 // dp px ( )
    public int dp2px(int dpValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

 // [ ] WiFi , SSID
    private void showConnectWifiDialog(String presetSsid) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_connect_wifi);
        dialog.setCancelable(true);

 // 20dp
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            params.x = dp2px(20);
            window.setAttributes(params);
        }

        EditText etSsid = dialog.findViewById(R.id.et_ssid);
        EditText etPwd = dialog.findViewById(R.id.et_pwd);
        Button btnConfirm = dialog.findViewById(R.id.btn_ok);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);

 // SSID( )
        if (presetSsid != null) {
            etSsid.setText(presetSsid);

            etSsid.setSelection(etSsid.getText().length());
        }

        btnConfirm.setOnClickListener(v -> {
            String ssid = etSsid.getText().toString().trim();
            String pwd = etPwd.getText().toString().trim();

            if (TextUtils.isEmpty(ssid)) {
 // WiFi
                appendText("WiFi name cannot be left blank");
                return;
            }

            try {
                mSystemBinderService.connectToWifiCallback(ssid, pwd, new IWifiConnectCallback.Stub() {
                    @Override
                    public void onResult(int code) throws RemoteException {

                        runOnUiThread(()->{
                            switch (code){
                                case 0:
                                    LogUtils.d("WIFI : "+"连接成功");
                                    appendText( "WIFI : "+"连接成功");
                                    break;
                                case 2:
                                    LogUtils.d("WIFI : "+"WiFi硬件未开启");
                                    appendText( "WIFI : "+"WiFi硬件未开启");
                                    break;
                                case 3:
                                    LogUtils.d("WIFI : "+"密码错误");
                                    appendText( "WIFI : "+"密码错误");
                                    break;
                                case 4:
                                    LogUtils.d("WIFI : "+"找不到目标WiFi");
                                    appendText(  "WIFI : "+"找不到目标WiFi");
                                    break;
                                case 5:
                                    LogUtils.d("WIFI : "+"添加WiFi配置失败");
                                    appendText(  "WIFI : "+"添加WiFi配置失败");
                                    break;
                                default:
                                    LogUtils.d("WIFI : "+"连接失败/超时");
                                    appendText("WIFI : "+"连接失败/超时");
                                    break;
                            }
                        });
                    }
                });
            } catch (RemoteException e) {
                e.printStackTrace();
            }
             dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

 // ==================== 20dp ====================
    private void setDialogRight20dp(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams p = window.getAttributes();
            p.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            p.x = dp2px(20);
            window.setAttributes(p);
        }
    }


    /**
     * 显示屏幕超时时间选择弹窗（靠右20dp + 列表单选 + 当前值自动选中）
     */
    private void showScreenTimeoutDialog() {
        if (mSystemBinderService == null) {
 // SDK
            appendText("SDK service is not connected");
            return;
        }

        try {
 // ===================== 1. =====================
            List<String> nameList = new ArrayList<>();
            List<Long> valueList = new ArrayList<>();

            nameList.add("Never");        valueList.add(-1L);
            nameList.add("15 Second");       valueList.add(15000L);
            nameList.add("30Second");       valueList.add(30000L);
            nameList.add("1minutes");       valueList.add(60000L);
            nameList.add("2minutes");       valueList.add(120000L);
            nameList.add("5minutes");       valueList.add(300000L);
            nameList.add("10minutes");      valueList.add(600000L);
            nameList.add("30minutes");      valueList.add(1800000L);

 // ===================== 2. , =====================
            long currentMs = mSystemBinderService.getScreenOffTimeout();
            int selectedPos = 2;

            for (int i = 0; i < valueList.size(); i++) {
                long val = valueList.get(i);
                if (val == -1 && currentMs > 3600000) {
                    selectedPos = i;
                    break;
                }
                if (val == currentMs) {
                    selectedPos = i;
                    break;
                }
            }

 // ===================== 3. =====================
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_language_list);
            dialog.setCancelable(true);

 // 20dp
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                params.x = dp2px(20);
                window.setAttributes(params);
            }

 // ===================== 4. =====================
            ListView listView = dialog.findViewById(R.id.lv_language);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_single_choice, nameList);
            listView.setAdapter(adapter);
            listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            listView.setItemChecked(selectedPos, true);

 // ===================== 5. =====================
            listView.setOnItemClickListener((parent, view, pos, id) -> {
                long timeoutMs = valueList.get(pos);
                String name = nameList.get(pos);


                if (timeoutMs == -1) {
                    timeoutMs = 3600000 * 24;
                }

                try {
                    int result = mSystemBinderService.setScreenOffTimeout(timeoutMs);
                    if (result == 0) {

                        appendText("Screen timeout setting successful:" + name);
                    } else {

                        appendText("Failed to set screen timeout.：" + name);
                    }
                    dialog.dismiss();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            });

            dialog.show();

        } catch (RemoteException e) {
            e.printStackTrace();

            appendText("Failed to obtain screen timeout information ：" + e.getMessage());
        }
    }

    /**
     * 显示系统字体大小调节弹窗（居右20dp）
     */
    private void showFontDialog() {
        if (mSystemBinderService == null) {
 // SDK
            appendText("The SDK service is not connected, so the font adjustment cannot be performed.");
            return;
        }

        try {
 // 1. Dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_font);
            dialog.setCancelable(true);

            Window window = dialog.getWindow();
            if (window == null) {

                appendText("Window creation failed ");
                return;
            }

 // 2. 20dp
            WindowManager.LayoutParams params = window.getAttributes();
            params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
            params.x = dp2px(20); // 20dp
            window.setAttributes(params);


            SeekBar seekBar = dialog.findViewById(R.id.seekBar_font);
            TextView tvValue = dialog.findViewById(R.id.tv_font_value);

 // 4. SeekBar
            float currentScale = mSystemBinderService.getSystemFontScale();

            // 0.85f -> 0
            // 1.0f  -> 1
            // 1.15f -> 2
            // 1.30f -> 3
            int progress;
            if (currentScale <= 0.85f) {
                progress = 0;
            } else if (currentScale <= 1.0f) {
                progress = 1;
            } else if (currentScale <= 1.15f) {
                progress = 2;
            } else {
                progress = 3;
            }

 // SeekBar : 0~3( 4 , )
            seekBar.setMax(3);
            seekBar.setProgress(progress);
            tvValue.setText(getFontScaleText(currentScale));


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    float scale;
                    switch (progress) {
                        case 0:
                            scale = 0.85f;
                            break;
                        case 1:
                            scale = 1.0f;
                            break;
                        case 2:
                            scale = 1.15f;
                            break;
                        case 3:
                            scale = 1.30f;
                            break;
                        default:
                            scale = 1.0f;
                    }
                    tvValue.setText(getFontScaleText(scale));
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int progress = seekBar.getProgress();
                    float scale;
                    switch (progress) {
                        case 0:
                            scale = 0.85f;
                            break;
                        case 1:
                            scale = 1.0f;
                            break;
                        case 2:
                            scale = 1.15f;
                            break;
                        case 3:
                            scale = 1.30f;
                            break;
                        default:
                            scale = 1.0f;
                    }

                    try {
                        int result = mSystemBinderService.setSystemFontScale(scale);
                        if (result == 0) {

                            appendText("Font size setting was successful : " + getFontScaleText(scale));
                        } else {

                            appendText("Failed to set the font size : " + getFontScaleText(scale));
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();

                        appendText("Set font exception : " + e.getMessage());
                    }
                }
            });

            dialog.show();

        } catch (RemoteException e) {
            e.printStackTrace();

            appendText("Exception occurred while obtaining font information: " + e.getMessage());
        }
    }

    /**
     * 将缩放值转换为友好文本
     */
    private String getFontScaleText(float scale) {
        if (scale <= 0.85f) {

            return "Little";
        } else if (scale <= 1.0f) {

            return "Default";
        } else if (scale <= 1.15f) {

            return "Big";
        } else {

            return "Extra-large";
        }
    }

    /**
     * 显示屏幕亮度调节弹窗
     */
    /**
     * 显示屏幕亮度调节弹窗
     */
    private void showBrightnessDialog() {
        try {
            if (mSystemBinderService == null) {
 // SDK
                appendText("The SDK service is not connected, so the brightness cannot be adjusted.");
                return;
            }

 // Dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_brightness);
            dialog.setCancelable(true);

            Window window = dialog.getWindow();
            if (window == null) {

                appendText("Window creation failed");
                return;
            }

            WindowManager.LayoutParams params = window.getAttributes();


            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.y = dp2px(100); // 100dp
            params.width = WindowManager.LayoutParams.MATCH_PARENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;

 // 2. 20dp( !)
            params.horizontalMargin = dp2px(20) / getResources().getDisplayMetrics().widthPixels;

            window.setAttributes(params);


            SeekBar seekBar = dialog.findViewById(R.id.seekBar_brightness);
            TextView tvValue = dialog.findViewById(R.id.tv_brightness_value);


            int currentBrightness = mSystemBinderService.getScreenBrightness();
            seekBar.setProgress(currentBrightness);
            tvValue.setText(currentBrightness + "%");


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    tvValue.setText(progress + "%");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    int value = seekBar.getProgress();
                    try {
                        int result = mSystemBinderService.setScreenBrightness(value);
                        if (result == 0) {

                            appendText("Screen brightness setting successful : " + value + "%");
                        } else {

                            appendText("Failed to set screen brightness : " + value + "%");
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();

                        appendText("Abnormal brightness setting : " + e.getMessage());
                    }
                }
            });

            dialog.show();

        } catch (RemoteException e) {
            e.printStackTrace();

            appendText(getResources().getString(R.string.getLightError)+": " + e.getMessage());
        }
    }

    /**
     * dp 转 px
     */
    private int dp2px(float dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }

    /**
     * 显示音量调节弹窗（靠右20dp）
     */
    private void showVolumeDialog() {

        if (mSystemBinderService == null) {
 // SDK
            appendText(getResources().getString(R.string.sdkServiceErrorVold));
            return;
        }

        try {
 // 2. Dialog
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.dialog_volume);
            dialog.setCancelable(true);

 // 3. Initialize controls
            SeekBar seekBar = dialog.findViewById(R.id.seekBar_volume);
            TextView tvValue = dialog.findViewById(R.id.tv_value);

 // 4. Volume type: Media volume
            int streamType = 3;
            /**
             * STREAM_VOICE_CALL	0	通话音量
             * STREAM_SYSTEM	1	系统提示音（按键、锁屏等）
             * STREAM_RING	2	来电铃声
             * STREAM_MUSIC	3	媒体（音乐、视频、游戏）
             * STREAM_ALARM	4	闹钟
             * STREAM_NOTIFICATION	5	通知（短信、App 推送）
             * STREAM_BLUETOOTH_SCO	6	蓝牙通话
             * STREAM_DTMF	8	拨号按键音
             * STREAM_ACCESSIBILITY	10	无障碍语音
             */

 // 5. Obtain the current volume, maximum value, and minimum value
            int current = mSystemBinderService.getSoundVolume(streamType);
            int max = mSystemBinderService.getSoundMaxVolume(streamType);
            int min = mSystemBinderService.getSoundMinVolume(streamType);

 // 6. SeekBar
            seekBar.setMax(max);
            seekBar.setMin(min);
            seekBar.setProgress(current);

            tvValue.setText("Current volume ：" + current);


            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        try {
 // SDK
                            mSystemBinderService.setSoundVolume(streamType, progress);

                            tvValue.setText("Current volume ：" + progress);

                            appendText("Volume setting successful : " + progress);
                        } catch (RemoteException e) {
                            e.printStackTrace();

                            appendText("Failed to set the volume : " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

 // ===================== 20dp =====================
            Window window = dialog.getWindow();
            if (window != null) {
                WindowManager.LayoutParams params = window.getAttributes();
                params.gravity = Gravity.CENTER_VERTICAL | Gravity.END;
                params.x = dp2px(20); // 20dp
                window.setAttributes(params);
            }

            dialog.show();

        } catch (RemoteException e) {
            e.printStackTrace();
            appendText(getResources().getString(R.string.ObtainAbnormalVolumeInformation)+" : " + e.getMessage());
        }
    }

}

