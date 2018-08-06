package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.obd.R;

@PageSetting(contentViewId = R.layout.protocol_check_fail_layout, toHistory = false)
public class ProtocolCheckFailPage extends AppBasePage implements BleCallBackListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("匹配结果");
        showProgress();
        BlueManager.getInstance().write(ProtocolUtils.getTirePressureStatus());
    }

    @Override
    public void onStart() {
        super.onStart();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                protocolCheck((byte[]) data);
                break;
        }
    }

    /**
     * 检查协议
     */
    private void protocolCheck(byte[] result) {

        byte[] bytes = HexUtils.getBooleanArray(result[19]);
        if (bytes[0] == 1) {
            // 车型不支持
            BlueManager.getInstance().write(ProtocolUtils.getTirePressureStatus());
        } else {
            dismissProgress();
            // 支持
            ProtocolCheckSuccessPage page = new ProtocolCheckSuccessPage();
            Bundle bundle = new Bundle();
            if (getDate() != null) {
                bundle.putBoolean("showStudy", (boolean) getDate().get("showStudy"));
            }
            bundle.putString("sn", getDate().getString("sn").toString());
            page.setDate(bundle);
            PageManager.go(page);
        }
    }
}
