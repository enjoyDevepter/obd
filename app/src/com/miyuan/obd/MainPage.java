package com.miyuan.obd;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.BasePage;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.OBDStatusInfo;
import com.miyuan.hamster.PressureInfo;
import com.miyuan.hamster.core.HexUtils;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.hamster.log.FileLoggingTree;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.preferences.SettingPreferencesConfig;
import com.miyuan.obd.utils.AlarmManager;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;
import com.miyuan.obd.utils.URLUtils;
import com.miyuan.obd.view.NumberSeekBar;

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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.main_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class MainPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {
    private static final int UNIT = 1024;
    CustomDialog dialog = null;
    CustomDialog updateDialog = null;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.warm)
    private View warm;
    @ViewInject(R.id.reset)
    private View reset;
    @ViewInject(R.id.misinformation)
    private View misinformationV;
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
    private NumberSeekBar sensitiveView;
    private boolean misinformation;
    private boolean under;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("汽车卫士");
        back.setOnClickListener(this);
        warm.setOnClickListener(this);
        reset.setOnClickListener(this);
        reportV.setOnClickListener(this);
//        sensitive.setOnClickListener(this);
        misinformationV.setOnClickListener(this);
//        phoneTV.setText("手机号:" + SettingPreferencesConfig.PHONE.get());
        carTV.setText(SettingPreferencesConfig.CAR.get());
        BlueManager.getInstance().addBleCallBackListener(this);
        obdStatusInfo = (OBDStatusInfo) getDate().getSerializable("obdStatusInfo");
        getUserInfo();
        checkOBDVersion(obdStatusInfo);
        heartTimer = new Timer();
        heartTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BlueManager.getInstance().send(ProtocolUtils.sentHeart());
            }
        }, 1000 * 30, 1000 * 60);
        ImmersionBar.with(MainActivity.getInstance())
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(MainActivity.getInstance().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? android.R.color.black : R.color.main_title_color)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().send(ProtocolUtils.stopGetNewTirePressureStatus());
        BlueManager.getInstance().removeCallBackListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

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
            case R.id.back:
                PageManager.back();
                break;
            case R.id.warm:
                showWarm();
                break;
            case R.id.sensitive:
                showSensitive();
                break;
            case R.id.reset:
                showReset();
                break;
            case R.id.report:
                uploadLog();
                break;
            case R.id.misinformation:
                showMisinformation();
                break;
        }
    }

    private void showMisinformation() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.change).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                under = false;
                                misinformation = true;
                                showMisinformationStepOne();
                            }
                        });

                        view.findViewById(R.id.auth).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                under = true;
                                misinformation = false;
                                showUnder();
                            }
                        });
                        view.findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                under = false;
                                misinformation = false;
                                dialog.dismiss();
                                showResetOne();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_misinformation)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void showMisinformationStepOne() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView infoTV = view.findViewById(R.id.info);
                        infoTV.setText(Html.fromHtml("<font color='#4A4A4A'>导致误报的常见原因:<br><br>1、车辆跑偏：<br></font><font color='#009488'>解决办法：</font><font color='#4A4A4A'>优先做四轮定位；也可以点击\"下一步\"来修正误报。<br>2、校准路况不好，有明显颠簸或未直线行驶。<br></font><font color='#009488'>解决办法：</font><font color='#4A4A4A'>重新校准。<br>3、当前路况不好：例如路面打滑，或者单边长时间偏高，单边长期压隔离带行驶等。<br></font><font color='#009488'>解决办法：</font><font color='#4A4A4A'>重新插拔盒子，消除蜂鸣声，保持原有灵敏度。<br><br>上述\"2\"和\"3\"的情况，则点击\"取消\"，上述情况\"1\"或者其他未知原因请点击\"下一步\"</font>"));
                        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                showMisinformationStepTwo();
                            }
                        });
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_misinformation_step_one)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void showMisinformationStepTwo() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView infoTV = view.findViewById(R.id.info);

                        infoTV.setText(Html.fromHtml("<font color='#4A4A4A'>在您修正误报之前，请您确认：<br><br>1、误报后未重新校准或插拔盒子。<br>2、当前胎压正常(胎压变化在10%以内)。<br>由于我司胎压监测非常灵敏，所以有时候肉眼观察不到的亏气，也会报警。<br><br>确认以上情况后，请继续修正，否则取消。</font>"));

                        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.misinformation());
                                dialog.dismiss();
                            }
                        });
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_misinformation_step_two)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void showUnder() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.disclose());
                                dialog.dismiss();
                            }
                        });
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_under_speak)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void showResetOne() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                BlueManager.getInstance().send(ProtocolUtils.resetSens());
                            }
                        });
                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_reset)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void showConFirm(final String title, final String conent) {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView titleTV = view.findViewById(R.id.title);
                        titleTV.setText(title);
                        TextView infoTV = view.findViewById(R.id.info);
                        infoTV.setText(conent);

                        view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_common_confirm)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void uploadLog() {
        Log.d("MainPage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                    .addPart(MultipartBody.Part.createFormData("type", "1"));
            for (File file : logs) {
                if (!file.getName().equals(FileLoggingTree.fileName)) {
                    builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
                }
            }
            Request request = new Request.Builder()
                    .url(URLUtils.UPDATE_ERROR_FILE)
                    .post(builder.build())
                    .build();

            GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("MainPage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("MainPage uploadLog success " + responese);
                    try {
                        final JSONObject result = new JSONObject(responese);
                        if ("000".equals(result.optString("status"))) {
                            GlobalUtil.getHandler().post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getContext(), "上报成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            for (File delete : logs) {
                                if (!delete.getName().equals(FileLoggingTree.fileName)) {
                                    delete.delete();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("MainPage uploadLog failure " + e.getMessage());
                    }
                }
            });
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
                        sensitiveView = view.findViewById(R.id.sensitive);
                        Log.d("obdStatusInfo.getSensitive() " + obdStatusInfo.getSensitive());
                        sensitiveView.setCurProgress(obdStatusInfo.getSensitive());
                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                BlueManager.getInstance().send(ProtocolUtils.setSensitive(sensitiveView.getCurProgress()));
                                dialog.dismiss();
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
        if (SettingPreferencesConfig.ADJUST_SUCCESS.get()) {
            SettingPreferencesConfig.ADJUST_SUCCESS.set(false);
        }
        return super.onBackPressed();
    }

    private void showReset() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        view.findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                SettingPreferencesConfig.ADJUST_START.set(true);
                                SettingPreferencesConfig.ADJUST_SUCCESS.set(true);
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
                parseStatus((PressureInfo) data);
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
//                mHandler.sendEmptyMessage(0);
                break;
            case OBDEvent.OBD_STUDY_PROGRESS:
//                if ((Integer) data >= 0) {
////                    mHandler.sendEmptyMessage(0);
//                } else {
//                    // 弹出胎压学习对话框
//                    showStudy();
//                }
                break;
            case OBDEvent.PARAM_UPDATE_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                if (needNotifyParamsSuccess) {
                    notifyUpdateSuccess((OBDStatusInfo) data);
                }
                break;
            case OBDEvent.PARAM_UPDATE_FAIL:
                break;
            case OBDEvent.ADJUSTING:
                obdStatusInfo = (OBDStatusInfo) data;
                if (SettingPreferencesConfig.ADJUST_START.get()) {
                    SettingPreferencesConfig.ADJUST_START.set(false);
                    AlarmManager.getInstance().play(R.raw.main_start_adjust);
                    BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
                }
                break;
            case OBDEvent.ADJUST_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                if (SettingPreferencesConfig.ADJUST_SUCCESS.get()) {
                    SettingPreferencesConfig.ADJUST_SUCCESS.set(false);
                    AlarmManager.getInstance().play(R.raw.main_start_finished);
                }
                break;
            case OBDEvent.NORMAL:
                obdStatusInfo = (OBDStatusInfo) data;
                if (null != sensitiveView) {
                    sensitiveView.setCurProgress(obdStatusInfo.getSensitive());
                }
                break;
            case OBDEvent.SENSITIVE_CHANGE:
                updateBlmdInfo((byte[]) data);
                if (misinformation) {
                    showConFirm("修正成功", Html.fromHtml("<font color='#4A4A4A'>四轮定位之后，请您按以下步骤重新设置：<br>1、恢复出厂灵敏度。<br>2、重新校准。<br><br>如果车辆仍然误报，则需要继续修正误报！一般2-3次修复，可以彻底解决误报！</font>").toString());
                } else {
                    if (under) {
                        showConFirm("修正成功", Html.fromHtml("<font color='#4A4A4A'>当车辆亏气时，盒子可以正常报警了！</font>").toString());
                    } else {
                        showConFirm("恢复成功", Html.fromHtml("<font color='#4A4A4A'>恭喜您，恢复成功！</font>").toString());
                    }
                }
                break;
        }
    }

    /**
     * 上传状态信息
     */
    private void updateBlmdInfo(byte[] status) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("blmd", HexUtils.formatHexString(status));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("updateBlmdInfo input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_SEN_STATUS)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("updateBlmdInfo failure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("updateBlmdInfo success " + responese);
            }
        });

    }

    /**
     * 解析OBD状态码
     *
     * @param status
     */
    private void parseStatus(PressureInfo status) {

        if (status.getStatus() != 0) {
            if (highAnimationDrawable != null && highAnimationDrawable.isRunning()) {
                return;
            }
            switch (status.getStatus()) {
                case 1:
                    leftTop.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) leftTop.getBackground();
                    rightButtom.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) rightButtom.getBackground();
                    break;
                case 2:
                    rightTop.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) rightTop.getBackground();
                    leftButtom.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) leftButtom.getBackground();
                    break;
                case 3:
                    leftButtom.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) leftButtom.getBackground();
                    rightTop.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) rightTop.getBackground();
                    break;
                case 4:
                    rightButtom.setBackgroundResource(R.drawable.high_bg);
                    highAnimationDrawable = (AnimationDrawable) rightButtom.getBackground();
                    leftTop.setBackgroundResource(R.drawable.low_bg);
                    lowAnimationDrawable = (AnimationDrawable) leftTop.getBackground();
                    break;
            }
            if (highAnimationDrawable != null && !highAnimationDrawable.isRunning()) {
                highAnimationDrawable.start();
            }

            if (lowAnimationDrawable != null && !lowAnimationDrawable.isRunning()) {
                lowAnimationDrawable.start();
            }

            if (SettingPreferencesConfig.TIRE_WARM.get()) {
                AlarmManager.getInstance().play(R.raw.warm);
                SettingPreferencesConfig.TIRE_WARM.set(false);
            }

            if (status.isUpdate()) {
                // 上传胎压信息
                updateTireInfo(status.getOrigin());

            }
        } else {
            SettingPreferencesConfig.TIRE_WARM.set(true);
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
            jsonObject.put("pState", HexUtils.formatHexString(tire));
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
//                        SettingPreferencesConfig.PHONE.set(userInfo.getPhone());
                        SettingPreferencesConfig.CAR.set(userInfo.getModelName() + " " + userInfo.getStyleName());
//                        phoneTV.setText("手机号:" + SettingPreferencesConfig.PHONE.get());
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
                            switch (obdVersion.getpUpdateState()) {
                                case 0:
                                    BlueManager.getInstance().send(ProtocolUtils.getNewTirePressureStatus());
                                    break;
                                case 1: // 版本参数都更新
                                    needNotifyParamsSuccess = true;
                                    SettingPreferencesConfig.ADJUST_START.set(true);
                                    SettingPreferencesConfig.ADJUST_SUCCESS.set(true);
                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
                                    break;
//                                case 2: // 只有版本更新
//                                    downloadUpdate(obdVersion);
//                                    break;
//                                case 3: // 只有参数更新
//                                    needNotifyParamsSuccess = true;
//                                    ADJUST_START.set(true);
//                                    ADJUST_SUCCESS.set(true);
//                                    BlueManager.getInstance().send(ProtocolUtils.updateParams(obdStatusInfo.getSn(), obdVersion.getParams()));
//                                    break;
                            }
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
//        //创建下载任务
//        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(obdVersion.getUrl()));
//        request.setAllowedOverRoaming(false);//漫游网络是否可以下载
//
//        //设置文件类型，可以在下载结束后自动打开该文件
//        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
//        String mimeString = mimeTypeMap.getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(obdVersion.getUrl()));
//        request.setMimeType(mimeString);
//
//        //在通知栏中显示，默认就是显示的
//        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
//        request.setVisibleInDownloadsUi(true);
//
//        //sdcard的目录下的download文件夹，必须设置
//        File file = new File(Environment.getExternalStoragePublicDirectory("/download/"), "update.bin");
//        if (file.exists()) {
//            file.delete();
//        }
//        request.setDestinationInExternalPublicDir("/download/", "update.bin");
//
//        //将下载请求加入下载队列
//        downloadManager = (DownloadManager) getContext().getSystemService(Context.DOWNLOAD_SERVICE);
//        //加入下载队列后会给该任务返回一个long型的id，
//        //通过该id可以取消任务，重启任务等等，看上面源码中框起来的方法
//        mTaskId = downloadManager.enqueue(request);
//
//        //注册广播接收者，监听下载状态
//        getContext().registerReceiver(receiver,
//                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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
}
