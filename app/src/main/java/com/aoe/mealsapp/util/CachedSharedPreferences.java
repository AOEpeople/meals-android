package com.aoe.mealsapp.util;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

/**
 * WIP !!!
 */
public class CachedSharedPreferences {

    private SharedPreferences sharedPreferences;
    private Map<String, String> cache = new HashMap<>();
    private OnCachedSharedPreferenceChangedListener listener;

    public CachedSharedPreferences(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    public void setSharedPreferencesChangedListener(OnCachedSharedPreferenceChangedListener listener) {
        this.listener = listener;
    }

    public String get(String key) {
        return cache.get(key);
    }

    public void put(String key, String value) {
        cache.put(key, value);
    }

    public void update() {
        for (String key : cache.keySet()) {
            update(key);
        }
    }

    /**
     * Reads the value for the specified key from the SharedPreferences and compares it with the
     * cached one. If it hasn't been read before it's compared with null. Finally, caches the
     * current value.
     *
     * @return True if the value read from the SharedPreferences has changed since the last time.
     * False otherwise.
     *
     * The current value and the cached one are considered to be equal if String.equals() yields
     * true or if both are null.
     */
    private void update(String key) {
        String currentValue = sharedPreferences.getString(key, null);
        String cachedValue = cache.get(key);

        boolean valueChanged = !(currentValue == null ? cachedValue == null : currentValue.equals(cachedValue));

        cache.put(key, cachedValue);

        if (valueChanged && listener != null) {
            listener.onCachedSharedPreferenceChanged(key);
        }
    }

    interface OnCachedSharedPreferenceChangedListener {
        void onCachedSharedPreferenceChanged(String key);
    }
}
