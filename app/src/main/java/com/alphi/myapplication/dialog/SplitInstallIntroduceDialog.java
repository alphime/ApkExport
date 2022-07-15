package com.alphi.myapplication.dialog;
/*
  IDEA 2022/03/18
 */

import static com.alphi.myapplication.Utils.MD5Utils.getSignaturesMD5;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.alphi.myapplication.R;
import com.alphi.myapplication.widget.OnClickEvent;
import com.google.android.material.bottomsheet.BottomSheetDialog;

/**
 * apks分包和xapk安装介绍介绍
 */
public class SplitInstallIntroduceDialog extends BottomSheetDialog {
    public SplitInstallIntroduceDialog(@NonNull Context context) {
        super(context);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(8, 10, 8, 100);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView tv_title = newTextView(getContext().getString(R.string.howto_install_apks));
        tv_title.setTextSize(22);
        TextView tv_detail = new TextView(getContext());
        tv_detail.setText("您可推荐使用以下工具进入本App备份路径来安装\n温馨提示：双击可直接转跳 Play Store 哦~");
        tv_detail.setTextSize(13);
        tv_detail.setPadding(60, 0, 40, 30);
        LinearLayout lin_zarchive = newLinTextView("ZArchive", "温馨提示：正版软件采用MD设计比较简陋且无广告软件占用空间小，国产应用商店很少能见到正版！");
        lin_zarchive.setOnClickListener(openApplicationMarket("ru.zdevs.zarchiver"));
        TextView tv_sai = newTextView("Split APKs Installer");
        tv_sai.setOnClickListener(openApplicationMarket("com.aefyr.sai"));
        LinearLayout lin_InstallByAdb = newLinTextView("通过ADB命令来安装apks", "打开USB调试，连接电脑并解压apks安装包然后输入命令：\n格式: adb install-multiple -r -t <base.apk> <split.apk>\n" +
                "·双击进入开发者选项设置");
        lin_InstallByAdb.setOnClickListener(new OnClickEvent(new OnClickEvent.OnClickListener() {
            @Override
            public void onSingleClick() {
                Toast.makeText(getContext(), "例如: adb install-multiple -r -t base.apk config.armeabi_v8a.apk config.cn.apk", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDoubleClick() {
                startDevelopmentActivity();
            }
        }));
        linearLayout.addView(tv_title);
        linearLayout.addView(tv_detail);
        linearLayout.addView(lin_zarchive);
        linearLayout.addView(tv_sai);
        linearLayout.addView(lin_InstallByAdb);
        setContentView(linearLayout);
    }

    @NonNull
    private TextView newTextView(String text) {
        TextView tv_mailto = new TextView(getContext());
        tv_mailto.setText(text);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tv_mailto.setLayoutParams(layoutParams);
        tv_mailto.setPadding(60, 36, 30, 36);
        tv_mailto.setTextSize(18);
        return tv_mailto;
    }

    private LinearLayout newLinTextView(String title, String text) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(60, 36, 30, 36);
        TextView tv_title = new TextView(getContext());
        tv_title.setText(title);
        tv_title.setTextSize(18);
        TextView tv_text = new TextView(getContext());
        tv_text.setText(text);
        tv_text.setTextSize(12);
        linearLayout.addView(tv_title);
        linearLayout.addView(tv_text);
        return linearLayout;
    }

    private OnClickEvent openApplicationMarket(String pkgName) {
        return new OnClickEvent(new OnClickEvent.OnClickListener() {
            @Override
            public void onSingleClick() {
                if (pkgName.equals("ru.zdevs.zarchiver")){
                    PackageInfo packageInfo;
                    try {
                        packageInfo = getContext().getPackageManager().getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
                        Signature signature = packageInfo.signatures[0];
                        String signaturesMD5 = getSignaturesMD5(signature);
                        if (!signaturesMD5.equals("135313873751656236811883201194340890628")){
                            Toast.makeText(getContext(), R.string.za_parite, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(pkgName);
                if (intent == null) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=" + pkgName));
                }
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    getContext().startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), "正在转跳 Play Store", Toast.LENGTH_SHORT).show();
                    intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
                    getContext().startActivity(intent);
                }
            }

            @Override
            public void onDoubleClick() {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Toast.makeText(getContext(), "正在转跳 Play Store", Toast.LENGTH_SHORT).show();
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName));
                getContext().startActivity(intent);
            }
        });
    }

    /**
     * 启动开发者设置
     */
    private void startDevelopmentActivity() {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            getContext().startActivity(intent);
        } catch (Exception e) {
            System.err.println("---------- DEVELOPMENT 第一种方法执行失败 ----------");
            e.printStackTrace();
            System.err.println("---------- DEVELOPMENT 尝试执行第二种方法 ----------");
            try {
                ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.DevelopmentSettings");
                Intent intent = new Intent();
                intent.setComponent(componentName);
                intent.setAction("android.intent.action.View");
                getContext().startActivity(intent);
            } catch (Exception e1) {
                System.err.println("---------- DEVELOPMENT 第二种方法执行失败 ----------");
                e1.printStackTrace();
                System.err.println("---------- DEVELOPMENT 尝试执行第三种方法: 针对小米特殊机型法 ----------");
                try {
                    //部分小米手机采用这种方式跳转
                    Intent intent = new Intent("com.android.settings.APPLICATION_DEVELOPMENT_SETTINGS");
                    getContext().startActivity(intent);
                } catch (Exception e2) {
                    System.err.println("---------- DEVELOPMENT 第三种方法执行失败 ----------");
                    e2.printStackTrace();
                    System.err.println("---------- DEVELOPMENT err:所有方法都执行失败！ ----------");
                }
            }
        }
    }
}
