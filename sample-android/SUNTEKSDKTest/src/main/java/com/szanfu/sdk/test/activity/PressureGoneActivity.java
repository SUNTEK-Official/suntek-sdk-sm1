package com.szanfu.sdk.test.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import cn.com.uama.flashlightcompat.FlashlightCompat;

public class PressureGoneActivity extends ListButtonBaseActivity {

    public static final int POSITION_LIGHT_TEST = 0;
    private String[] mButtons;
    Timer timer = new Timer();
    private EditText ed_conut;
    int iTimeOut =1;
    private LinearLayout ll_count;
    boolean bButtonFlag = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mButtons = getResources().getStringArray(R.array.pressure_LIST);
        setButtonDataSource(mButtons);
        ed_conut = findViewById(R.id.ed_conut);
        ll_count = findViewById(R.id.ll_count);
        ll_count.setVisibility(View.VISIBLE);
    }
    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
    }
    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {
            case POSITION_LIGHT_TEST:
                    if (bButtonFlag){
                    ToastUtils.showToast(this,"The last task was not completed, please try again later",1200);
                    }else {
                        if (ed_conut.getText().toString().trim().isEmpty()) {
                            iTimeOut = 1*1000*5;
                            LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
                        } else {
                            iTimeOut = Integer.parseInt(ed_conut.getText().toString().trim())*1000*60;
                            LogUtils.e("sacnQrCodetimeOut :"+iTimeOut);
                        }
                        appendText("Turn on the light, set the opening time to:"+iTimeOut+"Seconds");
                        setFrontFlashStatus(true);
                        bButtonFlag = true;
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                setFrontFlashStatus( false );
                            }
                        }, iTimeOut);
                    }

                break;
        }
    }
    private void setFrontFlashStatus(boolean enable) {
        if (enable) {
            FlashlightCompat.INSTANCE.turnOn(PressureGoneActivity.this);
        } else {
            appendText("Turn off not only the light");
            bButtonFlag = false;
            FlashlightCompat.INSTANCE.turnOff(PressureGoneActivity.this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
