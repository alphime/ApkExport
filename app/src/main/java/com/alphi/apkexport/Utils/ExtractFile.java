package com.alphi.apkexport.Utils;
/*
  IDEA 2022/02/18
 */

import static com.alphi.apkexport.Utils.ZipUtil.zipFile;

import android.os.Environment;
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

public class ExtractFile {
    private File file;
    private String[] filesPath;
    private final String outFileName;
    private final String TAG = ExtractFile.class.getSimpleName();
    private File outFile;
    private String obb_fileName;
    private FileInputStream obb_inputStream;
    public static String savePath = "ApkExport";


    public ExtractFile(final String filePath, final String outFileName) {
        this.file = new File(filePath);
        this.outFileName = outFileName;
    }

    public ExtractFile(final String[] filesPath, final String outFileName) {
        this.filesPath = filesPath;
        this.outFileName = outFileName;
    }

    /**
     * 对于xapk
     *
     * @param apk_filePath    原生路径
     * @param obb_fileName    obb二进制文件名
     * @param obb_inputStream obb二进制输出流
     * @param outFileName     输出文件名
     */
    public ExtractFile(final String apk_filePath, String obb_fileName, FileInputStream obb_inputStream, final String outFileName) {
        this.file = new File(apk_filePath);
        this.obb_fileName = obb_fileName;
        this.obb_inputStream = obb_inputStream;
        this.outFileName = outFileName;
    }

    public final boolean toSave() {
        File storage = Environment.getExternalStorageDirectory();
        File myAppsDir = new File(storage, savePath);
        if (!myAppsDir.exists()) {
            boolean mkdirs = myAppsDir.mkdirs();
            if (!mkdirs) {
                Log.e(TAG, "toSave: 外部储存目录创建失败！");
                return false;
            }
        }
        outFile = new File(myAppsDir, outFileName);

        // 验证已存在的文件
        if (verifyExistFile()) return true;

        // 备份操作：

        // 对于apks
        if (filesPath != null) {
            return zipFile(filesPath, outFile);
        }

        // 对于XApk
        if (obb_fileName != null && obb_inputStream != null) {
            try (
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(outFile));
                WritableByteChannel writableByteChannel = Channels.newChannel(zos);
                FileChannel apk_inChannel = new FileInputStream(file).getChannel();
                FileChannel obb_inChannel = obb_inputStream.getChannel()
            ) {
                zos.putNextEntry(new ZipEntry("base.apk"));
                apk_inChannel.transferTo(0, apk_inChannel.size(), writableByteChannel);
                zos.closeEntry();
                apk_inChannel.close();
                zos.putNextEntry(new ZipEntry(obb_fileName));
                obb_inChannel.transferTo(0, obb_inChannel.size(), writableByteChannel);
                zos.closeEntry();
                obb_inChannel.close();
                return true;
            } catch (Exception e) {
                Log.e(TAG, "toSave: ", e);
                return false;
            }
        }

        // 对于普通apk
        try(
            FileChannel inChannel = new FileInputStream(file).getChannel();
            FileChannel outChannel = new FileOutputStream(outFile).getChannel()
        ) {
            outChannel.transferFrom(inChannel, 0, inChannel.size());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "toSave: ", e);
            Log.e(TAG, "toSave: " + storage.getPath());
            return false;
        }
    }

    /**
     * 验证已存在的文件
     *
     * @return 相同则返回true
     */
    private boolean verifyExistFile() {
        if (outFile.exists() && outFile.isFile()) {
            String fName = outFile.getName();
            String fType = fName.substring(fName.lastIndexOf("."));
            if (fType.equals(".apk")) {
                if (file.length() == outFile.length()) return true;
            } else if (fType.equals(".xapk")) {
                try (ZipFile zipFile = new ZipFile(outFile)) {
                    Enumeration<? extends ZipEntry> entries1 = zipFile.entries();
                    while (entries1.hasMoreElements()) {
                        ZipEntry zipEntry = entries1.nextElement();
                        if (zipEntry.getName().equals("base.apk")) {
                            if (zipEntry.getSize() != file.length()) return false;
                        } else {
                            if (zipEntry.getSize() < 10) return false;
                        }
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                try (ZipFile zipFile = new ZipFile(outFile)) {
                    Enumeration<? extends ZipEntry> entries1 = zipFile.entries();
                    int filesPathLength = filesPath.length;
                    File[] files = new File[filesPathLength];
                    for (int i = 0; i < filesPathLength; i++) {
                        String path = filesPath[i];
                        files[i] = new File(path);
                    }
                    int passVerifyFile = 0;
                    while (entries1.hasMoreElements()) {
                        ZipEntry zipEntry = entries1.nextElement();
                        for (File file : files) {
                            if (file.getName().equals(zipEntry.getName())) {
                                if (file.length() == zipEntry.getSize()) {
                                    passVerifyFile++;
                                    break;
                                }
                                return false;
                            }
                        }
                    }
                    if (zipFile.size() == filesPathLength &&
                            filesPathLength == passVerifyFile) return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            outFile.delete();
        }
        return false;
    }

    public final String getOutFileDir() {
        return outFile.getPath();
    }
}
