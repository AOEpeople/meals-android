package com.aoe.mealsapp.settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, Thread.currentThread().name + " ### "
                + "onCreate() called with: savedInstanceState = [$savedInstanceState]")

        fragmentManager
                .beginTransaction()
                .replace(android.R.id.content, SettingsFragment())
                .commit()
    }

    private companion object {
        private const val TAG = "SettingsActivity"
    }
}