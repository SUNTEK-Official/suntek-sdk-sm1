package com.szanfu.sdk.test.util;

public class TLVModel {


    private int TLen;
    private char Tag;
    private char Tag2;
    private char Tag3;
    private int len;
    private String value;

    public int getTLen() {
        return TLen;
    }

    public void setTLen(int tLen) {
        TLen = tLen;
    }

    public char getTag() {
        return Tag;
    }

    public void setTag(char tag) {
        Tag = tag;
    }

    public char getTag2() {
        return Tag2;
    }

    public void setTag2(char tag2) {
        Tag2 = tag2;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public char getTag3() {
        return Tag3;
    }
    public void setTag3(char tag3) {
        Tag3 = tag3;
    }
}
