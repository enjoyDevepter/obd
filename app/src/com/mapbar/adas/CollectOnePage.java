package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.collect_one_layout, toHistory = false)
public class CollectOnePage extends AppBasePage implements View.OnClickListener {

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
        title.setText("磨合方法");
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                CollectTwoPage collectTwoPage = new CollectTwoPage();
                Bundle bundle = new Bundle();
                bundle.putBoolean("matching", getDate().getBoolean("matching"));
                collectTwoPage.setDate(bundle);
                PageManager.go(collectTwoPage);
                break;
        }
    }
}
