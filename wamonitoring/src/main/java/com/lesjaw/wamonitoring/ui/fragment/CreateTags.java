package com.lesjaw.wamonitoring.ui.fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import com.lesjaw.wamonitoring.ui.MapsActivity;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.SpinnerListener;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CreateTags extends Fragment {
    private Button btn_submit;
    private EditText tag_name,loc,timeInterval;
    private Spinner spin;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final String TAG = "CreateTagsActivity";
    private CoordinatorLayout coordinatorLayout;
    StyleableToast styleableToast;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_input_tag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_submit = (Button) view.findViewById(R.id.btn_submit);
        tag_name = (EditText)view.findViewById(R.id.tag_name);
        loc = (EditText)view.findViewById(R.id.loc);
        timeInterval = (EditText)view.findViewById(R.id.time_interval);
        spin = (Spinner) view.findViewById(R.id.div_name);

        tag_name.setFilters( new InputFilter[] {new InputFilter.LengthFilter(15)});
        mPrefHelper = new PreferenceHelper(getContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        coordinatorLayout=(CoordinatorLayout)view.findViewById(R.id.main_content);

        GetTags();
        new GetDivision().execute();

        btn_submit.setOnClickListener(v -> submit());

        String lat = String.valueOf(mPrefHelper.getTagLatitude());
        String lot = String.valueOf(mPrefHelper.getTagLongitude());
        loc.setText(lat+","+lot);

        loc.setOnClickListener(view1 -> new AlertDialog.Builder(getContext())
                .setTitle("Set location for tag")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setMessage("Do you want to autodetect or chosing from map?")
                .setPositiveButton("Map", (dialog, id) -> {

                    Intent i = new Intent(getContext(), MapsActivity.class);
                    i.putExtra("id_caller","CreateTags");
                    startActivity(i);

                })

                .setNeutralButton("Cancel", (dialog, id) -> dialog.cancel())

                .setNegativeButton("Auto", (dialog, id) -> {

                    double lat1 = mPrefHelper.getTagLatitude();
                    double lot1 = mPrefHelper.getTagLongitude();

                    loc.setText(lat1 +","+ lot1);
                })

                .show());

    }

    public boolean validate() {
        boolean valid = true;

        String vtagName = tag_name.getText().toString();
        String vLoc = loc.getText().toString();
        String vTimeInterval = timeInterval.getText().toString();

        if (vtagName.isEmpty() || vtagName.length() < 3) {
            tag_name.setError("at least 3 characters");
            valid = false;
        } else {
            tag_name.setError(null);
        }

        if (vLoc.isEmpty() || vLoc.length() < 3) {
            loc.setError("at least 3 characters");
            valid = false;
        } else {
            loc.setError(null);
        }

        if (vTimeInterval.isEmpty() || vTimeInterval.length() < 3) {
            timeInterval.setError("at least 3 characters");
            valid = false;
        } else {
            timeInterval.setError(null);
        }



        return valid;
    }

    public void onLoginFailed() {
        styleableToast = new StyleableToast
                .Builder(getContext())
                .icon(R.drawable.ic_action_info)
                .text("Check field please!")
                .textColor(Color.WHITE)
                .backgroundColor(Color.RED)
                .build();
        styleableToast.show();
        btn_submit.setEnabled(true);
    }

    public void submit() {

        if (!validate()) {
            onLoginFailed();
            return;
        }

        int packtype = Integer.parseInt(mPrefHelper.getPackage());
        int tagcount = Integer.parseInt(mPrefHelper.getTagsCount());

        //Log.d(TAG, "submit: "+usercount +" "+tagcount);

        if (packtype==1 && tagcount>=5){
            snacknotif("tag has reach package limit - 5 tags");
            return;
        }

        if (packtype==2 && tagcount>=50){
            snacknotif("tag has reach package limit - 50 tags");
            return;
        }

        if (packtype==3 && tagcount>=150){
            snacknotif("tag has reach package limit - 150 tags");
            return;
        }

        btn_submit.setEnabled(false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating tag checkpoint. Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String tagName = tag_name.getText().toString();
        String mloc = loc.getText().toString();

        sendJsonTag(tagName, mloc, timeInterval.getText().toString() );

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

    private void sendJsonTag(String jTagname, String jLoc, String jTime){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String mCompany_id = sharedPref.getString("company_id", "olmatix1");

        spin.setOnItemSelectedListener(new SpinnerListener());
        String databaseId = (String.valueOf(( (DivisionAndID) spin.getSelectedItem ()).getId()));


        //Log.d(TAG, "sendJsonTag: "+databaseId);
        RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());

        String url = "https://olmatix.com/wamonitoring/create_tags.php";
        //Log.d(TAG, "onResponse: "+response);
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, this::parsingJson, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("tag_name", jTagname);
                MyData.put("tag_location", jLoc);
                MyData.put("company_id", mCompany_id);
                MyData.put("division_name", databaseId);
                MyData.put("time_interval", jTime);

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

            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            //String result_code = jObject.getString("success");
            //String result_user = jObject.getString("email");


            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_json,TSnackbar.LENGTH_SHORT);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.show();

            //new GetUsers().execute();


            progressDialog.hide();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void GetTags() {

        String url  = Config.DOMAIN + "wamonitoring/get_count_tags.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                        mPrefHelper.setTagsCount(result_message);
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
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }

    @Override
    public void onPause() {
        super.onPause();
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

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());

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

                        String mLevelUser = mPrefHelper.getLevelUser();
                        ArrayList<DivisionAndID> aName;
                        aName = new ArrayList<>();
                        aName.clear();
                        if (mLevelUser.equals("0")){
                            String sDivision = mPrefHelper.getDivisionFull();
                            //Log.d(TAG, "getDivision: "+sDivision);
                            //JSONObject jsonResponse1;
                            try {
                                //jsonResponse1 = new JSONObject(sDivision);
                                JSONArray cast1 = jsonResponse.getJSONArray("division");
                                for (int i = 0; i < cast1.length(); i++) {
                                    JSONObject div_name = cast1.getJSONObject(i);
                                    String name = div_name.getString("name");
                                    String cid = div_name.getString("cid");

                                    aName.add(new DivisionAndID(name,cid));
                                    //Log.d(TAG, "editData: "+name);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {

                            String name = mPrefHelper.getDivName();
                            String cid =sharedPref.getString("division", "olmatix1");

                            aName.add(new DivisionAndID(name,cid));
                        }
                        ArrayAdapter<DivisionAndID> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, aName);
                        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                        spin.setAdapter(spinnerArrayAdapter);

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
    public void onResume() {
        super.onResume();

        loc.setText("");
        final PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());
        String lat = String.valueOf(mPrefHelper.getTagLatitude());
        String lot = String.valueOf(mPrefHelper.getTagLongitude());
        loc.setText(lat+","+lot);
    }
}
