package com.lesjaw.wamonitoring.ui.fragment;

import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.github.lzyzsd.circleprogress.ArcProgress;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListNoticeAdapter;
import com.lesjaw.wamonitoring.adapter.UserMainAdapter;
import com.lesjaw.wamonitoring.model.EmployeeModel;
import com.lesjaw.wamonitoring.model.tagsAlertModel;
import com.lesjaw.wamonitoring.ui.FragmentContainer;
import com.lesjaw.wamonitoring.ui.ListEmployeeActivity;
import com.lesjaw.wamonitoring.ui.ListTagsActivity;
import com.lesjaw.wamonitoring.ui.NoticeDetail;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.EndlessRecyclerViewScrollListener;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class Main extends android.support.v4.app.Fragment {

    private static final String TAG = "Main";
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private List<tagsAlertModel> tagList = new ArrayList<>();
    private List<EmployeeModel> tagList1 = new ArrayList<>();

    private UserMainAdapter mAdapterUser;
    private ListNoticeAdapter mAdapter;
    private int firstpage = 10, firstpageUser = 10;
    private Double usercount, tagcount;
    private int packType;
    private ArcProgress arcProgress1, arcProgress2;
    private String mBirth;
    private String mCompanyAddress;
    private String mEmployeeAddress;
    private String mUser;
    private String mCompanyID;
    private String mPhone1;
    private String mPhone2;
    private String placeB;
    private String dateB;
    private String mUserName;
    private String mLevelUser;
    private String mDivision, date_created_masuk = "null", date_created_pulang, latitude = "0", longitude = "0", working_gps = "0.0,0.0";
    private boolean editdata = false;
    private EditText eDateBirth;
    private DatePickerDialog datePickerDialog;
    private TextView tagsThisMonth, tagsToday, Checklist, labellasttag, lLogin, lLogout;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private StyleableToast styleableToast;
    private Timer autoUpdate;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.frag_main, container, false);

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        arcProgress1 = (ArcProgress) view.findViewById(R.id.arc_progress1);
        arcProgress2 = (ArcProgress) view.findViewById(R.id.arc_progress2);
        tagsThisMonth = (TextView) view.findViewById(R.id.TotalMonthTags1);
        TextView companyname1 = (TextView) view.findViewById(R.id.labelpt);

        tagsToday = (TextView) view.findViewById(R.id.TotalDayTags1);
        Checklist = (TextView) view.findViewById(R.id.TotalMonthCheclist1);
        labellasttag = (TextView) view.findViewById(R.id.labellasttag);
        lLogin = (TextView) view.findViewById(R.id.onlineTxt);
        lLogout = (TextView) view.findViewById(R.id.offlineTxt);
        CardView tagthisMonth = (CardView) view.findViewById(R.id.CVTotalMonthTags);
        CardView tagToday = (CardView) view.findViewById(R.id.CVTotalDayTags);
        CardView Checklist = (CardView) view.findViewById(R.id.CVTotalMonthCheclist);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        Context mContext = getActivity();
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        RecyclerView mRecycleViewUser = (RecyclerView) view.findViewById(R.id.rvUser);
        mRecycleViewUser.setHasFixedSize(true);

        mAdapter = new ListNoticeAdapter(tagList, mContext);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        SnapHelper snapHelper = new PagerSnapHelper();
        mRecycleView.setLayoutManager(layoutManager);
        snapHelper.attachToRecyclerView(mRecycleView);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        mAdapterUser = new UserMainAdapter(tagList1, mContext);
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        mRecycleViewUser.setLayoutManager(layoutManager1);
        //snapHelper.attachToRecyclerView(mRecycleViewUser);
        mRecycleViewUser.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewUser.setAdapter(mAdapterUser);

        mBirth = sharedPref.getString("birth", "none");
        mCompanyAddress = sharedPref.getString("company_address", "jakarta");
        mEmployeeAddress = sharedPref.getString("employee_address", "jakarta");
        mUser = sharedPref.getString("email", "jakarta");
        mPhone1 = sharedPref.getString("phone1", "jakarta");
        mPhone2 = sharedPref.getString("phone2", "jakarta");

        mUserName = sharedPref.getString("real_name", "jakarta");
        String companyname = sharedPref.getString("company_name", "jakarta");
        mDivision = sharedPref.getString("division", "jakarta");

        mLevelUser = mPrefHelper.getLevelUser();

        Log.d(TAG, "onViewCreated: "+mLevelUser);

        companyname1.setText(companyname);

        labellasttag.setOnClickListener(v -> itungAbsen());

        packType = Integer.parseInt(mPrefHelper.getPackage());

        Calendar now = Calendar.getInstance();
        int mYear = now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mYear, mMonth, mDay);
        getData("0");
        getDataUser("1", "", "0");

        EndlessRecyclerViewScrollListener scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d(TAG, "last position and firtpage NOTICE is " + firstpage);
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                getData(String.valueOf(firstpage));
                firstpage = firstpage + 10;
            }
        };
        mRecycleView.addOnScrollListener(scrollListener);

        EndlessRecyclerViewScrollListener scrollListenerUser = new EndlessRecyclerViewScrollListener(layoutManager1) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                //Log.d(TAG, "last position and firtpage USER is " + firstpageUser);
                getDataUser("1", "", String.valueOf(firstpageUser));
                firstpageUser = firstpageUser + 10;
            }
        };
        mRecycleViewUser.addOnScrollListener(scrollListenerUser);

        if (TextUtils.isEmpty(mBirth) || mBirth.equals("none")) {
            //Toast.makeText(MainActivity.this, "Birth Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        } else {
            String[] splitmBirth = mBirth.split(",");
            placeB = splitmBirth[0];
            dateB = splitmBirth[1].trim();

        }
        if (TextUtils.isEmpty(mCompanyAddress) || mCompanyAddress.equals("none")) {
            //Toast.makeText(MainActivity.this, "Company Address Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        }
        if (TextUtils.isEmpty(mEmployeeAddress) || mEmployeeAddress.equals("none")) {
            //Toast.makeText(MainActivity.this, "Employee Address Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        }
        if (TextUtils.isEmpty(mPhone1) || mPhone1.equals("none")) {
            //Toast.makeText(MainActivity.this, "Employee Address Empty", Toast.LENGTH_SHORT).show();
            editdata = true;
        }

        if (editdata) {
            editData();
        }

        labellasttag.setText(mPrefHelper.getLastLog());

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String type = intent.getStringExtra("type");
                    //String message = intent.getStringExtra("message");
                    String user = intent.getStringExtra("user");
                    //String time = intent.getStringExtra("timestamp");

                    if (!user.equals(mUserName)) {

                        switch (type) {
                            case "0":
                                break;
                            case "1":
                                break;
                            case "4":
                                break;
                            case "2":
                                if (autoUpdate != null) {
                                    autoUpdate.cancel();
                                }
                                labellasttag.setText(mPrefHelper.getLastLog());
                                getTagThisMonth();
                                getTagToday();
                                getChecklist();

                                new Handler(Looper.getMainLooper()).postDelayed(() -> refreshHeader(), 5000L);

                                break;
                            case "8":
                                if (mPrefHelper.getLogin().equals("1")) {
                                    getAbsence("1");
                                } else {
                                    getAbsence("2");
                                }

                                break;
                        }

                    }
                }
            }
        };

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                String EmployeName = tagList.get(position).getEmployee_name();
                String imageUrl = tagList.get(position).getLokasi();
                String Email = tagList.get(position).getEmail();
                String Note = tagList.get(position).getNotes();
                String Tgl = tagList.get(position).getDate_created();
                String id = tagList.get(position).getPanic_id();


                CircleImageView imgProfile = (CircleImageView) view.findViewById(R.id.img_profile);
                ImageView imgView = (ImageView) view.findViewById(R.id.imgview);
                TextView textView = (TextView) view.findViewById(R.id.notes);

                Intent i = new Intent(getActivity(), NoticeDetail.class);
                i.putExtra("email", Email);
                i.putExtra("EmployeName", EmployeName);
                i.putExtra("imageUrl", imageUrl);
                i.putExtra("tgl", Tgl);
                i.putExtra("note", Note);
                i.putExtra("id", id);

                String transitionName1 = getString(R.string.imgProfile);
                String transitionName2 = getString(R.string.imgView);
                String transitionName3 = getString(R.string.textview);

                Pair<View, String> p1 = Pair.create(imgProfile, transitionName1);
                Pair<View, String> p2 = Pair.create(imgView, transitionName2);
                Pair<View, String> p3 = Pair.create(textView, transitionName3);
                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), p2, p1, p3);
                startActivity(i, options.toBundle());
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        mRecycleViewUser.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleViewUser, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {


            }

            @Override
            public void onLongClick(View view, int position) {
            }

        }));


        tagthisMonth.setOnClickListener(v -> {
            TextView text = (TextView) view.findViewById(R.id.TotalMonthTags);

            Intent i = new Intent(getActivity(), FragmentContainer.class);
            i.putExtra("id_fragement", "LogDaily");
            String transitionName = getString(R.string.imgProfile);

            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), text, transitionName);
            startActivity(i, transitionActivityOptions.toBundle());
        });

        tagToday.setOnClickListener(v -> {
            TextView text = (TextView) view.findViewById(R.id.TotalMonthTags);

            Intent i = new Intent(getActivity(), FragmentContainer.class);
            i.putExtra("id_fragement", "LogDaily");
            String transitionName = getString(R.string.imgProfile);

            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), text, transitionName);
            startActivity(i, transitionActivityOptions.toBundle());
        });

        Checklist.setOnClickListener(v -> {

            TextView text = (TextView) view.findViewById(R.id.TotalMonthCheclist);

            Intent i = new Intent(getActivity(), FragmentContainer.class);
            i.putExtra("id_fragement", "Checklist");
            String transitionName = getString(R.string.imgProfile);

            ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), text, transitionName);
            startActivity(i, transitionActivityOptions.toBundle());
        });

        arcProgress1.setOnClickListener(v -> {
            if (!mLevelUser.equals("2")) {
                Intent i = new Intent(getContext(), ListEmployeeActivity.class);
                startActivity(i);
            } else {
                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf("Your team are " + arcProgress1.getProgress() + " peoples"))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
        });

        arcProgress2.setOnClickListener(v -> {
            if (!mLevelUser.equals("2")) {
                Intent i = new Intent(getContext(), ListTagsActivity.class);
                startActivity(i);
            } else {
                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf("You have " + arcProgress2.getProgress() + " tags"))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
        });

        if (mPrefHelper.getLogin().equals("1")) {
            getAbsence("1");
        } else {
            getAbsence("2");
        }

    }

    public void refreshHeader() {
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(() -> itungAbsen());
            }
        }, 100, 1000L); // updates GUI each 40 secs
    }

    private void getData(String page) {

        Log.d(TAG, "getData Notice: ");
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        String url;
        if (mLevelUser.equals("4")){
            mCompanyID = mPrefHelper.getGroup();
             url = Config.DOMAIN + "wamonitoring/get_log_notice_sa.php";

        } else {
            mCompanyID = sharedPref.getString("company_id", "olmatix1");

            url = Config.DOMAIN + "wamonitoring/get_log_notice.php";
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
                    //Log.d(TAG, "getData: "+cast);
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String email = tags_name.getString("email");
                        String division = tags_name.getString("division");
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
                                division, date_created, notes, latitude, longitude, lokasi, phone1, phone2, tipe, panic_id, comment);

                        if (tipe == 1) {

                            //Log.d(TAG, "getData: "+mDivision+" : "+division);

                            if (mDivision.equals("none")) {
                                tagList.add(tags);
                            } else if (mDivision.equals(division) || (division.equals("none"))||(division.equals(""))||(division.equals("null"))) {
                                tagList.add(tags);
                            }
                        }

                    }
                    mAdapter.notifyDataSetChanged();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
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

    private void GetUsers() {
        String url;

        //String tag_json_obj = TAG;
        String mDivision = sharedPref.getString("division", "olmatix1");

        if (mPrefHelper.getLevelUser().equals("0")) {
            url = Config.DOMAIN + "wamonitoring/get_count_users.php";
        } else if(mPrefHelper.getLevelUser().equals("4")) {
            url = Config.DOMAIN + "wamonitoring/get_count_users_bygroup.php";
        }else {
            url = Config.DOMAIN + "wamonitoring/get_count_users_bydiv.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    //Log.d(TAG, "doInBackground: user count " + result_message);
                    if (mPrefHelper.getLevelUser().equals("0")) {
                        mPrefHelper.setUserCount(result_message);
                    } else {
                        mPrefHelper.setUserCountDiv(result_message);
                    }

                } else {
                    mPrefHelper.setUserCount("0");
                }

                if (mPrefHelper.getLevelUser().equals("0")) {
                    usercount = Double.valueOf(mPrefHelper.getUserCount());
                } else {
                    usercount = Double.valueOf(mPrefHelper.getUserCountDiv());
                }
                Double setProg = null;
                int setProgInt = 0;
                if (packType == 1) {
                    arcProgress1.setMax(10);
                    setProg = (double) Math.round((usercount / 10) * 10);
                    setProgInt = setProg.intValue();
                }

                if (packType == 2) {
                    arcProgress1.setMax(100);
                    setProg = (double) Math.round((usercount / 100) * 100);
                    setProgInt = setProg.intValue();
                }

                if (packType == 3) {
                    arcProgress1.setMax(150);
                    setProg = (double) Math.round((usercount / 150) * 150);
                    setProgInt = setProg.intValue();
                }
                Log.d(TAG, "onPostExecute: " + packType);
                if (packType >= 4) {
                    arcProgress1.setMax(5000);
                    setProg = (double) Math.round((usercount / 5000) * 5000);
                    setProgInt = setProg.intValue();
                }
                arcProgress1.setProgress(setProgInt);

                GetTags();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
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

    private void GetTags() {
        String url;
        String mDivision = sharedPref.getString("division", "olmatix1");

        if (mPrefHelper.getLevelUser().equals("0")) {
            url = Config.DOMAIN + "wamonitoring/get_count_tags.php";
        } else if(mPrefHelper.getLevelUser().equals("4")){
            url = Config.DOMAIN + "wamonitoring/get_count_tags_bygroup.php";
        }else {
            url = Config.DOMAIN + "wamonitoring/get_count_tags_bydiv.php";
        }
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_message = jObject.getString("message");
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {
                    Log.d(TAG, "doInBackground: tags count " + result_message);
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
                if (packType == 1) {
                    arcProgress2.setMax(5);
                    setProg1 = (double) Math.round((tagcount / 5) * 5);
                    setProgInt1 = setProg1.intValue();
                }

                if (packType == 2) {
                    arcProgress2.setMax(50);
                    setProg1 = (double) Math.round((tagcount / 50) * 50);
                    setProgInt1 = setProg1.intValue();
                }

                if (packType == 3) {
                    arcProgress2.setMax(150);
                    setProg1 = (double) Math.round((tagcount / 150) * 150);
                    setProgInt1 = setProg1.intValue();
                }

                if (packType >= 4) {
                    arcProgress2.setMax(5000);
                    setProg1 = (double) Math.round((tagcount / 5000) * 5000);
                    setProgInt1 = setProg1.intValue();
                }
                arcProgress2.setProgress(setProgInt1);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
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

    private void editData() {
        EditText ePlaceBirth = new EditText(getActivity());
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

        EditText eCompanyAddress = new EditText(getActivity());
        eCompanyAddress.setHint("Company Address");
        if (!mCompanyAddress.equals("none")) {
            eCompanyAddress.setText(mCompanyAddress);
        }
        eCompanyAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eCompanyAddress.setMaxLines(1);

        EditText eEmployeeAddress = new EditText(getContext());
        eEmployeeAddress.setHint("Employee Address");
        if (!mEmployeeAddress.equals("none")) {
            //Log.d(TAG, "editData: mEmployeeAddress " + mEmployeeAddress);
            eEmployeeAddress.setText(mEmployeeAddress);
        }
        eEmployeeAddress.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
        eEmployeeAddress.setMaxLines(1);

        EditText ePhone1 = new EditText(getActivity());
        ePhone1.setHint("Phone Number 1");
        if (!mPhone1.equals("none")) {
            ePhone1.setText(mPhone1);
        }
        ePhone1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        ePhone1.setMaxLines(1);

        EditText ePhone2 = new EditText(getActivity());
        ePhone2.setHint("Phone Number 2");
        if (!mPhone2.equals("none")) {
            ePhone2.setText(mPhone2);
        }
        ePhone2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        ePhone2.setMaxLines(1);

        eDateBirth.setOnClickListener(view -> datePickerDialog.show());

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(ePlaceBirth);
        layout.addView(eDateBirth);
        if (mLevelUser.equals("0")) {
            layout.addView(eCompanyAddress);
        }
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
                    editor.putString("birth", iPlaceBirth + ", " + iDateBirth);
                    editor.putString("employee_address", iEmployeeAddress);

                    if (!mLevelUser.equals("0")) {
                        editor.putString("company_address", iComapanyAdddress);
                    }
                    editor.putString("phone1", iPhone1);
                    editor.putString("phone2", iPhone2);

                    editor.apply();

                    sendJsonAdmin(iPlaceBirth, iDateBirth, iComapanyAdddress, iEmployeeAddress, iPhone1, iPhone2);

                }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
                }).show();
    }

    private void sendJsonAdmin(String jplacecebirth, String jdatebirth, String jcompany_address, String jemployee_address,
                               String jphone1, String jphone2) {
        String mUserName = sharedPref.getString("email", "olmatix1");
        String mRealName = sharedPref.getString("real_name", "olmatix1");

        //String tag_json_obj = "login";

        String url = Config.DOMAIN + "wamonitoring/update_employee.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, this::parsingJson, error -> {
            Log.d(TAG, "onErrorResponse: " + error);
            StyleableToast styleableToast = new StyleableToast
                    .Builder(getContext())
                    .icon(R.drawable.ic_action_info)
                    .text("Network error, try again later")
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.RED)
                    .build();
            styleableToast.show();
        }) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("real_name", mRealName);
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
            Log.d(TAG, "result_json before: " + json);
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");
            //Log.d(TAG, "result_json: " + result_json + "result_code: " + result_code);

            styleableToast = new StyleableToast
                    .Builder(getContext())
                    .icon(R.drawable.ic_action_info)
                    .text(result_json)
                    .textColor(Color.WHITE)
                    .backgroundColor(Color.BLUE)
                    .build();
            styleableToast.show();


        } catch (JSONException e) {
            e.printStackTrace();
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
                    eDateBirth.setText(date);
                }, year, month, dayOfMonth);
    }

    private void getTagThisMonth() {

        //String tag_json_obj = TAG;
        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month_byEmail.php";
                break;
            case "4":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month_byGroup.php";
                break;
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    tagsThisMonth.setText(result_message);
                } else {
                    tagsThisMonth.setText("0");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("email", mUser);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void getTagToday() {

        //String tag_json_obj = TAG;
        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today_byEmail.php";
                break;
            case "4":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today_byGroup.php";
                break;
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                //JSONObject jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    tagsToday.setText(result_message);
                } else {
                    tagsToday.setText("0");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("email", mUser);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void getChecklist() {

        //String tag_json_obj = TAG;
        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_checklist_need_concern.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_checklist_need_concern_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_checklist_need_concern_byDiv.php";
                break;
            case "4":
                url = Config.DOMAIN + "wamonitoring/get_checklist_need_concern_byDiv.php";
                break;
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                //JSONObject jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    Checklist.setText(result_message);
                } else {
                    Checklist.setText("0");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("email", mUser);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void getLogin() {

        //String tag_json_obj = TAG;
        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_login.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_login_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_login_byDiv.php";
                break;
            case "4":
                url = Config.DOMAIN + "wamonitoring/get_login_byGroup.php";
                break;
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                //JSONObject jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    lLogin.setText(result_message);
                } else {
                    lLogin.setText("0");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
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

    private void getLogout() {

        //String tag_json_obj = TAG;
        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_logout.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_logout_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_logout_byDiv.php";
                break;
            case "4":
                url = Config.DOMAIN + "wamonitoring/get_logout_byGroup.php";
                break;        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    lLogout.setText(result_message);
                } else {
                    lLogout.setText("0");

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
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

    private void getDataUser(String searchid, String searchby, String page) {

        String url;
        if (mLevelUser.equals("0")) {
            url = Config.DOMAIN + "wamonitoring/get_employee_all.php";
            mCompanyID = sharedPref.getString("company_id", "jakarta");

        } else if (mLevelUser.equals("4")) {
            url = Config.DOMAIN + "wamonitoring/get_employee_all_sa.php";
            mCompanyID = mPrefHelper.getGroup();

        } else {
            mCompanyID = sharedPref.getString("company_id", "jakarta");
            url = Config.DOMAIN + "wamonitoring/get_employee_by_division.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");

                Log.d(TAG, "getDataUser: "+response);
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("employee");

                    if (page.equals("0")) {
                        tagList1.clear();
                    }

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
                        String firebase_id = tags_name.getString("firebase_id");

                        EmployeeModel tags = new EmployeeModel(real_name, password, place_birth, date_birth, email,
                                employee_address, employment_status, division, employment, phone1, phone2,
                                last_update, level_user, foto, log_status, firebase_id);
                        tagList1.add(tags);
                        //Log.d(TAG, "setData: " + real_name);
                    }

                    mAdapterUser.notifyDataSetChanged();


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
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    @Override
    public void onResume() {
        super.onResume();
        getData("0");
        GetUsers();
        getTagThisMonth();
        getTagToday();
        getChecklist();
        getLogin();
        getLogout();
        //labellasttag.setText(mPrefHelper.getLastLog());

        if (mPrefHelper.getLogin().equals("1")) {
            refreshHeader();
        } else {
            if (autoUpdate != null) {
                autoUpdate.cancel();
            }
        }
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
        //AppController.getInstance().getRequestQueue().cancelAll(TAG);
        if (autoUpdate != null) {
            autoUpdate.cancel();
        }
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

    private void getAbsence(String statAbsen) {

        String url = Config.DOMAIN + "wamonitoring/get_data_absen.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("absen");

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);

                        date_created_masuk = tags_name.getString("date_created_masuk");
                        date_created_pulang = tags_name.getString("date_created_pulang");
                        latitude = tags_name.getString("latitude");
                        longitude = tags_name.getString("longitude");
                        working_gps = tags_name.getString("working_gps");
                        //log_status = tags_name.getString("log_status");

                    }
                    itungAbsen();
                    refreshHeader();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mUser);
                MyData.put("stat_absen", statAbsen);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void itungAbsen() {
        //Log.d(TAG, "itungAbsen: " + latitude + "," + longitude + "," + date_created_masuk + "," + date_created_pulang);

        int distanceInt = 0;
        if (!working_gps.isEmpty()) {
            String locSplit[] = working_gps.split(",");
            String locLat = locSplit[0];
            String locLot = locSplit[1];

            Location startPoint = new Location("locationA");
            startPoint.setLatitude(Double.parseDouble(locLat));
            startPoint.setLongitude(Double.parseDouble(locLot));


            Location endPoint = new Location("locationB");
            endPoint.setLatitude(Double.parseDouble(latitude));
            endPoint.setLongitude(Double.parseDouble(longitude));

            double distance = startPoint.distanceTo(endPoint);
            distanceInt = (int) distance;
        }
        String input = date_created_masuk;
        String output = date_created_pulang;

        //SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

        Date date;
        if (!input.equals("null")) {
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(input);
                long milliseconds = date.getTime();
                long updatetime = Long.parseLong(String.valueOf(milliseconds));

                Calendar now = Calendar.getInstance();
                now.setTime(new Date());
                now.getTimeInMillis();
                long dura;
                String workLabel;
                if (mPrefHelper.getLogin().equals("1")) {
                    dura = now.getTimeInMillis() - updatetime;
                    workLabel = "Your ";
                } else {
                    date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(output);
                    long milliseconds1 = date.getTime();
                    long updatetime1 = Long.parseLong(String.valueOf(milliseconds1));
                    dura = updatetime1 - updatetime;
                    workLabel = "Your last ";

                    if (autoUpdate != null) {
                        autoUpdate.cancel();
                    }
                }

                labellasttag.setText(workLabel + "working time " + Utils.getDuration(dura / 1000) + ", range from working location " + Utils.getJarak(distanceInt));

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

}
