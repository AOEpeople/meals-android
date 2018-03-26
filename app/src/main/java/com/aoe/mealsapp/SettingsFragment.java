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

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

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

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
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

        SharedPreferenceKeys keys = SharedPreferenceKeys.getInstance(getActivity());

        if (key.equals(keys.username)) {
            findPreference(keys.username).setSummary(
                    sharedPreferences.getString(keys.username, null));

        } else if (key.equals(keys.language)) {
            handleLanguageChange(sharedPreferences);

        } else if (key.equals(keys.reminderFrequency)) {
            handleReminderFrequencyChange(sharedPreferences);
        }
    }

    //
    // HELPER
    //

    private void handleLanguageChange(SharedPreferences sharedPreferences) {

        SharedPreferenceKeys keys = SharedPreferenceKeys.getInstance(getActivity());
        Context context = getActivity();

        /* set summary text in UI */

        String languageKey = sharedPreferences.getString(keys.language, keys.language_entryValues_default);
        String languageText = context.getString(keys.getEntry(languageKey));
        findPreference(keys.language).setSummary(languageText);
    }

    private void handleReminderFrequencyChange(SharedPreferences sharedPreferences) {

        SharedPreferenceKeys keys = SharedPreferenceKeys.getInstance(getActivity());
        Context context = getActivity();

        /* set summary text in UI */

        String reminderFrequencyKey = sharedPreferences.getString(keys.reminderFrequency,
                keys.reminderFrequency_entryValues_default);
        String reminderFrequencyText = context.getString(keys.getEntry(reminderFrequencyKey));
        findPreference(keys.reminderFrequency).setSummary(reminderFrequencyText);

        /* set/cancel alarm & enable/disable boot receiver */

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            Log.e(TAG, "handleReminderFrequencyChange: Couldn't get AlarmManager. No alarm set.");
            return;
        }

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0,
                new Intent(context, AlarmReceiver.class), 0);

        if (reminderFrequencyKey.equals(keys.reminderFrequency_entryValues_beforeMonday)
                || reminderFrequencyKey.equals(keys.reminderFrequency_entryValues_beforeEveryWeekday)) {

            Calendar reminderTime = null;
            try {
                reminderTime = readReminderTimeFromConfigFile(context);
            } catch (IOException | ParseException e) {
                Log.e(TAG, "handleReminderFrequencyChange: Couln't read reminder time from" +
                        "config file. No alarm set.");
                return;
            }

            /* set daily alarm at reminderTime & enable boot receiver*/

            // TODO if reminder time in past: skip (?)

            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);

            enableBootReceiver(context);

        } else if (reminderFrequencyKey.equals(keys.reminderFrequency_entryValues_never)) {
            alarmManager.cancel(alarmIntent);
            disableBootReceiver(context);
        }
    }

    private Calendar readReminderTimeFromConfigFile(Context context) throws IOException, ParseException {

        Properties properties = new Properties();
        properties.load(context.getResources().openRawResource(R.raw.config));

        /* create Calendar object with today's date and config's time */

        Calendar parsedTime = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        parsedTime.setTime(dateFormat.parse(properties.getProperty(PropertiesKeys.REMINDER_TIME)));

        Calendar reminderTime = Calendar.getInstance();
        reminderTime.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY));
        reminderTime.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE));
        reminderTime.set(Calendar.SECOND, parsedTime.get(Calendar.SECOND));
        reminderTime.set(Calendar.MILLISECOND, parsedTime.get(Calendar.MILLISECOND));

        return reminderTime;
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
}
