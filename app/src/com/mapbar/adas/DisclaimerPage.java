package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.obd.R;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.DISCALIMER_VISIBLE;

@PageSetting(transparent = true, toHistory = false, contentViewId = R.layout.disclaimer_layout)
public class DisclaimerPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.agree)
    private TextView agreeTV;
    @ViewInject(R.id.disagree)
    private TextView disagreeTV;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("免责声明");
        agreeTV.setOnClickListener(this);
        disagreeTV.setOnClickListener(this);
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
                PageManager.go(new MainPage());
                break;
            case R.id.disagree:
                PageManager.back();
                break;
        }
    }
}
