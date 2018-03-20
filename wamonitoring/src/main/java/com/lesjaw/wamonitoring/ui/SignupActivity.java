package com.lesjaw.wamonitoring.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lesjaw@gmail.com on 01/09/2017.
 */

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";
    Button _signupButton, _signupButtonUser;
    EditText _nameText,_emailText,_passwordText, _userText, _companyText;
    TextView _loginLink;
    String real_name, email, password, company_name, company_id;
    int level_user;
    ProgressDialog progressDialog;
    CoordinatorLayout coordinatorLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        _nameText = (EditText)findViewById(R.id.input_name);
        _emailText = (EditText)findViewById(R.id.input_email);
        _passwordText = (EditText)findViewById(R.id.input_password);
        _companyText = (EditText)findViewById(R.id.input_company);
        _signupButton = (Button)findViewById(R.id.btn_signup);
        _signupButtonUser = (Button)findViewById(R.id.btn_signupUser);
        _loginLink = (TextView) findViewById(R.id.link_login);

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);


        _signupButton.setOnClickListener(v -> signup());

        _signupButtonUser.setOnClickListener(v -> {
            Intent i = new Intent(getBaseContext(), ScalingScannerActivity.class);
            startActivity(i);
        });


        _loginLink.setOnClickListener(v -> {
            // Finish the registration screen and return to the Login activity
            finish();
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if (!validate()) {
            onSignupFailed();
            return;
        }

        _signupButton.setEnabled(false);

        real_name = _nameText.getText().toString();
        email = _emailText.getText().toString();
        password = _passwordText.getText().toString();
        company_name = _companyText.getText().toString();
        company_id = UUID.randomUUID().toString();
        level_user = 0;
        progressDialogShow(0);
        onSignupSuccess();

    }



    public void onSignupSuccess() {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);
        sendJsonSignUp();
    }

    public void onSignupFailed() {
        //Toast.makeText(getBaseContext(), "SIgn failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();
        String company_name = _companyText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 12) {
            _passwordText.setError("between 4 and 12 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        if (company_name.isEmpty() || company_name.length() < 4 || company_name.length() > 100) {
            _companyText.setError("between 4 and 100 alphanumeric characters");
            valid = false;
        } else {
            _companyText.setError(null);
        }

        return valid;
    }

    private void sendJsonSignUp(){
        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();
        company_id = UUID.randomUUID().toString();

        String url = "https://olmatix.com/wamonitoring/create_users.php";
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "onResponse: "+response);
            parsingJson(response);
        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("real_name", real_name); //Add the data you'd like to send to the server.
                MyData.put("email", email);
                MyData.put("password", password);
                MyData.put("company_name", company_name);
                MyData.put("company_id", String.valueOf(company_id));
                MyData.put("level_user", String.valueOf(level_user));
                MyData.put("date_created", String.valueOf(now.getTimeInMillis()));
                MyData.put("last_update", String.valueOf(now.getTimeInMillis()));

                return MyData;
            }
        };

        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);



    }

    public void parsingJson(String json) {
        try {
            JSONObject jObject = new JSONObject(json);
            String msg = jObject.optString("error_msg");
            Log.d(TAG, "parsingJson: "+msg);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");

            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_json,TSnackbar.LENGTH_INDEFINITE);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.setAction("OK", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
            snackbar.show();
            progressDialogShow(1);

            if (result_code.equals(1)||result_code.equals("1")) {
                _signupButton.setEnabled(false);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void progressDialogShow (int what){
        if (what==0) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(true);
            progressDialog.setMessage("Registering your ID, please wait..");
            progressDialog.show();

        } else {
            if (progressDialog!=null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.d("DEBUG", "progressDialogStop: ");
            }
        }
    }
}