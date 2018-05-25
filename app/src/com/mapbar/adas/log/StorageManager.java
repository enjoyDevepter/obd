package com.mapbar.adas.log;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.SparseArray;

import com.mapbar.adas.GlobalUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.mapbar.adas.log.IStorageDevice.Type;
import static com.mapbar.adas.log.IStorageDevice.Type.INTERNAL;
import static com.mapbar.adas.log.IStorageDevice.Type.USB;

/**
 * Created by xiaoyee on 11/16/16.
 * 存储设备管理类
 */

public class StorageManager {
    private static final List<OnStorageStateChangedListener> mStateChangedListeners = new ArrayList<>();
    private static final Object STORAGE_STATE_CHANGE_LISTENER_SYNC_OBJ = new Object();
    private static Bundle mDevices;
    private static LocalStorage mPrimary;
    private static Pattern ENVIRONMENT_PATTERN = Pattern.compile("EXTERNAL_STORAGE([0-9]?)=([^\\s]+)");
    //包含 ext 或者 sdcard[0-9] 或者 removable 三者任一的字符串,大小写不敏感
    private static Pattern EXTERNAL_PATTERN = Pattern.compile("^.*(?i)(ext|sdcard[0-9]|removable).*");
    private static Pattern USB_PATH_PATTERN = Pattern.compile("^.*(?i)usb.*");
    /**
     * 需要更正的路径
     */
    private static SparseArray<String> mRectifiedPath;
    /**
     * 仅仅针对 sdk 大于等于 KITKAT（19） 的缓存
     */
    private static SparseArray<String> mValidPathCache;
    private final StorageStateChangedReceiver mStateChangedReceiver = new StorageStateChangedReceiver();
    private OnStorageListRefreshedListener mOnStorageListRefreshedListener;


    private StorageManager() {
        checkCache();
    }

    private static void checkCache() {
        if (mValidPathCache == null) {
            mValidPathCache = new SparseArray<>();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static String checkValidExternalPath(String path, Context context) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                final String logInfo = String.format("Build.VERSION.SDK_INT %s < Build.VERSION_CODES.KITKAT %s", Build.VERSION.SDK_INT, Build.VERSION_CODES.KITKAT);
                Log.i(LogTag.STORAGE_DEVICE, logInfo);
            }
            return path;
        }

        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, String.format("Build.VERSION.SDK_INT %s >= Build.VERSION_CODES.KITKAT %s", Build.VERSION.SDK_INT, Build.VERSION_CODES.KITKAT));
        }

        checkCache();
        final int hashCodeOfPath = path.hashCode();
        final int indexOfThis = mValidPathCache.indexOfKey(hashCodeOfPath);

        //缓存中已有
        if (indexOfThis >= 0) {
            final String validPath = mValidPathCache.valueAt(indexOfThis);
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, "击中缓存中的 valid path >>>" + validPath);
            }
            return validPath;
        }
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, String.format("缓存中没有 %s 对应的 valid path,现在走逻辑，计算一下 hash code: %s", path, hashCodeOfPath));
        }

        File file = new File(path, "temp" + System.currentTimeMillis() + ".txt");
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, String.format("测试文件为: %s", file.getAbsolutePath()));
        }

        try {
            if (file.createNewFile()) {
                //noinspection ResultOfMethodCallIgnored
                file.delete();

                mValidPathCache.put(hashCodeOfPath, path);
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, String.format("此路径可用 %s", path));
                }

                return path;
            }
        } catch (IOException e) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                Log.w(LogTag.STORAGE_DEVICE, String.format("文件不能正常创建，意味着此路径：%s 是不可用的,原因为:\r\n\t\t%s", path, e.getCause()));
            }
        }

        //1、查看可用的外置存储路径
        File[] externalFilesDirs = context.getExternalFilesDirs(null);

        android.util.Log.d("storageManagersss", String.format("通过 getExternalFilesDirs 的方式获取到的外置存储卡路径 :%s", externalFilesDirs[0]));

        for (File dir : externalFilesDirs) {
            if (dir != null && dir.getAbsolutePath().contains(path)) {
                final String validPath = dir.getAbsolutePath();
                mValidPathCache.put(hashCodeOfPath, validPath);
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, String.format("path: %s 对应的 valid path 为： %s ;hash code 为:%s", path, validPath, hashCodeOfPath));
                }
                return validPath;
            }
        }

        //2、直接使用带 "/Android/data/" + context.getPackageName() + "/files" 的形式获取
        final String pathWithPackage = FileUtils.join(path, "/Android/data/" + context.getPackageName() + "/files");
        if (FileUtils.canWriteFile(pathWithPackage)) {
            android.util.Log.d("storageManagersss", "通过 pathWithPackage 的方式获取到了外置存储卡路径");
            return pathWithPackage;
        }

