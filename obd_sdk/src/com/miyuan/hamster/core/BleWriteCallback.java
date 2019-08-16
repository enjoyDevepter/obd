package com.miyuan.hamster.core;


public interface BleWriteCallback {

    void onWriteSuccess(byte[] justWrite);

    void onWriteFailure(byte[] bytes);

}
