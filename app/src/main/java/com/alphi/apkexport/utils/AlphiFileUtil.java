package com.alphi.apkexport.utils;
/*
  IDEA 2022/02/17
 */

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.channels.FileChannel;

public class AlphiFileUtil {

    public static String getSize(File file) {
        long longSize = getLongSize(file);
        return getSize(longSize);
    }

    public static String getSize(long size) {
        double ds = Long.valueOf(size).doubleValue();
        int unitAt = 0;
        String[] Unit = {"B", "kB", "MB", "GB", "TB"};
        for (; unitAt < Unit.length; unitAt++) {
            if (ds < 1024) break;
            ds = ds / 1024;
        }
        BigDecimal bigDecimal = new BigDecimal(ds).setScale(2, BigDecimal.ROUND_HALF_UP);
        double value = bigDecimal.doubleValue();
        if (value % 1 == 0)
            return (int) value + " " + Unit[unitAt];
        else
            return value + " " + Unit[unitAt];
    }

    public static long getLongSize(File file) {
        long length = file.length();
        if (length == 0) {
            try (FileChannel channel = new FileInputStream(file).getChannel()){
                return channel.size();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return length;
    }
}
