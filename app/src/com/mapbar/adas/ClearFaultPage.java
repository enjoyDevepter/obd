package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;


@PageSetting(contentViewId = R.layout.clear_fault_layout)
public class ClearFaultPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView titleTV;
    @ViewInject(R.id.back)
    private View backV;
    @ViewInject(R.id.home)
    private View homeV;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.info)
    private TextView infoTV;
    @ViewInject(R.id.confirm)
    private View confirmV;

    private CustomDialog dialog;


    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        titleTV.setText("清除须知及免责申明");
        backV.setOnClickListener(this);
        homeV.setOnClickListener(this);
        confirmV.setOnClickListener(this);
        reportV.setVisibility(View.INVISIBLE);
        infoTV.setText(Html.fromHtml("<font color='#4B4B4B'>1、清除故障码后，可能会重置汽车的某些参数，进而引起仪表盘灯短时间亮灯。<br><br>" +
                "2、部分故障是在一些偶发因素下产生的，可能不会再复现，清除之后也不会再出现。<br><br>" +
                "3、清除故障码不代表解决故障。清除故障码，只是不再显示，既有的故障不会因清除故障而得到解决。" +
                "<br><br></font><font color='#35BDB2'>4、如果清除之后，同一故障码反复出现，说明该故障码仍然存在，请第一时间找维修机构维修处理。</font><br><br><font color='#4B4B4B'>" +
                "5、为了保存您的历史记录，我们会在云端存储你的故障码信息。<br><br>" +
                "6、清除故障码之前，建议与专业维修人员确认收再操作，因清除故障码引起的车辆问题我们不承担任何责任。</font><br><br>"));
    }

    @Override
    public void onStart() {
        BlueManager.getInstance().addBleCallBackListener(this);
        super.onStart();
    }


    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.confirm:
                BlueManager.getInstance().send(ProtocolUtils.clearFaultCode());
                confirmV.setEnabled(false);
                break;
            case R.id.home:
                PageManager.go(new HomePage());
                break;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.CLEAN_FAULT_CODE:
                int status = (int) data;
                if (status == 0) {
                    showSuccessDialog();
                }
                break;
        }
    }

    private void showSuccessDialog() {
        if (dialog != null && dialog.isVisible()) {
            return;
        }
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View confirm = view.findViewById(R.id.save);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PageManager.go(new FaultCodePage());
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_fault)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(MainActivity.getInstance(), R.dimen.dailog_width))
                .show();
    }
}
