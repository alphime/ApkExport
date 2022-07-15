package com.alphi.myapplication.adapter;
/*
  IDEA 2022/03/18
 */

import static com.alphi.myapplication.Utils.BlackFilter.isPkgBlackFilter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.Toast;

import com.alphi.myapplication.R;
import com.alphi.myapplication.Utils.ShareUtil;
import com.alphi.myapplication.activity.MainActivity;
import com.alphi.myapplication.widget.ExtractApp;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
    private final MyListViewAdapter mAdapter;
    private final Context context;
    protected static final Set<PackageInfo> mSelectedItems = new HashSet<>();
    public static int uninstallFailed;

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
                    @Override
                    public void run() {
                        List<Intent> intents = new ArrayList<>();
                        for (PackageInfo packageInfo : mSelectedItems) {
                            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                            if (!isPkgBlackFilter(context, applicationInfo)) {
                                Intent intent = new Intent(Intent.ACTION_DELETE);
                                Uri uri = Uri.parse("package:" + packageInfo.packageName);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                intents.add(intent);
                            } else {
                                uninstallFailed++;
                            }
                        }
                        if (intents.size() > 0) {
                            try {
                                context.startActivities(intents.toArray(new Intent[0]));
                            } catch (Exception e) {
                                uninstallFailed = -1;
                                e.printStackTrace();
                            }
                        }
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (uninstallFailed > 0) {
                                    Toast.makeText(context, uninstallFailed + "项为无法卸载的系统应用", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(context, "卸载出现异常，可能是可卸载的系统应用的限制！\n请进入‘已更新的系统应用’进入详情进行排查", Toast.LENGTH_SHORT).show();
                                }
                                uninstallFailed = 0;
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
