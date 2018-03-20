package com.lesjaw.wamonitoring.ui.fragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.androidadvance.topsnackbar.TSnackbar;
import com.bumptech.glide.Glide;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ListTagsLogAdapter;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.ScalingScannerActivity;
import com.lesjaw.wamonitoring.ui.UploadFotoAcivity;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.EndlessRecyclerViewScrollListener;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import ng.max.slideview.SlideView;


public class Tags extends Fragment {

    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    public static final String TAG = "TagActivity";
    private CoordinatorLayout coordinatorLayout;
    private List<tagsDailyModel> tagList = new ArrayList<>();
    private ListTagsLogAdapter mAdapter;
    private String mUser;
    private String mDivision;
    private String mCompanyID;
    private String real_name;
    private CircleImageView foto;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private boolean isCanceled = false;
    private String adString = null;
    private ProgressBar loading;
    private int firstpage = 10;
    private EndlessRecyclerViewScrollListener scrollListener;
    RelativeLayout RLMain;
    boolean runPanic = false;
    StyleableToast styleableToast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_tag, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageButton nfctag = (ImageButton) view.findViewById(R.id.nfctag);
        ImageButton codetag = (ImageButton) view.findViewById(R.id.codetag);
        TextView user = (TextView) view.findViewById(R.id.user);
        foto = (CircleImageView) view.findViewById(R.id.img_profile);
        loading = (ProgressBar) view.findViewById(R.id.loading);

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.main_content);
        RLMain = (RelativeLayout) view.findViewById(R.id.RLMain);
        new OkHttpClient();

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        mPrefHelper = new PreferenceHelper(getContext());
        mUser = sharedPref.getString("email", "jakarta");
        mDivision = sharedPref.getString("division", "jakarta");
        mCompanyID = sharedPref.getString("company_id", "olmatix1");
        real_name = sharedPref.getString("real_name", "olmatix1");
        String mDivision1 = mPrefHelper.getDivName();

        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);
        mRecycleView.setHasFixedSize(true);

        mAdapter = new ListTagsLogAdapter(tagList, getContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        getData("0");

        codetag.setOnClickListener(view1 -> {
            if (mPrefHelper.getLogin().equals("1")) {
                Intent i = new Intent(getContext(), ScalingScannerActivity.class);
                startActivity(i);
            } else {
                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf("Sorry, you can't tag when you are not Working"))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
        });

        nfctag.setOnClickListener(v -> {

            if (mPrefHelper.getLogin().equals("1")) {

                String url = Config.DOMAIN + "wamonitoring/get_last_panic_data.php";

                StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                    try {
                        JSONObject jObject = new JSONObject(response);
                        String result_code = jObject.getString("success");
                        if (result_code.equals("1")) {

                            JSONArray cast = jObject.getJSONArray("panic");

                            for (int i = 0; i < cast.length(); i++) {
                                JSONObject tags_name = cast.getJSONObject(i);
                                String dateLast = tags_name.getString("dateLast");

                                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "You have used panic button for today" +
                                        " at " + dateLast, TSnackbar.LENGTH_INDEFINITE);
                                View snackbarView = snackbar.getView();
                                snackbarView.setBackgroundColor(Color.parseColor("#e6ffe6"));
                                snackbar.setAction("OK", vi -> {

                                });
                                snackbar.setActionTextColor(Color.BLACK);
                                snackbar.show();
                            }

                        } else {

                            // custom dialog
                            final Dialog dialog = new Dialog(getContext());
                            dialog.setContentView(R.layout.panic_button);
                            isCanceled = false;

                            TextView count = (TextView) dialog.findViewById(R.id.count);
                            SlideView slideView = (SlideView) dialog.findViewById(R.id.slideView);
                            Button cancel = (Button) dialog.findViewById(R.id.cancelBut);

                            cancel.setOnClickListener(v1 -> {
                                isCanceled = true;
                                dialog.dismiss();

                            });
                            slideView.setOnSlideCompleteListener(new SlideView.OnSlideCompleteListener() {

                                long millisInFuture = 10000; //25 seconds
                                long countDownInterval = 1000; //1 second

                                @Override
                                public void onSlideComplete(SlideView slideView) {
                                    new CountDownTimer(millisInFuture, countDownInterval) {
                                        public void onTick(long millisUntilFinished) {
                                            if (isCanceled) {
                                                //If user requested to pause or cancel the count down timer
                                                cancel();
                                            } else {
                                                count.setText(millisUntilFinished / 1000 + " sec until broadcast");
                                                if (millisUntilFinished < 6000) {
                                                    count.setTextColor(Color.RED);
                                                    count.setTypeface(null, Typeface.BOLD);
                                                }
                                                //Put count down timer remaining time in a variable
                                                slideView.setEnabled(false);
                                            }

                                            //counter++;
                                        }

                                        public void onFinish() {
                                            isCanceled = false;
                                            sendNotification(mCompanyID + "-" + mDivision, mPrefHelper.getDivName() + " | PANIC! button", real_name, "PANIC!");
                                            if (!runPanic) {
                                                InsertPanic("PANIC! button");
                                            }
                                            dialog.dismiss();
                                            //count.setText("FINISH!!");

                                        }
                                    }.start();
                                }
                            });

                            dialog.show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<>();
                        MyData.put("email", mUser);
                        return MyData;
                    }
                };

                // Adding request to request queue
                //AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);
                NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);


            } else {
                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf("Sorry, you can't press Panic button when you are not Working"))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
        });

        user.setText(mUser + " - " + mDivision1);

        foto.setOnClickListener(v -> {
            if (mPrefHelper.getLogin().equals("1")) {
                Intent i = new Intent(getContext(), UploadFotoAcivity.class);
                startActivity(i);
            } else {
                styleableToast = new StyleableToast
                        .Builder(getContext())
                        .icon(R.drawable.ic_action_info)
                        .text(String.valueOf("Sorry, you can't upload photo when you are not Working"))
                        .textColor(Color.WHITE)
                        .backgroundColor(Color.BLUE)
                        .build();
                styleableToast.show();
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                //Log.d(TAG, "onReceive: " + intent);

               /* if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    *//*Log.d(TAG, "onReceive: " + user + " " + real_name);
                    if (!user.equals(real_name)) {
                        TSnackbar snackbar = TSnackbar.make(coordinatorLayout, message + " by " + user, TSnackbar.LENGTH_INDEFINITE);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#e6ffe6"));
                        snackbar.setAction("OK", v -> {

                        });
                        snackbar.setActionTextColor(Color.BLACK);
                        snackbar.show();
                    }*//*
                }*/
            }
        };

        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                // Triggered only when new data needs to be appended to the list
                // Add whatever code is needed to append new items to the bottom of the list
                Log.d(TAG, "last position and firtpage is " + firstpage);
                /*if (countLoad==1){
                    firstpage = 10;
                } else */
                loadNextDataFromApi(String.valueOf(firstpage));
                firstpage = firstpage + 10;
            }
        };

        mRecycleView.addOnScrollListener(scrollListener);


    }

    public void loadNextDataFromApi(String page) {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String mEmail = sharedPref.getString("email", "olmatix1");

        //String tag_json_obj = "Tags-getData";

        String url = Config.DOMAIN + "wamonitoring/get_tags_record_byemail.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

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

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc, division_name, "", tgl, jam,
                                tid, checklist_done, trid, email, after_foto, before_foto,latitude, longitude);
                        tagList.add(tags);
                        //Log.d(TAG, "setData: "+employee_name);
                    }
                    mAdapter.notifyDataSetChanged();

                    loading.setVisibility(View.GONE);

                } else {
                    mAdapter.notifyDataSetChanged();

                    loading.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                loading.setVisibility(View.GONE);

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);
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
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mMessageReceiver, new IntentFilter("tags"));
        tagList.clear();
        mAdapter.notifyDataSetChanged();
        scrollListener.resetState();
        firstpage = 10;
        getData("0");
        //GetPhoto();
        getPhotoProfileSdcard(mUser);
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mRegistrationBroadcastReceiver, new IntentFilter(Config.PUSH_NOTIFICATION));
    }

    @Override
    public void onPause() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mRegistrationBroadcastReceiver);

        super.onPause();
    }

    private void getData(String page) {

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String mEmail = sharedPref.getString("email", "olmatix1");

        String url = Config.DOMAIN + "wamonitoring/get_tags_record_byemail.php";

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

                        tagsDailyModel tags = new tagsDailyModel(employee_name, tag_name, range_loc, division_name, "", tgl, jam,
                                tid, checklist_done, trid, email, after_foto, before_foto,latitude,longitude);
                        tagList.add(tags);
                        //Log.d(TAG, "setData: "+employee_name);
                    }
                    mAdapter.notifyDataSetChanged();

                    loading.setVisibility(View.GONE);

                } else {
                    loading.setVisibility(View.GONE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
                loading.setVisibility(View.GONE);

            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);
                MyData.put("offsett", page);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("notify");
            Log.d("DEBUG", "onReceive1: " + message);
            if (message != null) {


                if (message.equals("Checklist update success")) {
                    getData("0");
                    TSnackbar snackbar = TSnackbar.make(coordinatorLayout, "Update success", TSnackbar.LENGTH_LONG);
                    View snackbarView = snackbar.getView();
                    snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                    snackbar.setAction("OK", v -> {

                    });
                    snackbar.setActionTextColor(Color.BLACK);
                    snackbar.show();
                }
            }
        }
    };

    private void getPhotoProfileSdcard(String email) {
        String IMAGE_DIRECTORY = "/wamonitoring";
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + "/upload");
        File f = new File(wallpaperDirectory, email + ".jpg");
        String imageInSD = f.getAbsolutePath();
        Glide.with(getContext())
                .load(imageInSD)
                .into(foto);
    }

    /*private void GetPhoto() {
        String mEmail = sharedPref.getString("email", "olmatix1");
        String tag_json_obj = "login";

        String url = Config.DOMAIN + "wamonitoring/get_foto_employee.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("message");

                JSONArray cast = jObject.getJSONArray("employee");
                String imageString = null;
                for (int i = 0; i < cast.length(); i++) {


                    JSONObject tags_name = cast.getJSONObject(i);
                    imageString = tags_name.getString("foto");
                    if (imageString.length() > 2) {

                        Glide.with(getContext())
                                .load(imageString)
                                .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                                .into(foto);
                    } else {
                        foto.setImageResource(R.drawable.user);

                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("email", mEmail);
                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }*/

    private void sendNotification(final String reg_token, String body, String title, String imgUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("yy-d-MM hh:mm:ss", Locale.ENGLISH);
                    String times = timeformat.format(System.currentTimeMillis());

                    JSONObject root = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject payload = new JSONObject();

                    payload.put("title", title);
                    payload.put("is_background", false);
                    payload.put("message", body);
                    payload.put("image", imgUrl);
                    payload.put("timestamp", times);

                    data.put("data", payload);
                    root.put("data", data);
                    root.put("to", "/topics/" + reg_token);

                    RequestBody body = RequestBody.create(JSON, root.toString());
                    com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                            .header("Authorization", "key=AIzaSyAwU7DMeeysvpQjcwZsS3hJFfx8wWcrpNU")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d(TAG, "doInBackground: " + finalResponse);


                } catch (Exception e) {
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }

    private void InsertPanic(String note) {

        runPanic = true;

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mEmployeeName = sharedPref.getString("real_name", "olmatix1");
        String mEmail = sharedPref.getString("email", "olmatix1");
        String mDivision = sharedPref.getString("division", "olmatix1");

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        String tgl = df.format(Calendar.getInstance().getTime());

        String lat = String.valueOf(mPrefHelper.getPhoneLatitude());
        String lot = String.valueOf(mPrefHelper.getPhoneLongitude());

        final Geocoder geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
        try {
            List<Address> list;
            list = geocoder.getFromLocation(mPrefHelper.getPhoneLatitude(), mPrefHelper.getPhoneLongitude(), 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                address.getLocality();

                if (address.getAddressLine(0) != null)
                    adString = address.getAddressLine(0);
            }
        } catch (final IOException e) {
            new Thread(() -> {
                Log.e("DEBUG", "Geocoder ERROR", e);
                adString = lat + ", " + lot;
            }).start();
        }

        if (adString==null){
            adString = lat + ", " + lot;
        }

        String url = Config.DOMAIN + "wamonitoring/insert_panic.php";

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    //Log.d(TAG, "location insert success ");

                    Intent pushNotification = new Intent(Config.SEND_NOTICE);
                    pushNotification.putExtra("message", "sending notice");
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(pushNotification);

                    runPanic = false;

                } else {
                    //Log.d(TAG, "location insert fail ");
                    runPanic = false;

                }

            } catch (JSONException e) {
                e.printStackTrace();
                runPanic = false;

            }

        }, error -> Log.d(TAG, "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("email", mEmail);
                MyData.put("employee_name", mEmployeeName);
                MyData.put("company_id", mCompanyID);
                MyData.put("date_created", tgl);
                MyData.put("notes", note);
                MyData.put("division", mDivision);
                MyData.put("latitude", lat);
                MyData.put("longitude", lot);
                MyData.put("lokasi", adString);
                MyData.put("type", "0");

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);

    }
}
