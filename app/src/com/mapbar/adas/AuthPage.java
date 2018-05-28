package com.mapbar.adas;

import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.obd.R;

@PageSetting(contentViewId = R.layout.auth_layout)
public class AuthPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.content)
    private EditText content;
    @ViewInject(R.id.next)
    private TextView next;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("输入授权码");
        next.setOnClickListener(this);
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
                PageManager.go(new IdentifyPage());
                break;
        }
    }
}
