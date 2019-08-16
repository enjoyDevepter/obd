package com.miyuan.obd;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.log.MapbarStorageUtil;
import com.miyuan.obd.log.SdcardUtil;

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
