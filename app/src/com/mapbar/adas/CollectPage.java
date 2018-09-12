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

    @Override
    public void onResume() {
        super.onResume();
        title.setText("深度校准");
        back.setVisibility(View.GONE);
        BlueManager.getInstance().send(ProtocolUtils.run());
        showProgress();
    }

    @Subscriber(tag = EventBusTags.COLLECT_DIRECT_EVENT)
    private void updateCollectDirectStauts(int type) {
        dismissProgress();
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
