package com.alphi.myapplication.Utils;
/*
  IDEA 2022/02/19
 */

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Parcelable;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

import java.io.File;
import java.util.ArrayList;

public class ShareUtil {
    public static void shareApkFile(Context context, @NonNull String...  fileName) {
        Intent share = new Intent();
        ArrayList<Parcelable> uris = new ArrayList<>();
        Uri contentUri = null;
        for (String filename : fileName) {
            File file = new File(filename);
            if (file.exists()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    contentUri = FileProvider.getUriForFile(context, "com.alphi.myApps.fileprovider", file);
                    context.grantUriPermission(context.getPackageName(), contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    if (fileName.length > 1){
                        uris.add(contentUri);
                    }
                } else {
                    // 对于安卓7以下
                    contentUri = Uri.fromFile(file);
                    if (fileName.length > 1) {
                        uris.add(contentUri);
                    }
                }
            } else {
                Toast.makeText(context, "分享文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (fileName.length == 1) {
            share.setAction(Intent.ACTION_SEND);
            // URI 保存与 Intent 关联的数据流，与ACTION_SEND一起使用以提供正在发送的数据。
            share.putExtra(Intent.EXTRA_STREAM, contentUri);
        } else {
            share.setAction(Intent.ACTION_VIEW);
            share.setAction(Intent.ACTION_SEND_MULTIPLE);
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        share.setType("application/vnd.android.package-archive");
        share.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);      // 标记活动为新任务
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);      // 标记授予读取 URI 权限
        context.startActivity(Intent.createChooser(share, "分享文件"));
    }
}
