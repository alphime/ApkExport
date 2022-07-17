package com.alphi.ApkExport.Utils;
/*
  IDEA 2022/02/18
 */

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {
    public static boolean zipFile(final String[] filesPath, final File outFile) {
        try (
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
            WritableByteChannel writableByteChannel = Channels.newChannel(zos)
        ) {
            for (String filePath : filesPath) {
                File file = new File(filePath);
                FileChannel channel = new FileInputStream(file).getChannel();
                try {
                    zos.putNextEntry(new ZipEntry(file.getName()));      //文件名
                    channel.transferTo(0, channel.size(), writableByteChannel);
                    zos.closeEntry();   //关闭当前 ZIP 条目并定位流以写入下一个条目。
                } catch (Exception e) {
                    e.printStackTrace();
                }
                channel.close();
            }
            return true;
        } catch (IOException e) {
            Log.e(ZipUtil.class.getSimpleName(), "zipFile: ", e);
            return false;
        }
    }

    public static boolean readZip_IsExistFile(String path, String contains){
        try (ZipFile zipFile = new ZipFile(path)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            if (contains != null) {
                while (entries.hasMoreElements()) {
                    if (entries.nextElement().toString().contains(contains)){
                        return true;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
