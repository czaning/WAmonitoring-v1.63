package com.lesjaw.wamonitoring.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
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
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBActivity extends AppCompatActivity {
    private SharedPreferences sharedPref;
    private static final String TAG = "DBActivity";
    private CoordinatorLayout coordinatorLayout;
    private PreferenceHelper mPrefHelper;
    private DatePickerDialog datePickerDialog;
    private EditText eDisplayName, eDateBirth, ePlaceBirth, eEmail, ePassword, eEmployeeAddress;
    private ProgressDialog pDialog;
    boolean emptyDiv,emptyEmployee, emptyEmployment;
    private String mLevelUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_db_structure);
        Button create_division = (Button) findViewById(R.id.divison);
        Button create_eployment = (Button) findViewById(R.id.employment);
        Button create_employee_status = (Button) findViewById(R.id.employee_status);

        Button create_user = (Button) findViewById(R.id.add_employee);
        Button create_tags = (Button) findViewById(R.id.tags);
        Button lock_tags = (Button) findViewById(R.id.lock_tags);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        pDialog = new ProgressDialog(DBActivity.this);

        final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        mLevelUser = mPrefHelper.getLevelUser();

        Calendar now = Calendar.getInstance();
        int mYear =  now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mDay,mMonth,mYear);

        getSupportActionBar().setTitle(R.string.app_name);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        pDialog = new ProgressDialog(DBActivity.this);
        pDialog.setMessage("Loading Division, Employment names & Employee status. Please wait...");
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        new GetDivision().execute();

        create_user.setOnClickListener(view -> editData());

        create_division.setOnClickListener(view -> {
            if (mLevelUser.equals("1")) {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Sorry, you're not allowed to change division",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
            } else {
                Intent i = new Intent(getApplication(), DivisionActivity.class);
                startActivity(i);
            }
        });
        create_eployment.setOnClickListener(view -> {
            if (mLevelUser.equals("1")) {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Sorry, you're not allowed to change employment",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
            } else {
                Intent i = new Intent(getApplication(), EmploymentActivity.class);
                startActivity(i);
            }
        });

        create_employee_status.setOnClickListener(view -> {
            if (mLevelUser.equals("1")) {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Sorry, you're not allowed to change employee status",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
            } else {
                Intent i = new Intent(getApplication(), EmployeeStatustActivity.class);
                startActivity(i);
            }
        });

        create_tags.setOnClickListener(view -> {
            Intent i = new Intent(getApplication(), CreateTagsActivity.class);
            startActivity(i);
        });

        lock_tags.setOnClickListener(view -> {
            Intent i = new Intent(getApplication(), ListTagsActivity.class);
            startActivity(i);
        });
    }

    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth1) -> {
                    String daymonth = String.valueOf(dayOfMonth1);
                    String mMonth = String.valueOf(++month1);

                    if (daymonth.length()==1){
                        daymonth = "0"+ dayOfMonth1;
                    } else {
                        daymonth = String.valueOf(dayOfMonth1);
                    }
                    if (mMonth.length()==1){
                        mMonth = "0"+ month1;
                    } else {
                        mMonth = String.valueOf(month1);
                    }

                    String date = daymonth + "-" + mMonth + "-" + year1;
                    eDateBirth.setText(date);
                }, year, month, dayOfMonth);
    }

    private void editData () {
        eDisplayName = new EditText(DBActivity.this);
        eDisplayName.setHint("Real Name");
        eDisplayName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eDisplayName.setMaxLines(1);

        ePlaceBirth = new EditText(DBActivity.this);
        ePlaceBirth.setHint("Place Birth");
        ePlaceBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        ePlaceBirth.setMaxLines(1);

        eDateBirth = new EditText(DBActivity.this);
        eDateBirth.setHint("Date Birth - double click here");
        eDateBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
        eDateBirth.setMaxLines(1);

        eDateBirth.setOnClickListener(view -> datePickerDialog.show());

        eEmail = new EditText(DBActivity.this);
        eEmail.setHint("Email");
        eEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        eEmail.setMaxLines(1);

        ePassword = new EditText(DBActivity.this);
        ePassword.setHint("Password");
        ePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_SHORT_MESSAGE);
        ePassword.setMaxLines(1);

        eEmployeeAddress = new EditText(DBActivity.this);
        eEmployeeAddress.setHint("Employee Address");
        eEmployeeAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eEmployeeAddress.setMaxLines(1);

        final EditText ePhone1 = new EditText(DBActivity.this);
        ePhone1.setHint("Phone Number 1");
        ePhone1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        ePhone1.setMaxLines(1);

        final EditText ePhone2 = new EditText(DBActivity.this);
        ePhone2.setHint("Phone Number 2");
        ePhone2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        ePhone2.setMaxLines(1);

        final Spinner eDivision = new Spinner(DBActivity.this);
        String sDivision = mPrefHelper.getDivisionFull();
        Log.d(TAG, "getDivision: "+sDivision);
        JSONObject jObject;
        try {
            jObject = new JSONObject(sDivision);
            String result_json = jObject.getString("division");
            emptyDiv = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Spinner eEmployment = new Spinner(DBActivity.this);
        String sEmployment = mPrefHelper.getEmployment();
        Log.d(TAG, "getEmployment: "+sEmployment);

        try {
            jObject = new JSONObject(sEmployment);
            String result_json = jObject.getString("employment");
            emptyEmployment = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Spinner eEmployee_status = new Spinner(DBActivity.this);
        String sEmployee_status = mPrefHelper.getEmployee_status();
        Log.d(TAG, "getEmployee_status: "+sEmployee_status);

        try {
            jObject = new JSONObject(sEmployee_status);
            String result_json = jObject.getString("employee_status");
            emptyEmployee = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }



        if (emptyDiv || emptyEmployee || emptyEmployment){
            String textEmpty = null;
            if (emptyDiv) {
                textEmpty = "Division name is empty, please add division first";
            }
            if (emptyEmployee) {
                textEmpty = "Employee status is empty, please add employee status first";
            }
            if (emptyEmployment) {
                textEmpty = "Employment name is empty, please add employment first";
            }
            if (emptyDiv && emptyEmployee ){
                textEmpty = "Division and Employee status are empty, please add them first";

            }
            if (emptyDiv && emptyEmployment){
                textEmpty = "Division and Employment are empty, please add them first";

            }
            if (emptyEmployee && emptyEmployment){
                textEmpty = "Employment and Employee status are empty, please add them first";

            }
            if (emptyDiv && emptyEmployee && emptyEmployment){
                textEmpty = "Division, Employment and Employee status are empty, please add them first";

            }
            assert textEmpty != null;
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,textEmpty,TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();
            return;
        }

        JSONObject jsonResponse;
        ArrayList<String> aName = null;
        try {
            jsonResponse = new JSONObject(sDivision);
            JSONArray cast1 = jsonResponse.getJSONArray("division");
            aName = new ArrayList<>();
            aName.clear();
            for (int i = 0; i < cast1.length(); i++) {
                JSONObject div_name = cast1.getJSONObject(i);
                String name = div_name.getString("name");
                aName.add(name);
                Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aName != null;
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aName);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eDivision.setAdapter(spinnerArrayAdapter);

        JSONObject jsonResponseEmployment;
        ArrayList<String> aNameEmployment = null;
        try {
            jsonResponseEmployment = new JSONObject(sEmployment);
            JSONArray cast2 = jsonResponseEmployment.getJSONArray("employment");
            aNameEmployment = new ArrayList<>();
            aNameEmployment.clear();
            for (int i = 0; i < cast2.length(); i++) {
                JSONObject div_name1 = cast2.getJSONObject(i);
                String name = div_name1.getString("name");
                aNameEmployment.add(name);
                //Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aNameEmployment != null;
        ArrayAdapter<String> spinnerArrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aNameEmployment);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eEmployment.setAdapter(spinnerArrayAdapter1);


        JSONObject jsonResponseEmployeeStatus;
        ArrayList<String> aNameEmployeeStatus = null;
        try {
            jsonResponseEmployeeStatus = new JSONObject(sEmployee_status);
            JSONArray cast3 = jsonResponseEmployeeStatus.getJSONArray("employee_status");
            aNameEmployeeStatus = new ArrayList<>();
            aNameEmployeeStatus.clear();
            for (int i = 0; i < cast3.length(); i++) {
                JSONObject div_name2 = cast3.getJSONObject(i);
                String name = div_name2.getString("name");
                aNameEmployeeStatus.add(name);
               // Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aNameEmployeeStatus != null;
        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aNameEmployeeStatus);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eEmployee_status.setAdapter(spinnerArrayAdapter2);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(eDisplayName);
        layout.addView(ePlaceBirth);
        layout.addView(eDateBirth);
        layout.addView(eEmail);
        layout.addView(ePassword);
        layout.addView(eEmployeeAddress);
        layout.addView(ePhone1);
        layout.addView(ePhone2);
        if (mLevelUser.equals("0")) {
            layout.addView(eDivision);

        }
        layout.addView(eEmployment);
        layout.addView(eEmployee_status);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(layout);


        new AlertDialog.Builder(DBActivity.this)
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

                    String getDivLogic = null;

                    if (mLevelUser.equals("0")){
                        getDivLogic = eDivision.getSelectedItem().toString();
                    } else {
                        getDivLogic = sharedPref.getString("division", "olmatix1");
                    }

                    String iDivison = getDivLogic;
                    String iEmployment = eEmployment.getSelectedItem().toString();
                    String iEmployee_status = eEmployee_status.getSelectedItem().toString();
                    String iPhone1 = ePhone1.getText().toString();
                    String iPhone2 = ePhone2.getText().toString();

                    //Log.d(TAG, "onClick: "+iDivison+":"+iEmployment+":"+iEmployee_status);

                    if (!validate()) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Fail adding new employee, fill all the field please..",TSnackbar.LENGTH_SHORT);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> finish());
                        snackbar.show();

                        return;
                    }

                    pDialog = new ProgressDialog(DBActivity.this);
                    pDialog.setMessage("Creating your employee data. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();

                    sendJsonAdmin(iDisplayName, iPlaceBirth, iDateBirth, iEmail, iPassword, iEmployeeAddress,
                            iDivison, iEmployment, iEmployee_status, iPhone1, iPhone2);

                }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
                }).show();
    }

    private void sendJsonAdmin(String jdisplayname, String jplacecebirth,String jdatebirth,
                               String jemail, String jpassword, String jemployee_address, String jDivison
            , String jEmployment, String jEmployee_status, String jPhone1, String jPhone2){

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();
        String pCompanyName = sharedPref.getString("company_name", "olmatix1");
        String pCompanyAddress = sharedPref.getString("company_address", "olmatix1");
        String pCompanyID = sharedPref.getString("company_id", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://olmatix.com/wamonitoring/create_employee.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "onResponse: "+response);
            parsingJson(response);
            Log.d(TAG, "sendJsonLogin: "+response);
        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("real_name", jdisplayname);
                MyData.put("place_birth", String.valueOf(jplacecebirth));
                MyData.put("date_birth", String.valueOf(jdatebirth));
                MyData.put("email", jemail);
                MyData.put("password", jpassword);
                MyData.put("company_name", pCompanyName);
                MyData.put("company_address", pCompanyAddress);
                MyData.put("company_id", pCompanyID);
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
            Log.d(TAG, "result_json before: "+json);
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

            //if (result_code.equals(1)||result_code.equals("1")) {

            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_json,TSnackbar.LENGTH_SHORT);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();

            pDialog.cancel();

           /* } else {
                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_json,TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> finish());
                snackbar.show();
            }
*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                //JSONObject jsonResponse = null;
                try {
                    //jsonResponse = new JSONObject(response);
                    mPrefHelper.setDivisionFull(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);
                    //String result_code = jObject.getString("success");


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
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
            new GetEmployeeStatus().execute();

        }
    }

    private class GetEmployeeStatus extends AsyncTask<Void, Void, Void> {

        List<HashMap<String, String>> allNames;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_employee_status.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                allNames = new ArrayList<>();
                //JSONObject jsonResponse = null;
                try {
                    //jsonResponse = new JSONObject(response);
                    mPrefHelper.setEmployee_status(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);


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
            new GetEmployment().execute();
        }
    }

    private class GetEmployment extends AsyncTask<Void, Void, Void> {

        List<HashMap<String, String>> allNames;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            /*RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
            String urlJsonArry = "https://olmatix.com/wamonitoring/get_division.php";
            Log.d("DEBUG", "doInBackground: "+urlJsonArry);*/

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_employment.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                allNames = new ArrayList<>();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);
                    mPrefHelper.setEmployment(String.valueOf(response));

                   /* JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    if (!result_code.equals(1)||!result_code.equals("1")) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"currently you have no employment name",TSnackbar.LENGTH_SHORT);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> finish());
                        snackbar.show();
                    }*/
                    pDialog.dismiss();

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

}
