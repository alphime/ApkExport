package com.alphi.apkexport.adapter;
/*
  IDEA 2022/02/17
 */

import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;

public class ListScrollListener implements AbsListView.OnScrollListener {
    private final InputMethodManager manager;
    public int scrollState;

    public ListScrollListener(InputMethodManager manager) {
        this.manager = manager;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        this.scrollState = scrollState;
        if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            manager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

    }
}
