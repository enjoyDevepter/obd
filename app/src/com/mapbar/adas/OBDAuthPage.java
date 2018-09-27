package com.mapbar.adas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.obd_auth_layout, toHistory = false)
public class OBDAuthPage extends AppBasePage implements BleCallBackListener, LocationListener, View.OnClickListener {

    OBDStatusInfo obdStatusInfo;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.status)
    private View statusV;
    private volatile boolean verified;
    private CustomDialog dialog;
    private LocationManager locationManager;
    private volatile boolean needNotifyParamsSuccess;
    private volatile boolean needNotifyVerifiedSuccess;

    private AnimationDrawable animationDrawable;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("获取盒子状态");
        reportV.setOnClickListener(this);
        back.setVisibility(View.GONE);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        animationDrawable.start();
        verify();
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
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
        animationDrawable.stop();
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
//                // 激活
                obdStatusInfo = (OBDStatusInfo) data;
                Log.d("obdStatusInfo  " + obdStatusInfo);
                InstallationGuidePage obdInitPage = new InstallationGuidePage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", obdStatusInfo.getBoxId());
                obdInitPage.setDate(bundle);
                PageManager.go(obdInitPage);
                break;
            case OBDEvent.AUTHORIZATION: //未授权或者授权过期
                obdStatusInfo = (OBDStatusInfo) data;
                // 获取授权码
                getLisense();
                break;
            case OBDEvent.AUTHORIZATION_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                authSuccess();
                break;
            case OBDEvent.AUTHORIZATION_FAIL:
                authFail("授权失败!请联系客服!");
                uploadLog();
            case OBDEvent.NO_PARAM: // 无参数
                obdStatusInfo = (OBDStatusInfo) data;
                checkOBDVersion();
                break;
            case OBDEvent.PARAM_UPDATE_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                notifyUpdateSuccess();
                break;
            case OBDEvent.PARAM_UPDATE_FAIL:
                break;
            case OBDEvent.CURRENT_MISMATCHING:
                ProtocolCheckFailPage checkFailPage = new ProtocolCheckFailPage();
                Bundle checkFailPageBundle = new Bundle();
                checkFailPageBundle.putBoolean("before_matching", false);
                checkFailPage.setDate(checkFailPageBundle);
                PageManager.go(checkFailPage);
                break;
            case OBDEvent.BEFORE_MATCHING:
                ProtocolCheckFailPage protocolCheckFailPage = new ProtocolCheckFailPage();
                Bundle protocolCheckFailPageBundle = new Bundle();
                protocolCheckFailPageBundle.putBoolean("before_matching", true);
                protocolCheckFailPage.setDate(protocolCheckFailPageBundle);
                PageManager.go(protocolCheckFailPage);
                break;
            case OBDEvent.UN_ADJUST:
                CollectGuide collectGuide = new CollectGuide();
                Bundle collectBundle = new Bundle();
                collectBundle.putBoolean("matching", true);
                collectBundle.putString("sn", obdStatusInfo.getSn());
                collectGuide.setDate(collectBundle);
                PageManager.go(collectGuide);
                break;
            case OBDEvent.NORMAL:
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
     */
    private void getLisense() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
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
                                                confirm.setEnabled(false);
                                                getLisense();
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
                        BlueManager.getInstance().send(ProtocolUtils.auth(obdStatusInfo.getSn(), code));
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                StatusInfoPage statusInfoPage = new StatusInfoPage();
                                Bundle bundle1 = new Bundle();
                                bundle1.putBoolean("fake", true);
                                statusInfoPage.setDate(bundle1);
                                PageManager.go(statusInfoPage);
                            }
                        });
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
    private void authSuccess() {

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
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
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
                                                confirm.setEnabled(false);
                                                authSuccess();
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
    private void notifyUpdateSuccess() {
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
                uploadLog();
                break;
        }
    }

    private void checkOBDVersion() {

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
                if ("000".equals(obdVersion.getStatus())) {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            switch (obdVersion.getpUpdateState()) {
                                case 0:  // 无参数更新
                                    break;
                                case 1: // 有更新
                                    needNotifyParamsSuccess = true;
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
                                    break;
                                case 2: // 临时车型，需要采集
                                    CollectGuide collectGuide = new CollectGuide();
                                    Bundle bundle = new Bundle();
                                    bundle.putBoolean("matching", false);
                                    bundle.putString("sn", obdStatusInfo.getSn());
                                    collectGuide.setDate(bundle);
                                    PageManager.go(collectGuide);
                                    break;
                                case 3: // 临时车型，参数已采集
                                    StatusInfoPage statusInfoPage = new StatusInfoPage();
                                    Bundle bundle1 = new Bundle();
                                    bundle1.putBoolean("fake", false);
                                    statusInfoPage.setDate(bundle1);
                                    PageManager.go(statusInfoPage);
                                    break;
                            }
                        }
                    });
                } else {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), obdVersion.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    private void uploadLog() {
        Log.d("OBDAuthPage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                    .addPart(MultipartBody.Part.createFormData("type", "1"));
            for (File file : logs) {
                builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            }
            Request request = new Request.Builder()
                    .url(URLUtils.UPDATE_ERROR_FILE)
                    .post(builder.build())
                    .build();

            GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("OBDAuthPage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("OBDAuthPage uploadLog success " + responese);
                    try {
                        final JSONObject result = new JSONObject(responese);
                        if ("000".equals(result.optString("status"))) {
                            GlobalUtil.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "上报成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            for (File delete : logs) {
                                delete.delete();
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("OBDAuthPage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }

}
