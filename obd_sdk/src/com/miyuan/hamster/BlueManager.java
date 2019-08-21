package com.miyuan.hamster;

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

import com.miyuan.hamster.core.BleWriteCallback;
import com.miyuan.hamster.core.HexUtils;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.hamster.log.Log;

import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static android.bluetooth.BluetoothDevice.TRANSPORT_LE;

/**
 * Created by guomin on 2018/3/8.
 */
public class BlueManager {

    public static final String KEY_WRITE_BUNDLE_STATUS = "write_status";
    public static final String KEY_WRITE_BUNDLE_VALUE = "write_value";
    private static final int STOP_SCAN_AND_CONNECT = 0;
    private static final int MSG_SPLIT_WRITE = 1;
    private static final int MSG_OBD_DISCONNECTED = 12;


    private static final int MSG_ERROR = 20; // 错误
    private static final int MSG_STATUS_UPDATA = 21; // 状态信息上传
    private static final int MSG_UNREGISTERED = 30; //未注册
    private static final int MSG_AUTHORIZATION = 40; //未授权或者授权过期
    private static final int MSG_AUTHORIZATION_SUCCESS = 41; //授权成功
    private static final int MSG_AUTHORIZATION_FAIL = 42; //授权成功
    private static final int MSG_NO_CAR_ID = 43; //未选择车型
    private static final int MSG_NO_PARAM = 50; // 无车型参数
    private static final int MSG_PARAM_UPDATE_SUCCESS = 51; // 车型参数更新成功
    private static final int MSG_PARAM_UPDATE_FAIL = 53; // 车型参数更新失败
    private static final int MSG_CURRENT_MISMATCHING = 60; // 当前胎压不匹配
    private static final int MSG_BEFORE_MATCHING = 70; // 之前胎压匹配过
    private static final int MSG_UN_ADJUST = 80; // 未完成校准
    private static final int MSG_ADJUSTING = 81; // 校准中
    private static final int MSG_ADJUST_SUCCESS = 83; // 校准完成
    private static final int MSG_UN_LEGALITY = 90; // BoxId 不合法
    private static final int MSG_NORMAL = 100; // 胎压盒子可以正常使用
    private static final int MSG_COLLECT_DATA = 110; // 采集数据
    private static final int MSG_COLLECT_DATA_FOR_CAR = 120; // 全车数据
    private static final int MSG_PHYSICAL = 130; // 体检
    private static final int MSG_FAULT_CODE = 140; // 故障码
    private static final int MSG_CLEAR_FAULT_CODE = 150; // 清除故障码
    private static final int MSG_SENSITIVE_CODE = 160; // 清除故障码
    private static final int MSG_COMMON_INFO = 170; // 统一回复信息
    private static final int MSG_HUD_STATUS_INFO = 180; // HUD区域属性
    private static final int MSG_HUD_WARM_STATUS_INFO = 181; // HUD区域警告属性
    private static final int MSG_HUD_PARAMS_INFO = 182; // HUD参数属性
    private static final int MSG_FM_STATUS_INFO = 183; // FM参数属性


    private static final int MSG_VERIFY = 2;
    private static final int MSG_AUTH_RESULT = 3;
    private static final int MSG_OBD_VERSION = 4;
    private static final int MSG_TIRE_PRESSURE_STATUS = 5;
    private static final int MSG_BEGIN_TO_UPDATE = 6;
    private static final int MSG_UPDATE_FOR_ONE_UNIT = 7;
    private static final int MSG_PARAMS_UPDATE_SUCESS = 8;
    private static final int MSG_STUDY = 10;
    private static final int MSG_STUDY_PROGRESS = 11;


