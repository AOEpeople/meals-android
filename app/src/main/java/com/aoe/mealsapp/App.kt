package com.aoe.mealsapp

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.util.Alarm

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called")

        /* set default preference values on first app launch */

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        /* run on first app start */

        val settings = Settings.getInstance(this)

        if (settings.firstAppStart) {
            Alarm.setDailyAlarm(this)
            settings.firstAppStart = false
        }

        /* */

        Notifications.createNotificationChannel(this)
    }

    private companion object {
        private const val TAG = "App"
    }
}
