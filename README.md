# meals-android
Android wrapper for the web app

# Architecture

## Overview

The most important components of the app are the activities (MainActivity, SettingsActivity), the BroadcastReceivers (AlarmReceiver, BootReceiver) and the SharedPreferences store.

The MainActivity normally holds the WebFragment which contains a WebView that displays the Meals web app. It is loaded when the user starts the app and gets the user's credentials from the SharedPreference store. If no credentials exist or the existing credentials are invalid the LoginFragment is presented to the user.

The WebFragment also shows an app bar with a menu. There the user can click the settings item to open the SettingsActivity. The SettingsActivity contains the SettingsFragment which loads its UI from the preferences.xml file. There the user can change his credentials, the web app's language and the reminder frequency.

Changing the reminder frequency sets the appropriate alarm that is triggered the day the user wants to be notified (before Mondays / before any weekday / never). When the alarm is triggered the AlarmReceiver's onReceive() method is called that sends a request to the Meals server to determine whether the user is already registered for meal. If not, a notification is added to the notification tray. Clicking it opens the app.

The server requests consists of two HTTP requests: a POST request that performs an OAuth login and returns the OAuth token and a GET request that uses the OAuth token to get the users meals participation for next week as a JSON string. That string is parsed for the relevant information: whether the user participates the next day.

Because all alarms removed when the device reboots the BootReceiver will set the alarm again as soon as the system boots.

Any preference changes are stored in the app's default SharedPreferences. The WebFragment checks these for changes when it resumes and reloads the web page accordingly.

## Alarm

The app sets an alarm that is triggered daily at a pre-set reminder time which is defined in the config file. It is set to some hours before the registration period ends.

The alarm is set on the first app startup (App.onCreate()). As alarms are deleted when the system reboots the alarm is also set after booting (BootReceiver.onReceive()).

As a consequence not every triggered alarm will lead to a server request depending on the user's setting for the reminder frequency. If the user decides that he doesn't want to be notified at all then nothing will be done on any day's alarm.

Alternatively one might have chosen to only set alarms for days on which the server needs to be requested. In that case, however, multiple, weekly alarms would have to be set to cover reminder frequencies like "before every weekday" which would make the code more complicated. For the same reason the daily alarm isn't deactivated when the user choses "never" as the reminder frequency. This might be improved by using a JobScheduler (https://developer.android.com/topic/performance/scheduling.html) when targeting API level 21+.

So, it's super simple: The alarm fires every day. Then, it's determined based on the set reminder frequency whether the server is requested or not.

Finally, if the app is first started or the device booted after the set reminder time the reminder functionality will still be executed if the latest possible reminder time (shortly before the registration period ends) hasn't passed, yet.

Note: On Android, especially on the latest versions, alarms are inexact by design so that the system can batch alarms that are timed close to each other to reduce energy consumption. In theory an inexact alarm can be triggered up 150% too late (i.e. an alarm set to be triggered in one day might be triggered after 2.5 days). In practice alarms are delayed at most by 10-15 minutes.

## Server Request

When the alarm is triggered the app checks whether the user wants to be notified for the next day. If the next day is a weekday and the user set a reminder frequency that covers the next day this will be true.

In that case the app sends two server requests to determine whether the user is already registered for tomorrows meal:
1. A POST login request to receive a valid OAuth token
2. A GET current-week request (with the token) to receive the user's meal participation for the next week(s)

The app then extracts the participation value for the next day and notifies the user if it's false.

If a problem occurs when contacting the server, for example if the server is not available because the device is not connected to the network, the app will try to contact the server again every 5 minutes until it succeeds or the latest reminder time is passed.

## Notification

If the the user wants to be notified for the next day and the server request yields that he hasn't registered yet the user is remembered with a notification. Clicking the notification in the notification tray opens the web view.

Since Android 8.0 each app has one or multiple categories that a notification belongs to. Therefore, the app registers a single category on startup that the notification belongs to.

# Coding Guidelines

## Logging

To faciliate debugging every method that is invoked by the operating system logs the current thread, its name and its parameters. The only exception are methods that would obviously flood the log file like RecyclerView.Adapter.getItemCount().

For this purpose Android Studio provides Live Templates like 'logm' which generates code like this:

    Log.d(TAG, "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");

This project uses extended versions of these Live Templates that include the information on which thread the method is called. The previous 'logm' example is extended to 'logmt' that produces the following code:

    Log.d(TAG, Thread.currentThread().getName() + " ### "
                + "onCreate() called with: savedInstanceState = [" + savedInstanceState + "]");
                
The same applies to the other Live Templates ('logd' becomes 'logdt', 'loge' becomes 'loget', etc.)

Note that the thread name is separated from the method information by the sequence '###'. This allows for easily filtering the log to effectively hide system log entries.

Also, every exception is logged using 'loget':

    } catch (IOException | ParseException e) {
        Log.e(TAG, Thread.currentThread().getName() + " ### "
                + "accept: Couldn't read reminder time from config file. No alarm set.", e);
        return;
    }
