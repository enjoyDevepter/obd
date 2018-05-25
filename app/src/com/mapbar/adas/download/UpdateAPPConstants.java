package com.mapbar.adas.download;

import android.os.Build;
import android.os.Environment;

/**
 * Created by shisk on 2017/7/19.
 */
//http://wdservice.mapbar.com/appstorewsapi/checkexistlist/21?ck=a7dc3b0377b14a6cb96ed3d18b5ed117&package_name=com.mapbar.obd.net.android
public class UpdateAPPConstants {
    public static final String APPUPDATE_URL = "http://wdservice.mapbar.com/appstorewsapi/checkexistlist/" + Build.VERSION.SDK_INT;
    public static final String UPDATE_FOLDER = Environment.getExternalStorageDirectory() + "/mapbar/download/";
    public static final String UPDATE_FILE = "mapbar_adas.apk";
    public static String CK = "8fe3c993bf3e4dc297976ca88017eed6";
    public static AppInfoBean appInfoBean = null;
}
