package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.collect_guide_layout)
public class CollectGuide extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private Timer timer = new Timer();
    private int time = 10;
    private boolean ishow;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("深度校准准备");
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmV.setEnabled(ishow);
        back.setVisibility(View.GONE);
        if (!ishow) {
            ishow = true;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (time <= 0 && timer != null) {
                                timer.cancel();
                                timer = null;
                                confirmV.setText("确认已拉手刹、并打火");
                                confirmV.setEnabled(true);
                                confirmV.setOnClickListener(CollectGuide.this);
                            } else {
                                confirmV.setText("确认已拉手刹、并打火(" + time + "s)");
                            }
                            time--;
                        }
                    });
                }
            }, 1000, 1000);
        } else {
            confirmV.setOnClickListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                BlueManager.getInstance().send(ProtocolUtils.idling());
                CollectOnePage collectOnePage = new CollectOnePage();
                Bundle bundle = new Bundle();
                bundle.putBoolean("matching", getDate().getBoolean("matching"));
                bundle.putString("sn", getDate().getString("sn"));
                bundle.putString("pVersion", getDate().getString("pVersion"));
                bundle.putString("bVersion", getDate().getString("bVersion"));
                collectOnePage.setDate(bundle);
                PageManager.go(collectOnePage);
                break;
        }
    }
}
