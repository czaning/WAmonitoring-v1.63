package com.lesjaw.wamonitoring.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ChatMessageAdapter;
import com.lesjaw.wamonitoring.model.ChatMessage;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class ChatUsers extends Fragment {

    private static final String TAG = "ChatActivity";
    private static final int RC_PHOTO_PICKER = 1;
    private EditText messageTxt;
    private RecyclerView messagesList;
    private ChatMessageAdapter adapter;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private String username;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String chatroom;
    private Context mContext;
    private String titleChat;
    private String times, email;
    private int chatID = 2;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_google_chat_users, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        PreferenceHelper mPrefHelper = new PreferenceHelper(getActivity());
        new OkHttpClient();

        mContext = getContext();
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        username = sharedPref.getString("real_name", "olmatix1");
        String mLevelUser = mPrefHelper.getLevelUser();

        TextView labelchat = (TextView) view.findViewById(R.id.labelchat);
        ImageView left = (ImageView) view.findViewById(R.id.arrowleft);
        ImageView right = (ImageView) view.findViewById(R.id.arrowright);

        String div = sharedPref.getString("division", "olmatix1");
        String companyID = sharedPref.getString("company_id", "olmatix1");
        email = sharedPref.getString("email", "olmatix1");

        chatroom = companyID + "-" + div.replace(" ", "");
        titleChat = mPrefHelper.getDivName();

        labelchat.setText(titleChat);
        left.setVisibility(View.VISIBLE);
        if (mLevelUser.equals("1")){
            right.setVisibility(View.VISIBLE);
        } else {
            right.setVisibility(View.GONE);
        }
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ImageButton sendBtn = (ImageButton) view.findViewById(R.id.sendBtn);
        messageTxt = (EditText) view.findViewById(R.id.messageTxt);
        messagesList = (RecyclerView) view.findViewById(R.id.messagesList);
        messagesList.setHasFixedSize(false);
        messagesList.setLayoutManager(layoutManager);

        ImageButton imageBtn;
        imageBtn = (ImageButton) view.findViewById(R.id.imageBtn);
        // Show an image picker when the user wants to upload an imasge
        imageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
        });




        sendBtn.setOnClickListener(v -> {
            SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            times = timeformat.format(System.currentTimeMillis());
            ChatMessage chat = new ChatMessage(messageTxt.getText().toString(), username, times, email);
            // Push the chat message to the database

            String checkText = messageTxt.getText().toString();
            if (checkText.isEmpty()) {
                messageTxt.setHint("type some word");
            } else {
                databaseRef.push().setValue(chat);
                sendNotification(chatroom, username, titleChat, messageTxt.getText().toString(), "");
                messageTxt.setText("");
            }

            Log.d(TAG, "users send");


        });

        adapter = new ChatMessageAdapter(getActivity(), mContext, chatID);
        messagesList.setAdapter(adapter);
        // When record added, list will scroll to bottom
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messagesList.scrollToPosition(adapter.getItemCount() - 1);
            }
        });

        // Get the Firebase app and all primitives we'll use
        FirebaseApp app = FirebaseApp.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance(app);
        FirebaseAuth.getInstance(app);
        storage = FirebaseStorage.getInstance(app);

        // Get a reference to our chat "room" in the database
        databaseRef = database.getReference(chatroom);
        int dataload = 300;
        Query databaseRef1 = databaseRef.startAt(null).limitToLast(dataload);

        // Listen for when child nodes get added to the collection
        databaseRef1.addChildEventListener(new ChildEventListener() {
            public void onChildAdded(DataSnapshot snapshot, String s) {
                // Get the chat message from the snapshot and add it to the UI
                ChatMessage chat = snapshot.getValue(ChatMessage.class);
                adapter.addMessage(chat, titleChat);
            }

            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            }

            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
            }

            public void onCancelled(DatabaseError databaseError) {
            }
        });

        messagesList.addOnLayoutChangeListener((v, left1, top, right1, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                messagesList.postDelayed(() -> {
                    int countlist = messagesList.getAdapter().getItemCount();
                    if (countlist>1){
                        messagesList.smoothScrollToPosition(
                                messagesList.getAdapter().getItemCount() - 1);
                    }
                }, 100);
            }
        });

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Recieved result from image picker
        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK) {
            Uri selectedImageUri = data.getData();
            // Get a reference to the location where we'll store our photos
            StorageReference storageRef = storage.getReference("chat_photos");
            // Get a reference to store file at chat_photos/<FILENAME>
            final StorageReference photoRef = storageRef.child(selectedImageUri.getLastPathSegment());
            // Upload file to Firebase Storage
            photoRef.putFile(selectedImageUri)
                    .addOnSuccessListener(getActivity(), taskSnapshot -> {
                        SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
                        times = timeformat.format(System.currentTimeMillis());
                        // When the image has successfully uploaded, we get its download URL
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        // Send message with Image URL
                        ChatMessage chat = new ChatMessage(downloadUrl.toString(), username, times, email);
                        databaseRef.push().setValue(chat);
                        sendNotification(chatroom, username, titleChat, "Pictures received", downloadUrl.toString());

                        messageTxt.setText("");
                    });
        }
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
                    root.put("to", "/topics/" + reg_token);

                    RequestBody body = RequestBody.create(JSON, root.toString());
                    Request request = new Request.Builder()
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

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("notify");
            Log.d("DEBUG", "onReceive1: " + message);
            if (message != null) {
                messageTxt.setText(message+"\n_______________\n\n");
                int textLength = messageTxt.getText().length();
                messageTxt.setSelection(textLength, messageTxt.getText().toString().length());
                messageTxt.setFocusable(true);
                InputMethodManager im = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                im.showSoftInput(messageTxt, 0);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(
                mMessageReceiver, new IntentFilter(String.valueOf(chatID)));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mMessageReceiver);

    }
}