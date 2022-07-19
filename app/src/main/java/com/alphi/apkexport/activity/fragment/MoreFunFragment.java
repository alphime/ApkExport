package com.alphi.apkexport.activity.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alphi.apkexport.R;
import com.alphi.apkexport.activity.SignatureActivity;

public class MoreFunFragment extends Fragment {

    private View view;
    private ActionBar supportActionBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportActionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        supportActionBar.setTitle("更多");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_morefun, container, false);
        }
        view.findViewById(R.id.entry_md5_btn).setOnClickListener((v) -> {
            Intent intent = new Intent()
                    .setClass(getContext(), SignatureActivity.class);
            startActivity(intent);
        });
        return view;
    }

    @Override
    public void onStop() {
        super.onStop();
        supportActionBar.setTitle("设置");
    }
}