package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.system.ErrnoException;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AnalogClock;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.bumptech.glide.Glide;
import com.google.android.gms.maps.MapsInitializer;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.BuildConfig;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.fragment.ChatFrag;
import com.lesjaw.wamonitoring.ui.fragment.LogAlert;
import com.lesjaw.wamonitoring.ui.fragment.Main;
import com.lesjaw.wamonitoring.ui.fragment.TagInfo;
import com.lesjaw.wamonitoring.ui.fragment.Tags;
import com.lesjaw.wamonitoring.utils.AESEncryptionDecryption;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.DataPagerAdapter;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.theartofdev.edmodo.cropper.CropImageView;

import net.gotev.uploadservice.MultipartUploadRequest;
import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadNotificationConfig;
import net.gotev.uploadservice.UploadStatusDelegate;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import io.ghyeok.stickyswitch.widget.StickySwitch;

public class MainActivity extends AppCompatActivity implements LocationListener, NavigationView.OnNavigationItemSelectedListener {

    private ViewPager mViewPager;
    private DataPagerAdapter mDataPagerAdaper;
    private TabLayout tabLayout;
    private CoordinatorLayout coordinatorLayout;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private FirebaseAuth auth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private String companyID, divTrim, mUserName, mDistrict;
    private String mLevelUser, mEmail;
    private NfcAdapter mNfcAdapter;
    boolean nfcdevices = false;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "MainActivity";
    boolean isGPS = false;
    boolean isNetwork = false;
    boolean canGetLocation = true;
    private LocationManager locationManager;
    private Location loc;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 100;
    private static final long MIN_TIME_BW_UPDATES = 1000 * 60 * 30;
    private CircleImageView imageView;
    private SlideUp slideUp;
    private View dim;
    private FloatingActionButton fab;
    private Bitmap cropped;
    private CropImageView mCropImageView;
    private static final String IMAGE_DIRECTORY = "/wamonitoring";
    private EditText note;
    private Bitmap icon;
    private String versionName;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private int backButtonCount, position;
    boolean on_tagging = false;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private StyleableToast styleableToast;
    private StickySwitch oNoFF;
    int firstopen = 0;
    String labelAbsen, clock;
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
        }
    };
    private Toolbar toolbar;
    private ProgressDialog pDialog;
    boolean insertRecord = false;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new TedPermission(getBaseContext())
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA, Manifest.permission.NFC, Manifest.permission.MODIFY_AUDIO_SETTINGS,
                        Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        MapsInitializer.initialize(this);

        View sliderView = findViewById(R.id.slideViewMain);
        //TextView slidedown = (TextView) findViewById(R.id.textView);
        mCropImageView = (CropImageView) findViewById(R.id.imageView2);
        note = (EditText) findViewById(R.id.noteEdit);
        new OkHttpClient();

        sliderView.setOnClickListener(v -> {
            slideUp.hide();
            fab.show();
        });

        icon = BitmapFactory.decodeResource(getResources(),
                R.drawable.logowam);

        mCropImageView.setImageBitmap(icon);

        dim = findViewById(R.id.dim);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        slideUp = new SlideUpBuilder(sliderView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                        dim.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        /*if (visibility == View.GONE) {

                        }*/
                    }
                })
                .withStartGravity(Gravity.BOTTOM)
                .withLoggingEnabled(false)
                .withGesturesEnabled(false)
                .withStartState(SlideUp.State.HIDDEN)
                .build();

        fab.setOnClickListener(view -> {
            // jika sudah absen
            if (mPrefHelper.getLogin().equals("1")) {
                slideUp.show();
                fab.hide();
            // jika belum absen
            } else {
                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf("Sorry, you can't create logs when you are not Working"))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
        });

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView username = (TextView) headerView.findViewById(R.id.textView);
        imageView = (CircleImageView) headerView.findViewById(R.id.img_profile);
        oNoFF = (StickySwitch ) headerView.findViewById(R.id.onOff);

        versionName = BuildConfig.VERSION_NAME;

        navigationView.setNavigationItemSelectedListener(this);
        invalidateOptionsMenu();
        //MenuItem nav_version = menu.findItem(R.id.nav_version);
        //nav_version.setTitle("Version "+versionName);

        mPrefHelper = new PreferenceHelper(getApplicationContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        auth = FirebaseAuth.getInstance();

        String logStat = mPrefHelper.getLogin();
        if (logStat.equals("1")) {
            /*oNoFF.setChecked(true);
            oNoFF.setText("Online");*/
            oNoFF.setDirection(StickySwitch.Direction.RIGHT);

        } else {
          /*  oNoFF.setChecked(false);
            oNoFF.setText("Offline");*/
            oNoFF.setDirection(StickySwitch.Direction.LEFT);

        }
        checkTime();

        mLevelUser = mPrefHelper.getLevelUser();
        mUserName = sharedPref.getString("real_name", "olmatix1");
        mDistrict = sharedPref.getString("division", "olmatix1");
        companyID = sharedPref.getString("company_id", "olmatix1");

        mEmail = sharedPref.getString("email", "olmatix1");
        String mPass = sharedPref.getString("password", "olmatix1");

        mAuthListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user != null) {
                // User is signed in
                //Log.d("DEBUG", "onAuthStateChanged:signed_in:" + user.getUid() + " email " + user.getEmail());
                mPrefHelper.setUserName(mUserName);
                mPrefHelper.setUserEmail(user.getEmail());
                mPrefHelper.setGoogle(2);
            } else {
                // User is signed out
                //Log.d("DEBUG", "onAuthStateChanged:signed_out");
                mPrefHelper.setGoogle(1);
            }
            // ...
        };

        SharedPreferences.Editor editor = sharedPref.edit();
        switch (mLevelUser) {
            case "0":
                navigationView.getMenu().clear(); //clear old inflated items.
                navigationView.inflateMenu(R.menu.activity_main_drawer); //inflate new items.
                username.setText(mUserName + "\nAdministrator");
                mPrefHelper.setDivName("none");
                editor.putString("division","none");
                editor.apply();

                break;
            case "1":
                navigationView.getMenu().clear(); //clear old inflated items.
                navigationView.inflateMenu(R.menu.activity_main_drawer); //inflate new items.
                username.setText(mUserName + "\nDivision Head " + mPrefHelper.getDivName());

                break;
            case "2":
                navigationView.getMenu().clear(); //clear old inflated items.
                navigationView.inflateMenu(R.menu.activity_main_drawer); //inflate new items.
                username.setText(mUserName + " - " + mPrefHelper.getDivName());

                break;

            case "4":
                navigationView.getMenu().clear(); //clear old inflated items.
                navigationView.inflateMenu(R.menu.activity_main_drawer); //inflate new items.
                username.setText(mUserName + "\nGroup Head " + mPrefHelper.getDivName());
                // group head ga punya divisi karena dia diatas admin (perusahaan), struktur group head, admin, div head, staff
                mPrefHelper.setDivName("none");
                editor.putString("division","none");
                editor.apply();
                break;
        }

        mViewPager = (ViewPager) findViewById(R.id.container);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        mDataPagerAdaper = new DataPagerAdapter(getSupportFragmentManager());

        tabLayout.setupWithViewPager(mViewPager);
        setupViewPager(mViewPager);

        RegisterFirebase(mEmail, mPass);

        locationManager = (LocationManager) getSystemService(Service.LOCATION_SERVICE);
        isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(MainActivity.this);
        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            if (mLevelUser.equals("2")) {
                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text("This device doesn't support NFC.")
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
            }
            nfcdevices = false;
        } else {
            if (!mNfcAdapter.isEnabled()) {
                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text("NFC disabled.")
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
            }
            nfcdevices = true;
            handleIntent(getIntent());
        }

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "onReceive: " + intent);
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notification

                    displayFirebaseRegId(mEmail);

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String type = intent.getStringExtra("type");
                    String message = intent.getStringExtra("message");
                    String user = intent.getStringExtra("user");
                    //String time = intent.getStringExtra("timestamp");

                    if (!user.equals(mUserName)) {

                        if (type.equals("0") && position != 3) {
                            callSnackbar(message + " by " + user);
                        } else if (type.equals("1")) {
                            callSnackbar(message + " by " + user);
                        } else if (type.equals("2")) {
                            callSnackbar(message + " by " + user);
                        } else if (type.equals("4")) {
                            // ini untuk apa ya ?
                            styleableToast = new StyleableToast
                                    .Builder(getBaseContext())
                                    .icon(R.drawable.ic_action_info)
                                    .text(message + " | " + user)
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.BLUE)
                                    .build();
                            styleableToast.show();
                        }


                    }

                } else if (intent.getAction().equals(Config.SEND_MESSAGE)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");

                            styleableToast = new StyleableToast
                                    .Builder(getBaseContext())
                                    .icon(R.drawable.ic_action_info)
                                    .text(message)
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.BLUE)
                                    .build();
                            styleableToast.show();

                }
            }
        };

        displayFirebaseRegId(mEmail);

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                position = tab.getPosition();
                //if (!mLevelUser.equals("2") && position == 2) {
                if (position == 2) {
                    fab.show();
                } else {
                    fab.hide();
                    slideUp.hide();
                }

                int colorFrom = ((ColorDrawable) toolbar.getBackground()).getColor();
                int colorTo = getColorForTab(tab.getPosition());

                ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
                colorAnimation.setDuration(1000);
                colorAnimation.addUpdateListener(animator -> {
                    int color = (int) animator.getAnimatedValue();

                    toolbar.setBackgroundColor(color);
                    tabLayout.setBackgroundColor(color);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        getWindow().setStatusBarColor(color);
                    }
                });
                colorAnimation.start();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        getPhotoProfileSdcard(mEmail);

        fab.hide();

        updateWidget();

        oNoFF.setOnSelectedChangeListener(new StickySwitch.OnSelectedChangeListener() {
            @Override
            public void onSelectedChange(@NotNull StickySwitch.Direction direction, @NotNull String s) {

                //Log.d(TAG, "Now Selected : " + direction.name() + ", Current Text : " + s);
                if (firstopen != 0) {

                    oNoFF.setEnabled(false);
                    if (direction.name().equals("RIGHT")) {
                        labelAbsen = "Do you want absence go to WORK?";
                        clock = "ClockIn";
                    } else {
                        labelAbsen = "Do you want absence go to HOME?";
                        clock = "ClockOut";

                    }

                    pDialog = new ProgressDialog(MainActivity.this);
                    pDialog.setMessage("Doing absence. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();

                    String url = Config.DOMAIN + "wamonitoring/get_working_gps.php";

                    StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                        try {
                            JSONObject jObject = new JSONObject(response);
                            String result_code = jObject.getString("success");
                            if (result_code.equals("1")) {
                                JSONArray cast = jObject.getJSONArray("employee");

                                for (int i = 0; i < cast.length(); i++) {
                                    JSONObject tags_name = cast.getJSONObject(i);
                                    String working_gps = tags_name.getString("working_gps");

                                    if (working_gps.isEmpty()) {
                                        working_gps = "0.0,0.0";
                                    }

                                    String locSplit[] = working_gps.split(",");
                                    double locLat = Double.parseDouble(String.valueOf(locSplit[0]));
                                    double locLot = Double.parseDouble(String.valueOf(locSplit[1]));

                                    double prefLat = mPrefHelper.getPhoneLatitude();
                                    double prefLot = mPrefHelper.getPhoneLongitude();

                                    final Geocoder geocoder = new Geocoder(getBaseContext(), Locale.getDefault());
                                    String adString = null;
                                    try {
                                        List<Address> list;
                                        list = geocoder.getFromLocation(prefLat, prefLot, 1);
                                        if (list != null && list.size() > 0) {
                                            Address address = list.get(0);
                                            address.getLocality();

                                            if (address.getAddressLine(0) != null)
                                                adString = address.getAddressLine(0);

                                        }

                                    } catch (final IOException e) {
                                        new Thread(() -> Log.e("DEBUG", "Geocoder ERROR", e)).start();
                                    }

                                    Location startPoint = new Location("locationA");
                                    startPoint.setLatitude((locLat));
                                    startPoint.setLongitude((locLot));

                                    Location endPoint = new Location("locationB");
                                    endPoint.setLatitude((prefLat));
                                    endPoint.setLongitude((prefLot));

                                    double distance = startPoint.distanceTo(endPoint);
                                    int distanceInt = (int) distance;

                                    AnalogClock jam = new AnalogClock(MainActivity.this);
                                    LinearLayout layout = new LinearLayout(MainActivity.this);
                                    layout.setOrientation(LinearLayout.VERTICAL);
                                    layout.addView(jam);

                                    new AlertDialog.Builder(MainActivity.this)
                                            .setTitle("Absence")
                                            .setIcon(R.mipmap.ic_check_black)
                                            .setMessage(labelAbsen + "\n\nYou are now at " + adString + "\n\nIt's " +
                                                    Utils.getJarak(distanceInt) + " from your work location")
                                            .setCancelable(false)
                                            .setView(layout)
                                            .setPositiveButton(clock, (dialog, id) -> {

                                                if (s.equals("IN")) {
                                                    absence("1", String.valueOf(distanceInt));

                                                } else {
                                                    absence("2", String.valueOf(distanceInt));
                                                }
                                            })

                                            .setNegativeButton("Cancel", (dialog, id) -> {
                                                firstopen = 0;
                                                if (s.equals("IN")) {
                                                    oNoFF.setDirection(StickySwitch.Direction.LEFT);
                                                } else {
                                                    oNoFF.setDirection(StickySwitch.Direction.RIGHT);

                                                }

                                                dialog.dismiss();
                                                firstopen = 1;
                                                pDialog.dismiss();

                                            })

                                            .show();

                                    oNoFF.setEnabled(true);

                                }

                            } else {

                                styleableToast = new StyleableToast
                                        .Builder(getBaseContext())
                                        .icon(R.drawable.ic_action_info)
                                        .text("Sorry can't absence temporary, please try again later")
                                        .textColor(Color.WHITE)
                                        .backgroundColor(Color.BLUE)
                                        .build();
                                styleableToast.show();

                                oNoFF.setEnabled(true);


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> {
                        Log.d(TAG, "onErrorResponse: " + error);
                        if (mPrefHelper.getLogin().equals("1")) {
                            oNoFF.setDirection(StickySwitch.Direction.RIGHT);

                        } else {
                            oNoFF.setDirection(StickySwitch.Direction.LEFT);

                        }

                        styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text(String.valueOf("Network error, try again later"))
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .build();
                        styleableToast.show();
                        oNoFF.setEnabled(true);
                        pDialog.dismiss();

                    }) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<>();
                            MyData.put("email", mEmail);
                            return MyData;
                        }
                    };
                    // Adding request to request queue
                    NetworkRequest.getInstance(MainActivity.this).addToRequestQueue(jsonObjReq);
                }
            }
        });

        firstopen =1;

    }

    private void checkTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);


        Calendar now;
        now = Calendar.getInstance();
        now.setTime(new Date());
        long phoneTime = now.getTimeInMillis();
        long serverTime  = mPrefHelper.getServerTime();

        String phoneNow = simpleDateFormat.format(phoneTime);
        String serverNow = simpleDateFormat.format(serverTime);

        String phoneWaktu = simpleDateFormat1.format(phoneTime);
        String serverWaktu = simpleDateFormat1.format(serverTime);

        Log.d(TAG, "checkTime: "+phoneNow +" "+phoneWaktu+" | "+serverNow+" "+serverWaktu);

        if (!phoneNow.equals(serverNow)){
            StyleableToast styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Date error, it seems that your phone date is WRONG!, exiting now..")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();

            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 4000);
        }

    }

    private void callSnackbar(String message) {
        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, message, TSnackbar.LENGTH_SHORT);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#000000"));

        snackbar.show();
    }

    public int getColorForTab(int position) {
        if (position == 0) return ContextCompat.getColor(this, R.color.colorPrimary);
        else if (position == 1) return ContextCompat.getColor(this, R.color.indigo);
        else if (position == 2) return ContextCompat.getColor(this, R.color.node_title);
        else if (position == 3) return ContextCompat.getColor(this, R.color.amberdark);
        else return ContextCompat.getColor(this, R.color.grey);
    }

    private void getPhotoProfileSdcard(String email) {
        String IMAGE_DIRECTORY = "/wamonitoring";
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + "/upload");
        File f = new File(wallpaperDirectory, email + ".jpg");
        String imageInSD = f.getAbsolutePath();
        Glide.with(getBaseContext())
                .load(imageInSD)
                .into(imageView);
    }

    public void onLoadImageClick(View view) {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }

    public void onCropImageClick(View view) {
        cropped = mCropImageView.getCroppedImage(1000, 1000);
        if (cropped != null)
            mCropImageView.setImageBitmap(cropped);
    }

    public void onUploadImageClick(View view) {
        String checkText = note.getText().toString();

        if (checkText.isEmpty()) {
            note.setHint("type some word");
        } else {
            if (cropped != null) {
                saveImage(cropped);
                slideUp.hide();
                note.setText("");
                mCropImageView.setImageBitmap(icon);


            } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You are not cropping the photo, sending text message only?", TSnackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.setAction("OK", v -> {
                    insertAlert(note.getText().toString().trim());
                    slideUp.hide();
                    fab.show();
                    note.setText("");
                });
                snackbar.show();
            }
        }
    }

    public Intent getPickImageChooserIntent() {

        Uri outputFileUri = getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager = getPackageManager();

        Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery = packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new Intent(galleryIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

        Intent mainIntent = allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity")) {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

        Intent chooserIntent = Intent.createChooser(mainIntent, "Select source");

        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImage = getExternalCacheDir();
        if (getImage != null) {
            outputFileUri = Uri.fromFile(new File(getImage.getPath(), "pickImageResult.jpeg"));
        }
        return outputFileUri;
    }

    public Uri getPickImageResultUri(Intent data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ? getCaptureImageOutputUri() : data.getData();
    }

    public boolean isUriRequiresPermissions(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            stream.close();
            return false;
        } catch (FileNotFoundException e) {
            if (e.getCause() instanceof ErrnoException) {
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data != null) Log.d(TAG, "onActivityResult: " + resultCode + " " + data.getData());

        if (resultCode == Activity.RESULT_OK) {
            Uri imageUri = getPickImageResultUri(data);
            boolean requirePermissions = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    isUriRequiresPermissions(imageUri)) {

                // request permissions and handle the result in onRequestPermissionsResult()
                requirePermissions = true;
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }

            if (!requirePermissions) {
                mCropImageView.setImageUriAsync(imageUri);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void displayFirebaseRegId(String mEmail) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e("DEBUG", "Firebase reg id: " + regId);


        if (!TextUtils.isEmpty(regId)) {
            //Toast.makeText(getApplicationContext(), "Firebase reg id:  " + regId, Toast.LENGTH_LONG).show();
            new FirebaseID(mEmail, regId).execute();

        } else {
            String regid = FirebaseInstanceId.getInstance().getToken();
            Log.e("DEBUG", "onTokenRefresh: " + regid + mEmail);
            new FirebaseID(mEmail, regid).execute();
            //Toast.makeText(getApplicationContext(), "Firebase Reg Id is not received yet! " + regId+ " " + regid, Toast.LENGTH_LONG).show();
        }
        String div = sharedPref.getString("division", "olmatix1");
        divTrim = div.replace(" ", "");

        FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
        FirebaseMessaging.getInstance().subscribeToTopic(companyID);
        FirebaseMessaging.getInstance().subscribeToTopic(companyID + "-" + divTrim);
        if (!mLevelUser.equals("2")) {
            FirebaseMessaging.getInstance().subscribeToTopic(companyID + "-AM");
        }

    }

    public void RegisterFirebase(String email, String password) {

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, task -> {
                    //Toast.makeText(MainActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                    // If sign in fails, display a message to the user. If sign in succeeds
                    // the auth state listener will be notified and logic to handle the
                    // signed in user can be handled in the listener.
                    if (!task.isSuccessful()) {
                        //Toast.makeText(MainActivity.this, "Authentication failed." + task.getException(),
                        //        Toast.LENGTH_SHORT).show();
                        //Log.d("DEBUG", "onComplete: " + task.getException());
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            //Toast.makeText(MainActivity.this, "User with this email already exist, trying login to chat now", Toast.LENGTH_SHORT).show();
                            auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener(MainActivity.this, task1 -> {
                                        Log.d("DEBUG", "signInWithEmail:onComplete:" + task1.isSuccessful());
                                        //Toast.makeText(MainActivity.this, "Auth success "+task.isSuccessful(),
                                        //        Toast.LENGTH_SHORT).show();
                                        mPrefHelper.setGoogle(2);
                                        // If sign in fails, display a message to the user. If sign in succeeds
                                        // the auth state listener will be notified and logic to handle the
                                        // signed in user can be handled in the listener.
                                        if (!task1.isSuccessful()) {

                                            styleableToast = new StyleableToast
                                                    .Builder(getBaseContext())
                                                    .icon(R.drawable.ic_upload_error)
                                                    .text("Auth failed")
                                                    .textColor(Color.WHITE)
                                                    .backgroundColor(Color.BLUE)
                                                    .build();

                                        }

                                    });
                        }

                    }
                });
    }

    private void setupViewPager(ViewPager viewPager) {

        if (!mLevelUser.equals("2")) {
            mDataPagerAdaper.addFrag(new Main(), "MAIN");
            mDataPagerAdaper.addFrag(new Tags(), "TAGS");
            mDataPagerAdaper.addFrag(new LogAlert(), "Alert");
            mDataPagerAdaper.addFrag(new ChatFrag(), "CHATS");
            viewPager.setOffscreenPageLimit(4);

        } else {
            mDataPagerAdaper.addFrag(new Main(), "MAIN");
            mDataPagerAdaper.addFrag(new Tags(), "TAGS");
            mDataPagerAdaper.addFrag(new LogAlert(), "ALERT");
            mDataPagerAdaper.addFrag(new ChatFrag(), "CHATS");
            viewPager.setOffscreenPageLimit(4);

        }
        viewPager.setAdapter(mDataPagerAdaper);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        // super.onSaveInstanceState(outState);
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        menu = navigationView.getMenu();
        menu.findItem(R.id.nav_version).setTitle("Version " + versionName);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        auth.addAuthStateListener(mAuthListener);
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));

        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.REGISTRATION_COMPLETE));

        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.SEND_MESSAGE));
    }

    @Override
    public void onStop() {
        if (mAuthListener != null) {
            auth.removeAuthStateListener(mAuthListener);
        }

        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onStop();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mNfcAdapter != null) {
            try {
                setupForegroundDispatch(MainActivity.this, mNfcAdapter);
            } catch (Exception e) {
                Log.e(TAG, "Error starting foreground dispatch", e);
            }
        }

        /*LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));*/
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: 2");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            return true;
        }

        if (id == R.id.action_settings) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.action_foto) {
            Intent i = new Intent(this, UploadFotoAcivity.class);
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

            final EditText pass = new EditText(MainActivity.this);
            pass.setHint("Type logout password");
            pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pass.setMaxLines(1);

            String divPass;
            if (mPrefHelper.getLevelUser().equals("1")){
                divPass = "none";
            } else {
                divPass = sharedPref.getString("division", "olmatix1");
            }


            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(pass);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Logout")
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setMessage("Do you want to logout?")
                    .setView(layout)
                    .setPositiveButton("Ok", (dialog, id1) -> {
                        Log.d(TAG, "onOptionsItemSelected: "+divPass+" : "+pass.getText().toString().trim());

                        String url = Config.DOMAIN + "wamonitoring/get_pass_logout.php";

                        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

                            try {

                                JSONObject jObject = new JSONObject(response);
                                String result_message = jObject.getString("message");
                                String result_code = jObject.getString("success");

                                if (result_code.equals("1")) {

                                    LogOut();

                                } else {
                                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, result_message,
                                            TSnackbar.LENGTH_LONG);
                                    View snackbarView = snackbar.getView();
                                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                                    snackbar.setAction("OK", v -> {

                                    });
                                    snackbar.setActionTextColor(Color.BLACK);
                                    snackbar.show();
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
                            protected Map<String, String> getParams() {
                                Map<String, String> MyData = new HashMap<>();
                                MyData.put("company_id", companyID);
                                MyData.put("division", divPass);
                                MyData.put("password", pass.getText().toString().trim());
                                MyData.put("email", mEmail);

                                return MyData;
                            }
                        };

                        // Adding request to request queue
                        NetworkRequest.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);
                    })

                    .setNegativeButton("Cancel", (dialog, id12) -> dialog.cancel())

                    .show();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    private void LogOut() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email", "");
        editor.putString("password", "");
        editor.putString("user_name", "John Smith");
        editor.apply();

        FirebaseMessaging.getInstance().unsubscribeFromTopic(Config.TOPIC_GLOBAL);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(companyID);
        FirebaseMessaging.getInstance().unsubscribeFromTopic(companyID + "-" + divTrim);
        if (!mLevelUser.equals("2")) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(companyID + "-AM");
        }
        auth.signOut();
        mPrefHelper.setGoogle(1);
        mPrefHelper.setLastLog("Last tag log by notification");
        mPrefHelper.setDivisionFull("Empty");
        mPrefHelper.setEmployment("Empty");
        mPrefHelper.setEmployee_status("Empty");
        mPrefHelper.setLogin("2");

        finish();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_create_db) {
            if (!mLevelUser.equals("2")) {
                Intent i = new Intent(getBaseContext(), DBStructureActivity.class);
                startActivity(i);
            } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry you are not allowed to use this menu", TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#000000"));
                snackbar.show();
            }
            return true;

        }
        if (id == R.id.nav_tag_monitor) {
            if (!mLevelUser.equals("2")) {
                Intent i = new Intent(getBaseContext(), MainDataActivity.class);
                startActivity(i);
            } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry you are not allowed to use this menu", TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#000000"));
                snackbar.show();
            }
            return true;

        }
        if (id == R.id.nav_track_employee) {
            if (!mLevelUser.equals("2")) {
                Intent i = new Intent(getBaseContext(), TrackEmployeeActivity.class);
                startActivity(i);
            } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry you are not allowed to use this menu", TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#000000"));
                snackbar.show();
            }
            return true;

        }
        if (id == R.id.nav_employee_db) {
            if (!mLevelUser.equals("2")) {

                Intent i = new Intent(getBaseContext(), ListEmployeeActivity.class);
                startActivity(i);
            } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry you are not allowed to use this menu", TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#000000"));
                snackbar.show();
            }
            return true;

        }

        if (id == R.id.nav_absence) {
            if (!mLevelUser.equals("2")) {
                Intent i = new Intent(getBaseContext(), MainAbsenActivity.class);
                //i.putExtra("id_fragement", "Absence");
                startActivity(i);
            } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry you are not allowed to use this menu", TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#000000"));
                snackbar.show();
            }
            return true;

        }

        if (id == R.id.nav_version) {
            styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Version " + versionName)
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.BLUE)
                    .build();
            styleableToast.show();
            return true;

        }

        if (id == R.id.nav_about) {
            Intent i = new Intent(this, AboutActivity.class);
            startActivity(i);
            return true;
        }


        if (id == R.id.nav_preferences) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
            return true;
        }


        return false;
    }

    private class FirebaseID extends AsyncTask<Void, Void, Void> {

        String email, fbid;

        FirebaseID(String email, String fbid) {
            this.email = email;
            this.fbid = fbid;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            Log.d("DEBUG", "doInBackground: " + email + " " + fbid);

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/update_fbid.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    //String result_code = jObject.getString("success");
                    //Log.d("DEBUG", "doInBackground: " + result_code + " email " + email + " id " + fbid);
                    /*if (result_code.equals("1")) {
                        //Log.d("DEBUG", "doInBackground: " + result_code + " email " + email + " id " + fbid);

                    }*/

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("email", email);
                    MyData.put("firebase_id", fbid);
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
                Log.d("DEBUG", "Wrong mime type: " + type);
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
            int languageCodeLength = payload[0] & 51;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {

                Log.d(TAG, "onPostExecute NFC: " + result);
                String result1 = "";
                try {
                    result1 = AESEncryptionDecryption.decrypt(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d(TAG, "onPostExecute NFC after decrypt: " + result1);

                try {
                    JSONObject jObject = new JSONObject(result1);
                    //String result_name = jObject.getString("name");
                    //String result_division = jObject.getString("division");
                    String result_tagID = jObject.getString("code");
                    // String result_loc = jObject.getString("loc");
                    getTags(result_tagID);
                    /*Log.d(TAG, "handleResult: " + mprefDivName + ":" + result_division);

                    if (mprefDivName.equals(result_division)) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "tag name : " + result_name + "\ndivision : " + result_division, TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            // get location
                            getLocation();
                            Log.d(TAG, "split location: " + result_loc);

                            pDialog = new ProgressDialog(MainActivity.this);
                            pDialog.setMessage("Validating. Please wait...");
                            pDialog.setCancelable(true);
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.show();

                            String locSplit[] = result_loc.split(",");
                            String locLat = locSplit[0];
                            String locLot = locSplit[1];

                            Log.d(TAG, "split location: " + locLat + ":" + locLot);
                            getTags(result_tagID, locLat, locLot);
                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    } else if (mprefDivName.equals("none")) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "tag name : " + result_name + "\ndivision : " + result_division, TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            // get location
                            /*getLocation();
                            Log.d(TAG, "split location: " + result_loc);

                            pDialog = new ProgressDialog(MainActivity.this);
                            pDialog.setMessage("Validating. Please wait...");
                            pDialog.setCancelable(true);
                            pDialog.setCanceledOnTouchOutside(false);
                            pDialog.show();

                            String locSplit[] = result_loc.split(",");
                            String locLat = locSplit[0];
                            String locLot = locSplit[1];

                            Log.d(TAG, "split location: " + locLat + ":" + locLot);
                            getTags(result_tagID, locLat, locLot);
                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You are not allowed to tag this checkpoint" +
                                "", TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {
                            getLocation();

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();*/
                    //}*/


                } catch (JSONException e) {
                    e.printStackTrace();
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

    /*public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }*/

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

    private void updateUI(Location loc) {

        String lat = (Double.toString(loc.getLatitude()));
        String lot = (Double.toString(loc.getLongitude()));
        //tvTime.setText(DateFormat.getTimeInstance().format(loc.getTime()));

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        mPrefHelper.setPhoneLatitude(Double.parseDouble(lat));
        mPrefHelper.setPhoneLongitude(Double.parseDouble(lot));

    }

    @Override
    public void onBackPressed() {

        int tabpos = tabLayout.getSelectedTabPosition();
        if (tabpos == 3) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                mViewPager.setCurrentItem(2);
            }
        } else if (tabpos == 2) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                mViewPager.setCurrentItem(1);
            }
        } else if (tabpos == 1) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                mViewPager.setCurrentItem(0);
            }
        } else if (tabpos == 0) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                if (backButtonCount >= 1) {

                    finish();
                    //System.exit(0);
                } else {
                    TSnackbar snackbar = TSnackbar.make((coordinatorLayout), "You are exiting " + getResources().getString(R.string.app_name)
                            , TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbar.setIconRight(R.mipmap.logo, 24);
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.show();

                    backButtonCount++;
                }


            }
        }
        //super.onBackPressed();

    }

    private void getTags(String tgid) {

        //String tag_json_obj = "getTags";

        Log.d(TAG, "getTags: " + tgid);

        String url = Config.DOMAIN + "wamonitoring/get_tags_data.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            String tag_location = null, division_name = null;

            try {

                JSONObject jObject = new JSONObject(response);
                //String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    JSONArray cast = jObject.getJSONArray("tags");
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        tag_location = tags_name.getString("tag_location");
                        division_name = tags_name.getString("division_name");
                    }

                    String mprefDivName = mPrefHelper.getDivName();
                    String mprefDivName1 = sharedPref.getString("division", "jakarta");

                    //Log.d(TAG, "getTags: " + mprefDivName + " " + division_name);
                    boolean executeTags;
                    //Log.d(TAG, "getTags1: " + executeTags);
                    executeTags = mprefDivName.equals(division_name) || mprefDivName1.equals("none");

                    if (executeTags) {
                        //Log.d(TAG, "getTags3: " + executeTags);
                        pDialog = new ProgressDialog(MainActivity.this);
                        pDialog.setMessage("Validating. Please wait...");
                        pDialog.setCancelable(true);
                        pDialog.setCanceledOnTouchOutside(false);
                        pDialog.show();

                        getLocation();
                        String locSplit[] = tag_location.split(",");
                        String locLat = locSplit[0];
                        String locLot = locSplit[1];
                        if (!on_tagging) {
                            ValidateTags(tgid, locLat, locLot);
                            on_tagging = true;

                        }

                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You are not allowed to tag this checkpoint" +
                                "", TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> getLocation());
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();

                    }

                } else {

                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Tag checkpoint is not REGISTERED, contact ADMIN",
                            TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> {

                    });
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();
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
            on_tagging = false;

        })

        {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("tid", tgid);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void ValidateTags(String tgid, String tgLat, String tgLot) {

        //String tag_json_obj = "validateTags";
        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mEmail = sharedPref.getString("email", "olmatix1");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
        String tgl1 = df.format(Calendar.getInstance().getTime());

        String url = Config.DOMAIN + "wamonitoring/validate_tags_before_sending.php";

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

                    }

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH    );
                    String dateNow = simpleDateFormat.format(Calendar.getInstance().getTime());

                    Log.d(TAG, "data: " + tgl + dateNow);

                    SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm:ss",Locale.ENGLISH);
                    Date startDate = null, endDate = null;
                    try {
                        String jamNow = simpleDateFormat1.format(Calendar.getInstance().getTime());
                        startDate = simpleDateFormat1.parse(jam);
                        endDate = simpleDateFormat1.parse(jamNow);

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    long difference = endDate.getTime() - startDate.getTime();

                    Log.d(TAG, "time difference: " + difference);

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

                        if (tgl.equals(dateNow)) {
                            pDialog.cancel();
                            on_tagging = false;

                            TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You just Tag this tag " + min + " minute ago",
                                    TSnackbar.LENGTH_INDEFINITE);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.setAction("OK", v -> {

                            });
                            snackbar.setActionTextColor(Color.BLACK);
                            snackbar.show();

                        } else {
                            if (!insertRecord){
                                InsertTags(tgid, tgLat, tgLot);

                            }
                        }
                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You Tag this tag " + hours + " hours "
                                        + min + " minute ago",
                                TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> {

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                        if (!insertRecord) {

                            InsertTags(tgid, tgLat, tgLot);
                        }

                    }


                } else {
                    if (!insertRecord) {
                        InsertTags(tgid, tgLat, tgLot);
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
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void InsertTags(String tgid, String tgLat, String tgLot) {

        insertRecord = true;
        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mEmployee_name = sharedPref.getString("real_name", "olmatix1");
        //String mDivision = sharedPref.getString("division", "olmatix1");

        String mEmail = sharedPref.getString("email", "olmatix1");
        String mLat = String.valueOf(mPrefHelper.getPhoneLatitude());
        String mLot = String.valueOf(mPrefHelper.getPhoneLongitude());

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
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

        String url = Config.DOMAIN + "wamonitoring/insert_tag_record.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

            try {


                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {
                    sendNotification(mCompanyID, mPrefHelper.getDivName(), mEmployee_name, "TAGS");

                   /* pDialog.cancel();
                    on_tagging = false;*/

                }

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, result_message, TSnackbar.LENGTH_INDEFINITE);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
                pDialog.cancel();
                on_tagging = false;

                Intent intent = new Intent("tags");
                intent.putExtra("notify", "Checklist update success");
                LocalBroadcastManager.getInstance(MainActivity.this).sendBroadcast(intent);
                insertRecord = true;


            } catch (JSONException e) {
                e.printStackTrace();
                insertRecord = true;

            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
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
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 20, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + "/notice");
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {

            Calendar now;
            now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();

            File f = new File(wallpaperDirectory, now.getTimeInMillis() + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            new uploadImg(f.getAbsolutePath(), note.getText().toString(), now.getTimeInMillis()).execute();

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private class uploadImg extends AsyncTask<Void, Void, Void> {
        String img, notes;
        long millis;

        uploadImg(String img, String notes, long millis) {
            this.img = img;
            this.notes = notes;
            this.millis = millis;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mEmail = sharedPref.getString("email", "olmatix1");
            String url = "https://olmatix.com/wamonitoring/upload_panic_log_message.php";
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
            String tgl = df.format(Calendar.getInstance().getTime());
            try {
                String uploadId = UUID.randomUUID().toString();
                new MultipartUploadRequest(MainActivity.this, uploadId, url)
                        .addFileToUpload(img, "image") //Adding file
                        .addParameter("name", mEmail) //Adding text parameter to the request
                        .addParameter("employee_name", mUserName) //Adding text parameter to the request
                        .addParameter("type", "1") //Adding text parameter to the request
                        .addParameter("company_id", companyID) //Adding text parameter to the request
                        .addParameter("date_created_panic", tgl) //Adding text parameter to the request
                        .addParameter("notes", notes) //Adding text parameter to the request
                        .addParameter("millis", String.valueOf(millis)) //Adding text parameter to the request

                        .setNotificationConfig(new UploadNotificationConfig())
                        .setMaxRetries(2)
                        .setDelegate(new UploadStatusDelegate() {
                            @Override
                            public void onProgress(Context context, UploadInfo uploadInfo) {

                            }

                            @Override
                            public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse, Exception exception) {

                            }

                            @Override
                            public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
                                //Toast.makeText(getBaseContext(), "Upload complete", Toast.LENGTH_SHORT).show();

                                Intent pushNotification = new Intent(Config.SEND_NOTICE);
                                pushNotification.putExtra("message", "sending notice");
                                LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(pushNotification);
                            }

                            @Override
                            public void onCancelled(Context context, UploadInfo uploadInfo) {

                            }
                        })
                        .startUpload(); //Starting the upload

            } catch (Exception exc) {
                //Toast.makeText(MainActivity.this, exc.getMessage(), Toast.LENGTH_SHORT).show();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

    private void insertAlert(String note) {

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mEmployeeName = sharedPref.getString("real_name", "olmatix1");
        String mEmail = sharedPref.getString("email", "olmatix1");
        String mDivision = sharedPref.getString("division", "olmatix1");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
        String tgl = df.format(Calendar.getInstance().getTime());

        //String tag_json_obj = "insertAlert";

        String url = Config.DOMAIN + "wamonitoring/insert_alert_message.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    //Log.d(TAG, "location insert success ");

                    Intent pushNotification = new Intent(Config.SEND_NOTICE);
                    pushNotification.putExtra("message", "sending notice");
                    LocalBroadcastManager.getInstance(getBaseContext()).sendBroadcast(pushNotification);

                } else {
                    Log.d(TAG, "location insert fail ");

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
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);
                MyData.put("employee_name", mEmployeeName);
                MyData.put("company_id", mCompanyID);
                MyData.put("date_created_panic", tgl);
                MyData.put("notes", note);
                MyData.put("division", mDivision);
                MyData.put("type", "1");

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void sendNotification(final String reg_token, String body, String title, String imgUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
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

    private void updateWidget() {
        Intent intent = new Intent(this, TagInfo.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TagInfo.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        sendBroadcast(intent);

    }

    private void absence(String logStatus, String distance) {

        //String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mEmployeeName = sharedPref.getString("real_name", "olmatix1");
        String mEmail = sharedPref.getString("email", "olmatix1");
        //String mDivision = sharedPref.getString("division", "olmatix1");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String tgl = df.format(Calendar.getInstance().getTime());

        String lat = String.valueOf(mPrefHelper.getPhoneLatitude());
        String lot = String.valueOf(mPrefHelper.getPhoneLongitude());

        String url = Config.DOMAIN + "wamonitoring/insert_update_absense.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    if (logStatus.equals("1")) {
                        //oNoFF.setText("Online");
                        oNoFF.setDirection(StickySwitch.Direction.RIGHT);

                    } else if (logStatus.equals("2")) {
                        //oNoFF.setText("Offline");
                        oNoFF.setDirection(StickySwitch.Direction.LEFT);

                    }

                    mPrefHelper.setLogin(logStatus);
                    pDialog.dismiss();
                    //updateOn_Off(logStatus);

                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", "8");
                    pushNotification.putExtra("message", "");
                    pushNotification.putExtra("user", "");
                    pushNotification.putExtra("timestamp", "");

                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);

                } else {
                    firstopen = 0;
                    pDialog.dismiss();

                    if (result_message.equals("You have absence go work for today")) {
                       /* oNoFF.setChecked(false);
                        oNoFF.setText("Offline");*/
                        oNoFF.setDirection(StickySwitch.Direction.LEFT);

                    }

                    if (result_message.equals("You have absence go home for today")){
                       /* oNoFF.setChecked(false);
                        oNoFF.setText("Offline");*/
                        oNoFF.setDirection(StickySwitch.Direction.LEFT);

                    }

                }

                firstopen = 1;

                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf(result_message))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();

                //firstopen =1;

            } catch (JSONException e) {
                e.printStackTrace();
                pDialog.dismiss();

            }

        }, error -> {
            Log.d(TAG, "onErrorResponse: " + error);
            if (mPrefHelper.getLogin().equals("1")){
                oNoFF.setDirection(StickySwitch.Direction.RIGHT);

            } else {
                oNoFF.setDirection(StickySwitch.Direction.LEFT);

            }

            styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text(String.valueOf("Network error, try again later"))
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();
            pDialog.dismiss();

        }

        ) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);
                MyData.put("employee_name", mEmployeeName);
                MyData.put("latitude", lat);
                MyData.put("longitude", lot);
                MyData.put("log_status", logStatus);
                MyData.put("date_created", tgl);
                MyData.put("distance", distance);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

}
