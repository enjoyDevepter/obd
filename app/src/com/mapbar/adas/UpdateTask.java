package com.mapbar.adas;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.mapbar.adas.utils.CustomDialog;
import com.mapbar.adas.utils.OBDUtils;
import com.mapbar.adas.utils.URLUtils;
import com.mapbar.hamster.log.Log;
import com.miyuan.obd.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.mapbar.adas.GlobalUtil.getContext;
import static com.mapbar.adas.preferences.SettingPreferencesConfig.UPDATE_ID;

/**
 * 更新功能初始化
 */
public class UpdateTask extends BaseTask {

    CustomDialog dialog = null;
    DownloadManager downloadManager = (DownloadManager) GlobalUtil.getContext().getSystemService(Context.DOWNLOAD_SERVICE);
    DownLoadBroadCastReceiver downLoadBroadCastReceiver;
    private long downloadId;

    /**
     * 获取当前应用版本号
     *
     * @param context
     * @return
     */
    public static int getCurrentVersionCode(Context context) {

        PackageManager packageManager = context.getPackageManager();

        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);

            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return 0;

    }

    @Override
    public void excute() {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("version", getCurrentVersionCode(GlobalUtil.getContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("app update input " + jsonObject.toString());
        RequestBody requestBody = new FormBody.Builder()
                .add("params", GlobalUtil.encrypt(jsonObject.toString())).build();
        Request request = new Request.Builder()
                .url(URLUtils.APK_UPDATE)
                .addHeader("content-type", "application/json;charset:utf-8")
                .post(requestBody).build();
        GlobalUtil.getOkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("app update failure " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responese = response.body().string();
                Log.d("app update success " + responese);
                try {
                    JSONObject result = new JSONObject(responese);
                    if ("000".equals(result.optString("status"))) {
                        UpdateInfo updateInfo = JSON.parseObject(responese, UpdateInfo.class);
                        if (updateInfo.getUpdateState() == 1) {
                            // 提示更新对话框
                            showWarm(updateInfo);
                        }
                    }
                } catch (JSONException e) {
                    Log.d("app update failure " + e.getMessage());
                }
            }
        });
        complate();
    }

    private void showWarm(final UpdateInfo updateInfo) {

        dialog = CustomDialog.create(GlobalUtil.getMainActivity().getSupportFragmentManager())
                .setViewListener(new CustomDialog.ViewListener() {
                    @Override
                    public void bindView(View view) {
                        ((TextView) view.findViewById(R.id.desc)).setText("版本号" + updateInfo.getVersion() + "\n" + updateInfo.getDesc());
                        if (updateInfo.getIsMust() == 1) {
                            view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.dismiss();
                                }
                            });
                        } else {
                            view.findViewById(R.id.cancel).setVisibility(View.GONE);
                        }

                        view.findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                downLoadBroadCastReceiver = new DownLoadBroadCastReceiver();
                                IntentFilter intentFilter = new IntentFilter();
                                intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                                GlobalUtil.getContext().registerReceiver(downLoadBroadCastReceiver, intentFilter);
                                downloadAPk(updateInfo.getUrl());
                                dialog.dismiss();
                            }
                        });

                    }
                })
                .setLayoutRes(R.layout.dailog_apk_update)
                .setDimAmount(0.5f)
                .isCenter(true)
                .setWidth(OBDUtils.getDimens(getContext(), R.dimen.dailog_width))
                .show();

    }

    private void downloadAPk(String url) {
        downloadId = UPDATE_ID.get();
        if (downloadId != -1l) {

            int status = getDownloadStatus(downloadId);

            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                Uri uri = getDownloadUri(downloadId);
                if (uri != null) {
                    //对比下载的apk版本和本地应用版本
                    if (compare(getApkInfo(uri.getPath()))) {
                        installApk(uri);
                    } else {
                        downloadManager.remove(downloadId);
                        startDownload(url);
                    }
                }
            } else if (status == DownloadManager.STATUS_FAILED) {
                startDownload(url);
            }
        } else {
            startDownload(url);
        }
    }

    private void startDownload(final String url) {
        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("");
        request.setVisibleInDownloadsUi(true);
        //设置下载的路径
        //创建目录
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdir();

        //设置文件存放路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "app.apk");

        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        downloadId = downloadManager.enqueue(request);

        UPDATE_ID.set(downloadId);
    }

    /**
     * 获取保存的apk文件的地址
     *
     * @param downloadApkId
     * @return
     */
    private Uri getDownloadUri(long downloadApkId) {
        return downloadManager.getUriForDownloadedFile(downloadApkId);
    }

    /**
     * 获取下载的apk版本信息
     *
     * @param path
     * @return
     */
    private PackageInfo getApkInfo(String path) {
        PackageManager pm = GlobalUtil.getContext().getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(path, PackageManager.GET_ACTIVITIES);
        if (info != null) {

            return info;
        }
        return null;
    }

    /**
     * 如果当前版本号小于apk的版本号则返回true
     *
     * @param apkInfo
     * @return
     */
    private boolean compare(PackageInfo apkInfo) {
        if (apkInfo == null) {
            return false;
        }
        int versionCode = getCurrentVersionCode(GlobalUtil.getContext());

        if (apkInfo.versionCode > versionCode) {
            return true;
        }
        return false;
    }

    private int getDownloadStatus(long downloadApkId) {

        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadApkId);
        Cursor c = downloadManager.query(query);
        if (c != null) {
            try {
                if (c.moveToFirst()) {
                    return c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {

                c.close();
            }
        }
        return -1;
    }


    private void installApk(final Uri uri) {


//        File file = new File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//                , "myApp.apk");
//        Intent intent = new Intent(Intent.ACTION_VIEW);
//        // 由于没有在Activity环境下启动Activity,设置下面的标签
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        if (Build.VERSION.SDK_INT >= 24) { //判读版本是否在7.0以上
//            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
//            Uri apkUri =
//                    FileProvider.getUriForFile(GlobalUtil.getContext(), "com.mapbar.obd.fileprovider", file);
//            //添加这一句表示对目标应用临时授权该Uri所代表的文件
//            Log.d("apkUri " + apkUri);
//            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
//        } else {
//            intent.setDataAndType(Uri.fromFile(file),
//                    "application/vnd.android.package-archive");
//        }
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath(), "app.apk");
        Log.d("file.exists() " + file.exists());
        Log.d("file.getPath() " + file.getPath());
        Intent install = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {//判读版本是否在7.0以上
            Uri apkUri = FileProvider.getUriForFile(GlobalUtil.getContext(), "com.miyuan.obd.fileprovider", file);//在AndroidManifest中的android:authorities值
            Log.d("apkUri " + apkUri);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);//添加这一句表示对目标应用临时授权该Uri所代表的文件
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        GlobalUtil.getContext().startActivity(install);

    }

    private class DownLoadBroadCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (downloadId == id) {
                //下载完成
                GlobalUtil.getContext().unregisterReceiver(downLoadBroadCastReceiver);
                //跳到安装界面
                installApk(getDownloadUri(id));
            }
        }
    }

}