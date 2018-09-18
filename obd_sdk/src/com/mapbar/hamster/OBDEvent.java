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
    public static final int OBD_DISCONNECTED = 6;
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


    /**
     * 未注册
     */
    public static final int UNREGISTERED = 100; //未注册

    public static final int STATUS_UPDATA = 101; // 状态上传
    /**
     * 未授权或者授权过期
     */
    public static final int AUTHORIZATION = 110; //未授权或者授权过期
    public static final int AUTHORIZATION_SUCCESS = 111; //未授权或者授权过期
    public static final int AUTHORIZATION_FAIL = 112; //未授权或者授权过期

    /**
     * 无车型参数
     */
    public static final int NO_PARAM = 120; // 无车型参数
    public static final int PARAM_UPDATE_SUCCESS = 121; // 无车型参数
    public static final int PARAM_UPDATE_FAIL = 122; // 无车型参数
    /**
     * 当前胎压不匹配
     */
    public static final int CURRENT_MISMATCHING = 130; // 当前胎压不匹配
    /**
     * 之前胎压匹配成功过
     */
    public static final int BEFORE_MATCHING = 140; // 之前胎压匹配过
    /**
     * 未校准
     */
    public static final int UN_ADJUST = 150; // 未完成校准
    public static final int ADJUSTING = 151; // 校准中
    public static final int ADJUST_SUCCESS = 152; // 校准完成
    /**
     * BoxId不合法，盗版
     */
    public static final int UN_LEGALITY = 160; // BoxId 不合法
    /**
     * 胎压盒子正常
     */
    public static final int NORMAL = 170; // 胎压盒子可以正常使用

    /**
     * 采集数据
     */
    public static final int COLLECT_DATA = 180; // 采集数据
    public static final int COLLECT_DATA_FOR_CAR = 190; // 全车数据


}
