package com.mapbar.adas.log;


import android.os.Environment;
import android.text.TextUtils;

import com.mapbar.adas.GlobalUtil;

import java.io.File;

/**
 * Created by xiaoyee on 11/16/16.
 * 本地存储设备类型,如内置卡、 SD 卡、/data/data/package_name/files
 */

public class LocalStorage implements IStorageDevice {

    private static final String INNER_FILES_NAME = "inner_filesinner_files";
    //idea 因为一旦使用此存储类型，那么会经常使用，创建此实例是为了避免频繁创建新对象
    private static LocalStorage INNER_FILES_STORAGE;
    /**
     * 是否可写
     */
    private boolean mCanRead;
    /**
     * 是否可读
     */
    private boolean mCanWrite;
    /**
     * 设备名
     */
    private String mName;
    /**
     * 存储设备路径
     */
    private String mPath;
    /**
     * 挂载状态
     *
     * @see android.os.Environment#MEDIA_MOUNTED
     * @see android.os.Environment#MEDIA_UNMOUNTED
     */
    private String mState;
    /**
     * 存储设备类型,目前只有内置存储路径和外置存储路径两种
     *
     * @see com.mapbar.adas.log.IStorageDevice.Type#INTERNAL
     * @see com.mapbar.adas.log.IStorageDevice.Type#EXTERNAL
     * @see com.mapbar.adas.log.IStorageDevice.Type#INNER
     */
    private Type mType;

    public LocalStorage(String path, Type type, String state, String name) {
        this.mCanRead = true;
//        this.mPath = path;
        this.mType = type;
        this.mName = name;
        this.mState = state;
        //如果是外置存储卡，且已挂载，那么判断一下是否路径可用
        if (mType == Type.EXTERNAL && state.equals(Environment.MEDIA_MOUNTED)) {
            mPath = StorageManager.checkValidExternalPath(path, GlobalUtil.getContext());
        } else {
            mPath = path;
        }

        initStorage();
    }

    public static LocalStorage innerFilesInstance() {
        if (INNER_FILES_STORAGE == null) {
            INNER_FILES_STORAGE = new LocalStorage(StorageManager.getInnerFilesPath(GlobalUtil.getContext()), Type.INNER, "mounted", INNER_FILES_NAME);
        }
        return INNER_FILES_STORAGE;
    }

    private void initStorage() {
        switch (mType) {
            case INTERNAL:
                this.mCanWrite = true;
                break;
            case INNER:
                this.mCanRead = true;
                this.mCanWrite = true;
                break;
            case EXTERNAL:
            case USB:
            default:
                this.mCanWrite = checkWriteAccess();
                this.mCanRead = new File(this.mPath).canRead();
                break;
        }
    }

    @Override
    public String getPath() {
        return this.mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    @Override
    public Quota getQuota() {
        if (mName != null && TextUtils.equals(mName, INNER_FILES_NAME)) {
            return Quota.getQuotaForPath("/data");
        }
        return Quota.getQuotaForPath(this.mPath);
    }

    @Override
    public Type getType() {
        return this.mType;
    }

    void setType(Type type) {
        this.mType = type;
        initStorage();
    }

    private boolean checkWriteAccess() {
        if (!isMounted()) {
            return false;
        }
        if (!GlobalUtil.isKitKat()) {
            return true;
        }
        File file = new File(this.mPath, String.valueOf(System.nanoTime()));
        while (file.exists()) {
            file = new File(this.mPath, String.valueOf(System.nanoTime()));
        }
        if (!file.mkdir()) {
            return false;
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
        return true;
    }

    void setName(String str) {
        this.mName = str;
    }

    public boolean canWrite() {
        return this.mCanWrite;
    }

    public boolean canRead() {
        return this.mCanRead;
    }

    public LocalStorage variant(String str) {
        return new StorageVariant(this, str);
    }

    public LocalStorage getReal() {
        return this;
    }

    /**
     * 是否挂载了
     */
    public boolean isMounted() {
        return this.mState != null && this.mState.toLowerCase().equals("mounted");
    }

    /**
     * 设备是否已挂载且可读可写
     */
    public boolean isAvaliable() {
        return isMounted() && mCanRead && mCanWrite;
    }

    public boolean isAccessibleFrom(LocalStorage localStorage) {
        return this.mPath.startsWith(localStorage.mPath);
    }

    public boolean exists() {
        return new File(this.mPath).exists();
    }

    public String toString() {
        return String.format("name:%s\r\npath: [%s]\r\ntype:[%s]\r\nstate:[%s]", this.mName, this.mPath, this.mType, this.mState);
    }

    public class StorageVariant extends LocalStorage {
        private LocalStorage mParent;

        public StorageVariant(LocalStorage localStorage, String str) {
            super(str, localStorage.getType(), localStorage.mState, localStorage.mName);
            this.mParent = localStorage;
        }

        public LocalStorage getReal() {
            return this.mParent;
        }

        public LocalStorage getParent() {
            return this.mParent;
        }

    }
}
