package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.miyuan.obd.R;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.DISCALIMER_VISIBLE;

@PageSetting(transparent = true, toHistory = false, contentViewId = R.layout.disclaimer_layout)
public class DisclaimerPage extends AppBasePage implements View.OnClickListener {
    @ViewInject(R.id.title_text)
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
                DISCALIMER_VISIBLE.set(true);
                if (BlueManager.getInstance().isConnected()) {
                    PageManager.go(new OBDAuthPage());
                } else {
                    BlueManager.getInstance().stopScan(false);
                    PageManager.go(new OBDeviceCheckPage());
                }
                break;
            case R.id.disagree:
                PageManager.back();
                break;
        }
    }
}
