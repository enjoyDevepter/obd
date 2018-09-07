package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.installation_guide_layout)
public class InstallationGuidePage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.confirm)
    private View confirmV;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("安装引导");
        confirmV.setOnClickListener(this);
        back.setOnClickListener(this);

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
