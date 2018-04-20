package com.lesjaw.wamonitoring.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.tagsModel;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import io.github.luizgrp.sectionedrecyclerviewadapter.StatelessSection;


public class ListTagsActivity extends AppCompatActivity {

    SharedPreferences sharedPref;

    private Context mContext;
    private SectionedRecyclerViewAdapter sectionAdapter;

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_rc);

        getSupportActionBar().setTitle(Html.fromHtml("<font color='#ffffff'>Tags List </font>"));
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);
        mContext = ListTagsActivity.this;

        ProgressDialog pDialog = new ProgressDialog(ListTagsActivity.this);
        pDialog.setMessage("Updating your data. Please wait...");
        pDialog.setCancelable(true);
        pDialog.setCanceledOnTouchOutside(false);
        pDialog.show();

        sectionAdapter = new SectionedRecyclerViewAdapter();
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        PreferenceHelper mPrefHelper = new PreferenceHelper(mContext);
        String mLevelUser = mPrefHelper.getLevelUser();

        if (mLevelUser.equals("0")) {

            String sDivision = mPrefHelper.getDivisionFull();
            JSONObject jsonResponse1;
            try {
                jsonResponse1 = new JSONObject(sDivision);
                JSONArray cast1 = jsonResponse1.getJSONArray("division");
                for (int i = 0; i < cast1.length(); i++) {
                    JSONObject div_name = cast1.getJSONObject(i);
                    String name = div_name.getString("cid");
                    String Divname = div_name.getString("name");

                    List<tagsModel> tagList1 = new ArrayList<>();

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    String mCompanyID = sharedPref.getString("company_id", "olmatix1");

                    String url;
                            url = "https://olmatix.com/wamonitoring/get_tags_data_all_by_name.php";

                   // Log.d(TAG, "getTagsbyName: " + " " + name);

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
                                    String tid = tags_name.getString("tid");
                                    String tag_name = tags_name.getString("tag_name");
                                    String tag_location = tags_name.getString("tag_location");
                                    //String division_name = tags_name.getString("division_name");
                                    String time_interval = tags_name.getString("time_interval");
                                    String type = tags_name.getString("type");

                                    tagsModel tags = new tagsModel(tid, tag_name, tag_location, Divname, time_interval, type);
                                    tagList1.add(tags);
                                    //Log.d(TAG, " sections " + name + " add Data: " + tag_name + " div " + division_name);
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
                            MyData.put("divname", name);

                            return MyData;
                        }
                    };
                    NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
                    sectionAdapter.addSection(new TagListSection(String.valueOf(Divname), tagList1, mContext));
                    pDialog.cancel();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if(mLevelUser.equals("4")){
            String sDivision = mPrefHelper.getDivisionFull();
            Log.d("DIVISION FULL GROUP",sDivision);
            JSONObject jsonResponse1;
            try {
                jsonResponse1 = new JSONObject(sDivision);
                JSONArray cast1 = jsonResponse1.getJSONArray("division");
                for (int i = 0; i < cast1.length(); i++) {
                    JSONObject div_name = cast1.getJSONObject(i);
                    String name = div_name.getString("cid");
                    String Divname = div_name.getString("name");

                    List<tagsModel> tagList1 = new ArrayList<>();

                    sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                    String mCompanyID = sharedPref.getString("company_id", "olmatix1");

                    String url;
                    url = "https://olmatix.com/wamonitoring/get_tags_data_all_by_name.php";

                    // Log.d(TAG, "getTagsbyName: " + " " + name);

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
                                    String tid = tags_name.getString("tid");
                                    String tag_name = tags_name.getString("tag_name");
                                    String tag_location = tags_name.getString("tag_location");
                                    //String division_name = tags_name.getString("division_name");
                                    String time_interval = tags_name.getString("time_interval");
                                    String type = tags_name.getString("type");

                                    tagsModel tags = new tagsModel(tid, tag_name, tag_location, Divname, time_interval, type);
                                    tagList1.add(tags);
                                    //Log.d(TAG, " sections " + name + " add Data: " + tag_name + " div " + division_name);
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
                            MyData.put("divname", name);

                            return MyData;
                        }
                    };
                    NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
                    sectionAdapter.addSection(new TagListSection(String.valueOf(Divname), tagList1, mContext));
                    pDialog.cancel();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            String name = sharedPref.getString("division", "olmatix1");
            String mCompanyID = sharedPref.getString("company_id", "olmatix1");
            String division_nameHeader = mPrefHelper.getDivName();

            List<tagsModel> tagList1 = new ArrayList<>();

            String url;
            url = "https://olmatix.com/wamonitoring/get_tags_data_all_by_name.php";

            //Log.d(TAG, "getTagsbyName: " + " " + name);

            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");

                    //Log.d(TAG, "respnse json: "+response);
                    //tagList1.clear();

                    if (result_code.equals("1")) {

                        JSONArray cast = jsonResponse.getJSONArray("tags");
                        for (int is = 0; is < cast.length(); is++) {
                            JSONObject tags_name = cast.getJSONObject(is);
                            String tid = tags_name.getString("tid");
                            String tag_name = tags_name.getString("tag_name");
                            String tag_location = tags_name.getString("tag_location");
                            String division_name = tags_name.getString("division_name");
                            String time_interval = tags_name.getString("time_interval");
                            String type = tags_name.getString("type");

                            tagsModel tags = new tagsModel(tid, tag_name, tag_location, division_name, time_interval, type);
                            tagList1.add(tags);
                            //Log.d(TAG, " sections " + name + " add Data: " + tag_name + " div " + division_name);
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
                    MyData.put("divname", name);

                    return MyData;
                }
            };


            NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);

            sectionAdapter.addSection(new TagListSection(String.valueOf(division_nameHeader), tagList1, mContext));
            pDialog.cancel();
        }

        RecyclerView sectionHeader = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ListTagsActivity.this);
        sectionHeader.setLayoutManager(linearLayoutManager);
        sectionHeader.setHasFixedSize(true);
        sectionHeader.setAdapter(sectionAdapter);

        sectionHeader.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                /*if (newState == RecyclerView.SCROLL_STATE_IDLE) {


                }*/
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Write your logic here
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class TagListSection extends StatelessSection {

        String title;
        List<tagsModel> list;
        Context mContext;
        SharedPreferences sharedPref;
        private int position1;
        private int UNSELECTED = -1;
        private int selectedItem = UNSELECTED;
        boolean expanded = true;


        TagListSection(String title, List<tagsModel> list, Context mContext) {
            super(new SectionParameters.Builder(R.layout.frag_item_list_tags)
                    .headerResourceId(R.layout.section_tagslist_header)
                    .build());

            this.title = title;
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

            final tagsModel mTagModel = list.get(position);
            this.position1 = position;

            if (position==0){
                if (itemHolder != null) {
                    itemHolder.expandBut.setSelected(false);
                    itemHolder.expandableLayout.collapse();
                }
            }

            itemHolder.tag_id.setText(mTagModel.getTag_id());
            itemHolder.tag_name.setText(mTagModel.getTag_name());
            //itemHolder.tag_division_name.setText((CharSequence) mTagModel.getTag_division_name());

            String locSplit[] = mTagModel.getTag_location().split(",");
            String locLat = locSplit[0];
            String locLot = locSplit[1];

            double latitude = Double.parseDouble(locLat);
            double longitude = Double.parseDouble(locLot);

            final Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            String adString = null;
            try {
                List<Address> list;
                list = geocoder.getFromLocation(latitude, longitude, 1);
                if (list != null && list.size() > 0) {
                    Address address = list.get(0);
                    address.getLocality();

                    if (address.getAddressLine(0) != null)
                        adString = address.getAddressLine(0);

                }


            } catch (final IOException e) {
                new Thread(() -> Log.e("DEBUG", "Geocoder ERROR", e)).start();
            }


            itemHolder.tag_location.setText(adString);
            itemHolder.tag_timer.setText(mTagModel.getTag_timer());

            itemHolder.tag.setOnClickListener(view -> {
                Intent i = new Intent(mContext, BarcodeGenerator.class);
                i.putExtra("barcode", mTagModel.getTag_id());
                i.putExtra("barname", mTagModel.getTag_name());
                i.putExtra("division", mTagModel.getTag_division_name());
                i.putExtra("loc", mTagModel.getTag_location());

                mContext.startActivity(i);
            });

            itemHolder.print.setOnClickListener(view -> {
                Intent i = new Intent(mContext, BarcodeGenerator.class);
                i.putExtra("barcode", mTagModel.getTag_id());
                i.putExtra("barname", mTagModel.getTag_name());
                i.putExtra("division", mTagModel.getTag_division_name());
                i.putExtra("loc", mTagModel.getTag_location());

                mContext.startActivity(i);
            });

            itemHolder.write.setOnClickListener(view -> {
                Intent i = new Intent(mContext, writeNFCActivity.class);
                i.putExtra("barcode", mTagModel.getTag_id());
                i.putExtra("barname", mTagModel.getTag_name());
                i.putExtra("division", mTagModel.getTag_division_name());
                i.putExtra("loc", mTagModel.getTag_location());

                mContext.startActivity(i);

            });

            itemHolder.checklist.setOnClickListener(view -> {
                final EditText checklist_name = new EditText(mContext);
                checklist_name.setHint("Checklist name");
                checklist_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                checklist_name.setFilters( new InputFilter[] {new InputFilter.LengthFilter(15)});
                checklist_name.setMaxLines(1);

                LinearLayout layout = new LinearLayout(mContext);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(checklist_name);

                new AlertDialog.Builder(mContext)
                        .setTitle("Add tags checklist")
                        .setMessage("Please type your checklist name")
                        .setView(layout)
                        .setPositiveButton("SUBMIT", (dialog, which) -> {

                            String ichecklist_name = checklist_name.getText().toString();
                            String itid = mTagModel.getTag_id();

                            itemHolder.expand = 0;

                            sendJsonAdmin(ichecklist_name,itid, view);

                        }).setNegativeButton("CANCEL", (dialog, whichButton) -> {
                        }).show();
            });

            itemHolder.expandBut.setOnClickListener(view -> {

                if (itemHolder != null) {
                    itemHolder.expandBut.setSelected(false);
                    itemHolder.expandableLayout.collapse();
                }

                if (position1 == selectedItem) {
                    selectedItem = UNSELECTED;
                } else {
                    itemHolder.expandBut.setSelected(true);
                    itemHolder.expandableLayout.expand();
                    selectedItem = position1;

                    itemHolder.loadingCklist.setVisibility(View.VISIBLE);

                    if (itemHolder.expand==0){

                        itemHolder.cklist.setText("");

                        RequestQueue MyRequestQueue = Volley.newRequestQueue(mContext);

                        String url = "https://olmatix.com/wamonitoring/get_checklist.php";
                        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {

                            JSONObject jsonResponse = null;
                            try {

                                jsonResponse = new JSONObject(response);
                                JSONObject jObject = new JSONObject(response);
                                String result_code = jObject.getString("success");
                                if (result_code.equals("1")) {
                                    itemHolder.loadingCklist.setVisibility(View.GONE);

                                    JSONArray cast = jsonResponse.getJSONArray("checklist");
                                    final ArrayList<String> cklist1 = new ArrayList<>();
                                    cklist1.clear();
                                    for (int i = 0; i < cast.length(); i++) {
                                        JSONObject tags_name = cast.getJSONObject(i);
                                        //String tclid = tags_name.getString("tclid");
                                        String checklist = tags_name.getString("checklist");
                                        cklist1.add(checklist)  ;
                                        //Log.d("DEBUG", "setDataList: "+checklist);
                                    }
                                    //Log.d("DEBUG", "onClick: "+cklist1.size());
                                    while ( itemHolder.i<cklist1.size() ) {
                                        itemHolder.cklist.setText(itemHolder.cklist.getText() + cklist1.get(itemHolder.i) + " | ");
                                        itemHolder.i++;
                                    }
                                    itemHolder.expand = 1;

                                } else {
                                    itemHolder.loadingCklist.setVisibility(View.GONE);
                                    itemHolder.cklist.setText("Empty - No checklist");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                            protected Map<String, String> getParams() {
                                Map<String, String> MyData = new HashMap<>();
                                MyData.put("tid", mTagModel.getTag_id());

                                return MyData;
                            }
                        };

                        int socketTimeout = 60000;//30 seconds - change to what you want
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        MyStringRequest.setRetryPolicy(policy);
                        MyRequestQueue.add(MyStringRequest);
                    } else {
                        itemHolder.loadingCklist.setVisibility(View.GONE);

                    }

                }
            });

            itemHolder.rootView.setOnClickListener(v -> {


            });
        }

        @Override
        public RecyclerView.ViewHolder getHeaderViewHolder(View view) {
            return new HeaderViewHolder(view);
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

            headerHolder.tvTitle.setText(title);
            headerHolder.expandableLayout.expand();

            headerHolder.imgExp.setOnClickListener(v -> {
                if (headerHolder != null) {
                    headerHolder.imgExp.setSelected(false);
                    headerHolder.expandableLayout.collapse();
                }

                if (position1 == selectedItem) {
                    selectedItem = UNSELECTED;
                } else {
                    headerHolder.imgExp.setSelected(true);
                    headerHolder.expandableLayout.expand();
                    selectedItem = position1;
                }
            });
            headerHolder.expandableLayout.collapse();

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
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvTitle;
        private final ImageView imgExp;
        ExpandableLayout expandableLayout;
        private final View rootView;

        HeaderViewHolder(View view) {
            super(view);
            rootView = view;

            tvTitle = (TextView) view.findViewById(R.id.tvTitle);
            imgExp = (ImageView) view.findViewById(R.id.test);
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
        }
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {

        private final View rootView;
        TextView tag_id, tag_name, tag_location, tag_timer, cklist;
        ImageView tag;
        Button print, write, checklist;
        ImageButton expandBut;
        ExpandableLayout expandableLayout;
        int i = 0;
        ProgressBar loadingCklist;
        int expand = 0;

        ItemViewHolder(View view) {
            super(view);

            rootView = view;
            tag_id = (TextView) view.findViewById(R.id.tag_id);
            tag_name = (TextView) view.findViewById(R.id.tag_name);
            //tag_division_name = (TextView) view.findViewById(R.id.tag_div);
            tag_timer = (TextView) view.findViewById(R.id.tag_time);
            tag_location = (TextView) view.findViewById(R.id.tag_location);
            tag = (ImageView) view.findViewById(R.id.tag);
            print = (Button) view.findViewById(R.id.print);
            write = (Button) view.findViewById(R.id.write);
            checklist = (Button) view.findViewById(R.id.checklist);
            expandBut = (ImageButton) view.findViewById(R.id.opt);
            expandableLayout = (ExpandableLayout) view.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            cklist = (TextView) view.findViewById(R.id.cklist);
            loadingCklist = (ProgressBar) view.findViewById(R.id.progress);
        }
    }

    private void sendJsonAdmin(String jChecklist, String jItid, View v){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(mContext);

        String url = "https://olmatix.com/wamonitoring/insert_checklist.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Log.d("DEBUG", "onResponse: "+response);
            parsingJson(response, v);
        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("tid", jItid);
                MyData.put("company_id", mCompanyID);
                MyData.put("checklist", jChecklist);

                return MyData;
            }
        };

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        MyStringRequest.setRetryPolicy(policy);
        MyRequestQueue.add(MyStringRequest);


    }

    public void parsingJson(String json, View v1) {
        try {
            JSONObject jObject = new JSONObject(json);

            //String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");

            if (result_code.equals("1")) {


                TSnackbar snackbar = TSnackbar.make(v1,"Updating success",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();



            } else {


                TSnackbar snackbar = TSnackbar.make(v1,"Updating failed",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
