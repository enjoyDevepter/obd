package com.mapbar.adas;

import com.mapbar.adas.download.AppInfoBean;
import com.mapbar.adas.download.UpdateAPPConstants;
import com.mapbar.adas.download.UpdateDownLoadManager;

/**
 * 更新功能初始化
 */
public class UpdateTask extends BaseTask {

    @Override
    public void excute() {
        UpdateDownLoadManager.getInstance().checkUpdate(new UpdateDownLoadManager.OnCheckUpdateListener() {
            @Override
            public void prepareUpdate(AppInfoBean appInfoBean) {
                UpdateAPPConstants.appInfoBean = appInfoBean;
            }

            @Override
            public void onError() {
            }
        });
        complate();
    }
}
