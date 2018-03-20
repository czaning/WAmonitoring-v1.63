package com.lesjaw.wamonitoring.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.tagsAlertModel;
import com.lesjaw.wamonitoring.ui.NoticeDetail;
import com.lesjaw.wamonitoring.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListAlertAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<tagsAlertModel> nodeList;
    Context context;
    private static final int TYPE_ONE = 1;
    private static final int TYPE_TWO = 2;
    private static final int TYPE_THREE = 3;

    // Static inner class to initialize the views of rows
    private static class ViewHolderOne extends RecyclerView.ViewHolder {

        TextView name, division, tgl, notes, lokasi, idcomment;
        CardView cardView;
        CircleImageView imgprofile;
        FloatingActionButton phone;

        ViewHolderOne(View itemView) {
            super(itemView);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            name = (TextView) itemView.findViewById(R.id.employee_name);
            division = (TextView) itemView.findViewById(R.id.division_name);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            notes = (TextView) itemView.findViewById(R.id.notes);
            lokasi = (TextView) itemView.findViewById(R.id.lokasi);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            phone = (FloatingActionButton) itemView.findViewById(R.id.phone);
            idcomment = (TextView) itemView.findViewById(R.id.comments);

        }
    }

    private static class ViewHolderTwo extends RecyclerView.ViewHolder {
        TextView name, tgl, notes, division,idcomment;
        CardView cardView;
        ImageView imgview;
        CircleImageView imgprofile;

        ViewHolderTwo(View itemView) {
            super(itemView);

            name = (TextView) itemView.findViewById(R.id.employee_name);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            notes = (TextView) itemView.findViewById(R.id.notes);
            cardView = (CardView) itemView.findViewById(R.id.cv);
            imgview = (ImageView) itemView.findViewById(R.id.imgview);
            division = (TextView) itemView.findViewById(R.id.division_name);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            idcomment = (TextView) itemView.findViewById(R.id.comments);

        }
    }

    private static class ViewHolderThree extends RecyclerView.ViewHolder {


        ViewHolderThree(View itemView) {
            super(itemView);

        }
    }

    public ListAlertAdapter(List<tagsAlertModel> nodeList, Context mContext) {
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
                initLayoutThree();
                break;
            default:
                break;
        }

    }

    private void initLayoutOne(ViewHolderOne holder, int position) {

        final tagsAlertModel mTagModel = nodeList.get(position);

        holder.cardView.setCardBackgroundColor(Color.parseColor("#ffcce0"));

        holder.name.setText(mTagModel.getEmployee_name());
        if (mTagModel.getDivision().equals("null")){
            holder.division.setText("Administrator");

        } else {
            holder.division.setText(mTagModel.getDivision());
        }
        //holder.tgl.setText(mTagModel.getDate_created());
        holder.notes.setText(mTagModel.getNotes());
        holder.lokasi.setText(mTagModel.getLokasi());

        String input = mTagModel.getDate_created();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);


        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(input);
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

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mTagModel.getEmail() + ".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        holder.imgprofile.setOnClickListener(view -> {


        });

        holder.phone.setOnClickListener(v -> new AlertDialog.Builder(context)
                .setTitle("Make a Call")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setMessage("Do you want to make a call to "+mTagModel.getEmployee_name()+"?" +
                        "\nPhone1 "+mTagModel.getPhone1()+"" +
                        "\nPhone2 "+mTagModel.getPhone2())
                .setPositiveButton("Phone 1", (dialog, id) -> {
                    String phone_no = mTagModel.getPhone1().replaceAll("-", "");
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

                })

                .setNegativeButton("Phone 2", (dialog, id) -> {
                    String phone_no = mTagModel.getPhone2().replaceAll("-", "");
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
                })

                .show());

        holder.lokasi.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=" + mTagModel.getLatitude() + "," + mTagModel.getLongitude() + "(" + mTagModel.getEmployee_name() + " - PANIC BUTTON!)"));
            context.startActivity(intent);
        });

        holder.cardView.setOnClickListener(v -> {
            Intent i = new Intent(context, NoticeDetail.class);
            i.putExtra("email", mTagModel.getEmail());
            i.putExtra("EmployeName", mTagModel.getEmployee_name());
            i.putExtra("imageUrl", mTagModel.getLokasi());
            i.putExtra("tgl", mTagModel.getDate_created());
            i.putExtra("note", mTagModel.getNotes());
            i.putExtra("id", mTagModel.getPanic_id());

            String transitionName1 = context.getString(R.string.imgProfile);
            //String transitionName2 = context.getString(R.string.imgView);
            String transitionName3 = context.getString(R.string.textview);

            Pair<View, String> p1 = Pair.create(holder.imgprofile, transitionName1);
            Pair<View, String> p3 = Pair.create(holder.notes, transitionName3);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, p1, p3);
            context.startActivity(i, options.toBundle());
        });

        holder.idcomment.setText(mTagModel.getComment());

    }

    private void initLayoutTwo(ViewHolderTwo holder, int position) {
        final tagsAlertModel mTagModel = nodeList.get(position);

        holder.cardView.setCardBackgroundColor(Color.parseColor("#e6ffe6"));

        holder.name.setText(mTagModel.getEmployee_name());
        //holder.tgl.setText(mTagModel.getDate_created());
        holder.notes.setText(mTagModel.getNotes());
        if (mTagModel.getDivision().equals("null")){
            holder.division.setText("Administrator");

        } else {
            holder.division.setText(mTagModel.getDivision());
        }
        //String lokasi = mTagModel.getLokasi();
        String input = mTagModel.getDate_created();
        SimpleDateFormat simpleDateFormat1 = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);


        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(input);
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
        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mTagModel.getEmail() + ".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        if (!mTagModel.getLokasi().isEmpty() || !mTagModel.getLokasi().equals("")) {
            holder.imgview.setVisibility(View.VISIBLE);

            Picasso.with(context)
                    .load(mTagModel.getLokasi())
                    .into(holder.imgview);
        } else {
            holder.imgview.setVisibility(View.GONE);
        }

        holder.imgprofile.setOnClickListener(view -> {


        });

        holder.idcomment.setText(mTagModel.getComment());

        holder.cardView.setOnClickListener(v -> {
            Intent i = new Intent(context, NoticeDetail.class);
            i.putExtra("email", mTagModel.getEmail());
            i.putExtra("EmployeName", mTagModel.getEmployee_name());
            i.putExtra("imageUrl", mTagModel.getLokasi());
            i.putExtra("tgl", mTagModel.getDate_created());
            i.putExtra("note", mTagModel.getNotes());
            i.putExtra("id", mTagModel.getPanic_id());

            String transitionName1 = context.getString(R.string.imgProfile);
            String transitionName2 = context.getString(R.string.imgView);
            String transitionName3 = context.getString(R.string.textview);

            Pair<View, String> p1 = Pair.create(holder.imgprofile, transitionName1);
            Pair<View, String> p2;
            if (!mTagModel.getLokasi().isEmpty() || !mTagModel.getLokasi().equals("")) {
               p2 = Pair.create(holder.imgview, transitionName2);
            } else {
                p2 = p1;
            }
            Pair<View, String> p3 = Pair.create(holder.notes, transitionName3);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, p2, p1, p3);
            context.startActivity(i, options.toBundle());
        });

    }

    private void initLayoutThree() {

    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

}
