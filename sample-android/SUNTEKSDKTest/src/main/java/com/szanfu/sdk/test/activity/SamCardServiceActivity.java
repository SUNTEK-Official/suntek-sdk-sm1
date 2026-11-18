package com.szanfu.sdk.test.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.card.ICardBinderService;
import com.szanfu.sdk.api.card.ICardTestCallback;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.error.ErrorCode;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.LogUtils;


public class SamCardServiceActivity extends ListButtonBaseActivity {
    int checkedSelectItem = 0;
    private android.app.AlertDialog alertDialog;
    public static final int POSITION_TEST_SAM_CARD_Detect = 0;
    public static final int POSITION_TEST_SAM_CARD_PowerOn = 1;
    public static final int POSITION_TEST_SAM_CARD_IsoCmd = 2;
    public static final int POSITION_TEST_SAM_CARD_Close= 3;
    public static final int POSITION_TEST_SAM_CARD_DetectPro_test = 4;
    public static final int POSITION_TEST_SAM_CARD_DetectPro = 5;
    public static final int POSITION_TEST_SAM_CARD_PowerOnPro = 6;
    public static final int POSITION_TEST_SAM_CARD_IsoCmdPro = 7;
    public static final int POSITION_TEST_SAM_CARD_ClosePro= 8;

    private String[] mButtons;
    public static final int FIND_CARD_TIMEOUT = 30;
    private ICardBinderService mCardBinderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.SAM_CARD_SERVICE_LIST);
        setButtonDataSource(mButtons);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mCardBinderService = ICardBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_CARD));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void samCardPowerOn(String itemName) {
        Log.e("szaf", "samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samPowerOn(FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void samCardDetect(String itemName) {
        Log.e("szaf", "samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samDetect(FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void samCardClose(String itemName) {
        Log.e("szaf", "samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samClose(FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void samCardIsoCmd(String itemName) {
        Log.e("szaf", "samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samIsoCmd(FIND_CARD_TIMEOUT,"0084000008", new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void samCardPowerOnPro(int slot,String itemName) {
        Log.e("szaf", "samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samPowerOnPro(slot,FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * 检测是否有SAM卡
     * @param slot
     * @param itemName
     */
    private void samCardDetectPro(int slot,String itemName) {
        Log.e("szaf", "slot : "+slot+"samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samDetectPro(slot,FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * sam卡下电
     * @param slot
     * @param itemName
     */
    private void samCardClosePro(int slot,String itemName) {
        Log.e("szaf", "slot : "+slot+"samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samClosePro(slot,FIND_CARD_TIMEOUT, new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    /**
     * SAM卡通讯 APDU指令
     * @param slot
     * @param itemName
     */
    private void samCardIsoCmdPro(int slot,String itemName) {
        Log.e("szaf", "slot : "+slot+"samCardTest: -----"+itemName );
        appendText(itemName);
        try {
            mCardBinderService.samIsoCmdPro(slot,FIND_CARD_TIMEOUT,"0084000008", new ICardTestCallbackProxy(itemName));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
            case POSITION_TEST_SAM_CARD_Detect:
                samCardDetect(itemName);
                break;
            case POSITION_TEST_SAM_CARD_PowerOn:
                samCardPowerOn(itemName);
                break;
            case POSITION_TEST_SAM_CARD_IsoCmd:
                samCardIsoCmd(itemName);
                break;
            case POSITION_TEST_SAM_CARD_Close:
                samCardClose(itemName);
                break;
            case POSITION_TEST_SAM_CARD_DetectPro_test:
            break;
            case POSITION_TEST_SAM_CARD_DetectPro:
                showSingleAlertDialog( 0,getResources().getString(R.string.samcard_check));
                break;
            case POSITION_TEST_SAM_CARD_PowerOnPro:
                showSingleAlertDialog( 1,getResources().getString(R.string.samcard_poweron));
                break;
            case POSITION_TEST_SAM_CARD_IsoCmdPro:
                showSingleAlertDialog( 2,getResources().getString(R.string.samcard_cmd));
                break;
            case POSITION_TEST_SAM_CARD_ClosePro:
                showSingleAlertDialog( 3,getResources().getString(R.string.samcard_poweoff));
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
            appendText(getResources().getString(R.string.the_card_type_is_searched_successfully) + cardType);
        }

        @Override
        public void onTrackDataRead(byte[] track1Data, byte[] track2Data, byte[] track3Data) throws RemoteException {
            appendErrorText("onTrackDataRead");
        }

        @Override
        public void onCardDataRead(byte[] cardData) throws RemoteException {
//            appendText("SAM CMD Return value: " + showResult16Str(cardData));
            appendErrorText("onCardDataRead");
        }

        @Override
        public void onFindSamCardStart() throws RemoteException {
            appendText("onFindSamCardStart");
        }

        @Override
        public void onResult(int errorCode, String errorMsg, int subErrorCode) throws RemoteException {
            if (errorCode == ErrorCode.SUCCESS){
                appendText(mItemName+":" + errorMsg + "\n");
            }
            else{
                appendText("Error Code: " + errorCode + "  Sub Error Code: " + subErrorCode
                        + "\nError Msg: " + errorMsg + "\n");
            }
        }
    }

    public void showSingleAlertDialog(int requestInterface,String titleName) {
        final String[] items = {
                "SAM : 1"
                ,"SAM : 2"
                ,"SAM : 3"
                };
        android.app.AlertDialog.Builder alertBuilder = new android.app.AlertDialog.Builder(this);
        alertBuilder.setTitle(titleName);
        alertBuilder.setSingleChoiceItems(items, checkedSelectItem , new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int slot) {
                checkedSelectItem = slot;
                appendInfoText("select ： "+items[slot]);
                if (requestInterface == 0){
                    samCardDetectPro(slot, titleName);
                }else if (requestInterface == 1){
                    samCardPowerOnPro(slot,titleName);
                }else if (requestInterface == 2){
                    samCardIsoCmdPro(slot,titleName);
                }else if (requestInterface == 3){
                    samCardClosePro(slot , titleName);
                }
                alertDialog.dismiss();
            }
        });
        alertDialog = alertBuilder.create();
        alertDialog.show();
    }
}
