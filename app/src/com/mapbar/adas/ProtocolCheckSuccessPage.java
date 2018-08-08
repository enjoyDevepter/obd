package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.protocol_check_success_layout, toHistory = false)
public class ProtocolCheckSuccessPage extends AppBasePage {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.time)
    private TextView timeTV;

    private int time = 6;
    private Timer timer;
    private TimerTask timerTask;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("匹配结果");
        time = 6;
        initTimer();
        timer.schedule(timerTask, 0, 1000);
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
                            MainPage page = new MainPage();
                            if (getDate() != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString("sn", getDate().getString("sn"));
                                if (getDate().containsKey("showStudy")) {
                                    bundle.putBoolean("showStudy", (Boolean) getDate().get("showStudy"));
                                }
                                page.setDate(bundle);
                            }
                            PageManager.go(page);
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
