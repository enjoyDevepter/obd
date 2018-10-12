package com.mapbar.adas;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import java.util.Timer;
import java.util.TimerTask;

@PageSetting(contentViewId = R.layout.collect_one_layout)
public class CollectOnePage extends AppBasePage implements View.OnClickListener {
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    @ViewInject(R.id.cancel)
    private View cancelV;
    @ViewInject(R.id.info)
    private TextView infoTV;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private int time = 15;
    private boolean ishow;
    private boolean timeOut;

    @Override
    public void onResume() {
        super.onResume();
        back.setOnClickListener(this);
        cancelV.setOnClickListener(this);
        reportV.setVisibility(View.GONE);
        title.setText("深度校准步骤");
        infoTV.setText(Html.fromHtml("<font color='#4A4A4A'>第一步：提速至20km/h;<br>第二步：掉头；<br>第三步：缓慢提速至60km/h以上并</font><font color='#009488'>保持直线行驶</font>若干分钟，直至校准完成！<br><br>注意：<font color='#009488'>请不要急加速或急减速！</font><br><br><font color='#4A4A4A'>请在安全行驶的前提下操作！<br>校准完成后APP会语音提示您！</font><br><br>"));
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
                                timeOut = true;
                                timer.cancel();
                                timer = null;
                                timerTask.cancel();
                                timerTask = null;
                                confirmV.setText("马上开始");
                                confirmV.setEnabled(true);
                                confirmV.setOnClickListener(CollectOnePage.this);
                            } else {
                                if (time == 5) {
                                    BlueManager.getInstance().send(ProtocolUtils.run());
                                }
                                confirmV.setText("马上开始(" + time + "s)");
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
                CollectTwoPage collectTwoPage = new CollectTwoPage();
                Bundle bundle = new Bundle();
                bundle.putBoolean("matching", getDate().getBoolean("matching"));
                bundle.putString("sn", getDate().getString("sn"));
                bundle.putString("pVersion", getDate().getString("pVersion"));
                bundle.putString("bVersion", getDate().getString("bVersion"));
                collectTwoPage.setDate(bundle);
                PageManager.go(collectTwoPage);
                break;
            case R.id.cancel:
                PageManager.finishActivity(MainActivity.getInstance());
                break;
        }
    }
}
