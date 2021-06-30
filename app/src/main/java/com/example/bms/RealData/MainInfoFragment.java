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
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.example.bms.DataProcess;
import com.example.bms.R;
import com.example.bms.other.ToastUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainInfoFragment extends Fragment {
    private static final String TAG = "MainInfoFragment";
    private View view;          //fragment视图

    public TextView tv_hardwareVersion;
    public TextView tv_softwareVersion;
    public TextView tv_FCP;
    private TextView tv_FCVersion;
    private TextView tv_batProDate;
    private TextView tv_BMSProDate;
    private TextView tv_packCodeBar;
    private TextView tv_BMSCodebar;
    private TextView tv_cellType;
    private TextView tv_cellModel;
    private TextView tv_cellStrings;
    private TextView tv_frontChipType;
    private TextView tv_BMSType;
    private TextView tv_customCode;
    private TextView tv_batVendorCode;
    private TextView tv_salerCode;
    private TextView tv_fullChgCap;
    private TextView tv_packDesCap;
    private TextView tv_splitter;
    private TextView tv_totalV;
    private TextView tv_averV;
    private TextView tv_minOutV;
    private TextView tv_maxOutV;
    private TextView tv_minOutVchannel;
    private TextView tv_maxOutVchannel;
    private TextView tv_outVDiffer;
    private TextView tv_currentC;
    private TextView tv_averc;
    private TextView tv_minDischgC;
    private TextView tv_maxDsichgC;
    private TextView tv_minT;
    private TextView tv_maxT;
    private TextView tv_averT;
    private TextView tv_outTDiffer ;
    private TextView tv_maxTChannel;
    private TextView tv_minTChannel;
    private TextView tv_soc;
    private TextView tv_soh;
    private TextView tv_preCalculDischgT;
    private TextView tv_preCalculChgFullT;
    private TextView tv_leastCap;
    private TextView tv_addCap;
    private TextView tv_cycleCnt;
    private TextView tv_currentJobMode;
    private TextView tv_batPackType;

    private TextView tv_OVTime;
    private TextView tv_UVTime;
    private TextView tv_VDifferHTime;
    private TextView tv_VDifferLTime;
    private TextView tv_chgOCTime;
    private TextView tv_dischgOCTime;
    private TextView tv_chgHTTime;
    private TextView tv_chgLTTime;
    private TextView tv_dischgHTTime;
    private TextView tv_dischgLTTime;

    public Timer mainInfoTimer = new Timer();
    public Handler mainInfoHandler;

    private final ArrayList<TextView> TempArray = new ArrayList<>();
    private final ArrayList<Switch> switchStateArray = new ArrayList<>();

    public Activity mActivity;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (Activity)context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.real_data_main_info,container,false);
        Log.e(TAG, "onCreateView: ");
        initView();
        return view;
    }
    /******************************************
    *   函数描述：初始化所有的TextView
    *   parameter：
    *   return ：
    *******************************************/
    private void initView(){
        tv_hardwareVersion = view.findViewById(R.id.hardwareVersionNum);
        tv_softwareVersion = view.findViewById(R.id.softwareVersion);
        tv_FCP = view.findViewById(R.id.FCP);
        tv_FCVersion = view.findViewById(R.id.FCPVersion);
        tv_batProDate = view.findViewById(R.id.BatProDate);
        tv_BMSProDate = view.findViewById(R.id.BMSProDate);
        tv_packCodeBar = view.findViewById(R.id.PackBarCode);
        tv_BMSCodebar = view.findViewById(R.id.BMSBarCode);
        tv_cellType = view.findViewById(R.id.CellType);
        tv_cellModel = view.findViewById(R.id.CellModel);
        tv_cellStrings = view.findViewById(R.id.CellStrings);
        tv_frontChipType = view.findViewById(R.id.FontChip);
        tv_BMSType = view.findViewById(R.id.BMSType);
        tv_customCode = view.findViewById(R.id.CustomerCode);
        tv_batVendorCode = view.findViewById(R.id.BatProCode);
        tv_salerCode = view.findViewById(R.id.SellerCode);
        tv_fullChgCap = view.findViewById(R.id.FullCHargeCap);
        tv_packDesCap = view.findViewById(R.id.PackDesignCap);
        tv_splitter = view.findViewById(R.id.ShuntReesistan);
        tv_totalV = view.findViewById(R.id.ToatalVol);
        tv_averV = view.findViewById(R.id.AverVOl);
        tv_minOutV = view.findViewById(R.id.MinOutVol);
        tv_maxOutV = view.findViewById(R.id.MaxOutVol);
        tv_minOutVchannel = view.findViewById(R.id.MinOutVolChannel);
        tv_maxOutVchannel = view.findViewById(R.id.MaxOutVolChannel);
        tv_outVDiffer = view.findViewById(R.id.OutVolDiffer);
        tv_currentC = view.findViewById(R.id.CurrentC);
        tv_averc = view.findViewById(R.id.AverC);
        tv_maxDsichgC = view.findViewById(R.id.MaxDischgC);
        tv_minDischgC = view.findViewById(R.id.MinDischgC);
        tv_minT = view.findViewById(R.id.LowestT);
        tv_maxT = view.findViewById(R.id.HighestT);
        tv_averT = view.findViewById(R.id.AverT);
        tv_outTDiffer = view.findViewById(R.id.OutTDiffer);
        tv_maxTChannel = view.findViewById(R.id.HighestTChannel);
        tv_minTChannel = view.findViewById(R.id.LowestTChannel);
        TextView tv_t1 = view.findViewById(R.id.T1);
        TextView tv_t2 = view.findViewById(R.id.T2);
        TextView tv_t3 = view.findViewById(R.id.T3);
        TextView tv_t4 = view.findViewById(R.id.T4);
        tv_soc = view.findViewById(R.id.SOC);
        tv_soh = view.findViewById(R.id.SOH);
        tv_preCalculChgFullT = view.findViewById(R.id.PreChgT);
        tv_preCalculDischgT = view.findViewById(R.id.PreDischgT);
        tv_leastCap = view.findViewById(R.id.SurplusCap);
        tv_addCap = view.findViewById(R.id.UmulativeChg);
        tv_cycleCnt = view.findViewById(R.id.CycleCnt);
        tv_currentJobMode = view.findViewById(R.id.CurrentJobModel);
        tv_batPackType = view.findViewById(R.id.batPackType);

        TempArray.add(tv_t1);
        TempArray.add(tv_t2);
        TempArray.add(tv_t3);
        TempArray.add(tv_t4);

        Switch selfDischg = view.findViewById(R.id.selfDischgSwitchStatue);
        Switch selfHeat = view.findViewById(R.id.selfHeatSwitchState);
        Switch preChg = view.findViewById(R.id.prechgSwitchState);
        Switch preDischg = view.findViewById(R.id.preDischgSwitchState);
        Switch dischgMOS = view.findViewById(R.id.dischgMOSSwitchstate);
        Switch chgMOS = view.findViewById(R.id.chgMOSSwitchState);

        switchStateArray.add(selfDischg);
        switchStateArray.add(selfHeat);
        switchStateArray.add(preChg);
        switchStateArray.add(preDischg);
        switchStateArray.add(dischgMOS);
        switchStateArray.add(chgMOS);

        tv_OVTime = view.findViewById(R.id.OVTime);
        tv_UVTime = view.findViewById(R.id.UVTime);
        tv_VDifferHTime = view.findViewById(R.id.VDifferHTime);
        tv_VDifferLTime = view.findViewById(R.id.VDifferLTime);
        tv_chgOCTime = view.findViewById(R.id.chgOCTime);
        tv_dischgOCTime = view.findViewById(R.id.dischgOCTime);
        tv_chgHTTime = view.findViewById(R.id.chgHTTime);
        tv_chgLTTime = view.findViewById(R.id.chgLTTime);
        tv_dischgHTTime = view.findViewById(R.id.dischgHTTime);
        tv_dischgLTTime = view.findViewById(R.id.dischgLTTime);
    }
    /******************************************
    *   函数描述：把数据显示在TextView
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    public void mainInfoDataShow(){
        int i=0;
        //硬件版本
        tv_hardwareVersion.setText(DataProcess.IntArrayToASCIIString(DataProcess.mainInfoArray,i,5));
        i += 5;
        tv_softwareVersion.setText(DataProcess.IntArrayToASCIIString(DataProcess.mainInfoArray,i,5));
        i = 14;
        //总电压
        tv_totalV.setText(new DecimalFormat("0.0").format(DataProcess.mainInfoArray[i++]/10) +"V");
        //平均电压
        tv_averV.setText(DataProcess.mainInfoArray[i++]+"mV");
        //最小输出电压
        tv_minOutV.setText(DataProcess.mainInfoArray[i++]+"mV");
        //最大输出电压
        tv_maxOutV.setText(DataProcess.mainInfoArray[i++]+"mV");
        //最小输出电压通道（字节低位）
        tv_minOutVchannel.setText(((DataProcess.mainInfoArray[i] & 0xFF)+1)+"");
        //最大输出电压通道（字节高位）
        tv_maxOutVchannel.setText(((DataProcess.mainInfoArray[i++] >> 8)+1)+"");
        //输出压差
        tv_outVDiffer.setText(DataProcess.mainInfoArray[i++]+"mV");
        i += 3;
        //预计放电剩余时间
        tv_preCalculDischgT.setText(Integer.toString(DataProcess.mainInfoArray[i] >> 8)+mActivity.getString(R.string.hour)+
                String.valueOf(DataProcess.mainInfoArray[i++] & 0xFF)+mActivity.getString(R.string.min));
        //预计充满剩余时间
        tv_preCalculChgFullT.setText(Integer.toString(DataProcess.mainInfoArray[i] >> 8)+mActivity.getString(R.string.hour)+
                String.valueOf(DataProcess.mainInfoArray[i++] & 0xFF)+mActivity.getString(R.string.min));
        //当前电流值
        tv_currentC.setText(new DecimalFormat("0.0").format(((short)DataProcess.mainInfoArray[i++])/10)+"A");
        //平均电流值
        tv_averc.setText(new DecimalFormat("0.0").format(((short)DataProcess.mainInfoArray[i++])/10)+"A");
        //当前允许的最大放电电流
        tv_maxDsichgC.setText(new DecimalFormat("0.0").format(((short)DataProcess.mainInfoArray[i++])/10)+"A");
        //当前允许的最大充电电流
        tv_minDischgC.setText(new DecimalFormat("0.0").format(((short)DataProcess.mainInfoArray[i++])/10)+"A");
        //平均温度
        tv_averT.setText((byte)(DataProcess.mainInfoArray[i] & 0xFF)+"°C");
        //当前工作模式
        switch (DataProcess.mainInfoArray[i++] >> 8){
            case 0:tv_currentJobMode.setText("准备启动");
            break;
            case 1:tv_currentJobMode.setText("充电");
                break;
            case 2:tv_currentJobMode.setText("放电");
                break;
            case 3:tv_currentJobMode.setText("静止");
                break;
            case 4:tv_currentJobMode.setText("均衡");
                break;
            case 5:tv_currentJobMode.setText("自放电");
                break;
            case 6:tv_currentJobMode.setText("休眠");
                break;
            case 7:tv_currentJobMode.setText("掉电");
                break;
            default:tv_currentJobMode.setText("模式错误");
        }
        //最低温度（字节低位）
        tv_minT.setText((byte)(DataProcess.mainInfoArray[i] & 0xFF)+"°C");
        //最高温度（字节高位）
        tv_maxT.setText((byte)(DataProcess.mainInfoArray[i++] >> 8)+"°C");
        //最低温度通道数（字节低位）
        tv_minTChannel.setText(((DataProcess.mainInfoArray[i] & 0xFF)+1)+"");
        //最高温度通道数（字节高位）
        tv_maxTChannel.setText(((DataProcess.mainInfoArray[i++] >> 8)+1)+"");
        //输出温差
        tv_outTDiffer.setText((byte)(DataProcess.mainInfoArray[i++] & 0xFF)+"°C");
        //PACK设计容量（SOH的分母）
        tv_packDesCap.setText(new DecimalFormat("0.00").format(DataProcess.mainInfoArray[i++]/100)+"AH");
        //最新满充容量（SOC的分母，SOH的分子）
        tv_fullChgCap.setText(new DecimalFormat("0.00").format(DataProcess.mainInfoArray[i++]/100)+"AH");
        //剩余容量（SOC的分子）
        tv_leastCap.setText(new DecimalFormat("0.00").format(DataProcess.mainInfoArray[i++]/100)+"AH");
        //充电累计AH
        tv_addCap.setText(DataProcess.mainInfoArray[i++]+"AH");
        //SOC值（字节低位）
        tv_soc.setText((DataProcess.mainInfoArray[i] & 0xFF)+"%");
        //SOH值（字节高位）
        tv_soh.setText((DataProcess.mainInfoArray[i++] >> 8)+"%");
        //循环次数
        tv_cycleCnt.setText(DataProcess.mainInfoArray[i++]+"次");
        //电芯类型（字节低位）
        switch (DataProcess.mainInfoArray[i] & 0xFF){
            case 0:tv_cellType.setText(R.string.polymer);
            break;
            case 1:tv_cellType.setText(R.string.ternary);
            break;
            case 2:tv_cellType.setText(R.string.Lithium);
            break;
            case 3:tv_cellType.setText(R.string.LiTiO);
            break;
        }
        //电芯串数（字节高位）
        tv_cellStrings.setText((DataProcess.mainInfoArray[i++] >> 8)+mActivity.getString(R.string.series));
        //前端芯片型号
        switch (DataProcess.mainInfoArray[i] & 0xFF){
            case 0:tv_frontChipType.setText("bq76940");
                break;
            case 1:tv_frontChipType.setText("bq76930");
                break;
            case 2:tv_frontChipType.setText("bq76920");
                break;
            case 3:tv_frontChipType.setText("bq40Z80");
                break;
            case 4:tv_frontChipType.setText("bq40Z50");
                break;
        }
        //电芯型号
        tv_cellModel.setText((DataProcess.mainInfoArray[i++] >> 8)+"");
        //分流器阻值
        tv_splitter.setText(DataProcess.mainInfoArray[i++]+"μΩ");
        //飞控协议
        i = 0;
        switch (DataProcess.productInfoArray[0]){
            case 0x01:tv_FCP.setText(R.string.boYing);
            break;
            case 0x02:tv_FCP.setText(R.string.BoYingWithReceive);
            break;
            case 0x03:tv_FCP.setText("UAVCAN");
            break;
            case 0x04:tv_FCP.setText(R.string.JiYi);
            break;
        }
        //飞控版本号
        i += 5;
        tv_FCVersion.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,5));
        //销售商编码
        i += 5;
        tv_salerCode.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,5));
        //客户编码
        i += 5;
        tv_customCode.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,5));
        //电池厂商编码
        i += 5;
        tv_batVendorCode.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,5));
        //电池包条形码
        i += 5;
        tv_packCodeBar.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,8));
        //电池包型号
        i += 8;
        tv_batPackType.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,5));
        //BMS型号
        i += 5;
        tv_BMSType.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,5));
        //BMS条码
        i += 5;
        tv_BMSCodebar.setText(DataProcess.IntArrayToASCIIString(DataProcess.productInfoArray,i,8));
        //BMS生产日期
        i += 8;
        tv_BMSProDate.setText("20"+(DataProcess.productInfoArray[i] & 0xFF)+"/"+(DataProcess.productInfoArray[i++] >> 8)+"/"
                                +(DataProcess.productInfoArray[i] & 0xFF));
        //电池包生产日期
        tv_batProDate.setText("20"+(DataProcess.productInfoArray[i++] >> 8)+"/"+(DataProcess.productInfoArray[i] & 0xFF)+"/"
                                +(DataProcess.productInfoArray[i++] >> 8));
        //温度
        if(DataProcess.TempNum < 0 || DataProcess.TempNum > 4){
            ToastUtil.show(getContext(),"温度点数错误");
            return;
        }
        switch (DataProcess.TempNum){
            case 1:TempArray.get(0).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] & 0xFF)+"°C");
            break;
            case 2:TempArray.get(0).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] & 0xFF)+"°C");
                TempArray.get(1).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] >> 8)+"°C");
            break;
            case 3:TempArray.get(0).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] & 0xFF)+"°C");
                TempArray.get(1).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] >> 8)+"°C");
                TempArray.get(2).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings+1] & 0xFF)+"°C");
            break;
            case 4:TempArray.get(0).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] & 0xFF)+"°C");
                TempArray.get(1).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings] >> 8)+"°C");
                TempArray.get(2).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings+1] & 0xFF)+"°C");
                TempArray.get(3).setText((byte)(DataProcess.volArray[DataProcess.BATStrings/16+1+DataProcess.BATStrings+1] >> 8)+"°C");
            break;
        }
        i = 0x20AF-0x2076;
        //过压总次数
        tv_OVTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //欠压总次数
        tv_UVTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //压差H过大总次数
        tv_VDifferHTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //压差L过大总次数
        tv_VDifferLTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //充电过流总次数
        tv_chgOCTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //放电过流总次数
        tv_dischgOCTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //充电高温总次数
        tv_chgHTTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //充电低温总次数
        tv_chgLTTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //放电高温总次数
        tv_dischgHTTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //放电低温的总次数
        tv_dischgLTTime.setText(""+DataProcess.productInfoArray[i++]+"次");
        //开关状态告警信息
        for(int j=0;j<switchStateArray.size();j++){
            if((DataProcess.mainInfoArray[0x200D-0x2000] & (1 << (j+3))) != 0){
                switchStateArray.get(j).setChecked(true);
            }else{
                switchStateArray.get(j).setChecked(false);
            }
        }
    }
    /******************************************
    *   函数描述：   mainInfo数据显示定时器
    *   parameter：
    *   return ：
    *******************************************/
    @SuppressLint("HandlerLeak")
    public void mainInfoShow(){
        Log.e(TAG, "mainInfoShow: 1");
        mainInfoTimer = new Timer();
        mainInfoHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(msg.what == 1){
                            mainInfoDataShow();
                        }
                    }
                });
                super.handleMessage(msg);
            }
        };
        mainInfoTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                mainInfoHandler.handleMessage(message);
            }
        },10,300);
    }
}
