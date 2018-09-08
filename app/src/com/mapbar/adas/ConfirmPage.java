package com.mapbar.adas;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.ADJUST_START;

@PageSetting(contentViewId = R.layout.confirm_layout, toHistory = false)
public class ConfirmPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.info)
    private TextView info;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.save)
    private TextView save;
    @ViewInject(R.id.report)
    private View reportV;
    private int time = 10;
    private Timer timer;
    private TimerTask timerTask;

    private CustomDialog dialog;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("校准");
        info.setText(Html.fromHtml("保存当前胎压后，当某个轮胎胎压<font color='#FF0000'>发生变化</font>时， 盒子会发出报警声音。<br><br> 建议检查四个轮胎胎压均为正常的情况下 再进行保存操作！"));
    }

    @Override
    public void onStart() {
        super.onStart();
        initTimer();
        timer.schedule(timerTask, 0, 1000);
        BlueManager.getInstance().addBleCallBackListener(this);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                BlueManager.getInstance().send(ProtocolUtils.study());
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
                            timer.cancel();
                            timer = null;
                            timerTask.cancel();
                            timerTask = null;
                            save.setText("校准");
                            save.setOnClickListener(ConfirmPage.this);
                            save.setBackgroundResource(R.drawable.btn_bg);
                        } else {
                            save.setText("校准(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        };

        timer = new Timer();
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.ADJUSTING:
                if (ADJUST_START.get()) {
                    ADJUST_START.set(false);
                    AlarmManager.getInstance().play(R.raw.begin);
                }
                break;
            case OBDEvent.UN_LEGALITY:
                authFail("请从正规渠道购买!");
                break;
            case OBDEvent.NORMAL:
                MainPage mainPage = new MainPage();
                Bundle mainBundle = new Bundle();
                mainBundle.putSerializable("obdStatusInfo", (OBDStatusInfo) data);
                mainPage.setDate(mainBundle);
                PageManager.go(mainPage);
                break;
        }
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
}
