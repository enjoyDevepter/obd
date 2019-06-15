package com.mapbar.adas;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.TIRE_STATUS;

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
    @ViewInject(R.id.message)
    private View messageV;
    @ViewInject(R.id.hud)
    private View hudV;
    private OBDStatusInfo obdStatusInfo;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        BlueManager.getInstance().addBleCallBackListener(this);
        BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        trieV.setOnClickListener(this);
        faultV.setOnClickListener(this);
        physicalV.setOnClickListener(this);
        dashV.setOnClickListener(this);
        messageV.setOnClickListener(this);
        hudV.setOnClickListener(this);
        title.setText("汽车卫士");
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(MainActivity.getInstance().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? android.R.color.black : R.color.main_title_color)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trie:
                if (null != obdStatusInfo && TIRE_STATUS.get() != 2) {
                    MainPage mainPage = new MainPage();
                    Bundle mainBundle = new Bundle();
                    mainBundle.putSerializable("obdStatusInfo", obdStatusInfo);
                    mainPage.setDate(mainBundle);
                    PageManager.go(mainPage);
                } else {
                    Toast.makeText(getContext(), "此硬件不支持胎压功能！", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.fault:
                PageManager.go(new FaultReadyPage());
                break;
            case R.id.physical:
                PageManager.go(new PhysicalReadyPage());
                break;
            case R.id.dash:
                PageManager.go(new DashBoardPage());
                break;
            case R.id.message:
                // 判断HUD类型跳转对应设置界面
                if (null != obdStatusInfo) {
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
                        case 0x23:
                            PageManager.go(new F3SettingPage());
                            break;
                        case 0x24:
                            PageManager.go(new F4SettingPage());
                            break;
                        case 0x25:
                            PageManager.go(new F5SettingPage());
                            break;
                        case 0x26:
                            PageManager.go(new F6SettingPage());
                            break;
                        case 0x43:
                            PageManager.go(new P3SettingPage());
                            break;
                        case 0x44:
                            PageManager.go(new P4SettingPage());
                            break;
                        case 0x45:
                            PageManager.go(new P5SettingPage());
                            break;
                        case 0x46:
                            PageManager.go(new P6SettingPage());
                            break;
                        case 0x47:
                            PageManager.go(new P7SettingPage());
                            break;
                        default:
                            break;
                    }
                }
                break;
            case R.id.hud:
                AMapNavi.getInstance(getContext()).addAMapNaviListener(new AMapNaviListener() {
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
                        BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(0xFF, 0));
                    }

                    @Override
                    public void onArriveDestination() {
                        BlueManager.getInstance().send(ProtocolUtils.getTurnInfo(0xFF, 0));

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
                                type = 5;
                                break;
                            case 3:
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
                        if (aMapNaviCameraInfos.length > 0) {
                            int type = 0;
                            switch (aMapNaviCameraInfos[0].getCameraType()) {
                                case 0: // 测速
                                    type = 6;
                                    break;
                                case 1: // 监控摄像
                                    type = 7;
                                    break;
                                case 2: // 闯红灯拍照
                                    type = 8;
                                    break;
                                case 3: // 违章拍照
                                    type = 1;
                                    break;
                                case 4: // 公交专用道摄像头
                                    type = 2;
                                    break;
                                case 5: // 应急车道拍照
                                    type = 3;
                                    break;
                                case 6: // 非机动车道(暂未使用)
                                    type = 0;
                                    break;
                                case 8: // 区间测速起始
                                    type = 4;
                                    break;
                                case 9: // 区间测速解除
                                    type = 5;
                                    break;
                                default:
                                    break;
                            }
                            BlueManager.getInstance().send(ProtocolUtils.getCameraInfo(true, type, type == 6 ? aMapNaviCameraInfos[0].getCameraSpeed() : aMapNaviCameraInfos[0].getCameraSpeed()));
                        } else {
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
                        int enter = 0;
                        int count = aMapLaneInfos.length;
                        for (int i = 0; i < aMapLaneInfos.length; i++) {
                            if (aMapLaneInfos[i].isRecommended()) {
                                enter += Math.pow(2, i);
                            }
                        }
                        Log.d("aMapLaneInfo  showLaneInfo ");
                        BlueManager.getInstance().send(ProtocolUtils.getLineInfo(true, count, enter));
                    }

                    @Override
                    public void showLaneInfo(AMapLaneInfo aMapLaneInfo) {
                    }

                    @Override
                    public void hideLaneInfo() {
                        Log.d("aMapLaneInfo  hideLaneInfo ");
                        BlueManager.getInstance().send(ProtocolUtils.getLineInfo(false, 0, 0));
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
                break;
            default:
                break;
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

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.AUTHORIZATION_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                break;
            case OBDEvent.NORMAL:
                obdStatusInfo = (OBDStatusInfo) data;
                break;
        }
    }
}
