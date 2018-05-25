package com.mapbar.adas;


import com.wedrive.welink.adas.R;

public abstract class AppBasePage extends FragmentPage {

    public AppBasePage() {
        super(MainActivity.getInstance());
    }

    @Override
    protected int getContainerViewId() {
        return R.id.main_activity_page_layer;
    }
}