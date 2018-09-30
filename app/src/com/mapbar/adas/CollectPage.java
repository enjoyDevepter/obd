package com.mapbar.adas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.AlarmManager;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


@PageSetting(contentViewId = R.layout.collect_layout)
public class CollectPage extends AppBasePage implements View.OnClickListener, BleCallBackListener, LocationListener {

    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.status)
    private View statusV;

    private int currentSpeed;

    private boolean isStudy;

    private LinkedList<Integer> adjustSpeed = new LinkedList<>();

    private LocationManager locationManager;

    private AnimationDrawable animationDrawable;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("深度校准即将完成");
        back.setVisibility(View.GONE);
        reportV.setOnClickListener(this);
        statusV.setBackgroundResource(R.drawable.check_status_bg);
        animationDrawable = (AnimationDrawable) statusV.getBackground();
        animationDrawable.start();
        if (!isStudy) {
            isStudy = true;
            GlobalUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    AlarmManager.getInstance().play(R.raw.adjust_last);
                }
            }, 2000);
            GlobalUtil.getHandler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    BlueManager.getInstance().send(ProtocolUtils.study());
                }
            }, 5000);
            locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);
        }
    }

    @Override
    public boolean onBackPressed() {
        PageManager.finishActivity(MainActivity.getInstance());
        return true;
    }

    @Override
    public void onStart() {
        BlueManager.getInstance().addBleCallBackListener(this);
        super.onStart();
    }

    @Override
    public void onDestroy() {
        isStudy = false;
        locationManager.removeUpdates(this);
        BlueManager.getInstance().removeCallBackListener(this);
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        animationDrawable.stop();
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

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.ADJUST_SUCCESS:
                locationManager.removeUpdates(this);
                CollectFinish collectFinish = new CollectFinish();
                Bundle bundle = new Bundle();
                bundle.putBoolean("success", true);
                collectFinish.setDate(bundle);
                PageManager.go(collectFinish);
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if ("gps".equals(location.getProvider())) {
            currentSpeed = (int) (location.getSpeed() * 3.6);
            if (currentSpeed < 50) {
                adjustSpeed.addLast(currentSpeed);
                if (adjustSpeed.size() >= 40) {
                    AlarmManager.getInstance().play(R.raw.adjust_last);
                    adjustSpeed.clear();
                }
            } else {
                adjustSpeed.clear();
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
}
