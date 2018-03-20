package com.lesjaw.wamonitoring.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.signature.StringSignature;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.checklistModel;
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.utils.Utils;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListTagsLogAdapterFrag extends RecyclerView.Adapter<ListTagsLogAdapterFrag.MainViewHolder> {
    private List<tagsDailyModel> nodeList;
    Context context;
    SharedPreferences sharedPref;
    private List<checklistModel> tagList = new ArrayList<>();
    CheckListAdapter mAdapter;
    private ProgressDialog pDialog;

    class MainViewHolder extends RecyclerView.ViewHolder {
        String cklistBoolean;
        TextView tag_name, tgl  ;
        ImageView cklist;
        ImageView before_photo, after_photo;
        CardView cardView;
        int jarak;
        boolean far;

        public MainViewHolder(View itemView) {
            super(itemView);
            tag_name = (TextView) itemView.findViewById(R.id.tag_name);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            cklist = (ImageView)itemView.findViewById(R.id.cklist);
            before_photo = (ImageView)itemView.findViewById(R.id.before_photo);
            after_photo = (ImageView)itemView.findViewById(R.id.after_photo);
            cardView = (CardView) itemView.findViewById(R.id.cv);

        }

    }

    public ListTagsLogAdapterFrag(List<tagsDailyModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_log, parent, false);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final tagsDailyModel mTagModel = nodeList.get(position);

       /* if((position % 2 == 0)){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#cce6ff"));
        }else{
            holder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));
        }*/

        holder.jarak = Integer.parseInt(mTagModel.getRange_loc());

        if (holder.jarak>50){
            holder.far = true;
            holder.tag_name.setTextColor(Color.RED);
        } else {
            holder.far = false;
            holder.tag_name.setTextColor(Color.BLACK);
        }

        holder.cklistBoolean = mTagModel.getChecklist_done();

        holder.after_photo.setVisibility(View.INVISIBLE);
        holder.before_photo.setVisibility(View.INVISIBLE);

        if (holder.cklistBoolean.equals("0")){
            holder.cklist.setImageResource(android.R.drawable.checkbox_off_background);
        } else {
            holder.cklist.setImageResource(android.R.drawable.checkbox_on_background);
        }

        holder.tag_name.setText(mTagModel.getTag_name()+ "\n"+ Utils.getJarak(holder.jarak));
        Time jam = Time.valueOf(mTagModel.getJam());
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm");

        holder.tgl.setText(simpleDateFormat1.format(jam));

        holder.cklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*if (holder.cklistBoolean.equals("0")) {
                    pDialog = new ProgressDialog(context);
                    pDialog.setMessage("Getting tag checklist. Please wait...");
                    pDialog.setCancelable(true);
                    pDialog.setCanceledOnTouchOutside(false);
                    pDialog.show();
                    new GetChecklist(mTagModel.getTid(), mTagModel.getTrid()).execute();

                } else {
                    sendMessageDetail("You have done doing this tag..");

                }*/
            }
        });

        holder.tgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               /* Intent i = new Intent(context, UploadFotoTags.class);
                i.putExtra("trid", mTagModel.getTrid());
                context.startActivity(i);*/
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

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
