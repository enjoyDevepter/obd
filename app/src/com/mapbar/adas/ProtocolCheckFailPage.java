package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import org.simple.eventbus.EventBus;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.protocol_check_fail_layout, toHistory = false)
public class ProtocolCheckFailPage extends AppBasePage implements BleCallBackListener, View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private CustomDialog dialog;

    private volatile boolean showConfirm;
    private volatile int times = 1;
    private int time = 15;
    private Timer timer;
    private TimerTask timerTask;
    private volatile OBDStatusInfo obdStatusInfo;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setOnClickListener(this);
        confirmV.setSelected(false);
        title.setText("匹配结果");
        BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
    }

    @Override
    public void onStart() {
        super.onStart();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.CURRENT_MISMATCHING:
                obdStatusInfo = (OBDStatusInfo) data;
                if (!showConfirm) {
                    showConfirm = true;
                    time = 15;
                    initTimer();
                    timer.schedule(timerTask, 3000, 1000);
                }
                BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
                break;
            case OBDEvent.UN_ADJUST:
                dismissProgress();
                obdStatusInfo = (OBDStatusInfo) data;
                PageManager.go(new ConfirmPage());
                break;
            case OBDEvent.UN_LEGALITY:
                authFail("请从正规渠道购买!");
                break;
            case OBDEvent.NORMAL:
                dismissProgress();
                obdStatusInfo = (OBDStatusInfo) data;
                MainPage mainPage = new MainPage();
                Bundle mainBundle = new Bundle();
                mainBundle.putSerializable("obdStatusInfo", (OBDStatusInfo) data);
                mainPage.setDate(mainBundle);
                PageManager.go(mainPage);
                break;
        }
    }

    private void initTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (time <= 0 && timer != null) {
                            showProgress();
                            timer.cancel();
                            timer = null;
                            timerTask.cancel();
                            timerTask = null;
                            confirmV.setText("确认，我已打火!");
                            confirmV.setSelected(true);
                            confirmV.setOnClickListener(ProtocolCheckFailPage.this);
                        } else {
                            confirmV.setText("确认，我已打火!(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        };

        timer = new Timer();
    }

    /**
     * 授权失败
     *
     * @param reason
     */
    private void authFail(final String reason) {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) (view.findViewById(R.id.confirm))).setText("确认");
                                ((TextView) (view.findViewById(R.id.info))).setText(reason);
                                ((TextView) (view.findViewById(R.id.title))).setText("授权失败");
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        // 退出应用
                                        PageManager.finishActivity(MainActivity.getInstance());
                                    }
                                });
                            }
                        })
                        .setLayoutRes(R.layout.dailog_common_warm)
                        .setCancelOutside(false)
                        .setDimAmount(0.5f)
                        .isCenter(true)
                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                        .show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:
                break;
            case R.id.confirm:
                if (times >= 2) {
                    if (!obdStatusInfo.isCurrentMatching()) {
                        if (obdStatusInfo.isBerforeMatching()) {
                            authFail("请不要换车!");
                        } else {
                            EventBus.getDefault().post(0, EventBusTags.START_COLLECT);
                            PageManager.go(new CollectGuide());
                        }
                    }
                } else {
                    times++;
                    confirmV.setOnClickListener(null);
                    confirmV.setSelected(false);
                    showProgress();
                    time = 15;
                    initTimer();
                    timer.schedule(timerTask, 1000, 1000);
                }
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

}
