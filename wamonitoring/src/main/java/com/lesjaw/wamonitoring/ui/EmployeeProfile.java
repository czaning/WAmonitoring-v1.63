package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListEmployeeLocationAdapter;
import com.lesjaw.wamonitoring.adapter.ListTagsLogAdapter;
import com.lesjaw.wamonitoring.model.DivisionAndID;
import com.lesjaw.wamonitoring.model.locEmployeeLastTenModel;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.SpinnerListener;
import com.lesjaw.wamonitoring.utils.Utils;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


public class EmployeeProfile extends AppCompatActivity implements OnMapReadyCallback {
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final String TAG = "EmployeeProfile";
    private String mCompanyID;
    private String mUserName;
    private String mEmail;
    private String mLevelUser;
    private String levelUser;
    private String result_phone1;
    private String result_phone2;
    private String result_place_birth;
    private String result_date_birth;
    private String result_division;
    private String mDivision;
    private String result_divID;
    private String employment;
    private String employment_status ;
    private TextView tagsThisMonth;
    private SlideUp slideUp;
    private View dim;
    private List<tagsDailyModel> tagList = new ArrayList<>();
    private List<locEmployeeLastTenModel> tagList1 = new ArrayList<>();
    private String firbaseID;
    private ListEmployeeLocationAdapter mAdapterLoc;
    private ListTagsLogAdapter mAdapter;
    private RecyclerView mRecycleViewLoc;
    private TextView real_name, place_date_birth, division, email, address, phone, lastseen, level_user;
    private ImageView bg;
    private CircleImageView img;
    private EditText eDateBirth;
    private DatePickerDialog datePickerDialog;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private StyleableToast styleableToast;
    private GoogleMap mMap;
    String working_gps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.employee_detail_activity);

        img = (CircleImageView) findViewById(R.id.img_profile);
        real_name = (TextView) findViewById(R.id.employee_name);
        place_date_birth = (TextView) findViewById(R.id.place_dateBirth);
        division = (TextView) findViewById(R.id.employee_div);
        email = (TextView) findViewById(R.id.employee_email);
        address = (TextView) findViewById(R.id.employee_address);
        phone = (TextView) findViewById(R.id.phone);
        lastseen = (TextView) findViewById(R.id.lastseen);
        level_user = (TextView) findViewById(R.id.level_user);
        bg = (ImageView) findViewById(R.id.bg);
        RecyclerView mRecycleView = (RecyclerView) findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mRecycleViewLoc = (RecyclerView) findViewById(R.id.rvLoc);
        mRecycleViewLoc.setHasFixedSize(true);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        ImageButton edit = (ImageButton) findViewById(R.id.edit);
        tagsThisMonth = (TextView) findViewById(R.id.tagMonth);
        CardView cvMonth = (CardView) findViewById(R.id.cvMonth);
        CardView empDeatil = (CardView) findViewById(R.id.cv);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        mPrefHelper = new PreferenceHelper(getApplicationContext());

        //String companyname = sharedPref.getString("company_name", "olmatix1");
        mCompanyID = sharedPref.getString("company_id", "jakarta");
        mDivision = sharedPref.getString("division", "jakarta");
        String mEmailCaller = sharedPref.getString("email", "jakarta");

        mLevelUser = mPrefHelper.getLevelUser();

        getSupportActionBar().setTitle("Employee details");
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        mEmail = i.getStringExtra("email");
        mUserName = mEmail;

        mAdapter = new ListTagsLogAdapter(tagList, getBaseContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        mAdapterLoc = new ListEmployeeLocationAdapter(tagList1, EmployeeProfile.this);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getBaseContext());
        mRecycleViewLoc.setLayoutManager(layoutManager1);
        mRecycleViewLoc.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewLoc.setAdapter(mAdapterLoc);

        View sliderView = findViewById(R.id.slideViewMain);
        dim = findViewById(R.id.dim);
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


        cvMonth.setOnClickListener(v -> {
            slideUp.show();
            bg.setVisibility(View.INVISIBLE);
            getData("0");

        });

        sliderView.setOnClickListener(v -> {
            slideUp.hide();
            bg.setVisibility(View.VISIBLE);

        });

        if (mLevelUser.equals("0")) {
            edit.setEnabled(true);
            cvMonth.setEnabled(true);

        } else if (mLevelUser.equals("1") && mDivision.equals(result_division)) {
            edit.setEnabled(true);
            cvMonth.setEnabled(true);

        } else if (mLevelUser.equals("2")) {
            edit.setEnabled(false);
            cvMonth.setEnabled(false);

        }

        working_gps = "0.0,0.0";

        edit.setOnClickListener(v -> {
            String buttonDialog;
            String setLev = levelUser;
            if (setLev.equals("2")) {
                buttonDialog = "Set as Admin";
                setLev = "1";
            } else {
                buttonDialog = "UnSet as Admin";
                setLev = "2";

            }
            String finalSetLev = setLev;
            new AlertDialog.Builder(EmployeeProfile.this)
                    .setTitle("Edit or Delete")
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setMessage("Do you want to edit or set admin for this user : " + real_name.getText() + "?")
                    .setPositiveButton(buttonDialog, (dialog, id) -> {
                        mPrefHelper = new PreferenceHelper(getBaseContext());
                        String mLevelUser1 = mPrefHelper.getLevelUser();

                        Log.d("DEBUG", "onClick: " + mLevelUser1);
                        if (mLevelUser1.equals("0")) {
                            updateAdmin(mEmail, finalSetLev);
                        } else {
                            TSnackbar snackbar = TSnackbar.make(v, "Sorry, you're not allowed to change " +
                                    "head division, ask your supervisor please", TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.setAction("OK", v12 -> {

                            });
                            snackbar.setActionTextColor(Color.BLACK);
                            snackbar.show();
                        }
                    })

                    .setNeutralButton("Cancel", (dialog, id) -> dialog.cancel())

                    .setNegativeButton("Edit", (dialog, id) -> {

                        if (mLevelUser.equals("0") || mLevelUser.equals("1") && levelUser.equals("2") &&
                                mDivision.equals(result_divID)) {

                            EditText eDisplayName = new EditText(EmployeeProfile.this);
                            eDisplayName.setHint("Real Name");
                            eDisplayName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                            eDisplayName.setMaxLines(1);
                            eDisplayName.setText(real_name.getText());

                            EditText ePlaceBirth = new EditText(EmployeeProfile.this);
                            ePlaceBirth.setHint("Place Birth");
                            ePlaceBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                            ePlaceBirth.setMaxLines(1);
                            ePlaceBirth.setText(result_place_birth);

                            eDateBirth = new EditText(EmployeeProfile.this);
                            eDateBirth.setHint("Date Birth - double click here");
                            eDateBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
                            eDateBirth.setMaxLines(1);
                            eDateBirth.setText(result_date_birth);

                            eDateBirth.setOnClickListener(view -> datePickerDialog.show());

                            EditText eEmployeeAddress = new EditText(EmployeeProfile.this);
                            eEmployeeAddress.setHint("Employee Address");
                            eEmployeeAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                            eEmployeeAddress.setMaxLines(1);
                            String str = address.getText().toString();
                            String after = str.substring(str.indexOf(":") + ":".length());
                            eEmployeeAddress.setText(after.trim());


                            final EditText ePhone1 = new EditText(EmployeeProfile.this);
                            ePhone1.setHint("Phone Number 1");
                            ePhone1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                            ePhone1.setMaxLines(1);
                            ePhone1.setText(result_phone1);


                            final EditText ePhone2 = new EditText(EmployeeProfile.this);
                            ePhone2.setHint("Phone Number 2");
                            ePhone2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
                            ePhone2.setMaxLines(1);
                            ePhone2.setText(result_phone2);

                            final Spinner eDivision = new Spinner(EmployeeProfile.this);
                            String sDivision = mPrefHelper.getDivisionFull();

                            JSONObject jsonResponse = null;
                            ArrayList<DivisionAndID> aName = null;
                            try {
                                jsonResponse = new JSONObject(sDivision);
                                JSONArray cast1 = jsonResponse.getJSONArray("division");
                                aName = new ArrayList<>();
                                aName.clear();
                                for (int i1 = 0; i1 < cast1.length(); i1++) {
                                    JSONObject div_name = cast1.getJSONObject(i1);
                                    String name = div_name.getString("name");
                                    String cid = div_name.getString("cid");
                                    aName.add(new DivisionAndID(name, cid));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            assert aName != null;
                            ArrayAdapter<DivisionAndID> spinnerArrayAdapter = new ArrayAdapter<>(EmployeeProfile.this, R.layout.spinner_item_black, aName);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            eDivision.setAdapter(spinnerArrayAdapter);

                            for (int i1 = 0; i1 < eDivision.getCount(); i1++) {
                                if (eDivision.getAdapter().getItem(i1).toString().equals(result_division)) {
                                    eDivision.setSelection(i1);
                                    break;
                                }
                            }

                            final Spinner eEmployment = new Spinner(EmployeeProfile.this);
                            String sEmployment = mPrefHelper.getEmployment();

                            JSONObject jsonResponseEmployment = null;
                            ArrayList<DivisionAndID> aNameEmployment = null;
                            try {
                                jsonResponseEmployment = new JSONObject(sEmployment);
                                JSONArray cast2 = jsonResponseEmployment.getJSONArray("employment");
                                aNameEmployment = new ArrayList<>();
                                aNameEmployment.clear();
                                for (int i12 = 0; i12 < cast2.length(); i12++) {
                                    JSONObject div_name1 = cast2.getJSONObject(i12);
                                    String name = div_name1.getString("name");
                                    String cid = div_name1.getString("cid");
                                    aNameEmployment.add(new DivisionAndID(name, cid));
                                    //Log.d(TAG, "editData: "+name);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            assert aNameEmployment != null;
                            ArrayAdapter<DivisionAndID> spinnerArrayAdapter1 = new ArrayAdapter<>(EmployeeProfile.this, android.R.layout.simple_spinner_item, aNameEmployment);
                            spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            eEmployment.setAdapter(spinnerArrayAdapter1);

                            for (int i1 = 0; i1 < eEmployment.getCount(); i1++) {
                                if (eEmployment.getAdapter().getItem(i1).toString().equals(employment)) {
                                    eEmployment.setSelection(i1);
                                    break;
                                }
                            }

                            final Spinner eEmployee_status = new Spinner(EmployeeProfile.this);
                            String sEmployee_status = mPrefHelper.getEmployee_status();
                            JSONObject jsonResponseEmployeeStatus = null;
                            ArrayList<DivisionAndID> aNameEmployeeStatus = null;
                            try {
                                jsonResponseEmployeeStatus = new JSONObject(sEmployee_status);
                                JSONArray cast3 = jsonResponseEmployeeStatus.getJSONArray("employee_status");
                                aNameEmployeeStatus = new ArrayList<>();
                                aNameEmployeeStatus.clear();
                                for (int i12 = 0; i12 < cast3.length(); i12++) {
                                    JSONObject div_name2 = cast3.getJSONObject(i12);
                                    String name = div_name2.getString("name");
                                    String cid = div_name2.getString("cid");
                                    aNameEmployeeStatus.add(new DivisionAndID(name, cid));
                                    /// / Log.d(TAG, "editData: "+name);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            assert aNameEmployeeStatus != null;
                            ArrayAdapter<DivisionAndID> spinnerArrayAdapter2 = new ArrayAdapter<>(EmployeeProfile.this, android.R.layout.simple_spinner_item, aNameEmployeeStatus);
                            spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            eEmployee_status.setAdapter(spinnerArrayAdapter2);

                            for (int i1 = 0; i1 < eEmployee_status.getCount(); i1++) {
                                if (eEmployee_status.getAdapter().getItem(i1).toString().equals(employment_status)) {
                                    eEmployee_status.setSelection(i1);
                                    break;
                                }
                            }

                            LinearLayout layout = new LinearLayout(EmployeeProfile.this);
                            layout.setOrientation(LinearLayout.VERTICAL);
                            layout.addView(eDisplayName);
                            layout.addView(ePlaceBirth);
                            layout.addView(eDateBirth);
                            layout.addView(eEmployeeAddress);
                            layout.addView(ePhone1);
                            layout.addView(ePhone2);
                            layout.addView(eDivision);
                            layout.addView(eEmployment);
                            layout.addView(eEmployee_status);

                            ScrollView scrollView = new ScrollView(EmployeeProfile.this);
                            scrollView.addView(layout);


                            new AlertDialog.Builder(EmployeeProfile.this)
                                    .setTitle("Edit Employee")
                                    .setMessage("Type employee data..")
                                    .setView(scrollView)
                                    .setPositiveButton("SUBMIT", (dialog1, which) -> {
                                        String iDisplayName = eDisplayName.getText().toString();
                                        String iPlaceBirth = ePlaceBirth.getText().toString();
                                        String iDateBirth = eDateBirth.getText().toString();
                                        String iEmployeeAddress = eEmployeeAddress.getText().toString();
                                        String iPhone1 = ePhone1.getText().toString();
                                        String iPhone2 = ePhone2.getText().toString();
                                        String email1 = mEmail;

                                        eDivision.setOnItemSelectedListener(new SpinnerListener());
                                        int divID = Integer.parseInt(String.valueOf(((DivisionAndID) eDivision.getSelectedItem()).getId()));

                                        eEmployment.setOnItemSelectedListener(new SpinnerListener());
                                        int eEmploymentID = Integer.parseInt(String.valueOf(((DivisionAndID) eEmployment.getSelectedItem()).getId()));

                                        eEmployee_status.setOnItemSelectedListener(new SpinnerListener());
                                        int eEmployee_statusID = Integer.parseInt(String.valueOf(((DivisionAndID) eEmployee_status.getSelectedItem()).getId()));


                                        UpdateUsers(iDisplayName, iPlaceBirth, iDateBirth, iEmployeeAddress,
                                                iPhone1, iPhone2, email1, String.valueOf(divID),String.valueOf(eEmploymentID),
                                                String.valueOf(eEmployee_statusID));

                                        //Log.d(TAG, "onClick: "+iDivison+":"+iEmployment+":"+iEmployee_status);

                                    }).setNegativeButton("CANCEL", (dialog12, whichButton) -> {

                                    }).show();

                        } else {
                            TSnackbar snackbar = TSnackbar.make(v, "Sorry, you're not allowed to change " +
                                    "head division, ask your supervisor please", TSnackbar.LENGTH_LONG);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                            snackbar.setAction("OK", v1 -> {

                            });
                            snackbar.setActionTextColor(Color.BLACK);
                            snackbar.show();
                        }
                    })

                    .show();
        });

        Calendar now = Calendar.getInstance();
        int mYear = now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);
        setDatePicker(mYear, mMonth, mDay);

        getLastTenEmployeeLocation();

        empDeatil.setOnClickListener(v -> {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    EmployeeProfile.this);

            // set title
            alertDialogBuilder.setTitle("Ping");

            // set dialog message
            alertDialogBuilder
                    .setMessage("Ping "+real_name.getText()+" device" +"?\nWe will trying to reach "+real_name.getText()+" device" +
                            "\n\nYou will be notified if his/her device online")
                    .setCancelable(true)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
                        String fbID =  pref.getString("regId","");
                        sendNotification(firbaseID, mEmailCaller, mDivision, "Ping Request", "PING, "+fbID);

                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());

            // create alert dialog
            AlertDialog alertDialog = alertDialogBuilder.create();

            // show it
            alertDialog.show();

        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "onReceive: " + intent);
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notification

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String type = intent.getStringExtra("type");
                    String message = intent.getStringExtra("message");
                    String user = intent.getStringExtra("user");
                    //String time = intent.getStringExtra("timestamp");

                    if (type.equals("4")){
                        styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text(message +" | "+user)
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.BLUE)
                                .build();
                        styleableToast.show();
                    }



                }
            }
        };

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
    protected void onPostResume() {
        super.onPostResume();
        getEmployeeDetail();
        // getTagToday();
        getTagThisMonth();
    }

    private void getEmployeeDetail() {

        String url = Config.DOMAIN + "wamonitoring/get_employee_detail.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);

                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {

                    String result_level_user = jObject.getString("level_user");
                    result_place_birth = jObject.getString("place_birth");
                    result_date_birth = jObject.getString("date_birth");
                    String result_employee_address = jObject.getString("employee_address");
                    result_division = jObject.getString("division");
                    result_divID = jObject.getString("div_id");

                    String result_real_name = jObject.getString("real_name");
                    String result_lastseen = jObject.getString("last_update");
                    result_phone1 = jObject.getString("phone1");
                    result_phone2 = jObject.getString("phone2");
                    employment = jObject.getString("employment");
                    employment_status = jObject.getString("employment_status");

                    levelUser = jObject.getString("level_user");
                    String log_status = jObject.getString("log_status");
                    firbaseID = jObject.getString("firebase_id");
                    working_gps = jObject.getString("working_gps");


                    SimpleDateFormat df = new SimpleDateFormat("EE, yyyy-MM-dd HH:mm",Locale.ENGLISH);
                    long updatetime = Long.parseLong(result_lastseen);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(new Date(updatetime));
                    cal.getTimeInMillis();

                    Picasso.with(getBaseContext())
                            .load("https://olmatix.com/wamonitoring/foto_profile/" + mEmail + ".jpg")
                            .into(img);

                    real_name.setText(result_real_name);
                    place_date_birth.setText(result_place_birth + ", " + result_date_birth);
                    division.setText("Division : " + result_division + ", Status : " + employment_status + "\nEmployment : " + employment);
                    email.setText(mEmail);
                    address.setText("Address : " + result_employee_address);
                    phone.setText("Ph. " + result_phone1 + ", " + result_phone2);
                    lastseen.setText(Html.fromHtml("<u>Last seen at " + Utils.getTimeAgo(cal) + " | " + df.format(updatetime) + "</u>"));

                    if (result_level_user.equals("1")) {
                        level_user.setVisibility(View.VISIBLE);
                        level_user.setText(result_division + " Division Head");
                    } else {
                        level_user.setVisibility(View.GONE);
                    }

                    switch (log_status) {
                        case "1":
                            real_name.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online, 0, 0, 0);
                            break;
                        case "2":
                            real_name.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline, 0, 0, 0);

                            break;
                        default:
                            real_name.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_invisible, 0, 0, 0);

                            break;
                    }
                    real_name.setCompoundDrawablePadding(10);

                    FloatingActionButton phone1 = (FloatingActionButton) findViewById(R.id.phoneBut);

                    phone1.setOnClickListener(v -> new AlertDialog.Builder(EmployeeProfile.this)
                            .setTitle("Make a Call")
                            .setIcon(R.drawable.ic_info_black_24dp)
                            .setMessage("Do you want to make a call to " + result_real_name + "?" +
                                    "\nPhone1 " + result_phone1 + "" +
                                    "\nPhone2 " + result_phone2)
                            .setPositiveButton("Phone 1", (dialog, id) -> {
                                String phone_no = result_phone1.replaceAll("-", "");
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setPackage("com.android.server.telecom");
                                callIntent.setData(Uri.parse("tel:" + phone_no));
                                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (ActivityCompat.checkSelfPermission(EmployeeProfile.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                startActivity(callIntent);

                                dialog.dismiss();

                            })

                            .setNegativeButton("Phone 2", (dialog, id) -> {
                                String phone_no = result_phone2.replaceAll("-", "");
                                Intent callIntent = new Intent(Intent.ACTION_CALL);
                                callIntent.setPackage("com.android.server.telecom");
                                callIntent.setData(Uri.parse("tel:" + phone_no));
                                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (ActivityCompat.checkSelfPermission(EmployeeProfile.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    // TODO: Consider calling
                                    //    ActivityCompat#requestPermissions
                                    // here to request the missing permissions, and then overriding
                                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                    //                                          int[] grantResults)
                                    // to handle the case where the user grants the permission. See the documentation
                                    // for ActivityCompat#requestPermissions for more details.
                                    return;
                                }
                                startActivity(callIntent);
                                dialog.dismiss();
                            })

                            .show());
                    /*String locSplit[] = working_gps.split(",");
                    double locLat = Double.parseDouble(locSplit[0]);
                    double locLot = Double.parseDouble(locSplit[1]);*/
                    onMapReady(mMap);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mUserName);
                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    private void getTagThisMonth() {

        //String tag_json_obj = TAG;

        String url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month_byEmail.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                //JSONObject jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");
                String result;
                if (result_code.equals("1")) {

                    result = result_message;
                    tagsThisMonth.setText(result);
                } else {
                    result = "0";
                    tagsThisMonth.setText(result);

                }

                getTagToday(result);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("email", mUserName);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void getTagToday(String tagMonth) {

        //String tag_json_obj = TAG;
        String url = Config.DOMAIN + "wamonitoring/get_tags_by_today_byEmail.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                //JSONObject jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    tagsThisMonth.setText("Tags " + result_message + " today, " + tagMonth + " by this month");

                } else {
                    tagsThisMonth.setText("Tags 0" + " today, " + tagMonth + " by this month");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("email", mUserName);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void getData(String page) {

        //String mEmail = sharedPref.getString("email", "olmatix1");

        //String tag_json_obj = "Tags-getData";

        String url = Config.DOMAIN + "wamonitoring/get_tags_record_byemail.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
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

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc, division_name,"", tgl, jam,
                                tid, checklist_done, trid, email, after_foto, before_foto,latitude,longitude);
                        tagList.add(tags);
                        //Log.d(TAG, "setData: "+employee_name);
                    }

                    mAdapter.notifyDataSetChanged();

                }
            } catch (JSONException e) {
                e.printStackTrace();

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mUserName);
                MyData.put("offsett", page);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void updateAdmin(String email, String setLev) {
        String url = Config.DOMAIN + "wamonitoring/update_user_to_admin.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    getEmployeeDetail();
                }
                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text(result_message)
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", email);
                MyData.put("level_user", setLev);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    private void UpdateUsers(String DisplayName, String PlaceBirth, String DateBirth, String EmployeeAddress,
                             String Phone1, String Phone2, String email, String Division, String employment,
                             String empl_status) {
        String mRealName = sharedPref.getString("real_name", "olmatix1");

        //String tag_json_obj = TAG;
        String url = Config.DOMAIN + "wamonitoring/update_employee.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                //Log.d("DEBUG", "doInBackground: " + response);
                if (result_code.equals("1")) {

                    getEmployeeDetail();
                }

                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text(result_message)
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.RED)
                        .build();
                styleableToast.show();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("real_name", mRealName);
                MyData.put("email", email);
                MyData.put("real_name", DisplayName);
                MyData.put("place_birth", PlaceBirth);
                MyData.put("date_birth", DateBirth);
                MyData.put("employee_address", EmployeeAddress);
                MyData.put("phone1", Phone1);
                MyData.put("phone2", Phone2);
                MyData.put("division", Division);
                MyData.put("employment", employment);
                MyData.put("employment_status", empl_status);
                MyData.put("company_address", sharedPref.getString("company_address", "jakarta"));

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(EmployeeProfile.this,
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

                    String date = year1 + "-" + mMonth + "-" + daymonth;
                    eDateBirth.setText(date);
                    //(date);
                }, year, month, dayOfMonth);
    }

    private void getLastTenEmployeeLocation() {
        String url = "https://olmatix.com/wamonitoring/get_employee_location_by_employee.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    tagList1.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        double latitude = Double.parseDouble(tags_name.getString("latitude"));
                        double longitude = Double.parseDouble(tags_name.getString("longitude"));
                        String timestamps = tags_name.getString("timestamps");
                        String EmployeeName = tags_name.getString("employee_name");

                        final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
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

                        locEmployeeLastTenModel tags = new locEmployeeLastTenModel(latitude, longitude, adString, timestamps, EmployeeName);
                        tagList1.add(tags);

                    }
                    mRecycleViewLoc.setAdapter(mAdapterLoc);
                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    private void sendNotification(final String reg_token, final String username, final String title, final String message,
                                  final String image) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss",Locale.ENGLISH);
                    String times = timeformat.format(System.currentTimeMillis());

                    JSONObject root = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject payload = new JSONObject();

                    payload.put("title", username);
                    payload.put("is_background", false);
                    payload.put("message", message + " | " + title);
                    payload.put("image", image);
                    payload.put("timestamp", times);

                    data.put("data", payload);
                    root.put("data", data);
                    root.put("to", reg_token);

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

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getBaseContext()).unregisterReceiver(mRegistrationBroadcastReceiver);

    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getBaseContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

       // if (working_gps = "0.0,0.0");

        String locSplit[] = working_gps.split(",");
        double locLat = Double.parseDouble(String.valueOf(locSplit[0]));
        double locLot = Double.parseDouble(String.valueOf(locSplit[1]));
        Geocoder geocoder = new Geocoder(getBaseContext());
        List<Address> addresses = null;
        try {
            // Find a maximum of 3 locations with the name Kyoto
            addresses = geocoder.getFromLocation(locLat, locLot, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (addresses != null) {
            for (Address loc : addresses) {
                MarkerOptions opts = new MarkerOptions()
                        .position(new LatLng(locLat, locLot))
                        .title(loc.getAddressLine(0));
                mMap.addMarker(opts);

                LatLng position = opts.getPosition();

                mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15));
            }
        }

    }
}