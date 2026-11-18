package com.szanfu.sdk.test.util;

import java.nio.charset.StandardCharsets;

/**
 * ASCII字节数组转换工具类
 */
public class AsciiUtils {

    /**
     * 将ASCII字节数组转换为字符串
     * @param bytes ASCII字节数组
     * @return 转换后的字符串，如果输入为null则返回null
     */
    public static String bytesToAsciiString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return new String(bytes, StandardCharsets.US_ASCII);
    }

    /**
     * 将ASCII字节数组转换为字符串（带范围限制）
     * @param bytes ASCII字节数组
     * @param offset 起始位置
     * @param length 转换长度
     * @return 转换后的字符串，如果输入为null则返回null
     */
    public static String bytesToAsciiString(byte[] bytes, int offset, int length) {
        if (bytes == null) {
            return null;
        }
        if (offset < 0 || length < 0 || offset + length > bytes.length) {
            throw new IllegalArgumentException("Invalid offset or length");
        }
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }

    /**
     * 将字符串转换为ASCII字节数组
     * @param str 输入字符串
     * @return ASCII字节数组，如果输入为null则返回null
     */
    public static byte[] asciiStringToBytes(String str) {
        if (str == null) {
            return null;
        }
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    /**
     * 安全转换 - 将可能包含非ASCII字符的字节数组转换为字符串
     * 非ASCII字符会被替换为'?'
     * @param bytes 字节数组
     * @return 转换后的字符串
     */
    public static String safeBytesToAsciiString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder(bytes.length);
        for (byte b : bytes) {
 // ASCII 0-127, '?'
            if (b >= 0 && b <= 127) {
                sb.append((char) b);
            } else {
                sb.append('?');
            }
        }
        return sb.toString();
    }

    /**
     * 验证字节数组是否只包含ASCII字符
     * @param bytes 要验证的字节数组
     * @return 如果所有字节都在ASCII范围内返回true，否则返回false
     */
    public static boolean isPureAscii(byte[] bytes) {
        if (bytes == null) {
            return false;
        }

        for (byte b : bytes) {
            if (b < 0 || b > 127) {
                return false;
            }
        }
        return true;
    }
}
