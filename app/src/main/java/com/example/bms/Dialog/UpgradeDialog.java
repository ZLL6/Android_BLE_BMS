package com.example.bms.Dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.MovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.bms.BLE.BLEFragment;
import com.example.bms.DataProcess;
import com.example.bms.R;
import com.example.bms.other.ToastUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class UpgradeDialog extends Dialog{
    private static final String TAG = "UpgradeDialog";

    private final Context mContext;
    private Spinner binFileSpinner;
    private String binFileName;
    private Button upgradeBt;
    private ProgressBar upgradeProgress;
    //private byte[] binFilebuff;
    private TextView binFileContent;
    public Timer binFileTimer;
    private int binFileSendCmdFlag = 1;         //bin文件发送帧指令选择
    private Handler binFileSendFrameHandler;
    private boolean binFileWaitFlag = false;
    private int frameNum = 0;
    private int whilecnt = 0;
    private int index;                          //文件数组开始发送的下标位置
    private int fileAddrCnt;

    public UpgradeDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upgrade_fragment);
        initView();
        binFileSendFrameBoardcast();
    }
    /******************************************
     *   函数描述：注册意图过滤器，添加行为
     *   parameter：
     *   return ：
     *******************************************/
    private void binFileSendFrameBoardcast(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERBOOT");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERBOOTRETFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERCASHFALSHFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.OPERATEERASHFLASHFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERLOADFILRFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.OPERATELOADFILEFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.CHECKFLASHFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.OPERATECHECKFLASHFAILED");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.UPGARDESCUESS");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.send enter into bootload instruct");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.have entered into bootload");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.sent earsed FLASH instruct");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.Earsed FLASH successfully");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.sending bin file data");
        intentFilter.addAction("com.example.bmsble.ui.notifications.NotificationsFragment.Bin file data load successfully");
        MyBoradcastReceiver receiver = new MyBoradcastReceiver();
        mContext.registerReceiver(receiver,intentFilter);
    }
    /******************************************
     *   函数描述：初始化View
     *   parameter：
     *   return ：
     *******************************************/
    private void initView(){
        binFileSpinner = findViewById(R.id.binFileSpinner);
        try {
            String[] tempFileArray = mContext.getAssets().list("");
            Log.e(TAG, "tempFileArray: "+ Arrays.toString(tempFileArray));
            ArrayList<String> file = new ArrayList<String>();
            for(String str:tempFileArray){
                if(str.endsWith(".bin") || str.endsWith(".Bin")){           //只留下bin文件
                    if(!file.contains(str))
                        file.add(str);
                }
            }
            //创建适配器
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),R.layout.upgrade_spinner_item,file);
            binFileSpinner.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        upgradeBt = findViewById(R.id.upgrade_Bt);
        Button clearBt = findViewById(R.id.clearText);
        upgradeProgress = findViewById(R.id.upgrade_progressBar);
        binFileContent = findViewById(R.id.file_content);
        binFileContent.setMovementMethod(ScrollingMovementMethod.getInstance());            //设置可滚动
        //spinner bin文件选择
        binFileSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binFileName = binFileSpinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //升级按钮
        upgradeBt.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Log.e(TAG, "upgradeBt.getText(): "+upgradeBt.getText());
                Log.e(TAG, "mContext.getString(R.string.upgrade): "+mContext.getString(R.string.upgrade));
                if(upgradeBt.getText().equals(mContext.getString(R.string.upgrade))){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Hint");
                    builder.setMessage("Whether to upgrade?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            /*读取文件*/
                            byte[] fileByteArray = readBinFile(binFileName);
                            if(fileByteArray != null){
                                /*升级烧入*/
                                upgradeWrite(fileByteArray);
                            }
                        }
                    });
                    builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();
                }else{                                              //如果是正在升级
                    /*关闭发送数据帧定时器*/
                    if(binFileTimer != null)
                        binFileTimer.cancel();                          //关闭定时线程
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                }
            }
        });
        //清空text
        clearBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binFileContent.setText(null);
            }
        });
    }
    /******************************************
     *   函数描述：读取二进制文件
     *   parameter：
     *   return ：
     *******************************************/
    private byte[] readBinFile(String fileName){
        InputStream inputStream = mContext.getClassLoader().getResourceAsStream("assets/"+fileName);
        try {
            byte[] buff = new byte[inputStream.available()];
            int n=0;
            int count;
            while((count = inputStream.read(buff,n, buff.length-n)) > 0){
                n += count;
            }
            return buff;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    /******************************************
     *   函数描述：升级烧入
     *   parameter：
     *   return ：
     *******************************************/
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void upgradeWrite(byte[] fileData){
        byte[] tempFileData = fileData;
        //检查蓝牙是否连接
        if(!BLEFragment.BLEConnectState){
            Toast.makeText(mContext,"蓝牙未连接",Toast.LENGTH_SHORT).show();
            return;
        }
        //关闭历史数据读取
        //关掉实时数据显示
        DataProcess.getFrameTimer.cancel();                 //关闭实时帧发送定时器
        //判断bin文件数据起始地址是否正确
        for(int i=0;i<tempFileData.length;i++){
            if(tempFileData[i] == 0x9D && tempFileData[i+1] == 0x41 && tempFileData[i+2] == 0x00 && tempFileData[i+3] == 0x08){
                tempFileData = DataProcess.remove(0,i-4,tempFileData);
            }
        }
        //设置按钮为正在升级
        upgradeBt.setText(R.string.upgrading);
        //设置进度条可见，且最大值为100
        upgradeProgress.setMax(tempFileData.length);
        upgradeProgress.setMin(0);
        upgradeProgress.setVisibility(View.VISIBLE);

        //初始化变量
        binFileSendCmdFlag = 1;
        binFileWaitFlag = false;
        frameNum = 0;                           //发送帧的数目
        DataProcess.upgradeRetFlag = false;     //文件接收状态标志
        index = 0;                              //文件数组下标，随着帧发送增加
        fileAddrCnt = 0;                        //bin文件帧地址，用于判断
        //开启定时器
        startupSendTimer(tempFileData,tempFileData.length);
    }

    /******************************************
     *   函数描述：发送文件数据
     *   parameter：
     *   return ：
     *******************************************/
    @SuppressLint("HandlerLeak")
    private void startupSendTimer(byte[] array,int fileLen){
        byte[] FileArray = array;
        binFileTimer = new Timer();
        binFileSendFrameHandler = new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case 1://发送进入bootload指令
                    {
                        if (!binFileWaitFlag) {
                            DataProcess.pack_sendData(null, 0x01, 0x8000, 0x00,0);
                            Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.send enter into bootload instruct");
                            mContext.sendBroadcast(intent);
                            binFileWaitFlag = true;
                            frameNum++;
                            whilecnt = 0;
                        }
                        whilecnt++;
                        if (whilecnt > 100) {
                            binFileWaitFlag = false;
                            if (frameNum > 5) {
                                Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERBOOT");
                                mContext.sendBroadcast(intent);
                                binFileTimer.cancel();  //关闭定时器
                                return;
                            }
                        }
                        if (DataProcess.dataStartAddr == 0x8000) {        //等到回复帧
                            //binFileContent.append("dataArray[10]："+DataProcess.dataArray[10]);
                            if (DataProcess.upgradeRetFlag) {
                                DataProcess.upgradeRetFlag = false;
                                binFileWaitFlag = false;//成功则是为了后面的帧能发，失败则是为了再发一帧
                                //Toast.makeText(getContext(),"进入bootload超时",Toast.LENGTH_LONG).show();
                                Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.have entered into bootload");
                                mContext.sendBroadcast(intent);
                                frameNum = 0;
                                binFileSendCmdFlag = 2;
                                break;
                            } else {
                                if (frameNum > 5) //第五次帧是错误
                                {
                                    Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERBOOTRETFAILED");
                                    mContext.sendBroadcast(intent);
                                    binFileTimer.cancel();  //关闭定时器
                                    return;
                                }
                            }
                        }
                    }
                    break;
                    case 2://擦除Flash指令
                    {
                        if (!binFileWaitFlag) //如果没收到回复帧，即dataStartAddr != "8000"
                        {
                            //发送帧
                            DataProcess.pack_sendData(null, 0x01, 0x8001, 0x00,0);
                            Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.sent earsed FLASH instruct");
                            mContext.sendBroadcast(intent);
                            binFileWaitFlag = true;
                            frameNum++;
                            whilecnt = 0;
                        }
                        whilecnt++;
                        if (whilecnt > 100) {
                            binFileWaitFlag = false;
                            if (frameNum > 5) //第三次都没收到回复
                            {
                                Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERCASHFALSHFAILED");
                                mContext.sendBroadcast(intent);
                                binFileTimer.cancel();  //关闭定时器
                                return;
                            }
                        }
                        if (DataProcess.dataStartAddr == 0x8001) {
                            if (DataProcess.upgradeRetFlag) {
                                DataProcess.upgradeRetFlag = false;
                                binFileWaitFlag = false;
                                //ui->label_upHint->setText("Earse Flash Successfully!");
                                Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.Earsed FLASH successfully");
                                mContext.sendBroadcast(intent);
                                frameNum = 0;
                                binFileSendCmdFlag = 3;
                                break;
                            } else {
                                if (frameNum > 5) //第三次帧是错误
                                {
                                    Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.OPERATEERASHFLASHFAILED");
                                    mContext.sendBroadcast(intent);
                                    binFileTimer.cancel();  //关闭定时器
                                    return;
                                }
                            }
                        }
                        break;
                    }
                    case 3://下载bin文件
                    {
                        if(index > fileLen)
                        {
                            return ;
                        }
                        if(!binFileWaitFlag)
                        {
                            //发送帧
                            if(((fileLen-index)>0 && (fileLen-index)<128) || index+128 == fileLen )  //判断是否最后一帧
                            {
                                DataProcess.pack_sendData(FileArray,1,0x9FFF,fileLen-index,index);//发送最后一帧
                                upgradeProgress.setProgress(fileLen-index);         //设置进度条值
                            }else{
                                Log.e(TAG, "已发送: "+ToastUtil.currentTime()+Integer.toHexString(0x9001+fileAddrCnt));
                                DataProcess.pack_sendData(FileArray,1,0x9001+fileAddrCnt,128,index);
                                upgradeProgress.setProgress(index);                 //设置进度条值
                                if(index == 0){
                                    Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.sending bin file data");
                                    mContext.sendBroadcast(intent);
                                }
                            }
                            frameNum++;
                            binFileWaitFlag = true;
                            whilecnt = 0;
                        }
                        whilecnt++;
                        if(whilecnt > 100)
                        {
                            binFileWaitFlag = false;
                            if(frameNum > 4) //第三次都没收到回复
                            {
                                Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.ENTERLOADFILRFAILED");
                                mContext.sendBroadcast(intent);
                                binFileTimer.cancel();  //关闭定时器
                                return;
                            }
                        }
                        if((DataProcess.dataStartAddr == (0x9001+fileAddrCnt)) || (DataProcess.dataStartAddr == 0x9FFF))
                        {
                            if(DataProcess.upgradeRetFlag)
                            {
                                DataProcess.upgradeRetFlag = false;
                                binFileWaitFlag = false;
                                frameNum = 0;
                                fileAddrCnt++;
                                //ui->label_upHint->setText("Bin File Data Downloading");
                                if(((fileLen-index)>0 && (fileLen-index)<128) || index+128 == fileLen )  //判断是否最后一帧
                                {
                                    //ui->label_upHint->setText("Last FileData Frame Erase Operation Successfully!");
                                    binFileSendCmdFlag = 4;             //最后一帧成功跳入下一个指令
                                    Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.Bin file data load successfully");
                                    mContext.sendBroadcast(intent);
                                    break;
                                }
                                index = index + 128;
                            }else{
                                if(frameNum > 5)                        //第五次帧是错误
                                {
                                    Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.OPERATELOADFILEFAILED");
                                    mContext.sendBroadcast(intent);
                                    binFileTimer.cancel();              //关闭定时器
                                    return;
                                }
                            }
                        }

                    }
                    break;
                    case 4://文件校验
                    {
                        if(!binFileWaitFlag) //如果没收到回复帧，即dataStartAddr != "8000"
                        {
                            //发送帧
                            DataProcess.pack_sendData(null, 0x01, 0x8002, 0x00,0);
                            Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.file check frame has sent");
                            mContext.sendBroadcast(intent);
                            frameNum++;
                            binFileWaitFlag = true;
                            whilecnt = 0;
                        }
                        whilecnt++;
                        if(whilecnt > 50)
                        {
                            binFileWaitFlag = false;
                            if(frameNum > 5) //第三次都没收到回复
                            {
                                Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.CHECKFLASHFAILED");
                                mContext.sendBroadcast(intent);
                                binFileTimer.cancel();  //关闭定时器
                                return;
                            }
                        }
                        if(DataProcess.dataStartAddr == 0x8002)
                        {
                            if(DataProcess.upgradeRetFlag)
                            {
                                DataProcess.upgradeRetFlag = false;
                                binFileWaitFlag = false;
                                binFileSendCmdFlag = 5;
                                break;
                            }
                            else{
                                if(frameNum > 5) //第三次帧是错误
                                {
                                    Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.OPERATECHECKFLASHFAILED");
                                    mContext.sendBroadcast(intent);
                                    binFileTimer.cancel();  //关闭定时器
                                    return;
                                }
                            }
                        }
                    }
                    break;
                    case 5://提示升级成功
                        //Toast.makeText(getContext(),"升级成功",Toast.LENGTH_LONG).show();
                        Intent intent = new Intent("com.example.bmsble.ui.notifications.NotificationsFragment.UPGARDESCUESS");
                        mContext.sendBroadcast(intent);
                        binFileTimer.cancel();  //关闭定时器
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + msg.what);
                }

                super.handleMessage(msg);
            }
        };
        binFileTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                switch (binFileSendCmdFlag) {
                    case 1://发送进入bootload指令
                        message.what = 1;
                        break;
                    case 2://擦除Flash指令
                        message.what = 2;
                        break;
                    case 3://校验flash指令
                        message.what = 3;
                        break;
                    case 4://下载文件数据
                        message.what = 4;
                        break;
                    case 5://下载文件数据
                        message.what = 5;
                        break;
                }
                binFileSendFrameHandler.handleMessage(message);
            }
        },0,10);
    }
    /******************************************
     *   类描述：用于升级模块下载bin文件的一些信息提示     *
     *******************************************/
    public class MyBoradcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
