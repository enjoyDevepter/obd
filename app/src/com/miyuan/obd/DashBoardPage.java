package com.miyuan.obd;

import android.animation.ObjectAnimator;
import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.PressureInfo;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.obd.view.TextViewFontLcdEx;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.dash_board_layout)
public class DashBoardPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.iv_physical_icon)
    View faultV;
    @ViewInject(R.id.tv_check_result)
    TextView faultTV;
    @ViewInject(R.id.tv_voltage)
    TextView voltageTV;
    @ViewInject(R.id.tv_engineCoolantTemperature)
    TextView temperatureTV;
    @ViewInject(R.id.tve_rpm)
    TextViewFontLcdEx rpmTVFL;
    @ViewInject(R.id.tve_speed)
    TextViewFontLcdEx speedTVFL;
    @ViewInject(R.id.tv_shunshi_consumption)
    TextViewFontLcdEx oilConsumptionTVFLE;
    @ViewInject(R.id.tv_cost2)
    TextViewFontLcdEx consumptionTVFLE;
    @ViewInject(R.id.surplusOil)
    TextViewFontLcdEx surplusOilTVFLE;
    @ViewInject(R.id.dazhen)
    View dazhenV;
    @ViewInject(R.id.xiaozhen)
    View xiaozhenV;
    @ViewInject(R.id.bt_stop)
    View stopV;
    private float preSpeedRotation = 0;
    private float rmp1Rotation = 0;
    private Timer heartTimer;


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        BlueManager.getInstance().addBleCallBackListener(this);
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000 * 30, 1000 * 60);
        BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
        stopV.setOnClickListener(this);
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
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                PressureInfo pressureInfo = (PressureInfo) data;
                parseStatus(pressureInfo);
                break;
        }

    }

    private void parseStatus(PressureInfo pressureInfo) {

        if (pressureInfo.getFaultCount() > 0) {
            faultV.setBackgroundResource(R.drawable.fault_code_error);
            faultTV.setText(String.valueOf(pressureInfo.getFaultCount()));
        }
        voltageTV.setText(String.valueOf(pressureInfo.getVoltage()) + "v");
        temperatureTV.setText(String.valueOf(pressureInfo.getTemperature()) + "℃");
        rpmTVFL.setTextFormat000(pressureInfo.getRotationRate());
        speedTVFL.setTextFormat000(pressureInfo.getSpeed());
        oilConsumptionTVFLE.setTextFormat00dot0((float) pressureInfo.getOilConsumption());
        consumptionTVFLE.setTextFormat00dot0((float) pressureInfo.getConsumption());
        surplusOilTVFLE.setTextFormat000(pressureInfo.getSurplusOil());
        // 速度指针
        float speed = pressureInfo.getSpeed();
        float speedRotation = 0.0f;

        if (speed > 120) {
            speedRotation = ((speed - 120) * 1.0f);
        } else if (speed < 120) {
            speedRotation = (speed - 120) * 1.0f;
        } else if (speed == 0) {
            speedRotation = -120;
        }
        ObjectAnimator animationBigPointer = ObjectAnimator.ofFloat(dazhenV, "rotation", preSpeedRotation, speedRotation);
        animationBigPointer.start();
        preSpeedRotation = speedRotation;

        // 转速指针
        float rmp = pressureInfo.getRotationRate();
        float rmpRotation = 0.0f;
        if (rmp >= 8000) {
            rmpRotation = 120;
        } else if (rmp < 8000) {
            rmpRotation = rmp * 0.015f;
        }

        ObjectAnimator animationSmallPointer = ObjectAnimator.ofFloat(xiaozhenV, "rotation", rmp1Rotation, rmpRotation);
        animationSmallPointer.start();
        rmp1Rotation = rmpRotation;
    }
}
