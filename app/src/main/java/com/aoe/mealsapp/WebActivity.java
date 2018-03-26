package com.aoe.mealsapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Main activity that contains the WebView that shows the MealsApp website.
 */
public class WebActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //
    // CONSTANTS
    //

    private static final String TAG = "## " + WebActivity.class.getSimpleName();

    private static final String PAGE_MAIN = BuildConfig.SERVER_URL;
    private static final String PAGE_LOGIN = BuildConfig.SERVER_URL + "/login";
    private static final String PAGE_TRANSACTIONS = BuildConfig.SERVER_URL + "/accounting/transactions";
    private static final String PAGE_LANGUAGE_SWITCH = BuildConfig.SERVER_URL + "/language-switch";

    private static final String HTTP_USER_AGENT = "MealsApp Android WebView";

    //
    // FIELDS
    //

    private WebView webView;

    //
    // EXTENDS AppCompatActivity
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        setContentView(R.layout.activity_web);

        setUpAppBar();
        initWebView();

        // set default preference values
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        // register for settings changes
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        loadLoginPage();
    }

    /**
     * - open login page with credentials from preferences (send via POST)
     */
    private void loadLoginPage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferenceKeys keys = SharedPreferenceKeys.getInstance(this);

        String username = sharedPreferences.getString(keys.username, null);
        String password = sharedPreferences.getString(keys.password, null);

        if (username == null) {
            Log.w(TAG, "onCreate: SharedPreferences contains no username. username = \"\"");
            username = "";
        }

        if (password == null) {
            Log.w(TAG, "onCreate: SharedPreferences contains no password. password = \"\"");
            password = "";
        }

        String postData = "_username=" + username + "&_password=" + password;
        webView.postUrl(PAGE_LOGIN, postData.getBytes());
    }

    /**
     * - enable JavaScript
     * - set custom HTTP user agent
     * - set onPageFinished handler that redirects to the LoginActivity if credentials are wrong
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView() {
        webView = findViewById(R.id.webActivity_webView_webApp);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(HTTP_USER_AGENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, Thread.currentThread().getName() + ": "
                        + "onPageFinished() called with: view = [" + view + "], url = [" + url + "]");

                /* remove trailing slash from URL */

                url = url.replaceAll("/$", "");

                /* unexpected page (e.g. login because bad credentials) -> LoginActivity */

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(WebActivity.this);
                SharedPreferenceKeys keys = SharedPreferenceKeys.getInstance(WebActivity.this);

                if (!(url.equals(PAGE_MAIN) || url.equals(PAGE_TRANSACTIONS))) {
                    sharedPreferences.edit().putBoolean(keys.credentialsWereValidated, false).apply();

                    startActivity(new Intent(WebActivity.this, LoginActivity.class));
                    finish();

                } else {
                    sharedPreferences.edit().putBoolean(keys.credentialsWereValidated, true).apply();
                }
            }
        });
    }

    /**
     * - set toolbar as app bar
     * - set AOE logo left to the title
     */
    private void setUpAppBar() {
        Toolbar toolbar = findViewById(R.id.webActivity_toolbar_appBar);
        toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreateOptionsMenu() called with: menu = [" + menu + "]");

        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onDestroy() called");

        /* unregister from settings changes */

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onOptionsItemSelected() called with: item = [" + item + "]");

        int optionsItemId = item.getItemId();

        switch (optionsItemId) {
            case R.id.mainMenu_alarm:
                AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
                if (alarmManager == null) {
                    Log.e(TAG, "onOptionsItemSelected: Couldn't get AlarmManager. No alarm set.");
                    return true;
                }

                // TODO request code == magic number
                PendingIntent alarmIntent = PendingIntent.getBroadcast(this, 0,
                        new Intent(this, AlarmReceiver.class), 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        Calendar.getInstance().getTimeInMillis(), alarmIntent);
                break;

            case R.id.mainMenu_account:
                webView.loadUrl(PAGE_TRANSACTIONS);
                return true;

            case R.id.mainMenu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * If user is not on initial WebView page: Go back in browser history. When user has arrived
     * on initial page: Default behaviour (close activity -> closes app).
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onBackPressed() called");

        if (webView.canGoBack())
            webView.goBack();
        else
            super.onBackPressed();
    }

    //
    // IMPLEMENTS SharedPreferences.OnSharedPreferenceChangeListener
    //

    /**
     * Handle all settings changes that are relevant for this activity, i.e. the WebView (e.g.
     * user credentials or website language).
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onSharedPreferenceChanged() called with: sharedPreferences = ["
                + sharedPreferences + "], key = [" + key + "]");

        SharedPreferenceKeys keys = SharedPreferenceKeys.getInstance(this);

        if (key.equals(keys.username) || key.equals(keys.password)) {

            /* load login page with new credentials */

            String username = sharedPreferences.getString(keys.username, null);
            String password = sharedPreferences.getString(keys.password, null);

            // TODO review null handling

            if (username == null) {
                Log.w(TAG, "onSharedPreferenceChanged: username == null. return");
                return;
            }

            if (password == null) {
                Log.w(TAG, "onSharedPreferenceChanged: password == null. return");
                return;
            }

            String postData = "_username=" + username + "&_password=" + password;
            webView.postUrl(PAGE_LOGIN, postData.getBytes());

        } else if (key.equals(keys.language)) {

            /* reload current web page with other language */

            Map<String, String> additionalHttpHeaders = new HashMap<>();
            additionalHttpHeaders.put("Referer", webView.getUrl());
            webView.loadUrl(PAGE_LANGUAGE_SWITCH, additionalHttpHeaders);
        }
    }
}
