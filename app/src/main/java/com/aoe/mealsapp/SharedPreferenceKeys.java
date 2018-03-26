package com.aoe.mealsapp;

import android.content.Context;
import android.support.annotation.StringRes;

import java.util.HashMap;
import java.util.Map;

class SharedPreferenceKeys {
    private static SharedPreferenceKeys ourInstance;

    private Map<String, Integer> entryValuesToEntries = new HashMap<>();

    static SharedPreferenceKeys getInstance(Context context) {
        if (ourInstance == null) {
            ourInstance = new SharedPreferenceKeys(context);
        }

        return ourInstance;
    }

    final String credentialsWereValidated;
    final String language;
    final String language_entryValues_english;
    final String language_entryValues_german;
    final String language_entryValues_default;
    final String password;
    final String reminderFrequency;
    final String reminderFrequency_entryValues_beforeEveryWeekday;
    final String reminderFrequency_entryValues_beforeMonday;
    final String reminderFrequency_entryValues_never;
    final String reminderFrequency_entryValues_default;
    final String username;

    private SharedPreferenceKeys(Context context) {
        credentialsWereValidated = context.getString(R.string.preferences_credentialsWereValidated);
        language = context.getString(R.string.preferences_language);
        language_entryValues_english = context.getString(R.string.preferences_language_entryValues_english);
        language_entryValues_german = context.getString(R.string.preferences_language_entryValues_german);
        language_entryValues_default = context.getString(R.string.preferences_language_entryValues_default);
        password = context.getString(R.string.preferences_password);
        reminderFrequency = context.getString(R.string.preferences_reminderFrequency);
        reminderFrequency_entryValues_beforeEveryWeekday = context.getString(R.string.preferences_reminderFrequency_entryValues_beforeEveryWeekday);
        reminderFrequency_entryValues_beforeMonday = context.getString(R.string.preferences_reminderFrequency_entryValues_beforeMonday);
        reminderFrequency_entryValues_never = context.getString(R.string.preferences_reminderFrequency_entryValues_never);
        reminderFrequency_entryValues_default = context.getString(R.string.preferences_reminderFrequency_entryValues_default);
        username = context.getString(R.string.preferences_username);

        entryValuesToEntries.put(language_entryValues_english, R.string.preferences_language_entries_english);
        entryValuesToEntries.put(language_entryValues_german, R.string.preferences_language_entries_german);
        entryValuesToEntries.put(reminderFrequency_entryValues_beforeEveryWeekday, R.string.preferences_reminderFrequency_entries_beforeEveryWeekday);
        entryValuesToEntries.put(reminderFrequency_entryValues_beforeMonday, R.string.preferences_reminderFrequency_entries_beforeMonday);
        entryValuesToEntries.put(reminderFrequency_entryValues_never, R.string.preferences_reminderFrequency_entries_never);
    }

    @StringRes
    public int getEntry(String entryValue) {
        return entryValuesToEntries.get(entryValue);
    }
}
