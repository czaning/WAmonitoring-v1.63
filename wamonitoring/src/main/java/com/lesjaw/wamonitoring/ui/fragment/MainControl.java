package com.lesjaw.wamonitoring.ui.fragment;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.bumptech.glide.Glide;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.DBActivity;
import com.lesjaw.wamonitoring.ui.ListEmployeeActivity;
import com.lesjaw.wamonitoring.ui.MainDataActivity;
import com.lesjaw.wamonitoring.ui.TrackEmployeeActivity;
import com.lesjaw.wamonitoring.ui.UploadFotoAcivity;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainControl extends android.support.v4.app.Fragment {

    private static final String TAG = "MainControl";
    Button create_db, view_data, tracking, search;
    LinearLayout coordinatorLayout;
    String mBirth, mCompanyAddress,mEmployeeAddress, mUser, packTypeLabel,mCompanyID, mPhone1, mPhone2, placeB, dateB, mUserName, companyname ;
    boolean editdata = false;
    TextView user, company_name;
    private DatePickerDialog datePickerDialog;
    EditText eDateBirth;
    SharedPreferences sharedPref;
    PreferenceHelper mPrefHelper;
    ArcProgress arcProgress1;
    ArcProgress arcProgress2;
    int packType;
    CircleImageView foto;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Double usercount, tagcount;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return  inflater.inflate(R.layout.frag_main_control, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        create_db = (Button) view.findViewById(R.id.db);
        view_data = (Button) view.findViewById(R.id.view_data);
        tracking = (Button) view.findViewById(R.id.tracking);
        search = (Button) view.findViewById(R.id.search);
        coordinatorLayout=(LinearLayout)view.findViewById(R.id.main_content);
        user = (TextView) view.findViewById(R.id.user);
        company_name = (TextView) view.findViewById(R.id.labelpt);
        TextView labellasttag = (TextView) view.findViewById(R.id.labellasttag);
        //NestedScrollView scrollView = (NestedScrollView) view.findViewById(R.id.scrollView);

        arcProgress1 = (ArcProgress) view.findViewById(R.id.arc_progress1);
        arcProgress2 = (ArcProgress) view.findViewById(R.id.arc_progress2);
        foto = (CircleImageView) view.findViewById(R.id.img_profile);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        mBirth = sharedPref.getString("birth", "none");
        mCompanyAddress = sharedPref.getString("company_address", "jakarta");
        mEmployeeAddress = sharedPref.getString("employee_address", "jakarta");
        mCompanyID = sharedPref.getString("company_id", "jakarta");
        mUser = sharedPref.getString("email", "jakarta");
        mPhone1 = sharedPref.getString("phone1", "jakarta");
        mUserName = sharedPref.getString("real_name", "jakarta");
        companyname = sharedPref.getString("company_name", "jakarta");

        Calendar now = Calendar.getInstance();
        int mYear =  now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mYear,mMonth,mDay);

        Log.d(TAG, "onCreate: "+mCompanyID);

        if (TextUtils.isEmpty(mBirth)||mBirth.equals("none")){
            //Toast.makeText(MainActivity.this, "Birth Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        } else {
            String[] splitmBirth = mBirth.split(",");
            placeB = splitmBirth[0];
            dateB = splitmBirth[1].trim();

        }
        if (TextUtils.isEmpty(mCompanyAddress)||mCompanyAddress.equals("none")){
            //Toast.makeText(MainActivity.this, "Company Address Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        }
        if (TextUtils.isEmpty(mEmployeeAddress)||mEmployeeAddress.equals("none")){
            //Toast.makeText(MainActivity.this, "Employee Address Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        }
        if (TextUtils.isEmpty(mPhone1)||mPhone1.equals("none")){
            //Toast.makeText(MainActivity.this, "Employee Address Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        }

        if (editdata){
            editData();
        }

        create_db.setOnClickListener(view1 -> {
            Intent i = new Intent(getContext(), DBActivity.class);
            startActivity(i);
        });

        search.setOnClickListener(view12 -> {
            Intent i = new Intent(getContext(), ListEmployeeActivity.class);
            startActivity(i);
        });

        view_data.setOnClickListener(view13 -> {
            Intent i = new Intent(getContext(), MainDataActivity.class);
            startActivity(i);
        });

        tracking.setOnClickListener(view14 -> {
            Intent i = new Intent(getContext(), TrackEmployeeActivity.class);
            startActivity(i);
        });

        //Calendar currentDate = Calendar.getInstance();
        //long dateCreat = mPrefHelper.getDateCreated();
        //long currentDateInMillis = currentDate.getTimeInMillis();
        //long daysMillis = currentDateInMillis-dateCreat;
        //int days = (int) ((daysMillis / (1000*60*60*24)) % 7);
        //Log.d(TAG, "onCreate: "+dateCreat+" days "+days);

        packType = Integer.parseInt(mPrefHelper.getPackage());
        packTypeLabel = null;
        if (packType==1){
            packTypeLabel = "trial";
        } else if (packType==2){
            packTypeLabel = "beginner";
        } else if (packType==3){
            packTypeLabel = "advance";
        } else if (packType==4){
            packTypeLabel = "expert";
        } else if (packType>=5){
            packTypeLabel = "unlimited";
        }

        if (mPrefHelper.getLevelUser().equals("0")) {
            user.setText(mUser + " - " + packTypeLabel);
        } else {
            user.setText(mUser + " - " + sharedPref.getString("division", "olmatix1"));
        }

        company_name.setText(companyname);

        GetUsers();
        getPhotoProfileSdcard(mUser);

        foto.setOnClickListener(v -> {
            Intent i = new Intent(getContext(), UploadFotoAcivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);

            //getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        });


        labellasttag.setText(mPrefHelper.getLastLog());

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

              if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                  // new push notification is received
                  String type = intent.getStringExtra("type");
                  String message = intent.getStringExtra("message");
                  String user = intent.getStringExtra("user");
                  String time = intent.getStringExtra("timestamp");

                  if (!user.equals(mUserName)) {

                      switch (type) {
                          case "0":

                              break;
                          case "1":


                              break;
                          case "2":
                              mPrefHelper.setLastLog(message + " by " + user + " at " + time);
                              labellasttag.setText(message + " by " + user + " at " + time);
                              break;
                      }


                  }
                }
            }
        };


    }

    private void getPhotoProfileSdcard (String email){
        String IMAGE_DIRECTORY = "/wamonitoring";
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + "/upload");
        File f = new File(wallpaperDirectory, email + ".jpg");
        String imageInSD = f.getAbsolutePath();
        Glide.with(getContext())
                .load(imageInSD)
                .into(foto);
    }


    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(getContext(),
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

                    String date = year1 + "-" + mMonth + "-" + daymonth;
                    eDateBirth.setText(date);
                }, year, month, dayOfMonth);
    }

    private void editData (){
        final EditText ePlaceBirth = new EditText(getActivity());
        ePlaceBirth.setHint("Place Birth");
        if (!mBirth.equals("none, none")) {
            ePlaceBirth.setText(placeB);
        }
        ePlaceBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        ePlaceBirth.setMaxLines(1);

        eDateBirth = new EditText(getActivity());
        eDateBirth.setHint("Date Birth - double click here");
        if (!mBirth.equals("none, none")) {
            eDateBirth.setText(dateB);
        }
        eDateBirth.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_DATETIME);
        eDateBirth.setMaxLines(1);
        eDateBirth.setOnClickListener(view -> datePickerDialog.show());

        final EditText eCompanyAddress = new EditText(getActivity());
        eCompanyAddress.setHint("Company Address");
        if (!mCompanyAddress.equals("none")) {
            eCompanyAddress.setText(mCompanyAddress);
        }
        eCompanyAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eCompanyAddress.setMaxLines(1);

        final EditText eEmployeeAddress = new EditText(getActivity());
        eEmployeeAddress.setHint("Employee Address");
        if (!mEmployeeAddress.equals("none")) {
            eEmployeeAddress.setText(mEmployeeAddress);
        }
        eEmployeeAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eEmployeeAddress.setMaxLines(1);

        final EditText ePhone1 = new EditText(getActivity());
        ePhone1.setHint("Phone Number 1");
        if (!mPhone1.equals("none")) {
            eEmployeeAddress.setText(mPhone1);
        }
        ePhone1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        ePhone1.setMaxLines(1);

        final EditText ePhone2 = new EditText(getActivity());
        ePhone2.setHint("Phone Number 2");
        if (!mPhone2.equals("none")) {
            eEmployeeAddress.setText(mPhone2);
        }
        ePhone2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        ePhone2.setMaxLines(1);

        eDateBirth.setOnClickListener(view -> datePickerDialog.show());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(ePlaceBirth);
        layout.addView(eDateBirth);
        layout.addView(eCompanyAddress);
        layout.addView(eEmployeeAddress);
        layout.addView(ePhone1);
        layout.addView(ePhone2);

        new AlertDialog.Builder(getContext())
                .setTitle("Update your data")
                .setMessage("Please type your data as field requested")
                .setView(layout)
                .setPositiveButton("SUBMIT", (dialog, which) -> {

                    String iPlaceBirth = ePlaceBirth.getText().toString();
                    String iDateBirth = eDateBirth.getText().toString();
                    String iComapanyAdddress = eCompanyAddress.getText().toString();
                    String iEmployeeAddress = eEmployeeAddress.getText().toString();
                    String iPhone1 = ePhone1.getText().toString();
                    String iPhone2 = ePhone2.getText().toString();

                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("birth",iPlaceBirth+", "+iDateBirth);
                    editor.putString("company_address",iComapanyAdddress);
                    editor.putString("employee_address",iEmployeeAddress);
                    editor.putString("phone1",iEmployeeAddress);
                    editor.putString("phone2",iEmployeeAddress);

                    editor.apply();
/*
                    pDialog = new ProgressDialog(getContext());
                    pDialog.setMessage("Updating your data. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();*/

                    sendJsonAdmin(iPlaceBirth, iDateBirth ,iComapanyAdddress, iEmployeeAddress, iPhone1, iPhone2);

                }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
                }).show();
    }

    private void sendJsonAdmin(String jplacecebirth,String jdatebirth, String jcompany_address, String jemployee_address,
                               String jphone1, String jphone2){
        String mUserName = sharedPref.getString("email", "olmatix1");

        //String tag_json_obj = "login";

        String url = Config.DOMAIN+"wamonitoring/login.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "onResponse: "+response);
            parsingJson(response);
            Log.d(TAG, "sendJsonLogin: "+response);
        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mUserName);
                MyData.put("place_birth", String.valueOf(jplacecebirth));
                MyData.put("date_birth", String.valueOf(jdatebirth));
                MyData.put("company_address", String.valueOf(jcompany_address));
                MyData.put("employee_address", String.valueOf(jemployee_address));
                MyData.put("phone1", String.valueOf(jphone1));
                MyData.put("phone2", String.valueOf(jphone2));

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);


    }

    public void parsingJson(String json) {
        try {
            Log.d(TAG, "result_json before: "+json);
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

            if (result_code.equals("1")) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "No employee found", TSnackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();



            } else {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "No employee found", TSnackbar.LENGTH_LONG);
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
    }

    private void GetUsers(){
        String url;

        //String tag_json_obj = "GetUsers";
        String mDivision = sharedPref.getString("division", "olmatix1");

        if (mPrefHelper.getLevelUser().equals("0")){
            url = Config.DOMAIN+"wamonitoring/get_count_users.php";
        } else {
            url = Config.DOMAIN+"wamonitoring/get_count_users_bydiv.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    Log.d(TAG, "doInBackground: user count "+result_message);
                    if (mPrefHelper.getLevelUser().equals("0")) {
                        mPrefHelper.setUserCount(result_message);
                    } else {
                        mPrefHelper.setUserCountDiv(result_message);
                    }

                }  else {
                    mPrefHelper.setUserCount("0");
                }

                if (mPrefHelper.getLevelUser().equals("0")) {
                    usercount = Double.valueOf(mPrefHelper.getUserCount());
                } else {
                    usercount = Double.valueOf(mPrefHelper.getUserCountDiv());
                }
                Double setProg = null;
                int setProgInt = 0;
                if (packType==1){
                    arcProgress1.setMax(30);
                    setProg = (double) Math.round((usercount / 30) * 30);
                    setProgInt = setProg.intValue();
                }

                if (packType==2){
                    arcProgress1.setMax(100);
                    setProg = (double) Math.round((usercount / 100) * 100);
                    setProgInt = setProg.intValue();
                }

                if (packType==3){
                    arcProgress1.setMax(100);
                    setProg = (double) Math.round((usercount / 100) * 100);
                    setProgInt = setProg.intValue();
                }
                Log.d(TAG, "onPostExecute: "+packType);
                if (packType>=4){
                    arcProgress1.setMax(10000);
                    setProg = (double) Math.round((usercount / 10000) * 10000);
                    setProgInt = setProg.intValue();
                }
                arcProgress1.setProgress(setProgInt);

                GetTags();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void GetTags (){
        String url;
        //String tag_json_obj = "login";
        String mDivision = sharedPref.getString("division", "olmatix1");

        if (mPrefHelper.getLevelUser().equals("0")){
            url = Config.DOMAIN+"wamonitoring/get_count_tags.php";
        } else {
            url = Config.DOMAIN+"wamonitoring/get_count_tags_bydiv.php";
        }
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    Log.d(TAG, "doInBackground: tags count "+result_message);
                    if (mPrefHelper.getLevelUser().equals("0")) {
                        mPrefHelper.setTagsCount(result_message);
                    } else {
                        mPrefHelper.setTagsCountDiv(result_message);

                    }
                } else {
                    mPrefHelper.setTagsCount("0");
                }

                if (mPrefHelper.getLevelUser().equals("0")) {
                    tagcount = Double.valueOf((mPrefHelper.getTagsCount()));
                } else {
                    tagcount = Double.valueOf((mPrefHelper.getTagsCountDiv()));
                }
                Double setProg1 = null;
                int setProgInt1 = 0;
                if (packType==1){
                    arcProgress2.setMax(5);
                    setProg1 = (double) Math.round((tagcount / 5) * 5);
                    setProgInt1 = setProg1.intValue();
                }

                if (packType==2){
                    arcProgress2.setMax(15);
                    setProg1 = (double) Math.round((tagcount / 15) * 15);
                    setProgInt1 = setProg1.intValue();
                }

                if (packType==3){
                    arcProgress2.setMax(15);
                    setProg1 = (double) Math.round((tagcount / 15) * 15);
                    setProgInt1 = setProg1.intValue();
                }

                if (packType>=4){
                    arcProgress2.setMax(10000);
                    setProg1 = (double) Math.round((tagcount / 10000) * 10000);
                    setProgInt1 = setProg1.intValue();
                }
                arcProgress2.setProgress(setProgInt1);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }

    @Override
    public void onResume() {
        super.onResume();
        GetUsers();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
    }

    /*private void GetPhoto(){
        String mEmail = sharedPref.getString("email", "olmatix1");
        //String tag_json_obj = "login";

        String url = Config.DOMAIN+"wamonitoring/get_foto_employee.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("message");

                JSONArray cast = jObject.getJSONArray("employee");
                String imageString = null;
                for (int i = 0; i < cast.length(); i++) {


                    JSONObject tags_name = cast.getJSONObject(i);
                    imageString = tags_name.getString("foto");
                    if (imageString.length()>2) {

                        Glide.with(getContext())
                                .load(imageString)
                                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                .into(foto);
                    } else {
                        foto.setImageResource(R.drawable.user);

                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("email", mEmail);
                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }*/

}
