package com.alphi.apkexport.utils;
/*
  IDEA 2022/03/20
 */

import android.content.pm.ApplicationInfo;

public class BlackFilter {

    private static final String[] filters = {"sau", "romupdate"};

    public static boolean isPkgBlackFilter(ApplicationInfo applicationInfo) {
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 1)
            return false;
        for (String filter : filters) {
            if (applicationInfo.packageName.contains(filter))
                return true;
        }
        return false;
    }
}
