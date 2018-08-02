package com.aoe.mealsapp

import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import com.aoe.mealsapp.settings.Settings
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var editText_username: EditText
    private lateinit var editText_password: EditText

    private lateinit var oldUsername: String
    private lateinit var oldPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        setContentView(R.layout.activity_login)

        /* init UI widgets */

        val button_login = loginActivity_button_login
        button_login.setOnClickListener(this)

        val settings = Settings.getInstance(this)

        editText_username = loginActivity_editText_username
        editText_username.setText(settings.username)
        oldUsername = settings.username

        editText_password = loginActivity_editText_password
        editText_password.setText(settings.password)
        oldPassword = settings.password

        /* click 'Done' -> login */

        editText_password.setOnEditorActionListener { textView, actionId, keyEvent ->
            Log.d(TAG, Thread.currentThread().name + " ### " +
                    "onCreate() called with: textView = [$textView], actionId = [$actionId]" +
                    ", keyEvent = [$keyEvent]")

            if (actionId == EditorInfo.IME_ACTION_SEND) {
                button_login.performClick()

                true // handled
            } else {
                false // not handled
            }
        }
    }

    override fun onClick(view: View?) {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onClick() called with: view = [$view]")

        when (view!!.id) {
            R.id.loginActivity_button_login -> {

                val newUsername = editText_username.text.toString()
                val newPassword = editText_password.text.toString()

                if (oldUsername != newUsername || oldPassword != newPassword) {

                    /* store credentials */

                    val settings = Settings.getInstance(this)

                    settings.username = newUsername
                    settings.password = newPassword

                    /* close activity */

                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        ActivityCompat.finishAffinity(this)
    }

    private companion object {
        private const val TAG = "LoginActivity"
    }
}
