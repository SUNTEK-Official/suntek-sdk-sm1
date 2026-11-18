package com.szanfu.sdk.test.util;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.Security;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * 加密算法KCV计算工具类
 * 支持DES、3DES、SM4算法
 */
public class KCVUtil {
//    String dataHex = "0000000000000000";
    static {
 // BouncyCastle SM4
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * 十六进制字符串转字节数组
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] bytes = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            bytes[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return bytes;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * 字节数组转十六进制字符串
     */
    public static String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder("");
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    /**
     * 计算DES算法的KCV值
     * @param keyHex 十六进制格式的密钥
     *  dataHex 十六进制格式的明文数据
     * @return KCV值（加密结果的前6个十六进制字符）
     */
    public static String computeDESKCV(String keyHex) {
        try {
            String dataHex = "0000000000000000";
            byte[] keyBytes = hexStringToBytes(keyHex);
            byte[] dataBytes = hexStringToBytes(dataHex);

            DESKeySpec desKeySpec = new DESKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey secretKey = keyFactory.generateSecret(desKeySpec);

            Cipher cipher = Cipher.getInstance("DES/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(dataBytes);

            String encryptedHex = bytesToHexString(encryptedData);
 // KCV 6 (3 )
            return encryptedHex.substring(0, 6);
//            return encryptedHex.substring(0, encryptedHex.length());
        } catch (Exception e) {
            throw new RuntimeException("DES KCV计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算3DES算法的KCV值
     * @param keyHex 十六进制格式的密钥
     *  dataHex 十六进制格式的明文数据
     * @return KCV值（加密结果的前6个十六进制字符）
     */
    public static String compute3DESKCV(String keyHex) {
        try {
            String dataHex = "0000000000000000";
            byte[] keyBytes = hexStringToBytes(keyHex);
            byte[] dataBytes = hexStringToBytes(dataHex);

            DESedeKeySpec desedeKeySpec = new DESedeKeySpec(keyBytes);
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
            SecretKey secretKey = keyFactory.generateSecret(desedeKeySpec);

            Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(dataBytes);

            String encryptedHex = bytesToHexString(encryptedData);
 // KCV 6 (3 )
            return encryptedHex.substring(0, 6);
//            return encryptedHex.substring(0, encryptedHex.length());
        } catch (Exception e) {
            throw new RuntimeException("3DES KCV计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 计算SM4算法的KCV值和完整加密结果
     * @param keyHex 十六进制格式的密钥
     *  dataHex 十六进制格式的明文数据
     * @return 包含KCV和完整加密结果的字符串数组 [KCV, 完整结果]
     */
    public static String[] computeSM4KCV(String keyHex) {
        try {
            String dataHex = "0000000000000000";
            byte[] keyBytes = hexStringToBytes(keyHex);
            byte[] dataBytes = hexStringToBytes(dataHex);

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "SM4");

            Cipher cipher = Cipher.getInstance("SM4/ECB/NoPadding", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedData = cipher.doFinal(dataBytes);

            String encryptedHex = bytesToHexString(encryptedData);
 // SM4 KCV 6 (3 )
            String kcv = encryptedHex.substring(0, 6);
//            String kcv = encryptedHex.substring(0, encryptedHex.length());


            return new String[]{kcv, encryptedHex};
        } catch (Exception e) {
            throw new RuntimeException("SM4 KCV计算失败: " + e.getMessage(), e);
        }
    }

    /**
     * 测试示例
     */
    public static void main(String[] args) {
        LogUtils.e("=== DES/3DES/SM4 KCV计算示例 ===");

 // 1: DES
        String desKey1 = "0123456789ABCDEF"; // 8 DES
        String desData1 = "0000000000000000";
        String desKcv1 = computeDESKCV(desKey1);
        LogUtils.e("DES算法 - 全零数据:");
        LogUtils.e("密钥: " + desKey1);
        LogUtils.e("数据: " + desData1);
        LogUtils.e("KCV值: " + desKcv1);
        LogUtils.e("**************************************");

 // 2: DES
        String desKey2 = "0123456789ABCDEF";
        String desData2 = "1234567890123456";
        String desKcv2 = computeDESKCV(desKey2);
        LogUtils.e("DES算法 - 特定数据:");
        LogUtils.e("密钥: " + desKey2);
        LogUtils.e("数据: " + desData2);
        LogUtils.e("KCV值: " + desKcv2);
        LogUtils.e("**************************************");


 // 3: 3DES
        String tripleDesKey1 = "0123456789ABCDEF0123456789ABCDEF"; // 16 3DES
        String tripleDesData1 = "0000000000000000";
        String tripleDesKcv1 = compute3DESKCV(tripleDesKey1);
        LogUtils.e("3DES算法 - 全零数据:");
        LogUtils.e("密钥: " + tripleDesKey1);
        LogUtils.e("数据: " + tripleDesData1);
        LogUtils.e("KCV值: " + tripleDesKcv1);
        LogUtils.e("**************************************");


 // 4: 3DES
        String tripleDesKey2 = "0123456789ABCDEF0123456789ABCDEF";
        String tripleDesData2 = "1234567890123456";
        String tripleDesKcv2 = compute3DESKCV(tripleDesKey2);
        LogUtils.e("3DES算法 - 特定数据:");
        LogUtils.e("密钥: " + tripleDesKey2);
        LogUtils.e("数据: " + tripleDesData2);
        LogUtils.e("KCV值: " + tripleDesKcv2);
        LogUtils.e("**************************************");


 // 5: SM4
        String sm4Key = "0123456789ABCDEFFEDCBA9876543210"; // 16 SM4
        String sm4Data = "00000000000000000000000000000000";
        String[] sm4Result = computeSM4KCV(sm4Key);
        LogUtils.e("SM4算法 - 全零数据:");
        LogUtils.e("密钥: " + sm4Key);
        LogUtils.e("数据: " + sm4Data);
        LogUtils.e("KCV值: " + sm4Result[0]);
        LogUtils.e("完整加密结果: " + sm4Result[1]);
        LogUtils.e("**************************************");


 // 6: 24 3DES
        String tripleDesKey24 = "0123456789ABCDEF0123456789ABCDEF0123456789ABCDEF";
        String tripleDesKcv24 = compute3DESKCV(tripleDesKey24);
        LogUtils.e("3DES算法(24字节密钥) - 全零数据:");
        LogUtils.e("密钥: " + tripleDesKey24);
        LogUtils.e("数据: " + desData1);
        LogUtils.e("KCV值: " + tripleDesKcv24);
    }
}
