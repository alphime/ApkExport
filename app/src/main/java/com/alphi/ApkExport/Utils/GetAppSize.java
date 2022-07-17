package com.alphi.ApkExport.Utils;

import android.annotation.SuppressLint;
import android.app.usage.StorageStats;
import android.app.usage.StorageStatsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.os.Build;
import android.os.Process;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.text.format.Formatter;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/*
  IDEA 2022/02/11
 */

/**
 * 获取app大小
 */
public class GetAppSize {
    private static final String TAG = GetAppSize.class.getSimpleName();
    public long appSize;
    public long dataSize;
    public long cacheSize;
    public long totalSize;
    private boolean debug;

    public GetAppSize(Context context, String packageName) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            handlePackageSizeInfo(context, packageName);
        } else {
            getAppSizeForOreo(context, packageName);
        }
    }


    //Android8.0以下获取Apk大小方法：通过反射调用getPackageSizeInfo获取

    private void handlePackageSizeInfo(Context context, String packageName) {
        PackageManager mPackageManager = context.getPackageManager();
        Method getPackageSizeInfo = null;
        try {
            getPackageSizeInfo = mPackageManager.getClass().getMethod("getPackageSizeInfo", String.class, IPackageStatsObserver.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        MyPackageStatsObserver mSecurityPackageStatsObserver = new MyPackageStatsObserver();
        if (getPackageSizeInfo != null) {
            try {
                getPackageSizeInfo.setAccessible(true);
                // 调用该函数，待调用完成后会回调MyPackageStatsObserver
                getPackageSizeInfo.invoke(mPackageManager, packageName, mSecurityPackageStatsObserver);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    private class MyPackageStatsObserver extends IPackageStatsObserver.Stub {

        @Override
        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) {
            cacheSize = pStats.cacheSize;
            dataSize = pStats.codeSize;
            appSize = pStats.dataSize;
            totalSize = cacheSize + dataSize + appSize;
            if (debug) {
                Log.d(TAG, succeeded + " cacheSize = " + cacheSize + "; appSize = " + dataSize + "; " + "appSize = " + appSize + "; totalSize = " + totalSize);
            }
        }
    }


    // Android8.0及以上获取Apk大小方法：

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void getAppSizeForOreo(Context context, String packageName) {
        if (hasUsageStatsPermission(context)) {
            final StorageStatsManager storageStatsManager = (StorageStatsManager) context.getSystemService(Context.STORAGE_STATS_SERVICE);
            final UserHandle user = Process.myUserHandle();
            try {
                StorageStats storageStats = storageStatsManager.queryStatsForPackage(StorageManager.UUID_DEFAULT, packageName, user);
                Log.d(TAG, "storage stats for app of package name:" + packageName + " : ");
                appSize = storageStats.getAppBytes();
                cacheSize = storageStats.getCacheBytes();
                dataSize = storageStats.getDataBytes();
                totalSize = dataSize + appSize;
                if (debug) {
                    Log.d(TAG, "getAppBytes:" + Formatter.formatShortFileSize(context, appSize) + " getCacheBytes:" + Formatter.formatShortFileSize(context,
                            cacheSize) + " getDataBytes:" + Formatter.formatShortFileSize(context, dataSize));
                    Log.d(TAG, "getAppSizeForOreo: -----------------------------------------------");
                }
            } catch (PackageManager.NameNotFoundException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean hasUsageStatsPermission(Context context) {
        UsageStatsManager usageStatsManager;
        usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
        if (usageStatsManager == null) {
            return false;
        }
        long currentTime = System.currentTimeMillis();
        // try to get app usage state in last 2 min
        List<UsageStats> stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 2 * 60 * 1000, currentTime);
        return stats != null && stats.size() > 0;
    }

    /**
     * 请求AppUsage权限
     */
    @SuppressLint("SetTextI18n")
    private void requestAppUsagePermission(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Start usage access settings activity fail!", e);
        }
    }
}
