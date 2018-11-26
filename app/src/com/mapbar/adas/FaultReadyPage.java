package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;


@PageSetting(contentViewId = R.layout.fault_ready_layout)
public class FaultReadyPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.back)
    private View backV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private View confirmV;


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        titleTV.setText("车辆状态确认");
        backV.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        reportV.setVisibility(View.INVISIBLE);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                PageManager.go(new FaultCodePage());
                break;
        }
    }

}
