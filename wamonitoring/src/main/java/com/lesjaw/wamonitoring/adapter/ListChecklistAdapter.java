package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.commentChecklistModel;
import com.lesjaw.wamonitoring.model.tagsAlertModel;
import com.lesjaw.wamonitoring.model.tagsChecklistModel;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.Utils;
import com.squareup.picasso.Picasso;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
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

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListChecklistAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<tagsChecklistModel> nodeList;
    Context context;
    private static final int TYPE_ONE = 1;
    private static final int TYPE_TWO = 2;
    private static final int TYPE_THREE = 3;



    // Static inner class to initialize the views of rows
    static class ViewHolderOne extends RecyclerView.ViewHolder {

        TextView checklist_name, division_name, tgl, notes, comments;
        CircleImageView imgprofile;
        CardView cardView;
        int position;

        public ViewHolderOne(View itemView) {
            super(itemView);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            checklist_name = (TextView) itemView.findViewById(R.id.checklist_name);
            division_name = (TextView) itemView.findViewById(R.id.division_name);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            notes = (TextView) itemView.findViewById(R.id.notes);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            comments = (TextView) itemView.findViewById(R.id.comments);

        }

    }

    static class ViewHolderTwo extends RecyclerView.ViewHolder {


        public ViewHolderTwo(View itemView) {
            super(itemView);


        }
    }

    static class ViewHolderThree extends RecyclerView.ViewHolder {


        public ViewHolderThree(View itemView) {
            super(itemView);

        }
    }

    public ListChecklistAdapter(List<tagsChecklistModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        tagsChecklistModel item = nodeList.get(position);
        if (item.getType() == 0) {
            return TYPE_ONE;
        } else if (item.getType() == 1) {
            return TYPE_TWO;
        } else if (item.getType() == 2) {
            return TYPE_THREE;
        } else {
            return -1;
        }
    }

    // specify the row layout file and click for each row
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case TYPE_ONE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_item_list_checklist_note, parent, false);

                return new ViewHolderOne(view);
            case TYPE_TWO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_item_list_alert_two, parent, false);
                return new ViewHolderTwo(view);
            case TYPE_THREE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_item_list_alert_one, parent, false);
                return new ViewHolderThree(view);
        }

        return null;
    }

    // specify the row layout file and click for each row
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case TYPE_ONE:
                initLayoutOne((ViewHolderOne) holder, position);
                break;
            case TYPE_TWO:
                initLayoutTwo((ViewHolderTwo) holder, position);
                break;
            case TYPE_THREE:
                initLayoutThree((ViewHolderThree) holder, position);
                break;
            default:
                break;
        }

    }

    private void initLayoutOne(ViewHolderOne holder, int position) {

        final tagsChecklistModel mTagModel = nodeList.get(position);
        //this.position = position;

        /*holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });*/

        holder.checklist_name.setText(mTagModel.getChecklist());
        holder.division_name.setText(mTagModel.getEmployee_name() + ", " + mTagModel.getDivision());
        holder.notes.setText(mTagModel.getChecklist_note());
        holder.comments.setText(mTagModel.getComment());

        //String tglTag = mTagModel.getTgl();
        String input = mTagModel.getTgl() + " " + mTagModel.getJam();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

        Date date = null;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(input);
            long milliseconds = date.getTime();
            long updatetime = Long.parseLong(String.valueOf(milliseconds));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(updatetime));
            cal.getTimeInMillis();
            if (Utils.getTimeAgo(cal).contains("Days ago")) {
                holder.tgl.setText(mTagModel.getTagname()+", "+simpleDateFormat2.format(updatetime));

            } else {
                holder.tgl.setText(mTagModel.getTagname()+", "+Utils.getTimeAgo(cal) + " | " + simpleDateFormat1.format(updatetime));

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mTagModel.getEmail() + ".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);


    }


    private void initLayoutTwo(ViewHolderTwo holder, int position) {
        final tagsChecklistModel mTagModel = nodeList.get(position);


    }

    private void initLayoutThree(ViewHolderThree holder, int position) {

    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
