/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wushaohong.zxing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

import com.wushaohong.zxing.camera.CameraManager;
import com.wushaohong.zxing.common.Scanner;

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class ViewfinderView extends View {

    private static final int CURRENT_POINT_OPACITY = 0xA0;
    private static final int POINT_SIZE = 6;


    private CameraManager cameraManager;
    private final Paint paint;
    private Bitmap resultBitmap;

    private int animationDelay = 0;
    private Bitmap laserLineBitmap;

    private int resultColor = Scanner.color.RESULT_VIEW;
    private int laserLineTop;

    private int laserLineHeight;
    private int frameCornerWidth;
    private int frameCornerLength;
    private int tipTextSize;
    private int tipTextMargin;
    private ScannerOptions scannerOptions;

    public ViewfinderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    void setCameraManager(CameraManager cameraManager) {
        this.cameraManager = cameraManager;
    }

    void setScannerOptions(ScannerOptions scannerOptions) {
        this.scannerOptions = scannerOptions;
        laserLineHeight = dp2px(scannerOptions.getLaserLineHeight());
        frameCornerWidth = dp2px(scannerOptions.getFrameCornerWidth());
        frameCornerLength = dp2px(scannerOptions.getFrameCornerLength());

        tipTextSize = Scanner.sp2px(getContext(), scannerOptions.getTipTextSize());
        tipTextMargin = dp2px(scannerOptions.getTipTextToFrameMargin());
    }

    private int dp2px(int dp) {
        return Scanner.dp2px(getContext(), dp);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (cameraManager == null) {
            return;
        }
        Rect frame = cameraManager.getFramingRect();

        Rect previewFrame = cameraManager.getFramingRectInPreview();
        if (frame == null || previewFrame == null) {
            return;
        }

        if (!scannerOptions.isScanFullScreen()) {
            drawMask(canvas, frame);
        }
 // Bitmap, result Bitmap
        if (resultBitmap != null) {
            paint.setAlpha(CURRENT_POINT_OPACITY);
            canvas.drawBitmap(resultBitmap, null, frame, paint);
        } else {
            if (!scannerOptions.isFrameHide())
                drawFrame(canvas, frame);
            if (!scannerOptions.isFrameCornerHide())
                drawFrameCorner(canvas, frame);
            drawText(canvas, frame);


            if (scannerOptions.isLaserMoveFullScreen()) {
                moveLaserSpeedFullScreen(cameraManager.getScreenResolution());
                drawLaserLineFullScreen(canvas, cameraManager.getScreenResolution());
            } else {
                drawLaserLine(canvas, frame);
                moveLaserSpeed(frame);
            }
            if (scannerOptions.getViewfinderCallback() != null) {
                scannerOptions.getViewfinderCallback().onDraw(this, canvas, frame);
            }
        }
    }

    private void moveLaserSpeed(Rect frame) {

        if (laserLineTop == 0) {
            laserLineTop = frame.top;
        }
        int laserMoveSpeed = scannerOptions.getLaserLineMoveSpeed();
 // LASER_VELOCITY
        laserLineTop += laserMoveSpeed;
        if (laserLineTop >= frame.bottom) {
            laserLineTop = frame.top;
        }
        if (animationDelay == 0) {
            animationDelay = (int) ((1.0f * 1000 * laserMoveSpeed) / (frame.bottom - frame.top));
        }


        postInvalidateDelayed(animationDelay, frame.left - POINT_SIZE, frame.top - POINT_SIZE
                , frame.right + POINT_SIZE, frame.bottom + POINT_SIZE);
    }

    private void moveLaserSpeedFullScreen(Point point) {

        int laserMoveSpeed = scannerOptions.getLaserLineMoveSpeed();
 // LASER_VELOCITY
        laserLineTop += laserMoveSpeed;
        if (laserLineTop >= point.y) {
            laserLineTop = 0;
        }
        if (animationDelay == 0) {
            animationDelay = (int) ((1.0f * 1000 * laserMoveSpeed) / point.y);
        }
        postInvalidateDelayed(animationDelay);
    }

    /**
     * 画扫描框外区域
     *
     * @param canvas
     * @param frame
     */
    private void drawMask(Canvas canvas, Rect frame) {
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        paint.setColor(resultBitmap != null ? resultColor : scannerOptions.getFrameOutsideColor());
        canvas.drawRect(0, 0, width, frame.top, paint);
        canvas.drawRect(0, frame.top, frame.left, frame.bottom + 1, paint);
        canvas.drawRect(frame.right + 1, frame.top, width, frame.bottom + 1, paint);
        canvas.drawRect(0, frame.bottom + 1, width, height, paint);
    }

    /**
     * 绘制提示文字
     *
     * @param canvas
     * @param frame
     */
    private void drawText(Canvas canvas, Rect frame) {
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(scannerOptions.getTipTextColor());
        textPaint.setTextSize(tipTextSize);

        float x = frame.left;
 // drawTextGravityBottom
        float y = !scannerOptions.isTipTextToFrameTop() ? frame.bottom + tipTextMargin
                : frame.top - tipTextMargin;

        StaticLayout staticLayout = new StaticLayout(scannerOptions.getTipText(), textPaint, frame.width()
                , Layout.Alignment.ALIGN_CENTER, 1.0f, 0, false);
        canvas.save();
        canvas.translate(x, y);
        staticLayout.draw(canvas);
        canvas.restore();
    }

    /**
     * 绘制扫描框4角
     *
     * @param canvas
     * @param frame
     */
    private void drawFrameCorner(Canvas canvas, Rect frame) {
        paint.setColor(scannerOptions.getFrameCornerColor());
        paint.setStyle(Paint.Style.FILL);
        if (scannerOptions.isFrameCornerInside()) {

            canvas.drawRect(frame.left, frame.top, frame.left + frameCornerWidth, frame.top + frameCornerLength, paint);

            canvas.drawRect(frame.left, frame.top, frame.left + frameCornerLength, frame.top + frameCornerWidth, paint);

            canvas.drawRect(frame.right - frameCornerWidth, frame.top, frame.right, frame.top + frameCornerLength, paint);

            canvas.drawRect(frame.right - frameCornerLength, frame.top, frame.right, frame.top + frameCornerWidth, paint);

            canvas.drawRect(frame.left, frame.bottom - frameCornerLength, frame.left + frameCornerWidth, frame.bottom, paint);

            canvas.drawRect(frame.left, frame.bottom - frameCornerWidth, frame.left + frameCornerLength, frame.bottom, paint);

            canvas.drawRect(frame.right - frameCornerWidth, frame.bottom - frameCornerLength, frame.right, frame.bottom, paint);

            canvas.drawRect(frame.right - frameCornerLength, frame.bottom - frameCornerWidth, frame.right, frame.bottom, paint);
        } else {

            canvas.drawRect(frame.left - frameCornerWidth, frame.top, frame.left, frame.top + frameCornerLength, paint);
            canvas.drawRect(frame.left - frameCornerWidth, frame.top - frameCornerWidth, frame.left + frameCornerLength, frame.top, paint);

            canvas.drawRect(frame.right, frame.top, frame.right + frameCornerWidth, frame.top + frameCornerLength, paint);
            canvas.drawRect(frame.right - frameCornerLength, frame.top - frameCornerWidth, frame.right + frameCornerWidth, frame.top, paint);

            canvas.drawRect(frame.left - frameCornerWidth, frame.bottom - frameCornerLength, frame.left, frame.bottom, paint);
            canvas.drawRect(frame.left - frameCornerWidth, frame.bottom, frame.left + frameCornerLength, frame.bottom + frameCornerWidth, paint);

            canvas.drawRect(frame.right, frame.bottom - frameCornerLength, frame.right + frameCornerWidth, frame.bottom, paint);
            canvas.drawRect(frame.right - frameCornerLength, frame.bottom, frame.right + frameCornerWidth, frame.bottom + frameCornerWidth, paint);
        }
    }

    /**
     * 画扫描框
     *
     * @param canvas
     * @param frame
     */
    private void drawFrame(Canvas canvas, Rect frame) {
        paint.setColor(scannerOptions.getFrameStrokeColor());
        paint.setStrokeWidth(scannerOptions.getFrameStrokeWidth());
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(frame, paint);
    }

    /**
     * 画扫描线
     *
     * @param canvas
     * @param frame
     */
    private void drawLaserLine(Canvas canvas, Rect frame) {
        if (scannerOptions.getLaserStyle() == ScannerOptions.LaserStyle.COLOR_LINE) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(scannerOptions.getLaserLineColor());
            canvas.drawRect(frame.left, laserLineTop, frame.right
                    , laserLineTop + laserLineHeight, paint);
        } else {
 // Bitmap
            if (laserLineBitmap == null)
                laserLineBitmap = BitmapFactory.decodeResource(getResources(), scannerOptions.getLaserLineResId());
            int height = laserLineBitmap.getHeight();

            if (scannerOptions.getLaserStyle() == ScannerOptions.LaserStyle.RES_GRID) {
                RectF dstRectF = new RectF(frame.left, frame.top, frame.right, laserLineTop);
                Rect srcRect = new Rect(0, (int) (height - dstRectF.height())
                        , laserLineBitmap.getWidth(), height);
                canvas.drawBitmap(laserLineBitmap, srcRect, dstRectF, paint);
            }

            else {

                if (laserLineHeight == dp2px(ScannerOptions.DEFAULT_LASER_LINE_HEIGHT)) {
                    laserLineHeight = laserLineBitmap.getHeight() / 2;
                }
                Rect laserRect = new Rect(frame.left, laserLineTop, frame.right
                        , laserLineTop + laserLineHeight);
                canvas.drawBitmap(laserLineBitmap, null, laserRect, paint);
            }
        }
    }


    /**
     * 画全屏宽扫描线
     *
     * @param canvas
     * @param point
     */
    private void drawLaserLineFullScreen(Canvas canvas, Point point) {
        if (scannerOptions.getLaserStyle() == ScannerOptions.LaserStyle.COLOR_LINE) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(scannerOptions.getLaserLineColor());
            canvas.drawRect(0, laserLineTop, point.x, laserLineTop + laserLineHeight, paint);
        } else {
            if (laserLineBitmap == null) // Bitmap
                laserLineBitmap = BitmapFactory.decodeResource(getResources(), scannerOptions.getLaserLineResId());
            int height = laserLineBitmap.getHeight();

            if (scannerOptions.getLaserStyle() == ScannerOptions.LaserStyle.RES_GRID) {
                int dstRectFTop = 0;
                if (laserLineTop >= height) {
                    dstRectFTop = laserLineTop - height;
                }
                RectF dstRectF = new RectF(0, dstRectFTop, point.x, laserLineTop);
                Rect srcRect = new Rect(0, (int) (height - dstRectF.height()), laserLineBitmap.getWidth(), height);
                canvas.drawBitmap(laserLineBitmap, srcRect, dstRectF, paint);
            }

            else {

                if (laserLineHeight == dp2px(ScannerOptions.DEFAULT_LASER_LINE_HEIGHT)) {
                    laserLineHeight = laserLineBitmap.getHeight() / 2;
                }
                Rect laserRect = new Rect(0, laserLineTop, point.x, laserLineTop + laserLineHeight);
                canvas.drawBitmap(laserLineBitmap, null, laserRect, paint);
            }
        }
    }

    void drawViewfinder() {
        Bitmap resultBitmap = this.resultBitmap;
        this.resultBitmap = null;
        if (resultBitmap != null) {
            resultBitmap.recycle();
        }
        invalidate();
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param barcode An image of the decoded barcode.
     */
    void drawResultBitmap(Bitmap barcode) {
        resultBitmap = barcode;
        invalidate();
    }

    void laserLineBitmapRecycle() {
        if (laserLineBitmap != null) {
            laserLineBitmap.recycle();
            laserLineBitmap = null;
        }
    }
}
