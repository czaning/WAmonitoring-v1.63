package com.lesjaw.wamonitoring.ui;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.DivisionAndID;
import com.lesjaw.wamonitoring.ui.fragment.CreateDivision;
import com.lesjaw.wamonitoring.ui.fragment.CreateEmployment;
import com.lesjaw.wamonitoring.ui.fragment.CreateStatus;
import com.lesjaw.wamonitoring.ui.fragment.CreateTags;
import com.lesjaw.wamonitoring.ui.fragment.CreateWorkingPlan;
import com.lesjaw.wamonitoring.ui.fragment.ListTags;
import com.lesjaw.wamonitoring.ui.fragment.UserRegisBarcode;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.SpinnerListener;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DBStructureActivity extends AppCompatActivity {
    BoomMenuButton bmb, bmb1;
    private EditText eDisplayName, eDateBirth, ePlaceBirth, eEmail, ePassword, eEmployeeAddress;
    boolean emptyDiv, emptyEmployee, emptyEmployment;
    private PreferenceHelper mPrefHelper;
    private SharedPreferences sharedPref;
    StyleableToast styleableToast;

    private DatePickerDialog datePickerDialog;
    private static final String TAG = "DBActivity";
    private String mLevelUser;
    private ProgressDialog pDialog;
    String pCompanyID,mEmail,pCompanyAddress,pCompanyName;
    private LinearLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dbstructure);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("DB Structure");
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        coordinatorLayout=(LinearLayout) findViewById(R.id.main_content);

        Calendar now = Calendar.getInstance();
        int mYear = now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mDay, mMonth, mYear);

        bmb = (BoomMenuButton) findViewById(R.id.bmb);
        bmb1 = (BoomMenuButton) findViewById(R.id.bmb1);
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        mLevelUser = mPrefHelper.getLevelUser();
        pCompanyID = sharedPref.getString("company_id", "olmatix1");
        pCompanyAddress = sharedPref.getString("company_address", "olmatix1");
        pCompanyName = sharedPref.getString("company_name", "olmatix1");

        mEmail = sharedPref.getString("email", "jakarta");

       /* android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        ListTags hello1 = new ListTags();
        fragmentTransaction.replace(R.id.fragment_container, hello1, "HELLO");
        fragmentTransaction.commit();*/

        for (int i = 0; i < bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();


            if (i == 0) {
                builder.normalImageRes(R.mipmap.ic_dvision)
                        .normalText("Create Division")
                        .subNormalText("Click to create your division company/project");
            }

            if (i == 1) {

                builder.normalImageRes(R.drawable.ic_face_black_48dp)
                        .normalText("Create Employment")
                        .subNormalText("Click to create employment of employee, ex. junior, senior, etc");
            }
            if (i == 2) {

                builder.normalImageRes(R.mipmap.ic_status_employee)
                        .normalText("Create Status")
                        .subNormalText("Click to Employee status, ex. Staff, Non staff, freelance etc");
            }

            if (i == 3) {
                builder.normalImageRes(R.mipmap.barcode_img)
                        .normalText("Create Tags")
                        .subNormalText("Click to create tags/checkpoint");
            }

            if (i == 4) {
                builder.normalImageRes(R.mipmap.ic_employee)
                        .normalText("Create User DB")
                        .subNormalText("Click to create employee account");
            }

            if (i == 5) {
                builder.normalImageRes(R.mipmap.ic_password)
                        .normalText("Create Working Plan")
                        .subNormalText("Click to create working plan");
            }


            builder.listener(index -> {
                // When the boom-button corresponding this builder is clicked.

                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (index == 0) {
                    if (mLevelUser.equals("0")) {

                        CreateDivision hello = new CreateDivision();
                        fragmentTransaction.replace(R.id.fragment_container, hello, "HELLO");
                    } else {
                        styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text("Only admin can create division")
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .build();
                        styleableToast.show();
                    }
                }

                if (index == 1) {
                    if (mLevelUser.equals("0")) {

                        CreateEmployment hello = new CreateEmployment();
                        fragmentTransaction.replace(R.id.fragment_container, hello, "HELLO");
                    } else {
                        styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text("Only admin can create employment")
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .build();
                        styleableToast.show();
                    }
                }

                if (index == 2) {
                    if (mLevelUser.equals("0")) {

                        CreateStatus hello = new CreateStatus();
                        fragmentTransaction.replace(R.id.fragment_container, hello, "HELLO");
                    } else {
                        styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text("Only admin can create employee status")
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .build();
                        styleableToast.show();
                    }
                }

                if (index == 3) {
                    CreateTags hello = new CreateTags();
                    fragmentTransaction.replace(R.id.fragment_container, hello, "HELLO");
                }

                if (index == 4) {

                    int packtype = Integer.parseInt(mPrefHelper.getPackage());
                    int tagcount = Integer.parseInt(mPrefHelper.getTagsCount());
                    int usercount = Integer.parseInt(mPrefHelper.getUserCount());

                    //Log.d(TAG, "submit: "+usercount +" "+tagcount);

                    if (packtype==1 && usercount>=10){
                        snacknotif("users has reach package limit - 30 users");
                        return;
                    }

                    if (packtype==2 && usercount>=100){
                        snacknotif("users has reach package limit - 100 users");

                        return;
                    }

                    if (packtype==3 && usercount>=250){
                        snacknotif("users has reach package limit - 250 users");

                        return;
                    }


                    editData();
                }

                if (index == 5) {
                    CreateWorkingPlan createWorkingPlan = new CreateWorkingPlan();
                    fragmentTransaction.replace(R.id.fragment_container, createWorkingPlan, "HELLO");
                }

                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            });
            bmb.isDraggable();
            bmb.addBuilder(builder);

        }

        for (int i = 0; i < bmb1.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();


            if (i == 0) {
                builder.normalImageRes(R.mipmap.barcode_img)
                        .normalText("View Tags/Checkpoint")
                        .subNormalText("Click to view, print or write tags/checkpoint");
            }

            if (i == 1) {
                builder.normalImageRes(R.mipmap.ic_employee)
                        .normalText("View Tags self user registration")
                        .subNormalText("Click to view, print or write user registration");
            }


            if (i == 2) {
                builder.normalImageRes(R.mipmap.ic_password)
                        .normalText("Create Logout password")
                        .subNormalText("Click to create logout password for each division");
            }

            builder.listener(index -> {
                // When the boom-button corresponding this builder is clicked.

                android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (index == 0) {
                    ListTags hello = new ListTags();
                    fragmentTransaction.replace(R.id.fragment_container, hello, "HELLO");
                }

                if (index == 1) {

                    /*Intent i1 = new Intent(DBStructureActivity.this, BarcodeGenerator.class);
                    i1.putExtra("barcode", mPrefHelper.getDivName()+ " user registration");
                    i1.putExtra("barname", "User Registration");
                    i1.putExtra("division",sharedPref.getString("division", "jakarta"));
                    i1.putExtra("loc", mPrefHelper.getTagLatitude()+","+mPrefHelper.getTagLongitude());

                    startActivity(i1);*/
                    Bundle bundle = new Bundle();
                    bundle.putString("barcode", sharedPref.getString("division", "jakarta")+","+pCompanyID+","+pCompanyAddress+","+pCompanyName);
                    bundle.putString("barname", "User Registration");
                    bundle.putString("division", mPrefHelper.getDivName());
                    bundle.putString("loc", mPrefHelper.getTagLatitude()+","+mPrefHelper.getTagLongitude());

                    UserRegisBarcode data1 = new UserRegisBarcode();
                    data1.setArguments(bundle);
                    fragmentTransaction.replace(R.id.fragment_container, data1);

                }

                if (index == 2) {
                    String divId = sharedPref.getString("division", "olmatix1");

                    final EditText pass = new EditText(DBStructureActivity.this);
                    pass.setHint("Type your logout password");
                    pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    pass.setMaxLines(1);

                    LinearLayout layout = new LinearLayout(DBStructureActivity.this);
                    layout.setOrientation(LinearLayout.VERTICAL);
                    layout.addView(pass);

                    new AlertDialog.Builder(DBStructureActivity.this)
                            .setTitle("Logout password")
                            .setIcon(R.drawable.ic_info_black_24dp)
                            .setMessage("Logout password for "+mPrefHelper.getDivName()+"\nYou need to change this password often")
                            .setView(layout)
                            .setPositiveButton("Send", (dialog, id) -> {

                                // SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                //String tgl = df.format(Calendar.getInstance().getTime());

                                String url = Config.DOMAIN+"wamonitoring/insert_update_pass_logout.php";

                                StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

                                    JSONObject jObject = null;
                                    try {
                                        jObject = new JSONObject(response);
                                        //String result_json = jObject.getString("message");
                                        String result_code = jObject.getString("success");

                                        if (result_code.equals("1")) {
                                            styleableToast = new StyleableToast
                                                    .Builder(getBaseContext())
                                                    .icon(R.drawable.ic_action_info)
                                                    .text("Logout password success updated")
                                                    .textColor(Color.WHITE)
                                                    .backgroundColor(Color.RED)
                                                    .build();
                                            styleableToast.show();
                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                                    protected Map<String, String> getParams() {
                                        Map<String, String> MyData = new HashMap<>();
                                        MyData.put("division", divId);
                                        MyData.put("config_value", pass.getText().toString().trim());
                                        MyData.put("config_maker", mEmail);
                                        MyData.put("company_id", pCompanyID);


                                        return MyData;
                                    }
                                };

                                // Adding request to request queue
                                NetworkRequest.getInstance(DBStructureActivity.this).addToRequestQueue(jsonObjReq);

                            })

                            .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())

                            .show();

                }


                //fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();

            });
            bmb1.isDraggable();
            bmb1.addBuilder(builder);

        }


        bmb.setAutoBoom(true);

        new GetDivision().execute();
        new GetEmployment().execute();
        new GetEmpStatus().execute();

    }

    public void snacknotif (String notif){
        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,notif,TSnackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
        snackbar.setAction("OK", v -> {

        });
        snackbar.setActionTextColor(Color.BLACK);
        snackbar.show();
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        // super.onSaveInstanceState(outState);
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
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        //bmb.boom();
    }

    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(this,
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

                    String date = daymonth + "-" + mMonth + "-" + year1;
                    eDateBirth.setText(date);
                }, year, month, dayOfMonth);
    }

    private void editData() {
        eDisplayName = new EditText(this);
        eDisplayName.setHint("Real Name");
        eDisplayName.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eDisplayName.setMaxLines(1);
        eDisplayName.setFilters( new InputFilter[] {new InputFilter.LengthFilter(20)});

        ePlaceBirth = new EditText(this);
        ePlaceBirth.setHint("Place Birth");
        ePlaceBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        ePlaceBirth.setMaxLines(1);
        ePlaceBirth.setFilters( new InputFilter[] {new InputFilter.LengthFilter(20)});

        eDateBirth = new EditText(this);
        eDateBirth.setHint("Date Birth - double click here");
        eDateBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
        eDateBirth.setMaxLines(1);
        eDateBirth.setFilters( new InputFilter[] {new InputFilter.LengthFilter(10)});

        eDateBirth.setOnClickListener(view -> datePickerDialog.show());

        eEmail = new EditText(this);
        eEmail.setHint("Email");
        eEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        eEmail.setMaxLines(1);
        eEmail.setFilters( new InputFilter[] {new InputFilter.LengthFilter(25)});

        ePassword = new EditText(this);
        ePassword.setHint("Password");
        ePassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        ePassword.setMaxLines(1);
        ePassword.setFilters( new InputFilter[] {new InputFilter.LengthFilter(10)});

        eEmployeeAddress = new EditText(this);
        eEmployeeAddress.setHint("Employee Address");
        eEmployeeAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eEmployeeAddress.setMaxLines(1);
        eEmployeeAddress.setFilters( new InputFilter[] {new InputFilter.LengthFilter(50)});

        final EditText ePhone1 = new EditText(this);
        ePhone1.setHint("Phone Number 1");
        ePhone1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        ePhone1.setMaxLines(1);
        ePhone1.setFilters( new InputFilter[] {new InputFilter.LengthFilter(12)});

        final EditText ePhone2 = new EditText(this);
        ePhone2.setHint("Phone Number 2");
        ePhone2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_NUMBER);
        ePhone2.setMaxLines(1);
        ePhone2.setFilters( new InputFilter[] {new InputFilter.LengthFilter(12)});

        final Spinner eDivision = new Spinner(this);
        String sDivision = mPrefHelper.getDivisionFull();
        //Log.d(TAG, "getDivision: "+sDivision);
        JSONObject jObject;
        try {
            jObject = new JSONObject(sDivision);
            String result_json = jObject.getString("division");
            emptyDiv = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Spinner eEmployment = new Spinner(this);
        String sEmployment = mPrefHelper.getEmployment();
        //Log.d(TAG, "getEmployment: "+sEmployment);

        try {
            jObject = new JSONObject(sEmployment);
            String result_json = jObject.getString("employment");
            emptyEmployment = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final Spinner eEmployee_status = new Spinner(this);
        String sEmployee_status = mPrefHelper.getEmployee_status();
        //Log.d(TAG, "getEmployee_status: "+sEmployee_status);
        try {
            jObject = new JSONObject(sEmployee_status);
            String result_json = jObject.getString("employee_status");
            emptyEmployee = result_json.equals("Empty");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        if (emptyDiv || emptyEmployee || emptyEmployment) {
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
            if (emptyDiv && emptyEmployee) {
                textEmpty = "Division and Employee status are empty, please add them first";

            }
            if (emptyDiv && emptyEmployment) {
                textEmpty = "Division and Employment are empty, please add them first";

            }
            if (emptyEmployee && emptyEmployment) {
                textEmpty = "Employment and Employee status are empty, please add them first";

            }
            if (emptyDiv && emptyEmployee && emptyEmployment) {
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
        ArrayList<DivisionAndID> aName = null;
        try {
            jsonResponse = new JSONObject(sDivision);
            JSONArray cast1 = jsonResponse.getJSONArray("division");
            aName = new ArrayList<>();
            aName.clear();
            for (int i = 0; i < cast1.length(); i++) {
                JSONObject div_name = cast1.getJSONObject(i);
                String name = div_name.getString("name");
                String cid = div_name.getString("cid");
                aName.add(new DivisionAndID(name, cid));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aName != null;
        ArrayAdapter<DivisionAndID> spinnerArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aName);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eDivision.setAdapter(spinnerArrayAdapter);

        JSONObject jsonResponseEmployment;
        ArrayList<DivisionAndID> aNameEmployment = null;
        try {
            jsonResponseEmployment = new JSONObject(sEmployment);
            JSONArray cast2 = jsonResponseEmployment.getJSONArray("employment");
            aNameEmployment = new ArrayList<>();
            aNameEmployment.clear();
            for (int i = 0; i < cast2.length(); i++) {
                JSONObject div_name1 = cast2.getJSONObject(i);
                String name = div_name1.getString("name");
                String cid = div_name1.getString("cid");
                aNameEmployment.add(new DivisionAndID(name, cid));
                //Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aNameEmployment != null;
        ArrayAdapter<DivisionAndID> spinnerArrayAdapter1 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aNameEmployment);
        spinnerArrayAdapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eEmployment.setAdapter(spinnerArrayAdapter1);


        JSONObject jsonResponseEmployeeStatus;
        ArrayList<DivisionAndID> aNameEmployeeStatus = null;
        try {
            jsonResponseEmployeeStatus = new JSONObject(sEmployee_status);
            JSONArray cast3 = jsonResponseEmployeeStatus.getJSONArray("employee_status");
            aNameEmployeeStatus = new ArrayList<>();
            aNameEmployeeStatus.clear();
            for (int i = 0; i < cast3.length(); i++) {
                JSONObject div_name2 = cast3.getJSONObject(i);
                String name = div_name2.getString("name");
                String cid = div_name2.getString("cid");
                aNameEmployeeStatus.add(new DivisionAndID(name, cid));
                /// / Log.d(TAG, "editData: "+name);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        assert aNameEmployeeStatus != null;
        ArrayAdapter<DivisionAndID> spinnerArrayAdapter2 = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, aNameEmployeeStatus);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        eEmployee_status.setAdapter(spinnerArrayAdapter2);

        LinearLayout layout = new LinearLayout(DBStructureActivity.this);
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


        new AlertDialog.Builder(DBStructureActivity.this)
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
                    eDivision.setOnItemSelectedListener(new SpinnerListener());
                    int divID = Integer.parseInt(String.valueOf(((DivisionAndID) eDivision.getSelectedItem()).getId()));
                    int EmployementID = Integer.parseInt(String.valueOf(((DivisionAndID) eEmployment.getSelectedItem()).getId()));
                    int StatusID = Integer.parseInt(String.valueOf(((DivisionAndID) eEmployee_status.getSelectedItem()).getId()));

                    if (mLevelUser.equals("0")) {
                        getDivLogic = String.valueOf(divID);
                    } else {
                        getDivLogic = sharedPref.getString("division", "olmatix1");
                    }

                    String iDivison = getDivLogic;
                    String iEmployment = String.valueOf(EmployementID);
                    String iEmployee_status = String.valueOf(StatusID);
                    String iPhone1 = ePhone1.getText().toString();
                    String iPhone2 = ePhone2.getText().toString();

                    //Log.d(TAG, "onClick: "+iDivison+":"+iEmployment+":"+iEmployee_status);

                    if (!validate()) {

                        styleableToast = new StyleableToast
                                .Builder(getBaseContext())
                                .icon(R.drawable.ic_action_info)
                                .text("Check field please!, all field must be fill")
                                .textColor(Color.WHITE)
                                .backgroundColor(Color.RED)
                                .build();
                        styleableToast.show();
                        return;
                    }

                    pDialog = new ProgressDialog(DBStructureActivity.this);
                    pDialog.setMessage("Creating your employee data. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();

                    sendJsonAdmin(iDisplayName, iPlaceBirth, iDateBirth, iEmail, iPassword, iEmployeeAddress,
                            iDivison, iEmployment, iEmployee_status, iPhone1, iPhone2);

                }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
                }).show();
    }

    private void sendJsonAdmin(String jdisplayname, String jplacecebirth, String jdatebirth,
                               String jemail, String jpassword, String jemployee_address, String jDivison
            , String jEmployment, String jEmployee_status, String jPhone1, String jPhone2) {

        Calendar now = Calendar.getInstance();
        now.setTime(new Date());
        now.getTimeInMillis();
        String pCompanyName = sharedPref.getString("company_name", "olmatix1");
        String pCompanyAddress = sharedPref.getString("company_address", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://olmatix.com/wamonitoring/create_employee.php";

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, this::parsingJson, error -> Log.d(TAG, "onErrorResponse: " + error)) {
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
                MyData.put("working_gps", mPrefHelper.getTagLatitude()+","+mPrefHelper.getTagLongitude());


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
            //Log.d(TAG, "result_json before: " + json);
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            //Log.d(TAG, "result_json: " + result_json + "result_code: " + result_code);

            if (result_code.equals("1")) {
                GetUsers();
            }

            styleableToast = new StyleableToast
                    .Builder(getBaseContext())
                    .icon(R.drawable.ic_action_info)
                    .text(result_json)
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.BLUE)
                    .build();
            styleableToast.show();


            pDialog.cancel();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void GetUsers() {

        String url = Config.DOMAIN + "wamonitoring/get_count_users.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    //Log.d(TAG, "doInBackground: user count " + result_message);
                        mPrefHelper.setUserCount(result_message);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", sharedPref.getString("company_id", "jakarta"));

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getBaseContext()).addToRequestQueue(jsonObjReq);

    }

    public boolean validate() {
        boolean valid = true;

        String vDisplayName = eDisplayName.getText().toString();
        //eDateBirth.getText().toString();
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

    private class GetEmployment extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_employment.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    mPrefHelper.setEmployment(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    if (result_code.equals("1")) {

                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"currently you have no employment name",TSnackbar.LENGTH_SHORT);
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

    private class GetEmpStatus extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");

            /*RequestQueue requestQueue = Volley.newRequestQueue(getBaseContext());
            String urlJsonArry = "https://olmatix.com/wamonitoring/get_division.php";
            Log.d("DEBUG", "doInBackground: "+urlJsonArry);*/

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

            String url = "https://olmatix.com/wamonitoring/get_employee_status.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    mPrefHelper.setEmployee_status(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    if (result_code.equals("1")) {

                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"currently you have no employee status",TSnackbar.LENGTH_SHORT);
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

}
