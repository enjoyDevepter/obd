package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.HUDStatus;
import com.mapbar.hamster.HUDWarmStatus;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import static com.mapbar.hamster.OBDEvent.HUD_STATUS_INFO;
import static com.mapbar.hamster.OBDEvent.HUD_WARM_STATUS_INFO;

@PageSetting(contentViewId = R.layout.pro_lily_layout, toHistory = false)
public class ProLilySettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.multifunctional_bg)
    View multifunctional_bgV;
    @ViewInject(R.id.multifunctional)
    View multifunctionalV;
    @ViewInject(R.id.lily_remaining_bg)
    View lily_remaining_bgV;
    @ViewInject(R.id.lily_remaining)
    View lily_remainingV;

    @ViewInject(R.id.lily_temp_bg)
    View lily_temp_bgV;
    @ViewInject(R.id.lily_temp)
    View lily_tempV;

    @ViewInject(R.id.da_speed_bg)
    View da_speed_bgV;
    @ViewInject(R.id.da_speed)
    View da_speedV;
    @ViewInject(R.id.da_mile)
    View da_mileV;
    @ViewInject(R.id.da_warm_bg)
    View da_warm_bgV;
    @ViewInject(R.id.fault)
    View faultV;
    @ViewInject(R.id.voltage)
    View voltageV;
    @ViewInject(R.id.speed)
    View speedV;
    @ViewInject(R.id.tired)
    View tiredV;
    @ViewInject(R.id.da_rpm_bg)
    View da_rpm_bgV;
    @ViewInject(R.id.da_rpm)
    View da_rpmV;
    @ViewInject(R.id.da_oil_bg)
    View da_oil_bgV;
    @ViewInject(R.id.da_oil)
    View da_oilV;
    @ViewInject(R.id.da_camera_bg)
    View da_camera_bgV;
    @ViewInject(R.id.da_camera)
    View da_cameraV;
    @ViewInject(R.id.da_line)
    View da_lineV;
    @ViewInject(R.id.da_tire_bg)
    View da_tire_bgV;
    @ViewInject(R.id.da_tire)
    View da_tireV;
    @ViewInject(R.id.da_distance_bg)
    View da_distance_bgV;
    @ViewInject(R.id.da_navi)
    View da_naviV;
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
        settingV.setOnClickListener(this);
        multifunctionalV.setOnClickListener(this);
        lily_remainingV.setOnClickListener(this);
        lily_tempV.setOnClickListener(this);
        da_speedV.setOnClickListener(this);
        da_mileV.setOnClickListener(this);
        da_warm_bgV.setOnClickListener(this);
        da_rpmV.setOnClickListener(this);
        da_oilV.setOnClickListener(this);
        da_tireV.setOnClickListener(this);

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
                    settingV.setText("设置");
                } else {
                    settingV.setText("完成");
                }
                settingV.setSelected(!choice);
                multifunctional_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                lily_remaining_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                lily_temp_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_speed_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_warm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_rpm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_oil_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_camera_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                da_distance_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.da_speed:
                showNormalDailog(viewId, "车速显示区", hudStatus.isSpeedShow());
                break;
            case R.id.da_tire:
                showNormalDailog(viewId, "胎压显示区", hudStatus.isTireShow());
                break;
            case R.id.lily_temp:
                showNormalDailog(viewId, "水温显示区", hudStatus.isTempShow());
                break;
            case R.id.da_mile:
                showNormalDailog(viewId, "里程显示区", hudStatus.isMileShow());
                break;
            case R.id.da_rpm:
                showNormalDailog(viewId, "转速显示区", hudStatus.isRpmShow());
                break;
            case R.id.da_oil:
                showNormalDailog(viewId, "瞬时油耗显示区", hudStatus.isOilShow());
                break;
            case R.id.lily_remaining:
                showNormalDailog(viewId, "剩余燃油显示区", hudStatus.isRemainderOilShow());
                break;
            case R.id.da_warm_bg:
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
                                    case R.id.da_speed:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x03, 01));
                                        break;
                                    case R.id.da_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 01));
                                        break;
                                    case R.id.temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 01));
                                        break;
                                    case R.id.da_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 01));
                                        break;
                                    case R.id.da_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 01));
                                        break;
                                    case R.id.remaining:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x07, 01));
                                        break;
                                    case R.id.da_mile:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0A, 01));
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
                                    case R.id.da_speed:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x03, 00));
                                        break;
                                    case R.id.da_tire:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 00));
                                        break;
                                    case R.id.temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 00));
                                        break;
                                    case R.id.da_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 00));
                                        break;
                                    case R.id.da_oil:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 00));
                                        break;
                                    case R.id.remaining:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x07, 00));
                                        break;
                                    case R.id.da_mile:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0A, 00));
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
                        View fault_showV = view.findViewById(R.id.fault_show);
                        fault_showV.setSelected(hudWarmStatus.isFaultWarmShow());
                        fault_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x07, 1));
                                dialog.dismiss();
                            }
                        });

                        View fault_dismissV = view.findViewById(R.id.fault_dismiss);
                        fault_dismissV.setSelected(!hudWarmStatus.isFaultWarmShow());
                        fault_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x07, 0));
                                dialog.dismiss();
                            }
                        });

                        View voltage_showV = view.findViewById(R.id.voltage_show);
                        voltage_showV.setSelected(hudWarmStatus.isVoltageWarmShow());
                        voltage_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x02, 1));
                                dialog.dismiss();
                            }
                        });

                        View voltage_dismissV = view.findViewById(R.id.voltage_dismiss);
                        voltage_dismissV.setSelected(!hudWarmStatus.isVoltageWarmShow());
                        voltage_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x02, 0));
                                dialog.dismiss();
                            }
                        });

                        View temp_showV = view.findViewById(R.id.temp_show);
                        temp_showV.setSelected(hudWarmStatus.isTemperatureWarmShow());
                        temp_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x01, 1));
                                dialog.dismiss();
                            }
                        });

                        View temp_dismissV = view.findViewById(R.id.temp_dismiss);
                        temp_dismissV.setSelected(!hudWarmStatus.isTemperatureWarmShow());
                        temp_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x01, 0));
                                dialog.dismiss();
                            }
                        });

                        View tried_showV = view.findViewById(R.id.tried_show);
                        tried_showV.setSelected(hudWarmStatus.isTiredWarmShow());
                        tried_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x06, 1));
                                dialog.dismiss();
                            }
                        });

                        View tried_dismissV = view.findViewById(R.id.tried_dismiss);
                        tried_dismissV.setSelected(!hudWarmStatus.isTiredWarmShow());
                        tried_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x06, 0));
                                dialog.dismiss();
                            }
                        });

                        View tire_showV = view.findViewById(R.id.tire_show);
                        tire_showV.setSelected(hudWarmStatus.isTrieWarmShow());
                        tire_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x05, 1));
                                dialog.dismiss();
                            }
                        });

                        View tire_dismissV = view.findViewById(R.id.tire_dismiss);
                        tire_dismissV.setSelected(!hudWarmStatus.isTrieWarmShow());
                        tire_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x05, 0));
                                dialog.dismiss();
                            }
                        });

                        View speed_showV = view.findViewById(R.id.speed_show);
                        speed_showV.setSelected(hudWarmStatus.isTrieWarmShow());
                        speed_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x04, 1));
                                dialog.dismiss();
                            }
                        });

                        View speed_dismissV = view.findViewById(R.id.speed_dismiss);
                        speed_dismissV.setSelected(!hudWarmStatus.isTrieWarmShow());
                        speed_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x04, 0));
                                dialog.dismiss();
                            }
                        });


                        View remainder_showV = view.findViewById(R.id.remainder_show);
                        remainder_showV.setSelected(hudWarmStatus.isTrieWarmShow());
                        remainder_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 1));
                                dialog.dismiss();
                            }
                        });

                        View remainder_dismissV = view.findViewById(R.id.remainder_dismiss);
                        remainder_dismissV.setSelected(!hudWarmStatus.isTrieWarmShow());
                        remainder_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 0));
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.ff_setting_warm_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
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
            lily_remainingV.setBackgroundResource(hudStatus.isRemainderOilShow() ? R.drawable.lily_remaining_show : R.drawable.lily_remaining_dismiss);
            lily_tempV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.lily_temp_show : R.drawable.lily_temp_dismiss);
            da_speedV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.da_speed_show : R.drawable.da_speed_dismiss);
            da_mileV.setBackgroundResource(hudStatus.isMileShow() ? R.drawable.da_mile_show : R.drawable.da_mile_dismiss);
            da_rpmV.setBackgroundResource(hudStatus.isRpmShow() ? R.drawable.da_rpm_show : R.drawable.da_rpm_dismiss);
            da_oilV.setBackgroundResource(hudStatus.isOilShow() ? R.drawable.da_oil_show : R.drawable.da_oil_dismiss);
            da_tireV.setBackgroundResource(hudStatus.isTireShow() ? R.drawable.da_tire_show : R.drawable.da_tire_dismiss);

            switch (hudStatus.getMultifunctionalOneType()) {
                case 0x00:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_dismiss);
                    break;
                case 0x01:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_temp_show);
                    break;
                case 0x04:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_l_show);
                    break;
                case 0x05:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_oil_show);
                    break;
                case 0x08:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_voltage_show);
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
