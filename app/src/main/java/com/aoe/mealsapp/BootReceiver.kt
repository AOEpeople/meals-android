package com.aoe.mealsapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

import com.aoe.mealsapp.util.Alarm

class BootReceiver : BroadcastReceiver() {

    /**
     * Called if BootReceiver has been enabled. Checks received intent for security reasons,
     * reads reminder time from config.properties and sets alarm at reminder time which
     * repeats daily.
     */
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, Thread.currentThread().name + " ### " +
                "onReceive() called with: context = [$context], intent = [$intent]")

        if (intent.action != null && intent.action == "android.intent.action.BOOT_COMPLETED") {
            Alarm.setDailyAlarm(context)
        }
    }

    private companion object {
        private const val TAG = "BootReceiver"
    }
}