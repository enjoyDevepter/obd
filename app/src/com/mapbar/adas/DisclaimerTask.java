package com.mapbar.adas;


import com.mapbar.hamster.BlueManager;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.DISCALIMER_VISIBLE;

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
        boolean isFistSatrt = DISCALIMER_VISIBLE.get();
        if (!isFistSatrt) {
            PageManager.go(new HomePage());
        } else {
            if (BlueManager.getInstance().isConnected()) {
                PageManager.go(new HomePage());
            } else {
                BlueManager.getInstance().stopScan(false);
                PageManager.go(new HomePage());
            }
        }
        complate();
    }
}
