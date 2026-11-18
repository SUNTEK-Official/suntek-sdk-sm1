package com.szanfu.sdk.test.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.util.Arrays;

public class DUKPTValidation {

    private static final String DERIVATION_KEY = "0123456789ABCDEFFEDCBA9876543210";
    private static final String[][] TEST_DATA = {
            // KSN,                   Expected PIN Block, Expected MAC,     Expected Encrypted Data
            {"FFFF9876543210E00000", "041274EDCBA9876F", "4012345678909D987", "89ABCDEF01234567"},
            {"FFFF9876543210E00001", "1B9C1845EB993A7A", "20364223C1FF00FA", "89ABCDEF01234567"},
            {"FFFF9876543210E00002", "10A01C8D02C69107", "CDEF0123456789AB", "1234567890ABCDEF"},
            {"FFFF9876543210E00003", "18DC07B94797B466", "ABCDEF0123456789", "FEDCBA9876543210"},
            {"FFFF9876543210E00004", "0BC79509D5645DF7", "789ABCDEF0123456", "567890ABCDEF1234"},
            {"FFFF9876543210E00005", "5BC0AF22AD87B327", "456789ABCDEF0123", "90ABCDEF12345678"}
    };

    public static void main(String[] args) throws Exception {
        for (String[] test : TEST_DATA) {
            String ksn = test[0];
            String expectedPinBlock = test[1];
            String expectedMac = test[2];
            String expectedData = test[3];

            System.out.println("Testing KSN: " + ksn);
            String derivedKey = deriveKey(DERIVATION_KEY, ksn);

            // Validate PIN encryption
            String pinBlock = encryptData(derivedKey, "1234");
            System.out.println("PIN Block: " + pinBlock + " (Expected: " + expectedPinBlock + ")");
            assert pinBlock.equalsIgnoreCase(expectedPinBlock);

            // Validate MAC calculation
            String mac = encryptData(derivedKey, "MAC Test Data");
            System.out.println("MAC: " + mac + " (Expected: " + expectedMac + ")");
            assert mac.equalsIgnoreCase(expectedMac);

            // Validate data encryption
            String encryptedData = encryptData(derivedKey, "Sensitive Data");
            System.out.println("Encrypted Data: " + encryptedData + " (Expected: " + expectedData + ")");
            assert encryptedData.equalsIgnoreCase(expectedData);

            System.out.println("Test Passed!\n");
        }
    }


    private static String deriveKey(String derivationKey, String ksn) {

        return derivationKey.substring(0, 16) + ksn.substring(0, 4);
    }


    private static String encryptData(String key, String data) throws Exception {
        byte[] keyBytes = hexStringToByteArray(key);
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "DESede");
        Cipher cipher = Cipher.getInstance("DESede/CBC/NoPadding");


        IvParameterSpec ivSpec = new IvParameterSpec(new byte[8]);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);


        byte[] paddedData = padData(data.getBytes());
        byte[] encrypted = cipher.doFinal(paddedData);
        return byteArrayToHex(encrypted);
    }

 // (8 )
    private static byte[] padData(byte[] data) {
        int length = (data.length + 7) / 8 * 8;
        return Arrays.copyOf(data, length);
    }


    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


    private static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
