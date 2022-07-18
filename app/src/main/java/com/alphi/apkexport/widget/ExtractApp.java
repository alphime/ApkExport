package com.alphi.apkexport.widget;
/*
  IDEA 2022/02/19
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.documentfile.provider.DocumentFile;

import com.alphi.apkexport.R;
import com.alphi.apkexport.activity.MainActivity;
import com.alphi.apkexport.utils.ExtractFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ExtractApp implements View.OnClickListener {
    private static final List<String> runningBackupEvent = new ArrayList<>();
    private final String pkgName;
    private final String sourceDir;
    private final String fileName;
    private final Context context;
    private final String[] splitSourceDirs;
    private final String appLabel;
    private ExtractFile extractFile;
    @SuppressLint("StaticFieldLeak")
    private static ProgressBar progressBar;
    private final String oobPath;
    private Handler handler = new Handler();

    public ExtractApp(Context context, final PackageInfo packageInfo, final String appLabel) {
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;
        this.sourceDir = applicationInfo.sourceDir;
        this.appLabel = appLabel;
        this.pkgName = applicationInfo.packageName;
        splitSourceDirs = applicationInfo.splitSourceDirs;
        this.context = context;
        this.oobPath = getXApk_ObbFile(packageInfo);
        this.fileName = appLabel + " _" + packageInfo.versionName;     //文件命名
    }

    @Override
    public void onClick(View v) {
        if (!new File(sourceDir).exists()) {
            Toast.makeText(context, R.string.no_exist_app, Toast.LENGTH_SHORT).show();
        } else if (isBackupAPP(pkgName)) {
            Toast.makeText(context, appLabel + "正在提取中...\n请不要重复操作！", Toast.LENGTH_SHORT).show();
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int save_rs = extractedApp("提取中...\t\t\n请不要退出应用！");
                    if (save_rs >= 0) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (save_rs == 1) {
                                    Toast.makeText(context, appLabel + " 安装包提取成功！\n保存路径在：/sdcard/" + ExtractFile.savePath, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, appLabel + " 安装包提取失败！\n没有存储权限或存储空间不足！", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }).start();
        }
    }

    public int extractedApp(String toastText) {
        Runnable runToast = null;
        if (toastText != null) {
            runToast = () -> {
                Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show();
            };
            handler.postDelayed(runToast, 200);
        }
        try {
            runningBackupEvent.add(pkgName);
            updateProgress(handler);
            if (splitSourceDirs == null && oobPath == null) {
                extractFile = new ExtractFile(sourceDir, fileName + ".apk");
            } else if (splitSourceDirs != null) {
                int length = splitSourceDirs.length;
                String[] fileDirs = new String[length + 1];
                fileDirs[0] = sourceDir;
                System.arraycopy(splitSourceDirs, 0, fileDirs, 1, length);
                extractFile = new ExtractFile(fileDirs, fileName + ".apks");
            } else {
                File file = new File(oobPath);
                if (!file.canRead()) {
                    Uri uriObb = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb/document/primary%3A" +
                            oobPath.replaceFirst(Environment.getExternalStorageDirectory().getPath() + File.separator, "").replaceAll("/", "%2F"));
                    try {
                        FileInputStream inputStream = (FileInputStream) context.getContentResolver().openInputStream(uriObb);
                        extractFile = new ExtractFile(sourceDir, file.getName(), inputStream, fileName + ".xapk");
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        TextView textView = new TextView(context);
                        textView.setText(R.string.reqVisitObbDir);
                        textView.setPadding(52, 30, 52, 30);
                        builder.setTitle("请求目录权限")
                                .setView(textView);
                        builder.setPositiveButton("授权", (dialog, which) -> requestVisitObbDir());
                        builder.setNegativeButton("拒绝", null);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                builder.create().show();
                            }
                        });
                        runningBackupEvent.remove(pkgName);
                        updateProgress(handler);
                        return -1;
                    }
                } else {
                    try {
                        extractFile = new ExtractFile(sourceDir, file.getName(), new FileInputStream(file), fileName + ".apk");
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            boolean result = extractFile != null && extractFile.toSave();
            runningBackupEvent.remove(pkgName);
            updateProgress(handler);
            return result ? 1 : 0;
        } finally {
            if (toastText != null) {
                handler.removeCallbacks(runToast);
            }
        }
    }

    public static boolean isBackupAPP(String pkgName) {
        return runningBackupEvent.contains(pkgName);
    }

    /**
     * 更新备份软件进度条状态
     */
    public static void updateProgress(Handler handler) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (isRunningBackUp()) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public static boolean isRunningBackUp() {
        return runningBackupEvent.size() > 0;
    }

    public static void initProgressBar(ProgressBar progressBar) {
        ExtractApp.progressBar = progressBar;
    }

    public String getOutFileDir() {
        return extractFile.getOutFileDir();
    }

    private String getXApk_ObbFile(@NonNull PackageInfo packageInfo) {
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/Android/obb/"
                + packageInfo.packageName + "/main." + packageInfo.versionCode + "." + packageInfo.packageName + ".obb");
        if (file.exists()) {
            return file.getPath();
        }
        return null;
    }

    private void requestVisitObbDir() {
        // %3A代表 :       %2F代表 /
        Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb");
        DocumentFile documentFile = DocumentFile.fromTreeUri(context, uri);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
                | Intent.FLAG_GRANT_PREFIX_URI_PERMISSION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (documentFile != null) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentFile.getUri());
            } else {
                System.err.println("1212q: documentFile = null");
            }
        }
        MainActivity.setActivityResultEvent(new MainActivity.OnRunningActivityResult() {
            @Override
            public void event(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uriTree = data.getData();
                        if (uriTree.equals(uri)) {
                            context.getContentResolver().takePersistableUriPermission(uriTree,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                    }
                }
            }
        });
        MainActivity.intentActivityResultLauncher.launch(intent);
    }

    public ExtractApp setHandler(Handler handler) {
        this.handler = handler;
        return this;
    }
}
