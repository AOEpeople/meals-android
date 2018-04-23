package com.aoe.mealsapp;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity
        implements WebFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";

    //
    // EXTENDS AppCompatActivity
    //

    /**
     * Notifies the fragment that is on top of the fragment back stack if that fragment implements
     * {@link OnBackPressedListener}.
     * <p>
     * To work, the fragment must have been added with it's tag name to the back stack. Might not
     * work if the fragment is contained multiple times in the back stack.
     */
    @Override
    public void onBackPressed() {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onBackPressed() called");

        FragmentManager fragmentManager = getSupportFragmentManager();

        /* peek fragment on top of fragment back stack */

        int topBackStackEntryIndex = fragmentManager.getBackStackEntryCount() - 1;
        FragmentManager.BackStackEntry topBackStackEntry = fragmentManager.getBackStackEntryAt(topBackStackEntryIndex);
        Fragment topFragment = fragmentManager.findFragmentByTag(topBackStackEntry.getName());

        /* notify fragment & handle back press if not handled by fragment */

        if (topFragment instanceof OnBackPressedListener) {
            boolean backPressedHandled = ((OnBackPressedListener) topFragment).onBackPressed();

            if (!backPressedHandled) {
                if (fragmentManager.getBackStackEntryCount() == 1) {
                    // close if only one fragment left
                    // prevents the screen from going blank when last fragment would be removed
                    finish();
                } else {
                    super.onBackPressed();
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        setContentView(R.layout.activity_main);

        /* set default preference values on first app launch */

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        /* show WebFragment initially */

        WebFragment webFragment = WebFragment.newInstance();
        String fragmentTag = webFragment.getClass().getCanonicalName();

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.mainActivity_frameLayout_fragmentContainer, webFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit();
    }

    //
    // IMPLEMENTS WebFragment.OnFragmentInteractionListener
    //

    @Override
    public void onLoginFailed() {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onLoginFailed() called");

        LoginFragment loginFragment = LoginFragment.newInstance();
        String fragmentTag = loginFragment.getClass().getCanonicalName();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.mainActivity_frameLayout_fragmentContainer, loginFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commitAllowingStateLoss();
    }

    //
    // IMPLEMENTS LoginFragment.OnFragmentInteractionListener
    //

    @Override
    public void onLoginClicked() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void onLeaveLogin() {
        finish();
    }
}
