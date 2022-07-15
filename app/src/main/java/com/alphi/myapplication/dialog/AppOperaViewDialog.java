package com.alphi.myapplication.dialog;
/*
  IDEA 2022/02/18
 */

import static com.alphi.myapplication.Utils.AlphiFileUtil.getSize;
import static com.alphi.myapplication.Utils.BlackFilter.isPkgBlackFilter;
import static com.alphi.myapplication.Utils.BlackFilter.readPkgBlackFilter;
import static com.alphi.myapplication.Utils.BlackFilter.writePkgBlackFilter;
import static com.alphi.myapplication.Utils.MD5Utils.getSignaturesMD5;
import static com.alphi.myapplication.Utils.ShareUtil.shareApkFile;
import static com.alphi.myapplication.Utils.ZipUtil.readZip_IsExistFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;

import com.alphi.myapplication.BuildConfig;
import com.alphi.myapplication.R;
import com.alphi.myapplication.Utils.ExtractFile;
import com.alphi.myapplication.Utils.LoadAppInfos;
import com.alphi.myapplication.Utils.MD5Utils;
import com.alphi.myapplication.activity.MainActivity;
import com.alphi.myapplication.widget.ExtractApp;
import com.alphi.myapplication.widget.OnClickEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class AppOperaViewDialog extends BottomSheetDialog {
    private final String sourceDir;
    private final ApplicationInfo applicationInfo;
    private final Map<String, Long> apkSizeMap;
    private final Map<String, String> appLabels;
    private final PackageInfo packageInfo;
    private final Map<String, Bitmap> appIcons; //app图标Map
    private final Map<String, Long> appSizeMap;
    private final Handler handler = new Handler();
    private final Thread remind_thread = new Thread(new Runnable() {
        @SuppressLint("SdCardPath")
        @Override
        public void run() {
            while (true) {
                String[] stringArray = getContext().getResources().getStringArray(R.array.dialog_reminder);
                for (int i = 0, len = stringArray.length; i < len; i++) {
                    String s;
                    if (i == 0)
                        s = String.format(stringArray[i], "/sdcard/" + ExtractFile.savePath);
                    else
                        s = stringArray[i];
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            tv_save_reminder.setText(s);
                        }
                    });
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!isShowing()) return;
            }
        }
    });
    private TextView tv_save_reminder;

    @SuppressLint("SetTextI18n")
    public AppOperaViewDialog(Context context, PackageInfo packageInfo, Map<String, String> appLabels, Map<String, Bitmap> appIcons, Map<String, Long> apkSizeMap, Map<String, Long> appSizeMap) {
        super(context);
        this.packageInfo = packageInfo;
        this.applicationInfo = packageInfo.applicationInfo;
        this.apkSizeMap = apkSizeMap;
        this.appIcons = appIcons;
        this.appSizeMap = appSizeMap;
        this.sourceDir = applicationInfo.sourceDir;
        this.appLabels = appLabels;
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_content_normal, null);
        setContentView(contentView);   // 关联布局
        TextView tv_title = contentView.findViewById(R.id.appName);
        TextView tv_pkg = contentView.findViewById(R.id.pkgName);
        TextView tv_versionName = contentView.findViewById(R.id.app_Version);
        TextView tv_versionCode = contentView.findViewById(R.id.app_VersionCode);
        ImageView aab = contentView.findViewById(R.id.split_aab);
        ImageView flag_xapk = contentView.findViewById(R.id.flag_xapk);
        TextView tv_size = contentView.findViewById(R.id.apksize);
        TextView tv_sdk = contentView.findViewById(R.id.appTargetSdk);
        ImageView mAppIcon = contentView.findViewById(R.id.appIcon);
        TextView tv_showSys_lib = contentView.findViewById(R.id.sysApps_lib);
        Button mbtn_extract = contentView.findViewById(R.id.extract_app);
        Button mbtn_shareApp = contentView.findViewById(R.id.share_app);
        Button mbtn_app_InfoSettings = contentView.findViewById(R.id.app_DetailsSettings);
        TextView mtv_md5 = contentView.findViewById(R.id.md5_info);
        tv_save_reminder = contentView.findViewById(R.id.save_reminder);
        TextView tv_minSupport = contentView.findViewById(R.id.tv_minSupport);
        TextView tv_appsize = contentView.findViewById(R.id.tv_appsize);
        TextView tv_firstInstall = contentView.findViewById(R.id.tv_firstInstall);
        TextView tv_lastUpdate = contentView.findViewById(R.id.tv_lastUpdate);

        SharedPreferences preferences = getContext().getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
        LoadAppInfos loadAppInfos = new LoadAppInfos(getContext());
        loadAppInfos.load(packageInfo);
        String packageName = packageInfo.packageName;
        String appLabel = appLabels.get(packageName);
        tv_title.setText(appLabel);
        tv_pkg.setText(packageName);
        tv_versionName.setText(packageInfo.versionName);
        tv_versionCode.setText(String.valueOf(packageInfo.versionCode));
        long apkSize;
        if (apkSizeMap != null && apkSizeMap.containsKey(packageName)) {
            apkSize = apkSizeMap.get(packageName);
        } else {
            apkSize = loadAppInfos.getApkSize();
        }
        String apkSizeStr = getSize(apkSize);
        tv_size.setText(apkSizeStr);
        tv_sdk.setText("SDK: " + applicationInfo.targetSdkVersion);
        if (appIcons != null) {
            mAppIcon.setImageBitmap(appIcons.get(packageName));
        } else {
            mAppIcon.setImageBitmap(loadAppInfos.getBitmapIcon());
        }
        if (loadAppInfos.isAAB()) {
            aab.setVisibility(View.VISIBLE);
        } else {
            aab.setVisibility(View.GONE);
        }
        loadAppInfos.load(packageInfo);
        if (loadAppInfos.isXApk()) {
            flag_xapk.setVisibility(View.VISIBLE);
        } else {
            flag_xapk.setVisibility(View.GONE);
        }
        mAppIcon.setOnClickListener(new OnClickEvent(new OnClickEvent.OnClickListener() {
            @Override
            public void onSingleClick() {
            }

            @Override
            public void onDoubleClick() {
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(packageName);
                if (intent != null) {
                    getContext().startActivity(intent);
                }
            }
        }));
        tv_showSys_lib.setVisibility(View.VISIBLE);
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
            tv_showSys_lib.setText(getContext().getString(R.string.sysapp));
        } else {
            String libType = loadAppInfos.getLibType();
            if (libType != null) {
                switch (libType) {
                    case "arm64":
                        tv_showSys_lib.setText("arm64-v8a");
                        break;
                    case "arm":
                        if (readZip_IsExistFile(applicationInfo.sourceDir, "lib/armeabi-v7a/")) {
                            tv_showSys_lib.setText("armeabi-v7a");
                        } else {
                            tv_showSys_lib.setText("armeabi");
                        }
                        break;
                    default:
                        tv_showSys_lib.setText(libType);
                }
            } else {
                tv_showSys_lib.setVisibility(View.GONE);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            tv_minSupport.setText("支持 " + loadAppInfos.getSupport() + " 及以上的设备");
        } else {
            tv_minSupport.setVisibility(View.GONE);
        }
        int flags = applicationInfo.flags;
        if ((flags & ApplicationInfo.FLAG_SYSTEM) != 1 || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 128) {
            if (appSizeMap != null) {
                Long size = appSizeMap.get(packageName);
                if (size != null) {
                    tv_appsize.setText(getSize(size));
                } else {
                    tv_appsize.setText(getSize(loadAppInfos.getTotalSize()));
                }
            } else {
                tv_appsize.setText(getSize(loadAppInfos.getTotalSize()));
            }
        }
        if (packageName.equals("ru.zdevs.zarchiver")) {
            try {
                PackageInfo packageInfo = getContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_SIGNATURES);
                Signature signature = packageInfo.signatures[0];
                String signaturesMD5 = getSignaturesMD5(signature);
                if (!signaturesMD5.equals("135313873751656236811883201194340890628")) {
                    Drawable pirate = AppCompatResources.getDrawable(getContext(),
                            R.drawable.pirate);
                    pirate.setBounds(0, 0,
                            pirate.getIntrinsicWidth() / 3,
                            pirate.getIntrinsicHeight() / 3);
                    tv_title.setCompoundDrawables(null, null,
                            pirate, null);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (ExtractApp.isBackupAPP(packageName)) {
            mbtn_extract.setText("提取中..");
            mbtn_extract.setTextSize(12);
        }
        ExtractApp extractApp = new ExtractApp(getContext(), packageInfo, appLabel);
        mbtn_extract.setOnClickListener(extractApp);
        mbtn_shareApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExistApkFile()) {
                    Toast.makeText(getContext(), R.string.no_exist_app, Toast.LENGTH_SHORT).show();
                } else if (ExtractApp.isBackupAPP(packageName)) {
                    Toast.makeText(getContext(), appLabel + "正在提取中...\n请不要重复操作！", Toast.LENGTH_SHORT).show();
                } else {
                    Handler handler = new Handler();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            int result = extractApp.extractedApp("正在就绪中...");
                            if (result != -1) {
                                if (result == 0) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), appLabel + " 安装包提取失败！\n请先授予存储权限！", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                } else {
                                    shareApkFile(getContext(), extractApp.getOutFileDir());
                                }
                            }
                        }
                    }).start();
                }
            }
        });
        mbtn_app_InfoSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isExistApkFile()) {
                    Toast.makeText(getContext(), R.string.no_exist_app, Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent mIntent = new Intent();
                mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                mIntent.setData(Uri.fromParts("package", packageName, null));
                try {
                    if (!isPkgBlackFilter(getContext(), applicationInfo)) {
                        getContext().startActivity(mIntent);
                    }
                } catch (Exception e) {
                    @SuppressLint("SdCardPath")
                    String path = "/sdcard/" + ExtractFile.savePath + "/errFilter.txt";
                    File file = new File(path);
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writePkgBlackFilter(getContext(), packageName);
                        if (file.isDirectory()) file.delete();
                        File fileDir = file.getParentFile();
                        if (fileDir.isFile()) fileDir.delete();
                        if (!fileDir.exists()) fileDir.mkdirs();
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        Set<String> filter = readPkgBlackFilter(getContext());
                        writer.write("pkg: " + Arrays.toString(filter.toArray()));
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                    Toast.makeText(getContext(), "出错啦！请将" + path + "文件发送给开发者邮箱！", Toast.LENGTH_SHORT).show();
                    Log.e("SecurityException: pkg", packageName, e);
                }
            }
        });
        if (preferences.getBoolean("key_show_md5", false)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String fileMD5 = MD5Utils.getFileMD5(applicationInfo.sourceDir);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mtv_md5.setText("MD5：" + fileMD5 + "\t");
                            mtv_md5.setTextIsSelectable(true);
                        }
                    });
                }
            }).start();
        } else {
            mtv_md5.setVisibility(View.GONE);
        }
        if (preferences.getBoolean("key_show_installData", false)) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            tv_firstInstall.setText("初次安装：" + dateFormat.format(new Date(loadAppInfos.getFirstInstallTime())));
            tv_lastUpdate.setText("最近更新：" + dateFormat.format(new Date(loadAppInfos.getLastUpdataTime())));
        } else {
            tv_firstInstall.setVisibility(View.GONE);
            tv_lastUpdate.setVisibility(View.GONE);
        }
        remind_thread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        remind_thread.interrupt();
    }

    private boolean isExistApkFile() {
        File file = new File(sourceDir);
        return file.exists();
    }
}
