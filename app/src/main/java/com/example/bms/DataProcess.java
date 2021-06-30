package com.example.bms;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bms.BLE.BLEFragment;
import com.example.bms.history.HistoryQueryFragment;
import com.example.bms.history.RecyclerViewAdapter;
import com.example.bms.other.ToastUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class DataProcess extends BaseActivity {
    private static final String TAG = "DataProcess";
    @SuppressLint("StaticFieldLeak")
    public static Context mContext;
    public static volatile boolean frameFlag = false;
    public static volatile boolean headFlag = false;
    public static volatile byte frameHead = 0x55;
    public static volatile int index = 0;

    public static volatile int[] mainInfoArray = new int[50];            //主要信息数组（除了产品信息，包括告警信息）0x2000~0x2029
    public static volatile int[] productInfoArray = new int[70];         //产品信息0x2076~0x20AB
    public static volatile int[] volArray = new int[20];                 //均衡状态，单节电压，温度0x2100~0x2100+3+BATStrings+TempNum
    public static volatile int[] historyExtremumArray = new int[90];

    public static volatile boolean upgradeRetFlag = false;

    public static volatile Timer getFrameTimer = new Timer();            //该定时用于循环发送问询帧
    public static volatile int sendFrameFlag = 1;                        //用于切换发送问询帧

    public static volatile byte TempNum;                                 //温度点数
    public static volatile byte BATStrings;                              //电池串数

    public static volatile int dataStartAddr;                            //一帧数据的起始地址
    public static volatile int dataLen;                                  //一帧的有效数据长度
    public static volatile int crcRx;                                    //接收的校验码
    public static volatile int crcCalcul;                                //计算得出的校验码
    //历史数据
    public static volatile boolean finishFlag = false;
    public static volatile int frameNum = -1;

    public synchronized void getFullFrame(byte[] array, int len){
        byte[] dataArray = new byte[200];                           //储存完整帧的数组
        if(frameFlag){
            Log.e(TAG, "进入到解析前的if处理");
            frameHead = 0x55;
            index = 0;
            headFlag = false;
            frameFlag = false;
        }else{
            for(int i=0;i<len;i++){
                if(!headFlag){
                    if(frameHead != array[i]){
                        index = 0;
                        frameHead = 0x55;
                        if(array[i] == 0x55){
                            dataArray[index++] = array[i];
                            frameHead = (byte) 0xAA;
                        }
                    }else{
                        dataArray[index++] = array[i];
                        switch (index){
                            case 1: frameHead = (byte) 0xAA;
                                break;
                            case 2: frameHead = 0x00;
                                break;
                            case 3: frameHead = 0x00;
                                break;
                            case 4: frameHead = (byte) 0xAA;
                                break;
                            case 5: frameHead = 0x55;
                                break;
                        }
                        if(index == 6){         //收到完整帧头
                            headFlag = true;
                        }
                    }
                }else{      //捕捉到完整帧头
                    dataArray[index++] = array[i];
                    if(index > dataArray.length || index == dataArray.length){
                        frameFlag = false;
                        frameHead = 0x55;
                        index = 0;
                        headFlag = false;
                        return;
                    }
                    //这时dataVector接收完成了6byte帧头、1byte读写、2byte数据起始地址、
                    // 1byte数据长度当index=9时，dataVector[9]还没有被赋值
                    if(index == 10){
                        dataStartAddr =  (((dataArray[8] << 8) & 0xFF00) + (dataArray[7] & 0xFF));
                        Log.e(TAG, "数据地址："+Integer.toHexString(dataStartAddr));
                        dataLen = dataArray[9] & 0xFF;
                        Log.e(TAG, "数据长度："+Integer.toHexString(dataLen));
                        if((dataLen + 12) > dataArray.length){
                            frameFlag = false;
                            frameHead = 0x55;
                            index = 0;
                            headFlag = false;
                            return;
                        }
                    }
                    if(index == (12 + dataLen)){
                        crcRx =  ((dataArray[index-1] << 8) & 0xFF00) + (dataArray[index-2] & 0xFF);       //接收到的校验
                        byte[] bytes = new byte[index];
                        System.arraycopy(dataArray,0,bytes,0,index);
                        crcCalcul = calculCRC16(bytes = remove(0,6,bytes),4+dataLen);   //帧头不加入校验
                        if(dataStartAddr >= 0x4000 && dataStartAddr <= 0x4FFF){
                            frameFlag = true;
                        }else{
                            if((crcCalcul & 0xFFFF) == crcRx){         //校验成功
                                frameFlag = true;
                                Log.e(TAG, "getFullFrame: "+"校验成功"+" 地址："+Integer.toHexString(dataStartAddr));
                            }else{
                                Log.e(TAG, "getFullFrame: "+"校验失败"+" 地址："+Integer.toHexString(dataStartAddr));
                                frameHead = 0x55;
                                index = 0;
                                headFlag = false;
                                frameFlag = false;
                                return;
                            }
                        }
                    }

                    if(frameFlag){
                        frameHead = 0x55;
                        index = 0;
                        headFlag = false;
                        frameFlag = false;
                        Log.e(TAG, "getFullFrame: 相关数据复位初始");
                        if(dataStartAddr >= 0x8000){        //如果回复帧是属于bootload
                            Log.e(TAG, "进入地址大于0x8000 函数解析");
                            if(dataArray[10] != 0x00){
                                upgradeRetFlag = false;
                            }else{
                                upgradeRetFlag = true;
                            }
                        }else if(dataStartAddr >= 0x4000 && dataStartAddr <= 0x4FFF){
                            Log.e(TAG, "进入历史数据解析");
                            if(dataStartAddr == 0x4FFF){
                                Log.e(TAG, "dataStartAddr == 0x4FFF");
                                if(dataArray[10] == -1 && dataArray[11] == -1 && dataArray[12] == -1 && dataArray[13] == -1){
                                    finishFlag = true;
                                    Intent intent = new Intent("have a history data");
                                    mContext.sendBroadcast(intent);
                                    Log.e(TAG, "历史数据：已收到最后一帧");
                                }else{
                                    frameNum = (dataArray[13] & 0xFF) << 24  | (dataArray[12] & 0xFF) << 16 | (dataArray[11] & 0xFF) << 8 | dataArray[10] & 0xFF;      //总帧数
                                    Log.e(TAG, "总帧数："+Integer.toHexString(frameNum));
                                }
                            }else{
                                HistoryQueryFragment.History_ProcessData(dataArray,65+12);
                            }
                        }else{
                            Log.e(TAG, "进入实时数据帧解析: "+Integer.toHexString(dataStartAddr));
                            realDataAnaly(dataArray);
                        }
                    }
                }

            }
        }

    }
    /******************************************
    *   函数描述：实时数据解析
    *   parameter：
    *   return ：
    *******************************************/
    public static void realDataAnaly(byte[] dataArray){
        int[] tempArray = new int[90];      //tempArray数组的大小必须大于mainInfoArray，productInfoArray，volArray的长度
        if(dataLen > tempArray.length*2)        //字节比较
            return;
        //去除帧头等无效数据
        for(int i=0,j=0;i<dataLen;i=i+2,j++){
            //6byte帧头，1byte读写指令，2字节数据地址，1字节数据长度
            tempArray[j] = (dataArray[10+i] & 0xFF) + ((dataArray[10+i+1]) << 8 & 0xFF00);                    //小端模式
        }
        switch (dataStartAddr){
            case 0x2000://0x2000~0x2029
                Log.e(TAG, "realDataAnaly: "+"接收到 0x2000"+"\n");
                System.arraycopy(tempArray,0,mainInfoArray,0,mainInfoArray.length);             //临时数组复制到目标数组
                BATStrings = (byte) ((mainInfoArray[0x2027-0x2000] >> 8) & 0xFF);                              //电池串数赋值
                Log.e(TAG, "电池串数: "+BATStrings);
                break;
            case 0x2076://0x2076~0x20AB
                Log.e(TAG, "realDataAnaly: "+"接收到 0x2076"+"\n");
                System.arraycopy(tempArray,0,productInfoArray,0,productInfoArray.length);       //临时数组复制到目标数组
                break;
            case 0x2032://温度点数
                Log.e(TAG, "realDataAnaly: "+"接收到 0x2032"+"\n");
                TempNum = (byte) (tempArray[0] >> 8 & 0xFF);                                                                         //温度点数赋值
                Log.e(TAG, "温度点数: "+TempNum);
                break;
            case 0x2100://均衡状态，单节电压，电池温度
                Log.e(TAG, "realDataAnaly: "+"接收到 0x2100"+"\n");
                System.arraycopy(tempArray,0,volArray,0,volArray.length);
                break;
            case 0x5000://历史数据极值
                Log.e(TAG, "realDataAnaly: "+"接收到 0x5000"+"\n");
                System.arraycopy(tempArray,0,historyExtremumArray,0,historyExtremumArray.length);
                break;
        }

    }
    /******************************************
    *   函数描述：16校验计算
    *   parameter：
    *   return ：
    *******************************************/
    public static int calculCRC16(byte[] buff,int len){
        short poly = 0x1021;
        crcCalcul = (short) 0xFFFF;
        int i=0;
        while(len != 0){
            len--;
            int j;
            crcCalcul ^= buff[i++] << 8;
            for(j=0;j<8;j++){
                crcCalcul = (short) (((crcCalcul & 0x8000) !=0)  ? ((crcCalcul << 1) ^ poly) : (crcCalcul << 1));
            }
        }
        return crcCalcul;
    }
    /******************************************
    *   函数描述：打包读取数据帧
    *   parameter：
    *   return ：
    *******************************************/
    public static void pack_sendData(int addr,int len){
        byte[] tempArray = new byte[12];
        tempArray[0] = 0x55;
        tempArray[1] = (byte) 0xAA;
        tempArray[2] = 0x00;
        tempArray[3] = 0x00;
        tempArray[4] = (byte) 0xAA;
        tempArray[5] = 0x55;
        tempArray[6] = 0x00;
        tempArray[7] = (byte) (addr & 0xFF);
        tempArray[8] = (byte) (addr >> 8);
        tempArray[9] = (byte) len;
        byte[] bytes = new byte[10];
        System.arraycopy(tempArray,0,bytes,0,10);           //将字节数组tempArray复制到bytes
        int crcCalcul = calculCRC16(bytes = DataProcess.remove(0,6,bytes),4);  //去除帧头进行校验
        byte crc_L = (byte) ((crcCalcul >> 8) & 0xFF);
        byte crc_H = (byte) (crcCalcul & 0xFF);
        tempArray[10] = crc_H;
        tempArray[11] = crc_L;
        /* 蓝牙读取设备数据*/
        BLEFragment.BLEWriteData(tempArray,12);
    }
    /******************************************
     *   函数描述：写入一个字
     *   parameter：写入数组
     *   return ：
     *******************************************/
    public static void pack_sendWord(int addr,short data){
        byte[] tempArray = new byte[14];
        tempArray[0] = 0x55;
        tempArray[1] = (byte) 0xAA;
        tempArray[2] = 0x00;
        tempArray[3] = 0x00;
        tempArray[4] = (byte) 0xAA;
        tempArray[5] = 0x55;
        tempArray[6] = 0x01;
        tempArray[7] = (byte) (addr & 0xFF);
        tempArray[8] = (byte) (addr >> 8);
        tempArray[9] = 0x02;
        tempArray[10] = (byte) (data & 0xFF);
        tempArray[11] = (byte) (data >> 8);
        byte[] buff = new byte[12];
        System.arraycopy(tempArray,0,buff,0,12);
        int crcCalcul = calculCRC16(buff = remove(0,6,buff),6);  //去除帧头进行校验
        byte crc_L = (byte) ((crcCalcul >> 8) & 0xFF);
        byte crc_H = (byte) (crcCalcul & 0xFF);
        tempArray[12] = crc_H;
        tempArray[13] = crc_L;
        /* 蓝牙读取设备数据*/
        BLEFragment.BLEWriteData(tempArray,14);
    }
    /******************************************
    *   函数描述：打包发送数据
    *   parameter：sendArray == null表示读指令else写指令 ，cmd 0（读）1（写），addr地址，len数据长度，index数据起始发送下标
    *   return ：
    *******************************************/
    public static void pack_sendData(byte[] sendArray,int cmd,int addr,int len,int index){
        byte[] tempArray = new byte[250];
        tempArray[0] = 0x55;
        tempArray[1] = (byte) 0xAA;
        tempArray[2] = 0x00;
        tempArray[3] = 0x00;
        tempArray[4] = (byte) 0xAA;
        tempArray[5] = 0x55;
        tempArray[6] = (byte) cmd;
        tempArray[7] = (byte) (addr & 0xFF);
        tempArray[8] = (byte) (addr >> 8);
        tempArray[9] = (byte) len;
        if(sendArray != null && len < sendArray.length){
            System.arraycopy(sendArray,index,tempArray,10,len);         //将sendArray字节数组复制到tempArray字节数组
        }
        byte[] bytes = new byte[250];
        System.arraycopy(tempArray,0,bytes,0,10+len);           //将字节数组tempArray复制到bytes
        int crcCalcul = calculCRC16(bytes = DataProcess.remove(0,6,bytes),10+len-6);  //去除帧头进行校验
        byte crc_L = (byte) ((crcCalcul >> 8) & 0xFF);
        byte crc_H = (byte) (crcCalcul & 0xFF);
        tempArray[10+len] = crc_H;
        tempArray[11+len] = crc_L;
        /* 蓝牙把数据写入设备*/
        BLEFragment.BLEWriteData(tempArray,len+12);
    }

    /******************************************
    *   函数描述：       发送问询帧定时器（先发送查询电池串数 和 温度点数）索要的数据长度不得大于60
    *   parameter：
    *   return ：
    *******************************************/
    public static void getData(){
        Log.e(TAG, "getData: ");
        sendFrameFlag = 1;                  //因为要先发送电池串数和温度点数，所以必须有先后
        getFrameTimer = new Timer();
        getFrameTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                switch (sendFrameFlag){
                    case 1: sendFrameFlag++;        //包含电池串数
                        Log.e(TAG, "run: send 2000");
                        pack_sendData(0x2000,0x2A*2);
                        break;
                    case 2:sendFrameFlag++;         //包含温度点数
                        Log.e(TAG, "run: send 2032");
                        pack_sendData(0x2032,0x02);
                        break;
                    case 3:sendFrameFlag++;
                        Log.e(TAG, "run: send 2076");
                        pack_sendData(0x2076,(0x20BA-0x2076+1)*2);
                        break;
                    case 4:sendFrameFlag++;
                        Log.e(TAG, "run: send 2100");
                        pack_sendData(0x2100,(BATStrings/16+1+BATStrings)*2+TempNum);
                        break;
                    case 5:sendFrameFlag = 1;
                        Log.e(TAG, "run: send 5000");
                        pack_sendData(0x5000,180);
                        break;
                }
            }
        },10,500);  //延迟10ms执行，之后每500ms执行一次
    }

    /******************************************
     *   函数描述：      在一个字节数组中从指定位置startIndex删除一个长度len数据
     *   parameter：startIndex 字节数组的开始开标  len删除的数组长度
     *   return ：
     *******************************************/
    public static byte[] remove(int startIndex, int len, byte[] byteArray){
        if(startIndex+len > byteArray.length)
            return byteArray;
        for(int i=0;i<byteArray.length-startIndex;i++){        //把挖空部分用后面的补上，最后部分补0
            if(startIndex+len+1+i > byteArray.length){
                byteArray[startIndex+i] = 0x00;
            }else{
                byteArray[startIndex+i] = byteArray[startIndex+len+i];
            }
        }
        byte[] array = new byte[byteArray.length-len];
        for(int i=0;i<byteArray.length-len;i++){
            array[i] = byteArray[i];
        }
        return array;
    }
    /******************************************
    *   函数描述：       整型数组转字符串
    *   parameter：
    *   return ：
    *******************************************/
    public static String IntArrayToASCIIString(int[] array,int index,int len){
        if(array == null || index+len > array.length){
            return null;
        }
        int[] tempArray = new int[len];
        System.arraycopy(array,index,tempArray,0,len);
        StringBuilder str = new StringBuilder();
        for(int i=0;i<len;i++){
            str.append((char)(tempArray[i] & 0xFF));
            str.append((char)((tempArray[i] >> 8) & 0xFF));
        }
        return str.toString();
    }

}


