package com.miyuan.obd;

import android.app.Application;
import android.os.Handler;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.log.Log;
import com.miyuan.obd.log.MapbarStorageUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
                String error = Log.toString(ex);
                recordErrorToFile(error);
                defaultUncaughtExceptionHandler.uncaughtException(thread, ex);
            }
        });
    }

    static void recordErrorToFile(String error) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String fileName = sdf.format(new Date(System.currentTimeMillis()));
        String dirPath = MapbarStorageUtil.getCurrentValidMapbarPath();
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
    }
}