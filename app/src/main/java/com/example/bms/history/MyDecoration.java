package com.example.bms.history;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

//drawable----------------------divider.xml
//style.name.listDivider--------@drawable/divider
//public static final int[] ATTRS = new int[]{android.R.attr.listDivider};
public class MyDecoration extends RecyclerView.ItemDecoration {
    private Context mContext;
    private Drawable mDivider;
    private int mOrientation;
    public static final int HORIZAONTAL_LIST = LinearLayoutManager.HORIZONTAL;
    public static final int VerTICAL_LIST = LinearLayoutManager.VERTICAL;

    public static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    public MyDecoration(Context context, int orientation){
        this.mContext = context;
        @SuppressLint("Recycle") final TypedArray ta = context.obtainStyledAttributes(ATTRS);
        this.mDivider = ta.getDrawable(0);
        ta.recycle();
        mOrientation = orientation;
    }
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
        drawHorizontalLine(c,parent,state);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if(mOrientation == HORIZAONTAL_LIST){
            //画横线，就是往下偏移一个分割线的高度
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        }else {
            //画竖线，就是往右偏移一个分割线的宽度
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }

    }
    public void drawHorizontalLine(Canvas c,RecyclerView parent,RecyclerView.State state){
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for(int i=0;i<childCount;i++){
            final View child = parent.getChildAt(i);
            //获取child的布局信息
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int botton = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left,top,right,botton);
            mDivider.draw(c);
        }
    }
    //画竖线
    public void drawVerticalLine(Canvas c, RecyclerView parent, RecyclerView.State state){
        int top = parent.getPaddingTop();
        int bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++){
            final View child = parent.getChildAt(i);

            //获得child的布局信息
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams)child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }

}
