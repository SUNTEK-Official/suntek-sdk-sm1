package com.szanfu.sdk.test.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.RemoteException;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.card.ICardBinderService;
import com.szanfu.sdk.api.card.ICardTestCallback;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.emv.DalcscIsoMifareCmdResult;
import com.szanfu.sdk.api.emv.IEmvBinderService;
import com.szanfu.sdk.api.error.ErrorCode;
import com.szanfu.sdk.api.printer.IPrinterBinderService;
import com.szanfu.sdk.api.printer.IPrinterCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.adapter.MyButtonAdapter;
import com.szanfu.sdk.test.base.BaseActivity;
import com.szanfu.sdk.test.base.ResultAdapter;
import com.szanfu.sdk.test.bean.ItemButtonBean;
import com.szanfu.sdk.test.bean.ItemResultBean;
import com.szanfu.sdk.test.bean.PrintParm;
import com.szanfu.sdk.test.entity.LcdInfo;
import com.szanfu.sdk.test.lcd.LcdFillStressRunner;
import com.szanfu.sdk.test.util.AFUtils;
import com.szanfu.sdk.test.util.BatteryUtils;
import com.szanfu.sdk.test.util.BitmapUtil;
import com.szanfu.sdk.test.util.ConstantData;
import com.szanfu.sdk.test.util.DataConversionUtils;
import com.szanfu.sdk.test.util.DateUtil;
import com.szanfu.sdk.test.util.DivBitmapUtils;
import com.szanfu.sdk.test.util.FileHelper;
import com.szanfu.sdk.test.util.FileStorageUtils;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.LcdUtils;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.PrintUtil;
import com.szanfu.sdk.test.util.WatermarkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.uama.flashlightcompat.FlashlightCompat;

import static android.graphics.Bitmap.createBitmap;

import org.json.JSONException;
import org.json.JSONObject;

public class PressureActivity extends BaseActivity implements View.OnClickListener {
    private static FileStorageUtils fileStorage;
    boolean FQRScanFlag = true;
    private AlertDialog alertDialog;
    int iNumberGrid = 0;
    int result = -1;
    private EditText et_interval;
    private String[] mButtons;
    private int iIntervalSeconds;
    private List<ItemResultBean> resultData = new ArrayList<>();
    RecyclerView recyclerViewResult;
    MyButtonAdapter myButtonAdapter;
    private Button clear_text;
    private List<ItemButtonBean> buttonData = new ArrayList<>();
    RecyclerView recycleView_btn;
    ResultAdapter resultAdapter;
    int iResultCount = 250; // 250 , List
    private EditText ed_conut;
    private int iCountNumberNeedsRun = 0;
    private int iRunSuccessful = 0;
    private int iUnsuccessful = 0;
    private int iIsRunsCount = 0;
    private int iSearchCardType = 1;
    boolean iSearCardType1DataNull = false;
    public static final int FIND_CARD_TIMEOUT = 10;

    private IEmvBinderService mIEmvBinderService;
    private ICardBinderService mCardBinderService;
    private IPrinterBinderService mPrinterBinderService;
    private ISystemBinderService mSystemBinderService;
    private IEmvBinderService mEmvBinderService;
    String cardNumber = "";
    private boolean mMifareCardOperationMode;
    private DalcscIsoMifareCmdResult dalcscIsoMifareCmdResult;

    private static Bitmap imgBmp;
    private boolean isBold = false;
    StringBuffer sbMsg = new StringBuffer();

    Timer timer = new Timer();
    int iTimeOut = 1;
    private Context mContext;
    private boolean isTaskRuning = false;
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard/Android/DeviceMSG.txt";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    JSONObject jsonObjectMsg = new JSONObject();

 // LCD
    private final LcdUtils mLcdUtils = LcdUtils.getInstance();

