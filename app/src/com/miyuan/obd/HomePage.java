package com.miyuan.obd;

import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
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


    static {
        System.loadLibrary("tools");
    }

    private byte[] lastBitmap;

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

    public native static byte[] convertPicture(byte[] src, byte[] des);

    public static int shortToByteArray1(short i, byte[] data, int offset) {
        data[offset + 1] = (byte) (i >> 8 & 255);
        data[offset] = (byte) (i & 255);
        return offset + 2;
    }

    public static int RGB888ToRGB565(int rgb8888) {
        return (rgb8888 >> 19 & 31) << 11 | (rgb8888 >> 10 & 63) << 5 | rgb8888 >> 3 & 31;
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

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.AUTHORIZATION:
            case OBDEvent.AUTHORIZATION_SUCCESS:
            case OBDEvent.AUTHORIZATION_FAIL:
            case OBDEvent.NO_PARAM:
                obdStatusInfo = (OBDStatusInfo) data;
                checkOBDRight();
                Log.d("obdStatusInfo  " + obdStatusInfo);
                break;
            case OBDEvent.NORMAL:
                obdStatusInfo = (OBDStatusInfo) data;
                checkOBDRight();
                Log.d("obdStatusInfo  " + obdStatusInfo);
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
                                case 0x62:
                                case 0x48:
                                    PageManager.go(new C2SettingPage());
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
