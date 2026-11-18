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
import com.szanfu.sdk.test.entity.CapkEntity;
import com.szanfu.sdk.test.util.LogUtils;

import java.util.List;

public class CapkListActivity extends ListButtonBaseActivity {
    String packageName="";

    private IEmvBinderService mEmvBinderService;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid_list);
        Intent intent = getIntent();
        packageName = intent.getStringExtra("packageName");
    }

    private void initViewCAPK(String packageName) {
        try {
            String getStrCapk = mEmvBinderService.queryCapk(packageName);
            if (TextUtils.isEmpty(getStrCapk)){
                Toast.makeText(CapkListActivity.this, "Capk is not written. Please write it first", Toast.LENGTH_SHORT)
                        .show();
            }else {
                Gson gson = new Gson();
                List<CapkEntity> studentInfoList = gson.fromJson(getStrCapk,
                        new TypeToken<List<CapkEntity>>() {}.getType());
                String[] serviceItemName = new String[studentInfoList.size()];
                for (int i = 0; i < studentInfoList.size(); i++) {
                    StringBuffer stringBuffer = new StringBuffer();
                    stringBuffer.append(studentInfoList.get(i).getTag9F06()+","+studentInfoList.get(i).getTag9F22());
                    serviceItemName[i] = stringBuffer.toString() ;
                }
                ListView testListView = (ListView) findViewById(R.id.lv_main);
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                        android.R.layout.simple_expandable_list_item_1, serviceItemName);
                testListView.setAdapter(adapter);
                testListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 // todo CAPK
                        try {
                            String ssss= serviceItemName[position];
                            String[] capkValue=ssss.split(",");
                            int ret = mEmvBinderService.delCapk(getPackageName(),capkValue[0],capkValue[1]);
                            if (ret == 0 ){
                                Toast.makeText(CapkListActivity.this, "del Capk："+serviceItemName[position]+"success!", Toast.LENGTH_SHORT)
                                        .show();
                                adapter.notifyDataSetChanged();
                            }else {
                                Toast.makeText(CapkListActivity.this, "del Capk："+serviceItemName[position]+"error!", Toast.LENGTH_SHORT)
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
    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            LogUtils.e( "onServiceConnected: ");
            mEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
            initViewCAPK(packageName);
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
