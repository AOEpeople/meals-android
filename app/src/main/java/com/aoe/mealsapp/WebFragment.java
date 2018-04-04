package com.aoe.mealsapp;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class WebFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //
    // CONSTANTS
    //

    private static final String TAG = "## " + WebFragment.class.getSimpleName();

    private static final String PAGE_MAIN = BuildConfig.SERVER_URL;
    private static final String PAGE_LOGIN = BuildConfig.SERVER_URL + "/login";
    private static final String PAGE_TRANSACTIONS = BuildConfig.SERVER_URL + "/accounting/transactions";
    private static final String PAGE_LANGUAGE_SWITCH = BuildConfig.SERVER_URL + "/lastLanguage-switch";

    private static final String HTTP_USER_AGENT = "MealsApp Android WebView";

    //
    // STATIC METHODS
    //

    public static WebFragment newInstance() {
        return new WebFragment();
    }

    //
    // FIELDS
    //

    private WebView webView;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    private String lastUsername;
    private String lastPassword;
    private String lastLanguage;

    //
    // CONSTRUCTORS
    //

    public WebFragment() {
        // Required empty public constructor
    }

    //
    // EXTENDS Fragment
    //

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onAttach() called with: context = [" + context + "]");

        if (context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreateView() called with: inflater = [" + inflater + "], container = [" + container
                + "], savedInstanceState = [" + savedInstanceState + "]");

        View rootView =  inflater.inflate(R.layout.fragment_web, container, false);

        /* set up app bar */

        AppCompatActivity appCompatActivity = (AppCompatActivity) getActivity();
        assert appCompatActivity != null; // fragment has been attached TODO review

        Toolbar toolbar = rootView.findViewById(R.id.webFragment_toolbar_appBar);
        toolbar.setLogo(R.drawable.logo);
        appCompatActivity.setSupportActionBar(toolbar);

        /* init WebView */

        webView = rootView.findViewById(R.id.webFragment_webView_webApp);
        initWebView(webView);

        /* load login page */

        loadLoginPage();

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");

        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onDetach() called");

        onFragmentInteractionListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onOptionsItemSelected() called with: item = [" + item + "]");

        int optionsItemId = item.getItemId();

        switch (optionsItemId) {
            case R.id.mainMenu_alarm:
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager == null) {
                    Log.e(TAG, "onOptionsItemSelected: Couldn't get AlarmManager. No alarm set.");
                    return true;
                }

                // TODO request code == magic number
                PendingIntent alarmIntent = PendingIntent.getBroadcast(getContext(), 0,
                        new Intent(getContext(), AlarmReceiver.class), 0);
                alarmManager.set(AlarmManager.RTC_WAKEUP,
                        Calendar.getInstance().getTimeInMillis(), alarmIntent);
                break;

            case R.id.mainMenu_account:
                webView.loadUrl(PAGE_TRANSACTIONS);
                return true;

            case R.id.mainMenu_settings:
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onPause() called");

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        /* stop listening to SharedPreference changes */

        defaultSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onResume() called");

        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        /* check for changes in default SharedPreferences */

        String preferenceUsername = defaultSharedPreferences.getString(SharedPreferenceKeys.USERNAME, null);
        String preferencePassword = defaultSharedPreferences.getString(SharedPreferenceKeys.PASSWORD, null);
        String preferenceLanguage = defaultSharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null);

        assert preferenceLanguage != null;

        boolean sameUsername = (lastUsername == null ? preferenceUsername == null : lastUsername.equals(preferenceUsername));
        boolean samePassword = (lastPassword == null ? preferencePassword == null : lastPassword.equals(preferencePassword));
        boolean sameLanguage = lastLanguage.equals(preferenceLanguage);

        lastUsername = preferenceUsername;
        lastPassword = preferencePassword;
        lastLanguage = preferenceLanguage;

        if (!sameUsername || !samePassword) {
            refreshWebsite();
        } else if (!sameLanguage) {
            switchWebsiteLanguage();
        }

        /* listen for changes in default SharedPreferences */

        defaultSharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    //
    // IMPLEMENTS SharedPreferences.OnSharedPreferenceChangeListener
    //

    /**
     * Handle all settings changes that are relevant for this activity, i.e. the WebView (e.g.
     * user credentials or website lastLanguage).
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onSharedPreferenceChanged() called with: sharedPreferences = ["
                + sharedPreferences + "], key = [" + key + "]");

        switch (key) {
            case SharedPreferenceKeys.USERNAME:
            case SharedPreferenceKeys.PASSWORD:
                refreshWebsite();
                break;

            case SharedPreferenceKeys.LANGUAGE:
                switchWebsiteLanguage();
                break;
        }
    }

    //
    // HELPER
    //

    /**
     * - open login page with credentials from preferences (send via POST)
     */
    private void loadLoginPage() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

        lastUsername = sharedPreferences.getString(SharedPreferenceKeys.USERNAME, null);
        lastPassword = sharedPreferences.getString(SharedPreferenceKeys.PASSWORD, null);
        lastLanguage = sharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null);

        String postUsername = lastUsername;
        String postPassword = lastPassword;

        if (lastUsername == null) {
            Log.w(TAG, "onCreate: lastUsername == null. postUsername = \"\"");
            postUsername = "";
        }

        if (lastPassword == null) {
            Log.w(TAG, "onCreate: lastPassword == null. postPassword = \"\"");
            postPassword = "";
        }

        String postData = "_username=" + postUsername + "&_password=" + postPassword;
        webView.postUrl(PAGE_LOGIN, postData.getBytes());
    }

    /**
     * - enable JavaScript
     * - set custom HTTP user agent
     * - set onPageFinished handler that redirects to the LoginActivity if credentials are wrong
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(WebView webView) {

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

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

                if (!(url.equals(PAGE_MAIN) || url.equals(PAGE_TRANSACTIONS))) {
                    sharedPreferences.edit().putBoolean(SharedPreferenceKeys.CREDENTIALS_WERE_VALIDATED, false).apply();

//                    startActivity(new Intent(getContext(), LoginActivity.class));
                    onFragmentInteractionListener.onLoginFailed();
//                    finish();

                } else {
                    sharedPreferences.edit().putBoolean(SharedPreferenceKeys.CREDENTIALS_WERE_VALIDATED, true).apply();
                }
            }
        });
    }

    private void refreshWebsite() {
        String postData = "_username=" + lastUsername + "&_password=" + lastPassword;
        Log.d(TAG, "refreshWebsite: !!!");
        webView.postUrl(PAGE_LOGIN, postData.getBytes());
    }

    private void switchWebsiteLanguage() {
        Map<String, String> additionalHttpHeaders = new HashMap<>();
        additionalHttpHeaders.put("Referer", webView.getUrl());
        Log.d(TAG, "switchWebsiteLanguage: !!!");
        webView.loadUrl(PAGE_LANGUAGE_SWITCH, additionalHttpHeaders);
    }

    //
    // INNER TYPES
    //

    public interface OnFragmentInteractionListener {
        void onLoginFailed();
    }
}
