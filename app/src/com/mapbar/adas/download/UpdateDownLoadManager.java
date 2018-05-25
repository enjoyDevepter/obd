package com.mapbar.adas.download;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mapbar.adas.GlobalUtil;
import com.wedrive.welink.adas.R;

import java.io.File;
import java.text.DecimalFormat;

/**
 * Created by shisk on 2017/7/19.
 */

public class UpdateDownLoadManager {


    private DownLoadAppTask downLoadAppTask;
    private CheckUpdateTask checkUpdateTask;
    private DecimalFormat df = new java.text.DecimalFormat("#.00");

    private UpdateDownLoadManager() {
    }

    ;
    private static UpdateDownLoadManager updateDownLoadManager;

    public static synchronized UpdateDownLoadManager getInstance() {

        if (updateDownLoadManager == null)
            updateDownLoadManager = new UpdateDownLoadManager();

        return updateDownLoadManager;
    }

    /**
     * 检测是否有更新
     */
    public void checkUpdate(OnCheckUpdateListener onCheckUpdateListener) {
        boolean bl = NetUtils.getInstance().isConnected(GlobalUtil.getContext());
        if (!bl) {
            Toast.makeText(GlobalUtil.getContext(), GlobalUtil.getResources().getString(R.string.net_disconnect), Toast.LENGTH_SHORT).show();
            return;
        }
        if (checkUpdateTask == null) {
            checkUpdateTask = new CheckUpdateTask(onCheckUpdateListener);
            checkUpdateTask.execute();
        } else {
            AsyncTask.Status status = checkUpdateTask.getStatus();
            if (status != AsyncTask.Status.RUNNING) {
                checkUpdateTask = new CheckUpdateTask(onCheckUpdateListener);
                checkUpdateTask.execute();
            }
        }

    }

    /**
     * 开始更新
     */
    public void update(AppInfoBean appInfoBean, OnUpateListener onUpateListener) {
        if (downLoadAppTask == null) {
            downLoadAppTask = new DownLoadAppTask(onUpateListener);
            downLoadAppTask.execute(appInfoBean);
        } else {
            AsyncTask.Status status = downLoadAppTask.getStatus();
            if (status != AsyncTask.Status.RUNNING) {
                downLoadAppTask = new DownLoadAppTask(onUpateListener);
                downLoadAppTask.execute(appInfoBean);
            }
        }
    }

    /**
     * 检测是否是新版本
     *
     * @param info
     * @return
     */
    boolean hasNewAppVersion(AppInfoBean info) {
        int versionCode = -1;
        try {
            PackageManager manager = GlobalUtil.getContext().getPackageManager();
            PackageInfo packageInfo = manager.getPackageInfo(GlobalUtil.getContext().getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
        }
        return (info.getVersion_no() > versionCode && versionCode != -1) ? true : false;
    }

    /**
     * 检测更新的回调
     */
    public interface OnCheckUpdateListener {
        void prepareUpdate(AppInfoBean appInfoBean);

        void onError();
    }

    /**
     * 更新回调接口
     */
    public interface OnUpateListener {
        void downProgress(int progress);

        void onError();

        void onSuccess(String apkFileName);

        void onCancled();
    }


    public void install() {
        if (UpdateAPPConstants.appInfoBean == null)
            return;
        File fileApk = new File(UpdateAPPConstants.UPDATE_FOLDER + UpdateAPPConstants.appInfoBean.getVersion_name() + UpdateAPPConstants.UPDATE_FILE);
        Intent i = new Intent();
        i.setAction(Intent.ACTION_VIEW);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setDataAndType(Uri.fromFile(fileApk), "application/vnd.android.package-archive");
        GlobalUtil.getContext().startActivity(i);
    }

    /**
     * 终止下载操作
     */
    public void stopUpDate() {
        if (downLoadAppTask != null) {
            downLoadAppTask.stopTask();
        }
    }

    /**
     * 已经下载了百分之
     *
     * @return
     */
    public int getHasDownSize() {
        if (UpdateAPPConstants.appInfoBean == null) {
            return 0;
        }

        File fileDir = new File(UpdateAPPConstants.UPDATE_FOLDER);
        File fileApk = new File(UpdateAPPConstants.UPDATE_FOLDER + UpdateAPPConstants.appInfoBean.getVersion_name() + UpdateAPPConstants.UPDATE_FILE);
        if (!fileDir.exists()) {
            return 0;
        }
        if (fileApk.exists()) {//存在说明已经下载过了
            if (MD5Helper.fileToMD5(fileApk).equals(UpdateAPPConstants.appInfoBean.getMd5())) {
                return 100;
            } else {
                return 0;
            }
        } else {//不存在直接下载
            return 0;
        }

    }
}
