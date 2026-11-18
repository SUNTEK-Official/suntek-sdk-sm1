package com.szanfu.sdk.test.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Paint;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.List;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.printer.IPrinterBinderService;
import com.szanfu.sdk.api.printer.IPrinterCallback;
import com.szanfu.sdk.test.util.PrintUtil;

import java.text.DecimalFormat;
import java.util.Locale;

/**
 * 车票打印工具类
 * Ticket printing utility class
 * 负责车票和汇总单的位图渲染及打印功能
 * Responsible for bitmap rendering and printing of tickets and summary sheets
 */
public class TicketPrinter {
    private static final String TAG = "TicketPrinter";
    private static final String AFSDK_PACKAGE = "com.szanfu.sdk.service";
    private static final String AFSDK_ACTION  = "com.szanfu.sdk.service.SDK_SERVICE";
 // Paint
    // Reuse Paint objects globally to avoid repeated creation
    private Paint pNorm, pBold, pCtr, pCtrB, pRgt, pRgtB, pTitle, pLarge, pLine;

    // Global decimal formatting tool to avoid repeated creation
    private final DecimalFormat priceFormat = new DecimalFormat("0.00");
 // Handler, UI
    // Main thread Handler for unified UI operations processing
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final Context context;
    private ISdkServiceManager mSdkServiceManager;
    private IPrinterBinderService mPrinterBinderService;
    private boolean mServiceBound;

    // Print configuration constants to avoid repeated array creation
    private static final int[] PRINT_CONFIG = {
            1, 0, 1, 0, 0, 384, 0, 0, 1, 1
    };

    /**
     * 构造函数
     * Constructor
     * @param context 上下文对象 / Context object
     */
    public TicketPrinter(Context context) {
        this.context = context.getApplicationContext();
 // Paint
        // Initialize global Paint objects, create only once
        initGlobalPaints();
        bindSdkService();
    }

    /**
     * 初始化全局Paint对象，复用避免频繁GC
     * Initialize global Paint objects, reuse to avoid frequent GC
     */
    private void initGlobalPaints() {
        pLine = new Paint();
        pLine.setColor(Color.BLACK);
        pLine.setStrokeWidth(1f);

        pNorm = createBasePaint(false, Paint.Align.LEFT);
        pBold = createBasePaint(true, Paint.Align.LEFT);
        pCtr = createBasePaint(false, Paint.Align.CENTER);
        pCtrB = createBasePaint(true, Paint.Align.CENTER);
        pRgt = createBasePaint(false, Paint.Align.RIGHT);
        pRgtB = createBasePaint(true, Paint.Align.RIGHT);
        pTitle = createBasePaint(true, Paint.Align.CENTER);
        pLarge = createBasePaint(true, Paint.Align.CENTER);
    }

