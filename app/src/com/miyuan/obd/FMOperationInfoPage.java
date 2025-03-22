package com.miyuan.obd;

import android.view.View;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;


@PageSetting(contentViewId = R.layout.fm_operation_info)
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
        GlobalUtil.changeBarColor(R.color.white);
        back.setOnClickListener(this);
        homeV.setOnClickListener(this);
        infoV.setOnClickListener(this);
        confirmV.setOnClickListener(this);
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
