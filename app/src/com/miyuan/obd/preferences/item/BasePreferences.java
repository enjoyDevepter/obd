/**
 *
 */
package com.miyuan.obd.preferences.item;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;

import com.miyuan.obd.log.Listener;
import com.miyuan.obd.log.WeakSuccinctListeners;
import com.miyuan.obd.preferences.SharedPreferencesWrapper;

/**
 *
 */
public class BasePreferences {

    private SharedPreferencesWrapper sharedPreferencesWrapper;
    private String sharedPreferencesKey;
    private SharedPreferenceChangeListener sharedPreferenceChangeListener;

    BasePreferences(SharedPreferencesWrapper sharedPreferencesWrapper, String sharedPreferencesKey) {
        this.sharedPreferencesWrapper = sharedPreferencesWrapper;
        this.sharedPreferencesKey = sharedPreferencesKey;
    }

    public void remove() {
        getSharedPreferences().edit().remove(sharedPreferencesKey).commit();
    }

    public boolean contains() {
        return getSharedPreferences().contains(sharedPreferencesKey);
    }

    /**
     * @return the {@link #sharedPreferencesKey}
     */
    String getSharedPreferencesKey() {
        return sharedPreferencesKey;
    }

    /**
     * @return
     */
    SharedPreferencesWrapper getSharedPreferences() {
        return sharedPreferencesWrapper;
    }

    /**
     * @param listener
     */
    public void addListener(Listener.SuccinctListener listener) {
        if (null == sharedPreferenceChangeListener) {
            sharedPreferenceChangeListener = new SharedPreferenceChangeListener(getSharedPreferences(), sharedPreferencesKey);
        }
        sharedPreferenceChangeListener.add(listener);
    }

    /**
     *
     */
    private static final class SharedPreferenceChangeListener implements OnSharedPreferenceChangeListener {

        private String sharedPreferencesKey;
        private WeakSuccinctListeners listeners = new WeakSuccinctListeners();

        /**
         *
         */
        private SharedPreferenceChangeListener(SharedPreferencesWrapper sharedPreferencesWrapper, String sharedPreferencesKey) {
            sharedPreferencesWrapper.registerOnSharedPreferenceChangeListener(this);
            this.sharedPreferencesKey = sharedPreferencesKey;
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(sharedPreferencesKey)) {
                listeners.conveyEvent();
            }
        }

        /**
         * @param listener
         */
        public void add(Listener.SuccinctListener listener) {
            listeners.add(listener);
        }

    }

}
