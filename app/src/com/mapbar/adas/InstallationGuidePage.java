package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.installation_guide_layout)
public class InstallationGuidePage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private int time = 10;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("安装引导一");
        reportV.setVisibility(View.GONE);
        confirmV.setSelected(false);
        back.setVisibility(View.GONE);
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
                            confirmV.setText("确认已拉手刹、并打火");
                            confirmV.setSelected(true);
                            confirmV.setOnClickListener(InstallationGuidePage.this);
                        } else {
                            confirmV.setText("确认已拉手刹、并打火(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        };
        timer.schedule(timerTask, 1000, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
            timer.cancel();
            timer = null;
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                InstallationGuideTwoPage authPage = new InstallationGuideTwoPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", getDate().getString("boxId"));
                authPage.setDate(bundle);
                PageManager.go(authPage);
                break;
        }
    }

}
