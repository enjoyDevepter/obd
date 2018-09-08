package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.collect_one_layout, toHistory = false)
public class CollectOnePage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.time)
    private TextView timeTV;
    @ViewInject(R.id.confirm)
    private View confirmV;

    private int time = 40;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmV.setOnClickListener(this);
        title.setText("P挡空踩油门");
        time = 40;
        initTimer();
    }

    private void initTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        timeTV.setText(String.valueOf(time));
                        if (time <= 1 && timer != null) {
                            timeTV.setVisibility(View.GONE);
                            timer.cancel();
                            timer = null;
                            timerTask.cancel();
                            timerTask = null;
                            PageManager.go(new CollectPage());
                        }
                        time--;
                    }
                });
            }
        };
        timer = new Timer();
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
        }
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                BlueManager.getInstance().send(ProtocolUtils.idling());
                timer.schedule(timerTask, 0, 1000);
                break;
        }
    }
}
