package com.mapbar.adas;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
                check();
                break;
        }
    }

    private void check() {

        String sn01 = sn_01.getText().toString();
        String sn02 = sn_02.getText().toString();
        String sn03 = sn_03.getText().toString();
        String sn04 = sn_04.getText().toString();

        if (GlobalUtil.isEmpty(sn01) || GlobalUtil.isEmpty(sn02) || GlobalUtil.isEmpty(sn03) || GlobalUtil.isEmpty(sn04)) {
            Toast.makeText(getContext(), "请输入授权码", Toast.LENGTH_LONG).show();
            return;
        }
        next.setEnabled(false);
        final StringBuilder sn = new StringBuilder();
        sn.append(sn01).append("-").append(sn02).append("-").append(sn03).append("-").append(sn04);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("check sn input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder().add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .addHeader("content-type", "application/json;charset:utf-8")
                .url(URLUtils.SN_CHECK).post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("sn_check failure " + e.getMessage());
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(GlobalUtil.getMainActivity())
                                .setMessage("网络异常,请检查网络状态后重试!")
                                .setTitle("网络异常")
                                .setNegativeButton("重试", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        check();
                                    }
                                })
                                .create().show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d(responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        ChoiceCarPage choiceCarPage = new ChoiceCarPage();
                        Bundle bundle = new Bundle();
                        bundle.putString("boxId", getDate().getString("boxId"));
                        bundle.putString("phone", getDate().getString("phone"));
                        bundle.putString("code", getDate().getString("code"));
                        bundle.putString("sn", sn.toString());
                        choiceCarPage.setDate(bundle);
                        PageManager.go(choiceCarPage);
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                next.setEnabled(true);
                                Toast.makeText(GlobalUtil.getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("sn_check failure " + e.getMessage());
                }
            }
        });

    }
}
