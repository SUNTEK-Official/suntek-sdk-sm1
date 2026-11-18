package com.szanfu.sdk.test.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public final class PaymentQrManager {

    private static final String TAG = "PaymentQrManager";

    private static final String ADD_BASE_URL =
            "https://api.example.com/prod-api/system/payment/add";
    private static final String FIND_BASE_URL =
            "https://api.example.com/prod-api/system/payment/Find";

    private static final long POLL_INTERVAL_MS = 1000L;
    private static final long DEFAULT_TIMEOUT_MS = 5 * 60 * 1000L;

    public interface Callback {
        void onQrDisplayed(String uploadUrl);

        void onPolling(String queryResponse);

        void onPaymentSuccess(String queryResponse);

        void onTimeout();

        void onError(String msg);
    }

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean polling = new AtomicBoolean(false);

    private final LcdQrHelper lcdQrHelper;

    private Runnable pollRunnable;

    public PaymentQrManager(Context context, LcdUtils lcdUtils) {
        this.lcdQrHelper = new LcdQrHelper(context, lcdUtils);
    }

    public String buildAddUrl(String sn, String money) {
        try {
            return ADD_BASE_URL
                    + "?sn=" + URLEncoder.encode(sn, "UTF-8")
                    + "&money=" + URLEncoder.encode(money, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("buildAddUrl error: " + e.getMessage(), e);
        }
    }

    public String buildFindUrl(String sn) {
        try {
            return FIND_BASE_URL
                    + "?sn=" + URLEncoder.encode(sn, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("buildFindUrl error: " + e.getMessage(), e);
        }
    }

    public void startPayment(String sn, String money, Callback callback) {
        startPayment(sn, money, DEFAULT_TIMEOUT_MS, callback);
    }

    public void startPayment(String sn, String money, long timeoutMs, Callback callback) {
        if (TextUtils.isEmpty(sn)) {
            if (callback != null) callback.onError("sn is empty");
            return;
        }
        if (TextUtils.isEmpty(money)) {
            if (callback != null) callback.onError("money is empty");
            return;
        }

        stopPayment();

        final String addUrl = buildAddUrl(sn, money);
        final String findUrl = buildFindUrl(sn);

        boolean showRet = lcdQrHelper.lcdDisplayQrUrl(addUrl);
        if (!showRet) {
            if (callback != null) callback.onError("lcdDisplayQrUrl failed");
            return;
        }

        if (callback != null) {
            callback.onQrDisplayed(addUrl);
        }

        final long deadline = SystemClock.elapsedRealtime() + timeoutMs;
        polling.set(true);

        pollRunnable = new Runnable() {
            @Override
            public void run() {
                if (!polling.get()) {
                    return;
                }

                if (SystemClock.elapsedRealtime() >= deadline) {
                    stopPayment();
                    if (callback != null) callback.onTimeout();
                    return;
                }

                ioExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            final String response = doGet(findUrl);

                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!polling.get()) {
                                        return;
                                    }

                                    if (callback != null) {
                                        callback.onPolling(response);
                                    }

                                    if (isPaymentSuccess(response)) {
                                        stopPayment();
                                        if (callback != null) {
                                            callback.onPaymentSuccess(response);
                                        }
                                    } else {
                                        mainHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                                    }
                                }
                            });

                        } catch (final Exception e) {
                            mainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (!polling.get()) {
                                        return;
                                    }

                                    Log.e(TAG, "poll error: " + e.getMessage(), e);


                                    mainHandler.postDelayed(pollRunnable, POLL_INTERVAL_MS);
                                }
                            });
                        }
                    }
                });
            }
        };

        mainHandler.post(pollRunnable);
    }

    public void stopPayment() {
        polling.set(false);
        if (pollRunnable != null) {
            mainHandler.removeCallbacks(pollRunnable);
        }
    }

    public void release() {
        stopPayment();
        ioExecutor.shutdownNow();
    }

    private String doGet(String urlStr) throws Exception {
        HttpURLConnection connection = null;
        InputStream is = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(urlStr);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setUseCaches(false);
            connection.connect();

            int code = connection.getResponseCode();
            is = (code >= 200 && code < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            if (is == null) {
                return "";
            }

            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();

        } finally {
            try {
                if (reader != null) reader.close();
            } catch (Exception ignore) {
            }
            try {
                if (is != null) is.close();
            } catch (Exception ignore) {
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private boolean isPaymentSuccess(String json) {
        if (TextUtils.isEmpty(json)) {
            return false;
        }

        try {
            JSONObject root = new JSONObject(json);

            if (root.optBoolean("paid", false)) {
                return true;
            }

            if (root.optBoolean("success", false)) {
                String status = normalize(firstNonEmpty(
                        root.optString("status"),
                        root.optString("tradeStatus"),
                        root.optString("payStatus"),
                        root.optString("state")
                ));
                if (isSuccessStatus(status)) {
                    return true;
                }
            }

            JSONObject data = root.optJSONObject("data");
            if (data != null) {
                if (data.optBoolean("paid", false)) {
                    return true;
                }
                if (data.optBoolean("success", false)) {
                    return true;
                }

                String dataStatus = normalize(firstNonEmpty(
                        data.optString("status"),
                        data.optString("tradeStatus"),
                        data.optString("payStatus"),
                        data.optString("state")
                ));
                if (isSuccessStatus(dataStatus)) {
                    return true;
                }

                int payStatusInt = data.optInt("payStatus", -1);
                if (payStatusInt == 1) {
                    return true;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "isPaymentSuccess parse error: " + e.getMessage(), e);
        }

        return false;
    }

    private boolean isSuccessStatus(String status) {
        return "SUCCESS".equals(status)
                || "PAID".equals(status)
                || "PAY_SUCCESS".equals(status)
                || "TRADE_SUCCESS".equals(status)
                || "1".equals(status)
                || "TRUE".equals(status);
    }

    private String normalize(String value) {
        if (value == null) return "";
        return value.trim().toUpperCase();
    }

    private String firstNonEmpty(String... values) {
        if (values == null) return "";
        for (String v : values) {
            if (!TextUtils.isEmpty(v)) {
                return v;
            }
        }
        return "";
    }
}