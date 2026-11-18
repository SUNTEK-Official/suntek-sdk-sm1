package com.szanfu.sdk.test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.szanfu.sdk.test.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class SimCardDivActivity extends Activity {
    private SubscriptionManager subscriptionManager;
    private TelephonyManager telephonyManager;
    private Spinner simSpinner;
    private EditText currentPinEditText;
    private EditText newPinEditText;
    private EditText confirmPinEditText;
    private Button enablePinButton;
    private Button disablePinButton;
    private Button changePinButton;
    private TextView pinStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_card_div);


        initViews();


        subscriptionManager = (SubscriptionManager) getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

 // SIM
        setupSimSpinner();


        setupButtonListeners();

 // PIN
        updatePinStatus();
    }

    private void initViews() {
        simSpinner = findViewById(R.id.sim_spinner);
        currentPinEditText = findViewById(R.id.current_pin);
        newPinEditText = findViewById(R.id.new_pin);
        confirmPinEditText = findViewById(R.id.confirm_pin);
        enablePinButton = findViewById(R.id.enable_pin);
        disablePinButton = findViewById(R.id.disable_pin);
        changePinButton = findViewById(R.id.change_pin);
        pinStatusTextView = findViewById(R.id.pin_status);
    }

    private void setupSimSpinner() {
 // SIM
        List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();

        if (subscriptionInfos == null || subscriptionInfos.isEmpty()) {
            Toast.makeText(this, "未检测到SIM卡", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String[] simNames = new String[subscriptionInfos.size()];
        for (int i = 0; i < subscriptionInfos.size(); i++) {
            SubscriptionInfo info = subscriptionInfos.get(i);
            simNames[i] = "SIM " + (i + 1) + " - " + info.getCarrierName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, simNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        simSpinner.setAdapter(adapter);
    }

    private void setupButtonListeners() {
        enablePinButton.setOnClickListener(v -> enablePin());
        disablePinButton.setOnClickListener(v -> disablePin());
        changePinButton.setOnClickListener(v -> changePin());
    }

    private void enablePin() {
        int simIndex = simSpinner.getSelectedItemPosition();
        String newPin = newPinEditText.getText().toString();
        String confirmPin = confirmPinEditText.getText().toString();

        if (!validatePinInput(newPin, confirmPin)) {
            return;
        }

 // TelephonyManager

        boolean success = setSimPinLock(simIndex, newPin, true);

        if (success) {
            Toast.makeText(this, "已启用SIM卡PIN码保护", Toast.LENGTH_SHORT).show();
            updatePinStatus();
        } else {
            Toast.makeText(this, "启用PIN码失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void disablePin() {
        int simIndex = simSpinner.getSelectedItemPosition();
        String currentPin = currentPinEditText.getText().toString();

        if (currentPin.isEmpty()) {
            Toast.makeText(this, "请输入当前PIN码", Toast.LENGTH_SHORT).show();
            return;
        }

 // TelephonyManager

        boolean success = setSimPinLock(simIndex, currentPin, false);

        if (success) {
            Toast.makeText(this, "已禁用SIM卡PIN码保护", Toast.LENGTH_SHORT).show();
            updatePinStatus();
        } else {
            Toast.makeText(this, "禁用PIN码失败，请检查PIN码是否正确", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePin() {
        int simIndex = simSpinner.getSelectedItemPosition();
        String currentPin = currentPinEditText.getText().toString();
        String newPin = newPinEditText.getText().toString();
        String confirmPin = confirmPinEditText.getText().toString();

        if (currentPin.isEmpty()) {
            Toast.makeText(this, "请输入当前PIN码", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!validatePinInput(newPin, confirmPin)) {
            return;
        }

 // TelephonyManager

        boolean success = changeSimPin(simIndex, currentPin, newPin);

        if (success) {
            Toast.makeText(this, "已成功修改PIN码", Toast.LENGTH_SHORT).show();
            updatePinStatus();
        } else {
            Toast.makeText(this, "修改PIN码失败，请检查当前PIN码是否正确", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean validatePinInput(String newPin, String confirmPin) {
        if (newPin.isEmpty() || confirmPin.isEmpty()) {
            Toast.makeText(this, "请输入新PIN码和确认PIN码", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!newPin.equals(confirmPin)) {
            Toast.makeText(this, "新PIN码和确认PIN码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (newPin.length() < 4) {
            Toast.makeText(this, "PIN码必须至少4位数字", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean setSimPinLock(int simIndex, String pin, boolean enabled) {
 // TelephonyManager setSimLockEnabled


        try {
            TelephonyManager tm = getSimTelephonyManager(simIndex);
            if (tm != null) {
 // return tm.setSimLockEnabled(enabled, pin);
                return true;
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "无权限修改SIM卡设置", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean changeSimPin(int simIndex, String oldPin, String newPin) {
 // TelephonyManager changeSimPin


        try {
            TelephonyManager tm = getSimTelephonyManager(simIndex);
            if (tm != null) {
 // return tm.changeSimPin(oldPin, newPin);
                return true;
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "无权限修改SIM卡PIN码", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private TelephonyManager getSimTelephonyManager(int simIndex) {
 // SIM TelephonyManager
        List<SubscriptionInfo> subscriptionInfos = subscriptionManager.getActiveSubscriptionInfoList();
        if (subscriptionInfos != null && simIndex < subscriptionInfos.size()) {
            SubscriptionInfo info = subscriptionInfos.get(simIndex);
            return telephonyManager.createForSubscriptionId(info.getSubscriptionId());
        }
        return null;
    }

    private void updatePinStatus() {
        int simIndex = simSpinner.getSelectedItemPosition();
        TelephonyManager tm = getSimTelephonyManager(simIndex);

        if (tm != null) {
 // TelephonyManager getSimState
 // PIN
            boolean pinEnabled = isSimPinEnabled(tm);

            String status = pinEnabled ? "已启用" : "已禁用";
            pinStatusTextView.setText("当前状态: " + status);


            disablePinButton.setEnabled(pinEnabled);
            enablePinButton.setEnabled(!pinEnabled);
        }
    }

    private boolean isSimPinEnabled(TelephonyManager tm) {
 // PIN

        return false;
    }
}