 // LCD Fill Runner
    private LcdFillStressRunner mLcdStressRunner;


    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
            mCardBinderService = ICardBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_CARD));
            mIEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
            mPrinterBinderService = IPrinterBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PRINTER));
            mLcdUtils.init(mSystemBinderService);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pressure_list);

        fileStorage = FileStorageUtils.getInstance(this);
        mContext = this;
        et_interval = findViewById(R.id.et_interval);
        mButtons = getResources().getStringArray(R.array.pressure_LIST);
        ed_conut = findViewById(R.id.ed_conut);
        clear_text = findViewById(R.id.clear_text);
        clear_text.setOnClickListener(this);

        recyclerViewResult = findViewById(R.id.rv_result);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewResult.setLayoutManager(linearLayoutManager);
        resultAdapter = new ResultAdapter(resultData, this);
        recyclerViewResult.setAdapter(resultAdapter);
        Toast.makeText(mContext, "压力测试过程中，请保持屏幕常亮；", Toast.LENGTH_SHORT).show();
        resultAdapter.setmOnRecyclerItemClickClicklistener(new ResultAdapter.OnRecyclerItemClickClicklistener() {
            @Override
            public void onRecyclerItemClickListener(int position) {
                Log.e("yxd", "onRecyclerItemClickListener: ===" + position);
            }
        });


        recycleView_btn = (RecyclerView) findViewById(R.id.rv_btn);
        LinearLayoutManager linearLayoutBtnManager = new LinearLayoutManager(this);
        recycleView_btn.setLayoutManager(linearLayoutBtnManager);
        for (int i = 0; i < mButtons.length; i++) {
            LogUtils.e("setButtonDataSource: " + mButtons);
            String btnName = mButtons[i];
            ItemButtonBean itemButtonBean = new ItemButtonBean();
            itemButtonBean.setName(btnName);
            buttonData.add(itemButtonBean);
        }
        LogUtils.e("setButtonDataSource: " + buttonData.size());
        myButtonAdapter = new MyButtonAdapter(buttonData, this);
        recycleView_btn.setAdapter(myButtonAdapter);
        myButtonAdapter.setmOnRecyclerItemClickClicklistener(new MyButtonAdapter.OnRecyclerItemClickClicklistener() {
            @Override
            public void onRecyclerItemClickListener(int position) {
                if (ed_conut.getText().toString().trim().isEmpty()) {
                    iCountNumberNeedsRun = 1;
                } else {
                    iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
                }
                if (0 == position) {

                    if (et_interval.getText().toString().trim().isEmpty()) {
                        iIntervalSeconds = 1000;
                    } else {
                        iIntervalSeconds = 1000 * Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                    }

                    if (!isTaskRuning) {

                        iSearchCardType = 1;
                        fileStorage.deleteFile("pressMSR.txt");
                        fileStorage.writeFile("pressureDone.txt","0");
                        fileStorage.writeFile("pressureNeedsRun.txt",String.valueOf(iCountNumberNeedsRun));

                        msrCardTest(buttonData.get(position).getName());
                        isTaskRuning = true;
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure_backup), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure), Toast.LENGTH_LONG).show();
                    }

                } else if (1 == position) {

                    if (et_interval.getText().toString().trim().isEmpty()) {
                        iIntervalSeconds = 1000;
                    } else {
                        iIntervalSeconds = 1000 * Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                    }

                    if (!isTaskRuning) {
                        iSearchCardType = 2;

                        fileStorage.deleteFile("pressIC.txt");
                        fileStorage.writeFile("pressureDone.txt","0");
                        fileStorage.writeFile("pressureNeedsRun.txt",String.valueOf(iCountNumberNeedsRun));

                        icCardTest(buttonData.get(position).getName());
                        isTaskRuning = true;
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure_backup), Toast.LENGTH_LONG).show();

                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure), Toast.LENGTH_LONG).show();
                    }


                } else if (2 == position) {

                    if (et_interval.getText().toString().trim().isEmpty()) {
                        iIntervalSeconds = 1000;
                    } else {
                        iIntervalSeconds = 1000 * Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                    }
 // NFC
                    if (!isTaskRuning) {
                        iSearchCardType = 4;
                        fileStorage.deleteFile("pressNFC.txt");
                        fileStorage.writeFile("pressureDone.txt","0");
                        fileStorage.writeFile("pressureNeedsRun.txt",String.valueOf(iCountNumberNeedsRun));
                        nfcCardTest(buttonData.get(position).getName());
                        isTaskRuning = true;
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure_backup), Toast.LENGTH_LONG).show();


                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure), Toast.LENGTH_LONG).show();
                    }
                } else if (3 == position) {
                    showSingleAlertDialog();

                } else if (4 == position) {


                    if (et_interval.getText().toString().trim().isEmpty()) {
                        iIntervalSeconds = 1000;
                    } else {
                        iIntervalSeconds = 1000 * Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                    }
                    if (!isTaskRuning) {
                        fileStorage.deleteFile("pressBuzzer.txt");
                        fileStorage.writeFile("pressureDone.txt","0");
                        fileStorage.writeFile("pressureNeedsRun.txt",String.valueOf(iCountNumberNeedsRun));
                        isTaskRuning = true;
                        buzzerTest(buttonData.get(position).getName());
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure_backup), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure), Toast.LENGTH_LONG).show();
                    }
                } else if (5 == position) {


                    if (et_interval.getText().toString().trim().isEmpty()) {
                        iIntervalSeconds = 1000;
                    } else {
                        iIntervalSeconds = 1000 * Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                    }
                    if (!isTaskRuning) {
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure_backup), Toast.LENGTH_LONG).show();
                        if (ed_conut.getText().toString().trim().isEmpty()) {
                            iTimeOut = 1 * 1000 * 5;
                            LogUtils.e("sacnQrCodetimeOut :" + iTimeOut);
                        } else {
                            iTimeOut = Integer.parseInt(ed_conut.getText().toString().trim()) * 1000 * 60;
                            LogUtils.e("sacnQrCodetimeOut :" + iTimeOut);
                        }
                        runUIFrash(3, "Turn on the light, set the opening time to:" + iTimeOut + "MS");
                        setFrontFlashStatus(true);
                        isTaskRuning = true;
                        runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                setFrontFlashStatus(false);
                            }
                        }, iTimeOut);
                    } else {
                        Toast.makeText(mContext, getResources().getString(R.string.task_pressure), Toast.LENGTH_LONG).show();
                    }
                } else if (6 == position) {
                    LogUtils.e(" 智能扫码每次都电");
                    iIntervalSeconds = 0;
                    iRunSuccessful = 0;
                    iUnsuccessful = 0;
                    iIsRunsCount = 0;
                    isTaskRuning = true;
                    FQRScanFlag = true;
                    try {
                        jsonObjectMsg.put("scanPower", "null");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    FileHelper.writeToFile(mContext, "pressTest.txt", jsonObjectMsg.toString());
                    scannerOpen(buttonData.get(position).getName());
                } else if (7 == position) {
                    LogUtils.e(" 智能扫码只有一次上电操作");
                    iIntervalSeconds = 0;
                    iRunSuccessful = 0;
                    iUnsuccessful = 0;
                    iIsRunsCount = 0;
                    isTaskRuning = true;
                    FQRScanFlag = false;
                    try {
                        jsonObjectMsg.put("scanpowerOne", "null");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    FileHelper.writeToFile(mContext, "pressTest.txt", jsonObjectMsg.toString());
                    scannerOpen(buttonData.get(position).getName());
                } else if (8 == position) {
                    showLcdFillStressMenu("LCD Fill Stress");
                } else if (9 == position) {
                    statisticsFileJsonInfo("pressMSR.txt");
                    statisticsFileJsonInfo("pressIC.txt");
                    statisticsFileJsonInfo("pressNFC.txt");
                    statisticsFileJsonInfo("pressPrint.txt");
                    statisticsFileJsonInfo("pressBuzzer.txt");
                    String strResult = FileHelper.readFromFile(mContext, "pressTest.txt");
                    runUIFrash(2, strResult);
                }


            }
        });

//        if (AppConfig.buildType != 0){
        setWaterMask();
