package com.lesjaw.wamonitoring.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.firebase.FirebaseApp;
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

public class ChatActivityCompany extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private static final int RC_PHOTO_PICKER = 1;
    private EditText messageTxt;
    private RecyclerView messagesList;
    private ChatMessageAdapter adapter;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private Query databaseRef1;
    private String username;
    private String chatRoom;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private String titleChat;
    private int dataload =300;
    private String times, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_chat);

        setupActionBar();

        Context mContext = getApplicationContext();

        PreferenceHelper mPrefHelper = new PreferenceHelper(getApplicationContext());
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String companyname = sharedPref.getString("company_name", "olmatix1");
        //String div = sharedPref.getString("division", "olmatix1");
        email = sharedPref.getString("email", "olmatix1");


        new OkHttpClient();

        username = mPrefHelper.getUserName();

        Intent i = getIntent();
        chatRoom = i.getStringExtra("room");
        String divFromAdapter = i.getStringExtra("div");

        Log.d(TAG, "onCreate: "+chatRoom+" "+chatRoom.length());

        if (chatRoom.length()==36){
            setTitle("Group " + companyname);
            titleChat = "Group " + companyname;

        } else {
            setTitle("Group "  +divFromAdapter);
            titleChat = "Group " + divFromAdapter;
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        ImageButton sendBtn = (ImageButton) findViewById(R.id.sendBtn);
        messageTxt = (EditText) findViewById(R.id.messageTxt);
        messagesList = (RecyclerView) findViewById(R.id.messagesList);
        messagesList.setHasFixedSize(false);
        messagesList.setLayoutManager(layoutManager);
        ImageButton imageBtn = (ImageButton) findViewById(R.id.imageBtn);

        // Show an image picker when the user wants to upload an imasge
        imageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
        });

        SimpleDateFormat timeformat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.ENGLISH);
        times = timeformat.format(System.currentTimeMillis());

        sendBtn.setOnClickListener(v -> {
            ChatMessage chat = new ChatMessage(messageTxt.getText().toString(), username, times, email);
            // Push the chat message to the database
            String checkText = messageTxt.getText().toString();
            if (checkText.isEmpty()){
                messageTxt.setHint("type some word");
            } else {
                databaseRef.push().setValue(chat);
                sendNotification(chatRoom, username, titleChat, messageTxt.getText().toString(), "");
                messageTxt.setText("");
            }
        });

        int chatID = 0;
        adapter = new ChatMessageAdapter(this, mContext, chatID);
        messagesList.setAdapter(adapter);
        // When record added, list will scroll to bottom
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messagesList.scrollToPosition(adapter.getItemCount()-1);
            }
        });

        // Get the Firebase app and all primitives we'll use
        FirebaseApp app = FirebaseApp.getInstance();
        assert app != null;
        FirebaseDatabase database = FirebaseDatabase.getInstance(app);
        //FirebaseAuth auth = FirebaseAuth.getInstance(app);
        storage = FirebaseStorage.getInstance(app);

        // Get a reference to our chat "room" in the database
        databaseRef = database.getReference(chatRoom);
        databaseRef1 = databaseRef.startAt(null).limitToLast(dataload);


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

        messagesList.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int pastVisibleItems = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (pastVisibleItems  == 0) {
                    databaseRef1 = databaseRef.startAt(null).limitToLast(++dataload);
                    recyclerView.post(() -> Log.d(TAG, "Update list "));
                    Log.d(TAG, "onScrolled ON TOP ");
                }
               /* if (dy > 0) {
                } else {
                }*/
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

               /* if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
                } else {
                    // Do something
                }*/
            }
        });

        messagesList.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                messagesList.postDelayed(() -> messagesList.smoothScrollToPosition(
                        messagesList.getAdapter().getItemCount() - 1), 100);
            }
        });
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                    .addOnSuccessListener(this, taskSnapshot -> {
                        // When the image has successfully uploaded, we get its download URL
                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        // Send message with Image URL
                        assert downloadUrl != null;
                        ChatMessage chat = new ChatMessage(downloadUrl.toString(), username, times, email);
                        databaseRef.push().setValue(chat);
                        sendNotification(chatRoom, username,titleChat, "Pictures received",downloadUrl.toString() );
                        messageTxt.setText("");

                    });
        }
    }


    private void sendNotification(final String reg_token, final String username, final String title, final String message,
                                  final String image) {
        new AsyncTask<Void,Void,Void>(){
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
                    payload.put("message", message+" | "+title);
                    payload.put("image", image);
                    payload.put("timestamp", times);

                    data.put ("data", payload);
                    root.put("data", data);
                    root.put("to","/topics/"+reg_token);

                    Log.d(TAG, "doInBackground: JSON"+root.toString());

                    RequestBody body = RequestBody.create(JSON, root.toString());
                    Request request = new Request.Builder()
                            .header("Authorization","key=AIzaSyAwU7DMeeysvpQjcwZsS3hJFfx8wWcrpNU")
                            .url("https://fcm.googleapis.com/fcm/send")
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String finalResponse = response.body().string();
                    Log.d(TAG, "doInBackground: "+finalResponse);
                }catch (Exception e){
                    //Log.d(TAG,e+"");
                }
                return null;
            }
        }.execute();

    }



}