package com.lesjaw.wamonitoring.service;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.MainActivity;
import com.lesjaw.wamonitoring.ui.fragment.TagInfo;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.NotificationUtils;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class salmonFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = salmonFirebaseMessagingService.class.getSimpleName();
    private Context context;

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.e(TAG, "From: " + remoteMessage.getFrom());

        context = getApplicationContext();

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Notification Body: " + remoteMessage.getNotification().getBody()+remoteMessage.getNotification().getTitle());
            handleNotification(remoteMessage.getNotification().getBody(),remoteMessage.getNotification().getTitle());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.e(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message, String title) {
        if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
            // app is in foreground, broadcast the push message
            Intent pushNotification = new Intent(Config.SEND_MESSAGE);
            String mMessage = null;
            if (message.equals("Open EMP please")){
                mMessage = title+" sending notification";
            }
            pushNotification.putExtra("message", mMessage);
            LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
           // Log.d(TAG, "handleNotification app foreground: ");

        } else {
            // If the app is in background, firebase itself handles the notification
            // play notification sound
            //Log.d(TAG, "handleNotification app background: ");
            NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
            notificationUtils.playNotificationSound();
        }
    }

    private void handleDataMessage(JSONObject json) {
        //Log.e(TAG, "push json: " + json.toString());
        String type;

        try {
            JSONObject data = json.getJSONObject("data");
            String title = data.getString("title");
            String message = data.getString("message");
            //boolean isBackground = data.getBoolean("is_background");
            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");


            AudioManager audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            //int currentVolume = audio.getStreamVolume(AudioManager.STREAM_ALARM);
            int currentVolume1 = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
            //float percent = 0.7f;

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceHelper mPrefHelper = new PreferenceHelper(context);

            String real_name = sharedPref.getString("real_name", "jakarta");
            String mDivision = sharedPref.getString("division", "jakarta");
            final Boolean mPanicSwitch = sharedPref.getBoolean("notifications_new_message_panic", true);
            //String mUser = sharedPref.getString("email", "jakarta");
            //String mCompanyID = sharedPref.getString("company_id", "jakarta");
            //String mLevelUser = mPrefHelper.getLevelUser();
            String absence = mPrefHelper.getLogin();
            String input = timestamp;
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("dd MMM HH:mm", Locale.ENGLISH);
            String mDiv = mPrefHelper.getDivName();
            Date date;
            try {
                date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).parse(input);
                long milliseconds = date.getTime();
                long updatetime = Long.parseLong(String.valueOf(milliseconds));

                timestamp = simpleDateFormat2.format(updatetime);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (imageUrl.equals("PANIC!")) {
                type = "1";
                if (!title.equals(real_name)) {

                    if (mPanicSwitch && absence.equals("1")) {

                        audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
                        Log.d(TAG, "media play set: PANIC, curent vol: " + currentVolume1);
                        MediaPlayer ring;
                        new MediaPlayer();
                        ring = MediaPlayer.create(context, R.raw.ambulance);
                        ring.start();

                        ring.setOnCompletionListener(mp -> {
                            // TODO Auto-generated method stub
                            mp.release();
                            audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume1, AudioManager.FLAG_PLAY_SOUND);
                            int currentVolume2 = audio.getStreamVolume(AudioManager.STREAM_MUSIC);

                            Log.d(TAG, "after media play set back: PANIC, curent vol: " + currentVolume2);
                        });

                    }


                }
            } else if (imageUrl.equals("TAGS")) {
                type = "2";

                if (mDiv.equals(message)|| mDivision.equals("none")) {
                    mPrefHelper.setLastLog(message + " by " + title + " at " + timestamp);

                }

                updateWidget();

            } else if (imageUrl.startsWith("PING,")) {
                type = "3";

                String ping[] = imageUrl.split(",");
                String regID = ping[1];
                Log.d(TAG, "handleDataMessage: PING "+regID);
                sendNotification(regID, real_name, mDivision, "Ping reply", "REPLY");


            } else if (imageUrl.startsWith("REPLY")) {
                type = "4";


            }  else {
                type = "0";

            }


            if (!NotificationUtils.isAppIsInBackground(getApplicationContext())) {
                // app is in foreground, broadcast the push message
                if (imageUrl.equals("TAGS") && mDivision.equals(message) || mDivision.equals("none")) {
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", type);
                    pushNotification.putExtra("message", message);
                    pushNotification.putExtra("user", title);
                    pushNotification.putExtra("timestamp", timestamp);

                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                    Log.d(TAG, "APP in foreground send broadcast: ");
                } else {
                    Intent pushNotification = new Intent(Config.PUSH_NOTIFICATION);
                    pushNotification.putExtra("type", type);
                    pushNotification.putExtra("message", message);
                    pushNotification.putExtra("user", title);
                    pushNotification.putExtra("timestamp", timestamp);

                    LocalBroadcastManager.getInstance(this).sendBroadcast(pushNotification);
                    Log.d(TAG, "APP in foreground send broadcast: ");
                }

            } else {
                // app is in background, show the notification in notification tray
                if (imageUrl.equals("TAGS") && mDivision.equals(message) || mDivision.equals("none")) {
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                    resultIntent.putExtra("message", message);
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);

                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                    notificationUtils.playNotificationSound();
                } else {
                    Intent resultIntent = new Intent(getApplicationContext(), MainActivity.class);
                    resultIntent.putExtra("message", message);
                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);

                    showNotificationMessage(getApplicationContext(), title, message, timestamp, resultIntent);

                    // play notification sound
                    NotificationUtils notificationUtils = new NotificationUtils(getApplicationContext());
                    notificationUtils.playNotificationSound();
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Json Exception: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Showing notification with text only
     */
    private void showNotificationMessage(Context context, String title, String message, String timeStamp, Intent intent) {
        NotificationUtils notificationUtils = new NotificationUtils(context);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        notificationUtils.showNotificationMessage(title, message, timeStamp, intent);
    }


    private void updateWidget() {
        Intent intent = new Intent(this, TagInfo.class);
        intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        int ids[] = AppWidgetManager.getInstance(getApplication()).getAppWidgetIds(new ComponentName(getApplication(), TagInfo.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
        sendBroadcast(intent);

    }

    private void sendNotification(final String reg_token, final String username, final String title, final String message,
                                  final String image) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss",Locale.ENGLISH);
                    String times = timeformat.format(System.currentTimeMillis());

                    JSONObject root = new JSONObject();
                    JSONObject data = new JSONObject();
                    JSONObject payload = new JSONObject();

                    payload.put("title", username);
                    payload.put("is_background", false);
                    payload.put("message", message + " | " + title);
                    payload.put("image", image);
                    payload.put("timestamp", times);

                    data.put("data", payload);
                    root.put("data", data);
                    root.put("to", reg_token);

                    RequestBody body = RequestBody.create(JSON, root.toString());
                    com.squareup.okhttp.Request request = new com.squareup.okhttp.Request.Builder()
                            .header("Authorization", "key=AIzaSyAwU7DMeeysvpQjcwZsS3hJFfx8wWcrpNU")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d(TAG, "doInBackground: " + finalResponse);
                } catch (Exception e) {
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }

}
