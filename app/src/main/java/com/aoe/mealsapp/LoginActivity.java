package com.aoe.mealsapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Activity that is shown on first startup for the user to enter his credentials. Will be started
 * whenever the credentials become invalid.
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "## " + LoginActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

        setContentView(R.layout.activity_login);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* skip this activity if credentials are valid */

        boolean validCredentials = sharedPreferences.getBoolean(SharedPreferenceKeys.CREDENTIALS_WERE_VALIDATED, false);

        if (validCredentials)
            startActivity(new Intent(this, WebActivity.class));

        /* set up toolbar */

        Toolbar toolbar = findViewById(R.id.webActivity_toolbar_appBar);
        toolbar.setLogo(R.drawable.logo);
        setSupportActionBar(toolbar);

        /* autofill credentials */

        EditText editText_username = findViewById(R.id.loginActivity_editText_username);
        EditText editText_password = findViewById(R.id.loginActivity_editText_password);

        editText_username.setText(sharedPreferences.getString(SharedPreferenceKeys.USERNAME, ""));
        editText_password.setText(sharedPreferences.getString(SharedPreferenceKeys.PASSWORD, ""));

        /* set up last EditText to login on clicking the Done key */

        editText_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Log.d(TAG, Thread.currentThread().getName() + ": "
                        + "onEditorAction() called with: textView = [" + textView + "], actionId = ["
                        + actionId + "], keyEvent = [" + keyEvent + "]");

                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    Button button_login = findViewById(R.id.loginActivity_button_login);
                    button_login.performClick();

                    handled = true;
                }

                return handled;
            }
        });

        /* set up button */

        Button button_login = findViewById(R.id.loginActivity_button_login);
        button_login.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("ApplySharedPref")
            @Override
            public void onClick(View view) {
                Log.d(TAG, Thread.currentThread().getName() + ": "
                        + "onClick() called with: view = [" + view + "]");

                /* save username, password in preferences & start WebActivity */

                SharedPreferences sharedPreferences
                        = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this);

                EditText editText_username = findViewById(R.id.loginActivity_editText_username);
                String username = editText_username.getText().toString();
                sharedPreferences.edit().putString(SharedPreferenceKeys.USERNAME, username).commit();

                EditText editText_password = findViewById(R.id.loginActivity_editText_password);
                String password = editText_password.getText().toString();
                sharedPreferences.edit().putString(SharedPreferenceKeys.PASSWORD, password).commit();

                startActivity(new Intent(LoginActivity.this, WebActivity.class));
            }
        });
    }

    /**
     * Finish this activity when starting another so that pressing the back button on the device
     * does not return to this login activity. It will be started on first startup and whenever
     * the credentials become invalid.
     *
     * @param intent
     */
    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        Log.d(TAG, Thread.currentThread().getName() + ": "
                + "startActivity() called with: intent = [" + intent + "]");

        finish();
    }
}
