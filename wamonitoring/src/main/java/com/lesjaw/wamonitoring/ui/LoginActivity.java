package com.lesjaw.wamonitoring.ui;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.service.wamonitorservice;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;
    private Button _loginButton;
    private EditText _emailText,_passwordText;
    private ProgressDialog progressDialog;
    private CoordinatorLayout coordinatorLayout;
    private String email, password ;

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            Toast.makeText(LoginActivity.this, "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(LoginActivity.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

       new TedPermission(this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.CAMERA)
                .check();

        _loginButton = (Button) findViewById(R.id.btn_login);
        TextView _signupLink = (TextView) findViewById(R.id.link_signup);
        _emailText = (EditText)findViewById(R.id.input_email);
        _passwordText = (EditText)findViewById(R.id.input_password);

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);

        _loginButton.setOnClickListener(v -> login());

        _signupLink.setOnClickListener(v -> {
            // Start the Signup activity
            Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
            startActivityForResult(intent, REQUEST_SIGNUP);
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        _loginButton.setEnabled(false);

        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();

        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();

        // TODO: Implement your own authentication logic here.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("email",email);
        editor.putString("password",password);
        editor.apply();

        String mUserName = sharedPref.getString("email", "olmatix1");
        String mPassword = sharedPref.getString("password", "olmatix");

        Log.d(TAG, "login: "+mUserName +" : "+mPassword);
        sendJsonLogin();

    }

    private void sendJsonLogin(){

        //String tag_json_obj = "login";

        String url = Config.DOMAIN+"wamonitoring/loginV1.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, this::parsingJson,
                error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", email);
                MyData.put("password", password);
                return MyData;
            }
        };

        // Adding request to request queue
        //AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);


    }

    public void parsingJson(String json) {
        try {

            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            String result_user = jObject.getString("email");

            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);


            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_user+" "+result_json,TSnackbar.LENGTH_SHORT);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.show();


            if (result_code.equals("1")) {
                String result_real_name = jObject.getString("real_name");
                String result_place_birth = jObject.getString("place_birth");
                String result_date_birth = jObject.getString("date_birth");
                String result_company_name = jObject.getString("company_name");
                String result_company_id = jObject.getString("company_id");
                String result_company_address = jObject.getString("company_address");
                String result_employee_address = jObject.getString("employee_address");
                String result_package = jObject.getString("package");
                String result_level_user = jObject.getString("level_user");
                String result_division = jObject.getString("division");
                String result_date_created = jObject.getString("date_created");
                String result_phone1 = jObject.getString("phone1");
                String result_phone2 = jObject.getString("phone2");
                String result_group = jObject.getString("group");

                final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
                mPrefHelper.setLevelUser(result_level_user);
                mPrefHelper.setDateCreated(Long.parseLong(result_date_created));
                mPrefHelper.setGroup(result_group);

                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("real_name",result_real_name);
                editor.putString("birth",result_place_birth+", "+result_date_birth);
                editor.putString("company_address",result_company_address);
                editor.putString("employee_address",result_employee_address);
                editor.putString("company_name",result_company_name);
                editor.putString("company_id",result_company_id);
                editor.putString("division",result_division);
                editor.putString("phone1",result_phone1);
                editor.putString("phone2",result_phone2);
                editor.apply();

                Intent i = new Intent(this, wamonitorservice.class);
                startService(i);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    switch (result_level_user) {
                        case "0":
                            if (TextUtils.isEmpty(result_package) || result_package.equals("0")) {
                                Intent i1 = new Intent(getApplication(), PackageActivity.class);
                                startActivity(i1);
                                progressDialog.hide();
                                finish();
                            } else {
                                final PreferenceHelper mPrefHelper1 = new PreferenceHelper(getApplicationContext());
                                mPrefHelper1.setPackage(result_package);
                                Intent i1 = new Intent(getApplication(), MainActivity.class);
                                startActivity(i1);
                                progressDialog.hide();
                                finish();
                            }

                            break;
                        case "1": {
                            Intent i1 = new Intent(getApplication(), MainActivity.class);
                            startActivity(i1);
                            progressDialog.hide();
                            finish();
                            break;
                        }
                        case "2": {
                            Intent i1 = new Intent(getApplication(), MainActivity.class);
                            startActivity(i1);
                            progressDialog.hide();
                            finish();
                            break;
                        }

                        case "4": {
                            Intent i1 = new Intent(getApplication(), MainActivity.class);
                            startActivity(i1);
                            progressDialog.hide();
                            finish();
                            break;
                        }
                    }
                }, 3000);
            } else {
                progressDialog.hide();
                _loginButton.setEnabled(true);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginFailed() {
        StyleableToast styleableToast = new StyleableToast
                .Builder(getBaseContext())
                .icon(R.drawable.ic_action_info)
                .text("Wait.. or try again!")
                .textColor(Color.WHITE)
                .backgroundColor(Color.BLUE)
                .build();
        styleableToast.show();
        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 8 || password.length() > 10) {
            _passwordText.setError("between 8 and 10 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}