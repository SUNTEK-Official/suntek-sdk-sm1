package com.szanfu.sdk.test.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.pinpad.IPinPadCallback;
import com.szanfu.sdk.api.pinpad.KcvInfoParam;
import com.szanfu.sdk.api.pinpad.KeyInfoParam;
import com.szanfu.sdk.api.pinpad.PinPadOptions;
import com.szanfu.sdk.api.pinpad.PosCalcDesResult;
import com.szanfu.sdk.api.pinpad.PosGetKcvResult;
import com.szanfu.sdk.api.pinpad.PosGetMacResult;
import com.szanfu.sdk.api.pinpad.PstKcvInfoIn;
import com.szanfu.sdk.api.pinpad.PstKeyInfoIn;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.entity.WorkingKeyEntity;
import com.szanfu.sdk.test.util.AFUtils;
import com.szanfu.sdk.test.util.HexSupport;
import com.szanfu.sdk.test.util.KCVUtil;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.TriDES;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;


public class PinPadServiceActivity extends ListButtonBaseActivity {

    private static final String TAG = "PinPadServiceActivity";
    final String PREFIX_CALLACK = "==>> ";

    /////////////////////////////////////////////////////////////////////////////
    public static final String hexStringPlaintextKey    = "11111111111111111111111111111111";
    //    Transmission key  POS_TLK
    public static final String hexStringPlaintextKeyTLK = "11111111111111111111111111111111";
    //    Master key  POS_TMK
    public static final String hexStringPlaintextKeyTMK = "22222222222222222222222222222222"; // TMK Plaintext data
    public static final String encryptionTMK = "950973182317F80B950973182317F80B";   // DES Result        00962B60AA556E65

    public static final String encryptionTPK = "DF801B5B86D24F71DF801B5B86D24F71";   // TPK Ciphertext: 	Secret key：33333333333333333333333333333333  Data 0000000000000000 	---》 DES Result  ：ADC67D8473BF2F06
    public static final String encryptionTAK = "083F38779D5F384C083F38779D5F384C";   // TAK Ciphertext: 	Secret key：44444444444444444444444444444444  Data 0000000000000000	    ---》 DES Result  ：E2F2434039AA34AC
    public static final String encryptionTCK = "4856617624D761524856617624D76152";   // TCK Ciphertext: 	Secret key：55555555555555555555555555555555  Data 0000000000000000     ---》 DES Result  ：0CD7DC4903A8B88B


    public static final String hexStringPlaintextKeyTPK = "44444444444444444444444444444444";
    public static final String hexStringPlaintextKeyTDK = "66666666666666666666666666666666";

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    //srcKeyIdx  Source key index，（0 or 2~10） : usScrKeyIdx =0,Indicate plaintext writing
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    final int MINIMUM_VALUE_RANGE = 2;
    final int MAXIMUM_VALUE_RANGE = 10;
    //      ucKeyIdx         Destination key index [2~10]  Destination key index [2~10]
    public static final int ucSrcKeyIdx = 8;
    public static final int ucDstKeyIdx = 8;


    /////////////////////////////////////////////////////////////////////////////


    public static final int POSITION_LOAD_TLK_KEY = 0;
    public static final int POSITION_LOAD_NULL1_PLAINTEXT_KEY = 1; // ******
    public static final int POSITION_LOAD_TMK_Plaintext_KEY = 2; // Plaintext TMK
    public static final int POSITION_LOAD_TMK_Ciphertext_KEY = 3; // Ciphertext TMK
    public static final int POSITION_LOAD_NULL2_PLAINTEXT_KEY = 4; // ******
    public static final int POSITION_LOAD_TPK_KEY = 5; // TPK
    public static final int POSITION_LOAD_TAK_KEY = 6; // TAK
    public static final int POSITION_LOAD_TDK_KEY = 7; // TDK
    public static final int POSITION_LOAD_TCK_KEY = 8; // TCK
    public static final int POSITION_LOAD_NULL3_PLAINTEXT_KEY = 9; // ******
    public static final int POSITION_ALG_DES = 10;
    public static final int POSITION_GET_MAC = 11;
    public static final int POSITION_START_PINPAD = 12;
    public static final int POSITION_START_RANDOM_KEYPAD_PINPAD = 13;
    public static final int LANGUAGE_CUSTOM_PINPAD = 14;
    public static final int LANGUAGE_CUSTOM_PINPAD_RANDOM_KEYPAD = 15;

    private static final String POS_GET_MAC_TEST_DATA = "31323334353637383930414243444546";
    private static final int DEFAULT_POS_GET_MAC_MODE = 0x04;
    private static final String[] POS_GET_MAC_MODE_NAMES = new String[]{
            "0x00 DES CBC-First_GetMac",
            "0x01 DES ECB-First_GetMac",
            "0x02 DES CBC-QCUP_GetMac",
            "0x03 DES ECB-QCUP_GetMac",
            "0x04 DES CBC-ANSIX9.19",
            "0x05 DES ECB-ANSIX9.19",
            "0x06 DES CBC-ANSIX9.19 8-byte key",
            "0x07 DES ECB-ANSIX9.19 8-byte key"
    };
    private static final int PIN_PAD_KEYPAD_LAYOUT_SEQUENTIAL = 0;
    private static final int PIN_PAD_KEYPAD_LAYOUT_RANDOM = 1;

