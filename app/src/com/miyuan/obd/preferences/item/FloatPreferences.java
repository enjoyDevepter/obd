package com.miyuan.obd.preferences.item;

import com.miyuan.obd.preferences.SharedPreferencesWrapper;

/**
 * @author guomin
 */
public class FloatPreferences extends BasePreferences {

    private float defaultValue;

    public FloatPreferences(SharedPreferencesWrapper sharedPreferencesWrapper, String sharedPreferencesKey, float sharedPreferencesDefaultValue) {
        super(sharedPreferencesWrapper, sharedPreferencesKey);
        defaultValue = sharedPreferencesDefaultValue;
    }

    public void set(float value) {
        getSharedPreferences().edit().putFloat(getSharedPreferencesKey(), value).commit();
    }

    public float get() {
        return getSharedPreferences().getFloat(getSharedPreferencesKey(), getDefaultValue());
    }

    public float getDefaultValue() {
        return defaultValue;
    }
}
