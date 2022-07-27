package com.alphi.apkexport.activity.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.alphi.apkexport.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class PrivacyFragment extends Fragment {

    private StringBuilder sb;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            InputStream stream = getContext().getAssets().open("privacy_description");
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            sb = new StringBuilder();
            while (br.ready()) {
                sb.append(br.readLine()).append("\n");
            }
            stream.close();
            br.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup inflate = (ViewGroup) inflater.inflate(R.layout.fragment_privacy, container, false);
        WebView webView = (WebView) inflate.getChildAt(0);
        webView.loadData(sb.toString(), "text/html", StandardCharsets.UTF_8.toString());
        return inflate;
    }
}