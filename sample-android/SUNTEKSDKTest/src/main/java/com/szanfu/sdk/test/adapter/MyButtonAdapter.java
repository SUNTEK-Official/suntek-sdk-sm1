package com.szanfu.sdk.test.adapter;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.bean.ItemButtonBean;

import java.util.List;

public class MyButtonAdapter extends RecyclerView.Adapter<MyButtonAdapter.MyViewHolder> {
    private boolean isClickEnabled = false;

    private List<ItemButtonBean> data;
    private Context context;


//    public void setClickEnabled(boolean enabled) {
//        isClickEnabled = enabled;
//    }
//    public boolean getClickEnabled() {
//        return isClickEnabled ;
//    }

    public MyButtonAdapter(List<ItemButtonBean> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=View.inflate(context, R.layout.recyclerview_btn_item,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
        holder.tv_btn.setText(data.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return data == null ? 0: data.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_btn;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_btn=itemView.findViewById(R.id.tv_btn);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnRecyclerItemClickClicklistener != null){
                        mOnRecyclerItemClickClicklistener.onRecyclerItemClickListener(getAdapterPosition());
                    }
                }
            });
        }
    }


    public void updateDataSet(List<ItemButtonBean> newDataSet) {
        data = newDataSet;
        notifyDataSetChanged(); // RecyclerView
    }


 // recycler

    private OnRecyclerItemClickClicklistener   mOnRecyclerItemClickClicklistener;
    public void setmOnRecyclerItemClickClicklistener(OnRecyclerItemClickClicklistener listener){
        mOnRecyclerItemClickClicklistener=listener;
    }

    public interface OnRecyclerItemClickClicklistener{
        void  onRecyclerItemClickListener(int position);
    }

}
