package com.mapbar.hamster.core;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import com.mapbar.hamster.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;


/**
 * Created by guomin on 2018/3/8.
 */
class BluetoothManager {

    protected static final int CONNECTION_STATE_CONNECTED = 3;
    private static final long OBD_COMMAND_TIMEOUT = 30000;
    private static final String MSYNCSOCKET = "MSYNCSOCKET";
    private static final String SPP_UUID = "00001101-0000-1000-8000-00805F9B34FB";
    private static final int CONNECTION_STATE_DISCONNECTED = 0;
    private static final int CONNECTION_STATE_WAIT_START = 1;
    private static final int CONNECTION_STATE_CONNECTING = 2;
    private BluetoothAdapter mBluetoothAdapter;
    private BroadcastReceiver mBroadcastReceiver;
    private BluetoothDevice mBluetoothDevice = null;
    private BluetoothSocket mBluetoothSocket = null;
    private InputStream mInputStream = null;
    private OutputStream mOutputStream = null;
    private ArrayList<String> mFoundedDeviceMacs = null;
    private Context mContext;
    private volatile int mConnectionState = CONNECTION_STATE_DISCONNECTED;
    private TimeoutThread mTimeoutThread = null;


    private BluetoothManager() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public static BluetoothManager getInstance() {
        return BluetoothManager.InstanceHolder.INSTANCE;
    }

