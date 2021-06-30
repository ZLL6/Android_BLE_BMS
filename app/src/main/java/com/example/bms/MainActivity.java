package com.example.bms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bms.BLE.BLEFragment;
import com.example.bms.Dialog.MenuDialog;
import com.example.bms.Dialog.UpgradeDialog;
import com.example.bms.RealData.RealDataFragment;
import com.example.bms.history.HistoryQueryFragment;
import com.example.bms.other.LanguageUtil;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";

    public static FragmentTransaction transaction;
    //标题
    private ImageView mTitle_back;
    public ImageView mTitle_menu;
    @SuppressLint("StaticFieldLeak")
    public static TextView mTitle_text;
    private MenuDialog mMenuDialog;
    //BLE Fragment
    @SuppressLint("StaticFieldLeak")
    public static BLEFragment mBLEFragment;

    //Fragment 与 activity 的广播
    private MyBoradcastReceiver receiver;

    //实时数据
    public static RealDataFragment mRealDataFragment;
    //升级
    public UpgradeDialog mUpgradeDialog;
    //历史
    public HistoryQueryFragment mHistoryQueryFragment;
    @SuppressLint("StaticFieldLeak")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        DataProcess.mContext = this;
        initView();
        initData();
        addBoardcast();
    }
    /******************************************
    *   函数描述： 初始化View
    *   parameter：
    *   return ：
    *******************************************/
    private void initView(){
        mTitle_back = findViewById(R.id.title_back);
        mTitle_menu = findViewById(R.id.title_menu);
        mTitle_text = findViewById(R.id.title_text);
        mBLEFragment = new BLEFragment();
        mRealDataFragment = new RealDataFragment();
        mHistoryQueryFragment = new HistoryQueryFragment();
        mTitle_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                receiver.switchFragment(receiver.currentFragment,receiver.mOldFragment);
            }
        });
        mTitle_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMenuDialog = new MenuDialog(MainActivity.this,R.style.MenuDialog_Style);
                Window window = mMenuDialog.getWindow();
                window.setGravity(Gravity.TOP | Gravity.RIGHT);                 //设置弹出位置是右上角
                WindowManager.LayoutParams params = window.getAttributes();
                params.y = 175;                                                 //设置纵向偏移
                window.setAttributes(params);
//                //语言切换
//                mMenuDialog.setOnLanguageClickListener(new MenuDialog.OnLanguageClickListener() {
//                    @Override
//                    public void onLanguageClick() {
//                        LanguageUtil.settingLanguage(MainActivity.this,LanguageUtil.getInstance());
//                        //activity活动重建
//                        recreate();
//                        mMenuDialog.dismiss();
//                    }
//                });
                //切换到升级模块
                mMenuDialog.setOnUpgradeClickListener(new MenuDialog.OnUpgradeClickListener() {
                    @Override
                    public void onUpgradeClick() {
                        DataProcess.getFrameTimer.cancel();              //实时数据问询帧定时器关闭
                        mUpgradeDialog = new UpgradeDialog(MainActivity.this,R.style.UpgradeDialog_Style);
                        mUpgradeDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                if(receiver.currentFragment == mRealDataFragment){
                                    DataProcess.getData();                  //实时数据问询帧定时器开启
                                }
                                if(mUpgradeDialog.binFileTimer != null)
                                    mUpgradeDialog.binFileTimer.cancel();       //如果正在升级时关闭升级弹出框，则需关闭定时器
                            }
                        });
                        mMenuDialog.dismiss();
                        mUpgradeDialog.show();
                    }
                });
                mMenuDialog.setOnHistoryClickListener(new MenuDialog.OnHistoryClickListener() {
                    @Override
                    public void onHistoryClick() {
                        receiver.switchFragment(receiver.currentFragment,mHistoryQueryFragment);
                        mMenuDialog.dismiss();
                    }
                });
                mMenuDialog.show();
            }
        });
        mTitle_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(receiver.currentFragment != mBLEFragment)
                    receiver.switchFragment(receiver.currentFragment,mBLEFragment);
                if(BLEFragment.BLEConnectState){                //如果当前蓝牙还连接着
                    mTitle_back.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    /******************************************
    *   函数描述：初始化数据
    *   parameter：
    *   return ：
    *******************************************/
    private void initData(){
        //显示蓝牙未连接的layout
        transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.initial_frament,mBLEFragment).show(mBLEFragment).commit();
        //状态显示未连接
        mTitle_text.setText(R.string.BLE_disconnected);
    }
    /******************************************
    *   函数描述：   添加广播
    *   parameter：
    *   return ：
    *******************************************/
    private void addBoardcast(){
        IntentFilter intentFilter = new IntentFilter();
        receiver = new MyBoradcastReceiver();
        intentFilter.addAction("BLE have connected");
        intentFilter.addAction("Ble disconnected");
        registerReceiver(receiver,intentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    public class MyBoradcastReceiver extends BroadcastReceiver{
        public Fragment currentFragment = MainActivity.mBLEFragment;
        public Fragment mOldFragment = null;
        public boolean isBLEConnected = false;
        @SuppressLint("SetTextI18n")
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()){
                case "BLE have connected":  //蓝牙设备已连接
                    Log.e(TAG, "onReceive: BLE have connected "+ BLEFragment.mBluetoothGatt.getDevice().getName());
                    mTitle_text.setText(getString(R.string.Connected) + "\n"+BLEFragment.mBluetoothGatt.getDevice().getName());
                    switchFragment(MainActivity.mBLEFragment,MainActivity.mRealDataFragment);
                    isBLEConnected = true;
                break;
                case "Ble disconnected":    //蓝牙断开连接
                    Log.e(TAG, "onReceive: Ble disconnected");
                    mTitle_text.setText("未连接");
                    isBLEConnected = false;
                    DataProcess.getFrameTimer.cancel();              //实时数据问询帧定时器关闭
                    switchFragment(currentFragment,MainActivity.mBLEFragment);
                    //设置TitleBack不可见
                    if(mTitle_back.getVisibility() != View.INVISIBLE){
                        mTitle_back.setVisibility(View.INVISIBLE);
                    }
                break;
            }
        }
        public void switchFragment(Fragment oldFragment, Fragment newFragment){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            if(oldFragment != null)
                transaction.hide(oldFragment);
            if(!newFragment.isAdded()){
                transaction.add(R.id.initial_frament,newFragment);
            }
            transaction.show(newFragment).commitAllowingStateLoss();
            currentFragment = newFragment;
            mOldFragment = oldFragment;
            //设置titleBack的可见性
            if(currentFragment == mRealDataFragment || (currentFragment == mBLEFragment && !isBLEConnected)){
                mTitle_back.setVisibility(View.INVISIBLE);
            }
            if(currentFragment == mHistoryQueryFragment){
                mTitle_back.setVisibility(View.VISIBLE);
            }
        }
    }
}