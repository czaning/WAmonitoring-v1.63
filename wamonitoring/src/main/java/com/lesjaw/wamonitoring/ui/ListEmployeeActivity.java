package com.lesjaw.wamonitoring.ui;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListEmployeeAdapter;
import com.lesjaw.wamonitoring.model.EmployeeModel;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.EndlessRecyclerViewScrollListener;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ListEmployeeActivity extends AppCompatActivity {

    private List<EmployeeModel> tagList = new ArrayList<>();
    private ListEmployeeAdapter mAdapter;
    private ProgressDialog pDialog;
    private SharedPreferences sharedPref;
    private CoordinatorLayout coordinatorLayout;
    private String mLevelUser;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_rc);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_content);

        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Employee List</font>"));

        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        pDialog = new ProgressDialog(ListEmployeeActivity.this);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        mLevelUser = mPrefHelper.getLevelUser();

        Context mContext = ListEmployeeActivity.this;
        RecyclerView mRecycleView = (RecyclerView) findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new ListEmployeeAdapter(tagList, mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        getData("1", "", "0");

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                //Log.d(TAG, "last position and firtpage is " + firstpage);
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                loadNextDataFromApi("1", "", String.valueOf(firstpage));
                firstpage = firstpage + 10;
            }
        };
        mRecycleView.addOnScrollListener(scrollListener);

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(ListEmployeeActivity.this,
                mRecycleView, new ClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

    }

    private void loadNextDataFromApi(String searchid, String searchby, String page) {
        pDialog = new ProgressDialog(ListEmployeeActivity.this);
        pDialog.setMessage("Loading Employees list. Please wait...");
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mDivision;
        if(!mLevelUser.equals("4")){
            mDivision = sharedPref.getString("division", "olmatix1");
        }else{
            final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
            mDivision = mPrefHelper.getGroup();
        }

        String url;
        if (mLevelUser.equals("0")) {
            url = Config.DOMAIN + "wamonitoring/get_employee_all.php";
        } else if(mLevelUser.equals("4")){
            url = Config.DOMAIN + "wamonitoring/get_employee_by_group.php";
        }else {
            url = Config.DOMAIN + "wamonitoring/get_employee_by_division.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("employee");
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String real_name = tags_name.getString("real_name");
                        String password = tags_name.getString("password");
                        String place_birth = tags_name.getString("place_birth");
                        String date_birth = tags_name.getString("date_birth");
                        String email = tags_name.getString("email");
                        String employee_address = tags_name.getString("employee_address");
                        String employment_status = tags_name.getString("employment_status");
                        String division = tags_name.getString("division");
                        String employment = tags_name.getString("employment");
                        String phone1 = tags_name.getString("phone1");
                        String phone2 = tags_name.getString("phone2");
                        String last_update = tags_name.getString("last_update");
                        String level_user = tags_name.getString("level_user");
                        String foto = tags_name.getString("foto");
                        String log_status = tags_name.getString("log_status");
                        String firbaseID = tags_name.getString("firebase_id");


                        EmployeeModel tags = new EmployeeModel(real_name, password, place_birth, date_birth, email,
                                employee_address, employment_status, division, employment, phone1, phone2,
                                last_update, level_user, foto, log_status, firbaseID);
                        tagList.add(tags);
                        //Log.d(TAG, "setData: "+real_name);
                    }

                    pDialog.cancel();
                    //mAdapter.notifyDataSetChanged();
                    //mAdapter = new ListEmployeeAdapter(tagList,mContext);
                    mAdapter.notifyDataSetChanged();

                } else {
                    pDialog.cancel();

                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Last employee list", TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.show();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("searchid", searchid);
                MyData.put("searchby", searchby);
                MyData.put("division", mDivision);
                MyData.put("offsett", page);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }


    private void getData(String searchid, String searchby, String page) {
        pDialog.setMessage("Loading Employees list. Please wait...");
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mDivision;
        if(!mLevelUser.equals("4")){
            mDivision = sharedPref.getString("division", "olmatix1");
        }else{
            final PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
            mDivision = mPrefHelper.getGroup();
        }

        String url;
        if (mLevelUser.equals("0")) {
            url = Config.DOMAIN + "wamonitoring/get_employee_all.php";
        } else if(mLevelUser.equals("4")){
            url = Config.DOMAIN + "wamonitoring/get_employee_by_group.php";
        } else {
            url = Config.DOMAIN + "wamonitoring/get_employee_by_division.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("employee");
                    tagList.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String real_name = tags_name.getString("real_name");
                        String password = tags_name.getString("password");
                        String place_birth = tags_name.getString("place_birth");
                        String date_birth = tags_name.getString("date_birth");
                        String email = tags_name.getString("email");
                        String employee_address = tags_name.getString("employee_address");
                        String employment_status = tags_name.getString("employment_status");
                        String division = tags_name.getString("division");
                        String employment = tags_name.getString("employment");
                        String phone1 = tags_name.getString("phone1");
                        String phone2 = tags_name.getString("phone2");
                        String last_update = tags_name.getString("last_update");
                        String level_user = tags_name.getString("level_user");
                        String foto = tags_name.getString("foto");
                        String log_status = tags_name.getString("log_status");
                        String firbaseID = tags_name.getString("firebase_id");


                        EmployeeModel tags = new EmployeeModel(real_name, password, place_birth, date_birth, email,
                                employee_address, employment_status, division, employment, phone1, phone2,
                                last_update, level_user, foto, log_status, firbaseID);
                        tagList.add(tags);
                        //Log.d(TAG, "setData: " + real_name);
                    }

                    pDialog.cancel();
                    mAdapter.notifyDataSetChanged();
                    //mAdapter = new ListEmployeeAdapter(tagList,mContext);
                    //mRecycleView.setAdapter(mAdapter);

                } else {
                    pDialog.cancel();

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
                pDialog.cancel();
            }

        }, error -> {
            Log.d("DEBUG", "onErrorResponse: " + error);
            pDialog.cancel();

        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("searchid", searchid);
                MyData.put("searchby", searchby);
                MyData.put("division", mDivision);
                MyData.put("offsett", page);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Write your logic here
                this.finish();
                return true;

            case R.id.action_search_Name:
                dialogSearch("6", "Search by Name", "Search by Name");
                return true;

            case R.id.action_search_division:
                if (mLevelUser.equals("0")) {
                    dialogSearch("3", "Search by Division", "Search by Division");
                } else {
                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry, you can't search by division", TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> {

                    });
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();
                }
                return true;

            case R.id.action_search_email:
                dialogSearch("2", "Search by Email", "Search by Email");

                return true;

            case R.id.action_search_employment:
                if (mLevelUser.equals("0")) {
                    dialogSearch("5", "Search by Employment", "Search by Employment");
                } else {
                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry, you can't search by employment", TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> {

                    });
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();
                }
                return true;

            case R.id.action_search_status:
                if (mLevelUser.equals("0")) {
                    dialogSearch("4", "Search by Status", "Search by Status");
                } else {
                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Sorry, you can't search by employee status", TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> {

                    });
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();
                }
                return true;

            case R.id.action_search_clear:
                tagList.clear();
                mAdapter.notifyDataSetChanged();
                scrollListener.resetState();
                firstpage = 10;
                getData("1", "", "0");

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search_employee, menu);
        return true;
    }

    private void dialogSearch(String searchid, String hint, String title) {
        final EditText searchby = new EditText(ListEmployeeActivity.this);
        searchby.setHint(hint);

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(searchby);


        new AlertDialog.Builder(ListEmployeeActivity.this)
                .setTitle(title)
                .setMessage("Please type your data as field requested")
                .setView(layout)
                .setPositiveButton("SUBMIT", (dialog, which) -> {

                    String search = searchby.getText().toString();
                    getData(searchid, search, "0");


                }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
                }).show();
    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("notify");
            Log.d("DEBUG", "onReceive1: " + message);
            if (message != null) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, message, TSnackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
                if (message.equals("Updating success")) {
                    getData("1", "", "0");
                }
            }
        }
    };

    @Override
    protected void onPostResume() {
        super.onPostResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("admins"));

        //tagList.clear();
        //mAdapter.notifyDataSetChanged();
        scrollListener.resetState();
        firstpage = 10;
        getData("1", "", "0");

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);

    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                try {
                    clicklistener.onClick(child, rv.getChildAdapterPosition(child));
                } catch (NoSuchFieldException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public interface ClickListener {
        void onClick(View view, int position) throws NoSuchFieldException, IllegalAccessException;

        void onLongClick(View view, int position);
    }

}
