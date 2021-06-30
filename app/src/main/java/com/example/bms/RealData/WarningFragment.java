package com.example.bms.RealData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.bms.DataProcess;
import com.example.bms.R;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class WarningFragment extends Fragment {
    private static final String TAG = "WarningFragment";
    private View view;
    public boolean flag = false;
    private final ArrayList<TextView> arrayWarning = new ArrayList<>();

    public Timer warningTimer = new Timer();
    public Handler warningHandler;
    public Activity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.real_data_warning,container,false);
        Log.e(TAG, "onCreateView: ");
        initView();
        flag = true;
        return view;
    }
    /******************************************
    *   函数描述：初始化告警显示的View
    *   parameter：
    *   return ：
    *******************************************/
    private void initView(){
        arrayWarning.add(view.findViewById(R.id.singleOVW));
        arrayWarning.add(view.findViewById(R.id.singleOVP));
        arrayWarning.add(view.findViewById(R.id.singleUVW));
        arrayWarning.add(view.findViewById(R.id.singleUVP));
        arrayWarning.add(view.findViewById(R.id.VDifferW_H));
        arrayWarning.add(view.findViewById(R.id.VDifferP_H));
        arrayWarning.add(view.findViewById(R.id.VDifferW_L));
        arrayWarning.add(view.findViewById(R.id.VDifferP_L));
        arrayWarning.add(view.findViewById(R.id.chgOCW));
        arrayWarning.add(view.findViewById(R.id.chgOCP));
        arrayWarning.add(view.findViewById(R.id.chgShortCircuit));
        arrayWarning.add(view.findViewById(R.id.dischgOCW));
        arrayWarning.add(view.findViewById(R.id.dischgOCP));
        arrayWarning.add(view.findViewById(R.id.dischgShortCircuit));
        arrayWarning.add(view.findViewById(R.id.chgHTW));
        arrayWarning.add(view.findViewById(R.id.chgHTP));
        arrayWarning.add(view.findViewById(R.id.chgLTW));
        arrayWarning.add(view.findViewById(R.id.chgLTP));
        arrayWarning.add(view.findViewById(R.id.chgTDiffer));
        arrayWarning.add(view.findViewById(R.id.dischgHTW));
        arrayWarning.add(view.findViewById(R.id.dischgHTP));
        arrayWarning.add(view.findViewById(R.id.dischgLTW));
        arrayWarning.add(view.findViewById(R.id.dischgLTP));
        arrayWarning.add(view.findViewById(R.id.dischgTDiffer));
        arrayWarning.add(view.findViewById(R.id.chgMOSHTW));
        arrayWarning.add(view.findViewById(R.id.chgMOSHTP));
        arrayWarning.add(view.findViewById(R.id.dischgMOSHTW));
        arrayWarning.add(view.findViewById(R.id.dischgMOSHTP));
        arrayWarning.add(view.findViewById(R.id.currentTMoreThanMost));
        arrayWarning.add(view.findViewById(R.id.currentTLessThanLowest));
        arrayWarning.add(view.findViewById(R.id.SOCLW));
        arrayWarning.add(view.findViewById(R.id.batHTDisable));
        arrayWarning.add(view.findViewById(R.id.batLTDisable));
        arrayWarning.add(view.findViewById(R.id.batOVDisable));
        arrayWarning.add(view.findViewById(R.id.batUVDisable));
        arrayWarning.add(view.findViewById(R.id.VDifferDisable));
        arrayWarning.add(view.findViewById(R.id.non_originalCharger));
        arrayWarning.add(null);
        arrayWarning.add(null);
        arrayWarning.add(null);
        arrayWarning.add(view.findViewById(R.id.front_endChipCollect));
        arrayWarning.add(view.findViewById(R.id.VCollectFail));
        arrayWarning.add(view.findViewById(R.id.TCollectFail));
        arrayWarning.add(view.findViewById(R.id.CCollectCircuitDisable));
        arrayWarning.add(view.findViewById(R.id.memoryErr));
        arrayWarning.add(view.findViewById(R.id.selfDischgMOSDisabled));
        arrayWarning.add(view.findViewById(R.id.selfHeatMOSDisable));
        arrayWarning.add(view.findViewById(R.id.chgMOSDisable));
        arrayWarning.add(view.findViewById(R.id.dischgMOSDisable));
        arrayWarning.add(view.findViewById(R.id.preDischgMOSDisable));
        arrayWarning.add(view.findViewById(R.id.prechgMOSDisable));
    }
    /******************************************
    *   函数描述：       告警信息显示
    *   parameter：
    *   return ：
    *******************************************/
    public void WarningMsgShow(){
        for(int i=0;i<16;i++){
            if((DataProcess.mainInfoArray[0x200A-0x2000] & (1 << i)) != 0){
                arrayWarning.get(i).setTextColor(Color.parseColor("#FF0000"));             //有告警文字显示红色
            }else{
                arrayWarning.get(i).setTextColor(Color.parseColor("#000000"));             //没告警显示黑色
            }
            if((DataProcess.mainInfoArray[0x200B-0x2000] & (1 << i)) != 0){
                arrayWarning.get(16+i).setTextColor(Color.parseColor("#FF0000"));             //有告警文字显示红色
            }else{
                arrayWarning.get(16+i).setTextColor(Color.parseColor("#000000"));             //没告警显示黑色
            }
            if((DataProcess.mainInfoArray[0x200C-0x2000] & (1 << i)) != 0){
                if ((arrayWarning.get(32 + i) != null)) {
                    arrayWarning.get(32 + i).setTextColor(Color.parseColor("#FF0000"));        //有告警文字显示红色
                }
            }else{
                if((arrayWarning.get(32 + i) != null)){
                    arrayWarning.get(32+i).setTextColor(Color.parseColor("#000000"));             //没告警显示黑色
                }
            }
            if(i < 3){
                if((DataProcess.mainInfoArray[0x200D-0x2000] & (1 << i)) != 0){
                    arrayWarning.get(48+i).setTextColor(Color.parseColor("#FF0000"));             //有告警文字显示红色
                }else{
                    arrayWarning.get(48+i).setTextColor(Color.parseColor("#000000"));             //没告警显示黑色
                }
            }
        }
    }
    /******************************************
    *   函数描述：warning信息显示
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint("HandlerLeak")
    public void warningShow(){
        warningTimer = new Timer();
        warningHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg.what == 1){
                            WarningMsgShow();
                        }
                    }
                });
                super.handleMessage(msg);
            }
        };
        warningTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                warningHandler.handleMessage(message);
            }
        },50,300);
    }
}
