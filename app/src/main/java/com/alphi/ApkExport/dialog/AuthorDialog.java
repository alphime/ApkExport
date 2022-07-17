package com.alphi.ApkExport.dialog;
/*
  IDEA 2022/03/17
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alphi.ApkExport.R;
import com.alphi.ApkExport.activity.SupportMeActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class AuthorDialog extends BottomSheetDialog {

    public AuthorDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
    }

    private void initView(){
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 800));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(8, 10, 8, 10);
        TextView title = newTextView("About the author");
        title.setTextColor(getContext().getResources().getColor(android.R.color.holo_blue_light));

        TextView tv_mailto = newTextView(getContext().getString(R.string.mailto_author));
        tv_mailto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("mailto:alphime@foxmail.com");
                Intent sendIt = new Intent(Intent.ACTION_SENDTO, uri);
                sendIt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(sendIt);
            }
        });

        TextView tv_coolApk = newTextView("逛一逛开发者的酷安");
        tv_coolApk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("http://www.coolapk.com/u/1003867");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getContext().startActivity(intent);
            }
        });

        TextView tv_supportMe = newTextView("支持开发者");
        tv_supportMe.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setClass(getContext(), SupportMeActivity.class);
            getContext().startActivity(intent);
            dismiss();
        });

        linearLayout.addView(title);
        linearLayout.addView(tv_mailto);
        linearLayout.addView(tv_coolApk);
        linearLayout.addView(tv_supportMe);
        setContentView(linearLayout);
    }

    @NonNull
    private TextView newTextView(String text) {
        TextView tv_mailto = new TextView(getContext());
        tv_mailto.setText(text);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_mailto.setLayoutParams(layoutParams);
        tv_mailto.setPadding(60, 30, 30, 30);
        tv_mailto.setTextSize(18);
        return tv_mailto;
    }
}
