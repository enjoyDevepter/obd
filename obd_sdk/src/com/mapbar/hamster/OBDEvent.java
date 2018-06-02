package com.mapbar.hamster;

/**
 * Created by guomin on 2018/3/7.
 */

class OBDEvent {
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
    public static final int BLUE_CONNECTING = 4;
    /**
     * 蓝牙设备连接成功<br>
     */
    public static final int BLUE_CONNECTED = 5;
    /**
     * 蓝牙设备正在断开连接<br>
     */
    public static final int BLUE_DISCONNECTING = 6;
    /**
     * 蓝牙设备断开连接<br>
     */
    public static final int BLUE_DISCONNECTED = 7;
}
