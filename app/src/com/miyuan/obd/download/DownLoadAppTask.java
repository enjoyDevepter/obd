package com.miyuan.obd.download;

import android.os.AsyncTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.text.DecimalFormat;

/**
 * Created by shisk on 2017/7/19.
 */

class DownLoadAppTask extends AsyncTask<AppInfoBean, Integer, String> {
    private DecimalFormat df = new java.text.DecimalFormat("#.00");

    public DownLoadAppTask(UpdateDownLoadManager.OnUpateListener onUpateListener) {
        this.onUpateListener = onUpateListener;
    }

    private UpdateDownLoadManager.OnUpateListener onUpateListener;

    boolean interrupt = false;

    @Override
    protected String doInBackground(AppInfoBean... params) {
        AppInfoBean appInfoBean = params[0];
        try {
            File fileDir = new File(UpdateAPPConstants.UPDATE_FOLDER);
            File fileApk = new File(UpdateAPPConstants.UPDATE_FOLDER + params[0].getVersion_name() + UpdateAPPConstants.UPDATE_FILE);
            if (!fileDir.exists()) {
                fileDir.mkdirs();
            }
            if (fileApk.exists()) {//存在说明已经下载过了
                long filelen = fileApk.length();
                if (MD5Helper.fileToMD5(fileApk).equals(appInfoBean.getMd5())) {//说明已经下载完成
                    onUpateListener.onSuccess(fileApk.getAbsolutePath());
                } else {// 没下载完成继续下载
                    down(appInfoBean, fileApk.getAbsolutePath(), filelen);
                }

            } else {//不存在直接下载
                fileApk.createNewFile();
                down(appInfoBean, fileApk.getAbsolutePath(), 0);
            }
            return fileApk.getAbsolutePath();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param fileApk
     * @param range
     */
    private void down(AppInfoBean appInfoBean, String fileApk, long range) {
        URL url = null;
        try {
            url = new URL(appInfoBean.getApk_path());
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            //"RANGE",
            urlConnection.setRequestProperty("RANGE", "bytes=" + range + "-");
            urlConnection.setConnectTimeout(5000);
            InputStream input = urlConnection.getInputStream();
            int code = urlConnection.getResponseCode();

            if (code != 206) {
                onUpateListener.onError();
                return;
            }
            byte[] buffer = new byte[1024];

            RandomAccessFile raf = new RandomAccessFile(fileApk, "rwd");
            //FileOutputStream fileOutputStream = new FileOutputStream(fileApk);
            raf.seek(range);
            long count = appInfoBean.getByteSize();
            int len = 0;
            float total = range;

            while ((len = input.read(buffer)) != -1 && !interrupt) {
                raf.write(buffer, 0, len);
                total += len;
                onProgressUpdate((int) (Double.valueOf(df.format(total / count)) * 100));
            }
            if (onUpateListener != null && !interrupt && MD5Helper.fileToMD5(new File(fileApk)).equals(appInfoBean.getMd5())) {
                onUpateListener.onSuccess(fileApk);
            }
            raf.close();
            input.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            onUpateListener.onError();
        } catch (ProtocolException e) {
            e.printStackTrace();
            onUpateListener.onError();
        } catch (IOException e) {
            e.printStackTrace();
            onUpateListener.onError();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (onUpateListener != null)
            onUpateListener.downProgress(values[0]);
    }

    @Override
    protected void onPostExecute(String o) {
        super.onPostExecute(o);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        interrupt = true;
        if (onUpateListener != null)
            onUpateListener.onCancled();
    }

    void stopTask() {
        interrupt = true;
    }

    @Override
    protected void onCancelled(String s) {
        super.onCancelled(s);
    }
}