    void init(Context context) {
        mContext = context;
        mFoundedDeviceMacs = new ArrayList<>();

        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Toast.makeText(context, "Device does not support Bluetooth", Toast.LENGTH_LONG);
        }

        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }

        mTimeoutThread = new TimeoutThread();
        mTimeoutThread.setName("OBDTimeoutThread");
        mTimeoutThread.setDaemon(true);
        mTimeoutThread.start();

        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {//蓝牙扫描过程开始
                    Log.d("OBDEvent  " + OBDEvent.scanStatred);
                } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { //蓝牙扫描时，扫描到任一远程蓝牙设备时，会发送此广播
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    Log.d("OBDEvent  " + OBDEvent.found);
                    if (device.getName() != null && (device.getName().startsWith("Mapbar") || device.getName().startsWith("mapbar"))) {
                        mBluetoothAdapter.cancelDiscovery();
                        setConnectionState(CONNECTION_STATE_WAIT_START);
                        connectDevice(device.getAddress());
                    }
                    if (device != null && !mFoundedDeviceMacs.contains(device.getAddress())) {
                        mFoundedDeviceMacs.add(device.getAddress());
                    }
                } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {  //蓝牙扫描过程结束
                    if (mBluetoothAdapter.isDiscovering()) {
                        mBluetoothAdapter.cancelDiscovery();
                    }
                    Log.d("OBDEvent  " + OBDEvent.scanFinished);
                } else if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {//蓝牙状态值发生改变
                } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {//这个广播不表示配对成功或连接成功，它是在两个蓝牙设备建立RFCOMM通道时，就会发出这个广播
                } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { //指明一个来自于远程设备的低级别（ACL）连接的断开
                }
            }
        };

        // Register for broadcasts when discovery has started
        IntentFilter filter = new IntentFilter(BluetoothAdapter
                .ACTION_DISCOVERY_STARTED);
        context.registerReceiver(mBroadcastReceiver, filter);

        // Register for broadcasts when a device is discovered
        filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(mBroadcastReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        context.registerReceiver(mBroadcastReceiver, filter);

        // Register for broadcasts when the state of bluetooth has changed
        filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(mBroadcastReceiver, filter);

        // Register for broadcasts when the connection state of bluetooth has
        // changed
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        context.registerReceiver(mBroadcastReceiver, filter);

        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        context.registerReceiver(mBroadcastReceiver, filter);

        mBluetoothAdapter.startDiscovery();
    }

    /**
     * @param address
     */
    private void connectDevice(String address) {
        mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);

        if (null == mBluetoothDevice) {
            mBluetoothAdapter.startDiscovery();
            return;
        }

        // 开启线程链接
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d("OBDEvent  " + OBDEvent.obdConnecting);
                    setConnectionState(CONNECTION_STATE_CONNECTING);
                    // Android SDK: 2.3.3+
                    if (Build.VERSION.SDK_INT >= 10) {
                        mBluetoothSocket = mBluetoothDevice
                                .createInsecureRfcommSocketToServiceRecord(UUID
                                        .fromString(SPP_UUID));
                    } else {
                        mBluetoothSocket = mBluetoothDevice
                                .createRfcommSocketToServiceRecord(UUID
                                        .fromString(SPP_UUID));
                    }

                    if (null != mBluetoothSocket) {
                        mBluetoothSocket.connect();
                        mInputStream = mBluetoothSocket.getInputStream();
                        mOutputStream = mBluetoothSocket.getOutputStream();
                        setConnectionState(CONNECTION_STATE_CONNECTED);
                        Log.d("OBDEvent  " + OBDEvent.obdConnectSucc);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("OBDEvent  " + OBDEvent.obdConnectFailed);
                    setConnectionState(CONNECTION_STATE_DISCONNECTED);
                }
            }
        }, "BluetoothSocket").start();

    }

    /**
     * 断开链接
     */

    void disconnectDevice() {

        setConnectionState(CONNECTION_STATE_DISCONNECTED);
        if (mContext != null) {
            mContext.unregisterReceiver(mBroadcastReceiver);
        }

        if (null != mOutputStream) {
            try {
                mOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mOutputStream = null;
            }
        }

        if (null != mInputStream) {
            try {
                mInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mInputStream = null;
            }
        }

        if (null != mBluetoothSocket) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                mBluetoothSocket = null;
            }
        }
    }

    /**
     * 同步读写蓝牙socket
     *
     * @param msg
     * @param limit
     * @return String
     */
    public synchronized String sendAndReceiveData(String msg, byte limit) {
        String r = null;
        Log.d("XXXXXXXXX " + msg);
        try {
            if (getConnectionState() == CONNECTION_STATE_CONNECTED) {
                StringBuilder result = new StringBuilder();

//                mTimeoutThread.startCommand();

                byte c = 0;

                //清空串口缓冲区数据，仅在android 4.3以后可用
                if (!TextUtils.isEmpty(msg)) {
                    clearBuffer(mInputStream);
                    mOutputStream.write(msg.getBytes());
                    mOutputStream.flush();
                }

                if (limit != (byte) 0) {
                    int rd = mInputStream.read();
                    while (rd < 1 && !msg.contains("ATBOOT")) {
                        //TODO 当没有数据的时候,执行到读到数据为止
                        rd = mInputStream.read();
                    }
                    c = (byte) rd;
                    while (rd > 0 && c != limit) {
                        result.append((char) c);
                        rd = mInputStream.read();
                        c = (byte) rd;
                    }
                    r = result.toString();
                } else {
                    r = "";
                }
                Log.d("XXXXXXXXX result " + r);

//                mTimeoutThread.endCommand();

                return r;
            } else {
                if (getConnectionState() != CONNECTION_STATE_CONNECTED) {
                    return r;
                }
            }
        } catch (Exception e) {
            synchronized (MSYNCSOCKET) {
                disconnectDevice();
                mBluetoothDevice = null;
//                mTimeoutThread.endCommand();
            }
        }

        if (getConnectionState() != CONNECTION_STATE_DISCONNECTED) {
            setConnectionState(CONNECTION_STATE_DISCONNECTED);
            Log.d("OBDEvent  " + OBDEvent.disconnected);
            // Otherwise means the connection has been invalid.
        }
        return r;
    }

    protected int getConnectionState() {
        return mConnectionState;
    }

    private void setConnectionState(int state) {
        mConnectionState = state;
    }

    /**
     * 清理当前串口缓冲区的内容
     *
     * @param inputStream
     */
    private void clearBuffer(InputStream inputStream) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }
        try {
            int len = inputStream.available();
            int cur = 0;
            while (cur < len) {
                if (mInputStream.read() == -1) {
                    break;
                }
                cur++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class InstanceHolder {
        private static final BluetoothManager INSTANCE = new BluetoothManager();
    }

    private static class TimeoutThread extends Thread {

        private static final String MTIMEOUTSYNC = "MTIMEOUTSYNC";
        public boolean mNeedCloseSocket = false;
        private boolean mNeedStop = false;
        private boolean mWaitForCommand = false;

        @Override
        public synchronized void start() {
            mNeedCloseSocket = true;
            mNeedStop = false;
            super.start();
        }

        @Override
        public void run() {
            super.run();
            while (!mNeedStop) {
                synchronized (MTIMEOUTSYNC) {
                    try {
                        // Do we should stop the thread?
                        if (mNeedStop) {
                            return;
                        }

                        if (mWaitForCommand) {
                            MTIMEOUTSYNC.wait(OBD_COMMAND_TIMEOUT);
                            if (mNeedCloseSocket) {
                                // Timeout, should disconnect the connection.
                            }
//                            BluetoothManager.getInstance()
//                                    .disconnectDeviceInMainThread();
                        }
                        MTIMEOUTSYNC.notifyAll();
                        MTIMEOUTSYNC.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public void startCommand() {
            synchronized (MTIMEOUTSYNC) {
                mWaitForCommand = true;
                mNeedCloseSocket = true;
                MTIMEOUTSYNC.notifyAll();
            }
        }

        public void endCommand() {
            synchronized (MTIMEOUTSYNC) {
                mWaitForCommand = false;
                mNeedCloseSocket = false;
                MTIMEOUTSYNC.notifyAll();
                try {
                    MTIMEOUTSYNC.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void cancel() {
            mNeedCloseSocket = false;
            mNeedStop = true;
            interrupt();
        }
    }

}