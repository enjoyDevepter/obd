package com.mapbar.hamster.core;

/**
 * Created by guomin on 2018/3/7.
 */

class OBDEvent {
    /**
     * 开始扫描设备<br>
     * 回调中返回的数据: null
     */
    public static final int scanStatred = 0;
    /**
     * 扫描设备结束<br>
     * 回调中返回的数据: null
     */
    public static final int scanFinished = 1;
    /**
     * 扫描失败<br>
     * 回调中返回的数据: null
     */
    public static final int scanFailed = 2;
    /**
     * 发现新设备设备<br>
     * 回调中返回的数据: {@link android.bluetooth.BluetoothDevice}实例
     */
    public static final int found = 3;
    /**
     * OBD设备正在连接中<br>
     */
    public static final int obdConnecting = 4;
    /**
     * OBD设备连接成功，开始准备数据<br>
     */
    public static final int obdConnectSucc = 5;
    /**
     * OBD设备连接失败<br>
     */
    public static final int obdConnectFailed = 6;
    /**
     * OBD设备断开连接
     */
    public static final int disconnected = 7;
}
