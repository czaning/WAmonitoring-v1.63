package com.lesjaw.wamonitoring.ui.fragment;

import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
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
import com.lesjaw.wamonitoring.adapter.CheckListAdapterData;
import com.lesjaw.wamonitoring.adapter.EmployeeAdapter;
import com.lesjaw.wamonitoring.adapter.ListEmployeeLastTagAdapter;
import com.lesjaw.wamonitoring.adapter.ListTagsLogAdapterFrag;
import com.lesjaw.wamonitoring.model.EmployeeAndEmail;
import com.lesjaw.wamonitoring.model.EmployeeLastTagModel;
import com.lesjaw.wamonitoring.model.EmployeeModel;
import com.lesjaw.wamonitoring.model.checklistModelData;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.EndlessRecyclerViewScrollListener;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

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


public class LogUsers extends android.support.v4.app.Fragment {

    private RecyclerView mRecycleView, mRecycleViewData, mRecycleViewEmployee;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final String TAG = "LogUsers";
    private List<tagsDailyModel> tagList = new ArrayList<>();
    private List<EmployeeModel> tagList2 = new ArrayList<>();
    private List<EmployeeLastTagModel> tagList3 = new ArrayList<>();

    private EmployeeAdapter mAdapterData;
    private ListEmployeeLastTagAdapter mAdapterLastTags;

    private ListTagsLogAdapterFrag mAdapter;
    private String mLevelUser, mEmail, times;
    private Spinner employee;
    private TextView mTgl;
    private DatePickerDialog datePickerDialog;
    private ProgressBar loading;
    private TextView tagdata;
    private SlideUp slideUp;
    private View dim;
    private List<checklistModelData> tagList1 = new ArrayList<>();
    private CheckListAdapterData mAdapterCklist;
    private ListView lv;
    private EndlessRecyclerViewScrollListener scrollListener;
    private int firstpage = 10, fromRV = 0;
    private StyleableToast styleableToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.frag_log_users, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loading = (ProgressBar) view.findViewById(R.id.loading);
        View sliderView = view.findViewById(R.id.slideViewLeft);
        //TextView slidedown = (TextView) view.findViewById(R.id.textView);
        tagdata = (TextView) view.findViewById(R.id.textView1);
        lv = (ListView) view.findViewById(R.id.listview_checklist);
        dim = view.findViewById(R.id.dim);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        Context mContext = getActivity();
        mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleViewData = (RecyclerView) view.findViewById(R.id.rvdata);
        mRecycleViewEmployee = (RecyclerView) view.findViewById(R.id.rvEmployee);

        employee = (Spinner) view.findViewById(R.id.spinnerEmployee);
        mTgl = (TextView) view.findViewById(R.id.tgl);

        mRecycleViewData.setHasFixedSize(true);
        mRecycleView.setHasFixedSize(true);
        mRecycleViewEmployee.setHasFixedSize(true);

        mAdapterData = new EmployeeAdapter(tagList2, mContext);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        mRecycleViewData.setLayoutManager(layoutManager);
        mRecycleViewData.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewData.setAdapter(mAdapterData);

