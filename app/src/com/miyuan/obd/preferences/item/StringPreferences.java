package com.miyuan.obd.preferences.item;

import com.miyuan.obd.preferences.SharedPreferencesWrapper;

/**
 * @author guomin
 */
public class StringPreferences extends BasePreferences {

    private String defaultValue;

    public StringPreferences(SharedPreferencesWrapper sharedPreferencesWrapper, String sharedPreferencesKey, String sharedPreferencesDefaultValue) {
        super(sharedPreferencesWrapper, sharedPreferencesKey);
        defaultValue = sharedPreferencesDefaultValue;
    }

    public void set(String value) {
        getSharedPreferences().edit().putString(getSharedPreferencesKey(), value).commit();
    }

    public String get() {
        return getSharedPreferences().getString(getSharedPreferencesKey(), getDefaultValue());
    }

    public String getDefaultValue() {
        return defaultValue;
    }
}
