package com.example.bms.other;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class ToastUtil {
    private static Toast toast = null;

    @SuppressLint("ShowToast")
    public static void show(Context context, String text) {
        try {
            if (toast != null) {
                toast.setText(text);
            } else {
                toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            }
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
            //解决在子线程中调用Toast的异常情况处理
            Looper.prepare();
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }
    public static String currentTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS ");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
}