        mAdapter = new ListTagsLogAdapterFrag(tagList, mContext);
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getContext());
        mRecycleView.setLayoutManager(layoutManager1);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        mAdapterLastTags = new ListEmployeeLastTagAdapter(tagList3, mContext);
        LinearLayoutManager layoutManager2 = new LinearLayoutManager(getContext());
        mRecycleViewEmployee.setLayoutManager(layoutManager2);
        mRecycleViewEmployee.setItemAnimator(new DefaultItemAnimator());
        mRecycleViewEmployee.setAdapter(mAdapterLastTags);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());
        mEmail = sharedPref.getString("email", "olmatix1");

        mLevelUser = mPrefHelper.getLevelUser();

        Calendar now = Calendar.getInstance();
        int mYear = now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mYear, mMonth, mDay);
        SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        mTgl.setText(timeformat.format(Calendar.getInstance().getTime()));
        times = mTgl.getText().toString();

        mRecycleView.setVisibility(View.GONE);
        mRecycleViewData.setVisibility(View.GONE);

        // spinner
        getEmployee();
        //
        getDataEmployee(mEmail);
        getData(mEmail, times);
        getDataLastTag("0");

        employee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                EmployeeAndEmail swt = (EmployeeAndEmail) parent.getItemAtPosition(position);
                mEmail = (String) swt.email;
                times = mTgl.getText().toString();

                if (fromRV == 0) {

                    if (!mEmail.equals("") || !mEmail.isEmpty()) {
                        getDataEmployee(mEmail);
                        getData(mEmail, times);

                        mRecycleViewEmployee.setVisibility(View.GONE);
                        mRecycleView.setVisibility(View.VISIBLE);
                        mRecycleViewData.setVisibility(View.VISIBLE);

                        tagList3.clear();
                        mAdapterLastTags.notifyDataSetChanged();
                        scrollListener.resetState();
                        firstpage = 10;

                    } else {
                        getDataLastTag("0");
                        mRecycleViewEmployee.setVisibility(View.VISIBLE);
                        mRecycleView.setVisibility(View.GONE);
                        mRecycleViewData.setVisibility(View.GONE);

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mTgl.setOnClickListener(view1 -> datePickerDialog.show());

        sliderView.setOnClickListener(v -> slideUp.hide());

        slideUp = new SlideUpBuilder(sliderView)
                .withListeners(new SlideUp.Listener.Events() {
                    @Override
                    public void onSlide(float percent) {
                        dim.setAlpha(1 - (percent / 100));
                    }

                    @Override
                    public void onVisibilityChanged(int visibility) {
                        if (visibility == View.GONE) {
                            dim.setAlpha(0);

                        }
                    }
                })
                .withStartGravity(Gravity.END)
                .withLoggingEnabled(false)
                .withGesturesEnabled(false)
                .withStartState(SlideUp.State.HIDDEN)
                .build();

        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {


                ImageView cklist = (ImageView) view.findViewById(R.id.cklist);
                cklist.setOnClickListener(v -> {
                    String EmployeName = tagList.get(position).getEmployee_name();

                    slideUp.hide();
                    slideUp.show();
                    tagdata.setText(EmployeName + " | " + tagList.get(position).getTag_name() + "\nDivision : " + tagList.get(position).getDivision_name());
                    GetChecklistItem(tagList.get(position).getTrid());
                });

                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.mipmap.barcode_img)
                        .text("This tag was tagged at "+ Utils.address(Double.parseDouble(tagList.get(position).getLatitude()),
                                Double.parseDouble(tagList.get(position).getLongitude()),
                                getContext()))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();

            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        mRecycleViewEmployee.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

                String EmployeName = tagList3.get(position).getEmployee_name();
                String mEmailPos = tagList3.get(position).getEmail();

                if (tagList3.get(position).getCounttags().isEmpty() || tagList3.get(position).getCounttags().equals("null")) {
                    styleableToast = new StyleableToast
                            .Builder(getContext())
                            .icon(R.drawable.ic_action_info)
                            .text("Employee " + EmployeName + " have no data this month")
                            .textColor(Color.WHITE)
                            .backgroundColor(Color.BLUE)
                            .build();
                    styleableToast.show();
                } else {
                    //Toast.makeText(getContext(), "Employee " + EmployeName + " last tag at " +
                    //        tagList3.get(position).getTgl() + " | " + tagList3.get(position).getJam(), Toast.LENGTH_SHORT).show();

                    String tgl = tagList3.get(position).getTgl();
                    if (!mEmailPos.equals("") || !mEmailPos.isEmpty()) {

                        mRecycleViewEmployee.setVisibility(View.GONE);
                        mRecycleView.setVisibility(View.VISIBLE);
                        mRecycleViewData.setVisibility(View.VISIBLE);

                        tagList3.clear();
                        mAdapterLastTags.notifyDataSetChanged();
                        scrollListener.resetState();
                        firstpage = 10;
                        mTgl.setText(tgl);


                        for (int i = 0; i < employee.getCount(); i++) {
                            if (employee.getAdapter().getItem(i).toString().equals(EmployeName)) {
                                employee.setSelection(i);
                                break;
                            }
                        }

                        //getDataEmployee(mEmail);
                        //getData(mEmail, tgl);

                    } else {
                        getDataLastTag("0");
                        mRecycleViewEmployee.setVisibility(View.VISIBLE);
                        mRecycleView.setVisibility(View.GONE);
                        mRecycleViewData.setVisibility(View.GONE);

                    }
                }


                TextView cklist = (TextView) view.findViewById(R.id.employee_email);
                cklist.setOnClickListener(v -> {
                    String EmployeName1 = tagList3.get(position).getEmployee_name();
                    styleableToast = new StyleableToast
                            .Builder(getContext())
                            .icon(R.drawable.ic_action_info)
                            .text("Employee " + EmployeName1)
                            .textColor(Color.WHITE)
                            .backgroundColor(Color.BLUE)
                            .build();
                    styleableToast.show();
                });
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        mRecycleViewData.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view, final int position) {


                CircleImageView imgProfile = (CircleImageView) view.findViewById(R.id.img_profile) ;
                imgProfile.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), EmployeeProfile.class);
                    i.putExtra("email", tagList2.get(position).getEmail());
                    String transitionName = getString(R.string.imgProfile);

                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(), imgProfile, transitionName);
                    startActivity(i, transitionActivityOptions.toBundle());
                });


            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager2) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                //Log.d(TAG, "last position and firtpage is " + firstpage);
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                getDataLastTag(String.valueOf(firstpage));
                firstpage = firstpage + 10;
            }
        };
        mRecycleViewEmployee.addOnScrollListener(scrollListener);

    }

    private void GetChecklistItem(String tagRID) {

        String url = Config.DOMAIN + "wamonitoring/get_checklist_data.php";


        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {

                    JSONArray cast = jsonResponse.getJSONArray("tags");
                    tagList1.clear();
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String trid = tags_name.getString("trid");
                        String checklistName = tags_name.getString("checklist");
                        String checklistStatus = tags_name.getString("checklist_status");
                        String checklistNote = tags_name.getString("checklist_note");
                        boolean cklstatus;
                        cklstatus = checklistStatus.equals("1");

                        checklistModelData tags = new checklistModelData(trid, checklistName, checklistStatus, checklistNote, cklstatus);
                        tagList1.add(tags);
                        //Log.d("DEBUG", "setData: " + checklistName + " " + cklstatus);
                    }


                    mAdapterCklist = new CheckListAdapterData(getContext(), tagList1);
                    lv.setAdapter(mAdapterCklist);


                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("trid", tagRID);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
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

                    //Log.d(TAG, "onDateSet: " + mTgl.getText());

                    if (!mEmail.equals("") || !mEmail.isEmpty()) {
                        getData(mEmail, times);
                        getDataEmployee(mEmail);

                    }
                }, year, month, dayOfMonth);
    }

    private void getData(String emails, String tgls) {

        Log.d(TAG, "getData: " + emails + " " + tgls);

        String url = Config.DOMAIN + "wamonitoring/get_tags_record_byemail_user.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    tagList.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String employee_name = tags_name.getString("employee_name");
                        String tag_name = tags_name.getString("tag_name");
                        String tid = tags_name.getString("tid");
                        String trid = tags_name.getString("trid");
                        String range_loc = tags_name.getString("range_loc");
                        String division_name = tags_name.getString("division_name");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String checklist_done = tags_name.getString("checklist_done");
                        String email = tags_name.getString("email");
                        String before_foto = tags_name.getString("before_foto");
                        String after_foto = tags_name.getString("after_foto");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc, division_name,"", tgl, jam,
                                tid, checklist_done, trid, email, after_foto, before_foto,latitude,longitude);
                        tagList.add(tags);
                        //Log.d(TAG, "setData Content: " + tag_name);

                    }
                    loading.setVisibility(View.GONE);
                    mRecycleView.setAdapter(mAdapter);

                } else {
                    loading.setVisibility(View.GONE);

                    tagList.clear();
                    mRecycleView.setAdapter(mAdapter);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                tagList.clear();

                mRecycleView.setAdapter(mAdapter);

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", emails);
                MyData.put("tgl", tgls);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void getDataEmployee(String emails) {

        String url = Config.DOMAIN + "wamonitoring/get_data_employee.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("employee");
                    tagList2.clear();
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
                                last_update, level_user, foto,log_status, firbaseID);
                        tagList2.add(tags);
                        //Log.d(TAG, "setData: Employe Header" + real_name);
                    }

                    mRecycleViewData.setAdapter(mAdapterData);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                tagList2.clear();

                mRecycleViewData.setAdapter(mAdapterData);

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", emails);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private void getDataLastTag(String page) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mDivision = sharedPref.getString("division", "olmatix1");
        String mGroup = mPrefHelper.getGroup();

        String url;
        if (mLevelUser.equals("0")) {
            url = Config.DOMAIN + "wamonitoring/get_tags_record_count.php";
        } else if (mLevelUser.equals("4")) {
            url = Config.DOMAIN + "wamonitoring/get_tags_record_count_byGroup.php";
        } else {
            url = Config.DOMAIN + "wamonitoring/get_tags_record_count_byDiv.php";
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    if (page.equals("0")) {
                        tagList3.clear();
                    }
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String counttags = tags_name.getString("counttags");
                        String employee_name = tags_name.getString("employee_name");
                        String tagname = tags_name.getString("tagname");
                        String range_loc = tags_name.getString("range_loc");
                        String division_name = tags_name.getString("division_name");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String tid = tags_name.getString("tid");
                        String trid = tags_name.getString("trid");
                        String email = tags_name.getString("email");
                        String last_update = tags_name.getString("last_update");
                        String level_user = tags_name.getString("level_user");

                        EmployeeLastTagModel tags = new EmployeeLastTagModel(counttags, employee_name, tagname, range_loc,
                                division_name, tgl, jam, tid, trid, email, last_update, level_user);

                        tagList3.add(tags);
                        //Log.d(TAG, "getDataLastTag: for ALL " + email);
                    }
                    loading.setVisibility(View.GONE);
                    mAdapterLastTags.notifyDataSetChanged();


                } else {
                    loading.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
                e.printStackTrace();

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                if(mLevelUser.equals("4")){
                    MyData.put("group", mGroup);
                }else{
                    MyData.put("company_id", mCompanyID);
                    MyData.put("division", mDivision);
                }
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
    }

    private void getEmployee() {

        String mCompanyID;
        if(mLevelUser.equals("4")){
            mCompanyID = mPrefHelper.getGroup();
        }else{
            mCompanyID= sharedPref.getString("company_id", "olmatix1");
        }

        String mDivision = sharedPref.getString("division", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(getContext());
        String url;
        if (mLevelUser.equals("0")) {
            url = "https://olmatix.com/wamonitoring/get_employee.php";
        } else if(mLevelUser.equals("4")){
            // kalau level user 4 kirim group id nya
            url = "https://olmatix.com/wamonitoring/get_employee_by_group_spinner.php";
        } else {
            url = "https://olmatix.com/wamonitoring/get_employee_by_division_spinner.php";
        }

        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {

            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {

                    List<EmployeeAndEmail> aName = new ArrayList<>();
                    aName.clear();
                    JSONArray cast = jsonResponse.getJSONArray("employee");

                    aName.add(new EmployeeAndEmail("ALL", ""));

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String real_name = tags_name.getString("real_name");
                        String email = tags_name.getString("email");

                        aName.add(new EmployeeAndEmail(real_name, email));

                        //Log.d(TAG, "setDataSpinner: " + real_name);
                    }

                    ArrayAdapter<EmployeeAndEmail> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_black, aName);
                    spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                    employee.setAdapter(spinnerArrayAdapter);


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

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        MyStringRequest.setRetryPolicy(policy);
        MyRequestQueue.add(MyStringRequest);

    }

    @Override
    public void onResume() {
        super.onResume();

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
