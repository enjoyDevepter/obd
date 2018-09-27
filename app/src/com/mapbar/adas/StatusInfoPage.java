package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

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
        boolean fake = getDate().getBoolean("fake");
        statusTV.setText(fake ? "您的盒子可能为盗版盒子，请联系商家或厂家客服。" : "您的胎压盒子还在进一步校准中，请耐心等待。整个校准过程大概需要十几分钟，如果遇到长时间没有校准完成，您可以联系我们的客服。");
        confirmV.setText(fake ? "退出" : "关闭");
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
