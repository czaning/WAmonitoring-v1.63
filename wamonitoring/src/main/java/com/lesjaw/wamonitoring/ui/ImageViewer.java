package com.lesjaw.wamonitoring.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.github.chrisbanes.photoview.PhotoView;
import com.koushikdutta.ion.Ion;
import com.lesjaw.wamonitoring.R;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageViewer extends AppCompatActivity {

        String imgURL, imgFoto;

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.picture_viewer);

            PhotoView img = (PhotoView ) findViewById(R.id.img_profile);
            FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
            ProgressBar progress = (ProgressBar) findViewById(R.id.loading);
            CircleImageView foto = (CircleImageView) findViewById(R.id.img_foto);

            Intent i = getIntent();
            imgURL = i.getStringExtra("img");
            imgFoto = i.getStringExtra("foto");

            Log.d("DEBUG", "onCreate: "+imgURL);
            progress.setVisibility(View.GONE);

           /* Ion.with(getBaseContext())
                    .load(imgURL)
                    .withBitmap()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.ic_upload_error)
                    .animateLoad(R.anim.spin_animation)
                    .intoImageView(img);*/

            Ion.with(img)
                    .placeholder(R.drawable.placeholder_image)
                    .smartSize(false)
                    .error(R.drawable.ic_upload_error)
                    .animateLoad(R.anim.spin_animation)
                    .animateIn(R.anim.trans_left_in)
                    .load(imgURL);

            /*Picasso.with(getBaseContext())
                    .load(imgURL)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(img, new com.squareup.picasso.Callback() {

                                @Override
                                public void onSuccess() {
                                    progress.setVisibility(View.GONE);
                                }

                                @Override
                                public void onError() {

                                }
                            });*/

            Picasso.with(getBaseContext())
                    .load(imgFoto)
                    .into(foto, new com.squareup.picasso.Callback() {

                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {

                        }
                    });


            fab.setOnClickListener(v -> finish());

            img.setOnClickListener(v -> {
                if (fab.isShown()){
                    fab.hide();
                    foto.setVisibility(View.INVISIBLE);
                } else {
                    fab.show();
                    foto.setVisibility(View.VISIBLE);
                }
            });
        }

}
