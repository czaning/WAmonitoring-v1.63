package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.tagsAlertModel;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListNoticeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<tagsAlertModel> nodeList;
    Context context;
    private static final int TYPE_ONE = 1;
    private static final int TYPE_TWO = 2;
    private static final int TYPE_THREE = 3;
    int lastPosition = -1;

    // Static inner class to initialize the views of rows
    static class ViewHolderOne extends RecyclerView.ViewHolder {


        public ViewHolderOne(View itemView) {
            super(itemView);
           }
    }

    static class ViewHolderTwo extends RecyclerView.ViewHolder {
        TextView notes,idcomment;
        CardView cardView;
        ImageView imgview;
        CircleImageView imgprofile;

        public ViewHolderTwo(View itemView) {
            super(itemView);

            notes = (TextView) itemView.findViewById(R.id.notes);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            imgview = (ImageView) itemView.findViewById(R.id.imgview);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            idcomment = (TextView) itemView.findViewById(R.id.comments);

        }
    }

    static class ViewHolderThree extends RecyclerView.ViewHolder {


        public ViewHolderThree(View itemView) {
            super(itemView);

        }
    }

    public ListNoticeAdapter(List<tagsAlertModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public int getItemViewType(int position) {
        tagsAlertModel item = nodeList.get(position);
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
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_item_list_alert_one, parent, false);
                return new ViewHolderOne(view);
            case TYPE_TWO:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.frag_item_list_notice_two, parent, false);
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
                initLayoutOne((ViewHolderOne)holder, position);
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

    }

    private void initLayoutTwo(ViewHolderTwo holder, int position) {
        final tagsAlertModel mTagModel = nodeList.get(position);

        holder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));

        holder.notes.setText(mTagModel.getNotes());

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/"+mTagModel.getEmail()+".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        if (!mTagModel.getLokasi().isEmpty()||!mTagModel.getLokasi().equals("")){
            holder.imgview.setVisibility(View.VISIBLE);

           /* Picasso.with(context)
                    .load(mTagModel.getLokasi())
                    //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(holder.imgview);*/

            Ion.with(context)
                    .load(mTagModel.getLokasi())
                    .withBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_upload_error)
                    .animateLoad(R.anim.spin_animation)
                    .intoImageView(holder.imgview);
        } else {
            Picasso.with(context)
                    .load(R.drawable.logowam)
                    //.signature(new StringSignature(String.valueOf(System.currentTimeMillis())))
                    .into(holder.imgview);

        }

        holder.imgprofile.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onClick(View view) {



            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.notes.setSelected(true);

            }
        });
        holder.idcomment.setText(mTagModel.getComment());

    }

    private void initLayoutThree(ViewHolderThree holder, int position) {

    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
