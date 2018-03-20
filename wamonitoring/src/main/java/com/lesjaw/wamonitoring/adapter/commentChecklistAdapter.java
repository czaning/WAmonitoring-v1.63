package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.commentChecklistModel;
import com.lesjaw.wamonitoring.model.emojiGifModel;
import com.lesjaw.wamonitoring.utils.Utils;
import com.squareup.picasso.Picasso;

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

public class commentChecklistAdapter extends RecyclerView.Adapter<commentChecklistAdapter.MainViewHolder> {
    private List<commentChecklistModel> nodeList;
    Context context;

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView name, comment, tgl;
        CircleImageView imgprofile;

        public MainViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.employee);
            comment = (TextView) itemView.findViewById(R.id.comment);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);

        }

    }

    public commentChecklistAdapter(List<commentChecklistModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_comment, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final commentChecklistModel mCommentChecklistModel = nodeList.get(position);

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mCommentChecklistModel.getFrom_email() + ".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        holder.name.setText(mCommentChecklistModel.getEmployee_name());
        holder.comment.setText(mCommentChecklistModel.getComment());
        String input = mCommentChecklistModel.getDate_update();
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
                holder.tgl.setText(simpleDateFormat2.format(updatetime));

            } else {
                holder.tgl.setText(Utils.getTimeAgo(cal) + " | " + simpleDateFormat1.format(updatetime));

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        //Log.d("DEBUG", "getItemCount: "+nodeList.size());
        return nodeList.size();
    }
}
