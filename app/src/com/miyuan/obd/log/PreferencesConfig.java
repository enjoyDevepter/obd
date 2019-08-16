package com.miyuan.obd.log;

import android.content.Context;

import com.miyuan.adas.GlobalUtil;
import com.miyuan.obd.preferences.SharedPreferencesWrapper;
import com.miyuan.obd.preferences.item.BooleanPreferences;
import com.miyuan.obd.preferences.item.IntPreferences;
import com.miyuan.obd.preferences.item.StringPreferences;


/**
 *
 */
public class PreferencesConfig {
    static final SharedPreferencesWrapper SHARED_PREFERENCES_INIT = new SharedPreferencesWrapper(GlobalUtil.getContext(), "init", Context.MODE_PRIVATE);
    /**
     * 数据存储路径;默认为外置存储卡;
     */
    public static final IntPreferences SDCARD_STATE = new IntPreferences(SHARED_PREFERENCES_INIT, "sdcard_state_key", MapbarStorageUtil.StorageType.OUTSIDE);
    /**
     * 是否上一次选择的存储路径现在不可用了
     */
    private static final BooleanPreferences LAST_CHOOSE_NOT_VALID = new BooleanPreferences(SHARED_PREFERENCES_INIT, "is_last_choosed_storage_not_valid", false);
    private static final BooleanPreferences IS_FIRST_NOT_VALID = new BooleanPreferences(SHARED_PREFERENCES_INIT, "is_first_not_valid_storage_not_valid", true);
    private static final StringPreferences CUSTOM_DATA_PATH = new StringPreferences(SHARED_PREFERENCES_INIT, "custom_data_path", "");
    private static final IntPreferences SETTING_DATA_STORAGE_TYPE_TEMP = new IntPreferences(SHARED_PREFERENCES_INIT, "setting_data_storage_type_temp", -1);

    public static void lastChoosedStorageNotValid() {
        if (IS_FIRST_NOT_VALID.get()) {
            IS_FIRST_NOT_VALID.set(false);
            return;
        }
        LAST_CHOOSE_NOT_VALID.set(true);
    }

    public static String getCustomDataPath() {
        return CUSTOM_DATA_PATH.get();
    }

    public static void setCustomDataPath(String path) {
        CUSTOM_DATA_PATH.set(path);
    }


}
