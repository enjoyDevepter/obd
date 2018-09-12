package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.collect_finish_layout)
public class CollectFinish extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.success)
    private View successV;
    @ViewInject(R.id.fail)
    private View failV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private boolean success;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmV.setOnClickListener(this);
        success = getDate().getBoolean("success");
        if (success) {
            title.setText("恭喜您");
            confirmV.setText("关闭");
            failV.setVisibility(View.GONE);
        } else {
            title.setText("完成");
            confirmV.setText("提交并申请支持开发");
            successV.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                if (success) {
                    PageManager.go(new OBDAuthPage());
                } else {
                    PageManager.finishActivity(MainActivity.getInstance());
                }
                break;
        }
    }
}
