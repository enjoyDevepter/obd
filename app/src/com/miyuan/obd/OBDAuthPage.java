package com.miyuan.obd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.model.AMapCalcRouteResult;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapModelCross;
import com.amap.api.navi.model.AMapNaviCameraInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviRouteNotifyData;
import com.amap.api.navi.model.AMapNaviTrafficFacilityInfo;
import com.amap.api.navi.model.AMapServiceAreaInfo;
import com.amap.api.navi.model.AimLessModeCongestionInfo;
import com.amap.api.navi.model.AimLessModeStat;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.OBDStatusInfo;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.hamster.log.FileLoggingTree;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.preferences.SettingPreferencesConfig;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;
import com.miyuan.obd.utils.URLUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.miyuan.obd.preferences.SettingPreferencesConfig.SN;

@PageSetting(contentViewId = R.layout.obd_auth_layout, toHistory = false)
public class OBDAuthPage extends AppBasePage implements BleCallBackListener, View.OnClickListener {

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
    private volatile boolean needNotifyParamsSuccess;
    private volatile boolean needNotifyVerifiedSuccess;

    private AnimationDrawable animationDrawable;

    static {
        System.loadLibrary("tools");
    }

    @Override
    public void onResume() {
        super.onResume();
        title.setText("获取盒子状态");
        reportV.setOnClickListener(this);
        back.setVisibility(View.GONE);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        animationDrawable.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        verify();
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
                                                verify();
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
        if (null != animationDrawable) {
            return;
        }
        animationDrawable.stop();
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    private Timer heartTimer = new Timer();
    private boolean showLane;
    private boolean endNavi;
    private byte[] lastBitmap;

    public native static byte[] convertPicture(byte[] src, byte[] des);


    /**
     * 获取采集上传状态
     */
    private void checkColectStauts() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("checkColectStauts input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_STATUS)
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
                                                checkColectStauts();
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
                Log.d("checkColectStauts success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        String state = result.optString("state");
                        if ("1".equals(state)) {
                            CollectPage collectPage = new CollectPage();
                            Bundle bundle = new Bundle();
                            bundle.putString("sn", obdStatusInfo.getSn());
                            collectPage.setDate(bundle);
                            PageManager.go(collectPage);
                        } else {
                            CollectGuide collectGuide = new CollectGuide();
                            Bundle collectBundle = new Bundle();
                            collectBundle.putBoolean("matching", true);
                            collectBundle.putString("sn", obdStatusInfo.getSn());
                            collectBundle.putString("pVersion", obdStatusInfo.getpVersion());
                            collectBundle.putString("bVersion", obdStatusInfo.getbVersion());
                            collectGuide.setDate(collectBundle);
                            PageManager.go(collectGuide);
                        }
                    }
                } catch (JSONException e) {
                    Log.d("checkColectStauts failure " + e.getMessage());
                }
            }
        });
    }

    public static int shortToByteArray1(short i, byte[] data, int offset) {
        data[offset + 1] = (byte) (i >> 8 & 255);
        data[offset] = (byte) (i & 255);
        return offset + 2;
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
    private void notifyUpdateSuccess(boolean force) {
        if (!needNotifyParamsSuccess && !force) {
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

        Log.d("notifyUpdateSuccess  " + force + " input " + jsonObject.toString());

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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:
                showLogDailog();
                break;
        }
    }


//    private void checkSupportTire() {
//
//        switch (SettingPreferencesConfig.TIRE_STATUS.get()) {
//            case 0:
//                checkTireSupport();
//                break;
//            case 1:
//                checkOBDVersion();
//                break;
//            case 2:
//                PageManager.go(new HomePage());
//                break;
//        }
//    }

    private void checkTireSupport() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("boxId", obdStatusInfo.getBoxId());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("checkSupportTire input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.TIRE_CHECK)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkSupportTire failure " + e.getMessage());
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
                                                checkTireSupport();
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
                Log.d("checkSupportTire success " + responese);
                final TPMSStatus tpmsStatus = JSON.parseObject(responese, TPMSStatus.class);
                if ("000".equals(tpmsStatus.getStatus())) {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            SettingPreferencesConfig.TIRE_STATUS.set(tpmsStatus.getState());
                            switch (tpmsStatus.getState()) {
                                case 1:
                                    checkOBDVersion();
                                    break;
                                case 2:
                                    notifyUpdateSuccess(true);
                                    PageManager.go(new HomePage());
                                    break;
                            }
                        }
                    });
                }
            }
        });
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
                                    bundle.putString("pVersion", obdStatusInfo.getpVersion());
                                    bundle.putString("bVersion", obdStatusInfo.getbVersion());
                                    collectGuide.setDate(bundle);
                                    PageManager.go(collectGuide);
                                    break;
                                case 3: // 临时车型，参数已采集
                                    CollectFinish collectFinish = new CollectFinish();
                                    Bundle collectBundle = new Bundle();
                                    collectBundle.putString("sn", obdStatusInfo.getSn());
                                    collectBundle.putString("pVersion", obdStatusInfo.getpVersion());
                                    collectBundle.putString("bVersion", obdStatusInfo.getbVersion());
                                    collectBundle.putBoolean("success", false);
                                    collectFinish.setDate(collectBundle);
                                    PageManager.go(collectFinish);
                                    break;
                                case 6: // 车型不支持
                                    CollectFinish finish = new CollectFinish();
                                    Bundle finishBundle = new Bundle();
                                    finishBundle.putString("sn", obdStatusInfo.getSn());
                                    finishBundle.putString("pVersion", obdStatusInfo.getpVersion());
                                    finishBundle.putString("bVersion", obdStatusInfo.getbVersion());
                                    finishBundle.putBoolean("success", false);
                                    finishBundle.putBoolean("unPlay", true);
                                    finish.setDate(finishBundle);
                                    PageManager.go(finish);
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


    private void checkOBDVersionForNew() {

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
                                case 2:
                                case 3:
                                case 6:
                                    CollectPage collectPage = new CollectPage();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("sn", obdStatusInfo.getSn());
                                    collectPage.setDate(bundle);
                                    PageManager.go(collectPage);
                                    break;
                                case 1: // 有更新
                                    needNotifyParamsSuccess = true;
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
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

    private void showLogDailog() {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) (view.findViewById(R.id.sn))).setText(SN.get());
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        uploadLog();
                                        dialog.dismiss();
                                    }
                                });
                                view.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //获取剪贴板管理器
                                        ClipboardManager cm = (ClipboardManager) GlobalUtil.getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        // 创建普通字符型ClipData
                                        ClipData mClipData = ClipData.newPlainText("Label", SN.get());
                                        // 将ClipData内容放到系统剪贴板里。
                                        cm.setPrimaryClip(mClipData);
                                    }
                                });
                            }
                        })
                        .setLayoutRes(R.layout.log_dailog)
                        .setCancelOutside(false)
                        .setDimAmount(0.5f)
                        .isCenter(true)
                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                        .show();
            }
        });
    }

    private void uploadLog() {
        Log.d("OBDAuthPage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "log");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0 && null != obdStatusInfo) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                    .addPart(MultipartBody.Part.createFormData("type", "1"));
            for (File file : logs) {
                if (!file.getName().equals(FileLoggingTree.fileName)) {
                    builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
                }
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
                                if (!delete.getName().equals(FileLoggingTree.fileName)) {
                                    delete.delete();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("OBDAuthPage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }

    public static int RGB888ToRGB565(int rgb8888) {
        return (rgb8888 >> 19 & 31) << 11 | (rgb8888 >> 10 & 63) << 5 | rgb8888 >> 3 & 31;
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.UNREGISTERED://未注册
                // 激活
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
                // 直接跳导航
                goNavi();
                break;
            case OBDEvent.AUTHORIZATION_FAIL:
                authFail("授权失败!请联系客服!");
                uploadLog();
                break;
            case OBDEvent.NO_CAR_ID:
                obdStatusInfo = (OBDStatusInfo) data;
                ChoiceCarPage choiceCarPage = new ChoiceCarPage();
                Bundle carBundle = new Bundle();
                carBundle.putString("boxId", obdStatusInfo.getbVersion());
                carBundle.putString("sn", obdStatusInfo.getSn());
                choiceCarPage.setDate(carBundle);
                PageManager.go(choiceCarPage);
                break;
            case OBDEvent.NO_PARAM: // 无参数
                obdStatusInfo = (OBDStatusInfo) data;
                checkTireSupport();
                break;
            case OBDEvent.PARAM_UPDATE_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                notifyUpdateSuccess(false);
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
                // FIXME: 2018/9/29
                obdStatusInfo = (OBDStatusInfo) data;
                checkColectStauts();
                break;
            case OBDEvent.ADJUSTING:
                checkOBDVersionForNew();
                break;
            case OBDEvent.ADJUST_SUCCESS:
                PageManager.go(new HomePage());
                break;
            default:
                break;
        }
    }

    private void initTimer() {
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.getTurnInfo());
            }
        }, 1000 * 3, 1000 * 8);
    }

    private void goNavi() {
        BlueManager.getInstance().setNavi(true);
        initTimer();
        final AMapNavi aMapNavi = AMapNavi.getInstance(getContext());
        aMapNavi.addAMapNaviListener(new AMapNaviListener() {
            @Override
            public void onInitNaviFailure() {
//                            Log.d("onInitNaviFailure");
            }

            @Override
            public void onInitNaviSuccess() {
//                            Log.d("onInitNaviSuccess");
            }

            @Override
            public void onStartNavi(int i) {
                endNavi = false;
//                            Log.d("onStartNavi " + i);
            }

            @Override
            public void onTrafficStatusUpdate() {

            }

            @Override
            public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

            }

            @Override
            public void onGetNavigationText(int i, String s) {

            }

            @Override
            public void onGetNavigationText(String s) {

            }

            @Override
            public void onEndEmulatorNavi() {
//                            Log.d("onEndEmulatorNavi");
                endNavi = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(0xFF, 0));
                    }
                }).start();
            }

            @Override
            public void onArriveDestination() {
//                            Log.d("onArriveDestination");
                endNavi = true;
                if (heartTimer != null) {
                    heartTimer.cancel();
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(0xFF, 0));
                    }
                }).start();
            }

            @Override
            public void onCalculateRouteFailure(int i) {

            }

            @Override
            public void onReCalculateRouteForYaw() {

            }

            @Override
            public void onReCalculateRouteForTrafficJam() {

            }

            @Override
            public void onArrivedWayPoint(int i) {

            }

            @Override
            public void onGpsOpenStatus(boolean b) {

            }

            @Override
            public void onNaviInfoUpdate(NaviInfo naviInfo) {
                if (endNavi) {
                    return;
                }
//                            Log.d("onNaviInfoUpdate  naviInfo " + naviInfo.getCurStepRetainDistance());
                int type = 0;
                switch (naviInfo.getIconType()) {
                    case 0:
                        break;
                    case 2:
                    case 21:
                    case 25:
                        type = 5;
                        break;
                    case 3:
                    case 26:
                    case 22:
                        type = 2;
                        break;
                    case 4:
                    case 51:
                        type = 4;
                        break;
                    case 5:
                    case 52:
                        type = 1;
                        break;
                    case 6:
                        type = 6;
                        break;
                    case 7:
                        type = 3;
                        break;
                    case 8:
                    case 28:
                        type = 7;
                        break;
                    case 11:
                        type = 8;
                        break;
                    default:
                        type = 0;
                        break;
                }

                if (obdStatusInfo.getHudType() == 0x62 || obdStatusInfo.getHudType() == 0x48) {
                    try {
                        byte[] bytes = naviInfo.getNextRoadName().getBytes("GBK");
                        String[] name = naviInfo.getExitDirectionInfo().getExitNameInfo();
                        String[] info = naviInfo.getExitDirectionInfo().getDirectionInfo();
                        StringBuilder sb = new StringBuilder();
                        if (null != name && name.length > 0) {
                            sb.append(name[0]).append(" ").append(info[0]);
                        }
                        byte[] exits = sb.toString().getBytes("GBK");
                        Log.d("getTurnInfo2  naviInfo.getPathRetainDistance() " + naviInfo.getPathRetainDistance() + "  " + naviInfo.getPathRetainTime() + "   " + naviInfo.getCurrentRoadName() + "  " + naviInfo.getNextRoadName() + "  " + Arrays.toString(naviInfo.getExitDirectionInfo().getDirectionInfo()) + "   " + Arrays.toString(naviInfo.getExitDirectionInfo().getExitNameInfo()));
                        BlueManager.getInstance().send(ProtocolUtils.getTurnInfo2(type, naviInfo.getCurStepRetainDistance(), naviInfo.getPathRetainDistance(), naviInfo.getPathRetainTime(), bytes, exits));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    int naviTpye = naviInfo.getIconType();
                    // 去除重复图片
                    if (null != naviInfo.getIconBitmap()) {
                        saveMyBitmap(naviInfo.getIconBitmap());
                    } else {
//                                    Log.d("NO BITMAP " + naviTpye);
                        getTurnImage(naviTpye);
                    }
                } else {
                    BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(type, naviInfo.getCurStepRetainDistance()));
                }
            }

            @Override
            public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {
            }

            @Override
            public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
                if (endNavi) {
                    return;
                }
                int index = showCamera(aMapNaviCameraInfos);
                if (index != -1) {
                    AMapNaviCameraInfo cameraInfo = aMapNaviCameraInfos[index];
                    Log.d("cameraInfo  " + cameraInfo.getCameraType() + " cameraInfo.getDistance() =  " + cameraInfo.getDistance() + "  cameraInfo.getCameraSpeed() =  " + cameraInfo.getCameraSpeed() + "  cameraInfo.getAverageSpeed() =  " + cameraInfo.getAverageSpeed() + "  cameraInfo.getCameraDistance()=  " + cameraInfo.getCameraDistance());
                    switch (cameraInfo.getCameraType()) {
                        case 0: // 测速
                            if (SettingPreferencesConfig.CAMERA_SPEED.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 6, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 1: // 监控摄像
                            if (SettingPreferencesConfig.SURVEILLANCE_CAMERA.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 7, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 2: // 闯红灯拍照
                            if (SettingPreferencesConfig.LIGHT.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 8, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 3: // 违章拍照
                            if (SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 1, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 4: // 公交专用道摄像头
                            if (SettingPreferencesConfig.BUS.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 2, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 5: // 应急车道拍照
                            if (SettingPreferencesConfig.EMERGENCY.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 3, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 6: // 非机动车道(暂未使用)
                            if (SettingPreferencesConfig.BICYCLE_LANE.get()) {
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 0, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            }
                            break;
                        case 8: //区间测速开始
//                                            Log.d("updateCameraInfo  INTERVALVELOCITYSTART " + cameraInfo.getAverageSpeed() + "   " + cameraInfo.getAverageSpeed());
                            BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 4, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            break;
                        case 9:
//                                            Log.d("updateCameraInfo  INTERVALVELOCITYEND " + cameraInfo.getAverageSpeed() + "   " + cameraInfo.getAverageSpeed());
                            BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 5, cameraInfo.getCameraSpeed(), cameraInfo.getAverageSpeed(), cameraInfo.getCameraDistance()));
                            break;
                        default:
                            break;
                    }
                } else {
                    Log.d("updateCameraInfo  dismiss");
                    BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(false, 0, 0, 0, 0));
                }
            }

            @Override
            public void updateIntervalCameraInfo(AMapNaviCameraInfo aMapNaviCameraInfo, AMapNaviCameraInfo aMapNaviCameraInfo1, int i) {

            }

            @Override
            public void onServiceAreaUpdate(AMapServiceAreaInfo[] aMapServiceAreaInfos) {

            }

            @Override
            public void showCross(AMapNaviCross aMapNaviCross) {
            }

            @Override
            public void hideCross() {

            }

            @Override
            public void showModeCross(AMapModelCross aMapModelCross) {
            }

            @Override
            public void hideModeCross() {

            }

            @Override
            public void showLaneInfo(AMapLaneInfo[] aMapLaneInfos, byte[] bytes, byte[] bytes1) {
                if (endNavi) {
                    return;
                }
                int enter = 0;
                int count = aMapLaneInfos.length;
                byte[] laneType = new byte[count];
                for (int i = 0; i < aMapLaneInfos.length; i++) {
                    if (aMapLaneInfos[i].isRecommended()) {
                        enter += Math.pow(2, i);
                    }
                    laneType[i] = (byte) (Integer.valueOf(String.valueOf(aMapLaneInfos[i].getLaneTypeIdArray()[0])) & 0xFF);
                }
                Log.d("aMapLaneInfo  laneType1 " + Arrays.toString(laneType));
                if (!showLane) {
                    showLane = true;
                    if (obdStatusInfo.getHudType() == 0x62 || obdStatusInfo.getHudType() == 0x48) {
                        BlueManager.getInstance().send(ProtocolUtils.getLineInfo(count > 0 ? true : false, count, enter, laneType));
                    } else {
                        BlueManager.getInstance().send(ProtocolUtils.getLineInfo(count > 0 ? true : false, count, enter));
                    }
                }
            }

            @Override
            public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
                Log.d("showLaneInfo  " + Arrays.toString(aMapLaneInfo.backgroundLane));
                Log.d("showLaneInfo  " + Arrays.toString(aMapLaneInfo.frontLane));
                Log.d("showLaneInfo  " + Integer.valueOf(String.valueOf(aMapLaneInfo.getLaneTypeIdArray())));
            }

            @Override
            public void hideLaneInfo() {
                if (endNavi) {
                    return;
                }
//                            Log.d("aMapLaneInfo  hideLaneInfo ");
                if (showLane) {
                    showLane = false;
                    BlueManager.getInstance().send(ProtocolUtils.getLineInfo(false, 0, 0, null));
                }
            }

            @Override
            public void onCalculateRouteSuccess(int[] ints) {

            }

            @Override
            public void notifyParallelRoad(int i) {

            }

            @Override
            public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo aMapNaviTrafficFacilityInfo) {

            }

            @Override
            public void OnUpdateTrafficFacility(AMapNaviTrafficFacilityInfo[] aMapNaviTrafficFacilityInfos) {

            }

            @Override
            public void OnUpdateTrafficFacility(TrafficFacilityInfo trafficFacilityInfo) {

            }

            @Override
            public void updateAimlessModeStatistics(AimLessModeStat aimLessModeStat) {

            }

            @Override
            public void updateAimlessModeCongestionInfo(AimLessModeCongestionInfo aimLessModeCongestionInfo) {

            }

            @Override
            public void onPlayRing(int i) {

            }

            @Override
            public void onCalculateRouteSuccess(AMapCalcRouteResult aMapCalcRouteResult) {

            }

            @Override
            public void onCalculateRouteFailure(AMapCalcRouteResult aMapCalcRouteResult) {

            }

            @Override
            public void onNaviRouteNotify(AMapNaviRouteNotifyData aMapNaviRouteNotifyData) {

            }
        });
        AmapNaviPage.getInstance().showRouteActivity(getContext(), new AmapNaviParams(null), null);

    }

    public void saveMyBitmap(Bitmap mBitmap) {
        if (obdStatusInfo.getHudType() != 0x62) {
            return;
        }
        int width = mBitmap.getWidth();
        int height = mBitmap.getHeight();
        Matrix matrix = new Matrix();
        matrix.postScale(44.0f / width, 44.0f / height);
        Bitmap newBitmap = Bitmap.createBitmap(mBitmap, 0, 0, width, height, matrix, true);
        byte[] result = bitmap2RGB(newBitmap);
        byte[] code = new byte[4096];
        code = convertPicture(result, code);
        // 验证图片是否一样
        if (null != lastBitmap) {
            for (int i = 0; i < result.length; i++) {
                if (result[i] != lastBitmap[i]) {
                    lastBitmap = result;
                    BlueManager.getInstance().send(ProtocolUtils.getImage(code));
                    return;
                }
            }
            Log.d("bitmap the same");
        } else {
            lastBitmap = result;
            BlueManager.getInstance().send(ProtocolUtils.getImage(code));
        }

    }

    public byte[] bitmap2RGB(Bitmap bitmap) {

        if (bitmap == null) {
            return null;
        }

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];

        byte[] result = new byte[44 * 44 * 2];

        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int i = 0; i < width * height; i++) {

            short rgb565 = (short) RGB888ToRGB565(pixels[i]);

            shortToByteArray1(rgb565, result, i * 2);
        }

        return result;
    }

    private int showCamera(AMapNaviCameraInfo[] cameraInfos) {
        if (null != cameraInfos && cameraInfos.length > 0) {
            ArrayList<Integer> types = new ArrayList<>();
            int index = 0;
            for (int i = 1; i < cameraInfos.length; i++) {
                if (cameraInfos[i].getCameraDistance() < cameraInfos[index].getCameraDistance()) {
                    index = i;
                }
            }
            types.add(cameraInfos[index].getCameraType());
            Log.d("cameraInfos types " + types);
            if ((SettingPreferencesConfig.CAMERA_SPEED.get() && types.contains(0))
                    || ((SettingPreferencesConfig.SURVEILLANCE_CAMERA.get() && types.contains(1))
                    || (SettingPreferencesConfig.LIGHT.get() && types.contains(2))
                    || (SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get() && types.contains(3))
                    || (SettingPreferencesConfig.BUS.get() && types.contains(4))
                    || (SettingPreferencesConfig.EMERGENCY.get() && types.contains(5))
                    || (SettingPreferencesConfig.INTERVALVELOCITYSTART.get() && types.contains(8))
                    || (SettingPreferencesConfig.INTERVALVELOCITYEND.get() && types.contains(9))
                    || (SettingPreferencesConfig.BICYCLE_LANE.get() && types.contains(6)))) {
                return index;
            }
        }
        return -1;
    }

    private void getTurnImage(int naviTpye) {
        int resID;
        switch (naviTpye) {
            case 1:
                //返回对应图片资源id
                resID = R.drawable.sou1_night;
                break;
            case 2:
                //返回对应图片资源id
                resID = R.drawable.sou2_night;
                break;
            case 3:
                //返回对应图片资源id
                resID = R.drawable.sou3_night;
                break;
            case 4:
                //返回对应图片资源id
                resID = R.drawable.sou4_night;
                break;
            case 5:
                //返回对应图片资源id
                resID = R.drawable.sou5_night;
                break;
            case 6:
                //返回对应图片资源id
                resID = R.drawable.sou6_night;
                break;
            case 7:
                //返回对应图片资源id
                resID = R.drawable.sou7_night;
                break;
            case 8:
                //返回对应图片资源id
                resID = R.drawable.sou8_night;
                break;
            case 9:
                //返回对应图片资源id
                resID = R.drawable.sou9_night;
                break;
            case 10:
                //返回对应图片资源id
                resID = R.drawable.sou10_night;
                break;
            case 11:
                //返回对应图片资源id
                resID = R.drawable.sou11_night;
                break;
            case 12:
                //返回对应图片资源id
                resID = R.drawable.sou12_night;
                break;
            case 13:
                //返回对应图片资源id
                resID = R.drawable.sou13_night;
                break;
            case 14:
                //返回对应图片资源id
                resID = R.drawable.sou14_night;
                break;
            case 15:
                //返回对应图片资源id
                resID = R.drawable.sou15_night;
                break;
            case 16:
                //返回对应图片资源id
                resID = R.drawable.sou16_night;
                break;
            case 17:
                //返回对应图片资源id
                resID = R.drawable.sou17_night;
                break;
            case 18:
                //返回对应图片资源id
                resID = R.drawable.sou18_night;
                break;
            case 19:
                //返回对应图片资源id
                resID = R.drawable.sou19_night;
                break;
            case 20:
                //返回对应图片资源id
                resID = R.drawable.sou20_night;
                break;
            default:
                //返回对应图片资源id
                resID = R.drawable.sou20_night;
                break;

        }
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), resID);
        saveMyBitmap(bitmap);
    }

}
