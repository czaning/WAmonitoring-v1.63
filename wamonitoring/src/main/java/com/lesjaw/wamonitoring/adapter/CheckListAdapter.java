package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.checklistModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lesjaw@gmail.com on 16/09/2017.
 */

public class CheckListAdapter extends BaseAdapter {

    private List<checklistModel> listData;
    private SparseBooleanArray booleanArray;
    private LayoutInflater layoutInflater;
    Context context;
    String tagRID;
    boolean cklist_still_false = true;

    public CheckListAdapter(Context context, List<checklistModel> listData, String tagRID) {
        this.listData = listData;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        booleanArray = new SparseBooleanArray(listData.size());
        this.tagRID = tagRID;

    }

    @Override
    public int getCount() {
        return listData.size();
    }

    @Override
    public Object getItem(int position) {
        return listData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_item_checklist, null);
            holder = new ViewHolder();
            holder.checkList = (CheckBox) convertView.findViewById(R.id.cbchecklist);
            holder.note = (EditText) convertView.findViewById(R.id.note);
            holder.tclid = (TextView) convertView.findViewById(R.id.tclid);
            holder.isChecked = ((CheckBox) convertView.findViewById(R.id.cbchecklist)).isChecked();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        checklistModel mTagModel = listData.get(position);
        //holder.tclid.setText(mTagModel.getTag_id());

        holder.note.setText(mTagModel.getNote());
        holder.checkList.setText(mTagModel.getChecklist());
        holder.checkList
                .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        booleanArray.put(position, isChecked);
                        Log.d("DEBUG", "onCheckedChanged: "+holder.checkList.getText()+" : "+isChecked+
                                ", note : " +holder.note.getText());

                        if (isChecked) {
                            holder.note.setEnabled(false);
                            mTagModel.setNote(holder.note.getText().toString());
                            mTagModel.setCklistBool(isChecked);
                        } else {
                            holder.note.setEnabled(true);
                            mTagModel.setNote(holder.note.getText().toString());
                            mTagModel.setCklistBool(isChecked);
                        }
                    }
                });


       /* holder.note.setOnFocusChangeListener(new View.OnFocusChangeListener() {

            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    mTagModel.setNote(holder.note.getText().toString());
            }
        });*/



        holder.note.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //mTagModel.setNote(holder.note.getText().toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.length()>0) {
                    mTagModel.setNote(holder.note.getText().toString());
                    Log.d("DEBUG", "onCheckedChanged: " + holder.checkList.getText() + " : " +
                            ", note : " + holder.note.getText());
                } else {
                    mTagModel.setNote("");
                }
            }
        });

        return convertView;
    }

    static class ViewHolder {
        CheckBox checkList;
        EditText note;
        TextView tclid;
        boolean isChecked;
    }

    public SparseBooleanArray getmSpCheckedState() {
        return booleanArray;
    }

    public void PopulateAll(String trid, View v) {

        if (!cklist_still_false) {
            JSONObject obj = null;
            JSONArray jsonArray = new JSONArray();
            for (int i = 0; i < listData.size(); i++) {
                /*Log.d("DEBUG", "PopulateAll: " + listData.get(i).getChecklist() + " "
                        + listData.get(i).getCklistBool() + " tclid " +
                        listData.get(i).getTag_id()
                        + " Notes : " + listData.get(i).getNote());*/
                obj = new JSONObject();
                try {
                    obj.put("tclid", listData.get(i).getTag_id());
                    obj.put("checklist_status", listData.get(i).getCklistBool());
                    obj.put("checklist_note", listData.get(i).getNote());
                    obj.put("trid", trid);


                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                jsonArray.put(obj);
            }
            String jsonStr = jsonArray.toString();

            new setChecklist(trid, jsonStr).execute();

            //System.out.println("jsonString: " + jsonStr);
        } else {
            //Toast.makeText(context,"You must check all checklist",Toast.LENGTH_LONG).show();
            TSnackbar snackbar = TSnackbar.make(v, "There is an empty note for uncheck checklist, please make note why you " +
                    "uncheck this checklist, updating canceled!", TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#e6ffe6"));
            snackbar.show();
        }


    }

    public void checkCklist() {
        boolean okCklist = false;
        boolean noteEmpty = true;

        for (int i = 0; i < listData.size(); i++) {
            boolean cklist = listData.get(i).getCklistBool();
            String note =  listData.get(i).getNote();
            String chek =  listData.get(i).getChecklist();
            if (note.isEmpty()||note.equals("")) {
                noteEmpty = true;
            } else {
                noteEmpty = false;
            }

            if (cklist) {
                okCklist =true;
                Log.d("DEBUG", "checkCklist TRUE: "+chek+" "+cklist +" note: "+ note+" | okCklist "+okCklist);

            }
            if (!cklist && noteEmpty) {
                okCklist =false;
                Log.d("DEBUG", "checkCklist FALSE: "+chek+" "+cklist +" note: "+ note+" | okCklist "+okCklist);

            } else if (!cklist && !noteEmpty) {
                okCklist = true;
                Log.d("DEBUG", "checkCklist FALSE: " + chek + " " + cklist + " note: " + note + " | okCklist " + okCklist);

            }
                Log.d("DEBUG", "checkCklist: "+chek+" "+cklist +" note: "+ note+" | okCklist "+okCklist);
            if (!okCklist){
                cklist_still_false = true;
                break;
            } else {
                cklist_still_false = false;
            }
        }
    }

    public void selectAll() {
        for (int i = 0; i < listData.size(); i++) {
            if (!booleanArray.get(i)) {
                booleanArray.put(i, true);
            }
        }
        notifyDataSetChanged();
    }

    public void clearAll() {
        for (int i = 0; i < listData.size(); i++) {
            if (booleanArray.get(i)) {
                booleanArray.put(i, false);
            }
        }
        notifyDataSetChanged();
    }

    private class setChecklist extends AsyncTask<Void, Void, Void> {
        String tgid, json;

        public setChecklist (String tagID, String json){
            this.tgid = tagID;
            this.json = json;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            RequestQueue MyRequestQueue = Volley.newRequestQueue(context);

            String url = "https://olmatix.com/wamonitoring/insert_checklist_record.php";
            StringRequest MyStringRequest = new StringRequest(Request.Method.POST, url, response -> {
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(response);

                    JSONObject jObject = new JSONObject(response);
                    String result_code = jObject.getString("success");
                    String result_message = jObject.getString("message");

                    if (result_code.equals(1)||result_code.equals("1")) {

                        sendMessageDetail(result_message);

                    } else {

                        sendMessageDetail(result_message);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
                protected Map<String, String> getParams() {
                    Map<String, String> MyData = new HashMap<String, String>();
                    MyData.put("trid", tgid);
                    MyData.put("jsonObj", json);

                    return MyData;
                }
            };

            // Adding request to request queue

            int socketTimeout = 60000;//30 seconds - change to what you want
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            MyStringRequest.setRetryPolicy(policy);
            MyRequestQueue.add(MyStringRequest);


            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    private void sendMessageDetail(String Notify) {
        Intent intent = new Intent("tags");
        intent.putExtra("notify", Notify);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}