    /**
     * 创建基础Paint模板
     * Create basic Paint template
     * @param bold 是否粗体 / Whether to use bold text
     * @param align 文字对齐方式 / Text alignment
     * @return 配置好的Paint对象 / Configured Paint object
     */
    private Paint createBasePaint(boolean bold, Paint.Align align) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setAntiAlias(false);
        p.setFakeBoldText(bold);
        p.setTextAlign(align);
        return p;
    }

    /**
     * 动态更新Paint字号（复用对象）
     * Dynamically update Paint font size (reuse objects)
     * @param fontSize 目标字号 / Target font size
     */
    private void updatePaintSizes(float fontSize) {
        pNorm.setTextSize(fontSize);
        pBold.setTextSize(fontSize);
        pCtr.setTextSize(fontSize);
        pCtrB.setTextSize(fontSize * 1.2f);
        pRgt.setTextSize(fontSize);
        pRgtB.setTextSize(fontSize);
        pTitle.setTextSize(fontSize * 1.5f);
        pLarge.setTextSize(fontSize * 1.2f);
    }

    private Paint makePaint(float size, boolean bold, Paint.Align align) {
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        p.setTextSize(size);
        p.setAntiAlias(false);
        p.setFakeBoldText(bold);
        p.setTextAlign(align);
        return p;
    }

    public void printFareChart(
            List<JSONObject>  sections,
            float             fontSize,
            float             lineSpacing
    ) {
        Bitmap bmp = renderFareChartBitmap(sections, fontSize, lineSpacing, 384);
        sendToPrinter(bmp);
    }

    private Bitmap renderFareChartBitmap(
            List<JSONObject> sections,
            float            fontSize,
            float            lineSpacing,
            int              printWidth
    ) {
        final float pad      = 12f;
        final float divGap   = 5f;
        final float colNo    = 60f;   // section-number column width
        final float colPrice = 90f;   // price column width

        // ── Paints ──────────────────────────────────────────────────────
        Paint pTitle  = makePaint(fontSize * 1.3f, true,  Paint.Align.CENTER);
        Paint pCtr    = makePaint(fontSize,         false, Paint.Align.CENTER);
        Paint pCtrB   = makePaint(fontSize,         true,  Paint.Align.CENTER);
        Paint pLeft   = makePaint(fontSize,         false, Paint.Align.LEFT);
        Paint pBoldL  = makePaint(fontSize,         true,  Paint.Align.LEFT);
        Paint pRight  = makePaint(fontSize,         false, Paint.Align.RIGHT);
        Paint pBoldR  = makePaint(fontSize,         true,  Paint.Align.RIGHT);
        Paint pSmall  = makePaint(fontSize * 0.82f, false, Paint.Align.CENTER);

        Paint pLine = new Paint();
        pLine.setColor(Color.BLACK);
        pLine.setStrokeWidth(1f);

        // ── Row heights — use getFontSpacing() on Paint objects ──────────
        // getFontSpacing() is a method on Paint, NOT on Paint.FontMetrics
        float titleH = pTitle.getFontSpacing() + lineSpacing;
        float rowH   = pLeft.getFontSpacing()  + lineSpacing;  // used for ALL normal rows
        float smallH = pSmall.getFontSpacing() + lineSpacing;

        // FontMetrics for baseline positioning
        Paint.FontMetrics fmTitle = pTitle.getFontMetrics();
        Paint.FontMetrics fm      = pLeft.getFontMetrics();   // all normal rows share the same size
        Paint.FontMetrics fmSmall = pSmall.getFontMetrics();

        int rowCount = (sections == null) ? 0 : sections.size();

        // ── Bitmap height ────────────────────────────────────────────────
        // Each section in the measurement is accounted for with rowH.
        float totalH = pad
                + titleH + divGap * 2   // bus name + divider
                + rowH   + divGap * 2   // permit   + divider
                + rowH   + divGap * 2   // "From:"  + divider
                + rowH   + divGap       // column headers + divider
                + (rowH  * Math.max(rowCount, 1)) // data rows
                + divGap * 2            // bottom divider gap
                + smallH                // timestamp
                + pad;

        // Use ceil + 60px safety so sub-pixel accumulation never clips a row
        Bitmap bmp = Bitmap.createBitmap(printWidth, (int) Math.ceil(totalH) + 60, Bitmap.Config.ARGB_8888);
        Canvas cv  = new Canvas(bmp);
        cv.drawColor(Color.WHITE);

        // ── Column x positions ───────────────────────────────────────────
        float cx          = printWidth / 2f;
        float xl          = pad;
        float xr          = printWidth - pad;
        float xNoRight    = xl + colNo;
        float xNameLeft   = xNoRight + 8f;
        float xPriceRight = xr;
        float noColCx     = xl + colNo / 2f;

        float y = pad;

        // ── Helper: draw one text row at the correct baseline ────────────
        // textTop = top-left y of the row slot.
        // We compute baseline = textTop + (-fm.ascent)  (ascent is negative).
        float baseline = y + (-fmTitle.ascent);

        // ── Column headers ────────────────────────────────────────────────
        baseline = y + (-fm.ascent);
        cv.drawText("No",      noColCx,     baseline, pCtrB);
        cv.drawText("Section", xNameLeft,   baseline, pBoldL);
        cv.drawText("Rs.",     xPriceRight, baseline, pBoldR);
        y += rowH;
        cv.drawLine(xl, y, xr, y, pLine); y += divGap;

        // ── Data rows ─────────────────────────────────────────────────────
        if (sections != null && !sections.isEmpty()) {
            for (int i = 0; i < sections.size(); i++) {
                JSONObject s = sections.get(i);

                String secNo    = s.optString("section_no",   "?");
                String secName  = s.optString("section_name", "?");
                float  price    = (float) s.optDouble("price", 0.0);

                // price=0 on the very first row (boarding stop) → show "0.00"
                // price=0 on other rows (no fare defined) → show "—"
                String priceStr;
                if (i == 0) {
                    priceStr = "0.00";
                } else {
                    priceStr = price > 0f
                            ? String.format(Locale.getDefault(), "%.2f", price)
                            : "—";
                }

                // Alternate row background
                if (i % 2 == 0) {
                    Paint bg = new Paint();
                    bg.setColor(0xFFF0F4FF);
                    cv.drawRect(xl, y, xr, y + rowH, bg);
                }

                // Draw text at correct baseline within the row slot
                baseline = y + (-fm.ascent);
                cv.drawText(secNo,    noColCx,     baseline, pCtr);
                cv.drawText(secName,  xNameLeft,   baseline, pLeft);
                cv.drawText(priceStr, xPriceRight, baseline, pRight);

                y += rowH; // advance exactly one rowH — guaranteed per row
            }
        } else {
            baseline = y + (-fm.ascent);
            cv.drawText("No sections available.", cx, baseline, pCtr);
            y += rowH;
        }

        return bmp;
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( )
    // Print single ticket (external interface)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 打印单张车票
     * Print a single ticket
     * @param busName 巴士名称 / Bus name
     * @param permitNumber 许可证号 / Permit number
     * @param ticketNumber 车票编号 / Ticket number
     * @param time 时间 / Time
     * @param date 日期 / Date
     * @param route 路线 / Route
     * @param busType 巴士类型 / Bus type
     * @param startStop 起点站 / Start stop
     * @param endStop 终点站 / End stop
     * @param fullCount 全票数量 / Full ticket count
     * @param fullPrice 全票单价 / Full ticket price
     * @param halfCount 半票数量 / Half ticket count
     * @param halfPrice 半票单价 / Half ticket price
     * @param luggageCount 行李数量 / Luggage count
     * @param luggagePrice 行李单价 / Luggage price
     * @param totalPrice 总价 / Total price
     * @param paidAmount 已付金额 / Paid amount
     * @param balanceAmount 余额 / Balance amount
     * @param busNumber 巴士编号 / Bus number
     * @param fontSize 字体大小 / Font size
     * @param lineSpacing 行间距 / Line spacing
     * @param is_show_paid 是否显示已付金额 / Whether to show paid amount
     */
    public void printTicket(
            String busName, String permitNumber,
            String ticketNumber, String time, String date,
            String route, String busType,
            String startStop, String endStop,
            int fullCount, float fullPrice,
            int halfCount, float halfPrice,
            int luggageCount, float luggagePrice,
            float totalPrice, float paidAmount,
            float balanceAmount, String busNumber,
            float fontSize, float lineSpacing,
            boolean is_show_paid
    ) {

        // Execute bitmap rendering and printing in child thread to avoid blocking main thread
        new Thread(() -> {
            Bitmap bmp = renderTicketBitmap(
                    busName, permitNumber, ticketNumber, time, date,
                    route, busType, startStop, endStop,
                    fullCount, fullPrice, halfCount, halfPrice,
                    luggageCount, luggagePrice, totalPrice,
                    paidAmount, balanceAmount, busNumber,
                    fontSize, lineSpacing, 384, is_show_paid
            );
            if (bmp != null) {
                sendToPrinter(bmp);
            }
        }).start();
    }

    public void printGroupTicket(
            String busName, String permitNumber,
            int ticketNumberStart, int ticketCount,
            String time, String date, float paidAmount,
            String route, String busType, String busNumber,
            String startStop, JSONArray tickets,
            float fontSize, float lineSpacing
    ) {

        // Execute bitmap rendering and printing in child thread to avoid blocking main thread
        new Thread(() -> {
            Bitmap bmp = null;
            try {
                bmp = renderGroupTicketBitmap(
                        busName, permitNumber, ticketNumberStart, ticketCount, time, date,
                        paidAmount, route, busType, busNumber, startStop, tickets,
                        fontSize, lineSpacing, 384
                );
            } catch (JSONException e) {
                e.printStackTrace ();
            }
            if (bmp != null) {
                sendToPrinter(bmp);
            }
        }).start();
    }

    private Bitmap renderGroupTicketBitmap(
            String busName, String permitNumber,
            int ticketNumberStart, int ticketCount,
            String time, String date, float paidAmount,
            String route, String busType, String busNumber,
            String startStop, JSONArray tickets,
            float fontSize, float lineSpacing,
            int printWidth
    ) throws JSONException {
        final float pad = 0f;
        final float divGap = 6f;

 // Paint
        // Reuse global Paint, only update font size
        updatePaintSizes(fontSize);

        float lh = pNorm.getFontSpacing() + lineSpacing;
        float tlh = pTitle.getFontSpacing() + lineSpacing;

 // ( )
        // Pre-calculate bitmap height (reduce repeated calculations)
        float totalH = (pad + tlh + divGap * 2
                + lh + divGap * 2
                + lh
                + lh + divGap * 2
                + lh
                + lh + divGap * 2
                + (lh*2)
                + divGap
                + lh + divGap * 2)*ticketCount
                + 150;

        float grandTotal = 0;

        // Avoid exception of zero height
        if (totalH <= 0) return null;

        Bitmap bmp = Bitmap.createBitmap(printWidth, (int) totalH, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        cv.drawColor(Color.WHITE);

        float cx = printWidth / 2f;
        float xl = pad;
        float xr = printWidth - pad;
        float c3w = (printWidth - pad * 2) / 3f;
        float c2x = xl + c3w + c3w / 2f;

        Paint.FontMetrics fm = pNorm.getFontMetrics();
        Paint.FontMetrics fmt = pTitle.getFontMetrics();
        float base = -fm.ascent;

        float y = 2;

 // ( , Paint)
        // Draw content (logic unchanged, only reuse Paint)
        for (int i=0; i < ticketCount; i++) {
            JSONObject ticket = tickets.getJSONObject(i);
            JSONObject endStopJson = ticket.getJSONObject("dest");
            String endStop = endStopJson.getString("bus_stop_description");
            int fullCount = ticket.getInt("pendingFull");
            float fullPrice = (float) ticket.getDouble("pendingFullPrice");
            int halfCount = ticket.getInt("pendingHalf");
            float halfPrice = (float) ticket.getDouble("pendingHalfPrice");
            int luggageCount = ticket.getInt("luggage");
            float luggagePrice = (float) ticket.getDouble("luggagePrice");
            float totalPrice = (fullCount*fullPrice) + (halfCount*halfPrice) + (luggageCount*luggagePrice);
            y += base;
            cv.drawText(busName, cx, y, pTitle);
            y += fmt.descent + lineSpacing;

            y += base;
            cv.drawText(permitNumber, xl, y, pNorm);
            cv.drawText("Bus No: " + busNumber, xr, y, pRgt);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText("Ticket #" + (ticketNumberStart+i), xl, y, pNorm);
            cv.drawText(time, c2x, y, pCtr);
            cv.drawText(date, xr, y, pRgt);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText(route, cx, y, pCtr);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText("From: " + startStop, cx, y, pCtrB);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText("To: " + endStop, cx, y, pCtrB);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText("Full: " + fullCount + " x " + fp(fullPrice), xl, y, pNorm);
            cv.drawText("Half: " + halfCount + " x " + fp(halfPrice), xr, y, pRgt);
            y += fm.descent + lineSpacing;

            if (luggageCount > 0) {
                y += base;
                cv.drawText("Luggage: " + luggageCount + " x " + fp(luggagePrice), cx, y, pCtr);
                y += fm.descent + lineSpacing;
            }

            y += divGap;

            y += base;
            cv.drawText("Total: Rs. " + fp(totalPrice), cx, y, pCtrB);
            y += fm.descent + lineSpacing;

            y += base;
            cv.drawText(busType, cx, y, pCtr);
            y += fm.descent + lineSpacing;
            y += divGap;
            y += 30;
           grandTotal += totalPrice;
        }

        drawDivider(cv, pLine, xl, xr, y);
        y += divGap;
        y += base;
        cv.drawText("─── SUMMARY ───", cx, y, pCtrB);
        y += fm.descent + lineSpacing;
        drawDivider(cv, pLine, xl, xr, y);
        y += divGap;

        y += base;
        cv.drawText("Grand Total: Rs. " + fp(grandTotal), cx, y, pLarge);
        y += fm.descent + lineSpacing;
        drawDivider(cv, pLine, xl, xr, y);
        y += divGap;

        y += base;
        cv.drawText("Paid:    Rs. " + fp(paidAmount), xl, y, pBold);
        cv.drawText("Bal: Rs. " + fp(paidAmount-grandTotal), xr, y, pRgtB);
        y += fm.descent + lineSpacing;
        drawDivider(cv, pLine, xl, xr, y);
        y += divGap;

        y += base;
        cv.drawText(busType, cx, y, pCtr);

        return bmp;
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( )
    // Print summary sheet (external interface)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 打印汇总单
     * Print summary sheet
     * @param busName 巴士名称 / Bus name
     * @param permitNumber 许可证号 / Permit number
     * @param time 时间 / Time
     * @param date 日期 / Date
     * @param route 路线 / Route
     * @param busType 巴士类型 / Bus type
     * @param startStop 起点站 / Start stop
     * @param lastDest 最后目的地 / Last destination
     * @param totalFull 全票总数 / Total full tickets
     * @param totalHalf 半票总数 / Total half tickets
     * @param totalLuggage 行李总数 / Total luggage
     * @param grandTotal 总计金额 / Grand total amount
     * @param paidAmount 已付金额 / Paid amount
     * @param balanceAmount 余额 / Balance amount
     * @param busNumber 巴士编号 / Bus number
     * @param fontSize 字体大小 / Font size
     * @param lineSpacing 行间距 / Line spacing
     */
    public void printSummary(
            String busName, String permitNumber,
            String time, String date, String route,
            String busType, String startStop, String lastDest,
            int totalFull, int totalHalf, int totalLuggage,
            float grandTotal, float paidAmount,
            float balanceAmount, String busNumber,
            float fontSize, float lineSpacing
    ) {

        // Execute bitmap rendering and printing in child thread
        new Thread(() -> {
            Bitmap bmp = renderSummaryBitmap(
                    busName, permitNumber, time, date, route,
                    busType, startStop, lastDest, totalFull,
                    totalHalf, totalLuggage, grandTotal,
                    paidAmount, balanceAmount, busNumber,
                    fontSize, lineSpacing, 384
            );
            if (bmp != null) {
                sendToPrinter(bmp);
            }
        }).start();
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( , )
    // Core optimization: Asynchronously send to printer (execute in child thread to avoid main thread blocking)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 将位图发送到打印机
     * Send bitmap to printer
     * @param bmp 要打印的位图 / Bitmap to print
     */
    private void sendToPrinter(Bitmap bmp) {
        try {
            if (mPrinterBinderService == null) {
                showToast("Printer service not connected.");
                Log.e(TAG, "sendToPrinter: mPrinterBinderService is NULL");
                if (bmp != null) bmp.recycle();
                return;
            }

 // AIDL
            // Batch execute AIDL calls to reduce inter-process communication times
            int status = mPrinterBinderService.dalThermalPrintGetStatus();
            mPrinterBinderService.dalThermalPrintSetDepth(6);
            Log.d(TAG, "Printer status: " + status);

            if (status != 0) {
                showToast("Printer not ready. Error: " + status);
                if (bmp != null) bmp.recycle();
                return;
            }

 // ( )
            // Optimize bitmap data conversion (optimized at the bottom layer)
            byte[] logoBitmap = PrintUtil.getBitmapData(bmp);
            int logoHeight = bmp.getHeight() + 15;

            // Recycle bitmap immediately to release memory
            bmp.recycle();


            // Update height parameter in print configuration
            PRINT_CONFIG[6] = logoHeight;
            PRINT_CONFIG[9] = 1;
            byte[] signBitmap = PrintUtil.getEmptyData(384, 1);


            // Asynchronously call print interface
            mPrinterBinderService.printBitmapPro(
                    logoBitmap, signBitmap, PRINT_CONFIG, "",
                    new IPrinterCallback.Stub() {
                        @Override
                        public void onResult(int errorCode, String errorMsg, int subErrorCode)
                                throws RemoteException {
                            Log.d(TAG, "onResult: code=" + errorCode
                                    + " msg=" + errorMsg + " sub=" + subErrorCode);

                            // Only process result prompts in callback to avoid time-consuming operations
                            mainHandler.post(() -> {
                                if (errorCode == 0) {
                                    try {
                                        mPrinterBinderService.dalThermalPrintPaperWorking(0);
                                    } catch (RemoteException e) {
                                        Log.e(TAG, "printPaperWorking error", e);
                                    }
                                    showToast("Printed!");
                                } else {
                                    showToast("Print error " + errorCode + ": " + errorMsg);
                                }
                            });
                        }
                    });

            showToast("Sending to printer…");

        } catch (Exception e) {
            Log.e(TAG, "sendToPrinter crash", e);
            showToast("Error: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    /**
     * 释放资源（Activity销毁时调用）
     * Release resources (call when Activity is destroyed)
     */
    public void release() {
        if (mServiceBound) {
            try {
                context.unbindService(mServiceConn);
            } catch (Exception e) {
                Log.e(TAG, "unbindService error", e);
            }
            mServiceBound = false;
        }

        // Clear global objects to assist GC
        mSdkServiceManager = null;
        mPrinterBinderService = null;
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( )
    // Service binding logic (optimized exception handling)
    // ─────────────────────────────────────────────────────────────────────
    private final ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSdkServiceManager = ISdkServiceManager.Stub.asInterface(service);
            if (mSdkServiceManager == null) return;
            try {
                mSdkServiceManager.asBinder().linkToDeath(() -> {
                    mSdkServiceManager = null;
                    mPrinterBinderService = null;
                    mServiceBound = false;

                    // Delay reconnection to avoid frequent binding
                    mainHandler.postDelayed(TicketPrinter.this::bindSdkService, 1000);
                }, 0);
                mPrinterBinderService = IPrinterBinderService.Stub.asInterface(
                        mSdkServiceManager.getService(ServiceID.SERVICE_ID_PRINTER));
                mServiceBound = true;
                Log.d(TAG, "Printer service connected.");
            } catch (RemoteException e) {
                Log.e(TAG, "onServiceConnected error", e);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mSdkServiceManager = null;
            mPrinterBinderService = null;
            mServiceBound = false;
        }
    };

    /**
     * 绑定SDK服务
     * Bind SDK service
     */
    private void bindSdkService() {
        Intent intent = new Intent();
        intent.setPackage(AFSDK_PACKAGE);
        intent.setAction(AFSDK_ACTION);
        try {
            boolean bound = context.bindService(intent, mServiceConn, Context.BIND_AUTO_CREATE);
            if (!bound) Log.e(TAG, "bindService() failed – is AFSDKService installed?");
        } catch (Exception e) {
            Log.e(TAG, "bindService exception", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( : Paint+ )
    // Single ticket bitmap rendering (optimization: reuse Paint + reduce calculations)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 渲染车票位图
     * Render ticket bitmap
     * @return 渲染好的位图 / Rendered bitmap
     */
    private Bitmap renderTicketBitmap(
            String busName, String permitNumber,
            String ticketNumber, String time, String date,
            String route, String busType,
            String startStop, String endStop,
            int fullCount, float fullPrice,
            int halfCount, float halfPrice,
            int luggageCount, float luggagePrice,
            float totalPrice, float paidAmount,
            float balanceAmount, String busNumber,
            float fontSize, float lineSpacing,
            int printWidth, boolean isShowPaid
    ) {
        final float pad = 12f;
        final float divGap = 6f;

 // Paint
        // Reuse global Paint, only update font size
        updatePaintSizes(fontSize);

        float lh = pNorm.getFontSpacing() + lineSpacing;
        float tlh = pTitle.getFontSpacing() + lineSpacing;
        boolean showPaid = paidAmount > 0f;

 // ( )
        // Pre-calculate bitmap height (reduce repeated calculations)
        float totalH = pad + tlh + divGap * 2
                + lh + divGap * 2
                + lh
                + lh + divGap * 2
                + lh
                + lh + divGap * 2
                + lh
                + (luggageCount > 0 ? lh : 0f)
                + divGap
                + lh + divGap * 2;

        if (showPaid || isShowPaid) {
            totalH += lh + divGap * 2;
        }


        // Avoid exception of zero height
        if (totalH <= 0) return null;

        Bitmap bmp = Bitmap.createBitmap(printWidth, (int) totalH, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        cv.drawColor(Color.WHITE);

        float cx = printWidth / 2f;
        float xl = pad;
        float xr = printWidth - pad;
        float c3w = (printWidth - pad * 2) / 3f;
        float c2x = xl + c3w + c3w / 2f;

        Paint.FontMetrics fm = pNorm.getFontMetrics();
        Paint.FontMetrics fmt = pTitle.getFontMetrics();
        float base = -fm.ascent;

        float y = 2;

 // ( , Paint)
        // Draw content (logic unchanged, only reuse Paint)
        y += base;
        cv.drawText(busName, cx, y, pTitle);
        y += fmt.descent + lineSpacing;

        y += base;
        cv.drawText(permitNumber, xl, y, pNorm);
        cv.drawText("Bus No: " + busNumber, xr, y, pRgt);
        y += fm.descent + lineSpacing;

        y += base;
        cv.drawText(ticketNumber, xl, y, pNorm);
        cv.drawText(time, c2x, y, pCtr);
        cv.drawText(date, xr, y, pRgt);
        y += fm.descent + lineSpacing;

        y += base;
        cv.drawText(route, cx, y, pCtr);
        y += fm.descent + lineSpacing;

        y += base;
        cv.drawText("From: " + startStop, cx, y, pCtrB);
        y += fm.descent + lineSpacing;

        y += base;
        cv.drawText("To: " + endStop, cx, y, pCtrB);
        y += fm.descent + lineSpacing;

        y += base;
        cv.drawText("Full: " + fullCount + " x " + fp(fullPrice), xl, y, pNorm);
        cv.drawText("Half: " + halfCount + " x " + fp(halfPrice), xr, y, pRgt);
        y += fm.descent + lineSpacing;

        if (luggageCount > 0) {
            y += base;
            cv.drawText("Luggage: " + luggageCount + " x " + fp(luggagePrice), cx, y, pCtr);
            y += fm.descent + lineSpacing;
        }

        y += divGap;

        y += base;
        cv.drawText("Total: Rs. " + fp(totalPrice), cx, y, pCtrB);
        y += fm.descent + lineSpacing;

        if (showPaid || isShowPaid) {
            y += base;
            cv.drawText("Paid: Rs. " + fp(paidAmount), xl, y, pBold);
            cv.drawText("Bal: Rs. " + fp(balanceAmount), xr, y, pRgtB);
            y += fm.descent + lineSpacing;
        }

        y += base;
        cv.drawText(busType, cx, y, pCtr);

        return bmp;
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( : Paint+ )
    // Summary sheet bitmap rendering (optimization: reuse Paint + reduce calculations)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 渲染汇总单位图
     * Render summary sheet bitmap
     * @return 渲染好的位图 / Rendered bitmap
     */
    private Bitmap renderSummaryBitmap(
            String busName, String permitNumber,
            String time, String date, String route,
            String busType, String startStop, String lastDest,
            int totalFull, int totalHalf, int totalLuggage,
            float grandTotal, float paidAmount,
            float balanceAmount, String busNumber,
            float fontSize, float lineSpacing,
            int printWidth
    ) {
        final float pad = 12f;
        final float divGap = 6f;

 // Paint
        // Reuse global Paint
        updatePaintSizes(fontSize);

        boolean showPaid = paidAmount > 0f;
        float lh = pNorm.getFontSpacing() + lineSpacing;
        float llh = pLarge.getFontSpacing() + lineSpacing;

        float totalH = divGap * 2 + lh
                + llh + divGap * 2 + 80;

        if (showPaid) {
            totalH += lh + divGap * 2;
        }

        if (totalH <= 0) return null;

        Bitmap bmp = Bitmap.createBitmap(printWidth, (int) totalH, Bitmap.Config.ARGB_8888);
        Canvas cv = new Canvas(bmp);
        cv.drawColor(Color.WHITE);

        float cx = printWidth / 2f;
        float xl = pad;
        float xr = printWidth - pad;

        Paint.FontMetrics fm = pNorm.getFontMetrics();
        Paint.FontMetrics fml = pLarge.getFontMetrics();
        float base = -fm.ascent;
        float lbase = -fml.ascent;

        float y = 0;

        drawDivider(cv, pLine, xl, xr, y);
        y += divGap * 2;
        y += base;
        cv.drawText("─── SUMMARY ───", cx, y, pCtrB);
        y += fm.descent + lineSpacing;
        drawDivider(cv, pLine, xl, xr, y);
        y += divGap * 2;

        y += lbase;
        cv.drawText("Grand Total: Rs. " + fp(grandTotal), cx, y, pLarge);
        y += fml.descent + lineSpacing;
        drawDivider(cv, pLine, xl, xr, y);
        y += divGap * 2;

        if (showPaid) {
            y += base;
            cv.drawText("Paid:    Rs. " + fp(paidAmount), xl, y, pBold);
            cv.drawText("Bal: Rs. " + fp(balanceAmount), xr, y, pRgtB);
            y += fm.descent + lineSpacing;
            drawDivider(cv, pLine, xl, xr, y);
            y += divGap * 2;
        }

        y += base;
        cv.drawText(busType, cx, y, pCtr);

        return bmp;
    }

    // ─────────────────────────────────────────────────────────────────────
 // ( : )
    // Tool methods (optimization: reuse formatting tools)
    // ─────────────────────────────────────────────────────────────────────
    /**
     * 绘制分隔线
     * Draw divider line
     * @param cv 画布 / Canvas
     * @param p 画笔 / Paint
     * @param xl 左坐标 / Left coordinate
     * @param xr 右坐标 / Right coordinate
     * @param y Y坐标 / Y coordinate
     */
    private void drawDivider(Canvas cv, Paint p, float xl, float xr, float y) {
        cv.drawLine(xl, y, xr, y, p);
    }

    /**
     * 优化价格格式化：复用DecimalFormat，避免频繁创建String.format
     * Optimize price formatting: reuse DecimalFormat to avoid frequent creation of String.format
     * @param price 价格 / Price
     * @return 格式化后的价格字符串 / Formatted price string
     */
    private String fp(float price) {
        try {
            return priceFormat.format(price);
        } catch (Exception e) {
            return String.format("%.2f", price);
        }
    }

    /**
     * 优化Toast：避免重复创建Handler，统一主线程调用
     * Optimize Toast: avoid repeated creation of Handler, unified main thread call
     * @param msg 提示信息 / Prompt message
     */
    private void showToast(String msg) {
        mainHandler.post(() -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show());
    }
}