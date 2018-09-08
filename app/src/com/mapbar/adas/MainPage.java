package com.mapbar.adas;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.preferences.SettingPreferencesConfig;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.adas.view.SensitiveView;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.ADJUST_START;
import static com.mapbar.adas.preferences.SettingPreferencesConfig.ADJUST_SUCCESS;
import static com.mapbar.adas.preferences.SettingPreferencesConfig.TIRE_WARM;
import static com.mapbar.adas.view.SensitiveView.Type.LOW;
import static com.mapbar.adas.view.SensitiveView.Type.MEDIUM;

@PageSetting(contentViewId = R.layout.main_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class MainPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    private static final int UNIT = 1024;
    CustomDialog dialog = null;
    CustomDialog updateDialog = null;
    private SensitiveView.Type type = SensitiveView.Type.MEDIUM;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.sensitive)
    private View sensitive;
    @ViewInject(R.id.warm)
    private View warm;
    @ViewInject(R.id.reset)
    private View reset;
    @ViewInject(R.id.phone)
    private TextView phoneTV;
    @ViewInject(R.id.car_name)
    private TextView carTV;
    @ViewInject(R.id.left_top)
    private View leftTop;
    @ViewInject(R.id.left_buttom)
    private View leftButtom;
    @ViewInject(R.id.right_top)
    private View rightTop;
    @ViewInject(R.id.right_buttom)
    private View rightButtom;
    private AnimationDrawable highAnimationDrawable;
    private AnimationDrawable lowAnimationDrawable;

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
    private OBDStatusInfo obdStatusInfo;

    private volatile boolean needNotifyParamsSuccess;

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
                                    BlueManager.getInstance().send(ProtocolUtils.updateInfo(version, HexUtils.longToByte(updates.length)));
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

    private Timer heartTimer;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("汽车卫士");
        warm.setOnClickListener(this);
        reset.setOnClickListener(this);
        reportV.setOnClickListener(this);
        sensitive.setOnClickListener(this);
        phoneTV.setText("手机号:" + SettingPreferencesConfig.PHONE.get());
        carTV.setText(SettingPreferencesConfig.CAR.get());
        BlueManager.getInstance().addBleCallBackListener(this);
        obdStatusInfo = (OBDStatusInfo) getDate().getSerializable("obdStatusInfo");
        getUserInfo();
