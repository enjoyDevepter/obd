package com.mapbar.adas.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by xiaoyee on 12/8/16.
 * 兼容获取存储设备路径的老逻辑,仅仅在包内使用，其他地方严禁使用
 * <p>
 * 此类存在的目的主要是避免因为"老版本与新版本获取存储路径的算法不一致，导致老版本数据无法在新版本上识别的问题"
 * </p>
 */
final class OldLogicCompatible {
    private static final String VIRTUAL_PRE = "/mnt";

    private static List<String> mSdcard2Paths;
    private static String mSdcard1Path;
    private static String mSdcard2Path;

    private static File[] files = null;

    private OldLogicCompatible() {

    }

    static void initInstance(Context context) {
        if (Build.VERSION.SDK_INT < 19) {
            mSdcard1Path = Environment.getExternalStorageDirectory().getAbsolutePath();
            initSdcard2Paths(context);
        } else {
            files = ContextCompat.getExternalFilesDirs(context, null);
            if (files != null) {
                if (files.length > 0 && files[0] != null) {
                    mSdcard1Path = resetPath(context, files[0].getAbsolutePath());
                }
                if (files.length > 1 && files[1] != null) {
                    mSdcard2Path = resetPath(context, files[1].getAbsolutePath());
                }
            }
        }
    }

    private static String resetPath(Context context, String path) {
        String tempPath = path.replace("/Android/data/" + context.getPackageName() + "/files", "");
        File file = new File(tempPath, "temp" + System.currentTimeMillis() + ".txt");
        try {
            if (file.createNewFile()) {
                final boolean deleteSuccess = file.delete();
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, String.format("删除测试文件成功？ %s", deleteSuccess));
                }
                return tempPath;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    /**
     * sdcard是否存在
     *
     * @return true 存在，false不存在
     */
    static boolean isExsitsSdcard() {
        if (Build.VERSION.SDK_INT >= 19) {
            if (mSdcard1Path == null) {
                return false;
            }
            return Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(files[0]));
        } else {
            return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
        }
    }

    static boolean isExsitsSdcard2() {
        if (TextUtils.isEmpty(getSdcard2PathNoSlash())) {
            return false;
        }
        File f = new File(getSdcard2PathNoSlash());
        return f.exists() && f.canWrite();
    }


    static String getSdcard2PathNoSlash() {
        return mSdcard2Path;
    }


    /**
     * 获取SDCARD路径，一般是/mnt/sdcard/,现在有些手机sdcard路径不是标准的，希望各位通过这种方法获取SDCARD路径
     *
     * @return SDCARD路径
     */
    static String getSdcardPathNoSlash() {
        return mSdcard1Path;
    }

    @SuppressLint("SdCardPath")
    private static void initSdcard2Paths(Context context) {
        // 3.2及以上SDK识别路径
        mSdcard2Paths = getSdcard2Paths(context);
        mSdcard2Paths.add("/mnt/emmc");
        mSdcard2Paths.add("/mnt/extsdcard");
        mSdcard2Paths.add("/mnt/ext_sdcard");
        mSdcard2Paths.add("/sdcard-ext");
        mSdcard2Paths.add("/mnt/sdcard-ext");
        mSdcard2Paths.add("/sdcard2");
        mSdcard2Paths.add("/sdcard");
        mSdcard2Paths.add("/mnt/sdcard2");
        mSdcard2Paths.add("/mnt/sdcard");
        mSdcard2Paths.add("/sdcard/sd");
        mSdcard2Paths.add("/sdcard/external");
        mSdcard2Paths.add("/flash");
        mSdcard2Paths.add("/mnt/flash");
        mSdcard2Paths.add("/mnt/sdcard/external_sd");

        mSdcard2Paths.add("/mnt/external1");
        mSdcard2Paths.add("/mnt/sdcard/extra_sd");
        mSdcard2Paths.add("/mnt/sdcard/_ExternalSD");
        mSdcard2Paths.add("/mnt/extrasd_bin");
        //4.1SDK 识别路径
        mSdcard2Paths.add("/storage/extSdCard");
        mSdcard2Paths.add("/storage/sdcard0");
        mSdcard2Paths.add("/storage/sdcard1");
        initSdcard2();
    }

    private static void initSdcard2() {
        String sdcardPath = getSdcardPathNoSlash();
        int count = mSdcard2Paths.size();
        for (int index = 0; index < count; index++) {
            boolean isSame = isSamePath(sdcardPath, mSdcard2Paths.get(index));
            if (isSame) {
                continue;
            }
            boolean isExsits = isExsitsPath(mSdcard2Paths.get(index));
            if (isExsits && !isSameSdcard(sdcardPath, mSdcard2Paths.get(index))) {
                mSdcard2Path = mSdcard2Paths.get(index);
                break;
            }
        }
    }

