package com.mapbar.adas;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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
import com.gyf.barlibrary.ImmersionBar;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements BleCallBackListener, LocationListener {

    private static MainActivity INSTANCE = null;
    public boolean first = true;
    private ViewGroup rootViewGroup;
    private View splashView;
    private boolean uploadSuccess;
    private LocationManager locationManager;

    private int currentSpeed;

    /**
     * 航向集合
     */
    private LinkedList<Float> bears = new LinkedList<>();
    private LinkedList<Long> bearsTime = new LinkedList<>();

    private Map<Integer, List<String>> collecData = new HashMap<>();
    private List<String> list20 = new ArrayList();
    private List<String> list2060 = new ArrayList();
    private List<String> list60 = new ArrayList();
    private List<String> locationList = new ArrayList<>();
    private long lastLocationTime;
    private long firstLocationTime;
    private Timer heartTimer;
    private volatile double maxSpeed = 0;
    private boolean mathing;
    private volatile boolean adjust_success;
    private boolean notify;
    private boolean startCollect;
    private OBDStatusInfo obdStatusInfo;
    private StringBuilder sb = new StringBuilder();
    private volatile boolean hasTrun = false;
    private long startTime;

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

        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isFirst()) {
            addTasks();
        }
        setFirst(false);
        BlueManager.getInstance().addBleCallBackListener(this);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);
    }

    @Override
    protected void onDestroy() {
        ImmersionBar.with(this).destroy(); //不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
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
                .addTask(new DisclaimerTask())
                .addTask(new LocationCheckTask())
                .addTask(new UpdateTask());
        TaskManager.getInstance().next();

    }

    @Subscriber(tag = EventBusTags.START_COLLECT)
    private void startCollect(boolean mathing) {
        this.mathing = mathing;
        startTime = System.currentTimeMillis();
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
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
        locationManager.removeUpdates(this);
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
        BlueManager.getInstance().send(ProtocolUtils.stopCollect());
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
            case OBDEvent.AUTHORIZATION_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                break;
            case OBDEvent.STATUS_UPDATA:
                obdStatusInfo = (OBDStatusInfo) data;
                updateStatusInfo(obdStatusInfo);
                break;
            case OBDEvent.ADJUST_SUCCESS:
                adjust_success = true;
                break;
            case OBDEvent.COLLECT_DATA:
                byte[] onePackage = (byte[]) data;
                if (System.currentTimeMillis() - lastLocationTime > 5000) { // 超过5s没获取到定位，数据丢弃
                    return;
                }
                byte[] speed = new byte[]{(byte) currentSpeed};
                byte[] time = HexUtils.longToByte(lastLocationTime);
                byte[] pack = new byte[speed.length + time.length + onePackage.length];
                System.arraycopy(speed, 0, pack, 0, speed.length);
                System.arraycopy(time, 0, pack, speed.length, time.length);
                System.arraycopy(onePackage, 0, pack, speed.length + time.length, onePackage.length);
                if (currentSpeed < 20) {
                    list20.add(HexUtils.byte2HexStr(pack));
                } else if (currentSpeed >= 20 || currentSpeed <= 60) {
                    list2060.add(HexUtils.byte2HexStr(pack));
                } else {
                    list60.add(HexUtils.byte2HexStr(pack));
                }
                break;
            case OBDEvent.COLLECT_DATA_FOR_CAR:
                new Thread(new CarRunnable((byte[]) data)).start();
                break;
        }
    }

    /**
     * 上传状态信息
     *
     * @param obdStatusInfo
     */
    private void updateStatusInfo(OBDStatusInfo obdStatusInfo) {

        if ("0000000000000000000".equals(obdStatusInfo.getSn())) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("bState", HexUtils.formatHexString(obdStatusInfo.getOrginal()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("updateStatusInfo input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_TIRE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("updateStatusInfo failure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("updateStatusInfo success " + responese);
            }
        });

    }

    @Override
    public void onLocationChanged(Location location) {
        if ("gps".equals(location.getProvider())) {
            if (firstLocationTime == 0) {
                firstLocationTime = System.currentTimeMillis();
            }
            lastLocationTime = System.currentTimeMillis();
            currentSpeed = (int) (location.getSpeed() * 3.6);
            maxSpeed = currentSpeed > maxSpeed ? currentSpeed : maxSpeed;
            if (!startCollect && currentSpeed > 15) {
                startCollect = true;
                BlueManager.getInstance().send(ProtocolUtils.startCollect());
            }
            sb = new StringBuilder();
            sb.append(location.getTime()).append("#")
                    .append(location.getSpeed()).append("#")
                    .append(currentSpeed).append("#")
                    .append(location.getLongitude()).append("#")
                    .append(location.getLatitude()).append("#");
            locationList.add(sb.toString());

            if (!startCollect && startTime != 0 && System.currentTimeMillis() - startTime > 2 * 1000 * 60) {
                Log.d("提示加速到20迈");
                AlarmManager.getInstance().play(R.raw.speed_20);
                startTime = System.currentTimeMillis();
            }

            Log.d("location.getBearing() " + location.getBearing() + "     currentSpeed  " + currentSpeed);

            if (startCollect) {
                if (currentSpeed > 10) {
                    Log.d("location.getBearing() " + location.getBearing() + "     currentSpeed  " + currentSpeed);
                    if (bears.size() > 9) {
                        bears.pollFirst();
                        bearsTime.pollFirst();
                    }
                    bears.addLast(location.getBearing());
                    bearsTime.addLast(location.getTime());
                }
                if (!hasTrun) {
                    if (hasTurn()) {
                        hasTrun = true;
                    }
                }

                if (!mathing) {
                    if (hasTrun && maxSpeed >= 50 && (list20.size() > 0 || list2060.size() > 0 || list60.size() > 0)) {
                        if (!uploadSuccess) {
                            uploadSuccess = true;
                            Log.d("提示校准失败");
                            AlarmManager.getInstance().play(R.raw.fail);
                            stopCollect();
                        }
                    } else {
                        if (!hasTrun) {
                            if (System.currentTimeMillis() - firstLocationTime >= 1000 * 60) {
                                // 提示请完成掉头操作
                                Log.d("提示请完成掉头操作");
                                AlarmManager.getInstance().play(R.raw.trun);
                                firstLocationTime = System.currentTimeMillis();
                                return;
                            }
                        } else {
                            if (System.currentTimeMillis() - firstLocationTime >= 1000 * 60) {
                                // 提示请完成加速
                                Log.d("提示请完成加速");
                                AlarmManager.getInstance().play(R.raw.speed);
                                firstLocationTime = System.currentTimeMillis();
                                return;
                            }
                        }
                    }
                } else {
                    if (!adjust_success) { // 未校准完成
                        if (hasTrun && maxSpeed >= 50 && (list20.size() > 0 || list2060.size() > 0 || list60.size() > 0)) {
                            if (!uploadSuccess) {
                                uploadSuccess = true;
                                AlarmManager.getInstance().play(R.raw.success);
                                stopCollect();
                            }
                        } else {
                            if (!hasTrun) {
                                if (System.currentTimeMillis() - firstLocationTime >= 1000 * 60) {
                                    // 提示请完成掉头操作
                                    Log.d("提示请完成掉头操作");
                                    AlarmManager.getInstance().play(R.raw.trun);
                                    firstLocationTime = System.currentTimeMillis();
                                    return;
                                }
                            } else {
                                if (System.currentTimeMillis() - firstLocationTime >= 1000 * 60) {
                                    // 提示请完成加速
                                    Log.d("提示请完成加速");
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
                            Log.d("提示校准完成");
                            AlarmManager.getInstance().play(R.raw.success);
                            EventBus.getDefault().post(0, EventBusTags.COLLECT_FINISHED);
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
        new Thread(new LocationRunnable("location")).start();
    }

    private boolean hasTurn() {
        if (bears.size() > 2) {
            float first = bears.getFirst();
            long firstTime = bearsTime.getFirst();
            float last = bears.getLast();
            long lastTime = bearsTime.getLast();
            if (150 <= Math.abs(last - first) && Math.abs(last - first) <= 210 && lastTime - firstTime <= 10000) {
                return true;
            }
        }
        return false;
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

    private void uploadCollectData(String filePath) {
        final File file = new File(filePath);

        Log.d("uploadCollectData input ");

        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                //一样的效果
                .addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                .addPart(MultipartBody.Part.createFormData("type", "2"))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"file\"; filename=\"Normal2050\"")
                        , fileBody).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_ERROR_FILE)
                .post(multipartBody)
                .build();

        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("uploadCollectData onFailure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("uploadCollectData success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
//                        if (file.exists()) {
//                            file.delete();
//                        }
                    }
                } catch (JSONException e) {
                    Log.d("uploadCollectData failure " + e.getMessage());
                }
            }
        });
    }

    private void uploadLocationData(String filePath) {

        final File file = new File(filePath);

        Log.d("uploadLocationData input ");

        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                //一样的效果
                .addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                .addPart(MultipartBody.Part.createFormData("type", "3"))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"file\"; filename=\"location\"")
                        , fileBody).build();


        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_ERROR_FILE)
                .post(multipartBody)
                .build();

        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("uploadCollectData onFailure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("uploadCollectData success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
//                        if (file.exists()) {
//                            file.delete();
//                        }
                    }
                } catch (JSONException e) {
                    Log.d("uploadCollectData failure " + e.getMessage());
                }
            }
        });
    }


    private void uploadCarData(String filePath) {

        final File file = new File(filePath);

        Log.d("uploadCarData input ");

        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                //一样的效果
                .addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                .addPart(MultipartBody.Part.createFormData("type", "4"))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"file\"; filename=\"car\"")
                        , fileBody).build();


        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_ERROR_FILE)
                .post(multipartBody)
                .build();

        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("uploadCarData onFailure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("uploadCarData success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
//                        if (file.exists()) {
//                            file.delete();
//                        }
                    }
                } catch (JSONException e) {
                    Log.d("uploadCarData failure " + e.getMessage());
                }
            }
        });
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
                    uploadCollectData(file.getPath());
                } catch (FileNotFoundException e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class LocationRunnable implements Runnable {

        private String fileName;

        public LocationRunnable(String fileName) {
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

                    for (String str : locationList) {
                        bw.write(str);
                        bw.newLine();
                        bw.flush();
                    }
                    bw.close();
                    fos.close();
                    // 上传
                    uploadLocationData(file.getPath());
                } catch (FileNotFoundException e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class CarRunnable implements Runnable {

        private byte[] data;

        public CarRunnable(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "obd_collect" + File.separator);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, "car");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                    bw.write(HexUtils.byte2HexStr(data));
                    bw.flush();
                    bw.close();
                    fos.close();
                    // 上传
                    uploadCarData(file.getPath());
                } catch (FileNotFoundException e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
