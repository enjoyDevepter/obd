package com.miyuan.obd;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.navi.AMapNavi;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.utils.NotificationUtil;

public class LocationService extends Service {
    public static final String EXTRA_NOTIFICATION_CONTENT = "notification_content";
    private static final String CHANNEL_ID = "com.miyuan.obd";
    private static final String CHANNEL_NAME = "Default Channel";

    private NotificationUtil notificationUtil;

    // 声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;
    // 声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {
            if (amapLocation == null) {
                Log.d("amapLocation is null!");
                return;
            }
            if (amapLocation.getErrorCode() != 0) {
                Log.d("amapLocation has exception errorCode:" + amapLocation.getErrorCode());
                return;
            }
            Double longitude = amapLocation.getLongitude();//获取经度
            Double latitude = amapLocation.getLatitude();//获取纬度
            //设置外部GPS数据
            Location location = new Location("gps仪器型号");
            location.setLongitude(longitude);
            location.setLatitude(latitude);
            location.setSpeed(amapLocation.getSpeed());
            location.setAccuracy(amapLocation.getAccuracy());
            location.setBearing(amapLocation.getBearing());
            location.setTime(amapLocation.getTime());
            AMapNavi.getInstance(getBaseContext()).setExtraGPSData(1, location);
        }
    };
    //声明AMapLocationClient类对象
    AMapLocationClient mLocationClient = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return START_NOT_STICKY;
        }

        String content = intent.getStringExtra(EXTRA_NOTIFICATION_CONTENT);
        notificationUtil = new NotificationUtil(GlobalUtil.getContext(), R.drawable.ic_launcher,
                "后台运行...", content,
                CHANNEL_ID, CHANNEL_NAME);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NotificationUtil.NOTIFICATION_ID, notificationUtil.getNotification());
        } else {
            notificationUtil.showNotification();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (notificationUtil != null) {
            notificationUtil.cancelNotification();
            notificationUtil = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getPosition();
    }

    public void getPosition() {
        // 初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        // 初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        // 设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);
        // 给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }
}
