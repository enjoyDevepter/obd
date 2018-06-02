package com.mapbar.adas;

import com.mapbar.adas.log.MapbarStorageUtil;
import com.mapbar.adas.log.SdcardUtil;

/**
 * SD初始化
 */
public class SDInitTask extends BaseTask {

    @Override
    public void excute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MapbarStorageUtil.listenForStorageRefresh();
                SdcardUtil.initInstance(GlobalUtil.getContext());
            }
        }).start();
        complate();
    }
}
