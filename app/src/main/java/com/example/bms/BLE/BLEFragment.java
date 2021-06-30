package com.example.bms.BLE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.bms.DataProcess;
import com.example.bms.R;
import com.example.bms.other.ToastUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;
@SuppressLint("NewApi")
public class BLEFragment extends Fragment {
    private static final String TAG = "BLEFragment";
    public DataProcess dataProcess;
    private View view;
    ProgressBar pbSearchBle;
    ImageView ivSerBleStatus;
    TextView tvSerBindStatus;
    ListView bleListView;
    //写入BLE数据按钮
    private List<BluetoothDevice> mDatas;
    private List<Integer> mRssis;

    private ScanCallback scan_Callback;
    private BluetoothLeScanner scanner;
    public BLEAdapter mAdapter;
    public BluetoothDevice mBluetoothDevice;
    public BluetoothAdapter mBluetoothAdapter;
    private boolean isScaning=false;
    private boolean isConnecting=false;
    public static boolean BLEConnectState = false;     //蓝牙连接状态
    //定义重连次数
    private int reConnectionNum = 0;
    public static BluetoothGatt mBluetoothGatt;

    //位置服务请求码
    final static int REQUEST_LOCATION_PERMISSION = 1;
    final static int REQUEST_BLEADAPTER_PERMISSION = 0;

