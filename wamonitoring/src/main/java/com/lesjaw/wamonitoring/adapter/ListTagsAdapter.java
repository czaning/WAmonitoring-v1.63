package com.lesjaw.wamonitoring.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.tagsModel;
import com.lesjaw.wamonitoring.ui.BarcodeGenerator;
import com.lesjaw.wamonitoring.ui.writeNFCActivity;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class ListTagsAdapter extends RecyclerView.Adapter<ListTagsAdapter.MainViewHolder> {
    private List<tagsModel> nodeList;
    Context context;
    private ProgressDialog pDialog;
    SharedPreferences sharedPref;
    private int position1;
    private int UNSELECTED = -1;
    private int selectedItem = UNSELECTED;

    class MainViewHolder extends RecyclerView.ViewHolder {

        TextView tag_id, tag_name, tag_location, tag_timer,  cklist;
        ImageView tag;
        Button print, write, checklist;
        ImageButton expandBut;
        ExpandableLayout expandableLayout;
        int i = 0;
        ProgressBar loadingCklist;
        int expand = 0;

        public MainViewHolder(View itemView) {
            super(itemView);
            tag_id = (TextView) itemView.findViewById(R.id.tag_id);
            tag_name = (TextView) itemView.findViewById(R.id.tag_name);
            //tag_division_name = (TextView) itemView.findViewById(R.id.tag_div);
            tag_timer = (TextView) itemView.findViewById(R.id.tag_time);
            tag_location = (TextView) itemView.findViewById(R.id.tag_location);
            tag = (ImageView) itemView.findViewById(R.id.tag);
            print = (Button) itemView.findViewById(R.id.print);
            write = (Button) itemView.findViewById(R.id.write);
            checklist = (Button) itemView.findViewById(R.id.checklist);
            expandBut = (ImageButton) itemView.findViewById(R.id.opt);
            expandableLayout = (ExpandableLayout) itemView.findViewById(R.id.expandable_layout);
            expandableLayout.collapse(false);
            cklist = (TextView) itemView.findViewById(R.id.cklist);
            loadingCklist = (ProgressBar) itemView.findViewById(R.id.progress);
        }

    }

    public ListTagsAdapter(List<tagsModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.frag_item_list_tags, parent, false);



        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final tagsModel mTagModel = nodeList.get(position);
        this.position1 = position;

        holder.tag_id.setText(mTagModel.getTag_id());
        holder.tag_name.setText(mTagModel.getTag_name());
        //holder.tag_division_name.setText((CharSequence) mTagModel.getTag_division_name());
        holder.tag_location.setText(mTagModel.getTag_location());
        holder.tag_timer.setText(mTagModel.getTag_timer());

        holder.tag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, BarcodeGenerator.class);
                i.putExtra("barcode", mTagModel.getTag_id().toString());
                i.putExtra("barname", mTagModel.getTag_name().toString());
                i.putExtra("division", mTagModel.getTag_division_name().toString());
                i.putExtra("loc", mTagModel.getTag_location());

                context.startActivity(i);
            }
        });

        holder.print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, BarcodeGenerator.class);
                i.putExtra("barcode", mTagModel.getTag_id().toString());
                i.putExtra("barname", mTagModel.getTag_name().toString());
                i.putExtra("division", mTagModel.getTag_division_name().toString());
                i.putExtra("loc", mTagModel.getTag_location());

                context.startActivity(i);
            }
        });

        holder.write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, writeNFCActivity.class);
                i.putExtra("barcode", mTagModel.getTag_id().toString());
                i.putExtra("barname", mTagModel.getTag_name().toString());
                i.putExtra("division", mTagModel.getTag_division_name().toString());
                i.putExtra("loc", mTagModel.getTag_location());

                context.startActivity(i);

            }
        });

        holder.checklist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText checklist_name = new EditText(context);
                checklist_name.setHint("Checklist name");
                checklist_name.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS);
                checklist_name.setMaxLines(1);

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.addView(checklist_name);

                new AlertDialog.Builder(context)
                        .setTitle("Add tags checklist")
                        .setMessage("Please type your checklist name")
                        .setView(layout)
                        .setPositiveButton("SUBMIT", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                String ichecklist_name = checklist_name.getText().toString();
                                String itid = mTagModel.getTag_id();

                                pDialog = new ProgressDialog(context);
                                pDialog.setMessage("Updating your data. Please wait...");
                                pDialog.setCancelable(true);
                                pDialog.setCanceledOnTouchOutside(false);
                                pDialog.show();

                                holder.expand = 0;

                                sendJsonAdmin(ichecklist_name,itid, view);

                            }
                        }).setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();
            }
        });

        holder.expandBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (holder != null) {
                    holder.expandBut.setSelected(false);
                    holder.expandableLayout.collapse();
                }

                if (position1 == selectedItem) {
                    selectedItem = UNSELECTED;
                } else {
                    holder.expandBut.setSelected(true);
                    holder.expandableLayout.expand();
                    selectedItem = position1;

                    holder.loadingCklist.setVisibility(View.VISIBLE);

                    if (holder.expand==0){

                        holder.cklist.setText("");

                        RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

                        String url = "https://olmatix.com/wamonitoring/get_checklist.php";
                        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {

                            JSONObject jsonResponse = null;
                            try {

                                jsonResponse = new JSONObject(response);
                                JSONObject jObject = new JSONObject(response);
                                String result_code = jObject.getString("success");
                                if (result_code.equals(1)||result_code.equals("1")) {
                                    holder.loadingCklist.setVisibility(View.GONE);

                                    JSONArray cast = jsonResponse.getJSONArray("checklist");
                                    final ArrayList<String> cklist1 = new ArrayList<String>();
                                    cklist1.clear();
                                    for (int i = 0; i < cast.length(); i++) {
                                        JSONObject tags_name = cast.getJSONObject(i);
                                        String tclid = tags_name.getString("tclid");
                                        String checklist = tags_name.getString("checklist");
                                        cklist1.add(checklist)  ;
                                        Log.d("DEBUG", "setDataList: "+checklist);
                                    }
                                    Log.d("DEBUG", "onClick: "+cklist1.size());
                                    while ( holder.i<cklist1.size() ) {
                                        holder.cklist.setText(holder.cklist.getText() + cklist1.get(holder.i) + " | ");
                                        holder.i++;
                                    }
                                    holder.expand = 1;

                                } else {
                                    holder.loadingCklist.setVisibility(View.GONE);
                                    holder.cklist.setText("Empty - No checklist");
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                            protected Map<String, String> getParams() {
                                Map<String, String> MyData = new HashMap<String, String>();
                                MyData.put("tid", mTagModel.getTag_id());

                                return MyData;
                            }
                        };

                        int socketTimeout = 60000;//30 seconds - change to what you want
                        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                        MyStringRequest.setRetryPolicy(policy);
                        MyRequestQueue.add(MyStringRequest);
                        } else {
                            holder.loadingCklist.setVisibility(View.GONE);

                    }

                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return nodeList.size();
    }

    private void sendJsonAdmin(String jChecklist, String jItid, View v){
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        String mCompanyID = sharedPref.getString("company_id", "olmatix1");

        RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

        String url = "https://olmatix.com/wamonitoring/insert_checklist.php";
        StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
            Log.d("DEBUG", "onResponse: "+response);
            parsingJson(response, v);
        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<String, String>();
                MyData.put("tid", jItid);
                MyData.put("company_id", mCompanyID);
                MyData.put("checklist", jChecklist);

                return MyData;
            }
        };

        int socketTimeout = 60000;//30 seconds - change to what you want
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        MyStringRequest.setRetryPolicy(policy);
        MyRequestQueue.add(MyStringRequest);


    }

    public void parsingJson(String json, View v1) {
        try {
            JSONObject jObject = new JSONObject(json);

            String result_json = jObject.getString("message");
            String result_code = jObject.getString("success");

            if (result_code.equals(1)||result_code.equals("1")) {

                pDialog.cancel();

                TSnackbar snackbar = TSnackbar.make(v1,"Updating success",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();



            } else {

                pDialog.cancel();

                TSnackbar snackbar = TSnackbar.make(v1,"Updating failed",TSnackbar.LENGTH_SHORT);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
