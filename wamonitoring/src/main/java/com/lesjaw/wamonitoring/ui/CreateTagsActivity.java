package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.NFCManager;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class CreateTagsActivity extends AppCompatActivity implements LocationListener{
    private static final String TAG = "CreateTagsActivity";
    private Button _btn_submit;
    private EditText _tag_name,_loc,_timeInterval;
    private String tagName, mloc, timeInterval;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;

    private boolean isGPS = false;
    private boolean isNetwork = false;
    private boolean canGetLocation = true;
    private LocationManager locationManager;
    private Location loc;
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 25;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 ;
    private Spinner spin;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private boolean submitNo = false;
    private NFCManager nfcMger;
    private NdefMessage message = null;
    private Context context;
    private StyleableToast styleableToast;

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Permission Denied\n" + deniedPermissions.toString())
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.BLUE)
                    .build();
            styleableToast.show();
            canGetLocation = false;
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_tag);
        _btn_submit = (Button) findViewById(R.id.btn_submit);

        _tag_name = (EditText)findViewById(R.id.tag_name);
        _loc = (EditText)findViewById(R.id.loc);
        _timeInterval = (EditText)findViewById(R.id.time_interval);
        spin = (Spinner) findViewById(R.id.div_name);
        context = getApplicationContext();
        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,Manifest.permission.NFC, Manifest.permission.INTERNET, Manifest.permission.ACCESS_NETWORK_STATE)
                .check();

        getSupportActionBar().setTitle(R.string.app_name);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        nfcMger = new NFCManager(this);

        _tag_name.setFilters( new InputFilter[] {new InputFilter.LengthFilter(15)});

        mPrefHelper = new PreferenceHelper(getApplicationContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mLevelUser = mPrefHelper.getLevelUser();
        ArrayList<String> aName;
        aName = new ArrayList<>();
        aName.clear();
        if (mLevelUser.equals("0")){
            String sDivision = mPrefHelper.getDivisionFull();
            Log.d(TAG, "getDivision: "+sDivision);
            JSONObject jsonResponse;
            try {
                jsonResponse = new JSONObject(sDivision);
                JSONArray cast1 = jsonResponse.getJSONArray("division");
                for (int i = 0; i < cast1.length(); i++) {
                    JSONObject div_name = cast1.getJSONObject(i);
                    String name = div_name.getString("name");
                    aName.add(name);
                    Log.d(TAG, "editData: "+name);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            aName.add(sharedPref.getString("division", "olmatix1"));
        }
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_item, aName);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spin.setAdapter(spinnerArrayAdapter);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        new GetUsers().execute();

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                }
            }
        }

        _btn_submit.setOnClickListener(v -> submit());

        _loc.setOnClickListener(view -> new AlertDialog.Builder(CreateTagsActivity.this)
                .setTitle("Set location for tag")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setMessage("Do you want to autodetect or chosing from map?")
                .setPositiveButton("Map", (dialog, id) -> {

                    Intent i = new Intent(getApplication(), MapsActivity.class);
                    i.putExtra("id_caller","CreateTags");
                    startActivity(i);

                })

                .setNeutralButton("Cancel", (dialog, id) -> dialog.cancel())

                .setNegativeButton("Auto", (dialog, id) -> getLocation())

                .show());
    }

    public void submit() {

        if (!validate()) {
            onLoginFailed();
            return;
        }

        int packtype = Integer.parseInt(mPrefHelper.getPackage());
        int tagcount = Integer.parseInt(mPrefHelper.getTagsCount());
        int usercount = Integer.parseInt(mPrefHelper.getUserCount());

        Log.d(TAG, "submit: "+usercount +" "+tagcount);

        if (packtype==1 && tagcount>=5){
            snacknotif("tag has reach package limit - 5 tags");
            return;
        }

        if (packtype==2 && tagcount>=50){
            snacknotif("tag has reach package limit - 15 tags");

            return;
        }
        if (packtype==2 && usercount>=150){
            snacknotif("users has reach package limit - 100 users");

            return;
        }
        _btn_submit.setEnabled(false);

        progressDialog = new ProgressDialog(CreateTagsActivity.this);
        progressDialog.setMessage("Creating tag checkpoint. Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        tagName = _tag_name.getText().toString();
        mloc = _loc.getText().toString();
        timeInterval = _timeInterval.getText().toString();

        sendJsonTag();

        submitNo = true;
    }

    private void sendJsonTag(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mCompany_id = sharedPref.getString("company_id", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://olmatix.com/wamonitoring/create_tags.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "onResponse: "+response);
            parsingJson(response);
        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("tag_name", tagName);
                MyData.put("tag_location", mloc);
                MyData.put("company_id", mCompany_id);
                MyData.put("division_name", spin.getSelectedItem().toString());
                MyData.put("time_interval", timeInterval);

                return MyData;
            }
        };

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        MyStringRequest.setRetryPolicy(policy);
        MyRequestQueue.add(MyStringRequest);


    }

    public void parsingJson(String json) {
        try {

            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            //String result_user = jObject.getString("email");

            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);


            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_json,TSnackbar.LENGTH_SHORT);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.show();

            new GetUsers().execute();


            progressDialog.hide();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onLoginFailed() {
        styleableToast = new StyleableToast
                .Builder(getBaseContext())
                .icon(R.drawable.ic_action_info)
                .text("Check field please!")
                .textColor(Color.WHITE)
                .backgroundColor(Color.BLUE)
                .build();
        styleableToast.show();
        _btn_submit.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String vtagName = _tag_name.getText().toString();
        String vLoc = _loc.getText().toString();
        String vTimeInterval = _timeInterval.getText().toString();

        if (vtagName.isEmpty() || vtagName.length() < 3) {
            _tag_name.setError("at least 3 characters");
            valid = false;
        } else {
            _tag_name.setError(null);
        }

        if (vLoc.isEmpty() || vLoc.length() < 3) {
            _loc.setError("at least 3 characters");
            valid = false;
        } else {
            _loc.setError(null);
        }

        if (vTimeInterval.isEmpty() || vTimeInterval.length() < 3) {
            _timeInterval.setError("at least 3 characters");
            valid = false;
        } else {
            _timeInterval.setError(null);
        }



        return valid;
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcMger.disableDispatch();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "onLocationChanged");
        updateUI(location);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}

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
                        if (loc != null)
                            updateUI(loc);
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
                        if (loc != null)
                            updateUI(loc);
                    }
                } else {
                    loc.setLatitude(0);
                    loc.setLongitude(0);
                    updateUI(loc);
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {

        return wanted.stream().filter(perm -> !hasPermission(perm)).collect(Collectors.toCollection(ArrayList::new));
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case ALL_PERMISSIONS_RESULT:
                Log.d(TAG, "onRequestPermissionsResult");
                permissionsRejected.addAll(permissionsToRequest.stream().filter(perms -> !hasPermission(perms)).collect(Collectors.toList()));

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            showMessageOKCancel("These permissions are mandatory for the application. Please allow access.",
                                    (dialog, which) -> {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            requestPermissions(permissionsRejected.toArray(
                                                    new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    });
                            return;
                        }
                    }
                } else {
                    Log.d(TAG, "No rejected permissions.");
                    canGetLocation = true;
                    getLocation();
                }
                break;
        }
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        });

        alertDialog.setNegativeButton("No", (dialog, which) -> dialog.cancel());

        alertDialog.show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(CreateTagsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void updateUI(Location loc) {
        Log.d(TAG, "updateUI");

        String lat = (Double.toString(loc.getLatitude()));
        String lot = (Double.toString(loc.getLongitude()));
        //tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));

        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"latitude : "+lat+"\nlongitude : "+lot,TSnackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
        snackbar.setAction("OK", v -> {
            final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
            mPrefHelper.setPhoneLatitude(Double.parseDouble(lat));
            mPrefHelper.setPhoneLongitude(Double.parseDouble(lot));
        });
        snackbar.setActionTextColor(Color.BLACK);
        snackbar.show();
        _loc.setText(lat+","+lot);

    }

    private class GetTags extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* pDialog = new ProgressDialog(ScalingScannerActivity.this);
            pDialog.setMessage("Loading Division names. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();*/
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_count_tags.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_message = jObject.getString("message");
                    String result_code = jObject.getString("success");

                    if (result_code.equals("1")) {
                        Log.d(TAG, "doInBackground: tags count "+result_message);

                        mPrefHelper.setTagsCount(result_message);
                        //status_tags.setText("total current tags "+result_message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("company_id", mCompanyID);

                    return MyData;
                }
            };

            // Adding request to request queue

            int socketTimeout = 60000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            MyStringRequest.setRetryPolicy(policy);
            MyRequestQueue.add(MyStringRequest);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class GetUsers extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* pDialog = new ProgressDialog(ScalingScannerActivity.this);
            pDialog.setMessage("Loading Division names. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();*/
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_count_users.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_message = jObject.getString("message");
                    String result_code = jObject.getString("success");

                    if (result_code.equals("1")) {
                        Log.d(TAG, "doInBackground: user count "+result_message);
                        mPrefHelper.setUserCount(result_message);
                        //status_user.setText("total current users "+result_message);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("company_id", mCompanyID);

                    return MyData;
                }
            };

            // Adding request to request queue

            int socketTimeout = 60000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            MyStringRequest.setRetryPolicy(policy);
            MyRequestQueue.add(MyStringRequest);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            new GetTags().execute();

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        if (!submitNo){
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"You haven't submit this yet",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("tags"));


        _loc.setText("");
        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        String lat = String.valueOf(mPrefHelper.getPhoneLatitude());
        String lot = String.valueOf(mPrefHelper.getPhoneLongitude());
        _loc.setText(lat+","+lot);
        try {
            nfcMger.verifyNFC();
            //nfcMger.enableDispatch();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        }
        catch(NFCManager.NFCNotSupported nfcnsup) {
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"NFC not supported",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();
        }
        catch(NFCManager.NFCNotEnabled nfcnEn) {
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"NFC not enabled",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();
        }
    }

    public void snacknotif (String notif){
        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,notif,TSnackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
        snackbar.setAction("OK", v -> {

        });
        snackbar.setActionTextColor(Color.BLACK);
        snackbar.show();
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        Tag currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
           // nfcMger.writeTag(currentTag, message);
            nfcMger.createWrite(currentTag, message,context);
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Trying to write NFC tag",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();
            //currentTag = null;
            message = null;
        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("notify");
            Log.d("DEBUG", "onReceive1: " + message);
            if (message != null) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, message, TSnackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> finish());
                snackbar.show();
            }
        }
    };

    @Override
    public void onBackPressed() {

        if (!submitNo) {
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You haven't submit this yet, your users will never can't tag it", TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.setActionTextColor(Color.BLACK);

            snackbar.show();

            //backButtonCount++;

        } else {
                finish();
            }
    }

}