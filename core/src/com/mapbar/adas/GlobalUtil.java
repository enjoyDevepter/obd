package com.mapbar.adas;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class GlobalUtil {

    private static Handler handler;

    private static Context context;

    private static Activity mainActivity;

    private static int sUID;

    private static String resPackageName;

    public static Context getContext() {
        return GlobalUtil.context;
    }

    public static void setContext(Context context) {
        GlobalUtil.context = context;
    }

    public static Resources getResources() {
        Context context = getMainActivity();
        if (context == null) {
            context = getContext();
        }
        return context.getResources();
    }

    public static Handler getHandler() {
        return GlobalUtil.handler;
    }

    public static void setHandler(Handler handler) {
        GlobalUtil.handler = handler;
    }


    public static Activity getMainActivity() {
        return mainActivity;
    }

    public static void setMainActivity(Activity mainActivity) {
        GlobalUtil.mainActivity = mainActivity;
    }

    public static String getFromAssets(Context context, String fileName) throws IOException {
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(fileName)));
        String line = "";
        StringBuilder result = new StringBuilder();
        while (null != (line = bufReader.readLine())) {
            result.append(line);
        }
        return result.toString();
    }

    public static String getResPackageName() {
        return resPackageName;
    }

    /**
     * 确保该包名同AndroidManifest中的package一致
     */
    public static void setResPackageName(String resPackageName) {
        GlobalUtil.resPackageName = resPackageName;
    }

    /**
     * 判断当前线程是否非UI线程
     *
     * @return
     */
    public static boolean isNotUIThread() {
        return Looper.myLooper() != Looper.getMainLooper();
    }


    public static boolean isM() {
        return Build.VERSION.SDK_INT >= 23 || "MNC".equals(Build.VERSION.CODENAME);
    }

    public static boolean isKitKat() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static boolean isNougat() {
        return Build.VERSION.SDK_INT >= 24 || "N".equals(Build.VERSION.CODENAME);
    }

    public static int getUnixUID() {
        if (sUID == 0) {
            try {
                sUID = mainActivity.getPackageManager().getPackageInfo(resPackageName, 0).applicationInfo.uid;
            } catch (Throwable e) {
//
            }
        }
        return sUID;
    }
}
