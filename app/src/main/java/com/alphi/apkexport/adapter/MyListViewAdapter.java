package com.alphi.apkexport.adapter;

import static com.alphi.apkexport.utils.AlphiFileUtil.getSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.alphi.apkexport.R;
import com.alphi.apkexport.utils.LoadAppInfos;
import com.alphi.apkexport.dialog.AppOperaViewDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * IDEA 2022/02/06
 */

public class MyListViewAdapter extends BaseAdapter {
    private boolean liteMode;
    private List<PackageInfo> data; //要显示的数组
    private final Context content;
    private LoadAppInfos loadAppInfos; //解析PackageInfo的方法
    private Map<String, Long> appSizeMap; //app总大小Map
    private Map<String, Bitmap> appIcons; //app图标Map
    private Map<String, String> appLabels;
    public boolean isShowFirstInstallTime;
    private Map<String, Long> apkSizeMap;


    public MyListViewAdapter(Context context) {
        this.content = context;
    }

    /**
     * 重新加载传入app信息和图标
     *
     * @param data 信息
     */
    public synchronized void update(List<PackageInfo> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void updateAppLabel(Map<String, String> appLabel) {
        this.appLabels = appLabel;
    }

    public void updateAppIcons(Map<String, Bitmap> appIcons) {
        this.appIcons = appIcons;
        notifyDataSetChanged();
    }

    public void update_apkSizeMap(Map<String, Long> apkSizeMap) {
        this.apkSizeMap = apkSizeMap;
    }

    /**
     * 获取app占用空间，因为比较慢，用多线程处理
     *
     * @param appSizeMap app总大小
     */
    public void updata_appSizeMap(Map<String, Long> appSizeMap) {
        this.appSizeMap = appSizeMap;
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint({"SetTextI18n", "SimpleDateFormat"})
    @Override
    public View getView(int position, View itemView, ViewGroup parent) {
        ViewHolder holder;
        if (itemView == null) {
            itemView = LayoutInflater.from(content).inflate(R.layout.item, parent, false);
            holder = new ViewHolder();
            holder.imageView = itemView.findViewById(R.id.appIcon);
            holder.tv_label = itemView.findViewById(R.id.appLabel);
            holder.tv_pkg = itemView.findViewById(R.id.appPackageName);
            holder.tv_targetSdk = itemView.findViewById(R.id.appTargetSdk);
            holder.tv_minSdk = itemView.findViewById(R.id.appMinSdk);
            holder.tv_lib = itemView.findViewById(R.id.libType);
            holder.tv_version = itemView.findViewById(R.id.app_Version);
            holder.tv_apkSize = itemView.findViewById(R.id.tv_apksize);
            holder.tv_appSize = itemView.findViewById(R.id.tv_appsize);
            holder.tv_installDate = itemView.findViewById(R.id.tv_installDate);
            holder.split_aab = itemView.findViewById(R.id.split_aab);
            holder.flag_xapk = itemView.findViewById(R.id.flag_xapk);
            holder.tv_lite_apksize = itemView.findViewById(R.id.tv_lite_apksize);
            holder.tv_lite_app_Version = itemView.findViewById(R.id.tv_lite_app_Version);
            holder.tv_lite_appTargetSdk = itemView.findViewById(R.id.tv_lite_appTargetSdk);
            holder.item_show_full = itemView.findViewById(R.id.item_show_full);
            holder.item_show_lite = itemView.findViewById(R.id.item_show_lite);
            holder.select_box = itemView.findViewById(R.id.select_box);
            itemView.setTag(holder);
            loadAppInfos = new LoadAppInfos(content);
        } else {
            holder = (ViewHolder) itemView.getTag();
        }
        PackageInfo packageInfo = data.get(position);
        loadAppInfos.load(packageInfo);

        String packageName = packageInfo.packageName;
        if (appIcons != null && appIcons.containsKey(packageName)) {
            holder.imageView.setImageBitmap(appIcons.get(packageName));
        } else {
            holder.imageView.setImageBitmap(loadAppInfos.getBitmapIcon());
        }

        if (appLabels != null) {
            holder.tv_label.setText(appLabels.get(packageName));
        } else {
            holder.tv_label.setText(loadAppInfos.getAppName());
        }

        boolean isXApk = loadAppInfos.isXApk();
        if (isXApk) {
            holder.flag_xapk.setVisibility(View.VISIBLE);
        } else {
            holder.flag_xapk.setVisibility(View.GONE);
        }

        holder.tv_pkg.setText(loadAppInfos.getPackageName());

        Long apkSize = null;
        if (apkSizeMap != null) {
            apkSize = apkSizeMap.get(packageName);
        }
        if (apkSize == null) {
            apkSize = loadAppInfos.getApkSize();
        }
        String apkSizeStr = getSize(apkSize);

        String libType = loadAppInfos.getLibType();

        // switch liteMode
        if (liteMode) {
            holder.item_show_full.setVisibility(View.GONE);
            holder.item_show_lite.setVisibility(View.VISIBLE);
            // liteMode
            holder.tv_lite_apksize.setText(apkSizeStr);
            holder.tv_lite_appTargetSdk.setText(String.valueOf(loadAppInfos.getAppLevel()));
            holder.tv_lite_app_Version.setText(loadAppInfos.getVersionName() + " (" + loadAppInfos.getVersionCode() + ")");
        } else {
            holder.tv_version.setText(loadAppInfos.getVersionName() + " (" + loadAppInfos.getVersionCode() + ")");
            if (loadAppInfos.isAAB()) {
                holder.split_aab.setVisibility(View.VISIBLE);
            } else {
                holder.split_aab.setVisibility(View.GONE);
            }
            holder.tv_targetSdk.setText(String.valueOf(loadAppInfos.getAppLevel()));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                holder.tv_minSdk.setText("Support " + loadAppInfos.getSupport() + "+");
            } else {
                holder.tv_minSdk.setVisibility(View.GONE);
            }
            if (libType != null) {
                holder.tv_lib.setVisibility(View.VISIBLE);
                holder.tv_lib.setText("| " + libType);
            } else {
                holder.tv_lib.setVisibility(View.GONE);
            }

            holder.tv_apkSize.setText(apkSizeStr);
            if (appSizeMap != null) {
                Long longSize = appSizeMap.get(packageName);
                if (longSize != null && longSize > 0) {
                    holder.tv_appSize.setText(getSize(longSize));
                } else {
                    holder.tv_appSize.setText(null);
                }
            }
            String dataTimeStr;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            if (!isShowFirstInstallTime) {
                dataTimeStr = "最近更新: " + simpleDateFormat.format(new Date(loadAppInfos.getLastUpdataTime()));
            } else {
                dataTimeStr = "初次安装: " + simpleDateFormat.format(new Date(loadAppInfos.getFirstInstallTime()));
            }
            holder.tv_installDate.setText(dataTimeStr);
            holder.item_show_full.setVisibility(View.VISIBLE);
            holder.item_show_lite.setVisibility(View.GONE);
        }

        // click event
        ListView listView = (ListView) parent;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AppOperaViewDialog showDialog = new AppOperaViewDialog(content, data.get(position), appLabels, appIcons, apkSizeMap, appSizeMap);
                showDialog.show();
            }
        });
        if (MultiChoiceModeListener.mSelectedItems.contains(packageInfo)) {
            holder.select_box.setVisibility(View.VISIBLE);
            if (!listView.isItemChecked(position)) listView.setItemChecked(position, true);
        } else {
            holder.select_box.setVisibility(View.INVISIBLE);
            if (listView.isItemChecked(position)) listView.setItemChecked(position, false);
        }
        return itemView;
    }

    public List<PackageInfo> getPackageInfos() {
        return data;
    }

    public void setLiteMode(boolean liteMode) {
        this.liteMode = liteMode;
    }

    private static class ViewHolder {
        View select_box;
        ImageView imageView, split_aab, flag_xapk;
        TextView tv_label, tv_pkg, tv_targetSdk, tv_minSdk, tv_lib, tv_version, tv_apkSize, tv_appSize, tv_installDate;
        TextView tv_lite_app_Version, tv_lite_apksize, tv_lite_appTargetSdk;
        ViewGroup item_show_full, item_show_lite;
    }
}
