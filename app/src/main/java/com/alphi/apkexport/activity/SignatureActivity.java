package com.alphi.apkexport.activity;

import static com.alphi.apkexport.utils.MD5Utils.getSignaturesMD5;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.alphi.apkexport.R;
import com.alphi.apkexport.adapter.SearchAdapt;
import com.alphi.apkexport.utils.LoadAppInfos;

import java.util.Arrays;
import java.util.HashMap;

public class SignatureActivity extends AppCompatActivity {
    private final int[] location_menu_search = new int[2];
    private ActionBar supportActionBar;
    private GridLayout grid;
    private ProgressBar progressBar;
    private HashMap<String, String> labels;
    private SearchAdapt searchAdapt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);
        grid = findViewById(R.id.grid);
        progressBar = findViewById(R.id.progress_bar);
        searchAdapt = SearchAdapt.init(findViewById(R.id.search_bar), new SearchAdapt.SearchAdaptListener() {
            @Override
            public void searchMethod(CharSequence key) {
                grid.removeAllViews();
                if (key.length() > 0) {
                    new Thread(new MRunnable(SignatureActivity.this).search(key.toString())).start();
                } else
                    new Thread(new MRunnable(SignatureActivity.this)).start();
            }

            @Override
            public int[] showAnimation(ViewGroup v) {
                Animator circularReveal = ViewAnimationUtils.createCircularReveal(v.getChildAt(0), location_menu_search[0], location_menu_search[1], 0, location_menu_search[0]);
                circularReveal.setDuration(200);
                circularReveal.start();
                return new int[]{100, 1};
            }

            @Override
            public int[] hideAnimation(ViewGroup v) {
                Animator circularReveal = ViewAnimationUtils.createCircularReveal(v.getChildAt(0), location_menu_search[0], location_menu_search[1], location_menu_search[0], 0);
                circularReveal.setDuration(300);
                circularReveal.start();
                return new int[]{300, 1};
            }

            @Override
            public void afterHiddenWindowEvent() {
                getSupportActionBar().show();
            }
        });
        new Thread(new MRunnable(this)).start();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add("搜索");
        item.setIcon(R.drawable.ic_search);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        supportActionBar = getSupportActionBar();
        supportActionBar.setTitle(R.string.fun_md5_show);
        if (supportActionBar != null) {
            supportActionBar.setShowHideAnimationEnabled(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        View viewById = findViewById(item.getItemId());
        viewById.getLocationInWindow(location_menu_search);
        System.out.println("kkk: " + Arrays.toString(location_menu_search));
        supportActionBar.hide();
        searchAdapt.show();
        return super.onOptionsItemSelected(item);
    }

    private class MRunnable implements Runnable {
        private final Context context;
        private String keyWord;

        private MRunnable(Context context) {
            this.context = context;
        }

        public MRunnable search(String keyWord) {
            this.keyWord = keyWord;
            return this;
        }

        @Override
        public void run() {
            LoadAppInfos loadAppInfos = new LoadAppInfos(context);
            if (keyWord == null) {
                labels = new HashMap<>();
            }
            PackageManager packageManager = context.getPackageManager();
            for (PackageInfo installedPackage : packageManager.getInstalledPackages(PackageManager.GET_SIGNATURES)) {
                loadAppInfos.load(installedPackage);
                Signature signature = loadAppInfos.getSignatures()[0];
                String sn = getSignaturesMD5(signature);
                if (keyWord == null) {
                    labels.put(installedPackage.packageName, installedPackage.applicationInfo.loadLabel(packageManager).toString());
                } else {
                    String label = labels.get(installedPackage.packageName);
                    if (label == null || !label.contains(keyWord))
                        continue;
                }
                TextView tv_name = createTextView(loadAppInfos.getAppName(), new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Toast.makeText(context, loadAppInfos.getPackageName(), Toast.LENGTH_SHORT).show();
                        return true;
                    }
                });
                TextView tv_sn = createTextView(sn, null);
                ((AppCompatActivity)context).runOnUiThread(() -> {
                    grid.addView(tv_name);
                    grid.addView(tv_sn);
                    progressBar.setVisibility(View.GONE);
                });
            }
        }

        public TextView createTextView(String str, View.OnLongClickListener listener) {
            TextView tv = new TextView(context);
            tv.setText(str);
            if (listener != null) {
                tv.setOnLongClickListener(listener);
                tv.setTextSize(20);
                tv.setPadding(30,22,30,10);
            } else {
                tv.setTextIsSelectable(true);
                tv.setPadding(30,10,30,26);
            }
            return tv;
        }
    }

    @Override
    public void onBackPressed() {
        if (!searchAdapt.isShowing()) {
            super.onBackPressed();
        } else
            searchAdapt.hidden();
    }
}