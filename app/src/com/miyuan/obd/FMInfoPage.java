package com.miyuan.obd;

import android.view.View;
import android.widget.TextView;

import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;


@PageSetting(contentViewId = R.layout.fm_info)
public class FMInfoPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private View confirmV;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("使用帮助");
        reportV.setVisibility(View.GONE);
        back.setOnClickListener(this);
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
                PageManager.go(new FMPage());
                break;
            default:
                break;
        }
    }
}
