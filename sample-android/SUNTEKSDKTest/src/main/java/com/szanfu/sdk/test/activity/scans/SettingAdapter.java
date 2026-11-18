package com.szanfu.sdk.test.activity.scans;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.imagealgorithmlab.barcode.DecodeEngine;
import com.imagealgorithmlab.barcode.SymbologySettingItem;
import com.szanfu.sdk.test.R;

import java.util.List;

/**
 * 条码设置填充类
 */

public class SettingAdapter extends BaseAdapter {

    private Context ctx;
    private LayoutInflater mInflater;
    private List<SymbologySettingItem> data;

    public SettingAdapter(Context ctx,List<SymbologySettingItem> data){
        mInflater = (LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.ctx = ctx;
        this.data = data;
    }
    @Override
    public int getCount() {
        return data.size();
    }


    @Override
    public Object getItem(int position) {
        return data.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem, null);
            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.codeName);
            holder.myCheckBox = (CheckBox) convertView.findViewById(R.id.myCheckBox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        final SymbologySettingItem symbology = data.get(position);
        holder.textView.setText(symbology.getName());
        if (symbology.getValue() == 1){
            holder.myCheckBox.setChecked(true);
        }else {
            holder.myCheckBox.setChecked(false);
        }

        holder.myCheckBox.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                boolean b = cb.isChecked();

               boolean result =  DecodeEngine.setCodeTypeOnAndOff(symbology.getName(),b?1:0);

                if (!result){
                    cb.setChecked(!b);
                    Toast.makeText(ctx,"更新失败",Toast.LENGTH_LONG).show();
                }else {
                    symbology.setValue(b?1:0);
                }

            }
        });
        return convertView;
    }

    public static class ViewHolder {
        public TextView textView;
        public CheckBox myCheckBox;
    }
}
