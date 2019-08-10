package com.mapbar.adas;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.IBinder;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.navi.AMapNavi;
import com.mapbar.hamster.log.Log;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
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
            String longitudestr = String.valueOf(longitude);
            String latitudestr = String.valueOf(latitude);
            Log.d("longitude:" + longitude + ",latitude：" + latitude);

            //设置外部GPS数据
            Location location = new Location("gps仪器型号");
            location.setLongitude(longitude);
            location.setLatitude(latitude);
            location.setSpeed(amapLocation.getSpeed());
            location.setAccuracy(amapLocation.getAccuracy());
            location.setBearing(amapLocation.getBearing());
            location.setTime(amapLocation.getTime());
            AMapNavi.getInstance(getBaseContext()).setExtraGPSData(2, location);
        }
    };
    //声明AMapLocationClient类对象
    AMapLocationClient mLocationClient = null;

    public LocationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        getPosition();
    }

    public void getPosition() {
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        // 设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        // 初始化AMapLocationClientOption对象
        mLocationOption = new AMapLocationClientOption();
        // 设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(1000);
        // 获取一次定位结果： //该方法默认为false。
        mLocationOption.setOnceLocation(false);
        mLocationOption.setOnceLocationLatest(false);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 启动定位
        mLocationClient.startLocation();
    }
}