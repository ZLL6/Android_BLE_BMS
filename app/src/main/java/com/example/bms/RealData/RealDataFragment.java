package com.example.bms.RealData;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.bms.DataProcess;
import com.example.bms.R;
import com.google.android.material.tabs.TabLayout;

//imageView_battery = findViewById(R.id.imageView_battery);
//        LayerDrawable layerDrawable = (LayerDrawable) imageView_battery.getDrawable();
//        ClipDrawable clipDrawable = (ClipDrawable) layerDrawable.findDrawableByLayerId(R.id.clip_drawable);
//        clipDrawable.setLevel(80*100);
public class RealDataFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    @SuppressLint("StaticFieldLeak")
    public static MainInfoFragment mainInfoFragment;
    @SuppressLint("StaticFieldLeak")
    public static SingleVolFragment singleVolFragment;
    @SuppressLint("StaticFieldLeak")
    public static WarningFragment warningFragment;
    @SuppressLint("StaticFieldLeak")
    public static HistoryExtremeValueFragment historyExtremeValueFragment;
    private Fragment[] fragments;
    public static int lastfragment = 0;                                       //用于记录上个选择的Fragment

    @SuppressLint("HandlerLeak")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView: ");
        View root = inflater.inflate(R.layout.real_data_fragment, container, false);
        if(savedInstanceState != null)
            lastfragment = savedInstanceState.getInt("lastFragment");
        mainInfoFragment = new MainInfoFragment();
        singleVolFragment = new SingleVolFragment();
        warningFragment = new WarningFragment();
        historyExtremeValueFragment = new HistoryExtremeValueFragment();
        fragments = new Fragment[]{mainInfoFragment,singleVolFragment,warningFragment,historyExtremeValueFragment};
        getChildFragmentManager().beginTransaction().replace(R.id.mLinear,
                fragments[lastfragment]).show(fragments[lastfragment]).commit();
        TabLayout mTabLayout = root.findViewById(R.id.TabLayout);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Handler handler = new Handler();
                switch (tab.getPosition()){
                    case 0://mainInfo界面
                        if(lastfragment != 0){
                            switchFragment(lastfragment,0);
                            lastfragment = 0;
                        }
                        handler.postDelayed(new Runnable() {        //为了让fragment有时间加载视图并初始化控件
                            @Override
                            public void run() {
                                mainInfoFragment.mainInfoShow();            //开启显示mainInfo信息
                            }
                        },100);
                        singleVolFragment.singleVolTimer.cancel();  //关闭singleVol定时器显示
                        warningFragment.warningTimer.cancel();      //关闭warning定时器显示
                        historyExtremeValueFragment.historyExtremeTimer.cancel();
                        break;
                    case 1://singleVol界面
                        if(lastfragment != 1){
                            switchFragment(lastfragment,1);
                            lastfragment = 1;
                        }
                        mainInfoFragment.mainInfoTimer.cancel();    //关闭mainInfo定时器显示
                        handler.postDelayed(new Runnable() {        //为了让fragment有时间加载视图并初始化控件
                            @Override
                            public void run() {
                                singleVolFragment.singleVolShow();          //开启singleVol定时器显示
                            }
                        },100);
                        warningFragment.warningTimer.cancel();      //关闭warning定时器显示
                        historyExtremeValueFragment.historyExtremeTimer.cancel();
                        break;
                    case 2://warning界面
                        if(lastfragment != 2){
                            switchFragment(lastfragment,2);
                            lastfragment = 2;
                        }
                        mainInfoFragment.mainInfoTimer.cancel();    //关闭mainInfo定时器显示
                        singleVolFragment.singleVolTimer.cancel();  //关闭singleVol定时器显示
                        historyExtremeValueFragment.historyExtremeTimer.cancel();
                        handler.postDelayed(new Runnable() {        //为了让fragment有时间加载视图并初始化控件
                            @Override
                            public void run() {
                                warningFragment.warningShow();           //开启warning定时器显示
                            }
                        },100);
                        break;
                    case 3://warning界面
                        if(lastfragment != 3){
                            switchFragment(lastfragment,3);
                            lastfragment = 3;
                        }
                        mainInfoFragment.mainInfoTimer.cancel();    //关闭mainInfo定时器显示
                        singleVolFragment.singleVolTimer.cancel();  //关闭singleVol定时器显示
                        warningFragment.warningTimer.cancel();
                        handler.postDelayed(new Runnable() {        //为了让fragment有时间加载视图并初始化控件
                            @Override
                            public void run() {
                                historyExtremeValueFragment.historyExtremeShow();
                            }
                        },100);
                        break;
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        return root;
    }

    /******************************************
     *   函数描述：隐藏lastfragment 展示fragment[index]
     *   parameter：
     *   return ：
     *******************************************/
    private void switchFragment(int lastfragment,int index){
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.hide(fragments[lastfragment]);      //隐藏上个fragment
        if(!fragments[index].isAdded()){
            transaction.add(R.id.mLinear,fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }
    /******************************************
    *   函数描述：       当切换到导航栏的其他界面时需要关闭实时数据显示的函数接口
    *   parameter：
    *   return ：
    *******************************************/
    public void cancelRealDataTimer(int lastfragment){
        switch (lastfragment){
            case 0:if(mainInfoFragment.mainInfoTimer != null)
                    mainInfoFragment.mainInfoTimer.cancel();
            break;
            case 1:if(singleVolFragment.singleVolTimer != null)
                        singleVolFragment.singleVolTimer.cancel();
            break;
            case 2:if(warningFragment.warningTimer != null)
                        warningFragment.warningTimer.cancel();
            break;
            case 3:if(historyExtremeValueFragment.historyExtremeTimer != null)
                        historyExtremeValueFragment.historyExtremeTimer.cancel();
            break;
        }
    }
    /******************************************
    *   函数描述：    当切换回该界面时需要开启实时数据显示的函数接口
    *   parameter：
    *   return ：
    *******************************************/
    public void startRealDataTimer(int lastfragment){
        switch (lastfragment){
            case 0:mainInfoFragment.mainInfoShow();
            break;
            case 1:singleVolFragment.singleVolShow();
            break;
            case 2:warningFragment.warningShow();
            break;
            case 3:historyExtremeValueFragment.historyExtremeShow();
            break;
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        if(!this.isHidden()){
            Log.e(TAG, "onResume: ");
            startRealDataTimer(lastfragment);
            DataProcess.getData();
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelRealDataTimer(lastfragment);
        DataProcess.getFrameTimer.cancel();
    }
    //fragment 隐藏与显示
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            Log.e(TAG, "实时数据页面显示: ");
            startRealDataTimer(lastfragment);
            DataProcess.getData();
        }else{
            Log.e(TAG, "实时数据页面消失: ");
            cancelRealDataTimer(lastfragment);
            DataProcess.getFrameTimer.cancel();
        }
    }
}