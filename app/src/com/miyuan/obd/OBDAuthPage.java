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
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AmapNaviPage;
import com.amap.api.navi.AmapNaviParams;
import com.amap.api.navi.INaviInfoCallback;
import com.amap.api.navi.enums.PageType;
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
    private boolean backToNavi = false;
    public static boolean hasCheck = false;
    private AnimationDrawable animationDrawable;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            goNavi();
            return true;
        }
    }
    );

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
        if (!backToNavi) {
            verify();
            BlueManager.getInstance().addBleCallBackListener(this);
        }
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
    private byte[] lastBitmap;

    public native static byte[] convertPicture(byte[] src, byte[] des);


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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:
                showLogDailog();
                break;
        }
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
            case OBDEvent.AUTHORIZATION: //未授权或者授权过期
                obdStatusInfo = (OBDStatusInfo) data;
                // 获取授权码
                getLisense();
                break;
            case OBDEvent.NORMAL:
                // 直接跳导航
                obdStatusInfo = (OBDStatusInfo) data;
                Log.d("obdStatusInfo  " + obdStatusInfo);
                updateCarID();
                break;
            default:
                break;
        }
    }


    /**
     * 检查固件升级
     */
    private void checkFirmwareVersion(OBDStatusInfo obdStatusInfo) {
        if (null == obdStatusInfo) {
            return;
        }
        String sn = obdStatusInfo.getSn();
        String bVersion = obdStatusInfo.getbVersion();
        String pVersion = obdStatusInfo.getpVersion();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
            jsonObject.put("pVersion", pVersion);
            jsonObject.put("bVersion", bVersion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("checkFirmwareVersion input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_FIRMWARE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkFirmwareVersion onFailure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkFirmwareVersion onResponse " + responese);
                final FirmwareUpdateInfo updateInfo = JSON.parseObject(responese, FirmwareUpdateInfo.class);
                if (updateInfo.getbUpdateState() == 1) { // 固件需要升级
                    // 弹出对话框
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            showUpdateConfirmDailog(updateInfo);
                        }
                    });
                } else {
                    handler.sendEmptyMessage(1);
                }
            }
        });
    }

    private void showUpdateConfirmDailog(final FirmwareUpdateInfo updateInfo) {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        Log.d("showUpdateConfirmDailog  " + updateInfo);
                        TextView textView = view.findViewById(R.id.info);
                        String info = "当前有新版本升级，共需约" + (int) ((updateInfo.getSize() / 1024 * 0.6) / 60) + "分钟。升级过程中不能关闭手机，不能关闭硬件设备，不能做其他任何操作。否则升级失败可能导致设备使用不正常，需要重新升级。";
                        textView.setText(info);
                        view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                OBDUpdatePage page = new OBDUpdatePage();
                                Bundle bundle = new Bundle();
                                bundle.putString("url", updateInfo.getUrl());
                                bundle.putString("serialNumber", obdStatusInfo.getSn());
                                bundle.putString("bVersion", obdStatusInfo.getbVersion());
                                bundle.putString("pVersion", obdStatusInfo.getpVersion());
                                bundle.putString("message", updateInfo.getDesc());
                                bundle.putInt("size", updateInfo.getSize());
                                bundle.putInt("id", updateInfo.getId());
                                page.setDate(bundle);
                                PageManager.go(page);
                                dialog.dismiss();
                            }
                        });

                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                goNavi();
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_update)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void updateCarID() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("carId", "10479");
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("updateCarID input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .url(URLUtils.MODIFY_CAR_BRAND)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responese = response.body().string();

                Log.d("updateCarID success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        if (!hasCheck) {
                            hasCheck = true;
                            checkFirmwareVersion(obdStatusInfo);
                        }
                    }
                } catch (JSONException e) {
                    Log.d("updateCarID failure " + e.getMessage());
                }
            }
        });
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
        backToNavi = true;
        final AMapNavi aMapNavi = AMapNavi.getInstance(getContext());
        aMapNavi.addAMapNaviListener(new AMapNaviListener() {
            @Override
            public void onInitNaviFailure() {
            }

            @Override
            public void onInitNaviSuccess() {
            }

            @Override
            public void onStartNavi(int i) {
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
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(0xFF, 0));
                    }
                }).start();
            }

            @Override
            public void onArriveDestination() {
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
        AmapNaviPage.getInstance().showRouteActivity(getContext(), new AmapNaviParams(null), new INaviInfoCallback() {
            @Override
            public void onInitNaviFailure() {

            }

            @Override
            public void onGetNavigationText(String s) {

            }

            @Override
            public void onLocationChange(AMapNaviLocation aMapNaviLocation) {

            }

            @Override
            public void onArriveDestination(boolean b) {

            }

            @Override
            public void onStartNavi(int i) {

            }

            @Override
            public void onCalculateRouteSuccess(int[] ints) {

            }

            @Override
            public void onCalculateRouteFailure(int i) {

            }

            @Override
            public void onStopSpeaking() {

            }

            @Override
            public void onReCalculateRoute(int i) {

            }

            @Override
            public void onExitPage(int i) {
                Log.d("onExitPage  i = " + i);
                if (PageType.COMPONENT == i) {
                    PageManager.back();
                }
            }

            @Override
            public void onStrategyChanged(int i) {

            }

            @Override
            public View getCustomNaviBottomView() {
                return null;
            }

            @Override
            public View getCustomNaviView() {
                return null;
            }

            @Override
            public void onArrivedWayPoint(int i) {

            }

            @Override
            public void onMapTypeChanged(int i) {

            }

            @Override
            public View getCustomMiddleView() {
                return null;
            }

            @Override
            public void onNaviDirectionChanged(int i) {

            }

            @Override
            public void onDayAndNightModeChanged(int i) {

            }

            @Override
            public void onBroadcastModeChanged(int i) {

            }

            @Override
            public void onScaleAutoChanged(boolean b) {

            }
        });
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
