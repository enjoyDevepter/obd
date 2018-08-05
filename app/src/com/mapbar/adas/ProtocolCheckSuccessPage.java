package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.protocol_check_success_layout, toHistory = false)
public class ProtocolCheckSuccessPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.confirm)
    private View confrimV;
    @ViewInject(R.id.time)
    private TextView timeTV;

    private int time = 6;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("胎压匹配检查");
        confrimV.setOnClickListener(this);

        time = 10;
        initTimer();
        timer.schedule(timerTask, 0, 1000);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                BlueManager.getInstance().startScan();
                PageManager.go(new ConnectPage());
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
                        timeTV.setText(String.valueOf(time));
                        if (time <= 1 && timer != null) {
                            timer.cancel();
                            timer = null;
                            timerTask.cancel();
                            timerTask = null;
                            PageManager.go(new MainPage());
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
}
