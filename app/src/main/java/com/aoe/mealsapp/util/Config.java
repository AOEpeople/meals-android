package com.aoe.mealsapp.util;

import android.content.Context;

import com.aoe.mealsapp.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Locale;

public class Config {

    private static final String REMINDER_TIME = "reminderTime";
    private static final String LATEST_REMINDER_TIME = "latestReminderTime";

    /**
     * @param context Necessary to access the config file.
     * @return A Calendar object with today's date and the config's time.
     * @throws IOException Failed to read the config file.
     * @throws ParseException Failed to parse the config file.
     */
    public static Calendar readReminderTime(Context context) throws IOException, ParseException {

        java.util.Properties properties = new java.util.Properties();
        properties.load(context.getResources().openRawResource(R.raw.config));

        /* create Calendar object with today's date and config's time */

        Calendar parsedTime = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        String s = dateFormat.format(parsedTime.getTime());
        parsedTime.setTime(dateFormat.parse("1:00 PM"));

        Calendar reminderTime = Calendar.getInstance();
        reminderTime.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY));
        reminderTime.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE));
        reminderTime.set(Calendar.SECOND, parsedTime.get(Calendar.SECOND));
        reminderTime.set(Calendar.MILLISECOND, parsedTime.get(Calendar.MILLISECOND));

        return reminderTime;
    }

    /**
     * @param context Necessary to access the config file.
     * @return A Calendar object with today's date and the config's time.
     * @throws IOException Failed to read the config file.
     * @throws ParseException Failed to parse the config file.
     */
    public static Calendar readLatestReminderTime(Context context) throws IOException, ParseException {

        java.util.Properties properties = new java.util.Properties();
        properties.load(context.getResources().openRawResource(R.raw.config));

        /* create Calendar object with today's date and config's time */

        Calendar parsedTime = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US);
        parsedTime.setTime(dateFormat.parse(properties.getProperty(LATEST_REMINDER_TIME)));

        Calendar latestReminderTime = Calendar.getInstance();
        latestReminderTime.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY));
        latestReminderTime.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE));
        latestReminderTime.set(Calendar.SECOND, parsedTime.get(Calendar.SECOND));
        latestReminderTime.set(Calendar.MILLISECOND, parsedTime.get(Calendar.MILLISECOND));

        return latestReminderTime;
    }
}
