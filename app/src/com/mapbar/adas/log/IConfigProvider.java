package com.mapbar.adas.log;

import org.json.JSONObject;

/**
 * @author baimi
 */
public interface IConfigProvider {

    IEnumType getType(JSONObject obj, String key);

    String getValue(JSONObject obj, String key);

}
