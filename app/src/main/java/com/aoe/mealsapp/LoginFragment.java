package com.aoe.mealsapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.aoe.mealsapp.settings.Settings;

/**
 * Activity that is shown on first startup for the user to enter his credentials. Will be started
 * whenever the credentials become invalid.
 */
public class LoginFragment extends Fragment implements View.OnClickListener, OnBackPressedListener {

    //
    // CONSTANTS
    //

    private static final String TAG = "LoginFragment";

    //
    // STATIC
    //

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }

    //
    // FIELDS & CONSTRUCTORS
    //

    private EditText editText_username;
    private EditText editText_password;

    private OnFragmentInteractionListener onFragmentInteractionListener;

    public LoginFragment() {
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

        final View rootView = inflater.inflate(R.layout.fragment_login, container, false);

        /* set up UI widgets */

        final Button button_login = rootView.findViewById(R.id.loginFragment_button_login);
        button_login.setOnClickListener(this);

        editText_username = rootView.findViewById(R.id.loginFragment_editText_username);
        editText_username.setText(Settings.Companion.getInstance(getContext()).getUsername());

        editText_password = rootView.findViewById(R.id.loginFragment_editText_password);
        editText_password.setText(Settings.Companion.getInstance(getContext()).getPassword());

        /* set up last EditText to login on clicking the Done key */

        editText_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Log.d(TAG, Thread.currentThread().getName() + " ### "
                        + "onEditorAction() called with: textView = [" + textView + "], actionId = ["
                        + actionId + "], keyEvent = [" + keyEvent + "]");

                boolean handled = false;

                if (actionId == EditorInfo.IME_ACTION_SEND) {
                    button_login.performClick();

                    handled = true;
                }

                return handled;
            }
        });

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onDetach() called");

        onFragmentInteractionListener = null;
    }

    //
    // IMPLEMENTS View.OnClickListener
    //

    @Override
    public void onClick(View view) {
        Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onClick() called with: view = [" + view + "]");

        switch (view.getId()) {
            case R.id.loginFragment_button_login:
                storeCredentialsAndNotifiyActivity();
                break;
        }
    }

    //
    // HELPER
    //

    private void storeCredentialsAndNotifiyActivity() {

        String username = editText_username.getText().toString();
        String password = editText_password.getText().toString();

        Settings.Companion.getInstance(getContext()).setUsername(username);
        Settings.Companion.getInstance(getContext()).setPassword(password);

        onFragmentInteractionListener.onLoginClicked();
    }

    //
    // IMPLEMENTS OnBackPressedListener
    //

    @Override
    public boolean onBackPressed() {
        onFragmentInteractionListener.onLeaveLogin();

        return true;
    }

    //
    // INNER TYPES
    //

    public interface OnFragmentInteractionListener {
        void onLoginClicked();
        void onLeaveLogin();
    }
}
