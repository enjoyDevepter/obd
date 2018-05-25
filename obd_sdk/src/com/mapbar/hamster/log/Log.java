package com.mapbar.hamster.log;

import com.mapbar.hamster.BuildConfig;

import timber.log.Timber;

/**
 * Created by guomin on 2018/4/23.
 */

public class Log {

    public final static String TAG = "OBD_CORE";

    static {
        if (BuildConfig.IS_SHOW_LOG) {
            Timber.plant(new Timber.DebugTree());
        }
    }

    public static void d(String message) {
        Timber.tag(TAG);
        Timber.d(message);
    }
}

