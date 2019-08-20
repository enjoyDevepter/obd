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

@PageSetting(contentViewId = R.layout.f5_layout)
public class F5SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.back)
    View backV;
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.params)
    TextView paramsV;
    @ViewInject(R.id.fly_multifunctional)
    View multifunctionalV;
    @ViewInject(R.id.fly_tire_bg)
    View fly_tire_bgV;
    @ViewInject(R.id.fly_tire)
    View tireV;

    @ViewInject(R.id.fly_rpm_bg)
    View fly_rpm_bgV;
    @ViewInject(R.id.fly_rpm)
    View fly_rpmV;

    @ViewInject(R.id.fly_speed_bg)
    View fly_speed_bgV;
    @ViewInject(R.id.fly_speed)
    View fly_speedV;

    @ViewInject(R.id.fly_oil_bg)
    View fly_oil_bgV;
    @ViewInject(R.id.fly_oil)
    View fly_oilV;

    @ViewInject(R.id.fly_warm_bg)
    View fly_warm_bgV;
    @ViewInject(R.id.warm)
    View warmV;
    @ViewInject(R.id.fault)
    View faultV;
    @ViewInject(R.id.temp)
    View temperatureV;
    @ViewInject(R.id.voltage)
    View voltageV;
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
        multifunctionalV.setOnClickListener(this);
        tireV.setOnClickListener(this);
        fly_rpmV.setOnClickListener(this);
        fly_oilV.setOnClickListener(this);
        fly_speedV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        temperatureV.setOnClickListener(this);
        voltageV.setOnClickListener(this);
        speedV.setOnClickListener(this);
        tiredV.setOnClickListener(this);
        warmV.setOnClickListener(this);
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
                fly_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                fly_speed_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                fly_rpm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                fly_oil_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                fly_warm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.multifunctional:
                showMultifunctional();
                break;
            case R.id.fly_tire:
                showNormalDailog(viewId, "胎压显示区", hudStatus.isTireShow());
                break;
            case R.id.fly_oil:
                showNormalDailog(viewId, "油耗显示区", hudStatus.isOilShow());
                break;
            case R.id.fly_speed:
                showNormalDailog(viewId, "车速显示区", hudStatus.isSpeedShow());
                break;
            case R.id.fly_rpm:
                showNormalDailog(viewId, "发动机转速显示区", hudStatus.isRpmShow());
                break;
            case R.id.warm:
            case R.id.fault:
            case R.id.temp:
            case R.id.voltage:
            case R.id.speed:
            case R.id.tired:
                showWarm();
                break;
            default:
                break;
        }
    }

    private void showNormalDailog(final int type, final String title, final boolean status) {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView titleTV = view.findViewById(R.id.title);
                        titleTV.setText(title);
                        View show = view.findViewById(R.id.show);
                        show.setSelected(status);
                        show.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (type) {
                                    case R.id.fly_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 01));
                                        break;
                                    case R.id.fly_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 01));
                                        break;
                                    case R.id.fly_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 01));
                                        break;
                                    case R.id.fly_speed:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x03, 01));
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });

                        View dismiss = view.findViewById(R.id.dismiss);
                        dismiss.setSelected(!status);
                        dismiss.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (type) {
                                    case R.id.fly_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 00));
                                        break;
                                    case R.id.fly_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 00));
                                        break;
                                    case R.id.fly_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 00));
                                        break;
                                    case R.id.fly_speed:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x03, 00));
                                        break;
                                    default:
                                        break;
                                }
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.hud_setting_noraml_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.hud_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.hud_normal_dailog_height))
                .show();
    }

    private void showMultifunctional() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View voltageV = view.findViewById(R.id.voltage);
                        final View speedV = view.findViewById(R.id.speed);
                        final View tempV = view.findViewById(R.id.temp);
                        final View oilV = view.findViewById(R.id.oil);
                        final View avgOilV = view.findViewById(R.id.avg_oil);
                        final View dismissV = view.findViewById(R.id.dismiss);
                        switch (hudStatus.getMultifunctionalOneType()) {
                            case 0x00: //隐藏
                                dismissV.setSelected(true);
                                break;
                            case 0x01: // 水温
                                tempV.setSelected(true);
                                break;
                            case 0x03: // 车速
                                speedV.setSelected(true);
                                break;
                            case 0x04: // 瞬时油耗
                                oilV.setSelected(true);
                                break;
                            case 0x05: // 平均油耗
                                avgOilV.setSelected(true);
                                break;
                            case 0x08: // 电压
                                voltageV.setSelected(true);
                                break;
                            default:
                                break;
                        }
                        voltageV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(true);
                                speedV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                avgOilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x08));
                                dialog.dismiss();
                            }
                        });

                        speedV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                speedV.setSelected(true);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                avgOilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x03));
                                dialog.dismiss();
                            }
                        });
                        tempV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                speedV.setSelected(false);
                                tempV.setSelected(true);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                avgOilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x01));
                                dialog.dismiss();
                            }
                        });
                        dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                speedV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(true);
                                oilV.setSelected(false);
                                avgOilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 00));
                                dialog.dismiss();
                            }
                        });
                        oilV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                speedV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(true);
                                avgOilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 00));
                                dialog.dismiss();
                            }
                        });
                        avgOilV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                speedV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                avgOilV.setSelected(true);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 00));
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.m4_setting_multifunctional_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.m4_ultifunctional_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.m4_ultifunctional_dailog_height))
                .show();
    }

    private void showWarm() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {

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

                        final View oil_showV = view.findViewById(R.id.oil_show);
                        final View oil_dismissV = view.findViewById(R.id.oil_dismiss);
                        oil_showV.setSelected(hudWarmStatus.isOilWarmShow());
                        oil_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 1));
                                oil_showV.setSelected(true);
                                oil_dismissV.setSelected(false);
                            }
                        });

                        oil_dismissV.setSelected(!hudWarmStatus.isOilWarmShow());
                        oil_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 0));
                                oil_showV.setSelected(false);
                                oil_dismissV.setSelected(true);
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.m4_setting_warm_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setCancelOutside(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.m4_warm_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.m4_warm_dailog_height))
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
        if (null != hudStatus) {
            switch (hudStatus.getMultifunctionalOneType()) {
                case 0x00:
                    multifunctionalV.setBackgroundResource(R.drawable.fly_multifunctional_dismiss);
                    break;
                case 0x01:
                    multifunctionalV.setBackgroundResource(R.drawable.fly_multifunctional_temp_show);
                    break;
                case 0x03:
                    multifunctionalV.setBackgroundResource(R.drawable.fly_multifunctional_speed_show);
                    break;
                case 0x04:
                    multifunctionalV.setBackgroundResource(R.drawable.fly_multifunctional_oil_show);
                    break;
                case 0x05:
                    multifunctionalV.setBackgroundResource(R.drawable.fly_multifunctional_avg_oil_show);
                    break;
                case 0x08:
                    multifunctionalV.setBackgroundResource(R.drawable.fly_multifunctional_voltage_show);
                    break;
                default:
                    break;
            }

            fly_oilV.setBackgroundResource(hudStatus.isOilShow() ? R.drawable.fly_oil_show : R.drawable.fly_oil_dismiss);
            tireV.setBackgroundResource(hudStatus.isTireShow() ? R.drawable.f2_tire_show : R.drawable.f2_tire_dismiss);
            fly_rpmV.setBackgroundResource(hudStatus.isRpmShow() ? R.drawable.fly_rpm_show : R.drawable.fly_rpm_dismiss);
            fly_speedV.setBackgroundResource(hudStatus.isSpeedShow() ? R.drawable.fly_speed_show : R.drawable.fly_speed_dismiss);

        }
        if (null != hudWarmStatus) {
            faultV.setBackgroundResource(hudWarmStatus.isFaultWarmShow() ? R.drawable.fault_show : R.drawable.fault_dismiss);
            voltageV.setBackgroundResource(hudWarmStatus.isVoltageWarmShow() ? R.drawable.voltage_show : R.drawable.voltage_dismiss);
            temperatureV.setBackgroundResource(hudWarmStatus.isTemperatureWarmShow() ? R.drawable.temperature_show : R.drawable.temperature_dismiss);
            tiredV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.tired_show : R.drawable.tired_dismiss);
            speedV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.speed_show : R.drawable.speed_dismiss);
        }
    }

}
