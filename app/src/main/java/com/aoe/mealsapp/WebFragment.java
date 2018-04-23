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
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
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

public class WebFragment extends Fragment implements OnBackPressedListener {

    //
    // CONSTANTS
    //

    private static final String TAG = "WebFragment";

    private static final String PAGE_MAIN = BuildConfig.SERVER_URL;
    private static final String PAGE_LOGIN = BuildConfig.SERVER_URL + "/login";
    private static final String PAGE_TRANSACTIONS = BuildConfig.SERVER_URL + "/accounting/transactions";
    private static final String PAGE_LANGUAGE_SWITCH = BuildConfig.SERVER_URL + "/language-switch";

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

    private SharedPreferences defaultSharedPreferences;

    // keep track of the values read from the default SharedPreferences last time
    // to be able to recognize changes in onResume()
    private String lastUsernamePreference;
    private String lastPasswordPreference;
    private String lastLanguagePreference;

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
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onAttach() called with: context = [" + context + "]");

        if (context instanceof OnFragmentInteractionListener) {
            onFragmentInteractionListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreateView() called with: inflater = [" + inflater + "], container = [" + container
                + "], savedInstanceState = [" + savedInstanceState + "]");

        View rootView = inflater.inflate(R.layout.fragment_web, container, false);

        /* set up app bar */

        FragmentActivity fragmentActivity = getActivity();
        assert fragmentActivity != null; // Fragment has been attached to Activity
        AppCompatActivity appCompatActivity;

        if (fragmentActivity instanceof AppCompatActivity) {
            appCompatActivity = (AppCompatActivity) fragmentActivity;
        } else {
            throw new RuntimeException(fragmentActivity.toString() + " must extend AppCompatActivity");
        }

        Toolbar toolbar = rootView.findViewById(R.id.webFragment_toolbar_appBar);
        toolbar.setLogo(R.drawable.logo);
        appCompatActivity.setSupportActionBar(toolbar);

        /* init WebView */

        webView = rootView.findViewById(R.id.webFragment_webView_webApp);
        initWebView(webView);

        /* load login page */

        // seems to be redundant with check for SharedPreference changes in onResume()
        // however, leaving this out would lead the first check to assume that the credentials
        // as well as the language changed (null -> something) so that the language would
        // be switched unintentionally

        lastUsernamePreference = defaultSharedPreferences.getString(SharedPreferenceKeys.USERNAME, null);
        lastPasswordPreference = defaultSharedPreferences.getString(SharedPreferenceKeys.PASSWORD, null);
        lastLanguagePreference = defaultSharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null);

        loadLoginPage(lastUsernamePreference, lastPasswordPreference);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        setHasOptionsMenu(true);

        defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreateOptionsMenu() called with: menu = [" + menu + "], inflater = [" + inflater + "]");

        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onDetach() called");

        onFragmentInteractionListener = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onOptionsItemSelected() called with: item = [" + item + "]");

        int optionsItemId = item.getItemId();

