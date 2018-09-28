package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.miyuan.obd.R;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

@PageSetting(contentViewId = R.layout.collect_two_layout, toHistory = false)
public class CollectTwoPage extends AppBasePage {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;

    @Override
    public void onResume() {
        super.onResume();
        reportV.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        title.setText("开始校准");
        EventBus.getDefault().register(this);
        EventBus.getDefault().post(getDate().getBoolean("matching"), EventBusTags.START_COLLECT);
        GlobalUtil.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmManager.getInstance().play(R.raw.start_adjust);
            }
        }, 2000);
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return super.onBackPressed();
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscriber(tag = EventBusTags.COLLECT_FINISHED)
    private void updateCollectStauts(int type) {
        boolean matching = getDate().getBoolean("matching");
        if (matching) {
            PageManager.go(new CollectPage());
        } else {
            CollectFinish collectFinish = new CollectFinish();
            Bundle bundle = new Bundle();
            bundle.putString("sn", getDate().getString("sn"));
            bundle.putString("pVersion", getDate().getString("pVersion"));
            bundle.putString("bVersion", getDate().getString("bVersion"));
            bundle.putBoolean("success", false);
            collectFinish.setDate(bundle);
            PageManager.go(collectFinish);
        }

    }
}
