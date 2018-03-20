package com.lesjaw.wamonitoring.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.VoiceInteractor;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.AbsenceModel;
import com.lesjaw.wamonitoring.model.EmployeeAndEmail;
import com.lesjaw.wamonitoring.model.EmployeeModel;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.ui.FragmentContainer;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class UserMainAdapter extends RecyclerView.Adapter<UserMainAdapter.MainViewHolder> {
    private List<EmployeeModel> nodeList;
    Context context;
    private PreferenceHelper mPrefHelper;
    private SharedPreferences sharedPref;

    private String mLevelUser;
    private StyleableToast styleableToast;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    class MainViewHolder extends RecyclerView.ViewHolder {

        ImageView name;
        CircleImageView imgprofile;
        BoomMenuButton bmb;

        public MainViewHolder(View itemView) {
            super(itemView);
            name = (ImageView) itemView.findViewById(R.id.imgStatus);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            bmb = (BoomMenuButton) itemView.findViewById(R.id.bmb);
            mPrefHelper = new PreferenceHelper(context);
            mLevelUser = mPrefHelper.getLevelUser();
            sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        }

    }

    public UserMainAdapter(List<EmployeeModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_user_main, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final EmployeeModel mEmployeeModel = nodeList.get(position);

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mEmployeeModel.getEmail() + ".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        if (mEmployeeModel.getLog_status().equals("1")) {
            holder.name.setImageResource(android.R.drawable.presence_online);
        } else if (mEmployeeModel.getLog_status().equals("2")) {
            holder.name.setImageResource(android.R.drawable.presence_offline);
        } else {
            holder.name.setImageResource(android.R.drawable.presence_invisible);
        }

        holder.bmb.clearBuilders();
        for (int i = 0; i < holder.bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();

            if (i == 0) {
                builder.normalImageRes(R.drawable.ic_face_black_48dp)
                        .normalText((mEmployeeModel.getReal_name() + " details"))
                        .subNormalText("Click to see employee details");
            }

            if (i == 1) {
                builder.normalImageRes(R.mipmap.ic_check_black)
                        .normalText("Checklist by "+mEmployeeModel.getReal_name())
                        .subNormalText("Click to see employee checklist");
            }
            if (i == 2) {
                builder.normalImageRes(R.mipmap.barcode_img)
                        .normalText("Tags by "+mEmployeeModel.getReal_name())
                        .subNormalText("Click to see employee tags");
            }

            if (i == 3) {
                builder.normalImageRes(R.mipmap.ic_phone)
                        .normalText("Call "+mEmployeeModel.getReal_name())
                        .subNormalText("Click to call employee");
            }

            if (i == 4) {
                builder.normalImageRes(R.mipmap.ic_send)
                        .normalText("Send Notification to "+mEmployeeModel.getReal_name())
                        .subNormalText("Click to tell employee to open EMP-monitoring");
            }


            builder.listener(new OnBMClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBoomButtonClick(int index) {
                    // When the boom-button corresponding this builder is clicked.
                    if (index == 0) {

                        Intent i = new Intent(context, EmployeeProfile.class);
                        i.putExtra("email", mEmployeeModel.getEmail());
                        View sharedView = holder.imgprofile;
                        String transitionName = context.getString(R.string.imgProfile);
                        ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context, sharedView, transitionName);
                        context.startActivity(i, transitionActivityOptions.toBundle());

                    }

                    if (index == 1) {
                        if (!mLevelUser.equals("2")) {
                            Intent i = new Intent(context, FragmentContainer.class);
                            i.putExtra("id_fragement", "Checklist Employee");
                            i.putExtra("Memail", mEmployeeModel.getEmail());
                            i.putExtra("nama", mEmployeeModel.getReal_name());
                            context.startActivity(i);
                        } else {
                            styleableToast = new StyleableToast
                                    .Builder(context)
                                    .icon(R.drawable.ic_action_info)
                                    .text("Sorry, you are not allowed to use this")
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.BLUE)
                                    .build();
                            styleableToast.show();
                        }
                    }

                    if (index == 2) {
                        if (!mLevelUser.equals("2")) {

                            Intent i = new Intent(context, FragmentContainer.class);
                            i.putExtra("id_fragement", "Tags Employee");
                            i.putExtra("Memail", mEmployeeModel.getEmail());
                            i.putExtra("nama", mEmployeeModel.getReal_name());

                            context.startActivity(i);
                        } else {
                            styleableToast = new StyleableToast
                                    .Builder(context)
                                    .icon(R.drawable.ic_action_info)
                                    .text("Sorry, you are not allowed to use this")
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.BLUE)
                                    .build();
                            styleableToast.show();
                        }
                    }

                    if (index == 3) {
                        new AlertDialog.Builder(context)
                                .setTitle("Make a Call")
                                .setIcon(R.mipmap.ic_phone)
                                .setMessage("Do you want to make a call to " + mEmployeeModel.getReal_name() + "?" +
                                        "\nPhone1 " + mEmployeeModel.getPhone1() + "" +
                                        "\nPhone2 " + mEmployeeModel.getPhone2())
                                .setPositiveButton("Phone 1", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String phone_no = mEmployeeModel.getPhone1().replaceAll("-", "");
                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setPackage("com.android.server.telecom");
                                        callIntent.setData(Uri.parse("tel:" + phone_no));
                                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        context.startActivity(callIntent);

                                        dialog.dismiss();

                                    }
                                })

                                .setNegativeButton("Phone 2", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        String phone_no = mEmployeeModel.getPhone2().replaceAll("-", "");
                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
                                        callIntent.setPackage("com.android.server.telecom");
                                        callIntent.setData(Uri.parse("tel:" + phone_no));
                                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                            // TODO: Consider calling
                                            //    ActivityCompat#requestPermissions
                                            // here to request the missing permissions, and then overriding
                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                            //                                          int[] grantResults)
                                            // to handle the case where the user grants the permission. See the documentation
                                            // for ActivityCompat#requestPermissions for more details.
                                            return;
                                        }
                                        context.startActivity(callIntent);
                                        dialog.dismiss();
                                    }
                                })

                                .show();
                    }

                    if (index == 4) {
                        sendFCMPush(mEmployeeModel.getFirebaseID());
                    }
                }
            });
            holder.bmb.addBuilder(builder);
        }

        holder.bmb.setVisibility(View.INVISIBLE);

        holder.imgprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.bmb.boom();
            }
        });

    }

    @Override
    public int getItemCount() {
        //Log.d("DEBUG", "getItemCount: "+nodeList.size());
        return nodeList.size();
    }

    private void sendFCMPush(String token) {
        String url = "https://fcm.googleapis.com/fcm/send";
        String Legacy_SERVER_KEY = "AIzaSyAwU7DMeeysvpQjcwZsS3hJFfx8wWcrpNU";
        String msg = "Open EMP please";
        String title = sharedPref.getString("real_name","");
        //String token = FCM_RECEIVER_TOKEN;

        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;

        try {
            obj = new JSONObject();
            objData = new JSONObject();

            objData.put("body", msg);
            objData.put("title", title);
            objData.put("sound", "default");
            objData.put("icon", "logo"); //   icon_name image must be there in drawable
            objData.put("tag", token);
            objData.put("priority", "high");

            dataobjData = new JSONObject();
            dataobjData.put("text", msg);
            dataobjData.put("title", title);

            obj.put("to", token);
            //obj.put("priority", "high");

            obj.put("notification", objData);
            obj.put("data", dataobjData);
            Log.e("!_@rj@_@@_PASS:>", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,url, obj,
                new com.android.volley.Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("!_@@_SUCESS", response + "");
                    }
                },
                new com.android.volley.Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("!_@@_Errors--", error + "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "key=" + Legacy_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }
}
