package com.alphi.apkexport.activity;

import static com.alphi.apkexport.utils.ShareUtil.shareApkFile;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.AnimRes;
import androidx.annotation.AnimatorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.alphi.apkexport.BuildConfig;
import com.alphi.apkexport.R;
import com.alphi.apkexport.activity.fragment.MoreFunFragment;
import com.alphi.apkexport.dialog.AuthorDialog;
import com.alphi.apkexport.dialog.SplitInstallIntroduceDialog;
import com.alphi.apkexport.utils.ExtractFile;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {

    private static FragmentManager supportFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            supportFragmentManager = getSupportFragmentManager();
            replaceFragment(new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 设置上级按钮
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    protected static FragmentTransaction replaceFragment(@NonNull Fragment fragment) {
        return replaceFragment(fragment, R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
    }

    protected static FragmentTransaction replaceFragment(@NonNull Fragment fragment, @AnimatorRes @AnimRes int enter,
                                                         @AnimatorRes @AnimRes int exit, @AnimatorRes @AnimRes int popEnter,
                                                         @AnimatorRes @AnimRes int popExit) {
        return supportFragmentManager.beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .replace(R.id.settings, fragment);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Preference key_version = findPreference("key_version");
            key_version.setSummary(BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ") ");
            Preference key_backup_path = findPreference("key_backup_path");
            key_backup_path.setSummary("路径: " + Environment.getExternalStorageDirectory().getPath() + File.separator + ExtractFile.savePath);
            key_backup_path.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    noRealizeFunction();
                    return true;
                }
            });
            findPreference("key_author").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    AuthorDialog authorDialog = new AuthorDialog(getContext());
                    authorDialog.show();
                    return true;
                }
            });
            findPreference("key_support").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startSupportActivity();
                    return true;
                }
            });
            findPreference("key_permission").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(getContext(), PermissionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    return true;
                }
            });

            findPreference("key_search_setting").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    replaceFragment(new SearchPerformance())
                            .addToBackStack(null).commit();
                    return true;
                }
            });

            findPreference("key_howto_install_apks").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new SplitInstallIntroduceDialog(getContext()).show();
                    return true;
                }
            });

            findPreference("key_share_this").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    ExtractFile extractFile = new ExtractFile(getContext().getPackageCodePath(), getString(R.string.app_name) + " _" + BuildConfig.VERSION_NAME + ".apk");
                    extractFile.toSave();
                    shareApkFile(getContext(), extractFile.getOutFileDir());
                    return true;
                }
            });

            key_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                private int pressCount = 0;

                @Override
                public boolean onPreferenceClick(Preference preference) {
                    pressCount++;
                    // 延迟执行
                    Handler handler = new Handler();
                    handler.postAtTime(new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("kk: " + pressCount);
                            if (pressCount > 5)
                                replaceFragment(new MoreFunFragment())
                                    .addToBackStack(null)
                                    .commit();
                            // 移除队列
                            handler.removeCallbacksAndMessages(this);
                            pressCount = 0;
                        }
                    }, this, SystemClock.uptimeMillis() + 1000);
                    return true;
                }
            });
        }

        public void startSupportActivity() {
            Intent intent = new Intent();
            intent.setClass(getContext(), SupportMeActivity.class);
            startActivity(intent);
        }

        private void noRealizeFunction() {
            Toast.makeText(getContext(), "在做呢！快了快了~", Toast.LENGTH_SHORT).show();
        }

        private <T extends androidx.preference.Preference> T findPreference(String key) {
            return getPreferenceScreen().findPreference(key);
        }
    }

    public static class SearchPerformance extends PreferenceFragmentCompat {

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.search_performance, rootKey);

        }

        private <T extends androidx.preference.Preference> T findPreference(String key) {
            return getPreferenceScreen().findPreference(key);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // toolbar上级按钮 id:android.R.id.home
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}