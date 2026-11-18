package com.szanfu.sdk.test.util;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Base64;

/**
 * String与byte[]转换工具类
 * 提供多种编码方式的字符串与字节数组相互转换功能
 */
public class StringByteConverter {

    /**
     * 使用UTF-8编码将String转换为byte[]
     *
     * @param str 要转换的字符串
     * @return UTF-8编码的字节数组，如果输入为null则返回空数组
     */
    public static byte[] stringToBytes(String str) {
        return stringToBytes(str, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定编码将String转换为byte[]
     *
     * @param str 要转换的字符串
     * @param charsetName 字符编码名称（如"UTF-8", "GBK", "ISO-8859-1"等）
     * @return 指定编码的字节数组
     * @throws UnsupportedCharsetException 如果指定的编码不被支持
     */
    public static byte[] stringToBytes(String str, String charsetName) {
        if (str == null) {
            return new byte[0];
        }

        Charset charset;
        try {
            charset = Charset.forName(charsetName);
        } catch (Exception e) {
            throw new UnsupportedCharsetException("不支持的编码格式: " + charsetName);
        }

        return stringToBytes(str, charset);
    }

    /**
     * 使用指定Charset将String转换为byte[]
     *
     * @param str 要转换的字符串
     * @param charset 字符编码
     * @return 指定编码的字节数组，如果输入为null则返回空数组
     */
    public static byte[] stringToBytes(String str, Charset charset) {
        if (str == null) {
            return new byte[0];
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        return str.getBytes(charset);
    }

    /**
     * 使用UTF-8编码将byte[]转换为String
     *
     * @param bytes 字节数组
     * @return 解码后的字符串，如果输入为null则返回空字符串
     */
    public static String bytesToString(byte[] bytes) {
        return bytesToString(bytes, StandardCharsets.UTF_8);
    }

    /**
     * 使用指定编码将byte[]转换为String
     *
     * @param bytes 字节数组
     * @param charsetName 字符编码名称
     * @return 解码后的字符串
     * @throws UnsupportedCharsetException 如果指定的编码不被支持
     */
    public static String bytesToString(byte[] bytes, String charsetName) {
        if (bytes == null) {
            return "";
        }

        Charset charset;
        try {
            charset = Charset.forName(charsetName);
        } catch (Exception e) {
            throw new UnsupportedCharsetException("不支持的编码格式: " + charsetName);
        }

        return bytesToString(bytes, charset);
    }

    /**
     * 使用指定Charset将byte[]转换为String
     *
     * @param bytes 字节数组
     * @param charset 字符编码
     * @return 解码后的字符串，如果输入为null则返回空字符串
     */
    public static String bytesToString(byte[] bytes, Charset charset) {
        if (bytes == null) {
            return "";
        }

        if (charset == null) {
            charset = StandardCharsets.UTF_8;
        }

        return new String(bytes, charset);
    }

    /**
     * 将字符串转换为Base64编码的字节数组
     *
     * @param str 要转换的字符串
     * @return Base64编码的字节数组
     */
    public static byte[] stringToBase64Bytes(String str) {
        if (str == null) {
            return new byte[0];
        }

        String base64Str = Base64.getEncoder().encodeToString(stringToBytes(str));
        return stringToBytes(base64Str);
    }

    /**
     * 将Base64编码的字节数组解码为字符串
     *
     * @param base64Bytes Base64编码的字节数组
     * @return 解码后的原始字符串
     */
    public static String base64BytesToString(byte[] base64Bytes) {
        if (base64Bytes == null) {
            return "";
        }

        String base64Str = bytesToString(base64Bytes);
        byte[] decodedBytes = Base64.getDecoder().decode(base64Str);
        return bytesToString(decodedBytes);
    }

    /**
     * 将字符串转换为十六进制表示的字节数组
     *
     * @param str 要转换的字符串
     * @return 十六进制表示形式的字节数组
     */
    public static byte[] stringToHexBytes(String str) {
        if (str == null) {
            return new byte[0];
        }

        String hexString = bytesToHex(stringToBytes(str));
        return stringToBytes(hexString);
    }

    /**
     * 将十六进制表示的字节数组解码为字符串
     *
     * @param hexBytes 十六进制表示形式的字节数组
     * @return 解码后的原始字符串
     */
    public static String hexBytesToString(byte[] hexBytes) {
        if (hexBytes == null) {
            return "";
        }

        String hexStr = bytesToString(hexBytes);
        byte[] decodedBytes = hexToBytes(hexStr);
        return bytesToString(decodedBytes);
    }

    /**
     * 将字节数组转换为十六进制字符串
     *
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    public static String bytesToHex(byte[] bytes) {
        if (bytes == null) {
            return "";
        }

        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * 将十六进制字符串转换为字节数组
     *
     * @param hexString 十六进制字符串
     * @return 字节数组
     */
    public static byte[] hexToBytes(String hexString) {
        if (hexString == null || hexString.length() % 2 != 0) {
            return new byte[0];
        }

        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 获取字符串在不同编码下的字节长度
     *
     * @param str 字符串
     * @param charsetName 字符编码名称
     * @return 字节长度
     */
    public static int getByteLength(String str, String charsetName) {
        if (str == null) {
            return 0;
        }

        try {
            return stringToBytes(str, charsetName).length;
        } catch (UnsupportedCharsetException e) {
 // UTF-8
            return stringToBytes(str).length;
        }
    }

    /**
     * 比较两个字符串在指定编码下的字节数组是否相同
     *
     * @param str1 字符串1
     * @param str2 字符串2
     * @param charsetName 字符编码名称
     * @return 如果字节数组相同返回true，否则返回false
     */
    public static boolean compareByteArrays(String str1, String str2, String charsetName) {
        byte[] bytes1 = stringToBytes(str1, charsetName);
        byte[] bytes2 = stringToBytes(str2, charsetName);

        if (bytes1.length != bytes2.length) {
            return false;
        }

        for (int i = 0; i < bytes1.length; i++) {
            if (bytes1[i] != bytes2[i]) {
                return false;
            }
        }

        return true;
    }
}
