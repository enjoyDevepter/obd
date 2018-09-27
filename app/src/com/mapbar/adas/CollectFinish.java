package com.mapbar.adas;

import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.miyuan.obd.R;

@PageSetting(contentViewId = R.layout.collect_finish_layout)
public class CollectFinish extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.success)
    private TextView successTV;
    @ViewInject(R.id.fail)
    private TextView failTV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private boolean success;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        confirmV.setOnClickListener(this);
        success = getDate().getBoolean("success");
        if (success) {
            title.setText("深度校准成功");
            confirmV.setText("关闭");
            successTV.setText(Html.fromHtml("<font color='#4A4A4A'>恭喜您！胎压盒子可以正常使用了！<br><br><br>当轮胎亏气时,</font><font color='#009488'>胎压盒子会发出连续蜂鸣声！</font><font color='#4A4A4A'>此时您需要停车并用APP连接盒子，点击“校准”后可以停止蜂鸣！如果继续亏气，仍然会再次蜂鸣！</font>"));
            failTV.setVisibility(View.GONE);
        } else {
            title.setText("深度校准失败");
            confirmV.setText("提交并申请支持开发");
            failTV.setText(Html.fromHtml("<font color='#4A4A4A'>抱歉，您的车辆与盒子不匹配!<br><br><br>请您点击提交按钮，我们会在第一时间寻找同款车型并在24-48小时内完成相关开发工作！<br><br>开发完成后我们会电话通知您！<br>届时您只需要重新联网升级即可！</font>"));
            successTV.setVisibility(View.GONE);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                if (success) {
                    PageManager.go(new OBDAuthPage());
                } else {
                    PageManager.finishActivity(MainActivity.getInstance());
                }
                break;
        }
    }
}
