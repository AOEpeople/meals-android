package com.aoe.mealsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aoe.mealsapp.util.Config;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;

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

            /* read reminderTime from config file */

            Calendar reminderTime;
            try {
                reminderTime = Config.readReminderTime(context);

            } catch (IOException | ParseException e) {
                Log.e(TAG, "onReceive: Couln't read reminder time from config file. No alarm set.");
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