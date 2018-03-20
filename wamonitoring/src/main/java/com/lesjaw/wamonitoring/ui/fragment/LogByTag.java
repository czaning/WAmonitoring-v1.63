package com.lesjaw.wamonitoring.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
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
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.CheckListAdapterData;
import com.lesjaw.wamonitoring.model.DivisionAndID;
import com.lesjaw.wamonitoring.model.checklistModelData;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.mancj.slideup.SlideUp;
import com.mancj.slideup.SlideUpBuilder;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;

public class LogByTag extends android.support.v4.app.Fragment {

    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final String TAG = "LogTags";
    private RecyclerView sectionHeader;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private Context mContext;
    private String mLevelUser, times, mCompanyID;
    private Spinner employee;
    private TextView mTgl;
    private DatePickerDialog datePickerDialog;
    private int firstopen = 0;
    private String divitem, mDiv;
    private TextView tagdata;
    private SlideUp slideUp;
    private View dim;
    private List<checklistModelData> tagList1 = new ArrayList<>();
    private StyleableToast styleableToast;

    private CheckListAdapterData mAdapterCklist;
    private ListView lv;
    //private CardView cardView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        return  inflater.inflate(R.layout.frag_log_tags, container, false);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View sliderView = view.findViewById(R.id.slideViewLeft);
        //TextView slidedown = (TextView) view.findViewById(R.id.textView);
        tagdata = (TextView) view.findViewById(R.id.textView1);
        lv = (ListView) view.findViewById(R.id.listview_checklist);
        dim = view.findViewById(R.id.dim);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());

        mContext = getContext();
        employee = (Spinner) view.findViewById(R.id.spinnerEmployee);
        mTgl = (TextView) view.findViewById(R.id.tgl);
        sectionHeader = (RecyclerView)view.findViewById(R.id.rv);
        sectionAdapter = new SectionedRecyclerViewAdapter();

        Calendar now = Calendar.getInstance();
        int mYear =  now.get(Calendar.YEAR);
        int mMonth = now.get(Calendar.MONTH);
        int mDay = now.get(Calendar.DAY_OF_MONTH);

        setDatePicker(mYear,mMonth,mDay);
        SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);

        mTgl.setText(timeformat.format(Calendar.getInstance().getTime()));
        times = mTgl.getText().toString();

        mCompanyID = sharedPref.getString("company_id", "olmatix1");

        mLevelUser = mPrefHelper.getLevelUser();
        if (!mLevelUser.equals("0")){
            employee.setEnabled(false);
        }

        getDatatags(times);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(mContext);
        sectionHeader.setLayoutManager(linearLayoutManager);
        sectionHeader.setHasFixedSize(true);
        sectionHeader.setAdapter(sectionAdapter);

        mTgl.setOnClickListener(view1 -> {
            mLevelUser = mPrefHelper.getLevelUser();
            datePickerDialog.show();
        });

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

        employee.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                divitem = employee.getSelectedItem().toString();

                int divID = Integer.parseInt(String.valueOf(((DivisionAndID) parent.getSelectedItem()).getId()));
                DivisionAndID divName = (((DivisionAndID) parent.getItemAtPosition(position)));
                mDiv = divName.div_name;


                Log.d(TAG, "onItemSelected: "+divitem+" open "+divID);
                if (!divitem.equals("ALL")) {
                    getDatabyFilter(times, String.valueOf(divID),mDiv);
                }
                if (firstopen!=0) {
                    if (divitem.equals("ALL")) {
                        mLevelUser = mPrefHelper.getLevelUser();
                        getDataAll(times);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        sectionHeader.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                sectionHeader, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {

            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));



    }

    private void getDatatags (String times){

        sectionAdapter.removeAllSections();
        List<tagsDailyModel> tagList1 = new ArrayList<>();

        if (mLevelUser.equals("0")) {
            employee.setEnabled(false);

            String sDivision = mPrefHelper.getDivisionFull();
            JSONObject jsonResponse1;
            ArrayList<DivisionAndID> aName;
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

                    String url = Config.DOMAIN+"wamonitoring/get_tags_record_data_byType.php";
                    StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                        JSONObject jsonResponse = null;
                        try {
                            jsonResponse = new JSONObject(response);

                            JSONObject jObject = new JSONObject(response);
                            String result_code = jObject.getString("success");

                            if (result_code.equals("1")) {

                                JSONArray cast = jsonResponse.getJSONArray("tags");

                                for (int is = 0; is < cast.length(); is++) {
                                    JSONObject tags_name = cast.getJSONObject(is);

                                    String employee_name = tags_name.getString("employee_name");
                                    String tag_name = tags_name.getString("tag_name");
                                    String tid = tags_name.getString("tid");
                                    String trid = tags_name.getString("trid");
                                    String range_loc = tags_name.getString("range_loc");
                                    String division_name = tags_name.getString("division_name");
                                    String div_id = tags_name.getString("div_id");

                                    String tgl = tags_name.getString("tgl");
                                    String jam = tags_name.getString("jam");
                                    String checklist_done = tags_name.getString("checklist_done");
                                    String email = tags_name.getString("email");
                                    String before_foto = tags_name.getString("before_foto");
                                    String after_foto = tags_name.getString("after_foto");
                                    String latitude = tags_name.getString("latitude");
                                    String longitude = tags_name.getString("longitude");

                                    tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc,
                                            division_name, div_id, tgl, jam, tid, checklist_done,trid,email,
                                            after_foto,before_foto,latitude,longitude);
                                    tagList1.add(tags);
                                    Log.d(TAG, "setData: "+employee_name);

                                }
                                //Log.d(TAG, " sections " + names + " add Data: " + tagList1);

                                employee.setEnabled(true);

                            } else {
                                employee.setEnabled(true);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();

                        }

                    }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
                        protected Map<String, String> getParams() {
                            Map<String, String> MyData = new HashMap<>();
                            MyData.put("company_id", mCompanyID);
                            MyData.put("division_name", cid);
                            MyData.put("tgl", times);

                            return MyData;
                        }
                    };

                    // Adding request to request queue
                    NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

                    sectionAdapter.addSection(new TagListSection(String.valueOf(name), cid, tagList1, mContext));
                    sectionHeader.setAdapter(sectionAdapter);

                }

                ArrayAdapter<DivisionAndID> spinnerArrayAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item_black, aName);
                spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
                employee.setAdapter(spinnerArrayAdapter);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            String name = sharedPref.getString("division", "olmatix1");
            String division_nameHeader = mPrefHelper.getDivName();
            String url = Config.DOMAIN+"wamonitoring/get_tags_record_data_byType.php";
            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");

                    if (result_code.equals("1")) {

                        JSONArray cast = jsonResponse.getJSONArray("tags");

                        for (int is = 0; is < cast.length(); is++) {
                            JSONObject tags_name = cast.getJSONObject(is);

                            String employee_name = tags_name.getString("employee_name");
                            String tag_name = tags_name.getString("tag_name");
                            String tid = tags_name.getString("tid");
                            String trid = tags_name.getString("trid");
                            String range_loc = tags_name.getString("range_loc");
                            String division_name = tags_name.getString("division_name");
                            String div_id = tags_name.getString("div_id");

                            String tgl = tags_name.getString("tgl");
                            String jam = tags_name.getString("jam");
                            String checklist_done = tags_name.getString("checklist_done");
                            String email = tags_name.getString("email");
                            String before_foto = tags_name.getString("before_foto");
                            String after_foto = tags_name.getString("after_foto");
                            String latitude = tags_name.getString("latitude");
                            String longitude = tags_name.getString("longitude");

                            tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc,
                                    division_name, div_id, tgl, jam, tid, checklist_done,trid,email,
                                    after_foto,before_foto,latitude,longitude);
                            tagList1.add(tags);
                            //Log.d(TAG, "setData: "+employee_name);

                        }
                        //Log.d(TAG, " sections " + name + " add Data: " + tagList1);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("company_id", mCompanyID);
                    MyData.put("division_name", name);
                    MyData.put("tgl", times);

                    return MyData;
                }
            };

            // Adding request to request queue
            NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

            sectionAdapter.addSection(new TagListSection(String.valueOf(division_nameHeader), name, tagList1, mContext));
            sectionHeader.setAdapter(sectionAdapter);

        }

        firstopen = 1;

    }

    private void getDatabyFilter (String time, String div, String nameDiv){

        sectionAdapter.removeAllSections();

        List<tagsDailyModel> tagList1 = new ArrayList<>();

        //String tag_json_obj = "getDatabyFilter";

        String url = Config.DOMAIN+"wamonitoring/get_tags_record_data_byType.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");

                if (result_code.equals("1")) {

                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    for (int is = 0; is < cast.length(); is++) {
                        JSONObject tags_name = cast.getJSONObject(is);

                        String employee_name = tags_name.getString("employee_name");
                        String tag_name = tags_name.getString("tag_name");
                        String tid = tags_name.getString("tid");
                        String trid = tags_name.getString("trid");
                        String range_loc = tags_name.getString("range_loc");
                        String division_name = tags_name.getString("division_name");
                        String div_id = tags_name.getString("div_id");
                        String tgl = tags_name.getString("tgl");
                        String jam = tags_name.getString("jam");
                        String checklist_done = tags_name.getString("checklist_done");
                        String email = tags_name.getString("email");
                        String before_foto = tags_name.getString("before_foto");
                        String after_foto = tags_name.getString("after_foto");
                        String latitude = tags_name.getString("latitude");
                        String longitude = tags_name.getString("longitude");

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc,
                                division_name,div_id, tgl, jam, tid, checklist_done,trid,email,
                                after_foto,before_foto,latitude,longitude);
                        tagList1.add(tags);
                        //Log.d(TAG, "setData: "+employee_name);

                    }
                    //Log.d(TAG, " sections " + name + " add Data: " + tagList1);

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division_name", div);
                MyData.put("tgl", time);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

        sectionAdapter.addSection(new TagListSection(String.valueOf(nameDiv), div,tagList1, mContext));
        sectionHeader.setAdapter(sectionAdapter);
        firstopen =1;
    }

    private void getDataAll (String time){
        sectionAdapter.removeAllSections();

        employee.setEnabled(false);

        String sDivision = mPrefHelper.getDivisionFull();
        JSONObject jsonResponse1;
        try {
            jsonResponse1 = new JSONObject(sDivision);
            JSONArray cast1 = jsonResponse1.getJSONArray("division");

            for (int i = 0; i < cast1.length(); i++) {
                JSONObject div_name = cast1.getJSONObject(i);
                String cid = div_name.getString("cid");
                String names = div_name.getString("name");

                List<tagsDailyModel> tagList1 = new ArrayList<>();

                String url = Config.DOMAIN+"wamonitoring/get_tags_record_data_byType.php";
                StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                    JSONObject jsonResponse = null;
                    try {
                        jsonResponse = new JSONObject(response);

                        JSONObject jObject = new JSONObject(response);
                        String result_code = jObject.getString("success");

                        if (result_code.equals("1")) {

                            JSONArray cast = jsonResponse.getJSONArray("tags");

                            for (int is = 0; is < cast.length(); is++) {
                                JSONObject tags_name = cast.getJSONObject(is);

                                String employee_name = tags_name.getString("employee_name");
                                String tag_name = tags_name.getString("tag_name");
                                String tid = tags_name.getString("tid");
                                String trid = tags_name.getString("trid");
                                String range_loc = tags_name.getString("range_loc");
                                String division_name = tags_name.getString("division_name");
                                String div_id = tags_name.getString("div_id");
                                String tgl = tags_name.getString("tgl");
                                String jam = tags_name.getString("jam");
                                String checklist_done = tags_name.getString("checklist_done");
                                String email = tags_name.getString("email");
                                String before_foto = tags_name.getString("before_foto");
                                String after_foto = tags_name.getString("after_foto");
                                String latitude = tags_name.getString("latitude");
                                String longitude = tags_name.getString("longitude");

                                tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc,
                                        division_name, div_id, tgl, jam, tid, checklist_done,trid,email,
                                        after_foto,before_foto, latitude,longitude);
                                tagList1.add(tags);
                                //Log.d(TAG, "setData: "+employee_name);

                            }
                            //Log.d(TAG, " sections " + names + " add Data: " + tagList1);

                            employee.setEnabled(true);

                        } else {
                            employee.setEnabled(true);

                        }

                    } catch (JSONException e) {
                        e.printStackTrace();

                    }

                }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<>();
                        MyData.put("company_id", mCompanyID);
                        MyData.put("division_name", cid);
                        MyData.put("tgl", time);

                        return MyData;
                    }
                };

                // Adding request to request queue
                NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

                sectionAdapter.addSection(new TagListSection(String.valueOf(names),cid, tagList1, mContext));
                sectionHeader.setAdapter(sectionAdapter);

            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
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
                    mTgl.setText(date);
                    times = mTgl.getText().toString();

                    int divID = Integer.parseInt(String.valueOf(((DivisionAndID) employee.getSelectedItem()).getId()));
                    divitem = employee.getSelectedItem().toString();

                   if (mLevelUser.equals("0")){
                       if (!divitem.equals("ALL")) {
                           getDatabyFilter(times, String.valueOf(divID), divitem);
                       }
                       if (firstopen!=0) {
                           if (divitem.equals("ALL")) {
                               mLevelUser = mPrefHelper.getLevelUser();
                               getDataAll(times);
                           }
                       }
                   } else {
                       getDatatags(times);
                   }
                }, year, month, dayOfMonth);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void GetChecklistItem (String tagRID){

        String url = Config.DOMAIN+"wamonitoring/get_checklist_data.php";

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

                        checklistModelData tags = new checklistModelData(trid, checklistName, checklistStatus,checklistNote, cklstatus );
                        tagList1.add(tags);
                        Log.d("DEBUG", "setData: "+checklistName +" "+cklstatus);
                    }


                    mAdapterCklist = new CheckListAdapterData(getContext(),tagList1);
                    lv.setAdapter(mAdapterCklist);
                    slideUp.hide();
                    slideUp.show();

                } else {
                    slideUp.hide();

                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("trid", tagRID);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private LogDaily.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final LogDaily.ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                try {
                    clicklistener.onClick(child,rv.getChildAdapterPosition(child));
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

    private class TagListSection extends StatelessSection {

        String title;
        String divID;
        List<tagsDailyModel> list;
        Context mContext;
        boolean expanded = true;

        TagListSection(String title, String divID, List<tagsDailyModel> list, Context mContext) {
            super(new SectionParameters.Builder(R.layout.frag_item_list_log_by_name)
                    .headerResourceId(R.layout.section_tagslist_header)
                    .footerResourceId(R.layout.section_taglist_footer)
                    .build());

            this.title = title;
            this.divID = divID;
            this.list = list;
            this.mContext = mContext;

        }

        @Override
        public int getContentItemsTotal() {
            return expanded? list.size() : 0;
        }

        @Override
        public RecyclerView.ViewHolder getItemViewHolder(View view) {
            return new ItemViewHolder(view);
        }

        @Override
        public void onBindItemViewHolder(RecyclerView.ViewHolder holder, int position) {
            final ItemViewHolder itemHolder = (ItemViewHolder) holder;

            final tagsDailyModel mTagModel = list.get(position);

            itemHolder.jarak = Integer.parseInt(mTagModel.getRange_loc());

            if (itemHolder.jarak>50){
                itemHolder.far = true;
                itemHolder.tag_name.setTextColor(Color.RED);
            } else {
                itemHolder.far = false;
                itemHolder.tag_name.setTextColor(Color.BLACK);
            }


            if((position % 2 == 0)){
                itemHolder.cardView.setCardBackgroundColor(Color.parseColor("#cce6ff"));
            }else{
                itemHolder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));
            }

            itemHolder.cklistBoolean = mTagModel.getChecklist_done();

            itemHolder.after_photo.setVisibility(View.INVISIBLE);
            itemHolder.before_photo.setVisibility(View.INVISIBLE);

            if (itemHolder.cklistBoolean.equals("0")){
                itemHolder.cklist.setImageResource(android.R.drawable.checkbox_off_background);
            } else {
                itemHolder.cklist.setImageResource(android.R.drawable.checkbox_on_background);
            }
            itemHolder.employeename.setText(mTagModel.getEmployee_name());
            itemHolder.tag_name.setText(mTagModel.getTag_name()+ "\n"+ Utils.getJarak(itemHolder.jarak));

            Time jam = Time.valueOf(mTagModel.getJam());
            SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            itemHolder.tgl.setText(simpleDateFormat1.format(jam));

            itemHolder.cklist.setOnClickListener(view -> {
                String EmployeName = mTagModel.getEmployee_name();

                //itemHolder.cklist.setEnabled(false);
                tagdata.setText(EmployeName +" | "+mTagModel.getTag_name()+"\nDivision : "+mTagModel.getDivision_name());
                GetChecklistItem(mTagModel.getTrid());
            });

            itemHolder.tgl.setOnClickListener(view -> {
              /*  Intent i = new Intent(mContext, UploadFotoTags.class);
                i.putExtra("trid", mTagModel.getTrid());
                mContext.startActivity(i);*/
            });

            String after = mTagModel.getAfter_photo();
            String before = mTagModel.getBefore_photo();

            //Log.d("DEBUG", "onBindViewHolder: "+mTagModel.getEmployee_name()+" "+after);

            if (!before.isEmpty() || !before.equals("")) {
                itemHolder.before_photo.setVisibility(View.VISIBLE);

                Glide.with(mContext)
                        .load("https://olmatix.com/wamonitoring/uploads/thumb_" + mTagModel.getTrid() + "-before_foto" + ".jpg")
                        .asBitmap()
                        .into(new BitmapImageViewTarget(itemHolder.before_photo) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mContext.getResources(),
                                        Bitmap.createScaledBitmap(resource, 50, 50, false));
                                drawable.setCircular(true);
                                itemHolder.before_photo.setImageDrawable(drawable);
                            }
                        });

                itemHolder.before_photo.setOnClickListener(v -> {
                    Intent i = new Intent(mContext, ImageViewer.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("img", "https://olmatix.com/wamonitoring/uploads/"+mTagModel.getTrid()+"-before_foto"+".jpg");
                    i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/"+mTagModel.getEmail()+".jpg");

                    mContext.startActivity(i);
                });

            }

            if (!after.isEmpty() || !after.equals("")) {
                itemHolder.after_photo.setVisibility(View.VISIBLE);

                Glide.with(mContext)
                        .load("https://olmatix.com/wamonitoring/uploads/thumb_" + mTagModel.getTrid() + "-after_foto" + ".jpg")
                        .asBitmap()
                        .into(new BitmapImageViewTarget(itemHolder.after_photo) {
                            @Override
                            protected void setResource(Bitmap resource) {
                                RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(mContext.getResources(),
                                        Bitmap.createScaledBitmap(resource, 50, 50, false));
                                drawable.setCircular(true);
                                itemHolder.after_photo.setImageDrawable(drawable);
                            }
                        });


                itemHolder.after_photo.setOnClickListener(v -> {
                    Intent i = new Intent(mContext, ImageViewer.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("img", "https://olmatix.com/wamonitoring/uploads/" + mTagModel.getTrid() + "-after_foto" + ".jpg");
                    i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/" + mTagModel.getEmail() + ".jpg");

                    mContext.startActivity(i);
                });
            }

            itemHolder.cardView.setOnClickListener(v -> {

                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.mipmap.barcode_img)
                        .text("This tag was tagged at "+ Utils.address(Double.parseDouble(mTagModel.getLatitude()),
                                Double.parseDouble(mTagModel.getLongitude()),
                                getContext()))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();

            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.y = headerHolder.getAdapterPosition()/headerHolder.x;

            /*Log.d(TAG, "header bind Viewer: "+title+" position "+headerHolder.getAdapterPosition()+" "+headerHolder.y+" "
                    +headerHolder.y % 2);*/

            int colorPos = headerHolder.y %  headerHolder.colors.length;
            headerHolder.rl.setBackgroundColor(Color.parseColor(headerHolder.colors[colorPos]));

            headerHolder.tvTitle.setText(title);
            headerHolder.rootView.setOnClickListener(v -> {
                expanded = !expanded;
                sectionAdapter.notifyDataSetChanged();
            });

            headerHolder.rootView.setOnClickListener(v -> {
                expanded = !expanded;
                headerHolder.imgExp.setImageResource(
                        expanded ? R.drawable.ic_keyboard_arrow_up_black_18dp : R.drawable.ic_keyboard_arrow_down_black_18dp
                );
                sectionAdapter.notifyDataSetChanged();
            });


        }

        @Override
        public RecyclerView.ViewHolder getFooterViewHolder(View view) {
            return new FooterViewHolder(view);
        }

        @Override
        public void onBindFooterViewHolder(RecyclerView.ViewHolder holder) {
            FooterViewHolder footerHolder = (FooterViewHolder) holder;

            //Log.d(TAG, "onBindFooterViewHolder: RUNNING");
            String url = Config.DOMAIN+"wamonitoring/get_tags_data_all_by_name.php";

            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");


                    if (result_code.equals("1")) {
                        final ArrayList<String> tagsName = new ArrayList<>();
                        JSONArray cast = jsonResponse.getJSONArray("tags");
                        for (int is = 0; is < cast.length(); is++) {

                            JSONObject tags_name = cast.getJSONObject(is);
                            String tag_name = tags_name.getString("tag_name");

                            tagsName.add(tag_name)  ;

                            //Log.d(TAG, "Get Data footer: " + tag_name + " div " + division_name);
                        }

                        //Log.d("DEBUG", "onClick: "+tagsName.size());
                        while ( footerHolder.i<tagsName.size() ) {
                            footerHolder.tvFooter.setText(footerHolder.tvFooter.getText() + tagsName.get(footerHolder.i) + " | ");
                            footerHolder.i++;
                        }

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<>();
                    MyData.put("company_id", mCompanyID);
                    MyData.put("divname", divID);
                    return MyData;
                }
            };

            // Adding request to request queue
            NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);


            //footerHolder.tvFooter.setText(title+" tags");
            //new GetTags(title).execute();

            footerHolder.rootView.setOnClickListener(v -> {
                //Toast.makeText(getContext(), String.format("Clicked on footer of Section %s", title), Toast.LENGTH_SHORT).show();

            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final ImageView imgExp;
        private final View rootView;
        private final RelativeLayout rl;
        private String[] colors = new String[] { "#ffffcc", "#ffccf2" };
        int y, x = 2;

        HeaderViewHolder(View view) {
            super(view);

            rootView = view;
            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imgExp = (ImageView) view.findViewById(R.id.test);
            rl = (RelativeLayout) view.findViewById(R.id.rl1);

        }
    }

    private class FooterViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        private final TextView tvFooter;
        int i = 0;
        //private int[] colors = new int[] { 0x30FF0000, 0x300000FF };
        //int y, x = 2;
        //private final RelativeLayout rl;

        FooterViewHolder(View view) {
            super(view);

            rootView = view;
            tvFooter = (TextView) view.findViewById(R.id.tvSeeMore);
            //rl = (RelativeLayout) view.findViewById(R.id.rootView);

        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        String cklistBoolean;
        TextView tag_name, tgl, employeename  ;
        ImageView cklist;
        ImageView before_photo, after_photo;
        CardView cardView;
        boolean far;
        int jarak;

        ItemViewHolder(View view) {
            super(view);

            tag_name = (TextView) view.findViewById(R.id.tag_name);
            employeename = (TextView) view.findViewById(R.id.employee_name);
            cardView = (CardView) view.findViewById(R.id.cv);
            tgl = (TextView) view.findViewById(R.id.tgl);
            cklist = (ImageView)view.findViewById(R.id.cklist);
            before_photo = (ImageView)view.findViewById(R.id.before_photo);
            after_photo = (ImageView)view.findViewById(R.id.after_photo);
        }
    }


}
