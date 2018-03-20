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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListChecklistAdapter;
import com.lesjaw.wamonitoring.model.tagsChecklistModel;
import com.lesjaw.wamonitoring.ui.CommentChecklistActivity;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class LogChecklist extends android.support.v4.app.Fragment {

    private SharedPreferences sharedPref;
    private static final String TAG = "LogAlert";
    private List<tagsChecklistModel> tagList = new ArrayList<>();
    private ListChecklistAdapter mAdapter;
    private String mUserName;
    private String mEmail;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10;
    boolean requestViewByEmail = false;
    TextView tglFormat;

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
        Bundle bundle = this.getArguments();
        mEmail = bundle.getString("message");
        requestViewByEmail = !mEmail.equals("none");
        return inflater.inflate(R.layout.frag_log_alert, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new TedPermission(getContext())
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("If you reject permission,you can not use this service\n\nPlease turn on permissions at [Setting] > [Permission]")
                .setPermissions(Manifest.permission.CALL_PHONE)
                .check();
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        Context mContext = getActivity();
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new ListChecklistAdapter(tagList, mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);
        mUserName = sharedPref.getString("real_name", "jakarta");

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

                if (intent.getAction().equals(Config.SEND_NOTICE)) {
                    // new push notification is received

                    mSwipeRefreshLayout.setRefreshing(true);
                    setRefresh();
                }
            }
        };

        mSwipeRefreshLayout.setRefreshing(true);

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                String UserEmail = sharedPref.getString("email", "olmatix1");

                //String EmployeName = tagList.get(position).getEmployee_name();
                //String mEmail = tagList.get(position).getEmail();

                String tcrid = tagList.get(position).getTcrid();
                String checklist = tagList.get(position).getChecklist();
                String checklistNote = tagList.get(position).getChecklist_note();

                tglFormat = (TextView) view.findViewById(R.id.tgl);

                CircleImageView imgProfile = (CircleImageView) view.findViewById(R.id.img_profile) ;
                imgProfile.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), EmployeeProfile.class);
                    i.putExtra("email", tagList.get(position).getEmail());
                    String transitionName = getString(R.string.imgProfile);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imgProfile, transitionName);
                    startActivity(i, transitionActivityOptions.toBundle());
                });
                CardView cardView = (CardView) view.findViewById(R.id.cv);
                cardView.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), CommentChecklistActivity.class);
                    i.putExtra("email", tagList.get(position).getEmail());
                    i.putExtra("tcrid", tagList.get(position).getTcrid());
                    i.putExtra("employee_name", tagList.get(position).getEmployee_name());
                    i.putExtra("tag_name", tagList.get(position).getTagname());
                    i.putExtra("division", tagList.get(position).getDivision());
                    i.putExtra("note", tagList.get(position).getChecklist_note());
                    i.putExtra("checklist", tagList.get(position).getChecklist());
                    i.putExtra("tgl", tglFormat.getText().toString());

                    String transitionName = getString(R.string.imgProfile);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imgProfile, transitionName);
                    startActivity(i, transitionActivityOptions.toBundle());

                });

                tglFormat.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                        .setTitle("Checklist action")
                        .setIcon(R.drawable.ic_info_black_24dp)
                        .setMessage("What do you want to do with this "+checklist+ " \nnote : "+checklistNote+"? \n\nIf you click Mark OK, " +
                                "this checklist note will not shown here anymore")
                        .setPositiveButton("Mark OK", (dialog, id) -> {

                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH);
                            String tgl = df.format(Calendar.getInstance().getTime());

                            SimpleDateFormat df1 = new SimpleDateFormat("HH:mm:ss",Locale.ENGLISH);
                            String jam = df1.format(Calendar.getInstance().getTime());

                            String url = Config.DOMAIN+"wamonitoring/update_checklist_note_mark.php";

                            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {


                                JSONObject jObject = null;
                                try {
                                    jObject = new JSONObject(response);
                                    String result_json = jObject.getString("message");
                                    String result_code = jObject.getString("success");

                                    Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

                                    if (result_code.equals("1")) {
                                        getData("0");
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                                protected Map<String, String> getParams() {
                                    Map<String, String> MyData = new HashMap<>();
                                    MyData.put("tcrid", tcrid);
                                    MyData.put("date_update_tgl", tgl);
                                    MyData.put("date_update_jam", jam);
                                    MyData.put("updater", UserEmail);

                                    return MyData;
                                }
                            };

                            // Adding request to request queue
                            NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

                        })

                        .setNegativeButton("Comment", (dialog, id) -> {

                            final EditText pass = new EditText(getContext());
                            pass.setHint("Type your comment");
                            pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            pass.setMaxLines(1);

                            LinearLayout layout = new LinearLayout(getContext());
                            layout.setOrientation(LinearLayout.VERTICAL);
                            layout.addView(pass);

                            new AlertDialog.Builder(getContext())
                                    .setTitle("Checklist comment")
                                    .setIcon(R.drawable.ic_info_black_24dp)
                                    .setView(layout)
                                    .setPositiveButton("Send", (dialog12, id12) -> {

                                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                                        String tgl = df.format(Calendar.getInstance().getTime());

                                        String url = Config.DOMAIN+"wamonitoring/insert_comment_checklist.php";

                                        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

                                            JSONObject jObject = null;
                                            try {
                                                jObject = new JSONObject(response);
                                                String result_json = jObject.getString("message");
                                                String result_code = jObject.getString("success");

                                                Log.d(TAG, "result_json: "+result_json +"result_code: "+result_code);

                                                if (result_code.equals("1")) {
                                                    getData("0");
                                                }

                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }

                                        }, error -> Log.d(TAG, "onErrorResponse: "+error)) {
                                            protected Map<String, String> getParams() {
                                                Map<String, String> MyData = new HashMap<>();
                                                MyData.put("tcrid", tcrid);
                                                MyData.put("date_update", tgl);
                                                MyData.put("from_email", UserEmail);
                                                MyData.put("comment", pass.getText().toString().trim());
                                                MyData.put("employee_name", mUserName);

                                                return MyData;
                                            }
                                        };

                                        // Adding request to request queue
                                        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

                                    })

                                    .setNegativeButton("Cancel", (dialog1, id1) -> dialog1.dismiss())

                                    .show();

                            dialog.dismiss();
                        })

                        .show());


            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            tagList.clear();
            mAdapter.notifyDataSetChanged();
            scrollListener.resetState();
            firstpage = 10;
            setRefresh();
        });

    }

    private void setRefresh() {
        getData("0");
    }


    private void getData(String page) {
        mSwipeRefreshLayout.setRefreshing(true);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mDiv = sharedPref.getString("division", "olmatix1");

        String mLevelUser = mPrefHelper.getLevelUser();


        String url;
        if (!requestViewByEmail) {
            if (mLevelUser.equals("0")){
                url = Config.DOMAIN + "wamonitoring/get_checklist_data_note.php";
            } else {
                url = Config.DOMAIN + "wamonitoring/get_checklist_data_note_div.php";
            }
        } else {
            url = Config.DOMAIN + "wamonitoring/get_checklist_data_note_email.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    if (page.equals("0")) {
                        tagList.clear();
                    }

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String tcrid = tags_name.getString("tcrid");
                        String checklist = tags_name.getString("checklist");
                        String checklist_status = tags_name.getString("checklist_status");
                        String checklist_note = tags_name.getString("checklist_note");
                        String trid = tags_name.getString("trid");
                        String tid = tags_name.getString("tid");
                        String tagname = tags_name.getString("tagname");
                        String division = tags_name.getString("division");
                        String employee_name = tags_name.getString("employee_name");
                        String email = tags_name.getString("email");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String comment = tags_name.getString("comment");

                        int tipe = 0;

                        tagsChecklistModel tags = new tagsChecklistModel(tcrid, checklist, checklist_status,
                                checklist_note, trid, tid, tagname, division, employee_name, email, tgl, jam, tipe,comment);

                        tagList.add(tags);

                        //Log.d(TAG, "setDataChecklist: " + employee_name);

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
                MyData.put("division", mDiv);
                MyData.put("offsett", page);
                MyData.put("email", mEmail);

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