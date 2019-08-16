package com.miyuan.obd.log;

//INFO 工具/文件/图吧常用路径管理工具

/**
 * 图吧业务相关的sd卡路径使用的工具类
 */
public class MapbarSdcardUtil {

    public static final String productPath = "adas/";
    private static final String productpathNoSlash = "adas";

    private static final String oldProductPath = "adas/";

    /**
     * 获取SDCARD下adas的路径，一般是/mnt/sdcard/adas
     *
     * @return 外置SD卡adas路径
     */
    protected static String getSdcardMapbarPathNoSlash() {
        return SdcardUtil.getSdcardPath() + productpathNoSlash;
    }

    protected static String getSdcard2MapbarPathNoSlash() {
        return SdcardUtil.getSdcard2Path() + productpathNoSlash;
    }

    /**
     * 获取SDCARD下adas的路径，一般是/mnt/sdcard/adas/
     *
     * @return 外置SD卡adas路径
     */
    protected static String getSdcardMapbarPath() {
        return SdcardUtil.getSdcardPath() + productPath;
    }

    protected static String getSdcard2MapbarPath() {
        return SdcardUtil.getSdcard2Path() + productPath;
    }

    protected static String getSdcardMapbarOldPath() {
        return SdcardUtil.getSdcardPath() + oldProductPath;
    }

    protected static String getSdcard2MapbarOldPath() {
        return SdcardUtil.getSdcard2Path() + oldProductPath;
    }
}
