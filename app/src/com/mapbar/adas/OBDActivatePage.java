package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.preferences.SettingPreferencesConfig;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDVersionInfo;
import com.mapbar.hamster.core.ProtocolUtils;
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

@PageSetting(contentViewId = R.layout.obd_activate_layout)
public class OBDActivatePage extends AppBasePage implements BleCallBackListener {

    String carName = "";
    OBDVersion obdVersion;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    private CustomDialog dialog;
    private String sn;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("正在激活胎压盒子");
        back.setVisibility(View.GONE);
        activate();
    }

    @Override
    public void onStart() {
        super.onStart();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }


    private void activate() {

        showProgress();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("boxId", getDate().get("boxId"));
            jsonObject.put("phone", getDate().get("phone"));
            jsonObject.put("code", getDate().get("code"));
            jsonObject.put("carId", getDate().get("carId"));
            jsonObject.put("serialNumber", getDate().get("sn"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("activate input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .url(URLUtils.ACTIVATE)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("activate failure " + e.getMessage());
                dismissProgress();
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                activate();
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
                Log.d("activate success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        String code = result.optString("rightStr");
                        SettingPreferencesConfig.CAR.set(carName);
                        SettingPreferencesConfig.PHONE.set(String.valueOf(getDate().get("phone")));
                        BlueManager.getInstance().send(ProtocolUtils.auth(getDate().getString("sn"), code));
                    }
                } catch (JSONException e) {
                    Log.d("activate failure " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_GET_VERSION:
                OBDVersionInfo version = (OBDVersionInfo) data;
                sn = version.getSn();
                checkOBDVersion(version);
                break;
            case OBDEvent.OBD_AUTH_RESULT:
                // 授权结果
                if ((Integer) data == 1) {
                    Log.d("activateSuccess  ");
                    activateSuccess();
                }
                break;
            case OBDEvent.OBD_UPDATE_PARAMS_SUCCESS:
                notifyUpdateSuccess(obdVersion);
                break;
        }
    }


    /**
     * 通知服务器固件升级完成
     */
    private void notifyUpdateSuccess(OBDVersion obdVersion) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
            jsonObject.put("bVersion", obdVersion.getbVersion());
            jsonObject.put("pVersion", obdVersion.getpVersion());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("notifyUpdateSuccess input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE_SUCCESS)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("notifyUpdateSuccess failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                dismissProgress();
                Log.d("notifyUpdateSuccess success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        OBDAuthPage authPage = new OBDAuthPage();
                        Bundle bundle = new Bundle();
                        bundle.putBoolean("showStudy", true);
                        authPage.setDate(bundle);
                        PageManager.go(authPage);
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("notifyUpdateSuccess failure " + e.getMessage());
                }
            }
        });
    }

    private void checkOBDVersion(final OBDVersionInfo version) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", version.getSn());
            jsonObject.put("bVersion", version.getVersion());
            jsonObject.put("pVersion", version.getCar_no());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("checkOBDVersion input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkOBDVersion failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkOBDVersion success " + responese);
                obdVersion = JSON.parseObject(responese, OBDVersion.class);
                GlobalUtil.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ("000".equals(obdVersion.getStatus())) {
                            switch (obdVersion.getUpdateState()) {
                                case 1:
                                case 2:
                                case 3: // 只有参数更新
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(version.getSn(), obdVersion.getParams()));
                                    break;
                            }
                        } else {
                            Toast.makeText(getContext(), obdVersion.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 1500);
            }
        });
    }

    /**
     * 激活成功
     */
    private void activateSuccess() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", getDate().get("sn"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("activate input  " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.ACTIVATE_SUCCESS)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("activate failure " + e.getMessage());
                dismissProgress();
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        showProgress();
                                        activate();
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

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("activate success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        BlueManager.getInstance().send(ProtocolUtils.getVersion());
                    } else {
                        Log.d("activate failure");
                    }
                } catch (JSONException e) {
                    Log.d("activate failure " + e.getMessage());
                }
            }
        });
    }
}
