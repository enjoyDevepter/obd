package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.obd_device_check_layout, toHistory = false)
public class OBDeviceCheckPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private View confrimV;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("插入OBD");
        confrimV.setOnClickListener(this);
        reportV.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                PageManager.go(new ConnectPage());
                break;
            case R.id.report:
                break;
        }
    }

}
