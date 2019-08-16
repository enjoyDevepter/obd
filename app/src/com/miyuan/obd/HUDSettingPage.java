package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.HUDParams;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.obd.preferences.SettingPreferencesConfig;
import com.miyuan.obd.view.OnItemClickListener;
import com.miyuan.obd.view.SettingItem;

import static com.miyuan.hamster.OBDEvent.HUD_PARAMS_INFO;

@PageSetting(contentViewId = R.layout.hud_setting_layout, toHistory = false)
public class HUDSettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.light)
    private SettingItem lightST;
    @ViewInject(R.id.volume)
    private SettingItem volumeST;
    @ViewInject(R.id.tempWarm)
    private SettingItem tempWarmST;
    @ViewInject(R.id.overSpeed)
    private SettingItem overSpeedST;
    @ViewInject(R.id.speedCalibration)
    private SettingItem speedCalibrationST;
    @ViewInject(R.id.mileCalibration)
    private SettingItem mileCalibrationST;
    @ViewInject(R.id.driveTime)
    private SettingItem driveTimeST;
    @ViewInject(R.id.start0)
    private TextView start0TV;
    @ViewInject(R.id.start1)
    private TextView start1TV;
    @ViewInject(R.id.start2)
    private TextView start2TV;
    @ViewInject(R.id.sleep)
    private ViewGroup sleepVG;
    @ViewInject(R.id.sound)
    private ViewGroup soundVG;
    @ViewInject(R.id.highmode)
    private ViewGroup highModeVG;
    @ViewInject(R.id.naviMode)
    private ViewGroup naviModeVG;
    @ViewInject(R.id.camera)
    private TextView camearTV;
    @ViewInject(R.id.bicycle_lane)
    private TextView bicycleLaneTV;
    @ViewInject(R.id.surveillance_camera)
    private TextView surveillanceCameraTV;
    @ViewInject(R.id.illegal_photography)
    private TextView illegalPhotographyTV;
    @ViewInject(R.id.light_camera)
    private TextView lightCameraTV;
    @ViewInject(R.id.emergency)
    private TextView emergencyTV;
    @ViewInject(R.id.bus)
    private TextView busTV;

    private HUDParams params;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getHUDParams());
        back.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
        title.setText("参数设置");
        setListener();
    }

    private void setListener() {
        sleepVG.setOnClickListener(this);
        soundVG.setOnClickListener(this);
        highModeVG.setOnClickListener(this);
        naviModeVG.setOnClickListener(this);
        start0TV.setOnClickListener(this);
        start1TV.setOnClickListener(this);
        start2TV.setOnClickListener(this);
        lightST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x01, "自动".equals(value) ? 0 : Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x01, Integer.valueOf(value)));
            }
        });
        volumeST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x02, "静音".equals(value) ? 0 : Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x02, Integer.valueOf(value)));
            }
        });
        tempWarmST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x04, Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x04, Integer.valueOf(value)));
            }
        });
        overSpeedST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x09, Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x09, Integer.valueOf(value)));
            }
        });
        speedCalibrationST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x08, Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x08, Integer.valueOf(value)));
            }
        });
        mileCalibrationST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x0B, Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x0B, Integer.valueOf(value)));
            }
        });
        driveTimeST.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onLeftClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x03, Integer.valueOf(value)));
            }

            @Override
            public void onRightClick(String value) {
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x03, Integer.valueOf(value)));
            }
        });
        camearTV.setSelected(SettingPreferencesConfig.CAMERA_SPEED.get());
        camearTV.setTextColor(SettingPreferencesConfig.CAMERA_SPEED.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        camearTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.CAMERA_SPEED.set(!SettingPreferencesConfig.CAMERA_SPEED.get());
                camearTV.setSelected(SettingPreferencesConfig.CAMERA_SPEED.get());
                camearTV.setTextColor(SettingPreferencesConfig.CAMERA_SPEED.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
        bicycleLaneTV.setSelected(SettingPreferencesConfig.BICYCLE_LANE.get());
        bicycleLaneTV.setTextColor(SettingPreferencesConfig.BICYCLE_LANE.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        bicycleLaneTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.BICYCLE_LANE.set(!SettingPreferencesConfig.BICYCLE_LANE.get());
                bicycleLaneTV.setSelected(SettingPreferencesConfig.BICYCLE_LANE.get());
                bicycleLaneTV.setTextColor(SettingPreferencesConfig.BICYCLE_LANE.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
        surveillanceCameraTV.setSelected(SettingPreferencesConfig.SURVEILLANCE_CAMERA.get());
        surveillanceCameraTV.setTextColor(SettingPreferencesConfig.SURVEILLANCE_CAMERA.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        surveillanceCameraTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.SURVEILLANCE_CAMERA.set(!SettingPreferencesConfig.SURVEILLANCE_CAMERA.get());
                surveillanceCameraTV.setSelected(SettingPreferencesConfig.SURVEILLANCE_CAMERA.get());
                surveillanceCameraTV.setTextColor(SettingPreferencesConfig.SURVEILLANCE_CAMERA.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
        illegalPhotographyTV.setSelected(SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get());
        illegalPhotographyTV.setTextColor(SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        illegalPhotographyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.set(!SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get());
                illegalPhotographyTV.setSelected(SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get());
                illegalPhotographyTV.setTextColor(SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
        lightCameraTV.setSelected(SettingPreferencesConfig.LIGHT.get());
        lightCameraTV.setTextColor(SettingPreferencesConfig.LIGHT.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        lightCameraTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.LIGHT.set(!SettingPreferencesConfig.LIGHT.get());
                lightCameraTV.setSelected(SettingPreferencesConfig.LIGHT.get());
                lightCameraTV.setTextColor(SettingPreferencesConfig.LIGHT.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
        emergencyTV.setSelected(SettingPreferencesConfig.EMERGENCY.get());
        emergencyTV.setTextColor(SettingPreferencesConfig.EMERGENCY.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        emergencyTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.EMERGENCY.set(!SettingPreferencesConfig.EMERGENCY.get());
                emergencyTV.setSelected(SettingPreferencesConfig.EMERGENCY.get());
                emergencyTV.setTextColor(SettingPreferencesConfig.EMERGENCY.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
        busTV.setSelected(SettingPreferencesConfig.BUS.get());
        busTV.setTextColor(SettingPreferencesConfig.BUS.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
        busTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SettingPreferencesConfig.BUS.set(!SettingPreferencesConfig.BUS.get());
                busTV.setSelected(SettingPreferencesConfig.BUS.get());
                busTV.setTextColor(SettingPreferencesConfig.BUS.get() ? Color.parseColor("#FFFFFFFF") : Color.parseColor("#FFBCBCBC"));
            }
        });
    }


    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.start0:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x06, 00));
                break;
            case R.id.start1:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x06, 01));
                break;
            case R.id.start2:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x06, 02));
                break;
            case R.id.sleep:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x07, sleepVG.isSelected() ? 0 : 1));
                break;
            case R.id.sound:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x05, soundVG.isSelected() ? 0 : 1));
                break;
            case R.id.highmode:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x0A, highModeVG.isSelected() ? 0 : 1));
                break;
            case R.id.naviMode:
                BlueManager.getInstance().send(ProtocolUtils.setHUDParams(0x0C, naviModeVG.isSelected() ? 0 : 1));
                break;
            default:
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case HUD_PARAMS_INFO:
                params = (HUDParams) data;
                updateUI();
                break;
            default:
                break;
        }
    }

    private void updateUI() {
        lightST.updateContent(String.valueOf(params.getLight()));
        volumeST.updateContent(String.valueOf(params.getVolume()));
        tempWarmST.updateContent(String.valueOf(params.getTempWarm()));
        overSpeedST.updateContent(String.valueOf(params.getOverSpeed()));
        speedCalibrationST.updateContent(String.valueOf(params.getSpeedCalibration()));
        mileCalibrationST.updateContent(String.valueOf(params.getMileCalibration()));
        driveTimeST.updateContent(String.valueOf(params.getDriveTime()));

        updateNormalSwitch();

        switch (params.getStart()) {
            case 0:
                start0TV.setTextColor(Color.parseColor("#FFFFFFFF"));
                start0TV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
                start1TV.setTextColor(Color.parseColor("#FFBCBCBC"));
                start1TV.setBackgroundColor(Color.TRANSPARENT);
                start2TV.setTextColor(Color.parseColor("#FFBCBCBC"));
                start2TV.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 1:
                start1TV.setTextColor(Color.parseColor("#FFFFFFFF"));
                start1TV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
                start0TV.setTextColor(Color.parseColor("#FFBCBCBC"));
                start0TV.setBackgroundColor(Color.TRANSPARENT);
                start2TV.setTextColor(Color.parseColor("#FFBCBCBC"));
                start2TV.setBackgroundColor(Color.TRANSPARENT);
                break;
            case 2:
                start2TV.setTextColor(Color.parseColor("#FFFFFFFF"));
                start2TV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
                start1TV.setTextColor(Color.parseColor("#FFBCBCBC"));
                start1TV.setBackgroundColor(Color.TRANSPARENT);
                start0TV.setTextColor(Color.parseColor("#FFBCBCBC"));
                start0TV.setBackgroundColor(Color.TRANSPARENT);
                break;
            default:
                break;
        }
    }

    public void updateNormalSwitch() {
        soundVG.setSelected(params.isSound());
        highModeVG.setSelected(params.isHighMode());
        sleepVG.setSelected(params.isSleep());
        naviModeVG.setSelected(params.isNaviMode());
        TextView soundFirstTV = (TextView) soundVG.getChildAt(0);
        TextView soundSecondTV = (TextView) soundVG.getChildAt(1);
        TextView highModeFirstTV = (TextView) highModeVG.getChildAt(0);
        TextView highModeSecondTV = (TextView) highModeVG.getChildAt(1);
        TextView sleepFirstTV = (TextView) sleepVG.getChildAt(0);
        TextView sleepSecondTV = (TextView) sleepVG.getChildAt(1);
        TextView naviModeFirstTV = (TextView) naviModeVG.getChildAt(0);
        TextView naviModeSecondTV = (TextView) naviModeVG.getChildAt(1);
        if (soundVG.isSelected()) {
            soundFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            soundFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            soundSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            soundSecondTV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            soundSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            soundSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            soundFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            soundFirstTV.setBackgroundColor(Color.TRANSPARENT);
        }

        if (highModeVG.isSelected()) {
            highModeFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            highModeFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            highModeSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            highModeSecondTV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            highModeSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            highModeSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            highModeFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            highModeFirstTV.setBackgroundColor(Color.TRANSPARENT);
        }

        if (sleepVG.isSelected()) {
            sleepFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            sleepFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            sleepSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            sleepSecondTV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            sleepSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            sleepSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            sleepFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            sleepFirstTV.setBackgroundColor(Color.TRANSPARENT);
        }

        if (naviModeVG.isSelected()) {
            naviModeFirstTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            naviModeFirstTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            naviModeSecondTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            naviModeSecondTV.setBackgroundColor(Color.TRANSPARENT);
        } else {
            naviModeSecondTV.setTextColor(Color.parseColor("#FFFFFFFF"));
            naviModeSecondTV.setBackgroundColor(Color.parseColor("#FF35BDB2"));
            naviModeFirstTV.setTextColor(Color.parseColor("#FFBCBCBC"));
            naviModeFirstTV.setBackgroundColor(Color.TRANSPARENT);
        }
    }

}