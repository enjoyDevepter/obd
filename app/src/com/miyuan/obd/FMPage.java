package com.miyuan.obd;

import android.view.View;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.FMStatus;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;

import static com.miyuan.obd.preferences.SettingPreferencesConfig.RATE_INDEX;


@PageSetting(contentViewId = R.layout.fm_layout)
public class FMPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.hand)
    private View handV;
    @ViewInject(R.id.confirm)
    private View confirmV;
    @ViewInject(R.id.home)
    private View homeV;
    @ViewInject(R.id.close)
    private View closeV;
    @ViewInject(R.id.info)
    private View infoV;
    @ViewInject(R.id.rate)
    private TextView rateTV;

    private FMStatus fmStatus;

    private String[] rates = new String[]{"87.5", "104.4", "94.4", "107.4", "105.4", "87.7", "87.9", "88.4", "89.9", "90.5", "91.3", "91.9", "92.0", "93.8", "94.5", "95.8", "98.8", "99.9", "101.2", "102.3", "107.2", "108.0"};

    private CustomDialog dialog;


    @Override
    public void onResume() {
        super.onResume();
        back.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        handV.setOnClickListener(this);
        homeV.setOnClickListener(this);
        closeV.setOnClickListener(this);
        infoV.setOnClickListener(this);
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏

        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getFMParams());
    }

    private void showSetDailog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View confirm = view.findViewById(R.id.confirm);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_fm_close)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void showCloseDailog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View cancel = view.findViewById(R.id.cancel);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setFMParams(false));
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.confirm_fm_close)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void showCloseConfirm() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View confirm = view.findViewById(R.id.confirm);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                PageManager.clearHistoryAndGo(new HomePage());
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_fm_close_confirm)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
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
            case R.id.confirm:
                String rate;
                if (RATE_INDEX.get() == rates.length - 1) {
                    rate = rates[0];
                    RATE_INDEX.set(0);
                } else {
                    rate = rates[RATE_INDEX.get() + 1];
                    RATE_INDEX.set(RATE_INDEX.get() + 1);
                }
                showSetDailog();
                rate = rate.replace(".", "");
                BlueManager.getInstance().send(ProtocolUtils.setFMParams(Integer.valueOf(rate)));
                break;
            case R.id.home:
                PageManager.clearHistoryAndGo(new HomePage());
                break;
            case R.id.close:
                showCloseDailog();
                break;
            case R.id.hand:
                PageManager.go(new FMOperationInfoPage());
                break;
            case R.id.info:
                PageManager.go(new FMInfoPage());
                break;
            default:
                break;

        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.FM_PARAMS_INFO:
                if (null != dialog && !dialog.isHidden()) {
                    dialog.dismiss();
                }
                fmStatus = (FMStatus) data;
                if (!fmStatus.isEnable()) {
                    showCloseConfirm();
                    return;
                }
                updateUI();
                break;
            default:
                break;
        }
    }

    private void updateUI() {
        int rate = fmStatus.getRate();
        String rateStr = String.valueOf(rate);
        if (rate > 1000) {
            rateTV.setText(rateStr.substring(0, 3) + "." + rateStr.substring(3));
        } else {
            rateTV.setText(rateStr.substring(0, 2) + "." + rateStr.substring(2));
        }
    }
}
