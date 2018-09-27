package com.mapbar.adas;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@PageSetting(contentViewId = R.layout.collect_layout)
public class CollectPage extends AppBasePage implements View.OnClickListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    private boolean matching;

    @ViewInject(R.id.status)
    private View statusV;

    private AnimationDrawable animationDrawable;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("正在深度校准");
        back.setVisibility(View.GONE);
        reportV.setOnClickListener(this);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        animationDrawable.start();
        matching = getDate().getBoolean("matching");
        GlobalUtil.getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AlarmManager.getInstance().play(R.raw.begin_collect);
            }
        }, 2000);
        if (matching) {
            GlobalUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    BlueManager.getInstance().send(ProtocolUtils.study());
                }
            }, 3000);
        }
    }

    @Subscriber(tag = EventBusTags.COLLECT_FINISHED)
    private void updateCollectStauts(int type) {
        CollectFinish collectFinish = new CollectFinish();
        Bundle bundle = new Bundle();
        bundle.putBoolean("success", matching);
        collectFinish.setDate(bundle);
        PageManager.go(collectFinish);
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        animationDrawable.stop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:
                uploadLog();
                break;
        }
    }

    private void uploadLog() {
        Log.d("CollectPage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", getDate().getString("sn")))
                    .addPart(MultipartBody.Part.createFormData("type", "1"));
            for (File file : logs) {
                builder.addFormDataPart("file", file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), file));
            }
            Request request = new Request.Builder()
                    .url(URLUtils.UPDATE_ERROR_FILE)
                    .post(builder.build())
                    .build();

            GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.d("CollectPage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("CollectPage uploadLog success " + responese);
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
                                delete.delete();
                            }
                        }
                    } catch (JSONException e) {
                        Log.d("CollectPage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }

}
