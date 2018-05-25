package com.mapbar.adas.log;

/**
 * Created by xiaoyee on 11/16/16.
 * 所有存储设备类型的公共接口
 */

public interface IStorageDevice {
    /**
     * 获取设备路径
     */
    String getPath();

    /**
     * 获取设备配额;是否可用,总容量,剩余容量
     */
    Quota getQuota();

    /**
     * 获取设备类型
     */
    Type getType();

    /**
     * 存储设备类型
     */
    enum Type {
        INNER,
        INTERNAL,
        EXTERNAL,
        USB,
        ROOT,
        REMOTE
    }
}
