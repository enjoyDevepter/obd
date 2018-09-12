package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.obd_init_layout, toHistory = false)
public class OBDInitPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.confirm)
    private View confirmTV;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("安装引导");
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmTV.setOnClickListener(this);
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.finishActivity(MainActivity.getInstance());
                break;
            case R.id.confirm:
                FireConfirmPage page = new FireConfirmPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", getDate().getString("boxId"));
                page.setDate(bundle);
                PageManager.go(page);
                break;
        }
    }
}
