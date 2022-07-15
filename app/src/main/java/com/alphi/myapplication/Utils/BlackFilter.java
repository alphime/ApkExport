package com.alphi.myapplication.Utils;
/*
  IDEA 2022/03/20
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;

import com.alphi.myapplication.BuildConfig;

import java.util.HashSet;
import java.util.Set;

public class BlackFilter {

    private static final String[] filters = {"sau"};
    private static SharedPreferences preferences;

    public static boolean isPkgBlackFilter(Context context, ApplicationInfo applicationInfo) {
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1)
            return false;
        for (String filter : filters) {
            if (applicationInfo.packageName.contains(filter))
                return true;
        }
        Set<String> pkg_filter = readPkgBlackFilter(context);
        return pkg_filter.contains(applicationInfo.packageName);
    }

    public static Set<String> readPkgBlackFilter(Context context) {
        preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
        return new HashSet<>(preferences.getStringSet("pkg_filter", new HashSet<>()));
    }

    public static void writePkgBlackFilter(Context context, String pkgName) {
        Set<String> pkg_filter = readPkgBlackFilter(context);
        pkg_filter.add(pkgName);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putStringSet("pkg_filter", pkg_filter);
        edit.apply();
    }
}
