package com.example.bms.history;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bms.DataProcess;
import com.example.bms.R;
import com.example.bms.other.ToastUtil;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>{
    private static final String TAG = "RecyclerViewAdapter";
    public Context mContext;
    public ArrayList<List<String>> mList;
    public byte TNUM = DataProcess.TempNum;
    public RecyclerViewAdapter(Context context, ArrayList<List<String>> list){
        mContext = context;
        mList = list;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View viewHolder = LayoutInflater.from(mContext).inflate(R.layout.recycler_view_item,parent,false);
        Log.e(TAG, ToastUtil.currentTime()+"onCreateViewHolder: ");
        return new MyViewHolder(viewHolder);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Log.e(TAG, "onBindViewHolder: position   "+position);
        Log.e(TAG, ToastUtil.currentTime()+"onBindViewHolder: ");
        holder.textView_soc.setText(mList.get(position).get(0));
        holder.textView_MaxV.setText(mList.get(position).get(1));
        holder.textView_MinV.setText(mList.get(position).get(2));
        holder.textView_C.setText(mList.get(position).get(3));
        holder.textView_fault.setText(mList.get(position).get(4));
        holder.textView_T1.setText(mList.get(position).get(5));
        if(TNUM == 2)
            holder.textView_T2.setText(mList.get(position).get(6));
        if(TNUM == 3)
            holder.textView_T3.setText(mList.get(position).get(7));
    }
    @Override
    public int getItemCount() {
        return mList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder{
        private final TextView textView_soc;
        private final TextView textView_MaxV;
        private final TextView textView_MinV;
        private final TextView textView_C;
        private final TextView textView_T1;
        private final TextView textView_T2;
        private final TextView textView_T3;
        private final TextView textView_fault;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textView_soc = itemView.findViewById(R.id.recyclerView_item_soc);
            textView_MaxV = itemView.findViewById(R.id.recyclerView_item_MaxV);
            textView_MinV = itemView.findViewById(R.id.recyclerView_item_MinV);
            textView_C = itemView.findViewById(R.id.recyclerView_item_C);
            textView_fault = itemView.findViewById(R.id.recyclerView_item_fault);
            textView_T1 = itemView.findViewById(R.id.recyclerView_item_T1);
            textView_T2 = itemView.findViewById(R.id.recyclerView_item_T2);
            textView_T3 = itemView.findViewById(R.id.recyclerView_item_T3);
            //判断温度点数
            if(TNUM == 2){
                textView_T2.setVisibility(View.VISIBLE);
            }else if(TNUM == 3){
                textView_T3.setVisibility(View.VISIBLE);
            }
        }
    }
}
