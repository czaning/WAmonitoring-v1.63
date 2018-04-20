package com.lesjaw.wamonitoring.ui.fragment;

import android.Manifest;
import android.app.ActivityOptions;
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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListAlertAdapter;
import com.lesjaw.wamonitoring.model.tagsAlertModel;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
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

import de.hdodenhof.circleimageview.CircleImageView;


public class LogAlert extends android.support.v4.app.Fragment {

    private SharedPreferences sharedPref;
    private static final String TAG = "LogAlert";
    private List<tagsAlertModel> tagList = new ArrayList<>();
    private ListAlertAdapter mAdapter;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10;

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            //Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(getContext(), "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return  inflater.inflate(R.layout.frag_log_alert, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new TedPermission(getContext())
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CALL_PHONE)
                .check();
        mSwipeRefreshLayout = (SwipeRefreshLayout)view.findViewById(R.id.swipeRefreshLayout);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        Context mContext = getActivity();
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new ListAlertAdapter(tagList, mContext);
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
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                getData(String.valueOf(firstpage));
                firstpage = firstpage + 10;
            }
        };
        mRecycleView.addOnScrollListener(scrollListener);

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                Log.d(TAG, "onReceive: " + intent);
                /*if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    *//*String message = intent.getStringExtra("message");
                    String user = intent.getStringExtra("user");

                    Log.d(TAG, "onReceive: "+user+" "+mUserName);
                    if (!user.equals(mUserName)) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, message+" by "+user, TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#e6ffe6"));
                        snackbar.setAction("OK", v -> {

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    }*//*
                }*/

                if (intent.getAction().equals(Config.SEND_NOTICE)) {
                    // new push notification is received

                    mSwipeRefreshLayout.setRefreshing(true);
                    setRefresh();
                }
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
                /*String EmployeName = tagList.get(position).getEmployee_name();
                String lat = tagList.get(position).getLatitude();
                String lot = tagList.get(position).getLongitude();
                String tgl = tagList.get(position).getDate_created();*/

                //int type = tagList.get(position).getType();

                CircleImageView imgProfile = (CircleImageView) view.findViewById(R.id.img_profile) ;
                imgProfile.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), EmployeeProfile.class);
                    i.putExtra("email", tagList.get(position).getEmail());
                    String transitionName = getString(R.string.imgProfile);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imgProfile, transitionName);
                    startActivity(i, transitionActivityOptions.toBundle());
                });




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
        PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());
        String mLevelUser = mPrefHelper.getLevelUser();

        String mCompanyID;
        if(mLevelUser.equals("4")){
            mCompanyID = mPrefHelper.getGroup();
        }else{
            mCompanyID = sharedPref.getString("company_id", "olmatix1");
        }
        String mDivision = sharedPref.getString("division", "olmatix1");

        String url;
        if(mLevelUser.equals("4")){
            // jika user nya group head, gambil log alert nya by group
            url = Config.DOMAIN+"wamonitoring/get_log_alert_byGroup.php";
        }else{
            url = Config.DOMAIN+"wamonitoring/get_log_alert.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("alert");

                    if (page.equals("0")) {
                        tagList.clear();
                    }

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String email = tags_name.getString("email");
                        String division = tags_name.getString("division");
                        String divID = tags_name.getString("div_id");
                        String company_id = tags_name.getString("company_id");
                        String notes = tags_name.getString("notes");
                        String date_created = tags_name.getString("date_created");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");
                        String lokasi = tags_name.getString("lokasi");
                        String phone1 = tags_name.getString("phone1");
                        String phone2 = tags_name.getString("phone2");
                        String type = tags_name.getString("type");
                        String panic_id = tags_name.getString("panic_id");
                        String comment = tags_name.getString("comment");

                        int tipe = Integer.parseInt(type);

                        tagsAlertModel tags = new tagsAlertModel(email, employee_name, company_id,
                                division, date_created, notes, latitude, longitude,lokasi,phone1,phone2,tipe, panic_id, comment);
                        //Log.d(TAG, "getData LogALERT: "+mDivision+" : "+division);

                        if (mDivision.equals("none")){
                            tagList.add(tags);
                        } else if (mDivision.equals(divID) || (division.equals("none"))||(division.equals(""))||(division.equals("null"))) {
                            tagList.add(tags);
                        }
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

        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
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
        getData("0");
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