    //服务和特征值
    public static UUID write_UUID_service;
    public static UUID write_UUID_chara;
    public UUID read_UUID_service;
    public UUID read_UUID_chara;
    public UUID notify_UUID_service;
    public UUID notify_UUID_chara;
    public UUID indicate_UUID_service;
    public UUID indicate_UUID_chara;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.ble_init,container,false);
        dataProcess = new DataProcess();
        init();
        return view;
    }
    /******************************************
    *   函数描述： 初始化
    *   parameter：
    *   return ：
    *******************************************/
    private void init(){
        /*使用此检查确定设备是否支持BLE。 然后你可以有选择地禁用与BLE相关的功能*/
        if(!Objects.requireNonNull(getActivity()).getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            Toast.makeText(getActivity(),"设备不支持低功耗蓝牙",Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
        final BluetoothManager mBluetoothManager= (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter=mBluetoothManager.getAdapter();
        scanner = mBluetoothAdapter.getBluetoothLeScanner();
            Log.e(TAG, "scanner（not null）: "+scanner);
        if (mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled()){
            Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent,REQUEST_BLEADAPTER_PERMISSION);
        }
        initView();
        initData();
    }

    public void initData() {
        mDatas=new ArrayList<>();
        mRssis=new ArrayList<>();
        mAdapter=new BLEAdapter(getContext(),mDatas,mRssis);
        bleListView.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();            //主动刷新界面
    }
    public void initView(){
        pbSearchBle = view.findViewById(R.id.progress_ser_bluetooth);
        ivSerBleStatus = view.findViewById(R.id.iv_ser_ble_status);
        tvSerBindStatus = view.findViewById(R.id.tv_ser_bind_status);
        bleListView = view.findViewById(R.id.ble_list_view);
        //扫描回调
        scan_Callback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.e(TAG, "onScanResult: ");
                if (!mDatas.contains(result.getDevice()) && (result.getDevice().getName() != null)){
                    mDatas.add(result.getDevice());
                    mRssis.add(result.getRssi());
                    mAdapter.notifyDataSetChanged();
                }
            }
        };
        //扫描设备
        ivSerBleStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isScaning){
                    tvSerBindStatus.setText(R.string.ble_StopSearch);
                    stopScanDevice();
                }else{
                    if(BLEConnectState && mBluetoothGatt != null){                  //如果已连接
                        mBluetoothGatt.disconnect();                                //先断开连接
                        mBluetoothGatt = null;
                        Intent intent = new Intent("Ble disconnected");
                        getActivity().sendBroadcast(intent);
                        Log.e(TAG, "mBluetoothGatt.disconnect()  mBluetoothGatt = null");
                    }
                    //检查蓝牙是否打开和位置是否打开
                    if (mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled()){
                        Intent intent=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(intent,REQUEST_BLEADAPTER_PERMISSION);
                    }
                    if (Build.VERSION.SDK_INT >= 23 && !isLocationOpen(Objects.requireNonNull(getContext()).getApplicationContext())) {
                        Log.e(TAG, "位置未打开: ");
                        Intent enableLocate = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivityForResult(enableLocate, REQUEST_LOCATION_PERMISSION);
                    }else{
                        Log.e(TAG, "进入扫描函数");
                        if(mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled())
                            scanDevice();
                    }
                }
            }
        });
        //listView 连接设备
        bleListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (isScaning){
                    Log.e(TAG, "正在搜索");
                    stopScanDevice();
                    Log.e(TAG, "已停止搜索");
                }
                if (!isConnecting){
                    Log.e(TAG, "正在连接");
                    isConnecting=true;
                    mBluetoothDevice = mDatas.get(position);
                    //连接设备
                    if(mBluetoothGatt != null){
                        mBluetoothGatt.disconnect();
                        mBluetoothGatt = null;
                        Intent intent = new Intent("Ble disconnected");
                        Objects.requireNonNull(getActivity()).sendBroadcast(intent);
                    }

                    tvSerBindStatus.setText(R.string.ble_Connecting);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mBluetoothGatt = mBluetoothDevice.connectGatt(getContext(),false,gattCallback,TRANSPORT_LE);
                    }else {
                        mBluetoothGatt = mBluetoothDevice.connectGatt(getContext(),false,gattCallback);
                    }
                }
            }
        });
    }
    /******************************************
     *   函数描述：  判断位置信息是否开启
     *   parameter：
     *   return ：
     *******************************************/
    public boolean isLocationOpen(final Context context){
        Log.e(TAG, "isLocationOpen: ");
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        //GPS定位
        boolean isGpsProvider = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //网络定位
        boolean isNetWorkProvider = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        return isGpsProvider || isNetWorkProvider;
    }
    /******************************************
     *   函数描述：开始扫描 10秒后自动停止
     *   parameter：
     *   return ：
     *******************************************/
    public void scanDevice(){
        if(scanner == null){
            Log.e(TAG, "scanner == null ");
            return;
        }
        tvSerBindStatus.setText(R.string.ble_Searching);
        isScaning=true;
        pbSearchBle.setVisibility(View.VISIBLE);
        if(bleListView.getVisibility() != View.VISIBLE){        //设置ListView可见
            bleListView.setVisibility(View.VISIBLE);
        }
        scanner.startScan(scan_Callback);
        Log.e(TAG, "scanDevice: ");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //结束扫描
                if (mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled())
                    return;
                scanner.stopScan(scan_Callback);
                Objects.requireNonNull(getActivity()).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isScaning=false;
                        pbSearchBle.setVisibility(View.GONE);
                        //可能在搜索一半的过程点击连接设备，连接上了，但是扫描周期到了却改变了连接状态
                        if(!tvSerBindStatus.getText().toString().equals(getString(R.string.ble_Connected))) {
                            tvSerBindStatus.setText(R.string.ble_SearchClosure);
                        }
                    }
                });
            }
        },10000);
    }
    /******************************************
     *   函数描述：停止扫描
     *   parameter：
     *   return ：
     *******************************************/
    private void stopScanDevice(){
        isScaning=false;
        pbSearchBle.setVisibility(View.GONE);
        if (mBluetoothAdapter==null||!mBluetoothAdapter.isEnabled())
            return;
        scanner.stopScan(scan_Callback);
    }
    /******************************************
     *   函数描述：权限请求返回结果处理函数
     *   parameter：
     *   return ：
     *******************************************/
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_LOCATION_PERMISSION){
            if(isLocationOpen(Objects.requireNonNull(getContext()).getApplicationContext())){
                if(ContextCompat.checkSelfPermission(getContext().getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED){
                    //请求权限
                    ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()),new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},REQUEST_LOCATION_PERMISSION);
                }
            }
        }else if (requestCode == REQUEST_BLEADAPTER_PERMISSION){
            if(resultCode == Activity.RESULT_OK){
                final BluetoothManager mBluetoothManager= (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter=mBluetoothManager.getAdapter();
                scanner = mBluetoothAdapter.getBluetoothLeScanner();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /******************************************
     *   函数描述：初始化服务和特征
     *   return ：
     *******************************************/
    private void initServiceAndChara(){
        List<BluetoothGattService> bluetoothGattServices= mBluetoothGatt.getServices();
        for (BluetoothGattService bluetoothGattService:bluetoothGattServices){
            List<BluetoothGattCharacteristic> characteristics=bluetoothGattService.getCharacteristics();
            for (BluetoothGattCharacteristic characteristic:characteristics){
                int charaProp = characteristic.getProperties();
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                    read_UUID_chara=characteristic.getUuid();
                    read_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"read_chara="+read_UUID_chara+"----read_service="+read_UUID_service);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                    write_UUID_chara=characteristic.getUuid();
                    write_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) {
                    write_UUID_chara=characteristic.getUuid();
                    write_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"write_chara="+write_UUID_chara+"----write_service="+write_UUID_service);

                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                    notify_UUID_chara=characteristic.getUuid();
                    notify_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"notify_chara="+notify_UUID_chara+"----notify_service="+notify_UUID_service);
                }
                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
                    indicate_UUID_chara=characteristic.getUuid();
                    indicate_UUID_service=bluetoothGattService.getUuid();
                    Log.e(TAG,"indicate_chara="+indicate_UUID_chara+"----indicate_service="+indicate_UUID_service);

                }
            }
        }
    }
    /******************************************
     *   函数描述：写入数据==========该函数会置于定时器，不得使用Toast,makeText()函数
     *   parameter：data：数据   len：长度
     *   return ：
     *******************************************/
    public static void BLEWriteData(byte[] data1,int len){
        if(mBluetoothGatt == null){
            return;
        }
        BluetoothGattService service=mBluetoothGatt.getService(write_UUID_service);
        if(service == null){
            DataProcess.getFrameTimer.cancel();
            return;
        }
        BluetoothGattCharacteristic charaWrite=service.getCharacteristic(write_UUID_chara);
        byte[] data = new byte[len];
        for(int i=0;i<len;i++){
            data[i] = data1[i];
        }
        if (len>20){//数据大于20个字节 分批次写入
            int num=0;  //分num次写入
            if (len%20!=0){
                num=len/20+1;
            }else{
                num=len/20;
            }
            for (int i=0;i<num;i++){
                byte[] tempArr;
                if (i==num-1){
                    tempArr=new byte[len-i*20];
                    System.arraycopy(data,i*20,tempArr,0,len-i*20);
                }else{
                    tempArr=new byte[20];
                    System.arraycopy(data,i*20,tempArr,0,20);
                }
                charaWrite.setValue(tempArr);
                mBluetoothGatt.writeCharacteristic(charaWrite);
                try {
                    Thread.sleep(3);                        //延时解决一股脑发送多包下位机接收不过来的情况
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.e(TAG, "BLEWriteData: "+bytes2hex(tempArr,tempArr.length));
            }
        }else{
            charaWrite.setValue(data);
            mBluetoothGatt.writeCharacteristic(charaWrite);
            Log.e(TAG, "BLEWriteData: "+bytes2hex(data,data.length));
        }
    }

    private static final String HEX = "0123456789ABCDEF";
    /******************************************
     *   函数描述：  字节数组转成字符串
     *   parameter： 字节数组
     *   return ：
     *******************************************/
    public static String bytes2hex(byte[] bytes,int len)
    {
        StringBuilder sb = new StringBuilder(len * 3);
        for (int i=0;i<len;i++)
        {
            // 取出这个字节的高4位，然后与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt((bytes[i] >> 4) & 0x0f));
            // 取出这个字节的低位，与0x0f与运算，得到一个0-15之间的数据，通过HEX.charAt(0-15)即为16进制数
            sb.append(HEX.charAt(bytes[i] & 0x0f));
            sb.append(" ");
        }
        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mBluetoothGatt != null){
            mBluetoothGatt.disconnect();
            Intent intent = new Intent("Ble disconnected");
            getActivity().sendBroadcast(intent);
        }
    }
    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            //操作成功的情况下
            if (status == BluetoothGatt.GATT_SUCCESS){
                //判断是否连接码
                if (newState == BluetoothGatt.STATE_CONNECTED) {
                    mBluetoothGatt.discoverServices();
                }else if(newState == BluetoothGatt.STATE_DISCONNECTED){
                    //判断是否断开连接码
                    Log.e(TAG, "已断开连接"+483);
                    BLEConnectState = false;
                    mDatas.clear();                                 //断开连接后需重新扫描获取设备列表，不使用旧列表，避免出现设备名==null
                    mRssis.clear();
                    if(getActivity() != null){                      //切换语言时可能为空
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvSerBindStatus.setText(R.string.BLE_disconnected);
                            }
                        });
                        Intent intent = new Intent("Ble disconnected");
                        if(getActivity() != null)
                            getActivity().sendBroadcast(intent);
                    }

                }
            }else{
                Log.e(TAG, "status != BluetoothGatt.GATT_SUCCESS ");
                //重连次数不大于最大重连次数
                //最多重连次数
//                int maxConnectionNum = 3;
//                if(reConnectionNum < maxConnectionNum){                     //出现升级过程中蓝牙会断开
//                    //重连次数自增
//                    reConnectionNum++;
//                    //连接设备
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                        mBluetoothGatt = mBluetoothDevice.connectGatt(getContext(),
//                                false, this, BluetoothDevice.TRANSPORT_LE);
//                    } else {
//                        mBluetoothGatt = mBluetoothDevice.connectGatt(getContext(), false, this);
//                    }
//                }else{
                    reConnectionNum = 0;
                    //断开连接，返回连接失败回调
                    mBluetoothGatt.disconnect();
                    mBluetoothGatt = null;
                    isConnecting=false;
                    BLEConnectState = false;
                    Intent intent = new Intent("Ble disconnected");
                    Objects.requireNonNull(getActivity()).sendBroadcast(intent);
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvSerBindStatus.setText(R.string.BLE_disconnected);
                        }
                    });
