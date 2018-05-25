package com.mapbar.hamster.core;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.mapbar.hamster.OBDAidlInterface;
import com.mapbar.hamster.log.Log;

/**
 * Created by guomin on 2018/3/23.
 */

public class OBDService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new OBDBinder();
    }

    class OBDBinder extends OBDAidlInterface.Stub {

        @Override
        public void connect() throws RemoteException {
            DeviceManger.getInstance().connectDevice(getApplicationContext(), new DeviceManger.OBDListener() {
                @Override
                public void onEvent(int event, Object data) {
                    Log.d("event " + event);
                    switch (event) {
                        case OBDEvent.obdConnectSucc:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                }
                            }).start();
                            break;

                    }
                }
            });
        }

        @Override
        public void disconnect() throws RemoteException {
            DeviceManger.getInstance().disconnect();
        }
    }
}
