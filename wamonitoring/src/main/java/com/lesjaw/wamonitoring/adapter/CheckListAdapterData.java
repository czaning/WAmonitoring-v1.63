package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.checklistModelData;

import java.util.List;

/**
 * Created by lesjaw@gmail.com on 16/09/2017.
 */

public class CheckListAdapterData extends BaseAdapter {

    private List<checklistModelData> listData;
    private SparseBooleanArray booleanArray;
    private LayoutInflater layoutInflater;
    Context context;

    public CheckListAdapterData(Context context, List<checklistModelData> listData) {
        this.listData = listData;
        this.context = context;
        layoutInflater = LayoutInflater.from(context);
        booleanArray = new SparseBooleanArray(listData.size());

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
        checklistModelData mTagModel = listData.get(position);

        holder.checkList.setChecked(mTagModel.getCklistBool());
        holder.checkList.setText(mTagModel.getChecklist());
        int noteSize = mTagModel.getNote().length();
        if (noteSize>0){
            holder.note.setVisibility(View.VISIBLE);
        } else {
            holder.note.setVisibility(View.GONE);

        }
        holder.note.setText(" - "+mTagModel.getNote());

        holder.checkList.setEnabled(false);
        holder.note.setEnabled(false);
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

    private void sendMessageDetail(String Notify) {
        Intent intent = new Intent("tags");
        intent.putExtra("notify", Notify);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
}