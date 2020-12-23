package com.miyuan.obd;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.adas.PageManager;
import com.miyuan.adas.anno.PageSetting;
import com.miyuan.adas.anno.ViewInject;
import com.miyuan.hamster.BleCallBackListener;
import com.miyuan.hamster.BlueManager;
import com.miyuan.hamster.OBDEvent;
import com.miyuan.hamster.Update;
import com.miyuan.hamster.core.HexUtils;
import com.miyuan.hamster.core.ProtocolUtils;
import com.miyuan.hamster.log.FileLoggingTree;
import com.miyuan.hamster.log.Log;
import com.miyuan.obd.utils.CustomDialog;
import com.miyuan.obd.utils.EncodeUtil;
import com.miyuan.obd.utils.OBDUtils;
import com.miyuan.obd.utils.URLUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import static com.miyuan.obd.preferences.SettingPreferencesConfig.SN;

@PageSetting(contentViewId = R.layout.obd_update_layout, toHistory = false)
public class OBDUpdatePage extends AppBasePage implements BleCallBackListener, View.OnClickListener {

    private static final int UNIT = 1024 * 4;
    @ViewInject(R.id.title)
    private TextView title;
    @ViewInject(R.id.back)
    private View back;
    @ViewInject(R.id.report)
    private View reportV;
    @ViewInject(R.id.message)
    private TextView messageTV;
    @ViewInject(R.id.info)
    private TextView infoTV;
    @ViewInject(R.id.progress)
    private ProgressBar progressBar;
    private CustomDialog dialog;
    private int flashIndex = 0;
    private byte[] checkUpdate;
    private String appFilePath = null;
    private String updatePath = null;
    private long count;
    private long current;
    private boolean update;
    private byte[] updates;
    private List<String> flashFilePath = new ArrayList<>();

