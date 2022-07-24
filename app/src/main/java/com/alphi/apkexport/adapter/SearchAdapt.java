package com.alphi.apkexport.adapter;

/*
    author: alphi
    createDate: 2022/7/19
*/

import static android.content.Context.INPUT_METHOD_SERVICE;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alphi.apkexport.R;

public final class SearchAdapt implements TextView.OnEditorActionListener, TextWatcher, View.OnClickListener {
    private final Context context;
    private final FrameLayout mSearch_bar;
    private final EditText mEdit_bar;
    private final TextView mtv_closeSearch;
    private final ImageView mClear_text;
    private SearchAdaptListener searchAdaptListener;
    private String keyWord;

    public static SearchAdapt init(@NonNull ViewGroup layout, @NonNull SearchAdaptListener searchAdapt) {
        SearchAdapt se = new SearchAdapt((FrameLayout) layout);
        se.searchAdaptListener = searchAdapt;
        return se;
    }

    private SearchAdapt(@NonNull FrameLayout layout) {
        this.context = layout.getContext();
        this.mSearch_bar = layout;
        this.mEdit_bar = layout.findViewById(R.id.et_search);
        this.mtv_closeSearch = layout.findViewById(R.id.close_search);
        this.mClear_text = layout.findViewById(R.id.clear_text);
        this.mEdit_bar.setOnEditorActionListener(this);
        this.mEdit_bar.addTextChangedListener(this);
        this.mClear_text.setOnClickListener(this);
        this.mtv_closeSearch.setOnClickListener(this);
    }

    public void show() {
        mSearch_bar.setVisibility(View.VISIBLE);
        int[] msg = searchAdaptListener.showAnimation(mSearch_bar);
        if (msg[1] != 0) {
            mEdit_bar.requestFocus();
            showSoftInput(mEdit_bar);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                searchAdaptListener.afterShowWindowEvent();
            }
        }, msg[0]);
    }

    public void hidden() {
        int[] msg = searchAdaptListener.hideAnimation(mSearch_bar);
        if (msg[1] != 0)
            hiddenSoftInput(mEdit_bar);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearch_bar.setVisibility(View.GONE);
                searchAdaptListener.afterHiddenWindowEvent();
            }
        }, msg[0]);
    }

    private void showSoftInput(View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        boolean b = imm.showSoftInput(v, 0);
        if (!b) {
            imm.toggleSoftInputFromWindow(v.getWindowToken(), 0, 0);
        }
    }

    public boolean isShowing() {
        return mSearch_bar.getVisibility() == View.VISIBLE;
    }

    /**
     * 隐藏软键盘
     */
    private void hiddenSoftInput(View v) {
        InputMethodManager manager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        Log.d("TAG", "afterTextChanged: " + s);
        keyWord = s.toString();
        searchAdaptListener.searchMethod(s);
    }

    @Override
    public void onClick(View v) {
        if (v.equals(mClear_text)) {
            mEdit_bar.getText().clear();
            return;
        }
        if (v.equals(mtv_closeSearch)) {
            hidden();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            hiddenSoftInput(v);
            keyWord = v.getText().toString();
            searchAdaptListener.searchMethod(keyWord);
            return true;
        }
        return false;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public static abstract class SearchAdaptListener {
        public abstract void searchMethod(CharSequence key);

        /**
         * 显示窗口动画
         *
         * @return 参数一：延迟隐藏时间；参数二：0为禁止输入法显示，1为启用输入法显示
         */
        public abstract int[] showAnimation(ViewGroup v);

        /**
         * 隐藏窗口动画
         *
         * @return 参数一：延迟隐藏时间；参数二：0为禁止输入法显示，1为启用输入法显示
         */
        public abstract int[] hideAnimation(ViewGroup v);

        public void afterHiddenWindowEvent() {}

        public void afterShowWindowEvent() {}
    }
}
