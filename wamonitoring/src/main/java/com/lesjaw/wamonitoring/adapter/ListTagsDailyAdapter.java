package com.lesjaw.wamonitoring.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
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
import com.lesjaw.wamonitoring.model.tagsDailyModel;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.ui.FragmentContainer;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.utils.Utils;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class ListTagsDailyAdapter extends RecyclerView.Adapter<ListTagsDailyAdapter.MainViewHolder> {
    private List<tagsDailyModel> nodeList;
    Context context;

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView employee_name;
        TextView range_loc;
        TextView division_name;
        TextView tgl;
        ImageView cklist;
        String cklistBoolean;
        ImageView before_photo, after_photo;
        CardView cardView;
        int jarak;
        CircleImageView imgprofile;
        BoomMenuButton bmb;

        public MainViewHolder(View itemView) {
            super(itemView);
            employee_name = (TextView) itemView.findViewById(R.id.employee_name);
            range_loc = (TextView) itemView.findViewById(R.id.range_loc);
            division_name = (TextView) itemView.findViewById(R.id.division_name);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            cklist = (ImageView)itemView.findViewById(R.id.cklist);
            before_photo = (ImageView)itemView.findViewById(R.id.before_photo);
            after_photo = (ImageView)itemView.findViewById(R.id.after_photo);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            bmb = (BoomMenuButton) itemView.findViewById(R.id.bmb);

        }

    }

    public ListTagsDailyAdapter(List<tagsDailyModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_daily, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final tagsDailyModel mTagModel = nodeList.get(position);

        /*if((position % 2 == 0)){
            holder.cardView.setCardBackgroundColor(Color.parseColor("#cce6ff"));
        }else{
            holder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));
        }*/

        holder.cklistBoolean = mTagModel.getChecklist_done();

        holder.employee_name.setText(mTagModel.getEmployee_name());
        //holder.tag_name.setText(mTagModel.getTag_name());
        holder.division_name.setText(mTagModel.getDivision_name());

        //String tglTag = mTagModel.getTgl();
        String input = mTagModel.getTgl()+" "+mTagModel.getJam();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH).parse(input);
            long milliseconds = date.getTime();
            long updatetime = Long.parseLong(String.valueOf(milliseconds));
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date(updatetime));
            cal.getTimeInMillis();
            if (Utils.getTimeAgo(cal).contains("Days ago")){
                holder.tgl.setText(mTagModel.getTag_name()+", "+simpleDateFormat2.format(updatetime));

            } else {
                holder.tgl.setText(mTagModel.getTag_name()+", "+Utils.getTimeAgo(cal) + " | "+simpleDateFormat1.format(updatetime));

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.after_photo.setVisibility(View.INVISIBLE);
        holder.before_photo.setVisibility(View.INVISIBLE);

        holder.jarak = Integer.parseInt(mTagModel.getRange_loc());
        if (holder.jarak>50){
            holder.range_loc.setTextColor(Color.RED);
            holder.range_loc.setTypeface(null, Typeface.BOLD);
        } else {
            holder.range_loc.setTextColor(Color.BLACK);
            holder.range_loc.setTypeface(null, Typeface.NORMAL);

        }

        holder.range_loc.setText(Utils.getJarak(holder.jarak));


        if (holder.cklistBoolean.equals("0")){
            holder.cklist.setImageResource(android.R.drawable.checkbox_off_background);
        } else {
            holder.cklist.setImageResource(android.R.drawable.checkbox_on_background);
        }

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

            holder.before_photo.setOnClickListener(v -> {
                Intent i = new Intent(context, ImageViewer.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 i.putExtra("img", "https://olmatix.com/wamonitoring/uploads/"+mTagModel.getTrid()+"-before_foto"+".jpg");
                i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/"+mTagModel.getEmail()+".jpg");

                context.startActivity(i);
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


            holder.after_photo.setOnClickListener(v -> {
                Intent i = new Intent(context, ImageViewer.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("img", "https://olmatix.com/wamonitoring/uploads/" + mTagModel.getTrid() + "-after_foto" + ".jpg");
                i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/" + mTagModel.getEmail() + ".jpg");

                context.startActivity(i);
            });
        }

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/"+mTagModel.getEmail()+".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        holder.bmb.clearBuilders();
        for (int i = 0; i < holder.bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();


            if (i == 0) {
                builder.normalImageRes(R.drawable.ic_face_black_48dp)
                        .normalText((mTagModel.getEmployee_name() + " details"))
                        .subNormalText("Click to see employee details");
            }

            if (i == 1) {
                builder.normalImageRes(R.mipmap.ic_check_black)
                        .normalText("Checklist by " + mTagModel.getEmployee_name())
                        .subNormalText("Click to see employee checklist");
            }
            if (i == 2) {
                builder.normalImageRes(R.mipmap.barcode_img)
                        .normalText("Tags by " + mTagModel.getEmployee_name())
                        .subNormalText("Click to see employee tags");
            }


            builder.listener(index -> {
                // When the boom-button corresponding this builder is clicked.
                if (index == 0) {

                    Intent i1 = new Intent(context, EmployeeProfile.class);
                    i1.putExtra("email", mTagModel.getEmail());
                    View sharedView = holder.imgprofile;
                    String transitionName = context.getString(R.string.imgProfile);
                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context, sharedView, transitionName);
                    context.startActivity(i1, transitionActivityOptions.toBundle());

                }

                if (index == 1) {
                        Intent i1 = new Intent(context, FragmentContainer.class);
                        i1.putExtra("id_fragement", "Checklist Employee");
                        i1.putExtra("Memail", mTagModel.getEmail());
                        i1.putExtra("nama", mTagModel.getEmployee_name());
                        context.startActivity(i1);

                }

                if (index == 2) {

                        Intent i1 = new Intent(context, FragmentContainer.class);
                        i1.putExtra("id_fragement", "Tags Employee");
                        i1.putExtra("Memail", mTagModel.getEmail());
                        i1.putExtra("nama", mTagModel.getEmployee_name());

                        context.startActivity(i1);

                }

            });
            holder.bmb.addBuilder(builder);
        }
    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
