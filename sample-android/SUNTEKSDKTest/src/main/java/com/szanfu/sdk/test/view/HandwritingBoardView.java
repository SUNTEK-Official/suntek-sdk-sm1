package com.szanfu.sdk.test.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * 手写板
 */
public class HandwritingBoardView extends View {


    private Paint mPaint;
    private Canvas mCanvas;
    private Bitmap mBitmap;
    private Path mPath;
    private float mX, mY;
    private static final float TOUCH_TOLERANCE = 4;
    private DrawPath drawPath; // Path
    private static List<DrawPath> savePath; // Path , List
    private int screenWidth, screenHeight;
    private Paint mBitmapPaint;
    private int mPenSize = 10;
    private int mPenColor = Color.BLACK;
    private int mPanelColor = Color.TRANSPARENT; // ( , )


    public HandwritingBoardView(Context context) {
        super(context);
        init();
    }

    public HandwritingBoardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HandwritingBoardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mPenSize);
        mPaint.setColor(mPenColor);
        savePath = new ArrayList<DrawPath>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth =   getWidth();  // 384;
        screenHeight = getHeight();
        /* hardware note */
        mBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mCanvas.drawColor(mPanelColor);
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawColor(mPanelColor);
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        if (mPath != null) {
            canvas.drawPath(mPath, mPaint);
        }
    }

    /**
     * 描绘路径的起点
     *
     * @param x
     * @param y
     */
    private void touchStart(float x, float y) {
        mPath.moveTo(x, y);
        mX = x;
        mY = y;
    }

    /**
     * 描绘移动产生的路径
     *
     * @param x
     * @param y
     */
    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(mY - y);
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) { // TOUCH_TOLERANCE
            mPath.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2); // x1,y1 x2,y2 , ( mPath.lineTo )
            mX = x;
            mY = y;
        }
    }

    /**
     * 路径结束时
     */
    private void touchUp() {
        mPath.lineTo(mX, mY);
        mCanvas.drawPath(mPath, mPaint); // mPaint , Bitmap
        savePath.add(drawPath);
        mPath = null;
    }

    /**
     * 撤销的核心思想就是将画布清空，
     * 将保存下来的Path路径最后一个移除掉，
     * 重新将路径画在画布上面。
     */
    public void undo() {
        mBitmap = Bitmap.createBitmap(screenWidth, screenHeight,
                Bitmap.Config.ARGB_8888);
        /*
         * 清空画布，但是如果图片有背景的话，则使用上面的重新初始化的方法，用该方法会将背景清空掉
         */
        mCanvas.setBitmap(mBitmap);
        if (savePath != null && savePath.size() > 0) {
            savePath.remove(savePath.size() - 1); // path
            for (HandwritingBoardView.DrawPath DrawPath : savePath) {
                mCanvas.drawPath(DrawPath.path, DrawPath.paint);
            }
            invalidate();
        }
    }

    /**
     * 重写，相当重新来过
     */
    public void redo() {
        savePath.clear();
        mCanvas.drawColor(mPanelColor, PorterDuff.Mode.CLEAR);
        invalidate();
    }

    public Bitmap getBitmap() {
        if(savePath == null || savePath.size() == 0){
            return null;
        }
        return mBitmap;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
 // (x,y)
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
 // Path
                mPath = new Path();

                drawPath = new DrawPath();
                drawPath.path = mPath;
                drawPath.paint = mPaint;
                touchStart(x, y);

                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                touchMove(x, y);

                invalidate();
                break;

            case MotionEvent.ACTION_UP:
                touchUp();

                invalidate();
                break;
        }
        return true;
    }

    /**
     * 设置笔头大小
     *
     * @param size
     */
    public void setPenSize(int size) {
        mPenSize = size > 0 ? size : 10;
        this.mPenSize = size;
        if(mPaint != null){
            mPaint.setStrokeWidth(mPenSize);
        }
    }

    /**
     * 设置面板的背景色
     *
     * @param bgColor
     */
    public void setPanelColor(@ColorInt int bgColor) {
        this.mPanelColor = bgColor;
    }


    /**
     * 设置画笔颜色
     *
     * @param mPenColor
     */
    public void setPenColor(int mPenColor) {
        this.mPenColor = mPenColor;
        if(mPaint != null) {
            mPaint.setColor(mPenColor);
        }
    }
    private static class DrawPath {
        public Path path;
        public Paint paint;
    }
}