        switch (optionsItemId) {
            case R.id.mainMenu_alarm:
                AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
                if (alarmManager == null) {
                    Log.e(TAG, Thread.currentThread().getName() + " ### "
                            + "onOptionsItemSelected: Couldn't get AlarmManager. No alarm set.");
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

    /**
     * Check for any changes made to the default SharedPreferences while this fragment was paused.
     */
    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onResume() called");

        /* check for changes in default SharedPreferences & react if necessary */

        boolean usernameChanged = usernamePreferenceChanged();
        boolean passwordChanged = passwordPreferenceChanged();
        boolean languageChanged = languagePreferenceChanged();

        if ((usernameChanged || passwordChanged) && languageChanged) {
            loadLanguageSwitchPage(PAGE_LOGIN);

        } else if (usernameChanged || passwordChanged) {
            loadLoginPage(lastUsernamePreference, lastPasswordPreference);

        } else if (languageChanged) {
            loadLanguageSwitchPage(webView.getUrl());
        }
    }

    //
    // HELPER
    //

    /**
     * Reads the username from the default SharedPreferences and compares it with the one read
     * last time. If it hasn't been read before it's compared with null. Finally, saves the
     * current value.
     *
     * @return True if the username read from the default SharedPreferences has
     * changed since the last time. False otherwise.
     *
     * The current value and the old one are considered to be equal if String.equals() yields true
     * or if both are null.
     */
    private boolean usernamePreferenceChanged() {
        String currentUsernamePreference = defaultSharedPreferences.getString(SharedPreferenceKeys.USERNAME, null);

        boolean usernamePreferenceChanged = !TextUtils.equals(currentUsernamePreference, lastUsernamePreference);

        lastUsernamePreference = currentUsernamePreference;

        return usernamePreferenceChanged;
    }

    /**
     * Reads the password from the default SharedPreferences and compares it with the one read
     * last time. If it hasn't been read before it's compared with null. Finally, saves the
     * current value.
     *
     * @return True if the password read from the default SharedPreferences has
     * changed since the last time. False otherwise.
     *
     * The current value and the old one are considered to be equal if String.equals() yields true
     * or if both are null.
     */
    private boolean passwordPreferenceChanged() {
        String currentPasswordPreference = defaultSharedPreferences.getString(SharedPreferenceKeys.PASSWORD, null);

        boolean passwordPreferenceChanged = !TextUtils.equals(currentPasswordPreference, lastPasswordPreference);

        lastPasswordPreference = currentPasswordPreference;

        return passwordPreferenceChanged;
    }

    /**
     * Reads the language from the default SharedPreferences and compares it with the one read
     * last time. If it hasn't been read before it's compared with null. Finally, saves the
     * current value.
     *
     * @return True if the language read from the default SharedPreferences has
     * changed since the last time. False otherwise.
     *
     * The current value and the old one are considered to be equal if String.equals() yields true
     * or if both are null.
     */
    private boolean languagePreferenceChanged() {
        String currentLanguagePreference = defaultSharedPreferences.getString(SharedPreferenceKeys.LANGUAGE, null);

        boolean languagePreferenceChanged = !TextUtils.equals(currentLanguagePreference, lastLanguagePreference);

        lastLanguagePreference = currentLanguagePreference;

        return languagePreferenceChanged;
    }

    private void loadLanguageSwitchPage(String targetUrl) {
        Map<String, String> additionalHttpHeaders = new HashMap<>();
        additionalHttpHeaders.put("Referer", targetUrl);
        webView.loadUrl(PAGE_LANGUAGE_SWITCH, additionalHttpHeaders);
    }

    private void loadLoginPage(String username, String password) {
        String postData = "_username=" + (username == null ? "" : username)
                + "&_password=" + (password == null ? "" : password);
        webView.postUrl(PAGE_LOGIN, postData.getBytes());
    }

    /**
     * - enable JavaScript
     * - set custom HTTP user agent
     * - set onPageFinished handler that notifies the parent activity if the credentials are wrong
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void initWebView(WebView webView) {

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(HTTP_USER_AGENT);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, Thread.currentThread().getName() + " ### "
                        + "onPageFinished() called with: view = [" + view + "], url = [" + url + "]");

                // ignore if activity has been destroyed
                if (onFragmentInteractionListener != null) {

                    /* remove trailing slash from URL */

                    url = url.replaceAll("/$", "");

                    /* unexpected page (e.g. login because bad credentials) -> notify MainActivity */

                    if (!(url.equals(PAGE_MAIN) || url.equals(PAGE_TRANSACTIONS))) {
                        onFragmentInteractionListener.onLoginFailed();
                    }
                }
            }
        });
    }

    //
    // IMPLEMENTS OnBackPressedListener
    //

    @Override
    public boolean onBackPressed() {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onBackPressed() called");

        if (webView.canGoBack()) {
            webView.goBack();

            return true;
        } else {
            return false;
        }
    }

    //
    // INNER TYPES
    //

    interface OnFragmentInteractionListener {
        void onLoginFailed();
    }
}
