package com.szanfu.sdk.test;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;


import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.szanfu.sdk.test.activity.AidInterfaceActivity;
import com.szanfu.sdk.test.activity.ApnActivity;
import com.szanfu.sdk.test.activity.CapkInterfaceActivity;
import com.szanfu.sdk.test.activity.CardServiceActivity;
import com.szanfu.sdk.test.activity.CmacTestActivity;
import com.szanfu.sdk.test.activity.DUKPTActivity_final;
import com.szanfu.sdk.test.activity.EmvKernelServiceActivity;
import com.szanfu.sdk.test.activity.LCDSecondaryDisplayScreenActivity;
import com.szanfu.sdk.test.activity.MTKSimCardManagerActivity;
import com.szanfu.sdk.test.activity.PermisionActivity;
import com.szanfu.sdk.test.activity.PhysicalKeyboardActivity;
import com.szanfu.sdk.test.activity.PressureActivity;
import com.szanfu.sdk.test.activity.PinPadServiceActivity;
import com.szanfu.sdk.test.activity.PrinterServiceActivity;
import com.szanfu.sdk.test.activity.SSLServiceActivity;
import com.szanfu.sdk.test.activity.SamCardServiceActivity;
import com.szanfu.sdk.test.activity.SystemServiceActivity;
import com.szanfu.sdk.test.activity.SystemServiceMtkActivity;
import com.szanfu.sdk.test.activity.TR31Activity;
import com.szanfu.sdk.test.activity.VoiceRecognitionActivity;
import com.szanfu.sdk.test.activity.qrscancode.QRScanDivActivity;
import com.szanfu.sdk.test.adapter.MainAdapter;
import com.szanfu.sdk.test.bean.ItemMainBean;
import com.szanfu.sdk.test.util.AppConfig;
import com.szanfu.sdk.test.util.WatermarkUtils;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * 水印重复填满
 */
public class MainActivity extends Activity {
    private String[] serviceItemName;
    RecyclerView rv_main;
    MainAdapter mainAdapter;
    List<ItemMainBean>  listMainData = new ArrayList<>();
    private final Class[] serviceItemClass = new Class[]{
            SystemServiceActivity.class,
            CardServiceActivity.class,
            PrinterServiceActivity.class,
            PinPadServiceActivity.class,
            QRScanDivActivity.class,
            SamCardServiceActivity.class,
            TR31Activity.class,
            DUKPTActivity_final.class,
            PressureActivity.class
    };

    private final Class[] serviceMtkItemClass = new Class[]{
            SystemServiceActivity.class,
            LCDSecondaryDisplayScreenActivity.class,
            CardServiceActivity.class,
            PrinterServiceActivity.class,
            PinPadServiceActivity.class,
            QRScanDivActivity.class,
            SamCardServiceActivity.class,
            SystemServiceMtkActivity.class,
            EmvKernelServiceActivity.class,
            AidInterfaceActivity.class,
            CapkInterfaceActivity.class,
            TR31Activity.class,
            DUKPTActivity_final.class,
            PressureActivity.class,
            SSLServiceActivity.class,
            PermisionActivity.class,
            CmacTestActivity.class,
            PhysicalKeyboardActivity.class,
            VoiceRecognitionActivity.class,
            MTKSimCardManagerActivity.class,
            ApnActivity.class,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (AppConfig.buildType == 0) { // =0 =1
            serviceItemName = getResources().getStringArray(R.array.SERVICE_LIST);
        } else { // =0 =1
            if (0 == AppConfig.buildPlatformType){

                serviceItemName = getResources().getStringArray(R.array.ALL_SERVICE_LIST);
            }else if (1 == AppConfig.buildPlatformType){
//                MTK
                serviceItemName = getResources().getStringArray(R.array.ALL_MTK_SERVICE_LIST);
            }
        }

        rv_main = findViewById(R.id.rv_main);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rv_main.setLayoutManager(linearLayoutManager);
        for (int i = 0; i < serviceItemName.length; i++) {
            ItemMainBean itemMainBean = new ItemMainBean();
            String strName = serviceItemName[i];
            itemMainBean.setName(strName);
            listMainData.add(itemMainBean);
        }
        mainAdapter = new MainAdapter(listMainData ,this);
        rv_main.setAdapter(mainAdapter);
        mainAdapter.setmOnRecyclerItemClickClicklistener(new MainAdapter.OnRecyclerItemClickClicklistener() {
            @Override
            public void onRecyclerItemClickListener(int position) {
                Intent intent = new Intent();
                intent.putExtra("title", serviceItemName[position]);

                if (0 == AppConfig.buildPlatformType){

                    if (position >= serviceItemClass.length) {
                        Toast.makeText(MainActivity.this, "unrealized", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    intent.setClass(MainActivity.this, serviceItemClass[position]);
                }else if (1 == AppConfig.buildPlatformType){
 // MTK
                    if (position >= serviceMtkItemClass.length) {
                        Toast.makeText(MainActivity.this, "unrealized", Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }
                    intent.setClass(MainActivity.this, serviceMtkItemClass[position]);
                }
                startActivity(intent);
            }
        });
 // ( )
        WatermarkUtils.addWatermark(this, "", Color.BLUE, 40, 0, false, 3);
        LitePal.initialize(this);
    }
}