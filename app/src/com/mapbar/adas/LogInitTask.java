package com.mapbar.adas;

/**
 * 日志初始化
 */
public class LogInitTask extends BaseTask {

    @Override
    public void excute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                new GlobalConfig().init();
            }
        }).start();
        complate();
    }
}
