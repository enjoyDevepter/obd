package com.mapbar.adas;

import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;


@PageSetting(contentViewId = R.layout.collect_layout)
public class CollectPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.speed20)
    private View speed20;
    @ViewInject(R.id.speed20To60)
    private View speed20To60V;
    @ViewInject(R.id.speed60)
    private View speed60V;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("直线行驶");
        back.setOnClickListener(this);
        BlueManager.getInstance().send(ProtocolUtils.run());
    }

    @Subscriber(tag = EventBusTags.COLLECT_DIRECT_EVENT)
    private void updateCollectDirectStauts(int type) {
        switch (type) {
            case 0:
                speed20.setVisibility(View.VISIBLE);
                break;
            case 1:
                speed20To60V.setVisibility(View.VISIBLE);
                break;
            case 2:
                speed60V.setVisibility(View.VISIBLE);
                break;
            case 3:
                PageManager.go(new CollectTurnFinish());
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
        }
    }


}
