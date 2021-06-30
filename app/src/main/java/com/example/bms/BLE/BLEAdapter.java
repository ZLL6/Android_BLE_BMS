package com.example.bms.BLE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.bms.R;


import java.util.List;

public class BLEAdapter extends BaseAdapter {
    private final Context mContext;
    private final List<BluetoothDevice> mBluetoothDevices;
    private final List<Integer> mRssis;

    public BLEAdapter(Context mContext, List<BluetoothDevice> bluetoothDevices, List<Integer> rssis) {
        this.mContext = mContext;
        this.mBluetoothDevices = bluetoothDevices;
        mRssis=rssis;
    }

    @Override
    public int getCount() {
        return mBluetoothDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mBluetoothDevices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    /******************************************
    *   函数描述：listview是一个特殊的控件，他需要的item是由对应的adapter来生成，
     *   但是它显示到界面上是和其他的是一样的也就是说getview()一般会在，
     *   1.第一次显示，
     *   2.页面刷新，
     *   3.或者当adapter对应的数据源变化时候会主动调用
     *   4.notifyDataSetChanged等方法主动刷新界面时被调用，
     *   *********也就是当listview需要显示item的时候就会调用getview()********
    *   return ：
    *******************************************/
    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.ble_devices_list_item, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        BluetoothDevice device = (BluetoothDevice) getItem(position);
        viewHolder.name.setText(device.getName());
        viewHolder.introduce.setText(device.getAddress());
        viewHolder.tvRssi.setText(mRssis.get(position)+"");
        return convertView;
    }


    class ViewHolder {
        public TextView name;
        public TextView introduce;
        public TextView tvRssi;
        public ViewHolder(View view) {
            name = view.findViewById(R.id.name);
            introduce =  view.findViewById(R.id.introduce);
            tvRssi=view.findViewById(R.id.rssi);
        }
    }
}
