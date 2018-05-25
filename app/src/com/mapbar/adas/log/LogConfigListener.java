package com.mapbar.adas.log;

import com.mapbar.adas.log.IEConfig.ConfigEventInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author baimi
 */
public class LogConfigListener implements Listener.GenericListener<ConfigEventInfo> {

    @Override
    public void onEvent(IEConfig.ConfigEventInfo eventInfo) {
        LogManager manager = LogManager.getInstance();
        // 内部配置
        JSONObject internal = eventInfo.getInternal();
        if (null != internal) {
            try {
                manager.jsonToLogManager(internal.getJSONObject("logConfig"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        JSONObject external = eventInfo.getExternal();
        // 外部配置
        if (null != external) {
            try {
                manager.jsonToLogManager(external.getJSONObject("logConfig"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }
}
