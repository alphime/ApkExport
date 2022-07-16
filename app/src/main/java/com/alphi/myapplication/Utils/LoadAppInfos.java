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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/*
  IDEA 2022/02/06
 */

public final class LoadAppInfos {
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

    public void load(final PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
        this.applicationInfo = packageInfo.applicationInfo;
    }

    public void load(CharSequence pkgName) {
        try {
            this.packageInfo = packageManager.getPackageInfo(pkgName.toString(), 0);
            this.applicationInfo = this.packageInfo.applicationInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String getAppName() {
        return applicationInfo.loadLabel(packageManager).toString();
    }

    public CharSequence getPackageName() {
        return packageInfo.packageName;
    }

    public Bitmap getBitmapIcon() {
        Drawable drawable = applicationInfo.loadIcon(packageManager);
        return BitmapUtil.drawableToBitmap150(drawable);
    }

    public Drawable getDrawable() {
        Drawable drawable = applicationInfo.loadIcon(packageManager);
        drawable.setBounds(0, 0, 150, 150);
        return drawable;
    }

    public String getVersionName() {
        return packageInfo.versionName;
    }

    public int getVersionCode() {
        return packageInfo.versionCode;
    }

    public int getAppLevel() {
        return applicationInfo.targetSdkVersion;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getSupport() {
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

    public String getLibType() {
        File file = new File(applicationInfo.nativeLibraryDir);
        if (file.list() != null) {
            return file.getName();
        }
        return null;
    }

    public boolean isAAB() {
        return packageInfo.splitNames != null;
    }

    public boolean isXApk() {
        return getVirtualXApk().exists();
    }

    public boolean hasKotlinLang() {
        try (ZipFile zipFile = new ZipFile(applicationInfo.sourceDir)){
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                String name = zipEntry.getName();
                if (name.contains("kotlin/") || Pattern.compile("META-INF/.*?kotlin.*?").matcher(name).matches())
                    return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long getApkSize() {
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
        if (virtualXApk.exists()) {
            length += virtualXApk.length();
        }
        File file = new File(applicationInfo.sourceDir);
        long length2 = getLongSize(file);
        return length + length2;
    }

    public long getTotalSize() {
        GetAppSize getApkSize = new GetAppSize(context, packageInfo.packageName);
        return getApkSize.totalSize;
    }

    public long getLastUpdataTime() {
        return packageInfo.lastUpdateTime;
    }

    public long getFirstInstallTime() {
        return packageInfo.firstInstallTime;
    }

    private File getVirtualXApk() {
        return new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + packageInfo.packageName + "/main." + packageInfo.versionCode + "." + packageInfo.packageName + ".obb");
    }
}
