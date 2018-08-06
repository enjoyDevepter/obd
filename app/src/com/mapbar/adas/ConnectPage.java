package com.mapbar.adas;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

@PageSetting(contentViewId = R.layout.connect_layout, toHistory = false)
public class ConnectPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.connect)
    private View connect;
    @ViewInject(R.id.retry)
    private View retry;

    private AnimationDrawable animationDrawable;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("方舟卫士");
        retry.setOnClickListener(this);
        BlueManager.getInstance().startScan();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        connect.setBackgroundResource(R.drawable.connect_bg);
        animationDrawable = (AnimationDrawable) connect.getBackground();
        animationDrawable.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry:
                animationDrawable.start();
                retry.setClickable(false);
                retry.setVisibility(View.INVISIBLE);
                BlueManager.getInstance().startScan();
                break;

        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.BLUE_SCAN_FINISHED:
                Log.d("OBDEvent.BLUE_SCAN_FINISHED " + data);
                if (!(boolean) data) {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            animationDrawable.stop();
                            retry.setClickable(true);
                            retry.setVisibility(View.VISIBLE);
                        }
                    });
                }
                break;
            case OBDEvent.BLUE_CONNECTED:
                Log.d("OBDEvent.BLUE_CONNECTED");
                PageManager.go(new OBDAuthPage());
                break;
        }

    }
}
