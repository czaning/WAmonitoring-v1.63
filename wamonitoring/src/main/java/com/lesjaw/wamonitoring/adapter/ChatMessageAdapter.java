package com.lesjaw.wamonitoring.adapter;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.ChatMessage;
import com.lesjaw.wamonitoring.ui.EmployeeProfile;
import com.lesjaw.wamonitoring.ui.ImageViewer;
import com.lesjaw.wamonitoring.utils.Utils;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatMessageAdapter extends RecyclerView.Adapter<ChatMessageAdapter.ChatMessageViewHolder> {

    private static final String TAG = "ChatMessageAdapter";
    private final Activity activity;
    List<ChatMessage> messages = new ArrayList<>();
    Context mContext;
    SharedPreferences sharedPref;
    String mUserName;
    int selected_position = 0;
    int chatID;

    public ChatMessageAdapter(Activity activity, Context mContext, int chatID) {
        this.activity = activity;
        this.mContext = mContext;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        mUserName = sharedPref.getString("email", "olmatix1");
        this.chatID = chatID;

    }

    public void addMessage(ChatMessage chat, String from) {
        //Log.d(TAG, "addMessage: "+chat +" "+from);
        messages.add(chat);
        notifyItemInserted(messages.size());
    }

    public void datachange(ChatMessage chat) {
        messages.add(chat);
        notifyItemInserted(messages.size());
    }


    @Override
    public ChatMessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ChatMessageViewHolder(activity, activity.getLayoutInflater().inflate(R.layout.layout_chat, parent, false));
    }

    @Override
    public void onBindViewHolder(ChatMessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
        holder.itemView.setBackgroundColor(selected_position == position ? Color.CYAN : Color.TRANSPARENT);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }


    public class ChatMessageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        //private final Activity activity;

        TextView name, message, time;
        ImageView image;
        ImageButton reply;
        CardView chatCard;
        View view1, view2;
        CircleImageView cropImageView, cropImageView1;

        public ChatMessageViewHolder(Activity activity, View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            //this.activity = activity;
            chatCard = (CardView) itemView.findViewById(R.id.chatCard);
            name = (TextView) itemView.findViewById(R.id.user);
            message = (TextView) itemView.findViewById(R.id.message);
            time = (TextView) itemView.findViewById(R.id.time);
            view1 = (View) itemView.findViewById(R.id.view1);
            view2 = (View) itemView.findViewById(R.id.view2);
            cropImageView = (CircleImageView) itemView.findViewById(R.id.img_profile);
            image = (ImageView) itemView.findViewById(R.id.imageview);
            cropImageView1 = (CircleImageView) itemView.findViewById(R.id.img_profile1);
            reply = (ImageButton) itemView.findViewById(R.id.reply);

            /*reply = new ImageButton(activity);
            ((ViewGroup) itemView).addView(reply);*/

        }


        //Bind data model with View
        public void bind(ChatMessage chat) {
            if (getAdapterPosition() == RecyclerView.NO_POSITION || selected_position == 0) {
                reply.setVisibility(View.GONE);
            } else {
                if (mUserName.equals(chat.getEmail())) {

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

                    layoutParams.setMargins(0, 0, 0, 0);

                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    reply.setLayoutParams(layoutParams);
                    reply.setVisibility(View.VISIBLE);
                    //reply.setVisibility(View.GONE);
                    Log.d(TAG, "Self user: ");

                } else {
                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT);

                    layoutParams.setMargins(0, 0, 0, 0);

                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    reply.setLayoutParams(layoutParams);
                    reply.setVisibility(View.VISIBLE);
                    Log.d(TAG, "user lain: ");

                }
            }

            if (mUserName.equals(chat.getEmail())) {
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(220, 0, 0, 0);

                layoutParams.addRule(RelativeLayout.LEFT_OF, R.id.img_profile1);
                chatCard.setLayoutParams(layoutParams);
                chatCard.setCardBackgroundColor(Color.parseColor("#cce6ff"));
                view2.setVisibility(View.GONE);
                view1.setVisibility(View.VISIBLE);
                //message.setGravity(Gravity.RIGHT);

                Picasso.with(mContext)
                        .load("https://olmatix.com/wamonitoring/foto_profile/" + chat.getEmail() + ".jpg")
                        .placeholder(R.mipmap.logo)
                        .into(cropImageView1);

                cropImageView.setVisibility(View.GONE);
                cropImageView1.setVisibility(View.VISIBLE);

            } else {

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(0, 0, 220, 0);

                layoutParams.addRule(RelativeLayout.RIGHT_OF, R.id.img_profile);
                chatCard.setLayoutParams(layoutParams);
                chatCard.setCardBackgroundColor(Color.parseColor("#e6ffe6"));
                view1.setVisibility(View.GONE);
                view2.setVisibility(View.VISIBLE);
                //message.setGravity(Gravity.LEFT);

                Picasso.with(mContext)
                        .load("https://olmatix.com/wamonitoring/foto_profile/" + chat.getEmail() + ".jpg")
                        .placeholder(R.mipmap.logo)
                        .into(cropImageView);
                cropImageView1.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);

            }

            name.setText(chat.getName());
            //Message is an image
            if (chat.getMessage().startsWith("https://firebasestorage.googleapis.com/")
                    || chat.getMessage().startsWith("content://")
                    || chat.getMessage().startsWith("https://olmatix.com/wamonitoring/animasi/")
                    || chat.getMessage().startsWith("https://media.giphy.com/media/")) {
                message.setVisibility(View.GONE);
                image.setVisibility(View.VISIBLE);
               /* Glide.with(mContext)
                        .load(chat.getMessage())
                        .asGif()
                        .placeholder(R.drawable.progress_animation)
                        .into(image);*/

                Ion.with(mContext)
                        .load(chat.getMessage())
                        .withBitmap()
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.ic_upload_error)
                        .animateLoad(R.anim.spin_animation)
                        .intoImageView(image);

                chatCard.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(mContext, ImageViewer.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.putExtra("img", chat.getMessage());
                        i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/" + chat.getEmail() + ".jpg");

                        mContext.startActivity(i);
                    }
                });

            } else {
                message.setVisibility(View.VISIBLE);
                image.setVisibility(View.GONE);
                message.setText(chat.getMessage());
            }

            String input = chat.getTime();
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
                //Log.d("DEBUG", "onBindViewHolder: "+Utils.getTimeAgo(cal) + " | "+simpleDateFormat1.format(updatetime));
                if (Utils.getTimeAgo(cal).contains("Days ago")){
                    time.setText(simpleDateFormat2.format(updatetime));

                } else {
                    time.setText(Utils.getTimeAgo(cal) + " | "+simpleDateFormat1.format(updatetime));

                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

            time.setGravity(Gravity.RIGHT);


            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(mContext, ImageViewer.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("img", chat.getMessage());
                    i.putExtra("foto", "https://olmatix.com/wamonitoring/foto_profile/" + chat.getEmail() + ".jpg");

                    mContext.startActivity(i);
                }
            });

            chatCard.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (getAdapterPosition() == RecyclerView.NO_POSITION)
                        return true;

                    // Updating old as well as new positions
                    notifyItemChanged(selected_position);
                    selected_position = getAdapterPosition();
                    notifyItemChanged(selected_position);

                    return false;
                }
            });

            reply.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewGone();

                    //Toast.makeText(mContext, chat.getName() + "\n" + chat.getMessage(), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(String.valueOf(chatID));
                    intent.putExtra("notify", "In Reply to "+chat.getName() + "\n" + chat.getMessage());
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                }
            });

            chatCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    viewGone();


                }
            });

            cropImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(mContext, EmployeeProfile.class);
                    i.putExtra("email", chat.getEmail());
                    View sharedView = cropImageView;
                    String transitionName = mContext.getString(R.string.imgProfile);

                    ActivityOptions transitionActivityOptions = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                        transitionActivityOptions = ActivityOptions.makeSceneTransitionAnimation((Activity) mContext, sharedView, transitionName);
                    }
                    mContext.startActivity(i, transitionActivityOptions.toBundle());
                }
            });

        }

        @Override
        public void onClick(View v) {
          viewGone();
        }

        @Override
        public boolean onLongClick(View v) {
            // Below line is just like a safety check, because sometimes holder could be null,
            // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
            if (getAdapterPosition() == RecyclerView.NO_POSITION)
                return true;

            // Updating old as well as new positions
            notifyItemChanged(selected_position);
            selected_position = getAdapterPosition();
            notifyItemChanged(selected_position);

            return false;
        }

        private void viewGone (){
            selected_position = 0;
            reply.setVisibility(View.GONE);

            notifyItemChanged(selected_position);
            notifyDataSetChanged();
        }
    }




}