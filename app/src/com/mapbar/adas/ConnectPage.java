package com.mapbar.adas;

import android.graphics.drawable.AnimationDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mapbar.adas.preferences.SettingPreferencesConfig.SN;

@PageSetting(contentViewId = R.layout.connect_layout, toHistory = false)
public class ConnectPage extends AppBasePage implements View.OnClickListener, BleCallBackListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.connect)
    private View connect;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.retry)
    private View retry;

    private AnimationDrawable animationDrawable;

    @Override
    public void onResume() {
        super.onResume();
        back.setVisibility(View.GONE);
        title.setText("连接盒子");
        retry.setOnClickListener(this);
        reportV.setOnClickListener(this);
        BlueManager.getInstance().startScan();
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        connect.setBackgroundResource(R.drawable.connect_bg);
        animationDrawable = (AnimationDrawable) connect.getBackground();
        animationDrawable.start();
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onStop() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry:
                animationDrawable.start();
                retry.setClickable(false);
                retry.setVisibility(View.INVISIBLE);
                BlueManager.getInstance().startScan();
                break;
            case R.id.report:
                uploadLog();
                break;

        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.BLUE_SCAN_FINISHED:
                Log.d("OBDEvent.BLUE_SCAN_FINISHED " + data);
                if (!(boolean) data) {
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            animationDrawable.stop();
                            retry.setClickable(true);
                            retry.setVisibility(View.VISIBLE);
                        }
                    });
                }
                break;
            case OBDEvent.BLUE_CONNECTED:
                Log.d("OBDEvent.BLUE_CONNECTED");
//                PageManager.go(new OBDAuthPage());
                PageManager.go(new PhysicalPage());
                break;
        }

    }

    private void uploadLog() {
        Log.d("ConnectPage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            String sn;
            if ("XXXX-XXXX-XXXX-XXXX".equals(SN.get())) {
                sn = Build.MANUFACTURER + "_" + Build.MODEL + "_" + Build.BOARD;
            } else {
                sn = SN.get();
            }
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", sn))
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
                    Log.d("ConnectPage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("ConnectPage uploadLog success " + responese);
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
                        Log.d("ConnectPage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }
}
