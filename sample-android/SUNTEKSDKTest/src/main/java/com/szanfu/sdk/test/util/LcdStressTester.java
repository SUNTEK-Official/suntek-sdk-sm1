package com.szanfu.sdk.test.util;

import android.os.Handler;
import android.os.Looper;


/**
 * LCD 压力测试 - 仅显示字符串（clear -> display -> refresh）
 */
public class LcdStressTester {

    /** LCD 业务接口*/
    public interface LcdApi {
        int lcdInit() throws Exception;
        int lcdGetDevLcdStatus(int[] width, int[] height, int[] type) throws Exception;

        int lcdClearScreen(int bgColor) throws Exception;
        int lcdDisplayString(int x, int y, String text, int fontSize, int fgColor, int bgColor) throws Exception;
        int lcdRefreshScreen() throws Exception;
    }

    /** 回调给 UI / 日志面板 */
    public interface Callback {
        void onStart();
        void onStop(String reason, long total, long ok, long fail);
        void onRound(String logLine);
        void onError(String errLine, Throwable t);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final LcdApi lcdApi;
    private final Callback callback;

    private volatile boolean running = false;

    private long total = 0;
    private long ok = 0;
    private long fail = 0;

    private int lcdWidth = 128;
    private int lcdHeight = 64;

 // ====== ======
    private int intervalMs = 150; // 100~300
    private int failLimit = 20;
    private boolean refreshEveryRound = true;
    private int clearEveryN = 1; // N , 1= ; 10

    private int[] fontSizes = new int[]{12, 16, 24};
    private int[] fgColors = new int[]{0xFFFF, 0x0000};
    private int[] bgColors = new int[]{0x0000, 0xFFFF};

    private String[] texts = new String[]{
            "HELLO",
            "1234567890",
            "AMOUNT:$0000123.45",
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789",
            "A very very long string exceeding width........"
    };

    public LcdStressTester(LcdApi lcdApi, Callback callback) {
        this.lcdApi = lcdApi;
        this.callback = callback;
    }

    // ================== Public API ==================
    public boolean isRunning() {
        return running;
    }

    public void setIntervalMs(int intervalMs) {
        this.intervalMs = Math.max(10, intervalMs);
    }

    public void setFailLimit(int failLimit) {
        this.failLimit = Math.max(1, failLimit);
    }

    public void setRefreshEveryRound(boolean refreshEveryRound) {
        this.refreshEveryRound = refreshEveryRound;
    }

    public void setClearEveryN(int clearEveryN) {
        this.clearEveryN = Math.max(1, clearEveryN);
    }

    public void setFontSizes(int[] fontSizes) {
        if (fontSizes != null && fontSizes.length > 0) this.fontSizes = fontSizes;
    }

    public void setColors(int[] fgColors, int[] bgColors) {
        if (fgColors != null && fgColors.length > 0) this.fgColors = fgColors;
        if (bgColors != null && bgColors.length > 0) this.bgColors = bgColors;
    }

    public void setTexts(String[] texts) {
        if (texts != null && texts.length > 0) this.texts = texts;
    }

    public void start() {
        if (running) return;

        // reset counters
        total = 0;
        ok = 0;
        fail = 0;

        try {
            int retInit = lcdApi.lcdInit();
            if (retInit != 0) {
                stopInternal("lcdInit failed ret=" + retInit);
                return;
            }

            int[] w = new int[1];
            int[] h = new int[1];
            int[] t = new int[1];
            int retStatus = lcdApi.lcdGetDevLcdStatus(w, h, t);
            if (retStatus == 0 && w[0] > 0 && h[0] > 0) {
                lcdWidth = w[0];
                lcdHeight = h[0];
            }
        } catch (Throwable e) {
            if (callback != null) callback.onError("start exception: " + e.getMessage(), e);
            stopInternal("start exception");
            return;
        }

        running = true;
        if (callback != null) callback.onStart();
        handler.post(roundTask);
    }

    public void stop(String reason) {
        stopInternal(reason);
    }

    public void toggle() {
        if (running) stop("stop by user");
        else start();
    }

    // ================== Internal ==================
    private void stopInternal(String reason) {
        running = false;
        handler.removeCallbacks(roundTask);
        if (callback != null) callback.onStop(reason, total, ok, fail);
    }

    private final Runnable roundTask = new Runnable() {
        @Override
        public void run() {
            if (!running) return;

            long startTs = System.currentTimeMillis();

 // ( , Random)
            int fontSize = fontSizes[(int) (total % fontSizes.length)];
            int fg = fgColors[(int) ((total / 3) % fgColors.length)];
            int bg = bgColors[(int) ((total / 7) % bgColors.length)];
            String text = texts[(int) ((total / 2) % texts.length)];


            int charWidth = Math.max(6, fontSize / 2);
            int maxChars = Math.max(1, (lcdWidth - 2) / charWidth);
            if (text.length() > maxChars) text = text.substring(0, maxChars);

 // ( / )
            int x, y;
            int pos = (int) (total % 5);
            if (pos == 0) { x = 0; y = 0; }
            else if (pos == 1) { x = Math.max(0, lcdWidth / 2); y = 0; }
            else if (pos == 2) { x = 0; y = Math.max(0, lcdHeight / 2); }
            else if (pos == 3) { x = Math.max(0, lcdWidth / 2); y = Math.max(0, lcdHeight / 2); }
            else {
                x = Math.max(0, (lcdWidth - (text.length() * charWidth)) / 2);
                y = Math.max(0, lcdHeight / 2 - fontSize);
            }

            int retClear = -1;
            int retDisplay = -1;
            int retRefresh = -1;

            try {

                if (clearEveryN <= 1 || (total % clearEveryN == 0)) {
                    retClear = lcdApi.lcdClearScreen(bg);
                }

                retDisplay = lcdApi.lcdDisplayString(x, y, text, fontSize, fg, bg);

                if (refreshEveryRound) {
                    retRefresh = lcdApi.lcdRefreshScreen();
                }

                boolean isOk = (retDisplay == 0);

                total++;
                if (isOk) ok++;
                else fail++;

                long cost = System.currentTimeMillis() - startTs;

                String line = "LCD_STRESS#" + total
                        + " clear=" + retClear
                        + " display=" + retDisplay
                        + " refresh=" + retRefresh
                        + " costMs=" + cost
                        + " fs=" + fontSize
                        + " x=" + x + " y=" + y
                        + " fg=0x" + Integer.toHexString(fg)
                        + " bg=0x" + Integer.toHexString(bg)
                        + " text=[" + text + "]";

                LogUtils.e(line);
                if (callback != null) callback.onRound(line);

                if (fail >= failLimit) {
                    stopInternal("fail >= " + failLimit);
                    return;
                }

            } catch (Throwable e) {
                total++;
                fail++;
                if (callback != null) callback.onError("round exception: " + e.getMessage(), e);

                if (fail >= failLimit) {
                    stopInternal("exception fail >= " + failLimit);
                    return;
                }
            }

            handler.postDelayed(this, intervalMs);
        }
    };
}
