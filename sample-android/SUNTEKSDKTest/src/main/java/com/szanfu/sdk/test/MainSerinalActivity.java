package com.szanfu.sdk.test;

import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;

public class MainSerinalActivity extends ListButtonBaseActivity implements View.OnClickListener{
    private TextView tv_011_result,tv_022_result;
    private Button tv_01,tv_02;
    private ISystemBinderService mSystemBinderService;
    private Button btn_finger;
    private EditText ed_input;
    private TextView tv_finger_result;
    private TextView tv_fingerget,tv_inputtest;
    private Button btn_fingerget,btn_finger_test;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_serinal);
        tv_01 = (Button) findViewById(R.id.tv_01);
        tv_02 = (Button)findViewById(R.id.tv_02);
        tv_01.setOnClickListener(this);
        tv_02.setOnClickListener(this);

        tv_011_result = findViewById(R.id.tv_011_result);
        tv_022_result = findViewById(R.id.tv_022_result);
        ed_input = (EditText) findViewById(R.id.ed_input);
        btn_finger = (Button) findViewById(R.id.btn_finger);
        btn_finger.setOnClickListener(this);
        tv_finger_result=findViewById(R.id.tv_finger_result);
        tv_fingerget=(TextView) findViewById(R.id.tv_fingerget);
        btn_fingerget = (Button) findViewById(R.id.btn_fingerget);
        btn_fingerget.setOnClickListener(this);
        tv_inputtest=(TextView) findViewById(R.id.tv_inputtest);
        btn_finger_test=(Button) findViewById(R.id.btn_finger_test);
        btn_finger_test.setOnClickListener(this);

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
           /* case R.id.tv_01:
                try {
                    int ret = mSystemBinderService.setHal5vControl(1);
                    tv_011_result.setText("Turn on 5v power supply"+"ret :"+ret);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.tv_02:
                try {
                    int ret = mSystemBinderService.setHal5vControl(0);
                    tv_022_result.setText("Turn off the 5v power supply"+"ret :"+ret);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_finger_test:
                try {
                    int ret = mSystemBinderService.uartSend(
                            tv_inputtest.getText().toString().trim(),
                            tv_inputtest.getText().toString().trim().length()
                            ,30);
                    tv_finger_result.setText("uartSend Indicates the result of the interface request ： "+ret +"\n");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.btn_finger:
                if (null == ed_input.getText().toString().trim() || ed_input.getText().toString().trim().isEmpty()){
                    Toast.makeText(this, "Please enter content~", Toast.LENGTH_SHORT)
                            .show();
                }else {
                    appendText("uartSend interface request:");
                    try {
                        int ret = mSystemBinderService.uartSend(
                                ed_input.getText().toString().trim(),
                                ed_input.getText().toString().trim().length()
                                ,30);
                        tv_finger_result.setText("Finger Test"+"ret :"+ret +"\n");
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.btn_fingerget:
                try {
//                    String data = "426100050100B6684C1B04";
//                    byte[] bData = HexUtil.hexStringToByte(data);
                    byte[] bData = new byte[1024];
                    String strData = mSystemBinderService.uartReceive(bData,256,1000*2);
                    tv_fingerget.setText("Recived :"+strData);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;*/
        }
    }
}