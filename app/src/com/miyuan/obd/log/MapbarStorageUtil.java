package com.miyuan.obd.log;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.miyuan.adas.GlobalUtil;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


/**
 * //INFO 工具/文件/图吧常用路径管理工具
 * 图吧业务相关的存储路径使用的工具类
 */
public class MapbarStorageUtil {

    private static final String productPath = SdcardUtil.productPath;
    private static final String productpathNoSlash = SdcardUtil.productpathNoSlash;
    private static final String oldProductPath = SdcardUtil.oldProductPath;
    private static final String poiImagePath = SdcardUtil.poiImagePath;
    private static final String offlineDataPath = SdcardUtil.offlineDataPath;
    private static final String offlineDataNoSlash = SdcardUtil.offlineDataNoSlash;
    /**
     * 是否已经检查过"向前兼容"了
     */
    private static boolean mIsCheckedForwardCompatible = false;

    /**
     * 默认先读取外部存储
     * 获取程序当前存储类型所在的"产品存储路径"，如果当前选择的不可用，那么就会自动切换到可用的存储设备路径
     *
     */
    public static String getCurrentValidMapbarPath() {

        setCurrentStorageType(StorageType.INSIDE);

        final String basePath = getCurrentStoragePath(true);
        return FileUtils.join(basePath, productPath);
    }

    public static String getCurrentValidImgPath() {
        final String basePath = getCurrentStoragePath(true);
        return FileUtils.join(basePath, poiImagePath);
    }


//    public static String productPath = "mapbar_navidog/navidata/";
//    public static String offlineDataPath = "mapbar_navidog/navidata/";
//    public static String VIRTUAL_PRE = "/mnt";
//    public static String poiImagePath = "mapbar_navidog/navidata/poi/image";
//    public static String productpathNoSlash = "mapbar_navidog/navidata";
//    public static String offlineDataNoSlash = "mapbar_navidog/navidata";
//    public static String oldProductPath = "mapbar/";

    /**
     * 获取当前存储类型所在的存储路径
     *
     * @param isAutoChooseAvailable 是否自动选择可用的存储；
     *                              <p>
     *                              如果设置为 true，那么如果当前存储类型不可用，那么会设置为可用的存储类型（如当前类型为 OUTSIDE，但是非可用状态，那么就会自动返回 INSIDE 类型的存储路径）
     *                              </p>
     * @return 存储当前存储类型所在的存储路径；如果 isAutoChooseAvailable 为 false，那么当 当前选择的存储类型不可用时将返回 null
     */
    @Nullable
    public static String getCurrentStoragePath(boolean isAutoChooseAvailable) {
        final LocalStorage currentStorageDevice = getCurrentStorageDevice(isAutoChooseAvailable);
        if (currentStorageDevice == null) {
            return null;
        }
        if (getCurrentStorageType() == StorageType.CUSTOM) {
            return getCustomDataPath();
        }

        return currentStorageDevice.getPath();
//        return StorageManager.checkValidStoragePath(currentStorageDevice.getPath(), GlobalUtil.getContext());
    }

