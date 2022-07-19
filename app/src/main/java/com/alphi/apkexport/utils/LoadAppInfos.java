package com.alphi.apkexport.utils;

import static com.alphi.apkexport.utils.AlphiFileUtil.getLongSize;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.alphi.apkexport.BuildConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/*
  IDEA 2022/02/06
 */

public final class LoadAppInfos {
    private PackageInfo packageInfo;
    private ApplicationInfo applicationInfo;
    private final Context context;
    private PackageManager packageManager;
    private static Properties apiProperties;

    // 缓存 Map
    private static Map<String, Long> apkSizeMap;
    private static Map<String, Long> appSizeMap;
    private static Map<String, Bitmap> appIcons;
    private static Map<String, String> appLabels;

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
        load(pkgName, 0);
    }

    public void load(CharSequence pkgName, int flag) {
        try {
            this.packageInfo = packageManager.getPackageInfo(pkgName.toString(), flag);
            this.applicationInfo = this.packageInfo.applicationInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Long> getApkSizeMap() {
        if (apkSizeMap == null)
            return null;
        return Collections.unmodifiableMap(apkSizeMap);
    }

    public static void setApkSizeMap(Map<String, Long> apkSizeMap) {
        LoadAppInfos.apkSizeMap = apkSizeMap;
    }

    public static Map<String, Long> getAppSizeMap() {
        if (appSizeMap == null)
            return null;
        return Collections.unmodifiableMap(appSizeMap);
    }

    public static void setAppSizeMap(Map<String, Long> appSizeMap) {
        LoadAppInfos.appSizeMap = appSizeMap;
    }

    public static Map<String, Bitmap> getAppIcons() {
        if (appIcons == null)
            return null;
        return Collections.unmodifiableMap(appIcons);
    }

    public static void setAppIcons(Map<String, Bitmap> appIcons) {
        LoadAppInfos.appIcons = appIcons;
    }

    public static Map<String, String> getAppLabels() {
        if (appLabels == null)
            return null;
        return Collections.unmodifiableMap(appLabels);
    }

    public static void setAppLabels(Map<String, String> appLabels) {
        LoadAppInfos.appLabels = appLabels;
    }

    public String getAppName() {
        if (appLabels != null) {
            String label = appLabels.get(applicationInfo.packageName);
            if (label != null)
                return label;
        }
        return applicationInfo.loadLabel(packageManager).toString();
    }

    public CharSequence getPackageName() {
        return packageInfo.packageName;
    }

    public Bitmap getBitmapIcon() {
        if (appIcons != null) {
            Bitmap bitmap = appIcons.get(applicationInfo.packageName);
            if (bitmap != null)
                return bitmap;
        }
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
        if (apkSizeMap != null) {
            Long aLong = apkSizeMap.get(getPackageName());
            if (aLong != null)
                return aLong;
        }
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
        if (appSizeMap != null) {
            Long aLong = appSizeMap.get(packageInfo.packageName);
            if (aLong != null)
                return aLong;
        }
        GetAppSize getApkSize = new GetAppSize(context, packageInfo.packageName);
        return getApkSize.totalSize;
    }

    public String getSourceDir() {
        return applicationInfo.sourceDir;
    }

    public Signature[] getSignatures() {
        return packageInfo.signatures;
    }

    public long getLastUpdataTime() {
        return packageInfo.lastUpdateTime;
    }

    public long getFirstInstallTime() {
        return packageInfo.firstInstallTime;
    }

    public boolean isSystemApp() {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1;
    }

    public boolean isUpdateSysApp() {
        return (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 128;
    }

    private File getVirtualXApk() {
        return new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/" + packageInfo.packageName + "/main." + packageInfo.versionCode + "." + packageInfo.packageName + ".obb");
    }

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public static void clearCache() {
        if (appLabels != null) {
            appLabels.clear();
            appLabels = null;
        }
        if (appIcons != null) {
            appIcons.clear();
            appIcons = null;
        }
        if (appSizeMap != null) {
            appSizeMap.clear();
            appSizeMap = null;
        }
        if (apkSizeMap != null) {
            apkSizeMap.clear();
            apkSizeMap = null;
        }
    }
}
