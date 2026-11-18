package com.szanfu.sdk.test.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.card.ICardBinderService;
import com.szanfu.sdk.api.card.ICardCscIsoCmdResult;
import com.szanfu.sdk.api.card.ICardDataResult;
import com.szanfu.sdk.api.card.ICardTestCallback;
import com.szanfu.sdk.api.card.ICscDetectResult;
import com.szanfu.sdk.api.card.ISciGetATRResult;
import com.szanfu.sdk.api.card.ICscGetATSResult;
import com.szanfu.sdk.api.card.ISciIsoCmdResult;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.emv.DalcscIsoMifareCmdResult;
import com.szanfu.sdk.api.emv.IEmvBinderService;
import com.szanfu.sdk.api.error.ErrorCode;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.AFUtils;
import com.szanfu.sdk.test.util.CardSearchType;
import com.szanfu.sdk.test.util.HexSupport;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.ToastUtils;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CardServiceActivity extends ListButtonBaseActivity {
    private static final String TAG = "CardServiceActivity";
    public static final int POSITION_TEST_MSR_CARD = 0;
    public static final int POSITION_TEST_IC_CARD = 1;
    public static final int POSITION_TEST_RF_CARD = 2;
    public static final int POSITION_SEARCH_CARD = 3;
    public static final int POSITION_OPEN_EMV_FIND_CARD = 4;
    public static final int POSITION_CLOSE_EMV_FIND_CARD = 5;
    public static final int POSITION_PICC_CSCGETATS = 6;
    public static final int POSITION_PICC_APDU = 7;
    public static final int POSITION_ICC_POWERON = 8;
    public static final int POSITION_ICC_POWEROFF = 9;
    public static final int POSITION_ICC_SciGETATR = 10;
    public static final int POSITION_ICC_APDU = 11;
    public static final int POSITION_MY_TEST = 12;
    public static final int POSITION_DAL_CSC_IS_MIFARE_CARD = 13;
    public static final int POSITION_DAL_CSC_MIFARE_GETINFO_CARD = 14;
    public static final int POSITION_TEST_Readmifare_CARDData = 15;
    public static final int POSITION_DAL_MODIFY_MIFARE_DATA = 16;
    public static final int POSITION_DAL_MODIFY_MIFARE_DATA_SECOND = 17;
    public static final int POSITION_DAL_MODIFY_MIFARE_UID = 18;
    public static final int POSITION_GETSIMCARDSTATUS = 19;

    String str2WriteData="FF110200300400";
    private String[] mButtons;

    public static final int FIND_CARD_TIMEOUT = 30;
    private IEmvBinderService mIEmvBinderService;
    private ICardBinderService mCardBinderService;
    private ISystemBinderService mSystemBinderService;
    private boolean mMifareCardOperationMode;
    private DalcscIsoMifareCmdResult dalcscIsoMifareCmdResult;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.CARD_SERVICE_LIST);
        setButtonDataSource(mButtons);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mCardBinderService = ICardBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_CARD));
            mIEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 磁条卡寻卡
     * @param itemName
     */
    private void msrCardTest(String itemName) {
        appendText(itemName);

        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mCardBinderService.testMSRCard(FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
                    appendText("**************************************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * IC卡寻卡
     * @param itemName
     */
    private void icCardTest(String itemName) {
        appendText(itemName);
        appendText(getResources().getString(R.string.start_looking_for_a_card));
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    LogUtils.e("---------------icCardTest-------->>>>开始请求寻卡>");
                    int cardType = mCardBinderService.openFindCardForEMV(2, FIND_CARD_TIMEOUT);
                    LogUtils.e("---------------icCardTest-------->>>>>寻卡结果："+cardType);
                    LogUtils.e("FIND_CARD_TIMEOUT :"+FIND_CARD_TIMEOUT);
                    LogUtils.e( "icCardTest: cardType ："+cardType );
                    appendText("openFindCardForEMV ："+cardType);
                    mCardBinderService.closeFindCardForEMV();
                    appendText("**************************************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 非接卡寻卡
     * @param itemName
     */
    private void rfCardTest(String itemName) {
        appendText(itemName);
        appendText(getResources().getString(R.string.start_looking_for_a_card));
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int cardType = mCardBinderService.openFindCardForEMV(4, FIND_CARD_TIMEOUT);
                    appendText("openFindCardForEMV : "+cardType);
                    mCardBinderService.closeFindCardForEMV();
                    appendText("**************************************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void rfCardMifareUID(String itemName) {
        appendText(itemName);
        appendText(getResources().getString(R.string.start_looking_for_a_card));
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
//                    Initiate the contactless card operation;
                    int cardType = mCardBinderService.openFindCardForEMV(4, FIND_CARD_TIMEOUT);
                    appendText("openFindCardForEMV : "+cardType);
                    if (cardType == 4){
//                        Determine whether it is a Mifare card
                            int ret = mIEmvBinderService.apiDALCscIsMifare();
                            appendText("apiDALCscIsMifare ret : " + ret);
                            if (ret == 1){
//                                After confirming that it is a MIFARE card, obtain the MIFARE card UID.
                                    String retUID = mIEmvBinderService.apiDALcscMifareGetInfo();
//                                    retUID ：The data is in hexadecimal format. The first 4 bytes represent the final obtained UID.
                                    appendText("apiDALCscIsMifare retUID : " + retUID);
                            }
                    }
                    appendText("**************************************");
                    mCardBinderService.closeFindCardForEMV();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void searchCard(String itemName) {
        appendText(itemName);
        ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mCardBinderService.closeFindCardForEMV();
                    int cardType = mCardBinderService.openFindCardForEMV(7, FIND_CARD_TIMEOUT);
 // int cardType = mCardBinderService.openFindCardForEMV(7, 60*10); // todo ;
                    switch (cardType) {
                        case CardSearchType.MAGNETIC_STRIPE_CARD: // 1 Magnetic stripe card // 1
                            appendInfoText( "cardType : "+cardType+"\n MSG : Identified the magnetic stripe card");
                            break;

                        case CardSearchType.IC_CARD: // 2 Identified the insertion of an IC card // 2 IC
                            appendInfoText("cardType : "+cardType+"\n MSG :Identified the insertion of an IC card");
                            break;

                        case CardSearchType.RF_CARD: // 4 Identified NFC card operation // 4 NFC
                            appendInfoText("cardType : "+cardType+"\n MSG :Identified NFC card operation");
                            break;

                        case ErrorCode.CARD_ERROR_FIND_CARD_TIMEOUT:
                            // -2003 Card search timeout. The possible reasons could be that the card search was unsuccessful or the [findCardAsyncStat] interface was not called correctly to return the correct card type.
 // 2003 , [findCardAsyncStat]
                            appendErrorText("ErrorCode : "+ErrorCode.CARD_ERROR_FIND_CARD_TIMEOUT
                                            +"\nErrorMsg ： detect card timeout");
                            break;
                        case ErrorCode.CARD_ERROR_FIND_CARD_RECERSE_IC_CARD:
                            // - In 2011,      Returns when an unknown IC card is detected to be inserted.
 // 2011 ic IC
                            appendErrorText("ErrorCode : "+ErrorCode.CARD_ERROR_FIND_CARD_RECERSE_IC_CARD
                                            +"\nErrorMsg :Returns when an unknown IC card is detected to be inserted.");
                            break;
                        case ErrorCode.CARD_ERROR_FIND_CARD_TYPE_NO:
                            // -2012      Before the card search starts, until the card is found or the search times out, the parameters will be returned.
 // 2012
                            appendErrorText("ErrorCode : "+ErrorCode.CARD_ERROR_FIND_CARD_TYPE_NO
                                    +"\nErrorMsg :Before the card search starts, until the card is found or the search times out, the parameters will be returned.");
                            break;
                        case ErrorCode.CARD_ERROR_FIND_ALLCARD:
                            // -2013 Default card search mode, indicating that magnetic stripe, IC and NFC cards can all be swiped simultaneously
 // 2013 , , IC , NFC
                            appendErrorText("ErrorCode : "+ErrorCode.CARD_ERROR_FIND_ALLCARD
                                    +"\nErrorMsg :Default card search mode, indicating that magnetic stripe, IC and NFC cards can all be swiped simultaneously");
                            break;

                        default:
                            appendErrorText("No card found, ret=" + cardType);
                            break;
                    }
                    mCardBinderService.closeFindCardForEMV();
                    appendText("**************************************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });


//        Handler handler = new Handler();
//        handler.post(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    mCardBinderService.closeFindCardForEMV();
//                    int cardType = mCardBinderService.openFindCardForEMV(7, FIND_CARD_TIMEOUT);
 // // int cardType = mCardBinderService.openFindCardForEMV(7, 60*10); // todo ;
//                    switch (cardType) {
//                        case CardSearchType.MAGNETIC_STRIPE_CARD:
//                            appendInfoText("Card swiped");
//                            break;
//
//                        case CardSearchType.IC_CARD:
//                            appendInfoText("ICC detected");
//                            break;
//
//                        case CardSearchType.RF_CARD:
//                            appendInfoText("PICC detected");
//                            break;
//
//                        case ErrorCode.CARD_ERROR_FIND_CARD_TIMEOUT:
//                            appendErrorText("detect card timeout");
//                            break;
//
//                        default:
//                            appendErrorText("No card found, ret=" + cardType);
//                            break;
//                    }
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            }
//        });

    }

    private void openFindCardForEMV(String itemName) {
        appendText(itemName);
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int cardType = mCardBinderService.openFindCardForEMV(7, FIND_CARD_TIMEOUT);
                    appendText(getResources().getString(R.string.open_emv_card_search_results) + cardType);
                    appendText(getResources().getString(R.string.disable_emv_card_search));
                    int ret = mCardBinderService.closeFindCardForEMV();
                    appendText(getResources().getString(R.string.closing_the_emv_card_search_result) + ret + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
    }

    private void icc_powerOff(String itemName) {
        appendText(itemName);
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = mCardBinderService.apiDalSciPowerOff();
                    appendText("icc_powerOff :" + ret + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
    }

    private void closeFindCardForEMV(String itemName) {
        appendText(itemName);
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = mCardBinderService.closeFindCardForEMV();
                    appendText(getResources().getString(R.string.closing_the_emv_card_search_result) + ret + "\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
    }


    void picc_cscGetATS(String iteamName){
        appendText(iteamName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ICscGetATSResult iCscGetATSResult = mCardBinderService.apiCscGetATS();
                    appendText("apiCscGetATS getRet : "+iCscGetATSResult.getRet());
                    appendText("apiCscGetATS getPuiAtrLen : "+iCscGetATSResult.getPuiAtrLen());
                    appendText("apiCscGetATS getPuchAtr : "+AFUtils.showResult16Str(  iCscGetATSResult.getPuchAtr() ));
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                appendText("**************************************");
            }
        });
    }

    void picc_sciGetATR(String iteamName){
        appendText(iteamName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ISciGetATRResult iSciGetATRResult = mCardBinderService.apiSciGetATR();
                    if (iSciGetATRResult.getRet() == 0){
                        appendText("apiCscGetATR getRet : "+iSciGetATRResult.getRet());
                        appendText("apiCscGetATR getPuiAtrLen : "+iSciGetATRResult.getPuiAtrLen());
                        appendText("apiCscGetATR getPuchAtr : "+AFUtils.showResult16Str(  iSciGetATRResult.getPuchAtr() ));
                    }else {
                        appendText("apiCscGetATR getRet : "+iSciGetATRResult.getRet());
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                appendText("**************************************");
            }
        });
    }


    void picc_detect() {
        try {
            ICscDetectResult iCscDetectResult = mCardBinderService.apiCscDetect(0);
            appendText("apiCscDetect ret : "+ iCscDetectResult.getRet());
            appendText("apiCscDetect getCardType :"+ AFUtils.showResult16Str(iCscDetectResult.getCardType()));
            appendText("apiCscDetect getCid : "+ AFUtils.showResult16Str(iCscDetectResult.getCid()));
            appendText( "apiCscDetect getSerialInfo : "+ AFUtils.showResult16Str(iCscDetectResult.getSerialInfo()));
            appendText( "apiCscDetect getOther : "+ AFUtils.showResult16Str(iCscDetectResult.getOther()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void picc_apdu(String itemName) {
        appendText(itemName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String capdu = "00A404000E325041592E5359532E4444463031";
                    byte[] inData = AFUtils.hexString2ByteArray(capdu);
                    appendText("T--> " + capdu);
                    ICardCscIsoCmdResult iCscDetectResult = mCardBinderService.apiDalCscIsoCmd( 0,   inData,  inData.length);
                    if(iCscDetectResult.getRet() == 0) {
                        appendText("C<-- " + AFUtils.showResult16Str(iCscDetectResult.getCardData()));
                        //appendText("apiDalSciIsoCmd getOutLen "+ iCscDetectResult.getOutLen());
                    } else {
                        appendText("C<-- failed, ret=" + iCscDetectResult.getRet());
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                appendText("**************************************");
            }
        });
    }

    void icc_poweron(String itemName) {
        appendText(itemName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ICardDataResult iCardDataResult =  mCardBinderService.apiDalSciPowerOn(0);
                    appendTextResult(iCardDataResult.getRet(),"apiDalSciPowerOn getRet ："+iCardDataResult.getRet());
                    appendText("ATR：" + AFUtils.showResult16Str(iCardDataResult.getCardData()));
                    appendText("**************************************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                appendText("**************************************");
            }
        });
    }

    void icc_apdu(String iteamName) {
        appendText(iteamName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    String capdu = "00A404000E315041592E5359532E4444463031";
                    byte[] inData = AFUtils.hexString2ByteArray(capdu);
                    appendText("T--> " + capdu);
                    ISciIsoCmdResult iSciIsoCmdResult = mCardBinderService.apiDalSciIsoCmd(0,   inData,  inData.length);
                    Log.e("szaf", "icc_apdu: inData.length--》"+inData.length );
                    if(iSciIsoCmdResult.getRet() == 0) {
                        appendText("C<-- " + AFUtils.showResult16Str(iSciIsoCmdResult.getOutData()));
                        //appendText("apiDalSciIsoCmd getOutLen "+ iSciIsoCmdResult.getOutLen());
                    } else {
                        appendErrorText("C<-- failed, ret=" + iSciIsoCmdResult.getRet());
                    }
                    appendText("**************************************");
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(e.getMessage());
                }
            }
        });
    }

    void my_test(String iteamName) {
        appendText(iteamName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                String capdu_list[] = {
                        "00A404000E315041592E5359532E4444463031",
                        "00B2010C00",
                        "00B2020C00",
                        "00A4040008A000000333010101",
                        "80A800000C830A00000000000001035600",
                        "00B2010C00",
                        "00B2020C00",
                        "00B2011400",
                        "00B2021400",
                        "00B2031400",
                        "00B2041400",
                        "00B2011C00",
                        "00B2021C00",
                        "00B2031C00",
                        "00B2012400",
                        "80CA9F3600",
                        "80AE800034000000000001000000000000035608800400000356240825006B45AF96135115546F6D277320536E61636B2053686F7000000000",
                };

                try {
                    //ATR
                    {
                        ICardDataResult iCardDataResult =  mCardBinderService.apiDalSciPowerOn(0);
                        appendTextResult(iCardDataResult.getRet(),"apiDalSciPowerOn getRet ："+iCardDataResult.getRet());
                        appendText("ATR：" + AFUtils.showResult16Str(iCardDataResult.getCardData()));
                        if(iCardDataResult.getRet() != 0)
                            return;
                    }

                    //Transmit APDU
                    for(int i=0; i<capdu_list.length; i++) {
                        appendText("#" + i + " T--> " + capdu_list[i]);
                        ISciIsoCmdResult iSciIsoCmdResult = mCardBinderService.apiDalSciIsoCmd(0,
                                HexSupport.toBytesFromHex(capdu_list[i]),
                                capdu_list[i].length()/2);
                        if(iSciIsoCmdResult.getRet() == 0) {
                            appendText("   C<-- " + AFUtils.showResult16Str(iSciIsoCmdResult.getOutData()));
                            //appendText("apiDalSciIsoCmd getOutLen "+ iSciIsoCmdResult.getOutLen());
                        } else {
                            appendErrorText("   C<-- failed, ret=" + iSciIsoCmdResult.getRet());
                        }
                    }

                } catch(Exception e) {
                    e.printStackTrace();
                    appendErrorText(e.getMessage());
                }
            }
        });

    }

    private void dalcscisMifare(String itemName) {
        appendText(itemName);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    int ret = mIEmvBinderService.apiDALCscIsMifare();
                    appendText("apiDALCscIsMifare ret : " + ret);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        });
    }
    private void dalCscMifareGetinfo(String itemName) {
        appendText(itemName);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String ret = mIEmvBinderService.apiDALcscMifareGetInfo();
                    appendText("apiDALCscIsMifare ret : " + ret);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    appendErrorText(itemName, e.getMessage());
                }
            }
        }).start();
    }

    private void mifareCardData(String itemName, boolean cardOperationMode) {
        appendText(itemName);
        try {
            this.mMifareCardOperationMode = cardOperationMode;
            mCardBinderService.testRFCard(FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }



    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
 // todo , IC RF MSR , emv , ;
            case POSITION_TEST_MSR_CARD:
                msrCardTest(itemName);
                break;
            case POSITION_TEST_IC_CARD:
                icCardTest(itemName);
                break;
            case POSITION_TEST_RF_CARD:
                rfCardTest(itemName);
                break;
            case POSITION_SEARCH_CARD:
                searchCard(itemName);
                break;
            case POSITION_OPEN_EMV_FIND_CARD:
                openFindCardForEMV(itemName);
                break;
            case POSITION_CLOSE_EMV_FIND_CARD:
                closeFindCardForEMV(itemName);
                break;
            case POSITION_PICC_CSCGETATS:
                picc_cscGetATS(itemName);
//                picc_detect();
                break;
            case POSITION_PICC_APDU:
                picc_apdu(itemName);
                break;
            case POSITION_ICC_POWERON:
                icc_poweron(itemName);
                break;
            case POSITION_ICC_SciGETATR:
                picc_sciGetATR(itemName);
            break ;
            case POSITION_ICC_POWEROFF:
                icc_powerOff(itemName);
                break;
            case POSITION_ICC_APDU:
                icc_apdu(itemName);
                break;

            case POSITION_MY_TEST:
                my_test(itemName);
                break;
            case POSITION_DAL_CSC_IS_MIFARE_CARD:
                dalcscisMifare(itemName);
                break;
            case POSITION_DAL_CSC_MIFARE_GETINFO_CARD:
                dalCscMifareGetinfo(itemName);
                break;
            case POSITION_TEST_Readmifare_CARDData:
                mifareCardData(itemName, false);
                break;
            case POSITION_DAL_MODIFY_MIFARE_DATA:
                str2WriteData = "11111111111111111111111111111111";
                mifareCardData(itemName, true);
                break;
            case POSITION_DAL_MODIFY_MIFARE_DATA_SECOND:
                str2WriteData = "22222222222222222222222222222222";
                mifareCardData(itemName, true);
                break;
            case POSITION_DAL_MODIFY_MIFARE_UID:
                rfCardMifareUID(itemName);
                break;
            case POSITION_GETSIMCARDSTATUS:
                getSimCardStatus();
                break;
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
            LogUtils.d("cardType"+cardType);
            appendText(getResources().getString(R.string.the_card_type_is_searched_successfully) + cardType);
            int ret = mIEmvBinderService.apiDALCscIsMifare();
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
                    LogUtils.d("onFindCardSuccess: Write data.... ");

                    String retM0M1 = mIEmvBinderService.apiDALcscMifareGetInfo();
                    appendText("apiDALCscIsMifare ret : " + ret);
 // FA9E788044000000000000000000 // M0
 // 62F55E4A04000800000000000000 // M1
                    if (retM0M1 != null){
                        String strCardType = retM0M1.substring(13,15);
                        LogUtils.d( "dalCscMifareGetinfo strCardType: "+strCardType );
                        if (strCardType.equals("00")){     // M0
                            appendText(" The current card is the M0 card ");
                            str2WriteData = str2WriteData.substring(0,8);
                        }else if (strCardType.equals("80")){ // M1
 // ToastUtils.showToast(mContext," M1 ", Toast.LENGTH_LONG);
                            appendText(" The current card is the M0 card ");
                        }
                    }else {
                        appendText(" Cannot determine whether the current card type is M0 or M1. ");
                    }
                    operateData = AFUtils.hexString2ByteArray(str2WriteData);
 // todo M0 /M1 , M0 4 , M1 , 16
                    operateDataLen = new int[]{operateData.length};
                    LogUtils.d( "Write data content: " + AFUtils.showResult0xStr(operateData) + " Write data Len: " + operateDataLen[0]);
                } else {
                    LogUtils.d( "onFindCardSuccess: Read data.... ");
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
//                    operateDataLen[0] = 0;
//                    for (int i = 0; i < 16; i++) {
//                        blockNum = i;
//                    }

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
            if (track1Data!=null){
                Log.e("szaf", "onTrackDataRead:收到磁条卡返回数据结果" );
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
            if (errorCode == ErrorCode.SUCCESS){
                appendText(mItemName + getResources().getString(R.string.succeed) + "\n");
            } else{
                appendText("Error Code: " + errorCode + "  Sub Error Code: " + subErrorCode
                        + "\nError Msg: " + errorMsg + "\n");
            }
            mCardBinderService.closeFindCardForEMV();
            appendText("**************************************");
        }
    }

    @Override
    protected void onDestroy() {
        closeFindCardForEMV("Closed EMV");
        super.onDestroy();
    }
 // SIM
    private void getSimCardStatus() {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            /**
             * 获取SIM卡状态数组
             * 返回数组说明：
             * simCardStatus[0] - SIM1状态码
             * simCardStatus[1] - SIM2状态码
             *
             * Status Code Definition: 状态码定义：
             *  SIM_STATE_ABSENT         No SIM card present                     无SIM卡
             *  SIM_STATE_READY          SIM card ready and available for use    SIM卡就绪，可用
             *  SIM_STATE_PIN_REQUIRED   SIM card locked by PIN code             SIM卡被PIN码锁定
             *  SIM_STATE_PUK_REQUIRED   SIM card locked by PUK code             SIM卡被PUK码锁定
             *  SIM_STATE_NETWORK_LOCKED SIM card locked by network lock         SIM卡被网络锁锁定
             *  SIM_STATE_UNKNOWN        Unknown SIM card status                 SIM卡状态未知
             *
             * @return String[]
             *     index 0: SIM Card 1 status code
             *     index 1: SIM Card 2 status code
             *     下标 0: 第一张SIM卡状态码
             *     下标 1: 第二张SIM卡状态码
             */
            String[] simCardStatus = mSystemBinderService.getSimCardStatus();
            LogUtils.e("simCardStatus : SIM0:"+ simCardStatus[0]+" SIM1: "+simCardStatus[1]);
            stringBuilder.append("SIM Card 1 Status :"+ simCardStatus[0]+"\n" );

            stringBuilder.append("SIM Card 2 Status :"+ simCardStatus[1]);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        appendText(  stringBuilder.toString() );
    }
}
