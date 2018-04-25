package com.lesjaw.wamonitoring.ui.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.toolbox.StringRequest;
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
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.adapter.ChatMessageAdapter;
import com.lesjaw.wamonitoring.adapter.emojiGifAdapter;
import com.lesjaw.wamonitoring.model.ChatMessage;
import com.lesjaw.wamonitoring.model.emojiGifModel;
import com.lesjaw.wamonitoring.utils.Config;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

import static android.app.Activity.RESULT_OK;


public class ChatCompany extends Fragment {

    private static final String TAG = "ChatActivity";
    private static final int RC_PHOTO_PICKER = 1;
    private EditText messageTxtCom;
    private RecyclerView messagesListCom;
    private ChatMessageAdapter adapter;
    private ImageButton emojiBtn;
    private FirebaseStorage storage;
    private DatabaseReference databaseRef;
    private String username;
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    private String chatroom;
    private Context mContext;
    private String titleChat;
    private String times, email;
    private int chatID = 0;
    private LinearLayout mRevealView;
    private boolean hidden = true;
    private List<emojiGifModel> tagList = new ArrayList<>();
    private emojiGifAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_google_chat_company, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new OkHttpClient();

        mContext = getContext();
        TextView labelchat = (TextView) view.findViewById(R.id.labelchat);
        ImageView left = (ImageView) view.findViewById(R.id.arrowleft);
        RecyclerView mRecycleView = (RecyclerView) view.findViewById(R.id.rv);

        mRevealView = (LinearLayout) view.findViewById(R.id.reveal_items);
        mRevealView.setVisibility(View.GONE);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        username = sharedPref.getString("real_name", "olmatix1");

        String companyname = sharedPref.getString("company_name", "olmatix1");
        String companyID = sharedPref.getString("company_id", "olmatix1");
        email = sharedPref.getString("email", "olmatix1");

        chatroom = companyID;
        titleChat = companyname;

