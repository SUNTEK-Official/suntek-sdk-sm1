package com.szanfu.sdk.test.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.pinpad.IPinPadCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ISystemEncDecBinderService;
import com.szanfu.sdk.api.system.ISystemReadUserKeyBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.HexSupport;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.TriDES;

import java.util.Arrays;

public class TR31Activity extends ListButtonBaseActivity {
    private static final String TAG = "TR31Activity";
    public static final int DUKPT_SET_TR31 = 0;
    public static final int DUKPT_GETKSN = 1;
    public static final int DUKPT_SESSIONKEYENC = 2;
    public static final int DUKPT_SESSIONKEYDEC = 3;
    public static final int DUKPT_KEY_IMPORTED = 4;
    public static final int DUKPT_KSNINCREASE = 5;
    public static final int POSITION_START_PINPAD = 6;

    public static final int DUKPT_SET_TR31_KEYINDEX12 = 8;
    public static final int DUKPT_GETKSN_KEYINDEX12 = 9;
    public static final int DUKPT_KSNINCREASE_KEYINDEX12 = 10;
    public static final int POSITION_START_PINPAD_KEYINDEX12 = 11;
    
    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private ISystemReadUserKeyBinderService mISystemReadUserKeyBinderService;
    private ISystemEncDecBinderService mISystemEncDecBinderService;
    private IPinPadBinderService mIPinPadBinderService;

    /**
     * 密钥索引
     */
    public static final int KEY_INDEX_DUKPT_INDEX_PIN_11 = 11;
    public static final int KEY_INDEX_DUKPT_INDEX_PIN_12 = 12;

