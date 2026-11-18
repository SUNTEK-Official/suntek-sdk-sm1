package com.szanfu.sdk.test.util;


import android.os.Build;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataConversionUtils {
    private static final String TAG = "DataConversionUtils";


    public static char[] StringToASCIIArray(String s) {
        if (s == null) {
            return new char[0];
        }
        return s.toCharArray();
    }

    /**
     * 字符串转16进制字符串 3030 >>>>> 33303330
     *
     * @param s
     * @return
     */
    public static byte[] StringToByteArray(String s) {
        int sl = s.length();
        byte[] charArray = new byte[sl];
        for (int i = 0; i < sl; i++) {
            char charElement = s.charAt(i);
            charArray[i] = (byte) charElement;
        }
        return charArray;
    }

    public static char[] ByteToCharArray(byte[] bs) {
        int bsl = bs.length;
        char[] charArray = new char[bsl];
        for (int i = 0; i < bsl; i++) {
            charArray[i] = (char) (((char) bs[i]) & 0x00FF);
        }
        return charArray;
    }

    /**
     * 将byte类型变量转为char类型
     *
     * @param b
     * @return
     */
    public static char byte2char(byte b) {
        return (char) (((char) b) & 0x00FF);
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param bs
     * @return
     */
    public static byte[] hexString2ByteArray(String bs) {
        int bsLength = bs.length();
        if (bsLength % 2 != 0) {
            return null;
        }
        byte[] cs = new byte[bsLength / 2];
        String st = "";
        for (int i = 0; i < bsLength; i = i + 2) {
            st = bs.substring(i, i + 2);
            cs[i / 2] = (byte) Integer.parseInt(st, 16);
        }
        return cs;
    }

    /**
     * 将byte数组以16进制字符显示
     *
     * @param b
     */
    public static String showResult16Str(byte[] b) {
        if (b == null) {
            return "";
        }
        String rs = "";
        int bl = b.length;
        byte bt;
        String bts = "";
        int btsl;
        for (int i = 0; i < bl; i++) {
            bt = b[i];
            bts = Integer.toHexString(bt);
            btsl = bts.length();
            if (btsl > 2) {
                bts = bts.substring(btsl - 2).toUpperCase();
            } else if (btsl == 1) {
                bts = "0" + bts.toUpperCase();
            } else {
                bts = bts.toUpperCase();
            }
//			System.out.println("i::"+i+">>>bts::"+bts);
            rs += bts;
        }
        return rs;
    }

    /**
     * 将byte数组以16进制0x样式字符显示
     *
     * @param b
     */
    public static String showResult0xStr(byte[] b) {
        String rs = "";
        int bl = b.length;
        byte bt;
        String bts = "";
        int btsl;
        for (int i = 0; i < bl; i++) {
            bt = b[i];
            bts = Integer.toHexString(bt);
            btsl = bts.length();
            if (btsl > 2) {
                bts = "0x" + bts.substring(btsl - 2);
            } else if (btsl == 1) {
                bts = "0x0" + bts;
            } else {
                bts = "0x" + bts;
            }
//			System.out.println("i::"+i+">>>bts::"+bts);
            rs += (bts + " ");
        }
//		System.out.println("rs::"+rs);
        return rs;
    }


    /**
     * 检测字符串是否是合法的十六进制展开
     *
     * @param str
     * @return
     */
    public static boolean checkHexStr(String str) {
        String regEx = "[^(0-9a-fA-F)]";
        Pattern p = Pattern.compile(regEx);
        Matcher m;
        boolean isNum = true;
        char[] pns = str.toCharArray();
        int pl = pns.length;
        for (int i = 0; i < pl; i++) {
            m = p.matcher("" + pns[i]);
            if (m.find()) {
                isNum = false;
                break;
            }
        }
        return isNum;
    }


    public static String toHexCode(byte[] data, int offset, int length) {
        final StringBuilder stringBuilder = new StringBuilder(data.length * 2);
        for (int i = offset; i < offset + length; i++) {
            byte byteChar = data[i];
            stringBuilder.append(String.format("%02X", byteChar).toUpperCase());
        }
        return stringBuilder.toString();
    }


    /**
     * 主密钥源密钥索引，（0或者2~10）当usScrKeyIdx =0时,表示明文写入
     *
     * @param keyIndex 秘钥索引
     * @return true:正常 false:异常
     */
    public static boolean checkImportMainKeySerkeyIndex(int srcKeyIdx) {
        String keyIndexStr = Integer.toString(srcKeyIdx);
        String regex = "^(0|[2-9]|10)$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * 检查秘钥索引是否正常
     *
     * @param keyIndex 秘钥索引
     * @return true:正常 false:异常
     */
    public static boolean checkKeyIndex(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);

        String regex = "^(10|[2-9])$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * DUKPT输入PIN检查秘钥索引
     *
     * @param keyIndex 秘钥索引
     * @return true:正常 false:异常
     */
    public static boolean checkUDKPTKeyIndex(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);

//        String regex = "^(?:[2-9]|1[0-9]|2[0-9]|30)$";
        String regex = "^(1[1-9]|2[0-9]|30)$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * 根据 keyIndex 判断密钥类型
     *
     * @param keyIndex 密钥索引
     * @return 0: Master/Session Key 1:
     */
    public static int getKeyTypeDescription(int keyIndex) {
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

    /**
     * 使用秘钥进行加解密计算
     * public static final int MAC_KEY_ID = 4; //mac key
     * public static final int TDK_KEY_ID = 5; //track key
     * public static final int TCK_KEY_ID = 6; //data key
     *
     * @param keyIndex 只允许 MAC_KEY_ID  TDK_KEY_ID TCK_KEY_ID
     * @return
     */
    public static boolean checkCalcDesKeyIndex(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);

        String regex = "^[4-6]$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * 工作秘钥KEY
     *
     * @param keyIndex POS_TPK 0x03 // PinKey
     *                 * POS_TAK 0x04 // MacKey
     *                 * POS_TDK 0x05 // TDEA decrypt key
     *                 * POS_TCK 0x06 // account encryption or decryption key
     * @return
     */
    public static boolean checkWkKeyIndex(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);
        String regex = "^([3-6])$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * usMKeyNo 源秘钥地址
     *
     * @param keyIndex
     * @return
     */
    public static boolean checkuSMKeyNo(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);
        String regex = "^(10|[2-9])$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * ucSrcKeyIdx 源密钥索引（0或者2~10）
     *
     * @param keyIndex
     * @return
     */
    public static boolean checkUcSrcKeyIdx(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);
        String regex = "^(10|[2-9])$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * 获取秘钥Check Value
     *
     * @param keyIndex 0-6
     * @return
     */
    public static boolean keyCheckValue(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);
        String regex = "^[0-6]$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * 计算MAC
     *
     * @param keyIndex 0-7
     * @return
     */
    public static boolean checkCalcMac(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);
        String regex = "^[0-7]$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * DUKPE秘钥索引必须满足 11~30
     *
     * @param keyIndex 11~30
     * @return
     */
    public static boolean checkDUKPEIndex(int keyIndex) {

        String keyIndexStr = Integer.toString(keyIndex);
        String regex = "^(1[1-9]|2[0-9]|30)$";

        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(keyIndexStr);

        return matcher.matches();
    }

    /**
     * 检查交易卡类型
     *
     * @param cardType 卡类型
     * @return true:正常 false:异常
     */
    public static boolean showPinCheckCardType(int cardType) {
        Log.e(TAG, "showPinCheckCardType: " + cardType);
        String strCardType = Integer.toString(cardType);
        String regex = "^(1|2|4)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(strCardType);
        return matcher.matches();
    }


    /**
     * 检查银行卡规则,支持最后一位是数字或 'F'
     * todo 不仅仅有1个F的情况，还有多个F的情况；  onEmvInputPIN:currentCardNumber-> 3540829999421012FFFF
     * @param pan 银行卡号，正常长度12-19位，后面如果带F自动加1
     *             @param pan 银行卡号，正常长度4-19位，后面如果带F自动加1  BCTC 测试案例，银行卡卡号 4-19 todo
     * @return true:合规 false:异常
     */
    public static boolean containsBankCardNumber(String pan) {
        LogUtils.e("containsBankCardNumber pan:"+pan);

        if (pan.contains("F")){
            String[] strPan=pan.split("F");
            pan = strPan[0];
        }
        LogUtils.e("containsBankCardNumber pan:"+pan);
        int minLen = 4;
        int maxLen = 22;

        if (isLastCharacterDigitOrF(pan)) {
            minLen++;
            maxLen++;
        }

        if (pan.length() < minLen || pan.length() > maxLen) {
            return false;
        }

        int panLen = pan.length() - 1;
        String pattern = "\\d{3," + panLen + "}[\\dFf]";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(pan);
        LogUtils.e("containsBankCardNumber boolean : "+m.matches());
        return m.matches();
    }

    private static boolean isLastCharacterDigitOrF(String pan) {
        return String.valueOf(pan.charAt(pan.length() - 1)).equalsIgnoreCase("F");
    }

    /**
     * 获取设备平台
     *
     * @return 【A1移远】 【A2 MTK】 【A3 820】
     */
    public static String getCustomerType() {

        //SZANFU_AF930_V1.32_02_20240329_A2_userdebug
        try {
            String[] fmtSystem = Build.DISPLAY.split("_");
            int systemInfoLen = fmtSystem.length;
            return fmtSystem[systemInfoLen - 2];
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static byte[] mergeByteArrays(byte[] array1, byte[] array2) {
        byte[] mergedArray = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, mergedArray, 0, array1.length);
        System.arraycopy(array2, 0, mergedArray, array1.length, array2.length);
        return mergedArray;
    }

    /**
     * 将16进制字符串转换为ASCII字符串 3030-》00
     *
     * @param hex 16进制字符串（每两个字符代表一个字节）
     * @return 转换后的ASCII字符串
     */
    public static String hexStringToAsciiString(String hex) {
        StringBuilder output = new StringBuilder();
        output.setLength(0);
        for (int i = 0; i < hex.length(); i += 2) {
            String str = hex.substring(i, i + 2);
            int decimal = Integer.parseInt(str, 16);
            output.append((char) decimal);
        }
        return output.toString();
    }
}
