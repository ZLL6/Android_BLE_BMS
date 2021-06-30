package com.example.bms.history;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bms.BLE.BLEFragment;
import com.example.bms.DataProcess;
import com.example.bms.R;
import com.example.bms.other.ToastUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.example.bms.R.string.historyReadErr;

public class HistoryQueryFragment extends Fragment{
    private static final String TAG = "HistoryQueryFragment";
    private View view;
    private RecyclerView mRecyclerView;
    private Button mReadBt;
    private Button mSetSaveTime;
    private EditText mLoopEt;
    private static ProgressBar mProgress;
    private MyBoradcastReceiver receiver = new MyBoradcastReceiver();
    public static RecyclerViewAdapter adapter;

    public static int frameCnt = 0;
    public static volatile ArrayList<String> mList = new ArrayList<String>();
    public static volatile ArrayList<List<String>> mListList = new ArrayList<>();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.history,container,false);
        initView();
        return view;
    }
    /******************************************

   return ：
    *******************************************/
    @SuppressLint("HandlerLeak")
    private void initView(){
        //RecyclerView
        mRecyclerView = view.findViewById(R.id.history_recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.addItemDecoration(new MyDecoration(Objects.requireNonNull(getContext()),MyDecoration.HORIZAONTAL_LIST));
        mListList.clear();
        adapter = new RecyclerViewAdapter(getContext(),mListList);
        mRecyclerView.setAdapter(adapter);
        //读取
        mReadBt = view.findViewById(R.id.history_read);
        mReadBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Log.e(TAG, "mReadBt.getText(): "+mReadBt.getText());
                Log.e(TAG, "getContext().getString(R.string.histopryRead): "+getContext().getString(R.string.histopryRead));
                if(mReadBt.getText().equals(getContext().getString(R.string.histopryRead))){
                    if(!BLEFragment.BLEConnectState){                       //检查蓝牙是否连接
                        Toast.makeText(getContext(),"蓝牙未连接",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    DataProcess.finishFlag = false;
                    frameCnt = 0;                                           //帧计数器
                    DataProcess.frameNum = -1;
                    mListList.clear();
                    mList.clear();
                    adapter.notifyDataSetChanged();
                    //发送读取指令
                    if(mLoopEt.getText().toString() == null){
                        Toast.makeText(getContext(), getContext().getString(R.string.historyErrHint),Toast.LENGTH_LONG).show();
                        return;
                    }
                    DataProcess.pack_sendData(0x4000+Integer.parseInt(mLoopEt.getText().toString()),0);
                    Log.e(TAG, ToastUtil.currentTime()+"DataProcess.frameNum前: "+DataProcess.frameNum);
                    SystemClock.sleep(1000);
                    Log.e(TAG, ToastUtil.currentTime()+"DataProcess.frameNum后: "+DataProcess.frameNum);
                    if(DataProcess.frameNum == -1 || DataProcess.frameNum==0){
                        Toast.makeText(getContext(), historyReadErr,Toast.LENGTH_LONG).show();
                        return;
                    }
                    mProgress.setVisibility(View.VISIBLE);
                    mProgress.setMin(0);
                    mProgress.setMax(DataProcess.frameNum);
                    mReadBt.setText(R.string.historyStopRead);                                      //改变按钮文本
                    mLoopEt.setEnabled(false);                                      //读取的循环数失能
                }else{
                    DataProcess.pack_sendData(null,1,0x4000+Integer.parseInt(mLoopEt.getText().toString()),0,0);
                    mReadBt.setText(R.string.histopryRead);
                    mProgress.setVisibility(View.INVISIBLE);
                    mLoopEt.setEnabled(true);
                }
            }
        });
        //循环
        mLoopEt = view.findViewById(R.id.history_cycle);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("have a history data");
        Objects.requireNonNull(getActivity()).registerReceiver(receiver,intentFilter);
        //进度条
        mProgress = view.findViewById(R.id.history_process);
        //设置存储间隔
        mSetSaveTime = view.findViewById(R.id.history_setSaveTime);
        mSetSaveTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DataProcess.pack_sendWord(0x20AE, (short) 60);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                DataProcess.pack_sendWord(0x2303, (short) 0);
            }
        });
    }

    /******************************************
     *   函数描述：   处理每一帧的数据
     *   parameter：
     *   return ：
     *******************************************/
    public static void History_ProcessData(byte[] tempArray,int len){
        if(tempArray == null || len == 0)
            return;
        frameCnt++;                                     //帧计数器
        mProgress.setProgress(frameCnt);
        mList.clear();
        //移除帧头等无效数据
        byte[] array = new byte[65];
        System.arraycopy(tempArray,10,array,0,65);
        //校验计算
        Log.e(TAG, "收到的校验: "+Integer.toHexString(array[64] & 0xFF));
        Log.e(TAG, "计算的累加校验和: "+Integer.toHexString(CRCSum(array,64) & 0xFF));

        if(array[64] != CRCSum(array,64)){
            mList.add("校验错误");
            mListList.add(mList);
            adapter.notifyDataSetChanged();
            return;
        }
        //SOC
        mList.add(array[13]+"%");
        //最大电压,最小电压
        byte[] volByte = new byte[DataProcess.BATStrings*2];
        System.arraycopy(array,27,volByte,0,volByte.length);
        int[] volArray = arrangeGroup(volByte,volByte.length);
        mList.add(volArray[volArray.length-1]+"");
        mList.add(volArray[0]+"");
        //电流
        mList.add(new DecimalFormat("0.0").format((short)((array[15] & 0xFF)|array[16] << 8)/10)+"");
        //故障代码
        mList.add("0x"+Integer.toHexString(array[24])+Integer.toHexString(array[23])+Integer.toHexString(array[22])+
                Integer.toHexString(array[21])+Integer.toHexString(array[20])+Integer.toHexString(array[19])+
                Integer.toHexString(array[18])+Integer.toHexString(array[17]));
        //温度
        mList.add(array[27+DataProcess.BATStrings*2]+"");
        mList.add(array[28+DataProcess.BATStrings*2]+"");
        mList.add(array[29+DataProcess.BATStrings*2]+"");
        mListList.add(mList);
        adapter.notifyDataSetChanged();
        Log.e(TAG, ToastUtil.currentTime()+"adapter.notifyDataSetChanged()-------已执行"+frameCnt);
    }
    /******************************************
     *   函数描述： 累加和校验+
     *   parameter：
     *   return ：
     *******************************************/
    public static byte CRCSum(byte[] array, int len){
        if(array ==null || len == 0){
            return -1;
        }
        int sum = 0;
        for(int i=0;i<len;i++){
            sum += (int) array[i];
        }
        return (byte)(sum & 0xFF);
    }
    /******************************************
     *   函数描述：       从小到大排列组合
     *   parameter：
     *   return ：
     *******************************************/
    public static int[] arrangeGroup(byte[] array, int len){
        int[] tempArray = new int[len/2];
        byte index = 0;
        for(int i=0;i<len;i=i+2){
            tempArray[index++] = (array[i+1] << 8) | ((int)array[i] & 0xFF);
        }
        int tempValue;
        for(int i=0;i<len/2-1;i++){
            for(int j=0;j<len/2-1-i;j++){
                if(tempArray[j] > tempArray[j+1]){
                    tempValue = tempArray[j];
                    tempArray[j] = tempArray[j+1];
                    tempArray[j+1] = tempValue;
                }
            }
        }
        return tempArray;
    }
    public class MyBoradcastReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "have a history data"://64历史数据有效载荷+1累加校验和+6帧头+1读写指令+2地址+1长度+2校验
                    Log.e(TAG, ToastUtil.currentTime()+"已接收到一条广播");
                        frameCnt++;
                        if(DataProcess.finishFlag){             //如果接收到最后一帧结束帧
                            mReadBt.setText(R.string.histopryRead);
                            mProgress.setVisibility(View.INVISIBLE);
                            mLoopEt.setEnabled(true);
                        }
                break;
            }
        }

    }
}
