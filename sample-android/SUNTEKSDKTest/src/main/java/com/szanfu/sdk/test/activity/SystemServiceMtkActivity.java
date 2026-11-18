package com.szanfu.sdk.test.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.system.IFirmwareUpdateCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ISystemEncDecBinderService;
import com.szanfu.sdk.api.system.ISystemReadUserKeyBinderService;
import com.szanfu.sdk.api.util.DataConversionUtils;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.ConstantData;

public class SystemServiceMtkActivity extends ListButtonBaseActivity {
//    private EditText ed_conut;
//    private LinearLayout ll_count;
    private int iPrinterSuccess=0;
    private int iPrinterError=0;
    private int iPrinterCount=0;
    private int iPrinterCountOrg = 1;


    public static final int POSITION_FILLINLIGHT_OPEN_TEST = 0;
    public static final int POSITION_FILLINLIGHT_CLOSE_TEST = 1;

    public static final int POSITION_LIGHT_OPEN_ONE_TEST = 2;
    public static final int POSITION_LIGHT_OPEN_TWO_TEST = 3;

    public static final int POSITION_FINGERPRINT_TURNON = 4;
    public static final int POSITION_FINGERPRINT_TURNOFF = 5;
    public static final int POSITION_5V_POWER_SUPPLY_TURNON = 6;
    public static final int POSITION_5V_POWER_SUPPLY_TURNOFF = 7;
    public static final int POSITION_FINGER = 8;


    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private ISystemReadUserKeyBinderService mISystemReadUserKeyBinderService;
    private ISystemEncDecBinderService mISystemEncDecBinderService;
    private IPinPadBinderService mIPinPadBinderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        ed_conut = findViewById(R.id.ed_conut);
//        ll_count = findViewById(R.id.ll_count);
//        ll_count.setVisibility(View.VISIBLE);
        String tempPlatform = DataConversionUtils.getCustomerType();
        if (TextUtils.isEmpty(tempPlatform)
                || ConstantData.PLATFORM_820_MTK.equals(tempPlatform)
                || ConstantData.PLATFORM_820_GTH.equals(tempPlatform)
                || ConstantData.PLATFORM_820_MTK_PINPAD.equals(tempPlatform)
            ) { // 820
            mButtons = getResources().getStringArray(R.array.ALL_SYSTEM_MTK820_SERVICE_LIST);
        }else {
            mButtons = getResources().getStringArray(R.array.ALL_SYSTEM_MTK_SERVICE_LIST);
        }


        setButtonDataSource(mButtons);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
            mISystemReadUserKeyBinderService = ISystemReadUserKeyBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM_READUSERKEY));
            mIPinPadBinderService = IPinPadBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PINPAD));
            mISystemEncDecBinderService = ISystemEncDecBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM_ENCDEC));
        } catch (RemoteException e) {
            e.printStackTrace();
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

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {

            case POSITION_FILLINLIGHT_OPEN_TEST:
                setMtkOpenLightStatus(itemName,1);
                break;
            case POSITION_FILLINLIGHT_CLOSE_TEST:
                setMtkClosedLightStatus(itemName,1);
                break;
            case POSITION_LIGHT_OPEN_ONE_TEST:
                setRFlight(itemName,true,true,true,true);
                break;
            case POSITION_LIGHT_OPEN_TWO_TEST:
                setRFlight(itemName,false,false,false,false);
                break;

            case POSITION_FINGERPRINT_TURNON:
                setHalUsbIdControl(itemName,1);
                break;
            case POSITION_FINGERPRINT_TURNOFF:
                setHalUsbIdControl(itemName,0);
                break;
            case POSITION_5V_POWER_SUPPLY_TURNON:
                setHal5vControl(itemName,1);
                break;
            case POSITION_5V_POWER_SUPPLY_TURNOFF:
                setHal5vControl(itemName,0);
                break;
//            case POSITION_FINGER:
//                fingerMsg(itemName);
//                break;
        }
    }

    private void setRFlight(String itemName, boolean NumberOne,boolean NumberTwo, boolean Numberthree,boolean NumberFour) {
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setLightStatus(NumberOne, NumberTwo, Numberthree, NumberFour);
            appendText(itemName + getResources().getString(R.string.end_result_code) + ret + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }


    }

    /**
     * 补光灯开
     * @param itemName
     * @param lightStatus
     */
    private void setMtkOpenLightStatus(String itemName,int lightStatus){
        appendText(itemName);
        try {

            int ret = mSystemBinderService.setMtkLightStatus(lightStatus);
            appendText(itemName+"ret :"+ret +"\n");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param itemName
     * @param lightStatus
     */
    private void setHal5vControl(String itemName,int lightStatus){
        appendText(itemName);
        try {

            int ret = mSystemBinderService.setHal5vControl(lightStatus);
            appendText(itemName+"ret :"+ret +"\n");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

//    private void fingerMsg(String itemName){
//        appendText(itemName);
//        String strFingerMsg = "123456";
//        HexUtil.hexString2ByteArray(strFingerMsg);
//        byte[] bytes = new byte[32];
//        try {
////        int ret = mSystemBinderService.fingerAsyncOpen(36,bytes,30);
//        int ret = mSystemBinderService.fingerAsyncOpen(36,HexUtil.hexString2ByteArray(strFingerMsg),30);
//        appendText(itemName+"ret :"+ret +"\n");
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//    }

    /**
     * 补光灯关
     * @param itemName
     * @param lightStatus
     */
    private void setMtkClosedLightStatus(String itemName,int lightStatus){
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setMtkLightStatusClosed(lightStatus);
            appendText(itemName+"ret :"+ret+"\n");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 指纹模块
     * @param itemName
     * @param lightStatus
     */
    private void setHalUsbIdControl(String itemName,int lightStatus){
        appendText(itemName);
        try {
            int ret = mSystemBinderService.setHalUsbIdControl(lightStatus);
            appendText(itemName+"ret :"+ret+"\n");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }




    @Override
    protected void onDestroy() {
        iPrinterCountOrg  = iPrinterCount;
        super.onDestroy();
    }

}
