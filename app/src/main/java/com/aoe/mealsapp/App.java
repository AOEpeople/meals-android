package com.aoe.mealsapp;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.aoe.mealsapp.util.Alarm;

public class App extends Application {

    private static final String TAG = "App";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreate() called");

        PreferenceManager.setDefaultValues(this, R.xml.preferences,false);

        /* run on first app start */

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean firstAppStart = defaultSharedPreferences.getBoolean("first_app_start", true);

        if (firstAppStart) {

            Alarm.setDailyAlarm(this);

            defaultSharedPreferences.edit().putBoolean("first_app_start", false).apply();
        }

        /* */

        Notifications.INSTANCE.createNotificationChannel(this);
    }
}
