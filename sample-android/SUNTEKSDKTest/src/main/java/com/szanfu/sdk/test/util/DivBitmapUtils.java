package com.szanfu.sdk.test.util;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

public class DivBitmapUtils {

    /**
     * 创建指定样式的Bitmap
     * @param displayMetrics DisplayMetrics用于dp到px的转换
     * @return 拼接后的完整Bitmap
     */
    public static Bitmap createCustomBitmap(DisplayMetrics displayMetrics) {
 // dp px
        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 384, displayMetrics);
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, displayMetrics);
        int blockSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, displayMetrics);

 // Bitmap
        Bitmap firstLineBitmap = createSolidBitmap(widthPx, heightPx, Color.BLACK);

 // Bitmap
        Bitmap secondLineBitmap = createCheckerboardBitmap(widthPx, heightPx, blockSizePx);

 // 10 Bitmap
        return combineBitmaps(firstLineBitmap, secondLineBitmap, 10);
    }

    /**
     * 创建纯色背景的Bitmap
     * @param width 宽度(px)
     * @param height 高度(px)
     * @param color 背景颜色
     * @return 纯色Bitmap
     */
    private static Bitmap createSolidBitmap(int width, int height, int color) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        canvas.drawRect(0, 0, width, height, paint);
        return bitmap;
    }

    /**
     * 创建棋盘格样式的Bitmap
     * @param width 总宽度(px)
     * @param height 总高度(px)
     * @param blockSize 小方块尺寸(px)
     * @return 棋盘格Bitmap
     */
    private static Bitmap createCheckerboardBitmap(int width, int height, int blockSize) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();


        int blocksPerRow = width / blockSize;

        for (int row = 0; row < height / blockSize; row++) {
            for (int col = 0; col < blocksPerRow; col++) {

                if (row % 2 == 0) {

                    paint.setColor((col % 2 == 0) ? Color.WHITE : Color.BLACK);
                } else {

                    paint.setColor((col % 2 == 0) ? Color.BLACK : Color.WHITE);
                }

                int left = col * blockSize;
                int top = row * blockSize;
                int right = left + blockSize;
                int bottom = top + blockSize;

                canvas.drawRect(left, top, right, bottom, paint);
            }
        }

        return bitmap;
    }

    /**
     * 将两种Bitmap交替拼接成指定行数
     * @param firstBitmap 第一种Bitmap
     * @param secondBitmap 第二种Bitmap
     * @param totalLines 总行数
     * @return 拼接后的完整Bitmap
     */
    private static Bitmap combineBitmaps(Bitmap firstBitmap, Bitmap secondBitmap, int totalLines) {
        int width = firstBitmap.getWidth();
        int singleHeight = firstBitmap.getHeight();
        int totalHeight = singleHeight * totalLines;

        Bitmap result = Bitmap.createBitmap(width, totalHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        for (int i = 0; i < totalLines; i++) {
            Bitmap sourceBitmap = (i % 2 == 0) ? firstBitmap : secondBitmap;
            canvas.drawBitmap(sourceBitmap, 0, i * singleHeight, null);
        }

        return result;
    }

    /**
     * 创建自定义样式的Bitmap（带自定义参数）
     * @param displayMetrics DisplayMetrics用于dp到px的转换
     * @param widthDp 总宽度(dp)
     * @param heightDp 单行高度(dp)
     * @param blockSizeDp 小方块尺寸(dp)
     * @param totalLines 总行数
     * @return 拼接后的完整Bitmap
     */
    public static Bitmap createCustomBitmap(DisplayMetrics displayMetrics,
                                            float widthDp,
                                            float heightDp,
                                            float blockSizeDp,
                                            int totalLines) {
 // dp px
        int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, widthDp, displayMetrics);
        int heightPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, heightDp, displayMetrics);
        int blockSizePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, blockSizeDp, displayMetrics);

 // Bitmap
        Bitmap firstLineBitmap = createSolidBitmap(widthPx, heightPx, Color.BLACK);

 // Bitmap
        Bitmap secondLineBitmap = createCheckerboardBitmap(widthPx, heightPx, blockSizePx);

 // Bitmap
        return combineBitmaps(firstLineBitmap, secondLineBitmap, totalLines);
    }

 // Activity Fragment
//    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
 // 1: (384dp , 50dp , 50dp , 10 )
//                    headerImage = DivBitmapUtils.createCustomBitmap(displayMetrics);


//                    headerImage = DivBitmapUtils.createCustomBitmap(displayMetrics,
//                            384f, 50f, 50f, 10);
}