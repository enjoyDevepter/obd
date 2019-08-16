package com.miyuan.obd.preferences;

import android.content.Context;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.preferences.item.BooleanPreferences;
import com.miyuan.obd.preferences.item.IntPreferences;
import com.miyuan.obd.preferences.item.LongPreferences;
import com.miyuan.obd.preferences.item.StringPreferences;

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

    public static final StringPreferences PHONE = new StringPreferences(SHARED_PREFERENCES_INIT, "phone", " ");

    public static final StringPreferences CAR = new StringPreferences(SHARED_PREFERENCES_INIT, "car", " ");

    public static final StringPreferences SN = new StringPreferences(SHARED_PREFERENCES_INIT, "sn", "XXXX-XXXX-XXXX-XXXX");

    public static final BooleanPreferences ADJUST_START = new BooleanPreferences(SHARED_PREFERENCES_INIT, "adjust_start", true);

    public static final BooleanPreferences ADJUST_SUCCESS = new BooleanPreferences(SHARED_PREFERENCES_INIT, "adjust_success", false);

    public static final BooleanPreferences TIRE_WARM = new BooleanPreferences(SHARED_PREFERENCES_INIT, "tire_warm", true);

    public static final IntPreferences TIRE_STATUS = new IntPreferences(SHARED_PREFERENCES_INIT, "tire_status", 0);

    public static final BooleanPreferences HUD_GUID = new BooleanPreferences(SHARED_PREFERENCES_INIT, "hud_guid", false);

    public static final BooleanPreferences FM_GUID = new BooleanPreferences(SHARED_PREFERENCES_INIT, "fm_guid", false);

    public static final BooleanPreferences CAMERA_SPEED = new BooleanPreferences(SHARED_PREFERENCES_INIT, "camera_speed", true);

    public static final BooleanPreferences BICYCLE_LANE = new BooleanPreferences(SHARED_PREFERENCES_INIT, "bicycle_lane", false);
    public static final BooleanPreferences SURVEILLANCE_CAMERA = new BooleanPreferences(SHARED_PREFERENCES_INIT, "surveillance_camera", false);
    public static final BooleanPreferences ILLEGAL_PHOTOGRAPHY = new BooleanPreferences(SHARED_PREFERENCES_INIT, "illegal_photography", false);
    public static final BooleanPreferences LIGHT = new BooleanPreferences(SHARED_PREFERENCES_INIT, "light", false);
    public static final BooleanPreferences EMERGENCY = new BooleanPreferences(SHARED_PREFERENCES_INIT, "emergency", false);
    public static final BooleanPreferences BUS = new BooleanPreferences(SHARED_PREFERENCES_INIT, "bus", false);

    public static final IntPreferences RATE_INDEX = new IntPreferences(SHARED_PREFERENCES_INIT, "rate_index", 0);

}