package com.aoe.mealsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aoe.mealsapp.settings.ReminderFrequency
import com.aoe.mealsapp.settings.Settings
import com.aoe.mealsapp.util.Alarm
import com.aoe.mealsapp.util.Config
import org.json.JSONObject
import java.util.*

/**
 * BroadcastReceiver that should be triggered once a day (approx. time set in config file) and
 * check whether user has forgotten to register for the next day's meal.
 */
class AlarmReceiver : BroadcastReceiver() {

    // region ### onReceive

    /**
     * On daily alarm that triggers participation check:
     * - check settings: Does user want to be notified?
     * - if so: ask server for participation
     * - if user does not yet participate: notify him
     *
     * If the server is not available: retry every 5min within one hour after the planned request.
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onReceive() called with: context = [$context], intent = [$intent]")

        /* if it's too late today: do nothing */

        if (latestReminderTimePassed(context)) {
            return
        }

        /* if user wants to be notified: request server */

        if (userWantsToBeNotifiedForTomorrow(context)) {

            requestUserParticipatesTomorrow(context) { userParticipatesTomorrow ->
                if (userParticipatesTomorrow == null) {
                    Log.w(TAG, Thread.currentThread().name + " ### "
                            + "onReceive: Couldn't request server. Retry if latest reminder time hasn't passed, yet.")

                    if (!latestReminderTimePassed(context)) {
                        Alarm.setRetryAlarm(context)
                    }

                } else if (!userParticipatesTomorrow) {
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

    // endregion

    // region ### server communication

    /**
     * Determine whether the user participates in any meal tomorrow.
     *
     * Therefore, request an OAuth token, use it to request the user's current week and parse it
     * for any participation tomorrow.
     */
    private fun requestUserParticipatesTomorrow(
            context: Context,
            callback: (userParticipatesTomorrow: Boolean?) -> Unit) {

        val volleyRequestQueue = Volley.newRequestQueue(context)

        requestLogin(volleyRequestQueue, context) { loginResponse ->

            val token = JSONObject(loginResponse).getString("access_token")

            requestCurrentWeek(volleyRequestQueue, token) { currentWeekResponse ->

                if (currentWeekResponse == null) {
                    callback(null)
                } else {
                    callback(userParticipatesTomorrow(currentWeekResponse))
                }
            }
        }
    }

    /**
     * Sends a POST request to the login endpoint and returns the response asynchronously.
     */
    @Throws(VolleyError::class)
    private fun requestLogin(
            volleyRequestQueue: RequestQueue,
            context: Context,
            callback: (loginResponse: String?) -> Unit) {

        volleyRequestQueue.add(object : StringRequest(
                Request.Method.POST,
                BuildConfig.SERVER_URL + "/oauth/v2/token",

                Response.Listener { response ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestLogin() called with: response = [$response]")

                    callback.invoke(response)
                },

                Response.ErrorListener { error ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestLogin() called with: error = [$error]")

                    callback.invoke(null)
                }
        ) {
            override fun getParams() = mapOf(
                    "grant_type" to "password",
                    "client_id" to OAUTH_CLIENT_ID,
                    "client_secret" to OAUTH_CLIENT_SECRET,
                    "username" to Settings.getInstance(context).username,
                    "password" to Settings.getInstance(context).password)
        })
    }

    /**
     * Sends a GET request to the currentWeek endpoint and returns the response asynchronously.
     */
    @Throws(VolleyError::class)
    private fun requestCurrentWeek(
            volleyRequestQueue: RequestQueue,
            token: String,
            callback: (currentWeekResponse: String?) -> Unit) {

        volleyRequestQueue.add(object : StringRequest(
                Request.Method.GET,
                BuildConfig.SERVER_URL + "/rest/v1/week/active",

                Response.Listener { response ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestCurrentWeek() called with: response = [$response]")

                    callback.invoke(response)
                },

                Response.ErrorListener { error ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "requestCurrentWeek() called with: error = [$error]")

                    callback(null)
                }
        ) {
            override fun getHeaders() = mapOf("Authorization" to "Bearer $token")
        })
    }

    /**
     * Parse a response from the currentWeek endpoint for any participations on the next day.
     *
     * @return true if user participates in at least one meal, false otherwise
     */
    private fun userParticipatesTomorrow(currentWeekResponse: String): Boolean {

        /* get tomorrow's dayOfWeek in Monday = 0, Tuesday = 1, ... format */

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_WEEK, 1)

        val dayOfWeek = (tomorrow.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 // Sun = 1 -> Sun = 6

        /* search JSON object for any meal participation tomorrow */

        val days = JSONObject(currentWeekResponse)
                .getJSONObject("currentWeek")
                .getJSONArray("days")

        if (dayOfWeek > days.length()) {
            return false
        }

        val meals = days
                .getJSONObject(dayOfWeek)
                .getJSONArray("meals")

        for (i in 0 until meals.length()) {
            if (meals.getJSONObject(i).getBoolean("isParticipate")) {
                return true
            }
        }

        return false
    }

    // endregion

    // region ### companion

    private companion object {

        private const val TAG = "AlarmReceiver"

        private const val OAUTH_CLIENT_ID = BuildConfig.OAUTH_CLIENT_ID
        private const val OAUTH_CLIENT_SECRET = BuildConfig.OAUTH_CLIENT_SECRET
    }

    // endregion
}
