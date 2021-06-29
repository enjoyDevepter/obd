package com.miyuan.obd;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.BackStackManager;
import com.miyuan.adas.BasePage;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.obd.utils.PermissionUtil;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.simple.eventbus.EventBus;

import java.util.List;

import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener;

public class MainActivity extends AppCompatActivity implements BleCallBackListener {

    private static MainActivity INSTANCE = null;
    public boolean first = true;
    private ViewGroup rootViewGroup;
    private View splashView;


    public MainActivity() {
        if (null == MainActivity.INSTANCE) {
            MainActivity.INSTANCE = this;
            GlobalUtil.setMainActivity(this);
        } else {
            throw new RuntimeException("MainActivity.INSTANCE is not null");
        }
    }

    /**
     * 获得实例
     *
     * @return
     */
    public static MainActivity getInstance() {
        return MainActivity.INSTANCE;
    }


    Intent serviceForegroundIntent;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? android.R.color.black : android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        GlobalUtil.setMainActivity(this);

        rootViewGroup = new FrameLayout(this);

        // 页面容器
        final FrameLayout pageContainer = new FrameLayout(this);
        pageContainer.setId(R.id.main_activity_page_layer);
        rootViewGroup.addView(pageContainer, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        // 启动画面
        splashView = new View(this);
        splashView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        splashView.setBackgroundResource(android.R.color.transparent);
        rootViewGroup.addView(splashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(rootViewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        BlueManager.getInstance().init(this);

        EventBus.getDefault().register(this);

        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? android.R.color.black : android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏

        BlueManager.getInstance().addBleCallBackListener(MainActivity.this);
    }


    @Override
    protected void onDestroy() {
        ImmersionBar.with(this).destroy(); //不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        super.onDestroy();
        if (serviceForegroundIntent != null) {
            stopService(serviceForegroundIntent);
            serviceForegroundIntent = null;
        }
        BlueManager.getInstance().disconnect();
        MainActivity.INSTANCE = null;
        EventBus.getDefault().unregister(this);
        BlueManager.getInstance().removeCallBackListener(this);
    }

    public boolean isFirst() {
        return first;
    }

    public void setFirst(boolean first) {
        this.first = first;
    }

    private void addTasks() {
        TaskManager.getInstance()
//                .addTask(new SDInitTask())
                .addTask(new DisclaimerTask())
//                .addTask(new LocationCheckTask())
                .addTask(new UpdateTask());
        TaskManager.getInstance().next();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBack();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    private void onBack() {
        final BasePage current = BackStackManager.getInstance().getCurrent();
        if (current != null) {
            if (current.onBackPressed()) {
            } else {
                PageManager.back();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        serviceForegroundIntent = new Intent(this, LocationService.class);
        serviceForegroundIntent.putExtra(LocationService.EXTRA_NOTIFICATION_CONTENT, "汽车卫士");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceForegroundIntent);
        } else {
            startService(serviceForegroundIntent);
        }
    }

    public void hideSplash() {
        if (splashView != null) {
            rootViewGroup.removeView(splashView);
            splashView = null;
        }
        getWindow().setBackgroundDrawable(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Nullable
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        return super.onCreateDialog(id, args);
    }


    @Override
    protected void onResume() {
        super.onResume();
        PermissionUtil.requestPermissionForInit(new PermissionUtil.RequestPermission() {
            @Override
            public void onRequestPermissionSuccess() {
                //request permission success, do something.
                if (isFirst()) {
                    addTasks();
                }
                setFirst(false);
            }

            @Override
            public void onRequestPermissionFailure(List<String> permissions) {
                PageManager.finishActivity(MainActivity.this);
            }

            @Override
            public void onRequestPermissionFailureWithAskNeverAgain(List<String> permissions) {
                PageManager.finishActivity(MainActivity.this);
            }
        }, new RxPermissions(MainActivity.getInstance()), RxErrorHandler.builder().with(MainActivity.getInstance()).responseErrorListener(new ResponseErrorListener() {
            @Override
            public void handleResponseError(Context context, Throwable t) {
            }
        }).build());
        if (serviceForegroundIntent != null) {
//            AMapNavi.getInstance(this).setIsUseExtraGPSData(false);
            stopService(serviceForegroundIntent);
            serviceForegroundIntent = null;
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_DISCONNECTED:
                Toast.makeText(GlobalUtil.getContext(), "OBD连接断开！", Toast.LENGTH_SHORT).show();
                PageManager.go(new ConnectPage());
                break;
            case OBDEvent.ADJUST_SUCCESS:
                break;
            case OBDEvent.COLLECT_DATA_FOR_CAR:
                break;
            case OBDEvent.HUD_WARM_STATUS_INFO:
                break;
            case OBDEvent.HUD_PARAMS_INFO:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
