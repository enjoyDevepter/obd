package com.miyuan.obd;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.utils.NotificationUtil;

public class LocationService extends Service {
    public static final String EXTRA_NOTIFICATION_CONTENT = "notification_content";
    private static final String CHANNEL_ID = "com.miyuan.obd";
    private static final String CHANNEL_NAME = "Default Channel";

    private NotificationUtil notificationUtil;

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
}
