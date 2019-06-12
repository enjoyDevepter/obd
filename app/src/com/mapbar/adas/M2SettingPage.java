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

@PageSetting(contentViewId = R.layout.m2_layout, toHistory = false)
public class M2SettingPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.setting)
    TextView settingV;
    @ViewInject(R.id.multifunctional_bg)
    View multifunctional_bgV;
    @ViewInject(R.id.multifunctional)
    View multifunctionalV;
    @ViewInject(R.id.m2_tire_bg)
    View m2_tire_bgV;
    @ViewInject(R.id.m2_tire)
    View tireV;
    @ViewInject(R.id.m2_warm_bg)
    View m2_warm_bgV;
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
        settingV.setOnClickListener(this);
        multifunctionalV.setOnClickListener(this);
        tireV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        temperatureV.setOnClickListener(this);
        voltageV.setOnClickListener(this);
        oilV.setOnClickListener(this);
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
                multifunctional_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                m2_tire_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
                m2_warm_bgV.setVisibility(choice ? View.INVISIBLE : View.VISIBLE);
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
            case R.id.m2_tire:
            case R.id.fault:
            case R.id.temperature:
            case R.id.voltage:
            case R.id.oil:
            case R.id.speed:
            case R.id.tired:
                showWarm();
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


                        View speed_showV = view.findViewById(R.id.speed_show);
                        speed_showV.setSelected(hudWarmStatus.isSpeedWarmShow());
                        speed_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x04, 1));
                                dialog.dismiss();
                            }
                        });

                        View speed_dismissV = view.findViewById(R.id.speed_dismiss);
                        speed_dismissV.setSelected(!hudWarmStatus.isSpeedWarmShow());
                        speed_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x04, 0));
                                dialog.dismiss();
                            }
                        });

                        View oil_showV = view.findViewById(R.id.oil_show);
                        oil_showV.setSelected(hudWarmStatus.isOilWarmShow());
                        oil_showV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 1));
                                dialog.dismiss();
                            }
                        });

                        View oil_dismissV = view.findViewById(R.id.oil_dismiss);
                        oil_dismissV.setSelected(!hudWarmStatus.isOilWarmShow());
                        oil_dismissV.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setHUDWarmStatus(0x03, 0));
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.m4_setting_warm_dailog)
                .setDimAmount(0.5f)
                .isCenter(true)
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
                    multifunctionalV.setBackgroundResource(R.drawable.m2_multifunctional_dismiss);
                    break;
                case 0x01:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_temp_show);
                    break;
                case 0x03:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_speed_show);
                    break;
                case 0x04:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_oil_show);
                    break;
                case 0x05:
                    multifunctionalV.setBackgroundResource(R.drawable.multifunctional_avg_oil_show);
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
            temperatureV.setBackgroundResource(hudWarmStatus.isTemperatureWarmShow() ? R.drawable.temperature_show : R.drawable.temperature_dismiss);
            tiredV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.tired_show : R.drawable.tired_dismiss);
            tireV.setBackgroundResource(hudWarmStatus.isTrieWarmShow() ? R.drawable.tire_show : R.drawable.tire_dismiss);
            speedV.setBackgroundResource(hudWarmStatus.isTiredWarmShow() ? R.drawable.speed_show : R.drawable.speed_dismiss);
            oilV.setBackgroundResource(hudWarmStatus.isOilWarmShow() ? R.drawable.oil_show : R.drawable.oil_dismiss);
        }
    }

}
