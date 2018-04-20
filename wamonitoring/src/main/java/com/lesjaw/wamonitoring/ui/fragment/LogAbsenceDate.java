package com.lesjaw.wamonitoring.ui.fragment;

import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.AbsenPulangAdapter;
import com.lesjaw.wamonitoring.model.AbsenceModel;
import com.lesjaw.wamonitoring.model.DivisionAndID;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.EndlessRecyclerViewScrollListener;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LogAbsenceDate extends android.support.v4.app.Fragment {

    private SharedPreferences sharedPref;
    private static final String TAG = "LogAbsenceDate";
    private List<AbsenceModel> tagList = new ArrayList<>();
    private AbsenPulangAdapter mAdapter;
    private String times;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10;
    private TextView mTgl;
    private DatePickerDialog datePickerDialog;
    private String divitem;
    private Spinner employee;
    String mLevelUser,mDivision;
    public LogAbsenceDate() {
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.frag_log_absen_date, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());

        Context mContext = getActivity();
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);
        mTgl = (TextView) view.findViewById(R.id.tgl);
        employee = (Spinner) view.findViewById(R.id.spinnerEmployee);

        mAdapter = new AbsenPulangAdapter(tagList, mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        mLevelUser = mPrefHelper.getLevelUser();
        mDivision = sharedPref.getString("division", "olmatix1");

        if (!mLevelUser.equals("0") && !mLevelUser.equals("4")) {
            employee.setEnabled(false);
        }

        Calendar now = Calendar.getInstance();
        int mYear = now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mYear, mMonth, mDay);
        SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        mTgl.setText(timeformat.format(Calendar.getInstance().getTime()));
        times = mTgl.getText().toString();

        //getData("0", times, mDivision);

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d(TAG, "last position and firtpage is " + firstpage);
                times = mTgl.getText().toString();

                getData(String.valueOf(firstpage), times, divitem);
                firstpage = firstpage + 10;
            }
        };
        mRecycleView.addOnScrollListener(scrollListener);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

            }
        };

        mSwipeRefreshLayout.setRefreshing(true);

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            tagList.clear();
            mAdapter.notifyDataSetChanged();
            scrollListener.resetState();
            firstpage = 10;
            setRefresh();
        });

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        mTgl.setOnClickListener(view1 -> datePickerDialog.show());

        GetDivision();

        employee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                divitem = employee.getSelectedItem().toString();
                int divID = Integer.parseInt(String.valueOf(((DivisionAndID) parent.getSelectedItem()).getId()));
                if (!divitem.equals("ALL")) {
                    getData("0", mTgl.getText().toString(), String.valueOf(divID));
                }

                if (divitem.equals("ALL")) {
                    divitem = "none";
                    getData("0", mTgl.getText().toString(), divitem);
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        divitem = mDivision;

    }

    private void setRefresh() {
        getData("0", mTgl.getText().toString(), sharedPref.getString("division", "olmatix1"));
    }

    private void getData(String page, String times, String mDivision) {
        mSwipeRefreshLayout.setRefreshing(true);

        String mCompanyID;
        String url;

        if(mLevelUser.equals("4")){
            mCompanyID = new PreferenceHelper(getContext()).getGroup();
            url = Config.DOMAIN + "wamonitoring/get_absen_date_byGroup.php";
        }else{
            mCompanyID = sharedPref.getString("company_id", "olmatix1");
            url = Config.DOMAIN + "wamonitoring/get_absen_date.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("absen");

                    if (page.equals("0")) {
                        tagList.clear();
                    }

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String email = tags_name.getString("email");
                        String date_created_masuk = tags_name.getString("date_created_masuk");
                        String date_created_pulang = tags_name.getString("date_created_pulang");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");
                        String working_gps = tags_name.getString("working_gps");
                        String log_status = tags_name.getString("log_status");
                        String phone1 = tags_name.getString("phone1");
                        String phone2 = tags_name.getString("phone2");
                        String range_loc = tags_name.getString("range_loc");

                        AbsenceModel tags = new AbsenceModel(employee_name, email,
                                date_created_masuk, date_created_pulang, latitude, longitude, working_gps, log_status,
                                phone1, phone2,range_loc);

                        tagList.add(tags);


                    }
                    mAdapter.notifyDataSetChanged();

                    mSwipeRefreshLayout.setRefreshing(false);


                } else {
                    mSwipeRefreshLayout.setRefreshing(false);

                }

            } catch (JSONException e) {
                e.printStackTrace();
                mSwipeRefreshLayout.setRefreshing(false);
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("date", times);
                MyData.put("offsett", page);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegistrationBroadcastReceiver);

    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.SEND_NOTICE));
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private LogDaily.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final LogDaily.ClickListener clicklistener) {

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

    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(getContext(),
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

                    String date = year1 + "-" + mMonth + "-" + daymonth;
                    mTgl.setText(date);
                    times = mTgl.getText().toString();

                    divitem = employee.getSelectedItem().toString();
                    int divID = Integer.parseInt(String.valueOf(((DivisionAndID) employee.getSelectedItem()).getId()));
                    if (!divitem.equals("ALL")) {
                        getData("0", mTgl.getText().toString(), String.valueOf(divID));
                    }

                    if (divitem.equals("ALL")) {
                        divitem = "none";
                        getData("0", mTgl.getText().toString(), divitem);
                    }

                }, year, month, dayOfMonth);
    }

    private void GetDivision() {

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());

            String mCompanyID;

            RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());

            String url;
            if(mLevelUser.equals("4")){
                mCompanyID = mPrefHelper.getGroup();
                url = "https://olmatix.com/wamonitoring/get_division_group.php";
            }else{
                mCompanyID = sharedPref.getString("company_id", "olmatix1");
                url = "https://olmatix.com/wamonitoring/get_division.php";
            }

            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    mPrefHelper.setDivisionFull(String.valueOf(response));

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    if (result_code.equals("1")) {
                        ArrayList<DivisionAndID> aName;
                        if (mLevelUser.equals("0") || mLevelUser.equals("4")) {
                            String sDivision = mPrefHelper.getDivisionFull();
                            JSONObject jsonResponse1;
                            try {
                                jsonResponse1 = new JSONObject(sDivision);
                                JSONArray cast1 = jsonResponse1.getJSONArray("division");
                                aName = new ArrayList<>();
                                aName.clear();
                                aName.add(new DivisionAndID("ALL", "1"));

                                for (int i = 0; i < cast1.length(); i++) {
                                    JSONObject div_name = cast1.getJSONObject(i);
                                    String name = div_name.getString("name");
                                    String cid = div_name.getString("cid");
                                    aName.add(new DivisionAndID(name, cid));
                                }

                                ArrayAdapter<DivisionAndID> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_black, aName);

                                Log.d("SPINNER",aName.toString());
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                                employee.setAdapter(spinnerArrayAdapter);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        } else {
                            aName = new ArrayList<>();
                            aName.clear();
                            aName.add(new DivisionAndID(mPrefHelper.getDivName(),mDivision));

                            ArrayAdapter<DivisionAndID> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_black, aName);
                            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                            employee.setAdapter(spinnerArrayAdapter);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
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

    }


}