    private String[] mButtons;
    private IPinPadBinderService mPinPadBinderService;

    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        mButtons = getResources().getStringArray(R.array.PINPAD_TEST);
        setButtonDataSource(mButtons);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mPinPadBinderService = IPinPadBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_PINPAD));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {

            case POSITION_LOAD_TLK_KEY:
//                TLk  Load the transmission key
                writeTLKKey(ucSrcKeyIdx, hexStringPlaintextKey);
                appendInfoText("*******Write TLK done****");
                break;
            case POSITION_LOAD_TMK_Plaintext_KEY:
//              Plaintext import TMK
                writeTMKPlaintextKey(ucSrcKeyIdx, hexStringPlaintextKeyTMK);
                appendInfoText("*******Write TMK done****");
                break;
            case  POSITION_LOAD_TMK_Ciphertext_KEY:
//              Before importing the ciphertext into TMK, it must first be imported into TLK
                writeTLKKey(ucSrcKeyIdx, hexStringPlaintextKeyTLK);
                appendInfoText("*******Write TLK ****");
//              Import the ciphertext into TMK
                byte[]  bTMKEncryptionKey= HexSupport.toBytesFromHex(encryptionTMK); // "950973182317F80B950973182317F80B"
                appendInfoText("bTMKEncryptionKey : "+encryptionTMK +"\n"+"KCV ： "+ "00962B60AA556E65");
                writeTMKEncryptionKey(ucSrcKeyIdx,ucDstKeyIdx, bTMKEncryptionKey);
                appendInfoText("*******Write TMK Enc done****");
                break;
            case POSITION_LOAD_TPK_KEY:
//                Before loading TPK, TLK must be loaded first and the ciphertext must be loaded with TMK
                writeTLKKey(ucSrcKeyIdx, hexStringPlaintextKeyTLK);
                appendInfoText("*******Write TLK ****");
//              Ciphertext loads TMK
                byte[]  bTMKEncryptionKey_TPK= HexSupport.toBytesFromHex(encryptionTMK); // "950973182317F80B950973182317F80B"
                appendInfoText("bTMKEncryptionKey : "+encryptionTMK +"\n"+"KCV ： "+ "00962B60AA556E65");
                writeTMKEncryptionKey(ucSrcKeyIdx,ucDstKeyIdx, bTMKEncryptionKey_TPK);
                appendInfoText("*******Write TMK ****");

                appendInfoText("bTPKEncryptionKey : "+encryptionTPK );
                appendInfoText("KCV ： "+ "ADC67D8473BF2F06");

                byte[]  bTPKEncryptionKey= HexSupport.toBytesFromHex(encryptionTPK); // "DF801B5B86D24F71DF801B5B86D24F71"
                writeTWKEncryptionKey(ucSrcKeyIdx,WorkingKeyEntity.PIN_KEY_ID, bTPKEncryptionKey);
                appendInfoText("*******Write TPK done****");

                break;
            case POSITION_LOAD_TAK_KEY:
//                Before loading TAK, TLK must be loaded first and the ciphertext must be loaded with TMK
                writeTLKKey(ucSrcKeyIdx, hexStringPlaintextKeyTLK);

//              Ciphertext loads TMK
                byte[]  bTMKEncryptionKey_TAK= HexSupport.toBytesFromHex(encryptionTMK); // "950973182317F80B950973182317F80B"
                appendInfoText("bTMKEncryptionKey : "+encryptionTMK +"\n"+"KCV ： "+ "00962B60AA556E65");
                writeTMKEncryptionKey(ucSrcKeyIdx,ucDstKeyIdx, bTMKEncryptionKey_TAK);
                appendInfoText("*******Write TPK done****");
                appendInfoText("bTPKEncryptionKey : "+encryptionTAK +"\n"+"KCV ： "+ "E2F2434039AA34AC");

                byte[]  bTAKEncryptionKey= HexSupport.toBytesFromHex(encryptionTAK); // "083F38779D5F384C083F38779D5F384C"
                writeTWKEncryptionKey(ucSrcKeyIdx,WorkingKeyEntity.MAC_KEY_ID, bTAKEncryptionKey);

                appendInfoText("*******Write TAK done****");
                break;
            case POSITION_LOAD_TDK_KEY:
//                Before loading TDK, TLK must be loaded first and the ciphertext must be loaded with TMK
                writeTLKKey(ucSrcKeyIdx, hexStringPlaintextKeyTLK);
                appendInfoText("*******Write TLK done****");

//              Ciphertext loads TMK
                byte[]  bTMKEncryptionKey_TDK= HexSupport.toBytesFromHex(encryptionTMK); // "950973182317F80B950973182317F80B"
                appendInfoText("bTMKEncryptionKey : "+encryptionTMK +"\n"+"KCV ： "+ "00962B60AA556E65");
                writeTMKEncryptionKey(ucSrcKeyIdx,ucDstKeyIdx, bTMKEncryptionKey_TDK);
                appendInfoText("*******Write TMK done****");

                appendInfoText("bTPKEncryptionKey : "+encryptionTPK +"\n"+"KCV ： "+ "ADC67D8473BF2F06");

                byte[]  bTDKEncryptionKey= HexSupport.toBytesFromHex(encryptionTPK); // "DF801B5B86D24F71DF801B5B86D24F71"
                writeTWKEncryptionKey(ucSrcKeyIdx,WorkingKeyEntity.TDK_KEY_ID, bTDKEncryptionKey);

                appendInfoText("*******Write TDK done****");
                break;
            case POSITION_LOAD_TCK_KEY:
//                Before loading TCK, TLK must be loaded first and the ciphertext must be loaded with TMK
                writeTLKKey(ucSrcKeyIdx, hexStringPlaintextKeyTLK);
                appendInfoText("*******Write TLK done****");
//              Ciphertext loads TMK
                byte[]  bTMKEncryptionKey_TCK= HexSupport.toBytesFromHex(encryptionTMK); // "950973182317F80B950973182317F80B"
                appendInfoText("bTMKEncryptionKey : "+encryptionTMK +"\n"+"KCV ： "+ "00962B60AA556E65");
                writeTMKEncryptionKey(ucSrcKeyIdx,ucDstKeyIdx, bTMKEncryptionKey_TCK);
                appendInfoText("*******Write TMK done****");

                appendInfoText("bTPKEncryptionKey : "+encryptionTCK );
                appendInfoText("KCV ： "+ "0CD7DC4903A8B88B");

                byte[]  bTCKEncryptionKey= HexSupport.toBytesFromHex(encryptionTCK); // "4856617624D761524856617624D76152"
                writeTWKEncryptionKey(ucSrcKeyIdx,WorkingKeyEntity.TCK_KEY_ID, bTCKEncryptionKey);

             /*   KcvInfoParam kcvInfoParamTCK = new KcvInfoParam();
                kcvInfoParamTCK.setCheckMode(0);
                appendInfoText("bTCKEncryptionKey : "+encryptionTCK );
                appendInfoText("KCV ： "+ "0CD7DC4903A8B88B");
                byte[]  bTCKEncryptionKey= HexSupport.toBytesFromHex(encryptionTCK); // 4856617624D761524856617624D76152
                int ucKeyLenTCK =bTCKEncryptionKey.length;
                int ucModeTCK = 0x01;
                writeWorkKey(WorkingKeyEntity.TCK_KEY_ID,ucKeyLenTCK,bTCKEncryptionKey,kcvInfoParamTCK, ucModeTCK,keyindex);*/

                appendInfoText("*******Write TCK done****");
                break;
            case POSITION_ALG_DES:
                posCalcDes();
                break;
            case POSITION_GET_MAC:
                showPosGetMacModeDialog();
                break;
            case POSITION_START_PINPAD:
                showPinPad();
                break;
            case POSITION_START_RANDOM_KEYPAD_PINPAD:
                showPinPadRandomKeypadDialog();
                break;
            case LANGUAGE_CUSTOM_PINPAD:
                showCustomPinPad();
                break;
            case LANGUAGE_CUSTOM_PINPAD_RANDOM_KEYPAD:
                showCustomPinPadRandomKeypadDialog();
                break;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////





    /**
     * encrypt the data using assigned key
     *
     * @param ucSrcKeyIdx     key index
     * @param keyId           key type
     * @param dataIn_8_align  data, must be 8 bytes align
     * @return null: failed, otherwise the encrypted value
     */
    byte[] pinpad_CalcDes(int ucSrcKeyIdx, int keyId, byte[] dataIn_8_align) {

        if (ucSrcKeyIdx < MINIMUM_VALUE_RANGE || ucSrcKeyIdx > MAXIMUM_VALUE_RANGE) {
            appendErrorText("invalid parameter");
            return null;
        }

        appendInfoText("pinpad_CalcDes");
        appendText("   ucSrcKeyIdx : " + ucSrcKeyIdx);
        appendText("   keyId : " + keyId);
        appendText("   dataIn_8_align : " + HexSupport.toHexFromBytes(dataIn_8_align));

        try {
            int ecbCbcMode = 0x00; //ecb mode
            byte[] iv = new byte[8]; //for cbc mode only

            int keyType = keyId;

            int algIde = 0x02;    //alg id, TDES, don't change this value
            int mode = 0x01;      //enc

            /**
             * PosCalcDesResult posCalcDes(int usKeyIdx, int keyType, int algIde, int ecbCbcMode, in byte[] dataIn, int mode, in byte[] iv);
             *
             * 使用秘钥进行加解密计算
             * @param usKeyIdx 秘钥组序号,合法索引值为(2-10)
             * @param keyType 秘钥类型
             *               POS_TMK    	0x02 //master key
             *               POS_TPK        0x03 //PinKey
             *               POS_TAK    	0x04 //MacKey
             *               POS_TDK    	0x05 //TDEA decrypt key
             *               POS_TCK    	0x06 // account encryption or decryption key
             * @param algIde  01：DES
             *                02：TDES
             *                40：SM4
             *                00：Default:DES 或者 3DES ，根据使用秘钥的长度决定
             * @param ecbCbcMode 算法类型 00: ECB
             *                          01: CBC
             * @param dataIn 要解密/解密的数据
             * @param mode 加解密模式  00：解密
             *                        01：加密
             * @param iv    IV数据，当ECB_CBC_Mode为1时生效
             */
            PosCalcDesResult posCalcDesResult = mPinPadBinderService.posCalcDes(ucSrcKeyIdx, keyType, algIde, ecbCbcMode, dataIn_8_align, mode, iv);
            int ret = posCalcDesResult.getRet();
            appendTextResult(ret, "   ret=" + ret);
            if (ret == 0) {
                byte out[] = posCalcDesResult.getDataOut();
                appendText("   enc=" + HexSupport.toHexFromBytes(out));
                return out;
            } else {
                return null;
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("err: " + e.getMessage());
            return null;
        }
    }

    public static String dataFormat(String pkgData) {
        LogUtils.i("数据处理8的倍数 dataFormat: " + pkgData);
        int pkgDataLen = pkgData.length();
        LogUtils.i("pkgDataLen: " + pkgDataLen);
        int missingLen = pkgDataLen % 16;
        if (missingLen != 0) {
            missingLen = 16 - missingLen;
        }
        LogUtils.i("需要补0的位数:" + missingLen);
        String rightPadRest = StringUtils.rightPad(pkgData, pkgDataLen + missingLen, "0");
        LogUtils.i("rightPadRest:" + rightPadRest);

        return rightPadRest;
    }

    /**
     * The data used to calculate MAC must be a multiple of 8 bytes. If the condition is not met, you need to fill the data to an integer multiple of 8 bytes
     *
     * @param pkgData parameter MAC calculation data
     * @param padStr  character to be filled
     * @return data that meets the requirements
     */
    public static String dataFormat(String pkgData, String padStr) {
        int pkgDataLen = pkgData.length();
        int missingLen = pkgDataLen % 16;
        if (missingLen != 0) {
            missingLen = 16 - missingLen;
        }
        String rightPadRest = StringUtils.rightPad(pkgData, pkgDataLen + missingLen, padStr);

        return rightPadRest;
    }

    void test_enc_Track2_F() {
        String plainData = "6074841230000000021D261262036700000";  //plaintext track2
        try {
            plainData = plainData.replace("D", "=");
            byte p2[] = plainData.getBytes();
            plainData = HexSupport.toHexFromBytes(p2);

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int rem = (plainData.length() / 2) % 8;
            int iterate = 0;
            if (rem > 0) {
                iterate = 8 - rem;
            }

            String planTrack2Data11 = plainData;

            //make the multiple of 8
            for (int i = 0; i < iterate; i++) {
                if (i == 0)
                    planTrack2Data11 = planTrack2Data11.concat("80");
                else
                    planTrack2Data11 = planTrack2Data11.concat("00");
            }

            //track2_before_enc=363037343834313233303030303030303032313d3236313236323033363730303030308000000000
            LogUtils.d("track2_before_enc=" + planTrack2Data11);

            //use M/S method and TDKEY to encrypt the whole track
            appendText("*** getEncTrack()");
            appendText("    data=" + planTrack2Data11);
            appendText("    ucSrcKeyIdx=" + ucSrcKeyIdx);
            byte outData_hex[] = pinpad_CalcDes(ucSrcKeyIdx, WorkingKeyEntity.TDK_KEY_ID, HexSupport.toBytesFromHex(planTrack2Data11));
            if (outData_hex == null) {
                appendErrorText("encTrack failed");
                return;
            }

            String outData = HexSupport.toHexFromBytes(outData_hex);
            //outData is the encrypted track2
            appendText("    encTrack=" + outData);

            //For test only
            try {
                byte[] dec_data_hex = TriDES.des3DecodeECB(HexSupport.toBytesFromHex(hexStringPlaintextKeyTDK), HexSupport.toBytesFromHex(outData));
                String dec_data = HexSupport.toHexFromBytes(dec_data_hex);
                LogUtils.d("planTrack2Data11(t2 before enc)=" + planTrack2Data11);
                LogUtils.d( "dec_data(t2 dec by hsm)=" + dec_data);
                if (planTrack2Data11.equalsIgnoreCase(dec_data)) {
                    appendText("match");
                } else {
                    appendErrorText("!!! unmatch !!!");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void test_enc_Track2_PKCS5_Padding_TDES_ECB() {
        appendText("test_enc_Track2_PKCS5_Padding_TDES_ECB()");

        /*
        byte tag57[] = null;    //Track 2 Equivalent Data
        String plainData = HexSupport.toHexFromBytes(tag57).toUpperCase();
        plainData = plainData.replace("F", ""); //remove the end 'F'
        */

        String plainData = "6074841230000000021D261262036700000";  //plaintext track2
        try {
            plainData = plainData.replace("D", "=");
            //"6074841230000000021=261262036700000"

            byte p2[] = plainData.getBytes();
            plainData = HexSupport.toHexFromBytes(p2);
            //"363037343834313233303030303030303032313D323631323632303336373030303030"

            //PKCS5 padding
            int len = plainData.length() / 2;
            int padding_len = 8 - (len % 8);
            byte padding[] = new byte[padding_len];
            Arrays.fill(padding, (byte) padding_len);
            plainData = plainData + HexSupport.toHexFromBytes(padding);

            //track2_before_enc=363037343834313233303030303030303032313D3236313236323033363730303030300505050505
            LogUtils.d( "track2_before_enc=" + plainData);

            //use M/S method and TDKEY to encrypt the whole track (ECB)
            appendText("*** getEncTrack()");
            appendText("    data=" + plainData);
            appendText("    ucSrcKeyIdx=" + ucSrcKeyIdx);
            byte outData_hex[] = pinpad_CalcDes(ucSrcKeyIdx, WorkingKeyEntity.TDK_KEY_ID, HexSupport.toBytesFromHex(plainData));
            if (outData_hex == null) {
                appendErrorText("encTrack failed");
                return;
            }

            String outData = HexSupport.toHexFromBytes(outData_hex);
            //outData is the encrypted track2
            appendText("    encTrack=" + outData);

            //For test only
            try {
                byte[] dec_data_hex = TriDES.des3DecodeECB(HexSupport.toBytesFromHex(hexStringPlaintextKeyTDK), HexSupport.toBytesFromHex(outData));
                String dec_data = HexSupport.toHexFromBytes(dec_data_hex);
                LogUtils.d( "plainData(t2 before enc)=" + plainData);
                LogUtils.d( "dec_data(t2 dec by hsm)=" + dec_data);
                if (plainData.equalsIgnoreCase(dec_data)) {
                    appendText("match");
                } else {
                    appendErrorText("!!! unmatch !!!");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void posCalcDes() {
        test_enc_Track2_F();
        test_enc_Track2_PKCS5_Padding_TDES_ECB();
    }

    void showPosGetMacModeDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("Select MAC Algorithm");
        alertBuilder.setSingleChoiceItems(POS_GET_MAC_MODE_NAMES, DEFAULT_POS_GET_MAC_MODE, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int mode) {
                dialogInterface.dismiss();
                posGetMac(mode);
            }
        });
        alertBuilder.setNegativeButton(android.R.string.cancel, null);
        alertBuilder.create().show();
    }

    void showPinPadRandomKeypadDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.pinpad_select_keypad_layout);
        alertBuilder.setSingleChoiceItems(getPinPadKeypadLayoutNames(), PIN_PAD_KEYPAD_LAYOUT_SEQUENTIAL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                showPinPadWithRandomKeypad(which == PIN_PAD_KEYPAD_LAYOUT_RANDOM);
            }
        });
        alertBuilder.setNegativeButton(android.R.string.cancel, null);
        alertBuilder.create().show();
    }

    void showCustomPinPadRandomKeypadDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle(R.string.pinpad_select_keypad_layout);
        alertBuilder.setSingleChoiceItems(getPinPadKeypadLayoutNames(), PIN_PAD_KEYPAD_LAYOUT_SEQUENTIAL, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
                showCustomPinPadWithRandomKeypad(which == PIN_PAD_KEYPAD_LAYOUT_RANDOM);
            }
        });
        alertBuilder.setNegativeButton(android.R.string.cancel, null);
        alertBuilder.create().show();
    }

    private String[] getPinPadKeypadLayoutNames() {
        return getResources().getStringArray(R.array.PIN_PAD_KEYPAD_LAYOUT_NAMES);
    }

    private String getPinPadKeypadLayoutName(boolean randomKeypad) {
        String[] names = getPinPadKeypadLayoutNames();
        int index = randomKeypad ? PIN_PAD_KEYPAD_LAYOUT_RANDOM : PIN_PAD_KEYPAD_LAYOUT_SEQUENTIAL;
        if (names.length > index) {
            return names[index];
        }
        return randomKeypad ? "Random keypad" : "Sequential keypad";
    }

    void posGetMac(int mode) {
        if (mPinPadBinderService == null) {
            appendErrorText("pinpad service not connected");
            return;
        }

        String dataInHex = dataFormat(POS_GET_MAC_TEST_DATA);
        byte[] dataIn = HexSupport.toBytesFromHex(dataInHex);
        int dataInLen = dataIn.length;

        appendInfoText("posGetMac");
        appendText("   keyIdx=" + ucSrcKeyIdx);
        appendText("   dataIn=" + dataInHex);
        appendText("   dataInLen=" + dataInLen);
        appendText("   mode=" + POS_GET_MAC_MODE_NAMES[mode]);

        try {
            PosGetMacResult posGetMacResult = mPinPadBinderService.posGetMac(ucSrcKeyIdx, dataIn, dataInLen, mode);
            if (posGetMacResult == null) {
                appendErrorText("posGetMac result is null");
                return;
            }
            int ret = posGetMacResult.getRet();
            appendTextResult(ret, "   ret=" + ret);
            if (ret == 0) {
                byte[] mac = posGetMacResult.getMac();
                appendText("   mac=" + HexSupport.toHexFromBytes(mac));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("posGetMac RemoteException :" + e.getMessage());
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    void DecryptPinBlock(String pinblock_enc, String card_pan) {
        appendInfoText("DecryptPinBlock");

        try {
            byte[] pinblock_plain = TriDES.des3DecodeECB(HexSupport.toBytesFromHex(hexStringPlaintextKeyTPK), HexSupport.toBytesFromHex(pinblock_enc));

            LogUtils.d( "pinblock_enc=" + pinblock_enc);
            LogUtils.d( "pinblock_plain=" + HexSupport.toHexFromBytes(pinblock_plain));
            LogUtils.d( "PAN=" + card_pan);

            int pan_len = card_pan.length();
            String pan_slice = card_pan.substring(pan_len - 13, pan_len - 1);
            LogUtils.d( "PAN_slice=" + pan_slice);

            byte pan_slice_hex[] = new byte[8];
            Arrays.fill(pan_slice_hex, (byte) 0x00);
            System.arraycopy(HexSupport.toBytesFromHex(pan_slice), 0, pan_slice_hex, 2, 6);
            LogUtils.d( "PAN_slice_hex=" + HexSupport.toHexFromBytes(pan_slice_hex));

            for (int i = 0; i < 8; i++)
                pinblock_plain[i] = (byte) (pinblock_plain[i] ^ pan_slice_hex[i]);
            LogUtils.d( "(pinblock_plain xor pan_slice_hex)=" + HexSupport.toHexFromBytes(pinblock_plain));

            appendText("pin=" + HexSupport.toHexFromBytes(pinblock_plain));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    int inputPinResult;

    void showPinPad() {
        try {
            if (mPinPadBinderService == null) {
                appendErrorText("pinpad service not connected");
                return;
            }

            //whether show 'CANCEL' button on pinpad
            boolean UI_show_Cancel = true;

            //bypass PIN flag
            //   0: can't bypass PIN
            //   1: can bypass PIN (press OK directly)
            int enable_bypass_pin = 1;

            int min_pin_length = 6;
            int max_pin_length = 6;

            String cardNumber = "6226552152848003";

            String amount = "0.00";  //reserved

            int encryptionMode = 0;  //0:DES/3DES  1:SM4

            //timeout,  valid [5, 180] seconds
            int timeout_second = 60;

            //1:online PIN   0:offline PIN
            int pin_type = 1;

            appendText("showPinPad");
            appendText("  ucSrcKeyIdx=" + ucSrcKeyIdx);
            appendText("  enable_bypass_pin=" + enable_bypass_pin);
            appendText("  min_pin_length=" + min_pin_length);
            appendText("  max_pin_length=" + max_pin_length);
            appendText("  UI_show_Cancel=" + UI_show_Cancel);
            appendText("  cardNumber=" + cardNumber);
            appendText("  encryptionMode=" + encryptionMode + " (" + ((encryptionMode == 0) ? "DES/3DES" : "SM4") + ")");
            appendText("  timeout_second=" + timeout_second);
            appendText("  pin_type=" + pin_type + " (" + ((pin_type == 0) ? "offline PIN" : "online PIN") + ")");

//            mPinPadBinderService.setOption(PinPadOptions.UI_PIN_BOX_SHOW,PinPadOptions.OPTION_ENABLE);
            /**
             * void showPinPad(in int keyIndex,in int[] setPasswordLengthLimit,in boolean hasCancelButton ,in String amount, in String cardNumber, in int encryptionMode,in int inputPwdTimeout,in int ucPinType,in IPinPadCallback pinPadCallback);
             * 用户输入密码弹窗
             * @param ucSrcKeyIdx                 秘钥索引,可供用户操作9组,合法索引值为2-10
             * @param setPasswordLengthLimit   设置PinPad参数，是否支持空密码和支持输入密码长度位数,有效值[bypass_pin, min_length,max_length]
             *                                 第一位0不允许直接按OK 1：允许直接按OK
             * @param hasCancelButton   是否需要CANCEL按键 true：需要 false:不需要 【只对930设备生效】
             * @param amount          交易金额
             * @param cardNumber      银行卡号,长度在12-19之间
             * @param encryptionMode  加密模式 0:DES/3DES 1:SM4
             * @param inputPwdTimeout 输入密码超时时间，单位S，最长不应超过3分钟,最短不建议少于5秒，从密码键盘显示开始计时
             * @param ucPinType
             * @param pinPadCallback  输入密码相关监听回调
             */
            mPinPadBinderService.showPinPad(ucSrcKeyIdx,
                    new int[]{enable_bypass_pin, min_pin_length, max_pin_length},
                    UI_show_Cancel,
                    amount,
                    cardNumber,
                    encryptionMode,
                    timeout_second,
                    pin_type,
                    new IPinPadCallback.Stub() {
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
                                DecryptPinBlock(pinblock, cardNumber);
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
                                LogUtils.e( "ucPinType：" + AFUtils.showResult0xStr(new byte[]{ucPinType}));
                                LogUtils.e( "ucLastTimes：" + AFUtils.showResult0xStr(new byte[]{ucLastTimes}));

                                currentCardNumber = getTagValue(0x5A);

                                LogUtils.i( "onEmvInputPIN:currentCardNumber-> " + currentCardNumber);
                                LogUtils.e( "ucPinType: " + (ucPinType & 0xFF));
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

    void showPinPadWithRandomKeypad(boolean randomKeypad) {
        try {
            if (mPinPadBinderService == null) {
                appendErrorText("pinpad service not connected");
                return;
            }

            //whether show 'CANCEL' button on pinpad
            boolean UI_show_Cancel = true;

            //bypass PIN flag
            //   0: can't bypass PIN
            //   1: can bypass PIN (press OK directly)
            int enable_bypass_pin = 1;

            int min_pin_length = 6;
            int max_pin_length = 6;

            String cardNumber = "6226552152848003";

            String amount = "0.00";  //reserved

            int encryptionMode = 0;  //0:DES/3DES  1:SM4

            //timeout,  valid [5, 180] seconds
            int timeout_second = 60;

            //1:online PIN   0:offline PIN
            int pin_type = 1;

            appendText("showPinPadWithRandomKeypad");
            appendText("  ucSrcKeyIdx=" + ucSrcKeyIdx);
            appendText("  enable_bypass_pin=" + enable_bypass_pin);
            appendText("  min_pin_length=" + min_pin_length);
            appendText("  max_pin_length=" + max_pin_length);
            appendText("  UI_show_Cancel=" + UI_show_Cancel);
            appendText("  cardNumber=" + cardNumber);
            appendText("  encryptionMode=" + encryptionMode + " (" + ((encryptionMode == 0) ? "DES/3DES" : "SM4") + ")");
            appendText("  timeout_second=" + timeout_second);
            appendText("  pin_type=" + pin_type + " (" + ((pin_type == 0) ? "offline PIN" : "online PIN") + ")");
            appendText("  randomKeypad=" + randomKeypad + " (" + getPinPadKeypadLayoutName(randomKeypad) + ")");


//            mPinPadBinderService.setOption(PinPadOptions.UI_PIN_BOX_SHOW,PinPadOptions.OPTION_ENABLE);
            /**
             * void showPinPadWithRandomKeypad(in int keyIndex,in int[] setPasswordLengthLimit,in boolean hasCancelButton ,in String amount, in String cardNumber, in int encryptionMode,in int inputPwdTimeout,in int ucPinType,in boolean randomKeypad,in IPinPadCallback pinPadCallback);
             * 用户输入密码弹窗
             * @param ucSrcKeyIdx                 秘钥索引,可供用户操作9组,合法索引值为2-10
             * @param setPasswordLengthLimit   设置PinPad参数，是否支持空密码和支持输入密码长度位数,有效值[bypass_pin, min_length,max_length]
             *                                 第一位0不允许直接按OK 1：允许直接按OK
             * @param hasCancelButton   是否需要CANCEL按键 true：需要 false:不需要 【只对930设备生效】
             * @param amount          交易金额
             * @param cardNumber      银行卡号,长度在12-19之间
             * @param encryptionMode  加密模式 0:DES/3DES 1:SM4
             * @param inputPwdTimeout 输入密码超时时间，单位S，最长不应超过3分钟,最短不建议少于5秒，从密码键盘显示开始计时
             * @param ucPinType
             * @param randomKeypad     true: random keypad layout, false: sequential keypad layout
             * @param pinPadCallback  输入密码相关监听回调
             */
            mPinPadBinderService.showPinPadWithRandomKeypad(ucSrcKeyIdx,
                    new int[]{enable_bypass_pin, min_pin_length, max_pin_length},
                    UI_show_Cancel,
                    amount,
                    cardNumber,
                    encryptionMode,
                    timeout_second,
                    pin_type,
                    randomKeypad,
                    new IPinPadCallback.Stub() {
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
                                DecryptPinBlock(pinblock, cardNumber);
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
                                LogUtils.e( "ucPinType：" + AFUtils.showResult0xStr(new byte[]{ucPinType}));
                                LogUtils.e( "ucLastTimes：" + AFUtils.showResult0xStr(new byte[]{ucLastTimes}));

                                currentCardNumber = getTagValue(0x5A);

                                LogUtils.i( "onEmvInputPIN:currentCardNumber-> " + currentCardNumber);
                                LogUtils.e( "ucPinType: " + (ucPinType & 0xFF));
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


    void showCustomPinPad() {
        try {
            //whether show 'CANCEL' button on pinpad
            boolean UI_show_Cancel = true;

            //bypass PIN flag
            //   0: can't bypass PIN
            //   1: can bypass PIN (press OK directly)
            int enable_bypass_pin = 1;

            int min_pin_length = 6;
            int max_pin_length = 6;

            String cardNumber = "6074841230000000021";

            String amount = "0.00";  //reserved

            int encryptionMode = 0;  //0:DES/3DES  1:SM4

            //timeout,  valid [5, 180] seconds
            int timeout_second = 60;

            //1:online PIN   0:offline PIN
            int pin_type = 1;
            // Input prompt text
            String inputPrompt = "Сума транзакцыі";
            // Currency code ISO_4217 (e.g., "CNY")
            String currencyType = "RUB";
            // Online transaction prompt
            String onlineMessage = "Калі ласка, увядзіце пароль ад вашай онлайн-транзакцыі";
            // Offline transaction prompt */
            String offlineMessage = "Калі ласка, увядзіце пароль ад вашай афлайн-транзакцыі";

            appendText("showPinPad");
            appendText("  ucSrcKeyIdx=" + ucSrcKeyIdx);
            appendText("  enable_bypass_pin=" + enable_bypass_pin);
            appendText("  min_pin_length=" + min_pin_length);
            appendText("  max_pin_length=" + max_pin_length);
            appendText("  UI_show_Cancel=" + UI_show_Cancel);
            appendText("  cardNumber=" + cardNumber);
            appendText("  encryptionMode=" + encryptionMode + " (" + ((encryptionMode == 0) ? "DES/3DES" : "SM4") + ")");
            appendText("  timeout_second=" + timeout_second);
            appendText("  pin_type=" + pin_type + " (" + ((pin_type == 0) ? "offline PIN" : "online PIN") + ")");

//            mPinPadBinderService.setOption(PinPadOptions.UI_PIN_BOX_SHOW,PinPadOptions.OPTION_DISABLE);

            /**
             * void showPinPad(in int keyIndex,in int[] setPasswordLengthLimit,in boolean hasCancelButton ,in String amount,in int cardType, in String cardNumber, in int encryptionMode,in int inputPwdTimeout,in int ucPinType,in IPinPadCallback pinPadCallback);
             * 用户输入密码弹窗
             * @param keyIndex                 秘钥索引,可供用户操作9组,合法索引值为2-10
             * @param setPasswordLengthLimit   设置PinPad参数，是否支持空密码和支持输入密码长度位数,有效值[bypass_pin, min_length,max_length]
             *                                 第一位0不允许直接按OK 1：允许直接按OK
             * @param hasCancelButton   是否需要CANCEL按键 true：需要 false:不需要 【只对930设备生效】
             * @param amount          交易金额
             * @param cardNumber      银行卡号,长度在12-19之间
             * @param encryptionMode  加密模式 0:DES/3DES 1:SM4
             * @param inputPwdTimeout 输入密码超时时间，单位S，最长不应超过3分钟,最短不建议少于5秒，从密码键盘显示开始计时
             * @param ucPinType
             * @param pinPadCallback  输入密码相关监听回调
             */
            mPinPadBinderService.showCustomPinPad(ucSrcKeyIdx,
                    new int[]{enable_bypass_pin, min_pin_length, max_pin_length},
                    UI_show_Cancel,
                    amount,
                    cardNumber,
                    encryptionMode,
                    timeout_second,
                    pin_type,
                    inputPrompt,
                    currencyType,
                    onlineMessage,
                    offlineMessage,
                    new IPinPadCallback.Stub() {
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
                                DecryptPinBlock(pinblock, cardNumber);
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
                                LogUtils.e( "ucPinType：" + AFUtils.showResult0xStr(new byte[]{ucPinType}));
                                LogUtils.e( "ucLastTimes：" + AFUtils.showResult0xStr(new byte[]{ucLastTimes}));

                                currentCardNumber = getTagValue(0x5A);

                                LogUtils.i( "onEmvInputPIN:currentCardNumber-> " + currentCardNumber);
                                LogUtils.e( "ucPinType: " + (ucPinType & 0xFF));
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

    void showCustomPinPadWithRandomKeypad(boolean randomKeypad) {
        try {
            if (mPinPadBinderService == null) {
                appendErrorText("pinpad service not connected");
                return;
            }

            //whether show 'CANCEL' button on pinpad
            boolean UI_show_Cancel = true;

            //bypass PIN flag
            //   0: can't bypass PIN
            //   1: can bypass PIN (press OK directly)
            int enable_bypass_pin = 1;

            int min_pin_length = 6;
            int max_pin_length = 6;

            String cardNumber = "6074841230000000021";

            String amount = "0.00";  //reserved

            int encryptionMode = 0;  //0:DES/3DES  1:SM4

            //timeout,  valid [5, 180] seconds
            int timeout_second = 60;

            //1:online PIN   0:offline PIN
            int pin_type = 1;
            // Input prompt text
            String inputPrompt = "Сума транзакцыі";
            // Currency code ISO_4217 (e.g., "CNY")
            String currencyType = "RUB";
            // Online transaction prompt
            String onlineMessage = "Калі ласка, увядзіце пароль ад вашай онлайн-транзакцыі";
            // Offline transaction prompt */
            String offlineMessage = "Калі ласка, увядзіце пароль ад вашай афлайн-транзакцыі";

            appendText("showCustomPinPadWithRandomKeypad");
            appendText("  ucSrcKeyIdx=" + ucSrcKeyIdx);
            appendText("  enable_bypass_pin=" + enable_bypass_pin);
            appendText("  min_pin_length=" + min_pin_length);
            appendText("  max_pin_length=" + max_pin_length);
            appendText("  UI_show_Cancel=" + UI_show_Cancel);
            appendText("  cardNumber=" + cardNumber);
            appendText("  encryptionMode=" + encryptionMode + " (" + ((encryptionMode == 0) ? "DES/3DES" : "SM4") + ")");
            appendText("  timeout_second=" + timeout_second);
            appendText("  pin_type=" + pin_type + " (" + ((pin_type == 0) ? "offline PIN" : "online PIN") + ")");
            appendText("  randomKeypad=" + randomKeypad + " (" + getPinPadKeypadLayoutName(randomKeypad) + ")");

//            mPinPadBinderService.setOption(PinPadOptions.UI_PIN_BOX_SHOW,PinPadOptions.OPTION_DISABLE);

            /**
             * void showCustomPinPadWithRandomKeypad(in int keyIndex,in int[] setPasswordLengthLimit,in boolean hasCancelButton,in String amount,in String cardNumber,in int encryptionMode,in int inputPwdTimeout,in int ucPinType,in String pinInputPrompt,in String currencyType,in String onlineMessage,in String offlineMessage,in boolean randomKeypad,in IPinPadCallback pinPadCallback);
             * @param randomKeypad true: random keypad layout, false: sequential keypad layout
             */
            mPinPadBinderService.showCustomPinPadWithRandomKeypad(ucSrcKeyIdx,
                    new int[]{enable_bypass_pin, min_pin_length, max_pin_length},
                    UI_show_Cancel,
                    amount,
                    cardNumber,
                    encryptionMode,
                    timeout_second,
                    pin_type,
                    inputPrompt,
                    currencyType,
                    onlineMessage,
                    offlineMessage,
                    randomKeypad,
                    new IPinPadCallback.Stub() {
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
                                DecryptPinBlock(pinblock, cardNumber);
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
                                LogUtils.e( "ucPinType：" + AFUtils.showResult0xStr(new byte[]{ucPinType}));
                                LogUtils.e( "ucLastTimes：" + AFUtils.showResult0xStr(new byte[]{ucLastTimes}));

                                currentCardNumber = getTagValue(0x5A);

                                LogUtils.i( "onEmvInputPIN:currentCardNumber-> " + currentCardNumber);
                                LogUtils.e( "ucPinType: " + (ucPinType & 0xFF));
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

    /**
     * Load the plan text TLk key
     *
     * @param ucKeyIdx    [in]      valid value [2~10]
     * @param hexStringPlaintextKey [in]   HexString of plaintext master key
     * @return 0: success
     */
    int writeTLKKey(int ucKeyIdx, String hexStringPlaintextKey) {

        if (ucKeyIdx < MINIMUM_VALUE_RANGE || ucKeyIdx > MAXIMUM_VALUE_RANGE) {
            appendErrorText("invalid parameter");
            return -1;
        }
        if (hexStringPlaintextKey == null || (hexStringPlaintextKey.length() != 32 && hexStringPlaintextKey.length() != 48)) {
            appendErrorText("invalid parameter");
            return -1;
        }

        try {
            KeyInfoParam keyInfoParam = new KeyInfoParam();
            //  // Destination Key Index (2-10)
            keyInfoParam.setDstKeyIdx((short) ucKeyIdx); //
            //  //TDES Destination key length
            keyInfoParam.setDstKeyLen(hexStringPlaintextKey.length() / 2);
            //Destination key type   POS_TLK type
            keyInfoParam.setDstKeyType((byte) WorkingKeyEntity.TLK_KEY_ID);
            // Destination key value //16 bytes plaintext main key
            keyInfoParam.setDstKeyValue(HexSupport.toBytesFromHex(hexStringPlaintextKey)); //16 bytes plaintext main key
//          ***************************************************************************************************************
            //  Source key index (0 or 2 to 10)
            keyInfoParam.setSrcKeyIdx((short)  0 ); //
            keyInfoParam.setSrcKeyType((byte) 0);   //

            KcvInfoParam kcvInfoParam = new KcvInfoParam();
            // KCV verification type 00: No KCV verification
//            0x01：Use the des or 3des algorithm to verify the hexadecimal data 0000000000000000
//	          0x02：Use the des or 3des algorithm to verify the hexadecimal data 1234567890123456
//	          0x40：Decryption is performed using the SM4 key. kcv verification has not been added yet.
            kcvInfoParam.setCheckMode(1);
            String desKcv1 = KCVUtil.computeDESKCV(hexStringPlaintextKey);
            byte[] bCheckBuf =AFUtils.hexString2ByteArray(desKcv1);
            // KCV data KCV
            kcvInfoParam.setCheckBuf(bCheckBuf);

            appendInfoText("pinpad_LoadTLK");
            appendText("   ucKeyIdx : " + ucKeyIdx);
            appendText("   Key : " + hexStringPlaintextKey);

            int ret = mPinPadBinderService.posMasterKey(keyInfoParam, kcvInfoParam);
            appendTextResult(ret, "   ret ： " + ret);
            return ret;

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("   ret=" + e.getMessage());
            return -1;
        }
    }


    /**
     * Load the plan text TMK key
     *  Load the plaintext TMK
     * @param ucKeyIdx          valid value [2~10]
     * @param hexStringPlaintextKey    HexString of plaintext master key
     * @return 0: success
     */
    int writeTMKPlaintextKey( int ucKeyIdx, String hexStringPlaintextKey) {

        LogUtils.e(" ucSrcKeyIdx : "+ucKeyIdx);
        if (ucKeyIdx < MINIMUM_VALUE_RANGE || ucKeyIdx > MAXIMUM_VALUE_RANGE) {
            appendErrorText("invalid parameter");
            return -1;
        }
        if (hexStringPlaintextKey == null || (hexStringPlaintextKey.length() != 32 && hexStringPlaintextKey.length() != 48)) {
            appendErrorText("invalid parameter");
            return -1;
        }

        try {
            PstKeyInfoIn pstKeyInfo = new PstKeyInfoIn();
            //  // Destination Key Index (2-10)
            pstKeyInfo.setDstKeyIdx((short) ucKeyIdx);
            //  //TDES Destination key length
            pstKeyInfo.setDstKeyLen(hexStringPlaintextKey.length() / 2);
            // Destination key type   POS_TMK 0x02
            pstKeyInfo.setDstKeyType((byte) WorkingKeyEntity.TMK_KEY_ID);
            // Destination key value //16 bytes plaintext main key
            pstKeyInfo.setDstKeyValue(HexSupport.toBytesFromHex(hexStringPlaintextKey)); //16 bytes plaintext main key
            //            ***************************************************************************************************************
            //  Source key index (0 or 2 to 10)
            pstKeyInfo.setSrcKeyIdx((short) 0 );       //  Write the corresponding TLK:   SrcKeyIdx
            pstKeyInfo.setSrcKeyType((byte) 0 );  //  Write the corresponding TLK : SrcKeyType

            PstKcvInfoIn pstKcvInfoIn = new PstKcvInfoIn();
            // KCV verification type 00: No KCV verification
//            0x01：Use the des or 3des algorithm to verify the hexadecimal data 0000000000000000
//	          0x02：Use the des or 3des algorithm to verify the hexadecimal data 1234567890123456
//	          0x40：Decryption is performed using the SM4 key. kcv verification has not been added yet.
            pstKcvInfoIn.setCheckMode(1); //
            String desKcv1 = KCVUtil.computeDESKCV(hexStringPlaintextKey);
            byte[] bCheckBuf =AFUtils.hexString2ByteArray(desKcv1);
            //            KCV data KCV
            pstKcvInfoIn.setCheckBuf(bCheckBuf);

            appendInfoText("pinpad_LoadTMKKey");
            appendText("   ucSrcKeyIdx ： " + ucKeyIdx);
            appendText("   hexStringPlaintextKey ： " + hexStringPlaintextKey);

            /**
             *
             * int dalPosWriteKey(in PstKeyInfoIn pstKeyInfo , in PstKcvInfoIn pstKcvInfoIn);
             * @param pstKeyInfo Master key parameter
             * @param pstKcvInfoIn Check value
             */
            int ret = mPinPadBinderService.dalPosWriteKey(pstKeyInfo, pstKcvInfoIn);
            appendTextResult(ret, "   ret=" + ret);
            return ret;

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("   ret=" + e.getMessage());
            return -1;
        }
    }



    /**
     * Load the Encryption text tmk key
     * Load the ciphertext TMK
     * @param ucSrcKeyIndex    valid value [2~10]
     * @param ucDstKeyIdx      valid value [2~10]
     * @param bTMKEncryptionKey   HexString of plaintext master key
     * @return 0: success
     */
    int writeTMKEncryptionKey(int ucSrcKeyIndex,int ucDstKeyIdx, byte[] bTMKEncryptionKey) {
        if (ucSrcKeyIndex < MINIMUM_VALUE_RANGE || ucSrcKeyIndex > MAXIMUM_VALUE_RANGE) {
            appendErrorText("invalid parameter");
            return -1;
        }
        LogUtils.e("bTMKEncryptionKey.length : "+HexSupport.toHexFromBytes(bTMKEncryptionKey).length());
        if (bTMKEncryptionKey == null || HexSupport.toHexFromBytes(bTMKEncryptionKey).length() != 32 && HexSupport.toHexFromBytes(bTMKEncryptionKey).length() != 48) {
            appendErrorText("invalid parameter");
            LogUtils.e(" bEncryptionKey : ");
            return -1;
        }
        try {
            PstKeyInfoIn pstKeyInfo = new PstKeyInfoIn();
            //  // Destination Key Index
            pstKeyInfo.setDstKeyIdx((short) ucDstKeyIdx);
            //  //TDES Destination key length
            pstKeyInfo.setDstKeyLen(HexSupport.toHexFromBytes(bTMKEncryptionKey).length() / 2);   //TDES
            //Destination key type  // POS_TPK 0x03 // PinKey// POS_TAK 0x04 // MacKey// POS_TDK 0x05 // TDEA decrypt key // POS_TCK 0x06 // Communication key
            pstKeyInfo.setDstKeyType((byte) WorkingKeyEntity.TMK_KEY_ID);
            // Destination key value //16 bytes plaintext main key
            pstKeyInfo.setDstKeyValue(bTMKEncryptionKey); //16 bytes Encryption main key
            //            ***************************************************************************************************************
            //  Source key index (0 or 2 to 10)
            pstKeyInfo.setSrcKeyIdx((short) ucSrcKeyIndex );       //  Write the corresponding TLK:   SrcKeyIdx
            // Source key type
            pstKeyInfo.setSrcKeyType((byte) WorkingKeyEntity.TLK_KEY_ID);   //  Write the corresponding TLK : SrcKeyType
            PstKcvInfoIn pstKcvInfoIn = new PstKcvInfoIn();
            pstKcvInfoIn.setCheckMode(0);
            byte[] bCheckBuf = new byte[0];
            pstKcvInfoIn.setCheckBuf(bCheckBuf);
            appendInfoText("pinpad_LoadTMKKey");
            appendText("   ucSrcKeyIndex ： " + ucSrcKeyIndex);
            appendText("   ucDstKeyIdx ： " + ucDstKeyIdx);
            appendText("   hexStringEncryptionKey ： " + HexSupport.toHexFromBytes(bTMKEncryptionKey) );

            int ret = mPinPadBinderService.dalPosWriteKey(pstKeyInfo, pstKcvInfoIn);
            appendTextResult(ret, "   ret=" + ret);
            if (ret == 0) {
                try {
                    /**
                     *  PosGetKcvResult posGetKcv(int keyIdx, int keyType);
                     * Obtained the secret key Check Value Data
                     * @param keyIdx 【2,10】
                     * @param keyType
                     *                POS_TLK 0x01 // Loading Key
                     *                POS_TMK 0x02 // master key
                     *                POS_TPK 0x03 // PinKey
                     *                POS_TAK 0x04 // MacKey
                     *                POS_TDK 0x05 // TDEA decrypt key
                     *                POS_TCK 0x06 // Communication key
                     */
                    PosGetKcvResult posGetKcvResult = mPinPadBinderService.posGetKcv(ucDstKeyIdx, WorkingKeyEntity.TMK_KEY_ID);
                    int getKcvResultRet = posGetKcvResult == null ? -1 : posGetKcvResult.getRet();
                    appendInfoText("posGetKcv()");
                    appendText("ucDstKeyIdx : " + ucDstKeyIdx);
                    appendText("keyId : " + WorkingKeyEntity.TMK_KEY_ID);
                    appendTextResult(getKcvResultRet, " ret " + getKcvResultRet);

                    if (getKcvResultRet == 0) {
                        appendPosGetKcvResult(posGetKcvResult);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return ret;

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("   ret : " + e.getMessage());
            return -1;
        }
    }



    /**
     * Load the Encryption text tmk key
     * Load the ciphertext TWK
     * @param ucKeyIdx          valid value [2~10]
     * @param dstKeyType // POS_TPK 0x03 // PinKey// POS_TAK 0x04 // MacKey// POS_TDK 0x05 // TDEA decrypt key // POS_TCK 0x06 // Communication key
     * @param bTWKEncryptionKey    HexString of plaintext master key
     * @return 0: success
     */
    int writeTWKEncryptionKey(int ucKeyIdx,int dstKeyType, byte[] bTWKEncryptionKey) {
        LogUtils.e("ucSrcKeyIdx : "+ucKeyIdx);
        if (ucKeyIdx < MINIMUM_VALUE_RANGE || ucKeyIdx > MAXIMUM_VALUE_RANGE) {
            appendErrorText("invalid parameter");
            return -1;
        }
        LogUtils.e("bTWKEncryptionKey.length : "+HexSupport.toHexFromBytes(bTWKEncryptionKey).length());
        if (bTWKEncryptionKey == null || HexSupport.toHexFromBytes(bTWKEncryptionKey).length() != 32 && HexSupport.toHexFromBytes(bTWKEncryptionKey).length() != 48) {
            appendErrorText("invalid parameter");
            LogUtils.e(" bEncryptionKey : ");
            return -1;
        }

        try {
            PstKeyInfoIn pstKeyInfo = new PstKeyInfoIn();
            //  // Destination Key Index (2-10)
            pstKeyInfo.setDstKeyIdx((short) ucKeyIdx);
            //  //TDES Destination key length
            pstKeyInfo.setDstKeyLen(HexSupport.toHexFromBytes(bTWKEncryptionKey).length() / 2);
            //Destination key type   // POS_TPK 0x03 // PinKey// POS_TAK 0x04 // MacKey// POS_TDK 0x05 // TDEA decrypt key // POS_TCK 0x06 // Communication key
            pstKeyInfo.setDstKeyType((byte) dstKeyType);
            // Destination key value //16 bytes plaintext main key
            pstKeyInfo.setDstKeyValue(bTWKEncryptionKey); //16 bytes Encryption main key
            //            ***************************************************************************************************************
            //  Source key index (0 or 2 to 10)
            pstKeyInfo.setSrcKeyIdx((short) ucKeyIdx );
            //  Source key type
            pstKeyInfo.setSrcKeyType((byte) WorkingKeyEntity.TMK_KEY_ID);   //  Write the corresponding TMK : SrcKeyType

            PstKcvInfoIn pstKcvInfoIn = new PstKcvInfoIn();
            // KCV verification type 00: No KCV verification
//            0x01：Use the des or 3des algorithm to verify the hexadecimal data 0000000000000000
//	          0x02：Use the des or 3des algorithm to verify the hexadecimal data 1234567890123456
//	          0x40：Decryption is performed using the SM4 key. kcv verification has not been added yet.
            pstKcvInfoIn.setCheckMode(0);
            byte[] bCheckBuf = new byte[0];
            pstKcvInfoIn.setCheckBuf(bCheckBuf);

            appendInfoText("pinpad_LoadTWKKey");
            appendText("   ucSrcKeyIdx ： " + ucKeyIdx);
            appendText("   SrcKeyType ： " + dstKeyType);
            appendText("   hexStringEncryptionKey ： " + AFUtils.showResult16Str(bTWKEncryptionKey) );


            /**
             * int dalPosWriteKey(in PstKeyInfoIn pstKeyInfo , in PstKcvInfoIn pstKcvInfoIn);
             * @param pstKeyInfo Master key parameter
             * @param pstKcvInfoIn Check value
             */

            LogUtils.e("DAL_POSWriteKey 接口请求开始 *");
            int ret = mPinPadBinderService.dalPosWriteKey(pstKeyInfo, pstKcvInfoIn);
            LogUtils.e("DAL_POSWriteKey 接口请求结束 *");
            appendTextResult(ret, "ret ：" + ret);
            if (ret == 0) {
                try {
                    /**
                     *  PosGetKcvResult posGetKcv(int keyIdx, int keyType);
                     * Obtained the secret key Check Value Data
                     * @param keyIdx 【2,10】
                     * @param keyType
                     *                POS_TLK 0x01 // Loading Key
                     *                POS_TMK 0x02 // master key
                     *                POS_TPK 0x03 // PinKey
                     *                POS_TAK 0x04 // MacKey
                     *                POS_TDK 0x05 // TDEA decrypt key
                     *                POS_TCK 0x06 // Communication key
                     */
                    PosGetKcvResult posGetKcvResult = mPinPadBinderService.posGetKcv(ucKeyIdx, dstKeyType);
                    int getKcvResultRet = posGetKcvResult == null ? -1 : posGetKcvResult.getRet();
                    appendInfoText("posGetKcv()");
                    appendText("ucKeyIdx : " + ucKeyIdx);
                    appendText("keyId : " + dstKeyType);
                    appendTextResult(getKcvResultRet, " ret " + getKcvResultRet);

                    if (getKcvResultRet == 0) {
                        appendPosGetKcvResult(posGetKcvResult);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
            return ret;

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText("   ret=" + e.getMessage());
            return -1;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     *
     * @param usKeyType  PIN_KEY_ID = 3; pin key  ||  MAC_KEY_ID = 4; mac key || TDK_KEY_ID = 5; track key (TDEA decrypt key) ||TCK_KEY_ID = 6; data key (Communication key)
     * @param ucKeyLen
     * @param bEncryption
     * @param kcvInfoParam
     *          CheckMode      iCheckMode  KCV校验类型
     *                         00：无KCV校验
     *                         0x01：使用des或者3des算法，对十六进制数据0000000000000000进行校验
     *                         0x02：使用des或者3des算法，对十六进制数据1234567890123456进行校验
     *                         0x40：使用SM4秘钥，进行解密，kcv校验暂未添加.
     *          CheckBuf       KCV数据
     * @param ucMode
     * @param keyindex         [in] usKeyType 目的密钥类型
     *                          = PIN_KEY_ID = 3; //pin key TPK
     *                          = MAC_KEY_ID = 4; //mac key TCK
     *                          = TDK_KEY_ID = 5; //track key (TDEA decrypt key) TDK
     * @return
     */
    int writeWorkKey(int usKeyType,int ucKeyLen,byte[] bEncryption,KcvInfoParam kcvInfoParam ,int ucMode ,int keyindex) {

        appendInfoText(" usKeyType : "+usKeyType);
        if (keyindex < MINIMUM_VALUE_RANGE || keyindex > MAXIMUM_VALUE_RANGE) {
            appendErrorText("invalid parameter");
            return -1;
        }
        if (bEncryption == null || HexSupport.toHexFromBytes(bEncryption).length() != 32 && HexSupport.toHexFromBytes(bEncryption).length() != 48) {
            appendErrorText("invalid parameter");
            LogUtils.e(" bEncryptionKey : ");
            return -1;
        }
        int ret = -1;
        try {
            ret = mPinPadBinderService.posWriteWorkingKey(usKeyType,
                    ucKeyLen,
                    bEncryption,
                    kcvInfoParam,
                    ucMode,
                    keyindex);
            appendTextResult(ret, "   ret=" + ret);
            //read out the kcv from pinpad to double confirm
            if (ret == 0) {
                try {
                    /**
                     *  PosGetKcvResult posGetKcv(int keyIdx, int keyType);
                     * 获取秘钥的Check Value 数据
                     * @param keyIdx 秘钥索引【2,10】
                     * @param keyType 秘钥类型
                     *                POS_TLK 0x01 // Loading Key
                     *                POS_TMK 0x02 // master key
                     *                POS_TPK 0x03 // PinKey
                     *                POS_TAK 0x04 // MacKey
                     *                POS_TDK 0x05 // TDEA decrypt key
                     *                POS_TCK 0x06 // Communication key
                     */
                    PosGetKcvResult posGetKcvResult = mPinPadBinderService.posGetKcv(keyindex, usKeyType);
                    int getKcvResultRet = posGetKcvResult == null ? -1 : posGetKcvResult.getRet();
                    appendInfoText("posGetKcv()");
                    appendText("keyindex : " + keyindex);
                    appendText("keyId : " + usKeyType);
                    appendTextResult(getKcvResultRet, " ret " + getKcvResultRet);

                    if (getKcvResultRet == 0) {
                        appendPosGetKcvResult(posGetKcvResult);
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            ret = -1;
        }
//        ******************************************************************************************
        return ret;
    }

    private void appendPosGetKcvResult(PosGetKcvResult result) {
        byte[] kcv = result == null ? null : result.getKcv();
        byte[] kcvInfo = result == null ? null : result.getKcvInfo();

        appendInfoText("kcv : " + toHex(kcv));
        appendInfoText("kcvInfoLen : " + getByteArrayLengthText(kcvInfo));
        appendInfoText("kcvInfo : " + toHex(kcvInfo));
        appendInfoText("checkMode : " + formatKcvInfoCheckMode(kcvInfo));
        appendInfoText("rawKcv[4..6] : " + toHex(extractKcvInfoData(kcvInfo)));
    }

    private String getByteArrayLengthText(byte[] data) {
        return data == null ? "null" : String.valueOf(data.length);
    }

    private String toHex(byte[] data) {
        return data == null ? "null" : HexSupport.toHexFromBytes(data);
    }

    private String formatKcvInfoCheckMode(byte[] kcvInfo) {
        if (kcvInfo == null || kcvInfo.length < 4) {
            return "N/A";
        }

        int checkMode = (kcvInfo[0] & 0xFF)
                | ((kcvInfo[1] & 0xFF) << 8)
                | ((kcvInfo[2] & 0xFF) << 16)
                | ((kcvInfo[3] & 0xFF) << 24);
        return String.format("0x%08X", checkMode);
    }

    private byte[] extractKcvInfoData(byte[] kcvInfo) {
        if (kcvInfo == null || kcvInfo.length < 7) {
            return null;
        }

        return Arrays.copyOfRange(kcvInfo, 4, 7);
    }


}
