package com.lesjaw.wamonitoring.ui;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmployeeStatustActivity extends AppCompatActivity {
    private static final String TAG = "DivisionActivity";
    private SharedPreferences sharedPref;
    private CoordinatorLayout coordinatorLayout;
    private PreferenceHelper mPrefHelper;
    private ProgressDialog pDialog;
    private ArrayList<HashMap<String, String>> divisionList;
    public static final String TAG_CID = "cid";
    public static final String TAG_NAME = "name";
    private ListView list_division;
    private EditText lab_division;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_create);
        list_division = (ListView) findViewById(R.id.div_list);
        lab_division = (EditText) findViewById(R.id.lab_division);
        TextView lac_activity = (TextView) findViewById(R.id.lab_actv);
        ImageButton but_add = (ImageButton) findViewById(R.id.but_division);

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);
        mPrefHelper = new PreferenceHelper(getApplicationContext());
        divisionList = new ArrayList<>();
        //ListView lv = getListView();

        getSupportActionBar().setTitle(R.string.app_name);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        lac_activity.setText("employee status");
        lab_division.setHint("type employment status");

        but_add.setOnClickListener(view -> {
            String div_name = lab_division.getText().toString();
            sendJsonDivision(div_name);
            lab_division.setText("");

            pDialog = new ProgressDialog(EmployeeStatustActivity.this);
            pDialog.setMessage("Updating your data. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
        });

        list_division.setOnItemClickListener((arg0, arg1, position, arg3) -> {
            arg1.setSelected(true);

            String pid = ((TextView) arg1.findViewById(R.id.pid)).getText().toString();
            String name = ((TextView) arg1.findViewById(R.id.name)).getText().toString();
            editData(pid,name);

            //Toast.makeText(getApplicationContext(), pid +" : "+name, Toast.LENGTH_SHORT).show();
        });
        //sendJsonDivision();
        new GetRecord().execute();

    }

    private void editData (String pid, String name){

        final EditText eName = new EditText(EmployeeStatustActivity.this);
        eName.setHint("Division name");
        eName.setText(name);
        new AlertDialog.Builder(EmployeeStatustActivity.this)
                .setTitle("Edit or Delete")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setMessage("Do you want to edit or Delete "+name+"?")
                .setView(eName)
                .setPositiveButton("Ok", (dialog, id) -> {
                    String mCompanyID = sharedPref.getString("company_id", "olmatix1");
                    RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

                    String url = "https://olmatix.com/wamonitoring/update_employee_status.php";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                        Log.d(TAG, "onResponse: "+response);
                        parsingJson(response);
                        Log.d(TAG, "sendJsonLogin: "+response);
                    }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<>();
                            MyData.put("company_id", mCompanyID);
                            MyData.put("pid", pid);
                            MyData.put("employee_status", eName.getText().toString());
                            return MyData;
                        }
                    };

                    int socketTimeout = 60000;//30 seconds - change to what you want
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    MyStringRequest.setRetryPolicy(policy);
                    MyRequestQueue.add(MyStringRequest);

                })

                .setNeutralButton("Cancel", (dialog, id) -> dialog.cancel())

                .setNegativeButton("Delete", (dialog, id) -> {
                    String mCompanyID = sharedPref.getString("company_id", "olmatix1");
                    RequestQueue MyRequestQueue = Volley.newRequestQueue(getBaseContext());

                    String url = "https://olmatix.com/wamonitoring/delete_employee_status.php";
                    StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                        Log.d(TAG, "onResponse: "+response);
                        parsingJson(response);
                        Log.d(TAG, "sendJsonLogin: "+response);
                    }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<>();
                            MyData.put("company_id", mCompanyID);
                            MyData.put("pid", pid);
                            //MyData.put("divison_name", name);
                            return MyData;
                        }
                    };

                    int socketTimeout = 60000;//30 seconds - change to what you want
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    MyStringRequest.setRetryPolicy(policy);
                    MyRequestQueue.add(MyStringRequest);
                })

                .show();
    }


    private void sendJsonDivision(String div_name){
        String mCompanyID = sharedPref.getString("company_id", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(this);

        String url = "https://olmatix.com/wamonitoring/insert_employee_status.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Log.d(TAG, "onResponse: "+response);
            parsingJson(response);
            Log.d(TAG, "sendJsonLogin: "+response);
        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("employee_status", div_name);

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
            //Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

            if (result_code.equals("1")) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout,result_json,TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> finish());
                snackbar.show();
                pDialog.cancel();
                new GetRecord().execute();

            } else {
                pDialog.cancel();

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private class GetRecord extends AsyncTask<Void, Void, Void> {

        List<HashMap<String, String>> allNames;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(EmployeeStatustActivity.this);
            pDialog.setMessage("Loading Employmee status. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();
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
                allNames = new ArrayList<>();
                JSONObject jsonResponse = null;
                try {
                    divisionList.clear();
                    jsonResponse = new JSONObject(response);
                    mPrefHelper.setEmployee_status(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("employee_status");

                        for (int i = 0; i < cast.length(); i++) {
                            JSONObject div_name = cast.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<>();
                            String cid = div_name.getString("cid");
                            String name = div_name.getString("name");
                            map.put(TAG_CID, cid);
                            map.put(TAG_NAME, name);
                            Log.d(TAG, "doInBackground: " + name);
                            divisionList.add(map);
                        }
                        // updating UI from Background Thread
                        //runOnUiThread(new Runnable() {
                        //   public void run() {
                        Log.d(TAG, "Do Background done: ");
                        ListAdapter adapter = new SimpleAdapter(
                                EmployeeStatustActivity.this, divisionList,
                                R.layout.list_item, new String[]{TAG_CID,
                                TAG_NAME},
                                new int[]{R.id.pid, R.id.name});
                        // updating listview
                        list_division.setAdapter(adapter);
                        //   }
                        // });

                    } else {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"currently you have no employee status",TSnackbar.LENGTH_SHORT);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                        snackbar.setAction("OK", v -> finish());
                        snackbar.show();
                    }
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

}
