package com.aoe.mealsapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * BroadcastReceiver that should be triggered once a day (approx. time set in config file) and
 * check whether user has forgotten to register for the next day's meal.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "## " + AlarmReceiver.class.getSimpleName();

    private static final String OAUTH_CLIENT_ID = BuildConfig.OAUTH_CLIENT_ID;
    private static final String OAUTH_CLIENT_SECRET = BuildConfig.OAUTH_CLIENT_SECRET;

    private static final String NOTIFICATION_CHANNEL_MEALS = "notification_channel_meals";
    private static final int NOTIFICATION_ID = 1;

    //
    // EXTENDS BroadcastReceiver
    //

    /**
     * On daily alarm that triggers participation check:
     * - check settings: Does user want to be notified?
     * - if so: ask server for participation
     * - if user does not yet participate: notify him
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        if (userWantsToBeNotifiedForTomorrow(context)) {

            requestServerForTomorrowsParticipation(context, new Consumer<Boolean>() {
                @Override
                public void accept(Boolean userParticipatesTomorrow) {

                    if (userParticipatesTomorrow == null) {
                        Log.e(TAG, "accept: Failed to request server. Aborting user notification.");
                        return;
                    }

                    if (!userParticipatesTomorrow) {
                        notifyUser(context);
                    }
                }
            });
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
    private boolean userWantsToBeNotifiedForTomorrow(Context context) {

        /* read set reminder frequency from default shared preferences */

        String reminderFrequencyKey = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SharedPreferenceKeys.REMINDER_FREQUENCY, null);

        if (reminderFrequencyKey == null) {
            // TODO review: should never happen
            Log.e(TAG, "userWantsToBeNotifiedForTomorrow: Cannot read reminder frequency " +
                    "from shared preferences. Return false.");
            return false;
        }

        /* evaluate depending on reminder frequency and today's weekday */

        Calendar today = Calendar.getInstance();
        int dayOfWeek = today.get(Calendar.DAY_OF_WEEK);

        switch (reminderFrequencyKey) {
            case SharedPreferenceKeys.REMINDER_FREQUENCY__BEFORE_MONDAY:
                return dayOfWeek == 1;

            case SharedPreferenceKeys.REMINDER_FREQUENCY__BEFORE_EVERY_WEEKDAY:
                return 1 <= dayOfWeek && dayOfWeek <= 5;

            case SharedPreferenceKeys.REMINDER_FREQUENCY__NEVER:
                return false;
        }

        // unreachable
        return false;
    }

    /**
     * Sends two requests to the server to determine whether the user is already registered for
     * tomorrows meal:
     * 1. POST request to server for access token
     * 2. GET request to server for participation
     *
     * @param context Context used to build Volley RequestQueue
     */
    private void requestServerForTomorrowsParticipation(Context context, final Consumer<Boolean> resultConsumer) {

        /* Volley RequestQueue for server communication */

        final RequestQueue requestQueue = Volley.newRequestQueue(context);

        postLoginAndProceed(requestQueue, new Consumer<Boolean>() {
            @Override
            public void accept(Boolean isParticipating) {

                resultConsumer.accept(isParticipating);
            }
        }, context);
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
     *                       delivers null if an error occurred
     */
    private void postLoginAndProceed(
            final RequestQueue requestQueue,
            final Consumer<Boolean> resultConsumer,
            final Context context) {

        /* send POST request */

        requestQueue.add(new StringRequest(
                Request.Method.POST,
                BuildConfig.SERVER_URL + "/oauth/v2/token",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String loginResponse) {
                        Log.d(TAG, Thread.currentThread().getName() + ": "
                                + "onResponse() called with: loginResponse = [" + loginResponse + "]");

                        /* proceed: extract access token and GET current week data */

                        try {
                            String token = new JSONObject(loginResponse).getString("access_token");
                            getCurrentWeekAndProceed(requestQueue, resultConsumer, token);

                        } catch (JSONException e) {
                            Log.e(TAG, "accept: Login failed. Returning null.", e);
                            resultConsumer.accept(null);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, Thread.currentThread().getName() + ": "
                                + "onErrorResponse() called with: error = [" + error + "]");

                        Log.e(TAG, "onErrorResponse: Login failed. Returning null.");
                        resultConsumer.accept(null);
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {

                /* read credentials from shared preferences */

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

                String username = sharedPreferences.getString(SharedPreferenceKeys.USERNAME, null);
                String password = sharedPreferences.getString(SharedPreferenceKeys.PASSWORD, null);

                // TODO handle null

                /* return HTTP POST params */

                Map<String, String> params = new HashMap<>();

                params.put("grant_type", "password");
                params.put("client_id", OAUTH_CLIENT_ID);
                params.put("client_secret", OAUTH_CLIENT_SECRET);
                params.put("username", username);
                params.put("password", password);

                return params;
            }
        });
    }

    /**
     * Send a GET request to server with the following HTTP header:
     * - Authorization = Bearer &lt;token&gt;
     *
     * @param requestQueue   Volley request queue that is used to send HTTP request
     * @param resultConsumer Callback for final result, delivers null if an error occurred
     */
    private void getCurrentWeekAndProceed(
            RequestQueue requestQueue,
            final Consumer<Boolean> resultConsumer,
            final String token) {

        /* send GET request */

        requestQueue.add(new StringRequest(
                Request.Method.GET,
                BuildConfig.SERVER_URL + "/rest/v1/week/active",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String currentWeekResponse) {
                        Log.d(TAG, Thread.currentThread().getName() + ": "
                                + "onResponse() called with: response = [" + currentWeekResponse + "]");

                        /* proceed: determine participation and send result to original caller via callback */

                        try {
                            boolean isParticipating = isParticipatingTomorrow(new JSONObject(currentWeekResponse));
                            resultConsumer.accept(isParticipating);

                        } catch (JSONException e) {
                            Log.e(TAG, "accept: Login failed. Response doesn't contain participation field. Cannot notify user.", e);
                            resultConsumer.accept(null);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d(TAG, Thread.currentThread().getName() + ": "
                                + "onErrorResponse() called with: error = [" + error + "]");

                        Log.e(TAG, "onErrorResponse: Receiving current week failed. Returning null.");
                        resultConsumer.accept(null);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                headers.put("Authorization", "Bearer " + token);

                return headers;
            }
        });
    }

    /**
     * Parse GET currentWeek answer for any set "isParticipate" property on tomorrow.
     *
     * @param currentWeekJsonAnswer JSON answer from GET currentWeek
     * @return user participates in at least one meal
     * @throws JSONException JSON doesn't contain "isParticipate" property at expected position
     */
    private boolean isParticipatingTomorrow(JSONObject currentWeekJsonAnswer) throws JSONException {

        /* get tomorrow's dayOfWeek in Monday = 0 format */

        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_WEEK, 1);
        int dayOfWeek = (tomorrow.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7; // Sun = 1 -> Mon = 0

        /* search JSON object for any meal participation tomorrow */

        JSONArray jsonObjectMeals = currentWeekJsonAnswer
                .getJSONObject("currentWeek")
                .getJSONArray("days").getJSONObject(dayOfWeek)
                .getJSONArray("meals");

        for (int i = 0; i < jsonObjectMeals.length(); i++) {
            if (jsonObjectMeals.getJSONObject(i).getBoolean("isParticipate")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Show a notification in the notification bar. Upon clicking it the MainActivity is started
     *
     * @param context Context necessary to create notification
     */
    private void notifyUser(Context context) {

        // TODO add comments

        String notificationTitle = context.getString(R.string.notification_title);
        String notificationText = context.getString(R.string.notification_text);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_MEALS)
                .setSmallIcon(android.R.drawable.sym_def_app_icon)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText);

        Intent resultIntent = new Intent(context, MainActivity.class);
        TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(context);
        taskStackBuilder.addParentStack(MainActivity.class);
        taskStackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                taskStackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
