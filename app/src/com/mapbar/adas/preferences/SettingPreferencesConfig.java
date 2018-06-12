package com.mapbar.adas.preferences;

import android.content.Context;

import com.mapbar.adas.GlobalUtil;
import com.mapbar.adas.preferences.item.BooleanPreferences;
import com.mapbar.adas.preferences.item.LongPreferences;
import com.mapbar.adas.preferences.item.StringPreferences;

/**
 * 全局设置相关参数
 */
public class SettingPreferencesConfig {
    static final SharedPreferencesWrapper SHARED_PREFERENCES_INIT = new SharedPreferencesWrapper(GlobalUtil.getContext(), "adas_setting", Context.MODE_PRIVATE);
    /**
     * 免责声明 是否提醒
     */
    public static final BooleanPreferences DISCALIMER_VISIBLE = new BooleanPreferences(SHARED_PREFERENCES_INIT, "calibration_dis", false);

    public static final LongPreferences UPDATE_ID = new LongPreferences(SHARED_PREFERENCES_INIT, "update_id", -1l);

    public static final StringPreferences PHONE = new StringPreferences(SHARED_PREFERENCES_INIT, "phone", "");

    public static final StringPreferences CAR = new StringPreferences(SHARED_PREFERENCES_INIT, "car", "");
}