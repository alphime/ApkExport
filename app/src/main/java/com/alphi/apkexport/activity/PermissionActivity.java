package com.alphi.apkexport.activity;
/*
  IDEA 2022/02/18
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.alphi.apkexport.BuildConfig;
import com.alphi.apkexport.R;
import com.alphi.apkexport.activity.fragment.PrivacyFragment;
import com.alphi.apkexport.widget.Toast;

import java.util.List;

public class PermissionActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 1024;
    private TextView tv_req_appUsage_rs;
    private TextView tv_req_storage_rs;
    private TextView tv_req_queryAllApp_rs;
    private final String key = "disableReminder";       //禁止温馨提醒
    private FragmentManager fragmentManager;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);
        SharedPreferences sharedPreferences = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_PRIVATE);
        tv_req_storage_rs = findViewById(R.id.req_storage_rs);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            findViewById(R.id.lin_req_appUsage).setVisibility(View.GONE);
        else
            tv_req_appUsage_rs = findViewById(R.id.req_appUsage_rs);
        tv_req_queryAllApp_rs = findViewById(R.id.req_queryAllApp_rs);
        TextView tv_privacy = findViewById(R.id.privacy_link);
        tv_privacy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        if (fragmentManager == null) {
            fragmentManager = getSupportFragmentManager();
        }
        tv_privacy.setOnClickListener(v -> {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_in_left, R.anim.slide_in_left, R.anim.slide_out_right)
                    .add(android.R.id.content, new PrivacyFragment())
                    .addToBackStack(null);
            transaction.commit();
        });
        Button entry = findViewById(R.id.entryMain);
        entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        CheckBox checkBox_disableReminder = findViewById(R.id.cb_disableReminder);
        checkBox_disableReminder.setChecked(sharedPreferences.getBoolean(key, false));
        checkBox_disableReminder.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(key, isChecked);
                editor.apply();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        CheckPermission checkPermission = new CheckPermission(this);
        PermissionDialog pd = new PermissionDialog(this);
        if (checkPermission.checkStoragePermission()) {
            tv_req_storage_rs.setText(R.string.accredit);
            tv_req_storage_rs.setTextColor(Color.GREEN);
            tv_req_storage_rs.setOnClickListener(null);
        } else {
            tv_req_storage_rs.setText(R.string.unauthorizedAndRequest);
            tv_req_storage_rs.setTextColor(Color.RED);
            tv_req_storage_rs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pd.requestStoragePermissionDialog();
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            if (checkPermission.checkAppUsagePermission()) {
                tv_req_appUsage_rs.setText(R.string.accredit);
                tv_req_appUsage_rs.setTextColor(Color.GREEN);
                tv_req_appUsage_rs.setOnClickListener(null);
            } else {
                tv_req_appUsage_rs.setText(R.string.unauthorizedAndRequest);
                tv_req_appUsage_rs.setTextColor(Color.RED);
                tv_req_appUsage_rs.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pd.requestAppUsagePermissionDialog();
                    }
                });
            }

        if (checkPermission.checkQueryAllPackagesPermission()) {
            tv_req_queryAllApp_rs.setText(R.string.accredit);
            tv_req_queryAllApp_rs.setTextColor(Color.GREEN);
        } else {
            tv_req_queryAllApp_rs.setText(R.string.unauthorized);
            tv_req_queryAllApp_rs.setTextColor(Color.RED);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return false;
    }


    // Dialog
    public static class PermissionDialog {
        private final Context context;

        /**
         * @param context 已做出自动转换为ActivityContent处理
         */
        public PermissionDialog(Context context) {
            if (context instanceof Activity)
                this.context = context;
            else
                this.context = (((ContextWrapper) context).getBaseContext());
        }

        public void requestStoragePermissionDialog() {
            permissionDialog(context.getString(R.string.reqStorage2), new PositiveEvent() {
                @Override
                public void event() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        intent.setData(Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                        ((Activity) context).startActivityForResult(intent, REQUEST_CODE);
                    } else {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
                    }
                }
            });
        }

        public void requestAppUsagePermissionDialog() {
            permissionDialog(context.getString(R.string.reqAppUsage2), new PositiveEvent() {
                @Override
                public void event() {
                    Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
        }

        private void permissionDialog(String description, PositiveEvent event) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle("权限申请");
            TextView textView = new TextView(context);
            textView.setText(description);
            textView.setPadding(50, 40, 50, 20);
            dialog.setView(textView);
            dialog.setPositiveButton("同意", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    event.event();
                }
            });
            dialog.setNegativeButton("拒绝", null);
            dialog.setCancelable(false);
            AlertDialog alertDialog = dialog.create();
            alertDialog.show();
        }
    }


    public static class CheckPermission {
        private final Context context;

        public CheckPermission(Context context) {
            this.context = context;
        }

        public boolean checkStoragePermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return Environment.isExternalStorageManager();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 先判断有没有权限
                return ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
            }
            return true;
        }

        boolean checkAppUsagePermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(USAGE_STATS_SERVICE);
                long currentTime = System.currentTimeMillis();
                if (usageStatsManager != null) {
                    List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 2 * 60 * 1000, currentTime);
                    return stats.size() > 0;
                }
                return false;
            }
            return true;
        }

        private boolean checkQueryAllPackagesPermission() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                return ActivityCompat.checkSelfPermission(context, Manifest.permission.QUERY_ALL_PACKAGES) == PackageManager.PERMISSION_GRANTED;
            }
            return true;
        }

        public boolean isGetAllPermission() {
            return checkQueryAllPackagesPermission() && checkStoragePermission() && checkAppUsagePermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限申请成功！", Toast.LENGTH_SHORT).show();
                tv_req_storage_rs.setText("已申请");
                tv_req_storage_rs.setTextColor(Color.GREEN);
                tv_req_storage_rs.setOnClickListener(null);
            }
        }
    }

    private interface PositiveEvent {
        void event();
    }
}
