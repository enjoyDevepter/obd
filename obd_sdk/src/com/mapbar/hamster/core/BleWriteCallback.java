package com.mapbar.hamster.core;


public interface BleWriteCallback {

    void onWriteSuccess(byte[] justWrite);

    void onWriteFailure();

}
