package com.aoe.mealsapp.util

import android.content.Context
import android.preference.PreferenceManager

object Settings {

    var username: String = ""

    private enum class Setting(val sharedPreferencesKey: String) {
        USERNAME("preferences_username"),
        PASSWORD("preferences_password"),
        LANGUAGE("preferences_language"),
        REMINDER_FREQUENCY("preferences_reminderFrequency")
    }

    fun readReminderFrequency(context: Context): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("TEST", false)
    }
}
