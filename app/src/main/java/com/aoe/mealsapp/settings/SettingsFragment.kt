package com.aoe.mealsapp.settings

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import android.util.Log
import com.aoe.mealsapp.R

class SettingsFragment : PreferenceFragment(), Settings.OnSettingChangeListener {

    //
    // fragment lifecycle
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        addPreferencesFromResource(R.xml.preferences)

        Settings.getInstance(activity).registerOnSettingChangeListener(this)

        initSummaries()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onDestroy() called")

        Settings.getInstance(activity).unregisterOnSettingChangeListener(this)
    }

    //
    // init summaries
    //

    private fun initSummaries() {
        val settings = Settings.getInstance(activity)

        findPreference(Setting.USERNAME.toString()).summary = settings.username ?: ""

        val reminderFrequencyText = getText(activity, settings.reminderFrequency)
        findPreference(Setting.REMINDER_FREQUENCY.toString()).summary = reminderFrequencyText

        val languageText = getText(activity, settings.language)
        findPreference(Setting.LANGUAGE.toString()).summary = languageText
    }

    private fun getText(context: Context, reminderFrequency: ReminderFrequency) =
            context.getString(when (reminderFrequency) {
                ReminderFrequency.ON_SUNDAYS -> R.string.preferences_reminderFrequency_entries_onSundays
                ReminderFrequency.BEFORE_EVERY_WEEKDAY -> R.string.preferences_reminderFrequency_entries_beforeEveryWeekday
                ReminderFrequency.NEVER -> R.string.preferences_reminderFrequency_entries_never
            })

    private fun getText(context: Context, language: Language) =
            context.getString(when (language) {
                Language.ENGLISH -> R.string.preferences_language_entries_english
                Language.GERMAN -> R.string.preferences_language_entries_german
            })

    //
    // listen for settings changes
    //

    override fun onUsernameChanged(username: String) {
        findPreference(Setting.USERNAME.toString()).summary = username
    }

    override fun onPasswordChanged(password: String) {}

    override fun onLanguageChanged(language: Language) {
        val languageText = getText(activity, language)
        findPreference(Setting.LANGUAGE.toString()).summary = languageText
    }

    override fun onReminderFrequencyChanged(reminderFrequency: ReminderFrequency) {
        val reminderFrequencyText = getText(activity, reminderFrequency)
        findPreference(Setting.REMINDER_FREQUENCY.toString()).summary = reminderFrequencyText
    }

    //
    //
    //

    private companion object {
        private const val TAG = "SettingsFragment"
    }
}
