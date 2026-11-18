package com.szanfu.sdk.test.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import androidx.annotation.BoolRes;
import androidx.annotation.RequiresApi;

import com.bigkoo.pickerview.TimePickerView;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.pinpad.IPinPadCallback;
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
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.CommonSetSNDialog;
import com.szanfu.sdk.test.util.CommonSetTUSNDialog;
import com.szanfu.sdk.test.util.HexSupport;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.TriDES;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DUKPTActivity_final extends ListButtonBaseActivity {
    private static final String TAG = "DUKPTActivity";
    public static final int DUKPT_SETKEY = 0;
    public static final int DUKPT_GETKSN = 1;
    public static final int DUKPT_SESSIONKEYENC = 2;
    public static final int DUKPT_SESSIONKEYDEC = 3;
    public static final int DUKPT_PINKEY_IMPORTED = 4;
    public static final int DUKPT_KSNINCREASE = 5;
    public static final int POSITION_START_PINPAD = 6;
    public static final int AUTOMATED_TESTING = 8;
    public static final int DUKPT_AES_TEST = 9;


    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private ISystemReadUserKeyBinderService mISystemReadUserKeyBinderService;
    private ISystemEncDecBinderService mISystemEncDecBinderService;
    private IPinPadBinderService mIPinPadBinderService;
    /////////////////////////////////////////////////////////////////////////////////////////////////////

    final int ALG__DES_ECB_ENC = 0x11;
    final int ALG__DES_ECB_DEC = 0x10;
    final int ALG__DES_CBC_ENC = 0x21;
    final int ALG__DES_CBC_DEC = 0x20;
    final int ALG__TDES_ECB_ENC = 0x41;
    final int ALG__TDES_ECB_DEC = 0x40;
    final int ALG__TDES_CBC_ENC = 0x81;
    final int ALG__TDES_CBC_DEC = 0x80;
    final int ALG__TDES_MAC = 0x02;
    final int ALG__TDES_CMAC = 0x04;
    final int ALG__AES_CMAC = 0x08;
    final int ALG__AES_ECB_ENC = 0x31;
    final int ALG__AES_ECB_DEC = 0x30;
    final int ALG__AES_CBC_ENC = 0x51;
    final int ALG__AES_CBC_DEC = 0x50;

    final int DUKPT_KEY_TYPE_PIK = 1;            //Pin Encryption, can't be used by dukptEncDec()
    final int DUKPT_KEY_TYPE_MAK_BOTH = 2;       //Message Authentication, request or both ways
    final int DUKPT_KEY_TYPE_MAK_RESP = 3;       //Message Authentication, response
    final int DUKPT_KEY_TYPE_DEK_BOTH = 4;       //Data Encryption, request or both ways
    final int DUKPT_KEY_TYPE_DEK_RESP = 5;       //Data Encryption, response


    /////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 密钥类型
     */
    public static final int KEY_INDEX_DUKPT_INDEX_PIN_29 = 29;
    public static final int KEY_INDEX_DUKPT_INDEX_PIN_30 = 30;

    /**
     * 导入DUKPT结果
     */
    private boolean dukptSetKeyResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.DUKPT_LIST);
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
     * 设置秘钥数据
     *
     * @param itemName
     */
    private void dukptSetKey(String itemName, int keyIndex, String keyValue, String ksn) {
        appendText(itemName);
        try {
            int dukptSetKeyResult = mSystemBinderService.dukptSetIPEKey(keyIndex, keyValue, ksn);
            Log.e(TAG, "dukptSetKeyResult:" + dukptSetKeyResult);
            appendText(itemName + (dukptSetKeyResult == 0 ? " success" : " failure") + "\n");
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
            int dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 4, "04122D6EDCBA9876", outData, "0000000000000000", 0x81, 0);
            if (dukptEncDec == 0) {
                Log.e(TAG, "dukptEnc:" + outData[0]);
                appendText(itemName + "dukptEnc==> Success:" + outData[0] + "\n");
            } else {
                appendText(itemName + "dukptEnc==> Fail: errorCode->" + dukptEncDec);
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
//            int dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 4, "29D9B4F203722E4C", outData, "0000000000000000", 0x80, 0);
            int dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 4, "8DF3115C6E58B26B", outData, "0000000000000000", 0x80, 0);
            if (dukptEncDec == 0) {
                Log.e(TAG, "dukptDec:" + outData[0]);
                appendText(itemName + "dukptDec==> Success:"+ outData[0] + "\n");
            } else {
                appendText(itemName + "dukptDec==> Fail: errorCode->" + dukptEncDec);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }


    private void dukptGetKSN(String itemName) {
        appendText(itemName);
        try {
            Log.e(TAG, "getSN: 获取明文KSN数据");
            String ksn = mSystemBinderService.dukptGetCurrentKSN(KEY_INDEX_DUKPT_INDEX_PIN_29);
            Log.e(TAG, "getKSN:" + ksn);
            appendText(itemName + getResources().getString(R.string.session_ksn) + ksn + "\n");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }




    private void dukptIncreasesKSN(String itemName) {
        appendText(itemName);
        try {
            Log.e(TAG, "dukptIncreasesKSN");
            boolean dukptIncreasesKSNResult = mSystemBinderService.dukptIncreasesKSN(KEY_INDEX_DUKPT_INDEX_PIN_29);
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


    public static final int key_index = KEY_INDEX_DUKPT_INDEX_PIN_29;
    final String PREFIX_CALLACK = "==>> ";
    int inputPinResult;

    void showPinPad() {
        try {
            //whether show 'CANCEL' button on pinpad
            boolean UI_show_Cancel = true;

            //bypass PIN flag
            //   0: can't bypass PIN
            //   1: can bypass PIN (press OK directly)
            int enable_bypass_pin = 1;

            int min_pin_length = 4;
            int max_pin_length = 4;

            String cardNumber = "4012345678909";

            String amount = "0.00";  //reserved

            int encryptionMode = 2;  //0:DES/3DES  1:SM4 2:DUKPT

            //timeout,  valid [5, 180] seconds
            int timeout_second = 60;

            //1:online PIN   0:offline PIN
            int pin_type = 1;

            appendText("showPinPad");
            appendText("  keyindex=" + key_index);
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
            mIPinPadBinderService.showPinPad(key_index,
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
                                //DecryptPinBlock(pinblock, cardNumber, key_index);
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
                        int strPinblock_plain = mSystemBinderService.dukptEncDec(key_index, 1, pinblock_enc, outData, "0000000000000000", 0x80, 0);
                        Log.i(TAG, "DUKPT strPinblock_plain: " + strPinblock_plain);
                        if (strPinblock_plain == 0) {
                            Log.e(TAG, "dukptDec:" + outData[0]);
                        } else {
                            appendText("calc pin error=" + strPinblock_plain);
                        }
                        pinblock_plain = HexUtil.hexString2ByteArray(outData[0]);
                        appendText("Clear ANSI PIN Block:=" + HexSupport.toHexFromBytes(pinblock_plain));
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
   /* public static final int DUKPT_SETKEY = 0;
    public static final int DUKPT_GETKSN = 1;
    public static final int DUKPT_SESSIONKEYENC = 2;
    public static final int DUKPT_SESSIONKEYDEC = 3;
    public static final int DUKPT_PINKEY_IMPORTED = 4;
    public static final int DUKPT_KSNINCREASE = 5;
    public static final int POSITION_START_PINPAD = 6;*/

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);
        Log.i(TAG, "onItemClick: " + position + "\t -->" + itemName);
        switch (position) {
            case DUKPT_SETKEY:
                dukptSetKey(itemName, KEY_INDEX_DUKPT_INDEX_PIN_29, "0123456789ABCDEFFEDCBA9876543210", "FFFF9876543210E00000");
                break;
            case DUKPT_GETKSN:
                dukptGetKSN(itemName);
                break;
            case DUKPT_SESSIONKEYENC:
                dukptSessionKeyEnc(itemName);
                break;
            case DUKPT_SESSIONKEYDEC:
                dukptSessionKeyDec(itemName);
                break;
            case DUKPT_PINKEY_IMPORTED:
                dukptKeyExist(itemName, KEY_INDEX_DUKPT_INDEX_PIN_29);
                break;
            case DUKPT_KSNINCREASE:
                dukptIncreasesKSN(itemName);
                break;
            case POSITION_START_PINPAD:
                showPinPad();
                break;
            case AUTOMATED_TESTING:
                encryptPin();
//                dukptCheckData(itemName);
                break;
            case DUKPT_AES_TEST:
                dukptAesTest(itemName);
                break;
            default:
                break;
        }
    }

    private void dukptAesTest(String itemName) {
        appendText(itemName);
        if (mSystemBinderService == null) {
            appendErrorText("system service not connected");
            return;
        }

        try {
            String testIpek = "6AC292FAA1315B4D858AB3A3D7D5933A";
            String testKsn = "FFFF9876543210E00000";
            String aesPlainData = "00112233445566778899AABBCCDDEEFF";
            String aesCbcIv = "00000000000000000000000000000000";

            appendInfoText("DUKPT AES test key");
            appendText("   keyIndex=" + KEY_INDEX_DUKPT_INDEX_PIN_29);
            appendText("   IPEK=" + testIpek);
            appendText("   KSN=" + testKsn);
            int setKeyRet = mSystemBinderService.dukptSetIPEKey(KEY_INDEX_DUKPT_INDEX_PIN_29, testIpek, testKsn);
            if (setKeyRet != 0) {
                appendErrorText("dukptSetIPEKey ret=" + setKeyRet);
                return;
            }

            if (!resetDukptAesTestKsn(testKsn, "AES ECB")) {
                return;
            }
            String ecbCipher = dukptAesOperation("AES_ECB_ENC", DUKPT_KEY_TYPE_DEK_BOTH,
                    aesPlainData, aesCbcIv, ALG__AES_ECB_ENC);
            if (ecbCipher == null) {
                return;
            }
            if (!resetDukptAesTestKsn(testKsn, "AES ECB decrypt")) {
                return;
            }
            String ecbPlain = dukptAesOperation("AES_ECB_DEC", DUKPT_KEY_TYPE_DEK_BOTH,
                    ecbCipher, aesCbcIv, ALG__AES_ECB_DEC);
            if (!verifyDukptAesResult("AES ECB round-trip", aesPlainData, ecbPlain)) {
                return;
            }

            if (!resetDukptAesTestKsn(testKsn, "AES CBC")) {
                return;
            }
            String cbcCipher = dukptAesOperation("AES_CBC_ENC", DUKPT_KEY_TYPE_DEK_BOTH,
                    aesPlainData, aesCbcIv, ALG__AES_CBC_ENC);
            if (cbcCipher == null) {
                return;
            }
            if (!resetDukptAesTestKsn(testKsn, "AES CBC decrypt")) {
                return;
            }
            String cbcPlain = dukptAesOperation("AES_CBC_DEC", DUKPT_KEY_TYPE_DEK_BOTH,
                    cbcCipher, aesCbcIv, ALG__AES_CBC_DEC);
            if (!verifyDukptAesResult("AES CBC round-trip", aesPlainData, cbcPlain)) {
                return;
            }

            if (!resetDukptAesTestKsn(testKsn, "AES CMAC request")) {
                return;
            }
            String requestCmac = dukptAesOperation("AES_CMAC Request", DUKPT_KEY_TYPE_MAK_BOTH,
                    aesPlainData, aesCbcIv, ALG__AES_CMAC);
            if (!verifyDukptAesOutputNotEmpty("AES_CMAC Request", requestCmac)) {
                return;
            }

            if (!resetDukptAesTestKsn(testKsn, "AES CMAC response")) {
                return;
            }
            String responseCmac = dukptAesOperation("AES_CMAC Response", DUKPT_KEY_TYPE_MAK_RESP,
                    aesPlainData, aesCbcIv, ALG__AES_CMAC);
            verifyDukptAesOutputNotEmpty("AES_CMAC Response", responseCmac);
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private boolean resetDukptAesTestKsn(String ksn, String caseName) throws RemoteException {
        int ret = mSystemBinderService.dukptSetKSN(KEY_INDEX_DUKPT_INDEX_PIN_29, ksn);
        appendText(caseName + " set KSN ret=" + ret + ", KSN=" + ksn);
        if (ret != 0) {
            appendErrorText(caseName + " set KSN failed, ret=" + ret);
            return false;
        }
        return true;
    }

    private String dukptAesOperation(String caseName, int keyType, String inData, String iv, int mode) throws RemoteException {
        String[] outData = new String[1];
        appendInfoText(caseName);
        appendText("   keyIndex=" + KEY_INDEX_DUKPT_INDEX_PIN_29);
        appendText("   keyType=" + keyType);
        appendText("   mode=0x" + Integer.toHexString(mode).toUpperCase());
        appendText("   inData=" + inData);
        appendText("   iv=" + iv);
        int ret = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, keyType,
                inData, outData, iv, mode, 0);
        if (ret != 0) {
            appendErrorText(caseName + " ret=" + ret);
            return null;
        }
        appendText("   outData=" + outData[0]);
        return outData[0];
    }

    private boolean verifyDukptAesResult(String caseName, String expected, String actual) {
        if (actual != null && expected.equalsIgnoreCase(actual)) {
            appendInfoText(caseName + " match");
            return true;
        }
        appendErrorText(caseName + " unmatch, expected=" + expected + ", actual=" + actual);
        return false;
    }

    private boolean verifyDukptAesOutputNotEmpty(String caseName, String output) {
        if (output != null && output.length() > 0) {
            appendInfoText(caseName + " success");
            return true;
        }
        appendErrorText(caseName + " output is empty");
        return false;
    }

    private void dukptCheckData(String itemName) {
        appendText(itemName);

        /*
          ANSI X9.24 PART1-2009

            BDK:   0123456789ABCDEFFEDCBA9876543210
            IPEK:  6AC292FAA1315B4D858AB3A3D7D5933A
            IKSN:  FFFF9876543210E00000
            PAN:   4012345678909

            Formatted PIN 041234FFFFFFFFFF
            Clear ANSI PIN Block: 041274EDCBA9876F
            MAC and Data Encryption Input (ASCII) “4012345678909D987”
            MAC and Data Encryption Input  (padded hex)
            3430313233343536.3738393039443938.
            3700000000000000

          A.4.2 Initial Sequence
         */
        final String[][] TEST_DATA = {
                // KSN,Expected PIN Block, Expected MAC（Request）,Expected MAC（Response）, Expected Encrypted Data
                {"FFFF9876543210E00000", "CE304CE3195EEF29", "CF265D17E592AAED", "EA5DE2EC30BD298A", "B815DC11B4821B89AE6594FD4FA9B2A23324CF9C9144C96B"},
                {"FFFF9876543210E00001", "1B9C1845EB993A7A", "9CCC78173FC4FB64", "20364223C1FF00FA", "FC0D53B7EA1FDA9EE68AAF2E70D9B9506229BE2AA993F04F"},
                {"FFFF9876543210E00002", "10A01C8D02C69107", "F608A9BCA6FFC311", "D1FCA6BEF05D24D2", "A2B4E70F846E63D68775B7215EB4563DFD3037244C61CC13"},
                {"FFFF9876543210E00003", "18DC07B94797B466", "20B59A4FEAC937E3", "BAD4CC9CC2AE326C", "BD751E65F10E75B6C1D5B1D283496A36C2DE21D993C387A7"},
                {"FFFF9876543210E00004", "0BC79509D5645DF7", "C7BFA6CC44161828", "1EB08AEECE6FF0C2", "1118F50947441BBDA3C8C70220021A12EC31CC473F7215F4"},
                {"FFFF9876543210E00005", "5BC0AF22AD87B327", "0202B96339022058", "5CBE3E81D1D2A0FB", "9FD7BD1EC28845ACA93367A9DA9317BD555C6B33AE22D365"},
                {"FFFF9876543210E00006", "A16DF70AE36158D8", "CF6C72E6A49892D5", "C60D38E858C936EB", "DC526613AD9095C1CEC60C41B7686ED606AC1AA2F4F54912"},
                {"FFFF9876543210E00007", "27711C16CB257F8E", "B11EB0D97CF167E8", "053EFD396A0B1333", "24700BD6F17751531F2A16CE2AF7731101E6F87839C67244"},
                {"FFFF9876543210E00008", "50E55547A5027551", "3679055BCCBE3D6B", "7FACA91D970D9187", "5F6584DEAFC51B48A239B51C2DACA97176C01D9CA8EDC33C"},
                {"FFFF9876543210E00009", "536CF7F678ACFC8D", "26AA23DC169152F8", "37F17B8F302EE3A6", "7CB9080923ED4D7D8D1B8A2849331684CC910522023BE537"},
                {"FFFF9876543210E0000A", "EDABBA23221833FE", "1632621C039098CE", "CE038CC91CB4CBBE", "C221F5A8CA5DA909BE04938E0FB8DFCFB30ED0D62B590663"},
                {"FFFF9876543210E0000B", "2328981C57B4BDBA", "76D95CB4F6B0D093", "27C9FA5E14F0A044", "98A271C13E4AEE80C6343867F2F0F6C55E8B7E5645866A0B"},
                {"FFFF9876543210E0000C", "038D03CC926CF286", "BAD9D84D1C934D3B", "6D2ECC6A3CE2A378", "273CBE2F275B1C29D53F3C01A24DEC73F92C3BE219FFCB91"},
                {"FFFF9876543210E0000D", "6C8AA97088B62C68", "AAAFB5983360C89A", "7E40B49274596E49", "7290CB9D6F6E35A9B8A34C590A93696F8FE0137A8558884C"},
                {"FFFF9876543210E0000E", "F17C9E1D72CD4950", "4EC79532D8EC0E30", "552339891A346CA4", "48207931E5F6E5E3E02E7E1D7BED4E3232FCA67E66A31F61"},
                {"FFFF9876543210E0000F", "B170F6E7F7F2F64A", "82500D05251841E7", "AFE23CBA2307F7CC", "166246602B1255114A4012513B0DD4A9E19E82C945860C55"},
                {"FFFF9876543210E00010", "D5D9638559EF53D6", "D39D3A598549B9CD", "9E933C88DA838A4F", "53EDA44BDBCA037B2D3FEAEBAF73499336767BD1D0D09679"},
                {"FFFF9876543210E00011", "D544F8CDD292C863", "F094AF9309CD8A91", "631E7B39C609220B", "086F3F46238B99AFB35DA42029675EE1FAB1C606307B35FB"},
                {"FFFF9876543210E00012", "7A21BD10F36DC41D", "A819AF04330EB313", "E93F5E271A20D156", "270439430E60BFC600FE3DC7E7AD8C72D22D5284EC676EF2"},
                {"FFFF9876543210E00013", "78649BD17D0DFA60", "79285A1733915039", "FCE7A5A5A7333665", "2FEAE132FA0E46B4ABF55A87477AFA907E598868BCC236DA"},
                {"FFFF9876543210E00014", "7E7E16EA0C31AD56", "E2B2637FC4BB5D0D", "A10E94AA46F9FA03", "A16A61328E36A21858B5FBFBE7BAF7985F5C54F5B8C12298"},
                {"FFFF9876543210E00015", "72105C22EBC791E6", "B2033EBD58DA8BF1", "BDA3AEE182D0FFEA", "42A9342FD5C1ACD20E137950A7368D2BBE884275B7906918"},

                {"FFFF9876543210EFF800", "33365F5CC6F23C35", "23E3CB62478E6EA4", "5776C37671CD7AF9", "5FF8C648565975E746C0CB7B1157DCB6EC3C5F25E82F8A62"},
                {"FFFF9876543210EFF801", "3A86BF003F835C9D", "97623632CAD07B98", "69D1EF4762ACC916", "55374FDBB3B2B4ED0751243FC0C54DB1F3F3CF76F0C0B3C4"},
                {"FFFF9876543210EFF802", "3DB977D05C36DF3F", "726085213E40251C", "BF7036B7CE0467D4", "74A1250E3234DEDEB422FA7F862C484BBA6924DF9BC32CE5"},
                {"FFFF9876543210EFF804", "BA83243305712099", "D86687B77B29ADC9", "645A0A18A6F318AF", "159E9FA8E979A9C76C118C7BE9E5D436AC0B22CD098BAE1F"},
                {"FFFF9876543210EFF808", "B0DA04AC90A36D85", "5E00713D3827FC2F", "E8D8026A95972BFB", "F87F61877DFF1CD39A635BEA4D15F81D30793B9157805BE4"},
                {"FFFF9876543210EFF810", "2CF02BD9C309EEDA", "1BCCF968BA8707E9", "9623FF4581EC1166", "4DEA2DDA8D079F8040CAD885C29A4037E9490480256D074D"},
                {"FFFF9876543210EFF820", "9D1E2F77AEEE81C6", "2F6D2191FD75B647", "FF864D190E4CE105", "CE625FFBBB339C8305FA86CBFD192F4292B29962649947DD"},
                {"FFFF9876543210EFF840", "40870B0F8BA2011C", "4DA4CBAC62677694", "3CE7AD5052AC3AE9", "29DDF3F2C3B9BE76A8338E2B22BA668B68B46AAE9C714607"},
                {"FFFF9876543210EFF880", "22E340D6ABB40981", "95C43BC2C019DBAC", "73D03F0B90E6DBA1", "9E9AA50594849DEB63BC3EF3BE63C5637F03D03B0DCFA973"},
                {"FFFF9876543210EFF900", "1A4C10AFBA03A430", "D6BEAA71DF19FF0A", "5D461B7199531F3D", "C653AFCD3245ADB3B113FFC48B46BD0B6C5B3B5FB10044EE"},
                {"FFFF9876543210EFFA00", "849763B43E5F9CFF", "93930C9F4AEC91D7", "531DA5F91D4E6C7C", "50EB117D8CB2BE5ADBBE98B6570C557C13622688A61D559D"},
                {"FFFF9876543210EFFC00", "DEFC6F09F8927B71", "2917C6EFF391D1BF", "AA8D2DA65925AE3B", "A2C773BC45B277E3F100C37E30A6A6FB1865134815B05EBC"},
                {"FFFF9876543210F00000", "73EC88AD0AC5830E", "7DFA93155E2D87A0", "1444BEC3726A3EA6", "1E830E7F53FC171E35380145320233EF9C6F3B08CED984AB"},
        };
        String clear_pinblock_hexstring = "041274EDCBA9876F";
        String mac_data_hexstring = "343031323334353637383930394439383700000000000000";
        String iv_hexsring = "0000000000000000";
        String outData_hexstring[] = new String[1];

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /* for test only
                    for(int i=0; i<256; i++) {
                        //appendInfoText("#" + i + " ---------------");
                        appendText("#" + i + " ---------------");
                    }
                    if(true)
                        return;
                    */


                    //set the initial KSN
                    dukptSetIPEK(itemName);

                    for (int i = 0; i < TEST_DATA.length; i++) {

                        appendInfoText("#" + i + " ---------------");

                        /////// set KSN
                        appendText("dukptSetKSN keyindex=" + KEY_INDEX_DUKPT_INDEX_PIN_29 + " KSN=" + TEST_DATA[i][0]);
                        int ret = mSystemBinderService.dukptSetKSN(KEY_INDEX_DUKPT_INDEX_PIN_29, TEST_DATA[i][0]);
                        if (ret != 0) {
                            appendErrorText("set KSN failed: ret=" + ret + " keyindex=" + KEY_INDEX_DUKPT_INDEX_PIN_29 + " KSN=" + TEST_DATA[i][0]);
                            return;
                        } else {
                            appendText("ret=" + ret);
                        }

                        /////// get KSN
                        dukptGetKSN(itemName);

                        /////// Expected PIN Block

                        /*
                        ret = mSystemBinderService.dukptEncDec(keyindex, DUKPT_KEY_TYPE_PIK,
                                                    clear_pinblock_hexstring, outData_hexstring, iv_hexsring,
                                                    ALG__TDES_CBC_ENC, 0);
                        if (ret == 0) {
                            appendText("enc pinblock ok, out=" + outData_hexstring[0]);
                            if(outData_hexstring[0].equalsIgnoreCase(TEST_DATA[i][1])) {
                                appendInfoText("===match===");
                            } else {
                                appendErrorText("===unmatch===");
                                return;
                            }
                        } else {
                            appendErrorText("dukptEncDec(DUKPT_KEY_TYPE_PIK) ret=" + ret);
                            return;
                        }
                        */

                        /////// Expected Expected MAC（Request）

                        ret = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, DUKPT_KEY_TYPE_MAK_BOTH,
                                mac_data_hexstring, outData_hexstring, iv_hexsring,
                                ALG__TDES_MAC, 0);
                        if (ret == 0) {
                            appendText("compute MAC (request) ok, out=" + outData_hexstring[0]);
                            if (outData_hexstring[0].equalsIgnoreCase(TEST_DATA[i][2])) {
                                appendInfoText("===match===");
                            } else {
                                appendErrorText("===unmatch===");
                                return;
                            }
                        } else {
                            appendErrorText("dukptEncDec(DUKPT_KEY_TYPE_MAK_BOTH) ret=" + ret);
                            return;
                        }

                        /////// Expected Expected MAC（Response）

                        ret = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, DUKPT_KEY_TYPE_MAK_RESP,
                                mac_data_hexstring, outData_hexstring, iv_hexsring,
                                ALG__TDES_MAC, 0);
                        if (ret == 0) {
                            appendText("compute MAC (Response) ok, out=" + outData_hexstring[0]);
                            if (outData_hexstring[0].equalsIgnoreCase(TEST_DATA[i][3])) {
                                appendInfoText("===match===");
                            } else {
                                appendErrorText("===unmatch===");
                                return;
                            }
                        } else {
                            appendErrorText("dukptEncDec(DUKPT_KEY_TYPE_MAK_RESP) ret=" + ret);
                            return;
                        }


                        /////// Expected Encrypted Data

                        ret = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, DUKPT_KEY_TYPE_DEK_BOTH,
                                mac_data_hexstring, outData_hexstring, iv_hexsring,
                                ALG__TDES_CBC_ENC, 0);
                        if (ret == 0) {
                            appendText("enc data ok, out=" + outData_hexstring[0]);
                            if (outData_hexstring[0].equalsIgnoreCase(TEST_DATA[i][4])) {
                                appendInfoText("===match===");
                            } else {
                                appendErrorText("===unmatch===");
                                return;
                            }
                        } else {
                            appendErrorText("dukptEncDec(DUKPT_KEY_TYPE_DEK_BOTH) ret=" + ret);
                            return;
                        }

                        /////// Increase KSN
                        //dukptIncreasesKSN(itemName, keyindex);

                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }


    private void dukptSetIPEK(String itemName) {
        appendText(itemName);
        try {
            /*
            ANSI X9.24 PART1-2009

            BDK:   0123456789ABCDEFFEDCBA9876543210
            IPEK:  6AC292FAA1315B4D858AB3A3D7D5933A
            IKSN:  FFFF9876543210E00000
            PAN:   4012345678909

            Formatted PIN 041234FFFFFFFFFF
            Clear ANSI PIN Block: 041274EDCBA9876F
            MAC and Data Encryption Input (ASCII) “4012345678909D987”
            MAC and Data Encryption Input  (padded hex)
            3430313233343536.3738393039443938.
            3700000000000000
            */
            String IKEK_hexstring = "6AC292FAA1315B4D858AB3A3D7D5933A";
            String IKSN_hexstring = "FFFF9876543210E00000";

            appendInfoText("dukptSetIPEKey");
            appendText("   keyindex=" + KEY_INDEX_DUKPT_INDEX_PIN_29);
            appendText("   IKEK=" + IKEK_hexstring);
            appendText("   IKSN=" + IKSN_hexstring);

            int ret = mSystemBinderService.dukptSetIPEKey(KEY_INDEX_DUKPT_INDEX_PIN_29, IKEK_hexstring, IKSN_hexstring);
            if (ret == 0)
                appendInfoText("successful");
            else
                appendErrorText("failed");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void shutdown() {
        executorService.shutdown();
    }

    private static final String[][] TEST_DATA = {
            // KSN,Expected PIN Block, Expected MAC（Request）,Expected MAC（Response）, Expected Encrypted Data
            {"FFFF9876543210E00000", "CE304CE3195EEF29", "CF265D17E592AAED", "EA5DE2EC30BD298A", "B815DC11B4821B89AE6594FD4FA9B2A23324CF9C9144C96B"},
            {"FFFF9876543210E00001", "1B9C1845EB993A7A", "9CCC78173FC4FB64", "20364223C1FF00FA", "FC0D53B7EA1FDA9EE68AAF2E70D9B9506229BE2AA993F04F"},
            {"FFFF9876543210E00002", "10A01C8D02C69107", "F608A9BCA6FFC311", "D1FCA6BEF05D24D2", "A2B4E70F846E63D68775B7215EB4563DFD3037244C61CC13"},
            {"FFFF9876543210E00003", "18DC07B94797B466", "20B59A4FEAC937E3", "BAD4CC9CC2AE326C", "BD751E65F10E75B6C1D5B1D283496A36C2DE21D993C387A7"},
            {"FFFF9876543210E00004", "0BC79509D5645DF7", "C7BFA6CC44161828", "1EB08AEECE6FF0C2", "1118F50947441BBDA3C8C70220021A12EC31CC473F7215F4"},
            {"FFFF9876543210E00005", "5BC0AF22AD87B327", "0202B96339022058", "5CBE3E81D1D2A0FB", "9FD7BD1EC28845ACA93367A9DA9317BD555C6B33AE22D365"}


    };

    private void encryptPin() {
        executorService.submit(new Runnable() {
            @Override
            public void run() {

                //dukptSetKey("Automated testing", KEY_INDEX_DUKPT_INDEX_PIN_29, "0123456789ABCDEFFEDCBA9876543210", "FFFF9876543210E00000");
                dukptSetKey("Automated testing", KEY_INDEX_DUKPT_INDEX_PIN_29, "6AC292FAA1315B4D858AB3A3D7D5933A", "FFFF9876543210E00000");
                for (String[] test : TEST_DATA) {
                    String ksn = test[0];
                    String expectedPinBlock = test[1];
                    String expectedMacRequest = test[2];
                    String expectedMacResponse = test[3];
                    String expectedData = test[4];
                    if (!dukptPinBlockEnc(expectedPinBlock, expectedMacRequest, expectedMacResponse, expectedData)) {
                        Log.e(TAG, "encryptPin: 测试终止!!!");
                        return;
                    }
                }
            }
        });
    }

    private String ksnNumber = null;
    int dukptEncDec = -1;

    private boolean dukptPinBlockEnc(String strPinBlock, String strMACRequest, String strMACResponse, String strData) {
        Log.i(TAG, "======================dukptPinBlockEnc: Begin====================== ");
        appendText("\n------dukptPinBlockEnc Begin------");
        try {
 // KSN
            String ksnNumber = mSystemBinderService.dukptGetCurrentKSN(KEY_INDEX_DUKPT_INDEX_PIN_29);
            Log.e(TAG, "getKSN:" + ksnNumber);
            appendText(getResources().getString(R.string.session_ksn) + ksnNumber);

 // PIN
            String[] outData = new String[1];
            /*  dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 1, "041274EDCBA9876F", outData, "0000000000000000", 0x81, 0);
            Log.e(TAG, "Correct PinBlock:" + strPinBlock);
            Log.e(TAG, "Actual calculation:" + outData[0]);
            if (dukptEncDec == 0 && strPinBlock.equalsIgnoreCase(outData[0])) {
                Log.e(TAG, "pinBlock:" + outData[0]);
                appendText("CALC PinBlock==> Success:" + outData[0]);
            } else {
                appendErrorText("CALC PinBlock Fail errorCode==>" + dukptEncDec + "\n Result==>" + outData[0] + "\n Correct PinBlock==>" + strPinBlock);
                return false;
            }*/
           /*  @param keyType
                       2:  Message Authentication, request or both ways
     				   3:  Message Authentication, response
     				   4:  Data Encryption, request or both ways
     				   5:  Data Encryption, response

            @param mode encryption / decryption mode
            DES_ECB_ENC    0x11
            DES_ECB_DEC    0x10
            DES_CBC_ENC    0x21
            DES_CBC_DEC    0x20
            TDES_ECB_ENC   0x41
            TDES_ECB_DEC   0x40
            TDES_CBC_ENC   0x81
            TDES_CBC_DEC   0x80
            TDES_MAC       0x02
            TDES_CMAC      0x04*/
            //MAC Request
            dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 2, "343031323334353637383930394439383700000000000000 ", outData, "0000000000000000", 0x02, 0);
            Log.e(TAG, "Correct MACRequest:" + strMACRequest);
            Log.e(TAG, "Actual calculation:" + outData[0]);
            if (dukptEncDec == 0 && strMACRequest.equalsIgnoreCase(outData[0])) {
                Log.e(TAG, "MAC->Request:" + outData[0]);
                appendText("CALC MAC->Request==> Success:" + outData[0]);
            } else {
                appendErrorText("CALC MACRequest Fail errorCode==>" + dukptEncDec + "\n Result==>" + outData[0] + "\n Correct MACRequest==>" + strMACResponse);
                return false;
            }

            //MAC Response
            dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 3, "343031323334353637383930394439383700000000000000 ", outData, "0000000000000000", 0x02, 0);
            Log.e(TAG, "Correct MACResponse:" + strMACResponse);
            Log.e(TAG, "Actual calculation:" + outData[0]);
            if (dukptEncDec == 0 && strMACResponse.equalsIgnoreCase(outData[0])) {
                Log.e(TAG, "MAC->Response:" + outData[0]);
                appendText("CALC MAC->Response==> Success:" + outData[0]);
            } else {
                appendErrorText("CALC MACResponse Fail errorCode==>" + dukptEncDec + "\n Result==>" + outData[0] + "\n Correct Response==>" + strMACResponse);
                return false;
            }

            //Data
            dukptEncDec = mSystemBinderService.dukptEncDec(KEY_INDEX_DUKPT_INDEX_PIN_29, 4, "343031323334353637383930394439383700000000000000 ", outData, "0000000000000000", 0x81, 0);
            Log.e(TAG, "Correct Data:" + strData);
            Log.e(TAG, "Actual calculation:" + outData[0]);
            if (dukptEncDec == 0 && strData.equalsIgnoreCase(outData[0])) {
                Log.e(TAG, "CALC Data==> Success::" + outData[0]);
                appendText("CALC Data==> Success:" + outData[0]);
            } else {
                appendErrorText("CALC Data Fail errorCode==>" + dukptEncDec + "\n Result==>" + outData[0] + "\n Correct Data==>" + strData);
                return false;
            }

            boolean ksnPlusOne = mSystemBinderService.dukptIncreasesKSN(KEY_INDEX_DUKPT_INDEX_PIN_29);
            Log.e(TAG, "ksnPlusOne:" + ksnPlusOne);
            appendText("ksn++" + (ksnPlusOne ? "success" : "failure"));

            return ksnPlusOne;


        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(ksnNumber, e.getMessage());
        }
        return false;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutdown();
    }

}
