package com.miyuan.obd;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;

import com.miyuan.adas.FragmentPage;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;

public abstract class AppBasePage extends FragmentPage {

    private CustomDialog dialog;

    public AppBasePage() {
        super(MainActivity.getInstance());
    }

    @Override
    protected int getContainerViewId() {
        return R.id.main_activity_page_layer;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isOPen(MainActivity.getInstance())) {
            showGpsDialog();
        } else {
            if (null != dialog && dialog.isVisible()) {
                dialog.dismiss();
            }
        }
    }

    public boolean isOPen(Context context) {
        LocationManager locationManager
                = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // 通过GPS卫星定位，定位级别可以精确到街（通过24颗卫星定位，在室外和空旷的地方定位准确、速度快）
        boolean gps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (gps) {
            return true;
        }
        return false;
    }

    private void showGpsDialog() {
        if (dialog != null && dialog.isVisible()) {
            return;
        }
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        ((TextView) (view.findViewById(R.id.confirm))).setText("开启GPS");
                        ((TextView) (view.findViewById(R.id.info))).setText("请打开GPS，否则无法完成当前操作!");
                        ((TextView) (view.findViewById(R.id.title))).setText("GPS异常");
                        final View confirm = view.findViewById(R.id.confirm);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openGPS(MainActivity.getInstance());
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_common_warm)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(MainActivity.getInstance(), R.dimen.dailog_width))
                .show();
    }


    public void openGPS(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            GlobalUtil.getContext().startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            // The Android SDK doc says that the location settings activity
            // may not be found. In that case show the general settings.
            // General settings activity
            intent.setAction(Settings.ACTION_SETTINGS);
            GlobalUtil.getContext().startActivity(intent);
        }
    }
}