//        }

    }


    void setWaterMask() {
        String libVersion = "";

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_CODE_STORAGE_PERMISSION);
        } else {
            String strMsg = readFile();
            try {
                JsonArray jsonArray = JsonParser.parseString(strMsg).getAsJsonArray();
                JsonObject firstObject = jsonArray.get(0).getAsJsonObject();

                libVersion = firstObject.get("Lib_Version").getAsString();
                LogUtils.e("libVersion :" + libVersion);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

 // ( )
        WatermarkUtils.addWatermark(this,
                libVersion,
                Color.BLUE, 24, 0, false, 3);
    }

    private String readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/DeviceMSG.txt");
        if (!file.exists())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e("stringBuilder.toString() :" + stringBuilder.toString());
        return stringBuilder.toString();
    }


    void runUIFrash(int iLevel, String strResultMsg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(strResultMsg);
                itemResultBean.setiResultLevel(iLevel);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();
                LogUtils.e("***buttonData.get(position).getName()***********" + resultData.size());
            }
        });
    }

    /**
     * 磁条卡压力测试
     *
     * @param itemName
     */
    private void msrCardTest(String itemName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                runUIFrash(2, itemName);
                try {
                    mCardBinderService.testMSRCard(FIND_CARD_TIMEOUT, new mCardTestCallbackProxy(itemName));
                } catch (RemoteException e) {
                    e.printStackTrace();
                    runUIFrash(1, itemName + e.getMessage());
                }

            }
        });
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_text:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultData.clear();
                        resultAdapter.notifyDataSetChanged();
                    }
                });

                break;
        }
    }

    private class mCardTestCallbackProxy extends ICardTestCallback.Stub {
        private final String mItemName;

        private mCardTestCallbackProxy(String itemName) {
            mItemName = itemName;
        }

        @Override
        public void onFindCardStart() throws RemoteException {
            runUIFrash(3, getResources().getString(R.string.start_looking_for_a_card));
        }

        @Override
        public void onFindCardSuccess(int cardType) throws RemoteException {
            if (cardType == 8) {
                iUnsuccessful = iUnsuccessful + 1;
            }
            runUIFrash(3, getResources().getString(R.string.the_card_type_is_searched_successfully) + cardType);
            int ret = mIEmvBinderService.apiDALCscIsMifare();
            runUIFrash(3, "apiDALCscIsMifare ret : " + ret);
            /**
             * MifareOperaData opcode
             * MIF_OPTFHIR=4,//M1 card: read
             * MIF_OPT-WRITE=5,//M1 card: write
             * MIF_OPT-INCREMENT=6,//M1 card:++
             * MIF_OPT_deCREMENT=7,//M1 card: - minus
             * MIF_OPT_Compatible_SRITE=8,//M0 card: compatible with writin
             */
            int operateCode;
            // After performing read, add, and subtract operations, read the data of the corresponding card block (Read Blocknum data/Upda
            byte[] operateData;
            //length of data read (The length of writing or reading Blocknum data)
            int[] operateDataLen;

            if (1 == ret && 4 == cardType) {
                if (mMifareCardOperationMode) {
                    operateCode = 5;
                    LogUtils.e("onFindCardSuccess: Write data.... ");
                    operateData = AFUtils.hexString2ByteArray("FF110200");
                    operateDataLen = new int[]{operateData.length};
                    LogUtils.e("Write data content: " + AFUtils.showResult0xStr(operateData) + " Write data Len: " + operateDataLen[0]);
                } else {
                    LogUtils.e("onFindCardSuccess: Read data.... ");
                    operateCode = 4;
                    operateData = new byte[512];
                    operateDataLen = new int[1];
                }
                try {
                    // The key required for M1 card authentication
                    byte[] cardKey = new byte[6];
                    Arrays.fill(cardKey, (byte) 0xFF);
                    //m1 card authentication type, KEYA: 0, KEYB: 1
                    int keyType = 0;
                    //The block number for operating M1 and M0 cards
                    int blockNum = 5;
                    dalcscIsoMifareCmdResult = mIEmvBinderService.apiDALcscIsoMifareCmd(operateCode, cardKey, keyType, blockNum, operateData, operateDataLen);
                    byte[] m1_opt_dataOut = new byte[operateDataLen[0]];
                    System.arraycopy(operateData, 0, m1_opt_dataOut, 0, operateDataLen[0]);
                    runUIFrash(3, "blockNum :" + blockNum);
                    runUIFrash(3, "Received data Len : " + (dalcscIsoMifareCmdResult == null ? "null" : dalcscIsoMifareCmdResult.getM1_opt_data_len()));
                    runUIFrash(3, "Received data : " + (dalcscIsoMifareCmdResult == null ? "null" : HexUtil.bcd2str(m1_opt_dataOut)) + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        public void onTrackDataRead(byte[] track1Data, byte[] track2Data, byte[] track3Data) throws RemoteException {
            runUIFrash(3, "Track1 Data: " + (track1Data == null ? "null" : (new String(track1Data)).trim()));
            runUIFrash(3, "Track2 Data: " + (track2Data == null ? "null" : (new String(track2Data)).trim()));
            runUIFrash(3, "Track3 Data: " + (track3Data == null ? "null" : (new String(track3Data)).trim()));
            if ((new String(track1Data)).trim().isEmpty()
                    || (new String(track2Data)).trim().isEmpty()
                    || (new String(track3Data)).trim().isEmpty()) {
                LogUtils.e("MSR 寻卡失败");
                iSearCardType1DataNull = false;
//                iUnsuccessful = iUnsuccessful + 1;
            } else {
                iSearCardType1DataNull = true;
                LogUtils.e("MSR 寻卡成功");
//                iRunSuccessful = iRunSuccessful + 1;
            }
        }

        @Override
        public void onCardDataRead(byte[] cardData) throws RemoteException {
            runUIFrash(3, "Card Data: " + (cardData == null ? "null" : HexUtil.bcd2str(cardData)));
        }

        @Override
        public void onFindSamCardStart() throws RemoteException {
            runUIFrash(3, "寻SAM卡====》》》");
        }

        @Override
        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
            LogUtils.e("MSR 寻卡 onResult"+mItemName + getResources().getString(R.string.succeed));
            if (errorCode == ErrorCode.SUCCESS) {
                runUIFrash(3, mItemName + getResources().getString(R.string.succeed));
                if (iSearchCardType == 1) {

                    if (iSearCardType1DataNull){

                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else {

                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",-1);
                            jsonObjectMsg.put("errorCode",errorCode);
                            jsonObjectMsg.put("errorMsg","磁道信息为null");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else {
                    jsonObjectMsg = new JSONObject();
                    try {
                        jsonObjectMsg.put("success",0);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                jsonObjectMsg = new JSONObject();
                try {
                    jsonObjectMsg.put("success",-1);
                    jsonObjectMsg.put("errorCode",errorCode);
                    jsonObjectMsg.put("errorMsg",errorMsg);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                iUnsuccessful = iUnsuccessful + 1;
                runUIFrash(1, "Error Code: " + errorCode + "  Sub Error Code: " + subErrorCode
                        + "\nError Msg: " + errorMsg);
            }
            runUIFrash(3, mItemName + ":" + getResources().getString(R.string.succeed));
            mCardBinderService.closeFindCardForEMV();
            if (iSearchCardType == 1) {
                fileStorage.appendFile("pressMSR.txt",jsonObjectMsg.toString());

                LogUtils.e("iSearchCardType : " + iSearchCardType);
            }
            if (iSearchCardType == 2) {
                fileStorage.appendFile("pressIC.txt",jsonObjectMsg.toString());
            }
            if (iSearchCardType == 4) {
                fileStorage.appendFile("pressNFC.txt",jsonObjectMsg.toString());
            }
            String strDone = fileStorage.readFileSync("pressureDone.txt");
            int iDone = Integer.parseInt(strDone)+1;
            fileStorage.writeFile("pressureDone.txt",String.valueOf(iDone));
            String  strpressureNeedsRun =fileStorage.readFileSync("pressureNeedsRun.txt");
            runUIFrash(2, getResources().getString(R.string.task_count) + iDone+"/"+Integer.parseInt(strpressureNeedsRun ));
            if (iDone < Integer.parseInt(strpressureNeedsRun )) {
                if (iSearchCardType == 1) {
                    runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            msrCardTest("MSR Card Test");
                        }
                    }, iIntervalSeconds);
                } else if (iSearchCardType == 2) {
                    runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            icCardTest("IC Card Test");
                        }
                    }, iIntervalSeconds);


                } else if (iSearchCardType == 4) {
                    runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                    Timer timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            nfcCardTest("NFC Card Test");
                        }
                    }, iIntervalSeconds);
                }
            } else {
                isTaskRuning = false;
            }
        }
    }

    /**
     * IC卡压力测试
     *
     * @param itemName
     */
    private void icCardTest(String itemName) {
        runUIFrash(2, itemName);
        runUIFrash(3, getResources().getString(R.string.start_looking_for_a_card));

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCardBinderService == null) {
                        return;
                    }
                    int cardType = mCardBinderService.openFindCardForEMV(2, FIND_CARD_TIMEOUT);
                    LogUtils.e("icCardTest: cardType ：" + cardType);

                    String cardNumber = emvKernelProcess(cardType);
                    runUIFrash(3, "cardNumber : " + cardNumber);

                    String strDone = fileStorage.readFileSync("pressureDone.txt");
                    int iDone = Integer.parseInt(strDone)+1;
                    fileStorage.writeFile("pressureDone.txt",String.valueOf(iDone));
                    String  strpressureNeedsRun =fileStorage.readFileSync("pressureNeedsRun.txt");

                    if (cardType == 2) {
                        if (cardNumber.length() > 6) {
                            jsonObjectMsg = new JSONObject();
                            try {
                                jsonObjectMsg.put("success",0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            fileStorage.appendFile("pressIC.txt",jsonObjectMsg.toString());
                            runUIFrash(2, getResources().getString(R.string.task_success));
                        } else {
                            jsonObjectMsg = new JSONObject();
                            try {
                                jsonObjectMsg.put("success",-1);
                                jsonObjectMsg.put("errorCode",cardType);
                                jsonObjectMsg.put("errorMsg","card number is null");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            fileStorage.appendFile("pressIC.txt",jsonObjectMsg.toString());
                            runUIFrash(1, getResources().getString(R.string.task_failed)+cardType);
                        }
                    } else {
                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",-1);
                            jsonObjectMsg.put("errorCode",cardType);
                            jsonObjectMsg.put("errorMsg","card number is null");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileStorage.appendFile("pressIC.txt",jsonObjectMsg.toString());
                        runUIFrash(1, getResources().getString(R.string.task_failed)+cardType);
                    }

                    runUIFrash(3, getResources().getString(R.string.task_count) + iDone+"/"+Integer.parseInt(strpressureNeedsRun));
                    runUIFrash(2, "**************************************");
                    if (iDone < Integer.parseInt(strpressureNeedsRun)) {
                        runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                icCardTest("Serach card for IC:");
                            }
                        }, iIntervalSeconds);
                    } else {
                        isTaskRuning = false;
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                    runUIFrash(1, itemName + e.getMessage());
                }
            }
        });
        thread.start();

    }

    /**
     * 非接卡压力测试
     *
     * @param itemName
     */
    private void nfcCardTest(String itemName) {
        runUIFrash(3, getResources().getString(R.string.start_looking_for_a_card));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCardBinderService == null) {
                        return;
                    }
                    int cardType = mCardBinderService.openFindCardForEMV(4, FIND_CARD_TIMEOUT);
                    String cardNumber = emvKernelProcess(cardType);
                    runUIFrash(3, "cardNumber : " + cardNumber);
                    mCardBinderService.closeFindCardForEMV();

                    String strDone = fileStorage.readFileSync("pressureDone.txt");
                    int iDone = Integer.parseInt(strDone)+1;
                    fileStorage.writeFile("pressureDone.txt",String.valueOf(iDone));
                    String  strpressureNeedsRun =fileStorage.readFileSync("pressureNeedsRun.txt");

                    if (cardType == 4) {
                        if (cardNumber.length() > 6) {
                            jsonObjectMsg = new JSONObject();
                            try {
                                jsonObjectMsg.put("success",0);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            fileStorage.appendFile("pressNFC.txt",jsonObjectMsg.toString());
                            runUIFrash(2, getResources().getString(R.string.task_success));
                        } else {
//                            iUnsuccessful = iUnsuccessful + 1;
                            jsonObjectMsg = new JSONObject();
                            try {
                                jsonObjectMsg.put("success",-1);
                                jsonObjectMsg.put("errorCode",cardType);
                                jsonObjectMsg.put("errorMsg","card number is null");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            fileStorage.appendFile("pressNFC.txt",jsonObjectMsg.toString());
                            runUIFrash(1, getResources().getString(R.string.task_failed)+cardType);
                        }
                    } else {
                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",-1);
                            jsonObjectMsg.put("errorCode",cardType);
                            jsonObjectMsg.put("errorMsg","card number is null");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileStorage.appendFile("pressNFC.txt",jsonObjectMsg.toString());
                        runUIFrash(1, getResources().getString(R.string.task_failed)+cardType);
                    }
                    runUIFrash(3, getResources().getString(R.string.task_count) + iDone+"/"+ Integer.parseInt(strpressureNeedsRun));
                    runUIFrash(2, "**************************************");
                    if (iDone < Integer.parseInt(strpressureNeedsRun)) {
                        runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                nfcCardTest("Serach card for IC:");
                            }
                        }, iIntervalSeconds);

                    } else {
                        isTaskRuning = false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    runUIFrash(1, itemName + e.getMessage());
                }
            }
        });
        thread.start();
    }


 // ************************************ ***************************************

    private void printerTest(String itemName) {
        runUIFrash(2, itemName);
        try {
            if (mPrinterBinderService == null) {
                Toast.makeText(mContext, getResources().getString(R.string.print_serviceerror), Toast.LENGTH_LONG).show();
                return;
            }

//            Bitmap blackWhitImage;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            Bitmap blackWhitImage1 = null;
            if (iNumberGrid != 0) {
                blackWhitImage1 = DivBitmapUtils.createCustomBitmap(displayMetrics,
                        384f, 9.6f, 9.6f, iNumberGrid);
                blackWhitImage1 = Bitmap.createScaledBitmap(blackWhitImage1, 384, blackWhitImage1.getHeight() * 384 / blackWhitImage1.getWidth(), false);
            }
            Bitmap printBmp;
            if (blackWhitImage1 != null) {
                Bitmap textBmp = creatTextBitmap(printMsg(), 384, 1500);
 // bitmap bitmap
                printBmp = BitmapUtil.addBitmap(blackWhitImage1, textBmp);
            } else {
                printBmp = creatTextBitmap(printMsg(), 384, 1500);
            }
            runUIFrash(3, "bmp width: " + printBmp.getWidth() + "." + "\n" + "bmp height: " + printBmp.getHeight() + ".");
            int x = 0;
            int width = printBmp.getWidth();
            int height = printBmp.getHeight() + 30;
            int bitW = PrintUtil.getPaddingBitWidth(width);
            byte[] bitmapData = PrintUtil.getBitmapData(printBmp);
            Log.e("szaf", "printerTest 发送打印请求接口: ");
            long timestampStart = System.currentTimeMillis();
            mPrinterBinderService.printBitmap(x, bitmapData, bitW, height, new IPrinterCallback.Stub() {
                @Override
                public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                    long timestampEnd = System.currentTimeMillis();

                    long diffInMills = timestampEnd - timestampStart;
 // ( )
                    double durationSeconds = diffInMills / 1000.0;

 // ( / )
                    double speed = height / durationSeconds;

                    final PrintParm printParm = new PrintParm();
                    printParm.setHeight(printBmp.getHeight());
                    printParm.setDiffInMills(durationSeconds);

                    runUIFrash(2, "打印时耗时" + durationSeconds);
                    runUIFrash(2, "打印速度：" + speed + "像素/秒");
                    LogUtils.e("SDKTest打印时间差：" + diffInMills);


                    String strDone = fileStorage.readFileSync("pressureDone.txt");
                    int iDone = Integer.parseInt(strDone)+1;
                    fileStorage.writeFile("pressureDone.txt",String.valueOf(iDone));
                    String  strpressureNeedsRun =fileStorage.readFileSync("pressureNeedsRun.txt");

                    boolean bCharging = BatteryUtils.fBatteryCharging(mContext);
                    float fBatterLevel = BatteryUtils.fBatteryLevel(mContext);
                    String sn="";
                    try {
                        sn = mSystemBinderService.getSN();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    String strlibVersion = "";
                    String strspHardwareVer="";
                    int printDepth =0;
                    try {
                        byte[] libVersion = mSystemBinderService.readSerialLibVersion(0);
                        strlibVersion = " :" + (libVersion == null ? "null" : new String(libVersion));
                        byte[] spHardwareVer = mSystemBinderService.readSpVersion(1);
                        strspHardwareVer = " :" + (spHardwareVer == null ? "null" : new String(spHardwareVer));
                        printDepth = mPrinterBinderService.dalThermalPrintGetDepth();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    runUIFrash(3,"Charging : "+bCharging);
                    runUIFrash(3,"BatterLevel : "+fBatterLevel);
                    if (errorCode == ErrorCode.SUCCESS) {
                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",0);
                            jsonObjectMsg.put("bCharging",bCharging);
                            jsonObjectMsg.put("BatterLevel",fBatterLevel);
                            jsonObjectMsg.put("sn",sn);
                            jsonObjectMsg.put("strlibVersion",strlibVersion);
                            jsonObjectMsg.put("strspHardwareVer",strspHardwareVer);
                            jsonObjectMsg.put("printDepth",printDepth);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileStorage.appendFile("pressPrint.txt",jsonObjectMsg.toString());
                        runUIFrash(3, itemName + ": " + getResources().getString(R.string.printesucceed));


                        runUIFrash(2, "**************************************");
                        if (iDone < Integer.parseInt(strpressureNeedsRun)) {
                            try {
                                runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                                Thread.sleep(1000);
                                Timer timer = new Timer();
                                timer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        printerTest(getResources().getString(R.string.task_print));
                                    }
                                }, iIntervalSeconds);

                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            isTaskRuning = false;
                        }
                    } else {
                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",-1);
                            jsonObjectMsg.put("errorCode",errorCode);
                            jsonObjectMsg.put("errorMsg",errorMsg);
                            jsonObjectMsg.put("bCharging",bCharging);
                            jsonObjectMsg.put("BatterLevel",fBatterLevel);
                            jsonObjectMsg.put("sn",sn);
                            jsonObjectMsg.put("strlibVersion",strlibVersion);
                            jsonObjectMsg.put("strspHardwareVer",strspHardwareVer);
                            jsonObjectMsg.put("printDepth",printDepth);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        fileStorage.appendFile("pressPrint.txt",jsonObjectMsg.toString());
                        runUIFrash(1, "Error Code: " + errorCode + "\n" +
                                "Sub Error Code: " + subErrorCode + "\n" +
                                "Error Msg: " + errorMsg);
                        runUIFrash(2, "**************************************");
                        if (iDone < Integer.parseInt(strpressureNeedsRun)) {
                            try {
                                Thread.sleep(1000);
                                printerTest(getResources().getString(R.string.task_print));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            isTaskRuning = false;
                        }
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            LogUtils.e("Print :" + itemName + e.getMessage());
            runUIFrash(1, itemName + e.getMessage());
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
        canvas.translate(0, height / 10);
        sl.draw(canvas);
        return newBitmap;
    }

    private String printMsg() {
        long currentDate = DateUtil.getTimeStamp(new Date(), 0);
        String date_time = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT, 0);
        String refId = DateUtil.getTimeStampString(currentDate, DateUtil.DATE_TIME_FORMAT_NO, 0) + generateRandomNumber(4);
        sbMsg.setLength(0);
        String deviceMSG = "";
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
            strlibVersion = " :" + (libVersion == null ? "null" : new String(libVersion));
            byte[] bBootVersion = mSystemBinderService.readSpVersion(1);
            strBootVersion = " :" + (bBootVersion == null ? "null" : new String(bBootVersion));
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
            sbMsg.append("Boot Version ： " +strBootVersion+ "\n\n");
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
            sbMsg.append("Boot Version ： " +strBootVersion+ "\n\n");
            sbMsg.append("printDepth ： " +printDepth+ "\n\n");
        }
        Log.i("PrinterServiceActivity", "printMsg: " + sbMsg.toString());
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
        Log.i("PrinterServiceActivity", "generateRandomNumber: " + sb.toString());
        return sb.toString();
    }
 // ************************************ ***************************************

    /**
     * 蜂鸣器压力测试
     *
     * @param itemName
     */
    private void buzzerTest(String itemName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                runUIFrash(2, itemName);
                try {
                    if (mSystemBinderService == null) {
                        return;
                    }
                    int ret = mSystemBinderService.setBeepStatus(true, 9600, 9600, 1000);
                    mCardBinderService.closeFindCardForEMV();


                    String strDone = fileStorage.readFileSync("pressureDone.txt");
                    int iDone = Integer.parseInt(strDone)+1;
                    fileStorage.writeFile("pressureDone.txt",String.valueOf(iDone));
                    String  strpressureNeedsRun =fileStorage.readFileSync("pressureNeedsRun.txt");

                    if (ret == 0) {
                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        runUIFrash(2, getResources().getString(R.string.task_success)  );
                    } else {
                        jsonObjectMsg = new JSONObject();
                        try {
                            jsonObjectMsg.put("success",-1);
                            jsonObjectMsg.put("errorCode",ret);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        runUIFrash(1, getResources().getString(R.string.task_failed) );
                    }
                    fileStorage.appendFile("pressBuzzer.txt", jsonObjectMsg.toString());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runUIFrash(3, getResources().getString(R.string.task_count) + iDone + "/"+Integer.parseInt(strpressureNeedsRun) );
                    runUIFrash(2, "**************************************");
                    if (iDone < Integer.parseInt(strpressureNeedsRun)) {
                        runUIFrash(3, "The time interval for task execution" + iIntervalSeconds / 1000 + "S");
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                buzzerTest(itemName);
                            }
                        }, iIntervalSeconds);

                    } else {
                        isTaskRuning = false;
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    runUIFrash(1, itemName + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * 闪光灯压力测试
     *
     * @param enable
     */
    private void setFrontFlashStatus(boolean enable) {
        if (enable) {
            FlashlightCompat.INSTANCE.turnOn(mContext);
        } else {
            runUIFrash(3, "Turn off not only the light");
            isTaskRuning = false;
            FlashlightCompat.INSTANCE.turnOff(mContext);
        }
    }

    @Override
    protected void onDestroy() {
        try {
            mCardBinderService.closeFindCardForEMV();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
 // LCD
        if (mLcdStressRunner != null) {
            mLcdStressRunner.stop();
            mLcdStressRunner = null;
        }
        super.onDestroy();
        WatermarkUtils.removeWatermark(this);
    }

    @Override
    public void onBackPressed() {

        if (!isTaskRuning) {
            LogUtils.e("任务执行完毕，页面能关闭: ");
            super.onBackPressed();
        } else {
            LogUtils.e("任务未执行完毕，页面不能关闭 ");
            Toast.makeText(mContext, "任务未执行完毕，页面不能关闭", Toast.LENGTH_LONG).show();
            return;
        }
    }

    /**
     * 配置交易参数
     * Configure EMV transaction parameters
     * 卡片类型 2：IC卡 4：非接卡
     */
    private String emvKernelProcess(int cardType) {
        try {
            //emv init
            mEmvBinderService.emvKernelInit();
            mEmvBinderService.apiEmvKernelJniPbocLogswitch(1);
            String sbEmvParam = "";
            if (2 == cardType) {
                sbEmvParam = "01000100010001000000074261726b6579202020202020202020202020202020202020202020202020202020202020202020200000010000000003330000000000000156250729143338313130303030303033353137313737313030303039383524ff6001009f3501229f3303e0c9c89f40056000f0e0019f1a0201565f2a020156df6901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            } else if (4 == cardType) {
                sbEmvParam = "03010100010000000000074261726b6579202020202020202020202020202020202020202020202020202020202020202020200000010000000000330000000000000156250729144732313130303030303033353137313737313030303039383524ff6001009f3501229f3303e0c9c89f40056000f0e0019f1a0201565f2a020156df6901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
            }
            byte[] emvParamByte = AFUtils.hexString2ByteArray(sbEmvParam);
            int kernelProcessResult = mEmvBinderService.apiEmvKernelProcess(emvParamByte);
            cardNumber = getTagValue(0x5A);
            LogUtils.e("cardNumber :" + cardNumber);

            mCardBinderService.closeFindCardForEMV();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return cardNumber;
    }


    /**
     * 根据TAG值获取数据
     *
     * @param tag 数据类型
     * @return 相应域的数据
     */
    private String getTagValue(int tag) {
        byte[] getTLV = null;
        try {
            getTLV = mEmvBinderService.apiKernelEmvGetTlv(tag);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return (getTLV == null ? "" : com.szanfu.sdk.api.util.HexUtil.bcd2str(getTLV));
    }


    private void scannerOpen(String itemName) {
        runUIFrash(3, itemName);
        long timestamp1 = System.currentTimeMillis();
        LogUtils.e("timestamp1--->" + timestamp1);
        try {
            if (FQRScanFlag) {

                LogUtils.e("智能扫码摄像头每次都上电");
                result = mSystemBinderService.scannerOpen();
            } else {

                LogUtils.e("智能扫码摄像头只上电1次");
                if (0 == iIsRunsCount) {
                    LogUtils.e("智能扫码摄像头只上电1次-----》");
                    result = mSystemBinderService.scannerOpen();
                }
            }
            LogUtils.e("scannerOpen result:: " + result);
            runUIFrash(2, "scannerOpen result: " + result);

            if (result == 0) {


                new Thread(() -> {
                    try {
                        while (true) {
                            byte[] responseData = new byte[1024];

                            int scannerGetDataResult = mSystemBinderService.scannerGetData(responseData, 1024 * 4);

                            LogUtils.e("scannerGetDataResult result:: " + scannerGetDataResult);

                            if (scannerGetDataResult > 0) {

                                int ret = mSystemBinderService.setBeepStatus(true, 9600, 9600, 200);

                                byte[] validData = Arrays.copyOf(responseData, scannerGetDataResult);

                                runOnUiThread(() -> {
                                    runUIFrash(3, "scannerGetDataResult DataLen :  " + scannerGetDataResult);
                                    String strValidData = AFUtils.showResult16Str(validData);
                                    runUIFrash(3, "scannerGetDataResult: " + AFUtils.showResult16Str(validData));
                                    runUIFrash(3, "strValidData: " + AFUtils.hexToAscii(strValidData));
                                    LogUtils.i("scannerGetDataResult DataLen : " + scannerGetDataResult);
                                    LogUtils.i("scannerGetDataResult : " + AFUtils.showResult16Str(validData));
                                });


                                long timestamp2 = System.currentTimeMillis();
                                LogUtils.e("<timestamp2---" + timestamp2);

 // ( )
                                long timeDifference = Math.abs(timestamp1 - timestamp2);

 // ( )
                                long seconds = timeDifference / 1000;
                                runUIFrash(2, "Time difference : " + seconds + " S");

                                iRunSuccessful = iRunSuccessful + 1;
                                iIsRunsCount = iRunSuccessful + iUnsuccessful;

                                try {
                                    String status = "";
                                    if (FQRScanFlag) {
                                        status = "智能扫码摄像头每次都上电 needsRun : " + iCountNumberNeedsRun + "; success : " + iRunSuccessful + "; Error : " + iUnsuccessful;
                                    } else {
                                        status = "智能扫码摄像头只上电1次 needsRun : " + iCountNumberNeedsRun + "; success : " + iRunSuccessful + "; Error : " + iUnsuccessful;
                                    }
                                    jsonObjectMsg.put("scanPower", status);
                                    FileHelper.writeToFile(mContext, "pressTest.txt", jsonObjectMsg.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                runUIFrash(1, "222Professional QRScan Count:" + iIsRunsCount + "\n"
                                        + "Professional QRScan Success :" + iRunSuccessful
                                        + "\n" + "Professional QRScan Error:" + iUnsuccessful
                                );
                                if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            scannerOpen(itemName);
                                        }
                                    }, iIntervalSeconds);

                                } else {

                                    scannerClose("Closed Professional QRScan");

                                    isTaskRuning = false;
                                }
                                break;

                            }

 // 500ms
                            Thread.sleep(500);

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            runUIFrash(3, "scannerGetData failed: " + e.getMessage());
                        });

                        iUnsuccessful = iUnsuccessful + 1;
                        iIsRunsCount = iRunSuccessful + iUnsuccessful;


                        String status = "";
                        if (FQRScanFlag) {
                            status = "智能扫码摄像头每次都上电 needsRun : " + iCountNumberNeedsRun + "; success : " + iRunSuccessful + "; Error : " + iUnsuccessful;
                        } else {
                            status = "智能扫码摄像头只上电1次 needsRun : " + iCountNumberNeedsRun + "; success : " + iRunSuccessful + "; Error : " + iUnsuccessful;
                        }
                        try {
                            jsonObjectMsg.put("scanPower", status);
                        } catch (JSONException ex) {
                            ex.printStackTrace();
                        }
                        FileHelper.writeToFile(mContext, "pressTest.txt", jsonObjectMsg.toString());


                        runUIFrash(1, "111Professional QRScan Count:" + iIsRunsCount + "\n"
                                + "Professional QRScan Success :" + iRunSuccessful
                                + "\n" + "Professional QRScan Error:" + iUnsuccessful
                        );
                        if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    scannerOpen(itemName);
                                }
                            }, iIntervalSeconds);

                        } else {

                            scannerClose("Closed Professional QRScan");

                            isTaskRuning = false;
                        }
                    }
                }).start();


            } else {
                LogUtils.e("********************扫码失败********************");

            }

        } catch (Exception e) {
            e.printStackTrace();
            iUnsuccessful = iUnsuccessful + 1;
            iIsRunsCount = iRunSuccessful + iUnsuccessful;

            String status = "";
            if (FQRScanFlag) {
                status = "智能扫码摄像头每次都上电 needsRun : " + iCountNumberNeedsRun + "; success : " + iRunSuccessful + "; Error : " + iUnsuccessful;
            } else {
                status = "智能扫码摄像头只上电1次 needsRun : " + iCountNumberNeedsRun + "; success : " + iRunSuccessful + "; Error : " + iUnsuccessful;
            }
            try {
                jsonObjectMsg.put("scanPower", status);
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            FileHelper.writeToFile(mContext, "pressTest.txt", jsonObjectMsg.toString());


            runUIFrash(1, "333Professional QRScan Count:" + iIsRunsCount + "\n"
                    + "Professional QRScan Success :" + iRunSuccessful
                    + "\n" + "Professional QRScan Error:" + iUnsuccessful
            );

            if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        scannerOpen(itemName);
                    }
                }, iIntervalSeconds);
            } else {

                scannerClose("Closed Professional QRScan");
                isTaskRuning = false;
            }


            runUIFrash(3, "scannerOpen failed: " + e.getMessage());
        }
    }


    private void scannerClose(String itemName) {
        runUIFrash(3, itemName);
        try {
            int result = mSystemBinderService.scannerClose();
            runUIFrash(3, "scannerClose result: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            runUIFrash(3, "scannerClose failed: " + e.getMessage());
        }
    }

    public void showSingleAlertDialog() {
        int retDepth = 0;
        final String[] items = {"Set Number of rows in the grid:0"
                , "Set Number of rows in the grid:1"
                , "Set Number of rows in the grid:2"
                , "Set Number of rows in the grid:3"
                , "Set Number of rows in the grid:4"
                , "Set Number of rows in the grid:5"
                , "Set Number of rows in the grid:6"
                , "Set Number of rows in the grid:7"
                , "Set Number of rows in the grid:8"
                , "Set Number of rows in the grid:9"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Set the printer printing dept");
        alertBuilder.setSingleChoiceItems(items, retDepth, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(PressureActivity.this, items[i], Toast.LENGTH_SHORT).show();
                iNumberGrid = i;
                LogUtils.e("iNumberGrid : " + iNumberGrid);
                alertDialog.dismiss();

                if (et_interval.getText().toString().trim().isEmpty()) {
                    iIntervalSeconds = 1000;
                } else {
                    iIntervalSeconds = 1000 * Integer.parseInt(String.valueOf(et_interval.getText().toString().trim()));
                }

                if (!isTaskRuning) {
                    isTaskRuning = true;
                    fileStorage.deleteFile("pressPrint.txt");
                    fileStorage.writeFile("pressureDone.txt","0");
                    fileStorage.writeFile("pressureNeedsRun.txt",String.valueOf(iCountNumberNeedsRun));

                    printerTest("Print");
                    Toast.makeText(mContext, getResources().getString(R.string.task_pressure_backup), Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(mContext, getResources().getString(R.string.task_pressure), Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }


    /**
     * LCD Fill 压测入口：按次数 / 按时间 / 停止
     */
    private void showLcdFillStressMenu(String itemName) {
        runUIFrash(2, itemName);
        final String[] items = new String[]{
                t("Start by Count (input count)", "按次数开始（输入次数）"),
                t("Start by Time (input seconds)", "按时间开始（输入秒数）"),
                t("Stop Stress", "停止压测")
        };

        new AlertDialog.Builder(this)
                .setTitle(t("LCD Fill Stress", "LCD 填充压测"))
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        showInputNumberDialog(t("Input Count", "输入次数"),
                                t("e.g. 1000", "例如：1000"),
                                value -> startLcdFillStressByCount(itemName, value));
                    } else if (which == 1) {
                        showInputNumberDialog(t("Input Seconds", "输入秒数"),
                                t("e.g. 60", "例如：60"),
                                value -> startLcdFillStressByTimeSeconds(itemName, value));
                    } else {
                        stopLcdFillStress(itemName);
                    }
                })
                .setCancelable(true)
                .show();
    }

    /**
     * 只允许输入数字的通用输入框
     */
    private interface OnNumberConfirm {
        void onConfirm(long value);
    }

    private void showInputNumberDialog(String title, String hint, OnNumberConfirm cb) {
        final EditText et = new EditText(this);
        et.setHint(hint);
        et.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(et)
                .setPositiveButton(t("OK", "确定"), (d, w) -> {
                    String s = et.getText() == null ? "" : et.getText().toString().trim();
                    if (TextUtils.isEmpty(s)) {
                        runUIFrash(1, title + ": " + t("empty input", "输入为空"));
                        return;
                    }
                    try {
                        long v = Long.parseLong(s);
                        cb.onConfirm(v);
                    } catch (Throwable t) {
                        runUIFrash(1, title + ": " + t("invalid number", "非法数字"));
                    }
                })
                .setNegativeButton(t("Cancel", "取消"), null)
                .show();
    }

    /**
     * 获取屏幕信息（避免空指针）
     */
    private LcdInfo requireLcdInfo(String itemName) {
        LcdInfo info = mLcdUtils.lcdGetDevLcdStatus();
        if (info == null) {
            runUIFrash(1, itemName + ": lcdGetDevLcdStatus=null");
            return null;
        }
        return info;
    }

    /**
     * 懒加载 Runner：依赖 mLcdUtils + lcdGetDevLcdStatus()
     */
    private LcdFillStressRunner ensureLcdStressRunner(String itemName) {
        LcdInfo info = requireLcdInfo(itemName);
        if (info == null) return null;

        return new LcdFillStressRunner(
                PressureActivity.this,
                new LcdFillStressRunner.LcdUtils() {
                    @Override
                    public int lcdClearScreenWithResult() {
                        return mLcdUtils.lcdClearScreenWithResult();
                    }

                    @Override
                    public int lcdFillScreen(int x1, int y1, int x2, int y2, int color) {
                        return mLcdUtils.lcdFillScreen(x1, y1, x2, y2, color);
                    }
                },
                new LcdFillStressRunner.LcdInfo(info.getType(), info.getWidth(), info.getHeight()),
                msg -> runUIFrash(3, msg)
        );
    }


    /**
     * 按次数开始（测试只输入 count）
     */
    private void startLcdFillStressByCount(String itemName, long count) {
        if (count <= 0) {
            runUIFrash(1, t("Invalid count: ", "次数非法：") + count);
            return;
        }


        if (mLcdStressRunner != null && mLcdStressRunner.isRunning()) {
            mLcdStressRunner.stop();
        }

        mLcdStressRunner = ensureLcdStressRunner(itemName);
        if (mLcdStressRunner == null) return;

        runUIFrash(2, t("Start LCD Stress by COUNT = ", "按次数开始压测：") + count);
        mLcdStressRunner.startStressByCount(count);
    }

    /**
     * 按时间开始
     */
    private void startLcdFillStressByTimeSeconds(String itemName, long seconds) {
        if (seconds <= 0) {
            runUIFrash(1, t("Invalid seconds: ", "秒数非法：") + seconds);
            return;
        }

        if (mLcdStressRunner != null && mLcdStressRunner.isRunning()) {
            mLcdStressRunner.stop();
        }

        mLcdStressRunner = ensureLcdStressRunner(itemName);
        if (mLcdStressRunner == null) return;

        long durationMs = seconds * 1000L;
        runUIFrash(2, t("Start LCD Stress by TIME = ", "按时间开始压测：") + seconds + "s");
        mLcdStressRunner.startStressByTime(durationMs);
    }

    private void stopLcdFillStress(String itemName) {
        runUIFrash(2, t("Stop LCD Stress", "停止 LCD 压测"));
        if (mLcdStressRunner != null) {
            mLcdStressRunner.stop();
        }
    }

    /**
     * 中英文自适配（系统语言 zh -> 中文，否则英文）
     */
    private String t(String en, String zh) {
        try {
            String lang = Locale.getDefault().getLanguage();
            if (lang != null && lang.toLowerCase().startsWith("zh")) return zh;
        } catch (Throwable ignore) {
        }
        return en;
    }


 // Gson , JSON
    private static final Gson GSON = new Gson();
    /**
     * 核心统计方法：传入文件名，完成各项统计并打印结果
     * @param fileName 待读取的文件名（如：pressIC），方法内会自动拼接.txt后缀
     */
    public void statisticsFileJsonInfo(String fileName) {
 // 1. , ( fileStorage.readFileSync )
        String strContext = fileStorage.readFileSync(fileName);

        if (strContext == null || strContext.trim().isEmpty()) {
            System.out.println("文件内容为空，无法进行统计！");
            return;
        }


        int successTotalCount = 0; // success
        int errorCodeTotalCount = 0; // errorCode
 // key: errorCode ; value: [0] = , [1] = errorMsg
        Map<String, Object[]> errorCodeMap = new HashMap<>();

 // 3. JSON ( JSON : {"a"}{"b"} ["{\"a\"}", "{\"b\"}"])
        String[] jsonArray = splitMultiJson(strContext);

 // 4. JSON
        for (String jsonStr : jsonArray) {
            try {
 // JSON
                JsonObject jsonObject = GSON.fromJson(jsonStr, JsonObject.class);

 // success
                if (jsonObject.has("success")) {
                    successTotalCount++;
                }

 // errorCode errorMsg
                if (jsonObject.has("errorCode")) {
                    errorCodeTotalCount++;
 // errorCode errorMsg
                    String errorCode = jsonObject.get("errorCode").getAsString();
                    String errorMsg = jsonObject.has("errorMsg") ? jsonObject.get("errorMsg").getAsString() : "无错误描述";

 // errorCodeMap
                    if (errorCodeMap.containsKey(errorCode)) {
 // errorCode, ( +1)
                        Object[] valueArr = errorCodeMap.get(errorCode);
                        int count = (int) valueArr[0] + 1;
                        errorCodeMap.put(errorCode, new Object[]{count, errorMsg});
                    } else {
 // errorCode, 1
                        errorCodeMap.put(errorCode, new Object[]{1, errorMsg});
                    }
                }
            } catch (Exception e) {
                LogUtils.e("解析JSON片段[" + jsonStr + "]失败，跳过该片段，异常信息：" + e.getMessage());
            }
        }


        printStatisticsResult(fileName,successTotalCount, errorCodeTotalCount, errorCodeMap);
    }



    /**
     * 拆分多个拼接的JSON对象，处理格式：{"a"}{"b"} -> ["{\"a\"}", "{\"b\"}"]
     * @param strContext 原始文件内容
     * @return 拆分后的单个JSON字符串数组
     */
    private static String[] splitMultiJson(String strContext) {

        String trimContext = strContext.trim();
 // "}{" , JSON
        String[] tempArray = trimContext.split("\\}\\{");
 // JSON , JSON
        for (int i = 0; i < tempArray.length; i++) {
            if (i == 0) {
 // JSON : "}"
                tempArray[i] = tempArray[i] + "}";
            } else if (i == tempArray.length - 1) {
 // JSON : "{"
                tempArray[i] = "{" + tempArray[i];
            } else {
 // JSON : "{", "}"
                tempArray[i] = "{" + tempArray[i] + "}";
            }
        }
        return tempArray;
    }

    /**
     * 打印最终的统计结果
     * @param successTotalCount success总个数
     * @param errorCodeTotalCount errorCode总个数
     * @param errorCodeMap errorCode统计Map
     */
    private  void printStatisticsResult(String fileName,int successTotalCount, int errorCodeTotalCount, Map<String, Object[]> errorCodeMap) {
        runUIFrash(2,"***"+fileName+"***");
        runUIFrash(3," success 字段总个数：" + successTotalCount);
        runUIFrash(1," errorCode 字段总个数：" + errorCodeTotalCount);
        runUIFrash(1," 相同 errorCode 统计（errorCode -> 出现次数 | 错误描述）：");
        if (errorCodeMap.isEmpty()) {
            System.out.println("   无有效 errorCode 信息");
        } else {
            for (Map.Entry<String, Object[]> entry : errorCodeMap.entrySet()) {
                String errorCode = entry.getKey();
                int count = (int) entry.getValue()[0];
                String errorMsg = (String) entry.getValue()[1];
                runUIFrash(1,"   errorCode: " + errorCode + " -> 出现次数：" + count + " | 错误描述：" + errorMsg);
            }
        }
        System.out.println("==================================================\n");
    }

}