    /**
     * 获取用户当前设置的存储设备
     *
     * @param isAutoChooseAvailable 是否自动选择可用的存储设备(如果设置为 true ，那么当"当前选择的存储设备不可用"时就会自动选择当前可用的存储设备)
     * @see #getCurrentStoragePath(boolean)
     */
    @Nullable
    public static LocalStorage getCurrentStorageDevice(boolean isAutoChooseAvailable) {

        @StorageTypes int storageType = getCurrentStorageType();
        boolean isCurrentAvaliable;
        switch (storageType) {
            case StorageType.INSIDE:
                isCurrentAvaliable = isTargetStorageAvaliable(StorageType.INSIDE);
                if (isCurrentAvaliable) {
                    return StorageManager.InstanceHolder.STORAGE_MANAGER.getInternalDevice();
                } else {
                    if (isAutoChooseAvailable) {
                        //内置的不可用，先判断一下是否外置的可用吧
                        final boolean isOutterAvaliable = isTargetStorageAvaliable(StorageType.OUTSIDE);
                        if (isOutterAvaliable) {
                            setCurrentStorageType(StorageType.OUTSIDE);
                            PreferencesConfig.lastChoosedStorageNotValid();
                            return StorageManager.InstanceHolder.STORAGE_MANAGER.getExternalDevice();
                        }
                        //如果内置、外置都不可用，那么再返回 INNER 的

//                    throw new IllegalStateException(String.format("内置卡竟然不可用？出错了吧,收集到的信息为:<<<<>>>>%s", ThrowUtil.getThrowInfo()));
                        return innerFilesStorage();
                    } else {
                        return null;
                    }
                }
            case StorageType.CUSTOM:
                isCurrentAvaliable = isTargetStorageAvaliable(StorageType.CUSTOM);
                if (isCurrentAvaliable) {
                    return StorageManager.find(getCustomDataPath());
                } else {
                    if (isAutoChooseAvailable) {
                        final boolean isOutterAvaliable = isTargetStorageAvaliable(StorageType.OUTSIDE);
                        if (isOutterAvaliable) {
                            setCurrentStorageType(StorageType.OUTSIDE);
                            PreferencesConfig.lastChoosedStorageNotValid();
                            return StorageManager.InstanceHolder.STORAGE_MANAGER.getExternalDevice();
                        } else {
                            final boolean isInsideAvaliable = isTargetStorageAvaliable(StorageType.INSIDE);
                            if (isInsideAvaliable) {
                                setCurrentStorageType(StorageType.INSIDE);
                                PreferencesConfig.lastChoosedStorageNotValid();
                                return StorageManager.InstanceHolder.STORAGE_MANAGER.getInternalDevice();
                            } else {
//                            throw new IllegalStateException(String.format("内置卡竟然不可用？出错了吧,收集到的信息为:<<<<>>>>%s", ThrowUtil.getThrowInfo()));
                                return innerFilesStorage();
                            }
                        }
                    } else {
                        return null;
                    }
                }
            case StorageType.INNER:
                //此处即使再设置一遍当前存储类型也不影响，故而不再特殊处理
                return innerFilesStorage();
            case StorageType.OUTSIDE:
            default:
                isCurrentAvaliable = isTargetStorageAvaliable(StorageType.OUTSIDE);
                if (isCurrentAvaliable) {
                    return StorageManager.InstanceHolder.STORAGE_MANAGER.getExternalDevice();
                } else {
                    //外置不可用
                    if (isAutoChooseAvailable) {
                        final boolean isInsideAvaliable = isTargetStorageAvaliable(StorageType.INSIDE);
                        if (isInsideAvaliable) {
                            setCurrentStorageType(StorageType.INSIDE);
                            PreferencesConfig.lastChoosedStorageNotValid();
                            return StorageManager.InstanceHolder.STORAGE_MANAGER.getInternalDevice();
                        } else {
//                            throw new IllegalStateException(String.format("内置卡竟然不可用？出错了吧,收集到的信息为:<<<<>>>>%s", ThrowUtil.getThrowInfo()));
                            return innerFilesStorage();
                        }
                    } else {
                        return null;
                    }
                }
        }
    }

    /**
     * 如果判断内置卡不可用，那么统一返回 /data/data 的，并且将当前存储类型设置为 INNER
     */
    private static LocalStorage innerFilesStorage() {
        setCurrentStorageType(StorageType.INNER);
        return LocalStorage.innerFilesInstance();
    }

    public static String getInnerFilesStoragePath() {
        return LocalStorage.innerFilesInstance().getPath();
    }

    /**
     * 目标存储类型是否可用
     *
     * @return 目标类型存储设备存在、已挂载、路径可读可写且所在文件夹内容不为空（内有文件或子文件夹）
     */
    public static boolean isTargetStorageAvaliable(@StorageTypes int storageType) {
        LocalStorage localStorage = null;

        switch (storageType) {
            case StorageType.OUTSIDE:
                localStorage = StorageManager.InstanceHolder.STORAGE_MANAGER.getExternalDevice();
//                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)){
//                    return false;
//                }
                break;
            case StorageType.INSIDE:
                localStorage = StorageManager.InstanceHolder.STORAGE_MANAGER.getInternalDevice();
//                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)){
//                    return false;
//                }
                break;
            case StorageType.CUSTOM:
                final String customPath = getCustomDataPath();
                if (!TextUtils.isEmpty(customPath)) {
                    localStorage = StorageManager.find(customPath);
                }
                break;
            // ▼ 增加对 //INNER 类型的支持 xiaoyee ▼
            case StorageType.INNER:
                return true;
            // ▲ 增加对 //INNER 类型的支持 xiaoyee ▲
        }

        return localStorage != null                                 //获取到的设备不为空
                && localStorage.isAvaliable()                       //是否已挂载且可读可写
//                && !FileUtils.isDirEmpty(localStorage.getPath())    //设备路径所在文件夹内容不为空,有子项目（文件或子文件夹）
                ;
    }