//        if (getDate() != null) {
//            if (getDate().getBoolean("showStudy")) {
//                showStudy();
//                return;
//            }
//        }

        checkOBDVersion(obdStatusInfo);

        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000 * 30, 1000 * 60);
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
        BlueManager.getInstance().send(ProtocolUtils.stopGetTirePressureStatusUpdateSucess());
        BlueManager.getInstance().removeCallBackListener(this);
        mHandler.removeMessages(0);
        mWorkerThread.quitSafely();
        mHandler = null;
        mWorkerThread = null;

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (null != heartTimer) {
            heartTimer.cancel();
            heartTimer = null;
        }
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
            case R.id.save:
                if (null != dialog) {
                    dialog.dismiss();
                }
                BlueManager.getInstance().send(ProtocolUtils.study());
                break;
            case R.id.report:
                break;
        }
    }

    private void showWarm() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.playWarm(02));
                            }
                        });

                        view.findViewById(R.id.auth).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.playWarm(01));
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
                                BlueManager.getInstance().send(ProtocolUtils.setSensitive(type == LOW ? 01 : type == MEDIUM ? 02 : 03));
                                dialog.dismiss();
                            }
                        });
                        sensitiveView.setType(type);
                        sensitiveView.setOnItemChoiceListener(new SensitiveView.OnItemChoiceListener() {
                            @Override
                            public void onChoice(SensitiveView.Type t) {
                                type = t;
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

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    private void showReset() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.study());
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
        if (dialog != null && dialog.isVisible()) {
            time = 10;
            initTimer();
            timer.schedule(timerTask, 0, 1000);
            return;
        }

        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        save = (TextView) view.findViewById(R.id.save);
                        time = 10;
                        initTimer();
                        timer.schedule(timerTask, 0, 1000);
                    }
                })
                .setLayoutRes(R.layout.dailog_confirm)
                .setDimAmount(0.5f)
                .setCancelOutside(false)
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
                            save.setText("校准");
                            save.setOnClickListener(MainPage.this);
                            save.setBackgroundResource(R.drawable.btn_bg);
                        } else {
                            save.setText("校准(" + time + "s)");
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
            case OBDEvent.OBD_BEGIN_UPDATE:
                if ((Integer) data == 0) { // 是否可以升级
                    try {
                        Thread.sleep(2000);
                        downloadUpdate(obdVersion);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 固件升级开始
                    updateForOneUnit(1);
                }
                break;
            case OBDEvent.OBD_UPDATE_FINISH_UNIT:
//                Update update = (Update) data;
//                if (update.getStatus() == 0) {
//                    // 重新传递
//                    updateForOneUnit(update.getIndex());
//                } else if (update.getStatus() == 1) {
//                    // 继续
//                    updateForOneUnit(update.getIndex() + 1);
//                } else if (update.getStatus() == 2) {
//                    File file = new File(Environment.getExternalStoragePublicDirectory("/download/"), "update.bin");
//                    if (file.exists()) {
//                        file.delete();
//                    }
//                    // 升级完成，通知服务器
//                    notifyUpdateSuccess(obdVersion);
//                    if (null != progressBar && null != updateDialog) {
//                        updateDialog.dismiss();
//                        updateDialog = null;
//                        progressBar = null;
//                    }
//                    showStudy();
//                }
                break;
            case OBDEvent.OBD_GET_VERSION:
//                OBDVersionInfo version = (OBDVersionInfo) data;
//                sn = version.getSn();
//                checkOBDVersion(version);
                break;
            case OBDEvent.OBD_UPDATE_PARAMS_SUCCESS:
//                // 判断是否需要升级固件
//                if (null != obdVersion) {
//                    if (obdVersion.getUpdateState() == 1) {
//                        downloadUpdate(obdVersion);
//                    } else {
//                        notifyUpdateSuccess(obdVersion);
//                        showStudy();
//                    }
//                }
                break;
            case OBDEvent.OBD_UPPATE_TIRE_PRESSURE_STATUS:
                // 胎压状态改变，
                parseStatus((byte[]) data);
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
                AlarmManager.getInstance().play(R.raw.begin);
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
            case OBDEvent.PARAM_UPDATE_SUCCESS:
                if (needNotifyParamsSuccess) {
                    notifyUpdateSuccess((OBDStatusInfo) data);
                    showStudy();
                }
                break;
            case OBDEvent.PARAM_UPDATE_FAIL:
                break;
            case OBDEvent.ADJUSTING:
                if (ADJUST_START.get()) {
                    ADJUST_START.set(false);
                    AlarmManager.getInstance().play(R.raw.begin);
                    BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
                }
                break;
            case OBDEvent.ADJUST_SUCCESS:
                if (ADJUST_SUCCESS.get()) {
                    ADJUST_SUCCESS.set(false);
                    AlarmManager.getInstance().play(R.raw.finish);
                }
                break;
        }
    }

    /**
     * 解析OBD状态码
     *
     * @param status
     */
    private void parseStatus(byte[] status) {
        if (null != status && status.length > 2) {

            byte[] bytes = HexUtils.getBooleanArray(status[4]);
            if (bytes[7] == 1 || bytes[6] == 1 || bytes[5] == 1 || bytes[4] == 1) {
                if (highAnimationDrawable != null && highAnimationDrawable.isRunning()) {
                    return;
                }
                if (bytes[7] == 1) {
                    leftTop.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) leftTop.getBackground();
                    rightButtom.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) rightButtom.getBackground();
                } else if (bytes[6] == 1) {
                    rightTop.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) rightTop.getBackground();
                    leftButtom.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) leftButtom.getBackground();
                } else if (bytes[5] == 1) {
                    leftButtom.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) leftButtom.getBackground();
                    rightTop.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) rightTop.getBackground();
                } else if (bytes[4] == 1) {
                    rightButtom.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) rightButtom.getBackground();
                    leftTop.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) leftTop.getBackground();
                }
                if (highAnimationDrawable != null && !highAnimationDrawable.isRunning()) {
                    highAnimationDrawable.start();
                }

                if (lowAnimationDrawable != null && !lowAnimationDrawable.isRunning()) {
                    lowAnimationDrawable.start();
                }

                if (TIRE_WARM.get()) {
                    AlarmManager.getInstance().play(R.raw.warm);
                    TIRE_WARM.set(false);
                }

                if (status[17] == 1) { // 上传标示
                    // 上传胎压信息
                    updateTireInfo(status);
                }

            } else {
                TIRE_WARM.set(true);
                if (highAnimationDrawable != null && highAnimationDrawable.isRunning()) {
                    highAnimationDrawable.stop();
                    highAnimationDrawable = null;
                }
                if (lowAnimationDrawable != null && lowAnimationDrawable.isRunning()) {
                    lowAnimationDrawable.stop();
                    lowAnimationDrawable = null;
                }
                leftTop.setBackgroundResource(R.drawable.t_nromal);
                leftButtom.setBackgroundResource(R.drawable.t_nromal);
                rightTop.setBackgroundResource(R.drawable.t_nromal);
                rightButtom.setBackgroundResource(R.drawable.t_nromal);
            }

