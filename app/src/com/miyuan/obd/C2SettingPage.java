package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.HUDStatus;
import com.miyuan.hamster.HUDWarmStatus;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;

import static com.miyuan.hamster.OBDEvent.HUD_STATUS_INFO;
import static com.miyuan.hamster.OBDEvent.HUD_WARM_STATUS_INFO;

@PageSetting(contentViewId = R.layout.c2_layout)
public class C2SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.back)
    View backV;
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.params)
    TextView paramsV;

    @ViewInject(R.id.tire)
    View tireV;
    @ViewInject(R.id.warm_layout)
    View warm_layoutV;
    @ViewInject(R.id.warm_bg)
    View warmBgV;
    @ViewInject(R.id.fault)
    View faultV;
    @ViewInject(R.id.temperature)
    View temperatureV;
    @ViewInject(R.id.voltage)
    View voltageV;
    @ViewInject(R.id.oil)
    View oilV;
    @ViewInject(R.id.speed)
    View speedV;
    @ViewInject(R.id.tired)
    View tiredV;
    CustomDialog dialog = null;

    private HUDStatus hudStatus;
    private HUDWarmStatus hudWarmStatus;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getHUDStatus());
        BlueManager.getInstance().send(ProtocolUtils.getHUDWarmStatus());
        backV.setOnClickListener(this);
        settingV.setOnClickListener(this);
        paramsV.setOnClickListener(this);
        warm_layoutV.setOnClickListener(this);
    }


    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        boolean choice = settingV.isSelected();
        switch (v.getId()) {
            case R.id.setting:
                if (choice) {
                    settingV.setText("界面");
                } else {
                    settingV.setText("完成");
                }
                settingV.setSelected(!choice);
                warmBgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                break;
            case R.id.params:
                PageManager.go(new HUDSettingPage());
                break;
            case R.id.back:
                PageManager.back();
                break;
            default:
                if (choice) {
                    showSetting(v.getId());
                }
                break;
        }
    }

    private void showSetting(int viewId) {
        if (null == hudStatus || null == hudWarmStatus) {
            return;
        }
        switch (viewId) {
            case R.id.warm_layout:
                showWarm();
                break;
            default:
                break;
        }
    }


    private void showWarm() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View fault_showV = view.findViewById(R.id.fault_show);
                        final View fault_dismissV = view.findViewById(R.id.fault_dismiss);
                        fault_showV.setSelected(hudWarmStatus.isFaultWarmShow());
                        fault_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x07, 1));
                                fault_showV.setSelected(true);
                                fault_dismissV.setSelected(false);
                            }
                        });

                        fault_dismissV.setSelected(!hudWarmStatus.isFaultWarmShow());
                        fault_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x07, 0));
                                fault_showV.setSelected(false);
                                fault_dismissV.setSelected(true);
                            }
                        });

                        final View voltage_showV = view.findViewById(R.id.voltage_show);
                        final View voltage_dismissV = view.findViewById(R.id.voltage_dismiss);
                        voltage_showV.setSelected(hudWarmStatus.isVoltageWarmShow());
                        voltage_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x02, 1));
                                voltage_showV.setSelected(true);
                                voltage_dismissV.setSelected(false);
                            }
                        });

                        voltage_dismissV.setSelected(!hudWarmStatus.isVoltageWarmShow());
                        voltage_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x02, 0));
                                voltage_showV.setSelected(false);
                                voltage_dismissV.setSelected(true);
                            }
                        });

                        final View temp_showV = view.findViewById(R.id.temp_show);
                        final View temp_dismissV = view.findViewById(R.id.temp_dismiss);
                        temp_showV.setSelected(hudWarmStatus.isTemperatureWarmShow());
                        temp_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x01, 1));
                                temp_showV.setSelected(true);
                                temp_dismissV.setSelected(false);
                            }
                        });

                        temp_dismissV.setSelected(!hudWarmStatus.isTemperatureWarmShow());
                        temp_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x01, 0));
                                temp_showV.setSelected(false);
                                temp_dismissV.setSelected(true);
                            }
                        });

                        final View tried_showV = view.findViewById(R.id.tried_show);
                        final View tried_dismissV = view.findViewById(R.id.tried_dismiss);
                        tried_showV.setSelected(hudWarmStatus.isTiredWarmShow());
                        tried_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x06, 1));
                                tried_showV.setSelected(true);
                                tried_dismissV.setSelected(false);
                            }
                        });

                        tried_dismissV.setSelected(!hudWarmStatus.isTiredWarmShow());
                        tried_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x06, 0));
                                tried_showV.setSelected(false);
                                tried_dismissV.setSelected(true);
                            }
                        });

                        final View tire_showV = view.findViewById(R.id.tire_show);
                        final View tire_dismissV = view.findViewById(R.id.tire_dismiss);
                        tire_showV.setSelected(hudWarmStatus.isTrieWarmShow());
                        tire_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x05, 1));
                                tire_showV.setSelected(true);
                                tire_dismissV.setSelected(false);
                            }
                        });

                        tire_dismissV.setSelected(!hudWarmStatus.isTrieWarmShow());
                        tire_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x05, 0));
                                tire_showV.setSelected(false);
                                tire_dismissV.setSelected(true);
                            }
                        });

                        final View speed_showV = view.findViewById(R.id.speed_show);
                        final View speed_dismissV = view.findViewById(R.id.speed_dismiss);
                        speed_showV.setSelected(hudWarmStatus.isSpeedWarmShow());
                        speed_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x04, 1));
                                speed_showV.setSelected(true);
                                speed_dismissV.setSelected(false);
                            }
                        });

                        speed_dismissV.setSelected(!hudWarmStatus.isSpeedWarmShow());
                        speed_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x04, 0));
                                speed_showV.setSelected(false);
                                speed_dismissV.setSelected(true);
                            }
                        });


                        final View remainder_showV = view.findViewById(R.id.remainder_show);
                        final View remainder_dismissV = view.findViewById(R.id.remainder_dismiss);
                        remainder_showV.setSelected(hudWarmStatus.isOilWarmShow());
                        remainder_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 1));
                                remainder_showV.setSelected(true);
                                remainder_dismissV.setSelected(false);
                            }
                        });

                        remainder_dismissV.setSelected(!hudWarmStatus.isOilWarmShow());
                        remainder_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 0));
                                remainder_showV.setSelected(false);
                                remainder_dismissV.setSelected(true);
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.ff_setting_warm_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setCancelOutside(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.hud_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.ff_warm_dailog_height))
                .show();
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case HUD_STATUS_INFO:
                hudStatus = (HUDStatus) data;
                break;
            case HUD_WARM_STATUS_INFO:
                hudWarmStatus = (HUDWarmStatus) data;
                break;
            default:
                break;
        }
        updateUI();
    }

    private void updateUI() {
        if (null != hudWarmStatus) {
            faultV.setBackgroundResource(hudWarmStatus.isFaultWarmShow() ? R.drawable.c2_fault_show : R.drawable.c2_fault_dismiss);
            voltageV.setBackgroundResource(hudWarmStatus.isVoltageWarmShow() ? R.drawable.c2_voltage_show : R.drawable.c2_voltage_dismiss);
            speedV.setBackgroundResource(hudWarmStatus.isSpeedWarmShow() ? R.drawable.c2_speed_show : R.drawable.c2_speed_dismiss);
            tiredV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.c2_tired_show : R.drawable.c2_tired_dismiss);
            tireV.setBackgroundResource(hudWarmStatus.isTrieWarmShow() ? R.drawable.c2_tire_show : R.drawable.c2_tire_dismiss);
            temperatureV.setBackgroundResource(hudWarmStatus.isTemperatureWarmShow() ? R.drawable.c2_temperature_show : R.drawable.c2_temperature_dismiss);
            oilV.setBackgroundResource(hudWarmStatus.isOilWarmShow() ? R.drawable.c2_oil_show : R.drawable.c2_oil_dismiss);
        }
    }
}
