package com.aoe.mealsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.aoe.mealsapp.rest.CurrentWeekResponse
import com.aoe.mealsapp.rest.MealsApiImpl
import com.aoe.mealsapp.settings.ReminderFrequency
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.util.Alarm
import com.aoe.mealsapp.util.Config
import java.util.*

/**
 * BroadcastReceiver that should be triggered once a day (approx. time set in config file) and
 * check whether user has forgotten to register for the next day's meal.
 */
class AlarmReceiver : BroadcastReceiver() {

    // region ### onReceive()
    //

    /**
     * On daily alarm that triggers participation check:
     * - check settings: Does user want to be notified?
     * - if so: ask server for participation
     * - if user does not yet participate: notify him
     *
     * If the server is not available: retry every 5min within one hour after the planned request.
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onReceive() called with: context = [$context], intent = [$intent]")

        /* if it's too late today: do nothing */

        if (latestReminderTimePassed(context)) {
            return
        }

        /* if user wants to be notified: request server */

        if (userWantsToBeNotifiedForTomorrow(context)) {

            requestUserParticipatesTomorrow(context) { userParticipatesTomorrow ->

                // no valid response from server
                if (userParticipatesTomorrow == null) {
                    Log.d(TAG, Thread.currentThread().name + " ### " +
                            "onReceive() called with: userParticipatesTomorrow = [$userParticipatesTomorrow]")

                    if (!latestReminderTimePassed(context)) {
                        Alarm.setRetryAlarm(context)
                    } else {
                        Notifications.showServerUnavailableNotification(context)
                    }
                }
                // valid response && user does not participate yet
                else if (!userParticipatesTomorrow) {
                    Notifications.showMealsNotification(context)
                }
            }
        }
    }

    private fun latestReminderTimePassed(context: Context): Boolean {
        val now = Calendar.getInstance()
        val latestReminderTime = Config.readTime(context, Config.LATEST_REMINDER_TIME)

        return now.timeInMillis > latestReminderTime.timeInMillis
    }

    /**
     * Read the settings and compare with current weekday to determine whether to check
     * for meals participation at all.
     *
     * @return Whether the user wants to be notified for tomorrow. false if reminder frequency
     * cannot be read from shared preferences.
     */
    private fun userWantsToBeNotifiedForTomorrow(context: Context): Boolean {

        val reminderFrequency = Settings.getInstance(context).reminderFrequency

        /* evaluate depending on reminder frequency and today's weekday */

        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        return when (reminderFrequency) {
            ReminderFrequency.BEFORE_EVERY_WEEKDAY -> dayOfWeek in 1..5
            ReminderFrequency.ON_SUNDAYS -> dayOfWeek == 1
            ReminderFrequency.NEVER -> false
        }
    }

    //
    // endregion

    // region ### server communication
    //

    /**
     * Determine whether the user participates in any meal tomorrow.
     *
     * Therefore, request an OAuth token, use it to request the user's current week and parse it
     * for any participation tomorrow.
     */
    private fun requestUserParticipatesTomorrow(
            context: Context,
            callback: (userParticipatesTomorrow: Boolean?) -> Unit) {

        val settings = Settings.getInstance(context)
        val api = MealsApiImpl.getInstance(context)

        api.requestLogin(settings.username, settings.password) { loginResponse ->

            val token = loginResponse?.accessToken
            if (token == null) {
                callback(null)
            } else {

                api.requestCurrentWeek(token) { currentWeekResponse ->
                    if (currentWeekResponse == null) {
                        callback(null)
                    } else {

                        callback(userParticipatesTomorrow(currentWeekResponse))
                    }
                }
            }
        }
    }

    /**
     * Parse a response from the currentWeek endpoint for any participations on the next day.
     *
     * @return true if user participates in at least one meal, false otherwise
     */
    private fun userParticipatesTomorrow(currentWeekResponse: CurrentWeekResponse): Boolean? {

        /* get tomorrow's dayOfWeek in Monday = 0, Tuesday = 1, ... format */

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_WEEK, 1)

        val dayOfWeek = (tomorrow.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 // Sun = 1 -> Sun = 6

        /* search JSON object for any meal participation tomorrow */

        val days = currentWeekResponse.currentWeek?.days ?: return null

        if (dayOfWeek > days.size) {
            return false
        }

        val meals = days[dayOfWeek].meals ?: return null

        for (meal in meals) {
            if (meal.isParticipate ?: return null) {
                return true
            }
        }

        return false
    }

    //
    // endregion

    private companion object {
        private const val TAG = "AlarmReceiver"
    }
}
