package com.aoe.mealsapp

import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity(),
        WebFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener {

    // region ### onCreate()
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        setContentView(R.layout.activity_main)

        /* set default preference values on first app launch */

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false)

        /* show WebFragment initially */

        val webFragment = WebFragment.newInstance()
        val fragmentTag = webFragment.javaClass.canonicalName

        supportFragmentManager
                .beginTransaction()
                .add(R.id.mainActivity_frameLayout_fragmentContainer, webFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commit()
    }

    //
    // endregion

    // region ### onBackPressed()
    //

    /**
     * Notifies the fragment that is on top of the fragment back stack if that fragment implements
     * [OnBackPressedListener].
     *
     * To work, the fragment must have been added with it's tag name to the back stack. Might not
     * work if the fragment is contained multiple times in the back stack.
     */
    override fun onBackPressed() {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onBackPressed() called")

        /* peek fragment on top of fragment back stack */

        val topBackStackEntryIndex = supportFragmentManager.backStackEntryCount - 1
        val topBackStackEntry = supportFragmentManager.getBackStackEntryAt(topBackStackEntryIndex)
        val topFragment = supportFragmentManager.findFragmentByTag(topBackStackEntry.name)

        /* notify fragment & handle back press if not handled by fragment */

        if (topFragment is OnBackPressedListener) {
            val backPressedHandled = (topFragment as OnBackPressedListener).onBackPressed()

            if (!backPressedHandled) {
                if (supportFragmentManager.backStackEntryCount == 1) {
                    // close if only one fragment left
                    // prevents the screen from going blank when last fragment would be removed
                    finish()
                } else {
                    super.onBackPressed()
                }
            }
        } else {
            super.onBackPressed()
        }
    }

    //
    // endregion

    // region ### WebFragment.OnFragmentInteractionListener
    //

    override fun onLoginFailed() {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onLoginFailed() called")

        val loginFragment = LoginFragment.newInstance()
        val fragmentTag = loginFragment.javaClass.canonicalName

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.mainActivity_frameLayout_fragmentContainer, loginFragment, fragmentTag)
                .addToBackStack(fragmentTag)
                .commitAllowingStateLoss()
    }

    //
    // endregion

    // region ### LoginFragment.OnFragmentInteractionListener
    //

    override fun onLoginClicked() {
        supportFragmentManager.popBackStack()
    }

    override fun onLeaveLogin() {
        finish()
    }

    //
    // endregion

    private companion object {
        private const val TAG = "MainActivity"
    }
}
