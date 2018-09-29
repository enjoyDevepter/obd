package com.mapbar.adas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

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
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.collect_two_layout, toHistory = false)
public class CollectTwoPage extends AppBasePage implements LocationListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    private Timer heartTimer;
    private LocationManager locationManager;
    private List<String> list20 = new ArrayList();
    private List<String> list2060 = new ArrayList();
    private List<String> list60 = new ArrayList();
    private List<String> locationList = new ArrayList<>();
    private int currentSpeed;
    private long lastLocationTime;
    private long firstLocationTime;
    private long turnLocationTime;
    private LinkedList<Float> bears = new LinkedList<>();
    private LinkedList<Long> bearsTime = new LinkedList<>();

    private Map<Integer, List<String>> collecData = new HashMap<>();
    private volatile double maxSpeed = 0;

    private boolean startCollect;

    private boolean uploadSuccess;

    private long startTime;

    private volatile boolean hasTrun = false;

    private StringBuilder sb = new StringBuilder();

    @Override
    public void onResume() {
        super.onResume();
        reportV.setVisibility(View.GONE);
        back.setVisibility(View.GONE);
        title.setText("开始校准");
        BlueManager.getInstance().addBleCallBackListener(this);
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);
        GlobalUtil.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmManager.getInstance().play(R.raw.start_adjust);
            }
        }, 2000);
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000, 60 * 1000);

        collecData.put(20, list20);
        collecData.put(2060, list2060);
        collecData.put(60, list60);

        startTime = System.currentTimeMillis();
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        BlueManager.getInstance().removeCallBackListener(this);
        locationManager.removeUpdates(this);
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
        super.onDestroy();
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

            if (!startCollect && startTime != 0 && System.currentTimeMillis() - startTime > 40 * 1000) {
                Log.d("提示加速到20迈");
                AlarmManager.getInstance().play(R.raw.speed_20);
                startTime = System.currentTimeMillis();
                return;
            }

            Log.d("location.getBearing() " + location.getBearing() + "     currentSpeed  " + currentSpeed);

            if (startCollect) {
                if (currentSpeed > 10) {
                    if (bears.size() > 9) {
                        bears.pollFirst();
                        bearsTime.pollFirst();
                    }
                    bears.addLast(location.getBearing());
                    bearsTime.addLast(location.getTime());
                }
                if (!hasTrun) {
                    if (hasTurn()) {
                        turnLocationTime = System.currentTimeMillis();
                        hasTrun = true;
                    }
                }
                if (hasTrun && maxSpeed >= 50 && (list20.size() > 0 || list2060.size() > 0 || list60.size() > 0)) {
                    if (!uploadSuccess) {
                        uploadSuccess = true;
                        stopCollect();
                    }
                } else {
                    if (!hasTrun) {
                        if (System.currentTimeMillis() - firstLocationTime >= 1000 * 40) {
                            // 提示请完成掉头操作
                            Log.d("提示请完成掉头操作");
                            AlarmManager.getInstance().play(R.raw.trun);
                            firstLocationTime = System.currentTimeMillis();
                        }
                    } else {
                        if (System.currentTimeMillis() - turnLocationTime >= 5 || System.currentTimeMillis() - firstLocationTime >= 1000 * 40) {
                            // 提示请完成加速
                            turnLocationTime = Long.MAX_VALUE;
                            Log.d("提示请完成加速");
                            AlarmManager.getInstance().play(R.raw.speed_60);
                            firstLocationTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        }
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

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
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
        }
    }

    private void stopCollect() {
        locationManager.removeUpdates(this);
        BlueManager.getInstance().send(ProtocolUtils.stopCollect());
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
        new Thread(new FileRunnable("Normal2050")).start();
        new Thread(new LocationRunnable("location")).start();
        updateCollectStauts();
    }

    private void updateCollectStauts() {
        boolean matching = getDate().getBoolean("matching");
        if (matching) {
            PageManager.go(new CollectPage());
        } else {
            CollectFinish collectFinish = new CollectFinish();
            Bundle bundle = new Bundle();
            bundle.putString("sn", getDate().getString("sn"));
            bundle.putString("pVersion", getDate().getString("pVersion"));
            bundle.putString("bVersion", getDate().getString("bVersion"));
            bundle.putBoolean("success", false);
            collectFinish.setDate(bundle);
            PageManager.go(collectFinish);
        }

    }

    private boolean hasTurn() {
        if (bears.size() > 2) {
            float last = bears.getLast();
            long lastTime = bearsTime.getLast();
            // 最后一个和之前的比较
            for (int i = 0; i < bears.size() - 2; i++) {
                if (150 <= Math.abs(last - bears.get(i)) && Math.abs(last - bears.get(i)) <= 210 && lastTime - bearsTime.get(i) <= 10000) {
                    return true;
                }
            }
        }
        return false;
    }


    private void uploadCollectData(String filePath) {
        final File file = new File(filePath);

        Log.d("uploadCollectData input ");

        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                //一样的效果
                .addPart(MultipartBody.Part.createFormData("serialNumber", getDate().getString("sn")))
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
                        if (file.exists()) {
                            file.delete();
                        }
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
                .addPart(MultipartBody.Part.createFormData("serialNumber", getDate().getString("sn")))
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
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } catch (JSONException e) {
                    Log.d("uploadCollectData failure " + e.getMessage());
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
}
