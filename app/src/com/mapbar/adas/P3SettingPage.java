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

@PageSetting(contentViewId = R.layout.f3_layout, toHistory = false)
public class P3SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.f3_multifunctional_bg)
    View f3_multifunctional_bgV;
    @ViewInject(R.id.f3_multifunctional)
    View f3_multifunctionalV;
    @ViewInject(R.id.f3_tire_bg)
    View f3_tire_bgV;
    @ViewInject(R.id.f3_tire)
    View f3_tireV;
    @ViewInject(R.id.f3_rpm_bg)
    View f3_rpm_bgV;
    @ViewInject(R.id.f3_rpm)
    View f3_rpmV;
    @ViewInject(R.id.f3_oil_bg)
    View f3_oil_bgV;
    @ViewInject(R.id.f3_oil)
    View f3_oilV;
    @ViewInject(R.id.f3_remaining_bg)
    View f3_remaining_bgV;
    @ViewInject(R.id.f3_remaining)
    View f3_remainingV;
    @ViewInject(R.id.f3_temp_bg)
    View f3_temp_bgV;
    @ViewInject(R.id.f3_temp)
    View f3_tempV;
    @ViewInject(R.id.f3_speed_bg)
    View f3_speed_bgV;
    @ViewInject(R.id.f3_speed)
    View f3_speedV;
    @ViewInject(R.id.f3_warm_bg1)
    View f3_warm_bg1V;
    @ViewInject(R.id.fault)
    View faultV;
    @ViewInject(R.id.voltage)
    View voltageV;
    @ViewInject(R.id.f3_warm_bg2)
    View f3_warm_bg2V;
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
        settingV.setOnClickListener(this);
        f3_multifunctionalV.setOnClickListener(this);
        f3_tireV.setOnClickListener(this);
        f3_rpmV.setOnClickListener(this);
        f3_oilV.setOnClickListener(this);
        f3_remainingV.setOnClickListener(this);
        f3_tempV.setOnClickListener(this);
        f3_speedV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        voltageV.setOnClickListener(this);
        speedV.setOnClickListener(this);
        tiredV.setOnClickListener(this);
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
                f3_multifunctional_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_rpm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_oil_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_remaining_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_temp_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_speed_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_warm_bg1V.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f3_warm_bg2V.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.f3_rpm:
                showNormalDailog(viewId, "发动机转速显示区", hudStatus.isRpmShow());
                break;
            case R.id.f3_temp:
                showNormalDailog(viewId, "水温显示区", hudStatus.isTempShow());
                break;
            case R.id.fault:
            case R.id.voltage:
            case R.id.speed:
            case R.id.tired:
                showWarm();
                break;
            case R.id.f3_speed:
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
                        final View tpmV = view.findViewById(R.id.tpm);
                        final View tempV = view.findViewById(R.id.temp);
                        final View dismissV = view.findViewById(R.id.dismiss);
                        switch (hudStatus.getMultifunctionalOneType()) {
                            case 0x00:
                                dismissV.setSelected(true);
                                break;
                            case 0x01:
                                tempV.setSelected(true);
                                break;
                            case 0x02:
                                tpmV.setSelected(true);
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
                                tpmV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x08));
                                dialog.dismiss();
                            }
                        });

                        tpmV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                tpmV.setSelected(true);
                                tempV.setSelected(false);
                                dismissV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x02));
                                dialog.dismiss();
                            }
                        });
                        tempV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                tpmV.setSelected(false);
                                tempV.setSelected(true);
                                dismissV.setSelected(false);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 0x01));
                                dialog.dismiss();
                            }
                        });
                        dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                voltageV.setSelected(false);
                                tpmV.setSelected(false);
                                tempV.setSelected(false);
                                dismissV.setSelected(true);
                                BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x21, 00));
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.f2_setting_multifunctional_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.hud_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.f2_ultifunctional_dailog_height))
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
                                    case R.id.f3_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 01));
                                        break;
                                    case R.id.f3_temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 01));
                                        break;
                                    case R.id.f3_speed:
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
                                    case R.id.f3_rpm:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 00));
                                        break;
                                    case R.id.f3_temp:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 00));
                                        break;
                                    case R.id.f3_speed:
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
                    }
                })
                .setLayoutRes(R.layout.f2_setting_warm_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.f2_warm_dailog_width))
                .setHeight(OBDUtils.getDimens(getContext(), R.dimen.f2_warm_dailog_height))
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
            f3_oilV.setBackgroundResource(hudStatus.isOilShow() ? R.drawable.f3_oil_show : R.drawable.f3_oil_dismiss);
            f3_tireV.setBackgroundResource(hudStatus.isTireShow() ? R.drawable.da_tire_show : R.drawable.da_tire_dismiss);
            f3_remainingV.setBackgroundResource(hudStatus.isRemainderOilShow() ? R.drawable.f3_remaining_show : R.drawable.f3_remaining_dismiss);
            f3_rpmV.setBackgroundResource(hudStatus.isRpmShow() ? R.drawable.f3_rpm_show : R.drawable.f3_rpm_dismiss);
            f3_speedV.setBackgroundResource(hudStatus.isSpeedShow() ? R.drawable.f3_speed_show : R.drawable.f3_speed_dismiss);
            f3_tempV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.f3_temp_show : R.drawable.f2_temp_dismiss);

            switch (hudStatus.getMultifunctionalOneType()) {
                case 0x00:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_dismiss);
                    break;
                case 0x01:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_temp_show);
                    break;
                case 0x04:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_avg_oil_show);
                    break;
                case 0x05:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_oil_show);
                    break;
                case 0x08:
                    f3_multifunctionalV.setBackgroundResource(R.drawable.f3_multifunctional_voltage_show);
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
