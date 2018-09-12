package com.mapbar.adas;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.protocol_check_fail_layout, toHistory = false)
public class ProtocolCheckFailPage extends AppBasePage implements BleCallBackListener, View.OnClickListener {

    @ViewInject(R.id.title_text)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.reason_one)
    private View beforeMatchingV;
    @ViewInject(R.id.reason_two)
    private View currentMatchingV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private CustomDialog dialog;

    private volatile boolean showConfirm;
    private volatile int times = 1;
    private int time = 15;
    private Timer timer;
    private TimerTask timerTask;
    private volatile OBDStatusInfo obdStatusInfo;
    private boolean beforeMatching;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setOnClickListener(this);
        confirmV.setSelected(false);
        title.setText("未检测到车辆数据");
        beforeMatching = getDate().getBoolean("before_matching");
        if (beforeMatching) {
            beforeMatchingV.setVisibility(View.VISIBLE);
            currentMatchingV.setVisibility(View.INVISIBLE);
            confirmV.setVisibility(View.INVISIBLE);
        } else {
            beforeMatchingV.setVisibility(View.INVISIBLE);
            currentMatchingV.setVisibility(View.VISIBLE);
            confirmV.setVisibility(View.VISIBLE);
        }
        BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
    }

    @Override
    public void onStart() {
        super.onStart();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.NO_PARAM: // 删除参数逻辑
                CollectGuide collectGuide = new CollectGuide();
                Bundle collectBundle = new Bundle();
                collectBundle.putBoolean("matching", false);
                collectGuide.setDate(collectBundle);
                PageManager.go(collectGuide);
                break;
            case OBDEvent.CURRENT_MISMATCHING:
                obdStatusInfo = (OBDStatusInfo) data;
                if (!showConfirm) {
                    showConfirm = true;
                    time = 15;
                    initTimer();
                    timer.schedule(timerTask, 3000, 1000);
                }
                BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
                break;
            case OBDEvent.BEFORE_MATCHING:
                BlueManager.getInstance().send(ProtocolUtils.checkMatchingStatus());
                break;
            case OBDEvent.UN_ADJUST:
                obdStatusInfo = (OBDStatusInfo) data;
                CollectGuide guide = new CollectGuide();
                Bundle bundle = new Bundle();
                bundle.putBoolean("matching", true);
                guide.setDate(bundle);
                PageManager.go(guide);
                break;
            case OBDEvent.NORMAL:
                obdStatusInfo = (OBDStatusInfo) data;
                MainPage mainPage = new MainPage();
                Bundle mainBundle = new Bundle();
                mainBundle.putSerializable("obdStatusInfo", (OBDStatusInfo) data);
                mainPage.setDate(mainBundle);
                PageManager.go(mainPage);
                break;
        }
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
                            confirmV.setText("确认，我已打火!");
                            confirmV.setSelected(true);
                            confirmV.setOnClickListener(ProtocolCheckFailPage.this);
                        } else {
                            confirmV.setText("确认，我已打火!(" + time + "s)");
                        }
                        time--;
                    }
                });
            }
        };

        timer = new Timer();
    }

    /**
     * 授权失败
     *
     * @param reason
     */
    private void authFail(final String reason) {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) (view.findViewById(R.id.confirm))).setText("确认");
                                ((TextView) (view.findViewById(R.id.info))).setText(reason);
                                ((TextView) (view.findViewById(R.id.title))).setText("授权失败");
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        dialog.dismiss();
                                        // 退出应用
                                        PageManager.finishActivity(MainActivity.getInstance());
                                    }
                                });
                            }
                        })
                        .setLayoutRes(R.layout.dailog_common_warm)
                        .setCancelOutside(false)
                        .setDimAmount(0.5f)
                        .isCenter(true)
                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                        .show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:
                break;
            case R.id.confirm:
                if (times >= 2) {
                    cleanParams(obdStatusInfo);
                } else {
                    times++;
                    confirmV.setOnClickListener(null);
                    confirmV.setSelected(false);
                    time = 15;
                    initTimer();
                    timer.schedule(timerTask, 3000, 1000);
                }
                break;
        }
    }

    /**
     * 清除参数
     *
     * @param obdStatusInfo
     */
    private void cleanParams(final OBDStatusInfo obdStatusInfo) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("cleanParams input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        showProgress();
        Request request = new Request.Builder()
                .url(URLUtils.GET_LISENSE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgress();
                        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                                .setViewListener(new CustomDialog.ViewListener() {
                                    @Override
                                    public void bindView(View view) {
                                        ((TextView) (view.findViewById(R.id.confirm))).setText("已打开网络，重试");
                                        ((TextView) (view.findViewById(R.id.info))).setText("请打开网络，否则无法完成当前操作!");
                                        ((TextView) (view.findViewById(R.id.title))).setText("网络异常");
                                        final View confirm = view.findViewById(R.id.confirm);
                                        confirm.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog.dismiss();
                                                showProgress();
                                                confirm.setEnabled(false);
                                                cleanParams(obdStatusInfo);
                                            }
                                        });
                                    }
                                })
                                .setLayoutRes(R.layout.dailog_common_warm)
                                .setCancelOutside(false)
                                .setDimAmount(0.5f)
                                .isCenter(true)
                                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                                .show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("cleanParams success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        GlobalUtil.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                dismissProgress();
                                BlueManager.getInstance().send(ProtocolUtils.cleanParams());
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.d("cleanParams failure " + e.getMessage());
                }
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        BlueManager.getInstance().removeCallBackListener(this);

        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

}
