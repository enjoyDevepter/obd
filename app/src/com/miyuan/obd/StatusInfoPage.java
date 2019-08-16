package com.miyuan.obd;

import android.view.View;
import android.widget.TextView;

import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;

@PageSetting(contentViewId = R.layout.status_info_layout, toHistory = false)
public class StatusInfoPage extends AppBasePage implements View.OnClickListener {
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.status)
    private TextView statusTV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmV.setOnClickListener(this);
        title.setText("获取盒子状态!");
        confirmV.setText("关闭");
        statusTV.setText("您的盒子可能为盗版盒子，请联系商家或厂家客服。");
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                PageManager.finishActivity(MainActivity.getInstance());
                break;
        }
    }
}
