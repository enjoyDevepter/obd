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

@PageSetting(contentViewId = R.layout.f6_layout)
public class F6SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.back)
    View backV;
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.params)
    TextView paramsV;
    @ViewInject(R.id.ff_speed_bg)
    View ff_speed_bgV;
    @ViewInject(R.id.ff_speed)
    View ff_speedV;
    @ViewInject(R.id.ff_tire_bg)
    View ff_tire_bgV;
    @ViewInject(R.id.ff_tire)
    View ff_tireV;
    @ViewInject(R.id.multifunctional)
    View multifunctionalV;
    @ViewInject(R.id.ff_warm_bg)
    View ff_warm_bgV;
    @ViewInject(R.id.warm)
    View warmV;
    @ViewInject(R.id.fault)
    View faultV;
    @ViewInject(R.id.voltage)
    View voltageV;
    @ViewInject(R.id.speed)
    View speedV;
    @ViewInject(R.id.tired)
    View tiredV;
    @ViewInject(R.id.ff_temp_bg)
    View ff_temp_bgV;
    @ViewInject(R.id.ff_temp)
    View ff_tempV;
    @ViewInject(R.id.ff_rpm_bg)
    View ff_rpm_bgV;
    @ViewInject(R.id.ff_rpm)
    View ff_rpmV;
    @ViewInject(R.id.ff_oil_bg)
    View ff_oil_bgV;
    @ViewInject(R.id.ff_oil)
    View ff_oilV;
    @ViewInject(R.id.ff_oil_l_bg)
    View ff_oil_l_bgV;
    @ViewInject(R.id.ff_oil_l)
    View ff_oil_lV;
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
        ff_speedV.setOnClickListener(this);
        ff_tireV.setOnClickListener(this);
        multifunctionalV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        warmV.setOnClickListener(this);
        voltageV.setOnClickListener(this);
        speedV.setOnClickListener(this);
        tiredV.setOnClickListener(this);

        ff_tempV.setOnClickListener(this);
        ff_rpmV.setOnClickListener(this);
        ff_oilV.setOnClickListener(this);
        ff_oil_lV.setOnClickListener(this);
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
                ff_speed_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                ff_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                ff_warm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                ff_temp_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                ff_rpm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                ff_oil_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                ff_oil_l_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.ff_speed:
                showNormalDailog(viewId, "车速显示区", hudStatus.isSpeedShow());
                break;
            case R.id.ff_tire:
                showNormalDailog(viewId, "胎压显示区", hudStatus.isTireShow());
                break;
            case R.id.ff_temp:
                showNormalDailog(viewId, "水温显示区", hudStatus.isTempShow());
                break;
            case R.id.ff_rpm:
                showNormalDailog(viewId, "转速显示区", hudStatus.isRpmShow());
                break;
            case R.id.ff_oil:
                showNormalDailog(viewId, "瞬时油耗显示区", hudStatus.isOilShow());
                break;
            case R.id.ff_oil_l:
                showNormalDailog(viewId, "剩余燃油显示区", hudStatus.isRemainderOilShow());
                break;
            case R.id.warm:
            case R.id.fault:
            case R.id.tired:
            case R.id.voltage:
            case R.id.speed:
                showWarm();
                break;
            case R.id.multifunctional:
                showMultifunctional();
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
                        final View speedV = view.findViewById(R.id.speed);
                        final View tempV = view.findViewById(R.id.temp);
                        final View dismissV = view.findViewById(R.id.dismiss);
                        final View oilV = view.findViewById(R.id.oil);
                        switch (hudStatus.getMultifunctionalOneType()) {
                            case 0x00:
                                dismissV.setSelected(true);
                                break;
                            case 0x03:
                                speedV.setSelected(true);
                                break;
                            case 0x04:
                                tempV.setSelected(true);
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
                                speedV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                oilV.setSelected(false);
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
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x04));
                                dialog.dismiss();
                            }
                        });

                        oilV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                speedV.setSelected(false);
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
                                speedV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(true);
                                oilV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 00));
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.ff_setting_multifunctional_dailog)
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
                                    case R.id.ff_speed:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x03, 01));
                                        break;
                                    case R.id.ff_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 01));
                                        break;
                                    case R.id.ff_temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 01));
                                        break;
                                    case R.id.ff_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 01));
                                        break;
                                    case R.id.ff_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 01));
                                        break;
                                    case R.id.ff_oil_l:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x07, 01));
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
                                    case R.id.ff_speed:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x03, 00));
                                        break;
                                    case R.id.ff_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 00));
                                        break;
                                    case R.id.ff_temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 00));
                                        break;
                                    case R.id.ff_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 00));
                                        break;
                                    case R.id.ff_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 00));
                                        break;
                                    case R.id.ff_oil_l:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x07, 00));
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
            ff_speedV.setBackgroundResource(hudStatus.isSpeedShow() ? R.drawable.ff_speed_show : R.drawable.ff_speed_dismiss);
            ff_tireV.setBackgroundResource(hudStatus.isTireShow() ? R.drawable.ff_tire_show : R.drawable.ff_tire_dismiss);
            ff_tempV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.ff_temp_show : R.drawable.ff_temp_dismiss);
            ff_rpmV.setBackgroundResource(hudStatus.isRpmShow() ? R.drawable.ff_rpm_show : R.drawable.ff_rpm_dismiss);
            ff_oil_lV.setBackgroundResource(hudStatus.isRemainderOilShow() ? R.drawable.ff_oil_l_show : R.drawable.ff_oil_l_dismiss);
            ff_oilV.setBackgroundResource(hudStatus.isOilShow() ? R.drawable.ff_oil_show : R.drawable.ff_oil_dismiss);
            ff_tempV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.ff_temp_show : R.drawable.ff_temp_dismiss);

            switch (hudStatus.getMultifunctionalOneType()) {
                case 0x00:
                    multifunctionalV.setBackgroundResource(R.drawable.ff_multifunctional_dismiss);
                    break;
                case 0x04:
                    multifunctionalV.setBackgroundResource(R.drawable.ff_multifunctional_oil);
                    break;
                case 0x03:
                    multifunctionalV.setBackgroundResource(R.drawable.ff_multifunctional_s);
                    break;
                case 0x05:
                    multifunctionalV.setBackgroundResource(R.drawable.ff_multifunctional_avg_oil);
                    break;
                case 0x08:
                    multifunctionalV.setBackgroundResource(R.drawable.ff_multifunctional_v);
                    break;
                default:
                    break;
            }
        }
        if (null != hudWarmStatus) {
            faultV.setBackgroundResource(hudWarmStatus.isFaultWarmShow() ? R.drawable.fault_show : R.drawable.fault_dismiss);
            voltageV.setBackgroundResource(hudWarmStatus.isVoltageWarmShow() ? R.drawable.voltage_show : R.drawable.voltage_dismiss);
            speedV.setBackgroundResource(hudWarmStatus.isSpeedWarmShow() ? R.drawable.speed_show : R.drawable.speed_dismiss);
            tiredV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.tired_show : R.drawable.tired_dismiss);
        }
    }
}