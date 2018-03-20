package com.lesjaw.wamonitoring.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.github.chrisbanes.photoview.PhotoView;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.commentChecklistAdapter;
import com.lesjaw.wamonitoring.model.commentChecklistModel;
import com.lesjaw.wamonitoring.utils.Config;
import com.squareup.picasso.Picasso;

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

import de.hdodenhof.circleimageview.CircleImageView;


public class NoticeDetail extends AppCompatActivity {
    SharedPreferences sharedPref;
    private RecyclerView mRecycleView;
    private commentChecklistAdapter mAdapter;
    private List<commentChecklistModel> tagList = new ArrayList<>();
    private String idComment;
    boolean running = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_item_notice_detail);

        CircleImageView img = (CircleImageView) findViewById(R.id.img_profile);
        PhotoView ImageView = (PhotoView) findViewById(R.id.ImageView);
        TextView tgl = (TextView) findViewById(R.id.tgl);
        TextView note = (TextView) findViewById(R.id.note);
        FloatingActionButton commen = (FloatingActionButton) findViewById(R.id.ButComment);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String companyname = sharedPref.getString("company_name", "olmatix1");
        String myEmail = sharedPref.getString("email", "olmatix1");
        String myName = sharedPref.getString("real_name", "olmatix1");
        getSupportActionBar().setTitle(companyname);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        String mEmail = i.getStringExtra("email");
        String employeName = i.getStringExtra("EmployeName");
        String image = i.getStringExtra("imageUrl");
        String tglI = i.getStringExtra("tgl");
        String noteI = i.getStringExtra("note");
        idComment = i.getStringExtra("id");

        Picasso.with(getBaseContext())
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mEmail + ".jpg")
                .into(img);

        if (!image.isEmpty()) {
            Picasso.with(getBaseContext())
                    .load(image)
                    .into(ImageView);
        } else {
            ImageView.setVisibility(View.GONE);
        }

        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(tglI);
            long milliseconds = date.getTime();
            long updatetime = Long.parseLong(String.valueOf(milliseconds));

            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();

            tgl.setText("By " + employeName + " at " + simpleDateFormat2.format(updatetime));

        } catch (ParseException e) {
            e.printStackTrace();
        }
        note.setText(noteI);

        mRecycleView = (RecyclerView) findViewById(R.id.rv);
        mAdapter = new commentChecklistAdapter(tagList, this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(layoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        commen.setOnClickListener(v -> {
            final EditText pass = new EditText(NoticeDetail.this);
            pass.setHint("Type your comment");
            pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pass.setMaxLines(1);

            LinearLayout layout = new LinearLayout(NoticeDetail.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(pass);

            new AlertDialog.Builder(NoticeDetail.this)
                    .setTitle("Alert/Log comment")
                    .setIcon(R.drawable.ic_info_black_24dp)
                    .setView(layout)
                    .setPositiveButton("Send", (dialog, id) -> {

                        if (!running) {
                            running = true;
                            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
                            String tgl1 = df.format(Calendar.getInstance().getTime());

                            String url = Config.DOMAIN + "wamonitoring/insert_comment_panic_log.php";

                            StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {

                                JSONObject jObject = null;
                                try {
                                    jObject = new JSONObject(response);
                                    // String result_json = jObject.getString("message");
                                    String result_code = jObject.getString("success");

                                    if (result_code.equals("1")) {
                                        running = false;
                                        getTCRID(idComment);

                                    }

                                    running = false;


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }, error -> {
                                Log.d("DEBUG", "onErrorResponse: " + error);
                                running = false;

                            }) {
                                protected Map<String, String> getParams() {
                                    Map<String, String> MyData = new HashMap<>();
                                    MyData.put("tcrid", idComment);
                                    MyData.put("date_update", tgl1);
                                    MyData.put("from_email", myEmail);
                                    MyData.put("comment", pass.getText().toString().trim());
                                    MyData.put("employee_name", myName);

                                    return MyData;
                                }
                            };

                            // Adding request to request queue
                            NetworkRequest.getInstance(NoticeDetail.this).addToRequestQueue(jsonObjReq);
                        }
                    })

                    .setNegativeButton("Cancel", (dialog, id) -> dialog.dismiss())

                    .show();
        });


    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getTCRID(idComment);
    }

    private void getTCRID(String tcrid) {

        String url = Config.DOMAIN + "wamonitoring/get_comment_for_panic_log.php";
        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("comment");
                    tagList.clear();
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String comment = tags_name.getString("comment");
                        String from_email = tags_name.getString("from_email");
                        String employee_name = tags_name.getString("employee_name");
                        String date_update = tags_name.getString("date_update");
                        String tcrid1 = tags_name.getString("tcrid");

                        commentChecklistModel tags = new commentChecklistModel(comment, from_email, employee_name,
                                date_update, tcrid1);

                        tagList.add(tags);
                        //Log.d("DEBUG", "getCommentChecklist: "+comment);

                    }

                    mRecycleView.setAdapter(mAdapter);

                }

                mRecycleView.setAdapter(mAdapter);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("tcrid", tcrid);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(this).addToRequestQueue(jsonObjReq);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}