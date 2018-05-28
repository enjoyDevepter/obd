package com.mapbar.adas;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mapbar.hamster.OBDAidlInterface;
import com.mapbar.hamster.core.Hex;
import com.mapbar.hamster.core.OBDService;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

public class MainActivity extends AppCompatActivity {

    private static MainActivity INSTANCE = null;
    private final int head = 0x7d;
    public boolean first = true;
    private ViewGroup rootViewGroup;
    private View splashView;
    private OBDAidlInterface obdAidlInterface;

    public MainActivity() {
        if (null == MainActivity.INSTANCE) {
            MainActivity.INSTANCE = this;
            GlobalUtil.setMainActivity(this);
        } else {
            throw new RuntimeException("MainActivity.INSTANCE is not null");
        }
    }

    /**
     * 获得实例
     *
     * @return
     */
    public static MainActivity getInstance() {
        return MainActivity.INSTANCE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        super.onCreate(savedInstanceState);

        GlobalUtil.setMainActivity(this);

        rootViewGroup = new FrameLayout(this);

        // 页面容器
        final FrameLayout pageContainer = new FrameLayout(this);
        pageContainer.setId(R.id.main_activity_page_layer);
        rootViewGroup.addView(pageContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // 启动画面
        splashView = new View(this);
        splashView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        splashView.setBackgroundResource(R.drawable.splash);
        rootViewGroup.addView(splashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(rootViewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        bindService(new Intent(this, OBDService.class), new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                obdAidlInterface = OBDAidlInterface.Stub.asInterface(service);
                try {
                    obdAidlInterface.connect();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        }, BIND_AUTO_CREATE);

        String sn = "a3116080275741";
        byte[] bytes = sn.getBytes();
        for (int i = 0; i < bytes.length; i++) {
            Log.d("bytes[" + i + "] == " + bytes[i] + "   " + Integer.toHexString(bytes[i]));
        }
        Log.d(Hex.str2HexStr(sn));

        Log.d("======================");

        long time = System.currentTimeMillis();

        Log.d("" + time);

        bytes = Hex.longToByte(time);

        for (int i = 0; i < bytes.length; i++) {
            Log.d("bytes[" + i + "] == " + bytes[i] + "   " + Integer.toHexString(bytes[i]));
        }

        Log.d(String.valueOf(Hex.byteToLong(bytes)));

        Log.d("======================");

        bytes = new byte[]{head};

        Log.d(String.valueOf(bytes[0]));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst()) {
            addTasks();
        }
        setFirst(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        MainActivity.INSTANCE = null;
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    private void addTasks() {
        TaskManager.getInstance()
                .addTask(new SDInitTask())
                .addTask(new LogInitTask())
                .addTask(new DisclaimerTask())
                .addTask(new UpdateTask());
        TaskManager.getInstance().next();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void onBack() {
        final BasePage current = BackStackManager.getInstance().getCurrent();
        if (current != null) {
            if (current.onBackPressed()) {
            } else {
                PageManager.back();
            }
        }
    }


    public void hideSplash() {
        if (splashView != null) {
            rootViewGroup.removeView(splashView);
            splashView = null;
        }
        getWindow().setBackgroundDrawable(null);
    }
}
