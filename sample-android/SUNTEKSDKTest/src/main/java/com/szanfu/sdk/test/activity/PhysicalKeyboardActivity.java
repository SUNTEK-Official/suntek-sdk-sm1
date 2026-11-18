package com.szanfu.sdk.test.activity;

import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.LogUtils;


public class PhysicalKeyboardActivity extends ListButtonBaseActivity {

    private EditText etCurrentPin, etNewPin;

    private ISystemBinderService mSystemBinderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_physical_keyboard);

        initViews();
        initTelephonyManager();
        setupClickListeners();
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void initViews() {
        etCurrentPin = findViewById(R.id.et_current_pin);
        etNewPin = findViewById(R.id.et_new_pin);

    }

    private void initTelephonyManager() {

    }

    private void setupClickListeners() {

    }


}