# meals-android
Android wrapper for the web app

# Architecture

The most important components of the app are the activities (MainActivity, SettingsActivity), the BroadcastReceivers (AlarmReceiver, BootReceiver) and the SharedPreferences store.

The MainActivity normally holds the WebFragment which contains a WebView that displays the Meals web app. It is loaded when the user starts the app and gets the user's credentials from the SharedPreference store. If no credentials exist or the existing credentials are invalid the LoginFragment is presented to the user.

The WebFragment also shows an app bar with a menu. There the user can click the settings item to open the SettingsActivity. The SettingsActivity contains the SettingsFragment which loads its UI from the preferences.xml file. There the user can change his credentials, the web app's language and the reminder frequency.

Changing the reminder frequency sets the appropriate alarm that is triggered the day the user wants to be notified (before Mondays / before any weekday / never). When the alarm is triggered the AlarmReceiver's onReceive() method is called that sends a request to the Meals server to determine whether the user is already registered for meal. If not, a notification is added to the notification tray. Clicking it opens the app.

The server requests consists of two HTTP requests: a POST request that performs an OAuth login and returns the OAuth token and a GET request that uses the OAuth token to get the users meals participation for next week as a JSON string. That string is parsed for the relevant information: whether the user participates the next day.

Because all alarms removed when the device reboots the BootReceiver will set the alarm again as soon as the system boots.

Any preference changes are stored in the app's default SharedPreferences. The WebFragment checks these for changes when it resumes and reloads the web page accordingly.
