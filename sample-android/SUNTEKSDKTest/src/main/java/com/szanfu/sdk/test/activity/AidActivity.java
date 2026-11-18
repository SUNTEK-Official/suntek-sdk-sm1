package com.szanfu.sdk.test.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.szanfu.sdk.api.ISdkServiceManager;
import com.szanfu.sdk.api.constant.ServiceID;
import com.szanfu.sdk.api.emv.IEmvBinderService;
import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.base.ListButtonBaseActivity;
import com.szanfu.sdk.test.entity.AidEntity;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.LogUtils;
import com.szanfu.sdk.test.util.WatermarkUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AidActivity extends ListButtonBaseActivity implements View.OnClickListener {
    private TextView tv_addaidresult;
    private Button btn_add_allAID;
    private TextView ed_addAIDresult;
    private Button btn_addAID;
    private TextView tv_delALLAID;
    private Button btn_delALLAID;
    private EditText ed_updateAID;
    private Button btn_add_allAID_offline;
    private TextView tv_addaidresult_offline;
    private Button btnQueryAID;
    private IEmvBinderService mEmvBinderService;
    private Button btnQueryAIDOfline;
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/sdcard/Android/DeviceMSG.txt";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aid);
        tv_addaidresult = findViewById(R.id.tv_addaidresult);
        btn_add_allAID = findViewById(R.id.btn_add_allAID);
        btn_add_allAID.setOnClickListener(this);
        ed_addAIDresult = findViewById(R.id.ed_addAIDresult);
        btn_addAID = findViewById(R.id.btn_addAID);
        btn_addAID.setOnClickListener(this);
        tv_delALLAID = findViewById(R.id.tv_delALLAID);
        btn_delALLAID = findViewById(R.id.btn_delALLAID);
        btn_delALLAID.setOnClickListener(this);


        btnQueryAID = findViewById(R.id.btnQueryAID);
        btnQueryAID.setOnClickListener(this);
        tv_addaidresult_offline = findViewById(R.id.tv_addaidresult_offline);
        btn_add_allAID_offline = findViewById(R.id.btn_add_allAID_offline);
        btn_add_allAID_offline.setOnClickListener(this);
        btnQueryAIDOfline = findViewById(R.id.btnQueryAIDOfline);
        btnQueryAIDOfline.setOnClickListener(this);

//        if (AppConfig.buildType != 0){
            setWaterMask();
//        }
    }

    void setWaterMask(){
        String libVersion="";
 // ( )
        WatermarkUtils.addWatermark(this,
                libVersion,
                Color.BLUE, 24, 0, false, 3);
    }


    private String readFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/Android/DeviceMSG.txt");
        if(!file.exists())
            return "";
        StringBuilder stringBuilder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        LogUtils.e("stringBuilder.toString() :"+stringBuilder.toString());
        return stringBuilder.toString();
    }

    @Override
    public void onServiceConnected(ISdkServiceManager sdkServiceManager) {
        try {
            mEmvBinderService = IEmvBinderService.Stub.asInterface(sdkServiceManager.getService(ServiceID.SERVICE_ID_EMV));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
 // Add all AID AID
            case R.id.btn_add_allAID:
                String strAID =
                        "9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000\n"
                                + "9F0608A000000333010102DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000\n"
                                + "9F0608A000000333010103DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000\n"
                                + "9F0607A00000015230109f090200019f0106010203040506df14039f3704ff01069f02069f0306df13050010000000df1205fce09cf800df1105dc000020009f1c0832303034313231359f1b0400000000df150400000000df170400000000df1604000000009f150207429f160f4d65726368616e74494420202020205f2a0204105f3601009f3c0200009f3d0100df2006999999999999\n"
                                + "9F0606A00000006510DF0101009F09020200DF1105FC6024A800DF1205FC60ACF800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801009F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9F0607A0000000041010DF0101009F09020002DF1105FC5080A000DF1205F85080F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9f0607a00000052410105f3601009f090200649f150212349f1b04000000009f1c0832303034313231359f1e0838333230314943439f3901059f3c0200009f3d01009f4005f000f0a0019f4e04616263649f6604360000009f1d080800800000000000df010100DF1105ffffffffffDF1205ffffffffffDF13050010000000df14039f3704df150400001388df160100df170100df1906000000005000df2006000000200001df2106000000005000df7208f4f0f0faaffe8000df730101df74010f\n"
                                + "9f0605a0000000039f0902008c9f0106010203040506df14039f3704ff01069f02069f0306df13050010000000df1205d84004f800df1105d84000a8009f1c0832303034313231359f1b0400000000df150400000000df170400000000df1604000000009f150207429f160f4d65726368616e74494420202020205f2a0204105f3601009f3c0200009f3d0100df2006999999999999\n"
                                + "9F0607A0000000031010DF0101009F09020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9F0607A0000000032010DF0101009F09020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801009F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9F0607A0000000033010DF0101009F09020140DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801009F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9f0607a0000000038010df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000\n"
                                + "9f0607a0000000032020df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000\n"
                                + "9F0607A0000000043060DF0101009F09020002DF1105FC5058A000DF1205F85058F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9F0606A00000079001DF0101009F09020200DF1105FC6024A800DF1205FC60ACF800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801009F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9f0605a0000000259f090200019f0106010203040506df14039f3704ff01069f02069f0306df13050000000000df1205cc00000000df1105cc000000009f1c0832303034313231359f1b0400000000df150400000000df170400000000df1604000000009f150207429f160f4d65726368616e74494420202020205f3601009f3c0200009f3d0100df2006999999999999\n"
                                + "9F0607A0000003710001DF0101009F09020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9F0607A0000003710002DF010100DF13050010180000DF1205D84004F800DF1105D84000A800DF14039F3704DF150400000000DF160100DF1701009F1B0400000000DF1801019F080200209F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                + "9f0607a0000006581010df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000df5206000000100000df5106000000500000ff01050000000000ff0205ffffffffffff03050000000000ff0402575a9f1a020643df560103df5501c8df5406000009999900df5306000009999900\n"
                                + "9f0607a0000006582010df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000df5206000000100000df5106000000500000ff01050000000000ff0205ffffffffffff03050000000000ff0402575a9f1a020643df560103df5501c8df5406000009999900df5306000009999900\n"
                                + "9F0607A0000001544442DF010100DF13050010180000DF1205D84004F800DF1105D84000A800DF14039F3704DF150400000000DF160100DF1701009F1B0400000000DF1801019F080200209F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"

                                + "9F0607A0000000651010DF010100DF13050010180000DF1205D84004F800DF1105D84000A800DF14039F3704DF150400000000DF160100DF1701009F1B0400000000DF1801019F080200209F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n" // master
                                + "9F0607D3600000010000DF010100DF13050010180000DF1205D84004F800DF1105D84000A800DF14039F3704DF150400000000DF160100DF1701009F1B0400000000DF1801019F080200209F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"

                                + "9F0608A000000333010106DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000\n"

                                +"9F0606A00000079001DF0101009F09020030DF1105D84000A800DF1205D84004F800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801009F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n" // 2025.03.10 1.4.4 1.4.7 AID
                                +"9f0605a0000001529f090200019f0106010203040506df14039f3704ff01069f02069f0306df13050000000000df1205fce09cf800df1105dc000020009f1c0832303034313231359f1b0400000000df150400000000df170400000000df1604000000009f150207429f160f4d65726368616e74494420202020205f2a0204105f3601009f3c0200009f3d0100df2006999999999999\n"
                                +"9F0605A000000004DF0101009F09020002DF1105FC5080A000DF1205F85080F800DF130504000000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                                +"9f0607a00000052410105f3601009f090200649f150212349f1b04000000009f1c0832303034313231359f1e0838333230314943439f3901059f3c0200009f3d01009f4005f000f0a0019f4e04616263649f6604360000009f1d080800800000000000df010100DF1105ffffffffffDF1205ffffffffffDF13050000000000df14039f3704df150400001388df160100df170100df1906000000005000df2006000000200001df2106000000005000df7208f4f0f0faaffe8000df730101df74010f\n"
                                +"9f0605a0000000039f0902008c9f0106010203040506df14039f3704ff01069f02069f0306df13050000000000df1205d84004f800df1105d84000a8009f1c0832303034313231359f1b0400000000df150400000000df170400000000df1604000000009f150207429f160f4d65726368616e74494420202020205f2a0204105f3601009f3c0200009f3d0100df2006999999999999\n"
                                +"9f0607a0000000031010df0101009f08020020df1105d84000a800df1205d84004f800df130500000000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000\n"
                                +"9f0607a0000000032010df0101009f08020020df1105d84000a800df1205d84004f800df130500000000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000\n"
                                +"9f0607a0000000032020df0101009f08020020df1105d84000a800df1205d84004f800df130500000000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000\n"
                                +"9f0607a0000000038010df0101009f08020020df1105d84000a800df1205d84004f800df130500000000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000\n"
                                +"9f0605a000000658df0101009f08020020df1105d84000a800df1205d84004f800df130500100000009f1b0400000001df150400000000df160199df170199df14039f3704df1801019f7b06000000100000df1906000000100000df2006000000100000df2106000000100000df5206000000100000df5106000000500000ff01050000000000ff0205ffffffffffff03050000000000ff0402575a9f1a020643df560103df5501c8df5406000009999900df5306000009999900\n"
                                +"9f0607a00000037100019f090200019f0106010203040506df14039f3704ff01069f02069f0306df13050000000000df1205cc00000000df1105cc000000009f1c0832303034313231359f1b0400000000df150400000000df170400000000df1604000000009f150207429f160f4d65726368616e74494420202020205f3601009f3c0200009f3d0100df2006999999999999\n"
                                +"9F0605A000000371DF0101009F09020002DF1105DC4000A800DF1205DC4004F800DF130500000000009F1B0400004E20DF1504000000009F1D080000000000000000DF160100DF170100DF14039F3704DF1801319F7B06000000200000DF1906000000000000DF2006999999999999DF2106000000000000970F9F02065F2A029A039C0195059F3704DF840801089F3303E0C9C8\n"
                                +"9F0607A0000000250104DF0101009F09020001DF1105DC50FC9800DF1205DE00FC9800DF130500100000009F1B0400000000DF150400000000DF160199DF170199DF14039F3704DF1801009F7B06000000100000DF1906000000100000DF2006000999999999DF2106000000100000\n"
                        ;




                List<String> aidList = new ArrayList<>(Arrays.asList(strAID.split("\n")));
                Log.e("szaf", "onClick aidList.size(): "+aidList.size() );
                try {
                    String strpackageName = getPackageName();
                    int ret =mEmvBinderService.saveAid(aidList, strpackageName);
                    if (ret == 0){
                        tv_addaidresult.setText("save AID  is success !");
                    }else {
                        tv_addaidresult.setText("save AID is ERROR");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.e("szaf", "onClick: " + aidList);
                break;

            case R.id.btn_add_allAID_offline:
                String strAIDoffline =
                        "9F0608A000000333010101DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000\n"
                                + "9F0608A000000333010102DF0101009F08020020DF1105D84000A800DF1205D84004F800DF130500100000009F1B040000C350DF150400000000DF160199DF170199DF14039F3704DF1801019F7B06100000000000DF1906100000000000DF2006100000000000DF2106100000000000\n"
                                ;
                List<String> aidListOffline = new ArrayList<>(Arrays.asList(strAIDoffline.split("\n")));
                Log.e("szaf", "onClick aidList.size(): "+aidListOffline.size() );
                try {
                    String strpackageName = getPackageName();
                    int ret =mEmvBinderService.saveAid(aidListOffline, "com.hjq.demo");
                    if (ret == 0){
                        tv_addaidresult_offline.setText("save AID  is success !");
                    }else {
                        tv_addaidresult_offline.setText("save AID is ERROR");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                Log.e("szaf", "onClick: " + aidListOffline);
                break;
 // Delete all AID AID
            case R.id.btn_delALLAID:
                try {
                    int ret = mEmvBinderService.delAllAID(getPackageName());
                    if (ret == 0 ){
                        tv_delALLAID.setText("del the allAID is success!!");
                    }else {
                        tv_delALLAID.setText("del the allAID is error!");
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
 // Example Query a single AID AID
            case R.id.btnQueryAID:
                try {
                    String packageName = getPackageName();
                    String getStrAID = mEmvBinderService.queryAID(packageName);
                    if (TextUtils.isEmpty(getStrAID)){
                        Toast.makeText(AidActivity.this, "AID is not written, please write first", Toast.LENGTH_SHORT)
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
                                    int ret = mEmvBinderService.delAID(getPackageName(),serviceItemName[position]);
                                    if (ret == 0){
                                        Toast.makeText(AidActivity.this, "del AID："+serviceItemName[position]+"success", Toast.LENGTH_SHORT)
                                                .show();
                                    }else {
                                        Toast.makeText(AidActivity.this, "del AID："+serviceItemName[position]+"ERROR", Toast.LENGTH_SHORT)
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
                break;



            case R.id.btnQueryAIDOfline:
                try {
                    String packageName = getPackageName();
                    String getStrAID = mEmvBinderService.queryAID("com.hjq.demo");
                    if (TextUtils.isEmpty(getStrAID)){
                        Toast.makeText(AidActivity.this, "AID is not written, please write first", Toast.LENGTH_SHORT)
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
                                    int ret = mEmvBinderService.delAID(getPackageName(),serviceItemName[position]);
                                    if (ret == 0){
                                        Toast.makeText(AidActivity.this, "del AID："+serviceItemName[position]+"success", Toast.LENGTH_SHORT)
                                                .show();
                                    }else {
                                        Toast.makeText(AidActivity.this, "del AID："+serviceItemName[position]+"ERROR", Toast.LENGTH_SHORT)
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
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        WatermarkUtils.removeWatermark(this);
    }
}
