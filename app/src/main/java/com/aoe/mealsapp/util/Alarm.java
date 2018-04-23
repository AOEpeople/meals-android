package com.aoe.mealsapp.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aoe.mealsapp.AlarmReceiver;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

public class Alarm {

    private static final String TAG = "Alarm";

    private static final int REQUEST_CODE_DAILY_ALARM = 1;
    private static final int REQUEST_CODE_RETRY_ALARM = 2;

    /**
     * Sets a daily alarm at the reminder time defined in the config file
     */
    public static void setDailyAlarm(Context context) {
        
        /* read reminderTime from config file */

        Calendar reminderTime;
        try {
            reminderTime = Config.readTime(context, Config.REMINDER_TIME);

        } catch (IOException | ParseException e) {
            Log.e(TAG, Thread.currentThread().getName() + " ### "
                    + "setDailyAlarm: Couldn't read reminder time from config file. No alarm set.", e);
            return;
        }

        /* set daily alarm at reminder time */

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) { // should probably never happen
            Log.e(TAG, Thread.currentThread().getName() + " ### "
                    + "setDailyAlarm: Couldn't retrieve AlarmManager. No alarm set.");
            return;
        }

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_DAILY_ALARM,
                new Intent(context, AlarmReceiver.class), 0);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                reminderTime.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);    
    }

    public static void setRetryAlarm(Context context) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) { // should probably never happen
            Log.e(TAG, Thread.currentThread().getName() + " ### "
                    + "accept: Couldn't retrieve AlarmManager. No alarm set.");
            return;
        }

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, REQUEST_CODE_RETRY_ALARM,
                new Intent(context, AlarmReceiver.class), 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().getTimeInMillis() + 1000 * 60 * 5, alarmIntent);
    }
}
