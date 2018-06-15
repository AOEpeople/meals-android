package com.aoe.mealsapp

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import com.aoe.mealsapp.settings.Settings

/**
 * Fragment that is shown on first startup for the user to enter his credentials. Will be started
 * whenever the credentials become invalid.
 */
class LoginFragment : Fragment(), View.OnClickListener, OnBackPressedListener {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText

    private var onFragmentInteractionListener: OnFragmentInteractionListener? = null

    // region ### attach/detach OnFragmentInteractionListener
    //

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onAttach() called with: context = [$context]")

        if (context is OnFragmentInteractionListener) {
            onFragmentInteractionListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onDetach() called")

        onFragmentInteractionListener = null
    }

    //
    // endregion

    // region ### init UI
    //

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreateView() called with: inflater = [$inflater], container = [$container]" +
                ", savedInstanceState = [$savedInstanceState]")

        val rootView = inflater.inflate(R.layout.fragment_login, container, false)

        /* init UI widgets */

        val loginButton = rootView.findViewById<Button>(R.id.loginFragment_button_login)
        loginButton.setOnClickListener(this)

        val settings = Settings.getInstance(context!!)

        usernameEditText = rootView.findViewById(R.id.loginFragment_editText_username)
        usernameEditText.setText(settings.username)

        passwordEditText = rootView.findViewById(R.id.loginFragment_editText_password)
        passwordEditText.setText(settings.password)

        /* click 'Done' -> login */

        passwordEditText.setOnEditorActionListener { textView, actionId, keyEvent ->
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onCreateView() called with: textView = [$textView], actionId = [$actionId]" +
                    ", keyEvent = [$keyEvent]")

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                loginButton.performClick()

                true // handled
            } else {
                false // not handled
            }
        }

        /* */

        return rootView
    }

    override fun onClick(view: View) {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onClick() called with: view = [$view]")

        when (view.id) {
            R.id.loginFragment_button_login -> {

                /* store credentials */

                val settings = Settings.getInstance(context!!)

                settings.username = usernameEditText.text.toString()
                settings.password = passwordEditText.text.toString()

                /* notify activity */

                onFragmentInteractionListener!!.onLoginClicked()
            }
        }
    }

    override fun onBackPressed(): Boolean {
        onFragmentInteractionListener!!.onLeaveLogin()

        return true
    }

    //
    // endregion

    interface OnFragmentInteractionListener {
        fun onLoginClicked()
        fun onLeaveLogin()
    }

    companion object {
        private const val TAG = "LoginFragment"

        fun newInstance(): LoginFragment {
            return LoginFragment()
        }
    }
}
