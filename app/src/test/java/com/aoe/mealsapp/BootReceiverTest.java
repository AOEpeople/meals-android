package com.aoe.mealsapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.ByteArrayInputStream;
import java.util.Calendar;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class, PendingIntent.class})
public class BootReceiverTest {

    private BootReceiver bootReceiver;

    private Context context;
    private Resources resources;
    private AlarmManager alarmManager;

    private Intent intent;

    @Before
    public void enableStaticMocking() {
        PowerMockito.mockStatic(Log.class);
        PowerMockito.mockStatic(PendingIntent.class);
    }

    @Before
    public void setUp() {

        // GIVEN a BootReceiver

        bootReceiver = new BootReceiver();

        // GIVEN a Context whose
        // - getResources().openRawResource() returns an InputStream that mimics the .config file
        // - getSystemService() returns an AlarmManager

        context = mock(Context.class);
        when(context.getResources()).thenReturn(resources = mock(Resources.class));
        when(context.getSystemService(anyString())).thenReturn(alarmManager = mock(AlarmManager.class));

        // GIVEN an Intent

        intent = mock(Intent.class);

        // GIVEN that PendingIntent.getBroadcast returns a PendingIntent

        when(PendingIntent.getBroadcast(any(Context.class), anyInt(), any(Intent.class), anyInt()))
                .thenReturn(mock(PendingIntent.class));
    }

    @Test
    public void onReceive_valid() throws Exception {

        // GIVEN a config file with "reminderTime=1:00 PM"
        // GIVEN a BOOT_COMPLETED Intent

        when(resources.openRawResource(anyInt())).thenReturn(
                new ByteArrayInputStream("reminderTime=1:00 PM".getBytes()));

        when(intent.getAction()).thenReturn("android.intent.action.BOOT_COMPLETED");


        // WHEN BootReceiver.onReceive() is invoked with the Context and the Intent

        bootReceiver.onReceive(context, intent);


        // THEN AlarmManager.setInexactRepeating() should be called with correct parameters

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 13);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        verify(alarmManager).setInexactRepeating(
                eq(AlarmManager.RTC_WAKEUP),
                eq(calendar.getTimeInMillis()),
                eq(AlarmManager.INTERVAL_DAY),
                any(PendingIntent.class));
    }

    @Test
    public void onReceive_wrongIntent() throws Exception {

        // GIVEN a non-BOOT_COMPLETED Intent

        when(intent.getAction()).thenReturn("android.intent.action.INPUT_METHOD_CHANGED");


        // WHEN BootReceiver.onReceive() is invoked with the Context and the Intent

        bootReceiver.onReceive(context, intent);


        // THEN AlarmManager.setInexactRepeating() should not be called

        verify(alarmManager, never()).setInexactRepeating(anyInt(), anyLong(), anyLong(), any(PendingIntent.class));
    }

    @Test
    public void onReceive_invalidConfigFile() throws Exception {

        // GIVEN a config file with invalid content
        // GIVEN a BOOT_COMPLETED Intent

        when(resources.openRawResource(anyInt())).thenReturn(
                new ByteArrayInputStream("reminderTime=".getBytes()));

        when(intent.getAction()).thenReturn("android.intent.action.BOOT_COMPLETED");


        // WHEN BootReceiver.onReceive() is invoked with the Context and the Intent

        bootReceiver.onReceive(context, intent);


        // THEN an error message should be logged
        // THEN AlarmManager.setInexactRepeating() shouldn't be called

        PowerMockito.verifyStatic();
        Log.e(anyString(), anyString(), any(Throwable.class));

        verify(alarmManager, never()).setInexactRepeating(anyInt(), anyLong(), anyLong(), any(PendingIntent.class));
    }
}