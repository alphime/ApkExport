package com.alphi.apkexport.adapter;
/*
  IDEA 2022/03/18
 */

import static com.alphi.apkexport.utils.BlackFilter.isPkgBlackFilter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.TextView;

import com.alphi.apkexport.R;
import com.alphi.apkexport.utils.ShareUtil;
import com.alphi.apkexport.widget.ExtractApp;
import com.alphi.apkexport.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
    private final MyListViewAdapter mAdapter;
    private final Context context;
    protected static final Set<PackageInfo> mSelectedItems = new HashSet<>();

    public MultiChoiceModeListener(MyListViewAdapter mAdapter, Context context) {
        this.mAdapter = mAdapter;
        this.context = context;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        PackageInfo item = (PackageInfo) mAdapter.getItem(position);
        if (checked) {
            mSelectedItems.add(item);
        } else {
            mSelectedItems.remove(item);
        }
        mode.setTitle("已选择" + mSelectedItems.size() + "项");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.mulitple_menu, menu);
        mAdapter.notifyDataSetChanged();
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        Handler handler = new Handler();
        switch (item.getItemId()) {
            case R.id.menu_share:
                new Thread(new Runnable() {
                    int fail;

                    @Override
                    public void run() {
                        List<String> paths = new ArrayList<>();
                        for (PackageInfo packageInfo : mSelectedItems) {
                            ExtractApp extractApp = new ExtractApp(context, packageInfo,
                                    context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString()).setHandler(handler);
                            int result = extractApp
                                    .extractedApp("正在提取…请勿退出！");
                            if (result != 1) {
                                fail++;
                            } else {
                                paths.add(extractApp.getOutFileDir());
                            }
                        }
                        if (fail > 0) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, "有" + fail + "项提取过程中出现问题暂时无法分享！",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        ShareUtil.shareApkFile(context, paths.toArray(new String[0]));
                    }
                }).start();
                break;
            case R.id.menu_extract:
                new Thread(new Runnable() {
                    int success;

                    @Override
                    public void run() {
                        int total = mSelectedItems.size();
                        for (PackageInfo packageInfo : mSelectedItems) {
                            int result = new ExtractApp(context, packageInfo,
                                    context.getPackageManager().getApplicationLabel(packageInfo.applicationInfo).toString()).setHandler(handler)
                                    .extractedApp("正在提取…请勿退出！");
                            if (result == 1) success++;
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "已成功提取" + success + "项，共选择" + total + "项",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
                break;
            case R.id.menu_selectAll:
                mSelectedItems.addAll(mAdapter.getPackageInfos());
                mode.setTitle("已选择" + mSelectedItems.size() + "项");
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.menu_uninstall:
                new Thread(new Runnable() {

                    private int errCount;

                    @Override
                    public void run() {
                        List<Intent> intents = new ArrayList<>();
                        for (PackageInfo packageInfo : mSelectedItems) {
                            if (isPkgBlackFilter(packageInfo.applicationInfo)) {
                                errCount++;
                                continue;
                            }
                            Intent intent = new Intent(Intent.ACTION_DELETE);
                            Uri uri = Uri.parse("package:" + packageInfo.packageName);
                            intent.setData(uri);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            intents.add(intent);
                        }
                        if (intents.size() > 0) {
                            try {
                                context.startActivities(intents.toArray(new Intent[0]));
                            } catch (Exception e) {
                                e.printStackTrace();
                                TextView tv = new TextView(context);
                                tv.setTextSize(17);
                                tv.setPadding(70, 30, 70, 20);
                                tv.setText("为了更好的使用，请到切换应用类型里面点击两次“系统应用”进入到“已更新的系统应用”，然后逐个点击应用再点详情按钮以进行排查，找到出错的应用进行截图并将截图反馈给作者吧！谢谢 ~");
                                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                                        .setTitle("出错啦！")
                                        .setView(tv)
                                        .setCancelable(false)
                                        .setPositiveButton("好的，我了解了", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                handler.post(builder::show);
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (errCount > 0) {
                                    Toast.makeText(context, errCount + "项为无法卸载的系统应用", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).start();
                break;
            default:
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mSelectedItems.clear();
        mAdapter.notifyDataSetChanged();
    }
}
