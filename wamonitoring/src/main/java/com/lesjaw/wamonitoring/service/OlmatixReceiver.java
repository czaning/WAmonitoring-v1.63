package com.lesjaw.wamonitoring.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.text.SimpleDateFormat;

/**
 * Created by lesjaw@gmail.com on 21/09/2017.
 */

public class OlmatixReceiver extends BroadcastReceiver {


    OlmatixAlarmReceiver alarm;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        Log.d("DEBUG", "onReceive: "+intent);

        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent serviceIntent = new Intent(context, wamonitorservice.class);
            context.startService(serviceIntent);
            alarm = new OlmatixAlarmReceiver();
            alarm.setAlarm(context);
        }

    }


}