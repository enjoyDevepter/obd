package com.miyuan.adas;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import androidx.annotation.ColorRes;
import androidx.appcompat.app.AppCompatActivity;

import com.gyf.immersionbar.ImmersionBar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.OkHttpClient;

public class GlobalUtil {

    private static final String PASS = "eBAHg0PXVkt8DxGT";

    private static Handler handler;

    private static Context context;

    private static AppCompatActivity mainActivity;

    private static int sUID;

    private static String resPackageName;

    private static OkHttpClient okHttpClient;

    public static Context getContext() {
        return GlobalUtil.context;
    }

    public static void setContext(Context context) {
        GlobalUtil.context = context;
    }

    public static OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    public static void setOkHttpClient(OkHttpClient okHttpClient) {
        GlobalUtil.okHttpClient = okHttpClient;
    }

    public static Resources getResources() {
        Context context = getMainActivity();
        if (context == null) {
            context = getContext();
        }
        return context.getResources();
    }

    public static Handler getHandler() {
        return GlobalUtil.handler;
    }

    public static void setHandler(Handler handler) {
        GlobalUtil.handler = handler;
    }


    public static AppCompatActivity getMainActivity() {
        return mainActivity;
    }


    public static void setMainActivity(AppCompatActivity mainActivity) {
        GlobalUtil.mainActivity = mainActivity;
    }

    public static String getFromAssets(Context context, String fileName) throws IOException {
        BufferedReader bufReader = new BufferedReader(new InputStreamReader(context.getResources().getAssets().open(fileName)));
        String line = "";
        StringBuilder result = new StringBuilder();
        while (null != (line = bufReader.readLine())) {
            result.append(line);
        }
        return result.toString();
    }

    public static String getResPackageName() {
        return resPackageName;
    }

    /**
     * 确保该包名同AndroidManifest中的package一致
     */
    public static void setResPackageName(String resPackageName) {
        GlobalUtil.resPackageName = resPackageName;
    }

    /**
     * 判断当前线程是否非UI线程
     *
     * @return
     */
    public static boolean isNotUIThread() {
        return Looper.myLooper() != Looper.getMainLooper();
    }


    public static void changeBarColor(@ColorRes int colorId) {
        ImmersionBar.with(GlobalUtil.getMainActivity())
                .fitsSystemWindows(true)
                .transparentStatusBar()       // 透明状态栏
                .statusBarDarkFont(true)      // 状态栏图标深色模式
                .navigationBarColor(colorId) // 导航栏颜色
                .init();
    }

    public static boolean isPhone(String phone) {
        if (phone.length() != 11) {
            return false;
        }
        Pattern p = Pattern.compile("[1][0-9]\\d{9}");
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    public static boolean isEmpty(String str) {
        if (null == str || "".equals(str.trim())) {
            return true;
        }
        return false;
    }

    /**
     * 对给定的字符串进行base64加密操作
     *
     * @param inputData
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String encodeData(String inputData) throws UnsupportedEncodingException {
        if (null == inputData) {
            return null;
        }
        String string = Base64.encodeToString(inputData.getBytes(), Base64.DEFAULT);
        return string;
    }

    /**
     * 加密
     *
     * @param content 需要加密的内容
     * @return
     * @since v1.0
     */
    public static String encrypt(String content) {
        try {
            IvParameterSpec ips = new IvParameterSpec(PASS.getBytes());
            SecretKeySpec sks = new SecretKeySpec(PASS.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, sks, ips);
            byte[] encryptedData = cipher.doFinal(content.getBytes());

            String res = parseByte2HexStr(encryptedData);
            System.out.println("res:" + res);
            return encodeData(res); // 加密
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf
     * @return
     * @since v1.0
     */
    public static String parseByte2HexStr(byte buf[]) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < buf.length; i++) {
            String hex = Integer.toHexString(buf[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

}
