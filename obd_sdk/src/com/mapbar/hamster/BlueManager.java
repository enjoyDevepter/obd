package com.mapbar.hamster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.MainThread;
import android.widget.Toast;

import com.mapbar.hamster.core.BleWriteCallback;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;

/**
 * Created by guomin on 2018/3/8.
 */
public class BlueManager {

    public static final String KEY_WRITE_BUNDLE_STATUS = "write_status";
    public static final String KEY_WRITE_BUNDLE_VALUE = "write_value";
    private static final int STOP_SCAN_AND_CONNECT = 0;
    private static final int MSG_SPLIT_WRITE = 1;
    private static final int MSG_VERIFY = 2;
    private static final int MSG_AUTH_RESULT = 3;
    private static final int MSG_OBD_VERSION = 4;
    private static final int MSG_TIRE_PRESSURE_STATUS = 5;
    private static final int MSG_BEGIN_TO_UPDATE = 6;
    private static final int MSG_UPDATE_FOR_ONE_UNIT = 7;
    private static final int MSG_PARAMS_UPDATE_SUCESS = 8;
    private static final int MSG_ERROR = 9;
    private static final int MSG_STUDY = 10;
    private static final int MSG_STUDY_PROGRESS = 11;


    private static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String READ_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final int CONNECTED = 1; // 连接成功
    private static final int DISCONNECTED = 0; // 断开连接
    private static final int UN_AUTH = 2; // 未授权
    private static final int UN_ACTIVATE = 3; //未激活
    private static final long COMMAND_TIMEOUT = 3000;
    private BluetoothGattCharacteristic writeCharacteristic;
    private BluetoothGattCharacteristic readCharacteristic;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothAdapter mBluetoothAdapter;
    private Activity mContext;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mWorkerThread;
    private Handler mHandler;
    private int connectStatus;
    private boolean isScaning = false;
    private ArrayList<BleCallBackListener> callBackListeners = new ArrayList<>();
    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        /**
         *
         * @param device    扫描到的设备
         * @param rssi
         * @param scanRecord
         */
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, final byte[] scanRecord) {
            if (null == device) {
                return;
            }
            String name = device.getName();
            if (name != null && name.startsWith("BT")) {
                Log.d("device.getName()     " + device.getName() + " device.getAddress() " + device.getAddress());
                Message msg = mHandler.obtainMessage();
                msg.what = STOP_SCAN_AND_CONNECT;
                msg.obj = device.getAddress();
                mHandler.sendMessage(msg);
            }
        }
    };
    private boolean split;
    private byte[] mData;
    private int mCount = 20;
    private Queue<byte[]> mDataQueue;
    private BleWriteCallback bleWriteCallback = new BleWriteCallback() {
        @Override
        public void onWriteSuccess(byte[] justWrite) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            write();
        }

        @Override
        public void onWriteFailure(byte[] date) {
            // 重新发送
            realWrite(date);
        }
    };
    private int mTotalNum;
    private BluetoothManager bluetoothManager;
    private byte[] result;
    private byte[] currentProtocol;
    private int repeat = 0;

    private BlueManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BlueManager getInstance() {
        return BlueManager.InstanceHolder.INSTANCE;
    }

    /**
     * is support ble?
     *
     * @return
     */
    boolean isSupportBle() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && mContext.getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void addBleCallBackListener(BleCallBackListener listener) {
        callBackListeners.add(listener);
    }

    void notifyBleCallBackListener(int event, Object data) {
        for (BleCallBackListener callBackListener : callBackListeners) {
            callBackListener.onEvent(event, data);
        }
    }

    public boolean removeCallBackListener(BleCallBackListener listener) {
        return callBackListeners.remove(listener);
    }

    public void setBleWriteCallback(BleWriteCallback bleWriteCallback) {
        this.bleWriteCallback = bleWriteCallback;
    }

    @SuppressLint("ServiceCast")
    @MainThread
    public void init(Activity activity) {
        mContext = activity;
        if (isSupportBle()) {
            bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(mContext, "Device does not support Bluetooth", Toast.LENGTH_LONG);
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        mWorkerThread = new HandlerThread(BlueManager.class.getSimpleName());
        mWorkerThread.start();
        mHandler = new WorkerHandler(mWorkerThread.getLooper());

        startScan();
    }

    public synchronized void startScan() {
        if (null == mBluetoothAdapter || isScaning) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        isScaning = true;

        mBluetoothAdapter.startLeScan(leScanCallback);

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, 10000);

    }

    synchronized void stopScan() {
        if (null == mBluetoothAdapter || !isScaning) {
            return;
        }
        isScaning = false;
        notifyBleCallBackListener(OBDEvent.BLUE_SCAN_FINISHED, null);
        mBluetoothAdapter.stopLeScan(leScanCallback);
    }

    void connect(String address) {

        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);

        if (bluetoothDevice == null) {
            return;
        }

        mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.d("getConnectionState " + status + "   " + newState);

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTING:
                            Log.d("onConnectionStateChange  STATE_CONNECTING");
                            break;
                        case BluetoothProfile.STATE_CONNECTED:
                            Log.d("onConnectionStateChange  STATE_CONNECTED");
                            connectStatus = CONNECTED;
                            mBluetoothGatt.discoverServices();
                            break;
                        case BluetoothProfile.STATE_DISCONNECTING:
                            Log.d("onConnectionStateChange  STATE_DISCONNECTING");
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            connectStatus = DISCONNECTED;
                            Log.d("onConnectionStateChange  STATE_DISCONNECTED");
                            disconnect();
                            break;
                    }
                } else {
                    disconnect();
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.d("onServicesDiscovered  " + status);
                if (status == BluetoothGatt.GATT_SUCCESS) {

                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.BLUE_CONNECTED, null);
                        }
                    });
                    //拿到该服务 1,通过UUID拿到指定的服务  2,可以拿到该设备上所有服务的集合
                    List<BluetoothGattService> serviceList = mBluetoothGatt.getServices();

                    //可以遍历获得该设备上的服务集合，通过服务可以拿到该服务的UUID，和该服务里的所有属性Characteristic
                    for (BluetoothGattService service : serviceList) {
                        Log.d("service UUID  " + service.getUuid());
                        List<BluetoothGattCharacteristic> characteristicList = service.getCharacteristics();
                        for (BluetoothGattCharacteristic characteristic : characteristicList) {
                            Log.d("characteristic  UUID " + characteristic.getUuid());
                        }
                    }

                    //2.通过指定的UUID拿到设备中的服务也可使用在发现服务回调中保存的服务
                    BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
