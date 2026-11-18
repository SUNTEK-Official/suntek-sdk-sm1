package com.szanfu.sdk.test.util;

import android.util.Log;

public class PatternLogger {
    private static final String BLOCK = "▀";
    private static final String BLOCK_WITH_SPACE = "▀ ";
    private static final int LINE_LENGTH_ODD = 21;
    private static final int LINE_LENGTH_EVEN = 16;

    public static String printPattern(int totalLines) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= totalLines; i++) {
            if (i % 2 == 1) {

                String strOddLine = printOddLine(i)+"\n";
                stringBuilder.append(strOddLine);
            } else {

                String strEvenLint = printEvenLine(i)+"\n";
                stringBuilder.append(strEvenLint);
            }
        }
        return stringBuilder.toString();
    }

    private static String printOddLine(int lineNumber) {
        StringBuilder line = new StringBuilder();
        for (int j = 0; j < LINE_LENGTH_ODD; j++) {
            line.append(BLOCK);
        }
        LogUtils.d( "第" + lineNumber + "行(无空格): " + line.toString());
        return line.toString();
    }

    private static String printEvenLine(int lineNumber) {
        StringBuilder line = new StringBuilder();
        for (int j = 0; j < LINE_LENGTH_EVEN; j++) {
            line.append(BLOCK_WITH_SPACE);
        }
        LogUtils.d("第" + lineNumber + "行(有空格): " + line.toString());
    return  line.toString();
    }
}
