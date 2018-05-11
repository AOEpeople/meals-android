package com.aoe.mealsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Log
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.aoe.mealsapp.util.Alarm
import com.aoe.mealsapp.util.Config
import com.aoe.mealsapp.util.Settings
import org.json.JSONException
import org.json.JSONObject
import java.util.*

/**
 * BroadcastReceiver that should be triggered once a day (approx. time set in config file) and
 * check whether user has forgotten to register for the next day's meal.
 */
class AlarmReceiver : BroadcastReceiver() {

    lateinit var context: Context

    //
    // EXTENDS BroadcastReceiver
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
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onReceive() called with: context = [$context], intent = [$intent]")

        /* if alarm received after latest reminder time: ignore */

        val now = Calendar.getInstance()
        val latestReminderTime = Config.readTime(context, Config.LATEST_REMINDER_TIME)

        if (now.timeInMillis > latestReminderTime.timeInMillis) {
            return
        }

        /* if user wants to be notified: request server */

        if (userWantsToBeNotifiedForTomorrow(context)) {

            requestServerForTomorrowsParticipation(context, this::onRequestServerForTomorrowsParticipation);
        }
    }

    private fun onRequestServerForTomorrowsParticipation(userParticipatesTomorrow: Boolean?) {

        if (userParticipatesTomorrow == null) {
            Log.w(TAG, Thread.currentThread().name + " ### "
                    + "onReceive: Couldn't request server. Retry if latest reminder time hasn't passed, yet.")

            /* try again as long as the latest reminder time hasn't passed */

            val now = Calendar.getInstance()
            val latestReminderTime = Config.readTime(context, Config.LATEST_REMINDER_TIME)

            if (now.timeInMillis < latestReminderTime.timeInMillis) {

                Alarm.setRetryAlarm(context)
            }

        } else if (!userParticipatesTomorrow) {
            Notifications.showMealsNotification(context)
        }
    }

    //
    // HELPER
    //

    /**
     * Read the user settings and compare with current weekday to determine whether to check
     * for meals participation at all.
     *
     * @return Whether the user wants to be notified for tomorrow. false if reminder frequency
     * cannot be read from shared preferences.
     */
    private fun userWantsToBeNotifiedForTomorrow(context: Context): Boolean {

        /* read set reminder frequency from default shared preferences */

        Settings.readReminderFrequency(context)

        val reminderFrequencyKey = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SharedPreferenceKeys.REMINDER_FREQUENCY, null)

        if (reminderFrequencyKey == null) {
            // TODO review: should never happen
            Log.e(TAG, Thread.currentThread().name + " ### "
                    + "userWantsToBeNotifiedForTomorrow: Couldn't read reminder frequency from "
                    + "shared preferences. Return false.")
            return false
        }

        /* evaluate depending on reminder frequency and today's weekday */

        val today = Calendar.getInstance()
        val dayOfWeek = today.get(Calendar.DAY_OF_WEEK)

        when (reminderFrequencyKey) {
            SharedPreferenceKeys.REMINDER_FREQUENCY__BEFORE_MONDAY -> return dayOfWeek == 1

            SharedPreferenceKeys.REMINDER_FREQUENCY__BEFORE_EVERY_WEEKDAY -> return 1 <= dayOfWeek && dayOfWeek <= 5

            SharedPreferenceKeys.REMINDER_FREQUENCY__NEVER -> return false
        }

        // unreachable
        return false
    }

    /**
     * Sends two requests to the server to determine whether the user is already registered for
     * tomorrows meal:
     * 1. POST request to server for access token
     * 2. GET request to server for participation
     *
     * @param context Context used to build Volley RequestQueue
     */
    private fun requestServerForTomorrowsParticipation(context: Context, resultConsumer: (Boolean?) -> Unit) {

        /* Volley RequestQueue for server communication */

        val requestQueue = Volley.newRequestQueue(context)

        postLoginAndProceed(requestQueue, Consumer { isParticipating -> resultConsumer(isParticipating) }, context)
    }

    /**
     * Send a POST request to server with the following POST body:
     * - grant_type = password
     * - client_id = from gradle.properties
     * - client_secret = from gradle.properties
     * - username = from shared preferences
     * - password = from shared preferences
     *
     * @param requestQueue   Volley request queue that is used to send HTTP request
     * @param resultConsumer Callback for final result, passed on to getCurrentWeekAndProceed(),
     * delivers null if an error occurred
     */
    private fun postLoginAndProceed(
            requestQueue: RequestQueue,
            resultConsumer: Consumer<Boolean>,
            context: Context) {

        /* send POST request */

        requestQueue.add(object : StringRequest(
                Request.Method.POST,
                BuildConfig.SERVER_URL + "/oauth/v2/token",
                Response.Listener { loginResponse ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "onResponse() called with: loginResponse = [" + loginResponse + "]")

                    /* proceed: extract access token and GET current week data */

                    try {
                        val token = JSONObject(loginResponse).getString("access_token")
                        getCurrentWeekAndProceed(requestQueue, resultConsumer, token)

                    } catch (e: JSONException) {
                        Log.e(TAG, Thread.currentThread().name + " ### "
                                + "onResponse: Couldn't login. Returning null.", e)
                        resultConsumer.accept(null)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "onErrorResponse() called with: error = [" + error + "]")

                    Log.e(TAG, Thread.currentThread().name + " ### "
                            + "onErrorResponse: Couldn't login. Returning null.")
                    resultConsumer.accept(null)
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getParams(): Map<String, String> {

                /* read credentials from shared preferences */

                val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

                val username = sharedPreferences.getString(SharedPreferenceKeys.USERNAME, null)
                val password = sharedPreferences.getString(SharedPreferenceKeys.PASSWORD, null)

                // TODO handle null

                /* return HTTP POST params */

                val params = HashMap<String, String>()

                params["grant_type"] = "password"
                params["client_id"] = OAUTH_CLIENT_ID
                params["client_secret"] = OAUTH_CLIENT_SECRET
                params["username"] = username
                params["password"] = password

                return params
            }
        })
    }

    /**
     * Send a GET request to server with the following HTTP header:
     * - Authorization = Bearer &lt;token&gt;
     *
     * @param requestQueue   Volley request queue that is used to send HTTP request
     * @param resultConsumer Callback for final result, delivers null if an error occurred
     */
    private fun getCurrentWeekAndProceed(
            requestQueue: RequestQueue,
            resultConsumer: Consumer<Boolean>,
            token: String) {

        /* send GET request */

        requestQueue.add(object : StringRequest(
                Request.Method.GET,
                BuildConfig.SERVER_URL + "/rest/v1/week/active",
                Response.Listener { currentWeekResponse ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "onResponse() called with: currentWeekResponse = [" + currentWeekResponse + "]")

                    /* proceed: determine participation and send result to original caller via callback */

                    try {
                        val isParticipating = isParticipatingTomorrow(JSONObject(currentWeekResponse))
                        resultConsumer.accept(isParticipating)

                    } catch (e: JSONException) {
                        Log.e(TAG, Thread.currentThread().name + " ### "
                                + "onResponse: Login failed. Response doesn't contain participation field. "
                                + "Cannot notify user.", e)
                        resultConsumer.accept(null)
                    }
                },
                Response.ErrorListener { error ->
                    Log.d(TAG, Thread.currentThread().name + " ### "
                            + "onErrorResponse() called with: error = [" + error + "]")

                    Log.e(TAG, Thread.currentThread().name + " ### "
                            + "onErrorResponse: Couldn't receive current week. Returning null.")
                    resultConsumer.accept(null)
                }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()

                headers["Authorization"] = "Bearer $token"

                return headers
            }
        })
    }

    /**
     * Parse GET currentWeek answer for any set "isParticipate" property on tomorrow.
     *
     * @param currentWeekJsonAnswer JSON answer from GET currentWeek
     * @return user participates in at least one meal
     * @throws JSONException JSON doesn't contain "isParticipate" property at expected position
     */
    @Throws(JSONException::class)
    private fun isParticipatingTomorrow(currentWeekJsonAnswer: JSONObject): Boolean {

        /* get tomorrow's dayOfWeek in Monday = 0 format */

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_WEEK, 1)
        val dayOfWeek = (tomorrow.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 // Sun = 1 -> Mon = 0

        if (dayOfWeek == 4 || dayOfWeek == 5) {
            return false
        }

        /* search JSON object for any meal participation tomorrow */

        val jsonObjectMeals = currentWeekJsonAnswer
                .getJSONObject("currentWeek")
                .getJSONArray("days").getJSONObject(dayOfWeek)
                .getJSONArray("meals")

        for (i in 0 until jsonObjectMeals.length()) {
            if (jsonObjectMeals.getJSONObject(i).getBoolean("isParticipate")) {
                return true
            }
        }

        return false
    }

    companion object {

        private val TAG = "AlarmReceiver"

        private val OAUTH_CLIENT_ID = BuildConfig.OAUTH_CLIENT_ID
        private val OAUTH_CLIENT_SECRET = BuildConfig.OAUTH_CLIENT_SECRET
    }
}
