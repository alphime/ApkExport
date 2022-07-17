package com.alphi.apkexport.utils;
/*
  IDEA 2022/02/19
 */

import android.content.pm.PackageInfo;

import androidx.annotation.NonNull;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.Comparator;
import java.util.Map;

public class MyAppComparator {
    public static class AppNameComparator implements Comparator<PackageInfo> {
        private final Map<String, String> appLabels;
        public static boolean is_abcTop;

        public AppNameComparator(Map<String, String> appLabels) {
            this.appLabels = appLabels;

        }

        @Override
        public int compare(PackageInfo a1, PackageInfo a2) {
            String s1 = appLabels.get(a1.packageName);
            String ds1 = dealStr(s1);
            String s2 = appLabels.get(a2.packageName);
            String ds2 = dealStr(s2);
            // 当应用首个字母与应用首个汉字的声母相同时
            if (ds1.charAt(0) == ds2.charAt(0)) {
                for (int i = 0; i < 2; i++) {
                    if (s1.length() < 2 || s2.length() < 2){
                        break;
                    }
                    char c1 = s1.charAt(i);
                    char c2 = s2.charAt(i);
                    if (!isChinese(c1) && isChinese(c2)) {
                        return 1;
                    } else if (isChinese(c1) && !isChinese(c2)) {
                        return -1;
                    }
                }
            }
            if (is_abcTop) {
                if (!Character.isLetter(ds1.charAt(0)) && Character.isLetter(ds2.charAt(0))) {
                    return 1;
                } else if (Character.isLetter(ds1.charAt(0)) && !Character.isLetter(ds2.charAt(0))) {
                    return -1;
                }
            }
            return MyAppComparator.compare(ds1, ds2);
        }

        private String dealStr(String s) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, len = s.length(); i < len; i++) {
                char c = s.charAt(i);
                if (isChinese(c)) {
                    sb.append(chineseSyllable(c));
                } else if (65 <= c && c <= 90) {
                    c = (char) (c + 32);
                    sb.append(c);
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        private String chineseSyllable(char c) {
            StringBuilder sb = new StringBuilder();
            String[] strings = PinyinHelper.toHanyuPinyinStringArray(c);
            for (String string : strings) {
                sb.append(string);
            }
            return sb.toString();
        }

        /**
         * 判断是否中文
         */
        private boolean isChinese(char c) {
            return 19968 <= c && c <= 40869;
        }
    }

    public static class PackageNameComparator implements Comparator<PackageInfo> {
        @Override
        public int compare(PackageInfo o1, PackageInfo o2) {
            return MyAppComparator.compare(o1.packageName, o2.packageName);
        }
    }

    public static class ApkSizeComparator implements Comparator<PackageInfo> {
        private final Map<String, Long> apkSize;

        public ApkSizeComparator(@NonNull Map<String, Long> apkSize) {
            this.apkSize = apkSize;
        }

        @Override
        public int compare(PackageInfo o1, PackageInfo o2) {
            long appSize1 = apkSize.get(o1.packageName);
            long appSize2 = apkSize.get(o2.packageName);
            return Long.compare(appSize2, appSize1);
        }
    }

    public static class appSizeComparator implements Comparator<PackageInfo> {

        private final Map<String, Long> appSize;

        public appSizeComparator(Map<String, Long> appSize) {
            this.appSize = appSize;
        }

        @Override
        public int compare(PackageInfo o1, PackageInfo o2) {
            Long appSize1 = appSize.get(o1.packageName);
            Long appSize2 = appSize.get(o2.packageName);
            long cp1 = appSize1 == null ? 0 : appSize1;
            long cp2 = appSize2 == null ? 0 : appSize2;
            return Long.compare(cp2, cp1);
        }
    }

    public static class sdkVersionComparator implements Comparator<PackageInfo>{
        @Override
        public int compare(PackageInfo o1, PackageInfo o2) {
            return o2.applicationInfo.targetSdkVersion - o1.applicationInfo.targetSdkVersion;
        }
    }

    public static class lastUpdataComparator implements Comparator<PackageInfo> {

        @Override
        public int compare(PackageInfo o1, PackageInfo o2) {
            return Long.compare(o2.lastUpdateTime, o1.lastUpdateTime);
        }
    }

    public static class firstInstallDataComparator implements Comparator<PackageInfo> {

        @Override
        public int compare(PackageInfo o1, PackageInfo o2) {
            return Long.compare(o2.firstInstallTime, o1.firstInstallTime);
        }
    }

    public static int compare(String s1, String s2) {
        int length = Math.min(s1.length(), s2.length());
        for (int i = 0; i < length; i++) {
            char c1 = s1.charAt(i);
            char c2 = s2.charAt(i);
            if (c1 == c2) continue;
            return c1 - c2;
        }
        return 0;
    }
}
