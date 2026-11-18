package com.szanfu.sdk.test.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.emv.IEmvBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.entity.AidEntity;
import com.szanfu.sdk.test.util.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AidListActivity extends ListButtonBaseActivity {
    private static final String TAG = "AidInterfaceActivity";
    String packageName="";
    int cardType = -1;

    private String[] mButtons;
    private IEmvBinderService mEmvBinderService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid_list);
        Intent intent = getIntent();
        packageName = intent.getStringExtra("packageName");
        cardType = intent.getIntExtra("cardType",-1);

    }

    private void initViewAID(String packageName) {
        try {
            String getStrAID = mEmvBinderService.queryAID(packageName);
            LogUtils.e("serviceItemName : getStrAID "+getStrAID);
            if (TextUtils.isEmpty(getStrAID)){
                Toast.makeText(AidListActivity.this, "AID is not written, please write first", Toast.LENGTH_SHORT)
                        .show();
            }else {
                Gson gson = new Gson();
                List<AidEntity> studentInfoList = gson.fromJson(getStrAID,
                        new TypeToken<List<AidEntity>>() {}.getType());
                String[] serviceItemName = new String[studentInfoList.size()];
                for (int i = 0; i < studentInfoList.size(); i++) {
                    AidEntity entity = studentInfoList.get(i);
                    String aid = entity.getTag9F06();
                    int cardType = entity.getCardType();
                    LogUtils.e("serviceItemName : cardType"+cardType);

                    if (cardType != 0) {
                        serviceItemName[i] = "cardType：" + cardType + "；" + aid;
                        LogUtils.e("serviceItemName : "+serviceItemName[i]);
                    } else {
                        serviceItemName[i] = aid;
                        LogUtils.e("serviceItemName : "+serviceItemName[i]);
                    }
                }
                LogUtils.e("serviceItemName : "+serviceItemName);
                ListView testListView = (ListView) findViewById(R.id.lv_main);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_expandable_list_item_1, serviceItemName);
                testListView.setAdapter(adapter);
                testListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 // todo AID
                        try {
                            int ret = mEmvBinderService.delAID(getPackageName(),serviceItemName[position]);

                            if (ret == 0){
                                Toast.makeText(AidListActivity.this, "del AID："+serviceItemName[position]+"success", Toast.LENGTH_SHORT)
                                        .show();
                                adapter.notifyDataSetChanged();
                            }else {
                                Toast.makeText(AidListActivity.this, "del AID："+serviceItemName[position]+"ERROR", Toast.LENGTH_SHORT)
                                        .show();
                            }
                            finish();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    void queryAidMsg(int cardType,String packageName){
        try {
            String getStrAID ="";
            if (cardType == 7){
                getStrAID = mEmvBinderService.queryAID(packageName);
            }else {
                getStrAID = mEmvBinderService.queryAIDMsg(cardType,packageName);
            }
            LogUtils.e("getStrAID ： "+getStrAID);
            if (TextUtils.isEmpty(getStrAID)){
                Toast.makeText(AidListActivity.this, "AID is not written, please write first", Toast.LENGTH_SHORT)
                        .show();
            }else {
                Gson gson = new Gson();
                List<AidEntity> studentInfoList = gson.fromJson(getStrAID,
                        new TypeToken<List<AidEntity>>() {}.getType());
                String[] serviceItemName = new String[studentInfoList.size()];
                for (int i = 0; i < studentInfoList.size(); i++) {
                    serviceItemName[i] = studentInfoList.get(i).getTag9F06();
                }
                ListView testListView = (ListView) findViewById(R.id.lv_main);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_expandable_list_item_1, serviceItemName);
                testListView.setAdapter(adapter);
                testListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 // todo AID
                        try {
                            int ret = -1;
                            if (cardType == 7){
                                 ret = mEmvBinderService.delAID(getPackageName(),serviceItemName[position]);
                            }else {
                                 ret = mEmvBinderService.delAIDMsg(cardType,getPackageName(),serviceItemName[position]);
                            }

                            if (ret == 0){
                                Toast.makeText(AidListActivity.this, "del AID："+serviceItemName[position]+"success", Toast.LENGTH_SHORT)
                                        .show();
                                adapter.notifyDataSetChanged();
                            }else {
                                Toast.makeText(AidListActivity.this, "del AID："+serviceItemName[position]+"ERROR", Toast.LENGTH_SHORT)
                                        .show();
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            Log.e(TAG, "onServiceConnected: ");
            mEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
            if (cardType == 2
                || cardType == 4
                ){
                queryAidMsg(cardType,packageName);
            }else if (cardType == 7){
                initViewAID(packageName);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onItemClick(int position, String itemName) {
        super.onItemClick(position, itemName);

        switch (position) {

        }
    }

}
