package com.miyuan.obd;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
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
import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.BasePage;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.FMStatus;
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
import java.util.ArrayList;
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

@PageSetting(contentViewId = R.layout.home_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class HomePage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.trie)
    private View trieV;
    @ViewInject(R.id.fault)
    private View faultV;
    @ViewInject(R.id.physical)
    private View physicalV;
    @ViewInject(R.id.dash)
    private View dashV;
    @ViewInject(R.id.hud_setting)
    private View hudSettingV;
    @ViewInject(R.id.navi)
    private View naviV;
    @ViewInject(R.id.fm)
    private View fmView;
    private OBDStatusInfo obdStatusInfo;
    private boolean showLane;

    private CustomDialog dialog;

    private Timer heartTimer = new Timer();

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
        back.setVisibility(View.GONE);
        trieV.setOnClickListener(this);
        reportV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        physicalV.setOnClickListener(this);
        dashV.setOnClickListener(this);
        hudSettingV.setOnClickListener(this);
        naviV.setOnClickListener(this);
        fmView.setOnClickListener(this);
        title.setText("汽车卫士");
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(MainActivity.getInstance().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? android.R.color.black : R.color.main_title_color)
                .init(); //初始化，默认透明状态栏和黑色导航栏
        BlueManager.getInstance().setNavi(false);
        if (null != heartTimer) {
            heartTimer.cancel();
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
        Log.d("HomePage uploadLog ");
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
                    Log.d("HomePage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("HomePage uploadLog success " + responese);
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
                        Log.d("HomePage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    private boolean ishowCamera;

    private void showFMDialog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                showOpenProgressDailog();
                                BlueManager.getInstance().send(ProtocolUtils.setFMParams(true));
                            }
                        });
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.fm_dailog_open)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }


    private OBDRightInfo obdRightInfo;

    private void checkOBDRight() {
        if (null == obdStatusInfo) {
            return;
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("checkOBDRight input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.RIGHT_CHECK)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkOBDRight failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkOBDRight success " + responese);
                obdRightInfo = JSON.parseObject(responese, OBDRightInfo.class);
            }
        });
    }

    private boolean endNavi;
    private AMapLocationClient locationClient = null;
    private AMapLocationClientOption locationOption = null;
    private NotificationManager notificationManager = null;

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.AUTHORIZATION:
            case OBDEvent.AUTHORIZATION_SUCCESS:
            case OBDEvent.AUTHORIZATION_FAIL:
            case OBDEvent.NO_PARAM:
                obdStatusInfo = (OBDStatusInfo) data;
                checkOBDRight();
                break;
            case OBDEvent.NORMAL:
                obdStatusInfo = (OBDStatusInfo) data;
                checkOBDRight();
                break;
            case OBDEvent.FM_PARAMS_INFO:
                FMStatus fmStatus = (FMStatus) data;
                if (null != dialog && !dialog.isHidden()) {
                    dialog.dismiss();
                }
                if (fmStatus.isEnable()) {
                    PageManager.go(new FMInfoPage());
                } else {
                    // 提示打开FM
                    showFMDialog();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trie:
                if (null != obdStatusInfo && SettingPreferencesConfig.TIRE_STATUS.get() != 2) {
                    MainPage mainPage = new MainPage();
                    Bundle mainBundle = new Bundle();
                    mainBundle.putSerializable("obdStatusInfo", obdStatusInfo);
                    mainPage.setDate(mainBundle);
                    PageManager.go(mainPage);
                } else {
                    showConFirm("当前设备不支持胎压功能!");
                }
                break;
            case R.id.fault:
                if (null != obdRightInfo && obdRightInfo.iSupportCheck()) {
                    PageManager.go(new FaultReadyPage());
                } else {
                    showConFirm("当前设备不支持故障码检测!");
                }
                break;
            case R.id.physical:
                if (null != obdRightInfo && obdRightInfo.iSupportCheck()) {
                    PageManager.go(new PhysicalReadyPage());
                } else {
                    showConFirm("当前设备不支持体检！");
                }
                break;
            case R.id.dash:
                PageManager.go(new DashBoardPage());
                break;
            case R.id.hud_setting:
                // 判断HUD类型跳转对应设置界面
                if (null != obdStatusInfo) {
                    if (!obdStatusInfo.isNews()) {
                        showConFirm("当前设备不支持HUD设置!");
                    } else {
                        if (SettingPreferencesConfig.HUD_GUID.get()) {
                            switch (obdStatusInfo.getHudType()) {
                                case 0x02:
                                    PageManager.go(new M2SettingPage());
                                    break;
                                case 0x03:
                                    PageManager.go(new M3SettingPage());
                                    break;
                                case 0x04:
                                    PageManager.go(new M4SettingPage());
                                    break;
                                case 0x22:
                                    PageManager.go(new F2SettingPage());
                                    break;
                                case 0x13:
                                case 0x23:
                                    PageManager.go(new F3SettingPage());
                                    break;
                                case 0x14:
                                case 0x24:
                                    PageManager.go(new F4SettingPage());
                                    break;
                                case 0x15:
                                case 0x25:
                                    PageManager.go(new F5SettingPage());
                                    break;
                                case 0x16:
                                case 0x26:
                                    PageManager.go(new F6SettingPage());
                                    break;
                                case 0x33:
                                case 0x43:
                                    PageManager.go(new P3SettingPage());
                                    break;
                                case 0x34:
                                case 0x44:
                                    PageManager.go(new P4SettingPage());
                                    break;
                                case 0x35:
                                case 0x45:
                                    PageManager.go(new P5SettingPage());
                                    break;
                                case 0x36:
                                case 0x46:
                                    PageManager.go(new P6SettingPage());
                                    break;
                                case 0x37:
                                case 0x47:
                                    PageManager.go(new P7SettingPage());
                                    break;
                                default:
                                    break;
                            }

                        } else {
                            HUDGuidPage guidPage = new HUDGuidPage();
                            Bundle bundle = new Bundle();
                            bundle.putInt("hudType", obdStatusInfo.getHudType());
                            guidPage.setDate(bundle);
                            PageManager.go(guidPage);
                        }
                    }
                }
                break;
            case R.id.navi:
                if (null != obdStatusInfo) {
                    if (!obdStatusInfo.isSupportNavi()) {
                        showConFirm("当前设备不支持导航!");
                        return;
                    }
                    BlueManager.getInstance().setNavi(true);
                    initTimer();
                    AMapNavi.getInstance(getContext()).addAMapNaviListener(new AMapNaviListener() {
                        @Override
                        public void onInitNaviFailure() {
                            Log.d("onInitNaviFailure");
                        }

                        @Override
                        public void onInitNaviSuccess() {
                            Log.d("onInitNaviSuccess");
                        }

                        @Override
                        public void onStartNavi(int i) {
                            endNavi = false;
                            Log.d("onStartNavi " + i);
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
                            Log.d("onEndEmulatorNavi");
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
                            Log.d("onArriveDestination");
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
                            BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(type, naviInfo.getCurStepRetainDistance()));
                        }

                        @Override
                        public void onNaviInfoUpdated(AMapNaviInfo aMapNaviInfo) {

                        }

                        @Override
                        public void updateCameraInfo(AMapNaviCameraInfo[] aMapNaviCameraInfos) {
                            if (endNavi) {
                                return;
                            }
                            if (showCamera(aMapNaviCameraInfos)) {
                                if (ishowCamera) {
                                    return;
                                }
                                Log.d("updateCameraInfo  ishowCamera");
                                ishowCamera = true;
                                for (AMapNaviCameraInfo cameraInfo : aMapNaviCameraInfos) {
                                    switch (cameraInfo.getCameraType()) {
                                        case 0: // 测速
                                            if (SettingPreferencesConfig.CAMERA_SPEED.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 6, aMapNaviCameraInfos[0].getCameraSpeed()));
                                            }
                                            break;
                                        case 1: // 监控摄像
                                            if (SettingPreferencesConfig.SURVEILLANCE_CAMERA.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 7, aMapNaviCameraInfos[0].getDistance()));
                                            }
                                            break;
                                        case 2: // 闯红灯拍照
                                            if (SettingPreferencesConfig.LIGHT.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 8, aMapNaviCameraInfos[0].getDistance()));
                                            }
                                            break;
                                        case 3: // 违章拍照
                                            if (SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 1, aMapNaviCameraInfos[0].getDistance()));
                                            }
                                            break;
                                        case 4: // 公交专用道摄像头
                                            if (SettingPreferencesConfig.BUS.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 2, aMapNaviCameraInfos[0].getDistance()));
                                            }
                                            break;
                                        case 5: // 应急车道拍照
                                            if (SettingPreferencesConfig.EMERGENCY.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 3, aMapNaviCameraInfos[0].getDistance()));
                                            }
                                            break;
                                        case 6: // 非机动车道(暂未使用)
                                            if (SettingPreferencesConfig.BICYCLE_LANE.get()) {
                                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, 0, aMapNaviCameraInfos[0].getDistance()));
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            } else {
                                if (!ishowCamera) {
                                    return;
                                }
                                ishowCamera = false;
                                Log.d("updateCameraInfo  dismiss");
                                BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(false, 0, 0));
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
                            for (int i = 0; i < aMapLaneInfos.length; i++) {
                                if (aMapLaneInfos[i].isRecommended()) {
                                    enter += Math.pow(2, i);
                                }
                            }
                            Log.d("aMapLaneInfo  showLaneInfo " + count);
                            if (!showLane) {
                                showLane = true;
                                BlueManager.getInstance().send(ProtocolUtils.getLineInfo(count > 0 ? true : false, count, enter));
                            }
                        }

                        @Override
                        public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
                        }

                        @Override
                        public void hideLaneInfo() {
                            if (endNavi) {
                                return;
                            }
                            Log.d("aMapLaneInfo  hideLaneInfo ");
                            if (showLane) {
                                showLane = false;
                                BlueManager.getInstance().send(ProtocolUtils.getLineInfo(false, 0, 0));
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
                break;
            case R.id.fm:
//                if (null != obdRightInfo && obdRightInfo.iSupportFM()) {
//                    BlueManager.getInstance().send(ProtocolUtils.getFMParams());
//                } else {
//                    showConFirm("当前设备不支持FM!");
//                }
                if (null != obdStatusInfo) {
                    if (obdStatusInfo.isSupportFM()) {
                        //是否打开FM
                        BlueManager.getInstance().send(ProtocolUtils.getFMParams());
                    } else {
                        showConFirm("当前设备不支持FM!");
                    }
                }
                break;
            case R.id.report:
                showLogDailog();
                break;
            default:
                break;
        }
    }

    private void showOpenProgressDailog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView infoTV = view.findViewById(R.id.info);
                        infoTV.setText("正在开启，请稍等...");
                    }
                })
                .setLayoutRes(R.layout.dailog_fm_progress)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private boolean showCamera(AMapNaviCameraInfo[] cameraInfos) {
        if (null != cameraInfos && cameraInfos.length > 0) {
            ArrayList<Integer> types = new ArrayList<>();
            for (AMapNaviCameraInfo cameraInfo : cameraInfos) {
                types.add(cameraInfo.getCameraType());
            }
            if ((SettingPreferencesConfig.CAMERA_SPEED.get() && types.contains(0))
                    || ((SettingPreferencesConfig.SURVEILLANCE_CAMERA.get() && types.contains(1))
                    || (SettingPreferencesConfig.LIGHT.get() && types.contains(2))
                    || (SettingPreferencesConfig.ILLEGAL_PHOTOGRAPHY.get() && types.contains(3))
                    || (SettingPreferencesConfig.BUS.get() && types.contains(4))
                    || (SettingPreferencesConfig.EMERGENCY.get() && types.contains(5))
                    || (SettingPreferencesConfig.BICYCLE_LANE.get() && types.contains(6)))) {
                return true;
            }
        }
        return false;
    }

    private void showConFirm(final String conent) {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView infoTV = view.findViewById(R.id.info);
                        infoTV.setText(conent);

                        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.fm_dailog_common_confirm)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

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
}
