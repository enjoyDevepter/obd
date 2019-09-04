package com.miyuan.hamster.log;

import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by guomin on 2018/4/23.
 */

public class Log {

    public final static String TAG = "OBD_CORE";
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    static {
        Timber.plant(new Timber.DebugTree());
        Timber.plant(new FileLoggingTree(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "log"));
    }

    public static void d(String message) {
        Timber.tag(TAG);
        Timber.d("Thread id  " + Thread.currentThread() + "  " + simpleDateFormat.format(new Date()) + "   " + message + "\n");
    }
}

