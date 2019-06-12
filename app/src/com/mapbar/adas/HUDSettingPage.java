package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.view.View;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.miyuan.obd.R;

import static com.mapbar.hamster.OBDEvent.HUD_PARAMS_INFO;

@PageSetting(contentViewId = R.layout.f2_layout, toHistory = false)
public class HUDSettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        BlueManager.getInstance().addBleCallBackListener(this);
    }


    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case HUD_PARAMS_INFO:
                break;
            default:
                break;
        }
    }

}
