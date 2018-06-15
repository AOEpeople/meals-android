package com.aoe.mealsapp.settings

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.aoe.mealsapp.util.SingletonHolder

class Settings private constructor(context: Context)
    : SharedPreferences.OnSharedPreferenceChangeListener {

    private val defaultSharedPreferences: SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    private val listeners: MutableList<OnSettingChangeListener> = mutableListOf()

    init {
        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    //
    // read & write settings
    //

    var username: String
        get() = defaultSharedPreferences.getString(Setting.USERNAME.toString(), "")
        set(value) {
            defaultSharedPreferences.edit().putString(Setting.USERNAME.toString(), value).apply()
        }

    var password: String
        get() = defaultSharedPreferences.getString(Setting.PASSWORD.toString(), "")
        set(value) {
            defaultSharedPreferences.edit().putString(Setting.PASSWORD.toString(), value).apply()
        }

    var language: Language
        get() = Language.valueOf(defaultSharedPreferences.getString(Setting.LANGUAGE.toString(), null))
        set(value) {
            defaultSharedPreferences.edit().putString(Setting.LANGUAGE.toString(), value.toString()).apply()
        }

    var reminderFrequency: ReminderFrequency
        get() = ReminderFrequency.valueOf(defaultSharedPreferences.getString(Setting.REMINDER_FREQUENCY.toString(), null))
        set(value) {
            defaultSharedPreferences.edit().putString(Setting.REMINDER_FREQUENCY.toString(), value.toString()).apply()
        }

    var firstAppStart: Boolean
        get() = defaultSharedPreferences.getBoolean(Setting.FIRST_APP_START.toString(), true)
        set(value) {
            defaultSharedPreferences.edit().putBoolean(Setting.FIRST_APP_START.toString(), value).apply()
        }

    //
    // notify about settings changes
    //

    fun registerOnSettingChangeListener(listener: OnSettingChangeListener) {
        listeners.add(listener)
    }

    fun unregisterOnSettingChangeListener(listener: OnSettingChangeListener) {
        listeners.remove(listener)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val setting = Setting.valueOf(key)
        when (setting) {
            Setting.USERNAME -> listeners.forEach { it.onUsernameChanged(username) }
            Setting.PASSWORD -> listeners.forEach { it.onPasswordChanged(password) }
            Setting.LANGUAGE -> listeners.forEach { it.onLanguageChanged(language) }
            Setting.REMINDER_FREQUENCY -> listeners.forEach { it.onReminderFrequencyChanged(reminderFrequency) }
        }
    }

    interface OnSettingChangeListener {
        fun onUsernameChanged(username: String)
        fun onPasswordChanged(password: String)
        fun onLanguageChanged(language: Language)
        fun onReminderFrequencyChanged(reminderFrequency: ReminderFrequency)
    }

    //
    //
    //

    companion object : SingletonHolder<Settings, Context>(::Settings)
}