    private static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    private static final String READ_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String WRITE_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final String NOTIFY_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
    private static final int CONNECTED = 1; // 连接成功
    private static final int DISCONNECTED = 0; // 断开连接
    private static long COMMAND_TIMEOUT = 5000;
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
    private boolean isNavi;
    private volatile boolean canGo = true;
    private String currentDeviceName;
    private ArrayList<String> scanResult = new ArrayList<>();
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
            if (scanResult.contains(name)) {
                return;
            }
            scanResult.add(name);
            if (name != null && (name.toUpperCase().startsWith("MYOBD") || name.toUpperCase().startsWith("GUARDIAN"))) {
                Log.d("device.getName()=    " + device.getName() + " device.getAddress()=" + device.getAddress());
                Message msg = mHandler.obtainMessage();
                msg.what = STOP_SCAN_AND_CONNECT;
                msg.obj = device.getAddress();
                mHandler.sendMessage(msg);
            }
        }
    };
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d("getConnectionState " + status + "   " + newState);

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.d("onConnectionStateChange  STATE_CONNECTED");
                    canGo = true;
                    connectStatus = CONNECTED;
                    mBluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("onConnectionStateChange  STATE_DISCONNECTED");
                    connectStatus = DISCONNECTED;
                    disconnect();
                    Message message = new Message();
                    message.what = MSG_OBD_DISCONNECTED;
                    mHandler.sendMessage(message);
                }
            } else {
                connectStatus = DISCONNECTED;
                disconnect();
                Message message = new Message();
                message.what = MSG_OBD_DISCONNECTED;
                mHandler.sendMessage(message);
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

                //2.通过指定的UUID拿到设备中的服务也可使用在发现服务回调中保存的服务
                BluetoothGattService bluetoothGattService = mBluetoothGatt.getService(UUID.fromString(SERVICE_UUID));
//
                //3.通过指定的UUID拿到设备中的服务中的characteristic，也可以使用在发现服务回调中通过遍历服务中信息保存的Characteristic
                writeCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(WRITE_UUID));
                readCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(NOTIFY_UUID));

                mBluetoothGatt.setCharacteristicNotification(readCharacteristic, true);

                currentDeviceName = gatt.getDevice().getAddress();
                canGo = true;
                Log.d("onServicesDiscovered  success");
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
    };

    public boolean isNavi() {
        return isNavi;
    }

    private volatile boolean split;
    private byte[] mData;
    private int mCount = 20;
    private Queue<byte[]> mDataQueue;
    private TimeOutThread timeOutThread;
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
    private BluetoothManager bluetoothManager;
    private byte[] currentProtocol;
    private volatile int currentRepeat = 0;
    private int repeat = 0;
    private LinkedList<byte[]> instructList;
    /**
     * 待发送指令
     */
    private LinkedBlockingQueue<byte[]> queue = new LinkedBlockingQueue(1);
    /**
     * 上一包是否接受完成
     */
    private boolean unfinish = true;
    /**
     * 完整包
     */
    private byte[] full;
    private int currentIndex;
    private int count;

    public void setNavi(boolean navi) {
        isNavi = navi;
    }

    private BlueManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        new Thread(new Runnable() {
            @Override
            public void run() {
                sentToBox();
            }
        }, "sendMessage").start();
        timeOutThread = new TimeOutThread();
        timeOutThread.start();
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

