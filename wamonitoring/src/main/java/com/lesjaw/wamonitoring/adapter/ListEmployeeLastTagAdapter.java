package com.lesjaw.wamonitoring.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.EmployeeLastTagModel;
import com.lesjaw.wamonitoring.utils.Utils;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListEmployeeLastTagAdapter extends RecyclerView.Adapter<ListEmployeeLastTagAdapter.MainViewHolder> {
    private List<EmployeeLastTagModel> nodeList;
    Context context;

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView real_name, email, division, lastseen;
        CircleImageView profile;
        CardView cardView;


        public MainViewHolder(View itemView) {
            super(itemView);
            real_name = (TextView) itemView.findViewById(R.id.employee_name);
            division = (TextView) itemView.findViewById(R.id.employee_div);
            email = (TextView) itemView.findViewById(R.id.employee_email);
            lastseen = (TextView) itemView.findViewById(R.id.lastseen);
            profile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            cardView = (CardView) itemView.findViewById(R.id.cv);

        }

    }

    public ListEmployeeLastTagAdapter(List<EmployeeLastTagModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_employee_last_tag, parent, false);

        return new MainViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final EmployeeLastTagModel mEmployeeModel = nodeList.get(position);

        if((position % 2 == 0)){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#cce6ff"));
        }else{
            holder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));
        }
        SimpleDateFormat df = new SimpleDateFormat("EE, yyyy-MM-dd HH:mm");

        long updatetime = Long.parseLong(mEmployeeModel.getLast_update());
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(updatetime));
        cal.getTimeInMillis();

        if (mEmployeeModel.getCounttags().isEmpty() || mEmployeeModel.getCounttags().equals("null")) {
            holder.division.setVisibility(View.GONE);
            holder.email.setText("No data");
        } else {
            holder.division.setVisibility(View.VISIBLE);
            String input = mEmployeeModel.getTgl()+" "+mEmployeeModel.getJam();
            Date date = null;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(input);
                long milliseconds = date.getTime();
                long updatetime1 = Long.parseLong(String.valueOf(milliseconds));
                Calendar cal1 = Calendar.getInstance();
                cal1.setTime(new Date(updatetime1));
                cal1.getTimeInMillis();

                holder.division.setText("Tags this month : " + mEmployeeModel.getCounttags());
                Time jam = Time.valueOf(mEmployeeModel.getJam());
                SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
                SimpleDateFormat simpleDateFormat3 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

                if (Utils.getTimeAgo(cal1).contains("Days ago")){
                    holder.email.setText("Last tag at "+simpleDateFormat3.format(updatetime1));

                } else {
                    holder.email.setText("Last tag at "+Utils.getTimeAgo(cal1) + " | "+simpleDateFormat1.format(updatetime1));

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }

        if (mEmployeeModel.getLevel_user().equals("1")) {
            holder.real_name.setText(mEmployeeModel.getEmployee_name() + " " + mEmployeeModel.getDivision_name()+" Head");
            holder.real_name.setBackgroundColor(Color.parseColor("#ffd11a"));
        } else if (mEmployeeModel.getLevel_user().equals("2")) {
            holder.real_name.setText(mEmployeeModel.getEmployee_name() + " " + mEmployeeModel.getDivision_name());
            holder.real_name.setBackgroundColor(Color.TRANSPARENT);

        } else {
            holder.real_name.setText(mEmployeeModel.getEmployee_name() + " Administrator");
            holder.real_name.setBackgroundColor(Color.parseColor("#ffd11a"));
        }


        holder.lastseen.setText(Html.fromHtml("<u>Last seen at " + Utils.getTimeAgo(cal) + " | " + df.format(updatetime) + "</u>"));
        Glide.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mEmployeeModel.getEmail() + ".jpg")
                .into(holder.profile);

        /*holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Dialog dialog = new Dialog(context);
                LayoutInflater li = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View v = li.inflate(R.layout.foto_profile_show, null);
                CircleImageView img = (CircleImageView) v.findViewById(R.id.img_profile);
                Glide.with(context)
                        .load("https://olmatix.com/wamonitoring/foto_profile/" + mEmployeeModel.getEmail() + ".jpg")
                        .signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                        .into(img);
                dialog.setContentView(v);
                dialog.show();
            }
        });*/




    }

    @Override
    public int getItemCount() {
        //Log.d("DEBUG", "getItemCount: "+nodeList.size());
        return nodeList.size();
    }


}
