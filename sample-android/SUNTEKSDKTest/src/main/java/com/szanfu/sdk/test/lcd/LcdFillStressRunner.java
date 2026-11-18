package com.szanfu.sdk.test.lcd;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.szanfu.sdk.test.R;

/**
 * LCD 全屏填充压力测试
 * <p>
 * 开放 2 种启动方式：
 * 1) startStressByCount(count)  -> 按次数
 * 2) startStressByTime(durationMs) -> 按时间(ms)
 * <p>
 */
public class LcdFillStressRunner {

    public interface LcdUtils {
        int lcdClearScreenWithResult();

        int lcdFillScreen(int x1, int y1, int x2, int y2, int color);
    }

    public static class LcdInfo {
        private final int type;
        private final int width;
        private final int height;

        public LcdInfo(int type, int width, int height) {
            this.type = type;
            this.width = width;
            this.height = height;
        }

        public int getType() {
            return type;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    public interface Logger {
        void log(String msg);
    }

    public static class Result {
        public long totalLoops;
        public long okCount;
        public long failCount;
        public long totalCostNs;

        public long avgCostMs() {
            if (totalLoops <= 0) return 0;
            return (totalCostNs / totalLoops) / 1_000_000L;
        }
    }

    private enum Mode {BY_COUNT, BY_TIME}

    private static class Config {
        int intervalMs;
        boolean clearBetween;
        boolean randomColor;
        int logEvery;

        int[] paletteBw;
        int[] paletteColor;
    }

    private static final int[] DEFAULT_COLORS_BW = new int[]{0x0000, 0xFFFF};
    private static final int[] DEFAULT_COLORS_COLOR = new int[]{
            0x0000, 0xFFFF, 0xF800, 0x07E0, 0x001F, 0xFFE0, 0xF81F, 0x07FF
    };

    private final Context appContext;
    private final LcdUtils lcd;
    private final LcdInfo info;
    private final Logger logger;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private final Object lock = new Object();

    private volatile boolean running = false;
    private Thread worker;


    /**
     * @param context getApplicationContext()
     */
    public LcdFillStressRunner(Context context, LcdUtils lcd, LcdInfo info, Logger logger) {
        this.appContext = (context == null) ? null : context.getApplicationContext();
        this.lcd = lcd;
        this.info = info;
        this.logger = logger;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        synchronized (lock) {
            running = false;
            if (worker != null) {
                try {
                    worker.interrupt();
                } catch (Throwable ignore) {
                }
                worker = null;
            }
        }
    }


    public Result startStressByCount(long count) {
        if (count <= 0) {
            postLog(str(R.string.lcd_invalid_count, count));
            return new Result();
        }
        return startInternal(Mode.BY_COUNT, count, buildDefaultStressConfig());
    }


    public Result startStressByTime(long durationMs) {
        if (durationMs <= 0) {
            postLog(str(R.string.lcd_invalid_time_ms, durationMs));
            return new Result();
        }
        return startInternal(Mode.BY_TIME, durationMs, buildDefaultStressConfig());
    }

    private Config buildDefaultStressConfig() {
        Config cfg = new Config();
        cfg.intervalMs = 3000;
        cfg.clearBetween = false;
        cfg.randomColor = false;
        cfg.logEvery = 5;
        cfg.paletteBw = DEFAULT_COLORS_BW;
        cfg.paletteColor = DEFAULT_COLORS_COLOR;
        return cfg;
    }

    private Result startInternal(final Mode mode, final long target, final Config cfg) {
        synchronized (lock) {
            stop();

            final Result result = new Result();

            if (lcd == null || info == null) {
                postLog(str(R.string.lcd_abort_null));
                return result;
            }

            final int width = info.getWidth();
            final int height = info.getHeight();
            if (width <= 0 || height <= 0) {
                postLog(str(R.string.lcd_abort_size, width, height));
                return result;
            }

            final int x1 = 0, y1 = 0, x2 = width - 1, y2 = height - 1;
            final int[] palette = (info.getType() == 1) ? cfg.paletteBw : cfg.paletteColor;

            running = true;

            final String tag = str(R.string.lcd_tag);
            final String startWord = str(R.string.lcd_start);
            final String modeLabel = (mode == Mode.BY_COUNT)
                    ? str(R.string.lcd_mode_count)
                    : str(R.string.lcd_mode_time_ms);

 // 1 START
            postLog(str(R.string.lcd_log_start_line, tag, startWord, width, height, modeLabel, target));

            worker = new Thread(() -> {
                final long startNs = System.nanoTime();
                final long deadlineNs = (mode == Mode.BY_TIME)
                        ? (startNs + target * 1_000_000L)
                        : Long.MAX_VALUE;

                int colorIndex = 0;
                int lastRet = 0;

                try {
                    safeClear();

                    while (running) {
                        if (mode == Mode.BY_COUNT) {
                            if (result.totalLoops >= target) break;
                        } else {
                            if (System.nanoTime() >= deadlineNs) break;
                        }

                        int color = pickColor(cfg, palette, colorIndex++);
                        if (cfg.clearBetween) safeClear();

                        long t1 = System.nanoTime();
                        int ret = lcd.lcdFillScreen(x1, y1, x2, y2, color);
                        long t2 = System.nanoTime();

                        lastRet = ret;

                        long costNs = t2 - t1;
                        result.totalCostNs += costNs;
                        result.totalLoops++;

                        if (ret == 0) result.okCount++;
                        else result.failCount++;

                        if (cfg.logEvery <= 1 || (result.totalLoops % cfg.logEvery == 0)) {
                            postLog(str(R.string.lcd_log_loop_line,
                                    tag, result.totalLoops, result.avgCostMs(), result.failCount, lastRet));
                        }

                        if (cfg.intervalMs > 0) {
                            try {
                                Thread.sleep(cfg.intervalMs);
                            } catch (InterruptedException ie) {
                                break;
                            }
                        }
                    }
                } catch (Throwable t) {
                    postLog(str(R.string.lcd_log_ex, String.valueOf(t.getMessage())));
                } finally {
                    running = false;
 // 1 END
                    postLog(str(R.string.lcd_log_end_line,
                            tag, result.totalLoops, result.okCount, result.failCount, result.avgCostMs(), lastRet));
                }
            }, "LcdFillStressRunner");

            worker.start();
            return result;
        }
    }

    private void safeClear() {
        try {
            lcd.lcdClearScreenWithResult();
        } catch (Throwable ignore) {
        }
    }

    private int pickColor(Config cfg, int[] palette, int colorIndex) {
        if (palette == null || palette.length == 0) return 0x0000;
        if (cfg.randomColor) {
            int idx = (int) (System.nanoTime() % palette.length);
            if (idx < 0) idx = -idx;
            return palette[idx];
        }
        return palette[colorIndex % palette.length];
    }

    private void postLog(final String msg) {
        if (logger == null) return;
        uiHandler.post(() -> logger.log(msg + "\n"));
    }

    // === i18n string helper ===
    private String str(int resId, Object... args) {
        try {
            if (appContext != null) return appContext.getString(resId, args);
        } catch (Throwable ignore) {
        }
 // fallback( Context )
        StringBuilder sb = new StringBuilder();
        sb.append("[LCD] ");
        sb.append(resId);
        if (args != null && args.length > 0) {
            sb.append(" ");
            for (Object a : args) sb.append(String.valueOf(a)).append(",");
        }
        return sb.toString();
    }
}
