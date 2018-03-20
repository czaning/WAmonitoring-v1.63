package com.lesjaw.wamonitoring.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.checklistModel;
import com.lesjaw.wamonitoring.model.locEmployeeLastTenModel;
import com.lesjaw.wamonitoring.model.tagsAlertModel;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.utils.Utils;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListEmployeeLocationAdapter extends RecyclerView.Adapter<ListEmployeeLocationAdapter.MainViewHolder> {
    private List<locEmployeeLastTenModel> nodeList;
    Context context;

    class MainViewHolder extends RecyclerView.ViewHolder {
        TextView location, tgl  ;
        CardView cardView;


        public MainViewHolder(View itemView) {
            super(itemView);
            location = (TextView) itemView.findViewById(R.id.lokasi);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            cardView = (CardView) itemView.findViewById(R.id.cv);

        }

    }

    public ListEmployeeLocationAdapter(List<locEmployeeLastTenModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_location, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        final locEmployeeLastTenModel mTagModel = nodeList.get(position);

        holder.location.setText(mTagModel.getAddstring());

        String input = mTagModel.getTimestamps();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);


        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(input);
            long milliseconds = date.getTime();
            long updatetime = Long.parseLong(String.valueOf(milliseconds));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(updatetime));
            cal.getTimeInMillis();
            //Log.d("DEBUG", "onBindViewHolder: "+Utils.getTimeAgo(cal) + " | "+simpleDateFormat1.format(updatetime));
            if (Utils.getTimeAgo(cal).contains("Days ago")) {
                holder.tgl.setText(simpleDateFormat2.format(updatetime));

            } else {
                holder.tgl.setText(Utils.getTimeAgo(cal) + " | " + simpleDateFormat1.format(updatetime));

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("geo:0,0?q=" + mTagModel.getLatitude() + "," + mTagModel.getLongitude() + "(" +
                                mTagModel.getEmployeeName() + " "+holder.tgl.getText()+")"));
                context.startActivity(intent);
            }
        });


    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
