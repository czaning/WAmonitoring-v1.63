package com.lesjaw.wamonitoring.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PackageActivity extends AppCompatActivity {
    private static final String TAG = "PackageActivity";
    SharedPreferences sharedPref;
    int pack_user;
    CoordinatorLayout coordinatorLayout;
    String package_name;
    private ProgressDialog pDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        ImageButton trial = (ImageButton) findViewById(R.id.trial);
        ImageButton beginner = (ImageButton) findViewById(R.id.beginner);
        ImageButton advanced = (ImageButton) findViewById(R.id.advanced);
        ImageButton expert = (ImageButton) findViewById(R.id.expert);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        trial.setOnClickListener(view -> {
            sendJsonPackage(1);
            pack_user =1;
            package_name ="trial";

            pDialog = new ProgressDialog(PackageActivity.this);
            pDialog.setMessage("Loading Division names. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        });

        beginner.setOnClickListener(view -> {

            StyleableToast styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Contact us, for now please choose trial")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();
           /* sendJsonPackage(2);
            pack_user =2;
            package_name ="beginner";*/

        });

        advanced.setOnClickListener(view -> {

            StyleableToast styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Contact us, for now please choose trial")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();
        /*    sendJsonPackage(3);
            pack_user =3;
            package_name ="advanced";*/

        });

        expert.setOnClickListener(view -> {

            StyleableToast styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Contact us, for now please choose trial")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();

        /*    sendJsonPackage(4);
            pack_user =4;
            package_name ="expert";*/

        });
    }

    private void sendJsonPackage(int pack){

        String mUserName = sharedPref.getString("email", "olmatix1");

        //String tag_json_obj = "UpdatePackage";

        String url = Config.DOMAIN+"wamonitoring/update_package.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, this::parsingJson, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mUserName);
                MyData.put("package", String.valueOf(pack));
                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);


    }

    public void parsingJson(String json) {
        try {
            Log.d(TAG, "result_json before: "+json);
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

            pDialog.cancel();

            if (result_code.equals("1")) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"You are choosing "+" "+package_name,TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> finish());
                snackbar.show();

                final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
                mPrefHelper.setPackage(String.valueOf(pack_user));
                String result_level_user = mPrefHelper.getLevelUser();

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    if (result_level_user.equals("0")){
                        Intent i = new Intent(getApplication(), MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Intent i = new Intent(getApplication(), MainActivity.class);
                        startActivity(i);
                        finish();
                    }

                }, 3000);
            } else {
                Intent i = new Intent(getApplication(), LoginActivity.class);
                startActivity(i);
                finish();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            //drawer.openDrawer(GravityCompat.START);

            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
