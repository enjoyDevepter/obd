package com.mapbar.adas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.miyuan.obd.R;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BleCallBackListener, LocationListener {

    private static MainActivity INSTANCE = null;
    public boolean first = true;
    private ViewGroup rootViewGroup;
    private View splashView;

    private LocationManager locationManager;

    private int currentSpeed;

    private Map<Integer, List<String>> collecData = new HashMap<>();
    private List<String> list20 = new ArrayList();
    private List<String> list2060 = new ArrayList();
    private List<String> list60 = new ArrayList();
    private long lastLocationTime;
    private long firstLocationTime;
    private volatile float stratTrunBearing;
    private Timer heartTimer;
    private volatile double maxSpeed = 0;
    private boolean mathing;
    private volatile boolean adjust_success;
    private boolean notify;
    private boolean startCollect;

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

    /**
     * 设置状态栏颜色
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     */
    public static void setColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View statusView = createStatusView(activity, color);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);
        return statusView;
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

        setColor(this, Color.parseColor("#FF35BDB2"));

        collecData.put(20, list20);
        collecData.put(2060, list2060);
        collecData.put(60, list60);

        BlueManager.getInstance().init(this);

        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst()) {
            addTasks();
        }
        setFirst(false);
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BlueManager.getInstance().disconnect();
        MainActivity.INSTANCE = null;
        EventBus.getDefault().unregister(this);
        BlueManager.getInstance().removeCallBackListener(this);
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
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
//                .addTask(new LogInitTask())
                .addTask(new DisclaimerTask())
                .addTask(new LocationCheckTask())
                .addTask(new UpdateTask());
        TaskManager.getInstance().next();

    }

    @Subscriber(tag = EventBusTags.START_COLLECT)
    private void startCollect(boolean mathing) {
        this.mathing = mathing;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000, 60 * 1000);
    }

    private void stopCollecct() {
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
        BlueManager.getInstance().send(ProtocolUtils.stopCollect());
        locationManager.removeUpdates(this);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (data != null) {
                String result = data.getStringExtra(CaptureActivity.SCANRESULT);
                if (result != null) {
                    Bundle bundle = BackStackManager.getInstance().getCurrent().getDate();
                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    bundle.putString("sn", result);
                }
            }
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_DISCONNECTED:
                Toast.makeText(GlobalUtil.getContext(), "OBD连接断开！", Toast.LENGTH_SHORT).show();
                PageManager.go(new ConnectPage());
                break;
            case OBDEvent.ADJUST_SUCCESS:
                adjust_success = true;
                break;
            case OBDEvent.COLLECT_DATA:
                byte[] onePackage = (byte[]) data;
                if (System.currentTimeMillis() - lastLocationTime > 5000) { // 超过5s没获取到定位，数据丢弃
                    byte[] speed = new byte[]{(byte) currentSpeed};
                    byte[] pack = new byte[speed.length + onePackage.length];
                    System.arraycopy(speed, 0, pack, 0, speed.length);
                    System.arraycopy(onePackage, 0, pack, speed.length, onePackage.length);
                    if (currentSpeed < 20) {
                        list20.add(HexUtils.byte2HexStr(pack));
                    } else if (currentSpeed >= 20 || currentSpeed <= 60) {
                        list2060.add(HexUtils.byte2HexStr(pack));
                    } else {
                        list60.add(HexUtils.byte2HexStr(pack));
                    }
                }
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if ("gps".equals(location.getProvider())) {
            if (firstLocationTime == 0) {
                firstLocationTime = System.currentTimeMillis();
            }
            lastLocationTime = location.getTime();
            currentSpeed = (int) (location.getSpeed() * 3.6);
            maxSpeed = currentSpeed > maxSpeed ? currentSpeed : maxSpeed;
            if (!startCollect && currentSpeed > 20) {
                startCollect = true;
                BlueManager.getInstance().send(ProtocolUtils.startCollect());
            }
            if (stratTrunBearing == 0) {
                stratTrunBearing = location.getBearing();
            } else if (stratTrunBearing != 0) {
                if (!mathing) {
                    if (location.getBearing() - stratTrunBearing >= 150) {
                        stopCollect();
                    } else {
                        if (System.currentTimeMillis() - firstLocationTime >= 1000 * 60) { // 1分钟内未完成操作播报一次
                            // 提示请完成掉头操作
                            AlarmManager.getInstance().play(R.raw.trun);
                            firstLocationTime = System.currentTimeMillis();
                        }
                    }
                } else {
                    if (!adjust_success) { // 未校准完成
                        if (location.getBearing() - stratTrunBearing >= 150 && maxSpeed >= 55) {
                            stopCollect();
                        } else {
                            if (System.currentTimeMillis() - firstLocationTime >= 1000 * 60) { // 1分钟内未完成操作播报一次
                                if (location.getBearing() - stratTrunBearing < 150) {
                                    // 提示请完成掉头操作
                                    AlarmManager.getInstance().play(R.raw.trun);
                                    firstLocationTime = System.currentTimeMillis();
                                    return;
                                }
                                if (maxSpeed < 55) {
                                    // 提示请完成加速
                                    AlarmManager.getInstance().play(R.raw.speed);
                                    firstLocationTime = System.currentTimeMillis();
                                    return;
                                }
                            }
                        }
                    } else {
                        // 校准完成
                        if (!notify) { // 未通知
                            notify = true;
                            stopCollect();
                        }
                    }
                }
            }
        }
    }

    private void stopCollect() {
        stopCollecct();
        EventBus.getDefault().post(0, EventBusTags.COLLECT_FINISHED);
        new Thread(new FileRunnable("Normal2050")).start();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private class FileRunnable implements Runnable {

        private String fileName;

        public FileRunnable(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public void run() {
            try {
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "obd_collect" + File.separator);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, fileName);
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

                    for (String str : list20) {
                        bw.write(str);
                        bw.newLine();
                        bw.flush();
                    }
                    for (String str : list2060) {
                        bw.write(str);
                        bw.newLine();
                        bw.flush();
                    }
                    for (String str : list60) {
                        bw.write(str);
                        bw.newLine();
                        bw.flush();
                    }
                    bw.close();
                    fos.close();
                    // 上传
                } catch (FileNotFoundException e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
