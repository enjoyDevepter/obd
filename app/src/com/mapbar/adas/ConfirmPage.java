package com.mapbar.adas;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.confirm_layout)
public class ConfirmPage extends AppBasePage implements View.OnClickListener {

    CustomDialog dialog = null;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.info)
    private TextView info;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.save)
    private TextView save;
    private int time = 10;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("保存当前胎压");
        info.setText(Html.fromHtml("保存当前胎压后，当某个轮胎胎压<font color='#FF0000'>发生变化</font>时， 盒子会发出报警声音。<br><br> 建议检查四个轮胎胎压均为正常的情况下 再进行保存操作！"));
    }

    @Override
    public void onStart() {
        super.onStart();
        initTimer();
        timer.schedule(timerTask, 0, 1000);
    }

    @Override
    public void onStop() {
        super.onStop();
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
                BlueManager.getInstance().write(ProtocolUtils.study());
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) view.findViewById(R.id.info)).setText(Html.fromHtml("保存当前胎压后，当某个轮胎胎压<font color='#FF0000'>发生变化</font>时， 盒子会发出报警声音。<br><br> 建议检查四个轮胎胎压均为正常的情况下 再进行保存操作！"));
                                view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });

                                view.findViewById(R.id.unsave).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                    }
                                });
                            }
                        })
                        .setLayoutRes(R.layout.dailog_save)
                        .setDimAmount(0.5f)
                        .isCenter(true)
                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                        .show();
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
                            save.setText("保存胎压");
                            save.setOnClickListener(ConfirmPage.this);
                            save.setBackgroundResource(R.drawable.disclaimer_agree_btn_bg);
                        } else {
                            save.setText("保存胎压(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        };

        timer = new Timer();
    }
}
