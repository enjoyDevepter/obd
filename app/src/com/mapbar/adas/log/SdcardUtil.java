package com.mapbar.adas.log;

import android.content.Context;
import android.support.annotation.Nullable;

import java.io.File;

import static com.mapbar.adas.log.StorageManager.InstanceHolder.STORAGE_MANAGER;

/**
 * SD卡状  态操作工具类
 */
public class SdcardUtil {

    //// TODO: 12/5/16 需要将以下常量移到 MapbarStorageUtil 类中进行统一管理；
    // todo：还有一个原因就是， SdcardUtil 类只是一个基础工具类，不应该将业务相关代码放入其中，但是，目前违章查询也耦合了此项，故而暂时不做移动
    public static String productPath = "/obd/";
    public static String offlineDataPath = "/obd/";
    public static String mVirtualHeader = "/mnt";
    public static String poiImagePath = "/obd/poi/image";
    public static String productpathNoSlash = "/obd";
    public static String offlineDataNoSlash = "/obd";
    public static String oldProductPath = "/obd/";

    private static SdcardUtil mInstance;

    private SdcardUtil() {
    }

    public static void initInstance(Context context) {
        STORAGE_MANAGER.refreshStorage();
        mInstance = new SdcardUtil();
        STORAGE_MANAGER.startWatchStorageStateChange(context);
    }

    public static void unInitInstance(Context context) {
        STORAGE_MANAGER.stopWatchStorageStateChage(context);
    }

    /**
     * 获取单实例
     *
     * @return Sdcard对象
     */
    public static SdcardUtil getInstance() {
        return mInstance;
    }

    public static String getSdcardState() {
        return STORAGE_MANAGER.getInternalDeviceState();
    }

    public static String getSdcard2State() {
        return STORAGE_MANAGER.getExternalDeviceState();
    }

    /**
     * sdcard是否存在
     *
     * @return true 存在，false不存在
     */
    public static boolean isExsitsSdcard() {
        return STORAGE_MANAGER.isInternalExist();
    }

    public static boolean isExistsStorageDevice() {
        return isExsitsSdcard() || isExsitsSdcard2();
    }

    public static boolean isExsitsSdcard2() {
        return STORAGE_MANAGER.isExternalExist();
    }

    /**
     * 获取外置 SD 卡路径
     *
     * @return NOTE:<b>如果外置存储卡不存在，那么返回值会为 null </b>
     */
    @Nullable
    public static String getSdcard2Path() {
        if (isExsitsSdcard2()) {
            return STORAGE_MANAGER.getExternalDevicePath() + File.separator;
        }
        return null;
    }

    /**
     * 获取内置卡路径，只是基础路径如 /storage/sdcard1 并不确保是否一定可读可写
     *
     * @return NOTE:<b>如果内置存储卡不存在，那么返回值会为 null </b>
     */
    @Nullable
    public static String getSdcardPath() {
        if (isExsitsSdcard()) {
            return STORAGE_MANAGER.getInternalDevicePath() + File.separator;
        }
        return null;
    }

    /**
     * 返回SD卡大小，单位是Byte
     */
    public static long getSdcardSize() {
        if (STORAGE_MANAGER.isInternalExist()) {
            assert STORAGE_MANAGER.getInternalDevice() != null : "isInternalExist return true, but getInternalDevice return null";
            return STORAGE_MANAGER.getInternalDevice().getQuota().getTotalSpace();
        }
        return 0;
    }

    public static long getSdcard2Size() {
        if (STORAGE_MANAGER.isExternalExist()) {
            assert STORAGE_MANAGER.getExternalDevice() != null : "isExternalExist return true, but getExternalDevice return null";
            return STORAGE_MANAGER.getExternalDevice().getQuota().getTotalSpace();
        }
        return 0;
    }

    /**
     * 返回SD卡剩余大小，单位是Byte
     */
    public static long getSdcardAvailSize() {
        if (STORAGE_MANAGER.isInternalExist()) {
            assert STORAGE_MANAGER.getInternalDevice() != null : "isInternalExist return true, but getInternalDevice return null";
            return STORAGE_MANAGER.getInternalDevice().getQuota().getFreeSpace();
        }
        return 0;
    }

    /**
     * 返回SD卡剩余大小，单位是Byte
     */
    public static long getSdcard2AvailSize() {
        if (STORAGE_MANAGER.isExternalExist()) {
            assert STORAGE_MANAGER.getExternalDevice() != null : "isExternalExist return true, but getExternalDevice return null";
            return STORAGE_MANAGER.getExternalDevice().getQuota().getFreeSpace();
        }
        return 0;
    }

}