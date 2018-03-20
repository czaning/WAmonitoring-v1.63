package com.lesjaw.wamonitoring.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.EmployeeModel;
import com.lesjaw.wamonitoring.ui.BarcodeGenerator;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.ui.FragmentContainer;
import com.lesjaw.wamonitoring.ui.MapsActivity;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomButtons.OnBMClickListener;
import com.nightonke.boommenu.BoomMenuButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListEmployeeAdapter extends RecyclerView.Adapter<ListEmployeeAdapter.MainViewHolder> {
    private List<EmployeeModel> nodeList;
    Context context;
    private PreferenceHelper mPrefHelper;
    private String mLevelUser;
    private StyleableToast styleableToast;
    DatePickerDialog datePickerDialog;
    EditText eDateBirth;

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView real_name,place_date_birth,email,division, address, phone, lastseen, level_user;
        CircleImageView profile;
        BoomMenuButton bmb;
        CardView cardView;


        public MainViewHolder(View itemView) {
            super(itemView);
            real_name = (TextView) itemView.findViewById(R.id.employee_name);
            place_date_birth = (TextView) itemView.findViewById(R.id.place_dateBirth);
            division = (TextView) itemView.findViewById(R.id.employee_div);
            email = (TextView) itemView.findViewById(R.id.employee_email);
            address = (TextView) itemView.findViewById(R.id.employee_address);
            phone = (TextView) itemView.findViewById(R.id.phone);
            lastseen = (TextView) itemView.findViewById(R.id.lastseen);
            level_user = (TextView) itemView.findViewById(R.id.level_user);
            profile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            bmb = (BoomMenuButton) itemView.findViewById(R.id.bmb);

            Calendar now = Calendar.getInstance();
            int mYear =  now.get(Calendar.YEAR);
            int mMonth = now.get(Calendar.MONTH);
            int mDay = now.get(Calendar.DAY_OF_MONTH);
            setDatePicker(mYear,mMonth,mDay);
            mPrefHelper = new PreferenceHelper(context);
            mLevelUser = mPrefHelper.getLevelUser();

        }

    }

    public ListEmployeeAdapter(List<EmployeeModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_employee, parent, false);

        return new MainViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final EmployeeModel mEmployeeModel = nodeList.get(position);

        SimpleDateFormat df = new SimpleDateFormat("EE, yyyy-MM-dd HH:mm");

        long updatetime = Long.parseLong(mEmployeeModel.getLastseen());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(updatetime));
        cal.getTimeInMillis();

        String logstatus = mEmployeeModel.getLog_status();

        if (logstatus.equals("1")){
            holder.real_name.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online,0,0,0);
        } else if (logstatus.equals("2")){
            holder.real_name.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline,0,0,0);

        } else {
            holder.real_name.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_invisible,0,0,0);

        }

        holder.real_name.setCompoundDrawablePadding(10);


        holder.real_name.setText(mEmployeeModel.getReal_name());
        holder.division.setText("Division : "+mEmployeeModel.getDivision()+"\nStatus : "+mEmployeeModel.getEmployment_status()+", Employment : "+mEmployeeModel.getEmployment());
        holder.email.setText(mEmployeeModel.getEmail()+" : "+mEmployeeModel.getPassword());
        holder.lastseen.setText(Html.fromHtml("<u>Last seen at " + Utils.getTimeAgo(cal) + " | "+df.format(updatetime)+"</u>" ));


        //decode base64 string to image
        String imageString = mEmployeeModel.getFoto();
        if (imageString.length()>2) {
            Picasso.with(context)
                    .load(mEmployeeModel.getFoto())
                    .into(holder.profile);
        } else {
            holder.profile.setImageResource(R.drawable.user);
        }


        if (mEmployeeModel.getLevel_user().equals("1")){
            holder.level_user.setVisibility(View.VISIBLE);
            holder.level_user.setText(mEmployeeModel.getDivision()+" Division Head");

        } else if (mEmployeeModel.getLevel_user().equals("2")) {
            holder.level_user.setVisibility(View.GONE);

        } else {
            holder.division.setText(Html.fromHtml("<u>Division : Administrator</u>"));

        }

        holder.bmb.clearBuilders();
        holder.bmb.setButtonVerticalMargin(20);

        for (int i = 0; i < holder.bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();


            if (i == 0) {
                builder.normalImageRes(R.drawable.ic_face_black_48dp)
                        .normalText((mEmployeeModel.getReal_name() + " details"))
                        .subNormalText("Click to see employee details");
            }

            if (i == 1) {
                builder.normalImageRes(R.mipmap.ic_check_black)
                        .normalText("Checklist by " + mEmployeeModel.getReal_name())
                        .subNormalText("Click to see employee checklist");
            }
            if (i == 2) {
                builder.normalImageRes(R.mipmap.barcode_img)
                        .normalText("Tags by " + mEmployeeModel.getReal_name())
                        .subNormalText("Click to see employee tags");

            }

            if (i == 3) {
                builder.normalImageRes(R.mipmap.ic_phone)
                        .normalText("Generato Barcode Absen " + mEmployeeModel.getReal_name())
                        .subNormalText("Click to call employee");
            }

            if (i == 4) {
                builder.normalImageRes(R.mipmap.ic_track)
                        .normalText("Track " + mEmployeeModel.getReal_name())
                        .subNormalText("Click to track employee");
            }

            if (i == 5) {
                builder.normalImageRes(R.mipmap.ic_location)
                        .normalText("Set Working location " + mEmployeeModel.getReal_name())
                        .subNormalText("Click to set working location of this employee");
            }


            builder.listener(new OnBMClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void onBoomButtonClick(int index) {
                    // When the boom-button corresponding this builder is clicked.
                    if (index == 0) {

                        Intent i = new Intent(context, EmployeeProfile.class);
                        i.putExtra("email", mEmployeeModel.getEmail());
                        View sharedView = holder.profile;
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
//                        new AlertDialog.Builder(context)
//                                .setTitle("Make a Call")
//                                .setIcon(R.mipmap.ic_phone)
//                                .setMessage("Do you want to make a call to " + mEmployeeModel.getReal_name() + "?" +
//                                        "\nPhone1 " + mEmployeeModel.getPhone1() + "" +
//                                        "\nPhone2 " + mEmployeeModel.getPhone2())
//                                .setPositiveButton("Phone 1", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        String phone_no = mEmployeeModel.getPhone1().replaceAll("-", "");
//                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
//                                        callIntent.setPackage("com.android.server.telecom");
//                                        callIntent.setData(Uri.parse("tel:" + phone_no));
//                                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                                            // TODO: Consider calling
//                                            //    ActivityCompat#requestPermissions
//                                            // here to request the missing permissions, and then overriding
//                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                            //                                          int[] grantResults)
//                                            // to handle the case where the user grants the permission. See the documentation
//                                            // for ActivityCompat#requestPermissions for more details.
//                                            return;
//                                        }
//                                        context.startActivity(callIntent);
//
//                                        dialog.dismiss();
//
//                                    }
//                                })
//
//                                .setNegativeButton("Phone 2", new DialogInterface.OnClickListener() {
//                                    public void onClick(DialogInterface dialog, int id) {
//                                        String phone_no = mEmployeeModel.getPhone2().replaceAll("-", "");
//                                        Intent callIntent = new Intent(Intent.ACTION_CALL);
//                                        callIntent.setPackage("com.android.server.telecom");
//                                        callIntent.setData(Uri.parse("tel:" + phone_no));
//                                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                                            // TODO: Consider calling
//                                            //    ActivityCompat#requestPermissions
//                                            // here to request the missing permissions, and then overriding
//                                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                            //                                          int[] grantResults)
//                                            // to handle the case where the user grants the permission. See the documentation
//                                            // for ActivityCompat#requestPermissions for more details.
//                                            return;
//                                        }
//                                        context.startActivity(callIntent);
//                                        dialog.dismiss();
//                                    }
//                                })
//
//                                .show();
                        Intent i = new Intent(context, BarcodeGenerator.class);
                        i.putExtra("barcode", mEmployeeModel.getEmail());
                        i.putExtra("barname", mEmployeeModel.getReal_name());
                        i.putExtra("division", mEmployeeModel.getDivision());
                        i.putExtra("loc", "absen");
                        context.startActivity(i);
                    }

                    if (index == 4) {
                            styleableToast = new StyleableToast
                                    .Builder(context)
                                    .icon(R.drawable.ic_action_info)
                                    .text("Sorry, work under progress")
                                    .textColor(Color.WHITE)
                                    .backgroundColor(Color.BLUE)
                                    .build();
                            styleableToast.show();

                    }

                    if (index == 5) {
                        if (!mLevelUser.equals("2")) {
                            Intent i = new Intent(context, MapsActivity.class);
                            i.putExtra("id_caller","SetWorkingGPS");
                            i.putExtra("email",mEmployeeModel.getEmail());

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
                }
            });
            holder.bmb.addBuilder(builder);
        }


        holder.cardView.setOnClickListener(new View.OnClickListener() {
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

    private class updateAdmin extends AsyncTask<Void, Void, Void> {

        View view;
        String email,setLev;

        public updateAdmin (View view, String email, String setLev) {
            this.view = view;
            this.email = email;
            this.setLev = setLev;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... arg0) {

            RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

            String url = "https://olmatix.com/wamonitoring/update_user_to_admin.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");

                    if (result_code.equals(1)||result_code.equals("1")){


                        sendMessageDetail("Updating success");
                    }



                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("email", email);
                    MyData.put("level_user", setLev);

                    return MyData;
                }
            };

            // Adding request to request queue

            int socketTimeout = 60000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            MyStringRequest.setRetryPolicy(policy);
            MyRequestQueue.add(MyStringRequest);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }

    private void sendMessageDetail(String Notify) {
        Intent intent = new Intent("admins");
        intent.putExtra("notify", Notify);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void setDatePicker(int year, int month, int dayOfMonth) {
        datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener(){
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String daymonth = String.valueOf(dayOfMonth);
                        String mMonth = String.valueOf(++month);

                        if (daymonth.length()==1){
                            daymonth = "0"+dayOfMonth;
                        } else {
                            daymonth = String.valueOf(dayOfMonth);
                        }
                        if (mMonth.length()==1){
                            mMonth = "0"+month;
                        } else {
                            mMonth = String.valueOf(month);
                        }

                        String date = year + "-" + mMonth + "-" + daymonth;
                        eDateBirth.setText(date);
                        //(date);
                    }
                }, year, month, dayOfMonth);
    }

    private class UpdateUsers extends AsyncTask<Void, Void, Void> {

        String DisplayName, PlaceBirth,DateBirth,EmployeeAddress,Phone1,Phone2,email;
        View view;

        public UpdateUsers (String DisplayName, String PlaceBirth, String DateBirth, String EmployeeAddress,
                            String Phone1, String Phone2, String email, View view) {
            this.DisplayName = DisplayName;
            this.PlaceBirth = PlaceBirth;
            this.DateBirth = DateBirth;
            this.EmployeeAddress = EmployeeAddress;
            this.Phone1 = Phone1;
            this.Phone2 = Phone2;
            this.view = view;
            this.email = email;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
           /* pDialog = new ProgressDialog(ScalingScannerActivity.this);
            pDialog.setMessage("Loading Division names. Please wait...");
            pDialog.setCancelable(true);
            pDialog.setCanceledOnTouchOutside(false);
            pDialog.show();*/
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

            String url = "https://olmatix.com/wamonitoring/update_employee.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                try {

                    JSONObject jObject = new JSONObject(response);
                    String result_message = jObject.getString("message");
                    String result_code = jObject.getString("success");

                    Log.d("DEBUG", "doInBackground: "+response);
                    if (result_code.equals(1)||result_code.equals("1")) {

                        sendMessageDetail("Updating success");

                    }  else {
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("email", email);
                    MyData.put("real_name", DisplayName);
                    MyData.put("place_birth", PlaceBirth);
                    MyData.put("date_birth", DateBirth);
                    MyData.put("employee_address", EmployeeAddress);
                    MyData.put("phone1", Phone1);
                    MyData.put("phone2", Phone2);

                    return MyData;
                }
            };

            // Adding request to request queue

            int socketTimeout = 60000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            MyStringRequest.setRetryPolicy(policy);
            MyRequestQueue.add(MyStringRequest);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

        }
    }


}
