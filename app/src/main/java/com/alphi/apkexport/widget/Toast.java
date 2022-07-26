package com.alphi.apkexport.widget;

/*
    author: alphi
    createDate: 2022/7/20
*/

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.alphi.apkexport.R;

public class Toast extends android.widget.Toast {
    @SuppressLint("StaticFieldLeak")
    private static Toast mToast;

    /**
     * Show the view or text notification for a short period of time.  This time
     * could be user-definable.  This is the default.
     *
     * @see #setDuration
     */
    public static final int LENGTH_SHORT = 0;

    /**
     * Show the view or text notification for a long period of time.  This time
     * could be user-definable.
     *
     * @see #setDuration
     */
    public static final int LENGTH_LONG = 1;

    private final View inflate;

    /**
     * Construct an empty Toast object.  You must call {@link #setView} before you
     * can call {@link #show}.
     *
     * @param context The context to use.  Usually your {@link Application}
     *                or {@link Activity} object.
     */
    public Toast(Context context) {
        super(context);
        if (mToast != null)
            mToast.cancel();
        inflate = LayoutInflater.from(context).inflate(R.layout.toast_layout, null);
        setView(inflate);
    }

    @Override
    public void setText(CharSequence s) {
        TextView tv = inflate.findViewById(R.id.toast_message);
        tv.setText(s);
    }

    @Override
    public void cancel() {
        super.cancel();
        mToast = null;
    }

    public static Toast makeText(Context context, CharSequence text, int duration) {
        mToast = new Toast(context);
        mToast.setText(text);
        mToast.setDuration(duration);
        return mToast;
    }
}
