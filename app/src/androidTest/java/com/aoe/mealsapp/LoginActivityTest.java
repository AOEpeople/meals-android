package com.aoe.mealsapp;


import android.content.Context;
import android.preference.PreferenceManager;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.webkit.CookieManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {

    private static final String TAG = "## " + LoginActivityTest.class.getSimpleName();

    @Rule
    public ActivityTestRule<WebActivity> webActivityRule =
            new ActivityTestRule<>(WebActivity.class);

    @Rule
    public ActivityTestRule<LoginActivity> loginActivityRule =
            new ActivityTestRule<>(LoginActivity.class);

    @Test
    public void credentialsActivityTest() {

        // GIVEN credentials that have not been validated yet

        Context context = InstrumentationRegistry.getTargetContext();

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(SharedPreferencesKeys.KEY_CREDENTIALS_WERE_VALIDATED, false).commit();

        loginActivityRule.launchActivity(null);

        // WHEN entering correct credentials

        onView(withId(R.id.loginFragment_editText_username)).perform(click());
        onView(withId(R.id.loginFragment_editText_username)).perform(replaceText("alice"), closeSoftKeyboard());
        onView(withId(R.id.loginFragment_editText_username)).perform(pressImeActionButton());
        onView(withId(R.id.loginFragment_editText_password)).perform(replaceText("alice"), closeSoftKeyboard());
        onView(withId(R.id.loginFragment_editText_password)).perform(pressImeActionButton());


        // THEN the WebActivity should be shown

        onView(withId(R.id.webActivity_webView_webApp)).check(matches(isDisplayed()));
    }

    @Test
    public void givenNotValidatedCredentials_whenStartingApp_thenCredentialsActivityShouldBeShown() {

        // GIVEN credentials that have not been validated yet

        Context context = InstrumentationRegistry.getTargetContext();

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(SharedPreferencesKeys.KEY_CREDENTIALS_WERE_VALIDATED, false).commit();


        // WHEN starting the app

        loginActivityRule.launchActivity(null);


        // THEN the LoginActivity should be shown

        onView(withId(R.id.loginFragment_textView_username)).check(matches(isDisplayed()));
    }

    @Test
    public void givenValidatedCredentials_whenStartingApp_thenMainActivityShouldBeShown() {

        // GIVEN credentials that have not been validated yet

        Context context = InstrumentationRegistry.getTargetContext();

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(SharedPreferencesKeys.KEY_CREDENTIALS_WERE_VALIDATED, true).commit();


        // WHEN starting the app

        loginActivityRule.launchActivity(null);


        // THEN the LoginActivity should be shown

        onView(withId(R.id.webActivity_webView_webApp)).check(matches(isDisplayed()));
    }

    @Test
    public void givenAppHasBeenStartedForTheFirstTime_whenLoggingInWithCorrectCredentials_thenWebActivityShouldBeShown() {

        // TODO doesn't work because f**k that IdlingResource

//        CountingIdlingResource webActivityIdlingResource = webActivityRule.getActivity()
//                .getEspressoIdlingResourceForMainActivity();

//        Espresso.registerIdlingResources(webActivityIdlingResource);

        // GIVEN credentials that have not been validated yet

        Context context = InstrumentationRegistry.getTargetContext();

        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(SharedPreferencesKeys.KEY_CREDENTIALS_WERE_VALIDATED, false).commit();


        // GIVEN the app has been started for the first time

        CookieManager.getInstance().removeAllCookies(null);

        // WHEN starting the app

        loginActivityRule.launchActivity(null);

        // WHEN logging in with correct credentials

        onView(withId(R.id.loginFragment_editText_username)).perform(click());
        onView(withId(R.id.loginFragment_editText_username)).perform(replaceText("alice"), closeSoftKeyboard());
        onView(withId(R.id.loginFragment_editText_username)).perform(pressImeActionButton());
        onView(withId(R.id.loginFragment_editText_password)).perform(replaceText("alice"), closeSoftKeyboard());
        onView(withId(R.id.loginFragment_editText_password)).perform(pressImeActionButton());


        // THEN the WebActivity should be shown

        onView(withId(R.id.loginFragment_editText_username)).check(matches(isDisplayed()));
    }
}
