package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.obd.R;

@PageSetting(contentViewId = R.layout.auth_layout)
public class AuthPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.sn_01)
    private EditText sn_01;
    @ViewInject(R.id.sn_02)
    private EditText sn_02;
    @ViewInject(R.id.sn_03)
    private EditText sn_03;
    @ViewInject(R.id.sn_04)
    private EditText sn_04;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("输入授权码");
        next.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.next:
                choiceCar();
                break;
        }
    }

    private void choiceCar() {

        String sn01 = sn_01.getText().toString();
        String sn02 = sn_02.getText().toString();
        String sn03 = sn_03.getText().toString();
        String sn04 = sn_04.getText().toString();

        if (GlobalUtil.isEmpty(sn01) || GlobalUtil.isEmpty(sn02) || GlobalUtil.isEmpty(sn03) || GlobalUtil.isEmpty(sn04)) {
            Toast.makeText(getContext(), "请输入SN", Toast.LENGTH_LONG).show();
            return;
        }

        StringBuilder sn = new StringBuilder();
        sn.append(sn01).append("-").append(sn02).append("-").append(sn03).append("-").append(sn04);

        ChoiceCarPage choiceCarPage = new ChoiceCarPage();
        Bundle bundle = new Bundle();
        bundle.putString("boxId", getDate().getString("boxId"));
        bundle.putString("phone", getDate().getString("phone"));
        bundle.putString("code", getDate().getString("code"));
        bundle.putString("sn", sn.toString());
        choiceCarPage.setDate(bundle);
        PageManager.go(choiceCarPage);

    }
}
