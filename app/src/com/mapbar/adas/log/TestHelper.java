package com.mapbar.adas.log;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author baimi
 */
public class TestHelper {

    private boolean test;
    private JSONObject configJson;
    private IConfigProvider provider;
    private Map<IEnumType, String> replaceURLs = new HashMap<IEnumType, String>();
    private JSONArray jsonArray;

    /**
     * 禁止构造
     */
    private TestHelper() {
    }

    /**
     * 获得单例
     */
    public static TestHelper getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public void setConfigJson(JSONObject configJson) {
        this.configJson = configJson;
    }

    public void setConfigProvider(IConfigProvider provider) {
        this.provider = provider;
    }

    /**
     * 是否测试环境
     *
     * @return
     */
    public boolean isTest() {
        if (configJson == null) {
            return false;
        }
        final String tester = configJson.optString(IEConfig.TESTER);
        test = !IEConfig.TESTER_NO.equals(tester);
        return test;
    }

    /**
     * 初始化进行替换的URI
     */
    public void initReplaceUri() {
        if (null == configJson) {
            return;
        }
        if (isTest()) {
            JSONArray array = configJson.optJSONArray("replace");
            if (null == array) {
                return;
            }
            JSONObject obj = null;
            for (int i = 0; i < array.length(); i++) {
                try {
                    obj = array.getJSONObject(i);
                    IEnumType type = provider.getType(obj, "key");
                    String url = obj.getString("val");
                    if ((null == type) || (null == url)) {
                        continue;
                    }
                    replaceURLs.put(type, url);
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    /**
     * 通过配置文件获取存储积分数据的dir
     * by zhangtj
     *
     * @return 是否存储SD卡
     */
    public boolean isStoreIntegral2SDCard() {
        boolean result = false;
        if (null != configJson && configJson.has("isStoreIntegral2SDCard")) {
            try {
                result = configJson.getBoolean("isStoreIntegral2SDCard");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String changeURL(String requestURL) {
        String result = null;
        if (jsonArray == null) {
            return null;
        }
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jo = jsonArray.getJSONObject(i);
                JSONObject ruleObj = jo.getJSONObject("rule");
                String ruleUrl = ruleObj.getString("url");
                if (requestURL.startsWith(ruleUrl)) {
                    JSONObject actionObj = jo.getJSONObject("action");
                    String actionUrl = actionObj.optString("url");
                    if (!TextUtils.isEmpty(actionUrl)) {
                        result = requestURL.replace(ruleUrl, actionUrl);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return result;
    }


//    /**
//     * 设置请求Listener
//     */
//    public void setRequestListener() {
//        if (null != configJson) {
//            jsonArray = configJson.optJSONArray("netReplace");
//            if (null != jsonArray) {
//                HttpHandler.setRequestListener(new IRequestListener() {
//
//                    @Override
//                    public void setTestEnvironment(IHttpHandler handler) {
//                        for (int i = 0; i < jsonArray.length(); i++) {
//                            try {
//                                JSONObject jo = jsonArray.getJSONObject(i);
//                                JSONObject ruleObj = jo.getJSONObject("rule");
//                                String ruleUrl = ruleObj.getString("url");
//                                final String requestUrl = handler.getRequestUrl();
//                                // 内外网验证
////                                try {
////                                    URL url = new URL(requestUrl);
////                                    final String host = url.getHost();
////                                    Pattern p = Pattern.compile("^(0|[1-9]?|1\\d\\d?|2[0-4]\\d|25[0-5])\\.(0|[1-9]?|1\\d\\d?|2[0-4]\\d|25[0-5])\\.(0|[1-9]?|1\\d\\d?|2[0-4]\\d|25[0-5])\\.(0|[1-9]?|1\\d\\d?|2[0-4]\\d|25[0-5])$");
////                                    Matcher matcher = p.matcher(host);
////                                    final boolean matches = matcher.matches();
////                                    // 日志
////                                    if (Log.isLoggable(LogTag.HTTP_NET, Log.INFO)) {
////                                        StringBuilder sb = new StringBuilder().append(" -->> ") //
////                                                .append(", matches = ").append(matches) //
////                                                .append(", requestUrl = ").append(requestUrl) //
////                                                ;
////                                        Log.i(LogTag.HTTP_NET, sb.toString());
////                                    }
////                                    if (matches) {
////                                        GlobalUtil.getHandler().post(new Runnable() {
////                                            @Override
////                                            public void run() {
////                                                throw new RuntimeException("请使用外网地址");
////                                            }
////                                        });
////                                    }
////                                } catch (MalformedURLException e) {
////                                    e.printStackTrace();
////                                }
//                                if (requestUrl.startsWith(ruleUrl)) {
//                                    JSONArray ruleParameters = ruleObj.optJSONArray("parameter");
//                                    JSONObject actionObj = jo.getJSONObject("action");
//                                    String actionUrl = actionObj.optString("url");
//                                    // actionUrl不为空,表明要替换url
//                                    if (!TextUtils.isEmpty(actionUrl)) {
//                                        handler.setRequestUrl(requestUrl.replace(ruleUrl, actionUrl));
//                                    }
//
//                                    if (null != ruleParameters) {
//                                        /* 参数匹配规则
//                                         * 1.存在就替换值 (rule中val为空则替换值,不为空需要val匹配上才替换)
//                                         * 2.不存在则追加为新的参数 (rule中配置该项即可)
//                                         * 3.rule和action中val都为空则移除
//                                         */
//                                        JSONArray actionParameters = actionObj.optJSONArray("parameter");
//                                        if (handler.getHttpMethod().equals(HttpRequestType.POST.name())) {
//                                            Map<String, String> postParams = handler.getPostParams();
//                                            executeParameter(ruleParameters, actionParameters, postParams, handler.getHttpMethod());
//                                        } else if (handler.getHttpMethod().equals(HttpRequestType.GET.name())) {
//                                            int index = -1;
//                                            String originUrl = "", paramVal = "";
//                                            Map<String, String> postParams = new HashMap<String, String>();
//                                            if ((index = requestUrl.indexOf("?")) < 0) {
//                                                originUrl = requestUrl;
//                                            } else {
//                                                originUrl = requestUrl.substring(0, index);
//                                                paramVal = requestUrl.replace(originUrl + "?", "");
//                                                String[] pairArr = paramVal.split("&");
//                                                for (String param : pairArr) {
//                                                    if (param.contains("=")) {
//                                                        String[] paramArr = param.split("=");
//                                                        postParams.put(paramArr[0], paramArr[1]);
//                                                    }
//                                                }
//                                            }
//                                            executeParameter(ruleParameters, actionParameters, postParams, handler.getHttpMethod());
//                                            StringBuilder sBuilder = new StringBuilder();
//                                            for (Map.Entry<String, String> entry : postParams.entrySet()) {
//                                                sBuilder.append(entry.getKey() + "=" + entry.getValue() + "&");
//                                            }
//                                            paramVal = sBuilder.toString();
//                                            handler.setRequestUrl(originUrl + "?" + paramVal.substring(0, paramVal.lastIndexOf("&")));
//                                        }
//                                    }
//                                    break;
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                                continue;
//                            }
//                        }
//                    }
//                });
//            }
//        }
//    }

//    private void executeParameter(JSONArray ruleParameters, JSONArray actionParameters, Map<String, String> postParams, String method) throws JSONException {
//        for (int m = 0; m < ruleParameters.length(); m++) {
//            JSONObject ruleParaJsonObj = ruleParameters.getJSONObject(m);
//            String ruleKey = ruleParaJsonObj.optString("key");
//            if (postParams.containsKey(ruleKey)) {
//                String ruleVal = ruleParaJsonObj.optString("val");
//                if (!TextUtils.isEmpty(ruleVal)) {    // rule中不为空需要val匹配上才替换
//                    if (ruleVal.equals(postParams.get(ruleKey))) {
//                        if (null != actionParameters) {
//                            for (int n = 0; n < actionParameters.length(); n++) {
//                                JSONObject actionParaJsonObj = actionParameters.getJSONObject(n);
//                                String actionKey = actionParaJsonObj.optString("key");
//                                if (ruleKey.equals(actionKey)) {
//                                    String actionVal = actionParaJsonObj.optString("val");
//                                    if (!TextUtils.isEmpty(actionVal)) {
//                                        // 日志
//                                        if (Log.isLoggable(LogTag.HTTP_NET, Log.INFO)) {
//                                            Log.i(LogTag.HTTP_NET, method + " -->> Parameter替换前" + actionKey + "=" + ruleVal + ",替换后" + actionKey + "=" + actionVal);
//                                        }
//                                        postParams.put(actionKey, actionVal);
//                                    } else {
//                                        // 日志
//                                        if (Log.isLoggable(LogTag.HTTP_NET, Log.INFO)) {
//                                            Log.i(LogTag.HTTP_NET, method + " -->> Parameter移除" + actionKey + "=" + ruleVal);
//                                        }
//                                        postParams.remove(actionKey);
//                                    }
//                                    break;
//                                }
//                            }
//                        }
//                    }
//                } else {    // rule中val为空则替换值
//                    if (null != actionParameters) {
//                        for (int n = 0; n < actionParameters.length(); n++) {
//                            JSONObject actionParaJsonObj = actionParameters.getJSONObject(n);
//                            String actionKey = actionParaJsonObj.optString("key");
//                            if (ruleKey.equals(actionKey)) {
//                                String actionVal = actionParaJsonObj.optString("val");
//                                if (!TextUtils.isEmpty(actionVal)) {
//                                    // 日志
//                                    if (Log.isLoggable(LogTag.HTTP_NET, Log.INFO)) {
//                                        Log.i(LogTag.HTTP_NET, method + " -->> Parameter替换前" + actionKey + "=" + postParams.get(ruleKey) + ",替换后" + actionKey + "=" + actionVal);
//                                    }
//                                    postParams.put(ruleKey, actionParaJsonObj.optString("val"));
//                                } else {
//                                    // 日志
//                                    if (Log.isLoggable(LogTag.HTTP_NET, Log.INFO)) {
//                                        Log.i(LogTag.HTTP_NET, method + " -->> Parameter移除" + actionKey + "=" + postParams.get(ruleKey));
//                                    }
//                                    postParams.remove(ruleKey);
//                                }
//                                break;
//                            }
//                        }
//                    }
//                }
//            } else { // postParams不存在该key, 追加新的参数
//                String ruleval = ruleParaJsonObj.optString("val");
//                if (!TextUtils.isEmpty(ruleval)) {
//                    // 日志
//                    if (Log.isLoggable(LogTag.HTTP_NET, Log.INFO)) {
//                        Log.i(LogTag.HTTP_NET, method + " -->> Parameter新增" + ruleKey + "=" + ruleval);
//                    }
//                    postParams.put(ruleKey, ruleval);
//                }
//            }
//        }
//    }

    /**
     * 显示测试相关信息提示(一般用于启动时)
     *
     * @param context
     */
    public void showTip(Context context) {
        if (isTest()) {
            String tester = null;
            String datetime = null;
            try {
                tester = configJson.getString(IEConfig.TESTER);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String sourceDir = context.getApplicationInfo().sourceDir;
            if (!TextUtils.isEmpty(sourceDir)) {
                File file = new File(sourceDir);
                if (file.isFile()) {
                    long lastModified = file.lastModified();
                    if (lastModified > 0) {
                        datetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(lastModified));
                    }
                }
            }
            Toast.makeText(context, "该版本由" + tester + ",\n于" + datetime + "编译", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * @param key
     * @return
     * @see java.util.Map#get(java.lang.Object)
     */
    public String getReplaceURL(IEnumType key) {
        return replaceURLs.get(key);
    }

    public String getTestValue(IEnumType key, String defaultValue) {
        String v = getReplaceURL(key);
        if (TextUtils.isEmpty(v)) {
            return defaultValue;
        } else {
            return v;
        }
    }

    /**
     * 单例持有器
     */
    private static final class InstanceHolder {
        private static final TestHelper INSTANCE = new TestHelper();
    }
}
