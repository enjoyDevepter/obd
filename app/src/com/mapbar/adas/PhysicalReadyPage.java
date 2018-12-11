package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.PressureInfo;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;


@PageSetting(contentViewId = R.layout.physical_ready_layout)
public class PhysicalReadyPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.back)
    private View backV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.info)
    private TextView infoTV;
    @ViewInject(R.id.confirm)
    private View confirmV;
    @ViewInject(R.id.idle)
    private View idleV;
    @ViewInject(R.id.temp)
    private TextView tempTV;
    @ViewInject(R.id.tempTag)
    private View tempTagV;
    private Timer heartTimer;


    @Override
    public void onResume() {
        super.onResume();
        ImmersionBar.with(GlobalUtil.getMainActivity())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        titleTV.setText("准备体检");
        backV.setOnClickListener(this);
        confirmV.setEnabled(false);
        reportV.setVisibility(View.INVISIBLE);
        infoTV.setText(Html.fromHtml("<font color='#4A4A4A'>体检项目包括:</font><br><font color='#FD0505'>七大</font><font color='#4A4A4A'>系统，至多</font><font color='#FD0505'>121项</font><font color='#4A4A4A'>数据流</font><br><br>"));
        BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000 * 30, 1000 * 60);
    }


    @Override
    public void onStart() {
        BlueManager.getInstance().addBleCallBackListener(this);
        super.onStart();
    }


    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                PageManager.go(new PhysicalPage());
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                PressureInfo pressureInfo = (PressureInfo) data;
                tempTV.setText(Html.fromHtml("<font color='#4A4A4A'>2、水温在80℃以上！（当前</font><font color='#FD0505'>" + pressureInfo.getTemperature() + "</font><font color='#4A4A4A'>℃）</font>"));

                idleV.setBackgroundResource(pressureInfo.getSpeed() <= 0 ? R.drawable.ready : R.drawable.unready);
                tempTagV.setBackgroundResource(pressureInfo.getTemperature() >= 80 ? R.drawable.ready : R.drawable.unready);
                if (pressureInfo.getTemperature() >= 80 && pressureInfo.getSpeed() == 0) {
                    confirmV.setEnabled(true);
                    confirmV.setOnClickListener(this);
                } else {
                    confirmV.setEnabled(false);
                }
                break;
        }
    }
}
