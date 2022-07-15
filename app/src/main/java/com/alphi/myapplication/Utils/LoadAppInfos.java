package com.alphi.myapplication.Utils;

import static com.alphi.myapplication.Utils.AlphiFileUtil.getLongSize;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alphi.myapplication.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
  IDEA 2022/02/06
 */

public class LoadAppInfos {
    private PackageInfo packageInfo;
    private ApplicationInfo applicationInfo;
    private final Context context;
    private PackageManager packageManager;
    private static Properties apiProperties;

    public LoadAppInfos(Context context) {
        this.context = context;
        if (context != null) {
            this.packageManager = context.getPackageManager();
        }
    }

    public final void load(final PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        this.applicationInfo = packageInfo.applicationInfo;
    }

    public final void load(CharSequence pkgName) {
        try {
            this.packageInfo = packageManager.getPackageInfo(pkgName.toString(), 0);
            this.applicationInfo = this.packageInfo.applicationInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public final String getAppName() {
        return applicationInfo.loadLabel(packageManager).toString();
    }

    public final CharSequence getPackageName() {
        return packageInfo.packageName;
    }

    public final Bitmap getBitmapIcon() {
        Drawable drawable = applicationInfo.loadIcon(packageManager);
        return BitmapUtil.drawableToBitmap150(drawable);
    }

    public final Drawable getDrawable() {
        Drawable drawable = applicationInfo.loadIcon(packageManager);
        drawable.setBounds(0, 0, 150, 150);
        return drawable;
    }

    public final String getVersionName() {
        return packageInfo.versionName;
    }

    public final int getVersionCode() {
        return packageInfo.versionCode;
    }

    public final int getAppLevel() {
        return applicationInfo.targetSdkVersion;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public final String getSupport() {
        int minApi = applicationInfo.minSdkVersion;
        if (minApi > 0 && apiProperties != null) {
            String value = apiProperties.getProperty(String.valueOf(minApi));
            return value != null ? value : "API " + minApi;
        }
        return null;
    }

    public static void getAndroidSdk(Context context) {
        AssetManager am = context.getAssets();
        try (InputStream is = am.open("android-version_map.properties")) {
            apiProperties = new Properties();
            apiProperties.load(is);
            Log.d("readApplicationSdkInfo", "getAndroidSdk: " + apiProperties.size());
        } catch (Exception e) {
            Log.e(BuildConfig.APPLICATION_ID, "getAndroidSdk: ", e);
        }
    }

    public final String getLibType() {
        File file = new File(applicationInfo.nativeLibraryDir);
        if (file.list() != null) {
            return file.getName();
        }
        return null;
    }

    public final boolean isAAB() {
        return packageInfo.splitNames != null;
    }

    public final boolean isXApk(){
        return getVirtualXApk().exists();
    }

    public final long getApkSize() {
        String[] splitPublicSourceDirs = applicationInfo.splitSourceDirs;
        int length = 0;
        if (splitPublicSourceDirs != null) {
            for (String splitPublicSourceDir : splitPublicSourceDirs) {
                File file = new File(splitPublicSourceDir);
                long longSize = getLongSize(file);
                length += longSize;
            }
        }
        File virtualXApk = getVirtualXApk();
        if (virtualXApk.exists()){
            length += virtualXApk.length();
        }
        File file = new File(applicationInfo.sourceDir);
        long length2 = getLongSize(file);
        return length+length2;
    }

    public final long getTotalSize() {
        GetAppSize getApkSize = new GetAppSize(context, packageInfo.packageName);
        return getApkSize.totalSize;
    }

    public final long getLastUpdataTime() {
        return packageInfo.lastUpdateTime;
    }

    public final long getFirstInstallTime(){
        return packageInfo.firstInstallTime;
    }

    private File getVirtualXApk(){
        return new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + packageInfo.packageName + "/main." + packageInfo.versionCode + "." + packageInfo.packageName + ".obb");
    }
}
