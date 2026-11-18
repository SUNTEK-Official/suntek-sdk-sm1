package com.szanfu.sdk.test.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.system.ISystemBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.adapter.StringAdapter;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.base.ResultAdapter;
import com.szanfu.sdk.test.bean.ItemResultBean;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.SimCardUtils;

import java.util.ArrayList;
import java.util.List;

public class ApnActivity extends ListButtonBaseActivity implements View.OnClickListener {
    private ISystemBinderService mSystemBinderService;
    private TextView btn_get_apns, btn_add_apn_sim1, btn_add_apn_sim2,btn_set_default_apn,btn_clear_log,log_text_view,btn_get_default_apns;
    private RecyclerView recyclerView;
    private StringAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apnset);
        initView();
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mSystemBinderService = ISystemBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_SYSTEM));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    void initView(){
        btn_get_apns = findViewById(R.id.btn_get_apns);
        btn_get_apns.setOnClickListener(this);
        btn_get_default_apns = findViewById(R.id.btn_get_default_apns);
        btn_get_default_apns.setOnClickListener(this);
        btn_add_apn_sim1 = findViewById(R.id.btn_add_apn_sim1);
        btn_add_apn_sim1.setOnClickListener(this);
        btn_add_apn_sim2 = findViewById(R.id.btn_add_apn_sim2);
        btn_add_apn_sim2.setOnClickListener(this);
        btn_set_default_apn = findViewById(R.id.btn_set_default_apn);
        btn_set_default_apn.setOnClickListener(this);
        btn_clear_log = findViewById(R.id.btn_clear_log);
        btn_clear_log.setOnClickListener(this);
        log_text_view =findViewById(R.id.log_text_view);
 // RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    public void onClick(View v) {
            switch (v.getId()){
 // APN
                case R.id.btn_get_apns:

                    try {
                        List<String> listDataAPN = mSystemBinderService.listDataQueryAllAPNS();
                        StringBuilder sbAPN = new StringBuilder();
                        LogUtils.e("listDataAPN : "+listDataAPN.size()  );
                        for (int i = 0; i <  listDataAPN.size(); i++) {
                            LogUtils.e(" listDataAPN :"+listDataAPN.get(i).toString());
                            sbAPN.append( listDataAPN.get(i).toString() );
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        log_text_view.setText(sbAPN.toString());

                                        adapter = new StringAdapter(listDataAPN);
                                        recyclerView.setAdapter(adapter);
                                    }
                                });
                            }
                        }).start();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    break;
                case R.id.btn_get_default_apns:
                    StringBuilder sbAPN = new StringBuilder();
                    try {
                        String[] arrayAPN =  mSystemBinderService.queryDefaultApns();
                        for (int i = 0; i < arrayAPN.length; i++) {
                            LogUtils.e("arrayAPN : " + arrayAPN[i]);
                            sbAPN.append(arrayAPN[i]+";\n" );
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_text_view.setText( "GetAPNS : \n"+sbAPN.toString()  );
                        }
                    });
                    break;
                case R.id.btn_add_apn_sim1:
                    /**
                     *         public String name;
                     *         public String apn;
                     *         public String proxy;
                     *         public String port;
                     *         public String user;
                     *         public String password;
                     *         public String server;
                     *         public String mmsc;
                     *         public String mmsProxy;
                     *         public String mmsPort;
                     *         public String mcc;
                     *         public String mnc;
                     *         public String type;
                     *         public int current;
                     */

                    StringBuilder sbSIM1APNS = new StringBuilder();

                    String[] arrayAPN = new String[15];
                    arrayAPN[0] = "SIM1 APN";
                    arrayAPN[1] = "sim1.example.com";
                    arrayAPN[2] = "2";
                    arrayAPN[3] = "3";
                    arrayAPN[4] = "4";
                    arrayAPN[5] = "5";
                    arrayAPN[6] = "6";
                    arrayAPN[7] = "7";
                    arrayAPN[8] = "8";
                    arrayAPN[9] = "9";
                    arrayAPN[10] = "10";
                    arrayAPN[11] = "11";
                    arrayAPN[12] = "12";
                    arrayAPN[13] = "13";
