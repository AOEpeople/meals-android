package com.aoe.mealsapp.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aoe.mealsapp.AlarmReceiver
import java.util.*

object Alarms {

    const val REQUEST_CODE_REMINDER_ALARM = 1
    const val REQUEST_CODE_RETRY_ALARM = 2
    const val REQUEST_CODE_REMOVE_NOTIFICATION_ALARM = 3

    const val EXTRA_REQUEST_CODE = "EXTRA_REQUEST_CODE"

    /**
     * Activate the daily alarm that triggers the participation check.
     */
    fun activateReminderAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                Config.readTime(context, Config.REMINDER_TIME).timeInMillis,
                AlarmManager.INTERVAL_DAY,
                createAlarmIntent(context, REQUEST_CODE_REMINDER_ALARM))
    }

    /**
     * Activate the alarm that triggers a retry in case the normal reminder alarm failed
     * after 5 minutes.
     */
    fun setRetryAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                Calendar.getInstance().timeInMillis + 1000 * 60 * 5,
                createAlarmIntent(context, REQUEST_CODE_RETRY_ALARM))
    }

    /**
     * Activate the daily alarm that removes the "You haven't registered, yet" and
     * "Server unavailable. Check manually." alarms from the notification tray when it's
     * too late to register.
     */
    fun activateRemoverAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                Config.readTime(context, Config.DEADLINE).timeInMillis,
                AlarmManager.INTERVAL_DAY,
                createAlarmIntent(context, REQUEST_CODE_REMOVE_NOTIFICATION_ALARM))
    }

    private fun createAlarmIntent(context: Context, requestCode: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)
                .putExtra(EXTRA_REQUEST_CODE, requestCode)

        return PendingIntent.getBroadcast(context, requestCode, intent, 0)
    }
}
