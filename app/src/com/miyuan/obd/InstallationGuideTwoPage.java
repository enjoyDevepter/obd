package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.installation_guide_two_layout)
public class InstallationGuideTwoPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.first)
    TextView firstTV;
    @ViewInject(R.id.second)
    TextView secondTV;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private Timer timer;
    private int time = 15;
    private boolean ishow;
    private boolean timeOut;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title.setText("安装引导二");
        back.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
        firstTV.setText(Html.fromHtml("第一步：<font color='#009488'>请检查轮胎胎压一致</font><br><font color='#4A4A4A'>（建议使用胎压计测量）</font><br><br>"));
        secondTV.setText(Html.fromHtml("第二步：<font color='#4A4A4A'>请您准备好以下工具</font><br>1、<font color='#009488'>手机</font><font color='#4A4A4A'>（注册收取验证码）</font><br>2、<font color='#009488'>包装盒</font><font color='#4A4A4A'>（获取授权码）</font>"));
        if (!ishow) {
            confirmV.setEnabled(ishow);
            ishow = true;
            timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (time <= 0 && timer != null) {
                                timeOut = true;
                                timer.cancel();
                                timer = null;
                                confirmV.setText("我已准备好了");
                                confirmV.setEnabled(true);
                                confirmV.setOnClickListener(InstallationGuideTwoPage.this);
                            } else {
                                confirmV.setText("我已准备好了(" + time + "s)");
                            }
                            time--;
                        }
                    });
                }
            }, 1000, 1000);
        } else {
            if (timeOut) {
                confirmV.setEnabled(ishow);
                confirmV.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        if (null != timer) {
            timer.cancel();
            timer = null;
        }
        super.onDestroy();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                AuthPage authPage = new AuthPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", getDate().getString("boxId"));
                authPage.setDate(bundle);
                PageManager.go(authPage);
                break;
        }
    }

}
