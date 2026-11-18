package com.szanfu.sdk.test.base;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.szanfu.sdk.test.R;
import com.szanfu.sdk.test.bean.ItemResultBean;

import java.util.List;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.MyViewHolder> {


    private List<ItemResultBean> data;
    private Context context;

    public ResultAdapter(List<ItemResultBean> data, Context context) {
        this.data = data;
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View  view=View.inflate(context, R.layout.recyclerview_result_item,null);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            holder.tv.setText(data.get(position).getName());
            if (1 == data.get(position).getiResultLevel()){
                holder.tv.setTextColor(Color.RED);
            }else if ( 2 == data.get(position).getiResultLevel() ){
                holder.tv.setTextColor(Color.BLUE);
            }else if (3 == data.get(position).getiResultLevel()){
                holder.tv.setTextColor(Color.BLACK);
            }
    }

    @Override
    public int getItemCount() {
        return data == null ? 0: data.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView tv;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);

            tv=itemView.findViewById(R.id.tv_btn);
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

 // recycler

    private OnRecyclerItemClickClicklistener   mOnRecyclerItemClickClicklistener;
    public void setmOnRecyclerItemClickClicklistener(OnRecyclerItemClickClicklistener listener){
        mOnRecyclerItemClickClicklistener=listener;
    }

    public interface OnRecyclerItemClickClicklistener{
    void  onRecyclerItemClickListener(int position);
    }

}
