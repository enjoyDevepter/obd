package com.mapbar.adas;

import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.obd.R;

@PageSetting(contentViewId = R.layout.connect_layout)
public class ConnectPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.connect)
    private ImageView connect;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("方舟卫士");
    }

    @Override
    public void onStart() {
        super.onStart();
        connect.setBackgroundResource(R.drawable.connect_bg);
        AnimationDrawable animationDrawable = (AnimationDrawable) connect.getBackground();
        animationDrawable.start();
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }
}
