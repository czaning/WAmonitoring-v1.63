package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
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
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private CardView cv2;
    private TextView textView2;
    private GoogleMap mMap;
    double onMoveLat, onMoveLot;
    private StyleableToast styleableToast;
    private String id_fragement;
    EditText searchEdit;
    ImageButton searchBut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        cv2 = (CardView) findViewById(R.id.cv2);
        textView2 = (TextView) findViewById(R.id.textView2);
        searchEdit = (EditText) findViewById(R.id.searchedit);
        searchBut = (ImageButton) findViewById(R.id.search);

        Intent i = getIntent();
        id_fragement = i.getStringExtra("id_caller");
        String mEmailuser = i.getStringExtra("email");

        cv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
                if (onMoveLat != 0.0 || onMoveLot != 0.0) {
                    mPrefHelper.setTagLatitude(onMoveLat);
                    mPrefHelper.setTagLongitude(onMoveLot);

                    if (id_fragement.equals("SetWorkingGPS")) {
                        setWorkingGPS(mEmailuser, onMoveLat, onMoveLot);
                    } else {
                        finish();
                    }
                }
            }
        });

        searchBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                List<Address> addresses = null;
                try {
                    // Find a maximum of 3 locations with the name Kyoto
                    addresses = geocoder.getFromLocationName(searchEdit.getText().toString(), 3);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (addresses != null) {
                    for (Address loc : addresses) {
                        MarkerOptions opts = new MarkerOptions()
                                .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                .title(loc.getAddressLine(0));
                        mMap.addMarker(opts);

                        LatLng position = opts.getPosition();

                        textView2.setText("Lat " + position.latitude + ", " + "Long " + position.longitude);
                        onMoveLat = position.latitude;
                        onMoveLot = position.longitude;

                    }
                }
            }
        });

        searchEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    mMap.clear();

                    Geocoder geocoder = new Geocoder(MapsActivity.this);
                    List<Address> addresses = null;
                    try {
                        // Find a maximum of 3 locations with the name Kyoto
                        addresses = geocoder.getFromLocationName(searchEdit.getText().toString(), 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (addresses != null) {
                        for (Address loc : addresses) {
                            MarkerOptions opts = new MarkerOptions()
                                    .position(new LatLng(loc.getLatitude(), loc.getLongitude()))
                                    .title(loc.getAddressLine(0));
                            mMap.addMarker(opts);

                            LatLng position = opts.getPosition();

                            mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
                            mMap.animateCamera(CameraUpdateFactory.zoomTo(20));

                            textView2.setText(position.latitude + ", " + position.longitude);
                            onMoveLat = position.latitude;
                            onMoveLot = position.longitude;

                        }
                    }

                    return true;
                }
                return false;
            }
        });

    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());

        double lat = mPrefHelper.getPhoneLatitude();
        double lot = mPrefHelper.getPhoneLongitude();

        LatLng locPref = new LatLng(lat, lot);

        textView2.setText("Lat " +lat + ", " + "Long " + lot);

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.addMarker(new MarkerOptions()
                .position(new LatLng(lat, lot))
                .title("This is your current location")
                .draggable(true)
                .snippet("Drag me to new location")
                .icon(BitmapDescriptorFactory
                        .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(locPref));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(25));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {

            @Override
            public void onMarkerDragStart(Marker marker) {
                // TODO Auto-generated method stub
                // Here your code

                styleableToast = new StyleableToast
                        .Builder(getBaseContext())
                        .icon(R.drawable.ic_action_info)
                        .text("Dragging Start")
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                // TODO Auto-generated method stub
                LatLng position = marker.getPosition(); //

                textView2.setText(position.latitude + ", " + position.longitude);
                onMoveLat = position.latitude;
                onMoveLot = position.longitude;
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                // TODO Auto-generated method stub
                // Toast.makeText(MainActivity.this, "Dragging",
                // Toast.LENGTH_SHORT).show();
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                // Creating a marker
                MarkerOptions markerOptions = new MarkerOptions();

                // Setting the position for the marker
                markerOptions.position(latLng);

                // Setting the title for the marker.
                // This will be displayed on taping the marker
                markerOptions.title(latLng.latitude + " : " + latLng.longitude);

                // Clears the previously touched position
                mMap.clear();

                // Animating to the touched position
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Placing a marker on the touched position
                mMap.addMarker(markerOptions);

                LatLng position = markerOptions.getPosition(); //

                textView2.setText(position.latitude + ", " + position.longitude);

            }
        });



    }

    private void setWorkingGPS (String email, double lat, double lot){
        String url = Config.DOMAIN + "wamonitoring/update_working_gps.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals(1) || result_code.equals("1")) {

                    styleableToast = new StyleableToast
                            .Builder(getBaseContext())
                            .icon(R.drawable.ic_action_info)
                            .text(String.valueOf(result_message))
                            .textColor(Color.WHITE)
                            .backgroundColor(Color.BLUE)
                            .build();
                    styleableToast.show();

                    finish();

                }


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("email", email);
                MyData.put("working_gps", lat+","+lot);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
    }


}
