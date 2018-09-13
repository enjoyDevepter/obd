package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.confirm_car_layout)
public class ConfirmCarPage extends AppBasePage implements View.OnClickListener {

    String carName = "";
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.car_info)
    private TextView carInfo;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.goback)
    private View goBack;
    @ViewInject(R.id.report)
    private View reportV;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("确认车型");
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        reportV.setOnClickListener(this);
        goBack.setOnClickListener(this);
        if (null != getDate()) {
            carName = getDate().get("carName").toString();
            carInfo.setText(carName);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
            case R.id.goback:
                PageManager.back();
                break;
            case R.id.report:
                BlueManager.getInstance().send(ProtocolUtils.reset());
                break;
            case R.id.next:
                OBDActivatePage page = new OBDActivatePage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", getDate().getString("boxId"));
                bundle.putString("phone", getDate().getString("phone"));
                bundle.putString("code", getDate().getString("code"));
                bundle.putString("sn", getDate().getString("sn").toString());
                bundle.putString("carId", getDate().getString("carId").toString());
                bundle.putString("carName", getDate().get("carName").toString());
                page.setDate(bundle);
                PageManager.go(page);
                break;
        }
    }
}
