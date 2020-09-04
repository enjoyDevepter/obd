package com.miyuan.obd;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
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
import com.miyuan.hamster.Update;
import com.miyuan.hamster.core.HexUtils;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.utils.EncodeUtil;
import com.miyuan.obd.utils.PermissionUtil;
import com.miyuan.obd.utils.URLUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

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
    }


    private static final int UNIT = 1024;

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

    private int flashIndex = 0;
    private byte[] checkUpdate;
    private String appFilePath = null;
    private byte[] updates;
    private List<String> flashFilePath = new ArrayList<>();

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
                BlueManager.getInstance().addBleCallBackListener(MainActivity.this);
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

        obdStatusInfo = new OBDStatusInfo();
        obdStatusInfo.setSn("JNL7-VB93-GCXL-V32W");
        obdStatusInfo.setbVersion("TPMSA01V1001");
        obdStatusInfo.setpVersion("TPMSA01V1001");
        checkFirmwareVersion(obdStatusInfo);
    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
            case OBDEvent.OBD_DISCONNECTED:
                Toast.makeText(GlobalUtil.getContext(), "OBD连接断开！", Toast.LENGTH_SHORT).show();
                PageManager.go(new ConnectPage());
                break;
            case OBDEvent.OBD_FIRMWARE_BEGIN_UPDATE:
                if ((Integer) data == 0) { // 是否可以升级
                    try {
                        Thread.sleep(2000);
                        BlueManager.getInstance().send(checkUpdate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 固件升级开始
                    updateForOneUnit(0, false);
                }
                break;
            case OBDEvent.OBD_FIRMWARE_UPDATE_FINISH_UNIT:
                Update update = (Update) data;
                switch (update.getStatus()) {
                    case 0:
                        //  重新传递
                        updateForOneUnit(update.getIndex(), false);
                        break;
                    case 1:
                        // 继续
                        updateForOneUnit(update.getIndex() + 1, false);
                        break;
                    case 2: // 固件升级完成
                        // 开始升级flash
                        String flashPath = flashFilePath.get(flashIndex);
                        getUpdateInfo(flashPath, true);
                        break;
                }
                break;
            case OBDEvent.OBD_FLASH_BEGIN_UPDATE:
                if ((Integer) data == 0) { // flash是否可以升级
                    try {
                        Thread.sleep(2000);
                        BlueManager.getInstance().send(checkUpdate);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    // 固件升级开始
                    updateForOneUnit(0, true);
                }
                break;
            case OBDEvent.OBD_FLASH_UPDATE_FINISH_UNIT:
                Update flashUpdate = (Update) data;
                switch (flashUpdate.getStatus()) {
                    case 0:
                        //  重新传递
                        updateForOneUnit(flashUpdate.getIndex(), true);
                        break;
                    case 1:
                        // 继续
                        updateForOneUnit(flashUpdate.getIndex() + 1, true);
                        break;
                    case 2: // 升级完成 判断是否还有其他flash文件
                        if (++flashIndex > flashFilePath.size() - 1) {
                            // flash 升级完成
                        } else {
                            getUpdateInfo(flashFilePath.get(flashIndex), true);
                        }
                        break;
                }
                break;
            case OBDEvent.STATUS_UPDATA:
                obdStatusInfo = (OBDStatusInfo) data;
                updateStatusInfo(obdStatusInfo);
                break;
            case OBDEvent.AUTHORIZATION:
                obdStatusInfo = (OBDStatusInfo) data;
                break;
            case OBDEvent.AUTHORIZATION_SUCCESS:
                obdStatusInfo = (OBDStatusInfo) data;
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
            case OBDEvent.NORMAL:
                obdStatusInfo = (OBDStatusInfo) data;
                checkFirmwareVersion(obdStatusInfo);
                break;
            default:
                break;
        }
    }

    private void updateForOneUnit(int index, boolean isFlash) {

        int num = updates.length % UNIT == 0 ? updates.length / UNIT : updates.length / UNIT + 1;

        if (index > num) {
            return;
        }

//        showUpdateProgress(index == 1 ? updates.length : (index - 1) * UNIT);

        byte[] date;
        if (index == num) {
            if (updates.length % UNIT == 0) {
                date = new byte[UNIT];
            } else {
                date = new byte[updates.length % UNIT];
            }
        } else {
            date = new byte[UNIT];
        }
        System.arraycopy(updates, 0 + (index - 1) * UNIT, date, 0, date.length);

        if (isFlash) {
            BlueManager.getInstance().send(ProtocolUtils.updateFlashForUnit(index, date));
        } else {
            BlueManager.getInstance().send(ProtocolUtils.updateFirmwareForUnit(index, date));
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
                FirmwareUpdateInfo info = JSON.parseObject(responese, FirmwareUpdateInfo.class);
                if (info.getbUpdateState() == 1) { // 固件需要升级
                    downloadFirmware(info.getUrl());
                }
            }
        });
    }

    private void downloadFirmware(final String url) {
        Request request = new Request.Builder().url(url).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 下载失败
                e.printStackTrace();
                Log.d("downloadFirmware failure " + e.getMessage());
            }

            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Sink sink = null;
                BufferedSink bufferedSink = null;
                try {
                    String mSDCardPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "update";
                    File dir = new File(mSDCardPath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(mSDCardPath, url.substring(url.lastIndexOf("/") + 1));
                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());
                    bufferedSink.close();
                    decompress(dest.getPath(), mSDCardPath);
                    // 分析固件文件
                    analysisFirmwareFile(mSDCardPath);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (bufferedSink != null) {
                        bufferedSink.close();
                    }
                }
            }
        });
    }

    private void analysisFirmwareFile(String mSDCardPath) {
        File dir = new File(mSDCardPath);
        if (dir.exists()) {
            for (File file : dir.listFiles()) {
                if (file.getName().startsWith("app")) {
                    appFilePath = file.getPath();
                    continue;
                }
                if (file.getName().startsWith("flash")) {
                    flashFilePath.add(file.getPath());
                    continue;
                }
            }
        }

        if (appFilePath != null) {
            getUpdateInfo(appFilePath, false);
            BlueManager.getInstance().send(checkUpdate);
        } else {
            if (flashFilePath.size() > 0) {
                getUpdateInfo(flashFilePath.get(flashIndex), true);
            }
            Log.d("appFilePath not exists");
        }
    }

    private void getUpdateInfo(String path, boolean isFlash) {
        FileInputStream fis = null;
        try {
            File file = new File(path);
            fis = new FileInputStream(file);
            updates = new byte[fis.available()];
            fis.read(updates);
            fis.close();
            if (isFlash) {
                String prefix = file.getName().substring(0, file.getName().indexOf("."));
                String[] version = prefix.split("_");
                int index = HexUtils.hexStringToBytes(version[1])[0];
                short start = HexUtils.byteToShort(HexUtils.hexStringToBytes(version[2]));
                checkUpdate = ProtocolUtils.updateFlashInfo(index, start, updates.length);
            } else {
                String prefix = file.getName().substring(0, file.getName().indexOf("."));
                String version = prefix.split("_")[1];
                Log.d(" appFilePath updates.length " + updates.length + " prefix =  " + prefix + " version=  " + version);
                checkUpdate = ProtocolUtils.updateFirmwareInfo(version.getBytes(), HexUtils.intToByte(updates.length));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != fis) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void decompress(String zipFile, String dstPath) throws Exception {
        String fileEncode = EncodeUtil.getEncode(zipFile, true);
        ZipFile zip = new ZipFile(zipFile, Charset.forName(fileEncode));
        for (Enumeration entries = zip.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            String zipEntryName = entry.getName();
            InputStream in = null;
            OutputStream out = null;
            try {
                in = zip.getInputStream(entry);
                String outPath = (dstPath + "/" + zipEntryName).replaceAll("\\*", "/");
                //判断路径是否存在,不存在则创建文件路径
                File file = new File(outPath.substring(0, outPath.lastIndexOf('/')));
                if (!file.exists()) {
                    file.mkdirs();
                }
                //判断文件全路径是否为文件夹,如果是上面已经上传,不需要解压
                if (new File(outPath).isDirectory()) {
                    continue;
                }
                out = new FileOutputStream(outPath);
                byte[] buf1 = new byte[1024];
                int len;
                while ((len = in.read(buf1)) > 0) {
                    out.write(buf1, 0, len);
                }
            } finally {
                if (null != in) {
                    in.close();
                }

                if (null != out) {
                    out.close();
                }
            }
        }
        zip.close();
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
