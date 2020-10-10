package com.miyuan.obd;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.gyf.barlibrary.ImmersionBar;
import com.miyuan.adas.BackStackManager;
import com.miyuan.adas.BasePage;
import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.HUDParams;
import com.miyuan.hamster.HUDWarmStatus;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.OBDStatusInfo;
import com.miyuan.hamster.core.HexUtils;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.OBDUtils;
import com.miyuan.obd.utils.PermissionUtil;
import com.miyuan.obd.utils.URLUtils;
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

import static com.miyuan.hamster.OBDEvent.AUTHORIZATION_SUCCESS;

public class MainActivity extends AppCompatActivity implements BleCallBackListener {

    private static MainActivity INSTANCE = null;
    public static boolean hasCheck = false;
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

    private FirmwareUpdateInfo updateInfo;


    CustomDialog dialog = null;

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_DISCONNECTED:
                Toast.makeText(GlobalUtil.getContext(), "OBD连接断开！", Toast.LENGTH_SHORT).show();
                PageManager.go(new ConnectPage());
                break;

            case OBDEvent.STATUS_UPDATA:
                obdStatusInfo = (OBDStatusInfo) data;
                updateStatusInfo(obdStatusInfo);
                break;
            case OBDEvent.AUTHORIZATION:
                obdStatusInfo = (OBDStatusInfo) data;
                break;
            case AUTHORIZATION_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
                if (!hasCheck) {
                    hasCheck = true;
                    checkFirmwareVersion(obdStatusInfo);
                }
                break;
            case OBDEvent.ADJUST_SUCCESS:
                break;
            case OBDEvent.COLLECT_DATA_FOR_CAR:
                new Thread(new CarRunnable((byte[]) data)).start();
                break;
            case OBDEvent.HUD_WARM_STATUS_INFO:
                updateWarmParams(((HUDWarmStatus) data).getOrigin());
                break;
            case OBDEvent.HUD_PARAMS_INFO:
                updateStateParams(((HUDParams) data).getOrigin());
                break;
            default:
                break;
        }
    }

    /**
     * 检查固件升级
     */
    private void checkFirmwareVersion(OBDStatusInfo obdStatusInfo) {
        if (null == obdStatusInfo) {
            return;
        }
        Log.d("checkFirmwareVersion");
        String sn = obdStatusInfo.getSn();
        String bVersion = obdStatusInfo.getbVersion();
        String pVersion = obdStatusInfo.getpVersion();

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", sn);
            jsonObject.put("pVersion", pVersion);
            jsonObject.put("bVersion", bVersion);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("checkFirmwareVersion input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_FIRMWARE)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("checkFirmwareVersion onFailure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("checkFirmwareVersion onResponse " + responese);
                updateInfo = JSON.parseObject(responese, FirmwareUpdateInfo.class);
                if (updateInfo.getbUpdateState() == 1) { // 固件需要升级
                    // 弹出对话框
                    GlobalUtil.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            showUpdateConfirmDailog();
                        }
                    });
                }
            }
        });
    }

    private void showUpdateConfirmDailog() {
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        TextView textView = view.findViewById(R.id.info);
                        String info = "当前有新版本升级，共需约" + (int) ((updateInfo.getSize() / 1024 * 0.6) / 60) + "分钟。升级过程中不能关闭手机，不能关闭硬件设备，不能做其他任何操作。否则升级失败可能导致设备使用不正常，需要重新升级。";
                        textView.setText(info);
                        view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                OBDUpdatePage page = new OBDUpdatePage();
                                Bundle bundle = new Bundle();
                                bundle.putString("url", updateInfo.getUrl());
                                bundle.putString("serialNumber", obdStatusInfo.getSn());
                                bundle.putString("bVersion", obdStatusInfo.getbVersion());
                                bundle.putString("pVersion", obdStatusInfo.getpVersion());
                                bundle.putString("message", updateInfo.getDesc());
                                bundle.putInt("size", updateInfo.getSize());
                                bundle.putInt("id", updateInfo.getId());
                                page.setDate(bundle);
                                PageManager.clearHistoryAndGo(page);
                                dialog.dismiss();
                            }
                        });

                        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_update)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(this, R.dimen.dailog_width))
                .show();
    }

    /**
     * 上传盒子预警信息
     *
     * @param state
     */
    private void updateWarmParams(byte[] state) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("warnParams", HexUtils.formatHexString(state));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("updateWarmParams input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_WARM_PARAMS)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("updateWarmParams failure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("updateWarmParams success " + responese);
            }
        });

    }


    /**
     * 上传盒子参数信息
     *
     * @param state
     */
    private void updateStateParams(byte[] state) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", obdStatusInfo.getSn());
            jsonObject.put("stateParams", HexUtils.formatHexString(state));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("updateStateParams input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.UPDATE_STATE_PARAMS)
                .post(requestBody)
                .addHeader("content-type", "application/json;charset:utf-8")
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("updateStateParams failure " + e.getMessage());

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("updateStateParams success " + responese);
            }
        });

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

    @Override
    protected void onStop() {
        super.onStop();
    }
}
