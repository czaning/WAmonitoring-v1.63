package com.lesjaw.wamonitoring.adapter;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.checklistModel;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.ui.UploadFotoTags;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

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

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListTagsLogAdapter extends RecyclerView.Adapter<ListTagsLogAdapter.MainViewHolder> {
    private List<tagsDailyModel> nodeList;
    Context context;
    SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;

    private List<checklistModel> tagList = new ArrayList<>();
    CheckListAdapter mAdapter;
    private ProgressDialog pDialog;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient mClient;

    class MainViewHolder extends RecyclerView.ViewHolder {
        String cklistBoolean;
        TextView tag_name, tgl  ;
        ImageView cklist;
        ImageView before_photo, after_photo;
        CardView cardView;
        LinearLayout liner;

        public MainViewHolder(View itemView) {
            super(itemView);
            tag_name = (TextView) itemView.findViewById(R.id.tag_name);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            cklist = (ImageView)itemView.findViewById(R.id.cklist);
            before_photo = (ImageView)itemView.findViewById(R.id.before_photo);
            after_photo = (ImageView)itemView.findViewById(R.id.after_photo);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            liner = (LinearLayout) itemView.findViewById(R.id.liner);

        }

    }

    public ListTagsLogAdapter(List<tagsDailyModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_log_user, parent, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefHelper = new PreferenceHelper(context);

        mClient = new OkHttpClient();

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final tagsDailyModel mTagModel = nodeList.get(position);

        if((position % 2 == 0)){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#cce6ff"));
        }else{
            holder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));
        }

        holder.cklistBoolean = mTagModel.getChecklist_done();

        holder.after_photo.setVisibility(View.INVISIBLE);
        holder.before_photo.setVisibility(View.INVISIBLE);

        if (holder.cklistBoolean.equals("0")){
            holder.cklist.setImageResource(android.R.drawable.checkbox_off_background);
        } else {
            holder.cklist.setImageResource(android.R.drawable.checkbox_on_background);
        }

        holder.tag_name.setText(mTagModel.getTag_name());

        //String tglTag = mTagModel.getTgl();
        String input = mTagModel.getTgl()+" "+mTagModel.getJam();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(input);
            long milliseconds = date.getTime();
            long updatetime = Long.parseLong(String.valueOf(milliseconds));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(updatetime));
            cal.getTimeInMillis();
            if (Utils.getTimeAgo(cal).contains("Days ago")){
                holder.tgl.setText(simpleDateFormat2.format(updatetime));

            } else {
                holder.tgl.setText(Utils.getTimeAgo(cal) + " | "+simpleDateFormat1.format(updatetime));

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.cklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.cklistBoolean.equals("0")) {
                    pDialog = new ProgressDialog(context);
                    pDialog.setMessage("Getting tag checklist. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();
                    GetChecklistItem(mTagModel.getTid(), mTagModel.getTrid(), holder.liner);

                } else {
                    sendMessageDetail("You have done doing this tag..");

                }
            }
        });

        holder.tgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UploadFotoTags.class);
                i.putExtra("trid", mTagModel.getTrid());
                context.startActivity(i);
            }
        });

        String after = mTagModel.getAfter_photo();
        String before = mTagModel.getBefore_photo();

        //Log.d("DEBUG", "onBindViewHolder: "+mTagModel.getEmployee_name()+" "+after);

        if (!before.isEmpty() || !before.equals("")) {
            holder.before_photo.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load("https://olmatix.com/wamonitoring/uploads/thumb_" + mTagModel.getTrid() + "-before_foto" + ".jpg")
                    .asBitmap()
                    .into(new BitmapImageViewTarget(holder.before_photo) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(),
                                    Bitmap.createScaledBitmap(resource, 50, 50, false));
                            drawable.setCircular(true);
                            holder.before_photo.setImageDrawable(drawable);
                        }
                    });

            holder.before_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ImageViewer.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("img", "https://olmatix.com/wamonitoring/uploads/"+mTagModel.getTrid()+"-before_foto"+".jpg");
                    i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/"+mTagModel.getEmail()+".jpg");

                    context.startActivity(i);
                }
            });

        }

        if (!after.isEmpty() || !after.equals("")) {
            holder.after_photo.setVisibility(View.VISIBLE);

            Glide.with(context)
                    .load("https://olmatix.com/wamonitoring/uploads/thumb_" + mTagModel.getTrid() + "-after_foto" + ".jpg")
                    .asBitmap()
                    .into(new BitmapImageViewTarget(holder.after_photo) {
                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(),
                                    Bitmap.createScaledBitmap(resource, 50, 50, false));
                            drawable.setCircular(true);
                            holder.after_photo.setImageDrawable(drawable);
                        }
                    });


            holder.after_photo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(context, ImageViewer.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("img", "https://olmatix.com/wamonitoring/uploads/" + mTagModel.getTrid() + "-after_foto" + ".jpg");
                    i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/" + mTagModel.getEmail() + ".jpg");

                    context.startActivity(i);
                }
            });
        }
    }

    private void sendMessageDetail(String Notify) {
        Intent intent = new Intent("tags");
        intent.putExtra("notify", Notify);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    private void GetChecklistItem (String tagID,String tagRID, View liner){
        String mCompanyID = sharedPref.getString("company_id", "olmatix1");
        String mDivision = sharedPref.getString("division", "olmatix1");
        String mEmployee_name = sharedPref.getString("real_name", "olmatix1");

        String tag_json_obj = "get_checklist";

        String url = Config.DOMAIN+"wamonitoring/get_checklist.php";


        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals(1)||result_code.equals("1")) {

                    JSONArray cast = jsonResponse.getJSONArray("checklist");
                    tagList.clear();
                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String tclid = tags_name.getString("tclid");
                        String checklist = tags_name.getString("checklist");

                        checklistModel tags = new checklistModel(tclid, checklist, "");
                        tagList.add(tags);
                        //Log.d("DEBUG", "setData: "+checklist);
                        pDialog.dismiss();
                    }

                    final Dialog dialog = new Dialog(context);
                    LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View view = li.inflate(R.layout.list_checklist, null);

                    ListView lv = (ListView) view.findViewById(R.id.ltchecklist);
                    Button submitBut = (Button) view.findViewById(R.id.submit);
                    mAdapter = new CheckListAdapter(context,tagList, tagRID);
                    lv.setAdapter(mAdapter);

                    submitBut.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mAdapter.checkCklist();

                            mAdapter.PopulateAll(tagRID, liner);
                            sendNotification(mCompanyID, mPrefHelper.getDivName(), mEmployee_name, "TAGS");

                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(view);
                    dialog.show();


                } else {
                    pDialog.dismiss();

                }
            } catch (JSONException e) {
                e.printStackTrace();
                pDialog.dismiss();

            }
        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("tid", tagID);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(context).addToRequestQueue(jsonObjReq);
    }

    private void noChecklist (){

    }

    private void sendNotification(final String reg_token, String body, String title, String imgUrl) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("hh:mm");
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
                    Log.d("DEBUG", "doInBackground: " + finalResponse);


                } catch (Exception e) {
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }


}
