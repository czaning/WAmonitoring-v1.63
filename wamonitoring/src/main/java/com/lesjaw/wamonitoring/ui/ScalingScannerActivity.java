package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.google.zxing.Result;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.DivisionAndID;
import com.lesjaw.wamonitoring.utils.AESEncryptionDecryption;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScalingScannerActivity extends BaseScannerActivity implements ZXingScannerView.ResultHandler, LocationListener {
    private static final String FLASH_STATE = "FLASH_STATE";
    private ZXingScannerView mScannerView;
    private boolean mFlash;
    private CoordinatorLayout coordinatorLayout;
    private boolean isGPS = false;
    private boolean isNetwork = false;
    private boolean canGetLocation = true;
    private LocationManager locationManager;
    private final String TAG = "GPS";
    private ArrayList<String> permissions = new ArrayList<>();
    private ArrayList permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 30;
    private ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    boolean insertRecord = false;
    private EditText eDisplayName, eDateBirth, ePlaceBirth, eEmail, ePassword, eEmployeeAddress;
    private DatePickerDialog datePickerDialog;
    boolean  emptyEmployee, emptyEmployment;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_scaling_scanner);
        setupToolbar();
        ViewGroup contentFrame = (ViewGroup) findViewById(R.id.content_frame);
        mScannerView = new ZXingScannerView(this);
        contentFrame.addView(mScannerView);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        new OkHttpClient();

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        Calendar now = Calendar.getInstance();
        int mYear = now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mDay, mMonth, mYear);
        GetUsers();

        if (!isGPS && !isNetwork) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            // check permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    requestPermissions((String[]) permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                            ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                }
            }
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
                Location loc;
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

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
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
                permissionsToRequest.stream().filter(perms -> !hasPermission((String) perms)).forEachOrdered(perms -> {
                    permissionsRejected.add((String) perms);
                });

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
        new AlertDialog.Builder(ScalingScannerActivity.this)
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

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        mPrefHelper.setPhoneLatitude(Double.parseDouble(lat));
        mPrefHelper.setPhoneLongitude(Double.parseDouble(lot));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        // You can optionally set aspect ratio tolerance level
        // that is used in calculating the optimal Camera preview size
        mScannerView.setAspectTolerance(0.2f);
        mScannerView.startCamera();
        mScannerView.setFlash(mFlash);
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FLASH_STATE, mFlash);
    }

    @Override
    public void handleResult(Result rawResult) {
        String result1 = rawResult.getText();

        String result = "";
        try {
            result = AESEncryptionDecryption.decrypt(result1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*byte[] dataDec = Base64.decode(result1, Base64.DEFAULT);
        String result = "";
        try {

            result = new String(dataDec, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();

        } finally {

        }*/

        try {
            JSONObject jObject = new JSONObject(result);
            String result_name = jObject.getString("name");
            String result_division = jObject.getString("division");
            String result_tagID = jObject.getString("code");
            String result_loc = jObject.getString("loc");

            String mprefDivName = mPrefHelper.getDivName();

            //Log.d(TAG, "handleResult: "+mprefDivName+":"+result_division);
            mScannerView.stopCamera();

            if (!result_name.equals("User Registration")) {

                String statLogin = mPrefHelper.getLogin();
                if (statLogin.equals("1")) {

                    if (mprefDivName.equals(result_division)) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "tag name : " + result_name + "\ndivision : " + result_division, TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            getLocation();
                            Log.d(TAG, "split location: " + result_loc);

                            pDialog = new ProgressDialog(ScalingScannerActivity.this);
                            pDialog.setMessage("Validating. Please wait...");
                            pDialog.setCancelable(true);
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.show();

                            String locSplit[] = result_loc.split(",");
                            String locLat = locSplit[0];
                            String locLot = locSplit[1];

                            //Log.d(TAG, "split location: "+locLat+":"+locLot);
                            new GetTags(result_tagID, locLat, locLot).execute();
                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    } else if (mprefDivName.equals("none")) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "tag name : " + result_name + "\ndivision : " + result_division, TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            pDialog = new ProgressDialog(ScalingScannerActivity.this);
                            pDialog.setMessage("Validating. Please wait...");
                            pDialog.setCancelable(true);
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.show();

                            String locSplit[] = result_loc.split(",");
                            String locLat = locSplit[0];
                            String locLot = locSplit[1];

                            //Log.d(TAG, "split location: "+locLat+":"+locLot);
                            new GetTags(result_tagID, locLat, locLot).execute();
                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You are not allowed to tag this checkpoint" +
                                "", TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> getLocation());
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    }
                }
            } else {


                int packtype = Integer.parseInt(mPrefHelper.getPackage());
                //int tagcount = Integer.parseInt(mPrefHelper.getTagsCount());
                int usercount = Integer.parseInt(mPrefHelper.getUserCount());

                //Log.d(TAG, "submit: "+usercount +" "+tagcount);

                if (packtype == 1 && usercount >= 10) {
                    snacknotif("users has reach package limit - 30 users");
                    return;
                }

                if (packtype == 2 && usercount >= 100) {
                    snacknotif("users has reach package limit - 100 users");

                    return;
                }

                if (packtype == 3 && usercount >= 250) {
                    snacknotif("users has reach package limit - 250 users");

                    return;
                }

                GetEmployment(result_tagID, result_loc);

            }

            // Note:
            // * Wait 2 seconds to resume the preview.
            // * On older devices continuously stopping and resuming camera preview can result in freezing the app.
            // * I don't know why this is the case but I don't have the time to figure out.
            Handler handler = new Handler();
            handler.postDelayed(() -> mScannerView.resumeCameraPreview(ScalingScannerActivity.this), 2000);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void snacknotif(String notif) {
        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, notif, TSnackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
        snackbar.setAction("OK", v -> {

        });
        snackbar.setActionTextColor(Color.BLACK);
        snackbar.show();
    }

    public void GetEmployment(String result_tagID, String result_loc) {

        //String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String locSplit[] = result_tagID.split(",");
        String mCompanyID = locSplit[1];
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

        String url = "https://olmatix.com/wamonitoring/get_employment.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            try {
                mPrefHelper.setEmployment(String.valueOf(response));

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    GetEmpStatus(result_tagID, result_loc);
                } else {
                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "currently you have no employment name", TSnackbar.LENGTH_SHORT);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.show();
                    mScannerView.startCamera();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
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
    }

    public void GetEmpStatus(String result_tagID, String result_loc) {
        mScannerView.stopCamera();

        //String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String locSplit[] = result_tagID.split(",");
        String mCompanyID = locSplit[1];

        RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

        String url = "https://olmatix.com/wamonitoring/get_employee_status.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            try {
                mPrefHelper.setEmployee_status(String.valueOf(response));

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    //String locSplit[] = result_tagID.split(",");
                    inputData(locSplit[0], locSplit[1], locSplit[2], locSplit[3], result_loc);
                } else {
                    mScannerView.startCamera();

                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "currently you have no employee status", TSnackbar.LENGTH_SHORT);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
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

    }

    @RequiresApi(api = Build.VERSION_CODES.CUPCAKE)
    private void inputData(String div, String companyid, String companyAdd, String companyName, String WorkingGPS) {
        eDisplayName = new EditText(this);
        eDisplayName.setHint("Real Name");
        eDisplayName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eDisplayName.setMaxLines(1);
        eDisplayName.setFilters( new InputFilter[] {new InputFilter.LengthFilter(20)});

        ePlaceBirth = new EditText(this);
        ePlaceBirth.setHint("Place Birth");
        ePlaceBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        ePlaceBirth.setMaxLines(1);
        ePlaceBirth.setFilters( new InputFilter[] {new InputFilter.LengthFilter(20)});

        eDateBirth = new EditText(this);
        eDateBirth.setHint("Date Birth - double click here");
        eDateBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
        eDateBirth.setMaxLines(1);
        eDateBirth.setFilters( new InputFilter[] {new InputFilter.LengthFilter(10)});

        eDateBirth.setOnClickListener(view -> datePickerDialog.show());

        eEmail = new EditText(this);
        eEmail.setHint("Email");
        eEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        eEmail.setMaxLines(1);
        eEmail.setFilters( new InputFilter[] {new InputFilter.LengthFilter(25)});

        ePassword = new EditText(this);
        ePassword.setHint("Password");
        ePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        ePassword.setMaxLines(1);
        ePassword.setFilters( new InputFilter[] {new InputFilter.LengthFilter(10)});

        eEmployeeAddress = new EditText(this);
        eEmployeeAddress.setHint("Employee Address");
        eEmployeeAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eEmployeeAddress.setMaxLines(1);
        eEmployeeAddress.setFilters( new InputFilter[] {new InputFilter.LengthFilter(50)});

        final EditText ePhone1 = new EditText(this);
        ePhone1.setHint("Phone Number 1");
        ePhone1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        ePhone1.setMaxLines(1);
        ePhone1.setFilters( new InputFilter[] {new InputFilter.LengthFilter(12)});

        final EditText ePhone2 = new EditText(this);
        ePhone2.setHint("Phone Number 2");
        ePhone2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        ePhone2.setMaxLines(1);
        ePhone2.setFilters( new InputFilter[] {new InputFilter.LengthFilter(12)});

        final Spinner eEmployment = new Spinner(this);
        String sEmployment = mPrefHelper.getEmployment();
        //Log.d(TAG, "getEmployment: "+sEmployment);

        JSONObject jObject;
        try {
            jObject = new JSONObject(sEmployment);
            String result_json = jObject.getString("employment");
            emptyEmployment = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Spinner eEmployee_status = new Spinner(this);
        String sEmployee_status = mPrefHelper.getEmployee_status();
        //Log.d(TAG, "getEmployee_status: "+sEmployee_status);
        try {
            jObject = new JSONObject(sEmployee_status);
            String result_json = jObject.getString("employee_status");
            emptyEmployee = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject jsonResponseEmployment;
        ArrayList<DivisionAndID> aNameEmployment = null;
        try {
            jsonResponseEmployment = new JSONObject(sEmployment);
            JSONArray cast2 = jsonResponseEmployment.getJSONArray("employment");
            aNameEmployment = new ArrayList<>();
            aNameEmployment.clear();
            for (int i = 0; i < cast2.length(); i++) {
                JSONObject div_name1 = cast2.getJSONObject(i);
                String name = div_name1.getString("name");
                String cid = div_name1.getString("cid");
                aNameEmployment.add(new DivisionAndID(name, cid));
                //Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aNameEmployment != null;
        ArrayAdapter<DivisionAndID> spinnerArrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aNameEmployment);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eEmployment.setAdapter(spinnerArrayAdapter1);


        JSONObject jsonResponseEmployeeStatus;
        ArrayList<DivisionAndID> aNameEmployeeStatus = null;
        try {
            jsonResponseEmployeeStatus = new JSONObject(sEmployee_status);
            JSONArray cast3 = jsonResponseEmployeeStatus.getJSONArray("employee_status");
            aNameEmployeeStatus = new ArrayList<>();
            aNameEmployeeStatus.clear();
            for (int i = 0; i < cast3.length(); i++) {
                JSONObject div_name2 = cast3.getJSONObject(i);
                String name = div_name2.getString("name");
                String cid = div_name2.getString("cid");
                aNameEmployeeStatus.add(new DivisionAndID(name, cid));
                /// / Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aNameEmployeeStatus != null;
        ArrayAdapter<DivisionAndID> spinnerArrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aNameEmployeeStatus);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eEmployee_status.setAdapter(spinnerArrayAdapter2);

        LinearLayout layout = new LinearLayout(ScalingScannerActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(eDisplayName);
        layout.addView(ePlaceBirth);
        layout.addView(eDateBirth);
        layout.addView(eEmail);
        layout.addView(ePassword);
        layout.addView(eEmployeeAddress);
        layout.addView(ePhone1);
        layout.addView(ePhone2);
        layout.addView(eEmployment);
        layout.addView(eEmployee_status);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);


        new AlertDialog.Builder(ScalingScannerActivity.this)
                .setTitle("Add Employee")
                .setMessage("Type employee data..")
                .setView(scrollView)
                .setPositiveButton("SUBMIT", (dialog, which) -> {
                    String iDisplayName = eDisplayName.getText().toString();
                    String iPlaceBirth = ePlaceBirth.getText().toString();
                    String iDateBirth = eDateBirth.getText().toString();
                    String iEmail = eEmail.getText().toString();
                    String iPassword = ePassword.getText().toString();
                    String iEmployeeAddress = eEmployeeAddress.getText().toString();


                    String iPhone1 = ePhone1.getText().toString();
                    String iPhone2 = ePhone2.getText().toString();

                    //Log.d(TAG, "onClick: "+iDivison+":"+iEmployment+":"+iEmployee_status);

                    if (!validate()) {
                        StyleableToast styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text("Check field please!, all field must be fill")
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .build();
                        styleableToast.show();

                        return;
                    }

                    int EmployementID = Integer.parseInt(String.valueOf(((DivisionAndID) eEmployment.getSelectedItem()).getId()));
                    int StatusID = Integer.parseInt(String.valueOf(((DivisionAndID) eEmployee_status.getSelectedItem()).getId()));
                    String iEmployment = String.valueOf(EmployementID);
                    String iEmployee_status = String.valueOf(StatusID);

                    pDialog = new ProgressDialog(ScalingScannerActivity.this);
                    pDialog.setMessage("Creating your employee data. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();

                    sendJsonAdmin(iDisplayName, iPlaceBirth, iDateBirth, iEmail, iPassword, iEmployeeAddress,
                            div, iEmployment, iEmployee_status, iPhone1, iPhone2, companyid, companyAdd, companyName, WorkingGPS);

                }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
        }).show();
    }

    private void sendJsonAdmin(String jdisplayname, String jplacecebirth, String jdatebirth,
                               String jemail, String jpassword, String jemployee_address, String jDivison
            , String jEmployment, String jEmployee_status, String jPhone1, String jPhone2, String comID,
                               String comAdd, String comName, String working_gps) {

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();


        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://olmatix.com/wamonitoring/create_employee.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, this::parsingJson, error -> Log.d(TAG, "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("real_name", jdisplayname);
                MyData.put("place_birth", String.valueOf(jplacecebirth));
                MyData.put("date_birth", String.valueOf(jdatebirth));
                MyData.put("email", jemail);
                MyData.put("password", jpassword);
                MyData.put("company_name", comName);
                MyData.put("company_address", comAdd);
                MyData.put("company_id", comID);
                MyData.put("employee_address", String.valueOf(jemployee_address));
                MyData.put("level_user", "2");
                MyData.put("employment", jEmployment);
                MyData.put("employment_status", jEmployee_status);
                MyData.put("division", jDivison);
                MyData.put("date_created", String.valueOf(now.getTimeInMillis()));
                MyData.put("phone1", jPhone1);
                MyData.put("phone2", jPhone2);
                MyData.put("last_update", String.valueOf(now.getTimeInMillis()));
                MyData.put("package", mPrefHelper.getPackage());
                MyData.put("working_gps", working_gps);


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
            //Log.d(TAG, "result_json before: " + json);
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            //Log.d(TAG, "result_json: " + result_json + "result_code: " + result_code);

            if (result_code.equals("1")) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, result_json, TSnackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {
                    Intent i = new Intent(getApplication(), LoginActivity.class);
                    startActivity(i);
                    finish();
                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
                pDialog.cancel();


            } else {
                pDialog.cancel();

                StyleableToast styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text(result_json)
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void GetUsers() {

        String url = Config.DOMAIN + "wamonitoring/get_count_users.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    //Log.d(TAG, "doInBackground: user count " + result_message);
                    mPrefHelper.setUserCount(result_message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", sharedPref.getString("company_id", "jakarta"));

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);

    }


    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth1) -> {
                    String daymonth = String.valueOf(dayOfMonth1);
                    String mMonth = String.valueOf(++month1);

                    if (daymonth.length() == 1) {
                        daymonth = "0" + dayOfMonth1;
                    } else {
                        daymonth = String.valueOf(dayOfMonth1);
                    }
                    if (mMonth.length() == 1) {
                        mMonth = "0" + month1;
                    } else {
                        mMonth = String.valueOf(month1);
                    }

                    String date = daymonth + "-" + mMonth + "-" + year1;
                    eDateBirth.setText(date);
                }, year, month, dayOfMonth);
    }

    public boolean validate() {
        boolean valid = true;

        String vDisplayName = eDisplayName.getText().toString();
        //String vDateBirth = eDateBirth.getText().toString();
        String vPlaceBirth = ePlaceBirth.getText().toString();
        String vEmail = eEmail.getText().toString();
        String vPassword = ePassword.getText().toString();
        String vEmployeeAddress = eEmployeeAddress.getText().toString();

        if (vDisplayName.isEmpty() || vDisplayName.length() < 3) {
            eDisplayName.setError("at least 3 characters");
            valid = false;
        } else {
            eDisplayName.setError(null);
        }

        if (vPlaceBirth.isEmpty() || vPlaceBirth.length() < 3) {
            ePlaceBirth.setError("at least 3 characters");
            valid = false;
        } else {
            ePlaceBirth.setError(null);
        }

       /* if (vDateBirth.isEmpty() || !android.util.Patterns.D.matcher(vDateBirth).matches()) {
            eEmail.setError("enter a valid email address");
            valid = false;
        } else {
            eEmail.setError(null);
        }*/

        if (vEmail.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(vEmail).matches()) {
            eEmail.setError("enter a valid email address");
            valid = false;
        } else {
            eEmail.setError(null);
        }

        if (vPassword.isEmpty() || vPassword.length() < 4 || vPassword.length() > 12) {
            ePassword.setError("between 4 and 12 alphanumeric characters");
            valid = false;
        } else {
            ePassword.setError(null);
        }

        if (vEmployeeAddress.isEmpty() || vEmployeeAddress.length() < 4 || vEmployeeAddress.length() > 100) {
            eEmployeeAddress.setError("between 4 and 100 alphanumeric characters");
            valid = false;
        } else {
            eEmployeeAddress.setError(null);
        }


        return valid;
    }

    private class GetTags extends AsyncTask<Void, Void, Void> {
        String tgid, tgLat, tgLot;

        GetTags(String tagID, String tagLat, String tagLot) {
            this.tgid = tagID;
            this.tgLat = tagLat;
            this.tgLot = tagLot;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            String url = "https://olmatix.com/wamonitoring/get_tags_data.php";
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");

                    if (result_code.equals("1")) {

                        new ValidateTags(tgid, tgLat, tgLot).execute();

                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Tag checkpoint is not REGISTERED, contact ADMIN", TSnackbar.LENGTH_SHORT);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();

                        pDialog.dismiss();
                        mScannerView.startCamera();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> {
                Log.d(TAG, "onErrorResponse: " + error);
                StyleableToast styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text("Network error, try again later")
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.RED)
                        .build();
                styleableToast.show();
                pDialog.dismiss();

            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("tid", tgid);

                    return MyData;
                }
            };

            // Adding request to request queue

            NetworkRequest.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private class ValidateTags extends AsyncTask<Void, Void, Void> {
        String tgid, tgLat, tgLot;

        ValidateTags(String tagID, String tagLat, String tagLot) {
            this.tgid = tagID;
            this.tgLat = tagLat;
            this.tgLot = tagLot;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");
            String mEmail = sharedPref.getString("email", "olmatix1");

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String tgl1 = df.format(Calendar.getInstance().getTime());

            String url = "https://olmatix.com/wamonitoring/validate_tags_before_sending.php";
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    //String result_message = jObject.getString("message");
                    String result_code = jObject.getString("success");

                    if (result_code.equals("1")) {

                        JSONArray cast = jObject.getJSONArray("tags");
                        String jam = null, tgl = null;
                        for (int i = 0; i < cast.length(); i++) {

                            JSONObject tags_name = cast.getJSONObject(i);
                            tgl = tags_name.getString("tgl");
                            jam = tags_name.getString("jam");

                            //Log.d(TAG, "data: " + tgl + jam);
                        }

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                        String dateNow = simpleDateFormat.format(Calendar.getInstance().getTime());


                        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                        Date startDate = null, endDate = null;
                        try {
                            String jamNow = simpleDateFormat1.format(Calendar.getInstance().getTime());
                            startDate = simpleDateFormat1.parse(jam);
                            endDate = simpleDateFormat1.parse(jamNow);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long difference = endDate.getTime() - startDate.getTime();

                        Date dateMax = null, dateMin = null;
                        if (difference < 0) {
                            try {
                                dateMax = simpleDateFormat1.parse("24:00");
                                dateMin = simpleDateFormat1.parse("00:00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            difference = (dateMax.getTime() - startDate.getTime()) + (endDate.getTime() - dateMin.getTime());
                        }
                        int days = (int) (difference / (1000 * 60 * 60 * 24));
                        int hours = (int) ((difference - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                        int min = (int) (difference - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                        Log.i("log_tag", "Hours: " + hours + ", Mins: " + min);

                        if (hours == 0 && min <= 10) {
                            pDialog.cancel();

                            if (tgl.equals(dateNow)) {

                                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You just Tag this tag " + min + " minute ago",
                                        TSnackbar.LENGTH_INDEFINITE);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                snackbar.setAction("OK", v -> finish());
                                snackbar.setActionTextColor(Color.BLACK);
                                snackbar.show();


                            } else {
                                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You Tag this tag " + hours + " hours "
                                                + min + " minute ago",
                                        TSnackbar.LENGTH_INDEFINITE);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                snackbar.setAction("OK", v -> finish());
                                snackbar.setActionTextColor(Color.BLACK);
                                snackbar.show();
                                if (!insertRecord) {
                                    new GetRecord(tgid, tgLat, tgLot).execute();
                                }
                            }
                        } else {

                            TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You Tag this tag " + hours + " hours "
                                            + min + " minute ago",
                                    TSnackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.setAction("OK", v -> finish());
                            snackbar.setActionTextColor(Color.BLACK);
                            snackbar.show();

                            if (!insertRecord) {
                                new GetRecord(tgid, tgLat, tgLot).execute();
                            }
                        }

                    } else {
                        if (!insertRecord) {
                            new GetRecord(tgid, tgLat, tgLot).execute();
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("tid", tgid);
                    MyData.put("email", mEmail);
                    MyData.put("company_id", mCompanyID);
                    MyData.put("tgl", tgl1);

                    return MyData;
                }
            };

            // Adding request to request queue

            NetworkRequest.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

    private class GetRecord extends AsyncTask<Void, Void, Void> {
        String tgid, tgLat, tgLot;

        GetRecord(String tagID, String tagLat, String tagLot) {
            this.tgid = tagID;
            this.tgLat = tagLat;
            this.tgLot = tagLot;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            insertRecord = true;

            String mCompanyID = sharedPref.getString("company_id", "olmatix1");
            //String mDivision = sharedPref.getString("division", "olmatix1");

            String mEmployee_name = sharedPref.getString("real_name", "olmatix1");
            String mEmail = sharedPref.getString("email", "olmatix1");
            String mLat = String.valueOf(mPrefHelper.getPhoneLatitude());
            String mLot = String.valueOf(mPrefHelper.getPhoneLongitude());

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            String tgl = df.format(Calendar.getInstance().getTime());

            SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            String jam = df1.format(Calendar.getInstance().getTime());

            Location startPoint = new Location("locationA");
            startPoint.setLatitude(Double.parseDouble(tgLat));
            startPoint.setLongitude(Double.parseDouble(tgLot));

            Location endPoint = new Location("locationA");
            endPoint.setLatitude(Double.parseDouble(mLat));
            endPoint.setLongitude(Double.parseDouble(mLot));

            double distance = startPoint.distanceTo(endPoint);
            int distanceInt = (int) distance;
            Log.d(TAG, "distance : " + distanceInt);


            String url = "https://olmatix.com/wamonitoring/insert_tag_record.php";
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

                try {

                    pDialog.cancel();

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    String result_message = jObject.getString("message");

                    if (result_code.equals("1")) {
                        sendNotification(mCompanyID, mPrefHelper.getDivName(), mEmployee_name, "TAGS");

                    }

                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, result_message, TSnackbar.LENGTH_INDEFINITE);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> finish());
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();

                    insertRecord = false;


                } catch (JSONException e) {
                    e.printStackTrace();
                    insertRecord = false;

                }

            }, error -> {
                Log.d(TAG, "onErrorResponse: " + error);
                insertRecord = false;

            }) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("tid", tgid);
                    MyData.put("company_id", mCompanyID);
                    MyData.put("employee_name", mEmployee_name);
                    MyData.put("email", mEmail);
                    MyData.put("latitude", mLat);
                    MyData.put("longitude", mLot);
                    MyData.put("range_loc", String.valueOf(distanceInt));
                    MyData.put("tgl", String.valueOf(tgl));
                    MyData.put("jam", String.valueOf(jam));

                    return MyData;
                }
            };

            // Adding request to request queue

            NetworkRequest.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

    public void toggleFlash(View v) {
        mFlash = !mFlash;
        mScannerView.setFlash(mFlash);
    }

    private void sendNotification(final String reg_token, String body, String title, String imgUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                    String times = timeformat.format(System.currentTimeMillis());

                    JSONObject root = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject payload = new JSONObject();

                    payload.put("title", title);
                    payload.put("is_background", false);
                    payload.put("message", body);
                    payload.put("image", imgUrl);
                    payload.put("timestamp", times);

                    data.put("data", payload);
                    root.put("data", data);
                    root.put("to", "/topics/" + reg_token);

                    RequestBody body = RequestBody.create(JSON, root.toString());
                    com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                            .header("Authorization", "key=AIzaSyAwU7DMeeysvpQjcwZsS3hJFfx8wWcrpNU")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d(TAG, "doInBackground: " + finalResponse);


                } catch (Exception e) {
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }

}