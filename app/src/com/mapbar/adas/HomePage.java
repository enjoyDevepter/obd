package com.mapbar.adas;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.home_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class HomePage extends AppBasePage implements View.OnClickListener {
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.trie)
    private View trieV;
    @ViewInject(R.id.fault)
    private View faultV;
    @ViewInject(R.id.physical)
    private View physicalV;
    @ViewInject(R.id.dash)
    private View dashV;
    @ViewInject(R.id.message)
    private View messageV;
    @ViewInject(R.id.hud)
    private View hudV;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        trieV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        physicalV.setOnClickListener(this);
        dashV.setOnClickListener(this);
        messageV.setOnClickListener(this);
        hudV.setOnClickListener(this);
        title.setText("汽车卫士");
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(MainActivity.getInstance().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? android.R.color.black : R.color.main_title_color)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trie:
                MainPage mainPage = new MainPage();
                Bundle mainBundle = new Bundle();
                mainBundle.putSerializable("obdStatusInfo", getDate().getSerializable("obdStatusInfo"));
                mainPage.setDate(mainBundle);
                PageManager.go(mainPage);
                break;
            case R.id.fault:
                break;
            case R.id.physical:
                PageManager.go(new PhysicalPage());
                break;
            case R.id.dash:
                break;
            case R.id.message:
                break;
            case R.id.hud:
                break;
        }
    }


    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }
}
