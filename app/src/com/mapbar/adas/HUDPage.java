package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.view.View;

import com.gyf.barlibrary.ImmersionBar;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.view.TextViewFontLcdEx;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.PressureInfo;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.hud_layout)
public class HUDPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.bt_stop)
    View stopV;
    @ViewInject(R.id.speed)
    TextViewFontLcdEx speedTVFL;
    @ViewInject(R.id.voltage)
    TextViewFontLcdEx voltageTV;
    @ViewInject(R.id.temperature)
    TextViewFontLcdEx temperatureTV;
    @ViewInject(R.id.consumption)
    TextViewFontLcdEx consumptionTV;
    @ViewInject(R.id.mirror)
    View mirrorV;
    @ViewInject(R.id.trie)
    View trieV;
    private Timer heartTimer;


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(R.color.hud_title_color)
                .init(); //初始化，默认透明状态栏和黑色导航栏
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000 * 30, 1000 * 60);
        stopV.setOnClickListener(this);
        mirrorV.setOnClickListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().send(ProtocolUtils.stopGetNewTirePressureStatus());
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_stop:
                PageManager.back();
                break;
            case R.id.mirror:
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                PressureInfo pressureInfo = (PressureInfo) data;

                trieV.setBackgroundResource(pressureInfo.getStatus() == 0 ? R.drawable.trie_nor : R.drawable.trie_error);

                speedTVFL.setTextFormat000(pressureInfo.getSpeed());

                voltageTV.setTextFormat00dot0(Float.valueOf(pressureInfo.getVoltage()));

                consumptionTV.setTextFormat00dot0((float) pressureInfo.getOilConsumption());

                temperatureTV.setTextFormat000(pressureInfo.getTemperature());

                break;
        }

    }
}
