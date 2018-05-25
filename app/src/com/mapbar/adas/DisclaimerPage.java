package com.mapbar.adas;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.wedrive.welink.adas.R;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.DISCALIMER_VISIBLE;

@PageSetting(transparent = true, toHistory = false, contentViewId = R.layout.disclaimer_layout)
public class DisclaimerPage extends AppBasePage {

    public final static int FLAG_ABOUT = 1;
    @ViewInject(R.id.title_text)
    private TextView textView;
    @ViewInject(R.id.disc_checkbox)
    private CheckBox checkBox;
    @ViewInject(R.id.disc_agree_btn)
    private Button agreeBtn;
    @ViewInject(R.id.title_back)
    private ImageButton backBtn;
    @ViewInject(R.id.disc_buttom)
    private FrameLayout bottom;
    private int flag = -1;
    private CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            DISCALIMER_VISIBLE.set(isChecked);
        }
    };
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.disc_agree_btn:
                    PageManager.go(new MainPage());
                    break;
                case R.id.title_back:
                    PageManager.back();
                    break;
            }
        }
    };

    public DisclaimerPage() {
        super();
    }

    @Override
    public void onResume() {
        super.onResume();
        backBtn.setVisibility(View.GONE);
        textView.setText(GlobalUtil.getResources().getText(R.string.disclaimer_tittle));
        agreeBtn.setOnClickListener(onClickListener);
        checkBox.setOnCheckedChangeListener(onCheckedChangeListener);
        checkBox.setChecked(DISCALIMER_VISIBLE.get());
        if (flag != -1) {
            backBtn.setOnClickListener(onClickListener);
            bottom.setVisibility(View.GONE);
            backBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }
}
