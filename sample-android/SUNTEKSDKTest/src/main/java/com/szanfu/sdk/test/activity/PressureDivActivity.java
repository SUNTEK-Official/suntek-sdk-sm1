package com.szanfu.sdk.test.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.card.ICardBinderService;
import com.szanfu.sdk.api.card.ICardCscIsoCmdResult;
import com.szanfu.sdk.api.card.ICardDataResult;
import com.szanfu.sdk.api.card.ICardTestCallback;
import com.szanfu.sdk.api.card.ICscDetectResult;
import com.szanfu.sdk.api.card.ICscGetATSResult;
import com.szanfu.sdk.api.card.ISciGetATRResult;
import com.szanfu.sdk.api.card.ISciIsoCmdResult;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.emv.DalcscIsoMifareCmdResult;
import com.szanfu.sdk.api.emv.IEmvBinderService;
import com.szanfu.sdk.api.error.ErrorCode;
import com.szanfu.sdk.api.printer.IPrinterBinderService;
import com.szanfu.sdk.api.printer.IPrinterCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.BaseAppActivity;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.AFUtils;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.BitmapUtil;
import com.szanfu.sdk.test.util.CardSearchType;
import com.szanfu.sdk.test.util.DateUtil;
import com.szanfu.sdk.test.util.HexSupport;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.PrintUtil;
import com.szanfu.sdk.test.util.SingleClickUtil;

import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static android.graphics.Bitmap.createBitmap;

public class PressureDivActivity extends BaseAppActivity {
    private static final String TAG = "PressureDivActivity";

    private EditText ed_conut;
    private int iCountNumberNeedsRun = 0;
    private int iRunSuccessful = 0;
    private int iUnsuccessful = 0;
    private int iIsRunsCount = 0;
    private int iSearchCardType = 1;

    private String[] mButtons;