//            }
//            switch (status[snBytes.length + 1]) {
//                case 1:
//                    type = LOW;
//                    break;
//                case 2:
//                    type = SensitiveView.Type.MEDIUM;
//                    break;
//                case 3:
//                    type = HIGHT;
//                    break;
//            }
//            // 验证是否已经学习完成,并播报语音
//            String studyStutus = HexUtils.formatHexString(Arrays.copyOfRange(status, snBytes.length + 2, snBytes.length + 2 + 24));
//            if (GlobalUtil.isEmpty(STUDYSTATUS.get())) {
//                STUDYSTATUS.set(studyStutus);
//            } else {
//                if (!studyStutus.equals(STUDYSTATUS.get())) {
//                    STUDYSTATUS.set(studyStutus);
//                    AlarmManager.getInstance().play(R.raw.finish);
//                }
//            }
//
//            if (bytes[1] == 1) {
//                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
//                        .setViewListener(new CustomDialog.ViewListener() {
//                            @Override
//                            public void bindView(View view) {
//                                ((TextView) (view.findViewById(R.id.confirm))).setText("确定");
//                                ((TextView) (view.findViewById(R.id.info))).setText("应用升级未完成，请确认网络链接正常!");
//                                ((TextView) (view.findViewById(R.id.title))).setText("升级中断");
//                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
//                                    @Override
//                                    public void onClick(View v) {
//                                        dialog.dismiss();
//                                        // 获取OBD版本信息，请求服务器是否有更新
//                                        BlueManager.getInstance().send(ProtocolUtils.getVersion());
//                                    }
//                                });
//                            }
//                        })
//                        .setLayoutRes(R.layout.dailog_common_warm)
//                        .setCancelOutside(false)
//                        .setDimAmount(0.5f)
//                        .isCenter(true)
//                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
//                        .show();
//            } else {
//                if (checkVersion) {
//                    // 获取OBD版本信息，请求服务器是否有更新
//                    BlueManager.getInstance().send(ProtocolUtils.getVersion());
//                }
//            }

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

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("status", HexUtils.formatHexString(tire));
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
                BlueManager.getInstance().send(ProtocolUtils.tirePressureStatusUpdateSucess());
                Log.d("update_tire success " + responese);
            }
        });

    }

    /**
     * 获取用户信息
     */
    private void getUserInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
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

    private void checkOBDVersion(final OBDStatusInfo obdStatusInfo) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("bVersion", obdStatusInfo.getbVersion());
            jsonObject.put("pVersion", obdStatusInfo.getpVersion());
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
                                    BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
                                    // 定时获取胎压状态
//                                    mHandler.sendEmptyMessage(0);
//                            BlueManager.getInstance().write(ProtocolUtils.getStudyProgess());
                                    break;
                                case 1: // 版本参数都更新
                                    needNotifyParamsSuccess = true;
                                    ADJUST_START.set(true);
                                    ADJUST_SUCCESS.set(true);
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
                                    break;
                                case 2: // 只有版本更新
                                    downloadUpdate(obdVersion);
                                    break;
                                case 3: // 只有参数更新
                                    needNotifyParamsSuccess = true;
                                    ADJUST_START.set(true);
                                    ADJUST_SUCCESS.set(true);
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
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
    private void notifyUpdateSuccess(OBDStatusInfo obdStatusInfo) {

        if (!needNotifyParamsSuccess) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("bVersion", obdStatusInfo.getbVersion());
            jsonObject.put("pVersion", obdStatusInfo.getpVersion());
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
                        needNotifyParamsSuccess = false;
//                        GlobalUtil.getHandler().post(new Runnable() {
//                            @Override
//                            public void run() {
//                                Toast.makeText(GlobalUtil.getContext(), "升级成功", Toast.LENGTH_LONG).show();
//                            }
//                        });
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

        BlueManager.getInstance().send(ProtocolUtils.updateForUnit(index, date));
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
                    BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
                    WorkerHandler.this.sendEmptyMessage(0);
                }
            }, 3000);
        }
    }
}
