package com.aoe.mealsapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.StringRes
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder

object Notifications {

    private const val REQUEST_CODE = 1

    /* notification channel */

    private const val CHANNEL_ID = "CHANNEL_ID"

    @StringRes
    private const val CHANNEL_NAME = R.string.notifications_channelName

    /* reminder notification */

    private const val NOTIFICATION_ID = 1

    @StringRes
    private const val NOTIFICATION_TITLE = R.string.notifications_notificationTitle

    @StringRes
    private const val NOTIFICATION_TEXT = R.string.notifications_notificationText

    /* server unavailable notification */

    private const val UNAVAILABLE_NOTIFICATION_ID = 2

    @StringRes
    private const val UNAVAILABLE_NOTIFICATION_TITLE = R.string.notifications_unavailableNotificationTitle

    @StringRes
    private const val UNAVAILABLE_NOTIFICATION_TEXT = R.string.notifications_unavailableNotificationText

    /* */

    fun createNotificationChannel(context: Context) {

        // notification channels available since Android 8.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            /* create notification channel */

            val channelName = context.getString(CHANNEL_NAME)

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, channelName, importance)

            /* register notification channel */

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showMealsNotification(context: Context) {

        /* create notification */

        val notificationTitle = context.getString(NOTIFICATION_TITLE)
        val notificationText = context.getString(NOTIFICATION_TEXT)

        val intent = Intent(context, WebActivity::class.java)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addNextIntentWithParentStack(intent)
        val pendingIntent = taskStackBuilder.getPendingIntent(
                REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // used until Android 7.1
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        /* show notification */

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(NOTIFICATION_ID, notification)
    }

    fun showServerUnavailableNotification(context: Context) {

        /* create notification */

        val notificationTitle = context.getString(UNAVAILABLE_NOTIFICATION_TITLE)
        val notificationText = context.getString(UNAVAILABLE_NOTIFICATION_TEXT)

        val intent = Intent(context, WebActivity::class.java)
        val taskStackBuilder = TaskStackBuilder.create(context)
        taskStackBuilder.addNextIntentWithParentStack(intent)
        val pendingIntent = taskStackBuilder.getPendingIntent(
                REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.notification)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // used until Android 7.1
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        /* show notification */

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(UNAVAILABLE_NOTIFICATION_ID, notification)
    }
}
