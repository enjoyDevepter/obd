package com.mapbar.adas;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.TextView;

import com.mapbar.adas.anno.PageSetting;
import com.mapbar.adas.anno.ViewInject;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.adas.view.SensitiveView;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.core.ProtocolUtils;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

@PageSetting(contentViewId = R.layout.main_layout, flag = BasePage.FLAG_SINGLE_TASK)
public class MainPage extends AppBasePage implements View.OnClickListener, BleCallBackListener, LocationListener {
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
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000l, 0, this);

        if (BlueManager.getInstance().isConnected()) {
            verify();
        }

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
                sendDate();
                break;
        }
    }

    private void sendDate() {
        byte[] temp = new byte[]{00, 01, 02, 03, 04, 05, 06, 07, 00, 01};
        Log.d(String.valueOf(temp.length));
        BlueManager.getInstance().write(ProtocolUtils.test(temp));
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
                        final SensitiveView sensitiveView = view.findViewById(R.id.sensitive);
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
                                BlueManager.getInstance().write(ProtocolUtils.getTirePressureStatus());
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
        new Exception().printStackTrace();

        switch (event) {
            case OBDEvent.OBD_FIRST_USE:
                Log.d("OBDEvent.OBD_FIRST_USE ");
                boxId = (String) data;
                Log.d("boxId  " + boxId);
                ChoiceCarPage phonePage = new ChoiceCarPage();
                Bundle bundle = new Bundle();
                bundle.putString("boxId", boxId);
                PageManager.go(phonePage);
                // 获取授权吗
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
                break;
            case OBDEvent.OBD_EXPIRE:
                Log.d("OBDEvent.OBD_EXPIRE ");
                sn = (String) data;
                // 获取授权吗
                break;
        }
    }


    /**
     * 获取授权码
     */
    private void auth() {

    }

    private void verify() {
        final Request request = new Request.Builder().url(URLUtils.GET_TIME).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkLocLicense success " + responese);
                if (verified) {
                    return;
                }
                verified = true;
                try {
                    JSONObject result = new JSONObject(responese);
                    BlueManager.getInstance().write(ProtocolUtils.verify(Long.valueOf(result.optString("server_time"))));
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
            SharedPreferences preferences = getContext().getSharedPreferences("setting", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong("server_time", location.getTime());
            editor.apply();
            locationManager.removeUpdates(this);
            if (verified) {
                return;
            }
            verified = true;
            BlueManager.getInstance().write(ProtocolUtils.verify(location.getTime()));
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
