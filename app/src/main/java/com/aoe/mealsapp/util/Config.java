package com.aoe.mealsapp.util;

import android.content.Context;

import com.aoe.mealsapp.R;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Properties;

public class Config {

    public static final String REMINDER_TIME = "reminderTime";
    public static final String LATEST_REMINDER_TIME = "latestReminderTime";

    /**
     * Read a time property with the time's format being H:mm (e.g. 9:05).
     *
     * @param context Necessary to access the config file.
     * @return A Calendar object with today's date and the config's time.
     * @throws IOException Failed to read the config file.
     * @throws ParseException Failed to parse the config file.
     */
    public static Calendar readTime(Context context, String key) throws IOException, ParseException {

        Properties properties = new Properties();
        properties.load(context.getResources().openRawResource(R.raw.config));

        /* create Calendar object with today's date and config's time */

        Calendar parsedTime = Calendar.getInstance();
        DateFormat dateFormat = new SimpleDateFormat("H:mm", Locale.US);
        parsedTime.setTime(dateFormat.parse(properties.getProperty(key)));

        Calendar time = Calendar.getInstance();
        time.set(Calendar.HOUR_OF_DAY, parsedTime.get(Calendar.HOUR_OF_DAY));
        time.set(Calendar.MINUTE, parsedTime.get(Calendar.MINUTE));
        time.set(Calendar.SECOND, parsedTime.get(Calendar.SECOND));
        time.set(Calendar.MILLISECOND, parsedTime.get(Calendar.MILLISECOND));

        return time;
    }
}
