package com.szanfu.sdk.test.activity;

import static android.graphics.Bitmap.createBitmap;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.InputType;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import com.bigkoo.pickerview.TimePickerView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.system.EncDecResult;
import com.szanfu.sdk.api.system.GetAttributeResult;
import com.szanfu.sdk.api.system.HalReadUserKeyByIdResult;
import com.szanfu.sdk.api.system.IFirmwareUpdateCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ISystemEncDecBinderService;
import com.szanfu.sdk.api.system.ISystemReadUserKeyBinderService;
import com.szanfu.sdk.api.system.VbatStateParam;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.entity.LcdInfo;
import com.szanfu.sdk.test.util.AFUtils;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.AsciiUtils;
import com.szanfu.sdk.test.util.CommonSetSNDialog;
import com.szanfu.sdk.test.util.CommonSetTUSNDialog;
import com.szanfu.sdk.test.util.CpuUsageUtil;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.LcdUtils;
import com.szanfu.sdk.test.util.LedState;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.SdCardFileUtils;
import com.szanfu.sdk.test.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class SystemServiceActivity extends ListButtonBaseActivity {



    private ImageView ivAnimationView;


    private Handler animationHandler = new Handler(android.os.Looper.getMainLooper());
    private Runnable animationRunnable;
    private int currentFrame = 0;
    private final int totalFrames = 50; // 00000 ~ 00049
    private final int delayMs = 40; // ( )
    private boolean isPlaying = false;

    private CpuUsageUtil cpuUtil;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    private static final String TAG = "SystemServiceActivity";
    private int iPrinterSuccess = 0;
    private int iPrinterError = 0;
    private int iPrinterCount = 0;
    private int iPrinterCountOrg = 1;
    public static final int POSITION_READ_SERIAL_VERSION = 0;
    public static final int POSITION_READ_SP_VERSION = 1;
    public static final int POSITION_POWER_ON_APP = 2;
    public static final int POSITION_POWER_Down_APP = 3;
    public static final int POSITION_RESET_SP = 4;
    public static final int POSITION_BEEP_TEST = 5;
    public static final int POSITION_OPEN_RF_LIGHT = 6;
    public static final int POSITION_CLOSE_RF_LIGHT = 7;
    public static final int SET_LED_ALLSTATUS = 8; // LED
    public static final int POSITION_GET_SN = 9;
    public static final int POSITION_GET_TUSN = 10;

    public static final int POSITION_SET_HAL_READUSERKEYNUM = 11;
    public static final int POSITION_SET_HAL_READUSERKEYBYID = 12;
    public static final int POSITION_GETPCBINFO = 13;
    public static final int POSITION_BUTTON_CELL_TEST = 14;
    public static final int POSITION_GETIMEI_TEST = 15;
    public static final int POSITION_SETOSDATETIME = 16;

    public static final int POSITION_ENCDEC = 17;

    public static final int DUKPT_SET_BDKEY_INDEX25 = 18;
    public static final int DUKPT_GET_BDKEY_INDEX25_KSN = 19;
    public static final int DUKPT_INCREMENT_BDKEY_KSN = 20;
    public static final int DUKPT_INDEX25_TDES_CBC_ENCRYPT = 21;
    public static final int DUKPT_CHECK_BDKEY_INDEX25_IMPORTED = 22;
    public static final int RESTART_TES23 = 23;    //
    public static final int DUKPT_SET_IPEK_INDEX26 = 24;
    public static final int DUKPT_GET_IPEK_INDEX26_KSN = 25;
    public static final int DUKPT_INCREMENT_IPEK_KSN = 26;
    public static final int DUKPT_INDEX26_TDES_CBC_ENCRYPT = 27;
    public static final int DUKPT_CHECK_IPEK_INDEX26_IMPORTED = 28;
    public static final int DUKPT_DUKPTSETPLAINKSN = 29;
    public static final int OPEN_SCAN_QR_CODE = 30; // 980
    public static final int Close_SCAN_QR_CODE = 31; // 980

    public static final int RESTART_DEVICE = 32;
    public static final int RESTART_TEST = 33;    //
    public static final int RESTART_DAL_USBOPEN_NO_BLOCKING = 34; // DAL_UsbOpen
    public static final int RESTART_DAL_USBOPEN_BLOCKING = 35; // DAL_UsbOpen
    public static final int RESTART_TEST_45 = 36;    //
    public static final int RESTART_WRITE_SDCARD = 37; // SDCard / , , ;
    public static final int KEYBOARD_NUMBER_ONKEYLISTENER = 38; // number, setOnKeyListener
    public static final int POSITION_KEYBOARD_LIGHT_TEST = 39; // Keyboard backlight ON/OFF test
    public static final int OPEN_SYSTEM_SETTINGS = 40;
    public static final int POSITION_READ_DEVICE_CONFIG_INFO = 41; // Read device config info
    public static final int POSITION_GET_KEY_MAP = 42;
    public static final int POSITION_REMOVE_KEY = 43;
    public static final int POSITION_ENTER_PAN = 44;
    public static final int POSITION_DUKPT_GET_CARD_DATA = 45;
    public static final int POSITION_CLEAR_TEMPORARY_CARD_DATA = 46;
    public static final int POSITION_CLEAR_OPEN_SETTINGS = 47;


    int retDalUsbOpen = -1;
    int timeout_ms = 1000 * 5;
    int blockFlags = 1; // 1: 0


    /*public static final int POSITION_FIRMWARE_UPDATE_TEST = 21;
    public static final int POSITION_LIGHT_OPEN_ONE_TEST = 22;
    public static final int POSITION_LIGHT_OPEN_TWO_TEST = 23;
    public static final int POSITION_LIGHT_OPEN_THREE_TEST = 24;

    public static final int POSITION_LIGHT_CLOSE_ONE_TEST = 25;
    public static final int POSITION_LIGHT_CLOSE_TWO_TEST = 26;
    public static final int POSITION_LIGHT_CLOSE_THREE_TEST = 27;*/



    private volatile boolean isThreadRunning = false;
    private Thread subThread;
    private final Object lock = new Object();

    private final AtomicBoolean shouldStop = new AtomicBoolean(false);
 // Handler
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private int attemptCount = 0;


    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private ISystemReadUserKeyBinderService mISystemReadUserKeyBinderService;
    private ISystemEncDecBinderService mISystemEncDecBinderService;
    private IPinPadBinderService mIPinPadBinderService;
    private boolean mKeyboardLightOn = false;

    /**
     * 密钥类型
     */
    private int KEY_TYPE_DUKPT_KEY_BDK = 0;
    private int KEY_TYPE_DUKPT_KEY_IPEK = 1;

    /**
     * 密钥索引
     */
    public static final int DUKPT_DBEY_INDEX_25 = 25;
    public static final int DUKPT_IPEY_INDEX_26 = 26;


    private final LcdUtils mLcdUtils = LcdUtils.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cpuUtil = CpuUsageUtil.getInstance();


        ivAnimationView = findViewById(R.id.iv_animation_view);

//        startMonitor();
        if (AppConfig.buildType == 0) { // =0 =1
            mButtons = getResources().getStringArray(R.array.SYSTEM_SERVICE_LIST);
        } else { // =0 =1
            mButtons = getResources().getStringArray(R.array.ALL_SYSTEM_SERVICE_LIST);
        }
        setButtonDataSource(mButtons);
    }

    private void startMonitor() {
        runnable = new Runnable() {
            @Override
            public void run() {

                new Thread(() -> {
                    float cpu = cpuUtil.getAppCpuUsage();
                    runOnUiThread(() -> appendText("CPU使用率: " + cpu + "%"));
                }).start();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            Log.e(TAG, "onServiceConnected: ");
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
            mISystemReadUserKeyBinderService = ISystemReadUserKeyBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM_READUSERKEY));
            mIPinPadBinderService = IPinPadBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PINPAD));
            mISystemEncDecBinderService = ISystemEncDecBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM_ENCDEC));
            mLcdUtils.init(mSystemBinderService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取串口底层SDK库版本信息接口
     * 通过传入不同uiIdMask标识，获取库版本、编译时间、代码哈希、设备公钥等不同维度版本数据
     * 底层为MTK平台专用串口Lib，返回UTF-8编码字符串字节数组，上层通过new String(byte[])解析文本
     *
     * @param uiIdMask 信息类型掩码，用于区分需要读取的版本字段，定义如下：
     *                 <ul>
     *                 <li>0：Lib Version 库版本号，示例返回字符串：0.16.980.MTK</li>
     *                 <li>1：Build Time 编译时间戳，格式YYYYMMDDHHMMSS，示例：20260603160418</li>
     *                 <li>2：Code Version 源码Git完整commit哈希串</li>
     *                 <li>9：ReadPubk 设备系统镜像公钥标识，用于区分调试样机/量产发行版：
     *                 <br>全0 BCD串 = 调试原型机(debugging prototype)
     *                 <br>非全0 BCD串 = 正式发行量产机(release)</li>
     *                 </ul>
     * @return byte[] UTF-8编码的版本信息文本字节数组；接口异常/无数据时返回null
     * @throws android.os.RemoteException Binder跨进程调用异常，服务断开、进程崩溃、权限不足时抛出
     */
    private void readSerialLibVersion(String itemName) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                try {
                    if (null != mSystemBinderService) {
                        byte[] libVersion = mSystemBinderService.readSerialLibVersion(0);
                        appendText("Lib Version: " + (libVersion == null ? "null" : new String(libVersion)));
                    }
                    if (null != mSystemBinderService) {
                        byte[] buildTime = mSystemBinderService.readSerialLibVersion(1);
                        appendText("Build Time: " + (buildTime == null ? "null" : new String(buildTime)));
                    }
                    if (null != mSystemBinderService) {
                        byte[] codeVersion = mSystemBinderService.readSerialLibVersion(2);
                        appendText("Code Version: " + (codeVersion == null ? "null" : new String(codeVersion)) + "\n");
                    }

                    if (null != mSystemBinderService) {
                        byte[] bPubk = mSystemBinderService.readSerialLibVersion(9);
                        if (null != bPubk) {
                            if (HexUtil.bcd2str(bPubk).equals("0000000000000000000000000000000000000000000000000000000000000000")) {
                                appendText("ReadPubk: " + getResources().getString(R.string.end_result_devicestype) + "\n");
                            } else {
                                appendText("ReadPubk: " + getResources().getString(R.string.end_result_devicestype_release) + "\n");
                            }
                        }
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });

    }

    /**
     * @param itemName Obtain the IMEI  获取IMEI号
     */
    private void getIMEI(String itemName) {
        appendText(itemName);
        try {
            String strIMEI = mSystemBinderService.getIMEI();
            appendText("IMEI: " + (strIMEI == null ? "null" : new String(strIMEI)));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    /**
     * Power up the SP (Secure Processor) chip.
     * <br>
     * 打开（上电）安全芯片电源。
     *
     * @return 0 if successful, non-zero if failed.
     * 0 表示成功，其它为失败
     */
    private void spPowerUp(String itemName) {
       /* appendText(itemName);
        try {
            int result = mSystemBinderService.powerUpSP();
            appendText("powerUpSP result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            appendText("powerUpSP failed: " + e.getMessage());
        }*/
    }

    /**
     * Power down the SP (Secure Processor) chip.
     * <br>
     * 关闭（下电）安全芯片电源。
     *
     * @return 0 if successful, non-zero if failed.
     * 0 表示成功，其它为失败
     */
    private void spPowerDown(String itemName) {
      /*  appendText(itemName);
        try {
            int result = mSystemBinderService.powerDownSP();
            appendText("powerDownSP result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            appendText("powerDownSP failed: " + e.getMessage());
        }*/
    }


    private void scannerOpenX(String itemName) {
        appendText(itemName);
        try {
            int result = mSystemBinderService.scannerOpen();
            Log.i(TAG, "scannerOpen result:: " + result);
            appendText("scannerOpen result: " + result);
            if (result == 0) {
                byte[] responseData = new byte[256];
                int scannerGetDataResult = mSystemBinderService.scannerGetData(responseData, 1024);
                Log.i(TAG, "scannerGetDataResult result:: " + scannerGetDataResult);
                if (scannerGetDataResult != 0) {

                    byte[] validData = Arrays.copyOf(responseData, scannerGetDataResult);
                    appendText("scannerGetDataResult DataLen :  " + scannerGetDataResult);
                    appendText("scannerGetDataResult: " + AFUtils.showResult16Str(validData));
                    Log.i(TAG, "scannerGetDataResult DataLen : " + scannerGetDataResult);
                    Log.i(TAG, "scannerGetDataResult : " + AFUtils.showResult16Str(validData));
                    int scannerCloseResult = mSystemBinderService.scannerClose();
                    appendText("scannerCloseResult: " + scannerCloseResult);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            appendText("scannerOpen failed: " + e.getMessage());
        }
    }

    private void scannerOpen(String itemName) {
        appendText(itemName);

        try {
            int result = mSystemBinderService.scannerOpen();
            Log.i(TAG, "scannerOpen result:: " + result);
            appendText("scannerOpen result: " + result);

            if (result == 0) {

                new Thread(() -> {
                    try {
                        while (true) {
                            byte[] responseData = new byte[1024];
                            int scannerGetDataResult = mSystemBinderService.scannerGetData(responseData, 1024 * 4);

                            Log.i(TAG, "scannerGetDataResult result:: " + scannerGetDataResult);

                            if (scannerGetDataResult > 0) {

                                int ret = mSystemBinderService.setBeepStatus(true, 9600, 9600, 200);

                                byte[] validData = Arrays.copyOf(responseData, scannerGetDataResult);

                                runOnUiThread(() -> {
                                    appendText("scannerGetDataResult DataLen :  " + scannerGetDataResult);
                                    String strValidData = AFUtils.showResult16Str(validData);
                                    appendText("scannerGetDataResult: " + AFUtils.showResult16Str(validData));
                                    appendText("strValidData: " + AFUtils.hexToAscii(strValidData));
                                    Log.i(TAG, "scannerGetDataResult DataLen : " + scannerGetDataResult);
                                    Log.i(TAG, "scannerGetDataResult : " + AFUtils.showResult16Str(validData));
                                });


                                int scannerCloseResult = mSystemBinderService.scannerClose();
                                runOnUiThread(() -> appendText("scannerCloseResult: " + scannerCloseResult));
                                break;

                            }

 // 500ms
                            Thread.sleep(500);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> appendText("scannerGetData failed: " + e.getMessage()));
                    }
                }).start();
            }

        } catch (Exception e) {
            e.printStackTrace();
            appendText("scannerOpen failed: " + e.getMessage());
        }
    }


    private void scannerClose(String itemName) {
        appendText(itemName);
        try {
            int result = mSystemBinderService.scannerClose();
            appendText("scannerClose result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            appendText("scannerClose failed: " + e.getMessage());
        }
    }


    private void devReBoot(String itemName) {
        appendText(itemName);
        try {
            mSystemBinderService.reBoot();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void readSpVersion(String itemName) {
        appendText(itemName);
        try {
            if (null != mSystemBinderService) {
                byte[] spBootVer = mSystemBinderService.readSpVersion(0);
                appendText("Boot Version: " + (spBootVer == null ? "null" : new String(spBootVer)));
            }

            if (null != mSystemBinderService) {
                byte[] spFirmwareVer = mSystemBinderService.readSpVersion(1);
                appendText("Firmware Version: " + (spFirmwareVer == null ? "null" : new String(spFirmwareVer)));
            }

            if (null != mSystemBinderService) {
                byte[] spHardwareVer = mSystemBinderService.readSpVersion(2);
                appendText("Hardware Version: " + (spHardwareVer == null ? "null" : new String(spHardwareVer)));
            }

            if (null != mSystemBinderService) {
                byte[] spCompDate = mSystemBinderService.readSpVersion(3);
                appendText("Compile Date: " + (spCompDate == null ? "null" : new String(spCompDate)) + "\n");
            }

            if (null != mSystemBinderService) {
                String spPubKey = mSystemBinderService.getSPPubKey();
                appendText("SP PubKey: " + (spPubKey == null ? "null" : spPubKey + "\n"));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void readDeviceConfigInfo(String itemName) {
        appendText(itemName);
        try {
            if (mSystemBinderService == null) {
                appendText(itemName + " Result: mSystemBinderService=null\n");
                return;
            }

 // devInfo 100 , len 0~100
            byte[] devInfo = new byte[100];
            int[] devInfoLen = new int[]{0};

            int ret = mSystemBinderService.getDeviceConfigInfo(devInfo, devInfoLen);

            int realLen = (devInfoLen != null && devInfoLen.length > 0) ? devInfoLen[0] : 0;

            appendText("getDeviceConfigInfo ret=" + ret + ", len=" + realLen);

            if (ret != 0) {
                appendText("getDeviceConfigInfo Result: FAIL ret=" + ret + "\n");
                return;
            }

            if (realLen <= 0) {
                appendText("getDeviceConfigInfo Result: FAIL len<=0\n");
                return;
            }

            if (realLen > devInfo.length) {
                appendText("getDeviceConfigInfo Result: FAIL len overflow (" + realLen + ">" + devInfo.length + ")\n");
                return;
            }

            String payload;
            try {
                payload = new String(devInfo, 0, realLen, "UTF-8").trim();
            } catch (Exception e) {
                payload = new String(devInfo, 0, realLen).trim();
            }

            if (payload.length() == 0) {
                appendText("getDeviceConfigInfo payload empty\n");
                appendText("payload(hex)=" + payload + "\n");
                return;
            }

            appendText("payload(hex)=" + payload + "\n");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        } catch (Throwable t) {
            t.printStackTrace();
            appendErrorText(itemName, t.getMessage());
        }
    }


    private void getPcbInfo(String itemName) {
        appendText(itemName);
        try {
            byte[] getPcbInfo = mSystemBinderService.getPcbInfo(0);
            if (null != getPcbInfo) {
                HexUtil.bcd2str(getPcbInfo);
//                Log.e("szaf:", "getPcbInfo: ---->"+ HexUtil.bcd2str(getPcbInfo));
//                Log.e("szaf:", "getPcbInfo: ---->"+ getPcbInfo.toString());
//                Log.e("szaf", "getPcbInfo: --->"+ getPcbInfo.toString());
//                Log.e("szaf", "getPcbInfo: ------>>"+ new String(getPcbInfo) );
//                appendText("getPcbInfo : " + HexUtil.bcd2str(getPcbInfo));
//                appendText("getPcbInfo : " + getPcbInfo.toString());
//                String strPCB = getPcbInfo.toString();
//                appendText("getPcbInfo : " + strPCB);

                appendText("getPcbInfo:" + bytesToHex(getPcbInfo));

            }
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void getCellTest(String itemName) {
        appendText(itemName);
        try {
            VbatStateParam vbatStateParam = mSystemBinderService.getButtonBatteryValue();
            appendText("ret : " + (vbatStateParam == null ? "null" : vbatStateParam.getRet()) + "\n"
                    + "vbatStateParam : "
                    + (vbatStateParam == null ? "null" : (vbatStateParam.getButtonBatteryValue() == null ? "null" : HexUtil.bcd2str(vbatStateParam.getButtonBatteryValue()))));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void dalRsa(String itemName) {
        appendText(itemName);
        try {
 // N B7ED2251DCF26185FA21689619AD113772D457BEA6ECE247838992E7310225F6DF2DA50B1C121345CC9BB49110FAD1F04A1F4296C0CDFF3296E277F3D913376225BA692D873002344572E719E33847E2D1FA37C6D99D8AD8A25C2FD8E07AB6576A3DC863D1C3E938DE7C92044CA8CF76C2BBE474D2D7E648C09CCC30A6EA9B17
 // D A4AA67598748714B739F497568D36B608B2BD4CAD9C3A6EA83B73D90426E3BF303E867A4C4A2E2225186ED681B1D93AB5F91111ACACF22B4F6251763776B1EDEF96E9990CD1C557A2D65EB6C4D66E2160B59C0DF1565974324E6E7B1D7D27F3CD879761A223400FA0B992C43EAE1071EB272DB6D91F8EC34C18479C4883DAA61
            String strModul = "B22F4CDBB2B965E120B9141D233F408E39A1F6A9E1C8E7C03E723D62CC0EFAF9BED83C7B5B1061A134C820E0970AA73630408A010E7B8A3C4E1C9478C77C39F2C21A55BDB3870F0987245D6BE58E72527559EBB44AE11766CF6D4E07C688B425785A2FC040D2315921CEC2A334F6ADFD44778442D8C1F003E36A50924D5F75BC3FC72BAD017CEDDF19CD5B585EFADED09FBE026BE30F36EA85618B98BB5564E004693D95B68E9CA535588839DFD8126F3692132E22F6C79A4126DF51548678B285987A7475C7EFA1FC17F4DD884854F8874FB9DEC32210620D7A6108A0ECF9DBD4D474E32D66C3000AA8739592E9186D25669A2678DF642C70979758041C7DF9";
            String strDataIn = "0002FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF004342344533454134303033303944414236353644384442464534423933463335";
            byte[] bModul = HexUtil.hexStringToByte(strModul);
            String strExp = "00010001"; // 65537    00010001
            byte[] bExp = HexUtil.hexString2ByteArray(strExp);
            byte[] bDataIn = HexUtil.hexStringToByte(strDataIn);
            byte[] bDataOut = new byte[bDataIn.length];
            EncDecResult encDecResult = mISystemEncDecBinderService.encDecResult(bModul, bModul.length, bExp, bExp.length, bDataIn, bDataOut);
            int retEncDec = encDecResult.getRet();
            byte[] bDataOutssss = encDecResult.getTagData();
            appendText("ret:" + retEncDec);
            appendText("bDataouts:" + bytesToHex(bDataOutssss));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public String bytesToHex(byte[] bytes) {
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
 // 16 , 2
        char[] resultCharArray = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) {
            resultCharArray[index++] = hexDigits[b >>> 4 & 0xf];
            resultCharArray[index++] = hexDigits[b & 0xf];
        }
        return new String(resultCharArray);
    }

    /**
     * 蜂鸣器音调控制接口：控制蜂鸣器播放指定音调、时长或立即停止
     * Buzzer tone control interface: Control buzzer to play specified tone, duration or stop immediately
     *
     * @param enable     使能开关：true 播放蜂鸣，false 立即停止发声
     *                   Enable switch: true Play beep, false Stop sounding immediately
     * @param level      音调等级：0 立即停止蜂鸣；1~12 对应硬件预设音阶
     *                   Tone level: 0 Stop beeping immediately; 1~12 Correspond to the hardware preset scale
     * @param durationMs 播放时长：单位毫秒，控制蜂鸣持续时间
     *                   Play duration: Unit in milliseconds, control the buzzer duration
     * @return int      执行结果：0 表示执行成功，非0值为错误码（异常返回）
     * Execution result: 0 means success, non-zero values are error codes (abnormal return)
     */
    private void beepTest(String itemName) {
        appendText(itemName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    /**
                     *@param enable    true Play, false Stop immediately
                     * @param level     0 Stop the beeping immediately
                     *                  1 ~ 12 Corresponding to the preset scale
                     * @param durationMs Duration (milliseconds)
                     * @return 0 Success, others are error codes
                     */
                    int ret = mSystemBinderService.setBeepTone(true, 12, 1000); //BeepStatus(true, 9600, 9600, 1000);
                    //            int ret = mSystemBinderService.setBeepStatus(true, 9600, 9600, 1000);
                    appendText("ret : " + ret);

                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });

    }

    /**
     * 四路指示灯状态控制接口
     * Four-way indicator light status control interface
     *
     * @param enableLight1 入参名称：第一路指示灯开关状态
     *                     入参数据类型：boolean 布尔型
     *                     入参取值范围：true-开启指示灯，false-关闭指示灯
     *                     Parameter Name: 1st indicator light switch status
     *                     Parameter Type: boolean
     *                     Value Range: true-Turn on the light, false-Turn off the light
     * @param enableLight2 入参名称：第二路指示灯开关状态
     *                     入参数据类型：boolean 布尔型
     *                     入参取值范围：true-开启指示灯，false-关闭指示灯
     *                     Parameter Name: 2nd indicator light switch status
     *                     Parameter Type: boolean
     *                     Value Range: true-Turn on the light, false-Turn off the light
     * @param enableLight3 入参名称：第三路指示灯开关状态
     *                     入参数据类型：boolean 布尔型
     *                     入参取值范围：true-开启指示灯，false-关闭指示灯
     *                     Parameter Name: 3rd indicator light switch status
     *                     Parameter Type: boolean
     *                     Value Range: true-Turn on the light, false-Turn off the light
     * @param enableLight4 入参名称：第四路指示灯开关状态
     *                     入参数据类型：boolean 布尔型
     *                     入参取值范围：true-开启指示灯，false-关闭指示灯
     *                     Parameter Name: 4th indicator light switch status
     *                     Parameter Type: boolean
     *                     Value Range: true-Turn on the light, false-Turn off the light
     * @return 返回值名称：接口执行结果状态码
     * 返回值数据类型：int 整型
     * 返回值示例：0-接口执行成功，非0-接口执行失败
     * Return Name: Interface execution result status code
     * Return Type: int
     * Return Example: 0-Execution success, Non-zero-Execution failure
     * @explain 出参说明：该接口无自定义出参，仅通过返回值反馈执行结果
     * Output Parameter: This interface has no custom output parameters, only return value for result feedback
     */
    private void setRFlight(String itemName, boolean enable1, boolean enable2, boolean enable3, boolean enable4) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setLightStatus(enable1, enable2, enable3, enable4);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setMtkOneRFFlight(String itemName) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatus(1);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setMtkTwoRFFlight(String itemName) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatus(2);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setMtkThreeRFFlight(String itemName) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatus(3);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void setMtkOneRFFlightClose(String itemName) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatusClosed(1);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setMtkTwoRFFlightClose(String itemName) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatusClosed(2);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setMtkThreeRFFlightClose(String itemName) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatusClosed(3);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setKeyboardLightTest(String itemName) {
        appendText(itemName);
        if (mSystemBinderService == null) {
            appendErrorText(itemName, "System service is null");
            return;
        }

        int status = mKeyboardLightOn ? 0 : 1;
        String statusText = status == 1 ? "开/ON" : "关/OFF";
        try {
            int ret = mSystemBinderService.setKeyboardLight(status);
            if (ret == 0) {
                mKeyboardLightOn = status == 1;
            }
            int nextStatus = mKeyboardLightOn ? 0 : 1;
            String nextStatusText = nextStatus == 1 ? "开/ON" : "关/OFF";
            appendText(itemName + "\n操作/Action: " + statusText
                    + "\n再次点击/Next tap: " + nextStatusText
                    + "\n结果码/Code: " + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void firmwareUpdateTest(String itemName) {
        appendText(itemName);
        try {
            mSystemBinderService.firmwareUpdate("/sdcard/firmwareTest.bin", new IFirmwareUpdateCallback.Stub() {
                @Override
                public void onDownloading(int progress, int total) throws RemoteException {
                    appendText(getResources().getString(R.string.firmware_loading) + ", progress: " + total + "  total: " + total);
                }

                @Override
                public void onUpdateStart() throws RemoteException {
                    appendText(getResources().getString(R.string.firmware_update) + "...\n");
                }

                @Override
                public void onUpdateFailed(int errorCode) throws RemoteException {
                    appendText(itemName + getResources().getString(R.string.failed_result_code) + errorCode + "\n");
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * SP(3121、3122)模块复位接口（冷启动，下电->重新上电处理）；
     * SP (3121, 3122) module reset interface (cold start, power-down -> power-on processing);
     *
     * @param 无入参 No input parameters
     * @return 返回值名称：接口执行结果状态码
     * 返回值数据类型：int 整型
     * 返回值示例：0-复位执行成功，非0-复位执行失败
     * Return Name: Interface execution result status code
     * Return Type: int
     * Return Example: 0-Reset success, Non-zero-Reset failure (module not ready / communication error / hardware error)
     */

    private void resetSP(String itemName) {
        appendText(itemName);
        try {
            if (null != mSystemBinderService) {
                int ret = mSystemBinderService.resetSP();
                appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private boolean dukptSetKeyResult;

    /**
     * 设置秘钥数据
     *
     * @param itemName
     */
    private void dukptSetKey(String itemName, int keyType, int keyIndex, String keyValue, String ksn) {
        appendText(itemName);
        try {
            switch (keyType) {
                case 0:
                    //KEY_TYPE_DUKPT_KEY_BDK
                    dukptSetKeyResult = mSystemBinderService.dukptSetBDKey(keyIndex, keyValue, ksn) == 0;
                    break;
                case 1:
                    //KEY_TYPE_DUKPT_KEY_IPEK
                    dukptSetKeyResult = mSystemBinderService.dukptSetIPEKey(keyIndex, keyValue, ksn) == 0;
                    break;
            }
            Log.e(TAG, "dukptSetKeyResult:" + dukptSetKeyResult);
            appendText(itemName + (dukptSetKeyResult ? " success" : " failure") + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * 3des 加密
     *
     * @param itemName
     */
    private void dukptSessionKeyEnc(String itemName, int keyIndex) {
        appendText(itemName);
        try {
            String[] outData = new String[1];
            int dukptENCDataResult = mSystemBinderService.dukptEncDec(keyIndex, 4, "6074841230000000021D261262036700", outData, "0000000000000000", 0x81, 0);
            Log.e(TAG, "dukptENCDataResult:" + (dukptENCDataResult == 0 ? outData[0] : "fail"));
            appendText(itemName + "dukptENCDataResult:" + (dukptENCDataResult == 0 ? outData[0] : "fail") + "\n");

            if (dukptENCDataResult == 0) {
                dukptSessionKeyDec(itemName, keyIndex, outData[0]);
            }


        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * 3des解密
     *
     * @param itemName #define TDES_CBC_ENC   0x81
     *                 #define TDES_CBC_DEC   0x80
     */
    private void dukptSessionKeyDec(String itemName, int keyIndex, String ciphertext) {
        appendText(itemName);
        try {
            String[] outData = new String[1];
            int dukptDECDataResult = mSystemBinderService.dukptEncDec(keyIndex, 4, ciphertext, outData, "0000000000000000", 0x80, 0);
            Log.e(TAG, "dukptDECDataResult:" + (dukptDECDataResult == 0 ? outData[0] : "fail"));
            appendText(itemName + "dukptDECDataResult:" + (dukptDECDataResult == 0 ? outData[0] : "fail") + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void dukptGetKSN(String itemName, int keyIndex) {
        appendText(itemName);
        try {
            Log.e(TAG, "getSN: 获取明文KSN数据");
            String ksn = mSystemBinderService.dukptGetCurrentKSN(keyIndex);
            Log.e(TAG, "getKSN:" + ksn);
            appendText(itemName + getResources().getString(R.string.session_ksn) + ksn + "\n");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void dukptIncreasesKSN(String itemName, int keyIndex) {
        appendText(itemName);
        try {
            Log.e(TAG, "dukptIncreasesKSN");
            boolean dukptIncreasesKSNResult = mSystemBinderService.dukptIncreasesKSN(keyIndex);
            Log.e(TAG, "dukptIncreasesKSNResult:" + dukptIncreasesKSNResult);
            appendText(itemName + (dukptIncreasesKSNResult ? "success" : "failure") + "\n");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void dukptKeyExist(String itemName, int keyIndex) {
        appendText(itemName);
        try {
            Log.e(TAG, "【dukptKeyExist】");
            boolean dukptKeyExistResult = mSystemBinderService.dukptKeyExist(keyIndex);
            Log.e(TAG, "dukptKeyExistResult:" + dukptKeyExistResult);
            appendText(itemName + (dukptKeyExistResult ? "success" : "failure") + "\n");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * 获取设备硬件SN序列号
     * Obtain the hardware serial number of the device
     * <p>
     * 入参：无
     * Input parameters: None
     * <p>
     * 出参：无
     * Output parameters: None
     * <p>
     * 返回值名：sn
     * 返回值数据类型：String
     * 返回值示例："7069900001433"
     * Return name: sn
     * Return data type: String
     * Return example: "7069900001433"
     */
    private void getSN(String itemName) {
        appendText(itemName);
        try {
            String sn = mSystemBinderService.getSN();
            appendText(itemName + getResources().getString(R.string.end_sn) + sn + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * 【中文】获取设备唯一TUSN序列号
     * 【English】Get the unique TUSN serial number of the device
     * <p>
     * 【中文】入参说明：本接口无输入参数
     * 【English】Input parameter description: This interface has no input parameters
     * <p>
     * 【中文】出参说明：无出参
     * 【English】Output parameter description: No output parameters
     * <p>
     * 【中文】返回值：设备TUSN编号，字符串类型；返回值示例："00005002706990001433"
     * 【English】Return value: Device TUSN number, String type; Return value example: "00005002706990001433"
     * <p>
     * 【中文】业务说明：用于读取硬件固化的唯一设备序列号，设备上电后可稳定读取
     * 【English】Business description: Used to read the unique device serial number that is hard-coded in the hardware. This serial number can be reliably read after the device is powered on.
     */
    private void getTUSN(String itemName) {
        appendText(itemName);
        try {
            String tusn = mSystemBinderService.getTUSN();
            appendText(itemName + getResources().getString(R.string.end_tusn) + tusn + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setSN(String itemName, String sn) {
        appendText(itemName);
        try {
//            String sn = "SN000099";
            int ret = mSystemBinderService.setSN(sn);
            appendText(itemName + ": " + sn + getResources().getString(R.string.result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setTUSN(String itemName, String tusn) {
        appendText(itemName);
        try {
//            String tusn = "TUSN0099";
            int ret = mSystemBinderService.setTUSN(tusn);
            appendText(itemName + ": " + tusn + getResources().getString(R.string.result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

//    private void setDateTime(String itemName,int year,int month,int day,int hour,int minute) {
//        appendText(itemName);
//        try {
//            int ret = mSystemBinderService.setOsDateTime(year, month, day, hour, minute);
//            appendText(itemName + ": " + ret + "\n");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//            appendErrorText(itemName, e.getMessage());
//        }
//    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
            case POSITION_READ_SERIAL_VERSION:
                readSerialLibVersion(itemName);
                //spPowerUp(itemName);
                break;
            case POSITION_READ_SP_VERSION:
                readSpVersion(itemName);
                //spPowerDown(itemName);
                break;
            case POSITION_BEEP_TEST:
                beepTest(itemName);
                break;
            case POSITION_OPEN_RF_LIGHT:
                setRFlight(itemName, true, false, true, false);
                break;
            case POSITION_CLOSE_RF_LIGHT:
                setRFlight(itemName, false, false, false, false);
                break;

            case POSITION_RESET_SP:
                resetSP(itemName);
                break;
            case POSITION_GET_SN:
                getSN(itemName);
                break;
            case POSITION_GET_TUSN:
                getTUSN(itemName);
                break;
            case POSITION_SET_HAL_READUSERKEYNUM:
                halReadUserKeyNum(itemName);
                break;
            case POSITION_SET_HAL_READUSERKEYBYID:
                halReadUserKeyById(itemName);
                break;
            case POSITION_POWER_ON_APP:
                Log.i("szaf", "POSITION_POWER_ON_APP: xxx");
                powerOnApp(itemName);
                break;
            case POSITION_POWER_Down_APP:
                Log.i("szaf", "POSITION_POWER_Down_APP: xxx");
                powerDownApp(itemName);
                break;
            case POSITION_GETPCBINFO:
                getPcbInfo(itemName);
                break;
            case POSITION_BUTTON_CELL_TEST:

                getCellTest(itemName);
                break;
            case POSITION_GETIMEI_TEST:
 // Obtain the IMEI IMEI
                getIMEI(itemName);
                break;
            case POSITION_SETOSDATETIME:

                showDate(itemName);
                break;
            case POSITION_ENCDEC:
                dalRsa(itemName);
                break;
         /*   public static final int DUKPT_SET_BDKEY_INDEX25 = 17;
            public static final int DUKPT_GET_BDKEY_INDEX25_KSN = 18;
            public static final int DUKPT_INCREMENT_BDKEY_KSN = 19;
            public static final int DUKPT_INDEX25_TDES_CBC_ENCRYPT = 20;
            public static final int DUKPT_INDEX25_TDES_CBC_DECRYPT = 21;
            public static final int DUKPT_CHECK_BDKEY_INDEX25_IMPORTED = 22;

            public static final int DUKPT_SET_IPEK_INDEX26 = 24;
            public static final int DUKPT_GET_IPEK_INDEX26_KSN = 25;
            public static final int DUKPT_INCREMENT_IPEK_KSN = 26;
            public static final int DUKPT_INDEX26_TDES_CBC_ENCRYPT = 27;
            public static final int DUKPT_INDEX26_TDES_CBC_DECRYPT = 28;
            public static final int DUKPT_CHECK_IPEK_INDEX26_IMPORTED = 29;*/
            case DUKPT_GET_BDKEY_INDEX25_KSN:
                dukptGetKSN(itemName, DUKPT_DBEY_INDEX_25);
                break;
            case DUKPT_GET_IPEK_INDEX26_KSN:
                dukptGetKSN(itemName, DUKPT_IPEY_INDEX_26);
                break;
            case DUKPT_SET_BDKEY_INDEX25:
                dukptSetKey(itemName, KEY_TYPE_DUKPT_KEY_BDK, DUKPT_DBEY_INDEX_25, "0123456789ABCDEFFEDCBA9876543210", "FFFF9876543210E00008");
                break;
            case DUKPT_SET_IPEK_INDEX26:
                /*
                IPEK key : 6AC292FAA1315B4D858AB3A3D7D5933A
                KSN : FFFF9876543210E00008
                Plain Track data : 6074841230000000021D261262036700
                ENC data : 5B490B78600FCBD8E9C6EE75C669B663
                */
                dukptSetKey(itemName, KEY_TYPE_DUKPT_KEY_IPEK, DUKPT_IPEY_INDEX_26, "6AC292FAA1315B4D858AB3A3D7D5933A", "FFFF9876543210E00008");
                break;
            case DUKPT_INCREMENT_BDKEY_KSN:
                dukptIncreasesKSN(itemName, DUKPT_DBEY_INDEX_25);
                break;
            case DUKPT_INCREMENT_IPEK_KSN:
                dukptIncreasesKSN(itemName, DUKPT_IPEY_INDEX_26);
                break;
            case DUKPT_INDEX25_TDES_CBC_ENCRYPT:
                dukptSessionKeyEnc(itemName, DUKPT_DBEY_INDEX_25);
                break;
            case DUKPT_INDEX26_TDES_CBC_ENCRYPT:
                dukptSessionKeyEnc(itemName, DUKPT_IPEY_INDEX_26);
                break;
            case DUKPT_CHECK_BDKEY_INDEX25_IMPORTED:
                dukptKeyExist(itemName, DUKPT_DBEY_INDEX_25);
                break;
            case DUKPT_CHECK_IPEK_INDEX26_IMPORTED:
                dukptKeyExist(itemName, DUKPT_IPEY_INDEX_26);
                break;
            case DUKPT_DUKPTSETPLAINKSN:
                dukptSetKSN(itemName);
                break;
            case SET_LED_ALLSTATUS:
                setLedAllStatus("LED Test");
                break;
            case OPEN_SCAN_QR_CODE:
                scannerOpen(itemName);
                break;
            case Close_SCAN_QR_CODE:
                scannerClose(itemName);
                break;
            case RESTART_DEVICE:
                devReBoot(itemName);
                break;
         /*   case POSITION_FIRMWARE_UPDATE_TEST:
                firmwareUpdateTest(itemName);
                break;
            case POSITION_LIGHT_OPEN_ONE_TEST:
                setMtkOneRFFlight(itemName);
                break;
            case POSITION_LIGHT_OPEN_TWO_TEST:
                setMtkTwoRFFlight(itemName);
                break;
            case POSITION_LIGHT_OPEN_THREE_TEST:
                setMtkThreeRFFlight(itemName);
                break;
            case POSITION_LIGHT_CLOSE_ONE_TEST:
                setMtkOneRFFlightClose(itemName);
                break;
            case POSITION_LIGHT_CLOSE_TWO_TEST:
                setMtkTwoRFFlightClose(itemName);
                break;
            case POSITION_LIGHT_CLOSE_THREE_TEST:
                setMtkThreeRFFlightClose(itemName);
                break;*/

            case RESTART_DAL_USBOPEN_NO_BLOCKING:
 // 0: ( )
                int ret = apiDALUsbOpenNonBlockingMode(itemName);
                break;
            case RESTART_DAL_USBOPEN_BLOCKING:
 // 1: ( , read
                int retBlock = apiDALUsbOpenBlockingMode(itemName);
                break;

            case RESTART_TEST_45:

                break;

            case RESTART_WRITE_SDCARD:
                // Example 1: Pure file name
 // 1: ( )
                String fileName1 = "test.txt";
                String content1 = "Hello Android 13 SDCard!\nWrite Time: 2026-03-29\n你好，Android13 SD卡！\n写入时间：2026-03-29";
                boolean writeResult1 = SdCardFileUtils.writeFileToSdCard(this, fileName1, content1);
                appendInfoText("Example 1 Write Result: " + writeResult1 + " / Example writing result ：" + writeResult1);

                // Example 2: File name with multi-level directories (auto create)
 // 2: ( )
                String fileName2 = "2026/03/android_test.txt";
                String content2 = "This is a file with directory\n这是带目录的文件";
                boolean writeResult2 = SdCardFileUtils.writeFileToSdCard(this, fileName2, content2);
                appendInfoText("Example 2 Write Result: " + writeResult2 + " / Example writing result：" + writeResult2);

                // ====================== Read File Examples ======================
 // ====================== ======================

                // Read file 1

                String readContent1 = SdCardFileUtils.readFileFromSdCard(this, fileName1);
                if (readContent1 != null) {
                    appendInfoText("File 1 Content:\n" + readContent1 + " ：\n" + readContent1);
                }

                // Read file 2

                String readContent2 = SdCardFileUtils.readFileFromSdCard(this, fileName2);
                if (readContent2 != null) {
                    appendInfoText("File 2 Content:\n" + readContent2 + " ：\n" + readContent2);
                }
                break;
            case KEYBOARD_NUMBER_ONKEYLISTENER:
                showNumberInputDialogWithKeyListener();
                break;
            case POSITION_KEYBOARD_LIGHT_TEST:
                setKeyboardLightTest(itemName);
                break;
            case OPEN_SYSTEM_SETTINGS:
                openSystemSettings();
                break;
            case POSITION_READ_DEVICE_CONFIG_INFO:
                readDeviceConfigInfo(itemName);
                break;
            case POSITION_GET_KEY_MAP:
                getKeyMap(itemName);
                break;
            case POSITION_REMOVE_KEY:
                showRemoveKeyDialog(itemName);
                break;
            case POSITION_ENTER_PAN:
                showEnterPanDialog(itemName);
                break;
            case POSITION_DUKPT_GET_CARD_DATA:
                showDukptGetCardDataDialog(itemName);
                break;
            case POSITION_CLEAR_TEMPORARY_CARD_DATA:
                clearTemporaryCardData(itemName);
                break;
        }
    }

    private void showEnterPanDialog(String itemName) {
        LinearLayout content = createDialogContent();

        CheckBox needExpiredDateInput = new CheckBox(this);
        needExpiredDateInput.setText(R.string.system_enter_pan_need_expired_date);
        needExpiredDateInput.setChecked(true);
        content.addView(needExpiredDateInput);

        CheckBox randomKeyboardInput = new CheckBox(this);
        randomKeyboardInput.setText(R.string.system_enter_pan_random_keyboard);
        randomKeyboardInput.setChecked(true);
        content.addView(randomKeyboardInput);

        CheckBox checkPanChecksumInput = new CheckBox(this);
        checkPanChecksumInput.setText(R.string.system_enter_pan_check_checksum);
        checkPanChecksumInput.setChecked(true);
        content.addView(checkPanChecksumInput);

        EditText minPanLengthInput = addDialogInput(
                content, R.string.system_enter_pan_min_length, "12", true);
        EditText maxPanLengthInput = addDialogInput(
                content, R.string.system_enter_pan_max_length, "19", true);
        EditText timeoutInput = addDialogInput(
                content, R.string.system_enter_pan_timeout, "60", true);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.system_enter_pan_title)
                .setView(content)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(unused ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            Integer minPanLength = parseRangedInteger(
                                    minPanLengthInput, 12, 19);
                            Integer maxPanLength = parseRangedInteger(
                                    maxPanLengthInput, 12, 19);
                            Integer timeoutSeconds = parseRangedInteger(
                                    timeoutInput, 5, 180);
                            if (minPanLength == null
                                    || maxPanLength == null
                                    || timeoutSeconds == null) {
                                return;
                            }
                            if (minPanLength > maxPanLength) {
                                minPanLengthInput.setError(
                                        getString(R.string.system_enter_pan_min_greater_than_max));
                                return;
                            }

                            enterPan(
                                    itemName,
                                    needExpiredDateInput.isChecked(),
                                    minPanLength,
                                    maxPanLength,
                                    timeoutSeconds,
                                    randomKeyboardInput.isChecked(),
                                    checkPanChecksumInput.isChecked());
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    private void enterPan(String itemName, boolean needExpiredDate, int minPanLength,
                          int maxPanLength, int timeoutSeconds, boolean randomKeyboard,
                          boolean checkPanChecksum) {
        appendText(itemName);
        appendText(getString(
                R.string.system_enter_pan_request,
                needExpiredDate,
                minPanLength,
                maxPanLength,
                timeoutSeconds,
                randomKeyboard,
                checkPanChecksum));

        ISystemBinderService service = mSystemBinderService;
        if (service == null) {
            appendErrorText(itemName, getString(R.string.system_key_service_not_connected));
            return;
        }

        new Thread(() -> {
            try {
                int result = service.enterPAN(
                        needExpiredDate,
                        minPanLength,
                        maxPanLength,
                        timeoutSeconds,
                        randomKeyboard,
                        checkPanChecksum);
                runOnUiThread(() -> appendText(getString(
                        R.string.system_enter_pan_result,
                        result,
                        getEnterPanResultDescription(result))));
            } catch (Exception e) {
                Log.e(TAG, "enterPAN failed", e);
                runOnUiThread(() -> appendErrorText(itemName, getErrorMessage(e)));
            }
        }, "enter-pan-test").start();
    }

    private String getEnterPanResultDescription(int result) {
        switch (result) {
            case 0:
                return getString(R.string.system_enter_pan_success);
            case 1:
                return getString(R.string.system_enter_pan_timed_out);
            case 2:
                return getString(R.string.system_enter_pan_cancelled);
            default:
                return getString(result < 0
                        ? R.string.system_enter_pan_failed
                        : R.string.system_enter_pan_unknown);
        }
    }

    private void showDukptGetCardDataDialog(String itemName) {
        LinearLayout content = createDialogContent();

        String[] dataTypes = {
                getString(R.string.system_card_data_masked_pan),
                getString(R.string.system_card_data_expired_date)
        };
        final int[] dataTypeValues = {4, 5};
        Spinner dataTypeInput = addDialogSpinner(
                content, R.string.system_dukpt_card_data_type, dataTypes);

        LinearLayout maskedPanParams = createDialogContent();
        EditText maskedPanLeftDigitsInput = addDialogInput(
                maskedPanParams, R.string.system_dukpt_masked_left_digits, "6", true);
        EditText maskedPanRightDigitsInput = addDialogInput(
                maskedPanParams, R.string.system_dukpt_masked_right_digits, "4", true);
        content.addView(maskedPanParams);

        TextView plaintextNote = new TextView(this);
        plaintextNote.setText(R.string.system_dukpt_plaintext_note);
        content.addView(plaintextNote);

        Runnable updateVisibleFields = () -> {
            int dataType = dataTypeValues[dataTypeInput.getSelectedItemPosition()];
            maskedPanParams.setVisibility(dataType == 4 ? View.VISIBLE : View.GONE);
        };
        AdapterView.OnItemSelectedListener visibilityListener =
                new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int position, long id) {
                        updateVisibleFields.run();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                        updateVisibleFields.run();
                    }
                };
        dataTypeInput.setOnItemSelectedListener(visibilityListener);
        updateVisibleFields.run();

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(content);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.system_dukpt_card_data_title)
                .setView(scrollView)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        dialog.setOnShowListener(unused ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            int dataType = dataTypeValues[dataTypeInput.getSelectedItemPosition()];
                            int maskedPanLeftDigits = 6;
                            int maskedPanRightDigits = 4;

                            if (dataType == 4) {
                                Integer parsedLeftDigits =
                                        parseRangedInteger(maskedPanLeftDigitsInput, 0, 19);
                                Integer parsedRightDigits =
                                        parseRangedInteger(maskedPanRightDigitsInput, 0, 19);
                                if (parsedLeftDigits == null || parsedRightDigits == null) {
                                    return;
                                }
                                maskedPanLeftDigits = parsedLeftDigits;
                                maskedPanRightDigits = parsedRightDigits;
                            }

                            getPlaintextCardData(
                                    itemName,
                                    dataType,
                                    maskedPanLeftDigits,
                                    maskedPanRightDigits);
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    private void getPlaintextCardData(String itemName, int dataType,
                                      int maskedPanLeftDigits, int maskedPanRightDigits) {
        appendText(itemName);
        appendText(getString(
                R.string.system_dukpt_card_data_request,
                dataType,
                maskedPanLeftDigits,
                maskedPanRightDigits));

        ISystemBinderService service = mSystemBinderService;
        if (service == null) {
            appendErrorText(itemName, getString(R.string.system_key_service_not_connected));
            return;
        }

        new Thread(() -> {
            try {
                String[] outData = new String[1];
                int result = service.getPlaintextCardData(
                        dataType,
                        outData,
                        maskedPanLeftDigits,
                        maskedPanRightDigits);
                runOnUiThread(() -> showDukptCardDataResult(result, dataType, outData));
            } catch (Exception e) {
                Log.e(TAG, "getPlaintextCardData failed", e);
                runOnUiThread(() -> appendErrorText(itemName, getErrorMessage(e)));
            }
        }, "plaintext-card-data-test").start();
    }

    private void clearTemporaryCardData(String itemName) {
        appendText(itemName);

        ISystemBinderService service = mSystemBinderService;
        if (service == null) {
            appendErrorText(itemName, getString(R.string.system_key_service_not_connected));
            return;
        }

        new Thread(() -> {
            try {
                int result = service.clearTemporaryCardData();
                runOnUiThread(() -> appendText(
                        getString(R.string.system_clear_temp_card_data_result, result)));
            } catch (Exception e) {
                Log.e(TAG, "clearTemporaryCardData failed", e);
                runOnUiThread(() -> appendErrorText(itemName, getErrorMessage(e)));
            }
        }, "clear-temporary-card-data-test").start();
    }

    private void showDukptCardDataResult(int result, int dataType, String[] outData) {
        appendText(getString(R.string.system_dukpt_card_data_result, result));
        if (result < 0) {
            return;
        }

        String firstValue = outData.length > 0 && outData[0] != null ? outData[0] : "null";
        switch (dataType) {
            case 4:
                appendText(getString(R.string.system_dukpt_masked_pan, firstValue));
                break;
            case 5:
                appendText(getString(R.string.system_dukpt_expired_date, firstValue));
                break;
            default:
                break;
        }
    }

    private LinearLayout createDialogContent() {
        LinearLayout content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        content.setPadding(padding, padding / 2, padding, padding / 2);
        return content;
    }

    private Spinner addDialogSpinner(LinearLayout parent, int labelResId, String[] entries) {
        TextView label = new TextView(this);
        label.setText(labelResId);
        parent.addView(label);

        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, entries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        parent.addView(spinner);
        return spinner;
    }

    private EditText addDialogInput(LinearLayout parent, int labelResId,
                                    String defaultValue, boolean numeric) {
        TextView label = new TextView(this);
        label.setText(labelResId);
        parent.addView(label);

        EditText input = new EditText(this);
        input.setSingleLine(true);
        input.setText(defaultValue);
        if (numeric) {
            input.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_SIGNED);
        }
        parent.addView(input);
        return input;
    }

    private Integer parseRangedInteger(EditText input, int minValue, int maxValue) {
        Integer value = parseInteger(input);
        if (value != null && (value < minValue || value > maxValue)) {
            input.setError(getString(
                    R.string.system_value_range_error, minValue, maxValue));
            return null;
        }
        return value;
    }

    private Integer parseInteger(EditText input) {
        try {
            return Integer.parseInt(input.getText().toString().trim());
        } catch (NumberFormatException e) {
            input.setError(getString(R.string.system_invalid_integer));
            return null;
        }
    }

    private boolean isValidHexIv(String iv) {
        return iv != null && iv.matches("(?i)[0-9a-f]{16}");
    }

    private String formatHexMode(int encMode) {
        return "0x" + Integer.toHexString(encMode).toUpperCase();
    }

    private void getKeyMap(String itemName) {
        appendText(itemName);
        if (mSystemBinderService == null) {
            appendErrorText(itemName, getString(R.string.system_key_service_not_connected));
            return;
        }

        try {
            String keyMap = mSystemBinderService.getKeyMap();
            appendText(getString(R.string.system_key_map_result,
                    keyMap == null ? "null" : keyMap));
        } catch (Exception e) {
            Log.e(TAG, "getKeyMap failed", e);
            appendErrorText(itemName, getErrorMessage(e));
        }
    }

    private void showRemoveKeyDialog(String itemName) {
        EditText keyIndexInput = new EditText(this);
        keyIndexInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        keyIndexInput.setHint(R.string.system_remove_key_hint);
        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle(R.string.system_remove_key_title)
                        .setMessage(R.string.system_remove_key_message)
                        .setView(keyIndexInput)
                        .setPositiveButton(R.string.system_remove_key_action, null)
                        .setNegativeButton(android.R.string.cancel, null)
                        .create();
        dialog.setOnShowListener(unused ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                        .setOnClickListener(view -> {
                            String input = keyIndexInput.getText().toString().trim();
                            int keyIndex;
                            try {
                                keyIndex = Integer.parseInt(input);
                            } catch (NumberFormatException e) {
                                keyIndexInput.setError(
                                        getString(R.string.system_remove_key_invalid_integer));
                                return;
                            }

                            if (keyIndex < 2 || keyIndex > 30) {
                                keyIndexInput.setError(
                                        getString(R.string.system_remove_key_invalid_range));
                                return;
                            }

                            removeKey(itemName, keyIndex);
                            dialog.dismiss();
                        }));
        dialog.show();
    }

    private void removeKey(String itemName, int keyIndex) {
        appendText(itemName);
        appendText(getString(R.string.system_remove_key_request, keyIndex));
        if (mSystemBinderService == null) {
            appendErrorText(itemName, getString(R.string.system_key_service_not_connected));
            return;
        }

        try {
            int result = mSystemBinderService.removeKey(keyIndex);
            appendText(getString(R.string.system_remove_key_result, result));
        } catch (Exception e) {
            Log.e(TAG, "removeKey failed, keyIndex=" + keyIndex, e);
            appendErrorText(itemName, getErrorMessage(e));
        }
    }

    private String getErrorMessage(Exception error) {
        if (error.getMessage() == null) {
            return error.getClass().getSimpleName();
        }
        return error.getMessage();
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


    /**
     * 数字输入弹框（使用 OnKeyListener 监听按键输入）
     * 点击弹框周边自动关闭
     */
    private void showNumberInputDialogWithKeyListener() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_number_input, null);
        EditText etNumber = dialogView.findViewById(R.id.et_number);
        TextView tvRealTime = dialogView.findViewById(R.id.tv_real_time);


        etNumber.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

 // ========== : OnKeyListener ==========
        etNumber.setOnKeyListener((v, keyCode, event) -> {

            if (event.getAction() == android.view.KeyEvent.ACTION_DOWN) {

                String inputContent = etNumber.getText().toString();

                tvRealTime.setText("实时输入内容：" + inputContent);
            }
            return false;
        });


        androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("请输入数字内容")
                .setView(dialogView)
                .setPositiveButton("确定", (dialog1, which) -> {
                    String finalInput = etNumber.getText().toString().trim();
                    appendText("最终输入数字：" + finalInput);
                    dialog1.dismiss();
                })
                .setNegativeButton("取消", (dialog1, which) -> dialog1.dismiss())
                .create();


        dialog.setCanceledOnTouchOutside(true);
        dialog.setCancelable(true);

        dialog.show();
    }


    // ======================

// ======================
    private void startScrollAnimation() {
        if (isPlaying) return;


        ivAnimationView.setVisibility(View.VISIBLE);
        isPlaying = true;
        currentFrame = 0;

        animationRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isPlaying) {
                    ivAnimationView.setVisibility(View.GONE);
                    return;
                }

 // animation_effect00000 ~ 00049
                String name = "animation_effect" + String.format("%05d", currentFrame);
                int resId = getResources().getIdentifier(name, "drawable", getPackageName());

                if (resId != 0) {
                    ivAnimationView.setImageResource(resId);
                }

                currentFrame++;
                if (currentFrame >= totalFrames) {

                    stopScrollAnimation();
                    return;
                }

                animationHandler.postDelayed(this, delayMs);
            }
        };

        animationHandler.post(animationRunnable);
    }

    // ======================

// ======================
    private void stopScrollAnimation() {
        isPlaying = false;
        animationHandler.removeCallbacks(animationRunnable);
        ivAnimationView.setVisibility(View.GONE);
    }

    // ======================
 // 50 Bitmap
// ======================
    private android.graphics.Bitmap[] getFullAnimationBitmaps() {
        android.graphics.Bitmap[] bitmaps = new android.graphics.Bitmap[totalFrames];

        for (int i = 0; i < totalFrames; i++) {
            String name = "animation_effect" + String.format("%05d", i);
            int resId = getResources().getIdentifier(name, "drawable", getPackageName());
            if (resId != 0) {
                bitmaps[i] = android.graphics.BitmapFactory.decodeResource(getResources(), resId);
            }
        }
        return bitmaps;
    }

    /**
     * 【中文】3121/3122 模块上电接口
     * 【English】Power-up interface of module 3121/312
     * <p>
     * 【中文】入参说明：本接口无输入参数
     * 【English】Input parameter description: This interface has no input parameters
     * <p>
     * 【中文】出参说明：无输出参数
     * 【English】Output parameter description: No output parameters
     * <p>
     * 【中文】返回值：执行结果码，int 类型；返回值示例：0（成功）、！= 0（失败）
     * 【English】Return value: Execution result code, of type int; Return value example: 0 (success), ! = 0 (failure)
     * <p>
     * 【中文】业务说明：通过串口透传通道执行应用上电/启动操作，用于控制设备应用程序启动
     * 【English】Business description: Execute application power-on / startup  serial transparent transmission channel, used to control device application startup
     */

    private void powerOnApp(String itemName) {
        appendText(itemName);
        try {
            Log.i("szaf", "powerOnApp: 111");
            int ret = mSystemBinderService.powerOnApp();
            Log.i("szaf", "powerOnApp: 222");
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * 【中文】3121 、3122 模块下电接口请求操作；
     * 【English】Power-off interface request operation under modules 3121 and 3122;
     * <p>
     * 【中文】入参说明：本接口无输入参数
     * 【English】Input parameter description: This interface has no input parameters
     * <p>
     * 【中文】出参说明：无输出参数
     * 【English】Output parameter description: No output parameters
     * <p>
     * 【中文】返回值：执行结果码，int 类型；返回值示例：0（成功）、非零（失败）
     * 【English】Return value: Execution result code, int type; Return value example: 0 (success), 非零 (failure)
     * <p>
     * 【中文】业务说明：通过接口请求操作3121/3122 下电操作;
     * 【English】Business Description: Request the power-off operation for 3121/3122 through the interface.
     */
    private void powerDownApp(String itemName) {
        appendText(itemName);
        try {
            Log.i("szaf", "powerDownApp: 111");
            int ret = mSystemBinderService.powerDownApp();
            Log.i("szaf", "powerDownApp: 222");
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void initSetSDialog(String itemName) {
        final CommonSetSNDialog dialog = new CommonSetSNDialog(SystemServiceActivity.this);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setMessage("Please enter the SN to be set")
                .setImageResId(R.mipmap.ic_launcher)
 // setTitle(" ")
                .setSingle(true).setOnClickBottomListener(new CommonSetSNDialog.OnClickBottomListener() {


                    @Override
                    public void onPositiveClick(String strsn) {
                        setSN(itemName, strsn);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void initSetTUSNDialog(String itemName) {
        final CommonSetTUSNDialog dialog = new CommonSetTUSNDialog(SystemServiceActivity.this);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setMessage("Please enter the SN to be set")
                .setImageResId(R.mipmap.ic_launcher)
 // setTitle(" ")
                .setSingle(true).setOnClickBottomListener(new CommonSetTUSNDialog.OnClickBottomListener() {


                    @Override
                    public void onPositiveClick(String strsn) {
                        setTUSN(itemName, strsn);
                        dialog.dismiss();
                    }

                    @Override
                    public void onNegtiveClick() {
                        dialog.dismiss();
                    }
                }).show();
    }

    private void halReadUserKeyNum(String itemName) {
        appendText(itemName);
        try {
            int ret = mISystemReadUserKeyBinderService.halReadUserKeyNum();
            appendText("halReadUserKeyNum:" + ret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void halReadUserKeyById(String itemName) {
        appendText(itemName);


        byte[] pubKey = new byte[256];
        int len = 256;
//        1  READ APP KEY  2  READ ROOT KEY
        try {
            HalReadUserKeyByIdResult halReadUserKeyByIdResult = mISystemReadUserKeyBinderService.halReadUserKeyById(1, pubKey, len);
            if (null != halReadUserKeyByIdResult) {

                halReadUserKeyByIdResult.getTagData();
                halReadUserKeyByIdResult.getRet();
                appendText("ret:" + halReadUserKeyByIdResult.getRet());
                appendText("halReadUserKeyById=1：" + HexUtil.bcd2str(halReadUserKeyByIdResult.getTagData()));
                LogUtils.e("ret:" + halReadUserKeyByIdResult.getRet());
                LogUtils.e("halReadUserKeyById=1：" + HexUtil.bcd2str(halReadUserKeyByIdResult.getTagData()));
                halReadUserKeyByIdResult = mISystemReadUserKeyBinderService.halReadUserKeyById(2, pubKey, len);
                halReadUserKeyByIdResult.getTagData();
                halReadUserKeyByIdResult.getRet();
                appendText("ret:" + halReadUserKeyByIdResult.getRet());
                appendText("halReadUserKeyById=2：" + HexUtil.bcd2str(halReadUserKeyByIdResult.getTagData()));
                LogUtils.e("ret:" + halReadUserKeyByIdResult.getRet());
                LogUtils.e("halReadUserKeyById=2：" + HexUtil.bcd2str(halReadUserKeyByIdResult.getTagData()));

            } else {
                Log.e("szaf", "halReadUserKeyByIdResult:If the result is empty, go to 【 System Service 】== 【 Read sp version 】 to check whether sp version is empty\n" +
                        "\n ");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setAttribute(String itemName) {
        appendText(itemName);
        try {
            GetAttributeResult getAttributeResult = mSystemBinderService.getAttribute(1);
            byte[] byteAtteibute = getAttributeResult.getValue();
            int ret = mSystemBinderService.setAttribute(1, byteAtteibute.length, byteAtteibute);
            appendText("ret:" + ret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void getAttribute(String itemName) {
        appendText(itemName);
        try {
            GetAttributeResult getAttributeResult = mSystemBinderService.getAttribute(1);
            int ret = getAttributeResult.getRet();
            byte[] byteAtteuvyteValue = getAttributeResult.getValue();
            appendText("ret" + ret);
            appendText("AtteuvyteValue:" + HexUtil.bcd2str(byteAtteuvyteValue));
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        iPrinterCountOrg = iPrinterCount;
        super.onDestroy();
 // Activity
//        stopUSBReadingThread();
        stopReadingThread();

 // Handler
        mainHandler.removeCallbacksAndMessages(null);
        Log.d(TAG, "Activity销毁，已清理线程");
        stopScrollAnimation();
        animationHandler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
 // onPause
        // stopUSBReadingThread();
    }

    @Override
    protected void onResume() {
        super.onResume();
 // onResume

    }


    private void showDate(String itemName) {
        TimePickerView pvTime = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onTimeSelect(Date date, View v) {
                String time = getTime(date);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime dateTime = LocalDateTime.parse(time, formatter);

                int year = dateTime.getYear();
                int month = dateTime.getMonthValue();
                int day = dateTime.getDayOfMonth();
                int hour = dateTime.getHour();
                int minute = dateTime.getMinute();
                int second = dateTime.getSecond();
                try {
                    int ret = mSystemBinderService.setOsDateTime(year, month, day, hour, minute);
                    appendText(itemName + ": " + ret + "\n");
                    appendText("Set OS Time:" + time);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendText("Set OS Time:" + "ERROR");
                    appendErrorText(itemName, e.getMessage());
                }
            }
        })
                .setType(new boolean[]{true, true, true, true, true, false})
                .setCancelText("取消")
                .setSubmitText("确定")
 // setContentSize(18)//
 // setTitleSize(20)//
 // //.setTitleText("Title")//
 // setOutSideCancelable(true)//
 // isCyclic(true)//
 // //.setTitleColor(Color.BLACK)//
 // setSubmitColor(Color.BLUE)//
 // setCancelColor(Color.BLUE)//
 // //.setTitleBgColor(0xFF666666)// Night mode
 // setBgColor(0xFF333333)// Night mode
 // // .setDate(selectedDate)// , */
 // // .setRangDate(startDate,endDate)//
 // //.setLabel(" "," "," "," "," "," ")//
                .isCenterLabel(false) // label , false item label
 // isDialog(true)//
                .build();
        pvTime.show();
    }

    private void dukptSetKSN(String itemName) {
        appendText(itemName);
        try {
            int setKSNResultRet = mSystemBinderService.dukptSetKSN(DUKPT_DBEY_INDEX_25, "2718E76CA82718E76CA8");
            Log.e(TAG, "setKSNResult:" + setKSNResultRet);
            appendText(itemName + "setKSNResult:->" + setKSNResultRet + "\n");

            String strKSNResultRet = mSystemBinderService.dukptGetCurrentKSN(DUKPT_DBEY_INDEX_25);
            appendText("dukptGetCurrentKSN :" + strKSNResultRet);
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void setLedAllStatus(final String itemName) {
        appendText(itemName);
        int[] ledStates = {
                LedState.OFF.getValue(), // LED
                LedState.RED_ON.getValue(),
                LedState.GREEN_ON.getValue(),
                LedState.YELLOW_ON.getValue(),
                LedState.BLUE_ON.getValue(),
                LedState.OFF.getValue(), // LED
                LedState.RED_BLINK.getValue(),
                LedState.GREEN_BLINK.getValue(),
                LedState.YELLOW_BLINK.getValue(),
                LedState.BLUE_BLINK.getValue(),
 // LedState.RED_ON.getValue() | LedState.GREEN_ON.getValue(), // +
 // LedState.RED_BLINK.getValue() | LedState.GREEN_BLINK.getValue(), // +
 // LedState.RED_ON.getValue() | LedState.BLUE_ON.getValue(), // +
 // LedState.RED_BLINK.getValue() | LedState.BLUE_BLINK.getValue(), // +
 // LedState.GREEN_ON.getValue() | LedState.BLUE_ON.getValue(), // +
 // LedState.GREEN_BLINK.getValue() | LedState.BLUE_BLINK.getValue(), // +
 // LedState.RED_ON.getValue() | LedState.GREEN_ON.getValue() | LedState.BLUE_ON.getValue(), // + +
 // LedState.RED_BLINK.getValue() | LedState.GREEN_BLINK.getValue() | LedState.BLUE_BLINK.getValue(), // + +
                LedState.OFF.getValue() // LED
        };

 // LED
        String[] ledStateDescriptions = {
                "关闭所有 LED",
                "红灯常亮",
                "绿灯常亮",
                "黄灯常亮",
                "蓝灯常亮",
                "关闭所有 LED",
                "红灯闪烁",
                "绿灯闪烁",
                "黄灯闪烁",
                "蓝灯闪烁",
 // " + "
 // " + "
 // " + "
 // " + "
 // " + "
 // " + "
 // " + + "
 // " + + "
                "关闭所有 LED"
        };

        boolean[] clearStates = {true};
        boolean[] powerOffSavingStates = {true};

        Handler handler = new Handler(Looper.getMainLooper());
        int delay = 0;

        for (int i = 0; i < ledStates.length; i++) {
            for (boolean isClearOriginalState : clearStates) {
                for (boolean isPowerOFFSaving : powerOffSavingStates) {
                    final int finalLedState = ledStates[i];
                    final String ledStateDescription = ledStateDescriptions[i];
                    final boolean finalIsClearOriginalState = isClearOriginalState;
                    final boolean finalIsPowerOFFSaving = isPowerOFFSaving;

                    handler.postDelayed(() -> {
                        try {
                            Log.e(TAG, String.format(
                                    "Testing LED: %s (0x%02X), ClearState: %b, PowerOffSave: %b",
                                    ledStateDescription, finalLedState, finalIsClearOriginalState, finalIsPowerOFFSaving
                            ));

                            appendText(String.format(
                                    "LED Test -> %s (ledState: 0x%02X), Clear: %b, Save: %b\n",
                                    ledStateDescription, finalLedState, finalIsClearOriginalState, finalIsPowerOFFSaving
                            ));

                            int setLedAllStatusResult = mSystemBinderService.setLedAllStatus(
                                    finalLedState, finalIsClearOriginalState, finalIsPowerOFFSaving
                            );

                            Log.e(TAG, "setLedAllStatusResult: " + setLedAllStatusResult);
                            appendText("Result: " + setLedAllStatusResult + "\n");

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            appendErrorText(itemName, e.getMessage());
                        }
                    }, delay);

                    delay += 3000;
                }
            }
        }
    }


    private String getTime(Date date) {
        //"yyyy-MM-dd HH:MM:SS"        "yyyy-MM-dd"
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return format.format(date);
    }

    private Bitmap createQrFullScreenBitmap(String url, int screenWidth, int screenHeight) throws Exception {
        int qrSize = Math.min(screenWidth, screenHeight) - 20;
        if (qrSize <= 0) return null;

        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);

        BitMatrix matrix = new QRCodeWriter().encode(
                url,
                BarcodeFormat.QR_CODE,
                qrSize,
                qrSize,
                hints
        );

        Bitmap qrBitmap = Bitmap.createBitmap(qrSize, qrSize, Bitmap.Config.RGB_565);
        for (int x = 0; x < qrSize; x++) {
            for (int y = 0; y < qrSize; y++) {
                qrBitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        Bitmap fullBitmap = Bitmap.createBitmap(screenWidth, screenHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(fullBitmap);
        canvas.drawColor(Color.WHITE);

        int left = (screenWidth - qrSize) / 2;
        int top = (screenHeight - qrSize) / 2;
        canvas.drawBitmap(qrBitmap, left, top, null);

        return fullBitmap;
    }

    /**
     * 将文字 生成 文字图片 的Bitmap, 打印的位图需要 黑字 白底(A Bitmap that generates text into text images. Printed bitmaps need black text on a white background)
     */
    public Bitmap creatTextBitmap(String text, int width, int height) {
        Bitmap newBitmap = createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(newBitmap, 0, 0, null);
        TextPaint textPaint = new TextPaint();
       /* if (isBold) {
            String familyName = "宋体";
            Typeface font = Typeface.create(familyName, Typeface.BOLD);
            textPaint.setTypeface(font);
        }*/
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);
 // " Android , Canvas.drawText , , .StaticLayout android , StaticLayout ";
        StaticLayout sl = new StaticLayout(text, textPaint, newBitmap.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        textPaint.setStyle(Paint.Style.FILL);
        canvas.translate(0, height / 10);
        sl.draw(canvas);
        return newBitmap;
    }

    private StringBuffer sbMsg = new StringBuffer();

    private String printMsg() {
        sbMsg.setLength(0);
        sbMsg.append("Welcome 980");
        LogUtils.i("printMsg: " + sbMsg.toString());
        return sbMsg.toString();
    }


    /**
     * 【中文】打开USB设备节点接口
     * 【English】Open USB device node interface
     * <p>
     * 【中文】入参名：blockedFlags
     * 【中文】入参数据类型：int 整型
     * 【中文】入参取值范围：0 - 非阻塞模式，1 - 阻塞模式
     * 【English】Input param name: blockedFlags
     * 【English】Input param type: int
     * 【English】Input param value range: 0 - Non-blocking mode, 1 - Blocking mode
     * <p>
     * 【中文】入参说明：设置USB读写的阻塞模式；1=阻塞模式（指定超时后read返回），0=非阻塞模式
     * 【English】Input param description: Set the blocking mode for USB read/write; 1=Blocking mode (read returns after specified timeout), 0=Non-blocking mode
     * <p>
     * 【中文】出参说明：无输出参数
     * 【English】Output parameter description: No output parameters
     * <p>
     * 【中文】返回值：USB设备文件描述符，int 类型
     * 【中文】返回值说明：非负整数=打开成功（用于后续USB读写），后续的读取和写入操作将通过该值进行。；-1=打开失败
     * 【中文】返回值示例：5（成功）、-1（失败）
     * 【English】Return value: USB device file descriptor, int type
     * 【English】Return value description: Non-negative integer = Success in opening (used for subsequent USB reading and writing), subsequent reading and writing operations will be carried out based on this value. ； -1 = Failure to open
     * 【English】Return value example: 5 (Success), -1 (Failed)
     * <p>
     * 【中文】业务说明：通过串口透传通道打开USB设备节点，获取文件描述符用于USB数据读写操作
     * 【English】Business description: Open USB device node via serial transparent transmission channel, obtain file descriptor for USB data read/write operations
     */
    private int apiDALUsbOpenBlockingMode(String itemName) {

        blockFlags = 1; // 1: 0
        appendInfoText(itemName);
        try {
 // step1: USB
            retDalUsbOpen = mSystemBinderService.apiDALUsbOpen(blockFlags);
            LogUtils.e("blocked_flags resultRet : " + retDalUsbOpen);
            if (retDalUsbOpen == -1) {
                appendErrorText("DALUsbOpen : ERROR ");
            } else {
                appendText("DALUsbNonOpen SUCCESS port : " + retDalUsbOpen);
 // step2: USB
                int baudrate = 115200;
                int bits = 8;
                char parity = 'n';
                int stop = 1;
                /**
                 * 【中文】调用AIDL配置USB串口参数
                 * 【English】Call AIDL to configure USB serial port parameters
                 *
                 * 【中文】入参 portFd：文件描述符，int，来自 apiDALUsbOpen 返回值
                 * 【中文】入参 baudrate：波特率，int {50,75,110,134,150,200,300,600,1200,1800,2400,4800,9600,19200,38400,57600,115200,
                 230400,460800,500000,576000,921600,1000000,1152000,1500000,2000000,2500000,3000000,3500000,4000000}
                 * 【中文】入参 bits：数据位，int {5, 6, 7, 8}
                 * 【中文】入参 parity：校验位，char 'n'/'N'：无校验;   'o'/'O'：奇校验; 'e','E'：偶校验
                 * 【中文】入参 stop：停止位，int 1：1个停止位  ,2：2个停止位;
                 * 【中文】入参 timeout_ms：超时时间，int 超时时间，注意只有在开启USB时设置为阻塞模式有效, 超时时间ms为单位，最小单位为100ms。
                 *
                 * ```
                 * 【English】Input parameter portFd: File descriptor, int type, returned by apiDALUsbOpen
                 * 【English】Input parameter baudrate: Baud rate, int type, optional values {50,75,110,134,150,200,300,600,1200,1800,2400,4800,9600,19200,38400,57600,115200,
                  230400,460800,500000,576000,921600,1000000,1152000,1500000,2000000,2500000,3000000,3500000,4000000}
                 * 【English】Input parameter bits: Data bits, int type, optional values {5, 6, 7, 8}
                 * 【English】Input parameter parity: Parity bit, char type; 'n'/'N': No parity; 'o'/'O': Odd parity; 'e'/'E': Even parity
                 * 【English】Input parameter stop: Stop bits, int type; 1: 1 stop bit, 2: 2 stop bits
                 * 【English】Input parameter timeout_ms: Timeout value, int type. This parameter only takes effect when USB is opened in blocking mode. The unit is millisecond, and the minimum value is 100ms.
                 *
                 * 【中文】返回值：0:成功，!= 0 失败；
                 * 【English】Return value:  0: Success, != 0: Failure
                 */
                apiDALUsbSetting(retDalUsbOpen, baudrate, bits, parity, stop, timeout_ms);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

 // 0: ( )
    private int apiDALUsbOpenNonBlockingMode(String itemName) {
        blockFlags = 0;
        appendInfoText(itemName);
        try {
            retDalUsbOpen = mSystemBinderService.apiDALUsbOpen(blockFlags);
            LogUtils.e("non blocked_flags resultRet : " + retDalUsbOpen);
            if (retDalUsbOpen == -1) {
                appendErrorText("DALUsbNonOpen : ERROR ");
            } else {
                appendText("DALUsbNonOpen SUCCESS port : " + retDalUsbOpen);
 // step2: USB
                int baudrate = 115200;
                int bits = 8;
                char parity = 'n';
                int stop = 1;
                apiDALUsbSetting(retDalUsbOpen, baudrate, bits, parity, stop, timeout_ms);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 【中文】关闭USB设备节点，释放文件描述符
     * 【English】interface to close USB device port and release file descriptor
     *
     * 【中文】入参 fd：USB文件描述符，int 类型；取值范围：apiDALUsbOpen 接口成功返回的非负整数
     * 【English】Input param fd: USB file descriptor, int type; Value range: Non-negative integer returned successfully by apiDALUsbOpen interface
     *
     * 【中文】出参：无输出参数
     * 【English】Output param: No output parameters
     *
     * 【中文】返回值：固定返回 0，执行结果通过日志与界面展示
     * 【English】Return value: Always return 0, execution result is displayed via log and UI
     *
     */
    private int apiDALUsbClose(String itemName, int fd) {
        appendInfoText(itemName);
        try {
            appendInfoText("apiDALUsbClose USB COM : " + retDalUsbOpen);
            int resultRet = mSystemBinderService.apiDALUsbClose(fd);
            LogUtils.e("apiDALUsbClose resultRet : " + resultRet);
            if (resultRet == 0) {
                appendText("apiDALUsbClose Success: ");
            } else {
                appendErrorText("apiDALUsbClose : ERROR ");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int apiDALUsbRead(String itemName) {
        appendInfoText(itemName);

        if (retDalUsbOpen != -1) {
            if (blockFlags == 1) { // 1: 0

                Thread threadReadBlock = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            /** apiDALUsbRead
                             * 【中文】USB串口数据读取入口（支持阻塞/非阻塞模式）
                             * 【English】USB Serial Port Read Entry (Supports Blocking/Non-blocking Mode)
                             *
                             * 【中文】内部关键变量：
                             * 【中文】retDalUsbOpen：USB文件描述符，int，来自 apiDALUsbOpen 返回值 入参；
                             * 【中文】bBuf：接收数据缓冲区，byte[]，大小256字节 出参；
                             * 【中文】count：最大读取长度，int（缓冲区大小） ，入参；
                             *
                             * 【English】Internal Key Variables:
                             * 【English】Internal Key Variables:
                             * 【English】retDalUsbOpen: USB file descriptor, int type, input parameter returned by apiDALUsbOpen;
                             * 【English】bBuf: Data receiving buffer, byte[] type, 256 bytes in size, output parameter;
                             * 【English】count: Maximum readable length, int type (buffer size), input parameter;
                             *
                             * 【中文】返回值：返回值 > 0：表示成功读取到的数据字节数。该值可能小于请求的 count
                             返回值 = 0：表示没有更多数据可读取。
                             返回值 = -1：表示读取失败
                             * 【English】Return value:
                             Return value > 0: Indicates the number of data bytes successfully read. This value may be less than the requested count.
                             Return value = 0: Indicates that there are no more data to be read.
                             Return value = -1: Indicates that the read operation failed.
                             */
                            int retDalUsbRead = -1;
                            byte[] outReadBuf = new byte[256];
                            LogUtils.e("retDalUsbOpen : " + retDalUsbOpen);
 // ( )
                            long timestamp1 = System.currentTimeMillis();
                            retDalUsbRead = mSystemBinderService.apiDALUsbRead(retDalUsbOpen, outReadBuf, 256);
 // ( )
                            long timestamp2 = System.currentTimeMillis();
                            long diffInSeconds2 = TimeUnit.MILLISECONDS.toSeconds(timestamp2 - timestamp1);
                            appendInfoText("使用TimeUnit时间差: " + diffInSeconds2 + " 秒");
                            LogUtils.e(" retDalUsbRead : " + retDalUsbRead);

                            LogUtils.e("读取到数据，提前退出循环" + retDalUsbRead);
                            if (retDalUsbRead == -1) {
                                appendErrorText("apiDALUsbRead Error : " + " : " + getResources().getString(R.string.apn_Indicates_read_failure_error));
                            } else if (retDalUsbRead == 0) {
                                appendErrorText("apiDALUsbRead Error : " + " : " + getResources().getString(R.string.apn_Indicates_read_no_more));
                            } else if (retDalUsbRead > 0) {
                                appendText("apiDALUsbRead resultRet : " + " : " + getResources().getString(R.string.apn_Indicates_read_success) + " : " + retDalUsbRead);
                                LogUtils.e("apiDALUsbRead resultRet : " + retDalUsbRead);
                                String resultBytesToAsciiString = AsciiUtils.bytesToAsciiString(outReadBuf);
                                appendText("resultBytesToAsciiString : " + resultBytesToAsciiString);
                            }
                            apiDALUsbClose("apiDALUsbClose ", retDalUsbOpen);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
                threadReadBlock.start();


            } else {
 // USB
                startReadingThread();
            }
        } else {
            ToastUtils.showToast(this, getResources().getString(R.string.apn_set_node_port_error), 1200);
            apiDALUsbClose("apiDALUsbClose ", retDalUsbOpen);
        }

        return 0;
    }

    private int apiDALUsbWrite(String itemName) {
        appendInfoText(itemName);
        try {
            if (retDalUsbOpen != -1) {
                byte[] bBuf = AFUtils.StringToByteArray("1234567890");

                /**
                 * 【中文】执行USB串口数据写入操作
                 * 【English】Execute USB serial port data write operation
                 *
                 * 【中文】内部变量说明：
                 * 【中文】retDalUsbOpen：int 类型，USB设备文件描述符，由 apiDALUsbOpen 接口返回
                 * 【中文】bBuf：byte[] 类型，待写入USB设备的数据缓冲区，由字符串转换得到
                 * 【中文】bBuf.length：int 类型，待写入数据的字节长度，作为 count 参数传入
                 *
                 * 【English】Internal Variable Description:
                 * 【English】retDalUsbOpen: int type, USB device file descriptor returned by apiDALUsbOpen
                 * 【English】bBuf: byte[] type, data buffer to be written to USB device, converted from string
                 * 【English】bBuf.length: int type, byte length of data to be written, passed as count parameter
                 *
                 * 【中文】返回值： ret > 0：实际写入的字字节数，
                 ret < count，表示剩余数据需要在循环中继续写入；
                 ret == -1：表示写入失败
                 * 【English】Return value:
                 ret > 0：The actual number of bytes written，
                 ret < count，The remaining data needs to be written in a loop.；
                 ret ==  -1：Indicates write failure
                 */
                int retDalUsbWrite = mSystemBinderService.apiDALUsbWrite(retDalUsbOpen, bBuf, bBuf.length);
                LogUtils.e("apiDALUsbWrite retDalUsbWrite : " + retDalUsbWrite);
                if (retDalUsbWrite > 0) {
                    appendText("apiDALUsbWrite is Success : ");
                    appendText("The actual number of bytes written : " + retDalUsbWrite);
                    apiDALUsbRead("apiDALUsbRead");
                } else {
                    appendErrorText("apiDALUsbWrite is error : " + retDalUsbWrite);
                    apiDALUsbClose("apiDALUsbClose ", retDalUsbOpen);
                }
            } else {
                ToastUtils.showToast(this, getResources().getString(R.string.apn_set_node_port_error), 1200);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 【中文】调用AIDL配置USB串口参数
     * 【English】Call AIDL to configure USB serial port parameters
     *
     * 【中文】入参 portFd：文件描述符，int，来自 apiDALUsbOpen 返回值
     * 【中文】入参 baudrate：波特率，int {50,75,110,134,150,200,300,600,1200,1800,2400,4800,9600,19200,38400,57600,115200,
     230400,460800,500000,576000,921600,1000000,1152000,1500000,2000000,2500000,3000000,3500000,4000000}
     * 【中文】入参 bits：数据位，int {5, 6, 7, 8}
     * 【中文】入参 parity：校验位，char 'n'/'N'：无校验;   'o'/'O'：奇校验; 'e','E'：偶校验
     * 【中文】入参 stop：停止位，int 1：1个停止位  ,2：2个停止位;
     * 【中文】入参 timeout_ms：超时时间，int 超时时间，注意只有在开启USB时设置为阻塞模式有效, 超时时间ms为单位，最小单位为100ms。
     *
     * ```
     * 【English】Input parameter portFd: File descriptor, int type, returned by apiDALUsbOpen
     * 【English】Input parameter baudrate: Baud rate, int type, optional values {50,75,110,134,150,200,300,600,1200,1800,2400,4800,9600,19200,38400,57600,115200,
     230400,460800,500000,576000,921600,1000000,1152000,1500000,2000000,2500000,3000000,3500000,4000000}
     * 【English】Input parameter bits: Data bits, int type, optional values {5, 6, 7, 8}
     * 【English】Input parameter parity: Parity bit, char type; 'n'/'N': No parity; 'o'/'O': Odd parity; 'e'/'E': Even parity
     * 【English】Input parameter stop: Stop bits, int type; 1: 1 stop bit, 2: 2 stop bits
     * 【English】Input parameter timeout_ms: Timeout value, int type. This parameter only takes effect when USB is opened in blocking mode. The unit is millisecond, and the minimum value is 100ms.
     *
     * 【中文】返回值：0:成功，!= 0 失败；
     * 【English】Return value:  0: Success, != 0: Failure
     */
    private int apiDALUsbSetting(int portFd, int baudrate, int bits, char parity, int stop, int timeout_ms) {
        appendInfoText("apiDALUsbSetting");
        try {
            int retDalUsbSetting = mSystemBinderService.apiDALUsbSetting(portFd, baudrate, bits, parity, stop, timeout_ms);
            LogUtils.e("apiDALUsbSetting  resultRet : " + retDalUsbSetting);
            appendText("apiDALUsbSetting  resultRet : " + retDalUsbSetting);
            if (retDalUsbSetting == 0) {
                ToastUtils.showToast(this, getResources().getString(R.string.apn_set_port_success), 1200);
                int retDalUsbWrite = apiDALUsbWrite(" apiDALUsbWrite");
            } else {
                appendErrorText(getResources().getString(R.string.apn_set_port_error));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return 0;
    }

//  ********************************************WD TOM *****************************************************

 // readUSBMSG
    private int readUSBMSG() {
        int retDalUsbRead = -1;
        byte[] bBuf = new byte[256];

        LogUtils.e("retDalUsbOpen : " + retDalUsbOpen);
        try {
            retDalUsbRead = mSystemBinderService.apiDALUsbRead(retDalUsbOpen, bBuf, 256);
            LogUtils.e(" retDalUsbRead : " + retDalUsbRead);

            LogUtils.e("retDalUsbRead : " + retDalUsbRead);
            if (retDalUsbRead == -1) {
//                appendErrorText("apiDALUsbRead Error : "+" : "+getResources().getString(R.string.apn_Indicates_read_failure_error));
            } else if (retDalUsbRead == 0) {
                appendErrorText("apiDALUsbRead Error : " + " : " + getResources().getString(R.string.apn_Indicates_read_no_more));
            } else if (retDalUsbRead > 0) {
                appendText("apiDALUsbRead resultRet : " + " : " + getResources().getString(R.string.apn_Indicates_read_success) + " : " + retDalUsbRead);
                LogUtils.e("apiDALUsbRead resultRet : " + retDalUsbRead);
                String resultBytesToAsciiString = AsciiUtils.bytesToAsciiString(bBuf);
                appendText("resultBytesToAsciiString : " + resultBytesToAsciiString);
//                apiDALUsbClose("apiDALUsbClose ", retDalUsbOpen);
                stopReadingThread();
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        return retDalUsbRead;
    }
//    *****************************************************************************************

    /**
     * 启动读取线程（线程安全，确保不会二次开启）
     */
    private void startReadingThread() {
        synchronized (lock) {
            if (isThreadRunning) {
                appendErrorText("The thread is already running. Do not start it again!");
                return;
            }
            shouldStop.set(false);
            isThreadRunning = true;
            attemptCount = 0;

            subThread = new Thread(new SystemServiceActivity.ReadingRunnable());
            subThread.start();
            appendText("The reading thread has been started");
        }
    }

    /**
     * 停止读取线程
     */
    private void stopReadingThread() {
        synchronized (lock) {
            if (!isThreadRunning) {
                appendText("There are no running threads");
                return;
            }
            shouldStop.set(true);
            // Wait for the thread to end
            new Thread(() -> {
                try {
                    if (subThread != null && subThread.isAlive()) {
                        subThread.join(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    mainHandler.post(() -> {
                        isThreadRunning = false;
                        appendText("The thread has stopped.");
                        apiDALUsbClose("apiDALUsbClose ", retDalUsbOpen);
                    });
                }
            }).start();
        }
    }

    /**
     * The Runnable implementation of the reading task 读取任务的Runnable实现
     */
    private class ReadingRunnable implements Runnable {
        @Override
        public void run() {

            final int MAX_ATTEMPTS = 20;
            boolean success = false;
            int lastReadValue = -1;

            for (attemptCount = 1; attemptCount <= MAX_ATTEMPTS; attemptCount++) {

                if (shouldStop.get()) {
                    LogUtils.e("The thread is interrupted." + "Interrupted at " + (attemptCount - 1) + "/20");
                    break;
                }

                final int currentCount = attemptCount;
                mainHandler.post(() ->
                        appendText("Execution: " + currentCount + "/20")
                );

                int ret = readUSBMSG();
                lastReadValue = ret;

                LogUtils.e("第 " + attemptCount + " 次读取: " + ret);

                if (ret > 0) {
                    success = true;

                    final int finalValue = ret;
                    mainHandler.post(() -> {
                        appendText("Read successfully！");
                        appendText("Result: " + finalValue);
                    });
                    break;
                }
                if (attemptCount == MAX_ATTEMPTS) {
                    apiDALUsbClose("apiDALUsbClose ", retDalUsbOpen);
                }
                try {
                    Thread.sleep(500); // A 500-millisecond interval 500
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
 // Thread termination processing
            synchronized (lock) {
                isThreadRunning = false;
            }
        }
    }

}
