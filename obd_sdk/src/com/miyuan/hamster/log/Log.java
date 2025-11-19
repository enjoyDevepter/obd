package com.miyuan.hamster.log;

import android.content.Context;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class Log {

    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>();

    public final static String TAG = "OBD_CORE";
    private static ThreadPoolExecutor singleThreadExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue);

    public static void init(Context context) {
        Timber.plant(new Timber.DebugTree());// 调试模式下输出日志到 Logcat
        Timber.plant(new FileLoggingTree(context.getExternalFilesDir(null).getAbsolutePath())); // 生产环境自定义日志行为（如不输出）
    }

    public static void d(String message) {
        singleThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                Timber.tag(TAG);
                Timber.d(simpleDateFormat.format(new Date()) + "   " + message + "\n");
            }
        });
    }

    public static String toString(Throwable ex) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        pw.flush();
        return ex.toString();
    }
}
