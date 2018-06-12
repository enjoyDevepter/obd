package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.phone_layout)
public class PhonePage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.content)
    private EditText content;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.back)
    private View back;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("输入手机号");
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
                getMSN();
                break;
        }
    }

    private void getMSN() {

        final String phone = content.getText().toString();
        if (GlobalUtil.isEmpty(phone)) {
            Toast.makeText(getContext(), "请输入手机号码", Toast.LENGTH_LONG).show();
            return;
        }

        if (!GlobalUtil.isPhone(phone)) {
            Toast.makeText(getContext(), "请输入正确的手机号码", Toast.LENGTH_LONG).show();
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("phone", phone);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.GET_SMS).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("get MSN failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d(responese);
                try {
                    JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        IdentifyPage page = new IdentifyPage();
                        Bundle bundle = new Bundle();
                        bundle.putString("boxId", getDate().getString("boxId"));
                        bundle.putString("phone", phone);
                        page.setDate(bundle);
                        PageManager.go(page);
                    }
                } catch (JSONException e) {
                    Log.d("get MSN failure " + e.getMessage());
                }
            }
        });

    }
}
