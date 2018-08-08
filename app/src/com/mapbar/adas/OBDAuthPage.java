package com.mapbar.adas;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.obd_auth_layout)
public class OBDAuthPage extends AppBasePage implements BleCallBackListener, LocationListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    private volatile boolean verified;
    private CustomDialog dialog;

    private LocationManager locationManager;

    private String sn;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("授权检查");
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
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                verify();
                                            }
                                        });
                                    }
                                })
                                .setLayoutRes(R.layout.dailog_common_warm)
                                .setDimAmount(0.5f)
                                .isCenter(true)
                                .setCancelOutside(false)
                                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                                .show();
                    }
                });
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
                    BlueManager.getInstance().write(ProtocolUtils.getOBDStatus(Long.valueOf(result.optString("server_time"))));
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
            case OBDEvent.OBD_FIRST_USE:
                dismissProgress();
                Log.d("OBDEvent.OBD_FIRST_USE ");
                // 激活
                String boxId = (String) data;
                Log.d("boxId  " + boxId);
                OBDInitPage obdInitPage = new OBDInitPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", boxId);
                obdInitPage.setDate(bundle);
                PageManager.go(obdInitPage);
                break;
            case OBDEvent.OBD_NORMAL:
                Log.d("OBDEvent.OBD_NORMAL ");
                // 解析OBD状态
                byte[] result1 = (byte[]) data;
                byte[] result = new byte[result1.length - 1];
                System.arraycopy(result1, 1, result, 0, result.length);
                protocolCheck(result);
                break;
            case OBDEvent.OBD_EXPIRE:
                Log.d("OBDEvent.OBD_EXPIRE " + data);
                // 获取授权码
                sn = (String) data;
                getLisense(sn);
                break;
            case OBDEvent.OBD_AUTH_RESULT:
                Log.d("OBDEvent.OBD_AUTH_RESULT " + data);
                // 授权结果
                if ((Integer) data == 1) {
                    authSuccess();
                } else {
                    authFail("授权码写入OBD盒子失败");
                }
                break;
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                protocolCheck((byte[]) data);
                break;
        }
    }

    /**
     * 检查协议
     */
    private void protocolCheck(byte[] result) {
        dismissProgress();
        byte[] snBytes = new byte[19];
        sn = new String(Arrays.copyOfRange(result, 0, snBytes.length));
        byte[] bytes = HexUtils.getBooleanArray(result[19]);
        if (bytes[0] == 1) {
            // 车型不支持
            ProtocolCheckFailPage page = new ProtocolCheckFailPage();
            Bundle bundle = new Bundle();
            bundle.putString("sn", sn);
            if (getDate() != null && getDate().containsKey("showStudy")) {
                bundle.putBoolean("showStudy", (boolean) getDate().get("showStudy"));
            }
            page.setDate(bundle);
            PageManager.go(page);
        } else {
            // 支持
            ProtocolCheckSuccessPage page = new ProtocolCheckSuccessPage();
            Bundle bundle = new Bundle();
            if (getDate() != null && getDate().containsKey("showStudy")) {
                bundle.putBoolean("showStudy", (boolean) getDate().get("showStudy"));
            }
            bundle.putString("sn", sn);
            page.setDate(bundle);
            PageManager.go(page);
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
                                        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                // 获取授权码
                                                showProgress();
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
                        String code = result.optString("rightStr");
                        BlueManager.getInstance().write(ProtocolUtils.auth(sn, code));
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
    private void authSuccess() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
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

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("activate success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        // 协议匹配检查（通过获取胎压信息）
                        BlueManager.getInstance().write(ProtocolUtils.getTirePressureStatus());
                    } else {
                        Log.d("activate failure" + result.optString("message"));
                    }
                } catch (JSONException e) {
                    Log.d("activate failure " + e.getMessage());
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
            BlueManager.getInstance().write(ProtocolUtils.getOBDStatus(location.getTime()));
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
}