//            binFileTimer.cancel();                          //关闭定时线程(发送广播的时候已关闭)
            switch (intent.getAction()){
                case "com.example.bmsble.ui.notifications.NotificationsFragment.ENTERBOOT":
                    binFileContent.append(ToastUtil.currentTime()+"Enter the Bootload timeout"+"\n");
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.ENTERBOOTRETFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Enter the Bootload check or operation failure"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.ENTERCASHFALSHFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Erase flash timeout"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.OPERATEERASHFLASHFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Erase flash check or operation failure"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.ENTERLOADFILRFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Bin-file data timeout"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.OPERATELOADFILEFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Bin-file data check failure"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.CHECKFLASHFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Bin-file data check timeout"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.OPERATECHECKFLASHFAILED":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Bin-file data check failure"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.UPGARDESCUESS":
                    upgradeBt.setText(R.string.upgradeBt);                        //设置按钮文本为“升级”
                    upgradeProgress.setVisibility(View.GONE);       //设置进度条不可见
                    binFileContent.append(ToastUtil.currentTime()+"Upgrade successfully"+"\n");
                    Toast.makeText(mContext,"Upgrade successfully",Toast.LENGTH_LONG).show();
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.send enter into bootload instruct":
                    binFileContent.append(ToastUtil.currentTime()+"Sent enter-into bootload instruct"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.have entered into bootload":
                    binFileContent.append(ToastUtil.currentTime()+"Has entered into bootload"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.sent earsed FLASH instruct":
                    binFileContent.append(ToastUtil.currentTime()+"Sent earsed FLASH instruct"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.Earsed FLASH successfully":
                    binFileContent.append(ToastUtil.currentTime()+"Earsed FLASH successfully"+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.sending bin file data":
                    binFileContent.append(ToastUtil.currentTime()+" sending bin file data..."+"\n");
                    break;
                case "com.example.bmsble.ui.notifications.NotificationsFragment.Bin file data load successfully":
                    binFileContent.append(ToastUtil.currentTime()+"Bin file data load successfully"+"\n");
                    break;
            }
        }
    }
}
