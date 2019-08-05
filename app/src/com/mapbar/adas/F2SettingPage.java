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

@PageSetting(contentViewId = R.layout.f2_layout)
public class F2SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.back)
    View backV;
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.params)
    TextView paramsV;
    @ViewInject(R.id.f2_oil_bg)
    View f2_oil_bgV;
    @ViewInject(R.id.f2_oil)
    View f2_oilV;
    @ViewInject(R.id.f2_tire_bg)
    View f2_tire_bgV;
    @ViewInject(R.id.f2_tire)
    View f2_tireV;
    @ViewInject(R.id.f2_voltage_bg)
    View f2_voltage_bgV;
    @ViewInject(R.id.f2_voltage)
    View f2_voltageV;
    @ViewInject(R.id.f2_engineload_bg)
    View f2_engineload_bgV;
    @ViewInject(R.id.f2_engineload)
    View f2_engineloadV;
    @ViewInject(R.id.f2_rpm_bg)
    View f2_rpm_bgV;
    @ViewInject(R.id.f2_rpm)
    View f2_rpmV;
    @ViewInject(R.id.f2_speed)
    View f2_speedV;
    @ViewInject(R.id.f2_temp_bg)
    View f2_temp_bgV;
    @ViewInject(R.id.f2_temp)
    View f2_tempV;
    @ViewInject(R.id.f2_fault_bg)
    View f2_fault_bgV;
    @ViewInject(R.id.f2_fault)
    View f2_faultV;
    @ViewInject(R.id.f2_v)
    View f2_vV;
    @ViewInject(R.id.f2_tired_bg)
    View f2_tired_bgV;
    @ViewInject(R.id.f2_t)
    View f2_tV;
    @ViewInject(R.id.f2_tired)
    View f2_tiredV;

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
        backV.setOnClickListener(this);
        paramsV.setOnClickListener(this);
        f2_oil_bgV.setOnClickListener(this);
        f2_tire_bgV.setOnClickListener(this);
        f2_voltage_bgV.setOnClickListener(this);
        f2_engineload_bgV.setOnClickListener(this);
        f2_rpm_bgV.setOnClickListener(this);
        f2_temp_bgV.setOnClickListener(this);
        f2_faultV.setOnClickListener(this);
        f2_vV.setOnClickListener(this);
        f2_tV.setOnClickListener(this);
        f2_tiredV.setOnClickListener(this);
        f2_speedV.setOnClickListener(this);
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
                f2_oil_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_voltage_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_engineload_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_rpm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_temp_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_fault_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                f2_tired_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.f2_oil_bg:
                showNormalDailog(viewId, "油耗显示区", hudStatus.isOilShow());
                break;
            case R.id.f2_tire_bg:
                showNormalDailog(viewId, "胎压显示区", hudStatus.isTireShow());
                break;
            case R.id.f2_voltage_bg:
                showMultifunctional();
                break;
            case R.id.f2_engineload_bg:
                showNormalDailog(viewId, "发动机负荷显示区", hudStatus.isEngineloadShow());
                break;
            case R.id.f2_rpm_bg:
                showNormalDailog(viewId, "发动机转速显示区", hudStatus.isRpmShow());
                break;
            case R.id.f2_temp_bg:
                showNormalDailog(viewId, "水温显示区", hudStatus.isTempShow());
                break;
            case R.id.f2_fault:
            case R.id.f2_tired:
            case R.id.f2_v:
            case R.id.f2_t:
                showWarm();
                break;
            case R.id.f2_speed:
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
                                    case R.id.f2_oil_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 01));
                                        break;
                                    case R.id.f2_tire_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 01));
                                        break;
                                    case R.id.f2_engineload_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0C, 01));
                                        break;
                                    case R.id.f2_rpm_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 01));
                                        break;
                                    case R.id.f2_temp_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 01));
                                        break;
                                    case R.id.f2_speed:
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
                                    case R.id.f2_oil_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x04, 00));
                                        break;
                                    case R.id.f2_tire_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0B, 00));
                                        break;
                                    case R.id.f2_engineload_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x0C, 00));
                                        break;
                                    case R.id.f2_rpm_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x02, 00));
                                        break;
                                    case R.id.f2_temp_bg:
                                        BlueManager.getInstance().send(ProtocolUtils.setHUDStatus(0x01, 00));
                                        break;
                                    case R.id.f2_speed:
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
                    }
                })
                .setLayoutRes(R.layout.f2_setting_warm_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setCancelOutside(true)
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
            f2_oilV.setBackgroundResource(hudStatus.isOilShow() ? R.drawable.f2_oil_show : R.drawable.f2_oil_dismiss);
            f2_tireV.setBackgroundResource(hudStatus.isTireShow() ? R.drawable.f2_tire_show : R.drawable.f2_tire_dismiss);
            f2_engineloadV.setBackgroundResource(hudStatus.isEngineloadShow() ? R.drawable.f2_engineload_show : R.drawable.f2_engineload_dismiss);
            f2_rpmV.setBackgroundResource(hudStatus.isRpmShow() ? R.drawable.f2_rpm_show : R.drawable.f2_rpm_dismiss);
            f2_speedV.setBackgroundResource(hudStatus.isSpeedShow() ? R.drawable.f2_speed_show : R.drawable.f2_speed_dismiss);
            f2_tempV.setBackgroundResource(hudStatus.isTempShow() ? R.drawable.f2_temp_show : R.drawable.f2_temp_dismiss);

            switch (hudStatus.getMultifunctionalOneType()) {
                case 0x00:
                    f2_voltageV.setBackgroundResource(R.drawable.f2_voltage_dismiss);
                    break;
                case 0x01:
                    f2_voltageV.setBackgroundResource(R.drawable.f2_voltage_c_show);
                    break;
                case 0x02:
                    f2_voltageV.setBackgroundResource(R.drawable.f2_voltage_r_show);
                    break;
                case 0x08:
                    f2_voltageV.setBackgroundResource(R.drawable.f2_voltage_v_show);
                    break;
                default:
                    break;
            }
        }
        if (null != hudWarmStatus) {
            f2_faultV.setBackgroundResource(hudWarmStatus.isFaultWarmShow() ? R.drawable.fault_show : R.drawable.fault_dismiss);
            f2_vV.setBackgroundResource(hudWarmStatus.isVoltageWarmShow() ? R.drawable.voltage_show : R.drawable.voltage_dismiss);
            f2_tV.setBackgroundResource(hudWarmStatus.isTemperatureWarmShow() ? R.drawable.temperature_show : R.drawable.temperature_dismiss);
            f2_tiredV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.tired_show : R.drawable.tired_dismiss);
        }
    }
}
