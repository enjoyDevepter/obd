package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.installation_guide_two_layout)
public class InstallationGuideTwoPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    private Timer timer = new Timer();
    private int time = 15;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("安装引导二");
        back.setOnClickListener(this);
        confirmV.setSelected(false);
        reportV.setVisibility(View.GONE);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (time <= 0 && timer != null) {
                            timer.cancel();
                            timer = null;
                            confirmV.setText("我已准备好了");
                            confirmV.setSelected(true);
                            confirmV.setOnClickListener(InstallationGuideTwoPage.this);
                        } else {
                            confirmV.setText("我已准备好了(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        }, 1000, 1000);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        if (null != timer) {
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
                AuthPage authPage = new AuthPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", getDate().getString("boxId"));
                authPage.setDate(bundle);
                PageManager.go(authPage);
                break;
        }
    }

}
