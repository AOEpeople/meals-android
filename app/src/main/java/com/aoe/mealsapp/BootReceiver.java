package com.aoe.mealsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "## " + BootReceiver.class.getSimpleName();

    /**
     * Called if BootReceiver has been enabled. Checks received intent for security reasons,
     * reads reminder time from config.properties and sets alarm at reminder time which
     * repeats daily.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            /* read reminderTime from config.properties */

            Calendar reminderTime;
            try {
                Properties properties = new Properties();
                properties.load(context.getResources().openRawResource(R.raw.config));

                /* create calendar with today's date and config time */

                Calendar parsedTime = Calendar.getInstance();
                DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
                parsedTime.setTime(dateFormat.parse(properties.getProperty(PropertiesKeys.REMINDER_TIME)));

                reminderTime = Calendar.getInstance();
                reminderTime.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY));
                reminderTime.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE));
                reminderTime.set(Calendar.SECOND, parsedTime.get(Calendar.SECOND));
                reminderTime.set(Calendar.MILLISECOND, parsedTime.get(Calendar.MILLISECOND));

            } catch (IOException e) {
                Log.e(TAG, "onReceive: IOException. No alarm set.", e);
                return;
            } catch (ParseException e) {
                Log.e(TAG, "onReceive: ParseException. No alarm set.", e);
                return;
            }

            /* set daily alarm at reminder time */

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager == null) {
                Log.e(TAG, "onReceive: alarmManager == null. No alarm set.");
                return;
            }

            PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 0,
                    new Intent(context, AlarmReceiver.class), 0);
            alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                    reminderTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
        }
    }
}