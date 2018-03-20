package com.lesjaw.wamonitoring.utils;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by lesjaw@gmail.com on 14/09/2017.
 */

public class Utils {

    public static String getTimeAgo(Calendar ref) {
        Calendar now = Calendar.getInstance();

        long milliseconds1 = ref.getTimeInMillis();
        long milliseconds2 = now.getTimeInMillis();
        long diff = milliseconds2 - milliseconds1;

        long diffSeconds = diff / 1000;
        //Log.d("DEBUG", "getTimeAgo: " + diffSeconds);
        return getScaledTime(diffSeconds);
    }

    public static String getScaledTime(long diffSeconds) {
        if (diffSeconds < 120)
            return "" + diffSeconds + " sec." + " ago";

        long diffMinutes = diffSeconds / 60;
        if (diffMinutes < 120)
            return "" + diffMinutes + " min."+ " ago";

        long diffHours = diffMinutes / (60);
        if (diffHours < 24)
            return "" + diffHours + " hours"+ " ago";

        long diffDays = diffHours / (24);
        if (diffDays == 1)
            return "Yesterday";

        return "" + diffDays + " Days"+ " ago";
    }

    public static String getDuration(long typicalOnDurationMsec) {
        long secondi = typicalOnDurationMsec;
        if (secondi < 60)
            return "" + secondi + " sec.";

        long diffMinutes = secondi / 60;
        secondi = secondi % 60;//resto
        if (diffMinutes < 120)
            return "" + diffMinutes + " minute & " + secondi + " second";
        long diffHours = diffMinutes / (60);
        diffMinutes = diffMinutes % 60;
        return "" + diffHours + " hours " + diffMinutes + " minute";
        //return null;
    }

    public static String getJarak (int jarak){
        int meter = jarak;
        if (meter < 1000)
            return meter+" m";

        int km = meter/1000;
        if (km >= 1)
            return km+" km";

        return jarak +" m";
    }

    public static  String address (double lat, double lot, Context context){

        double latitude = lat;
        double longitude = lot;

        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String adString = null;
        try {
            List<Address> list;
            list = geocoder.getFromLocation(latitude, longitude, 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                address.getLocality();

                if (address.getAddressLine(0) != null)
                    adString = address.getAddressLine(0);

            }


        } catch (final IOException e) {
            new Thread(() -> Log.e("DEBUG", "Geocoder ERROR", e)).start();
        }

        return adString;

    }
}
