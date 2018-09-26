package com.mapbar.adas;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

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

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.next)
    private TextView next;
    @ViewInject(R.id.scan)
    private View scanV;
    @ViewInject(R.id.sn_01)
    private EditText sn_01;
    @ViewInject(R.id.sn_02)
    private EditText sn_02;
    @ViewInject(R.id.sn_03)
    private EditText sn_03;
    @ViewInject(R.id.sn_04)
    private EditText sn_04;
    private CustomDialog dialog;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("输入授权码");
        next.setOnClickListener(this);
        scanV.setOnClickListener(this);
        back.setOnClickListener(this);
        reportV.setVisibility(View.GONE);

        if (getDate() != null && null != getDate().get("sn")) {
            String sn = (String) getDate().get("sn");
            String[] sns = sn.split("-");
            if (sns.length < 4) {
                Toast.makeText(getContext(), "识别错误", Toast.LENGTH_LONG).show();
                return;
            }
            sn_01.setText(sns[0]);
            sn_02.setText(sns[1]);
            sn_03.setText(sns[2]);
            sn_04.setText(sns[3]);
        }
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
            case R.id.scan:
                Intent intent = new Intent(GlobalUtil.getMainActivity(), CaptureActivity.class);
                GlobalUtil.getMainActivity().startActivityForResult(intent, 0);
                break;
            case R.id.report:
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
        showProgress();
        next.setEnabled(false);
        final StringBuilder sn = new StringBuilder();
        sn.append(sn01).append("-").append(sn02).append("-").append(sn03).append("-").append(sn04);

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn.toString());
            jsonObject.put("boxId", getDate().getString("boxId"));
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
                        dismissProgress();
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        ((TextView) (view.findViewById(R.id.confirm))).setText("已打开网络，重试");
                                        ((TextView) (view.findViewById(R.id.info))).setText("请打开网络，否则无法完成当前操作!");
                                        ((TextView) (view.findViewById(R.id.title))).setText("网络异常");
                                        final View confirm = view.findViewById(R.id.confirm);
                                        confirm.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                showProgress();
                                                confirm.setEnabled(false);
                                                check();
                                            }
                                        });
                                    }
                                })
                                .setLayoutRes(R.layout.dailog_common_warm)
                                .setCancelOutside(false)
                                .setDimAmount(0.5f)
                                .isCenter(true)
                                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                                .show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d(responese);
                try {
                    dismissProgress();
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        PhonePage page = new PhonePage();
                        Bundle bundle = new Bundle();
                        bundle.putString("boxId", getDate().getString("boxId"));
                        bundle.putString("sn", sn.toString());
                        page.setDate(bundle);
                        PageManager.go(page);
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
