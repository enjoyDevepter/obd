package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.obd.R;

@PageSetting(contentViewId = R.layout.identifying_layout)
public class IdentifyPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.content)
    private EditText content;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.phone)
    private TextView phone;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("输入验证码");
        next.setOnClickListener(this);
        back.setOnClickListener(this);
        if (getDate() != null) {
            phone.setText(getDate().getString("phone"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back:
                PageManager.back();
                break;
            case R.id.next:
                goAuth();
                break;
        }
    }

    private void goAuth() {
        String identify = content.getText().toString();
        if (GlobalUtil.isEmpty(identify)) {
            Toast.makeText(getContext(), "请输入验证码", Toast.LENGTH_LONG).show();
            return;
        }
        AuthPage authPage = new AuthPage();
        Bundle bundle = new Bundle();
        bundle.putString("boxId", getDate().getString("boxId"));
        bundle.putString("phone", getDate().getString("phone"));
        bundle.putString("code", identify);
        authPage.setDate(bundle);
        PageManager.go(authPage);

    }
}
