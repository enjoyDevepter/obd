package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.collect_guide_layout, toHistory = false)
public class CollectGuide extends AppBasePage implements View.OnClickListener {

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
        title.setText("车辆磨合");
        back.setVisibility(View.GONE);
//        reportV.setVisibility(View.GONE);
        confirmV.setOnClickListener(this);
        reportV.setOnClickListener(this);

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
            case R.id.confirm:
                CollectOnePage collectOnePage = new CollectOnePage();
                Bundle bundle = new Bundle();
                bundle.putBoolean("matching", getDate().getBoolean("matching"));
                collectOnePage.setDate(bundle);
                PageManager.go(collectOnePage);
                break;
            case R.id.report:
                BlueManager.getInstance().send(ProtocolUtils.reset());
                break;
        }
    }
}
