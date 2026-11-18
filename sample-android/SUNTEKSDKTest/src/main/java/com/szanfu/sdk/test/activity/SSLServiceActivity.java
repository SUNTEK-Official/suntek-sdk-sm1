package com.szanfu.sdk.test.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bigkoo.pickerview.TimePickerView;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.pinpad.IPinPadBinderService;
import com.szanfu.sdk.api.ssl.ISslService;
import com.szanfu.sdk.api.system.EncDecResult;
import com.szanfu.sdk.api.system.GetAttributeResult;
import com.szanfu.sdk.api.system.HalReadUserKeyByIdResult;
import com.szanfu.sdk.api.system.IFirmwareUpdateCallback;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.api.system.ISystemEncDecBinderService;
import com.szanfu.sdk.api.system.ISystemReadUserKeyBinderService;
import com.szanfu.sdk.api.system.VbatStateParam;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.base.ResultAdapter;
import com.szanfu.sdk.test.bean.ItemResultBean;
import com.szanfu.sdk.test.util.AFUtils;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.CommonSetSNDialog;
import com.szanfu.sdk.test.util.CommonSetTUSNDialog;
import com.szanfu.sdk.test.util.HexUtil;
import com.szanfu.sdk.test.util.LedState;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.PrintUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static android.graphics.Bitmap.createBitmap;

public class SSLServiceActivity extends ListButtonBaseActivity {
    private static final String TAG = "SSLServiceActivity";
    public static final int POSITION_SSL_SET_CERT_FROM_FILE = 0;
    public static final int POSITION_SSL_CONNECT = 1;
    public static final int POSITION_SSL_WRITE = 2;
    public static final int POSITION_SSL_READ = 3;
    public static final int POSITION_SSL_CLOSE = 4;
    public static final int POSITION_SSL_STRESS_TEST = 6;


