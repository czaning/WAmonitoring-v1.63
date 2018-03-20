package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.EmployeeModel;
import com.lesjaw.wamonitoring.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class EmployeeAdapter extends RecyclerView.Adapter<EmployeeAdapter.MainViewHolder> {
    private List<EmployeeModel> nodeList;
    Context context;

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView real_name,email,division, lastseen, level_user;
        CircleImageView profile;


        public MainViewHolder(View itemView) {
            super(itemView);
            real_name = (TextView) itemView.findViewById(R.id.employee_name);
            division = (TextView) itemView.findViewById(R.id.employee_div);
            email = (TextView) itemView.findViewById(R.id.employee_email);
            lastseen = (TextView) itemView.findViewById(R.id.lastseen);
            level_user = (TextView) itemView.findViewById(R.id.level_user);
            profile = (CircleImageView) itemView.findViewById(R.id.img_profile);


        }

    }

    public EmployeeAdapter(List<EmployeeModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_employee, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final EmployeeModel mEmployeeModel = nodeList.get(position);

        holder.division.setVisibility(View.GONE);

        SimpleDateFormat df = new SimpleDateFormat("EE, dd-MM-yyyy HH:mm");

        long updatetime = Long.parseLong(mEmployeeModel.getLastseen());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(updatetime));
        cal.getTimeInMillis();



        //holder.real_name.setText(mEmployeeModel.getReal_name());
        holder.real_name.setText("Division : "+mEmployeeModel.getDivision());
        holder.email.setText(mEmployeeModel.getEmail());
        holder.lastseen.setText(Html.fromHtml("<u>Last seen at " + Utils.getTimeAgo(cal) + " | "+df.format(updatetime)+"</u>" ));

        String logstatus = mEmployeeModel.getLog_status();
        if (logstatus.equals("1")){
            holder.email.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_online,0,0,0);
        } else if (logstatus.equals("2")){
            holder.email.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_offline,0,0,0);

        } else {
            holder.email.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.presence_invisible,0,0,0);
        }

        holder.email.setCompoundDrawablePadding(10);

        //decode base64 string to image
        String imageString = mEmployeeModel.getFoto();
        if (imageString.length()>2) {
            Glide.with(context)
                    .load(mEmployeeModel.getFoto())
                    .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(holder.profile);
        } else {
            holder.profile.setImageResource(R.drawable.user);

        }

        /*holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View itemView = li.inflate(R.layout.employee_detail_activity, null);
                CircleImageView img = (CircleImageView) itemView.findViewById(R.id.img_profile);
                TextView real_name = (TextView) itemView.findViewById(R.id.employee_name);
                TextView place_date_birth = (TextView) itemView.findViewById(R.id.place_dateBirth);
                TextView division = (TextView) itemView.findViewById(R.id.employee_div);
                TextView email = (TextView) itemView.findViewById(R.id.employee_email);
                TextView address = (TextView) itemView.findViewById(R.id.employee_address);
                TextView phone = (TextView) itemView.findViewById(R.id.phone);
                TextView lastseen = (TextView) itemView.findViewById(R.id.lastseen);
                TextView level_user = (TextView) itemView.findViewById(R.id.level_user);
                ImageButton edit = (ImageButton) itemView.findViewById(R.id.edit);

                String mUserName = mEmployeeModel.getEmail();

                String tag_json_obj = "login";

                String url = Config.DOMAIN+"wamonitoring/get_employee_detail.php";

                StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
                    try {

                        JSONObject jObject = new JSONObject(response);

                        String result_json = jObject.getString("message");
                        String result_code = jObject.getString("success");

                        Log.d("DEBUG", "result_json: "+result_json +"result_code: "+result_code);

                        if (result_code.equals(1)||result_code.equals("1")) {

                            String result_level_user = jObject.getString("level_user");
                            String result_place_birth = jObject.getString("place_birth");
                            String result_date_birth = jObject.getString("date_birth");
                            String result_employee_address = jObject.getString("employee_address");
                            String result_division = jObject.getString("division");
                            String result_real_name = jObject.getString("real_name");
                            String result_lastseen = jObject.getString("last_update");
                            String result_phone1 = jObject.getString("phone1");
                            String result_phone2 = jObject.getString("phone2");
                            String employment = jObject.getString("employment");
                            String employment_status = jObject.getString("employment_status");

                            SimpleDateFormat df = new SimpleDateFormat("EE, yyyy-MM-dd HH:mm");
                            long updatetime = Long.parseLong(result_lastseen);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(new Date(updatetime));
                            cal.getTimeInMillis();

                            Picasso.with(context)
                                    .load("https://olmatix.com/wamonitoring/foto_profile/"+mEmployeeModel.getEmail()+".jpg")
                                    .into(img);

                            real_name.setText(result_real_name);
                            place_date_birth.setText(result_place_birth+", "+result_date_birth);
                            division.setText("Division : "+result_division+", Status : "+employment_status+"\nEmployment : "+employment);
                            email.setText(mEmployeeModel.getEmail());
                            address.setText("Address : "+result_employee_address);
                            phone.setText("Ph. "+result_phone1+", "+result_phone2);
                            lastseen.setText(Html.fromHtml("<u>Last seen at " + Utils.getTimeAgo(cal) + " | "+df.format(updatetime)+"</u>" ));

                            if (result_level_user.equals("1")){
                                level_user.setVisibility(View.VISIBLE);
                                level_user.setText(result_division+" Division Head");
                            } else {
                                level_user.setVisibility(View.GONE);
                            }

                            dialog.setContentView(itemView);
                            dialog.show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }                }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                    protected Map<String, String> getParams() {
                        Map<String, String> MyData = new HashMap<String, String>();
                        MyData.put("email", mUserName);
                        return MyData;
                    }
                };

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(jsonObjReq, tag_json_obj);




            }
        });*/

        if (mEmployeeModel.getLevel_user().equals("1")){
            holder.level_user.setVisibility(View.VISIBLE);
            holder.level_user.setText(mEmployeeModel.getDivision()+" Division Head");
        } else {
            holder.level_user.setVisibility(View.GONE);
        }


    }

    @Override
    public int getItemCount() {
        //Log.d("DEBUG", "getItemCount: "+nodeList.size());
        return nodeList.size();
    }
}
