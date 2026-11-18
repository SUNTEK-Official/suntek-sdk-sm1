package com.szanfu.sdk.test.base;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.adapter.MyButtonAdapter;
import com.szanfu.sdk.test.bean.ItemButtonBean;
import com.szanfu.sdk.test.bean.ItemResultBean;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.ClickAntiShake;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.SDKServiceManager;
import com.szanfu.sdk.test.util.WatermarkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class ListButtonBaseActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "ListButtonBaseActivity";
    private static final String SERVICE_CHECK_TAG = "SDK_BIND_CHECK";
    protected TextView mTextView;
    protected ScrollView mScrollView;
    //    protected LinearLayout mContainer;
//    protected AutoMeasureListView mListView;
    protected ArrayAdapter<String> adapter;
    protected ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private Button clear_text;

    private ISdkServiceManager mSdkServiceManager = null;
    boolean isServiceBound = false;

    protected Context mContext;

//    private final List<String> logBuffer = new ArrayList<>();
    private final List<String> logBuffer = new CopyOnWriteArrayList<>();
 // Handler
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    private static final int MAX_LOG_LINES = 200;

    private boolean isUpdatingUI = false;

    private List<ItemResultBean> resultData = new ArrayList<>();
    RecyclerView recyclerViewResult;
    MyButtonAdapter myButtonAdapter;
    private List<ItemButtonBean> buttonData = new ArrayList<>();
    RecyclerView recycleView_btn;
    //    ItemResultBean itemResultBean;
    ResultAdapter resultAdapter;
    int iResultCount = 250; // 250 , List
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard/Android/DeviceMSG.txt";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;

    private SDKServiceManager sdkServiceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_list_button);
        //mContext = getApplicationContext();
        mContext = this;

        mTextView = (TextView) findViewById(R.id.tv_result);
        mScrollView = (ScrollView) findViewById(R.id.sv_result);
//        mContainer = (LinearLayout) findViewById(R.id.ll_container);
        clear_text = (Button) findViewById(R.id.clear_text);
        clear_text.setOnClickListener(this);

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

        sdkServiceManager = new SDKServiceManager(this);

        sdkServiceManager.setOnServiceConnectListener(new SDKServiceManager.OnServiceConnectListener() {
            @Override
            public void onConnected(ISdkServiceManager sdkServiceManager) {
                onServiceConnected(sdkServiceManager);
            }

            @Override
            public void onDisconnected() {
                Log.i(TAG, "onDisconnected Success ");
            }
        });
//        if (AppConfig.buildType != 0){
        setWaterMask();
