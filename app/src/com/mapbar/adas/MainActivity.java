package com.mapbar.adas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.client.android.CaptureActivity;
import com.gyf.barlibrary.ImmersionBar;
import com.mapbar.adas.utils.PermissionUtil;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.BleCallBackListener;
import com.mapbar.hamster.BlueManager;
import com.mapbar.hamster.OBDEvent;
import com.mapbar.hamster.OBDStatusInfo;
import com.mapbar.hamster.core.HexUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements BleCallBackListener {

    private static MainActivity INSTANCE = null;
    public boolean first = true;
    private ViewGroup rootViewGroup;
    private View splashView;


    private OBDStatusInfo obdStatusInfo;


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

    /**
     * 设置状态栏颜色
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     */
    public static void setColor(Activity activity, int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // 设置状态栏透明
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            // 生成一个状态栏大小的矩形
            View statusView = createStatusView(activity, color);
            // 添加 statusView 到布局中
            ViewGroup decorView = (ViewGroup) activity.getWindow().getDecorView();
            decorView.addView(statusView);
            // 设置根布局的参数
            ViewGroup rootView = (ViewGroup) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
            rootView.setFitsSystemWindows(true);
            rootView.setClipToPadding(true);
        }
    }

    /**
     * 生成一个和状态栏大小相同的矩形条
     *
     * @param activity 需要设置的activity
     * @param color    状态栏颜色值
     * @return 状态栏矩形条
     */
    private static View createStatusView(Activity activity, int color) {
        // 获得状态栏高度
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBarHeight = activity.getResources().getDimensionPixelSize(resourceId);

        // 绘制一个和状态栏一样高的矩形
        View statusView = new View(activity);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                statusBarHeight);
        statusView.setLayoutParams(params);
        statusView.setBackgroundColor(color);
        return statusView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setBackgroundDrawableResource(android.R.color.white);
        super.onCreate(savedInstanceState);

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
        splashView.setBackgroundResource(R.drawable.splash);
        rootViewGroup.addView(splashView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(rootViewGroup, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        setColor(this, Color.parseColor("#FF35BDB2"));

        BlueManager.getInstance().init(this);

        EventBus.getDefault().register(this);

        ImmersionBar.with(this)
                .fitsSystemWindows(true)
                .statusBarDarkFont(true)
                .statusBarColor(android.R.color.white)
                .init(); //初始化，默认透明状态栏和黑色导航栏
    }

    @Override
    protected void onResume() {
        super.onResume();

        PermissionUtil.requestPermissionForInit(new PermissionUtil.RequestPermission() {
            @Override
            public void onRequestPermissionSuccess() {
                //request permission success, do something.
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
        if (isFirst()) {
            addTasks();
        }
        setFirst(false);
        BlueManager.getInstance().addBleCallBackListener(this);
    }

    @Override
    protected void onDestroy() {
        ImmersionBar.with(this).destroy(); //不调用该方法，如果界面bar发生改变，在不关闭app的情况下，退出此界面再进入将记忆最后一次bar改变的状态
        super.onDestroy();
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
                .addTask(new LocationCheckTask())
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
        if (requestCode == 0) {
            if (data != null) {
                String result = data.getStringExtra(CaptureActivity.SCANRESULT);
                if (result != null) {
                    Bundle bundle = BackStackManager.getInstance().getCurrent().getDate();
                    if (bundle == null) {
                        bundle = new Bundle();
                    }
                    bundle.putString("sn", result);
                }
            }
        }
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_DISCONNECTED:
                Toast.makeText(GlobalUtil.getContext(), "OBD连接断开！", Toast.LENGTH_SHORT).show();
                PageManager.go(new ConnectPage());
                break;
            case OBDEvent.AUTHORIZATION_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                break;
            case OBDEvent.STATUS_UPDATA:
                obdStatusInfo = (OBDStatusInfo) data;
                updateStatusInfo(obdStatusInfo);
                break;
            case OBDEvent.ADJUST_SUCCESS:
                break;
            case OBDEvent.COLLECT_DATA_FOR_CAR:
                new Thread(new CarRunnable((byte[]) data)).start();
                break;
        }
    }

    /**
     * 上传状态信息
     *
     * @param obdStatusInfo
     */
    private void updateStatusInfo(OBDStatusInfo obdStatusInfo) {

        if ("0000000000000000000".equals(obdStatusInfo.getSn())) {
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("bState", HexUtils.formatHexString(obdStatusInfo.getOrginal()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("updateStatusInfo input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_TIRE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("updateStatusInfo failure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("updateStatusInfo success " + responese);
            }
        });

    }

    private void uploadCarData(String filePath) {

        final File file = new File(filePath);

        Log.d("uploadCarData input ");

        MediaType type = MediaType.parse("application/octet-stream");//"text/xml;charset=utf-8"
        RequestBody fileBody = RequestBody.create(type, file);

        RequestBody multipartBody = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE)
                //一样的效果
                .addPart(MultipartBody.Part.createFormData("serialNumber", obdStatusInfo.getSn()))
                .addPart(MultipartBody.Part.createFormData("type", "4"))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"file\"; filename=\"car\"")
                        , fileBody).build();


        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_ERROR_FILE)
                .post(multipartBody)
                .build();

        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("uploadCarData onFailure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("uploadCarData success " + responese);
                try {
                    final JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                } catch (JSONException e) {
                    Log.d("uploadCarData failure " + e.getMessage());
                }
            }
        });
    }

    private class CarRunnable implements Runnable {

        private byte[] data;

        public CarRunnable(byte[] data) {
            this.data = data;
        }

        @Override
        public void run() {
            try {
                File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "obd_collect" + File.separator);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File file = new File(dir, "car");
                try {
                    FileOutputStream fos = new FileOutputStream(file);
                    BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                    bw.write(HexUtils.byte2HexStr(data));
                    bw.flush();
                    bw.close();
                    fos.close();
                    // 上传
                    uploadCarData(file.getPath());
                } catch (FileNotFoundException e) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
