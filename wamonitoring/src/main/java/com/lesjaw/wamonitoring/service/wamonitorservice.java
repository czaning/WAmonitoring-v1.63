package com.lesjaw.wamonitoring.service;

import android.Manifest;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.SplashActivity;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class wamonitorservice extends Service implements LocationListener {

    boolean isGPS = false;
    boolean isNetwork = false;
    boolean isPassive = false;

    boolean canGetLocation = true;
    LocationManager locationManager;
    Location loc;
    final String TAG = "GPS service";
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 30;
    SharedPreferences sharedPref;

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            canGetLocation = false;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA, Manifest.permission.NFC, Manifest.permission.INTERNET)
                .check();

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            //showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            new TedPermission(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.CAMERA)
                    .check();

            getLocation();
        }

        return START_STICKY;
    }

    private void showNotification(CharSequence txt, CharSequence text) {
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, SplashActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
        mBuilder.setSmallIcon(R.mipmap.logo)  // the status icon
                .setTicker(text)  // the status text
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle(getText(R.string.app_name))  // the label of the entry
                .setContentText(txt)  // the contents of the entry
                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .setOngoing(true)
                .setPriority(Notification.FLAG_ONGOING_EVENT | Notification.FLAG_FOREGROUND_SERVICE | Notification.FLAG_NO_CLEAR)
                //.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                .build();

        // Send the notification.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        int NOTIFICATION = R.string.app_name;
        notificationManager.notify(NOTIFICATION, mBuilder.build());
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //OlmatixAlarmReceiver alarmCheckConn = new OlmatixAlarmReceiver();
        //alarmCheckConn.setAlarm(this);
        showNotification("Starting", "Starting2");

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        isPassive = locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            //showSettingsAlert();
            getLastLocation();
            getLocation();
        } else {
            Log.d(TAG, "Connection on");
            new TedPermission(this)
                    .setPermissionListener(permissionlistener)
                    .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                    .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.INTERNET)
                    .check();

            getLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        updateUI(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
        getLocation();
    }

    @Override
    public void onProviderDisabled(String s) {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    private void getLocation() {
        try {
            if (canGetLocation) {
                Log.d(TAG, "Can get location");
                if (isGPS) {
                    // from GPS
                    Log.d(TAG, "GPS on");
                    locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null) {
                            updateUI(loc);
                        } else {
                            passive_location();
                        }
                    }
                } else if (isNetwork) {
                    // from Network Provider
                    Log.d(TAG, "NETWORK_PROVIDER on");
                    locationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this);

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (loc != null) {
                            updateUI(loc);
                        } else {
                            passive_location();
                        }
                    }
                } else {
                    passive_location();
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void passive_location() {
        // from Other Apps
        try {
            if (isPassive) {
                locationManager.requestLocationUpdates(
                        LocationManager.PASSIVE_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                if (locationManager != null) {
                    loc = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                    Log.d(TAG, "PASSIVE PROVIDER on " + loc);
                    if (loc != null) {
                        updateUI(loc);
                    } /*else {
                        //getLastLocation();
                    }*/
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            criteria.setAccuracy(Criteria.ACCURACY_MEDIUM);
            criteria.setPowerRequirement(Criteria.POWER_LOW);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());

        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateUI(Location loc) {

        double lat = loc.getLatitude();
        double lot = loc.getLongitude();
        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());

        double prefLat = mPrefHelper.getPhoneLatitude();
        double prefLot = mPrefHelper.getPhoneLongitude();

        double checklat = lat - prefLat;
        double checklot = lot - prefLot;
        Log.d(TAG, "compare: " + lat + ":" + prefLat + ", " + lot + ":" + prefLot);
        Log.d(TAG, "result: " + checklat + ", " + checklot);

        if (checklat != 0.0 && checklot != 0.0) {
            Log.d(TAG, "update location to preference");

            mPrefHelper.setPhoneLatitude(lat);
            mPrefHelper.setPhoneLongitude(lot);
            mPrefHelper.setTagLatitude(lat);
            mPrefHelper.setTagLongitude(lot);
            new GetRecord(lat, lot).execute();
        }
    }

    private class GetRecord extends AsyncTask<Void, Void, Void> {

        double aLat, aLot;

        GetRecord(double lat, double lot) {
            this.aLat = lat;
            this.aLot = lot;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");
            String mEmployeeName = sharedPref.getString("real_name", "olmatix1");
            String mEmail = sharedPref.getString("email", "olmatix1");
            String mDivision = sharedPref.getString("division", "olmatix1");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            String tgl = df.format(Calendar.getInstance().getTime());

            if (!mEmployeeName.equals("Jhon Smith") || !mEmail.equals("")) {

                RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

                String url = "https://olmatix.com/wamonitoring/insert_employee_location_ver4.php";
                StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                    try {
                        SimpleDateFormat df1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                        String jam = df1.format(Calendar.getInstance().getTime());

                        JSONObject jObject = new JSONObject(response);
                        String result_code = jObject.getString("success");
                        if (result_code.equals("1")) {
                            //Log.d(TAG, "location insert success ");
                            showNotification(tgl + " | " + jam + " GPS updated", jam);
                            new updateRecord().execute();
                        } /*else {
                            //Log.d(TAG, "location insert fail ");

                        }*/

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Log.d(TAG, "onErrorResponse: insert GPS " + error)) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<>();
                        MyData.put("email", mEmail);
                        MyData.put("employee_name", mEmployeeName);
                        MyData.put("latitude", String.valueOf(aLat));
                        MyData.put("longitude", String.valueOf(aLot));
                        MyData.put("company_id", mCompanyID);
                        MyData.put("timestamps", tgl);
                        MyData.put("division", mDivision);

                        return MyData;
                    }
                };

                // Adding request to request queue

                int socketTimeout = 60000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                MyStringRequest.setRetryPolicy(policy);
                MyRequestQueue.add(MyStringRequest);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

    private class updateRecord extends AsyncTask<Void, Void, Void> {
        Calendar now;
        updateRecord() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mEmployeeName = sharedPref.getString("real_name", "Jhon Smith");
            String mEmail = sharedPref.getString("email", "olmatix1");

            if (!mEmployeeName.equals("Jhon Smith")) {

                //SimpleDateFormat df = new SimpleDateFormat("d-MM-yyyy HH:mm",Locale.ENGLISH);
                //timeUpdate = df.format(Calendar.getInstance().getTime());

                RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

                String url = "https://olmatix.com/wamonitoring/update_employe_lastGPSupdate.php";
                StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                    try {

                        JSONObject jObject = new JSONObject(response);
                        String result_code = jObject.getString("success");
                        if (result_code.equals("1")) {
                            Log.d(TAG, "user location insert success ");
                        } else {
                            Log.d(TAG, "user location insert fail ");

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> Log.d(TAG, "onErrorResponse: Update GPS " + error)) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<>();
                        MyData.put("email", mEmail);
                        MyData.put("last_update", String.valueOf(now.getTimeInMillis()));
                        return MyData;
                    }
                };

                // Adding request to request queue

                int socketTimeout = 60000;//30 seconds - change to what you want
                RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                MyStringRequest.setRetryPolicy(policy);
                MyRequestQueue.add(MyStringRequest);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

}