    /**
     * 获取当前存储类型
     */
    @StorageTypes
    public static int getCurrentStorageType() {
        @StorageTypes
        int result = PreferencesConfig.SDCARD_STATE.get();
        return result;
    }

    /**
     * 设置当前存储类型
     */
    public static void setCurrentStorageType(@StorageTypes int storageType) {
        PreferencesConfig.SDCARD_STATE.set(storageType);
    }

    /**
     * 获取当前自定义存储路径
     */
    public static String getCustomDataPath() {
        return PreferencesConfig.getCustomDataPath();
    }

    /**
     * 设置自定义类型的存储路径
     */
    public static void setCustomDataPath(@NonNull String customDataPath) {
        PreferencesConfig.setCustomDataPath(customDataPath);
    }

    public static String getMapbarCustomDataPath() {
        return FileUtils.fixSlashes(FileUtils.join(getCustomDataPath(), productPath), false);
    }

    /**
     * 清空自定义存储路径
     */
    public static void clearCustomDataPath() {
        setCustomDataPath("");
    }

    /**
     */
    public static boolean hasSettedCustomDataPath() {
        return !TextUtils.isEmpty(getCustomDataPath());
    }

    /**
     * 获取SDCARD下mapbar的路径，一般是/mnt/sdcard/mapbar
     *
     * @return 外置SD卡mapbar路径
     */
    public static String getInternalMapbarPathNoSlash() {
        return FileUtils.join(SdcardUtil.getSdcardPath() + File.separator, productpathNoSlash);
    }

    public static String getExternalMapbarPathNoSlash() {
        return FileUtils.join(SdcardUtil.getSdcard2Path(), productpathNoSlash);
    }

    /**
     * 获取内置存储设备中对本程序可用的产品路径，如：/storage/emulated/0/mapbar_navidog/navidata/
     */
    public static String getInternalMapbarPath() {
        return FileUtils.join(SdcardUtil.getSdcardPath() + File.separator, productPath);
    }

    /**
     * 获取外置存储设备中对本程序可用的产品路径,如: /storage/ext_sd/mapbar_navidog/navidata/ 或 /storage/ext_sd/Android/data/PACKAGE_NAME/files/mapbar_navidog/navidata/
     */
    public static String getExternalMapbarPath() {
        return FileUtils.join(SdcardUtil.getSdcard2Path(), productPath);
    }

    public static String getInternalMapbarOldPath() {
        return FileUtils.join(SdcardUtil.getSdcardPath() + File.separator, oldProductPath);
    }

    public static String getExternalMapbarOldPath() {
        return FileUtils.join(SdcardUtil.getSdcard2Path(), oldProductPath);
    }

    public static String getInternalPoiImagePath() {
        return FileUtils.join(SdcardUtil.getSdcardPath() + File.separator, poiImagePath);
    }

    public static String getExternalPoiImagePath() {
        return FileUtils.join(SdcardUtil.getSdcard2Path(), poiImagePath);
    }

    public static void listenForStorageRefresh() {
        StorageManager.InstanceHolder.STORAGE_MANAGER.setOnStorageListRefreshedListener(new StorageManager.OnStorageListRefreshedListener() {
            @Override
            public void onRefreshed() {
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, "监听到了设备列表刷新");
                }
                checkForwardCompatible(GlobalUtil.getContext());
            }
        });
    }

    private static void checkForwardCompatible(Context context) {
        if (mIsCheckedForwardCompatible) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, "已经检查过 sd 卡模块的向前兼容问题了");
            }
            return;
        }

        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "现在检查 sd 卡模块的向前兼容问题");
        }

