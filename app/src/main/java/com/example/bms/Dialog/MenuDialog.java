package com.example.bms.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.bms.R;

public class MenuDialog extends Dialog {
    //    private TextView mLanguage;
    public OnUpgradeClickListener onUpgradeClickListener;
    public OnHistoryClickListener onHistoryClickListener;
//    public OnLanguageClickListener onLanguageClickListener;
//
//    public void setOnLanguageClickListener(OnLanguageClickListener listener){
//        onLanguageClickListener = listener;
//    }
    public void setOnHistoryClickListener(OnHistoryClickListener listener){
        onHistoryClickListener = listener;
    }
    public void setOnUpgradeClickListener(OnUpgradeClickListener listener){
        onUpgradeClickListener = listener;
    }
    public MenuDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_content);
//        mLanguage = findViewById(R.id.menu_language);
//        mLanguage.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onLanguageClickListener.onLanguageClick();
//            }
//        });
        TextView mUpgrade = findViewById(R.id.menu_upgrade);
        mUpgrade.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUpgradeClickListener.onUpgradeClick();
            }
        });
        TextView mHistory = findViewById(R.id.menu_history);
        mHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onHistoryClickListener.onHistoryClick();
            }
        });
    }

//    public interface OnLanguageClickListener{
//        public void onLanguageClick();
//    }
    public interface OnUpgradeClickListener{
        public void onUpgradeClick();
    }
    public interface OnHistoryClickListener{
        public void onHistoryClick();
    }
}
