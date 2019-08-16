package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BlueManager;
import com.miyuan.obd.preferences.SettingPreferencesConfig;

@PageSetting(transparent = true, toHistory = false, contentViewId = R.layout.disclaimer_layout)
public class DisclaimerPage extends AppBasePage implements View.OnClickListener {
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.agree)
    private View agree;
    @ViewInject(R.id.disagree)
    private View disagree;
    @ViewInject(R.id.report)
    private View reportV;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        title.setText("免责声明");
        agree.setOnClickListener(this);
        disagree.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.agree:
                SettingPreferencesConfig.DISCALIMER_VISIBLE.set(true);
                if (BlueManager.getInstance().isConnected()) {
                    PageManager.go(new OBDAuthPage());
                } else {
                    BlueManager.getInstance().stopScan(false);
                    PageManager.go(new ConnectPage());
                }
                break;
            case R.id.disagree:
                PageManager.back();
                break;
        }
    }
}
