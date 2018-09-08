package com.mapbar.adas;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
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
    @ViewInject(R.id.img_loading_20)
    private ImageView iamge20;
    @ViewInject(R.id.speed20To60)
    private View speed20To60V;
    @ViewInject(R.id.speed60)
    private View speed60V;
    @ViewInject(R.id.img_loading_60)
    private ImageView iamge60;
    private Animation operatingAnim;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("直线行驶");
        back.setOnClickListener(this);
        operatingAnim = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        iamge20.startAnimation(operatingAnim);
        operatingAnim.setInterpolator(new LinearInterpolator());
        BlueManager.getInstance().send(ProtocolUtils.run());
    }

    @Subscriber(tag = EventBusTags.COLLECT_DIRECT_EVENT)
    private void updateCollectDirectStauts(int type) {
        switch (type) {
            case 0:
                iamge20.clearAnimation();
                iamge20.setVisibility(View.INVISIBLE);
                speed20.setVisibility(View.VISIBLE);
                iamge60.setVisibility(View.VISIBLE);
                iamge60.startAnimation(operatingAnim);
                break;
            case 1:
                speed20To60V.setVisibility(View.VISIBLE);
                break;
            case 2:
                iamge60.setVisibility(View.INVISIBLE);
                speed60V.setVisibility(View.VISIBLE);
                break;
            case 3:
                iamge60.clearAnimation();
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
