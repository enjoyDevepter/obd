package com.mapbar.adas;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.dailog_confirm)
public class ConfirmPage extends AppBasePage implements View.OnClickListener {

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
                BlueManager.getInstance().send(ProtocolUtils.study());
                PageManager.go(new MainPage());
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
                            save.setBackgroundResource(R.drawable.btn_bg);
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
