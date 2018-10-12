package com.mapbar.hamster.log;

import android.os.Environment;

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
        Timber.plant(new FileLoggingTree(Environment.getExternalStorageDirectory().getPath() + "/obd"));
    }

    public static void d(String message) {
        Timber.tag(TAG);
        Timber.d(simpleDateFormat.format(new Date()) + "   " + message + "\n");
    }
}

