package com.szanfu.sdk.test.bean;

import org.litepal.crud.LitePalSupport;

/**
 * @param height 图片高度
 * @param diffInMills 打印时长
 */
public class PrintParm extends LitePalSupport {
    int height;
    double diffInMills;

    public double getDiffInMills() {
        return diffInMills;
    }

    public void setDiffInMills(double diffInMills) {
        this.diffInMills = diffInMills;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public String toString() {
        return "PrintParm{" +
                "height=" + height +
                ", diffInMills=" + diffInMills +
                '}';
    }
}
