package com.wushaohong.zxing;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.View;

import com.google.zxing.BarcodeFormat;
import com.wushaohong.zxing.camera.open.CameraFacing;
import com.wushaohong.zxing.common.Scanner;
import com.wushaohong.zxing.decode.DecodeFormatManager;

import java.util.Collection;

/**
 * Created by hupei on 2017/11/21.
 */

public final class ScannerOptions {

    public static final int DEFAULT_LASER_LINE_HEIGHT = 2;
    private LaserStyle laserStyle = LaserStyle.COLOR_LINE;
    private int laserLineColor = Scanner.color.VIEWFINDER_LASER; // rgb
    private int laserLineResId;
    private int laserLineHeight = DEFAULT_LASER_LINE_HEIGHT;
    private int laserLineMoveSpeed = 6; // 6px, px
    private boolean laserMoveFullScreen;
    private int frameStrokeColor = Color.WHITE; // rgb
    private float frameStrokeWidth = 1f;
    private int frameWidth;
    private int frameHeight;
    private int frameCornerColor = laserLineColor; // 4 rgb
    private int frameCornerLength = 15; // 4 , dp 15
    private int frameCornerWidth = 2; // 4 , dp 2
    private boolean frameCornerInside;
    private boolean frameCornerHide;
    private int frameTopMargin;
    private boolean frameHide;
    private boolean viewfinderHide;
    private String tipText = "将二维码放入框内，即可自动扫描";
    private int tipTextColor = Color.WHITE; // rgb
    private int tipTextSize = 15; // sp 15
    private boolean tipTextToFrameTop;
    private int tipTextToFrameMargin = 20; // dp 20
    private int mediaResId;
    private Collection<BarcodeFormat> decodeFormats;
    private boolean createQrThumbnail;
    private boolean showQrThumbnail;
    private CameraFacing cameraFacing = CameraFacing.BACK;
    private boolean scanFullScreen;
    private boolean scanInvert; // ( )
    private double cameraZoomRatio;
    private ViewfinderCallback viewfinderCallback;
    private int frameOutsideColor = Scanner.color.VIEWFINDER_MASK;
    private String characterSet;


    private boolean isResultZoom = false;

    private boolean isResultZoomReset = false;

    private long resultZoomResetDelay = 1000;

    protected ScannerOptions() {
    }

    public LaserStyle getLaserStyle() {
        return laserStyle;
    }

    public int getLaserLineColor() {
        return laserLineColor;
    }

    public int getLaserLineResId() {
        return laserLineResId;
    }

    public int getLaserLineHeight() {
        return laserLineHeight;
    }

    public int getLaserLineMoveSpeed() {
        return laserLineMoveSpeed;
    }

    public boolean isLaserMoveFullScreen() {
        return laserMoveFullScreen;
    }

    public int getFrameStrokeColor() {
        return frameStrokeColor;
    }