//        File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(context, null);
        return path;
    }

    public static void logDevices(List<LocalStorage> devices) {
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            StringBuilder logInfo = new StringBuilder("所有可用设备如下：\r\n");
            if (devices == null || devices.size() == 0) {
                logInfo.append("无可用设备");
            } else {
                for (LocalStorage device : devices) {
                    logInfo.append(">>>");
                    logInfo.append(device);
                }
            }

            Log.i(LogTag.STORAGE_DEVICE, logInfo.toString());
        }
    }

    public static void logAvaliableDevices() {
        logDevices(mDevices.mAvailableDevices);
    }

    /**
     * 通过 API 的方式获取到内置、外置存储路径
     */
    private static void logSysGetDevices() {
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            StringBuilder infoToLog = new StringBuilder(" 通过 API 的方式获取到内置、外置存储路径\r\n");
            File externalStorageDir = Environment.getExternalStorageDirectory();
            infoToLog.append("externalStorageDir>>>");
            infoToLog.append(externalStorageDir.getAbsolutePath());
            infoToLog.append("\r\n");
//            externalStorageDir>>>/storage/emulated/0

            File filesDir = GlobalUtil.getContext().getFilesDir();
            infoToLog.append("filesDir>>>");
            infoToLog.append(filesDir.getAbsolutePath());
            //filesDir>>>/data/user/0/cn.com.tiros.android.navidog/files

            File[] externalFilesDirs = ContextCompat.getExternalFilesDirs(GlobalUtil.getContext(), null);
            infoToLog.append("externalFilesDirs>>>");
            for (File file : externalFilesDirs) {
                infoToLog.append("\tfile>>>");
                infoToLog.append(file.getAbsolutePath());
//                externalFilesDirs>>>	file>>>/storage/emulated/0/Android/data/cn.com.tiros.android.navidog/files
            }
            Log.i(LogTag.STORAGE_DEVICE, infoToLog.toString());
        }
    }

    /**
     * data/data/package_name/files
     */
    public static String getInnerFilesPath(Context context) {
        return context.getApplicationContext().getFilesDir().getAbsolutePath();
    }

    private static void checkDevices() {
        if (mDevices == null) {
            InstanceHolder.STORAGE_MANAGER.refreshStorage();
        }
    }

    private synchronized static void refresh(List<MountPoint> list) {
        Bundle bundle = new Bundle();
        //先尝试利用反射获取"设备列表"
        if (!getFromMountService(bundle, list)) {
            //一旦使用反射方式失败，那么就通过 /proc/pid/environ 与 /proc/mounts 文件的对比找出可用设备
            getFromEnvironment(bundle, list);
        }

        android.util.Log.w("storageManagersss", "storage manager 调用了 refresh 方法，并给 bundle 赋值了");
        mDevices = bundle;
        mPrimary = find(Environment.getExternalStorageDirectory().getAbsolutePath());
        swapIfNeeded(bundle);
        fillSynonyms(bundle, mPrimary);

        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            for (LocalStorage availableDevice : mDevices.mAvailableDevices) {
                Log.i(LogTag.STORAGE_DEVICE, String.format("可用设备:%s", availableDevice.toString()));
            }
        }
    }

    /**
     * 利用反射的形式，获取 volume list
     *
     * @param list 挂载点列表，作用是如果反射拿不到此设备的连接状态，那么从挂载点列表中拿
     * @return 成功使用反射 return true；否则返回 false
     */
    private static boolean getFromMountService(Bundle bundle, List<MountPoint> list) {
        try {
            Object[] objArr;
            IBinder iBinder = (IBinder) Reflection.callStatic("android.os.ServiceManager", "getService", new Object[]{"mount"});
            Object callStatic = Reflection.callStatic(
                    "android.os.storage.IMountService$Stub",
                    "asInterface",
                    new Class[]{IBinder.class}, new Object[]{iBinder}
            );
            if (GlobalUtil.isM()) {
                int unixUID = GlobalUtil.getUnixUID();
                Class[] clsArr = new Class[]{Integer.TYPE, String.class, Integer.TYPE};
                objArr = (Object[]) Reflection.call(
                        callStatic,
                        "getVolumeList",
                        clsArr,
                        unixUID,
                        GlobalUtil.getResPackageName(),
                        0
                );
            } else {
                objArr = (Object[]) Reflection.call(callStatic, "getVolumeList");
            }
            int i = -1;
            int length = objArr.length;
            int i2 = 0;
            while (i2 < length) {
                String detectStorageState;
                Type type;
                int i3;
                Object obj = objArr[i2];

                String storagePath = (String) Reflection.call(obj, "getPath");
                String storageState = (String) Reflection.call(obj, "getState");
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, String.format("storage path : %s, storage state: %s", storagePath, storageState));
                }
                if (storageState == null) {
                    detectStorageState = detectStorageState(storagePath, list);
                } else {
                    detectStorageState = storageState;
                }
                Integer num = (Integer) Reflection.call(obj, "getDescriptionId");
                int i4;
                if (!(Boolean) Reflection.call(obj, "isRemovable")) {
                    i4 = i;
                    type = INTERNAL;
                    i3 = i4;
                } else if (USB_PATH_PATTERN.matcher(storagePath).matches()) {
                    i3 = i + 1;
                    type = USB;
                } else {
                    i4 = i;
                    type = Type.EXTERNAL;
                    i3 = i4;
                }
                //todo 需要将设备名完善
//                通过类型初始化设备名
//                if (num == null || num == 0) {
//                    switch (LocalStorage.C09002.INTS[type.ordinal()]) {
//                        case 1 /*1*/:
//                            num = Integer.valueOf(2131165537);
//                            break;
//                        case 2 /*2*/:
//                            num = Integer.valueOf(2131165757);
//                            break;
//                        case 3 /*3*/:
//                            num = Integer.valueOf(2131165931);
//                            break;
//                    }
//                }
//                storageState = "name";
//                if (!FileUtils.isWRable(storagePath) || FileUtils.isDirEmpty(storagePath)) {
//                    detectStorageState = Environment.MEDIA_UNMOUNTED;
//                }
                if (Environment.MEDIA_MOUNTED.equals(detectStorageState)) {
                    addDevice(bundle, storagePath, type, detectStorageState, "name");
                } else {
                    addRemovedDevice(bundle, storagePath, type, detectStorageState, "name");
                }
                i2++;
                i = i3;
            }
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void getFromEnvironment(Bundle bundle, List<MountPoint> list) {
        List<String> pathsFromEnvironmentVariables = getPathsFromEnvironmentVariables();

        String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath();

        if (pathsFromEnvironmentVariables.contains(absolutePath)) {
            pathsFromEnvironmentVariables.remove(absolutePath);
        }
        boolean isExternalStorageRemovable = Environment.isExternalStorageRemovable();
//        Log.d("StorageManagerlog", String.format("======-----||||||现在增加%s类型, \r\n\t path: %s", isExternalStorageRemovable? "external" : "internal", absolutePath));
        addDevice(
                bundle,
                absolutePath,
                isExternalStorageRemovable ? Type.EXTERNAL : INTERNAL, Environment.getExternalStorageState(),
                isExternalStorageRemovable ? "external" : "internal"
        );
        int i = 0;
        for (MountPoint path : list) {
            String mountPointPath = path.getPath();

            if (isContainTargetStr(pathsFromEnvironmentVariables, mountPointPath)) {
                if (EXTERNAL_PATTERN.matcher(mountPointPath).matches()) {
                    addDevice(bundle, mountPointPath, Type.EXTERNAL, "mounted", "external");
                } else if (USB_PATH_PATTERN.matcher(mountPointPath).matches()) {
                    int tempField = i + 1;
                    addDevice(bundle, mountPointPath, USB, "mounted", String.format("usb%s", i));
                    i = tempField;
                } else {
                    if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                        Log.w(LogTag.STORAGE_DEVICE, "未知类型设备 >>> " + mountPointPath);
                    }
                }
                pathsFromEnvironmentVariables.remove(mountPointPath);
            }
        }

        for (String str : pathsFromEnvironmentVariables) {
            if (EXTERNAL_PATTERN.matcher(str).matches()) {
                addRemovedDevice(bundle, str, Type.EXTERNAL, "removed", "removed_external");
            } else if (USB_PATH_PATTERN.matcher(str).matches()) {
//                Object[] objArr2 = new Object[1];
                int i3 = i + 1;
//                objArr2[0] = i;
                addRemovedDevice(bundle, str, USB, "removed", String.format("removed_usb%s", i));
                i = i3;
            } else {
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.WARN)) {
                    Log.w(LogTag.STORAGE_DEVICE, "未知类型设备 >>> " + str);
                }
            }
        }
    }

    private static boolean isContainTargetStr(List<String> strSourceList, String targetStr) {
        for (String str : strSourceList) {
            if (str.contains(targetStr)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检测对应卷的挂载状态，是已经挂载还是已经移除了
     *
     * @return 如果挂载点集合中有此卷的路径，那么就说明此卷已挂载；否则就是移除掉了
     */
    private static String detectStorageState(String volumePath, List<MountPoint> list) {
        for (MountPoint path : list) {
            if (volumePath.equals(path.getPath())) {
                return "mounted";
            }
        }
        return "removed";
    }

    private static List<String> getPathsFromEnvironmentVariables() {

        final int myPid = Process.myPid();
        String format = String.format("/proc/%s/environ", myPid);
        List<String> arrayList = new ArrayList<>();
        try {
            format = FileUtils.readFile(format);

            Matcher matcher = ENVIRONMENT_PATTERN.matcher(format);
            while (matcher.find()) {
                String group = matcher.group();

                if (!arrayList.contains(group)) {
                    arrayList.add(group);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    /**
     * 查找 targetPath 对应的存储设备
     *
     * @return 如果未找到对应的路径，那么会返回 null
     */
    @Nullable
    public static LocalStorage find(String targetPath) {
        if (TextUtils.isEmpty(targetPath)) {
            return null;
        }
        checkDevices();
//        logDevices(mDevices.mAvailableDevices);
        int minPathLength = Integer.MAX_VALUE;
        String finalPath = null;
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "-----------------");
        }
        for (String storagePath : mDevices.mStorageDeviceMap.keySet()) {
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, String.format("storage path: %s", storagePath));
            }

            int storagePathLength = storagePath.length();
            //以 storagePath 开头
            final boolean startsWithStoragePath = targetPath.startsWith(storagePath);
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                Log.i(LogTag.STORAGE_DEVICE, String.format("%s start with %s ? %s", targetPath, storagePath, startsWithStoragePath));
            }
            if (startsWithStoragePath) {
                //且此 storagePath 的长度小于上一个的长度
                if (minPathLength > storagePathLength) {
                    finalPath = storagePath;
                    minPathLength = storagePathLength;
                }
            }
        }

        if (finalPath != null) {
            return mDevices.mStorageDeviceMap.get(finalPath);
        }
        return null;
    }

    private static List<MountPoint> readMountPoints() {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/mounts"));

            List<MountPoint> arrayList = new ArrayList<>();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine == null) {
                    return arrayList;
                }
                MountPoint parse = MountPoint.parse(readLine);
                if (parse != null) {
                    arrayList.add(parse);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static void swapIfNeeded(Bundle bundle) {
        if (bundle.mAvailableDevices.size() > 1 && mPrimary.getType() == Type.EXTERNAL) {
            for (LocalStorage localStorage : bundle.mAvailableDevices) {
                if (localStorage.getType() == INTERNAL) {
                    localStorage.setType(Type.EXTERNAL);
                }
            }
            mPrimary.setType(INTERNAL);
        }
    }

    @SuppressLint("SdCardPath")
    private static void fillSynonyms(Bundle bundle, LocalStorage localStorage) {
        if (localStorage != null) {
            bundle.mStorageDeviceMap.put("/sdcard", localStorage.variant("/sdcard"));
            bundle.mStorageDeviceMap.put("/mnt/sdcard", localStorage.variant("/mnt/sdcard"));
            if (Build.VERSION.SDK_INT >= 16) {
                bundle.mStorageDeviceMap.put("/storage/sdcard0", localStorage.variant("/storage/sdcard0"));
            }
            if (Build.VERSION.SDK_INT >= 17) {
                bundle.mStorageDeviceMap.put("/storage/emulated/legacy", localStorage.variant("/storage/emulated/legacy"));
            }
        }
    }

    private static void addDevice(Bundle bundle, String path, Type type, String state, String name) {
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            final String infoToLog = String.format("add device， path:%s\r\n type: %s\r\n state:%s", path, type, state);
            Log.i(LogTag.STORAGE_DEVICE, infoToLog);
        }
        if (mRectifiedPath != null) {
            final int index = mRectifiedPath.indexOfKey(path.hashCode());
            if (index > 0) {
                final String rectifiedPath = mRectifiedPath.valueAt(index);
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, String.format("此路径：%s 是需要更正的,更正后为%s", path, rectifiedPath));
                }
                path = rectifiedPath;
            }
        }

        LocalStorage localStorage = new LocalStorage(path, type, state, name);
        bundle.mAvailableDevices.add(localStorage);
        bundle.mStorageDeviceMap.put(path, localStorage);
    }

    private static void addRemovedDevice(Bundle bundle, String path, Type type, String state, String name) {
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            final String infoToLog = String.format("add removed device， path:%s\r\n type: %s\r\n state:%s", path, type, state);
            Log.i(LogTag.STORAGE_DEVICE, infoToLog);
        }
        LocalStorage localStorage = new LocalStorage(path, type, state, name);
        bundle.mRemovedDevices.add(localStorage);
        bundle.mStorageDeviceMap.put(path, localStorage);
    }

    private static IntentFilter getStorageDeviceStateFilter() {
        IntentFilter intentFilter = new IntentFilter();
        //移除
        intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
        //未挂载
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        //正在进行磁盘检查
        intentFilter.addAction(Intent.ACTION_MEDIA_CHECKING);
        //
        intentFilter.addAction(Intent.ACTION_MEDIA_NOFS);
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
        intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTABLE);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intentFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
        intentFilter.addDataScheme("file");
        return intentFilter;
    }

    /**
     * 添加需要更正的路径
     *
     * @param oldPath       需要更正的路径
     * @param correctedPath 更正后的路径
     */
    public void addNeedCorrectPath(@NonNull String oldPath, @NonNull String correctedPath) {
        if (mRectifiedPath == null) {
            mRectifiedPath = new SparseArray<>();
        }
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, String.format("现在增加需要更正的路径>>>更正前:%s, 更正后:%s", oldPath, correctedPath));
        }
        mRectifiedPath.put(oldPath.hashCode(), correctedPath);
    }

    public void setOnStorageListRefreshedListener(OnStorageListRefreshedListener listener) {
        this.mOnStorageListRefreshedListener = listener;
    }

    /**
     * 刷新存储设备列表
     * <p>
     * //NOTE：相对耗时，大约几十毫秒
     * </P>
     */
    public void refreshStorage() {
        long timeSpend = 0;
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "----------------- begin -----------------");
            timeSpend = System.currentTimeMillis();
            Log.i(LogTag.STORAGE_DEVICE, "call method refreshStorage begin");
        }
        final List<MountPoint> mountPoints;
        try {
            mountPoints = readMountPoints();
            logAllMountPoints(mountPoints);
            refresh(mountPoints);
            if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                logDevices(mDevices.mAvailableDevices);
                logSysGetDevices();
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.w("storageManagersss", String.format("msg: %s, cause: %s", e.getMessage(), e.getCause()));
        }
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "call method refreshStorage end");
            timeSpend = System.currentTimeMillis() - timeSpend;
            Log.i(LogTag.STORAGE_DEVICE, String.format("----------------- end -----------------耗时：%s 毫秒", timeSpend));
        }

        if (mOnStorageListRefreshedListener != null) {
            mOnStorageListRefreshedListener.onRefreshed();
        }
    }

    private void logAllMountPoints(List<MountPoint> mountPoints) {
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "logAllMountPoints  列出所有挂载点 start ------------");
        }
        if (mountPoints != null) {
            for (MountPoint mountPoint : mountPoints) {
                if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
                    Log.i(LogTag.STORAGE_DEVICE, mountPoint.toString());
                }
            }
        }
        if (Log.isLoggable(LogTag.STORAGE_DEVICE, Log.INFO)) {
            Log.i(LogTag.STORAGE_DEVICE, "logAllMountPoints  列出所有挂载点 end ++++++++++++++");
        }
    }

    /**
     * 获取内存设备
     * <br><b>NOTE:一定注意对"不存在内置存储卡时的逻辑处理"</b>
     *
     * @return 如果不存在内置存储卡，那么直接返回 null
     */
    @Nullable
    public LocalStorage getInternalDevice() {
        checkDevices();
        final List<LocalStorage> devices = mDevices.mAvailableDevices;
        if (devices != null) {
            for (LocalStorage device : devices) {
                if (device.getType() == INTERNAL) {
                    return device;
                }
            }
        }

        return null;
    }

    /**
     * 获取内置存储设备的挂载状态
     *
     * @return {@link Environment#MEDIA_MOUNTED} or {@link Environment#MEDIA_UNMOUNTED}
     */
    public String getInternalDeviceState() {
        if (isInternalExist()) {
            assert getInternalDevice() != null : "isInternalExist return true， but getInternalDevice == null, wrong";
            if (getInternalDevice().isMounted()) {
                return Environment.MEDIA_MOUNTED;
            }
        }
        return Environment.MEDIA_UNMOUNTED;
    }

    /**
     * 获取内置存储设备的路径
     */
    @Nullable
    public String getInternalDevicePath() {
        final LocalStorage localStorage = getInternalDevice();
        if (localStorage != null) {
            return localStorage.getPath();
        } else {
            return null;
        }
    }

    /**
     * 内置存储设备是否存在
     */
    public boolean isInternalExist() {
        final LocalStorage localStorage = getInternalDevice();
        return localStorage != null && localStorage.isAvaliable();
    }

    /**
     * 获取外置存储设备，一般为外置 SD 卡
     * <br><b>NOTE:一定注意对"不存在外置 SD 卡时的逻辑处理"</b>
     *
     * @return 如果不存在外置存储卡，那么返回 null；
     */
    @Nullable
    public LocalStorage getExternalDevice() {
        checkDevices();
        final List<LocalStorage> devices = mDevices.mAvailableDevices;
        if (devices != null) {
            for (LocalStorage device : devices) {
                if (device.getType() == Type.EXTERNAL) {
                    return device;
                }
            }
        }

        return null;
    }

    /**
     * 获取外置存储设备的挂载状态
     *
     * @return {@link Environment#MEDIA_MOUNTED} or {@link Environment#MEDIA_UNMOUNTED}
     */
    public String getExternalDeviceState() {
        if (isExternalExist()) {
            assert getExternalDevice() != null : "isExternalExist return true， but getExternalDevice == null, wrong";
            if (getExternalDevice().isMounted()) {
                return Environment.MEDIA_MOUNTED;
            }
        }
        return Environment.MEDIA_UNMOUNTED;
    }

    /**
     * 获取外置存储设备的路径
     */
    @Nullable
    public String getExternalDevicePath() {
        final LocalStorage localStorage = getExternalDevice();
        if (localStorage != null) {
            return localStorage.getPath();
        } else {
            return null;
        }
    }

    /**
     * 是否存在外置存储设备
     */
    public boolean isExternalExist() {
        final LocalStorage localStorage = getExternalDevice();
        return localStorage != null && localStorage.isAvaliable();
    }

    /**
     * 判断给定的两个路径是否属于一同个挂载点
     */
    public boolean inSameMountPoint(@NonNull String path, @NonNull String anotherPath) {
        checkDevices();
        LocalStorage storage = find(path);
        LocalStorage anotherStorage = find(anotherPath);

        if ((storage != null || anotherStorage != null) && (storage == null || anotherStorage == null || !storage.getPath().equals(anotherStorage.getPath()))) {
            return false;
        }
        return true;
    }

    /**
     * 获取所有可用设备
     */
    public List<LocalStorage> getAvailableDevices() {
        checkDevices();
        return mDevices.mAvailableDevices;
    }

    private void onStateChanged(Intent intent) {
        //需要将 intent 解析成相关信息

        //step 1、先刷新可用设备
        refreshStorage();

        if (intent == null) {
            return;
        }

        //step 2、通知
        synchronized (STORAGE_STATE_CHANGE_LISTENER_SYNC_OBJ) {
            for (OnStorageStateChangedListener changedListener : mStateChangedListeners) {
                changedListener.onAction(intent.getAction());
            }

        }
    }

    /**
     * 增加设备状态改变的监听
     */
    public void addStorageStateChangeListener(@NonNull OnStorageStateChangedListener listener) {
        synchronized (STORAGE_STATE_CHANGE_LISTENER_SYNC_OBJ) {
            mStateChangedListeners.add(listener);
        }
    }

    /**
     * 移除设备状态改变的监听
     */
    public void removeStorageStateChangedListener(@NonNull OnStorageStateChangedListener listener) {
        synchronized (STORAGE_STATE_CHANGE_LISTENER_SYNC_OBJ) {
            if (mStateChangedListeners.contains(listener)) {
                mStateChangedListeners.remove(listener);
            }
        }
    }

    /**
     * 开始监测设备状态的改变
     */
    public void startWatchStorageStateChange(@NonNull Context context) {
        context.registerReceiver(mStateChangedReceiver, getStorageDeviceStateFilter());
    }

    /**
     * 停止监测设备状态的改变
     */
    public void stopWatchStorageStateChage(@NonNull Context context) {
        context.unregisterReceiver(mStateChangedReceiver);
    }

    /**
     * 设备状态改变监听器
     */
    public interface OnStorageStateChangedListener {
        void onAction(String action);
    }

    /**
     * 存储设备列表刷新完成监听器
     */
    public interface OnStorageListRefreshedListener {
        void onRefreshed();
    }

    static class Bundle {
        List<LocalStorage> mAvailableDevices;
        List<LocalStorage> mRemovedDevices;
        ConcurrentHashMap<String, LocalStorage> mStorageDeviceMap;

        Bundle() {
            this.mAvailableDevices = new ArrayList<>();
            this.mRemovedDevices = new ArrayList<>();
            this.mStorageDeviceMap = new ConcurrentHashMap<>();
        }
    }

    public static final class InstanceHolder {
        public static final StorageManager STORAGE_MANAGER = new StorageManager();
    }

    /**
     * 存储设备状态更改的接收器
     */
    private class StorageStateChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            onStateChanged(intent);
        }
    }

}