//        if (!mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.enable();
//        }

        mWorkerThread = new HandlerThread(BlueManager.class.getSimpleName());
        mWorkerThread.start();
        mHandler = new WorkerHandler(mWorkerThread.getLooper());

        startScan();
    }

    public synchronized void startScan() {
        Log.d("startScan  " + isScaning + " mBluetoothAdapter   " + (null == mBluetoothAdapter));
        if (null == mBluetoothAdapter || isScaning) {
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        if (!"".equals(currentDeviceName) && null != currentDeviceName) {
            Message msg = mHandler.obtainMessage();
            msg.what = STOP_SCAN_AND_CONNECT;
            msg.obj = currentDeviceName;
            mHandler.sendMessage(msg);
            return;
        }

        isScaning = true;

        scanResult.clear();

        mBluetoothAdapter.startLeScan(leScanCallback);

        mMainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan(false);
            }
        }, 10000);
        Log.d("startScan   success");

    }

    public synchronized void stopScan(boolean find) {
        if (null == mBluetoothAdapter || !isScaning) {
            return;
        }
        Log.d("stopScan  ");
        isScaning = false;
        if (isNavi && !find) {
            startScan();
            return;
        }
        notifyBleCallBackListener(OBDEvent.BLUE_SCAN_FINISHED, find);
        mBluetoothAdapter.stopLeScan(leScanCallback);
    }

    void connect(String address) {

        BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);

        if (bluetoothDevice == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mBluetoothGatt = bluetoothDevice.connectGatt(mContext,
                    false, bluetoothGattCallback, TRANSPORT_LE);
        } else {
            mBluetoothGatt = bluetoothDevice.connectGatt(mContext, false, bluetoothGattCallback);
        }
    }

    /**
     * 断开链接
     */
    public synchronized void disconnect() {

        Log.d(" disconnect ");
        timeOutThread.endCommand();

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
        writeCharacteristic = null;
        readCharacteristic = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;

        isScaning = false;
        canGo = true;
        queue.clear();
        if (instructList != null) {
            instructList.clear();
        }
        Log.d(" disconnect success  queue.size()  " + queue.size() + "  instructList.size() " + instructList.size() + "  canGo  " + canGo);

    }

    public boolean isConnected() {
        return connectStatus == 1;
    }

    /**
     * 发送指令
     *
     * @param data
     */
    public synchronized void send(byte[] data) {
        // 判断该指令是否和待发送队列中最后一个指令相同，如果相同则不放入，不相同则加入，判断date中第2、3位是否一致既可

        Log.d("send1  instructList size " + (instructList != null ? instructList.size() : "NULL "));

        if (instructList == null) {
            instructList = new LinkedList<>();
            queue.add(data);
            return;
        }

        Log.d("send1  queue size " + queue.size() + " canGo " + canGo);

        if (queue.size() == 0 && canGo) {
            queue.add(data);
            return;
        }

        byte[] last = instructList.peekLast();
        if (last != null && data[1] == last[1] && data[2] == last[2] /*&& data[1] != 0x90*/) {
            if (data[1] == 0x90) {
                instructList.pollLast();
                instructList.addLast(data);
            }
            Log.d("instructList has one ");
            return;
        }
        Log.d("add to instructList");
        instructList.addLast(data);
    }

    /**
     * 蓝牙通信
     */
    private void sentToBox() {
        while (true) {
            try {
                byte[] message = queue.take();
                canGo = false;
                write(message);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送数据
     *
     * @param data
     */
    private synchronized void write(byte[] data) {
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
        if (mBluetoothGatt == null || writeCharacteristic == null) {
            return;
        }
        canGo = false;
        Log.d("APP->OBD " + HexUtils.byte2HexStr(data));

        if (split || (data[1] == (byte) 0x89 && data[2] == 01)) { // 未拆封包 或者 心跳包
            if (!split) {
                canGo = true;
            }
        } else {
            if ((data[1] == (byte) 0x88 && data[2] == 05)) {
                COMMAND_TIMEOUT = 15000;
                timeOutThread.startCommand(true);
            } else if ((data[1] == (byte) 0x90 && data[2] == 01) || (data[1] == (byte) 0x90 && data[2] == 02) || (data[1] == (byte) 0x90 && data[2] == 03)) {
                COMMAND_TIMEOUT = 1500;
                timeOutThread.startCommand(false);
            } else {
                COMMAND_TIMEOUT = 5000;
                timeOutThread.startCommand(true);
            }
        }
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
        write();
    }

    private void write() {
        if (mDataQueue.peek() == null) {
            timeOutThread.startCommand(true);
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
    public synchronized void analyzeProtocol(byte[] data) {
        if (null != data && data.length > 0) {
            if (data[0] == ProtocolUtils.PROTOCOL_HEAD_TAIL && data.length != 1 && unfinish && data.length >= 7) {
                // 获取包长度
                byte[] len = new byte[]{data[4], data[3]};
                count = byteToShort(len);
                if (data.length == count + 7) {  //为完整一包
                    full = new byte[count + 5];
                    System.arraycopy(data, 1, full, 0, full.length);
                    validateAndNotify(full);
                } else if (data.length < count + 7) {
                    unfinish = false;
                    full = new byte[count + 5];
                    currentIndex = data.length - 1;
                    System.arraycopy(data, 1, full, 0, data.length - 1);
                } else if (data.length > count + 7) {
                    Log.d(" analyzeProtocol error one ");
                    currentIndex = 0;
                    unfinish = true;
                    full = new byte[]{};
                    return;
                }
            } else {
                if ((currentIndex + data.length - 1) == count + 5) { // 最后一包
                    unfinish = true;
                    System.arraycopy(data, 0, full, currentIndex, data.length - 1);
                    validateAndNotify(full);
                } else if ((currentIndex + data.length - 1) < count + 5) { // 包不完整
                    // 未完成
                    System.arraycopy(data, 0, full, currentIndex, data.length);
                    currentIndex += data.length;
                } else {
                    Log.d(" analyzeProtocol error two ");
                    currentIndex = 0;
                    unfinish = true;
                    full = new byte[]{};
                    return;
                }
            }
        }
    }

    public short byteToShort(byte[] b) {
        short s = 0;
        short s0 = (short) (b[0] & 0xff);// 最低位
        short s1 = (short) (b[1] & 0xff);
        s1 <<= 8;
        s = (short) (s0 | s1);
        return s;
    }

    /**
     * @param res
     */
    private void validateAndNotify(byte[] res) {
        if (!(/*(res[0] == (byte) 0x09 && res[1] == 01) ||*/ (res[0] == (byte) 0x08 && res[1] == 03))) {
            Log.d(" validateAndNotify next ");
            timeOutThread.endCommand();
            byte[] msg = instructList.pollLast();
            canGo = true;
            if (msg != null && queue.size() == 0) {
                queue.add(msg);
            }
        }

        byte[] result = new byte[res.length];
        System.arraycopy(res, 0, result, 0, res.length);

        int cr = result[0];
        for (int i = 1; i < result.length - 1; i++) {
            cr = cr ^ result[i];
        }
        if (cr != result[result.length - 1]) {
            result = null;
        } else {
            byte[] content = new byte[result.length - 1];
            System.arraycopy(result, 0, content, 0, content.length); // 去掉校验码
            Log.d("content  " + HexUtils.formatHexString(content));
            if (content[0] == 00) {
                if (content[1] == 00) { // 通用错误
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_ERROR;
                    bundle.putInt("status", content[0]);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (content[1] == 01 || content[1] == 02) { // 获取终端状态
                    OBDStatusInfo obdStatusInfo = new OBDStatusInfo();
                    obdStatusInfo.setBoxId(HexUtils.formatHexString(Arrays.copyOfRange(content, 12, 24)));
                    obdStatusInfo.setSn(new String(Arrays.copyOfRange(content, 24, 43)));
                    obdStatusInfo.setbVersion(new String(Arrays.copyOfRange(content, 43, 55)));
                    obdStatusInfo.setpVersion(new String(Arrays.copyOfRange(content, 55, 67)));
                    obdStatusInfo.setSensitive((content[11] & 0xff));
                    obdStatusInfo.setCurrentMatching(content[7] == 01);
                    obdStatusInfo.setBerforeMatching(content[8] == 01);
                    obdStatusInfo.setOrginal(content);
                    Log.d(obdStatusInfo.toString());

                    if (content[content.length - 1] == 1) {
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_STATUS_UPDATA;
                        mHandler.sendMessage(message);
                    }
//                    Message message = mHandler.obtainMessage();
//                    Bundle bundle = new Bundle();
//                    bundle.putSerializable("obd_status_info", obdStatusInfo);
//                    message.setData(bundle);
                    // 判断是否注册
                    if (content[4] == 00) { // 未注册
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_UNREGISTERED;
                        mHandler.sendMessage(message);
                        return;
                    }
                    // 判断是否授权
                    if ((content[5] & 15) == 0) { // 未授权或者授权过期或者授权失败
//                        if ((content[5] >> 4) != 0) { // 授权失败
//                            Message message = mHandler.obtainMessage();
//                            Bundle bundle = new Bundle();
//                            bundle.putSerializable("obd_status_info", obdStatusInfo);
//                            message.setData(bundle);
//                            message.what = MSG_AUTHORIZATION_FAIL;
//                            mHandler.sendMessage(message);
//                            return;
//                        }
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_AUTHORIZATION;
                        mHandler.sendMessage(message);
                        return;
                    } else if ((content[5] & 15) == 1) {
                        // 授权成功
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_AUTHORIZATION_SUCCESS;
                        mHandler.sendMessage(message);
                    } else {
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_AUTHORIZATION_FAIL;
                        mHandler.sendMessage(message);
                        return;
                    }
                    // 三期新需求，优先判断是否选择车型
                    if (content[1] == 02) {
                        if (content[67] == 00) { // 未选择车型
                            Message message = mHandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("obd_status_info", obdStatusInfo);
                            message.setData(bundle);
                            message.what = MSG_NO_CAR_ID;
                            mHandler.sendMessage(message);
                            return;
                        }
                        obdStatusInfo.setNews(true);
                        obdStatusInfo.setHudType(content[68]);
                        obdStatusInfo.setSupportNavi(content[69] == 1);
                        obdStatusInfo.setSupportFM(content[70] == 1);
                    }

                    // 判断是否存在车型参数
                    if ((content[6] & 15) == 00) { // 未车型参数或者车型参数更新失败
                        if ((content[6] >> 4) != 00) { // 车型参数失败
                            Message message = mHandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("obd_status_info", obdStatusInfo);
                            message.setData(bundle);
                            message.what = MSG_PARAM_UPDATE_FAIL;
                            mHandler.sendMessage(message);
                            return;
                        }
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_NO_PARAM;
                        mHandler.sendMessage(message);
                        return;
                    } else {
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_PARAM_UPDATE_SUCCESS;
                        mHandler.sendMessage(message);
                    }

                    // 判断当前胎压是否匹配
                    if (content[7] == 00) { // 当前胎压不匹配
                        // 优先判断之前胎压是否匹配
                        if (content[8] == 01) { // 之前胎压匹配
                            Message message = mHandler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("obd_status_info", obdStatusInfo);
                            message.setData(bundle);
                            message.what = MSG_BEFORE_MATCHING;
                            mHandler.sendMessage(message);
                            return;
                        }
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_CURRENT_MISMATCHING;
                        mHandler.sendMessage(message);
                        return;
                    }

                    // 判断是否完成校准
                    if (content[9] == 00) { // 校准状态
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_UN_ADJUST;
                        mHandler.sendMessage(message);
                        return;
                    }
                    if (content[9] == 01) { // 校准中状态
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_ADJUSTING;
                        mHandler.sendMessage(message);
                        return;
                    }

                    if (content[9] == 02) { // 校准完成
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("obd_status_info", obdStatusInfo);
                        message.setData(bundle);
                        message.what = MSG_ADJUST_SUCCESS;
                        mHandler.sendMessage(message);
                    }

//                    // 判断BoxId是否合法
//                    if (content[10] == 00) { // boxId是否合法
//                        Message message = mHandler.obtainMessage();
//                        Bundle bundle = new Bundle();
//                        bundle.putSerializable("obd_status_info", obdStatusInfo);
//                        message.setData(bundle);
//                        message.what = MSG_UN_LEGALITY;
//                        mHandler.sendMessage(message);
//                        return;
//                    }

                    Message normalMessage = mHandler.obtainMessage();
                    Bundle normalBundle = new Bundle();
                    normalBundle.putSerializable("obd_status_info", obdStatusInfo);
                    normalMessage.setData(normalBundle);
                    normalMessage.what = MSG_NORMAL;
                    mHandler.sendMessage(normalMessage);

//                    // 灵敏度状态
//                    if (result[9] == 00) { // 之前胎压不匹配
//                        Message message = mHandler.obtainMessage();
//                        Bundle bundle = new Bundle();
//                        bundle.putInt("sensitive",(content[9] & 0xff));
//                        message.what = MSG_SENSITIVE;
//                        message.setData(bundle);
//                        mHandler.sendMessage(message);
//                        return;
//                    }
                }
            } else if (content[0] == 2) {
                if (content[1] == 5) {
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_SENSITIVE_CODE;
                    bundle.putByteArray("status", content);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } else if (content[0] == 8) {
                if (content[1] == 1) {
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_FAULT_CODE;
                    bundle.putByteArray("status", content);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (content[1] == 2) { // 清除故障码
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_CLEAR_FAULT_CODE;
                    bundle.putByteArray("status", content);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (content[1] == 3) { // 胎压状态
                    PressureInfo pressureInfo = new PressureInfo();
                    byte[] bytes = HexUtils.getBooleanArray(content[4]);
                    if (bytes[7] == 1) {
                        pressureInfo.setStatus(1);
                    } else if (bytes[6] == 1) {
                        pressureInfo.setStatus(2);
                    } else if (bytes[5] == 1) {
                        pressureInfo.setStatus(3);
                    } else if (bytes[6] == 1) {
                        pressureInfo.setStatus(4);
                    } else {
                        pressureInfo.setStatus(0);
                    }
                    pressureInfo.setFaultCount(content[5] & 0xFF);
                    DecimalFormat decimalFormat = new DecimalFormat(".0");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                    pressureInfo.setVoltage((decimalFormat.format(HexUtils.byteToShort(new byte[]{content[6], content[7]}) / 1000f)));
                    pressureInfo.setTemperature((content[8] & 0xFF) - 40);
                    pressureInfo.setSpeed(content[9] & 0xFF);
                    pressureInfo.setRotationRate(HexUtils.byteToShort(new byte[]{content[10], content[11]}));
                    pressureInfo.setOilConsumption((HexUtils.byteToShort(new byte[]{content[12], content[13]}) * 0.1));
                    pressureInfo.setConsumption((HexUtils.byteToShort(new byte[]{content[14], content[15]}) * 0.1));
                    pressureInfo.setSurplusOil(content[16] & 0xFF);
                    pressureInfo.setUpdate(content[content.length - 1] == 1);
                    pressureInfo.setOrigin(content);
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_TIRE_PRESSURE_STATUS;
                    bundle.putSerializable("pressureInfo", pressureInfo);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (content[1] == 05) {
                    if (content[1] == 05) {
                        Message message = mHandler.obtainMessage();
                        Bundle bundle = new Bundle();
                        message.what = MSG_PHYSICAL;
                        bundle.putByteArray("status", content);
                        message.setData(bundle);
                        mHandler.sendMessage(message);
                    }
                }
            } else if (content[0] == 9) {
                if (content[1] == 01) { // 采集数据
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_COLLECT_DATA;
                    bundle.putByteArray("status", content);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (content[1] == 2) {
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_COLLECT_DATA_FOR_CAR;
                    bundle.putByteArray("status", content);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } else if (content[0] == 0x0A) { // 区域属性反馈
                if (content[1] == 2) {
                    HUDStatus hudStatus = new HUDStatus();
                    hudStatus.setHudType(content[4]);
                    int count = content[5];
                    for (int i = 0; i < count; i++) {
                        int value = content[7 + i * 2];
                        switch (content[6 + i * 2]) {
                            case 0x01: // 水温
                                hudStatus.setTempShow(value == 1);
                                break;
                            case 0x02: // 转速
                                hudStatus.setRpmShow(value == 1);
                                break;
                            case 0x03: // 车速
                                hudStatus.setSpeedShow(value == 1);
                                break;
                            case 0x04: // 瞬时油耗
                                hudStatus.setOilShow(value == 1);
                                break;
                            case 0x05: // 平均油耗
                                hudStatus.setAvgOilShow(value == 1);
                                break;
                            case 0x06: // 燃油总消耗
                                hudStatus.setConsumeOilShow(value == 1);
                                break;
                            case 0x07: // 剩余燃油
                                hudStatus.setRemainderOilShow(value == 1);
                                break;
                            case 0x08: // 电压
                                hudStatus.setVoltageShow(value == 1);
                                break;
                            case 0x09: // 行驶时间
                                hudStatus.setTimeShow(value == 1);
                                break;
                            case 0x0A: // 小计里程
                                hudStatus.setMileShow(value == 1);
                                break;
                            case 0x0B: // 胎压
                                hudStatus.setTireShow(value == 1);
                                break;
                            case 0x0C: // 发动机负荷
                                hudStatus.setEngineloadShow(value == 1);
                                break;
                            case 0x21: // 多功能一区
                                hudStatus.setMultifunctionalOneType(value);
                                break;
                            case 0x22: // 多功能二区
                                hudStatus.setMultifunctionalTwoType(value);
                                break;
                            case 0x23: // 多功能三区
                                hudStatus.setMultifunctionalThreeType(value);
                                break;
                            default:
                                break;
                        }
                    }
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_HUD_STATUS_INFO;
                    bundle.putSerializable("HUDStatus", hudStatus);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } else if (content[0] == 0x0B) {
                if (content[1] == 1) { // HUD  参数
                    int count = content[4];
                    HUDParams params = new HUDParams();
                    for (int i = 0; i < count; i++) {
                        int value = Integer.parseInt(String.valueOf(content[6 + i * 2] & 0xFF), 10);
                        switch (content[5 + i * 2]) {
                            case 0x01: // 亮度等级
                                params.setLight(value);
                                break;
                            case 0x02: // 音量等级
                                params.setVolume(value);
                                break;
                            case 0x03: // 疲劳驾驶时长
                                params.setDriveTime(value);
                                break;
                            case 0x04: // 水温报警
                                params.setTempWarm(value);
                                break;
                            case 0x05: // 语音播报开关
                                params.setSound(value == 1);
                                break;
                            case 0x06: // 车辆自动启停配置
                                params.setStart(value);
                                break;
                            case 0x07: // 休眠模式
                                params.setSleep(value == 1);
                                break;
                            case 0x08: // 车速误差
                                params.setSpeedCalibration(value);
                                break;
                            case 0x09: // 超速阈值
                                params.setOverSpeed(value);
                                break;
                            case 0x0A: // 智能高速模式
                                params.setHighMode(value == 1);
                                break;
                            case 0x0B: // 里程误差校准
                                params.setMileCalibration(value);
                                break;
                            case 0x0C: // 导航模式
                                params.setNaviMode(value == 1);
                                break;
                            default:
                                break;
                        }
                    }
                    params.setOrigin(content);
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_HUD_PARAMS_INFO;
                    bundle.putSerializable("HUDParams", params);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                } else if (content[1] == 2) { // 警报属性设置反馈
                    int count = content[4];
                    HUDWarmStatus warmStatus = new HUDWarmStatus();
                    for (int i = 0; i < count; i++) {
                        int value = content[6 + i * 2];
                        switch (content[5 + i * 2]) {
                            case 0x01: //水温
                                warmStatus.setTemperatureWarmShow(value == 1);
                                break;
                            case 0x02: // 电压
                                warmStatus.setVoltageWarmShow(value == 1);
                                break;
                            case 0x03: // 剩余燃油
                                warmStatus.setOilWarmShow(value == 1);
                                break;
                            case 0x04: // 限速
                                warmStatus.setSpeedWarmShow(value == 1);
                                break;
                            case 0x05: // 胎压
                                warmStatus.setTrieWarmShow(value == 1);
                                break;
                            case 0x06: // 疲劳驾驶
                                warmStatus.setTiredWarmShow(value == 1);
                                break;
                            case 0x07: // 故障
                                warmStatus.setFaultWarmShow(value == 1);
                                break;
                            default:
                                break;
                        }
                    }
                    warmStatus.setOrigin(content);
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_HUD_WARM_STATUS_INFO;
                    bundle.putSerializable("HUDWarmStatus", warmStatus);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } else if (content[0] == 0x0C) {
                if (content[1] == 01) {
                    FMStatus fmStatus = new FMStatus();
                    fmStatus.setEnable(content[4] == 1);
                    fmStatus.setRate(((content[5] & 0xFF) << 8) | (content[6] & 0xFF));
                    Message message = mHandler.obtainMessage();
                    Bundle bundle = new Bundle();
                    message.what = MSG_FM_STATUS_INFO;
                    bundle.putSerializable("FMStatus", fmStatus);
                    message.setData(bundle);
                    mHandler.sendMessage(message);
                }
            } else if (content[0] == 0x3E) {
                if (content[1] == 01) {
                    Message message = mHandler.obtainMessage();
                    message.what = MSG_COMMON_INFO;
                    mHandler.sendMessage(message);
                }
            }
        }
    }

    public static class InstanceHolder {
        private static final BlueManager INSTANCE = new BlueManager();
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
                            stopScan(true);
                            try {
                                Thread.sleep(1500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
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
//                            OBDVersionInfo version = new OBDVersionInfo();
//                            version.setCar_no(bundle.getString("car_no"));
//                            version.setSn(bundle.getString("sn"));
//                            version.setVersion(bundle.getString("version"));
//                            notifyBleCallBackListener(OBDEvent.OBD_GET_VERSION, version);
                        }
                    });
                    break;
                case MSG_TIRE_PRESSURE_STATUS:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS, bundle.getSerializable("pressureInfo"));
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
//                            notifyBleCallBackListener(OBDEvent.OBD_ERROR, bundle.getInt("status"));
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
                case MSG_OBD_DISCONNECTED:
                    if (isNavi) {
                        Log.d("isNavi MSG_OBD_DISCONNECTED");
                        startScan();
                    } else {
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("OBDEvent.STATUS_UPDATA");
                                notifyBleCallBackListener(OBDEvent.OBD_DISCONNECTED, null);
                            }
                        });
                    }
                    break;
                case MSG_STATUS_UPDATA:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.STATUS_UPDATA");
                            notifyBleCallBackListener(OBDEvent.STATUS_UPDATA, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_UNREGISTERED: //未注册
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.UNREGISTERED");
                            notifyBleCallBackListener(OBDEvent.UNREGISTERED, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_AUTHORIZATION: //未授权或者授权过期
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.AUTHORIZATION");
                            notifyBleCallBackListener(OBDEvent.AUTHORIZATION, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_AUTHORIZATION_SUCCESS:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.AUTHORIZATION_SUCCESS");
                            notifyBleCallBackListener(OBDEvent.AUTHORIZATION_SUCCESS, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_AUTHORIZATION_FAIL:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.AUTHORIZATION_FAIL");
                            notifyBleCallBackListener(OBDEvent.AUTHORIZATION_FAIL, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_NO_CAR_ID:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.NO_CAR_ID");
                            notifyBleCallBackListener(OBDEvent.NO_CAR_ID, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_NO_PARAM: // 无车型参数
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.NO_PARAM");
                            notifyBleCallBackListener(OBDEvent.NO_PARAM, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_PARAM_UPDATE_SUCCESS:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.PARAM_UPDATE_SUCCESS");
                            notifyBleCallBackListener(OBDEvent.PARAM_UPDATE_SUCCESS, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_PARAM_UPDATE_FAIL:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.PARAM_UPDATE_FAIL");
                            notifyBleCallBackListener(OBDEvent.PARAM_UPDATE_FAIL, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_CURRENT_MISMATCHING: // 当前胎压不匹配
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.CURRENT_MISMATCHING");
                            notifyBleCallBackListener(OBDEvent.CURRENT_MISMATCHING, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_BEFORE_MATCHING: // 之前胎压匹配过
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.BEFORE_MISMATCHING");
                            notifyBleCallBackListener(OBDEvent.BEFORE_MATCHING, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_UN_ADJUST: // 未完成校准
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.UN_ADJUST");
                            notifyBleCallBackListener(OBDEvent.UN_ADJUST, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_ADJUSTING:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.ADJUSTING");
                            notifyBleCallBackListener(OBDEvent.ADJUSTING, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_ADJUST_SUCCESS:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.ADJUST_SUCCESS");
                            notifyBleCallBackListener(OBDEvent.ADJUST_SUCCESS, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_UN_LEGALITY: //BoxId 不合法
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.UN_LEGALITY");
                            notifyBleCallBackListener(OBDEvent.UN_LEGALITY, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_NORMAL: // 胎压盒子可以正常使用
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.NORMAL");
                            notifyBleCallBackListener(OBDEvent.NORMAL, bundle.getSerializable("obd_status_info"));
                        }
                    });
                    break;
                case MSG_COLLECT_DATA:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.COLLECT_DATA");
                            notifyBleCallBackListener(OBDEvent.COLLECT_DATA, bundle.getByteArray("status"));
                        }
                    });
                    break;
                case MSG_COLLECT_DATA_FOR_CAR:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.COLLECT_DATA_FOR_CAR");
                            notifyBleCallBackListener(OBDEvent.COLLECT_DATA_FOR_CAR, bundle.getByteArray("status"));
                        }
                    });
                    break;
                case MSG_PHYSICAL:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] result = bundle.getByteArray("status");
                            byte[] res = new byte[result.length - 4];
                            System.arraycopy(result, 4, res, 0, res.length);
                            if (res[0] == 01) {
                                Log.d("OBDEvent.PHYSICAL_STEP_ONE");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_ONE, res);
                            } else if (res[0] == 02) {
                                Log.d("OBDEvent.PHYSICAL_STEP_TWO");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_TWO, res);
                            } else if (res[0] == 03) {
                                Log.d("OBDEvent.PHYSICAL_STEP_THREE");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_THREE, res);
                            } else if (res[0] == 04) {
                                Log.d("OBDEvent.PHYSICAL_STEP_FOUR");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_FOUR, res);
                            } else if (res[0] == 05) {
                                Log.d("OBDEvent.PHYSICAL_STEP_FIVE");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_FIVE, res);
                            } else if (res[0] == 06) {
                                Log.d("OBDEvent.PHYSICAL_STEP_SEX");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_SEX, res);
                            } else if (res[0] == 07) {
                                Log.d("OBDEvent.PHYSICAL_STEP_SEVEN");
                                notifyBleCallBackListener(OBDEvent.PHYSICAL_STEP_SEVEN, res);
                            }
                        }
                    });
                    break;
                case MSG_FAULT_CODE:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] result = bundle.getByteArray("status");
                            byte[] res = new byte[result.length - 4];
                            System.arraycopy(result, 4, res, 0, res.length);
                            notifyBleCallBackListener(OBDEvent.FAULT_CODE, res);
                        }
                    });
                    break;
                case MSG_CLEAR_FAULT_CODE:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] result = bundle.getByteArray("status");
                            notifyBleCallBackListener(OBDEvent.CLEAN_FAULT_CODE, result[4] & 0xFF);
                        }
                    });
                    break;
                case MSG_SENSITIVE_CODE:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            byte[] result = bundle.getByteArray("status");
                            notifyBleCallBackListener(OBDEvent.SENSITIVE_CHANGE, result);
                        }
                    });
                    break;
                case MSG_COMMON_INFO:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyBleCallBackListener(OBDEvent.COMMON_INFO, null);
                        }
                    });
                    break;
                case MSG_HUD_STATUS_INFO:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.HUD_STATUS_INFO");
                            notifyBleCallBackListener(OBDEvent.HUD_STATUS_INFO, bundle.getSerializable("HUDStatus"));
                        }
                    });
                    break;
                case MSG_HUD_WARM_STATUS_INFO:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.HUD_WARM_STATUS_INFO");
                            notifyBleCallBackListener(OBDEvent.HUD_WARM_STATUS_INFO, bundle.getSerializable("HUDWarmStatus"));
                        }
                    });
                    break;
                case MSG_HUD_PARAMS_INFO:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.MSG_HUD_PARAMS_INFO");
                            notifyBleCallBackListener(OBDEvent.HUD_PARAMS_INFO, bundle.getSerializable("HUDParams"));
                        }
                    });
                    break;
                case MSG_FM_STATUS_INFO:
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("OBDEvent.MSG_FM_STATUS_INFO");
                            notifyBleCallBackListener(OBDEvent.FM_PARAMS_INFO, bundle.getSerializable("FMStatus"));
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }


    private class TimeOutThread extends Thread {

        private static final String TIMEOUTSYNC = "MTIMEOUTSYNC";

        private boolean needStop = false;

        private boolean waitForCommand = false;

        private boolean needRewire = false;

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
                            Log.d("TimeOutThread wait ");
                            TIMEOUTSYNC.wait(COMMAND_TIMEOUT);
                            if (needRewire) {
                                Log.d("TimeOutThread needRewire ");
                                if (currentRepeat > 2) {
                                    currentRepeat = 0;
                                    mMainHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Log.d("连续2次响应超时，断开连接！");
//                                            waitForCommand = false;
//                                            needRewire = false;
//                                            currentRepeat = 0;
                                            disconnect();
                                            notifyBleCallBackListener(OBDEvent.OBD_DISCONNECTED, null);
                                        }
                                    });
                                } else {
                                    Log.d("响应超时，重新发送！");
                                    if (mDataQueue != null) {
                                        mDataQueue.clear();
                                    }
                                    queue.add(currentProtocol);
                                    currentRepeat++;
                                }
                            } else {
                                canGo = true;
                                waitForCommand = false;
                                needRewire = false;
                                currentRepeat = 0;
                            }
                            Log.d("TimeOutThread notifyAll ");
                            TIMEOUTSYNC.notifyAll();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }

        public void startCommand(boolean reset) {
            if (!waitForCommand) {
                synchronized (TIMEOUTSYNC) {
                    Log.d("TimeOutThread startCommand ");
                    waitForCommand = true;
                    if (reset) {
                        needRewire = true;
                    }
                    TIMEOUTSYNC.notifyAll();
                }
            }
        }

        public void endCommand() {
            if (waitForCommand) {
                synchronized (TIMEOUTSYNC) {
                    Log.d("TimeOutThread endCommand ");
                    waitForCommand = false;
                    needRewire = false;
                    currentRepeat = 0;
                    TIMEOUTSYNC.notifyAll();
                }
            }
        }

        public void cancel() {
            Log.d("TimeOutThread cancel ");
            needStop = true;
            interrupt();
        }
    }
}