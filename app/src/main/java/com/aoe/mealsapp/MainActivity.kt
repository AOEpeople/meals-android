package com.aoe.mealsapp

import android.support.v7.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity(),
        WebFragment.OnFragmentInteractionListener, LoginFragment.OnFragmentInteractionListener {

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
