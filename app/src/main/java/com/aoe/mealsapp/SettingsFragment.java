package com.aoe.mealsapp;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;

import com.aoe.mealsapp.util.Config;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

/**
 * Allows the user to change website language (German/English) and enable/disable the
 * "Order Meal" reminder.
 */
public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "## " + SettingsFragment.class.getSimpleName();

    //
    // EXTENDS PreferenceFragment
    //

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + ": "
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
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onDestroy() called");

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    //
    // IMPLEMENTS SharedPreferences.OnSharedPreferenceChangeListener
    //

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
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

        /* set/cancel alarm & enable/disable boot receiver */

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) { // should not happen
            Log.e(TAG, "handleReminderFrequencyChange: Couldn't get AlarmManager. No alarm set.");
            return;
        }

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class), 0);

        // TODO switch

        if (reminderFrequencyKey.equals(SharedPreferenceKeys.REMINDER_FREQUENCY__BEFORE_EVERY_WEEKDAY)
                || reminderFrequencyKey.equals(SharedPreferenceKeys.REMINDER_FREQUENCY__BEFORE_MONDAY)) {

            Calendar reminderTime;
            try {
                reminderTime = Config.readReminderTime(context);

            } catch (IOException | ParseException e) {
                Log.e(TAG, "handleReminderFrequencyChange: Couln't read reminder time fromconfig file. No alarm set.");
                return;
            }

            /* set daily alarm at reminderTime & enable boot receiver*/

            // TODO if reminder time in past: skip (?)

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

            enableBootReceiver(context);

        } else if (reminderFrequencyKey.equals(SharedPreferenceKeys.REMINDER_FREQUENCY__NEVER)) {
            alarmManager.cancel(alarmIntent);
            disableBootReceiver(context);
        }
    }

    private void enableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager packageManager = context.getPackageManager();

        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void disableBootReceiver(Context context) {
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager packageManager = context.getPackageManager();

        packageManager.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

    private void handleLanguageChange(SharedPreferences sharedPreferences) {

        Context context = getActivity();

        /* set summary text in UI */

        String languageKey = sharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null); // TODO handle null
        String languageText = context.getString(SharedPreferenceKeys.keysToValues.get(languageKey));
        findPreference(SharedPreferenceKeys.LANGUAGE).setSummary(languageText);
    }
}
