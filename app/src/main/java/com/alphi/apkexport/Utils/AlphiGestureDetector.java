package com.alphi.apkexport.Utils;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class AlphiGestureDetector extends GestureDetector.SimpleOnGestureListener {
    private static final int MIN_DISTANCE = 80;
    private static final String TAG = AlphiGestureDetector.class.getSimpleName();

    public void onFlingUp(){
        Log.d(TAG, "onFlingUp: 上滑");
    }

    public void onFlingDown(){
        Log.d(TAG, "onFlingDown: 下滑");
    }

    public void onFlingLeft(){
        Log.d(TAG, "onFlingLeft: 左滑");
    }

    public void onFlingRight(){
        Log.d(TAG, "onFlingRight: 右滑");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d(TAG, "onFling: running");
        if(e1.getX()-e2.getX()>MIN_DISTANCE)
            onFlingLeft();
        else if(e2.getX()-e1.getX()>MIN_DISTANCE)
            onFlingRight();
        else if(e1.getY()-e2.getY()>MIN_DISTANCE)
            onFlingUp();
        else if(e2.getY()-e1.getY()>MIN_DISTANCE)
            onFlingDown();
        return true;
    }
}
