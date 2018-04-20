package com.lesjaw.wamonitoring.ui.fragment;

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

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.AbsenPulangAdapter;
import com.lesjaw.wamonitoring.model.AbsenceModel;
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


public class LogAbsencePulang extends android.support.v4.app.Fragment {

    private SharedPreferences sharedPref;
    private static final String TAG = "LogAbsencePulang";
    private List<AbsenceModel> tagList = new ArrayList<>();
    private AbsenPulangAdapter mAdapter;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10;
    //private StyleableToast styleableToast;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return  inflater.inflate(R.layout.frag_log_alert, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        Context mContext = getActivity();
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new AbsenPulangAdapter(tagList, mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        getData("0");

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d(TAG, "last position and firtpage is " + firstpage);

                getData(String.valueOf(firstpage));
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
    }

    private void setRefresh() {
        getData("0");
    }

    private void getData(String page) {
        mSwipeRefreshLayout.setRefreshing(true);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        //mLevelUser = mPrefHelper.getLevelUser();

        String mCompanyID;
        String mDivision = sharedPref.getString("division", "olmatix1");

        PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());
        String mLevelUser = mPrefHelper.getLevelUser();
        String url;
        if(mLevelUser.equals("4")){
            mCompanyID = mPrefHelper.getGroup();
            url = Config.DOMAIN+"wamonitoring/get_absen_out_group.php";
        }else{
            mCompanyID = sharedPref.getString("company_id", "olmatix1");
            url = Config.DOMAIN+"wamonitoring/get_absen_out.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

            try {
                JSONObject jsonResponse = new JSONObject(response);
                String result_code = jsonResponse.getString("success");
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
            Log.d("GROUP_response",response);
        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("offsett", page);
                Log.d("GROUP_mCompanyID",mCompanyID);
                Log.d("GROUP_mDivision",mDivision);
                Log.d("GROUP_page",page);
                Log.d("GROUP_url",url);

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

}