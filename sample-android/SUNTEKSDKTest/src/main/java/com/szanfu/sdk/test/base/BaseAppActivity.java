package com.szanfu.sdk.test.base;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.NoDoubleClickListener;
import com.szanfu.sdk.test.view.AutoMeasureListView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseAppActivity extends Activity implements View.OnClickListener {
    private static final String SERVICE_CHECK_TAG = "SDK_BIND_CHECK";

    protected TextView mTextView;
    protected ScrollView mScrollView;
    protected LinearLayout mContainer;
    protected AutoMeasureListView mListView;
    protected ArrayAdapter<String> adapter;
    protected ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    private Button clear_text;

    private ISdkServiceManager mSdkServiceManager = null;
    boolean isServiceBound = false;

    protected Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base_press_list_button);
        //mContext = getApplicationContext();
        mContext = this;

        mTextView = (TextView) findViewById(R.id.tv_result);
        mScrollView = (ScrollView) findViewById(R.id.sv_result);
        mContainer = (LinearLayout) findViewById(R.id.ll_container);
        clear_text = (Button) findViewById(R.id.clear_text);
        clear_text.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService();
    }

    /**
     * 绑定SDK binder服务，调用SDK接口的前提。
     */
    private void bindService() {
        Intent intent = new Intent();
        intent.setPackage("com.szanfu.sdk.service");
        intent.setAction("com.szanfu.sdk.service.SDK_SERVICE");
        bindService(intent, conn, Context.BIND_AUTO_CREATE);
    }

    private void unbindService(){
        LogUtils.d("--------- 注销服务 Unbind from Service ------------");
        if(isServiceBound){
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " calling unbindService(conn)");
            unbindService(conn);
            Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " unbindService(conn) called");
        }
    }

    public ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
            mSdkServiceManager = null;
            LogUtils.d("SDK binder service disconnected.");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isServiceBound = true;
            mSdkServiceManager = ISdkServiceManager.Stub.asInterface(service);
            if (mSdkServiceManager != null) {
                try {
                    /**
                     * 监听SDK binder服务的死亡状态，如果挂掉，则重新进行绑定。
                     */
                    mSdkServiceManager.asBinder().linkToDeath(new IBinder.DeathRecipient() {
                        @Override
                        public void binderDied() {
                            LogUtils.d("SDK binder service binderDied.");
                            bindService();
                        }
                    }, 0);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                BaseAppActivity.this.onServiceConnected(mSdkServiceManager);
            }
        }
    };

    public abstract void onServiceConnected(ISdkServiceManager sdkServiceManager);

    protected void onItemClick(int position, String itemName) {
    }

    final protected void setButtonDataSource(final String[] items) {

        if (AppConfig.buildType == 0) { // =0 =1
            clear_text.setText("Clear Test");
        } else { // =0 =1
            clear_text.setText("Clear Test" + "   Interface  number：" + items.length);
        }
        mListView = (AutoMeasureListView) findViewById(R.id.lv_button);
        adapter = new ArrayAdapter<String>(this, R.layout.item_list_button, R.id.button, items) {
            public View getView(final int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                Button btn = (Button) v.findViewById(R.id.button);
                btn.setOnClickListener(new NoDoubleClickListener() {
                    @Override
                    protected void onNoDoubleClick(View v) {
                        onItemClick(position, items[position]);
                    }
                });
//                btn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        onItemClick(position, items[position]);
//                    }
//                });
                return v;
            }

            @Override
            public boolean isEnabled(int position) {
                return super.isEnabled(position);
            }
        };
        mListView.setAdapter(adapter);
    }

    final protected void appendErrorText(String msg) {
        appendText(Color.RED, msg);
    }

    final protected void appendInfoText(String msg) {
        appendText(Color.BLUE, msg);
    }

    final protected void appendErrorText(String item, String msg) {
        appendText(Color.RED, item + "\n" + msg);
    }

    final protected void appendTextResult(int ret, String msg) {
        if(ret == 0)
            appendText(msg);
        else
            appendText(Color.RED, msg);
    }

    final protected void appendText(final String text) {
        Log.d("ttt [UI]", text);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextView.getLineCount() > 50) {
                    mTextView.setText("");
                }
                mTextView.append(text + "\n");
                mTextView.post(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });
            }
        });
    }

    final protected void appendText(final int color, final String text) {
        if(color == Color.RED)
            Log.e("ttt [UI]", text);
        else if(color == Color.BLUE)
            Log.i("ttt [UI]", text);
        else
            Log.d("ttt [UI]", text);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextView.getLineCount() > 50) {
                    mTextView.setText("");
                }
                int start = mTextView.getText().length();
                mTextView.append(text + "\n");
                Spannable style = (Spannable) mTextView.getText();
                int end = start + text.length();
                ForegroundColorSpan colors = new ForegroundColorSpan(color);
                style.setSpan(colors, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    final protected void appendText(final String text, final Bitmap bmp) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mTextView.getLineCount() > 50) {
                    mTextView.setText("");
                }
                ImageSpan imgSpan = new ImageSpan(BaseAppActivity.this, bmp);
                SpannableString spanString = new SpannableString("icon");
                spanString.setSpan(imgSpan, 0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                mTextView.append(spanString);
                mTextView.append(text);
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

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
                mTextView.setText("");
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
        Log.i(SERVICE_CHECK_TAG, getClass().getSimpleName() + " onDestroy(), isServiceBound=" + isServiceBound);
        if(isServiceBound){
            unbindService();
        }
    }

}
