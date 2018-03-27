package com.aoe.mealsapp;

import java.util.HashMap;
import java.util.Map;

class SharedPreferenceKeys {
    static final String CREDENTIALS_WERE_VALIDATED = "preferences_credentialsWereValidated";
    static final String LANGUAGE = "preferences_language";
    static final String LANGUAGE__ENGLISH = "preferences_language_entryValues_english";
    static final String LANGUAGE__GERMAN = "preferences_language_entryValues_german";
    static final String PASSWORD = "preferences_password";
    static final String REMINDER_FREQUENCY = "preferences_reminderFrequency";
    static final String REMINDER_FREQUENCY__BEFORE_EVERY_WEEKDAY = "preferences_reminderFrequency_entryValues_beforeEveryWeekday";
    static final String REMINDER_FREQUENCY__BEFORE_MONDAY = "preferences_reminderFrequency_entryValues_beforeMonday";
    static final String REMINDER_FREQUENCY__NEVER = "preferences_reminderFrequency_entryValues_never";
    static final String USERNAME = "preferences_username";

    static Map<String, Integer> keysToValues = new HashMap<>();
    static {
        keysToValues.put(LANGUAGE__ENGLISH, R.string.preferences_language_entries_english);
        keysToValues.put(LANGUAGE__GERMAN, R.string.preferences_language_entries_german);
        keysToValues.put(REMINDER_FREQUENCY__BEFORE_EVERY_WEEKDAY, R.string.preferences_reminderFrequency_entries_beforeEveryWeekday);
        keysToValues.put(REMINDER_FREQUENCY__BEFORE_MONDAY, R.string.preferences_reminderFrequency_entries_beforeMonday);
        keysToValues.put(REMINDER_FREQUENCY__NEVER, R.string.preferences_reminderFrequency_entries_never);
    }
}