    public static final int FIND_CARD_TIMEOUT = 10;
    private IEmvBinderService mIEmvBinderService;
    private ICardBinderService mCardBinderService;
    private IPrinterBinderService mPrinterBinderService;
    private ISystemBinderService mSystemBinderService;
    private boolean mMifareCardOperationMode;
    private DalcscIsoMifareCmdResult dalcscIsoMifareCmdResult;
    private static Bitmap imgBmp;
    private Button btn_msr_card, btn_ic_card, btn_nfc_card, clear_text, btn_print,btn_busser;
    private boolean isBold = false;
    StringBuffer sbMsg = new StringBuffer();
    boolean isClosedActivity = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.PRESSURE_SERVICE_LIST);
        setButtonDataSource(mButtons);
        ed_conut = findViewById(R.id.ed_conut);

        btn_msr_card = findViewById(R.id.btn_msr_card);
        btn_ic_card = findViewById(R.id.btn_ic_card);
        btn_nfc_card = findViewById(R.id.btn_nfc_card);
        clear_text = findViewById(R.id.clear_text);
        btn_print = findViewById(R.id.btn_print);
        btn_busser = findViewById(R.id.btn_busser);
        Toast.makeText(mContext, "进行压力测试过程中请无退出该页面，如误操作退出该页面，再次进入将重新计数；", Toast.LENGTH_SHORT).show();


        btn_msr_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosedActivity = false;
                Log.e(TAG, "onClick isClosedActivity: "+isClosedActivity );
                if (!SingleClickUtil.isFastClick()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setButtonEnableFalse();
                        }
                    });

                    initData();
                    iSearchCardType = 1;
                    try {
                        Thread.sleep(500);
                        msrCardTest(btn_msr_card.getText().toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        btn_ic_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosedActivity = false;
                Log.e(TAG, "onClick isClosedActivity: "+isClosedActivity );
                if (!SingleClickUtil.isFastClick()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setButtonEnableFalse();
                        }
                    });

                    iSearchCardType = 2;
                    initData();
                    try {
                        Thread.sleep(500);
                        icCardTest("Serach card for IC:");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        btn_nfc_card.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosedActivity = false;
                Log.e(TAG, "onClick isClosedActivity: "+isClosedActivity );
                if (!SingleClickUtil.isFastClick()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            appendText(btn_nfc_card.getText().toString()+":");
                            setButtonEnableFalse();
                        }
                    });
                    iSearchCardType = 4;
                    initData();
                    try {
                        Thread.sleep(500);
                        rfCardTest(btn_nfc_card.getText().toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosedActivity = false;
                Log.e(TAG, "onClick isClosedActivity: "+isClosedActivity );
                if (!SingleClickUtil.isFastClick()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setButtonEnableFalse();
                        }
                    });
                    initData();
                    try {
                        Thread.sleep(1000);
                        printerTest(btn_print.getText().toString());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        btn_busser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isClosedActivity = false;
                Log.e(TAG, "onClick isClosedActivity: "+isClosedActivity );
                if (!SingleClickUtil.isFastClick()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setButtonEnableFalse();
                        }
                    });
                    initData();

                    buzzerTest(btn_busser.getText().toString());
                }
            }
        });

    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
            mCardBinderService = ICardBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_CARD));
            mIEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
            mPrinterBinderService = IPrinterBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PRINTER));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        if (ed_conut.getText().toString().trim().isEmpty()) {
            iCountNumberNeedsRun = 1;
        } else {
            iCountNumberNeedsRun = Integer.parseInt(String.valueOf(ed_conut.getText().toString().trim()));
        }
        iIsRunsCount = 0;
        iRunSuccessful = 0;
        iUnsuccessful = 0;
    }

    /**
     * 磁条卡压力测试
     * @param itemName
     */
    private void msrCardTest(String itemName) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                try {
                    mCardBinderService.testMSRCard(FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }

            }
        });
        thread.start();
    }

    /**
     * IC卡压力测试
     * @param itemName
     */
    private void icCardTest(String itemName) {
        appendText(btn_ic_card.getText().toString()+":");
        appendText(getResources().getString(R.string.start_looking_for_a_card));

                Thread thread=new Thread(new Runnable() {
                    @Override
                    public void run() {
//                        appendText(itemName);
                        try {
                            if (mCardBinderService == null){

                                return;
                            }
                            int cardType = mCardBinderService.openFindCardForEMV(2, FIND_CARD_TIMEOUT);
                            Log.e(TAG, "icCardTest: cardType ：" + cardType);
                            mCardBinderService.closeFindCardForEMV();
                            if (cardType == 2) {
                                iRunSuccessful = iRunSuccessful + 1;
                            } else {
                                iUnsuccessful = iUnsuccessful + 1;
                            }
                            appendText(getResources().getString(R.string.task_success) + iRunSuccessful + "\n");
                            appendText(getResources().getString(R.string.task_failed) + iUnsuccessful + "\n");
                            iIsRunsCount = iRunSuccessful + iUnsuccessful;
                            appendText(getResources().getString(R.string.task_count) + iIsRunsCount + "\n");
                            appendText("**************************************");
                            if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                                icCardTest("Serach card for IC:");
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        setButtonEnableTrue();
                                    }
                                });
                            }

                        } catch (RemoteException e) {
                            e.printStackTrace();
                            appendErrorText(itemName, e.getMessage());
                        }
                    }
                });
                thread.start();

    }

    /**
     * 非接卡压力测试
     * @param itemName
     */
    private void rfCardTest(String itemName) {
        appendText(getResources().getString(R.string.start_looking_for_a_card));
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mCardBinderService == null){
                        return;
                    }
                    int cardType = mCardBinderService.openFindCardForEMV(4, FIND_CARD_TIMEOUT);
                    mCardBinderService.closeFindCardForEMV();
                    if (cardType == 4) {
                        iRunSuccessful = iRunSuccessful + 1;
                    } else {
                        iUnsuccessful = iUnsuccessful + 1;
                    }
                    appendText(getResources().getString(R.string.task_success) + iRunSuccessful + "\n");
                    appendText(getResources().getString(R.string.task_failed) + iUnsuccessful + "\n");
                    iIsRunsCount = iRunSuccessful + iUnsuccessful;
                    appendText(getResources().getString(R.string.task_count) + iIsRunsCount + "\n");
                    appendText("openFindCardForEMV : " + cardType);
                    appendText("**************************************");
                    if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                        rfCardTest("Serach card for IC:");
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setButtonEnableTrue();
                            }
                        });
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
        thread.start();
    }

    /**
     * 蜂鸣器压力测试
     * @param itemName
     */
    private void buzzerTest(String itemName) {
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                appendText(itemName);
                try {
                    if (mSystemBinderService == null){
                        return;
                    }
                    int ret = mSystemBinderService.setBeepStatus(true, 9600, 9600, 1000);
                    mCardBinderService.closeFindCardForEMV();
                    if (ret == 0) {
                        iRunSuccessful = iRunSuccessful + 1;
                    } else {
                        iUnsuccessful = iUnsuccessful + 1;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    appendText(getResources().getString(R.string.task_success) + iRunSuccessful + "\n");
                    appendText(getResources().getString(R.string.task_failed) + iUnsuccessful + "\n");
                    iIsRunsCount = iRunSuccessful + iUnsuccessful;
                    appendText(getResources().getString(R.string.task_count) + iIsRunsCount + "\n");
                    appendText("**************************************");
                    if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                        buzzerTest(itemName);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                setButtonEnableTrue();
                            }
                        });
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
        }
    }

    private class ICardTestCallbackProxy extends ICardTestCallback.Stub {
        private final String mItemName;

        private ICardTestCallbackProxy(String itemName) {
            mItemName = itemName;
        }

        @Override
        public void onFindCardStart() throws RemoteException {
            appendText(getResources().getString(R.string.start_looking_for_a_card));
        }

        @Override
        public void onFindCardSuccess(int cardType) throws RemoteException {
            if (cardType == 8){
                iUnsuccessful=iUnsuccessful+1;
            }
            appendText(getResources().getString(R.string.the_card_type_is_searched_successfully) + cardType);
            int ret = mIEmvBinderService.apiDALCscIsMifare();
            Log.e(TAG, "onFindCardSuccess ret: "+ret );
            appendText("apiDALCscIsMifare ret : " + ret);
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
                    Log.e(TAG, "onFindCardSuccess: Write data.... ");
                    operateData = AFUtils.hexString2ByteArray("FF110200");
                    operateDataLen = new int[]{operateData.length};
                    Log.e(TAG, "Write data content: " + AFUtils.showResult0xStr(operateData) + " Write data Len: " + operateDataLen[0]);
                } else {
                    Log.e(TAG, "onFindCardSuccess: Read data.... ");
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
                    appendText("blockNum :" + blockNum);
                    appendText("Received data Len : " + (dalcscIsoMifareCmdResult == null ? "null" : dalcscIsoMifareCmdResult.getM1_opt_data_len()));
                    appendText("Received data : " + (dalcscIsoMifareCmdResult == null ? "null" : HexUtil.bcd2str(m1_opt_dataOut)) + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }


        @Override
        public void onTrackDataRead(byte[] track1Data, byte[] track2Data, byte[] track3Data) throws RemoteException {
            appendText("Track1 Data: " + (track1Data == null ? "null" : (new String(track1Data)).trim()));
            appendText("Track2 Data: " + (track2Data == null ? "null" : (new String(track2Data)).trim()));
            appendText("Track3 Data: " + (track3Data == null ? "null" : (new String(track3Data)).trim()));
            if ((new String(track1Data)).trim().isEmpty()
                    || (new String(track2Data)).trim().isEmpty()
                    || (new String(track3Data)).trim().isEmpty()) {
                iUnsuccessful = iUnsuccessful + 1;
            } else {
                iRunSuccessful = iRunSuccessful + 1;
            }
        }

        @Override
        public void onCardDataRead(byte[] cardData) throws RemoteException {
            appendText("Card Data: " + (cardData == null ? "null" : HexUtil.bcd2str(cardData)));
        }

        @Override
        public void onFindSamCardStart() throws RemoteException {
            appendText("寻SAM卡====》》》");
        }

        @Override
        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
            if (errorCode == ErrorCode.SUCCESS) {
                appendText(mItemName + getResources().getString(R.string.succeed) + "\n");
            } else {
                iUnsuccessful = iUnsuccessful + 1;
                appendText("Error Code: " + errorCode + "  Sub Error Code: " + subErrorCode
                        + "\nError Msg: " + errorMsg + "\n");
            }
            appendText(mItemName + ":" + getResources().getString(R.string.succeed) + "\n");
            appendText(getResources().getString(R.string.task_success) + iRunSuccessful + "\n");
            appendText(getResources().getString(R.string.task_failed) + iUnsuccessful + "\n");
            iIsRunsCount = iRunSuccessful + iUnsuccessful;
            appendText(getResources().getString(R.string.task_count) + iIsRunsCount + "\n");
            mCardBinderService.closeFindCardForEMV();
            appendText("**************************************");

            if (iRunSuccessful + iUnsuccessful < iCountNumberNeedsRun) {
                if (iSearchCardType == 1) {
                    msrCardTest("MSR Card Test");
                } else if (iSearchCardType == 2) {
                    icCardTest("IC Card Test");
                } else if (iSearchCardType == 4) {
                    rfCardTest("NFC Card Test");
                }
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setButtonEnableTrue();
                    }
                });

            }
        }
    }



    private void printerTest(String itemName) {
        appendText(itemName);
        try {
            if (mPrinterBinderService == null) {
                Toast.makeText(mContext,getResources().getString(R.string.print_serviceerror),Toast.LENGTH_LONG).show();
                return;
            }

            Bitmap printBmp;
            if (imgBmp != null) {
                Bitmap textBmp = creatTextBitmap(printMsg(), 384, 950);
 // bitmap bitmap
                printBmp = BitmapUtil.addBitmap(textBmp, imgBmp);
            } else {
                printBmp = creatTextBitmap(printMsg(), 384, 1230);
            }

            appendText("bmp width: " + printBmp.getWidth() + "." + "\n" + "bmp height: " + printBmp.getHeight() + "." + "\n");
            int x = 0;
            int width = printBmp.getWidth();
            int height = printBmp.getHeight() + 30;
            int bitW = PrintUtil.getPaddingBitWidth(width);
            byte[] bitmapData = PrintUtil.getBitmapData(printBmp);
            Log.e("szaf", "printerTest 发送打印请求接口: ");
            mPrinterBinderService.printBitmap(x, bitmapData, bitW, height, new IPrinterCallback.Stub() {
                @Override
                public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
                    if (errorCode == ErrorCode.SUCCESS) {
                        appendText(itemName + ": " + getResources().getString(R.string.printesucceed) + "\n");
                        iRunSuccessful = iRunSuccessful + 1;
                        iIsRunsCount = iRunSuccessful + iUnsuccessful;
                        appendText("Printer Count:" + iIsRunsCount
                                + "\n" + "Printer Success :" + iRunSuccessful
                                + "\n" + "Printer Error:" + iUnsuccessful);
                        appendText("**************************************");
                        if (iIsRunsCount < iCountNumberNeedsRun) {
                            try {
                                Thread.sleep(1000);
                                printerTest(getResources().getString(R.string.task_print));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setButtonEnableTrue();
                                }
                            });

                        }
                    } else {
                        appendText("Error Code: " + errorCode + "\n" +
                                "Sub Error Code: " + subErrorCode + "\n" +
                                "Error Msg: " + errorMsg + "\n");
                        iUnsuccessful = iUnsuccessful + 1;
                        iIsRunsCount = iRunSuccessful + iUnsuccessful;
                        appendText("Printer Count:" + iIsRunsCount + "\n"
                                + "Printer Success :" + iRunSuccessful
                                + "\n" + "Printer Error:" + iUnsuccessful
                                + "\n");
                        appendText("**************************************");

                        if (iIsRunsCount < iCountNumberNeedsRun) {
                            try {
                                Thread.sleep(1000);
                                printerTest(getResources().getString(R.string.task_print));
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setButtonEnableTrue();
                                }
                            });

                        }
                    }
                }
            });
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
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
        if (isBold) {
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
        } else {
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

    @Override
    public void onBackPressed() {

        if (isClosedActivity) {
            Log.e(TAG, "onBackPressed 111111111111: " );
            super.onBackPressed();
        } else {
            Log.e(TAG, "onBackPressed 222222: " );
            Toast.makeText(mContext,"任务未执行完毕，页面不能关闭",Toast.LENGTH_LONG).show();
            return;
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        Log.e(TAG, "dispatchKeyEvent event.getKeyCode(): "+event.getKeyCode() );
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Log.e(TAG, "onKeyDown----- KEYCODE_BACK key  down: ");
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME){
            Log.e(TAG, "onKeyDown----- KEYCODE_HOME key  down: ");
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_APP_SWITCH){
            Log.e(TAG, "onKeyDown----- KEYCODE_APP_SWITCH key  down: ");
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_MENU){
            Log.e(TAG, "onKeyDown----- KEYCODE_MENU key  down: ");
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.e(TAG, "onKeyDown keyCode: "+keyCode );
        Log.e(TAG, "onKeyDown event: "+event );
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.e(TAG, "onKeyDown KEYCODE_BACK key  down: ");
        }
        if (keyCode == KeyEvent.KEYCODE_HOME){
            Log.e(TAG, "onKeyDown KEYCODE_HOME key  down: ");
        }
        if (keyCode == KeyEvent.KEYCODE_APP_SWITCH){
            Log.e(TAG, "onKeyDown KEYCODE_APP_SWITCH key  down: ");
        }
        return super.onKeyDown(keyCode, event);
    }
    private  void setButtonEnableTrue(){
        isClosedActivity = true;

        ed_conut.setEnabled(true);
        clear_text.setEnabled(true);
        btn_msr_card.setEnabled(true);
        btn_nfc_card.setEnabled(true);
        btn_ic_card.setEnabled(true);
        btn_print.setEnabled(true);
        btn_busser.setEnabled(true);

        btn_msr_card.setBackgroundResource(R.drawable.clors_btn);
        btn_nfc_card.setBackgroundResource(R.drawable.clors_btn);
        btn_ic_card.setBackgroundResource(R.drawable.clors_btn);
        btn_print.setBackgroundResource(R.drawable.clors_btn);
        btn_busser.setBackgroundResource(R.drawable.clors_btn);
    }
    private  void setButtonEnableFalse(){
        isClosedActivity = false;
        ed_conut.setEnabled(false);
        clear_text.setEnabled(false);
        btn_msr_card.setEnabled(false);
        btn_nfc_card.setEnabled(false);
        btn_ic_card.setEnabled(false);
        btn_print.setEnabled(false);
        btn_busser.setEnabled(false);

        btn_msr_card.setBackgroundResource(R.drawable.clors_dis);
        btn_nfc_card.setBackgroundResource(R.drawable.clors_dis);
        btn_ic_card.setBackgroundResource(R.drawable.clors_dis);
        btn_print.setBackgroundResource(R.drawable.clors_dis);
        btn_busser.setBackgroundResource(R.drawable.clors_dis);
    }

    @Override
    protected void onDestroy() {
        try {
            mCardBinderService.closeFindCardForEMV();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }
}
