package com.cuipengyu.bookpagetest;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by cuipengyu on 2018/3/28.
 */

public class FileUtils {

    public static String getChapterPath(String bookId, int chapter) {
        return Constant.PATH_TXT + bookId + File.separator + chapter + ".txt";
    }

    /**
     * 根据是否存在sd卡返回路径
     */
    public static String createRootPath(Context appConnect) {
        String cacheRootPath = "";
        //sd是否存在
        if (isSdCardAvailable()) {
            //获取 /sdcard/Android/data/<application package>/cache 一般存放临时缓存数据
            cacheRootPath = appConnect.getExternalCacheDir().getPath();
        } else {
            //获取 /data/data/<application package>/cache 获取缓存路径
            cacheRootPath = appConnect.getCacheDir().getPath();
        }
        return cacheRootPath;
    }

    /**
     * 确认sdcard的存在
     *
     * @return
     */
    public static boolean isSdCardAvailable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    //获取章节文件并判断是否为空，为空创建文件否则返回
    public static File getChapterFile(String bookId, int chapter) {
        File file = new File(getChapterPath(bookId, chapter));
        if (!file.exists())
            createFile(file);
        return file;
    }

    //创建文件
    private static String createFile(File file) {
        try {
            //public File getParentFile()返回此抽象路径名的父路径名的抽象路径名，如果此路径名没有指定父目录，则返回 null。	public boolean exists()
            // 测试此抽象路径名表示的文件或目录是否存在。
            if (file.getParentFile().exists()) {
                Log.e("----- 创建文件", file.getAbsolutePath());
                file.createNewFile();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                file.createNewFile();
                Log.e("----- 创建文件", file.getAbsolutePath());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 创建文件夹
     * public boolean mkdir()
     * 创建此抽象路径名指定的目录
     */

    private static String createDir(String dirPath) {
        try {
            File file = new File(dirPath);

            if (file.getParentFile().exists()) {
                Log.e("----- 创建文件夹", file.getAbsolutePath());
                file.mkdir();
                return file.getAbsolutePath();
            } else {
                createDir(file.getParentFile().getAbsolutePath());
                Log.e("----- 创建文件夹", file.getAbsolutePath());
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dirPath;
    }

    //返回字符编码
    public static String getCharset(String fileName) {
        BufferedInputStream bis = null;
        String charset = "GBK";
        byte[] first3Bytes = new byte[3];
        try {
            boolean checked = false;
            bis = new BufferedInputStream(new FileInputStream(fileName));
            bis.mark(0);
            int read = bis.read(first3Bytes, 0, 3);
            if (read == -1)
                return charset;
            if (first3Bytes[0] == (byte) 0xFF && first3Bytes[1] == (byte) 0xFE) {
                charset = "UTF-16LE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xFE
                    && first3Bytes[1] == (byte) 0xFF) {
                charset = "UTF-16BE";
                checked = true;
            } else if (first3Bytes[0] == (byte) 0xEF
                    && first3Bytes[1] == (byte) 0xBB
                    && first3Bytes[2] == (byte) 0xBF) {
                charset = "UTF-8";
                checked = true;
            }
            bis.mark(0);
            if (!checked) {
                while ((read = bis.read()) != -1) {
                    if (read >= 0xF0)
                        break;
                    if (0x80 <= read && read <= 0xBF) // 单独出现BF以下的，也算是GBK
                        break;
                    if (0xC0 <= read && read <= 0xDF) {
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) // 双字节 (0xC0 - 0xDF)
                            // (0x80 - 0xBF),也可能在GB编码内
                            continue;
                        else
                            break;
                    } else if (0xE0 <= read && read <= 0xEF) {// 也有可能出错，但是几率较小
                        read = bis.read();
                        if (0x80 <= read && read <= 0xBF) {
                            read = bis.read();
                            if (0x80 <= read && read <= 0xBF) {
                                charset = "UTF-8";
                                break;
                            } else
                                break;
                        } else
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return charset;
    }
}