    public float getFrameStrokeWidth() {
        return frameStrokeWidth;
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    public int getFrameCornerColor() {
        return frameCornerColor;
    }

    public int getFrameCornerLength() {
        return frameCornerLength;
    }

    public int getFrameCornerWidth() {
        return frameCornerWidth;
    }

    public boolean isFrameCornerInside() {
        return frameCornerInside;
    }

    public boolean isFrameCornerHide() {
        return frameCornerHide;
    }

    public int getFrameTopMargin() {
        return frameTopMargin;
    }

    public boolean isFrameHide() {
        return frameHide;
    }

    public boolean isViewfinderHide() {
        return viewfinderHide;
    }

    public String getTipText() {
        return tipText;
    }

    public int getTipTextColor() {
        return tipTextColor;
    }

    public int getTipTextSize() {
        return tipTextSize;
    }

    public boolean isTipTextToFrameTop() {
        return tipTextToFrameTop;
    }

    public int getTipTextToFrameMargin() {
        return tipTextToFrameMargin;
    }

    public int getMediaResId() {
        return mediaResId;
    }

    public Collection<BarcodeFormat> getDecodeFormats() {
        return decodeFormats;
    }

    public boolean isCreateQrThumbnail() {
        return createQrThumbnail;
    }

    public boolean isShowQrThumbnail() {
        return showQrThumbnail;
    }

    public CameraFacing getCameraFacing() {
        return cameraFacing;
    }

    public boolean isScanFullScreen() {
        return scanFullScreen;
    }

    public boolean isScanInvert() {
        return scanInvert;
    }

    public double getCameraZoomRatio() {
        return cameraZoomRatio;
    }

    public ViewfinderCallback getViewfinderCallback() {
        return viewfinderCallback;
    }

    public int getFrameOutsideColor() {
        return frameOutsideColor;
    }

    public String getCharacterSet() {
        return characterSet;
    }

    public boolean isResultZoom() {
        return isResultZoom;
    }

    public boolean isResultZoomReset() {
        return isResultZoomReset;
    }

    public long getResultZoomResetDelay() {
        return resultZoomResetDelay;
    }

    public enum LaserStyle {
        /**
         * 颜色线值样式
         */
        COLOR_LINE
        /**
         * 资源文件线样式
         */
        , RES_LINE
        /**
         * 资源文件网格样式
         */
        , RES_GRID
    }

    public interface ViewfinderCallback {
        void onDraw(View view, Canvas canvas, Rect frame);
    }

    public static final class Builder {
        private ScannerOptions options;

        public Builder() {
            options = new ScannerOptions();
        }

        public ScannerOptions build() {
            return options;
        }

        /**
         * 扫描线样式
         *
         * @param style 默认为{@link LaserStyle#COLOR_LINE 颜色线}
         * @param value style=COLOR_LINE，value为颜色值rgb，其余样式value为resId
         * @return
         */
        public Builder setLaserStyle(LaserStyle style, int value) {
            options.laserStyle = style;
            if (style == LaserStyle.COLOR_LINE) {
                options.laserLineColor = value;
            } else {
                options.laserLineResId = value;
            }
            return this;
        }

        /**
         * 设置扫描线颜色值<br>
         * 只支持扫描线样式为{@link LaserStyle#COLOR_LINE 颜色线}
         *
         * @param color rgb 颜色值
         */
        public Builder setLaserLineColor(int color) {
            options.laserStyle = LaserStyle.COLOR_LINE;
            options.laserLineColor = color;
            return this;
        }

        /**
         * 设置扫描线高度<br>
         * 支持扫描线样式为{@link LaserStyle#COLOR_LINE 颜色线}
         * or {@link LaserStyle#RES_LINE 资源文件}
         *
         * @param height dp
         */
        public Builder setLaserLineHeight(int height) {
            options.laserLineHeight = height;
            return this;
        }

        /**
         * 设置扫描框线移动间距
         *
         * @param moveSpeed 每毫秒移动 moveSpeed 像素 px
         * @return
         */
        public Builder setLaserMoveSpeed(int moveSpeed) {
            options.laserLineMoveSpeed = moveSpeed;
            return this;
        }

        /**
         * 扫描线是否全屏移动
         *
         * @param fullScreen true全屏，false扫描框内
         * @return
         */
        public Builder setLaserMoveFullScreen(boolean fullScreen) {
            options.laserMoveFullScreen = fullScreen;
            return this;
        }

        /**
         * 扫描边框颜色值，默认白色
         *
         * @param color rgb颜色值
         * @return
         */
        public Builder setFrameStrokeColor(int color) {
            options.frameStrokeColor = color;
            return this;
        }

        /**
         * 扫描边框宽度，默认1px
         *
         * @param width 单位px
         * @return
         */
        public Builder setFrameStrokeWidth(float width) {
            options.frameStrokeWidth = width;
            return this;
        }

        /**
         * 设置扫描框大小
         *
         * @param width  dp
         * @param height dp
         */
        public Builder setFrameSize(int width, int height) {
            options.frameWidth = width;
            options.frameHeight = height;
            return this;
        }

        /**
         * 设置扫描框4角颜色值
         *
         * @param color rgb
         */
        public Builder setFrameCornerColor(int color) {
            options.frameCornerColor = color;
            return this;
        }

        /**
         * 设置扫描框4角长度
         *
         * @param length dp
         */
        public Builder setFrameCornerLength(int length) {
            options.frameCornerLength = length;
            return this;
        }

        /**
         * 设置扫描框4角宽度
         *
         * @param width dp
         */
        public Builder setFrameCornerWidth(int width) {
            options.frameCornerWidth = width;
            return this;
        }

        /**
         * 设置扫描框4角是否在框内，默认框外
         *
         * @param inside true内部
         * @return
         */
        public Builder setFrameCornerInside(boolean inside) {
            options.frameCornerInside = inside;
            return this;
        }

        /**
         * 是否隐藏扫描框4角
         *
         * @param hide true隐藏
         * @return
         */
        public Builder setFrameCornerHide(boolean hide) {
            options.frameCornerHide = hide;
            if (!hide)
                options.laserMoveFullScreen = false;
            return this;
        }

        /**
         * 设置扫描框与屏幕顶部距离
         *
         * @param margin dp
         */
        public Builder setFrameTopMargin(int margin) {
            options.frameTopMargin = margin;
            return this;
        }

        /**
         * 是否隐藏扫描框，默认不隐藏
         *
         * @param hide true隐藏
         * @return
         */
        public Builder setFrameHide(boolean hide) {
            options.frameHide = hide;
            if (!hide)
                options.laserMoveFullScreen = false;
            return this;
        }

        /**
         * 设置隐藏取景视图包括文字，默认不隐藏
         *
         * @param hide
         * @return
         */
        public Builder setViewfinderHide(boolean hide) {
            options.viewfinderHide = hide;
            return this;
        }

        /**
         * 设置文字
         *
         * @param text 文字
         */
        public Builder setTipText(String text) {
            options.tipText = text;
            return this;
        }

        /**
         * 设置文字颜色
         *
         * @param color 文字颜色
         */
        public Builder setTipTextColor(int color) {
            options.tipTextColor = color;
            return this;
        }

        /**
         * 设置文字大小
         *
         * @param size 文字大小 sp
         */
        public Builder setTipTextSize(int size) {
            options.tipTextSize = size;
            return this;
        }

        /**
         * 设置文字与扫描框间距
         *
         * @param margin 间距 单位dp
         */
        public Builder setTipTextToFrameMargin(int margin) {
            options.tipTextToFrameMargin = margin;
            return this;
        }

        /**
         * 设置文字是否在扫描框上方，默认下方
         *
         * @param top true=上方，false=下方
         */
        public Builder setTipTextToFrameTop(boolean top) {
            options.tipTextToFrameTop = top;
            return this;
        }

        /**
         * 设置扫描成功的音频
         *
         * @param resId
         */
        public Builder setMediaResId(int resId) {
            options.mediaResId = resId;
            return this;
        }

        /**
         * 设置扫描解码类型（二维码、一维码、商品条码）
         *
         * @param scanMode {@linkplain Scanner.ScanMode mode}
         * @return
         */
        public Builder setScanMode(String scanMode) {
            options.decodeFormats = DecodeFormatManager.parseDecodeFormats(scanMode);
            return this;
        }

        /**
         * 设置扫描解码类型
         *
         * @param barcodeFormat
         * @return
         */
        public Builder setScanMode(BarcodeFormat... barcodeFormat) {
            options.decodeFormats = DecodeFormatManager.parseDecodeFormats(barcodeFormat);
            return this;
        }

        /**
         * 是否创建扫描结果缩略图，也就是扫描成功后的第三个参数，默认不创建
         *
         * @param thumbnail
         * @return
         */
        public Builder setCreateQrThumbnail(boolean thumbnail) {
            options.createQrThumbnail = thumbnail;
            return this;
        }

        /**
         * 是否显示扫描结果缩略图在扫描界面，默认不显示<br>
         * {@link #setCreateQrThumbnail(boolean) setCreateQrThumbnail(true)才有效}
         *
         * @param show
         * @return
         */
        public Builder setShowQrThumbnail(boolean show) {
            options.showQrThumbnail = show;
            return this;
        }

        /**
         * 设置扫描摄像头，默认后置
         *
         * @param cameraFacing
         * @return
         */
        public Builder setCameraFacing(CameraFacing cameraFacing) {
            options.cameraFacing = cameraFacing;
            return this;
        }

        /**
         * 是否全屏扫描，默认非全屏扫描<br>
         * true=全屏扫描，则隐藏扫描框，扫描框4角，扫描线全屏上下移动<br>
         * 如全屏扫描情况下，仍需显示扫描框、扫描框4角及扫描线在扫描框内上下的移动，则调用该方法之后再调用：
         * {@link #setFrameHide(boolean) setFrameHide(false)}，
         * {@link #setFrameCornerHide(boolean) setFrameCornerHide(false)}，
         * {@link #setLaserMoveFullScreen(boolean) setLaserMoveFullScreen(false)}
         *
         * @param scanFullScreen
         * @return
         */
        public Builder setScanFullScreen(boolean scanFullScreen) {
            options.scanFullScreen = scanFullScreen;
            if (scanFullScreen) {
                options.frameHide = true;
                options.frameCornerHide = true;
                options.laserMoveFullScreen = true;
            }
            return this;
        }

        /**
         * 是否扫描反色二维码（黑底白码）
         *
         * @param invertScan
         * @return
         */
        public Builder setScanInvert(boolean invertScan) {
            options.scanInvert = invertScan;
            return this;
        }

        /**
         * 设置相机变焦比率
         *
         * @param cameraZoomRatio >0 建议2倍
         * @return
         */
        public Builder setCameraZoomRatio(double cameraZoomRatio) {
            options.cameraZoomRatio = cameraZoomRatio;
            return this;
        }

        public Builder setViewfinderCallback(ViewfinderCallback callback) {
            options.viewfinderCallback = callback;
            return this;
        }

        /**
         * 设置扫描框以外区域颜色值
         *
         * @param color rgb
         * @return
         */
        public Builder setFrameOutsideColor(int color) {
            options.frameOutsideColor = color;
            return this;
        }

        /**
         * 指定编码解析二维码
         *
         * @param characterSet
         * @return
         */
        public Builder setCharacterSet(String characterSet) {
            options.characterSet = characterSet;
            return this;
        }

        /**
         * 设置扫描是否放大
         * @param isResultZoom
         * @return
         */
        public Builder setResultZoom(boolean isResultZoom) {
            options.isResultZoom = isResultZoom;
            return this;
        }

        public Builder setResultZoomReset(boolean isResultZoomReset) {
            options.isResultZoomReset = isResultZoomReset;
            return this;
        }

        public Builder setResultZoomResetDelay(long delayMS) {
            options.resultZoomResetDelay = delayMS;
            return this;
        }
    }
}
