package com.lesjaw.wamonitoring.adapter;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.AbsenceModel;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.ui.FragmentContainer;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.lesjaw.wamonitoring.utils.Utils;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.nightonke.boommenu.BoomButtons.HamButton;
import com.nightonke.boommenu.BoomMenuButton;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AbsenAdapter extends RecyclerView.Adapter<AbsenAdapter.MainViewHolder> {
    private List<AbsenceModel> nodeList;
    Context context;
    private String mLevelUser;
    private StyleableToast styleableToast;
    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView name, lokasi, tgl, workingHours;
        CircleImageView imgprofile;
        BoomMenuButton bmb;
        CardView cardView;

        public MainViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.employee);
            lokasi = (TextView) itemView.findViewById(R.id.comment);
            tgl = (TextView) itemView.findViewById(R.id.tgl);
            imgprofile = (CircleImageView) itemView.findViewById(R.id.img_profile);
            workingHours = (TextView) itemView.findViewById(R.id.workingHours);
            cardView = (CardView) itemView.findViewById(R.id.cv);

            bmb = (BoomMenuButton) itemView.findViewById(R.id.bmb);
            PreferenceHelper mPrefHelper = new PreferenceHelper(context);
            mLevelUser = mPrefHelper.getLevelUser();

        }

    }

    public AbsenAdapter(List<AbsenceModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_absence, parent, false);

        return new MainViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final AbsenceModel mEmployeeModel = nodeList.get(position);

        Picasso.with(context)
                .load("https://olmatix.com/wamonitoring/foto_profile/" + mEmployeeModel.getEmail() + ".jpg")
                .placeholder(R.mipmap.logo)
                .into(holder.imgprofile);

        holder.name.setText(mEmployeeModel.getEmployee_name());

        double latitude = Double.parseDouble(mEmployeeModel.getLatitude());
        double longitude = Double.parseDouble(mEmployeeModel.getLongitude());

        final Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String adString = null;
        try {
            List<Address> list;
            list = geocoder.getFromLocation(latitude, longitude, 1);
            if (list != null && list.size() > 0) {
                Address address = list.get(0);
                address.getLocality();

                if (address.getAddressLine(0) != null)
                    adString = address.getAddressLine(0);

            }


        } catch (final IOException e) {
            new Thread(() -> Log.e("DEBUG", "Geocoder ERROR", e)).start();
        }

        holder.lokasi.setText(adString);

        String input = mEmployeeModel.getDate_created();
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);

        Date date;
        try {
            date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(input);
            long milliseconds = date.getTime();
            long updatetime = Long.parseLong(String.valueOf(milliseconds));

            Calendar now = Calendar.getInstance();
            now.setTime(new Date());
            now.getTimeInMillis();

            long dura = now.getTimeInMillis() - updatetime;
            holder.workingHours.setText("Working time "+Utils.getDuration(dura/1000) +", range from working location is "+
                    Utils.getJarak(Integer.parseInt(mEmployeeModel.getRange_loc())));

            holder.tgl.setText(simpleDateFormat2.format(updatetime));

        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.bmb.clearBuilders();
        for (int i = 0; i < holder.bmb.getPiecePlaceEnum().pieceNumber(); i++) {
            HamButton.Builder builder = new HamButton.Builder();


            if (i == 0) {
                builder.normalImageRes(R.drawable.ic_face_black_48dp)
                        .normalText((mEmployeeModel.getEmployee_name() + " details"))
                        .subNormalText("Click to see employee details");
            }

            if (i == 1) {
                builder.normalImageRes(R.mipmap.ic_check_black)
                        .normalText("Checklist by " + mEmployeeModel.getEmployee_name())
                        .subNormalText("Click to see employee checklist");
            }
            if (i == 2) {
                builder.normalImageRes(R.mipmap.barcode_img)
                        .normalText("Tags by " + mEmployeeModel.getEmployee_name())
                        .subNormalText("Click to see employee tags");
            }

            if (i == 3) {
                builder.normalImageRes(R.mipmap.ic_phone)
                        .normalText("Call " + mEmployeeModel.getEmployee_name())
                        .subNormalText("Click to call employee");
            }


            builder.listener(index -> {
                // When the boom-button corresponding this builder is clicked.
                if (index == 0) {

                    Intent i1 = new Intent(context, EmployeeProfile.class);
                    i1.putExtra("email", mEmployeeModel.getEmail());
                    View sharedView = holder.imgprofile;
                    String transitionName = context.getString(R.string.imgProfile);
                    ActivityOptions transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) context, sharedView, transitionName);
                    context.startActivity(i1, transitionActivityOptions.toBundle());

                }

                if (index == 1) {
                    if (!mLevelUser.equals("2")) {
                        Intent i1 = new Intent(context, FragmentContainer.class);
                        i1.putExtra("id_fragement", "Checklist Employee");
                        i1.putExtra("Memail", mEmployeeModel.getEmail());
                        i1.putExtra("nama", mEmployeeModel.getEmployee_name());
                        context.startActivity(i1);
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

                        Intent i1 = new Intent(context, FragmentContainer.class);
                        i1.putExtra("id_fragement", "Tags Employee");
                        i1.putExtra("Memail", mEmployeeModel.getEmail());
                        i1.putExtra("nama", mEmployeeModel.getEmployee_name());

                        context.startActivity(i1);
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
                    new AlertDialog.Builder(context)
                            .setTitle("Make a Call")
                            .setIcon(R.mipmap.ic_phone)
                            .setMessage("Do you want to make a call to " + mEmployeeModel.getEmployee_name() + "?" +
                                    "\nPhone1 " + mEmployeeModel.getPhone1() + "" +
                                    "\nPhone2 " + mEmployeeModel.getPhone2())
                            .setPositiveButton("Phone 1", (dialog, id) -> {
                                String phone_no = mEmployeeModel.getPhone1().replaceAll("-", "");
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
                                String phone_no = mEmployeeModel.getPhone2().replaceAll("-", "");
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

                            .show();
                }
            });
            holder.bmb.addBuilder(builder);
        }


        holder.cardView.setOnClickListener(v -> holder.bmb.boom());


    }

    @Override
    public int getItemCount() {
        //Log.d("DEBUG", "getItemCount: "+nodeList.size());
        return nodeList.size();
    }
}
