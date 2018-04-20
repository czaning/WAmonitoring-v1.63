package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.EmployeeAndEmail;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class TrackEmployeeActivity extends FragmentActivity implements OnMapReadyCallback {
    CardView cv2;
    private GoogleMap mMap;
    SharedPreferences sharedPref;
    private ProgressDialog pDialog;
    private static final String TAG = "TrackEmployeeActivity";
    Spinner employee;
    Context context;
    PreferenceHelper mPrefHelper;
    String mLevelUser;
    int refresh = 0, firtsopen =0;
    String mEmail;

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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_maps_employee);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET)
                .check();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        cv2 = (CardView) findViewById(R.id.cv2);
        employee = (Spinner) findViewById(R.id.spinnerEmployee);
        ImageButton iconLoc = (ImageButton) findViewById(R.id.locButton);
        context = getApplicationContext();
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        mLevelUser = mPrefHelper.getLevelUser();

        Log.d(TAG, "onCreate: "+mLevelUser);
        getData();
        cv2.setOnClickListener(view -> {

        });

        employee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (firtsopen!=0) {

                    EmployeeAndEmail swt = (EmployeeAndEmail) parent.getItemAtPosition(position);
                    mEmail = (String) swt.email;
                    Log.d(TAG, "onItemSelected: " + mEmail);
                    if (!mEmail.equals("")||!mEmail.isEmpty()) {
                        loadingdialog(0);

                        refresh = 1;
                        mMap.clear();
                        onMapReady(mMap);
                    }
                }
                firtsopen = 1;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        iconLoc.setOnClickListener(v -> {
            loadingdialog(0);

            refresh = 0;
            mMap.clear();
            onMapReady(mMap);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh = 0;
        firtsopen = 0;
        onMapReady(mMap);
    }

    private void loadingdialog (int what){

        if (what == 0) {

            pDialog = new ProgressDialog(TrackEmployeeActivity.this);
            pDialog.setMessage("Loading employee location. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();

        } else {
            if (pDialog!=null) {
                pDialog.cancel();
            }
        }
    }

    private void getData() {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mCompanyID;
        String mDivision = sharedPref.getString("division", "olmatix1");

        String url;
        if (mLevelUser.equals("0")) {
            mCompanyID= sharedPref.getString("company_id", "olmatix1");
            url = "https://olmatix.com/wamonitoring/get_employee.php";
        } else if(mLevelUser.equals("4")){
            // kalau level user 4 kirim group id nya
            mCompanyID = mPrefHelper.getGroup();
            url = "https://olmatix.com/wamonitoring/get_employee_by_group_spinner.php";
        } else {
            mCompanyID = sharedPref.getString("company_id", "olmatix1");
            url = "https://olmatix.com/wamonitoring/get_employee_by_division_spinner.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {

                    List<EmployeeAndEmail> aName = new ArrayList<>();
                    aName.clear();
                    JSONArray cast = jsonResponse.getJSONArray("employee");

                    aName.add(new EmployeeAndEmail( "Click me to filter", ""));

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String real_name = tags_name.getString("real_name");
                        String email = tags_name.getString("email");

                        aName.add(new EmployeeAndEmail( real_name, email));

                        Log.d(TAG, "setDataSpinner: "+real_name);
                    }

                    ArrayAdapter<EmployeeAndEmail> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aName);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    employee.setAdapter(spinnerArrayAdapter);


                }

                loadingdialog(1);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        String url = null;
        mMap = googleMap;
        Log.d(TAG, "onMapReady: "+refresh);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mCompanyID;
        if(mLevelUser.equals("4")){
            mCompanyID = mPrefHelper.getGroup();
        }else{
            mCompanyID = sharedPref.getString("company_id", "olmatix1");
        }
        String mDivision = sharedPref.getString("division", "olmatix1");

        Log.d(TAG, "onMapReady: "+refresh);
        
        //String tag_json_obj = "login";

        if (refresh==0) {
            if (mLevelUser.equals("0")) {
                url = Config.DOMAIN+"wamonitoring/get_employee_location_last_all.php";
            } else if (mLevelUser.equals("4")) {
                url = Config.DOMAIN+"wamonitoring/get_employee_location_last_all_byGroup.php";
            } else{
                url = Config.DOMAIN+"wamonitoring/get_employee_location_last_all_byDivision.php";
            }
        } else  if (refresh==1) {
            url = "https://olmatix.com/wamonitoring/get_employee_location_by_employee.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String email = tags_name.getString("email");
                        String employee_name = tags_name.getString("employee_name");
                        double latitude = Double.parseDouble(tags_name.getString("latitude"));
                        double longitude = Double.parseDouble(tags_name.getString("longitude"));
                        String timestamps = tags_name.getString("timestamps");

                        final Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                        String adString = null, loc = null;
                        try {
                            List<Address> list;
                            list = geocoder.getFromLocation(latitude, longitude, 1);
                            if (list != null && list.size() > 0) {
                                Address address = list.get(0);
                                loc = address.getLocality();

                                if (address.getAddressLine(0) != null)
                                    adString = address.getAddressLine(0);

                            }


                        } catch (final IOException e) {
                            new Thread(() -> Log.e("DEBUG", "Geocoder ERROR", e)).start();
                        }

                        Date date = null;
                        try {
                            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(timestamps);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        long milliseconds = date.getTime();
                        long updatetime = Long.parseLong(String.valueOf(milliseconds));

                        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);
                        String tgl = simpleDateFormat2.format(updatetime);

                        LatLng locPref = new LatLng(latitude, longitude);
                        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                                .inflate(R.layout.custom_marker_layout, null);

                        TextView numTxt = (TextView) marker.findViewById(R.id.num_txt);
                        CircleImageView foto = (CircleImageView) marker.findViewById(R.id.img_profile);

                        if (loc==null)loc="";

                        if (refresh==1) {
                            numTxt.setText(tgl+"\n"+loc);
                        } else  if (refresh==0){
                            numTxt.setText(employee_name+"\n"+loc);

                        }

                        if (i==0){
                            numTxt.setBackgroundColor(Color.BLACK);
                            numTxt.setPadding(10,2,10,2);
                        }

                        Picasso.with(getBaseContext())
                                .load("https://olmatix.com/wamonitoring/foto_profile/"+email+".jpg")
                                .placeholder(R.mipmap.logo)
                                .into(foto);

                        Marker customMarker = mMap.addMarker(new MarkerOptions()
                                .position(locPref)
                                .title(employee_name)
                                .snippet(email + "\n" + loc + ", " + adString + "\n" + tgl)
                                .icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker))));


                        mMap.moveCamera(CameraUpdateFactory.newLatLng(locPref));
                        mMap.animateCamera(CameraUpdateFactory.zoomTo(10));

                        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                            @Override
                            public View getInfoWindow(Marker arg0) {
                                return null;
                            }

                            @Override
                            public View getInfoContents(Marker marker) {

                                Context context = getApplicationContext(); //or getActivity(), YourActivity.this, etc.

                                LinearLayout info = new LinearLayout(context);
                                info.setOrientation(LinearLayout.VERTICAL);

                                TextView title = new TextView(context);
                                title.setTextColor(Color.BLACK);
                                title.setGravity(Gravity.LEFT);
                                title.setTypeface(null, Typeface.BOLD);
                                title.setText(marker.getTitle());

                                TextView snippet = new TextView(context);
                                snippet.setTextColor(Color.GRAY);
                                snippet.setText(marker.getSnippet());

                                info.addView(title);
                                info.addView(snippet);

                                return info;
                            }
                        });
                    }

                    loadingdialog(1);


                } else {
                    loadingdialog(1);
                }

                firtsopen = 1;
                loadingdialog(1);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                if (refresh==0) {
                    MyData.put("company_id", mCompanyID);
                    MyData.put("division", mDivision);
                } else if (refresh==1) {
                    MyData.put("email", mEmail);
                }

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);


    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