//        如果之前选择了" sdcard1 "，那么就通过原逻辑获取到 sdcard1 对应的路径，

        long consumeTime = 0;
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "checkForwardCompatible begin------------");
            consumeTime = System.currentTimeMillis();
        }

        OldLogicCompatible.initInstance(context);
        int storageType = getCurrentStorageType();

        switch (storageType) {
            case StorageType.INSIDE:
                //idea 按照之前逻辑，实际上是选择了 sd 卡 1（虽然现在改为 INSIDE 了，但是存在 sp 中的值仍然是 sdcard 1 的值，故而可以认为是一个；OUTSIDE 同理）
                if (OldLogicCompatible.isExsitsSdcard()) {
                    final String oldPath = OldLogicCompatible.getSdcardPathNoSlash();
                    //old path 与当前 sdcard 1 的路径不同，且 old path 中已有数据，那么优先使用 old path（将 sdcard 1 的路径更改为 old path）
                    //todo 且 old path 中已有数据
                    final LocalStorage sdcard1 = StorageManager.find(oldPath);
                    if (sdcard1 != null) {
                        final String sdcard1Path = sdcard1.getPath();
                        if (!TextUtils.equals(sdcard1Path, oldPath)) {
                            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                                Log.i(LogTag.STORAGE_DEVICE, "当前获取存储路径的逻辑与之前获取存储路径的逻辑所获取到的存储路径不同");
                                Log.i(LogTag.STORAGE_DEVICE, String.format("sdcard 1 oldPath: %s; current path:%s", oldPath, sdcard1Path));
                            }
                            //先将当前路径更改
                            sdcard1.setPath(oldPath);
                            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                                Log.i(LogTag.STORAGE_DEVICE, "现在刷新一下可用列表，查看设置是否生效");
                                StorageManager.logDevices(StorageManager.InstanceHolder.STORAGE_MANAGER.getAvailableDevices());
                            }

                            //再将以后每次刷新的时候也考虑上
                            StorageManager.InstanceHolder.STORAGE_MANAGER.addNeedCorrectPath(sdcard1Path, oldPath);
                        }
                    } else {
                        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                            Log.w(LogTag.STORAGE_DEVICE, String.format("没找到 %s 对应的存储设备", oldPath));
                        }
                    }
                }
                break;
            case StorageType.OUTSIDE:
                if (OldLogicCompatible.isExsitsSdcard2()) {
                    final String oldPath = OldLogicCompatible.getSdcard2PathNoSlash();
                    final LocalStorage sdcard2 = StorageManager.find(oldPath);
                    //old path 与当前 sdcard 2 的路径不同，且 old path 中已有数据，那么优先使用 old path（将 sdcard 2 的路径更改为 old path）

                    if (sdcard2 != null) {
                        final String sdcard2Path = sdcard2.getPath();
                        if (!TextUtils.equals(sdcard2Path, oldPath)) {
                            //todo 且 old path 中已有数据
                            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                                Log.i(LogTag.STORAGE_DEVICE, "当前获取存储路径的逻辑与之前获取存储路径的逻辑所获取到的存储路径不同");
                                Log.i(LogTag.STORAGE_DEVICE, String.format("sdcard 2 oldPath: %s; current path:%s", oldPath, sdcard2Path));
                            }
                            sdcard2.setPath(oldPath);
                            StorageManager.InstanceHolder.STORAGE_MANAGER.addNeedCorrectPath(sdcard2Path, oldPath);
                        }
                    } else {
                        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                            Log.w(LogTag.STORAGE_DEVICE, String.format("没找到 %s 对应的存储设备", oldPath));
                        }
                    }
                }
                break;
            case StorageType.CUSTOM:
            default:
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, String.format("检查版本向前兼容时，未对此类型%s做处理（认为此类型在之前版本是没有的）", storageType));
                }
                //do nothing，because there was no custom type before
                break;
        }

        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            consumeTime = System.currentTimeMillis() - consumeTime;
            Log.i(LogTag.STORAGE_DEVICE, String.format("checkForwardCompatible end 耗时 %s 毫秒", consumeTime));
        }

        mIsCheckedForwardCompatible = true;
    }


    @Retention(RetentionPolicy.SOURCE)
    @IntDef({StorageType.CUSTOM, StorageType.OUTSIDE, StorageType.INSIDE, StorageType.INNER})
    private @interface StorageTypes {
    }

    /**
     * 存储类型
     */
    public static class StorageType {
        /**
         * /data/data/package_name/files
         */
        public static final int INNER = 4;
        /**
         * 内置存储卡
         */
        public static final int INSIDE = 0;
        /**
         * 外置存储卡
         */
        public static final int OUTSIDE = 1;
        /**
         * 自定义存储路径
         */
        public static final int CUSTOM = 2;
    }

}
