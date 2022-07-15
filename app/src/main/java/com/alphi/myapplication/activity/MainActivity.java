package com.alphi.myapplication.activity;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alphi.myapplication.BuildConfig;
import com.alphi.myapplication.R;
import com.alphi.myapplication.Utils.AlphiGestureDetector;
import com.alphi.myapplication.Utils.LoadAppInfos;
import com.alphi.myapplication.Utils.MyAppComparator;
import com.alphi.myapplication.adapter.ListScrollListener;
import com.alphi.myapplication.adapter.MultiChoiceModeListener;
import com.alphi.myapplication.adapter.MyListViewAdapter;
import com.alphi.myapplication.widget.ExtractApp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {
    private int totalAppsNum;
    /**
     * 0 所有应用， 1 系统应用， 2 用户应用
     **/
    private int flag = -1;
    private PopupMenu popupMenu;
    private ListView listView;
    private TextView mAppsInfo;
    private FrameLayout mProgressBar;
    private boolean isSyncApps = true;
    private List<PackageInfo> sysAppsPackage;
    private List<PackageInfo> sysAppUpdatePackage;
    private List<PackageInfo> userAppsPackage;
    private MyListViewAdapter listAdapter;
    private List<PackageInfo> allPackageInfos;
    private Map<String, Long> apkSize;
    private Map<String, Long> appSize;
    private Map<String, Bitmap> appIcons;
    private Map<String, String> appLabel;
    private LoadAppInfos loadAppInfos;
    private TextView mtv_search_rs_count;
    private EditText mEdit_bar;
    private ListScrollListener listScrollListener;
    private SharedPreferences preferences;
    private String intentClassSimpleName;
    private int[] location_menu_search;
    private final Lock lock_apkSize = new ReentrantLock();
    private final Lock lock_appSize = new ReentrantLock();
    private static OnRunningActivityResult activityResultEvent;
    private int sortType;
    /**
     * 0为无，1为apk大小，2为app大小
     **/
    private int delaySync;
    /**
     * 包名检索，@检索，.aab检索aab
     */
    private final boolean[] searchPerformance = new boolean[3];
    public static ActivityResultLauncher<Intent> intentActivityResultLauncher;
    private FrameLayout mSearch_bar;
    /** 搜索结果的集合 **/
    private List<PackageInfo> rs;
    private ImageView mBtn_syncApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LoadAppInfos.getAndroidSdk(this);
        }
        initIntentActivityResultLauncher();
        loadAppInfos = new LoadAppInfos(this);
        initView();
        getPerformance();
        listAdapter = new MyListViewAdapter(this);
        listView.setAdapter(listAdapter);
        listView.setMultiChoiceModeListener(new MultiChoiceModeListener(listAdapter, MainActivity.this));
        new Thread(new mRunnable()).start();
        try {
            long firstInstallTime = getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0).firstInstallTime;
            int day = preferences.getInt("supportDay", 30);
            if (day > 0 && System.currentTimeMillis() - firstInstallTime >= 1000L * 60 * 60 * 24 * day) {
                showSupportDialog(day);
                if (day == 30) {
                    preferences.edit().putInt("supportDay", 100).apply();
                } else if (day == 100) {
                    preferences.edit().putInt("supportDay", 365).apply();
                } else {
                    preferences.edit().putInt("supportDay", -1).apply();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "初始化出错啦！", Toast.LENGTH_SHORT).show();
        }
        // 开启app的权限提示
        if (!preferences.getBoolean("disableReminder", false) &&
                !new PermissionActivity.CheckPermission(this).isGetAllPermission()) {
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (location_menu_search == null) {
            location_menu_search = new int[2];
            View view_menuSearch = findViewById(R.id.menu_search);
            view_menuSearch.getLocationInWindow(location_menu_search);
            location_menu_search[0] += view_menuSearch.getWidth() / 2;
            location_menu_search[1] = getSupportActionBar().getHeight() / 2;
        }
        if (item.getItemId() == R.id.menu_search) {
            searchBar_Visible();
            listView.setOnScrollListener(listScrollListener);
            if (listScrollListener.scrollState == 0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mEdit_bar.requestFocus();
                        showSoftInput(mEdit_bar);
                    }
                }, 100);
            }
        } else if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent();
            intent.setClass(this, SettingsActivity.class);
            startActivity(intent);
            intentClassSimpleName = SettingsActivity.class.getSimpleName();
        } else {
            switch (item.getItemId()) {
                case R.id.sort_by_app_label:
                    listAdapter.isShowFirstInstallTime = false;
                    sortType = 0;
                    break;
                case R.id.sort_by_pkg_name:
                    listAdapter.isShowFirstInstallTime = false;
                    sortType = 1;
                    break;
                case R.id.sort_by_apksize:
                    listAdapter.isShowFirstInstallTime = false;
                    sortType = 3;
                    break;
                case R.id.sort_by_appsize:
                    listAdapter.isShowFirstInstallTime = false;
                    sortType = 4;
                    break;
                case R.id.sort_by_sdk:
                    listAdapter.isShowFirstInstallTime = false;
                    sortType = 2;
                    break;
                case R.id.sort_by_updata:
                    listAdapter.isShowFirstInstallTime = false;
                    sortType = 5;
                    break;
                case R.id.sort_by_first_install_data:
                    listAdapter.isShowFirstInstallTime = true;
                    sortType = 6;
                    break;
                default:
                    sortType = 0;
                    return false;
            }
            if (rs == null) {
                updateSort(getPackageInfos());
            } else
                updateSort(rs);
            item.setChecked(true);
            listAdapter.notifyDataSetChanged();
        }
        return true;
    }

    private void initView() {
        setSupportActionBar(findViewById(R.id.main_toolbar));
        mAppsInfo = findViewById(R.id.appsInfo);
        listView = findViewById(R.id.lv);
        mProgressBar = findViewById(R.id.progressFrame);
        ExtractApp.initProgressBar(findViewById(R.id.progressbar_extract));
        mBtn_syncApps = findViewById(R.id.syncApps);
        mSearch_bar = findViewById(R.id.search_bar);
        listScrollListener = new ListScrollListener((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        mEdit_bar = findViewById(R.id.et_search);
        TextView mtv_closeSearch = findViewById(R.id.close_search);
        ImageView mClear_text = findViewById(R.id.clear_text);
        mtv_search_rs_count = findViewById(R.id.search_rs_count);
        RelativeLayout status_bar = findViewById(R.id.status_bar);
        mBtn_syncApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSyncApps = true;
                mProgressBar.setVisibility(View.VISIBLE);
                mtv_search_rs_count.setVisibility(View.INVISIBLE);
                new Thread(new mRunnable()).start();
            }
        });
        mtv_closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar_Gone();
                hiddenSoftInput(v);
            }
        });
        mEdit_bar.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hiddenSoftInput(v);
                    searchMethod(v.getText().toString());
                    return true;
                }
                return false;
            }
        });
        mEdit_bar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(final CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(final Editable s) {
                Log.d("TAG", "afterTextChanged: " + s);
                searchMethod(s);
            }
        });
        mClear_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mEdit_bar.getText().clear();
            }
        });
        mAppsInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (popupMenu == null) {
                    popupMenu = new PopupMenu(MainActivity.this, v);
                    popupMenu.inflate(R.menu.popup_menu);
                    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @SuppressLint("NonConstantResourceId")
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.SysApps:
                                    if (flag != 1) {
                                        flag = 1;
                                    } else {
                                        flag = 11;
                                    }
                                    break;
                                case R.id.UserApps:
                                    flag = 2;
                                    break;
                                default:
                                case R.id.AllApps:
                                    flag = 0;
                            }
                            mProgressBar.setVisibility(View.VISIBLE);
                            mtv_search_rs_count.setText(null);
                            mtv_search_rs_count.setVisibility(View.INVISIBLE);
                            new Thread(new mRunnable()).start();
                            return true;
                        }
                    });
                }
                popupMenu.show();
            }
        });
    }

    private void searchBar_Visible() {
        mSearch_bar.setVisibility(View.VISIBLE);
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(mSearch_bar, location_menu_search[0], location_menu_search[1], 0, location_menu_search[0]);
        circularReveal.setDuration(200);
        circularReveal.start();
    }

    private void searchBar_Gone() {
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(mSearch_bar, location_menu_search[0], location_menu_search[1], location_menu_search[0], 0);
        circularReveal.setDuration(300);
        circularReveal.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSearch_bar.setVisibility(View.GONE);
                hiddenSoftInput(mSearch_bar);
            }
        }, 300);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ExtractApp.updateProgress(new Handler());
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        getPerformance();
        if (intentClassSimpleName != null && intentClassSimpleName.equals(SettingsActivity.class.getSimpleName())) {
            boolean key_abc_top = preferences.getBoolean("key_abc_top", false);
            boolean is_abcTop = MyAppComparator.AppNameComparator.is_abcTop;
            if (key_abc_top != is_abcTop) {
                MyAppComparator.AppNameComparator.is_abcTop = key_abc_top;
                if (sortType == 0) {
                    Collections.sort(getPackageInfos(), new MyAppComparator.AppNameComparator(appLabel));
                }
            }
            listAdapter.notifyDataSetChanged();
            intentClassSimpleName = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        listAdapter.setLiteMode(preferences.getBoolean("key_lite_mode", false));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MultiChoiceModeListener.uninstallFailed > 0) {
            Toast.makeText(this, MultiChoiceModeListener.uninstallFailed + "项为无法卸载的系统应用", Toast.LENGTH_SHORT).show();
            MultiChoiceModeListener.uninstallFailed = 0;
        } else if (MultiChoiceModeListener.uninstallFailed == -1) {
            Toast.makeText(this, "卸载出现异常，可能是可卸载的系统应用的限制！\n请进入‘已更新的系统应用’进入详情进行排查", Toast.LENGTH_SHORT).show();
        }
    }

    private void getPerformance() {
        if (preferences == null) {
            preferences = getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", MODE_PRIVATE);
        }
        searchPerformance[0] = preferences.getBoolean("key_search_pkg", true);
        searchPerformance[1] = preferences.getBoolean("key_search_at", true);
        searchPerformance[2] = preferences.getBoolean("key_search_aab", true);

        if (flag == -1) {
            flag = Integer.parseInt(preferences.getString("key_default_list_apptype", "0"));
        }
    }

    /**
     * 搜索
     */
    private synchronized void searchMethod(CharSequence s) {
        @SuppressLint("SetTextI18n") Runnable runnable = () -> {
            List<PackageInfo> packageInfos = getPackageInfos();
            if (packageInfos == null) {
                searchMethod(s);
                return;
            }
            boolean searchLibType = false, searchAboutAbi = false;
            int abi = 0, sdk = -1;
            String str_a = null;
            if (s.length() > 0) {
                rs = new ArrayList<>();
                String str = s.toString();
                for (PackageInfo packageInfo : packageInfos) {
                    loadAppInfos.load(packageInfo);
                    String pkgName = loadAppInfos.getPackageName().toString();
                    if (appLabel.containsKey(packageInfo.packageName) && appLabel.get(packageInfo.packageName).toLowerCase().contains(str.toLowerCase()) || searchPerformance[0] && pkgName.contains(s)) {
                        rs.add(packageInfo);
                    }
                    if (searchPerformance[2] && str.equals("..aab") && loadAppInfos.isAAB()) {
                        rs.add(packageInfo);
                    } else if (searchPerformance[1] && str.charAt(0) == '@' && str.length() > 2) {
                        if (str_a == null) {
                            str_a = str.substring(1);
                        }
                        String libType = loadAppInfos.getLibType();
                        if (libType != null) abi++;
                        if (str_a.equals(libType)) {
                            rs.add(packageInfo);
                            searchLibType = true;
                        } else if (str_a.equals(String.valueOf(loadAppInfos.getAppLevel()))) {
                            sdk = Integer.parseInt(str_a);
                            rs.add(packageInfo);
                        } else if (str_a.equals("abi") && libType != null) {
                            rs.add(packageInfo);
                            searchAboutAbi = true;
                        } else if (str_a.equals("noabi") && libType == null) {
                            rs.add(packageInfo);
                            searchAboutAbi = true;
                        }
                    }
                }
            } else {
                if (rs != null)
                    rs.clear();
                rs = null;
            }
            if (rs != null) {
                boolean finalSearchLibType = searchLibType;
                int finalAbi = abi;
                int finalSdk = sdk;
                boolean finalSearchAboutAbi = searchAboutAbi;
                String finalStr_a = str_a;
                runOnUiThread(() -> {
                    int size = rs.size();
                    listView.setVisibility(View.VISIBLE);
                    listAdapter.update(rs);
                    mtv_search_rs_count.setVisibility(View.VISIBLE);
                    if (finalSearchLibType) {
                        mtv_search_rs_count.setText(finalStr_a + "：" + size + " / " + finalAbi);
                    } else if (finalSdk > 0) {
                        mtv_search_rs_count.setText("SDK" + finalSdk + "的应用有" + size + "个");
                    } else if (finalSearchAboutAbi) {
                        String strType;
                        if (finalStr_a.equals("abi")) {
                            strType = "原生库的";
                        } else {
                            strType = "无原生库";
                        }
                        mtv_search_rs_count.setText(strType + "应用有" + size + "个");
                    } else {
                        mtv_search_rs_count.setText(getString(R.string.search_result_count, size));
                    }
                    mBtn_syncApps.setVisibility(View.INVISIBLE);
                });
            } else
                runOnUiThread(() -> {
                    listAdapter.update(getPackageInfos());
                    mtv_search_rs_count.setText(null);
                    mtv_search_rs_count.setVisibility(View.INVISIBLE);
                    mBtn_syncApps.setVisibility(View.VISIBLE);
                });
        };
        new Thread(runnable).start();
    }

    private void showSoftInput(View v) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        boolean b = imm.showSoftInput(v, 0);
        if (!b) {
            imm.toggleSoftInputFromWindow(v.getWindowToken(), 0, 0);
        }
    }

    /**
     * 隐藏软键盘
     */
    private void hiddenSoftInput(View v) {
        InputMethodManager manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        manager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }


    @NonNull
    private String getAppsTypeAndNum() {
        String appsType;
        switch (flag) {
            case 1:
                appsType = String.format(getString(R.string.sysapp_num), sysAppsPackage.size(), totalAppsNum);
                break;
            case 2:
                appsType = String.format(getString(R.string.userapp_num), userAppsPackage.size(), totalAppsNum);
                break;
            case 11:
                appsType = String.format(getString(R.string.sysapp_update_num), sysAppUpdatePackage.size(), totalAppsNum);
                break;
            default:
                appsType = String.format(getString(R.string.allapp_num), totalAppsNum);
        }
        return appsType;
    }

    class mRunnable implements Runnable {
        @SuppressLint("SetTextI18n")
        @Override
        public void run() {
            if (isSyncApps) {
                getAppLists();
            }
            listAdapter.isShowFirstInstallTime = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAppsInfo.setText(getAppsTypeAndNum());
                    mProgressBar.setVisibility(View.GONE);
                    listAdapter.update(updateSort(getPackageInfos()));
                    listAdapter.updateAppLabel(appLabel);
                }
            });
            if (isSyncApps) {
                ArrayList<PackageInfo> packageInfos = new ArrayList<>(allPackageInfos);
                synchronized (lock_apkSize) {
                    appIcons = new HashMap<>();
                    apkSize = new HashMap<>();
                    for (int i = 0, packageInfosSize = packageInfos.size(); i < packageInfosSize; i++) {
                        PackageInfo packageInfo = packageInfos.get(i);
                        loadAppInfos.load(packageInfo);
                        appIcons.put(packageInfo.packageName, loadAppInfos.getBitmapIcon());
                        apkSize.put(packageInfo.packageName, loadAppInfos.getApkSize());
                        if (i == (Math.min(packageInfosSize, 18))) {
                            runOnUiThread(() -> {
                                listAdapter.updateAppIcons(appIcons);      // app图标
                                listAdapter.update_apkSizeMap(apkSize);        // apk大小
                            });
                        }
                    }
                    if (delaySync == 1)
                        Collections.sort(getPackageInfos(), new MyAppComparator.ApkSizeComparator(apkSize));
                }
                synchronized (lock_appSize) {
                    appSize = new HashMap<>();
                    for (int i = 0, packageInfosSize = packageInfos.size(); i < packageInfosSize; i++) {
                        PackageInfo packageInfo = packageInfos.get(i);
                        int flags = packageInfo.applicationInfo.flags;
                        if ((flags & ApplicationInfo.FLAG_SYSTEM) != 1 || (flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 128) {
                            loadAppInfos.load(packageInfo);
                            appSize.put(packageInfo.packageName, loadAppInfos.getTotalSize());
                        }
                    }
                    if (delaySync == 2)
                        Collections.sort(getPackageInfos(), new MyAppComparator.appSizeComparator(appSize));
                }
                runOnUiThread(() -> listAdapter.updata_appSizeMap(appSize));        // app占用空间
            }
            isSyncApps = false;
        }
    }

    @SuppressLint("SetTextI18n")
    private synchronized void getAppLists() {
        sysAppsPackage = new ArrayList<>();
        userAppsPackage = new ArrayList<>();
        sysAppUpdatePackage = new ArrayList<>();
        appLabel = new HashMap<>();
        allPackageInfos = getPackageManager().getInstalledPackages(0);
        totalAppsNum = allPackageInfos.size();
        long start = System.currentTimeMillis();
        for (PackageInfo packageInfo : allPackageInfos) {
            loadAppInfos.load(packageInfo);
            appLabel.put(packageInfo.packageName, loadAppInfos.getAppName());
            if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                sysAppsPackage.add(packageInfo);
                if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 128) {
                    sysAppUpdatePackage.add(packageInfo);
                }
            } else {
                userAppsPackage.add(packageInfo);
            }
        }
        long end1 = System.currentTimeMillis();
        MyAppComparator.AppNameComparator.is_abcTop = preferences.getBoolean("key_abc_top", false);
        Log.d("耗时", "getAppLists: " + (end1 - start) + "ms");
    }

    /**
     * 根据类别获取列表： -1 所有应用， 0 系统应用， 1 用户应用
     */
    private List<PackageInfo> getPackageInfos() {
        switch (flag) {
            case 1:
                return sysAppsPackage;
            case 2:
                return userAppsPackage;
            case 11:
                return sysAppUpdatePackage;
            default:
                return allPackageInfos;
        }
    }

    private List<PackageInfo> updateSort(List<PackageInfo> packageInfos) {
        delaySync = 0;
        switch (sortType) {
            case 0:
                Collections.sort(packageInfos, new MyAppComparator.AppNameComparator(appLabel));
                break;
            case 1:
                Collections.sort(packageInfos, new MyAppComparator.PackageNameComparator());
                break;
            case 2:
                Collections.sort(packageInfos, new MyAppComparator.sdkVersionComparator());
                break;
            case 3:
                synchronized (lock_apkSize) {
                    if (apkSize != null) {
                        Collections.sort(packageInfos, new MyAppComparator.ApkSizeComparator(apkSize));
                    } else
                        delaySync = 1;
                }
                break;
            case 4:
                synchronized (lock_appSize) {
                    if (appSize != null) {
                        Collections.sort(packageInfos, new MyAppComparator.appSizeComparator(appSize));
                    } else
                        delaySync = 2;
                }
                break;
            case 5:
                Collections.sort(packageInfos, new MyAppComparator.lastUpdataComparator());
                break;
            case 6:
                Collections.sort(packageInfos, new MyAppComparator.firstInstallDataComparator());
                break;
            default:
        }
        return packageInfos;
    }

    private void initIntentActivityResultLauncher() {
        intentActivityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                activityResultEvent.event(result);
            }
        });
    }

    public static void setActivityResultEvent(OnRunningActivityResult result) {
        activityResultEvent = result;
    }

    @SuppressLint("SetTextI18n")
    public void showSupportDialog(int day) {
        TextView tv = new TextView(this);
        tv.setTextSize(18);
        tv.setPadding(50, 30, 50, 20);
        tv.setText("-- 致用户：\n感谢您陪伴了Apk Export应用" + day + "天；Apk Export离不开大家的支持！本应用承诺不会加入任何广告的，因为作者也不喜欢广告，喜欢做纯净的应用；同时作者也会利用业余时间来更新优化app的，如果你喜欢这个应用，欢迎支持下作者！捐赠是对软件的良性循环做出贡献！\n做App不易，感谢大家的支持！");
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("感谢有你")
                .setView(tv)
                .setPositiveButton("支持", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent()
                                .setClass(MainActivity.this, SupportMeActivity.class);
                        startActivity(intent);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("不了，谢谢", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setCancelable(false)
                .create();
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        if (mSearch_bar.getVisibility() == View.VISIBLE) {
            searchBar_Gone();
            return;
        }
        if (ExtractApp.isRunningBackUp()) {
            Toast.makeText(this, "为了防止文件损坏，备份过程中请不要退出！", Toast.LENGTH_SHORT).show();
            return;
        }
        super.onBackPressed();
    }

    public interface OnRunningActivityResult {
        void event(ActivityResult result);
    }
}