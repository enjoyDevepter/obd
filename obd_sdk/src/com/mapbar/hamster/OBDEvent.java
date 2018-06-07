package com.mapbar.hamster;

/**
 * Created by guomin on 2018/3/7.
 */

public class OBDEvent {
    /**
     * 开始扫描设备<br>
     */
    public static final int BLUE_SCAN_STATRED = 0;
    /**
     * 扫描设备结束<br>
     */
    public static final int BLUE_SCAN_FINISHED = 1;
    /**
     * 扫描失败<br>
     */
    public static final int BLUE_SCAN_FAILED = 2;
    /**
     * 正在连接蓝牙设备<br>
     */
    public static final int BLUE_CONNECTING = 3;
    /**
     * 蓝牙设备连接成功<br>
     */
    public static final int BLUE_CONNECTED = 4;
    /**
     * 蓝牙设备正在断开连接<br>
     */
    public static final int BLUE_DISCONNECTING = 5;
    /**
     * 蓝牙设备断开连接<br>
     */
    public static final int BLUE_DISCONNECTED = 6;
    /**
     * OBD首次使用
     */
    public static final int OBD_FIRST_USE = 7;
    /**
     * OBD正常使用
     */
    public static final int OBD_NORMAL = 8;
    /**
     * OBD过期
     */
    public static final int OBD_EXPIRE = 9;
    /**
     * 开始升级
     */
    public static final int OBD_BEGIN_UPDATE = 10;
    /**
     * 完成1k数据传递
     */
    public static final int OBD_UPDATE_FINISH_UNIT = 11;
    /**
     * OBD盒子版本
     */
    public static final int OBD_GET_VERSION = 12;

    /**
     * OBD 授权结果
     */
    public static final int OBD_AUTH_RESULT = 13;

    /**
     * OBD 参数更新成功
     */
    public static final int OBD_UPDATE_PARAMS_SUCCESS = 14;

    /**
     * 胎压状态更新
     */
    public static final int OBD_UPPATE_TIRE_PRESSURE_STATUS = 15;
    /**
     * OBD 异常
     */
    public static final int OBD_ERROR = 16;

    /**
     * OBD 胎压开始学习
     */
    public static final int OBD_STUDY = 17;

    /**
     * OBD 胎压学习进度
     */
    public static final int OBD_STUDY_PROGRESS = 18;
}
