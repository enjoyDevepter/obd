package com.mapbar.adas.download;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.mapbar.adas.GlobalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by shisk on 2017/7/19.
 */

class CheckUpdateTask extends AsyncTask<String, Integer, AppInfoBean> {
    private UpdateDownLoadManager.OnCheckUpdateListener onCheckUpdateListener;

    public CheckUpdateTask(UpdateDownLoadManager.OnCheckUpdateListener onCheckUpdateListener) {
        this.onCheckUpdateListener = onCheckUpdateListener;
    }

    @Override
    protected AppInfoBean doInBackground(String... params) {
        PackageInfo applicationInfo = null;
        try {
            applicationInfo = GlobalUtil.getContext().getPackageManager().getPackageInfo(GlobalUtil.getContext().getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            String strUrl = UpdateAPPConstants.APPUPDATE_URL + "?ck=" + UpdateAPPConstants.CK + "&package_name=" + GlobalUtil.getContext().getPackageName();
            // String strUrl = UpdateAPPConstants.APPUPDATE_URL + "?ck=" + UpdateAPPConstants.CK + "&package_name=" + applicationInfo.packageName;
            URL url = new URL(strUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setDoOutput(true);
            urlConnection.setDoInput(true);
            urlConnection.setUseCaches(false);

            InputStream is = urlConnection.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while (-1 != (len = is.read(buffer))) {
                baos.write(buffer, 0, len);
                baos.flush();
            }

            //byte[] buffer = new byte[input.available()];
            //input.read(buffer,0,input.available());
            String jsonStr = baos.toString("utf-8");
            try {
                JSONObject jsonObj = new JSONObject(jsonStr);
                if (jsonObj.getInt("status") != 200) {
                    return null;
                }
                JSONArray jsonArray = jsonObj.getJSONArray("data");
                JSONObject json = jsonArray.getJSONObject(0);
                Gson gsonFormat = new Gson();
                return gsonFormat.fromJson(json.toString(), AppInfoBean.class);
            } catch (JSONException e) {
                e.printStackTrace();
                onCheckUpdateListener.onError();
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            onCheckUpdateListener.onError();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            onCheckUpdateListener.onError();
            return null;
        }
    }

    /**
     * task 结束回调
     *
     * @param appInfoBean
     */
    @Override
    protected void onPostExecute(AppInfoBean appInfoBean) {
        super.onPostExecute(appInfoBean);
        if (appInfoBean == null) {             //下载完成
            onCheckUpdateListener.onError();
            return;
        }
        boolean bl = UpdateDownLoadManager.getInstance().hasNewAppVersion(appInfoBean);
        if (bl) {
            onCheckUpdateListener.prepareUpdate(appInfoBean);
        } else {
            onCheckUpdateListener.prepareUpdate(null);
        }

    }

    /**
     * task 执行中的回调
     *
     * @param values
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }


}
