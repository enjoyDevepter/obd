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

@PageSetting(contentViewId = R.layout.installation_guide_layout)
public class InstallationGuidePage extends AppBasePage implements View.OnClickListener {

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
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private int time = 10;
    private boolean ishow;
    private boolean timeOut;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title.setText("安装引导一");
        reportV.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        firstTV.setText(Html.fromHtml("第一步：<font color='#009488'>请停车拉手刹!</font><br><font color='#4A4A4A'>自动挡挂P档、并拉手刹；手动挡挂空挡、并拉手刹。</font><br><br>"));
        secondTV.setText(Html.fromHtml("第二步：<font color='#009488'>请将车辆打火!</font><br><font color='#4A4A4A'>请确保车辆已打火</font><br><br>请完成以上操作后，再点击确认按钮！<br>否则会导致安装失败！"));
        if (!ishow) {
            confirmV.setEnabled(ishow);
            ishow = true;
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (time <= 0 && timer != null) {
                                timer.cancel();
                                timer = null;
                                timerTask.cancel();
                                timerTask = null;
                                timeOut = true;
                                confirmV.setText("确认已拉手刹、并打火");
                                confirmV.setEnabled(true);
                                confirmV.setOnClickListener(InstallationGuidePage.this);
                            } else {
                                confirmV.setText("确认已拉手刹、并打火(" + time + "s)");
                            }
                            time--;
                        }
                    });
                }
            };
            timer.schedule(timerTask, 0, 1000);
        } else {
            if (timeOut) {
                confirmV.setEnabled(ishow);
                confirmV.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onDestroy() {
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
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
                InstallationGuideTwoPage authPage = new InstallationGuideTwoPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", getDate().getString("boxId"));
                authPage.setDate(bundle);
                PageManager.go(authPage);
                break;
        }
    }

}
