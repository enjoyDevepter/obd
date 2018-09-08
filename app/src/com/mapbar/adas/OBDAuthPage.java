package com.mapbar.adas;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.ProtocolUtils;
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

@PageSetting(contentViewId = R.layout.obd_auth_layout, toHistory = false)
public class OBDAuthPage extends AppBasePage implements BleCallBackListener, LocationListener, View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    private volatile boolean verified;
    private CustomDialog dialog;

    private LocationManager locationManager;

    private volatile boolean needNotifyParamsSuccess;
    private volatile boolean needNotifyVerifiedSuccess;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("授权检查");
        reportV.setOnClickListener(this);
        back.setVisibility(View.GONE);
        verify();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);
    }

    @Override
    public void onStart() {
        super.onStart();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    /**
     * 授权
     */
    private void verify() {
        showProgress();
        final Request request = new Request.Builder().url(URLUtils.GET_TIME).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("getOBDStatus fail " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getOBDStatus success " + responese);
                try {
                    JSONObject result = new JSONObject(responese);
                    if (verified) {
                        return;
                    }
                    verified = true;
                    BlueManager.getInstance().send(ProtocolUtils.getOBDStatus(Long.valueOf(result.optString("server_time"))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        verified = false;
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.UNREGISTERED://未注册
                dismissProgress();
//                // 激活
                OBDStatusInfo obdStatusInfo = (OBDStatusInfo) data;
                Log.d("obdStatusInfo  " + obdStatusInfo);
                OBDInitPage obdInitPage = new OBDInitPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", obdStatusInfo.getBoxId());
                obdInitPage.setDate(bundle);
                PageManager.go(obdInitPage);
                break;
            case OBDEvent.AUTHORIZATION: //未授权或者授权过期
                // 获取授权码
                getLisense(((OBDStatusInfo) data).getSn());
                break;
            case OBDEvent.AUTHORIZATION_SUCCESS:
                authSuccess((OBDStatusInfo) data);
                break;
            case OBDEvent.AUTHORIZATION_FAIL:
                authFail("授权失败!请联系客服!");
            case OBDEvent.NO_PARAM: // 无参数
                checkOBDVersion((OBDStatusInfo) data);
                break;
            case OBDEvent.PARAM_UPDATE_SUCCESS:
                notifyUpdateSuccess((OBDStatusInfo) data);
                break;
            case OBDEvent.PARAM_UPDATE_FAIL:
                break;
            case OBDEvent.CURRENT_MISMATCHING:
                dismissProgress();
                PageManager.go(new ProtocolCheckFailPage());
                break;
            case OBDEvent.UN_ADJUST:
                dismissProgress();
                PageManager.go(new ConfirmPage());
                break;
            case OBDEvent.UN_LEGALITY:
                authFail("请从正规渠道购买!");
                break;
            case OBDEvent.NORMAL:
                dismissProgress();
                MainPage mainPage = new MainPage();
                Bundle mainBundle = new Bundle();
                mainBundle.putSerializable("obdStatusInfo", (OBDStatusInfo) data);
                mainPage.setDate(mainBundle);
                PageManager.go(mainPage);
                break;
        }
    }


    /**
     * 获取授权码
     *
     * @param sn
     */
    private void getLisense(final String sn) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("getLisense input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.GET_LISENSE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        ((TextView) (view.findViewById(R.id.confirm))).setText("授权");
                                        ((TextView) (view.findViewById(R.id.info))).setText("OBD盒子已过期,请开启网络重新授权!");
                                        ((TextView) (view.findViewById(R.id.title))).setText("盒子过期");
                                        final View confirm = view.findViewById(R.id.confirm);
                                        confirm.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                // 获取授权码
                                                showProgress();
                                                confirm.setEnabled(false);
                                                getLisense(sn);
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
                Log.d("getLisense success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        needNotifyVerifiedSuccess = true;
                        String code = result.optString("rightStr");
                        BlueManager.getInstance().send(ProtocolUtils.auth(sn, code));
                    } else {
                        authFail(result.optString("message"));
                    }
                } catch (JSONException e) {
                    Log.d("getLisense failure " + e.getMessage());
                }
            }
        });
    }

    /**
     * 授权失败
     *
     * @param reason
     */
    private void authFail(final String reason) {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                dismissProgress();
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) (view.findViewById(R.id.confirm))).setText("确认");
                                ((TextView) (view.findViewById(R.id.info))).setText(reason);
                                ((TextView) (view.findViewById(R.id.title))).setText("授权失败");
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        // 退出应用
                                        PageManager.finishActivity(MainActivity.getInstance());
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

    /**
     * 激活成功
     */
    private void authSuccess(final OBDStatusInfo obdStatusInfo) {

        if (!needNotifyVerifiedSuccess) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("activate success input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.ACTIVATE_SUCCESS)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
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
                                ((TextView) (view.findViewById(R.id.confirm))).setText("重试");
                                ((TextView) (view.findViewById(R.id.info))).setText("网络异常,请检查网络状态后重试!");
                                ((TextView) (view.findViewById(R.id.title))).setText("网络异常");
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        showProgress();
                                        authSuccess(obdStatusInfo);
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
                        // 协议匹配检查（通过获取胎压信息）
                        needNotifyVerifiedSuccess = false;
                    } else {
                        Log.d("activate failure" + result.optString("message"));
                    }
                } catch (JSONException e) {
                    Log.d("activate failure " + e.getMessage());
                }
            }
        });
    }

    /**
     * 通知服务器固件升级完成
     */
    private void notifyUpdateSuccess(OBDStatusInfo obdStatusInfo) {
        if (!needNotifyParamsSuccess) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("bVersion", obdStatusInfo.getbVersion());
            jsonObject.put("pVersion", obdStatusInfo.getpVersion());
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
                Log.d("notifyUpdateSuccess success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        needNotifyParamsSuccess = false;
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

    @Override
    public void onLocationChanged(Location location) {
        if ("gps".equalsIgnoreCase(location.getProvider())) {
            Log.d("onLocationChanged ");
            locationManager.removeUpdates(this);
            if (verified) {
                return;
            }
            verified = true;
            BlueManager.getInstance().send(ProtocolUtils.getOBDStatus(location.getTime()));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:

                break;
        }
    }

    private void checkOBDVersion(final OBDStatusInfo obdStatusInfo) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("bVersion", obdStatusInfo.getbVersion());
            jsonObject.put("pVersion", obdStatusInfo.getpVersion());
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
                final OBDVersion obdVersion = JSON.parseObject(responese, OBDVersion.class);
                GlobalUtil.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ("000".equals(obdVersion.getStatus())) {
                            switch (obdVersion.getUpdateState()) {
                                case 1:
                                case 2:
                                case 3: // 只有参数更新
                                    needNotifyParamsSuccess = true;
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
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
}
