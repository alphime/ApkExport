package com.alphi.myapplication.widget;

import android.os.Handler;
import android.os.SystemClock;
import android.view.View;

public class OnClickEvent implements View.OnClickListener {;
    /**
     * 判断是否是快速点击
     */

    private int clickNum;
    private final OnClickListener onClickCallback;
    private final int interval = 250;
    private final Handler handler = new Handler();
    private final Object obj = new Object();

    @Override
    public void onClick(View v) {
        clickNum++;
        handler.postAtTime(new Runnable() {
            @Override
            public void run() {
                if (onClickCallback != null) {
                    if (clickNum > 1) {
                        onClickCallback.onDoubleClick();
                    }else {
                        onClickCallback.onSingleClick();
                    }
                }
                handler.removeCallbacksAndMessages(obj);
                clickNum = 0;
            }
        }, obj, SystemClock.uptimeMillis() + interval);
    }

    public interface OnClickListener {
        void onSingleClick();

        void onDoubleClick();
    }

    public OnClickEvent(OnClickListener onClickCallback) {
        super();
        this.onClickCallback = onClickCallback;
    }
}
