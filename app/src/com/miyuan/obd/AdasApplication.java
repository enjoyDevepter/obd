package com.miyuan.obd;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.WindowManager;

import com.miyuan.adas.GlobalUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class AdasApplication extends Application {

    public static void registerUncaughtException() {
        final Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable ex) {
                ex.printStackTrace();
                String error = format(ex);
                recordErrorToFile(error);
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }

    public static String format(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    static void recordErrorToFile(String error) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = sdf.format(new Date(System.currentTimeMillis()));
        String dirPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "obd";
        final File file = new File(dirPath, fileName + "_error.log");
        FileOutputStream fos = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            fos.write(error.getBytes(), 0, error.getBytes().length);
        } catch (Exception e) {
            try {
                if (null != fos) {
                    fos.flush();
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        GlobalUtil.setContext(this);
        GlobalUtil.setHandler(new Handler());
        registerUncaughtException();

        GlobalUtil.setOkHttpClient(new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build());

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityStarted(Activity activity) {

            }

            @Override
            public void onActivityResumed(Activity activity) {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }

            @Override
            public void onActivityPaused(Activity activity) {

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {

            }
        });
    }
}