//                    arrayAPN[14] = "12";



                    sbSIM1APNS.append("name :"+ arrayAPN[0] +";\n");
                    sbSIM1APNS.append("apn :"+ arrayAPN[1] +";\n");
                    sbSIM1APNS.append("apn :"+ arrayAPN[2] +";\n");
                    sbSIM1APNS.append("proxy :"+ arrayAPN[3] +";\n");
                    sbSIM1APNS.append("port :"+ arrayAPN[4] +";\n");
                    sbSIM1APNS.append("user :"+ arrayAPN[5] +";\n");
                    sbSIM1APNS.append("password :"+ arrayAPN[6] +";\n");
                    sbSIM1APNS.append("server :"+ arrayAPN[7] +";\n");
                    sbSIM1APNS.append("mmsc :"+ arrayAPN[8] +";\n");
                    sbSIM1APNS.append("mmsProxy :"+ arrayAPN[9] +";\n");
                    sbSIM1APNS.append("mmsPort :"+ arrayAPN[10]+";\n" );
                    sbSIM1APNS.append("mcc :"+ arrayAPN[11]+";\n" );
                    sbSIM1APNS.append("mnc :"+ arrayAPN[12] +";\n");
                    sbSIM1APNS.append("type :"+ arrayAPN[13] +";\n");

                    try {
                        mSystemBinderService.insertApnForSimValue(0,arrayAPN);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_text_view.setText( sbSIM1APNS.toString()  );
                        }
                    });

                    break;
                case R.id. btn_add_apn_sim2:
                    /**
                     *         public String name;
                     *         public String apn;
                     *         public String proxy;
                     *         public String port;
                     *         public String user;
                     *         public String password;
                     *         public String server;
                     *         public String mmsc;
                     *         public String mmsProxy;
                     *         public String mmsPort;
                     *         public String mcc;
                     *         public String mnc;
                     *         public String type;
                     *         public int current;
                     */
                    StringBuilder sbSIM2APNS = new StringBuilder();
                    String[] arrayAPN2 = new String[15];
                    arrayAPN2[0] = "SIM2 APN";
                    arrayAPN2[1] = "sim2.example.com";
                    arrayAPN2[2] = "22";
                    arrayAPN2[3] = "23";
                    arrayAPN2[4] = "24";
                    arrayAPN2[5] = "25";
                    arrayAPN2[6] = "26";
                    arrayAPN2[7] = "27";
                    arrayAPN2[8] = "28";
                    arrayAPN2[9] = "29";
                    arrayAPN2[10] = "210";
                    arrayAPN2[11] = "211";
                    arrayAPN2[12] = "212";
                    arrayAPN2[13] = "213";
//                    arrayAPN2[14] = "212";


                    sbSIM2APNS.append("name :"+ arrayAPN2[0] +";\n");
                    sbSIM2APNS.append("apn :"+ arrayAPN2[1] +";\n");
                    sbSIM2APNS.append("apn :"+ arrayAPN2[2] +";\n");
                    sbSIM2APNS.append("proxy :"+ arrayAPN2[3] +";\n");
                    sbSIM2APNS.append("port :"+ arrayAPN2[4] +";\n");
                    sbSIM2APNS.append("user :"+ arrayAPN2[5] +";\n");
                    sbSIM2APNS.append("password :"+ arrayAPN2[6] +";\n");
                    sbSIM2APNS.append("server :"+ arrayAPN2[7] +";\n");
                    sbSIM2APNS.append("mmsc :"+ arrayAPN2[8] +";\n");
                    sbSIM2APNS.append("mmsProxy :"+ arrayAPN2[9] +";\n");
                    sbSIM2APNS.append("mmsPort :"+ arrayAPN2[10] +";\n");
                    sbSIM2APNS.append("mcc :"+ arrayAPN2[11] +";\n");
                    sbSIM2APNS.append("mnc :"+ arrayAPN2[12] +";\n");
                    sbSIM2APNS.append("type :"+ arrayAPN2[13] +";\n");

                    try {
                        mSystemBinderService.insertApnForSimValue(1,arrayAPN2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_text_view.setText(  "Set SIM2 APN : \n"+
                                    sbSIM2APNS.toString() );
                        }
                    });
                    break;
                case R.id.btn_set_default_apn:
                    long apnID = 3645;
                    try {
                        boolean bSetDefaultAPN =  mSystemBinderService.setDefaultApn(apnID);
                        String strSetDefault ="";
                        if ( bSetDefaultAPN ){
                            strSetDefault="Setting APN to the default value was successful";
                            log_text_view.setText(  strSetDefault );
                        }else {
                            strSetDefault="Setting APN to the default value failed ";
                            log_text_view.setText(  strSetDefault );
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                case R.id.btn_clear_log:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log_text_view.setText("");
                            List<String> listDataAPN = new ArrayList<>();
                            adapter = new StringAdapter(listDataAPN);
                            recyclerView.setAdapter(adapter);
                        }
                    });
                    break;
            }
    }

}
