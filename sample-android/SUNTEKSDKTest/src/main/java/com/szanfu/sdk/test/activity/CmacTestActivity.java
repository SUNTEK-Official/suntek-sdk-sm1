package com.szanfu.sdk.test.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.CmacAlgorithm;
import com.szanfu.sdk.api.constant.CustomKeyOperationType;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.constant.WorkingKeyIndex;
import com.szanfu.sdk.api.constant.WorkingKeyMode;
import com.szanfu.sdk.api.constant.WorkingKeyType;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.pinpad.PosGetKcvResult;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.HexUtil;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CmacTestActivity extends ListButtonBaseActivity {

    private static final String TAG = "CmacTestActivity";

    public static final int POSITION_AES_CMAC_ONESHOT = 0;
    public static final int POSITION_AES_CMAC_FLOW = 1;
    public static final int POSITION_AES_CMAC_COMPARE = 2;

    public static final int POSITION_3DES_CMAC_ONESHOT = 3;
    public static final int POSITION_3DES_CMAC_FLOW = 4;
    public static final int POSITION_3DES_CMAC_COMPARE = 5;

    public static final int POSITION_WRITE_CUSTOM_WORKING_KEY = 6;
    public static final int POSITION_CUSTOM_KEY_AES_ECB_ENC = 7;
    public static final int POSITION_CUSTOM_KEY_AES_ECB_DEC = 8;
    public static final int POSITION_CUSTOM_KEY_AES_CBC_ENC = 9;
    public static final int POSITION_CUSTOM_KEY_AES_CBC_DEC = 10;
    public static final int POSITION_CUSTOM_KEY_AES_CMAC = 11;
    public static final int POSITION_AES_CMAC_128K_FLOW = 12;
    public static final int POSITION_AES_CMAC_128K_ONESHOT = 13;
    public static final int POSITION_CMAC_128K_SPEED_LOOP = 14;
    public static final int POSITION_UNLOADED_CUSTOM_KEY_103 = 15;

    private static final int CMAC_LARGE_DATA_SIZE = 128 * 1024;
    private static final int CMAC_LARGE_DATA_CHUNK_SIZE = 4096;
    private static final int CMAC_SPEED_LOOP_ROUNDS = 20;
    private static final int CUSTOM_KEY_KCV_LENGTH = 3;
    private static final int UNLOADED_CUSTOM_WORKING_KEY_INDEX = 103;

    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private IPinPadBinderService mPinPadBinderService;

    private static final class CmacTestVector {
        final String name;
        final int algorithm;
        final byte[] key;
        final byte[] fullData;
        final byte[][] parts;

        CmacTestVector(String name, int algorithm, String keyHex, String fullDataHex, String... partHexList) {
            this.name = name;
            this.algorithm = algorithm;
            this.key = HexUtil.hexStringToByte(keyHex);
            this.fullData = HexUtil.hexStringToByte(fullDataHex);
            this.parts = new byte[partHexList.length][];
            for (int i = 0; i < partHexList.length; i++) {
                this.parts[i] = HexUtil.hexStringToByte(partHexList[i]);
            }
        }
    }

    private static final class CustomKeyTestVector {
        final int keyIndex;
        final int keyType;
        final byte[] key;
        final byte[] iv;
        final byte[] plainData;

        CustomKeyTestVector(int keyIndex,
                            int keyType,
                            String keyHex,
                            String ivHex,
                            String plainDataHex) {
            this.keyIndex = keyIndex;
            this.keyType = keyType;
            this.key = HexUtil.hexStringToByte(keyHex);
            this.iv = HexUtil.hexStringToByte(ivHex);
            this.plainData = HexUtil.hexStringToByte(plainDataHex);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.CMAC_TEST_LIST);
        setButtonDataSource(mButtons);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            Log.e(TAG, "onServiceConnected");

            mSystemBinderService = ISystemBinderService.Stub.asInterface(
                    sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM)
            );

            mPinPadBinderService = IPinPadBinderService.Stub.asInterface(
                    sdkServiceManager.getService(ServiceID.SERVICE_ID_PINPAD)
            );
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
            case POSITION_AES_CMAC_ONESHOT:
                testAes128StandardVectors(itemName);
                break;
            case POSITION_AES_CMAC_FLOW:
                testCmacFlow(itemName, CmacAlgorithm.CMAC_AES);
                break;
            case POSITION_AES_CMAC_COMPARE:
                testCmacCompare(itemName, CmacAlgorithm.CMAC_AES);
                break;
            case POSITION_3DES_CMAC_ONESHOT:
                test3DesStandardVectors(itemName);
                break;
            case POSITION_3DES_CMAC_FLOW:
                testCmacFlow(itemName, CmacAlgorithm.CMAC_3DES);
                break;
            case POSITION_3DES_CMAC_COMPARE:
                testCmacCompare(itemName, CmacAlgorithm.CMAC_3DES);
                break;
            case POSITION_WRITE_CUSTOM_WORKING_KEY:
                testWriteCustomWorkingKey(itemName);
//                testImportCustomWorkingKeyRange(itemName);
                break;
            case POSITION_CUSTOM_KEY_AES_ECB_ENC:
                testCustomKeyOperation(itemName, CustomKeyOperationType.AES_ECB_ENC);
                break;
            case POSITION_CUSTOM_KEY_AES_ECB_DEC:
                testCustomKeyOperation(itemName, CustomKeyOperationType.AES_ECB_DEC);
                break;
            case POSITION_CUSTOM_KEY_AES_CBC_ENC:
                testCustomKeyOperation(itemName, CustomKeyOperationType.AES_CBC_ENC);
                break;
            case POSITION_CUSTOM_KEY_AES_CBC_DEC:
                testCustomKeyOperation(itemName, CustomKeyOperationType.AES_CBC_DEC);
                break;
            case POSITION_CUSTOM_KEY_AES_CMAC:
                testCustomKeyOperation(itemName, CustomKeyOperationType.AES_CMAC);
                break;
            case POSITION_AES_CMAC_128K_FLOW:
                testAesCmac128KFlow(itemName);
                break;
            case POSITION_AES_CMAC_128K_ONESHOT:
                testAesCmac128KOneShot(itemName);
                break;
            case POSITION_CMAC_128K_SPEED_LOOP:
                testCmac128KSpeedLoop(itemName);
                break;
            case POSITION_UNLOADED_CUSTOM_KEY_103:
                testUnloadedCustomKey103(itemName);
                break;
            default:
                appendText("Not support");
                break;
        }
    }


    /**
     * 验证自定义工作密钥索引 101 ~ 110 是否可以正常导入。
     */
    private void testImportCustomWorkingKeyRange(String itemName) {
        appendText(itemName);

        try {
            if (mPinPadBinderService == null) {
                appendText("mPinPadBinderService is null");
                return;
            }

            byte[] customKey = HexUtil.hexStringToByte("2B7E151628AED2A6ABF7158809CF4F3C");

            appendText("Start import custom working key range test");
            appendText("valid range : 101 ~ 110");
            appendText("keyLen      : " + customKey.length);
            appendText("key         : " + HexUtil.bcd2str(customKey));
            appendText("--------------------------------------------------");

            int successCount = 0;
            int failCount = 0;

            for (int keyIndex = WorkingKeyIndex.CUSTOM_WORKING_KEY_INDEX_101;
                 keyIndex <= WorkingKeyIndex.CUSTOM_WORKING_KEY_INDEX_110;
                 keyIndex++) {

                /*
                 * 这里主要验证 101~110 索引范围是否可用。
                 * 使用 POS_TDK 写入，表示该索引用作 AES-128 key。
                 */
                int ret = mPinPadBinderService.posWriteWorkingKey(
                        WorkingKeyType.POS_TDK,
                        customKey.length,
                        customKey,
                        null,
                        WorkingKeyMode.MODE_PLAINTEXT,
                        keyIndex
                );

                appendText("keyIndex : " + keyIndex + ", keyType : POS_TDK, ret : " + ret);

                if (ret == 0) {
                    successCount++;
                } else {
                    failCount++;
                }
            }

            appendText("--------------------------------------------------");
            appendText("valid index test finished");
            appendText("successCount : " + successCount);
            appendText("failCount    : " + failCount);

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private CmacTestVector buildCmacTestVector(int algorithm) {
        if (algorithm == CmacAlgorithm.CMAC_AES) {
            return new CmacTestVector(
                    "AES CMAC Demo",
                    CmacAlgorithm.CMAC_AES,
                    "2B7E151628AED2A6ABF7158809CF4F3C",
                    "6BC1BEE22E409F96E93D7E117393172AAE2D8A571E03AC9C9EB76FAC45AF8E51",
                    "6BC1BEE22E409F96",
                    "E93D7E117393172A",
                    "AE2D8A571E03AC9C",
                    "9EB76FAC45AF8E51"
            );
        } else if (algorithm == CmacAlgorithm.CMAC_3DES) {
            return new CmacTestVector(
                    "3DES CMAC Demo",
                    CmacAlgorithm.CMAC_3DES,
                    "0123456789ABCDEF23456789ABCDEF01456789ABCDEF0123",
                    "6BC1BEE22E409F96E93D7E117393172AAE2D8A571E03AC9C9EB76FAC45AF8E51",
                    "6BC1BEE22E409F96",
                    "E93D7E117393172A",
                    "AE2D8A571E03AC9C",
                    "9EB76FAC45AF8E51"
            );
        }
        return null;
    }

    private CustomKeyTestVector buildCustomKeyTestVector(int alg) {
        int keyIndex;
        int keyType;

        if (alg == CustomKeyOperationType.AES_CMAC) {
            keyIndex = WorkingKeyIndex.CUSTOM_WORKING_KEY_INDEX_102;
            keyType = WorkingKeyType.POS_TAK;
        } else {
            keyIndex = WorkingKeyIndex.CUSTOM_WORKING_KEY_INDEX_102;
            keyType = WorkingKeyType.POS_TDK;
        }

        return new CustomKeyTestVector(
                keyIndex,
                keyType,
                "2B7E151628AED2A6ABF7158809CF4F3C",
                "000102030405060708090A0B0C0D0E0F",
                "6BC1BEE22E409F96E93D7E117393172A"
        );
    }

    private CustomKeyTestVector buildCustomCmacKeyVector() {
        return new CustomKeyTestVector(
                WorkingKeyIndex.CUSTOM_WORKING_KEY_INDEX_101,
                WorkingKeyType.POS_TAK,
                "2B7E151628AED2A6ABF7158809CF4F3C",
                "000102030405060708090A0B0C0D0E0F",
                "6BC1BEE22E409F96E93D7E117393172A"
        );
    }

    private CustomKeyTestVector buildCustomAesKeyVector() {
        return new CustomKeyTestVector(
                WorkingKeyIndex.CUSTOM_WORKING_KEY_INDEX_102,
                WorkingKeyType.POS_TDK,
                "2B7E151628AED2A6ABF7158809CF4F3C",
                "000102030405060708090A0B0C0D0E0F",
                "6BC1BEE22E409F96E93D7E117393172A"
        );
    }

    private String getCustomKeyAlgName(int alg) {
        switch (alg) {
            case CustomKeyOperationType.AES_ECB_ENC:
                return "AES_ECB_ENC";
            case CustomKeyOperationType.AES_ECB_DEC:
                return "AES_ECB_DEC";
            case CustomKeyOperationType.AES_CBC_ENC:
                return "AES_CBC_ENC";
            case CustomKeyOperationType.AES_CBC_DEC:
                return "AES_CBC_DEC";
            case CustomKeyOperationType.AES_CMAC:
                return "AES_CMAC";
            default:
                return "UNKNOWN(" + alg + ")";
        }
    }

    private String getCustomWorkingKeyTypeName(int keyType) {
        switch (keyType) {
            case WorkingKeyType.POS_TAK:
                return "POS_TAK";
            case WorkingKeyType.POS_TDK:
                return "POS_TDK";
            default:
                return "UNKNOWN(" + keyType + ")";
        }
    }

    private int writeCustomWorkingKey(CustomKeyTestVector vector) throws RemoteException {
        if (mPinPadBinderService == null) {
            appendText("mPinPadBinderService is null");
            return -1;
        }

        if (vector == null || vector.key == null || vector.key.length == 0) {
            appendText("custom working key vector is invalid");
            return -1;
        }

        return mPinPadBinderService.posWriteWorkingKey(
                vector.keyType,
                vector.key.length,
                vector.key,
                null,
                WorkingKeyMode.MODE_PLAINTEXT,
                vector.keyIndex
        );
    }

    private void appendCustomWorkingKeyKcv(String label, CustomKeyTestVector vector) throws RemoteException {
        if (mPinPadBinderService == null) {
            appendText("mPinPadBinderService is null");
            return;
        }

        if (vector == null) {
            appendText(label + " kcvCheck : vector is null");
            return;
        }

        if (!isCustomAesKcvKeyType(vector.keyType)) {
            appendText(label + " kcvCheck : unsupported keyType " + vector.keyType);
            return;
        }

        PosGetKcvResult result = mPinPadBinderService.posGetKcv(vector.keyIndex, vector.keyType);
        int ret = result == null ? -1 : result.getRet();
        byte[] actualKcv = result == null ? null : result.getKcv();
        byte[] kcvInfo = result == null ? null : result.getKcvInfo();

        appendText(label + " kcvRet   : " + ret);
        appendText(label + " kcvLen   : " + getByteArrayLengthText(actualKcv));
        appendText(label + " kcv      : " + toHex(actualKcv));
        appendRawKcvInfo(label, kcvInfo);

        if (ret != 0 || result == null) {
            return;
        }

        byte[] expectedKcv = null;
        try {
            expectedKcv = calculateAesKcv(vector.key);
        } catch (GeneralSecurityException e) {
            appendText(label + " kcvCalc  : " + e.getMessage());
        }

        appendText(label + " expected : " + toHex(expectedKcv));
        appendText(label + " kcvMatch : " + isSameBytes(actualKcv, expectedKcv));
    }

    private boolean isCustomAesKcvKeyType(int keyType) {
        return keyType == WorkingKeyType.POS_TAK || keyType == WorkingKeyType.POS_TDK;
    }

    private byte[] calculateAesKcv(byte[] key) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        return Arrays.copyOf(cipher.doFinal(new byte[16]), CUSTOM_KEY_KCV_LENGTH);
    }

    private String toHex(byte[] data) {
        return data == null ? "null" : HexUtil.bcd2str(data);
    }

    private void testUnloadedCustomKey103(String itemName) {
        appendText(itemName);

        try {
            if (mPinPadBinderService == null) {
                appendText("mPinPadBinderService is null");
                return;
            }

            appendText("keyIndex  : " + UNLOADED_CUSTOM_WORKING_KEY_INDEX);
            appendText("writeKey  : skipped");
            appendText("expected  : ret != 0 and kcv is null/empty if key 103 has not been loaded");

            appendUnloadedCustomKeyKcv("AES", WorkingKeyType.POS_TDK);
            appendText("--------------------------------------------------");
            appendUnloadedCustomKeyKcv("CMAC", WorkingKeyType.POS_TAK);

            appendText("--------------------------------------------------");
            appendUnloadedCustomKeyOperation(
                    "AES",
                    CustomKeyOperationType.AES_ECB_ENC,
                    HexUtil.hexStringToByte("00000000000000000000000000000000")
            );
            appendUnloadedCustomKeyOperation(
                    "CMAC",
                    CustomKeyOperationType.AES_CMAC,
                    HexUtil.hexStringToByte("00000000000000000000000000000000")
            );

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void appendUnloadedCustomKeyKcv(String label, int keyType) throws RemoteException {
        PosGetKcvResult result = mPinPadBinderService.posGetKcv(
                UNLOADED_CUSTOM_WORKING_KEY_INDEX,
                keyType
        );

        int ret = result == null ? -1 : result.getRet();
        byte[] kcv = result == null ? null : result.getKcv();
        byte[] kcvInfo = result == null ? null : result.getKcvInfo();

        appendText(label + " keyType  : " + getCustomWorkingKeyTypeName(keyType));
        appendText(label + " kcvRet   : " + ret);
        appendText(label + " kcvLen   : " + getByteArrayLengthText(kcv));
        appendText(label + " kcv      : " + toHex(kcv));
        appendRawKcvInfo(label, kcvInfo);
        appendText(label + " expected : ret != 0, kcv null/empty");
        appendText(label + " pass     : " + (ret != 0 && isEmpty(kcv)));
    }

    private void appendUnloadedCustomKeyOperation(String label, int alg, byte[] input) throws RemoteException {
        if (mSystemBinderService == null) {
            appendText(label + " operation : mSystemBinderService is null");
            return;
        }

        byte[] output = mSystemBinderService.customKeyOperation(
                UNLOADED_CUSTOM_WORKING_KEY_INDEX,
                alg,
                null,
                input
        );

        appendText(label + " alg      : " + getCustomKeyAlgName(alg));
        appendText(label + " input    : " + toHex(input));
        appendText(label + " output   : " + toHex(output));
        appendText(label + " expected : output null/empty");
        appendText(label + " pass     : " + isEmpty(output));
    }

    private boolean isEmpty(byte[] data) {
        return data == null || data.length == 0;
    }

    private boolean isSameBytes(byte[] actual, byte[] expected) {
        return actual != null && expected != null && Arrays.equals(actual, expected);
    }

    private String getByteArrayLengthText(byte[] data) {
        return data == null ? "null" : String.valueOf(data.length);
    }

    private void appendRawKcvInfo(String label, byte[] kcvInfo) {
        appendText(label + " kcvInfoLen : " + (kcvInfo == null ? "null" : kcvInfo.length));
        appendText(label + " kcvInfo    : " + toHex(kcvInfo));
        appendText(label + " checkMode  : " + formatKcvInfoCheckMode(kcvInfo));
        appendText(label + " rawKcv[4..6] : " + toHex(extractKcvInfoData(kcvInfo)));
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

    private void testAes128StandardVectors(String itemName) {
        appendText(itemName);
        try {
            if (mSystemBinderService == null) {
                appendText("mSystemBinderService is null");
                return;
            }

            final String keyHex = "2B7E151628AED2A6ABF7158809CF4F3C";
            final byte[] key = HexUtil.hexStringToByte(keyHex);

            final String[] names = new String[]{
                    "AES128-CMAC-Example-1",
                    "AES128-CMAC-Example-2",
                    "AES128-CMAC-Example-3",
                    "AES128-CMAC-Example-4"
            };

            final String[] fullDataHexArray = new String[]{
                    "",
                    "6BC1BEE22E409F96E93D7E117393172A",
                    "6BC1BEE22E409F96E93D7E117393172AAE2D8A57",
                    "6BC1BEE22E409F96E93D7E117393172A" +
                            "AE2D8A571E03AC9C9EB76FAC45AF8E51" +
                            "30C81C46A35CE411E5FBC1191A0A52EF" +
                            "F69F2445DF4F9B17AD2B417BE66C3710"
            };

            final String[] expectedHexArray = new String[]{
                    "BB1D6929E95937287FA37D129B756746",
                    "070A16B46B4D4144F79BDD9DD04A287C",
                    "DFA66747DE9AE63030CA32611497C827",
                    "51F0BEBF7E3B9D92FC49741779363CFE"
            };

            final String[][] partsHexArray = new String[][]{
                    {},
                    {
                            "6BC1BEE22E409F96",
                            "E93D7E117393172A"
                    },
                    {
                            "6BC1BEE2",
                            "2E409F96E93D7E11",
                            "7393172AAE2D8A57"
                    },
                    {
                            "6BC1BEE22E409F96E93D7E117393172A",
                            "AE2D8A571E03AC9C9EB76FAC45AF8E51",
                            "30C81C46A35CE411E5FBC1191A0A52EF",
                            "F69F2445DF4F9B17AD2B417BE66C3710"
                    }
            };

            appendText("algorithm : CMAC_AES");
            appendText("key       : " + keyHex);

            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                String fullDataHex = fullDataHexArray[i];
                String expectedHex = expectedHexArray[i];
                String[] partHexList = partsHexArray[i];

                byte[] fullData = fullDataHex.length() == 0 ? new byte[0] : HexUtil.hexStringToByte(fullDataHex);

                byte[] oneShot = mSystemBinderService.cmac(fullData, key, CmacAlgorithm.CMAC_AES);
                String oneShotHex = oneShot == null ? "null" : HexUtil.bcd2str(oneShot);

                long sessionId = mSystemBinderService.createCmacSession(key, CmacAlgorithm.CMAC_AES);
                String flowHex = "null";

                appendText("--------------------------------------------------");
                appendText("name       : " + name);
                appendText("data       : " + (fullDataHex.length() == 0 ? "<empty>" : fullDataHex));
                appendText("expected   : " + expectedHex);

                if (sessionId != 0L) {
                    for (int j = 0; j < partHexList.length; j++) {
                        byte[] part = partHexList[j].length() == 0 ? new byte[0] : HexUtil.hexStringToByte(partHexList[j]);
                        mSystemBinderService.updateCmac(sessionId, part);
                    }

                    byte[] flow = mSystemBinderService.finishCmac(sessionId);
                    flowHex = flow == null ? "null" : HexUtil.bcd2str(flow);
                } else {
                    flowHex = "create session failed";
                }

                appendText("oneShot    : " + oneShotHex);
                appendText("flow       : " + flowHex);
            }

            appendText("--------------------------------------------------");
            appendText("AES128 standard vector test finished");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void test3DesStandardVectors(String itemName) {
        appendText(itemName);
        try {
            if (mSystemBinderService == null) {
                appendText("mSystemBinderService is null");
                return;
            }

            final String keyHex = "0123456789ABCDEF23456789ABCDEF01456789ABCDEF0123";
            final byte[] key = HexUtil.hexStringToByte(keyHex);

            final String[] names = new String[]{
                    "TDES-CMAC-Example-1",
                    "TDES-CMAC-Example-2",
                    "TDES-CMAC-Example-3",
                    "TDES-CMAC-Example-4"
            };

            final String[] fullDataHexArray = new String[]{
                    "",
                    "6BC1BEE22E409F96E93D7E117393172A",
                    "6BC1BEE22E409F96E93D7E117393172AAE2D8A57",
                    "6BC1BEE22E409F96E93D7E117393172A" +
                            "AE2D8A571E03AC9C9EB76FAC45AF8E51"
            };

            final String[] expectedHexArray = new String[]{
                    "7DB0D37DF936C550",
                    "30239CF1F52E6609",
                    "6C9F3EE4923F6BE2",
                    "99429BD0BF7904E5"
            };

            final String[][] partsHexArray = new String[][]{
                    {},
                    {
                            "6BC1BEE22E409F96",
                            "E93D7E117393172A"
                    },
                    {
                            "6BC1BEE2",
                            "2E409F96E93D7E11",
                            "7393172AAE2D8A57"
                    },
                    {
                            "6BC1BEE22E409F96",
                            "E93D7E117393172A",
                            "AE2D8A571E03AC9C",
                            "9EB76FAC45AF8E51"
                    }
            };

            appendText("algorithm : CMAC_3DES");
            appendText("key       : " + keyHex);

            for (int i = 0; i < names.length; i++) {
                String name = names[i];
                String fullDataHex = fullDataHexArray[i];
                String expectedHex = expectedHexArray[i];
                String[] partHexList = partsHexArray[i];

                byte[] fullData = fullDataHex.length() == 0 ? new byte[0] : HexUtil.hexStringToByte(fullDataHex);

                byte[] oneShot = mSystemBinderService.cmac(fullData, key, CmacAlgorithm.CMAC_3DES);
                String oneShotHex = oneShot == null ? "null" : HexUtil.bcd2str(oneShot);

                long sessionId = mSystemBinderService.createCmacSession(key, CmacAlgorithm.CMAC_3DES);
                String flowHex = "null";

                appendText("--------------------------------------------------");
                appendText("name       : " + name);
                appendText("data       : " + (fullDataHex.length() == 0 ? "<empty>" : fullDataHex));
                appendText("expected   : " + expectedHex);

                if (sessionId != 0L) {
                    for (int j = 0; j < partHexList.length; j++) {
                        byte[] part = partHexList[j].length() == 0 ? new byte[0] : HexUtil.hexStringToByte(partHexList[j]);
                        mSystemBinderService.updateCmac(sessionId, part);
                    }

                    byte[] flow = mSystemBinderService.finishCmac(sessionId);
                    flowHex = flow == null ? "null" : HexUtil.bcd2str(flow);
                } else {
                    flowHex = "create session failed";
                }

                appendText("oneShot    : " + oneShotHex);
                appendText("flow       : " + flowHex);
            }

            appendText("--------------------------------------------------");
            appendText("3DES standard vector test finished");

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testCmacOneShot(String itemName, int algorithm) {
        appendText(itemName);
        try {
            if (mSystemBinderService == null) {
                appendText("mSystemBinderService is null");
                return;
            }

            CmacTestVector vector = buildCmacTestVector(algorithm);
            if (vector == null) {
                appendText("unsupported algorithm : " + algorithm);
                return;
            }

            byte[] result = mSystemBinderService.cmac(vector.fullData, vector.key, vector.algorithm);

            appendText("name      : " + vector.name);
            appendText("algorithm : " + vector.algorithm);
            appendText("key       : " + HexUtil.bcd2str(vector.key));
            appendText("fullData  : " + HexUtil.bcd2str(vector.fullData));
            appendText("cmac      : " + (result == null ? "null" : HexUtil.bcd2str(result)));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testCmacFlow(String itemName, int algorithm) {
        appendText(itemName);
        try {
            if (mSystemBinderService == null) {
                appendText("mSystemBinderService is null");
                return;
            }

            CmacTestVector vector = buildCmacTestVector(algorithm);
            if (vector == null) {
                appendText("unsupported algorithm : " + algorithm);
                return;
            }

            long sessionId = mSystemBinderService.createCmacSession(vector.key, vector.algorithm);
            appendText("name      : " + vector.name);
            appendText("sessionId : " + sessionId);

            if (sessionId == 0L) {
                appendText("createCmacSession failed");
                return;
            }

            for (int i = 0; i < vector.parts.length; i++) {
                int ret = mSystemBinderService.updateCmac(sessionId, vector.parts[i]);
                appendText("part[" + i + "]  : " + HexUtil.bcd2str(vector.parts[i]));
                appendText("ret      : " + ret);
                if (ret != 0) {
                    mSystemBinderService.abortCmac(sessionId);
                    appendText("updateCmac failed, session aborted");
                    return;
                }
            }

            byte[] result = mSystemBinderService.finishCmac(sessionId);
            appendText("cmac      : " + (result == null ? "null" : HexUtil.bcd2str(result)));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testCmacCompare(String itemName, int algorithm) {
        appendText(itemName);
        try {
            if (mSystemBinderService == null) {
                appendText("mSystemBinderService is null");
                return;
            }

            CmacTestVector vector = buildCmacTestVector(algorithm);
            if (vector == null) {
                appendText("unsupported algorithm : " + algorithm);
                return;
            }

            byte[] oneShot = mSystemBinderService.cmac(vector.fullData, vector.key, vector.algorithm);

            long sessionId = mSystemBinderService.createCmacSession(vector.key, vector.algorithm);
            if (sessionId == 0L) {
                appendText("createCmacSession failed");
                return;
            }

            for (byte[] part : vector.parts) {
                int ret = mSystemBinderService.updateCmac(sessionId, part);
                if (ret != 0) {
                    appendText("updateCmac failed, ret = " + ret);
                    mSystemBinderService.abortCmac(sessionId);
                    return;
                }
            }

            byte[] flow = mSystemBinderService.finishCmac(sessionId);

            appendText("name      : " + vector.name);
            appendText("oneShot   : " + (oneShot == null ? "null" : HexUtil.bcd2str(oneShot)));
            appendText("flow      : " + (flow == null ? "null" : HexUtil.bcd2str(flow)));
            appendText("same      : " + Arrays.equals(oneShot, flow));
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testAesCmac128KFlow(final String itemName) {
        appendText(itemName);

        if (mSystemBinderService == null) {
            appendText("mSystemBinderService is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                runAesCmac128KFlow(itemName);
            }
        }).start();
    }

    private void runAesCmac128KFlow(String itemName) {
        byte[] key = HexUtil.hexStringToByte("2B7E151628AED2A6ABF7158809CF4F3C");
        byte[] data = buildLargeData(CMAC_LARGE_DATA_SIZE);

        appendText("dataSize  : " + data.length + " bytes");
        appendText("algorithm : CMAC_AES");
        appendText("mode      : createCmacSession + updateCmac + finishCmac");
        appendText("chunkSize : " + CMAC_LARGE_DATA_CHUNK_SIZE + " bytes");
        appendText("start large data CMAC, please wait...");

        try {
            CmacFlowResult flowResult = runFlowCmac(data, key, CMAC_LARGE_DATA_CHUNK_SIZE);

            appendText("cost      : " + flowResult.costMs + " ms");
            appendText("updates   : " + flowResult.updateCount);
            appendText("cmac      : " + formatCmac(flowResult.cmac));
            appendText("large data CMAC finished");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testAesCmac128KOneShot(final String itemName) {
        appendText(itemName);

        if (mSystemBinderService == null) {
            appendText("mSystemBinderService is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                runAesCmac128KOneShot(itemName);
            }
        }).start();
    }

    private void runAesCmac128KOneShot(String itemName) {
        byte[] key = HexUtil.hexStringToByte("2B7E151628AED2A6ABF7158809CF4F3C");
        byte[] data = buildLargeData(CMAC_LARGE_DATA_SIZE);

        appendText("dataSize  : " + data.length + " bytes");
        appendText("algorithm : CMAC_AES");
        appendText("mode      : cmac(data, key, algorithm)");
        appendText("start one-shot CMAC, please wait...");

        try {
            long startTime = System.nanoTime();
            byte[] result = mSystemBinderService.cmac(data, key, CmacAlgorithm.CMAC_AES);
            long costMs = nanosToMillis(System.nanoTime() - startTime);

            appendText("cost      : " + costMs + " ms");
            appendText("cmac      : " + formatCmac(result));
            appendText("one-shot CMAC finished");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testCmac128KSpeedLoop(final String itemName) {
        appendText(itemName);

        if (mSystemBinderService == null) {
            appendText("mSystemBinderService is null");
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                runCmac128KSpeedLoop(itemName);
            }
        }).start();
    }

    private void runCmac128KSpeedLoop(String itemName) {
        byte[] aesKey = HexUtil.hexStringToByte("2B7E151628AED2A6ABF7158809CF4F3C");
        byte[] desKey = HexUtil.hexStringToByte("0123456789ABCDEF23456789ABCDEF01456789ABCDEF0123");
        byte[] data = buildLargeData(CMAC_LARGE_DATA_SIZE);

        appendText("dataSize  : " + data.length + " bytes");
        appendText("mode      : cmac(data, key, algorithm) loop");
        appendText("rounds    : " + CMAC_SPEED_LOOP_ROUNDS);
        appendText("start speed loop, please wait...");

        try {
            CmacSpeedLoopResult aesResult = runOneShotSpeedLoop(
                    "CMAC_AES",
                    data,
                    aesKey,
                    CmacAlgorithm.CMAC_AES,
                    CMAC_SPEED_LOOP_ROUNDS
            );
            appendCmacSpeedLoopResult(aesResult);

            CmacSpeedLoopResult desResult = runOneShotSpeedLoop(
                    "CMAC_3DES",
                    data,
                    desKey,
                    CmacAlgorithm.CMAC_3DES,
                    CMAC_SPEED_LOOP_ROUNDS
            );
            appendCmacSpeedLoopResult(desResult);

            appendText("speed loop finished");
        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private CmacSpeedLoopResult runOneShotSpeedLoop(String algorithmName,
                                                    byte[] data,
                                                    byte[] key,
                                                    int algorithm,
                                                    int rounds) throws RemoteException {
        long totalNanos = 0L;
        long minNanos = Long.MAX_VALUE;
        long maxNanos = 0L;
        byte[] lastCmac = null;

        for (int i = 0; i < rounds; i++) {
            long startTime = System.nanoTime();
            lastCmac = mSystemBinderService.cmac(data, key, algorithm);
            long costNanos = System.nanoTime() - startTime;

            totalNanos += costNanos;
            if (costNanos < minNanos) {
                minNanos = costNanos;
            }
            if (costNanos > maxNanos) {
                maxNanos = costNanos;
            }
        }

        return new CmacSpeedLoopResult(algorithmName, rounds, totalNanos, minNanos, maxNanos, lastCmac);
    }

    private void appendCmacSpeedLoopResult(CmacSpeedLoopResult result) {
        appendText("algorithm : " + result.algorithmName);
        appendText("avg       : " + formatAverageMillis(result.totalNanos, result.rounds) + " ms");
        appendText("min       : " + nanosToMillis(result.minNanos) + " ms");
        appendText("max       : " + nanosToMillis(result.maxNanos) + " ms");
        appendText("cmac      : " + formatCmac(result.lastCmac));
    }

    private CmacFlowResult runFlowCmac(byte[] data, byte[] key, int chunkSize) throws RemoteException {
        long sessionId = 0L;
        long startTime = System.nanoTime();
        int updateCount = 0;

        try {
            sessionId = mSystemBinderService.createCmacSession(key, CmacAlgorithm.CMAC_AES);
            if (sessionId == 0L) {
                return new CmacFlowResult(nanosToMillis(System.nanoTime() - startTime), updateCount, null);
            }

            for (int offset = 0; offset < data.length; offset += chunkSize) {
                int length = Math.min(chunkSize, data.length - offset);
                byte[] part = new byte[length];
                System.arraycopy(data, offset, part, 0, length);

                int ret = mSystemBinderService.updateCmac(sessionId, part);
                updateCount++;
                if (ret != 0) {
                    mSystemBinderService.abortCmac(sessionId);
                    sessionId = 0L;
                    appendText("updateCmac failed, chunkSize = " + chunkSize + ", ret = " + ret);
                    return new CmacFlowResult(nanosToMillis(System.nanoTime() - startTime), updateCount, null);
                }
            }

            byte[] cmac = mSystemBinderService.finishCmac(sessionId);
            sessionId = 0L;
            return new CmacFlowResult(nanosToMillis(System.nanoTime() - startTime), updateCount, cmac);
        } finally {
            if (sessionId != 0L) {
                mSystemBinderService.abortCmac(sessionId);
            }
        }
    }

    private byte[] buildLargeData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i & 0xFF);
        }
        return data;
    }

    private long nanosToMillis(long nanos) {
        return nanos / 1000000L;
    }

    private String formatAverageMillis(long totalNanos, int count) {
        if (count <= 0) {
            return "0.00";
        }

        long avgHundredths = totalNanos / count / 10000L;
        long millis = avgHundredths / 100L;
        long hundredths = avgHundredths % 100L;
        return millis + "." + (hundredths < 10L ? "0" : "") + hundredths;
    }

    private String formatCmac(byte[] cmac) {
        return cmac == null ? "null" : HexUtil.bcd2str(cmac);
    }

    private static final class CmacSpeedLoopResult {
        final String algorithmName;
        final int rounds;
        final long totalNanos;
        final long minNanos;
        final long maxNanos;
        final byte[] lastCmac;

        CmacSpeedLoopResult(String algorithmName,
                            int rounds,
                            long totalNanos,
                            long minNanos,
                            long maxNanos,
                            byte[] lastCmac) {
            this.algorithmName = algorithmName;
            this.rounds = rounds;
            this.totalNanos = totalNanos;
            this.minNanos = minNanos;
            this.maxNanos = maxNanos;
            this.lastCmac = lastCmac;
        }
    }

    private static final class CmacFlowResult {
        final long costMs;
        final int updateCount;
        final byte[] cmac;

        CmacFlowResult(long costMs, int updateCount, byte[] cmac) {
            this.costMs = costMs;
            this.updateCount = updateCount;
            this.cmac = cmac;
        }
    }

    private void testWriteCustomWorkingKey(String itemName) {
        appendText(itemName);

        try {
            if (mPinPadBinderService == null) {
                appendText("mPinPadBinderService is null");
                return;
            }

            CustomKeyTestVector cmacVector = buildCustomCmacKeyVector();
            CustomKeyTestVector aesVector = buildCustomAesKeyVector();

            int cmacRet = writeCustomWorkingKey(cmacVector);
            appendText("CMAC keyIndex : " + cmacVector.keyIndex);
            appendText("CMAC keyType  : " + getCustomWorkingKeyTypeName(cmacVector.keyType));
            appendText("CMAC key      : " + HexUtil.bcd2str(cmacVector.key));
            appendText("CMAC writeRet : " + cmacRet);
            if (cmacRet == 0) {
                appendCustomWorkingKeyKcv("CMAC", cmacVector);
            }

            appendText("--------------------------------------------------");

            int aesRet = writeCustomWorkingKey(aesVector);
            appendText("AES keyIndex  : " + aesVector.keyIndex);
            appendText("AES keyType   : " + getCustomWorkingKeyTypeName(aesVector.keyType));
            appendText("AES key       : " + HexUtil.bcd2str(aesVector.key));
            appendText("AES writeRet  : " + aesRet);
            if (aesRet == 0) {
                appendCustomWorkingKeyKcv("AES", aesVector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }

    private void testCustomKeyOperation(String itemName, int alg) {
        appendText(itemName);

        try {
            if (mSystemBinderService == null) {
                appendText("mSystemBinderService is null");
                return;
            }

            if (mPinPadBinderService == null) {
                appendText("mPinPadBinderService is null");
                return;
            }

            CustomKeyTestVector vector = buildCustomKeyTestVector(alg);

            int writeRet = writeCustomWorkingKey(vector);
            appendText("writeRet  : " + writeRet);
            if (writeRet != 0) {
                appendText("write custom working key failed");
                return;
            }
            appendCustomWorkingKeyKcv("write", vector);

            byte[] input;
            byte[] iv;

            switch (alg) {
                case CustomKeyOperationType.AES_ECB_ENC:
                    input = vector.plainData;
                    iv = null;
                    break;

                case CustomKeyOperationType.AES_ECB_DEC: {
                    byte[] enc = mSystemBinderService.customKeyOperation(
                            vector.keyIndex,
                            CustomKeyOperationType.AES_ECB_ENC,
                            null,
                            vector.plainData
                    );

                    appendText("encData   : " + (enc == null ? "null" : HexUtil.bcd2str(enc)));

                    if (enc == null) {
                        appendText("prepare ecb enc data failed");
                        return;
                    }

                    input = enc;
                    iv = null;
                    break;
                }

                case CustomKeyOperationType.AES_CBC_ENC:
                    input = vector.plainData;
                    iv = vector.iv;
                    break;

                case CustomKeyOperationType.AES_CBC_DEC: {
                    byte[] enc = mSystemBinderService.customKeyOperation(
                            vector.keyIndex,
                            CustomKeyOperationType.AES_CBC_ENC,
                            vector.iv,
                            vector.plainData
                    );

                    appendText("encData   : " + (enc == null ? "null" : HexUtil.bcd2str(enc)));

                    if (enc == null) {
                        appendText("prepare cbc enc data failed");
                        return;
                    }

                    input = enc;
                    iv = vector.iv;
                    break;
                }

                case CustomKeyOperationType.AES_CMAC:
                    input = vector.plainData;
                    iv = null;
                    break;

                default:
                    appendText("unsupported alg : " + alg);
                    return;
            }

            byte[] out = mSystemBinderService.customKeyOperation(
                    vector.keyIndex,
                    alg,
                    iv,
                    input
            );

            appendText("keyIndex  : " + vector.keyIndex);
            appendText("keyType   : " + getCustomWorkingKeyTypeName(vector.keyType));
            appendText("alg       : " + getCustomKeyAlgName(alg));
            appendText("key       : " + HexUtil.bcd2str(vector.key));
            appendText("iv        : " + (iv == null ? "null" : HexUtil.bcd2str(iv)));
            appendText("input     : " + (input == null ? "null" : HexUtil.bcd2str(input)));
            appendText("output    : " + (out == null ? "null" : HexUtil.bcd2str(out)));

            if (alg == CustomKeyOperationType.AES_ECB_DEC
                    || alg == CustomKeyOperationType.AES_CBC_DEC) {
                boolean same = out != null && Arrays.equals(out, vector.plainData);
                appendText("compare   : " + same);
                appendText("plainData : " + HexUtil.bcd2str(vector.plainData));
            }

        } catch (RemoteException e) {
            e.printStackTrace();
            appendErrorText(itemName, e.getMessage());
        }
    }
}
