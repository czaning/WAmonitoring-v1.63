package com.lesjaw.wamonitoring.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.koushikdutta.ion.Ion;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.model.emojiGifModel;

import java.util.List;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class emojiGifAdapter extends RecyclerView.Adapter<emojiGifAdapter.MainViewHolder> {
    private List<emojiGifModel> nodeList;
    Context context;

    class MainViewHolder extends RecyclerView.ViewHolder {

        ImageView emojiImg;

        public MainViewHolder(View itemView) {
            super(itemView);
            emojiImg = (ImageView) itemView.findViewById(R.id.emoImg);

        }

    }

    public emojiGifAdapter(List<emojiGifModel> nodeList, Context mContext) {
        this.nodeList = nodeList;
        this.context = mContext;
    }

    @Override
    public MainViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_emoji_list, parent, false);

        return new MainViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MainViewHolder holder, int position) {
        //holder.bindData();
        final emojiGifModel mEmployeeModel = nodeList.get(position);

        String imageString = mEmployeeModel.getUrl();
        Log.d("DEBUG", "onBindViewHolder: "+imageString);
        if (imageString.length()>2) {
            Ion.with(context)
                    .load(imageString)
                    .withBitmap()
                    .placeholder(R.drawable.ic_upload)
                    .error(R.drawable.ic_upload_error)
                    .animateLoad(R.anim.spin_animation)
                    .intoImageView(holder.emojiImg);
        } else {

        }

        holder.emojiImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });


    }

    @Override
    public int getItemCount() {
        //Log.d("DEBUG", "getItemCount: "+nodeList.size());
        return nodeList.size();
    }
}
