package com.szanfu.sdk.test.activity.scans;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ListView;

import com.imagealgorithmlab.barcode.DecodeEngine;
import com.imagealgorithmlab.barcode.SymbologySettingItem;
import com.szanfu.sdk.test.R;

import java.util.Arrays;

/**
 * 设置界面
 */

public class SettingActivity extends Activity {


    private ListView myListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        initView();
        initData();
    }

    /**
     * 初始化控件
     */
    private void initView(){
        myListView = (ListView) findViewById(R.id.myListView);

    }

    /**
     * 初始化数据
     */
    private void initData(){

        SymbologySettingItem[] items = DecodeEngine.GetCodeTypeList();

        SettingAdapter adapter = new SettingAdapter(this, Arrays.asList(items));

        myListView.setAdapter(adapter);
    }

}
