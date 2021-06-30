package com.example.bms.RealData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.bms.DataProcess;
import com.example.bms.R;
import com.example.bms.other.ToastUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SingleVolFragment extends Fragment {
    private static final String TAG = "SingleVolFragment";
    private View view;
    public boolean flag = false;
    private final ArrayList<TextView> arrayVol = new ArrayList<>();
    private final ArrayList<CheckBox> arrayBalanceState = new ArrayList<>();

    public Timer singleVolTimer = new Timer();
    public Handler singleVolHandler;
    public Activity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.real_data_single_vol,container,false);
        Log.e(TAG, "onCreateView: ");
        initView();
        flag = true;
        return view;
    }
    /******************************************
    *   函数描述：   初始化TextView和checBox
    *   parameter：
    *   return ：
    *******************************************/
    private void initView(){
        arrayVol.add(view.findViewById(R.id.cellV1));
        arrayVol.add(view.findViewById(R.id.cellV2));
        arrayVol.add(view.findViewById(R.id.cellV3));
        arrayVol.add(view.findViewById(R.id.cellV4));
        arrayVol.add(view.findViewById(R.id.cellV5));
        arrayVol.add(view.findViewById(R.id.cellV6));
        arrayVol.add(view.findViewById(R.id.cellV7));
        arrayVol.add(view.findViewById(R.id.cellV8));
        arrayVol.add(view.findViewById(R.id.cellV9));
        arrayVol.add(view.findViewById(R.id.cellV10));
        arrayVol.add(view.findViewById(R.id.cellV11));
        arrayVol.add(view.findViewById(R.id.cellV12));
        arrayVol.add(view.findViewById(R.id.cellV13));
        arrayVol.add(view.findViewById(R.id.cellV14));
        arrayVol.add(view.findViewById(R.id.cellV15));
        arrayVol.add(view.findViewById(R.id.cellV16));
        arrayBalanceState.add(view.findViewById(R.id.checkbox1));
        arrayBalanceState.add(view.findViewById(R.id.checkbox2));
        arrayBalanceState.add(view.findViewById(R.id.checkbox3));
        arrayBalanceState.add(view.findViewById(R.id.checkbox4));
        arrayBalanceState.add(view.findViewById(R.id.checkbox5));
        arrayBalanceState.add(view.findViewById(R.id.checkbox6));
        arrayBalanceState.add(view.findViewById(R.id.checkbox7));
        arrayBalanceState.add(view.findViewById(R.id.checkbox8));
        arrayBalanceState.add(view.findViewById(R.id.checkbox9));
        arrayBalanceState.add(view.findViewById(R.id.checkbox10));
        arrayBalanceState.add(view.findViewById(R.id.checkbox11));
        arrayBalanceState.add(view.findViewById(R.id.checkbox12));
        arrayBalanceState.add(view.findViewById(R.id.checkbox13));
        arrayBalanceState.add(view.findViewById(R.id.checkbox14));
        arrayBalanceState.add(view.findViewById(R.id.checkbox15));
        arrayBalanceState.add(view.findViewById(R.id.checkbox16));
    }
    /******************************************
    *   函数描述：       单体电压显示和均衡状态显示
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint("SetTextI18n")
    public void singleVolAndBlance(){
        //均衡状态显示
        if(DataProcess.BATStrings < 0 || DataProcess.BATStrings > 16){      //检测电池串数
            ToastUtil.show(getContext(),"电池串数错误");
            return;
        }
        for(int i=0;i< DataProcess.BATStrings;i++){
            if((DataProcess.volArray[0] & (1 << i)) != 0){                  //逐位判断，1是均衡状态，0是非均衡状态
                arrayBalanceState.get(i).setChecked(true);
            }else{
                arrayBalanceState.get(i).setChecked(false);
            }
            arrayVol.get(i).setText(DataProcess.volArray[1+i]+"mV");        //单体电压显示
        }
    }
    /******************************************
    *   函数描述：       singleVol信息定时器显示
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint("HandlerLeak")
    public void singleVolShow(){
        singleVolTimer = new Timer();
        singleVolHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg.what == 1){
                            singleVolAndBlance();
                        }
                    }
                });
                super.handleMessage(msg);
            }
        };
        singleVolTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                singleVolHandler.handleMessage(message);
            }
        },10,300);
    }
}
