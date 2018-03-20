package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
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
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListTagsLogAdapter;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.service.wamonitorservice;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ng.max.slideview.SlideView;


public class TagActivity extends AppCompatActivity implements LocationListener {

    ImageButton nfctag, codetag;
    TextView user;
    SharedPreferences sharedPref;
    PreferenceHelper mPrefHelper;
    private NfcAdapter mNfcAdapter;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "TagActivity";
    CoordinatorLayout coordinatorLayout;
    boolean nfcdevices = false;
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    LocationManager locationManager;
    Location loc;
    ArrayList<String> permissions = new ArrayList<>();
    ArrayList<String> permissionsToRequest;
    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 1;
    private ProgressDialog pDialog;
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager layoutManager;
    private List<tagsDailyModel> tagList = new ArrayList<>();
    ListTagsLogAdapter mAdapter;
    String mBirth, mCompanyAddress,mEmployeeAddress, mUser, mDivision,mCompanyID, mPhone1, mPhone2, placeB, dateB,mLevelUser, real_name ;
    boolean editdata = false;
    CircleImageView foto;
    String companyID;
    private boolean isCanceled = false;
    OkHttpClient mClient;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private StyleableToast styleableToast;

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getApplicationContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        nfctag = (ImageButton) findViewById(R.id.nfctag);
        codetag = (ImageButton) findViewById(R.id.codetag);
        user = (TextView) findViewById(R.id.user);
        foto = (CircleImageView) findViewById(R.id.img_profile);


        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA,Manifest.permission.NFC, Manifest.permission.INTERNET)
                .check();

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        mUser = sharedPref.getString("email", "jakarta");
        mDivision = sharedPref.getString("division", "jakarta");
        mLevelUser = mPrefHelper.getLevelUser();
        companyID = sharedPref.getString("company_id", "olmatix1");
        real_name = sharedPref.getString("real_name", "olmatix1");

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            //Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("This device doesn't support NFC.")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.BLUE)
                    .build();
            styleableToast.show();

            nfcdevices = false;
        } else {
            if (!mNfcAdapter.isEnabled()) {
               // Toast.makeText(this, "NFC disabled.", Toast.LENGTH_LONG).show();
                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text("NFC disabled.")
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
            nfcdevices = true;
            handleIntent(getIntent());
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (!mLevelUser.equals("2")) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>"+ R.string.app_name+"</font>"));

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        mRecycleView    = (RecyclerView) findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new ListTagsLogAdapter(tagList,this);
        layoutManager = new LinearLayoutManager(TagActivity.this);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        getData();

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


        codetag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplication(), ScalingScannerActivity.class);
                startActivity(i);
            }
        });

        nfctag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // custom dialog
                final Dialog dialog = new Dialog(TagActivity.this);
                dialog.setContentView(R.layout.panic_button);
                isCanceled = false;

                TextView count = (TextView) dialog.findViewById(R.id.count);
                SlideView slideView = (SlideView) dialog.findViewById(R.id.slideView);
                Button cancel = (Button) dialog.findViewById(R.id.cancelBut);

                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        isCanceled = true;
                        dialog.dismiss();

                    }
                });
                slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {

                    long millisInFuture = 10000; //25 seconds
                    long countDownInterval = 1000; //1 second

                    @Override
                    public void onSlideComplete(SlideView slideView) {
                        new CountDownTimer(millisInFuture,countDownInterval){
                            public void onTick(long millisUntilFinished){
                                if(isCanceled)
                                {
                                    //If user requested to pause or cancel the count down timer
                                    cancel();
                                }
                                else {
                                    count.setText(millisUntilFinished / 1000+" sec");
                                    //Put count down timer remaining time in a variable
                                    slideView.setEnabled(false);
                                }

                                //counter++;
                            }
                            public  void onFinish(){
                                isCanceled = false;
                                sendNotification(mCompanyID, mDivision + " | PANIC! button", real_name, "PANIC!");
                                //counter=0;
                                dialog.dismiss();
                                //count.setText("FINISH!!");

                            }
                        }.start();
                    }
                });

                dialog.show();
            }
        });

        user.setText(mUser+" - "+mDivision);

        pDialog = new ProgressDialog(TagActivity.this);
        pDialog.setMessage("Loading tags list. Please wait...");
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();
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

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mNfcAdapter != null) {
            setupForegroundDispatch(this, mNfcAdapter);
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("tags"));
        getData();
        new GetPhoto().execute();
    }

    @Override
    public void onPause(){
        if (mNfcAdapter != null) {
            stopForegroundDispatch(this, mNfcAdapter);
        }
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (mNfcAdapter != null) {
            handleIntent(intent);
        }

    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    new NdefReaderTask().execute(tag);
                    break;
                }
            }
        }
    }

    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_1, menu);
        return true;
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

        if (id == R.id.action_foto) {
            Intent i = new Intent(getApplication(), UploadFotoAcivity.class);
            startActivity(i);

        }

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_chat) {
            Intent i = new Intent(this, ChatActivityCompany.class);
            i.putExtra("room", companyID);
            startActivity(i);
            return true;
        }


        if (id == R.id.action_logout) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email","");
            editor.putString("password","");
            editor.putString("user_name", "John Smith");
            editor.apply();

            Intent myService = new Intent(TagActivity.this, wamonitorservice.class);
            stopService(myService);

            Intent i = new Intent(getApplication(), LoginActivity.class);
            startActivity(i);
            finish();

            return true;
        }




        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
               //user.setText("Read content: " + result);
                //Toast.makeText(this, "Read content: " + result, Toast.LENGTH_LONG).show();
                try {
                    JSONObject jObject = new JSONObject(result);
                    String result_name = jObject.getString("name");
                    String result_division = jObject.getString("division");
                    String result_tagID = jObject.getString("code");
                    String result_loc = jObject.getString("loc");

                    String mprefDivName = sharedPref.getString("division", "jakarta");

                    Log.d(TAG, "handleResult: "+mprefDivName+":"+result_division);

                    if (mprefDivName.equals(result_division)){
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"tag name : "+result_name+"\ndivision : "+result_division,TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            // get location
                            getLocation();
                            Log.d(TAG, "split location: "+result_loc);

                            pDialog = new ProgressDialog(TagActivity.this);
                            pDialog.setMessage("Validating. Please wait...");
                            pDialog.setCancelable(true);
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.show();

                            String locSplit[] = result_loc.split(",");
                            String locLat = locSplit[0];
                            String locLot = locSplit[1];

                            Log.d(TAG, "split location: "+locLat+":"+locLot);
                            new GetTags(result_tagID, locLat,locLot).execute();
                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    } else if (mprefDivName.equals("none")) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"tag name : "+result_name+"\ndivision : "+result_division,TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            // get location
                            getLocation();
                            Log.d(TAG, "split location: "+result_loc);

                            pDialog = new ProgressDialog(TagActivity.this);
                            pDialog.setMessage("Validating. Please wait...");
                            pDialog.setCancelable(true);
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.show();

                            String locSplit[] = result_loc.split(",");
                            String locLat = locSplit[0];
                            String locLot = locSplit[1];

                            Log.d(TAG, "split location: "+locLat+":"+locLot);
                            new GetTags(result_tagID, locLat,locLot).execute();
                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"You are not allowed to tag this checkpoint" +
                                "",TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            getLocation();

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
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

    private class GetTags extends AsyncTask<Void, Void, Void> {
        String tgid, tgLat, tgLot;

        public GetTags (String tagID, String tagLat, String tagLot){
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
            Log.d(TAG, "check tags registered: "+tgid);
            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_tags_data.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_message = jObject.getString("message");
                    String result_code = jObject.getString("success");

                    if (result_code.equals(1)||result_code.equals("1")) {

                        new ValidateTags(tgid, tgLat,tgLot).execute();

                    } else {
                        pDialog.cancel();

                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Tag checkpoint is not REGISTERED, contact ADMIN",
                                TSnackbar.LENGTH_LONG);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK",  v -> {

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("tid", tgid);

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

    private class ValidateTags extends AsyncTask<Void, Void, Void> {
        String tgid, tgLat, tgLot;

        public ValidateTags (String tagID, String tagLat, String tagLot){
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

            SimpleDateFormat df = new SimpleDateFormat("d-MM-yyyy");
            String tgl1 = df.format(Calendar.getInstance().getTime());

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/validate_tags_before_sending.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_message = jObject.getString("message");
                    String result_code = jObject.getString("success");

                    if (result_code.equals(1)||result_code.equals("1")) {

                        JSONArray cast = jObject.getJSONArray("tags");
                        String jam = null, tgl = null;
                        for (int i = 0; i < cast.length(); i++) {

                            JSONObject tags_name = cast.getJSONObject(i);
                            tgl = tags_name.getString("tgl");
                            jam = tags_name.getString("jam");

                            Log.d(TAG, "data: "+tgl +jam);
                        }

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                        Date startDate = null, endDate = null;
                        try {
                            String jamNow = simpleDateFormat.format(Calendar.getInstance().getTime());
                            startDate = simpleDateFormat.parse(jam);
                            endDate = simpleDateFormat.parse(jamNow);

                        } catch (ParseException e) {
                            e.printStackTrace();
                        }

                        long difference = endDate.getTime() - startDate.getTime();

                        Date dateMax = null,dateMin= null;
                        if(difference<0) {
                            try {
                                dateMax = simpleDateFormat.parse("24:00");
                                dateMin = simpleDateFormat.parse("00:00");
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            difference=(dateMax.getTime() -startDate.getTime() )+(endDate.getTime()- dateMin.getTime());
                        }
                        int days = (int) (difference / (1000*60*60*24));
                        int hours = (int) ((difference - (1000*60*60*24*days)) / (1000*60*60));
                        int min = (int) (difference - (1000*60*60*24*days) - (1000*60*60*hours)) / (1000*60);
                        Log.i("log_tag","Hours: "+hours+", Mins: "+min);

                        if (min <= 10){
                            pDialog.cancel();

                            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"You just Tag this tag "+min+" minute ago",
                                    TSnackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.setAction("OK", v -> {

                            });
                            snackbar.setActionTextColor(Color.BLACK);
                            snackbar.show();
                        } else {

                            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"You Tag this tag "+hours+" hours "
                                    +min+" minute ago",
                                    TSnackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.setAction("OK", v -> {

                            });
                            snackbar.setActionTextColor(Color.BLACK);
                            snackbar.show();

                            new InsertTags(tgid, tgLat,tgLot).execute();
                        }

                    } else {
                        new InsertTags(tgid, tgLat,tgLot).execute();

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("tid", tgid);
                    MyData.put("email", mEmail);
                    MyData.put("company_id", mCompanyID);
                    MyData.put("tgl", tgl1);

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

    private class InsertTags extends AsyncTask<Void, Void, Void> {
        String tgid, tgLat, tgLot;

        public InsertTags (String tagID, String tagLat, String tagLot){
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
            Log.d(TAG, "doInBackground: ");
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");
            String mEmployee_name = sharedPref.getString("real_name", "olmatix1");
            String mEmail = sharedPref.getString("email", "olmatix1");
            String mLat = String.valueOf(mPrefHelper.getPhoneLatitude());
            String mLot = String.valueOf(mPrefHelper.getPhoneLongitude());

            SimpleDateFormat df = new SimpleDateFormat("d-MM-yyyy");
            String tgl = df.format(Calendar.getInstance().getTime());

            SimpleDateFormat df1 = new SimpleDateFormat("HH:mm");
            String jam = df1.format(Calendar.getInstance().getTime());


            Location startPoint=new Location("locationA");
            startPoint.setLatitude(Double.parseDouble(tgLat));
            startPoint.setLongitude(Double.parseDouble(tgLot));

            Location endPoint=new Location("locationA");
            endPoint.setLatitude(Double.parseDouble(mLat));
            endPoint.setLongitude(Double.parseDouble(mLot));

            double distance=startPoint.distanceTo(endPoint);
            int distanceInt = (int)distance;
            //Log.d(TAG, "distance : "+distance +" tgl: "+tgl +" Jam "+jam);

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/insert_tag_record.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    pDialog.cancel();

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("message");

                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_code,TSnackbar.LENGTH_INDEFINITE);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> {

                        getData();

                    });
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
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

    private void updateUI(Location loc) {

        String lat = (Double.toString(loc.getLatitude()));
        String lot = (Double.toString(loc.getLongitude()));
        //tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        mPrefHelper.setPhoneLatitude(Double.parseDouble(lat));
        mPrefHelper.setPhoneLongitude(Double.parseDouble(lot));

    }

    private void getData() {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(TagActivity.this);
        String mEmail = sharedPref.getString("email", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(TagActivity.this);

        String url = "https://olmatix.com/wamonitoring/get_tags_record_byemail.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals(1)||result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    tagList.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String tag_name = tags_name.getString("tag_name");
                        String tid = tags_name.getString("tid");
                        String trid = tags_name.getString("trid");
                        String range_loc = tags_name.getString("range_loc");
                        String division_name = tags_name.getString("division_name");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String checklist_done = tags_name.getString("checklist_done");
                        String email = tags_name.getString("email");
                        String before_foto = tags_name.getString("before_foto");
                        String after_foto = tags_name.getString("after_foto");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc, division_name, "",
                                tgl, jam, tid, checklist_done, trid, email,after_foto,before_foto, latitude,longitude);
                        tagList.add(tags);
                        mAdapter.notifyDataSetChanged();
                        //Log.d(TAG, "setData: "+employee_name);
                    }

                    pDialog.cancel();

                } else {
                    pDialog.cancel();
                }
                pDialog.cancel();
            } catch (JSONException e) {
                e.printStackTrace();
                pDialog.cancel();

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("email", mEmail);
                return MyData;
            }
        };

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        MyStringRequest.setRetryPolicy(policy);
        MyRequestQueue.add(MyStringRequest);

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
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
                if (message.equals("Checklist update success")){
                    getData();
                }
            }
        }
    };

    private class GetPhoto extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... arg0) {
            String mEmail = sharedPref.getString("email", "olmatix1");
            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());
            String url = "https://olmatix.com/wamonitoring/get_foto_employee.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("message");

                    JSONArray cast = jObject.getJSONArray("employee");
                    String imageString = null;
                    for (int i = 0; i < cast.length(); i++) {


                        JSONObject tags_name = cast.getJSONObject(i);
                        imageString = tags_name.getString("foto");
                        if (imageString.length()>2) {

                            Glide.with(getBaseContext())
                                    .load(imageString)
                                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                    .into(foto);

                        } else {
                            foto.setImageResource(R.drawable.user);

                        }


                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("email", mEmail);
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
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

        }
    }

    private void sendNotification(final String reg_token, String body, String title, String imgUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("yy-d-MM hh:mm:ss");
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