        labelchat.setText(titleChat);
        left.setVisibility(View.INVISIBLE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        ImageButton sendBtnCom = (ImageButton) view.findViewById(R.id.sendBtnCom);

        mAdapter = new emojiGifAdapter(tagList, mContext);
        LinearLayoutManager linlayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        mRecycleView.setLayoutManager(linlayoutManager);
        mRecycleView.setItemAnimator(new DefaultItemAnimator());
        mRecycleView.setAdapter(mAdapter);

        messageTxtCom = (EditText) view.findViewById(R.id.messageTxtCom);
        messagesListCom = (RecyclerView) view.findViewById(R.id.messagesListCom);
        messagesListCom.setHasFixedSize(false);
        messagesListCom.setLayoutManager(layoutManager);

        emojiBtn = (ImageButton) view.findViewById(R.id.emojiBtnCom);
        ImageButton imageBtn = (ImageButton) view.findViewById(R.id.imageBtnCom);
        // Show an image picker when the user wants to upload an imasge
        imageBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/jpeg");
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
            startActivityForResult(Intent.createChooser(intent, "Complete action using"), RC_PHOTO_PICKER);
        });

        messageTxtCom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emojiBtn.setVisibility(View.GONE);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sendBtnCom.setOnClickListener(v -> {
            SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
            times = timeformat.format(System.currentTimeMillis());
            ChatMessage chat = new ChatMessage(messageTxtCom.getText().toString(), username, times, email);
            // Push the chat message to the database

            String checkText = messageTxtCom.getText().toString();
            if (checkText.isEmpty()) {
                messageTxtCom.setHint("type some word");
            } else {
                databaseRef.push().setValue(chat);
                sendNotification(chatroom, username, titleChat, messageTxtCom.getText().toString(), "");

                messageTxtCom.setText("");
                emojiBtn.setVisibility(View.VISIBLE);

            }
        });

        adapter = new ChatMessageAdapter(getActivity(), mContext, chatID);
        messagesListCom.setAdapter(adapter);
        // When record added, list will scroll to bottom
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            public void onItemRangeInserted(int positionStart, int itemCount) {
                messagesListCom.scrollToPosition(adapter.getItemCount() - 1);
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

        messagesListCom.addOnLayoutChangeListener((v, left1, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                messagesListCom.postDelayed(() -> {
                    int countlist = messagesListCom.getAdapter().getItemCount();
                    if (countlist > 1) {
                        messagesListCom.smoothScrollToPosition(
                                messagesListCom.getAdapter().getItemCount() - 1);
                    }

                }, 100);
            }
        });

        emojiBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int cx = (mRevealView.getLeft() + mRevealView.getRight());
                int cy = mRevealView.getTop();
                int radius = Math.max(mRevealView.getWidth(), mRevealView.getHeight());

                getEmoGif();

                //Below Android LOLIPOP Version
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    SupportAnimator animator =
                            ViewAnimationUtils.createCircularReveal(mRevealView, cx, cy, 0, radius);
                    animator.setInterpolator(new AccelerateDecelerateInterpolator());
                    animator.setDuration(700);

                    SupportAnimator animator_reverse = animator.reverse();

                    if (hidden) {
                        mRevealView.setVisibility(View.VISIBLE);
                        animator.start();
                        hidden = false;
                    } else {
                        animator_reverse.addListener(new SupportAnimator.AnimatorListener() {
                            @Override
                            public void onAnimationStart() {

                            }

                            @Override
                            public void onAnimationEnd() {
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;

                            }

                            @Override
                            public void onAnimationCancel() {

                            }

                            @Override
                            public void onAnimationRepeat() {

                            }
                        });
                        animator_reverse.start();
                    }
                }
                // Android LOLIPOP And ABOVE Version
                else {
                    if (hidden) {
                        Animator anim = android.view.ViewAnimationUtils.
                                createCircularReveal(mRevealView, cx, cy, 0, radius);
                        mRevealView.setVisibility(View.VISIBLE);
                        anim.start();
                        hidden = false;
                    } else {
                        Animator anim = android.view.ViewAnimationUtils.
                                createCircularReveal(mRevealView, cx, cy, radius, 0);
                        anim.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mRevealView.setVisibility(View.INVISIBLE);
                                hidden = true;
                            }
                        });
                        anim.start();
                    }
                }
            }
        });


        mRecycleView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(),
                mRecycleView, new LogDaily.ClickListener() {
            @Override
            public void onClick(View view, final int position) {
                SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
                times = timeformat.format(System.currentTimeMillis());
                String emojiUrl = tagList.get(position).getUrl();
                Log.d(TAG, "onClick: "+emojiUrl);
                hideRevealView();
                ChatMessage chat = new ChatMessage(emojiUrl, username, times, email);
                databaseRef.push().setValue(chat);
                sendNotification(chatroom, username, titleChat, "Pictures received", emojiUrl);

                messageTxtCom.setText("");
                emojiBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLongClick(View view, int position) {

            }

        }));

    }

    private void hideRevealView() {
        if (mRevealView.getVisibility() == View.VISIBLE) {
            mRevealView.setVisibility(View.GONE);
            hidden = true;
        }
    }

    public void getEmoGif() {

        //String tag_json_obj = "getEmoGif";

        String url = Config.DOMAIN + "wamonitoring/get_emoji_gif.php";

        StringRequest jsonObjReq = new StringRequest(com.android.volley.Request.Method.POST, url, response -> {
            JSONObject jsonResponse = null;
            try {
                jsonResponse = new JSONObject(response);

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                if (result_code.equals("1")) {
                    JSONArray cast = jsonResponse.getJSONArray("tags");

                    tagList.clear();

                    for (int i = 0; i < cast.length(); i++) {

                        JSONObject tags_name = cast.getJSONObject(i);
                        String urls = tags_name.getString("url");
                        emojiGifModel tags = new emojiGifModel(urls);
                        tagList.add(tags);

                        //Log.d(TAG, "getEmoGif: "+urls);

                    }
                    mAdapter.notifyDataSetChanged();


                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: " + error)) {
            protected Map<String, String> getParams() {
                return new HashMap<>();
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(getContext()).addToRequestQueue(jsonObjReq);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Recieved result from image picker
        if (data != null) Log.d(TAG, "onActivityResult: " + resultCode + " " + data.getData());
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

                        messageTxtCom.setText("");
                        emojiBtn.setVisibility(View.VISIBLE);

                    });
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendNotification(final String reg_token, final String username, final String title, final String message,
                                  final String image) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    OkHttpClient client = new OkHttpClient();

                    SimpleDateFormat timeformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.ENGLISH);
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
                messageTxtCom.setText(message + "\n_______________\n\n");
                int textLength = messageTxtCom.getText().length();
                messageTxtCom.setSelection(textLength, messageTxtCom.getText().toString().length());
                messageTxtCom.setFocusable(true);
                InputMethodManager im = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                im.showSoftInput(messageTxtCom, 0);
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

    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private LogDaily.ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final LogDaily.ClickListener clicklistener) {

            this.clicklistener = clicklistener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recycleView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clicklistener != null) {
                        clicklistener.onLongClick(child, recycleView.getChildAdapterPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child = rv.findChildViewUnder(e.getX(), e.getY());
            if (child != null && clicklistener != null && gestureDetector.onTouchEvent(e)) {
                try {
                    clicklistener.onClick(child, rv.getChildAdapterPosition(child));
                } catch (NoSuchFieldException | IllegalAccessException e1) {
                    e1.printStackTrace();
                }
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

}