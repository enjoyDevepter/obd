package com.mapbar.hamster.core;

import android.content.Context;
import android.support.annotation.MainThread;

/**
 * Created by guomin on 2018/2/6.
 */
class DeviceManger {

    private OBDListener listener;

    private DeviceManger() {
    }

    public static DeviceManger getInstance() {
        return InstanceHolder.INSTANCE;
    }

    /**
     * 中止连接
     */
    public void disconnect() {
        BluetoothManager.getInstance().disconnectDevice();
    }

    /**
     * 链接OBD设备
     *
     * @param context
     */
    @MainThread
    public void connectDevice(Context context, OBDListener listener) {
        this.listener = listener;
        BluetoothManager.getInstance().init(context);
    }


    public OBDListener getListener() {
        return listener;
    }

    public interface OBDListener {

        void onEvent(int event, Object data);
    }

    public static class InstanceHolder {
        private static final DeviceManger INSTANCE = new DeviceManger();
    }
}
