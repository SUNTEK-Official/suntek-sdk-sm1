package com.szanfu.sdk.test.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.TextPaint;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

public class TextToImageUtil {

    public static Bitmap createTextImage(String text, int width, TextConfig config) {
        if (TextUtils.isEmpty(text)) {
            text = "";
        }


        if (config == null) {
            config = new TextConfig();
        }


        TextPaint paint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(config.textSize);
        paint.setColor(config.textColor);
        paint.setTypeface(config.bold == 1 ? Typeface.create(config.typeface, Typeface.BOLD) : config.typeface);


        if (config.underline == 1) {
            paint.setUnderlineText(true);
            paint.setStrokeWidth(config.underlineWidth);
        }


        Paint.FontMetrics fm = paint.getFontMetrics();
        float lineHeight = fm.descent - fm.ascent;
        float lineSpacing = (config.lineSpacingMultiplier - 1f) * lineHeight;
        float baseLineOffset = -fm.ascent;

 // ( )
        List<String> lines = splitLines(text, paint, width - 2 * config.padding);


        int height = (int) (config.padding * 2 + baseLineOffset
                + (lines.size() - 1) * (lineHeight + lineSpacing)
                + lineHeight);

 // Bitmap
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);


        if (config.backgroundColor != Color.TRANSPARENT) {
            canvas.drawColor(config.backgroundColor);
        }


        float y = config.padding + baseLineOffset;
        for (String line : lines) {
            float x = getStartX(line, width, paint, config.align);


            canvas.drawText(line, x, y, paint);

 // ( )
            if (config.underline == 1 && !TextUtils.isEmpty(line)) {
                float textWidth = paint.measureText(line);
                float underlineY = y + fm.descent + config.underlinePadding;

                Paint underlinePaint = new Paint();
                underlinePaint.setColor(config.underlineColor);
                underlinePaint.setStrokeWidth(config.underlineWidth);

                canvas.drawLine(x, underlineY, x + textWidth, underlineY, underlinePaint);
            }

            y += lineHeight + lineSpacing;
        }


        Bitmap scaledBitmap = scaleBitmap(bitmap, config.scaleX, config.scaleY);
        bitmap.recycle();


        return rotateBitmap(scaledBitmap, config.orientation);
    }

    private static List<String> splitLines(String text, Paint paint, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (maxWidth <= 0) return lines;


        String[] paragraphs = text.split("\n", -1);

        for (String para : paragraphs) {
            if (TextUtils.isEmpty(para)) {
                lines.add("");
                continue;
            }

            int start = 0;
            int end = 1;
            int len = para.length();

            while (end <= len) {
                float width = paint.measureText(para, start, end);

                if (width < maxWidth) {
                    end++;
                } else {

                    int breakPoint = findBreakPoint(para, start, end);

                    if (breakPoint <= start) {
                        lines.add(para.substring(start, end - 1));
                        start = end - 1;
                    } else {
                        lines.add(para.substring(start, breakPoint));
                        start = breakPoint;
                    }
                    end = start + 1;
                }
            }


            if (start < len) {
                lines.add(para.substring(start));
            }
        }

        return lines;
    }

    private static int findBreakPoint(String text, int start, int end) {

        for (int i = end - 1; i > start; i--) {
            char c = text.charAt(i);
            if (Character.isWhitespace(c) || c == '-' || c == '.' || c == ',') {
                return i + 1;
            }
        }
        return start;
    }

    private static float getStartX(String text, int maxWidth, Paint paint, int align) {
        float textWidth = paint.measureText(text);
        switch (align) {
            case 1:
                return (maxWidth - textWidth) / 2f;
            case 2:
                return maxWidth - textWidth - paint.getFontMetrics().descent;
            default: // (0)
                return 0;
        }
    }

    private static Bitmap scaleBitmap(Bitmap source, int scaleX, int scaleY) {
        if (scaleX <= 1 && scaleY <= 1) return source;

        int newWidth = source.getWidth() * scaleX;
        int newHeight = source.getHeight() * scaleY;

        Bitmap scaledBitmap = Bitmap.createScaledBitmap(source, newWidth, newHeight, true);
        source.recycle();
        return scaledBitmap;
    }

    private static Bitmap rotateBitmap(Bitmap source, int orientation) {
        if (orientation != 2) return source;

        Matrix matrix = new Matrix();
        matrix.postRotate(180);

        Bitmap rotatedBitmap = Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true
        );
        source.recycle();
        return rotatedBitmap;
    }


    public static class TextConfig {
        public float textSize = 48f; // ( )
        public int textColor = Color.BLACK;
        public int backgroundColor = Color.TRANSPARENT;
        public Typeface typeface = Typeface.DEFAULT;
        public float lineSpacingMultiplier = 1.2f;
        public int padding = 20; // ( )
        public int align = 0; // (0: ,1: ,2: )
        public int underline = 0; // (0: ,1: )
        public int bold = 0; // (0: ,1: )
        public int orientation = 0; // (0: ,2: 180 )
        public int scaleX = 1; // (1 2)
        public int scaleY = 1; // (1 2)


        public int underlineColor = Color.BLACK;
        public float underlineWidth = 2f; // ( )
        public float underlinePadding = 3f;
    }
}
