package com.miyuan.obd;

import android.view.View;

import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;


@PageSetting(contentViewId = R.layout.fm_operation_info, toHistory = false)
public class FMOperationInfoPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.confirm)
    private View confirmV;
    @ViewInject(R.id.home)
    private View homeV;
    @ViewInject(R.id.info)
    private View infoV;

    @Override
    public void onResume() {
        super.onResume();
        back.setOnClickListener(this);
        homeV.setOnClickListener(this);
        infoV.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                PageManager.go(new FMSetPage());
                break;
            case R.id.home:
                PageManager.clearHistoryAndGo(new HomePage());
                break;
            case R.id.info:
                PageManager.go(new FMInfoPage());
                break;
            default:
                break;
        }
    }
}
