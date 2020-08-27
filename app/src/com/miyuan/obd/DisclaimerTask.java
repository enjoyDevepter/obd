package com.miyuan.obd;


import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.hamster.BlueManager;
import com.miyuan.obd.preferences.SettingPreferencesConfig;

/**
 * 免责声明
 */
public class DisclaimerTask extends BaseTask {

    @Override
    public void excute() {

        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                ((MainActivity) GlobalUtil.getMainActivity()).hideSplash();
            }
        });
        // 第一次启动的时候
        boolean isFistSatrt = SettingPreferencesConfig.DISCALIMER_VISIBLE.get();
        if (!isFistSatrt) {
            PageManager.go(new DisclaimerPage());
        } else {
            if (BlueManager.getInstance().isConnected()) {
                PageManager.go(new OBDAuthPage());
            } else {
                BlueManager.getInstance().stopScan(false);
                PageManager.go(new ConnectPage());
            }
        }
        complate();
    }
}
