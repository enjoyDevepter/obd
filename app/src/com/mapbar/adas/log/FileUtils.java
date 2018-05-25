package com.mapbar.adas.log;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Comparator;

import static java.io.File.separatorChar;


/**
 *
 */
public class FileUtils {
    private static final int BYTE_UNIT = 1024;

    /**
     * 删除重复的相邻斜杠和任何尾部斜杠
     */
    public static String fixSlashes(String origPath) {
        return fixSlashes(origPath, true);
    }

    /**
     * 删除重复的相邻斜杠
     *
     * @param containLast 是否删除尾部的斜杠
     */
    public static String fixSlashes(String origPath, boolean containLast) {
        //step 1、 Remove duplicate adjacent slashes.
        boolean lastWasSlash = false;
        char[] newPath = origPath.toCharArray();
        int length = newPath.length;
        int newLength = 0;
        for (int i = 0; i < length; ++i) {
            char ch = newPath[i];
            if (ch == '/') {
                if (!lastWasSlash) {
                    newPath[newLength++] = separatorChar;
                    lastWasSlash = true;
                }
            } else {
                newPath[newLength++] = ch;
                lastWasSlash = false;
            }
        }
        //step 2、 Remove any trailing slash (unless this is the root of the file system).
        if (containLast && lastWasSlash && newLength > 1) {
            newLength--;
        }
        //STEP 3、 Reuse the original string if possible.
        return (newLength != length) ? new String(newPath, 0, newLength) : origPath;
    }

    public static boolean isDir(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        final File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            return true;
        }
        return false;
    }

    /**
     * 格式化文件大小为易读的形式，保留两位小数;将以 byte 为单位的文件大小转化为对应单位的；如 10245byte = 10KB
     *
     * @param bytes 待格式化的文件大小，以 byte 为单位，如 10245 byte;要求大于0
     * @return 格式化后的字符串，带单位；如10KB
     * @see <a href="https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java">stackoverflow.com</a>
     */
    @SuppressLint("DefaultLocale")
    public static String byteCountToDisplaySize(long bytes) {
        if (bytes < BYTE_UNIT) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(BYTE_UNIT));
        char pre = "kMGTPE".charAt(exp - 1);
        return String.format("%.2f%sB", bytes / Math.pow(BYTE_UNIT, exp), pre);
    }

    /**
     * 路径所在文件是否存在且可读可写
     */
    public static boolean isWRable(@Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        final File file = new File(path);
        return file.exists() && file.canWrite() && file.canRead();
    }

    public static boolean canWriteFile(@Nullable String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        final File file = new File(path, "temp" + System.currentTimeMillis() + ".txt");
        try {
            if (file.createNewFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                return true;
            }
        } catch (IOException e) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                Log.w(LogTag.STORAGE_DEVICE, String.format("文件不能正常创建，意味着此路径：%s 是不可用的,原因为:\r\n\t\t%s", path, e.getCause()));
            }
        }

        return false;
    }

    /**
     * 判断所给路径的文件夹为空文件夹
     */
    public static boolean isDirEmpty(@Nullable String dirPath) {
        if (TextUtils.isEmpty(dirPath)) {
            return true;
        }
        final File dirFile = new File(dirPath);
        if (dirFile.exists() && dirFile.isDirectory()) {
            File[] subFiles = dirFile.listFiles();
            if (subFiles != null && subFiles.length != 0) {
                return false;
            }
        }

        return true;
    }


    /**
     * 拼接文件名，并且去除多余的斜杠
     *
     * @param path          初始路径如 /storage/sdcard0
     * @param fileOrDirName 要拼接上的文件或者目录名称，如 image
     * @return 拼接完成后的字符串，如 /storage/sdcard0/image
     */
    public static String join(@Nullable String path, @NonNull String fileOrDirName) {
        return fixSlashes(path + File.separator + fileOrDirName, false);
    }

    public static String readFile(String str) throws IOException {
        return readToString(new BufferedReader(new FileReader(str)));
    }

    public static String readToString(Reader reader) throws IOException {
        try {
            char[] cArr = new char[24];
            StringBuilder stringBuilder = new StringBuilder();
            while (true) {
                int read = reader.read(cArr);

                if (read <= 0) {
                    break;
                }

                for (char c : cArr) {
                    if (c == '\0') {
                        stringBuilder.append(" ");
                    } else {
                        stringBuilder.append(c);
                    }
                }

            }
            return stringBuilder.toString();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    public static String toString(File f) {
        char[] buf = new char[1024];
        StringBuilder builder = new StringBuilder();
        FileReader fileReader = null;
        try {
            fileReader = new FileReader(f);
            while (fileReader.ready()) {
                int len = fileReader.read(buf);
                builder.append(buf, 0, len);
            }
            return builder.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            if (null != fileReader) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void toFile(String s, File f) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(f);
            fileWriter.write(s);
            fileWriter.flush();
        } catch (IOException e) {
        } finally {
            if (null != fileWriter) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * 通过递归方式删除文件或文件夹
     *
     * @param file
     */
    public static boolean delete(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File subFile : files) {
                    FileUtils.delete(subFile);
                }
            }
            return file.delete();// 删除文件夹
        } else {
            return file.delete();// 删除文件
        }
    }

    /**
     * 检查指定路径的文件夹是否存在，如果不存在则创建
     *
     * @param dirPath 待检查的文件夹路径
     */
    public static void checkDirExist(String dirPath) {
        File file = new File(dirPath);
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, String.format("checkDirExist dirPath: %s", dirPath));
        }
        if (!file.exists() || !file.isDirectory()) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, "满足!file.exists() || !file.isDirectory()，现在创建文件夹相关");
            }
            final boolean createSuccess = file.mkdirs();
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, "创建文件夹成功？" + createSuccess);
                Log.i(LogTag.STORAGE_DEVICE, "创建文件夹成功？file.isDirectory" + file.isDirectory());
            }
        } else {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, "非  !file.exists() || !file.isDirectory()");
            }
        }
    }

    /**
     * 确保是个目录
     *
     * @param dir
     * @return
     */
    public static boolean makeSureDir(File dir) {
        if (dir.isFile()) {
            dir.delete();
        }
        return !(!dir.isDirectory() && !dir.mkdirs());
    }

    /**
     * 文件比较器（排序用）。新文件放前面（例如：下标0），旧文件放后面（例如：下标9）
     */
    public static class FileComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            long fM1 = f1.lastModified();
            long fM2 = f2.lastModified();
            if (fM1 < fM2) {// f1更旧
                return 1;// f1放后面
            } else if (fM1 > fM2) {// f1更新
                return -1;// f1放前面
            } else {// 一样
                return 0;
            }
        }
    }

}
