package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.view.CustomScrollView;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.DISCALIMER_VISIBLE;

@PageSetting(transparent = true, toHistory = false, contentViewId = R.layout.disclaimer_layout)
public class DisclaimerPage extends AppBasePage implements CustomScrollView.ISmartScrollChangedListener {
    CustomDialog dialog = null;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.scrollView)
    private CustomScrollView scrollView;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("免责声明");
        scrollView.setScanScrollChangedListener(this);
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
    public void onScrolledToBottom() {
        Log.d("onScrolledToBottom  ");
        if (null == dialog) {
            showConfirm();
        } else if (null != dialog && !dialog.isAdded()) {
            dialog.show();
        }

    }

    @Override
    public void onScrolledToTop() {
        Log.d("onScrolledToTop  ");
    }

    @Override
    public void onScrolled() {
        Log.d("onScrolled  ");
        if (null != dialog && dialog.isVisible()) {
            dialog.dismiss();
        }
    }

    private void showConfirm() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.agree).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                DISCALIMER_VISIBLE.set(true);
                                PageManager.go(new MainPage());
                            }
                        });

                        view.findViewById(R.id.disagree).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                PageManager.back();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_disclaim)
                .setDimAmount(0.3f)
                .show();
    }
}
