package com.szanfu.sdk.test.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.LogUtils;


public class MTKSimCardManagerActivity extends ListButtonBaseActivity {

    private RadioGroup radioSimSlot;
    private EditText etCurrentPin, etNewPin, etPukCode, etNewPinPuk;
    private Button btnCheckPinStatus, btnVerifyPin, btnChangePin, btnUnlockWithPuk;
    private Button btnEnablePinLock, btnDisablePinLock;
    private TextView tvStatus;

    private TelephonyManager telephonyManager;
    private MTKSimPinManager mtkSimPinManager;
    private ISystemBinderService mSystemBinderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mtksim_card_manager);

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
        radioSimSlot = findViewById(R.id.radio_sim_slot);
        etCurrentPin = findViewById(R.id.et_current_pin);
        etNewPin = findViewById(R.id.et_new_pin);
        etPukCode = findViewById(R.id.et_puk_code);
        etNewPinPuk = findViewById(R.id.et_new_pin_puk);

        btnCheckPinStatus = findViewById(R.id.btn_check_pin_status);
        btnVerifyPin = findViewById(R.id.btn_verify_pin);
        btnChangePin = findViewById(R.id.btn_change_pin);
        btnUnlockWithPuk = findViewById(R.id.btn_unlock_with_puk);
        btnEnablePinLock = findViewById(R.id.btn_enable_pin_lock);
        btnDisablePinLock = findViewById(R.id.btn_disable_pin_lock);

        tvStatus = findViewById(R.id.tv_status);
    }

    private void initTelephonyManager() {
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        mtkSimPinManager = new MTKSimPinManager(this);
    }

    private void setupClickListeners() {
        btnCheckPinStatus.setOnClickListener(v -> checkPinStatus());
        btnVerifyPin.setOnClickListener(v -> verifyPin());
        btnChangePin.setOnClickListener(v -> changePin());
        btnUnlockWithPuk.setOnClickListener(v -> unlockWithPuk());
        btnEnablePinLock.setOnClickListener(v -> enablePinLock());
        btnDisablePinLock.setOnClickListener(v -> disablePinLock());
    }

    private int getSelectedSimSlot() {
        return radioSimSlot.getCheckedRadioButtonId() == R.id.radio_sim1 ? 0 : 1;
    }
 // PIN
    private void checkPinStatus() {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            String[] simCardStatus = mSystemBinderService.getSimCardStatus();

            /**
             * SIM_STATE_ABSENT  无SIM卡
             * SIM_STATE_READY   SIM卡就绪，可用
             * SIM_STATE_PIN_REQUIRED  SIM卡被PIN码锁定
             * SIM_STATE_PUK_REQUIRED  SIM卡被PUK码锁定
             * SIM_STATE_NETWORK_LOCKED SIM卡被网络锁锁定
             * SIM_STATE_UNKNOWN   SIM卡状态未知
             */

            if (simCardStatus[0] == "0"){

            }
            stringBuilder.append("SIM Card 1 状态 :"+ simCardStatus[0]+"\n" );

            stringBuilder.append("SIM Card 2 状态 :"+ simCardStatus[1]);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        tvStatus.setText(  stringBuilder.toString() );

//        try {
//            int simSlot = getSelectedSimSlot();
//            boolean isPinEnabled = mtkSimPinManager.isPinEnabled(simSlot);
//            int remainingAttempts = mtkSimPinManager.getPinRemainingAttempts(simSlot);
//
//            String status = String.format(
 // "SIM %d :\n" +
 // "PIN : %s\n" +
 // " : %d\n" +
 // "SIM : %s"
//                    simSlot + 1,
 // isPinEnabled ? " " : " "
//                    remainingAttempts,
//                    getSimStateString(simSlot)
//            );
//
//            tvStatus.setText(status);
//        } catch (Exception e) {
 // showError(" PIN : " + e.getMessage());
//        }
    }

 // PIN
    private void verifyPin() {

        int simCardSlot = getSelectedSimSlot();
        String pin = etCurrentPin.getText().toString().trim();
        if (pin.isEmpty()) {
            showError("请输入当前PIN码");
            return;
        }
        try {
        boolean verifyPIN =  mSystemBinderService.simCardVerifyPIN(simCardSlot,pin);
            LogUtils.e("验证PIN码 verifyPIN ： " +verifyPIN );
            if (verifyPIN ){
                tvStatus.setText("验证PIN码 : 成功" );
            }else {
                tvStatus.setText("验证PIN码 : 失败" );
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }



//
//        try {
//            int simSlot = getSelectedSimSlot();
//            boolean success = mtkSimPinManager.verifyPin(simSlot, pin);
//
//            if (success) {
 // showSuccess("PIN ");
 // checkPinStatus(); //
//            } else {
 // showError("PIN ");
//            }
//        } catch (Exception e) {
 // showError(" PIN : " + e.getMessage());
//        }
    }

 // PIN
    private void changePin() {
        int simCardSlot = getSelectedSimSlot();
        String currentPin = etCurrentPin.getText().toString().trim();
        String newPin = etNewPin.getText().toString().trim();

        if (currentPin.isEmpty() || newPin.isEmpty()) {
            showError("请输入当前PIN码和新PIN码");
            return;
        }

        try {
            boolean bSimCardChangePin =  mSystemBinderService.simCardChangePin(simCardSlot,currentPin,newPin);
            LogUtils.e(" 修改PIN码 bSimCardChangePin ： " +bSimCardChangePin );
            if(bSimCardChangePin){
                tvStatus.setText("修改PIN码 : 成功" );
            }else {
                tvStatus.setText("修改PIN码 : 失败" );
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

//
//        try {
//            int simSlot = getSelectedSimSlot();
//            boolean success = mtkSimPinManager.changePin(simSlot, currentPin, newPin);
//
//            if (success) {
 // showSuccess("PIN ");
//                etCurrentPin.setText("");
//                etNewPin.setText("");
//            } else {
 // showError("PIN ");
//            }
//        } catch (Exception e) {
 // showError(" PIN : " + e.getMessage());
//        }
    }
 // PUK
    private void unlockWithPuk() {
        int simCardSlot = getSelectedSimSlot();
        String puk = etPukCode.getText().toString().trim();
        String newPin = etNewPinPuk.getText().toString().trim();

        if (puk.isEmpty() || newPin.isEmpty()) {
            showError("请输入PUK码和新PIN码");
            return;
        }

        try {
             boolean UnlockWithPuk = mSystemBinderService.simCardUnlockWithPuk(simCardSlot,puk,newPin);
             LogUtils.e("PUK解锁 UnlockWithPuk : "+ UnlockWithPuk);
            if ( UnlockWithPuk){
                tvStatus.setText("PUK解锁 : 成功" );
            }else {
                tvStatus.setText("PUK解锁 : 失败" );
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }

//        try {
//            int simSlot = getSelectedSimSlot();
//            boolean success = mtkSimPinManager.unlockWithPuk(simSlot, puk, newPin);
//
//            if (success) {
 // showSuccess("PUK ");
//                etPukCode.setText("");
//                etNewPinPuk.setText("");
 // checkPinStatus(); //
//            } else {
 // showError("PUK ");
//            }
//        } catch (Exception e) {
 // showError("PUK : " + e.getMessage());
//        }
    }

 // PIN
    private void enablePinLock() {
        int simCardSlot = getSelectedSimSlot();

        String pin = etCurrentPin.getText().toString().trim();
        if (pin.isEmpty()) {
            showError("请输入PIN码");
            return;
        }

        try {
            boolean bEnablePinLock = mSystemBinderService.simCardEnablePinLock(simCardSlot,pin);
            LogUtils.e("启用PIN锁定 bEnablePinLock : "+bEnablePinLock);
            LogUtils.e("启用PIN锁定 slot : "+1);
            LogUtils.e("启用PIN锁定 pin : "+pin);
            if (bEnablePinLock){
                tvStatus.setText("启用PIN锁定 : 成功" );
            }else {
                tvStatus.setText("启用PIN锁定 : 失败" );
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

//        try {
//            int simSlot = getSelectedSimSlot();
//            boolean success = mtkSimPinManager.enablePinLock(simSlot, pin);
//
//            if (success) {
 // showSuccess("PIN ");
 // checkPinStatus(); //
//            } else {
 // showError(" PIN ");
//            }
//        } catch (Exception e) {
 // showError(" PIN : " + e.getMessage());
//        }
    }
 // PIN
    private void disablePinLock() {
        int simCardSlot = getSelectedSimSlot();

        String pin = etCurrentPin.getText().toString().trim();
        if (pin.isEmpty()) {
            showError("请输入PIN码");
            return;
        }

        try {
        boolean bDissablePinLock = mSystemBinderService.simCardDisablePinLock(simCardSlot,pin);
            if (bDissablePinLock){
                tvStatus.setText("禁用PIN锁定 : 成功" );
            }else {
                tvStatus.setText("禁用PIN锁定 : 失败" );
            }
            LogUtils.e("禁用PIN锁定 bDissablePinLock : " +bDissablePinLock );
        } catch (RemoteException e) {
            e.printStackTrace();
        }

//        try {
//            int simSlot = getSelectedSimSlot();
//            boolean success = mtkSimPinManager.disablePinLock(simSlot, pin);
//
//            if (success) {
 // showSuccess("PIN ");
 // checkPinStatus(); //
//            } else {
 // showError(" PIN ");
//            }
//        } catch (Exception e) {
 // showError(" PIN : " + e.getMessage());
//        }
    }

    private String getSimStateString(int simSlot) {
        try {
            int state = telephonyManager.getSimState(simSlot);
            switch (state) {
                case TelephonyManager.SIM_STATE_ABSENT:
                    return "无SIM卡";
                case TelephonyManager.SIM_STATE_PIN_REQUIRED:
                    return "需要PIN码";
                case TelephonyManager.SIM_STATE_PUK_REQUIRED:
                    return "需要PUK码";
                case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
                    return "网络锁定";
                case TelephonyManager.SIM_STATE_READY:
                    return "就绪";
                case TelephonyManager.SIM_STATE_NOT_READY:
                    return "未就绪";
                case TelephonyManager.SIM_STATE_PERM_DISABLED:
                    return "永久禁用";
                case TelephonyManager.SIM_STATE_CARD_IO_ERROR:
                    return "卡IO错误";
                default:
                    return "未知状态";
            }
        } catch (Exception e) {
            return "获取状态失败";
        }
    }

    private void showSuccess(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        tvStatus.setText("错误: " + message);
    }
}