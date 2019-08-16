package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.PressureInfo;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.obd.view.Rotate3dAnimation;
import com.miyuan.obd.view.TextViewFontLcdEx;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.hud_layout)
public class HUDPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.parent)
    View parentV;
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
    TextView mirrorTV;
    @ViewInject(R.id.trie)
    View trieV;
    @ViewInject(R.id.trie_info)
    TextView trieTV;
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
        mirrorTV.setOnClickListener(this);
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
                float centerX = parentV.getWidth() / 2f;
                float centerY = parentV.getHeight() / 2f;
                Rotate3dAnimation rotateAnimation = null;
                if ("镜像".equals(mirrorTV.getText().toString())) {
                    mirrorTV.setText("正面");
                    rotateAnimation = new Rotate3dAnimation(0, 180, centerX, centerY, 0.0f, false);
                } else {
                    mirrorTV.setText("镜像");
                    rotateAnimation = new Rotate3dAnimation(180, 0, centerX, centerY, 0.0f, false);
                }
                rotateAnimation.setDuration(0);
                rotateAnimation.setInterpolator(new AccelerateInterpolator());
                rotateAnimation.setFillAfter(true);
                parentV.startAnimation(rotateAnimation);
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                PressureInfo pressureInfo = (PressureInfo) data;

                trieV.setBackgroundResource(pressureInfo.getStatus() == 0 ? R.drawable.trie_nor : R.drawable.trie_error);
                trieTV.setTextColor(pressureInfo.getStatus() == 0 ? Color.parseColor("#ff00fff6") : Color.parseColor("#fff7a65a"));
                trieTV.setText(pressureInfo.getStatus() == 0 ? "胎压正常" : "胎压异常");
                speedTVFL.setTextFormat000(pressureInfo.getSpeed());

                voltageTV.setTextFormat00dot0(Float.valueOf(pressureInfo.getVoltage()));

                consumptionTV.setTextFormat00dot0((float) pressureInfo.getOilConsumption());

                temperatureTV.setTextFormat000(pressureInfo.getTemperature());

                break;
        }

    }
}