    /**
     * 导入DUKPT结果
     */
    private boolean dukptSetKeyResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.TR31_LIST);
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

    /**
     * 3des 加密
     *
     * @param itemName
     */
    private void dukptSessionKeyEnc(String itemName) {
        appendText(itemName);
        try {
                  /*
                    #define DES_ECB_ENC    0x11
                    #define DES_ECB_DEC    0x10

                    #define DES_CBC_ENC    0x21
                    #define DES_CBC_DEC    0x20

                    #define TDES_ECB_ENC   0x41
                    #define TDES_ECB_DEC   0x40

                    #define TDES_CBC_ENC   0x81
                    #define TDES_CBC_DEC   0x80*/
            String[] outData = new String[1];
            int dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_11, 4, "04122D6EDCBA9876", outData, "0000000000000000", 0x81, 0);
            if (dukptEncDec == 0) {
                Log.e(TAG, "dukptEnc:" + outData[0]);
                appendText(itemName + "dukptEnc->> Success:" + outData[0] + "\n");
            } else {
                appendText(itemName + "dukptEnc->> Fail: errorCode->" + dukptEncDec);
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
    private void dukptSessionKeyDec(String itemName) {
        appendText(itemName);
        try {
            String[] outData = new String[1];
            int dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_11, 4, "29D9B4F203722E4C", outData, "0000000000000000", 0x80, 0);
            if (dukptEncDec == 0) {
                Log.e(TAG, "dukptDec:" + outData[0]);
                appendText(itemName + "dukptDec->> Success:" +  outData[0] + "\n");
            } else {
                appendText(itemName + "dukptDec->> Fail: errorCode->" + dukptEncDec);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }



    private void dukptGetKSN(String itemName,int keyIndex) {
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


    private void dukptSetTR31(String itemName) {
        appendText(itemName);
        try {
            Log.e(TAG, "dukptSetTR31");
            int dukptSetTR31Result = mSystemBinderService.dukptSetKey_TR31(KEY_INDEX_DUKPT_INDEX_PIN_11, "57b9fbbc25c7a8d337a20186f7cedf915b0d97d9a7857583", "B0120B1TX00E0100KS18FFFF0000000110A00000520BEAC9F440153F436FE65F9811C77863D0AD6C51AEA3E8B82F257315C9BEA40A5979503823F1E0");
            Log.e(TAG, "dukptSetTR31Result:" + dukptSetTR31Result);
            appendText(itemName + (dukptSetTR31Result == 0 ? "success" : "failure") + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void dukptSetTR31_KeyIndex12(String itemName) {
        appendText(itemName);
        try {
            Log.e(TAG, "dukptSetTR31");
            int dukptSetTR31Result = mSystemBinderService.dukptSetKey_TR31(KEY_INDEX_DUKPT_INDEX_PIN_12, "341ad0942f3470b6bc5e2ca1abbfec58491a5ecb2abf3e2a", "B0120B1TX00E0100KS18FFFF00000001158000002437C9D97994BDAF140FD65E8BEBEB6E51FABB5398706F290D5F43D6E9CCD508DBE00E1B5A33EF42");
            Log.e(TAG, "dukptSetTR31Result:" + dukptSetTR31Result);
            appendText(itemName + (dukptSetTR31Result == 0 ? "success" : "failure") + "\n");
            Log.e(TAG, "dukptSetTR31 index 14");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void dukptIncreasesKSN(String itemName,int keyIndex) {
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
     * 根据 encryptionMode 返回加密模式的描述
     *
     * @param encryptionMode 加密模式代码
     * @return 加密模式的描述
     */
    private static String getEncryptionModeDescription(int encryptionMode) {
        switch (encryptionMode) {
            case 0:
                return "DES/3DES";
            case 1:
                return "SM4";
            case 2:
                return "DUKPT";
            default:
                return "Unknown";
        }
    }


    final String PREFIX_CALLACK = "==>> ";
    int inputPinResult;

    void showPinPad(int action_keyIndex) {
        try {
            //whether show 'CANCEL' button on pinpad
            boolean UI_show_Cancel = true;

            //bypass PIN flag
            //   0: can't bypass PIN
            //   1: can bypass PIN (press OK directly)
            int enable_bypass_pin = 1;

            int min_pin_length = 4;
            int max_pin_length = 4;

            String cardNumber = "8201991234567899";

            String amount = "0.00";  //reserved

            int encryptionMode = 2;  //0:DES/3DES  1:SM4 2:DUKPT

            //timeout,  valid [5, 180] seconds
            int timeout_second = 60;

            //1:online PIN   0:offline PIN
            int pin_type = 1;

            appendText("showPinPad");
            appendText("  keyindex=" + action_keyIndex);
            appendText("  enable_bypass_pin=" + enable_bypass_pin);
            appendText("  min_pin_length=" + min_pin_length);
            appendText("  max_pin_length=" + max_pin_length);
            appendText("  UI_show_Cancel=" + UI_show_Cancel);
            appendText("  cardNumber=" + cardNumber);
            appendText("  encryptionMode=" + encryptionMode + " (" + getEncryptionModeDescription(encryptionMode) + ")");
            appendText("  timeout_second=" + timeout_second);
            appendText("  pin_type=" + pin_type + " (" + ((pin_type == 0) ? "offline PIN" : "online PIN") + ")");

            /**
             * 用户输入密码弹窗
             * @param keyIndex                2-10 for master/session key,  11~30 for DUKPT
            秘钥索引,可供用户操作9组,合法索引值为2-10,11-30为DUKPT索引
             * @param setPasswordLengthLimit   Set PinPad parameters, including whether an empty password is supported and whether the password length can be entered. The value is 0, 6,6. The first digit 0 cannot be pressed directly
            设置PinPad参数，是否支持空密码和支持输入密码长度位数,有效值[0, 6,6] 第一位0不允许直接按OK 1：允许直接按OK
             * @param hasCancelButton   Whether to CANCEL is required true: Yes false: No [Valid only for 930 devices]
            是否需要CANCEL按键 true：需要 false:不需要 【只对930设备生效】
             * @param amount          Transaction amount
            交易金额
             * @param cardNumber      Bank card number, between 12 and 19 in length
            银行卡号,长度在12-19之间
             * @param encryptionMode  Encryption mode 0:DES/3DES 1:SM4 2:DUKPT
            加密模式 0:DES/3DES 1:SM4 2:DUKPT
             * @param inputPwdTimeout The maximum timeout period for entering a password is 3 minutes and the minimum timeout period is 5 seconds. The timeout period starts when the password keyboard is displayed
            输入密码超时时间，单位S，最长不应超过3分钟,最短不建议少于5秒，从密码键盘显示开始计时
             * @param inputPwdTimeout The maximum timeout period for entering a password is 3 minutes and the minimum timeout period is 5 seconds. The timeout period starts when the password keyboard is displayed
            onGetPinBlock
             * @param ucPinType      PIN类型   0 脱机 明文 PIN   1 联机PIN  pinBlock
            PIN type 0. Offline plain text PIN 1. Online confidential text PIN

            输入密码相关监听回调
            param keyIndex               The key index allows users to operate 9 groups. The valid index value ranges from 2 to 10
            param setPasswordLengthLimit   Set PinPad parameters, including whether an empty password is supported and whether the password length can be entered. The value is 0, 6,6. The first digit 0 cannot be pressed directly
            param hasCancelButton   Whether to CANCEL is required true: Yes false: No [Valid only for 930 devices]
            param amount          Transaction amount
            param cardNumber      Bank card number, between 12 and 19 in length
            param encryptionMode  Encryption mode 0:DES/3DES 1:SM4
            param inputPwdTimeout The maximum timeout period for entering a password is 3 minutes and the minimum timeout period is 5 seconds. The timeout period starts when the password keyboard is displayed
            param pinPadCallback  Enter a password-related listening callback
             */
            mIPinPadBinderService.showPinPad(action_keyIndex,
                    new int[]{enable_bypass_pin, min_pin_length, max_pin_length},
                    UI_show_Cancel,
                    amount,
                    cardNumber,
                    encryptionMode,
                    timeout_second,
                    pin_type, new IPinPadCallback.Stub() {
                        @Override
                        public void onGetPinBlock(boolean bypass_pin, String pinblock) throws RemoteException {

                            if (bypass_pin) {
                                appendInfoText(PREFIX_CALLACK + "    onGetPinBlock:: bypass_pin");
                                inputPinResult = 0;
                            } else {
                                appendInfoText(PREFIX_CALLACK + "   onGetPinBlock::");
                                appendText(PREFIX_CALLACK + "   pinblock=" + pinblock);
                                inputPinResult = 0;
                                //verify the pinblock, for test only
                                //DecryptPinBlock(pinblock, cardNumber, action_keyIndex);
                            }
                        }

                        @Override
                        public void onPinPadError(int errCode) throws RemoteException {
                            appendErrorText(PREFIX_CALLACK + "   onPinPadError: " + errCode);
                            UI_Toast("onPinPadError: " + errCode);
                            inputPinResult = errCode;

                            if (pin_type == 0) {  //offline PIN
                                // TODO: zzzzzzzzzzzz how I can know the offline PIN verify result  and the PIN retry times??
                                /** memo by Mr.Yang
                                 交易的时候返回 ucLastTimes及为重试次数
                                 mEmvBinderService.apiSDKEmvInPutConfirmCallBack(new ISdkKernelEmvInputPINCallback.Stub() {
                                @Override public int onEmvInputPIN(byte ucPinType, byte[] pucPinBlock, byte ucLastTimes) throws RemoteException {
                                Log.e(TAG, "ucPinType：" + AFUtils.showResult0xStr(new byte[]{ucPinType}));
                                Log.e(TAG, "ucLastTimes：" + AFUtils.showResult0xStr(new byte[]{ucLastTimes}));

                                currentCardNumber = getTagValue(0x5A);

                                Log.i(TAG, "onEmvInputPIN:currentCardNumber-> " + currentCardNumber);
                                Log.e(TAG, "ucPinType: " + (ucPinType & 0xFF));
 // PIN : : 0 ; -4 ; -1031 PIN ; -1029
                                return showPinPad(true, currentCardNumber, ucPinType & 0xFF, channelType);
                                }
                                });
                                 */
                            }
                        }

                        @Override
                        public void onTimeout() throws RemoteException {
                            appendInfoText(PREFIX_CALLACK + "   onTimeout");
                            UI_Toast("onTimeout");
                            inputPinResult = -4;
                        }

                        @Override
                        public void onCancelPasswordEntry() throws RemoteException {
                            appendInfoText(PREFIX_CALLACK + "   onCancelPasswordEntry");
                            UI_Toast("onCancelPasswordEntry");
                            inputPinResult = -1029;
                        }
                    });
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("pin RemoteException :" + e.getMessage());
        }
    }

    public static int getKeyType(int keyIndex) {
        if (keyIndex >= 2 && keyIndex <= 10) {
            Log.i(TAG, "Master/Session Key (Index: " + keyIndex + ")");
            return 0;
        } else if (keyIndex >= 11 && keyIndex <= 30) {
            Log.i(TAG, "DUKPT Key (Index: " + keyIndex + ")");
            return 1;
        } else {
            Log.i(TAG, "Unknown Key Type (Index: " + keyIndex + ")");
            return 2;
        }
    }

    void DecryptPinBlock(String pinblock_enc, String card_pan, int key_index) {
        appendInfoText("DecryptPinBlock");
        try {
            byte[] pinblock_plain = null;
            switch (getKeyType(key_index)) {
                case 0:
                    pinblock_plain = TriDES.des3DecodeECB(HexSupport.toBytesFromHex(PinPadServiceActivity.hexStringPlaintextKeyTPK), HexSupport.toBytesFromHex(pinblock_enc));
                    break;
                case 1:
                    try {
                        String[] outData = new String[1];
                        int strPinblock_plain = mSystemBinderService.dukptEncDec(key_index, 4, pinblock_enc, outData, "0000000000000000", 0x80, 0);
                        Log.i(TAG, "DUKPT strPinblock_plain: " + strPinblock_plain);
                        if (strPinblock_plain == 0) {
                            Log.e(TAG, "dukptDec:" + outData[0]);
                        } else {
                            appendText("calc pin error=" + strPinblock_plain);
                        }
                        pinblock_plain = HexUtil.hexString2ByteArray(outData[0]);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }

            /* byte[] pinblock_plain = TriDES.des3DecodeECB(HexSupport.toBytesFromHex(PinPadServiceActivity.pinKey_plain), HexSupport.toBytesFromHex(pinblock_enc));*/
            Log.d("ttt", "pinblock_enc=" + pinblock_enc);
            Log.d("ttt", "pinblock_plain=" + HexSupport.toHexFromBytes(pinblock_plain));
            Log.d("ttt", "PAN=" + card_pan);

            int pan_len = card_pan.length();
            String pan_slice = card_pan.substring(pan_len - 13, pan_len - 1);
            Log.d("ttt", "PAN_slice=" + pan_slice);

            byte pan_slice_hex[] = new byte[8];
            Arrays.fill(pan_slice_hex, (byte) 0x00);
            System.arraycopy(HexSupport.toBytesFromHex(pan_slice), 0, pan_slice_hex, 2, 6);
            Log.d("ttt", "PAN_slice_hex=" + HexSupport.toHexFromBytes(pan_slice_hex));

            for (int i = 0; i < 8; i++)
                pinblock_plain[i] = (byte) (pinblock_plain[i] ^ pan_slice_hex[i]);
            Log.d("ttt", "(pinblock_plain xor pan_slice_hex)=" + HexSupport.toHexFromBytes(pinblock_plain));

            appendText("pin=" + HexSupport.toHexFromBytes(pinblock_plain));


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);
        switch (position) {
            case DUKPT_SET_TR31:
                dukptSetTR31(itemName);
                break;
            case DUKPT_GETKSN:
                dukptGetKSN(itemName,KEY_INDEX_DUKPT_INDEX_PIN_11);
                break;
            case DUKPT_SESSIONKEYENC:
                dukptSessionKeyEnc(itemName);
                break;
            case DUKPT_SESSIONKEYDEC:
                dukptSessionKeyDec(itemName);
                break;
            case DUKPT_KEY_IMPORTED:
                dukptKeyExist(itemName, KEY_INDEX_DUKPT_INDEX_PIN_11);
                break;
            case DUKPT_KSNINCREASE:
                dukptIncreasesKSN(itemName,KEY_INDEX_DUKPT_INDEX_PIN_11);
                break;
            case POSITION_START_PINPAD:
                showPinPad(KEY_INDEX_DUKPT_INDEX_PIN_11);
                break;
            case DUKPT_SET_TR31_KEYINDEX12:
                dukptSetTR31_KeyIndex12(itemName);
                break;
            case DUKPT_GETKSN_KEYINDEX12:
                dukptGetKSN(itemName,KEY_INDEX_DUKPT_INDEX_PIN_12);
                break;
            case DUKPT_KSNINCREASE_KEYINDEX12:
                dukptIncreasesKSN(itemName,KEY_INDEX_DUKPT_INDEX_PIN_12);
                break;
            case POSITION_START_PINPAD_KEYINDEX12:
                showPinPad(KEY_INDEX_DUKPT_INDEX_PIN_12);
                break;
            default:
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
