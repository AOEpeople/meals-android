package com.aoe.mealsapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        setContentView(R.layout.activity_login)
    }

    private companion object {
        private const val TAG = "LoginActivity"
    }
}