//                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //直到这里才是真正建立了可通信的连接
                isConnecting=false;
                //获取初始化服务和特征值
                initServiceAndChara();
                //订阅通知
                BluetoothGattCharacteristic characteristic = mBluetoothGatt
                        .getService(notify_UUID_service).getCharacteristic(notify_UUID_chara);
                mBluetoothGatt.setCharacteristicNotification(characteristic,true);
                BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                if (descriptor != null) {
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
                BLEConnectState = true;
                Intent intent = new Intent("BLE have connected");
                Objects.requireNonNull(getActivity()).sendBroadcast(intent);
                //设置界面
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run(){
                        if(BLEConnectState){
                            bleListView.setVisibility(View.GONE);
                            tvSerBindStatus.setText(R.string.ble_Connected);
                        }
                    }
                });
            }
        }
        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //获取写入到外设的特征值
                characteristic.getValue();
                Log.e(TAG, "回调发送："+bytes2hex(characteristic.getValue(),characteristic.getValue().length));
            }
        }
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            final byte[] data = characteristic.getValue();
            Log.e(TAG, ToastUtil.currentTime()+" 回调接收: "+bytes2hex(data,data.length));
            dataProcess.getFullFrame(data,data.length);
        }
    };

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden && BLEConnectState){            //当前fragment可见且蓝牙已连接

        }
    }
}
