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


@PageSetting(contentViewId = R.layout.fm_set_layout, toHistory = false)
public class FMSetPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.confirm)
    private View confirmV;
    @ViewInject(R.id.home)
    private View homeV;
    @ViewInject(R.id.info)
    private View infoV;
    @ViewInject(R.id.rate)
    private TextView rateTV;
    @ViewInject(R.id.one)
    private View oneV;
    @ViewInject(R.id.two)
    private View twoV;
    @ViewInject(R.id.three)
    private View threeV;
    @ViewInject(R.id.four)
    private View fourV;
    @ViewInject(R.id.five)
    private View fiveV;
    @ViewInject(R.id.six)
    private View sixV;
    @ViewInject(R.id.seven)
    private View sevenV;
    @ViewInject(R.id.eight)
    private View eightV;
    @ViewInject(R.id.nine)
    private View nineV;
    @ViewInject(R.id.zero)
    private View zeroV;
    @ViewInject(R.id.dot)
    private View dotV;
    @ViewInject(R.id.del)
    private View delV;

    private CustomDialog dialog;

    private FMStatus fmStatus;

    private boolean set;

    @Override
    public void onResume() {
        super.onResume();
        back.setOnClickListener(this);
        homeV.setOnClickListener(this);
        infoV.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        oneV.setOnClickListener(this);
        twoV.setOnClickListener(this);
        threeV.setOnClickListener(this);
        fourV.setOnClickListener(this);
        fiveV.setOnClickListener(this);
        sixV.setOnClickListener(this);
        sevenV.setOnClickListener(this);
        eightV.setOnClickListener(this);
        nineV.setOnClickListener(this);
        zeroV.setOnClickListener(this);
        dotV.setOnClickListener(this);
        delV.setOnClickListener(this);
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.getFMParams());
    }

    private boolean canAdd() {
        String rate = rateTV.getText().toString().trim();
        return "".equals(rate) || (rate.contains(".") && rate.lastIndexOf(".") == rate.length() - 1) || (!rate.contains(".") && rate.length() < 3);
    }

    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        String rate = rateTV.getText().toString().trim();
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                if ("".equals(rate)) {
                    // 提示
                    showWarmDailog();
                    return;
                }
                if (rate.contains(".")) {
                    if (rate.indexOf("0") == rate.length() - 1) {
                        rate = rate + "0";
                    }
                } else {
                    rate = rate + ".0";
                }
                rate = rate.replace(".", "");
                int rateTemp = Integer.valueOf(rate);
                if (rateTemp < 875 || rateTemp > 1080) {
                    showWarmDailog();
                    return;
                }
                set = true;
                showProgressDailog();
                BlueManager.getInstance().send(ProtocolUtils.setFMParams(rateTemp));
                break;
            case R.id.home:
                PageManager.clearHistoryAndGo(new HomePage());
                break;
            case R.id.one:
                if (canAdd()) {
                    rateTV.setText(rate + "1");
                }
                break;
            case R.id.two:
                if (canAdd()) {
                    rateTV.setText(rate + "2");
                }
                break;
            case R.id.three:
                if (canAdd()) {
                    rateTV.setText(rate + "3");
                }
                break;
            case R.id.four:
                if (canAdd()) {
                    rateTV.setText(rate + "4");
                }
                break;
            case R.id.five:
                if (canAdd()) {
                    rateTV.setText(rate + "5");
                }
                break;
            case R.id.six:
                if (canAdd()) {
                    rateTV.setText(rate + "6");
                }
                break;
            case R.id.seven:
                if (canAdd()) {
                    rateTV.setText(rate + "7");
                }
                break;
            case R.id.eight:
                if (canAdd()) {
                    rateTV.setText(rate + "8");
                }
                break;
            case R.id.nine:
                if (canAdd()) {
                    rateTV.setText(rate + "9");
                }
                break;
            case R.id.zero:
                if (canAdd()) {
                    rateTV.setText(rate + "0");
                }
                break;
            case R.id.dot:
                if (!"".equals(rate) && !rate.contains(".")) {
                    rateTV.setText(rate + ".");
                }
                break;
            case R.id.del:
                if (!"".equals(rate)) {
                    rateTV.setText(rate.substring(0, rate.length() - 1));
                }
                break;
            case R.id.info:
                PageManager.go(new FMInfoPage());
                break;
            default:
                break;
        }
    }

    private void showWarmDailog() {
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
                .setLayoutRes(R.layout.dailog_fm_set_warm)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.FM_PARAMS_INFO:
                if (null != dialog && !dialog.isHidden()) {
                    dialog.dismiss();
                }
                fmStatus = (FMStatus) data;
                updateUI();
                break;
            default:
                break;
        }
    }

    private void showProgressDailog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                    }
                })
                .setLayoutRes(R.layout.dailog_fm_progress)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void showSuccessDailog(final String rate) {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View confirm = view.findViewById(R.id.confirm);
                        TextView infoTV = view.findViewById(R.id.info);
                        infoTV.setText("请您把车辆电台设置到\r\n" + rate);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_fm_set_success)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void updateUI() {
        int rate = fmStatus.getRate();
        String rateStr = String.valueOf(rate);
        if (rate >= 1000) {
            rateStr = rateStr.substring(0, 3) + "." + rateStr.substring(3);
        } else {
            rateStr = rateStr.substring(0, 2) + "." + rateStr.substring(2);
        }
        rateTV.setText(rateStr);

        if (set) {
            showSuccessDailog(rateStr);
            set = false;
        }
    }
}
