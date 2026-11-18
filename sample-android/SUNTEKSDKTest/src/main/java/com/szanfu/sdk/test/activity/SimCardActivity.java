package com.szanfu.sdk.test.activity;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.SimCardUtils;

import java.util.List;


public class SimCardActivity extends ListButtonBaseActivity implements View.OnClickListener {
    private TextView sim1_check_result;
    private TextView sim2_check_result;
    private TextView tv_check;
    private TextView btn_sim1_unlock,btn_sim2_unlock;
    private Context mContext;
    String[] itemsResult=new String[2];
    private SubscriptionManager mSubscriptionManager;
    private TelephonyManager mTelephonyManager;
    private ISystemBinderService mSystemBinderService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sim_card);
        sim1_check_result = findViewById(R.id.sim1_check_result);
        sim2_check_result = findViewById(R.id.sim2_check_result);

        tv_check=findViewById(R.id.tv_check);
        tv_check.setOnClickListener(this);
        btn_sim1_unlock = findViewById(R.id.btn_sim1_unlock);
        btn_sim1_unlock.setOnClickListener(this);
        btn_sim2_unlock = findViewById(R.id.btn_sim2_unlock);
        btn_sim2_unlock.setOnClickListener(this);
//        ******************************************************************************************
        mContext = this;
        mSubscriptionManager = SubscriptionManager.from(mContext);
        mTelephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_check:

                refreshSimInfo();

                itemsResult=new String[2];
                /**
                 * itemsResult[0] =0 ; No SIM card 无SIM卡
                 * itemsResult[0] =1 ; The SIM card is ready and available SIM卡就绪，可用
                 * itemsResult[0] =2 ; The SIM card is locked by the PIN code SIM卡被PIN码锁定
                 * itemsResult[0] =3 ; The SIM card is locked by the PUK code SIM卡被PUK码锁定
                 * itemsResult[0] =4 ; The SIM card is locked by the network lock SIM卡被网络锁锁定
                 * itemsResult[0] =5 ; The SIM card status is unknown SIM卡状态未知
                 */
                StringBuilder sbSIM1Status =new StringBuilder();
                StringBuilder sbSIM2Status =new StringBuilder();

                try {
                    itemsResult = mSystemBinderService.getSimCardStatus();

                    /**
                     *     <string name="noSIMcard">status:0 无SIM卡  </string>
                     *     <string name="simavailable">status:1 SIM卡就绪  </string>
                     *     <string name="simPINLocked">status:2 SIM卡被PIN码锁定  </string>
                     *     <string name="simPUKLocked">status:3 SIM卡被PUK码锁定  </string>
                     *     <string name="simnetLocked">status:4 SIM卡被网络锁锁定  </string>
                     *     <string name="simUndifind">status:5 SIM卡状态未知  </string>
                     */
                    if (itemsResult[0].equals("0")   ){
                        sbSIM1Status.append(getResources().getString(R.string.noSIMcard)); // "status:0 SIM
                    }else if (itemsResult[0].equals("1")){
                        sbSIM1Status.append(getResources().getString(R.string.simavailable)); // "status:1 SIM "
                    }else if (itemsResult[0].equals("2")){
                        sbSIM1Status.append(getResources().getString(R.string.simPINLocked) ); // "status:2 SIM PIN "
                        btn_sim1_unlock.setVisibility(View.VISIBLE);
                    }else if (itemsResult[0].equals("3")){
                        sbSIM1Status.append(getResources().getString(R.string.simPUKLocked) ); // "status:3 SIM PUK "
                        btn_sim1_unlock.setVisibility(View.VISIBLE);
                    }else if (itemsResult[0].equals("4")){
                        sbSIM1Status.append(getResources().getString(R.string.simnetLocked) ); // "status:4 SIM "
                    }else if (itemsResult[0].equals("5") ){
                        sbSIM1Status.append(getResources().getString(R.string.simUndifind) ); // "status:5 SIM "
                    }


                    if (itemsResult[1].equals("0")   ){
                        sbSIM2Status.append(getResources().getString(R.string.noSIMcard)); // "status:0 SIM
                    }else if (itemsResult[1].equals("1")){
                        sbSIM2Status.append(getResources().getString(R.string.simavailable)); // "status:1 SIM "
                    }else if (itemsResult[1].equals("2")){
                        sbSIM2Status.append(getResources().getString(R.string.simPINLocked) ); // "status:2 SIM PIN "
                        btn_sim1_unlock.setVisibility(View.VISIBLE);
                    }else if (itemsResult[1].equals("3")){
                        sbSIM2Status.append(getResources().getString(R.string.simPUKLocked) ); // "status:3 SIM PUK "
                        btn_sim1_unlock.setVisibility(View.VISIBLE);
                    }else if (itemsResult[1].equals("4")){
                        sbSIM2Status.append(getResources().getString(R.string.simnetLocked) ); // "status:4 SIM "
                    }else if (itemsResult[1].equals("5") ){
                        sbSIM2Status.append(getResources().getString(R.string.simUndifind) ); // "status:5 SIM "
                    }


                    LogUtils.e("SIM 1 : "+itemsResult[0]);
                    LogUtils.e("SIM 2 : "+itemsResult[1]);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        sim1_check_result.setText("SIM 1 Ststus : "+sbSIM1Status.toString());
                        sim2_check_result.setText("SIM 2 Status : "+sbSIM2Status.toString());
                    }
                });

                break;

            case R.id.btn_sim1_unlock:

                if (!itemsResult[0].equals(null)){
                    if (itemsResult[0].equals("2")){
 // Unlock PIN code PIN
                        showUnlockPINDialog(0,"Please input PIN code···");
                    }else if (itemsResult[0].equals(3)){
 // Unlock the PUK code PUK
                        showPUKDialog(0,itemsResult[1],"Please input PUK code···");
                    }
                }


                break;
            case  R.id.btn_sim2_unlock:
                LogUtils.e("itemsResult[1] :"+itemsResult[1]);
                if (!itemsResult[1].equals(null)){
                    if (itemsResult[1].equals("2")){
 // Unlock PIN code PIN
                        showUnlockPINDialog(1,"Please input PIN code···");
                    }else if (itemsResult[1].equals(3)){
 // Unlock the PUK code PUK
                        showPUKDialog(1,itemsResult[1],"Please input PUK code···");
                    }
                }

                break;
        }
    }


    /**
     * Unlock PIN code 解锁PIN码
     * @param slotIndex SIM1   SIM2
     * @param hitMsg
     */
    private void showUnlockPINDialog(int slotIndex ,String hitMsg) {


        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_input);
        dialog.setTitle("Input dialog");


        final EditText etInput = dialog.findViewById(R.id.etInput);
        etInput.setText(hitMsg );

        final EditText etInputnew = dialog.findViewById(R.id.etInputnew);
        etInputnew.setText(hitMsg );
        etInputnew.setVisibility(View.GONE);

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimCardActivity.this, "Cancle", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd_PIN_PUK = etInput.getText().toString().trim();
                String newPwdPIN = etInputnew.getText().toString().trim();
                if (!pwd_PIN_PUK.isEmpty()) {
                    Toast.makeText(SimCardActivity.this, "Input content: " + pwd_PIN_PUK, Toast.LENGTH_SHORT).show();
                    try {
                        String[] sUnlockPin = mSystemBinderService.unLockSIMCardPIN(slotIndex, pwd_PIN_PUK  );

                        if (sUnlockPin[0].equals("0")){
                            Toast.makeText(SimCardActivity.this, "PIN Unlock Success", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SimCardActivity.this, "PIN Unlock Failed"+sUnlockPin[1], Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(SimCardActivity.this, "The input cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dialog.show();
    }


    /**
     * PUK 密码解锁
     * @param slotIndex
     * @param pwdType
     * @param hitMsg
     */
    private void showPUKDialog(int slotIndex ,String pwdType,String hitMsg) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_input);
        dialog.setTitle("Input dialog");


        final EditText etInput = dialog.findViewById(R.id.etInput);
        etInput.setText(hitMsg );

        final EditText etInputnew = dialog.findViewById(R.id.etInputnew);
        etInputnew.setText(hitMsg );

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimCardActivity.this, "Cancle", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd_PIN_PUK = etInput.getText().toString().trim();
                String newPwdPIN = etInputnew.getText().toString().trim();
                if (!pwd_PIN_PUK.isEmpty()) {
                    Toast.makeText(SimCardActivity.this, "Input content: " + pwd_PIN_PUK, Toast.LENGTH_SHORT).show();

                    try {
                        String[] bUnlockPUK = mSystemBinderService.unLockSIMPUK(slotIndex, pwd_PIN_PUK,newPwdPIN  );
                        if (bUnlockPUK[0].equals("0")){
 // PUK , PIN
                            showSetInputDialog(slotIndex );

                        }else {
 // PUK
                            Toast.makeText(SimCardActivity.this, "The remaining number of failed PUK code decoding attempts"+bUnlockPUK[1] , Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(SimCardActivity.this, "The input cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dialog.show();
    }


    private void showSetInputDialog(int slotIndex ) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_input);
        dialog.setTitle("Input dialog box");


        final EditText etInput = dialog.findViewById(R.id.etInput);
        etInput.setText("" );

        final EditText etInputnew = dialog.findViewById(R.id.etInputnew);
        etInputnew.setText("" );

        Button btnCancel = dialog.findViewById(R.id.btnCancel);
        Button btnConfirm = dialog.findViewById(R.id.btnConfirm);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SimCardActivity.this, "cancle", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });


        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pwd_PIN_PUK = etInput.getText().toString().trim();
                String newPwdPIN = etInputnew.getText().toString().trim();
                if (!pwd_PIN_PUK.isEmpty()) {
                    Toast.makeText(SimCardActivity.this, "Input content: " + pwd_PIN_PUK, Toast.LENGTH_SHORT).show();


                    boolean bSetPIN = false;
                    try {
                        bSetPIN = mSystemBinderService.setNewPIN(slotIndex ,pwd_PIN_PUK, newPwdPIN   );
                        if ( bSetPIN  ){
                            Toast.makeText(SimCardActivity.this, "The PUK code decoding was successful", Toast.LENGTH_SHORT).show();
                        }else {
                            Toast.makeText(SimCardActivity.this, "Failed to set the PIN code", Toast.LENGTH_SHORT).show();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                } else {
                    Toast.makeText(SimCardActivity.this, "The input cannot be empty", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dialog.show();
    }

    private void refreshSimInfo() {

        if (checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 1);
 // resultTextView.setText(" ");
            LogUtils.e("请授予读取电话状态权限");
            return;
        }

 // SIM
        List<SimCardUtils.SimCardInfo> simCards = SimCardUtils.getSimCardsInfo(this);

        if (simCards.isEmpty()) {
 // resultTextView.setText(" SIM ");
            LogUtils.e("未检测到SIM卡");
            return;
        }


        StringBuilder sb = new StringBuilder();
        for (SimCardUtils.SimCardInfo info : simCards) {
            sb.append("SIM卡 ").append(info.getSlotIndex() + 1).append(":\n");
            sb.append("名称: ").append(info.getDisplayName()).append("\n");
            sb.append("ICCID: ").append(info.getIccId()).append("\n");
            sb.append("PIN剩余次数: ").append(info.getPin1RetryCount() != null ?
                    info.getPin1RetryCount() : "未知").append("\n");
            sb.append("PUK剩余次数: ").append(info.getPuk1RetryCount() != null ?
                    info.getPuk1RetryCount() : "未知").append("\n");
            sb.append("PIN2剩余次数: ").append(info.getPin2RetryCount() != null ?
                    info.getPin2RetryCount() : "未知").append("\n");
            sb.append("PUK2剩余次数: ").append(info.getPuk2RetryCount() != null ?
                    info.getPuk2RetryCount() : "未知").append("\n\n");
        }
        LogUtils.e(sb.toString());
//        resultTextView.setText(sb.toString());

 // Logcat
        SimCardUtils.printSimCardsInfo(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED) {
                refreshSimInfo();
            } else {
 // resultTextView.setText(" , SIM ");
                LogUtils.e("权限被拒绝，无法读取SIM卡信息");
            }
        }
    }


}