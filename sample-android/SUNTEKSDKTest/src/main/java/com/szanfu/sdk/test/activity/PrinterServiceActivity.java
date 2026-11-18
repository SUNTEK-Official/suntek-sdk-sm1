package com.szanfu.sdk.test.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.error.ErrorCode;
import com.szanfu.sdk.api.printer.IBatteryResult;
import com.szanfu.sdk.api.printer.IPrinterBinderService;
import com.szanfu.sdk.api.printer.IPrinterCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.MyApplication;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.activity.sign.SignatureActivity;

import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.db.RealmDbOpenHelper;

import com.szanfu.sdk.test.util.BatteryUtils;
import com.szanfu.sdk.test.util.BitmapUtil;
import com.szanfu.sdk.test.util.ConstantData;
import com.szanfu.sdk.test.util.DataConversionUtils;
import com.szanfu.sdk.test.util.DateUtil;
import com.szanfu.sdk.test.util.DivBitmapUtils;
import com.szanfu.sdk.test.util.JBIGUtil;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.PrintUtil;
import com.szanfu.sdk.test.util.ToastUtils;

import static android.graphics.Bitmap.createBitmap;

import androidx.annotation.Nullable;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class PrinterServiceActivity extends ListButtonBaseActivity {
    private static final String TAG = "PrinterServiceActivity";
    private AlertDialog alertDialog;
    public static final int POSITION_PRINTER_TEST = 0;
    public static final int POSITION_PRINTER_TEXT_TEST = 1;
    public static final int POSITION_SIGNATURE = 2;
    public static final int POSITION_LOCAL_JBIG = 3;
    public static final int POSITION_PRINTER_BOLD = 4;
    public static final int POSITION_PRINTER_CHECKBATTERY = 5;
    public static final int POSITION_PRINTER_DALTHERMALPRINTSETDEPTH = 6;
    public static final int POSITION_PRINTER_DALTHERMALPRINTGETSTATUS = 7;
    public static final int POSITION_PRINTER_DALTHERMALPRINTPAPERWORKING = 8;
    public static final int POSITION_PRINTER_PRINT_DIV_LEFT_ALIGNED = 9;
    public static final int POSITION_PRINTER_PRINT_DIV_CENTERED = 10;
    public static final int POSITION_PRINTER_PRINT_DIV_RIGHT_ALIGNMENT = 11;
    public static final int POSITION_PRINTER_PRINT_DIVLEFT_ALIGNED_UNDERLINE = 12;
    public static final int POSITION_PRINTER_PRINT_DIV_LEFT_ALIGNED_BOLDED = 13;
    public static final int POSITION_PRINTER_PRINT_DIVLEFT_ALIGNED2XFONT = 14;
    public static final int POSITION_PRINTER_SRILANKALKA = 15;
    public static final int POSITION_PRINTER_SOMERS_4006 = 16;

    Bitmap headerImage;
    Bitmap blackWhitImage;
    Bitmap blackWhitImageLast;
    String deviceMSG = "";

    private static final int PRINT_WIDTH = 384;
    private static final int SOMERS_PRINT_HEIGHT = 788;
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");
    int[] printConfig=new int[11];
    private final int[] PRINT_CONFIG = {
            1, 0, 1, 0, 0, PRINT_WIDTH, 0, 0, 1, 1
    };
    /*
    printConfig[0] =1; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
    printConfig[1] =0; // int[1] =0 ; int[1] =1 ;
    printConfig[2] =1; // int[2]=1 :1 ;int[2] = 2:2
    printConfig[3] =0; //
    printConfig[4] =0; // int[4] =0; , int[4] =1: ;
    printConfig[5] =384;
    printConfig[6] =0; // LOGO
    printConfig[7] =1200;
    printConfig[8] =50;
    printConfig[9] =300;
    printConfig[10] =0;
     */

    public static final int SIGN_REQUEST = 1000;
    private ImageView mIVSign;
    private ImageView mJbigToImg;
    private String[] mButtons;
    private static final int MAX_BIT_WIDTH = 300;
    private static final int MAX_BIT_HEIGHT = 200;
    private static Bitmap imgBmp;
    StringBuffer sbMsg = new StringBuffer();
    private boolean isBold = false;

 // JBIG bitmap
    private Bitmap getJBIGToBitmap() {
        RealmDbOpenHelper dbOpenHelper = ((MyApplication) this.getApplicationContext()).getDbOpenHelper();
        List<byte[]> jbigItems = dbOpenHelper.getJbigs();
        if (jbigItems != null && jbigItems.size() > 0) {
            return JBIGUtil.jBigToBmp(jbigItems.get(0));
        }
        return null;
    }

    private IPrinterBinderService mPrinterBinderService;
    private ISystemBinderService mSystemBinderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.PRINT_MACHINE_SERVICE);
        mIVSign = findViewById(R.id.iv_sign);
        mJbigToImg = findViewById(R.id.iv_jbigToimg);
        setButtonDataSource(mButtons);
        String tempPlatform = DataConversionUtils.getCustomerType();
        if (ConstantData.PLATFORM_930_QUECTEL.equals(tempPlatform)) {
            deviceMSG="PLATFORM_930_QUECTEL";
        }else if (ConstantData.PLATFORM_930_MTK.equals(tempPlatform)) {
            deviceMSG="PLATFORM_930_MTK";
        }else if (ConstantData.PLATFORM_820_MTK.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_MTK";
        }else if (ConstantData.PLATFORM_930_QUECTEL.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_MTK";
        }else if (ConstantData.PLATFORM_930_MTK_A14.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_MTK";
        }else if (ConstantData.PLATFORM_820_GTH.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_MTK";
        }else if (ConstantData.PLATFORM_820_MTK_PINPAD.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_MTK";
        }else if (ConstantData.PLATFORM_930_CY.equals(tempPlatform)) {
            deviceMSG="PLATFORM_930_CY";
        }else if (ConstantData.PLATFORM_980_MTK.equals(tempPlatform)) {
            deviceMSG="PLATFORM_980_MTK";
        }else if (ConstantData.PLATFORM_820_CY.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_CY";
        }else if (ConstantData.PLATFORM_820_COB.equals(tempPlatform)) {
            deviceMSG="PLATFORM_820_COB";
        }else if (ConstantData.PLATFORM_980_MTK_64.equals(tempPlatform)) {
            deviceMSG="PLATFORM_980_MTK_64";
        }else if (ConstantData.PLATFORM_980_GMS.equals(tempPlatform)) {
            deviceMSG="PLATFORM_980_GMS";
        }else if (ConstantData.PLATFORM_980_CY.equals(tempPlatform)) {
            deviceMSG="PLATFORM_980_CY";
        }else if (ConstantData.PLATFORM_980_CY.equals(tempPlatform)) {
            deviceMSG="PLATFORM_980_CY";
        }
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mPrinterBinderService = IPrinterBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PRINTER));
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private String printMsg() {
        long currentDate = DateUtil.getTimeStamp(new Date(), 0);
        String date_time = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT, 0);
        String refId = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT_NO, 0) + generateRandomNumber(4);
        sbMsg.setLength(0);
        boolean bCharging = BatteryUtils.fBatteryCharging(mContext);
        float fBatterLevel = BatteryUtils.fBatteryLevel(mContext);
        String sn="";
        try {
             sn = mSystemBinderService.getSN();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        String strlibVersion = "";
        String strSPBootVersion="";
        int printDepth =0;
        try {
            byte[] libVersion = mSystemBinderService.readSerialLibVersion(0);
            strlibVersion = " :" + (libVersion == null ? "null" : new String(libVersion));
            byte[] bSPSPBootVersion = mSystemBinderService.readSpVersion(1);
            strSPBootVersion = " :" + (bSPSPBootVersion == null ? "null" : new String(bSPSPBootVersion));
            printDepth = mPrinterBinderService.dalThermalPrintGetDepth();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        if (isBold) {

//            String strPattern = PatternLogger.printPattern(10);
//            sbMsg.append( strPattern );

            sbMsg.append("                          TEST\n");
            sbMsg.append("------------------------------------------\n");
            sbMsg.append("\t\t\t" + date_time + "\n");
            sbMsg.append("------------------------------------------\n");
            sbMsg.append("                          SALE");
            sbMsg.append("\n\n");
            sbMsg.append("MERCHANT NAME:" + "\n                         001420183990573\n");
            sbMsg.append("TERMINAL NO:" + "\n                                        00026715\n");
            sbMsg.append("OPERATOR NO:" + "\n                                        12345678\n");
            sbMsg.append("ISSUER:" + "\n                          01020001\n");
            sbMsg.append("CARD NO:" + "\n                  9558803602109503920\n");
            sbMsg.append("ACQUIRER:" + "\n                          03050011\n");
            sbMsg.append("TXN. TYPE:" + "\n                                           SALE\n");
            sbMsg.append("EXP. DATE:" + "\n                                                2023/08\n");
            sbMsg.append("REF NO:" + "\n                                   " + refId + "\n");
            sbMsg.append("------------------------------------------\n");
            sbMsg.append("AMOUNT:" + "RMB:2.55" + "\n");
            sbMsg.append("------------------------------------------\n");
            sbMsg.append("CARDHOLDER SIGNATURE" + "\n\n");
            sbMsg.append("Charging ： " +bCharging+ "\n\n");
            sbMsg.append("BatterLevel ：" +fBatterLevel+ "\n\n");
            sbMsg.append("deviceMSG ： " +deviceMSG+ "\n\n");
            sbMsg.append("SN ： " +sn+ "\n\n");
            sbMsg.append("libVersion ： " +strlibVersion+ "\n\n");
            sbMsg.append("Boot Version ： " +strSPBootVersion+ "\n\n");
            sbMsg.append("printDepth ： " +printDepth+ "\n\n");

        } else {

//            String strPattern = PatternLogger.printPattern(10);
//            sbMsg.append( strPattern );
            sbMsg.append("                          TEST\n");
            sbMsg.append("------------------------------------------------------\n");
            sbMsg.append("\t\t\t" + date_time + "\n");
            sbMsg.append("------------------------------------------------------\n");
            sbMsg.append("                          SALE");
            sbMsg.append("\n\n");
            sbMsg.append("MERCHANT NAME:" + "\n                               001420183990573\n");
            sbMsg.append("TERMINAL NO:" + "\n                                              00026715\n");
            sbMsg.append("OPERATOR NO:" + "\n                                              12345678\n");
            sbMsg.append("ISSUER:" + "\n                              01020001\n");
            sbMsg.append("CARD NO:" + "\n                      9558803602109503920\n");
            sbMsg.append("ACQUIRER:" + "\n                              03050011\n");
            sbMsg.append("TXN. TYPE:" + "\n                                           SALE\n");
            sbMsg.append("EXP. DATE:" + "\n                                                2023/08\n");
            sbMsg.append("REF NO:" + "\n                                     " + refId + "\n");
            sbMsg.append("------------------------------------------------------\n");
            sbMsg.append("AMOUNT:" + "RMB:2.55" + "\n");
            sbMsg.append("------------------------------------------------------\n");
            sbMsg.append("CARDHOLDER SIGNATURE" + "\n\n");
            sbMsg.append("Charging ： " +bCharging+ "\n\n");
            sbMsg.append("BatterLevel ：" +fBatterLevel+ "\n\n");
            sbMsg.append("deviceMSG ： " +deviceMSG+ "\n\n");
            sbMsg.append("SN ： " +sn+ "\n\n");
            sbMsg.append("libVersion ： " +strlibVersion+ "\n\n");
            sbMsg.append("Boot Version: ： " +strSPBootVersion+ "\n\n");
            sbMsg.append("printDepth ： " +printDepth+ "\n\n");
        }
        LogUtils.i( "printMsg: " + sbMsg.toString());
        return sbMsg.toString();
    }

    /**
     * 生成随机数
     *
     * @param length 生成的长度
     * @return 随机数
     */
    public static String generateRandomNumber(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        LogUtils.i( "generateRandomNumber: " + sb.toString());
        return sb.toString();
    }

    private void printerTest(String itemName) {

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                try {

                    int customerIDVersion = getCustomerVersion();
                    int customter_id = SystemProperties.getInt("ro.szanfu.customter_id", 0);
                    if (customerIDVersion == 6 && customter_id == 6){
                        headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.evo_log);
                    }else {
                        headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_log);
                    }
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    blackWhitImage = DivBitmapUtils.createCustomBitmap(displayMetrics,
                            384f, 9.6f, 9.6f, 20);
                    headerImage = BitmapUtil.addBitmap(headerImage , blackWhitImage);

                    blackWhitImage = DivBitmapUtils.createCustomBitmap(displayMetrics,
                            384f, 38.4f, 38.4f, 4);


                    headerImage = BitmapUtil.addBitmap(headerImage , blackWhitImage);
                    headerImage = Bitmap.createScaledBitmap(headerImage, 384, headerImage.getHeight() * 384 / headerImage.getWidth(), false);
                    Bitmap printBmp;
                    if (imgBmp != null) {
                        Bitmap textBmp = creatTextBitmap(printMsg(), 384, 1400);
 // bitmap bitmap
                        printBmp = BitmapUtil.addBitmap(textBmp, imgBmp);
                    } else {
                        LogUtils.d(" Start converting text into images  开始将文字转成图片");
                        printBmp = creatTextBitmap(printMsg(), 384, 1300);
                        LogUtils.d("The pictures have been assembled.  图片组装完毕");
                    }
                    appendText("bmp width: " + printBmp.getWidth() + "." + "\n" + "bmp height: " + printBmp.getHeight() + "." + "\n");

                    Bitmap bottomPrintBmp;
                    bottomPrintBmp = creatTextBitmap("\n\n\n\n", 384, 100);

                    printBmp = BitmapUtil.addBitmap(printBmp, bottomPrintBmp);

//                    blackWhitImageLast = DivBitmapUtils.createCustomBitmap(displayMetrics,
//                            384f, 9.6f, 9.6f, 1);
//                    printBmp = BitmapUtil.addBitmap(printBmp , blackWhitImageLast);

                    Bitmap fullBitmap = Bitmap.createBitmap(384, headerImage.getHeight() + printBmp.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(fullBitmap);
                    canvas.drawColor(Color.WHITE);
                    canvas.drawBitmap(headerImage, 0, 0, null);
                    canvas.drawBitmap(printBmp, 0, headerImage.getHeight(), null);

                    int x = 0;
                    int width = fullBitmap.getWidth();
                    int height = fullBitmap.getHeight() + 30;
                    int bitW = PrintUtil.getPaddingBitWidth(width);
                    LogUtils.d("准备将printBmp转为byte[]");
                    byte[] bitmapData = PrintUtil.getBitmapData(fullBitmap);

                    LogUtils.d("printBmp转为byte[]结束");
                    LogUtils.e( "printerTest 发送打印请求接口: ");
                    mPrinterBinderService.printBitmap(x, bitmapData, bitW, height, new IPrinterCallback.Stub() {
                        @Override
                        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                            if (errorCode == ErrorCode.SUCCESS) {
                                LogUtils.d("打印返回：成功");
                                appendText(itemName + ": " + getResources().getString(R.string.printesucceed) + "\n");
                            } else {
                                LogUtils.d("打印返回：失败");
                                appendText("Error Code: " + errorCode + "\n" +
                                        "Sub Error Code: " + subErrorCode + "\n" +
                                        "Error Msg: " + errorMsg + "\n");
                            }
                        }
                    });

                    if (mPrinterBinderService == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast(PrinterServiceActivity.this, getResources().getString(R.string.print_serviceerror), Toast.LENGTH_SHORT);
                            }
                        });
                        return;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
    }

    public static final String PRINT_TEST_TEXT = "MERCHANT NAME:\n" +
            "001420183990573\n" +
            "TERMINAL NO:00026715\n" +
            "OPERATOR NO:12345678\n" +
            "ISSUER:01020001 \n" +
            "minshengBank\n" +
            "CARD NO:\n" +
            "9558803602109503920\n" +
            "ACQUIRER:03050011 \n" +
            "minshengBank\n" +
            "TXN. TYPE:SALE\n" +
            "EXP. DATE:2023/08\n" +
            "DATE/TIME:\n" +
            "2018/12/28 16:46:32\n" +
            "REF NO:201812280015\n" +
            "AMOUNT:  RMB:2.55 ￥2.55\n" +
            "REFERENCE\n" +
            "--------------------------\n" +
            "㈠ ㈡ ㈢ ㈣ ㈤ ㈥ ㈦ ㈧ ㈨ ㈩ \n" +
            "--------------------------\n" +
            "CARDHOLDER SIGNATURE\n";
    private void printerTextTest(String itemName) {
        appendText(itemName);
        try {
            Bitmap printBmp;
            if(imgBmp != null){
                Bitmap textBmp = creatTextBitmap(PRINT_TEST_TEXT, 384, 630);
 // bitmap bitmap
                printBmp = BitmapUtil.addBitmap(textBmp, imgBmp);
            }else{
                printBmp = creatTextBitmap(PRINT_TEST_TEXT, 384, 630);
            }

            appendText("bmp width: " + printBmp.getWidth() +"."+ "\n" + "bmp height: " + printBmp.getHeight() + "."+"\n");
            int x = 0;
            int width = printBmp.getWidth();
            int height = printBmp.getHeight();
            int bitW = PrintUtil.getPaddingBitWidth(width);
            byte[] bitmapData = PrintUtil.getBitmapData(printBmp);
            if(mPrinterBinderService == null){
                ToastUtils.showToast(PrinterServiceActivity.this,"打印服务获取失败，请检查该服务", Toast.LENGTH_SHORT);
                return;
            }

            mPrinterBinderService.printBitmap(x, bitmapData, bitW, height, new IPrinterCallback.Stub() {
                @Override
                public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                    if (errorCode == ErrorCode.SUCCESS){
                        appendText(itemName +": "+ getResources().getString(R.string.printesucceed) + "\n");
                    } else {
                        appendText("Error Code: " + errorCode +"\n"+
                                "Sub Error Code: " + subErrorCode+"\n" +
                                "Error Msg: " + errorMsg + "\n");

                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void printerSomers4006Test(String itemName) {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                if (mPrinterBinderService == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showToast(PrinterServiceActivity.this, getResources().getString(R.string.print_serviceerror), Toast.LENGTH_SHORT);
                        }
                    });
                    return;
                }

                Bitmap printBmp = null;
                try {
                    appendText("SOMERS flow: dalThermalPrintGetStatus -> printBitmap(384x788)");
                    int status = mPrinterBinderService.dalThermalPrintGetStatus();
                    appendText("dalThermalPrintGetStatus: " + status);
                    if (status != ErrorCode.SUCCESS) {
                        appendText("Printer status is not ready, skip printBitmap.");
                        return;
                    }

                    printBmp = createSomersCustomerBitmap();
                    int bitW = PrintUtil.getPaddingBitWidth(printBmp.getWidth());
                    int printHeight = printBmp.getHeight() + 50;
                    byte[] bitmapData = PrintUtil.getBitmapData(printBmp);
                    appendText("bmp width: " + printBmp.getWidth() + "\n" +
                            "bmp height: " + printBmp.getHeight() + "\n" +
                            "print height: " + printHeight + "\n" +
                            "bitmap bytes: " + bitmapData.length);

                    mPrinterBinderService.printBitmap(0, bitmapData, bitW, printHeight, new IPrinterCallback.Stub() {
                        @Override
                        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                            if (errorCode == ErrorCode.SUCCESS) {
                                appendText(itemName + ": " + getResources().getString(R.string.printesucceed) + "\n");
                            } else {
                                appendText("Error Code: " + errorCode + "\n" +
                                        "Sub Error Code: " + subErrorCode + "\n" +
                                        "Error Msg: " + errorMsg + "\n");
                            }
                        }
                    });
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                } finally {
                    recycleSafely(printBmp);
                }
            }
        });
    }

    private Bitmap createSomersCustomerBitmap() {
        Bitmap bmp = createBitmap(PRINT_WIDTH, SOMERS_PRINT_HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        canvas.drawColor(Color.WHITE);

        Paint titlePaint = makePaint(24f, true, Paint.Align.CENTER);
        Paint normalPaint = makePaint(22f, false, Paint.Align.LEFT);
        Paint centerPaint = makePaint(22f, false, Paint.Align.CENTER);
        Paint boldPaint = makePaint(22f, true, Paint.Align.LEFT);
        Paint linePaint = new Paint();
        linePaint.setColor(Color.BLACK);
        linePaint.setStrokeWidth(1f);

        float y = 34f;
        float left = 12f;
        float center = PRINT_WIDTH / 2f;
        canvas.drawText("CUSTOMER RECEIPT", center, y, titlePaint);
        y += 28f;
        canvas.drawLine(left, y, PRINT_WIDTH - left, y, linePaint);
        y += 32f;

        String[] rows = {
                "TYPE: SALE",
                "AMOUNT: 1.00 RUB",
                "FEE: 1.00",
                "CARD: ************7944",
                "PAYMENT: MIR",
                "ENTRY MODE: CLESS",
                "RC: 00 APPROVED",
                "AUTH CODE: 287789",
                "RRN: 617601565964",
                "TERMINAL TIME: 2026-06-25 13:41:41",
                "HOST TIME: 2026-06-25 13:41:43",
                "AID: A0000006581010",
                "TVR: 8080008800",
                "TSI: 0800",
                "CRYPTOGRAM: DDB7C59C3AEAA8CA",
                "APP LABEL: MIR",
                "MERCHANT: SOMERS PAY",
                "SLIP BITMAP HEIGHT: 788"
        };

        for (int i = 0; i < rows.length; i++) {
            Paint paint = i == 1 ? boldPaint : normalPaint;
            canvas.drawText(rows[i], left, y, paint);
            y += 30f;
        }

        canvas.drawLine(left, y, PRINT_WIDTH - left, y, linePaint);
        y += 34f;
        canvas.drawText("API sequence under test", center, y, centerPaint);
        y += 30f;
        canvas.drawText("getStatus -> printBitmap", center, y, centerPaint);
        y += 30f;
        canvas.drawText("width=384 height=788 bytes=37824", center, y, centerPaint);

        return bmp;
    }



    private void printerDivTest(String itemName) {

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                try {
//     *********************************************************** Logo ***********************************************************
                    Bitmap headerImage;
                    int customerIDVersion = getCustomerVersion();
                    int customter_id = SystemProperties.getInt("ro.szanfu.customter_id", 0);
                    if (customerIDVersion == 6 && customter_id == 6){
                        headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.evo_log);
                    }else {
                        headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.image_log);
                    }
                    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                    blackWhitImage = DivBitmapUtils.createCustomBitmap(displayMetrics,
                            384f, 9.6f, 9.6f, 20);
                    headerImage = BitmapUtil.addBitmap(headerImage , blackWhitImage);

                    blackWhitImage = DivBitmapUtils.createCustomBitmap(displayMetrics,
                            384f, 38.4f, 38.4f, 4);
                    headerImage = BitmapUtil.addBitmap(headerImage , blackWhitImage);

                    headerImage = Bitmap.createScaledBitmap(headerImage, 384, headerImage.getHeight() * 384 / headerImage.getWidth(), false);
                    byte[] bitmapHeaderImageData = PrintUtil.getBitmapData(headerImage);
                    printConfig[6] =headerImage.getHeight(); // LOGO
//     *********************************************************** Logo ***********************************************************

 // *********************************************************** ***********************************************************
                    boolean bCharging = BatteryUtils.fBatteryCharging(mContext);
                    float fBatterLevel = BatteryUtils.fBatteryLevel(mContext);
                    String sn="";
                    try {
                        sn = mSystemBinderService.getSN();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    String strlibVersion = "";
                    String strBootVersion="";
                    int printDepth =0;
                    try {
                        byte[] libVersion = mSystemBinderService.readSerialLibVersion(0);
                        strlibVersion = "" + (libVersion == null ? "null" : new String(libVersion));
                        byte[] bBootVersion = mSystemBinderService.readSpVersion(1);
                        strBootVersion = "" + (bBootVersion == null ? "null" : new String(bBootVersion));
                        printDepth = mPrinterBinderService.dalThermalPrintGetDepth();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    StringBuilder sbPrintMsg = new StringBuilder();
                    long currentDate = DateUtil.getTimeStamp(new Date(), 0);
                    String date_time = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT, 0);
                    String refId = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT_NO, 0) + generateRandomNumber(4);

//                    String strPattern = PatternLogger.printPattern(10);
//                    sbPrintMsg.append( strPattern );

                    sbPrintMsg.append("TEST\n");
                    sbPrintMsg.append("\n");
                    sbPrintMsg.append(date_time + "\n");
                    sbPrintMsg.append("\n");
                    sbPrintMsg.append("SALE\n");
                    sbPrintMsg.append("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdef\n");
                    sbPrintMsg.append("AaBbCcDdEeFfGgHhIiJjKkLlMmNnOoPpQqRrSsTtUuVvWwXxYyZz\n");
                    sbPrintMsg.append("123456789012345678901234567890123456789012345678990\n");
                    sbPrintMsg.append("...................................................\n");
                    sbPrintMsg.append("\n\n");
                    sbPrintMsg.append("MERCHANT NAME:" + "\n001420183990573\n");
                    sbPrintMsg.append("TERMINAL NO:" + "\n00026715\n");
                    sbPrintMsg.append("OPERATOR NO:" + "\n12345678\n");
                    sbPrintMsg.append("ISSUER:" + "\n01020001\n");
                    sbPrintMsg.append("CARD NO:" + "\n9558803602109503920\n");
                    sbPrintMsg.append("ACQUIRER:" + "\n03050011\n");
                    sbPrintMsg.append("TXN. TYPE:" + "\nSALE\n");
                    sbPrintMsg.append("EXP. DATE:" + "\n2023/08\n");
                    sbPrintMsg.append("REF NO:" + "\n" + refId + "\n");
                    sbPrintMsg.append("\n");
                    sbPrintMsg.append("AMOUNT:" + "RMB:2.55" + "\n");
                    sbPrintMsg.append("\n");
                    sbPrintMsg.append("CARDHOLDER SIGNATURE" + "\n\n");
                    sbPrintMsg.append("Charging ： " +bCharging+ "\n\n");
                    sbPrintMsg.append("BatterLevel ：" +fBatterLevel+ "\n\n");
                    sbPrintMsg.append("deviceMSG ： " +deviceMSG+ "\n\n");
                    sbPrintMsg.append("SN ： " +sn+ "\n\n");
                    sbPrintMsg.append("libVersion ： " +strlibVersion+ "\n\n");
                    sbPrintMsg.append("Boot Version ： " +strBootVersion+ "\n\n");
                    sbPrintMsg.append("printDepth ： " +printDepth+ "\n\n");

 // *********************************************************** ***********************************************************
 // *********************************************************** ***********************************************************

                    byte[] signBitmapData = new byte[0];
                    if (imgBmp != null){
                        Bitmap signBitmap = Bitmap.createBitmap(384, imgBmp.getHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(signBitmap);
                        canvas.drawColor(Color.WHITE);
                        canvas.drawBitmap(imgBmp, 0, 0, null);
                        signBitmapData = new byte[PrintUtil.getBitmapData(signBitmap).length];
                        signBitmapData = PrintUtil.getBitmapData(signBitmap);
                        printConfig[8]= signBitmap.getHeight();
                    }else {
                        printConfig[8]= 0;
                    }
 // *********************************************************** ***********************************************************
                    LogUtils.e("printBitmapPro------start to  printer ");
                    mPrinterBinderService.printBitmapPro( bitmapHeaderImageData,signBitmapData, printConfig, sbPrintMsg.toString() ,new IPrinterCallback.Stub() {
                        @Override
                        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                    LogUtils.e("printBitmapPro------Received to  printer ");
                            if (errorCode == ErrorCode.SUCCESS) {
                                LogUtils.d("打印返回：成功");
                                appendText(itemName + ": " + getResources().getString(R.string.printesucceed) + "\n");
                            } else {
                                LogUtils.d("打印返回：失败");
                                appendText("Error Code: " + errorCode + "\n" +
                                        "Sub Error Code: " + subErrorCode + "\n" +
                                        "Error Msg: " + errorMsg + "\n");
                            }
                        }
                    });

                    if (mPrinterBinderService == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast(PrinterServiceActivity.this, getResources().getString(R.string.print_serviceerror), Toast.LENGTH_SHORT);
                            }
                        });
                        return;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });

    }

    void setPrintConfigLeftAligned(String itemName){
        printConfig[0] =0; // Alignment: int[0]= 0 left alignment; int[0]= 1 center alignment; int[2]= 0 right-aligned int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =0; // Whether there is an underline: int[1] =0 without an underline; int[1] =1 has an underscore; : int[1] =0 ; int[1] =1 ;
        printConfig[2] =1; // Text magnification :int[2]=1 :1 times; int[2] = 2 times 2 :int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =0; // Whether to bold int[4] =0; No bold, int[4] =1: Bold; int[4] =0; , int[4] =1: ;
        printConfig[5] =384; // Width
        printConfig[6] =0; // LOGO height LOGO
        printConfig[7] =1200; // Height of the text area
        printConfig[8] =50; // Height of the signature area
        printConfig[9] =300; // The height of the bottom blank area
        printConfig[10] =0; // Reserved field
        printerDivTest(itemName);
    }

    void setPrintConfigAlinnedCentered(String itemName){
        printConfig[0] =1; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =0; // int[1] =0 ; int[1] =1 ;
        printConfig[2] =1; // int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =0; // int[4] =0; , int[4] =1: ;
        printConfig[5] =384;
        printConfig[6] =0; // LOGO
        printConfig[7] =1200;
        printConfig[8] =50;
        printConfig[9] =300;
        printConfig[10] =0;
        printerDivTest(itemName);
    }

    void setPrintConfigSriAlinnedCentered(String itemName){
        printConfig[0] =1; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =0; // int[1] =0 ; int[1] =1 ;
        printConfig[2] =1; // int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =0; // int[4] =0; , int[4] =1: ;
        printConfig[5] =384;
        printConfig[6] =0; // LOGO
        printConfig[7] =310;
        printConfig[8] =50;
        printConfig[9] =300;
        printConfig[10] =0;
        printerDivSriTest(itemName);
    }

    private void printerDivSriTest(String itemName) {

        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                try {

 // *********************************************************** ***********************************************************
                    boolean bCharging = BatteryUtils.fBatteryCharging(mContext);
                    float fBatterLevel = BatteryUtils.fBatteryLevel(mContext);
                    String sn="";
                    try {
                        sn = mSystemBinderService.getSN();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    String strlibVersion = "";
                    String strBootVersion="";
                    int printDepth =0;
                    try {
                        byte[] libVersion = mSystemBinderService.readSerialLibVersion(0);
                        strlibVersion = "" + (libVersion == null ? "null" : new String(libVersion));
                        byte[] bBootVersion = mSystemBinderService.readSpVersion(1);
                        strBootVersion = "" + (bBootVersion == null ? "null" : new String(bBootVersion));
                        printDepth = mPrinterBinderService.dalThermalPrintGetDepth();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    StringBuilder sbPrintMsg = new StringBuilder();
                    long currentDate = DateUtil.getTimeStamp(new Date(), 0);
                    String date_time = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT, 0);
                    String refId = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT_NO, 0) + generateRandomNumber(4);

                    sbPrintMsg.append("TEST\n");
                    sbPrintMsg.append("CARDHOLDER SIGNATURE" + "\n\n");
                    sbPrintMsg.append("Charging ： " +bCharging+ "\n\n");
                    sbPrintMsg.append("BatterLevel ：" +fBatterLevel+ "\n\n");
                    sbPrintMsg.append("deviceMSG ： " +deviceMSG+ "\n\n");
                    sbPrintMsg.append("SN ： " +sn+ "\n\n");
                    sbPrintMsg.append("libVersion ： " +strlibVersion+ "\n\n");
                    sbPrintMsg.append("Boot Version ： " +strBootVersion+ "\n\n");
                    sbPrintMsg.append("printDepth ： " +printDepth+ "\n\n");


                    byte[] signBitmapData = new byte[0];
                    mPrinterBinderService.printBitmapPro( signBitmapData,signBitmapData, printConfig, sbPrintMsg.toString() ,new IPrinterCallback.Stub() {
                        @Override
                        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                            if (errorCode == ErrorCode.SUCCESS) {
                                LogUtils.d("打印返回：成功");
                                appendText(itemName + ": " + getResources().getString(R.string.printesucceed) + "\n");
                            } else {
                                LogUtils.d("打印返回：失败");
                                appendText("Error Code: " + errorCode + "\n" +
                                        "Sub Error Code: " + subErrorCode + "\n" +
                                        "Error Msg: " + errorMsg + "\n");
                            }
                        }
                    });

                    if (mPrinterBinderService == null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast(PrinterServiceActivity.this, getResources().getString(R.string.print_serviceerror), Toast.LENGTH_SHORT);
                            }
                        });
                        return;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });

    }

    void setPrintConfigRightAlignment(String itemName){
        printConfig[0] =2; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =0; // int[1] =0 ; int[1] =1 ;
        printConfig[2] =1; // int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =1; // int[4] =0; , int[4] =1: ;
        printConfig[5] =384;
        printConfig[6] =0; // LOGO
        printConfig[7] =1300;
        printConfig[8] =50;
        printConfig[9] =300;
        printConfig[10] =0;
        printerDivTest(itemName);
    }

    void setPrintConfigLeftUnderline(String itemName){
        printConfig[0] =0; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =1; // int[1] =0 ; int[1] =1 ;
        printConfig[2] =1; // int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =1; // int[4] =0; , int[4] =1: ;
        printConfig[5] =384;
        printConfig[6] =0; // LOGO
        printConfig[7] =1300;
        printConfig[8] =50;
        printConfig[9] =300;
        printConfig[10] =0;
        printerDivTest(itemName);
    }

    void setPrintConfigLeftBolded(String itemName){
        printConfig[0] =0; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =0; // int[1] =0 ; int[1] =1 ;
        printConfig[2] =1; // int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =1; // int[4] =0; , int[4] =1: ;
        printConfig[5] =384;
        printConfig[6] =0; // LOGO
        printConfig[7] =1200;
        printConfig[8] =50;
        printConfig[9] =300;
        printConfig[10] =0;
        printerDivTest(itemName);
    }
    void setPrintConfigLeft2X(String itemName){
        printConfig[0] =0; // int[0]= 0 ; int[0]= 1 ; int[2]= 0
        printConfig[1] =0; // int[1] =0 ; int[1] =1 ;
        printConfig[2] =2; // int[2]=1 :1 ;int[2] = 2:2
        printConfig[3] =0; //
        printConfig[4] =1; // int[4] =0; , int[4] =1: ;
        printConfig[5] =384;
        printConfig[6] =0; // LOGO
        printConfig[7] =2900;
        printConfig[8] =50;
        printConfig[9] =300;
        printConfig[10] =0;
        printerDivTest(itemName);
    }





    public void showSingleAlertDialog() {
        int retDepth = 0;
        try {
 // Gets the depth that the printer has set
            retDepth = mPrinterBinderService.dalThermalPrintGetDepth();
            LogUtils.e( "retDepth : "+retDepth );
            if (retDepth < 0) {
                retDepth = 0;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        final String[] items = {"Set the printer printing dept:1"
                , "Set the printer printing dept:2"
                , "Set the printer printing dept:3"
                , "Set the printer printing dept:4"
                , "Set the printer printing dept:5"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Set the printer printing dept");
        alertBuilder.setSingleChoiceItems(items, retDepth - 1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(PrinterServiceActivity.this, items[i], Toast.LENGTH_SHORT).show();
                try {
                    int ret = mPrinterBinderService.dalThermalPrintSetDepth(i + 1);
                    appendText("dalThermalPrintSetDepth ： " + ret);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }

    private void dalThermalPrintGetStatus(String itemName) {
        appendText(itemName);
        try {
            /**
             * DESCRIPTION   :Get printer status 获取打印机状态
             *0   打印机正常
             *ERR_PRINTER_BUSY		    -4001
             *ERR_PRINTER_OUTOFPAPER		-4002	缺纸
             *ERR_PRINTER_INVALID_PARAM	-4003
             *ERR_PRINTER_FAULT		    -4004
             *ERR_PRINTER_TEMPERATURE		-4005 // ( )
             *ERR_PRINTER_UNFINISHED		-4006
             *ERR_PRINTER_FONT_LIBRARY	-4007
             *ERR_PRINTER_BUFFER_OVERFLOW	-4008
             *ERR_PRINTER_SET_FONT		-4009
             *ERR_PRINTER_GET_FONT		-4010
             *ERR_PRINTER_UNKOWN		    -4011
             *ERR_PRINTER_NOT_OPEN		-4012
             *ERR_PRINTER_VOLTAGE		   - 4013
             */
            appendText("--API----->>>> printer status:" );
            int ret = mPrinterBinderService.dalThermalPrintGetStatus();
            appendText("<<<<--API----- PrintGetStatus:" + ret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void Dialog_dalThermalPrintPaperWorking() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        View dialogView = View.inflate(this, R.layout.dialog_enter_printerline, null);
        dialog.setView(dialogView);
        dialog.setCancelable(false);
        dialog.show();
        EditText strLine = dialogView.findViewById(R.id.et_printer_line);
        final Button btn_login = dialogView.findViewById(R.id.btn_ok);
        final Button btn_cancel = dialogView.findViewById(R.id.btn_cancel);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                appendText("dalThermalPrintPaperWorking");
                /**
                 *    * FUNCTION NAME :DAL_thermalPrintPaperWorking
                 *     * DESCRIPTION   :Set the printer to run paper  设置打印机走纸
                 *     * INPUT         :line_num ：Print line 打印行数
                 */
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            int ret = mPrinterBinderService.dalThermalPrintPaperWorking(Integer.parseInt(strLine.getText().toString()));
                            appendText("dalThermalPrintPaperWorking ： " + ret);

                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();

            }
        });
    }


    private void getBatteryLevel(String itemName) {
        try {
            IBatteryResult iBatteryResult = mPrinterBinderService.getBatteryLevel();
            int retBatteryLevel = iBatteryResult.getBatteryLevel();
            int retIsChargingStatus = iBatteryResult.getIsChargingStatus();
            appendText("Battery level ：" + retBatteryLevel);
            if (retIsChargingStatus == 0) {
                appendText("Battery ChargingStatus ：" + "is not charging");
            } else if (retIsChargingStatus == 1) {
                appendText("Battery ChargingStatus ：" + "is  charging");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
        if (isBold) {
            String familyName = "宋体";
            Typeface font = Typeface.create(familyName, Typeface.BOLD);
            textPaint.setTypeface(font);
        }
        textPaint.setAntiAlias(true);
        textPaint.setFilterBitmap(true);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);
 // " Android , Canvas.drawText , , .StaticLayout android , StaticLayout ";
        StaticLayout sl = new StaticLayout(text, textPaint, newBitmap.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
        textPaint.setStyle(Paint.Style.FILL);
        canvas.translate(0, 50);
        //canvas.translate(0, paddingTop); // paddingTop is the blank size you defined
        sl.draw(canvas);
        return newBitmap;
    }


    public void onSignClick() {
        Intent intent = new Intent(this, SignatureActivity.class);
        startActivityForResult(intent, SIGN_REQUEST);
    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);
        switch (position) {
            case POSITION_PRINTER_TEST:
                printerTest(itemName);
                break;
            case POSITION_PRINTER_TEXT_TEST:
                printerTextTest(itemName);
                break;
            case POSITION_SIGNATURE:
                onSignClick();
                break;
            case POSITION_LOCAL_JBIG:
                Bitmap bmp = getJBIGToBitmap();
                mJbigToImg.setVisibility(View.VISIBLE);
                mJbigToImg.setImageBitmap(bmp);
                Log.i("PrinterServiceActivity", bmp == null ? "---- 获取本地JBIG，转bitmap失败 -----" : "-----获取本地JBIG，转bitmap成功 -----");
                break;
            case POSITION_PRINTER_BOLD:
                isBold = !isBold;
                if (isBold) {
                    mButtons[3] = getString(R.string.printer_normal);
                } else {
                    mButtons[3] = getString(R.string.printer_bold);
                }
                setButtonDataSource(mButtons);
                break;
            case POSITION_PRINTER_CHECKBATTERY:
                getBatteryLevel(itemName);
                break;
            case POSITION_PRINTER_DALTHERMALPRINTSETDEPTH:
                showSingleAlertDialog();
                break;
            case POSITION_PRINTER_DALTHERMALPRINTGETSTATUS:
                dalThermalPrintGetStatus(itemName);
                break;
            case POSITION_PRINTER_DALTHERMALPRINTPAPERWORKING:
//                dalThermalPrintPaperWorking(itemName);
                Dialog_dalThermalPrintPaperWorking();
                break;
            case POSITION_PRINTER_PRINT_DIV_LEFT_ALIGNED:
                setPrintConfigLeftAligned(itemName);
                break;

            case POSITION_PRINTER_PRINT_DIV_CENTERED:
                setPrintConfigAlinnedCentered(itemName);
                break;
            case POSITION_PRINTER_PRINT_DIV_RIGHT_ALIGNMENT:
                setPrintConfigRightAlignment(itemName);
                break;
            case POSITION_PRINTER_PRINT_DIVLEFT_ALIGNED_UNDERLINE:
                setPrintConfigLeftUnderline(itemName);
                break;
            case POSITION_PRINTER_PRINT_DIV_LEFT_ALIGNED_BOLDED:
                setPrintConfigLeftBolded(itemName);
                break;
            case POSITION_PRINTER_PRINT_DIVLEFT_ALIGNED2XFONT:
                setPrintConfigLeft2X(itemName);
                break;
            case POSITION_PRINTER_SRILANKALKA:
                LogUtils.e("---mPrinterBinderService------Button Click----->");
                printerSavinduTest(itemName);
                break ;
            case POSITION_PRINTER_SOMERS_4006:
                printerSomers4006Test(itemName);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SIGN_REQUEST && resultCode == RESULT_OK && data != null) {
            byte[] byteArray = data.getByteArrayExtra("bitmap");
            final Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            mIVSign.setVisibility(View.VISIBLE);
            mIVSign.post(new Runnable() {
                @Override
                public void run() {
                    /**
                     *  BitmapFactory提供了多种方式根据不同的图片源来创建Bitmap对象：
                     *   （1）Bitmap BitmapFactory.decodeFile(String pathName, Options opts)： 读取SD卡上的图片。
                     *   （2）Bitmap BitmapFactory.decodeResource(Resources res, int id, Options opts) ： 读取网络上的图片。
                     *   （3）Bitmap BitmapFactory.decodeResource(Resources res, int id, Options opts) ： 读取资源文件中的图片。
                     *   这三个函数默认会直接为bitmap分配内存，我们通过第二个参数Options来控制，让它不分配内存，同时可以得到图片的相关信息。具体参考：
                     *   BitmapFactory.Options options = new BitmapFactory.Options();
                     *   options.inJustDecodeBounds = true;
                     *   BitmapFactory.decodeFile(pathName，options);
                     *   int imageHeight = options.outHeight;
                     *   int imageWidth = options.outWidth;
                     */
 // bitmap 1.5:1, bitmap
                    imgBmp = BitmapUtil.createScaleBitmap(bitmap, MAX_BIT_WIDTH, MAX_BIT_HEIGHT);
                    mIVSign.setImageBitmap(imgBmp);
 // bitmap JBIG
                    boolean isEncodeAndSaveSuccess = JBIGUtil.bmpToJBIGAndSave(imgBmp, getApplicationContext());
                    LogUtils.i( "----- 结果：" + isEncodeAndSaveSuccess);
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(blackWhitImage!=null){
            blackWhitImage.recycle();
        }
    }

    /**
     * 获取系统版本，使用AFSystemCommonInterface
     *
     * @return 系统版本号
     */
    public String getSystemVersion() {
        //SZANFU_AF930_V1.32_02_20240329_A2_userdebug
        Log.i(TAG, "getSystemVersion: " + Build.DISPLAY);
        String[] fmtSystem = Build.DISPLAY.split("_");
        if (fmtSystem.length >= 3) {
            Log.i(TAG, "系统版本:" + fmtSystem[2]);
            return fmtSystem[2];
        }
        return "";
    }


    /**
     * 获取客户ID
     *
     * @return 客户编号
     */
    public int getCustomerVersion() {
        String[] fmtSystem = Build.DISPLAY.split("_");


        if (fmtSystem.length < 4) {
            Log.e(TAG, "getCustomerVersion: DISPLAY 格式错误, 解析失败! DISPLAY=" + Build.DISPLAY);
            return 0;
        }

        String customerVersionStr = fmtSystem[3];
        Log.i(TAG, "getCustomerVersion 客户编号: " + customerVersionStr);

        try {
            return Integer.parseInt(customerVersionStr.trim());
        } catch (NumberFormatException e) {
            Log.e(TAG, "getCustomerVersion: 解析失败, 非法格式 -> " + customerVersionStr);
            return 0;
        }
    }

//    ----------------------------------------------------------------------------------------------
    private Bitmap renderHeaderBitmap(String busName, float fontSize) {
        final float pad = 12f;
        final float gap = 6f;

        // ── CHANGE 1: bus name is now 2.2× fontSize instead of 1.5× ──
        Paint pName = makePaint(fontSize * 2.2f, true, Paint.Align.CENTER);
        Paint pDiv  = new Paint();
        pDiv.setColor(Color.BLACK);
        pDiv.setStrokeWidth(2f);

        Paint.FontMetrics fmName = pName.getFontMetrics();

        float nameRowH = pName.getFontSpacing();
        int   bmpH     = (int) Math.ceil(pad + nameRowH + gap + 4f);

        Bitmap bmp = Bitmap.createBitmap(PRINT_WIDTH, bmpH, Bitmap.Config.ARGB_8888);
        Canvas cv  = new Canvas(bmp);
        cv.drawColor(Color.WHITE);

        float cx = PRINT_WIDTH / 2f;

        float y = pad + (-fmName.ascent);
        cv.drawText(busName != null ? busName : "", cx, y, pName);
        cv.translate(0, 0);
        return bmp;
    }

        private Bitmap renderTicketBodyBitmap() {
            final float pad    = 12f;
            final float divGap = 6f;
            float fontSize = 20f;
            float lineSpacing = 4f;

            Paint pNorm  = makePaint(fontSize,        false, Paint.Align.LEFT);
            Paint pBold  = makePaint(fontSize,        true,  Paint.Align.LEFT);
            Paint pCtr   = makePaint(fontSize,        false, Paint.Align.CENTER);
            Paint pCtrB  = makePaint(fontSize * 1.1f, true,  Paint.Align.CENTER);
            // ── CHANGE 2: dedicated paint for the Total line ──
            Paint pTotal = makePaint(fontSize * 1.6f, true,  Paint.Align.CENTER);
            Paint pRgt   = makePaint(fontSize,        false, Paint.Align.RIGHT);
            Paint pRgtB  = makePaint(fontSize,        true,  Paint.Align.RIGHT);
            Paint pLine  = new Paint();
            pLine.setColor(Color.BLACK);
            pLine.setStrokeWidth(1f);

            float lh      = pNorm.getFontSpacing() + lineSpacing - 5;
            // ── Total row is taller because pTotal is larger ──
            float totalRowH = pTotal.getFontSpacing() + lineSpacing;
            boolean showPaid = false;

            float fullHalfRowH = lh; //lh : 0f;

            float totalH = lh               // bus number
                    + lh                    // permitNumber | date
                    + lh                    // ticketNumber | time
                    + divGap + 5
                    + divGap
                    + divGap
                    + lh                    // route
                    + lh                    // From
                    + lh                    // To
                    + fullHalfRowH
                    + 0f      //lh : 0f
                    + totalRowH             // Total (uses taller pTotal)
                    + 1f + divGap
                    + (showPaid ? lh : 0f); // Paid | Bal

            Bitmap bmp = Bitmap.createBitmap(PRINT_WIDTH, (int) Math.ceil(totalH), Bitmap.Config.ARGB_8888);
            Canvas cv  = new Canvas(bmp);
            cv.drawColor(Color.WHITE);

            float cx  = PRINT_WIDTH / 2f;
            float xl  = pad;
            float xr  = PRINT_WIDTH - pad;

            Paint.FontMetrics fm     = pNorm.getFontMetrics();
            Paint.FontMetrics fmTot  = pTotal.getFontMetrics();
            float base    = -fm.ascent;
            float baseTot = -fmTot.ascent;

            float y = 0;

            y += base;
            cv.drawText("Bus No: 1572752", cx, y, pCtr);
            y += fm.descent + lineSpacing;

            // Permit | Date
            y += base;
            cv.drawText("PERMIL:57388888", xl, y, pNorm);
            cv.drawText("2026-05-08", xr, y, pRgt);
            y += fm.descent + lineSpacing;

            // TicketNumber | Time
            y += base;
            cv.drawText("Ticket:#10", xl, y, pNorm);
            cv.drawText("01:13 PM", xr, y, pRgt);
            y += fm.descent + lineSpacing;

            y += divGap;

            // Route
            y += base;
            cv.drawText("138 Hommageaienei-epidni", cx, y, pCtr);
            y += fm.descent + lineSpacing;

            // From / To
            y += base;
            cv.drawText("From: " + "Pattend Bus Teminus", cx, y, pCtrB);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText("To:   " + "Homaneidnien Terindin", cx, y, pCtrB);
            y += fm.descent + lineSpacing;

            // Full | Half
            y += base;
            cv.drawText("Full: " + 1 + " x " + fp(136), cx, y, pCtr);
            y += fm.descent + lineSpacing;


            // ── CHANGE 2 applied: use pTotal here ──
            y += baseTot;
            cv.drawText("Total: Rs. " + fp(136), cx, y, pTotal);
            y += fmTot.descent + lineSpacing;


            // Bus type footer
            y += base;
            cv.drawText(" ", cx, y, pCtr);

            return bmp;
        }

        public Bitmap addBitmap(Bitmap first, Bitmap second) {
            if(first == null || second == null)
                return null;

            int width = Math.max(first.getWidth(),second.getWidth());
            int height = first.getHeight() + second.getHeight();
            Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second,(width/2 - second.getWidth()/2), first.getHeight(),  null);
            return result;
        }

        private void printerSavinduTest(String itemName) {
            Bitmap signBmp = renderHeaderBitmap("Savindu Express", 20f);
            Bitmap logoBmp = renderTicketBodyBitmap();

            Bitmap logoBmpnew = addBitmap(logoBmp, signBmp);

            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    appendText(itemName);
                    try {
                        LogUtils.e("---mPrinterBinderService------dalThermalPrintGetStatus----->");
                        int status = mPrinterBinderService.dalThermalPrintGetStatus();
                        mPrinterBinderService.dalThermalPrintSetDepth(6);
                        Log.d(TAG, "Printer status: " + status);

                        if (status != 0) {
                            recycleSafely(signBmp);
                            recycleSafely(logoBmp);
                            recycleSafely(logoBmpnew);
                            return;
                        }

                        byte[] logoBitmapData = (logoBmpnew != null)
                                ? PrintUtil.getBitmapData(logoBmpnew)
                                : PrintUtil.getEmptyData(PRINT_WIDTH, 1);
                        int logoHeight = (logoBmpnew != null) ? logoBmpnew.getHeight() : 1;
                        LogUtils.e("---mPrinterBinderService------printBitmap----->");
                        mPrinterBinderService.printBitmap(0, logoBitmapData, PRINT_WIDTH, logoBmpnew.getHeight(), new IPrinterCallback.Stub() {
                            @Override
                            public void onResult(int errorCode, String errorMsg, int subErrorCode)
                                    throws RemoteException {
                                if (errorCode == ErrorCode.SUCCESS) {
                                    appendText(itemName + ": " + getResources().getString(R.string.printesucceed) + "\n");
                                } else {
                                    appendText("Error Code: " + errorCode + "\n" +
                                            "Sub Error Code: " + subErrorCode + "\n" +
                                            "Error Msg: " + errorMsg + "\n");
                                }
                            }
                        });

                        if (mPrinterBinderService == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showToast(PrinterServiceActivity.this, getResources().getString(R.string.print_serviceerror), Toast.LENGTH_SHORT);
                                }
                            });
                            return;
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                        appendErrorText(itemName, e.getMessage());
                    }
                }
            });

        }

    private static Paint makePaint(float size, boolean bold, Paint.Align align) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(size);
        p.setAntiAlias(true);
        p.setFakeBoldText(bold);
        p.setTextAlign(align);
        return p;
    }

    private String fp(float price) {
        try {
            return priceFormat.format(price);
        } catch (Exception e) {
            return String.format("%.2f", price);
        }
    }


    private static void recycleSafely(Bitmap bmp) {
        if (bmp != null && !bmp.isRecycled()) bmp.recycle();
    }

}
