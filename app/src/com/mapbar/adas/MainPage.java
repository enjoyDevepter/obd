package com.mapbar.adas;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.adas.view.SensitiveView;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDVersionInfo;
import com.mapbar.hamster.Update;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.main_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class MainPage extends AppBasePage implements View.OnClickListener, BleCallBackListener, LocationListener {
    private static final int UNIT = 1024;
    CustomDialog dialog = null;
    private volatile boolean verified;
    private String sn;
    private String boxId;
    private SensitiveView.Type type = SensitiveView.Type.MEDIUM;
    private LocationManager locationManager;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.sensitive)
    private View sensitive;
    @ViewInject(R.id.warm)
    private View warm;
    @ViewInject(R.id.reset)
    private View reset;
    @ViewInject(R.id.change_car)
    private View change;
    @ViewInject(R.id.tire_pressure_info)
    private View pressureInfo;
    @ViewInject(R.id.tire_pressure)
    private View pressureIcon;
    private byte[] updates;
    private OBDVersion obdVersion;
    private ProgressBar progressBar;

    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mWorkerThread;
    private Handler mHandler;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText(R.string.app_name);
        warm.setOnClickListener(this);
        reset.setOnClickListener(this);
        sensitive.setOnClickListener(this);
        change.setOnClickListener(this);

        BlueManager.getInstance().addBleCallBackListener(this);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);

        if (BlueManager.getInstance().isConnected()) {
            verify();
        }

        mWorkerThread = new HandlerThread(MainPage.class.getSimpleName());
        mWorkerThread.start();
        mHandler = new WorkerHandler(mMainHandler, mWorkerThread.getLooper());

        Log.d("onResumeonResumeonResume  ");
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.warm:
                showWarm();
                break;
            case R.id.sensitive:
                showSensitive();
                break;
            case R.id.reset:
                showReset();
                break;
            case R.id.change_car:
                changeCar();
                break;
        }
    }

    private void changeCar() {
        ChoiceCarPage choiceCarPage = new ChoiceCarPage();
        PageManager.go(choiceCarPage);
    }

    private void showWarm() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().write(ProtocolUtils.playWarm(02));
                            }
                        });

                        view.findViewById(R.id.auth).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().write(ProtocolUtils.playWarm(01));
                            }
                        });

                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_warm)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void showSensitive() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                        final SensitiveView sensitiveView = (SensitiveView) view.findViewById(R.id.sensitive);
                        sensitiveView.setType(type);
                        sensitiveView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (sensitiveView.getType()) {
                                    case LOW:
                                        sensitiveView.setType(SensitiveView.Type.MEDIUM);
                                        BlueManager.getInstance().write(ProtocolUtils.setSensitive(02));
                                        break;
                                    case MEDIUM:
                                        sensitiveView.setType(SensitiveView.Type.Hight);
                                        BlueManager.getInstance().write(ProtocolUtils.setSensitive(03));
                                        break;
                                    case Hight:
                                        sensitiveView.setType(SensitiveView.Type.LOW);
                                        BlueManager.getInstance().write(ProtocolUtils.setSensitive(01));
                                        break;
                                }
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_sensitive)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void showReset() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().write(ProtocolUtils.study());
                                dialog.dismiss();
                            }
                        });

                        view.findViewById(R.id.unsave).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_save)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_FIRST_USE:
                Log.d("OBDEvent.OBD_FIRST_USE ");
                // 激活
                boxId = (String) data;
                Log.d("boxId  " + boxId);
                PhonePage phonePage = new PhonePage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", boxId);
                phonePage.setDate(bundle);
                PageManager.go(phonePage);
                break;
            case OBDEvent.OBD_NORMAL:
                Log.d("OBDEvent.OBD_NORMAL ");
                int typeInt = Integer.valueOf((String) data);
                switch (typeInt) {
                    case 0:
                        type = SensitiveView.Type.LOW;
                        break;
                    case 1:
                        type = SensitiveView.Type.MEDIUM;
                        break;
                    case 2:
                        type = SensitiveView.Type.Hight;
                        break;
                }
                // 获取OBD版本信息，请求服务器是否有更新
                BlueManager.getInstance().write(ProtocolUtils.getVersion());
                break;
            case OBDEvent.OBD_EXPIRE:
                Log.d("OBDEvent.OBD_EXPIRE ");
                sn = (String) data;
                // 获取授权码
                getLisense();
                break;
            case OBDEvent.OBD_BEGIN_UPDATE:
                if ((Integer) data == 0) { // 是否可以升级
                    try {
                        Thread.sleep(500);
                        updateHeader(obdVersion);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 固件升级开始
                    updateForOneUnit(1);
                }
                break;
            case OBDEvent.OBD_UPDATE_FINISH_UNIT:
                Update update = (Update) data;
                if (update.getStatus() == 0) {
                    // 重新传递
                    updateForOneUnit(update.getIndex());
                } else if (update.getStatus() == 1) {
                    // 继续
                    updateForOneUnit(update.getIndex() + 1);
                } else if (update.getStatus() == 2) {
                    // 升级完成，通知服务器
                    notifyUpdateSuccess();
                    if (null != progressBar && null != dialog) {
                        dialog.dismiss();
                        progressBar = null;
                    }
                    // 开始检查胎压
                    mHandler.sendEmptyMessage(0);
                }
                break;
            case OBDEvent.OBD_GET_VERSION:
                OBDVersionInfo version = (OBDVersionInfo) data;
                sn = version.getSn();
                checkOBDVersion(version);
                break;
            case OBDEvent.OBD_AUTH_RESULT:
                // 授权结果
                if ((Integer) data == 1) {
                    activate_success();
                } else {
                    Toast.makeText(getContext(), "授权失败!", Toast.LENGTH_LONG).show();
                }
                break;
            case OBDEvent.OBD_UPDATE_PARAMS_SUCCESS:
                // 判断是否需要升级固件
                if (null != obdVersion) {
                    if (obdVersion.getUpdateState() == 1) {
                        updateHeader(obdVersion);
                    } else {
                        mHandler.sendEmptyMessage(0);
                    }
                }
                break;
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                // 胎压状态改变，
                if ((Integer) data != 0) {
                    pressureInfo.setVisibility(View.VISIBLE);
                    pressureIcon.setBackgroundResource(R.drawable.unusual);
                }
                break;
            case OBDEvent.OBD_ERROR:
                switch ((Integer) data) {
                    case 1:
                        Toast.makeText(getContext(), "校验码失效", Toast.LENGTH_LONG).show();
                        break;
                    case 3:
                        Toast.makeText(getContext(), "固件升级未完成", Toast.LENGTH_LONG).show();
                        break;
                    case 4:
                        Toast.makeText(getContext(), "授权过期", Toast.LENGTH_LONG).show();
                        break;
                    case 6:
                        Toast.makeText(getContext(), "系统繁忙，稍后再试", Toast.LENGTH_LONG).show();
                        break;
                }
                break;
        }
    }

    /**
     * 激活成功
     */
    private void activate_success() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", getDate().get("sn"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.ACTIVATE_SUCCESS)
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("activate failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("activate success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        BlueManager.getInstance().write(ProtocolUtils.getVersion());
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("activate failure " + e.getMessage());
                }
            }
        });
    }

    private void checkOBDVersion(OBDVersionInfo version) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
            jsonObject.put("bVersion", version.getVersion());
            jsonObject.put("pVersion", version.getCar_no());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE)
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkOBDVersion failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkOBDVersion success " + responese);
                obdVersion = JSON.parseObject(responese, OBDVersion.class);
                if ("000".equals(obdVersion.getStatus())) {
                    switch (obdVersion.getUpdateState()) {
                        case 0:
                            // 定时获取胎压状态
                            mHandler.sendEmptyMessage(0);
                            break;
                        case 1: // 版本参数都更新
                            BlueManager.getInstance().write(ProtocolUtils.updateParams(sn, obdVersion.getParams()));
                            break;
                        case 2: // 只有版本更新
                            updateHeader(obdVersion);
                            break;
                        case 3: // 只有参数更新
                            BlueManager.getInstance().write(ProtocolUtils.updateParams(sn, obdVersion.getParams()));
                            break;
                    }
                } else {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(), obdVersion.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }


    private void showUpdateProgress(final int percent) {
        if (null == dialog) {
            dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                    .setViewListener(new CustomDialog.ViewListener() {
                        @Override
                        public void bindView(View view) {
                            progressBar = (ProgressBar) view.findViewById(R.id.progress);
                            progressBar.setMax(percent);
                        }
                    })
                    .setLayoutRes(R.layout.dailog_update)
                    .setDimAmount(0.5f)
                    .isCenter(true)
                    .setCancelOutside(false)
                    .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                    .show();
        } else {
            progressBar.setProgress(percent);
        }
    }

    /**
     * 通知服务器升级完成
     */
    private void notifyUpdateSuccess() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE_SUCCESS)
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("notifyUpdateSuccess failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("notifyUpdateSuccess success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GlobalUtil.getContext(), "固件升级成功", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("notifyUpdateSuccess failure " + e.getMessage());
                }
            }
        });
    }

    private void updateHeader(final OBDVersion obdVersion) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(obdVersion.getUrl());
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");//设置请求方式为POST
                    connection.setDoOutput(true);//允许写出
                    connection.setDoInput(true);//允许读入
                    connection.setUseCaches(false);//不使用缓存
                    connection.connect();//连接
                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        InputStream is = connection.getInputStream();
                        FileOutputStream fos = new FileOutputStream(new File(Environment.getExternalStorageDirectory(), "update.bin"));
                        byte[] buf = new byte[1024];
                        while (-1 != is.read(buf)) {
                            fos.write(buf, 0, buf.length);
                            fos.flush();
                        }
                        FileInputStream fis = new FileInputStream(new File(Environment.getExternalStorageDirectory(), "update.bin"));

                        is.close();
                        updates = new byte[fis.available()];
                        fis.read(updates);
                        fis.close();
                        Log.d(" updates.length " + updates.length);
                        byte[] version = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
                        BlueManager.getInstance().write(ProtocolUtils.updateInfo(version, HexUtils.longToByte(updates.length)));
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void updateForOneUnit(int index) {

        int num = updates.length % UNIT == 0 ? updates.length / UNIT : updates.length / UNIT + 1;


        if (index > num) {
            return;
        }

        showUpdateProgress(index == 1 ? updates.length : (index - 1) * UNIT);

        byte[] date;
        if (index == num) {
            if (updates.length % UNIT == 0) {
                date = new byte[UNIT];
            } else {
                date = new byte[updates.length % UNIT];
            }

        } else {
            date = new byte[UNIT];
        }
        System.arraycopy(updates, 0 + (index - 1) * UNIT, date, 0, date.length);
        BlueManager.getInstance().write(ProtocolUtils.updateForUnit(index, date));
    }


    private void getLisense() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.GET_LISENSE)
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("getLisense failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getLisense success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        String code = result.optString("rightStr");
                        BlueManager.getInstance().write(ProtocolUtils.auth(getDate().getString("sn"), code));
                    } else {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("getLisense failure " + e.getMessage());
                }
            }
        });
    }

    private void verify() {
        final Request request = new Request.Builder().url(URLUtils.GET_TIME).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.d("getOBDStatus fail " + e.getMessage());
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(GlobalUtil.getMainActivity())
                                .setMessage("网络异常,请检查网络状态后重试!")
                                .setTitle("网络异常")
                                .setPositiveButton("重试", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        verify();
                                    }
                                });
                        builder.create().show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getOBDStatus success " + responese);
                if (verified) {
                    return;
                }
                verified = true;
                try {
                    JSONObject result = new JSONObject(responese);
                    BlueManager.getInstance().write(ProtocolUtils.getOBDStatus(Long.valueOf(result.optString("server_time"))));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        if ("gps".equalsIgnoreCase(location.getProvider())) {
            Log.d("onLocationChanged ");
            locationManager.removeUpdates(this);
            if (verified) {
                return;
            }
            verified = true;
            BlueManager.getInstance().write(ProtocolUtils.getOBDStatus(location.getTime()));
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public boolean onBackPressed() {
        return null != progressBar ? true : super.onBackPressed();
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private static final class WorkerHandler extends Handler {

        private Handler mainHandler;

        WorkerHandler(Handler handler, Looper looper) {
            super(looper);
            mainHandler = handler;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            mainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    BlueManager.getInstance().write(ProtocolUtils.getTirePressureStatus());
                    WorkerHandler.this.sendEmptyMessage(0);
                }
            }, 3000);
        }
    }
}
