package com.aoe.mealsapp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity
        implements WebFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener {

    private static final String TAG = "## " + MainActivity.class.getSimpleName();

    //
    // EXTENDS AppCompatActivity
    //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        setContentView(R.layout.activity_main);

        /* set default preference values on first app launch */

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        /* show WebFragment initially */

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainActivity_frameLayout_fragmentContainer, WebFragment.newInstance())
                .commit();
    }

    //
    // IMPLEMENTS WebFragment.OnFragmentInteractionListener
    //

    @Override
    public void onLoginFailed() {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onLoginFailed() called");
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainActivity_frameLayout_fragmentContainer, LoginFragment.newInstance())
                .addToBackStack(null)
                .commit();
    }

    //
    // IMPLEMENTS LoginFragment.OnFragmentInteractionListener
    //

    @Override
    public void onLoginClicked() {
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onLoginClicked() called");
        getSupportFragmentManager().popBackStack();
    }
}
