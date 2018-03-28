package com.lesjaw.wamonitoring.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.service.wamonitorservice;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {
    SharedPreferences sharedPref;
    boolean starting = false;
    private static final String TAG = "SplashActivity";
    CoordinatorLayout coordinatorLayout;
    private PreferenceHelper mPrefHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        // ngambil preferences username user aplikasi
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        String mUserName = sharedPref.getString("real_name", "John Smith");

        // kalau username nya masih John Smith brarti belum login dan bakal diarahin ke login activity
        if (mUserName.equals("John Smith")){
            Intent i = new Intent(getApplication(), LoginActivity.class);
            startActivity(i);
            finish();
        // kalau udah login dia bakal ngelakuin otentikasi
        } else {
            sendJsonLogin();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        starting = true;
    }

    // otentikasi user via API
    private void sendJsonLogin(){

        String mEmail = sharedPref.getString("email", "olmatix1");
        String mPassword = sharedPref.getString("password", "olmatix");

        //String tag_json_obj = "login";

        String url = Config.DOMAIN+"wamonitoring/loginV1.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, this::parsingJson, error -> {
            //Log.d(TAG, "onErrorResponse: " + error);
            StyleableToast styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Network error, it seems no internet connection available, exiting now..")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();

            new Handler(Looper.getMainLooper()).postDelayed(this::finish, 5000);

        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);
                MyData.put("password", mPassword);
                return MyData;
            }
        };

        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
    }

    // parsing response dari API
    public void parsingJson(String json) {
        try {

            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");

            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

            // ini case kalau dia sukses, api bakal kirim response succes yg nilainya 1
            if (result_code.equals("1")) {

                // buat variable sementara buat nampung data response dari API
                String result_level_user = jObject.getString("level_user");
                String result_place_birth = jObject.getString("place_birth");
                String result_date_birth = jObject.getString("date_birth");
                String result_employee_address = jObject.getString("employee_address");
                String result_company_address = jObject.getString("company_address");
                // division disini adalah divisi id yang ada di table user
                String result_division = jObject.getString("division");
                String result_company_name = jObject.getString("company_name");
                String result_real_name = jObject.getString("real_name");
                String result_package = jObject.getString("package");
                String result_company_id = jObject.getString("company_id");
                String result_date_created = jObject.getString("date_created");
                String result_phone1 = jObject.getString("phone1");
                String result_phone2 = jObject.getString("phone2");
                String log_status = jObject.getString("log_status");
                String server_time = jObject.getString("server_time");
                String result_group = jObject.getString("group");

                //Log.d(TAG, "parsingJson: "+server_time);
                // masukin data dari response API ke shared preferences via sharedPreference helper
                final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());

                mPrefHelper.setLevelUser(result_level_user);
                mPrefHelper.setDateCreated(Long.parseLong(result_date_created));
                mPrefHelper.setPackage(result_package);
                mPrefHelper.setLogin(log_status);
                mPrefHelper.setServerTime(Long.parseLong(server_time));
                mPrefHelper.setGroup(result_group);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("real_name",result_real_name);
                editor.putString("birth",result_place_birth+", "+result_date_birth);
                editor.putString("company_name",result_company_name);
                editor.putString("company_id",result_company_id);
                editor.putString("company_address",result_company_address);
                editor.putString("employee_address",result_employee_address);
                editor.putString("division",result_division);
                editor.putString("phone1",result_phone1);
                editor.putString("phone2",result_phone2);

                // commit perubahan shared preferences
                editor.apply();
                // ambil nama divisi dari user dengan mengirimkan divisi id dari shared preferences
                getDivName();

                Intent i = new Intent(SplashActivity.this, wamonitorservice.class);
                startService(i);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    switch (result_level_user) {
                        case "0":
                            new GetDivision().execute();

                            if (TextUtils.isEmpty(result_package) || result_package.equals("0")) {
                                Intent i1 = new Intent(SplashActivity.this, PackageActivity.class);
                                startActivity(i1);
                                finish();
                            } else {
                                final PreferenceHelper mPrefHelper1 = new PreferenceHelper(getApplicationContext());
                                mPrefHelper1.setPackage(result_package);
                                Intent i1 = new Intent(SplashActivity.this, MainActivity.class);
                                startActivity(i1);
                                finish();
                            }

                            break;
                        case "1": {
                            Intent i1 = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i1);
                            finish();
                            break;
                        }
                        case "2": {
                            Intent i1 = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i1);
                            finish();
                            break;
                        }

                        case "4": {
                            Intent i1 = new Intent(SplashActivity.this, MainActivity.class);
                            startActivity(i1);
                            finish();
                            break;
                        }
                    }

                }, 0);
            // ini case kalau dia gagal otentikasi (ga ada data user di database), api bakal kirim response succes yg nilainya 0 dan nyuruh dia buat login
            } else {
                Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(i);
                finish();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // mengambil nama divisi dari API
    private void getDivName(){

        String url = Config.DOMAIN+"wamonitoring/get_divName.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;

            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                // kalau sukses (ada data dengan div id yg sama di database) API bakal ngasih response 1
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("employee");
                    for (int i = 0; i < cast.length(); i++) {
                        JSONObject tags_name = cast.getJSONObject(i);
                        // ambil nama divisi dari API reponse yang diambil dari table divisi di database
                        String divison_name = tags_name.getString("divison_name");
                        // menyimpan nama divisi di shared preferences
                        mPrefHelper.setDivName(divison_name);
                        //Log.d(TAG, "getDivName: "+divison_name);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();

            }        }, error -> Log.d(TAG, "onErrorResponse: GetDivName" + error)) {
            // request body berupa divisi dari shared pref
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("divid", sharedPref.getString("division", "olmatix1"));
                return MyData;
            }
        };

        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);


    }


    private class GetDivision extends AsyncTask<Void, Void, Void> {

        List<HashMap<String, String>> allNames;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_division.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                allNames = new ArrayList<>();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    mPrefHelper.setDivisionFull(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    if (result_code.equals("1")) {

                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"currently you have no division name",TSnackbar.LENGTH_SHORT);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.show();
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


    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    protected void onPause() {
        super.onPause();

    }
}
