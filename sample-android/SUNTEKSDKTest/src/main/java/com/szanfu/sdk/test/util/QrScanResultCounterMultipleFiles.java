package com.szanfu.sdk.test.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QrScanResultCounterMultipleFiles {

    public static void main(String[] args) {

        String[] fileNames = {
                "pressureZXingResult.txt",
                "DEFINED_CODE.txt",
                "CUSTOM_FRONT_CAMERA_USE_HW.txt"
        };


        for (String fileName : fileNames) {
            try {
                System.out.println("处理文件: " + fileName);
                int[] counts = countQrScanResults(fileName);
                System.out.println("  qrScanresult为-1的数量: " + counts[0]);
                System.out.println("  qrScanresult为0的数量: " + counts[1]);
                System.out.println("  qrScanresult为其他值的数量: " + counts[2]);
                System.out.println("  qrScanresult总数量: " + (counts[0] + counts[1] + counts[2]));
                System.out.println("-----------------------------");
            } catch (IOException e) {
                System.err.println("无法读取文件: " + fileName + " - " + e.getMessage());
            } catch (Exception e) {
                System.err.println("处理文件时出错: " + fileName + " - " + e.getMessage());
            }
        }
    }

    /**
     * 统计文件中qrScanresult值的数量
     * @param fileName 文件名
     * @return 数组，[0]=-1的数量，[1]=0的数量，[2]=其他值的数量
     * @throws IOException 文件读取异常
     */
    private static int[] countQrScanResults(String fileName) throws IOException {

        int countMinus1 = 0;
        int count0 = 0;
        int countOther = 0;


        String content = new String(Files.readAllBytes(Paths.get(fileName)));

 // qrScanresult
        Pattern pattern = Pattern.compile("\"qrScanresult\":(-?\\d+)");
        Matcher matcher = pattern.matcher(content);


        while (matcher.find()) {
            try {
                int value = Integer.parseInt(matcher.group(1));


                if (value == -1) {
                    countMinus1++;
                } else if (value == 0) {
                    count0++;
                } else {
                    countOther++;

 // System.out.println(" : " + value);
                }
            } catch (NumberFormatException e) {
                System.err.println("无法解析数值: " + matcher.group(1));
            }
        }


        return new int[]{countMinus1, count0, countOther};
    }

    /**
     * 方法2：使用字符串分割（如果文件格式完全一致且只关心-1和0）
     * 这个方法比正则表达式更快，但不够灵活
     */
    private static int[] countQrScanResultsSimple(String fileName) throws IOException {

        String content = new String(Files.readAllBytes(Paths.get(fileName)));


        int countMinus1 = countOccurrences(content, "\"qrScanresult\":-1");
        int count0 = countOccurrences(content, "\"qrScanresult\":0");

 // ( )
 // "qrScanresult" -1 0
        int totalQrScanresult = countOccurrences(content, "\"qrScanresult\":");
        int countOther = totalQrScanresult - countMinus1 - count0;

        return new int[]{countMinus1, count0, countOther};
    }

    /**
     * 统计字符串中特定模式出现的次数
     * @param text 文本
     * @param pattern 要查找的模式
     * @return 出现次数
     */
    private static int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;

        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }

        return count;
    }

    /**
     * 方法3：使用更详细的正则表达式匹配每个完整的JSON对象
     * 如果需要提取更多信息，可以使用这个方法
     */
    private static int[] countQrScanResultsDetailed(String fileName) throws IOException {
        int countMinus1 = 0;
        int count0 = 0;
        int countOther = 0;


        String content = new String(Files.readAllBytes(Paths.get(fileName)));

 // JSON : {"qrScanresult": ,"qrScanUseTime":" "}

        Pattern pattern = Pattern.compile("\\{\"qrScanresult\":(-?\\d+),\"qrScanUseTime\":\"[^\"]*\"\\}");
        Matcher matcher = pattern.matcher(content);

        while (matcher.find()) {
            try {
 // JSON ( )
                String fullMatch = matcher.group(0);
 // System.out.println(" : " + fullMatch);

 // qrScanresult
                String valueStr = matcher.group(1);
                int value = Integer.parseInt(valueStr);


                if (value == -1) {
                    countMinus1++;
                } else if (value == 0) {
                    count0++;
                } else {
                    countOther++;
                }
            } catch (NumberFormatException e) {
                System.err.println("无法解析数值: " + matcher.group(1));
            }
        }

        return new int[]{countMinus1, count0, countOther};
    }
}