    private static boolean isSameSdcard(String sdcard1, String sdcard2) {
        long sdcard1Size = getSdcardSize(sdcard1);
        long sdcard2Size = getSdcardSize(sdcard2);
        if (sdcard1Size != sdcard2Size) {
            return false;
        }
        sdcard1Size = getSdcardAvailableSize(sdcard1);
        sdcard2Size = getSdcardAvailableSize(sdcard2);
        if (sdcard1Size != sdcard2Size) {
            return false;
        }

        File f1 = new File(sdcard1);
        File f2 = new File(sdcard2);

        String[] fileList1 = f1.list();
        String[] fileList2 = f2.list();

        //都是空，则认为是同一个目录
        if (fileList1 == null && fileList2 == null) {
            return true;
        }

        //有一个为空，则认为是不同目录
        if (fileList1 == null || fileList2 == null) {
            return false;
        }

        //不一样多的文件，则认为不同目录
        if (fileList1.length != fileList2.length) {
            return false;
        }

        //判断文件是否完全一样
//		int count = fileList1.length;
//		for(int index=0;index<count;index++)
//		{
//			boolean isHave = false;
//			for(int j=0;j<count;j++)
//			{
//				String name1= PathOperator.getFileName(fileList1[index]);
//				String name2 = PathOperator.getFileName(fileList2[j]);
//				if(name1.toLowerCase().equals(name2.toLowerCase()))
//				{
//					isHave = true;
//					break;
//				}
//			}
//			if(!isHave)
//			{
//				return false;
//			}
//		}
        return true;
    }

    private static boolean isExsitsPath(String path) {
        File f = new File(path);
        return f.exists() && f.canWrite();
    }

    private static boolean isSamePath(String path, String path2) {
        // 名称有空则认为一样
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(path2)) {
            return true;
        }
        if (path.trim().toLowerCase().equals(path2.trim().toLowerCase())) {
            return true;
        }
        //添加/mnt
        if (path2.trim().toLowerCase().equals((VIRTUAL_PRE + path).trim().toLowerCase())) {
            return true;
        }
        //添加/mnt
        return path.trim().toLowerCase().equals((VIRTUAL_PRE + path2).trim().toLowerCase());
    }

    @SuppressLint("InlinedApi")
    private static List<String> getSdcard2Paths(Context context) {
        List<String> paths = new LinkedList<>();
        if (Build.VERSION.SDK_INT < 13) {
            return paths;
        }
        android.os.storage.StorageManager sm = (android.os.storage.StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Class<? extends android.os.storage.StorageManager> clazz = sm.getClass();
            Method mlist = clazz.getMethod("getVolumeList", (Class[]) null);
            Class<?> cstrvol = Class.forName("android.os.storage.StorageVolume");
            Method mvol = cstrvol.getMethod("getPath", (Class[]) null);
            Object[] objects = (Object[]) mlist.invoke(sm);
            if (objects != null && objects.length > 0) {
                for (Object obj : objects) {
                    paths.add((String) mvol.invoke(obj));
                }
            }
        } catch (Exception e) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                Log.w(LogTag.STORAGE_DEVICE, String.format("获取 sd 卡 2 的路径失败，原因是 %s", e.getCause()));
            }
        }
        return paths;
    }

    private static long getSdcardSize(String sdcardPath) {
        long size = 0;
        try {
            StatFs statFs = new StatFs(sdcardPath);
            //noinspection deprecation
            int blockSize = statFs.getBlockSize();
            //noinspection deprecation
            int totalBlocks = statFs.getBlockCount();
            size = (long) totalBlocks * (long) blockSize;
        } catch (Exception e) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                Log.w(LogTag.STORAGE_DEVICE, String.format("获取存储设备(路径为%s)的大小失败,原因为 %s", sdcardPath, e.getCause()));
            }
        }
        return size;
    }

    private static long getSdcardAvailableSize(String sdcardPath) {
        long size = 0;
        try {
            StatFs statFs = new StatFs(sdcardPath);
            //noinspection deprecation
            int blockSize = statFs.getBlockSize();
            //noinspection deprecation
            int availableBlocks = statFs.getAvailableBlocks();
            size = (long) availableBlocks * (long) blockSize;
        } catch (Exception e) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                Log.w(LogTag.STORAGE_DEVICE, String.format("获取存储设备(路径为%s)的可用大小失败,原因为 %s", sdcardPath, e.getCause()));
            }
        }
        return size;
    }
}