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

@PageSetting(contentViewId = R.layout.p4_layout)
public class P4SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.back)
    View backV;
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.params)
    TextView paramsV;
    @ViewInject(R.id.f3_multifunctional_bg)
    View f3_multifunctional_bgV;
    @ViewInject(R.id.f3_multifunctional)
    View f3_multifunctionalV;
    @ViewInject(R.id.f3_tire_bg)
    View f3_tire_bgV;
    @ViewInject(R.id.f3_tire)
    View f3_tireV;
    @ViewInject(R.id.p4_rpm_bg)
    View p4_rpm_bgV;
    @ViewInject(R.id.p4_rpm)
    View p4_rpmV;
    @ViewInject(R.id.f3_remaining_bg)
    View f3_remaining_bgV;
    @ViewInject(R.id.f3_remaining)
    View f3_remainingV;
    @ViewInject(R.id.f3_temp_bg)
    View f3_temp_bgV;
    @ViewInject(R.id.f3_temp)
    View f3_tempV;
    @ViewInject(R.id.p4_speed_bg)
    View p4_speed_bgV;
    @ViewInject(R.id.p4_speed)
    View p4_speedV;
    @ViewInject(R.id.p4_warm_bg)
    View p4_warm_bgV;
    @ViewInject(R.id.fault)
    View faultV;
    @ViewInject(R.id.voltage)
    View voltageV;
    @ViewInject(R.id.speed)
    View speedV;
    @ViewInject(R.id.tired)
    View tiredV;
    @ViewInject(R.id.warm)
    View warmV;
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
        f3_multifunctionalV.setOnClickListener(this);
        f3_tireV.setOnClickListener(this);
        p4_rpmV.setOnClickListener(this);
        f3_remainingV.setOnClickListener(this);
        f3_tempV.setOnClickListener(this);
        p4_speedV.setOnClickListener(this);
        faultV.setOnClickListener(this);
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
                f3_multifunctional_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                p4_rpm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_remaining_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_temp_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                p4_speed_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                p4_warm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.f3_oil:
                showNormalDailog(viewId, "油耗显示区", hudStatus.isOilShow());
                break;
            case R.id.f3_tire:
                showNormalDailog(viewId, "胎压显示区", hudStatus.isTireShow());
                break;
            case R.id.f3_remaining:
                showNormalDailog(viewId, "剩余燃油显示区", hudStatus.isRemainderOilShow());
                break;
            case R.id.f3_multifunctional:
                showMultifunctional();
                break;
            case R.id.p4_rpm:
                showNormalDailog(viewId, "发动机转速显示区", hudStatus.isRpmShow());
                break;
            case R.id.f3_temp:
                showNormalDailog(viewId, "水温显示区", hudStatus.isTempShow());
                break;
            case R.id.warm:
            case R.id.fault:
            case R.id.voltage:
            case R.id.speed:
            case R.id.tired:
                showWarm();
                break;
            case R.id.p4_speed:
                showNormalDailog(viewId, "车速显示区", hudStatus.isSpeedShow());
                break;
            default:
                break;
        }
    }

    private void showMultifunctional() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View voltageV = view.findViewById(R.id.voltage);
                        final View oil_lV = view.findViewById(R.id.oil_l);
                        final View tempV = view.findViewById(R.id.temp);
                        final View dismissV = view.findViewById(R.id.dismiss);
                        final View oilV = view.findViewById(R.id.oil);
                        switch (hudStatus.getMultifunctionalOneType()) {
                            case 0x00:
                                dismissV.setSelected(true);
                                break;
                            case 0x01:
                                tempV.setSelected(true);
                                break;
                            case 0x04:
                                oil_lV.setSelected(true);
                                break;
                            case 0x05:
                                oilV.setSelected(true);
                                break;
                            case 0x08:
                                voltageV.setSelected(true);
                                break;
                            default:
                                break;
                        }
                        voltageV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(true);
                                oil_lV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x08));
                                dialog.dismiss();
                            }
                        });

                        oil_lV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                oil_lV.setSelected(true);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x04));
                                dialog.dismiss();
                            }
                        });
                        tempV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                oil_lV.setSelected(false);
                                tempV.setSelected(true);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x01));
                                dialog.dismiss();
                            }
                        });

                        oilV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                oil_lV.setSelected(false);
                                tempV.setSelected(true);
                                oilV.setSelected(true);
                                dismissV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x05));
                                dialog.dismiss();
                            }
                        });
                        dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                oil_lV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(true);
                                oilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 00));
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.da_setting_multifunctional_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.hud_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.ff_ultifunctional_dailog_height))
                .show();
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
                                    case R.id.f3_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 01));
                                        break;
                                    case R.id.f3_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 01));
                                        break;
                                    case R.id.f3_remaining:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x07, 01));
                                        break;
                                    case R.id.p4_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 01));
                                        break;
                                    case R.id.f3_temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 01));
                                        break;
                                    case R.id.p4_speed:
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
                                    case R.id.f3_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 00));
                                        break;
                                    case R.id.f3_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 00));
                                        break;
                                    case R.id.f3_remaining:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x07, 00));
                                        break;
                                    case R.id.p4_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 00));
                                        break;
                                    case R.id.f3_temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 00));
                                        break;
                                    case R.id.p4_speed:
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
        if (null != hudStatus) {
            f3_tireV.setBackgroundResource(hudStatus.isTireShow() ? R.drawable.da_tire_show : R.drawable.da_tire_dismiss);
            f3_remainingV.setBackgroundResource(hudStatus.isRemainderOilShow() ? R.drawable.f3_remaining_show : R.drawable.f3_remaining_dismiss);
            p4_rpmV.setBackgroundResource(hudStatus.isRpmShow() ? R.drawable.p4_rpm_show : R.drawable.p4_rpm_dismiss);
            p4_speedV.setBackgroundResource(hudStatus.isSpeedShow() ? R.drawable.p4_speed_show : R.drawable.p4_speed_dismiss);
            f3_tempV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.f3_temp_show : R.drawable.f3_temp_dismiss);

            switch (hudStatus.getMultifunctionalOneType()) {
                case 0x00:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_dismiss);
                    break;
                case 0x01:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_temp_show);
                    break;
                case 0x04:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_oil_show);
                    break;
                case 0x05:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_avg_oil_show);
                    break;
                case 0x08:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_voltage_show);
                    break;
                default:
                    break;
            }
        }
        if (null != hudWarmStatus) {
            faultV.setBackgroundResource(hudWarmStatus.isFaultWarmShow() ? R.drawable.m2_fault_show : R.drawable.m2_fault_dismiss);
            voltageV.setBackgroundResource(hudWarmStatus.isVoltageWarmShow() ? R.drawable.m2_voltage_show : R.drawable.m2_voltage_dismiss);
            speedV.setBackgroundResource(hudWarmStatus.isSpeedWarmShow() ? R.drawable.m2_speed_show : R.drawable.m2_speed_dismiss);
            tiredV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.m2_tired_show : R.drawable.m2_tired_dismiss);
        }
    }
}
