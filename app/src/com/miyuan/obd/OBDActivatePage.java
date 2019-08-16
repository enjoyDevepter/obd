package com.miyuan.obd;

import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.hamster.log.FileLoggingTree;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.preferences.SettingPreferencesConfig;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;
import com.miyuan.obd.utils.URLUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.obd_activate_layout)
public class OBDActivatePage extends AppBasePage implements View.OnClickListener {

    String carName = "";
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;

    @ViewInject(R.id.status)
    private View statusV;

    private AnimationDrawable animationDrawable;

    private CustomDialog dialog;

    @Override
    public void onResume() {
        super.onResume();
        MainActivity.getInstance().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        title.setText("激活设备");
        back.setVisibility(View.GONE);
        reportV.setOnClickListener(this);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        animationDrawable.start();
        updateCarID();
    }

    @Override
    public void onStop() {
        animationDrawable.stop();
        super.onStop();
    }

    private void updateCarID() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("carId", getDate().get("carId"));
            jsonObject.put("serialNumber", getDate().get("sn"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("updateCarID input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .url(URLUtils.MODIFY_CAR_BRAND)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("updateCarID failure " + e.getMessage());
                GlobalUtil.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
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
                                                confirm.setEnabled(false);
                                                updateCarID();
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
                final String responese = response.body().string();

                Log.d("updateCarID success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        SettingPreferencesConfig.CAR.set(carName);
                        BlueManager.getInstance().send(ProtocolUtils.setCarID());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        PageManager.clearHistoryAndGo(new OBDAuthPage());
                    }
                } catch (JSONException e) {
                    Log.d("updateCarID failure " + e.getMessage());
                }
            }
        });
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
        Log.d("OBDActivatePage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", getDate().getString("sn")))
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
                    Log.d("OBDActivatePage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("OBDActivatePage uploadLog success " + responese);
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
                        Log.d("OBDActivatePage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }
}
