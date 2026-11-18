package com.szanfu.sdk.test.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.szanfu.sdk.test.R;

public class SignatureValidator {
    private static final String TAG = "SignatureValidator";
    private static final long MIN_SIGNATURE_COMPLEXITY = 10000;
    private static final long MAX_SIGNATURE_COMPLEXITY = 40000;
    private static final float MIN_SIGNATURE_AREA_PERCENT = 0.05f;
    private static final float MAX_SIGNATURE_AREA_PERCENT = 0.9f;

    /**
     * 校验签名是否符合要求
     *
     * @param signatureBitmap 签名的 Bitmap 对象
     * @return 校验结果
     */
    public static ValidationResult validateSignature(Bitmap signatureBitmap, Context mContext) {
        if (signatureBitmap == null) {
            return new ValidationResult(false, mContext.getString(R.string.signature_cannot_be_empty));
        }


        long complexity = calculateComplexity(signatureBitmap);
        LogUtils.e( "validateSignature: 检查签名复杂度->" + complexity);
        if (complexity < MIN_SIGNATURE_COMPLEXITY) {
            return new ValidationResult(false, mContext.getString(R.string.signature_too_simple));
        }
 // So: ;
//        else if (complexity > MAX_SIGNATURE_COMPLEXITY) {
//            return new ValidationResult(false, mContext.getString(R.string.signature_too_complex));
//        }


        /*float areaPercent = calculateSignatureAreaPercent(signatureBitmap);
        Log.e(TAG, "validateSignature: 检查签名面积占比->" + areaPercent);
        if (areaPercent < MIN_SIGNATURE_AREA_PERCENT) {
            return new ValidationResult(false, "签名面积过小，请重新签名");
        } else if (areaPercent > MAX_SIGNATURE_AREA_PERCENT) {
            return new ValidationResult(false, "签名面积过大，请重新签名");
        }*/
        return new ValidationResult(true, "签名校验通过");
    }

    /**
     * 计算签名复杂度（按像素计算签名的复杂度）
     *
     * @param bitmap 签名 Bitmap
     * @return 复杂度
     */
    private static long calculateComplexity(Bitmap bitmap) {
        if (bitmap == null) return 0;

        long complexity = 0;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel != 0) {
                    complexity++;
                }
            }
        }
        return complexity;
    }

    /**
     * 计算签名占用的面积百分比
     *
     * @param bitmap 签名 Bitmap
     * @return 签名占用面积百分比
     */
    private static float calculateSignatureAreaPercent(Bitmap bitmap) {
        if (bitmap == null) return 0;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int totalPixels = width * height;

        int signaturePixels = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = bitmap.getPixel(x, y);
                if (pixel != 0) {
                    signaturePixels++;
                }
            }
        }

        return (float) signaturePixels / totalPixels;
    }

    public static class ValidationResult {
        public boolean isValid;
        public String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }
}
