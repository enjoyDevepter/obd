package com.mapbar.adas;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.Html;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.preferences.SettingPreferencesConfig;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mapbar.adas.view.SensitiveView.Type.Hight;
import static com.mapbar.adas.view.SensitiveView.Type.LOW;
import static com.mapbar.adas.view.SensitiveView.Type.MEDIUM;

@PageSetting(contentViewId = R.layout.main_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class MainPage extends AppBasePage implements View.OnClickListener, BleCallBackListener, LocationListener {
    private static final int UNIT = 1024;
    CustomDialog dialog = null;
    CustomDialog updateDialog = null;
    private volatile boolean verified;
    private volatile boolean isUpdate = false;
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
    @ViewInject(R.id.phone)
    private TextView phoneTV;
    @ViewInject(R.id.car_name)
    private TextView carTV;
    private byte[] updates;
    private OBDVersion obdVersion;
    private ProgressBar progressBar;
    private DownloadManager downloadManager;
    private long mTaskId;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());
    private HandlerThread mWorkerThread;
    private Handler mHandler;
    private int time = 10;
    private Timer timer;
    private TimerTask timerTask;
    private TextView save;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            DownloadManager.Query query = new DownloadManager.Query();
            query.setFilterById(mTaskId);//筛选下载任务，传入任务ID，可变参数
            Cursor c = downloadManager.query(query);
            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                switch (status) {
                    case DownloadManager.STATUS_PAUSED:
                    case DownloadManager.STATUS_PENDING:
                    case DownloadManager.STATUS_RUNNING:
                        break;
                    case DownloadManager.STATUS_SUCCESSFUL:
                        try {
                            File file = new File(Environment.getExternalStoragePublicDirectory("/download/"), "update.bin");
                            FileInputStream fis = new FileInputStream(file);
                            updates = new byte[fis.available()];
                            fis.read(updates);
                            fis.close();
                            Log.d(" updates.length " + updates.length);
                            final byte[] version = new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
                            GlobalUtil.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    BlueManager.getInstance().write(ProtocolUtils.updateInfo(version, HexUtils.longToByte(updates.length)));
                                }
                            });
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case DownloadManager.STATUS_FAILED:
                        break;
                }
            }
        }
    };

    private ProgressDialog progressDialog;


    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText(R.string.app_name);
        warm.setOnClickListener(this);
        reset.setOnClickListener(this);
        sensitive.setOnClickListener(this);
        change.setOnClickListener(this);
        phoneTV.setText("手机号:" + SettingPreferencesConfig.PHONE.get());
        carTV.setText(SettingPreferencesConfig.CAR.get());
        BlueManager.getInstance().addBleCallBackListener(this);

        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);

        if (BlueManager.getInstance().isConnected() && !verified) {
            progressDialog = ProgressDialog.show(getContext(), "", "正在同步盒子信息", false);
            verify();
        }

        if (verified) { // 已鉴权完成
            // 获取OBD版本信息，请求服务器是否有更新
            BlueManager.getInstance().write(ProtocolUtils.getVersion());
        }
        Log.d("onResumeonResumeonResume  ");
    }


    @Override
    public void onStart() {
        Log.d("onStartonStartonStartonStart");
        super.onStart();
        mWorkerThread = new HandlerThread(MainPage.class.getSimpleName());
        mWorkerThread.start();
        mHandler = new WorkerHandler(mMainHandler, mWorkerThread.getLooper());
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);
        mHandler.removeMessages(0);
        mWorkerThread.quitSafely();
        mHandler = null;
        mWorkerThread = null;
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
            case R.id.save:
                BlueManager.getInstance().write(ProtocolUtils.study());
                if (null != dialog) {
                    dialog.dismiss();
                }
                break;
        }
    }

    private void changeCar() {
        ChoiceCarPage choiceCarPage = new ChoiceCarPage();
        Bundle bundle = new Bundle();
        bundle.putString("type", "changeCar");
        bundle.putString("serialNumber", sn);
        choiceCarPage.setDate(bundle);
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
                        final SensitiveView sensitiveView = (SensitiveView) view.findViewById(R.id.sensitive);
                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SensitiveView.Type type = sensitiveView.getType();
                                BlueManager.getInstance().write(ProtocolUtils.setSensitive(type == LOW ? 01 : type == MEDIUM ? 02 : 03));
                                dialog.dismiss();
                            }
                        });
                        sensitiveView.setType(type);
                        sensitiveView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                switch (sensitiveView.getType()) {
                                    case LOW:
                                        type = MEDIUM;
                                        sensitiveView.setType(type);
                                        break;
                                    case MEDIUM:
                                        type = Hight;
                                        sensitiveView.setType(type);
                                        break;
                                    case Hight:
                                        type = LOW;
                                        sensitiveView.setType(type);
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

    private void showStudy() {

        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        ((TextView) view.findViewById(R.id.info)).setText(Html.fromHtml("保存当前胎压后，当某个轮胎胎压<font color='#FF0000'>发生变化</font>时， 盒子会发出报警声音。<br><br> 建议检查四个轮胎胎压均为正常的情况下 再进行保存操作！"));
                        save = (TextView) view.findViewById(R.id.save);
                        time = 10;
                        initTimer();
                        timer.schedule(timerTask, 0, 1000);
                    }
                })
                .setLayoutRes(R.layout.dailog_confirm)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void initTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (time <= 0 && timer != null) {
                            timer.cancel();
                            timer = null;
                            timerTask.cancel();
                            timerTask = null;
                            save.setText("保存胎压");
                            save.setOnClickListener(MainPage.this);
                            save.setBackgroundResource(R.drawable.disclaimer_agree_btn_bg);
                        } else {
                            save.setText("保存胎压(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        };

        timer = new Timer();
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.BLUE_CONNECTED:
                Toast.makeText(GlobalUtil.getContext(), "连接成功", Toast.LENGTH_SHORT).show();
                break;
            case OBDEvent.OBD_DISCONNECTED:
                AlertDialog.Builder builder = new AlertDialog.Builder(GlobalUtil.getMainActivity())
                        .setMessage("OBD链接断开,请检查设备后重试!")
                        .setTitle("OBD链接断开")
                        .setPositiveButton("重新连接", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                BlueManager.getInstance().startScan();
                            }
                        })
                        .setNegativeButton("退出应用", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PageManager.back();
                            }
                        });
                builder.create().show();
                break;
            case OBDEvent.OBD_FIRST_USE:
                Log.d("OBDEvent.OBD_FIRST_USE ");
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
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
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                // 解析OBD状态
                byte[] result1 = (byte[]) data;
                byte[] result = new byte[result1.length - 1];
                System.arraycopy(result1, 1, result, 0, result.length);
                parseStatus(result, true, true);
                getUserInfo();
                break;
            case OBDEvent.OBD_EXPIRE:
                Log.d("OBDEvent.OBD_EXPIRE ");
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
                sn = (String) data;
                getUserInfo();
                new AlertDialog.Builder(GlobalUtil.getMainActivity())
                        .setMessage("OBD盒子已过期,请重新授权!")
                        .setTitle("盒子过期")
                        .setNegativeButton("授权", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取授权码
                                getLisense();
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PageManager.back();
                            }
                        }).create().show();

                break;
            case OBDEvent.OBD_BEGIN_UPDATE:
                if ((Integer) data == 0) { // 是否可以升级
                    try {
                        Thread.sleep(2000);
                        downloadUpdate(obdVersion);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    isUpdate = true;
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
                    File file = new File(Environment.getExternalStoragePublicDirectory("/download/"), "update.bin");
                    if (file.exists()) {
                        file.delete();
                    }
                    // 升级完成，通知服务器
                    isUpdate = false;
                    notifyUpdateSuccess(obdVersion);
                    if (null != progressBar && null != updateDialog) {
                        updateDialog.dismiss();
                        updateDialog = null;
                        progressBar = null;
                    }
                    showStudy();
                }
                break;
            case OBDEvent.OBD_GET_VERSION:
                OBDVersionInfo version = (OBDVersionInfo) data;
                sn = version.getSn();
                // 查询学习进度
                BlueManager.getInstance().write(ProtocolUtils.getStudyProgess());

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
                        downloadUpdate(obdVersion);
                    } else {
                        notifyUpdateSuccess(obdVersion);
                        showStudy();
                    }
                }
                break;
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                // 胎压状态改变，
                parseStatus((byte[]) data, false, false);
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
            case OBDEvent.OBD_STUDY:
                mHandler.sendEmptyMessage(0);
                break;
            case OBDEvent.OBD_STUDY_PROGRESS:
                if ((Integer) data >= 0) {
                    mHandler.sendEmptyMessage(0);
                } else {
                    // 弹出胎压学习对话框
                    showStudy();
                }
                break;
        }
    }

    /**
     * 解析OBD状态码
     *
     * @param status
     */
    private void parseStatus(byte[] status, boolean update, boolean checkVersion) {
        if (null != status && status.length == 77) {

            byte[] snBytes = new byte[19];

            sn = new String(Arrays.copyOfRange(status, 0, snBytes.length));

            byte[] bytes = HexUtils.getBooleanArray(status[snBytes.length]);
            if (bytes[0] == 1) {
                Toast.makeText(getContext(), "车型不支持", Toast.LENGTH_LONG).show();
            } else {
                if (bytes[7] == 1 || bytes[6] == 1 || bytes[5] == 1 || bytes[4] == 1) {
                    if (pressureInfo.getVisibility() == View.INVISIBLE) {
                        pressureInfo.setVisibility(View.VISIBLE);
                        pressureIcon.setBackgroundResource(R.drawable.unusual);
                        // 上传胎压信息
                        byte[] tire = new byte[status.length - snBytes.length];
                        System.arraycopy(status, snBytes.length, tire, 0, tire.length);
                        updateTireInfo(tire);

                        if (update) { // 避免首次检测OBD时胎压异常，导致重复上传。
                            update = false;
                        }
                    }
                } else {
                    if (pressureInfo.getVisibility() == View.VISIBLE) {
                        pressureInfo.setVisibility(View.INVISIBLE);
                        pressureIcon.setBackgroundResource(R.drawable.normal);
                    }
                }

                if (update) {
                    // 上传胎压信息
                    byte[] tire = new byte[status.length - snBytes.length];
                    System.arraycopy(status, snBytes.length, tire, 0, tire.length);
                    updateTireInfo(tire);
                }
            }

            switch (status[snBytes.length + 1]) {
                case 1:
                    type = LOW;
                    break;
                case 2:
                    type = SensitiveView.Type.MEDIUM;
                    break;
                case 3:
                    type = Hight;
                    break;
            }
            if (bytes[1] == 1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GlobalUtil.getMainActivity())
                        .setMessage("应用升级未完成，请确保网络后重启应用!")
                        .setTitle("升级中断")
                        .setNegativeButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 获取OBD版本信息，请求服务器是否有更新
                                BlueManager.getInstance().write(ProtocolUtils.getVersion());
                            }
                        })
                        .setCancelable(false)
                        .setPositiveButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                PageManager.back();
                            }
                        });
                builder.create().show();
            } else {
                if (checkVersion) {
                    // 获取OBD版本信息，请求服务器是否有更新
                    BlueManager.getInstance().write(ProtocolUtils.getVersion());
                }
            }

        } else {
            Log.d(" status error " + status.length);
        }

    }

    /**
     * 上传胎压信息
     *
     * @param tire
     */
    private void updateTireInfo(byte[] tire) {

        if (null != tire && tire.length == 58) {
            JSONObject jsonObject = new JSONObject();
            try {
                byte[] content = new byte[8];
                jsonObject.put("serialNumber", sn);
                jsonObject.put("s_status", HexUtils.formatHexString(new byte[]{tire[0]}));
                jsonObject.put("s_level", HexUtils.formatHexString(new byte[]{tire[1]}));
                System.arraycopy(tire, 2, content, 0, content.length);
                jsonObject.put("study1", new String(content));
                System.arraycopy(tire, 10, content, 0, content.length);
                jsonObject.put("study2", new String(content));
                System.arraycopy(tire, 18, content, 0, content.length);
                jsonObject.put("study3", new String(content));
                System.arraycopy(tire, 26, content, 0, content.length);
                jsonObject.put("alarm1", new String(content));
                System.arraycopy(tire, 34, content, 0, content.length);
                jsonObject.put("alarm2", new String(content));
                System.arraycopy(tire, 42, content, 0, content.length);
                jsonObject.put("alarm3", new String(content));
                System.arraycopy(tire, 50, content, 0, content.length);
                jsonObject.put("alarm4", new String(content));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.d("update_tire input " + jsonObject.toString());

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
                    Log.d("update_tire failure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("update_tire success " + responese);
                }
            });
        }

    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("getUserInfo input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.GET_USER_INFO)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("getUserInfo failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("getUserInfo success " + responese);
                final UserInfo userInfo = JSON.parseObject(responese, UserInfo.class);
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        SettingPreferencesConfig.PHONE.set(userInfo.getPhone());
                        SettingPreferencesConfig.CAR.set(userInfo.getModelName() + " " + userInfo.getStyleName());
                        phoneTV.setText("手机号:" + SettingPreferencesConfig.PHONE.get());
                        carTV.setText(SettingPreferencesConfig.CAR.get());
                    }
                });
            }
        });
    }

    /**
     * 激活成功
     */
    private void activate_success() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("activate success input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.ACTIVATE_SUCCESS)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
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
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if ("000".equals(result.optString("status"))) {
                                BlueManager.getInstance().write(ProtocolUtils.getVersion());
                            } else {
                                Toast.makeText(getContext(), result.optString("message"), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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

        Log.d("checkOBDVersion input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
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
                GlobalUtil.getHandler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if ("000".equals(obdVersion.getStatus())) {
                            switch (obdVersion.getUpdateState()) {
                                case 0:
                                    // 定时获取胎压状态
//                            mHandler.sendEmptyMessage(0);
//                            BlueManager.getInstance().write(ProtocolUtils.getStudyProgess());
                                    break;
                                case 1: // 版本参数都更新
                                    BlueManager.getInstance().write(ProtocolUtils.updateParams(sn, obdVersion.getParams()));
                                    break;
                                case 2: // 只有版本更新
                                    downloadUpdate(obdVersion);
                                    break;
                                case 3: // 只有参数更新
                                    BlueManager.getInstance().write(ProtocolUtils.updateParams(sn, obdVersion.getParams()));
                                    break;
                            }
                        } else {

                            Toast.makeText(getContext(), obdVersion.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, 1500);
            }
        });
    }

    private void showUpdateProgress(final int percent) {
        if (null == updateDialog) {
            updateDialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
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
     * 通知服务器固件升级完成
     */
    private void notifyUpdateSuccess(OBDVersion obdVersion) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
            jsonObject.put("bVersion", obdVersion.getbVersion());
            jsonObject.put("pVersion", obdVersion.getpVersion());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("notifyUpdateSuccess input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE_SUCCESS)
                .addHeader("content-type", "application/json;charset:utf-8")
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
                                Toast.makeText(GlobalUtil.getContext(), "升级成功", Toast.LENGTH_LONG).show();
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

    private void downloadUpdate(OBDVersion obdVersion) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(obdVersion.getUrl()));
        request.setAllowedOverRoaming(false);//漫游网络是否可以下载

        //设置文件类型，可以在下载结束后自动打开该文件
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(obdVersion.getUrl()));
        request.setMimeType(mimeString);

        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setVisibleInDownloadsUi(true);

        //sdcard的目录下的download文件夹，必须设置
        File file = new File(Environment.getExternalStoragePublicDirectory("/download/"), "update.bin");
        if (file.exists()) {
            file.delete();
        }
        request.setDestinationInExternalPublicDir("/download/", "update.bin");

        //将下载请求加入下载队列
        downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
        //加入下载队列后会给该任务返回一个long型的id，
        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
        mTaskId = downloadManager.enqueue(request);

        //注册广播接收者，监听下载状态
        getContext().registerReceiver(receiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

        Log.d("getLisense input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.GET_LISENSE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
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
                        BlueManager.getInstance().write(ProtocolUtils.auth(sn, code));
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
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private final class WorkerHandler extends Handler {

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
                    if (!isUpdate) {
                        BlueManager.getInstance().write(ProtocolUtils.getTirePressureStatus());
                        WorkerHandler.this.sendEmptyMessage(0);
                    }
                }
            }, 30000);
        }
    }
}
