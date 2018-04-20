package com.aoe.mealsapp;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Allows the user to change website language (German/English) and enable/disable the
 * "Order Meal" reminder.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    //
    // EXTENDS PreferenceFragment
    //

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        addPreferencesFromResource(R.xml.preferences);

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        /* set summary fields */

        String username = sharedPreferences.getString(SharedPreferenceKeys.USERNAME, null);
        String reminderFrequencyKey = sharedPreferences.getString(SharedPreferenceKeys.REMINDER_FREQUENCY,null);
        String languageKey = sharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null);

        // TODO handle null

        findPreference(SharedPreferenceKeys.USERNAME).setSummary(username);
        findPreference(SharedPreferenceKeys.REMINDER_FREQUENCY).setSummary(
                getActivity().getString(SharedPreferenceKeys.keysToValues.get(reminderFrequencyKey)));
        findPreference(SharedPreferenceKeys.LANGUAGE).setSummary(
                getActivity().getString(SharedPreferenceKeys.keysToValues.get(languageKey)));

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onDestroy() called");

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    //
    // IMPLEMENTS SharedPreferences.OnSharedPreferenceChangeListener
    //

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onSharedPreferenceChanged() called with: sharedPreferences = [" + sharedPreferences
                + "], key = [" + key + "]");

        switch (key) {
            case SharedPreferenceKeys.USERNAME:
                findPreference(SharedPreferenceKeys.USERNAME).setSummary(
                        sharedPreferences.getString(SharedPreferenceKeys.USERNAME, null));
                break;

            case SharedPreferenceKeys.REMINDER_FREQUENCY:
                handleReminderFrequencyChange(sharedPreferences);
                break;

            case SharedPreferenceKeys.LANGUAGE:
                handleLanguageChange(sharedPreferences);
                break;
        }
    }

    //
    // HELPER
    //

    private void handleReminderFrequencyChange(SharedPreferences sharedPreferences) {

        Context context = getActivity();

        /* set summary text in UI */

        String reminderFrequencyKey = sharedPreferences.getString(SharedPreferenceKeys.REMINDER_FREQUENCY, null); // TODO handle null
        String reminderFrequencyText = context.getString(SharedPreferenceKeys.keysToValues.get(reminderFrequencyKey));
        findPreference(SharedPreferenceKeys.REMINDER_FREQUENCY).setSummary(reminderFrequencyText);
    }

    private void handleLanguageChange(SharedPreferences sharedPreferences) {

        Context context = getActivity();

        /* set summary text in UI */

        String languageKey = sharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null); // TODO handle null
        String languageText = context.getString(SharedPreferenceKeys.keysToValues.get(languageKey));
        findPreference(SharedPreferenceKeys.LANGUAGE).setSummary(languageText);
    }
}
