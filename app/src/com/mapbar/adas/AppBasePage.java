package com.mapbar.adas;


import com.mapbar.adas.view.CustomProgressDailog;
import com.mapbar.hamster.log.Log;
import com.mapbar.obd.R;

public abstract class AppBasePage extends FragmentPage {

    public CustomProgressDailog progress;

    public AppBasePage() {
        super(MainActivity.getInstance());
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                progress = new CustomProgressDailog(getContext());
            }
        });
    }

    @Override
    protected int getContainerViewId() {
        return R.id.main_activity_page_layer;
    }

    public void showProgress() {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d("showProgress ");
                if (!progress.isShowing()) {
                    progress.show();
                }
            }
        });
    }

    public void dismissProgress() {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                Log.d("dismissProgress ");
                if (progress.isShowing()) {
                    progress.hide();
                }
            }
        });
    }
}