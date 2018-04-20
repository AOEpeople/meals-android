package com.aoe.mealsapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aoe.mealsapp.util.Alarm;

public class BootReceiver extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";

    /**
     * Called if BootReceiver has been enabled. Checks received intent for security reasons,
     * reads reminder time from config.properties and sets alarm at reminder time which
     * repeats daily.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

            Alarm.setDailyAlarm(context);
        }
    }
}