//
                    //3.通过指定的UUID拿到设备中的服务中的characteristic，也可以使用在发现服务回调中通过遍历服务中信息保存的Characteristic
                    writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITE_UUID));
//
                    readCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(NOTIFY_UUID));

                    mBluetoothGatt.setCharacteristicNotification(readCharacteristic, true);
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.d("OBD->APP  " + HexUtils.byte2HexStr(characteristic.getValue()));
                analyzeProtocol(characteristic.getValue());
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.d("onCharacteristicRead  ");
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.d("onCharacteristicRead  success " + Arrays.toString(characteristic.getValue()));
                }
            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Message message = new Message();
                message.what = MSG_SPLIT_WRITE;
                Bundle bundle = new Bundle();
                bundle.putInt(KEY_WRITE_BUNDLE_STATUS, status);
                bundle.putByteArray(KEY_WRITE_BUNDLE_VALUE, characteristic.getValue());
                message.setData(bundle);
                mHandler.sendMessage(message);
            }
        });
    }

    /**
     * 断开链接
     */
    public void disconnect() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.disconnect();
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mBluetoothGatt != null) {
                refresh.invoke(mBluetoothGatt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mBluetoothGatt.close();
    }

    public boolean isConnected() {
        return connectStatus == 1;
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public synchronized void write(byte[] data) {
        if (null == writeCharacteristic) {
            return;
        }

        boolean reset = true;

        if (null != currentProtocol && currentProtocol.length == data.length) {
            for (int i = 0; i < currentProtocol.length; i++) {
                if (currentProtocol[i] != data[i]) {
                    reset = false;
                    break;
                }
            }
            if (!reset) {
                repeat = 0;
            }
        } else {
            repeat = 0;
        }

        currentProtocol = new byte[data.length];

        System.arraycopy(data, 0, currentProtocol, 0, data.length);

        if (data.length > 20) {
            split = true;
            mData = data;
            splitWrite();
        } else {
            split = false;
            realWrite(data);
        }
    }

    private void realWrite(byte[] data) {
        Log.d("APP->OBD " + HexUtils.byte2HexStr(data));
        writeCharacteristic.setValue(data);
        mBluetoothGatt.writeCharacteristic(writeCharacteristic);
    }


    private Queue<byte[]> splitByte(byte[] data, int count) {
        Queue<byte[]> byteQueue = new LinkedList<>();
        if (data != null) {
            int index = 0;
            do {
                byte[] rawData = new byte[data.length - index];
                byte[] newData;
                System.arraycopy(data, index, rawData, 0, data.length - index);
                if (rawData.length <= count) {
                    newData = new byte[rawData.length];
                    System.arraycopy(rawData, 0, newData, 0, rawData.length);
                    index += rawData.length;
                } else {
                    newData = new byte[count];
                    System.arraycopy(data, index, newData, 0, count);
                    index += count;
                }
                byteQueue.offer(newData);
            } while (index < data.length);
        }
        return byteQueue;
    }

    private void splitWrite() {
        if (mData == null) {
            throw new IllegalArgumentException("data is Null!");
        }
        if (mCount < 1) {
            throw new IllegalArgumentException("split count should higher than 0!");
        }
        mDataQueue = splitByte(mData, mCount);
        mTotalNum = mDataQueue.size();
        write();
    }

    private void write() {
        if (mDataQueue.peek() == null) {
            return;
        } else {
            byte[] data = mDataQueue.poll();
            realWrite(data);
        }
    }

    /**
     * 接受数据
     *
     * @param data
     */
    void analyzeProtocol(byte[] data) {

        if (null != data && data.length > 0) {
            if (data[0] == ProtocolUtils.PROTOCOL_HEAD_TAIL) { // 消息开头
                if (data[data.length - 1] == ProtocolUtils.PROTOCOL_HEAD_TAIL) {
                    // 完整消息
                    validateAndNotify(data);
                } else {
                    // 消息不完整
                    result = new byte[data.length];
                    System.arraycopy(data, 0, result, 0, data.length);
                }
            } else {
                if (null != result) {
                    byte[] temp = new byte[result.length];
                    System.arraycopy(result, 0, temp, 0, result.length);
                    result = new byte[temp.length + data.length];
                    System.arraycopy(temp, 0, result, 0, temp.length);
                    System.arraycopy(data, 0, result, temp.length, data.length);
                }
                if (data[data.length - 1] == ProtocolUtils.PROTOCOL_HEAD_TAIL) {
                    // 消息结尾
                    validateAndNotify(result);
                }
            }
        }
    }

    private void validateAndNotify(byte[] result) {
        int cr = result[1];
        for (int i = 2; i < result.length - 2; i++) {
            cr = cr ^ result[i];
        }
        if (cr != result[result.length - 2]) {
            result = null;
        } else {
            Log.d("content  " + HexUtils.formatHexString(Arrays.copyOfRange(result, 3, result.length - 2)));
            byte[] content = new byte[result.length - 5];
            System.arraycopy(result, 3, content, 0, content.length);
            if (result[1] == 00) {
                if (result[2] == 00) { // 通用错误
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_ERROR;
                    bundle.putInt("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (result[2] == 01) { // 获取终端状态
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_VERIFY;
                    bundle.putInt("status", content[0]);
                    if (bundle.getInt("status") != 2) {
                        bundle.putString("value", HexUtils.formatHexString(Arrays.copyOfRange(content, 1, content.length)));
                    } else {
                        bundle.putString("value", new String(Arrays.copyOfRange(content, 1, content.length)));
                    }
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (result[2] == 02) { // 授权结果
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_AUTH_RESULT;
                    bundle.putInt("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }

            } else if (result[1] == 01) {
                if (result[2] == 01) { // 获取终端版本
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_OBD_VERSION;
                    bundle.putString("sn", new String(Arrays.copyOfRange(content, 0, 19)));
                    bundle.putString("version", new String(Arrays.copyOfRange(content, 19, 31)));
                    bundle.putString("car_no", new String(Arrays.copyOfRange(content, 31, 43)));
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } else if (result[1] == 02) {
                if (result[2] == 01) { // 学习模式确认
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_STUDY;
                    bundle.putInt("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);

                } else if (result[2] == 02) { // 学习进度确认
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_STUDY_PROGRESS;
                    bundle.putInt("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);

                } else if (result[2] == 03) { // 轮胎状态
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_TIRE_PRESSURE_STATUS;
                    bundle.putByte("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (result[2] == 04) { // 灵敏度确认
                }

            } else if (result[1] == 03) {
                if (result[2] == 01) { // 报警结果确认
                }
            } else if (result[1] == 05) {
                if (result[2] == 01) { // 车型参数更新确认
                    Message message = mHandler.obtainMessage();
                    message.what = MSG_PARAMS_UPDATE_SUCESS;
                    mHandler.sendMessage(message);
                }
            } else if (result[1] == 06) {
                if (result[2] == 01) { // 固件升级确认
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_BEGIN_TO_UPDATE;
                    bundle.putInt("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (result[2] == 02) { // 升级包刷写反馈
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_UPDATE_FOR_ONE_UNIT;
                    bundle.putInt("index", content[0]);
                    bundle.putInt("status", content[1]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    public static class InstanceHolder {
        private static final BlueManager INSTANCE = new BlueManager();
    }

    private static class TimeOutThread extends Thread {

        private static final String TIMEOUTSYNC = "MTIMEOUTSYNC";

        private boolean needStop = false;

        private boolean waitForCommand = false;

        @Override
        public synchronized void start() {
            needStop = false;
            super.start();
        }

        @Override
        public void run() {
            super.run();
            while (!needStop) {

                synchronized (TIMEOUTSYNC) {

                    if (needStop) {
                        return;
                    }

                    if (waitForCommand) {

                        try {
                            TIMEOUTSYNC.wait(COMMAND_TIMEOUT);


                            TIMEOUTSYNC.notifyAll();
                            TIMEOUTSYNC.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

        public void startCommand() {
            synchronized (TIMEOUTSYNC) {
                waitForCommand = true;
                TIMEOUTSYNC.notifyAll();
            }
        }

        public void endCommand() {
            synchronized (TIMEOUTSYNC) {
                waitForCommand = false;
                TIMEOUTSYNC.notifyAll();
                try {
                    TIMEOUTSYNC.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            needStop = true;
            interrupt();
        }
    }

    private final class WorkerHandler extends Handler {

        WorkerHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            switch (msg.what) {
                case STOP_SCAN_AND_CONNECT:
                    final String address = (String) msg.obj;
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            stopScan();
                            connect(address);
                        }
                    });
                    break;
                case MSG_SPLIT_WRITE:
                    final int status = bundle.getInt(KEY_WRITE_BUNDLE_STATUS);
                    final byte[] value = bundle.getByteArray(KEY_WRITE_BUNDLE_VALUE);
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (split && bleWriteCallback != null) {
                                if (status == BluetoothGatt.GATT_SUCCESS) {
                                    bleWriteCallback.onWriteSuccess(value);
                                } else {
                                    bleWriteCallback.onWriteFailure(value);
                                }
                            }
                        }
                    });
                    break;
                case MSG_VERIFY:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            String date = bundle.getString("value");
                            switch (bundle.getInt("status")) {
                                case 0: // 首次使用
                                    notifyBleCallBackListener(OBDEvent.OBD_FIRST_USE, date);
                                    break;
                                case 1: // 设置授权正常
                                    notifyBleCallBackListener(OBDEvent.OBD_NORMAL, date);
                                    break;
                                case 2: // 设备授权过期
                                    notifyBleCallBackListener(OBDEvent.OBD_EXPIRE, date);
                                    break;
                            }
                        }
                    });
                    break;
                case MSG_AUTH_RESULT:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_AUTH_RESULT, bundle.getInt("status"));
                        }
                    });
                    break;
                case MSG_OBD_VERSION:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            OBDVersionInfo version = new OBDVersionInfo();
                            version.setCar_no(bundle.getString("car_no"));
                            version.setSn(bundle.getString("sn"));
                            version.setVersion(bundle.getString("version"));
                            notifyBleCallBackListener(OBDEvent.OBD_GET_VERSION, version);
                        }
                    });
                    break;
                case MSG_TIRE_PRESSURE_STATUS:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS, bundle.getByte("status"));
                        }
                    });
                    break;
                case MSG_BEGIN_TO_UPDATE:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_BEGIN_UPDATE, bundle.getInt("status"));
                        }
                    });
                    break;
                case MSG_UPDATE_FOR_ONE_UNIT:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Update update = new Update();
                            update.setIndex(bundle.getInt("index"));
                            update.setStatus(bundle.getInt("status"));
                            notifyBleCallBackListener(OBDEvent.OBD_UPDATE_FINISH_UNIT, update);
                        }
                    });
                    break;
                case MSG_PARAMS_UPDATE_SUCESS:
                    // 车型参数更新成功
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_UPDATE_PARAMS_SUCCESS, null);
                        }
                    });
                    break;
                case MSG_ERROR:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (repeat < 3) {
                                write(currentProtocol); // 重发
                                repeat++;
                            }
                            notifyBleCallBackListener(OBDEvent.OBD_ERROR, bundle.getInt("status"));
                        }
                    });
                    break;
                case MSG_STUDY:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_STUDY, null);
                        }
                    });
                    break;
                case MSG_STUDY_PROGRESS:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_STUDY_PROGRESS, bundle.getInt("status"));
                        }
                    });
                    break;

            }
        }
    }
}