package com.mapbar.adas;

import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
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

@PageSetting(contentViewId = R.layout.collect_finish_layout)
public class CollectFinish extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.status)
    private TextView statusTV;
    @ViewInject(R.id.confirm)
    private TextView confirmV;
    private Timer timer = new Timer();
    private TimerTask timerTask;
    private boolean success;

    private volatile boolean needNotifyParamsSuccess;
    private OBDStatusInfo obdStatusInfo;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        reportV.setVisibility(View.GONE);
        success = getDate().getBoolean("success");
        if (success) {
            title.setText("恭喜您");
            confirmV.setOnClickListener(this);
            confirmV.setVisibility(View.VISIBLE);
            statusTV.setText(Html.fromHtml("<font color='#4A4A4A'>恭喜您！胎压盒子可以正常使用了！<br><br><br>当轮胎亏气时,</font><font color='#009488'>胎压盒子会发出连续蜂鸣声！</font><font color='#4A4A4A'>此时您需要停车并用APP连接盒子，点击“校准”后可以停止蜂鸣！如果继续亏气，仍然会再次蜂鸣！</font>"));
            GlobalUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlarmManager.getInstance().play(R.raw.adjust_success);
                }
            }, 2000);
        } else {
            confirmV.setVisibility(View.GONE);
            title.setText("需要继续深度校准");
            statusTV.setText(Html.fromHtml("<font color='#4A4A4A'>由于您的车辆胎压不够灵敏，需要继续深度校准，深度校准过程一般需要10分钟，这期间可以关闭手机APP，可以按照任意路线行驶（无须按指定路线行驶）。</font><br><br><br><font color='#4A4A4A'>请您每个每隔</font><font color='#009488'>15分钟</font><font color='#4A4A4A'>连接手机APP检查校准结果，避免校准失败导致误报或漏报.</font><br><br><font color='#4A4A4A'>如果长时间深度校准未完成时，可以联系我司客服人员！</font>"));
            // 每隔1s一次获取参数
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    checkOBDVersion();
                }
            };
            BlueManager.getInstance().addBleCallBackListener(this);
            timer.schedule(timerTask, 0, 60 * 1000);
            GlobalUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlarmManager.getInstance().play(R.raw.adjust_fail);
                }
            }, 2000);
        }
    }

    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.confirm:
                PageManager.go(new OBDAuthPage());
                break;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!success && null != timer) {
            timerTask.cancel();
            timerTask = null;
            timer.cancel();
            timer = null;
        }
    }

    private void checkOBDVersion() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", getDate().getString("sn"));
            jsonObject.put("bVersion", getDate().getString("bVersion"));
            jsonObject.put("pVersion", getDate().getString("pVersion"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("CollectFinish checkOBDVersion input " + jsonObject.toString());

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
                Log.d("CollectFinish checkOBDVersion failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("CollectFinish checkOBDVersion success " + responese);
                final OBDVersion obdVersion = JSON.parseObject(responese, OBDVersion.class);
                if ("000".equals(obdVersion.getStatus())) {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (obdVersion.getUpdateState() == 4) {
                                // 车型不支持
                                title.setText("您的车辆不支持");
                                confirmV.setVisibility(View.VISIBLE);
                                confirmV.setText("关闭");
                                if (timerTask != null) {
                                    timerTask.cancel();
                                    timer.cancel();
                                    timerTask = null;
                                    timer = null;
                                }
                                statusTV.setText(Html.fromHtml("<font color='#4A4A4A'>校准失败，您的车辆不支持胎压盒子！</font><br><font color='#4A4A4A'>请您联系经销商退货！</font><br><br><font color='#4A4A4A'>给您带来的不便非常抱歉！</font>"));
                            } else {
                                switch (obdVersion.getpUpdateState()) {
                                    case 0:  // 无参数更新
                                        break;
                                    case 1: // 有更新
                                        needNotifyParamsSuccess = true;
                                        BlueManager.getInstance().send(ProtocolUtils.updateParams(getDate().getString("sn"), obdVersion.getParams()));
                                        break;
                                    case 2: // 临时车型，需要采集
                                        break;
                                    case 3: // 临时车型，参数已采集
                                        break;
                                }
                            }
                        }
                    });
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

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.PARAM_UPDATE_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                notifyUpdateSuccess();
                break;
        }
    }

    /**
     * 通知服务器参数升级完成
     */
    private void notifyUpdateSuccess() {
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
                        if (null != timer) {
                            timerTask.cancel();
                            timerTask = null;
                            timer.cancel();
                            timer = null;
                        }
                        PageManager.go(new OBDAuthPage());
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
}
