package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

import org.simple.eventbus.EventBus;

@PageSetting(contentViewId = R.layout.collect_two_layout, toHistory = false)
public class CollectTwoPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private View confirmV;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmV.setOnClickListener(this);
        title.setText("磨合方法(图示)");
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                EventBus.getDefault().post(getDate().getBoolean("matching"), EventBusTags.START_COLLECT);
                CollectPage collectPage = new CollectPage();
                Bundle bundle = new Bundle();
                bundle.putBoolean("matching", getDate().getBoolean("matching"));
                collectPage.setDate(bundle);
                PageManager.go(collectPage);
                break;
        }
    }
}
