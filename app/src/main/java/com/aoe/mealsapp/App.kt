package com.aoe.mealsapp

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import com.aoe.mealsapp.settings.Language
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.util.Alarm
import java.util.*

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

            /* init web app's language to system's language */

            val lastLanguage = when (Locale.getDefault().language) {
                "en" -> Language.ENGLISH
                "de" -> Language.GERMAN
                else -> Language.ENGLISH
            }

            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("LANGUAGE", lastLanguage.toString()).apply()

            settings.firstAppStart = false
        }

        /* */

        Notifications.createNotificationChannel(this)
    }

    private companion object {
        private const val TAG = "App"
    }
}