    private String[] mButtons;
    private ISystemBinderService mSystemBinderService;
    private ISslService mSslService;
    private RecyclerView recyclerViewResult;
    private List<ItemResultBean> resultData = new ArrayList<>();
    int iResultCount = 250; // 250 , List
    private ResultAdapter resultAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recyclerViewResult = findViewById(R.id.rv_result);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerViewResult.setLayoutManager(linearLayoutManager);
        resultAdapter = new ResultAdapter(resultData, this);
        recyclerViewResult.setAdapter(resultAdapter);
        resultAdapter.setmOnRecyclerItemClickClicklistener(new ResultAdapter.OnRecyclerItemClickClicklistener() {
            @Override
            public void onRecyclerItemClickListener(int position) {
                LogUtils.e("onRecyclerItemClickListener: ===" + position);
            }
        });
        mButtons = getResources().getStringArray(R.array.SSL_LIST);
        setButtonDataSource(mButtons);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            Log.e(TAG, "onServiceConnected: 服务连接成功");
            mSslService = ISslService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SSL_SERVICE));
            Log.e(TAG, (mSslService == null ? "ISslService 服务绑定失败" : "ISslService 服务绑定成功"));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);
        switch (position) {
            case POSITION_SSL_SET_CERT_FROM_FILE:
                doSetCertFromFile();
                break;
            case POSITION_SSL_CONNECT:
                doConnect();
                break;
            case POSITION_SSL_WRITE:
                doWriteData();
                break;
            case POSITION_SSL_READ:
                doReadData();
                break;
            case POSITION_SSL_CLOSE:
                doCloseConnection();
                break;
            case POSITION_SSL_STRESS_TEST:
                doStressTest();
                break;
            default:
                break;
        }
    }

    private void doSetCertFromFile() {

        String certPath = "/sdcard/ssl/overseaspos_cert_chain.pem";
        File file = new File(certPath);
        Log.e(TAG, "file.exists(): " + (file.exists() ? "存在" : "不存在"));
        try {
            if (file.exists()) {
                int slot = 1;
 // allowExpired (true= , false= )
                int result = mSslService.sslSetCert(slot, certPath, true);
                logResult("Set Cert", result);
            } else {
                log("certPath Error:" + certPath + "\n Does not exist");
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void doConnect() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                int result = mSslService.sslConnect(1, "overseaspos.szanfu.cn", 443, 5000);
                logResult("Connect", result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }

    private void doWriteData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                //byte[] data = "Hello SSL".getBytes();
                String host = "overseaspos.szanfu.cn";
                String path = "/prod-api/system/url/appAllList";
                String jsonBody = "{"
                        + "\"body\":{"
                        + "\"app_type\":1,"
                        + "\"customer_id\":2,"
                        + "\"mfr\":\"A2\""
                        + "},"
                        + "\"sn\":\"706990000886\","
                        + "\"timestamp\":1747893536995,"
                        + "\"version\":1"
                        + "}";
                byte[] request = HttpRequestBuilder.buildPost(host, path, null, jsonBody);
                int result = mSslService.sslWrite(1, request, 3000);
                logResult("Write", result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });
    }


    private void doReadData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                int[] errCode = new int[1];
                byte[] data = mSslService.sslRead(1, 3000, errCode);
                if (errCode[0] == 0) {
                    String responseData = AFUtils.showResult16Str(data);
                    HttpResponseDecoder.printFormattedHttpResponse(responseData);

                    log("Read: " + HttpResponseDecoder.decodeHexResponse(responseData));
                } else {
                    log("Read failed, errcode = " + errCode[0]);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        });

    }

    private void doCloseConnection() {
        try {
            int result = mSslService.sslClose(1);
            logResult("Close", result);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void doStressTest() {
        log("开始并发连接测试");

 // ( )
        final String certPath = "/sdcard/ssl/overseaspos_cert_chain.pem";
        final String certPem;
        /*try {
            certPem = AFUtils.readFile(certPath);
        } catch (IOException e) {
            log("证书读取失败: " + e.getMessage());
            return;
        }*/

        ExecutorService executor = Executors.newFixedThreadPool(5);

        for (int i = 1; i <= 5; i++) {
            final int slot = i;
            executor.execute(() -> {
                try {
                    String host = "overseaspos.szanfu.cn";
                    int setCertResult = mSslService.sslSetCert(slot, certPath, true);
                    if (setCertResult != 0) {
                        logResult("slot " + slot + " sslSetCert Error:", setCertResult);
                        return;
                    }

                    int connResult = mSslService.sslConnect(slot, host, 443, 5000);
                    if (connResult != 0) {
                        log("slot " + slot + " connect failed: " + connResult);
                        return;
                    }

                    String path = "/prod-api/system/url/appAllList";
                    String jsonBody = "{"
                            + "\"body\":{"
                            + "\"app_type\":1,"
                            + "\"customer_id\":2,"
                            + "\"mfr\":\"A2\""
                            + "},"
                            + "\"sn\":\"706990000886\","
                            + "\"timestamp\":1747893536995,"
                            + "\"version\":1"
                            + "}";
                    byte[] request = HttpRequestBuilder.buildPost(host, path, null, jsonBody);

                    int writeResult = mSslService.sslWrite(slot, request, 3000);
                    if (writeResult != 0) {
                        logResult("slot " + slot + " sslWrite Error:", writeResult);
                        return;
                    }

                    int[] errCode = new int[1];
                    byte[] data = mSslService.sslRead(slot, 3000, errCode);
                    if (errCode[0] == 0 && data != null) {
                        String responseData = AFUtils.showResult16Str(data);
                        HttpResponseDecoder.printFormattedHttpResponse(responseData);
                        log("slot " + slot + " Read: " + HttpResponseDecoder.decodeHexResponse(responseData));
                    } else {
                        log("slot " + slot + " Read failed, errCode = " + errCode[0]);
                    }

                    mSslService.sslClose(slot);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    log("slot " + slot + " RemoteException: " + e.getMessage());
                }
            });
        }

        executor.shutdown();
    }


    private void logResult(String tag, int result) {
        if (result == 0) {
            log(tag + " 成功");
        } else {
            log(tag + " 失败, code = " + result);
        }
    }

    private synchronized void log(String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(msg);
                itemResultBean.setiResultLevel(3);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();

            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_text:
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        resultData.clear();
                        resultAdapter.notifyDataSetChanged();
                    }
                });
                break;
        }
    }


    static class HttpResponseDecoder {

        /**
         * 将十六进制字符串转为可读字符串
         */
        public static String decodeHexResponse(String hex) {
            try {
                hex = hex.replaceAll("\\s+", "");
                byte[] bytes = hexStringToByteArray(hex);
                return new String(bytes, "UTF-8");
            } catch (Exception e) {
                return "解码失败: " + e.getMessage();
            }
        }

        /**
         * 十六进制字符串转 byte[]
         */
        private static byte[] hexStringToByteArray(String s) {
            int len = s.length();
            byte[] data = new byte[len / 2];
            for (int i = 0; i < len; i += 2) {
                data[i / 2] = (byte)
                        ((Character.digit(s.charAt(i), 16) << 4)
                                + Character.digit(s.charAt(i + 1), 16));
            }
            return data;
        }

        /**
         * 解析结果
         */
        public static void printFormattedHttpResponse(String hex) {
            String decoded = decodeHexResponse(hex);
            Log.i(TAG, "【解析后的 HTTP 响应】\n----------------------");
            Log.i(TAG, decoded);
            Log.i(TAG, "----------------------");
        }
    }

    /**
     * 构建 GET 请求
     */
    static class HttpRequestBuilder {
        public static byte[] buildGet(String host, String path, Map<String, String> headers) {
            StringBuilder builder = new StringBuilder();
            builder.append("GET ").append(path).append(" HTTP/1.1\r\n");
            builder.append("Host: ").append(host).append("\r\n");
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
                }
            }
            builder.append("Connection: close\r\n");
            builder.append("\r\n");
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }

        /**
         * 构建 POST 请求
         */
        public static byte[] buildPost(String host, String path, Map<String, String> headers, String body) {
            StringBuilder builder = new StringBuilder();
            builder.append("POST ").append(path).append(" HTTP/1.1\r\n");
            builder.append("Host: ").append(host).append("\r\n");
            builder.append("Content-Length: ").append(body != null ? body.getBytes(StandardCharsets.UTF_8).length : 0).append("\r\n");
            builder.append("Content-Type: application/json\r\n");
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
                }
            }
            builder.append("Connection: close\r\n");
            builder.append("\r\n");
            if (body != null) {
                builder.append(body);
            }
            return builder.toString().getBytes(StandardCharsets.UTF_8);
        }
    }

}
