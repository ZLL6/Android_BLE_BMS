package com.example.bms.RealData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bms.DataProcess;
import com.example.bms.R;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class HistoryExtremeValueFragment extends Fragment {
    private static final String TAG = "HistoryExtremeValueFrag";
    private View view;
    public boolean flag = false;
    private final ArrayList<TextView> historyExtreme = new ArrayList<>();

    public Timer historyExtremeTimer = new Timer();
    public Handler historyExtremeHandler;
    public Activity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.real_data_history, container, false);
        flag = true;
        initView();
        return view;
    }

    /******************************************
    *   函数描述： 初始化value 和 cycle TextView
    *   parameter：
    *   return ：
    *******************************************/
    private void initView(){
        historyExtreme.add(view.findViewById(R.id.hisMaxChgV1));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgVCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgV2));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgVCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgV3));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgVCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgV4));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgVCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgV5));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgVCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgV1));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgVCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgV2));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgVCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgV3));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgVCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgV4));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgVCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgV5));
        historyExtreme.add(view.findViewById(R.id.hisMinDisChgVCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDiffer1));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDifferCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDiffer2));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDifferCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDiffer3));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDifferCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDiffer4));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDifferCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDiffer5));
        historyExtreme.add(view.findViewById(R.id.hisMaxVDifferCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgC1));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgCCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgC2));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgCCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgC3));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgCCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgC4));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgCCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgC5));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgCCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgC1));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgCCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgC2));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgCCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgC3));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgCCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgC4));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgCCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgC5));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgCCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgT1));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgTCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgT2));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgTCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgT3));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgTCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgT4));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgTCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgT5));
        historyExtreme.add(view.findViewById(R.id.hisMaxChgTCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMinChgT1));
        historyExtreme.add(view.findViewById(R.id.hisMinChgTCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMinChgT2));
        historyExtreme.add(view.findViewById(R.id.hisMinChgTCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMinChgT3));
        historyExtreme.add(view.findViewById(R.id.hisMinChgTCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMinChgT4));
        historyExtreme.add(view.findViewById(R.id.hisMinChgTCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMinChgT5));
        historyExtreme.add(view.findViewById(R.id.hisMinChgTCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgT1));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgTCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgT2));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgTCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgT3));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgTCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgT4));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgTCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgT5));
        historyExtreme.add(view.findViewById(R.id.hisMaxDischgTCycle5));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgT1));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgTCycle1));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgT2));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgTCycle2));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgT3));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgTCycle3));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgT4));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgTCycle4));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgT5));
        historyExtreme.add(view.findViewById(R.id.hisMinDischgTCycle5));
    }
    /******************************************
    *   函数描述：显示历史数据极值
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint("SetTextI18n")
    public void historyExtreme(){
        if((historyExtreme.size() > DataProcess.historyExtremumArray.length) || historyExtreme.size() < 90)
            return;
        for(int i=0;i<historyExtreme.size();i++){
            if(i<30)
                historyExtreme.get(i).setText(""+DataProcess.historyExtremumArray[i]);
            else if(i >= 30 && i < 50)
                historyExtreme.get(i).setText(new DecimalFormat("0.0").format((short)DataProcess.historyExtremumArray[i]/10)+"");
            else
                historyExtreme.get(i).setText(""+(short)DataProcess.historyExtremumArray[i]);
        }
    }
    /******************************************
    *   函数描述： 显示历史数据极值定时器
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint("HandlerLeak")
    public void historyExtremeShow(){
        historyExtremeTimer = new Timer();
        historyExtremeHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg.what == 1){
                            historyExtreme();
                        }
                    }
                });
                super.handleMessage(msg);
            }
        };
        historyExtremeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                historyExtremeHandler.handleMessage(message);
            }
        },10,300);
    }

}