    private int firmwareCurrentIndex = -1;

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroy() {
        BlueManager.getInstance().removeCallBackListener(this);
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    private int flashCurrentIndex = -1;

    @Override
    public void onResume() {
        super.onResume();
        title.setText("固件升级");
        reportV.setOnClickListener(this);
        back.setVisibility(View.GONE);
        messageTV.setText(getDate().getString("message"));
        infoTV.setText("本次升级共需约" + (int) ((getDate().getInt("size") / 1024 * 0.6) / 60) + "分，当前升级进度：");
        if (!update) {
            BlueManager.getInstance().addBleCallBackListener(this);
            update = true;
            Log.d("update  " + update);
            downloadFirmware(getDate().getString("url"));
        }

    }

    @Override
    public void onEvent(int event, Object data) {
        switch (event) {
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
                    firmwareCurrentIndex = 0;
                    updateForOneUnit(0, false);
                }
                break;
            case OBDEvent.OBD_FIRMWARE_UPDATE_FINISH_UNIT:
                Update update = (Update) data;
                switch (update.getStatus()) {
                    case 0:
                        //  重新传递
                        firmwareCurrentIndex = update.getIndex();
                        updateForOneUnit(update.getIndex(), false);
                        break;
                    case 1:
                        // 继续
                        current += UNIT;
                        progressBar.setProgress((int) current);
                        if (firmwareCurrentIndex == update.getIndex() + 1) {
                            return;
                        }
                        firmwareCurrentIndex = update.getIndex() + 1;
                        updateForOneUnit(update.getIndex() + 1, false);
                        break;
                    case 2: // 固件升级完成
                        // 开始升级flash
                        if (flashFilePath.size() > 0) {
                            String flashPath = flashFilePath.get(flashIndex);
                            getUpdateInfo(flashPath, true);
                            BlueManager.getInstance().send(checkUpdate);
                        } else {
                            // 升级完成
                            firmwareCurrentIndex = -1;
                            notifyUpdateSuccess();
                            showUpdateFinisheDialog();
                        }
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
                    flashCurrentIndex = 0;
                    updateForOneUnit(0, true);
                }
                break;
            case OBDEvent.OBD_FLASH_UPDATE_FINISH_UNIT:
                Update flashUpdate = (Update) data;
                Log.d("OBD_FLASH_UPDATE_FINISH_UNIT  " + flashUpdate);
                switch (flashUpdate.getStatus()) {
                    case 0:
                        //  重新传递
                        flashCurrentIndex = flashUpdate.getIndex();
                        updateForOneUnit(flashUpdate.getIndex(), true);
                        break;
                    case 1:
                        // 继续
                        current += UNIT;
                        progressBar.setProgress((int) current);
                        if (flashCurrentIndex == flashUpdate.getIndex() + 1) {
                            return;
                        }
                        flashCurrentIndex = flashUpdate.getIndex() + 1;
                        updateForOneUnit(flashUpdate.getIndex() + 1, true);
                        break;
                    case 2: // 升级完成 判断是否还有其他flash文件
                        if (++flashIndex > flashFilePath.size() - 1) {
                            // flash 升级完成 通知服务器
                            flashCurrentIndex = -1;
                            notifyUpdateSuccess();
                            showUpdateFinisheDialog();
                        } else {
                            getUpdateInfo(flashFilePath.get(flashIndex), true);
                        }
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.report:
                showLogDailog();
                break;
        }
    }


    private void downloadFirmware(final String url) {
        updatePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "update";
        // 先删除原来升级文件
        delFile(new File(updatePath), true);

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
                    updatePath = Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "update";
                    File dir = new File(updatePath);
                    if (!dir.exists()) {
                        dir.mkdirs();
                    }
                    File dest = new File(updatePath, url.substring(url.lastIndexOf("/") + 1));
                    sink = Okio.sink(dest);
                    bufferedSink = Okio.buffer(sink);
                    bufferedSink.writeAll(response.body().source());
                    bufferedSink.close();
                    decompress(dest.getPath(), updatePath);
                    // 分析固件文件
                    analysisFirmwareFile(updatePath);
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

        BlueManager.getInstance().setObdUpdate(true);
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
                Log.d("prefix " + prefix);
                String[] version = prefix.split("_");
                Log.d("version " + Arrays.toString(version));
                int index = HexUtils.hexStringToBytes(version[1])[0];
                short start = HexUtils.byteToShort(HexUtils.hexStringToBytes(version[2]));
                Log.d(" flashFilePath updates.length " + updates.length + " index =  " + index + " start=  " + start);
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
                count += in.available();
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
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                progressBar.setMax((int) count);
            }
        });
        Log.d("updateFile Size " + count);
    }

    private void updateForOneUnit(int index, boolean isFlash) {

        int num = updates.length % UNIT == 0 ? updates.length / UNIT : updates.length / UNIT + 1;

        if (index > num - 1) {
            return;
        }

//        showUpdateProgress(index == 1 ? updates.length : (index - 1) * UNIT);

        byte[] date = new byte[UNIT];
        int length = UNIT;
        if (index == num - 1) {
            if (updates.length % UNIT != 0) {
                length = updates.length % UNIT;
            }
        }
        System.arraycopy(updates, 0 + index * UNIT, date, 0, length);

        if (isFlash) {
            BlueManager.getInstance().send(ProtocolUtils.updateFlashForUnit(index, date));
        } else {
            BlueManager.getInstance().send(ProtocolUtils.updateFirmwareForUnit(index, date));
        }
    }

    private void showLogDailog() {
        GlobalUtil.getHandler().post(new Runnable() {
            @Override
            public void run() {
                dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                        .setViewListener(new CustomDialog.ViewListener() {
                            @Override
                            public void bindView(View view) {
                                ((TextView) (view.findViewById(R.id.sn))).setText(SN.get());
                                view.findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        uploadLog();
                                        dialog.dismiss();
                                    }
                                });
                                view.findViewById(R.id.copy).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //获取剪贴板管理器
                                        ClipboardManager cm = (ClipboardManager) GlobalUtil.getMainActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                        // 创建普通字符型ClipData
                                        ClipData mClipData = ClipData.newPlainText("Label", SN.get());
                                        // 将ClipData内容放到系统剪贴板里。
                                        cm.setPrimaryClip(mClipData);
                                    }
                                });
                            }
                        })
                        .setLayoutRes(R.layout.log_dailog)
                        .setCancelOutside(false)
                        .setDimAmount(0.5f)
                        .isCenter(true)
                        .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                        .show();
            }
        });
    }

    /**
     * 通知服务器固件升级完成
     */
    private void notifyUpdateSuccess() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("serialNumber", getDate().getString("serialNumber"));
            jsonObject.put("bVersion", getDate().getString("bVersion"));
            jsonObject.put("pVersion", getDate().getString("pVersion"));
            jsonObject.put("id", getDate().getInt("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("notifyUpdateSuccess input " + jsonObject.toString());

        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();

        Request request = new Request.Builder()
                .url(URLUtils.FIRMWARE_UPDATE_SUCCESS)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody)
                .build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("notifyUpdateSuccess failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("notifyUpdateSuccess success " + responese);
                if (null != updatePath) {
                    File updateDir = new File(updatePath);
                    if (updateDir.exists()) {
                        delFile(updateDir, false);
                    }
                }
            }
        });
    }

    private void delFile(@NonNull File file, boolean isDown) {
        if (file.isFile()) {
            boolean del = file.delete();
            Log.d("isDown  " + isDown + " del  " + del + " delFile path  " + file.getPath());
            return;
        }
        if (null != file.listFiles() && file.listFiles().length > 0) {
            for (File f : file.listFiles()) {
                delFile(f, isDown);
            }
        }
    }

    private void showUpdateFinisheDialog() {
        BlueManager.getInstance().setObdUpdate(false);
        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        final View confirm = view.findViewById(R.id.confirm);
                        confirm.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                PageManager.finishActivity(MainActivity.getInstance());
                                dialog.dismiss();
                            }
                        });
                    }
                })
                .setLayoutRes(R.layout.dailog_update_finished)
                .setCancelOutside(false)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();
    }

    private void uploadLog() {
        Log.d("OBDUpdatePage uploadLog ");
        final File dir = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "obd" + File.separator + "log");
        final File[] logs = dir.listFiles();

        if (null != logs && logs.length > 0) {
            MultipartBody.Builder builder = new MultipartBody.Builder();
            builder.addPart(MultipartBody.Part.createFormData("serialNumber", getDate().getString("serialNumber")))
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
                    Log.d("OBDUpdatePage uploadLog onFailure " + e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responese = response.body().string();
                    Log.d("OBDUpdatePage uploadLog success " + responese);
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
                        Log.d("OBDUpdatePage uploadLog failure " + e.getMessage());
                    }
                }
            });
        }
    }
}