//        }
    }

    void setWaterMask() {
        String libVersion = "";
 // ( )
        WatermarkUtils.addWatermark(this,
                libVersion,
                Color.BLUE, 24, 0, false, 3);
    }


    private String readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/DeviceMSG.txt");
        if (!file.exists())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e("stringBuilder.toString() :" + stringBuilder.toString());
        return stringBuilder.toString();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (sdkServiceManager != null) {
            sdkServiceManager.bind();
        }
    }

    public abstract void onServiceConnected(ISdkServiceManager sdkServiceManager);

    protected void onItemClick(int position, String itemName) {
    }

    final protected void setButtonDataSource(final String[] items) {
        buttonData.clear();
        if (AppConfig.buildType == 0) { // =0 =1
            clear_text.setText("Clear Test");
        } else { // =0 =1
            clear_text.setText("Clear Test" + "   Interface  number：" + items.length);
        }

        recycleView_btn = (RecyclerView) findViewById(R.id.rv_btn);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recycleView_btn.setLayoutManager(linearLayoutManager);
        for (int i = 0; i < items.length; i++) {
            Log.e(TAG, "setButtonDataSource: " + items[i]);
            String btnName = items[i];
            ItemButtonBean itemButtonBean = new ItemButtonBean();
            itemButtonBean.setName(btnName);
            buttonData.add(itemButtonBean);
        }
        Log.e(TAG, "setButtonDataSource: " + buttonData.size());
        myButtonAdapter = new MyButtonAdapter(buttonData, this);
        recycleView_btn.setAdapter(myButtonAdapter);
        myButtonAdapter.setmOnRecyclerItemClickClicklistener(new MyButtonAdapter.OnRecyclerItemClickClicklistener() {
            @Override
            public void onRecyclerItemClickListener(int position) {
                if (!ClickAntiShake.isGlobalFastClick()) {
                    onItemClick(position, buttonData.get(position).getName());
                }
            }
        });
    }

    final protected void appendErrorText(String msg) {
//        appendText(Color.RED, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(msg);
                itemResultBean.setiResultLevel(1);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();
            }
        });

    }

    final protected void appendInfoText(String msg) {
//        appendText(Color.BLUE, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(msg);
                itemResultBean.setiResultLevel(2);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();
            }
        });

    }

    final protected void appendErrorText(String item, String msg) {
//        appendText(Color.RED, item + "\n" + msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(item + "\n" + msg);
                itemResultBean.setiResultLevel(1);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();
            }
        });

    }

    final protected void appendTextResult(int ret, String msg) {
        if (ret == 0) {
            //            appendText(msg);
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

        } else {
//            appendText(Color.RED, msg);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (resultData.size() > iResultCount) {
                        resultData.clear();
                        resultAdapter.notifyDataSetChanged();
                    }
                    ItemResultBean itemResultBean = new ItemResultBean();
                    itemResultBean.setName(msg);
                    itemResultBean.setiResultLevel(1);
                    resultData.add(itemResultBean);
                    recyclerViewResult.scrollToPosition(resultData.size() - 1);
                    resultAdapter.notifyDataSetChanged();
                }
            });

        }
    }

    final protected void appendText(final String text) {
        Log.e(TAG, "appendText: " + text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (mTextView.getLineCount() > 200) {
//                    mTextView.setText("");
//                }
                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(text);
                itemResultBean.setiResultLevel(3);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();

//                mTextView.append(text + "\n");
//                mTextView.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
//                    }
//                });
            }
        });
    }

    final protected void appendText(final int color, final String text) {
        if (color == Color.RED)
            Log.e("ttt [UI]", text);
        else if (color == Color.BLUE)
            Log.i("ttt [UI]", text);
        else
            Log.d("ttt [UI]", text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (mTextView.getLineCount() > 200) {
//                    mTextView.setText("");
//                }
//                int start = mTextView.getText().length();
//                mTextView.append(text + "\n");

                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(text);
                itemResultBean.setiResultLevel(3);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();
//                Spannable style = (Spannable) mTextView.getText();
//                int end = start + text.length();
//                ForegroundColorSpan colors = new ForegroundColorSpan(color);
//                style.setSpan(colors, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    final protected void appendText(final String text, final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                if (mTextView.getLineCount() > 200) {
//                    mTextView.setText("");
//                }
//                ImageSpan imgSpan = new ImageSpan(ListButtonBaseActivity.this, bmp);
//                SpannableString spanString = new SpannableString("icon");
//                spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
//                mTextView.append(spanString);
//                mTextView.append(text);

                if (resultData.size() > iResultCount) {
                    resultData.clear();
                    resultAdapter.notifyDataSetChanged();
                }
                ItemResultBean itemResultBean = new ItemResultBean();
                itemResultBean.setName(text);
                itemResultBean.setiResultLevel(3);
                resultData.add(itemResultBean);
                recyclerViewResult.scrollToPosition(resultData.size() - 1);
                resultAdapter.notifyDataSetChanged();
//                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    /**
     * 追加普通文本
     */
    /*final protected void appendText(final String text) {
        synchronized (logBuffer) {
            logBuffer.add(text);
        }
        scheduleLogUpdate();
    }

    *//**
     * 追加带颜色的文本
     *//*
    final protected void appendText(final int color, final String text) {
        logBuffer.add(text);
        uiHandler.post(() -> {
            if (mTextView.getLineCount() > MAX_LOG_LINES) {
                mTextView.setText("");
            }
            int start = mTextView.getText().length();
            mTextView.append(text + "\n");


            Spannable style = (Spannable) mTextView.getText();
            int end = start + text.length();
            style.setSpan(new ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    *//**
     * 追加带图片的文本
     *//*
    final protected void appendText(final String text, final Bitmap bmp) {
        uiHandler.post(() -> {
            if (mTextView.getLineCount() > MAX_LOG_LINES) {
                mTextView.setText("");
            }


            ImageSpan imgSpan = new ImageSpan(ListButtonBaseActivity.this, bmp);
            SpannableString spanString = new SpannableString("icon");
            spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            mTextView.append(spanString);
            mTextView.append(text + "\n");

            mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }

    *//**
     * 追加错误文本（封装）
     *//*
    final protected void appendErrorText(String msg) {
        appendText(Color.RED, msg);
    }

    *//**
     * 追加信息文本（封装）
     *//*
    final protected void appendInfoText(String msg) {
        appendText(Color.BLUE, msg);
    }

    *//**
     * 追加错误信息（带额外内容）
     *//*
    final protected void appendErrorText(String item, String msg) {
        appendText(Color.RED, item + "\n" + msg);
    }

    *//**
     * 追加结果文本（根据状态决定颜色）
     *//*
    final protected void appendTextResult(int ret, String msg) {
        if (ret == 0) {
            appendText(msg);
        } else {
            appendErrorText(msg);
        }
    }

    *//**
     * 安排日志更新任务，避免频繁刷新 UI
     *//*
    private void scheduleLogUpdate() {
        if (!isUpdatingUI) {
            isUpdatingUI = true;
            uiHandler.postDelayed(() -> {
                try {
                    updateTextView();
                } finally {
                    isUpdatingUI = false;
                }
            }, 100);
        }
    }

    */

    /**
     * 更新 TextView 内容
     *//*
    private void updateTextView() {
        runOnUiThread(() -> {
            List<String> logsToAppend;
            synchronized (logBuffer) {
                logsToAppend = new ArrayList<>(logBuffer);
                logBuffer.clear();
            }


            if (mTextView.getLineCount() > MAX_LOG_LINES) {
                mTextView.setText("");
            }


            for (String log : logsToAppend) {
                mTextView.append(log + "\n");
            }


            mScrollView.post(() -> mScrollView.fullScroll(ScrollView.FOCUS_DOWN));
        });
    }*/
    final protected void UI_Toast(final String text) {
        Log.d("ttt [UI].Toast", text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(mContext, text, Toast.LENGTH_LONG).show();
            }
        });
    }

//    public void clear(View v) {
//        mTextView.setText("");
//    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.clear_text:
//                mTextView.setText("");
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

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_HOME || event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogUtils.e("onDestroy---》");
        if (sdkServiceManager != null) {
            LogUtils.e("onDestroy---》isServiceBound：" + sdkServiceManager.isServiceBound());
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " onDestroy(), before unbind, isServiceBound="
                    + sdkServiceManager.isServiceBound());
            sdkServiceManager.unbind();
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " onDestroy(), after unbind, isServiceBound="
                    + sdkServiceManager.isServiceBound());
        }
    }

}
