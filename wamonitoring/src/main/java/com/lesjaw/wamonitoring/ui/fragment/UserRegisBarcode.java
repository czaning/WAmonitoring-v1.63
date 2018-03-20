package com.lesjaw.wamonitoring.ui.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.AESEncryptionDecryption;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;


public class UserRegisBarcode extends Fragment {
    private static final int WHITE = 0xFFFFFFFF;
    private static final int BLACK = 0xFF000000;
    private static final String TAG = "BarcodeGenerator";
    private static final String IMAGE_DIRECTORY = "/wamonitoring";
    private String barname, bardiv, barloc, barcode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View mView = inflater.inflate(R.layout.barcode_viewer, container, false);
        Bundle bundle = this.getArguments();

        barcode =  bundle.getString("barcode");
        barname =  bundle.getString("barname");
        bardiv =  bundle.getString("division");
        barloc =  bundle.getString("loc");

        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ImageView bcView = (ImageView) view.findViewById(R.id.view_bc);
        TextView lab_name = (TextView) view.findViewById(R.id.lab_name);
        TextView lab_div = (TextView) view.findViewById(R.id.lab_div);
        TextView lab_company_name = (TextView) view.findViewById(R.id.lab_company);
        FloatingActionButton share = (FloatingActionButton) view.findViewById(R.id.share);
        View rv = view.findViewById(R.id.ln);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());

        String pCompanyName = sharedPref.getString("company_name", "olmatix1");

        Log.d(TAG, "onCreate: "+barcode+":"+barname+":"+bardiv+":"+barloc);

        lab_name.setText(barname);
        lab_div.setText(bardiv);
        lab_company_name.setText(pCompanyName);
        // barcode data
        String barcode_data1 = "{\"code\":\""+barcode+"\",\"name\":\""+barname+ "\",\"division\":\"" + bardiv +
                "\",\"loc\":\"" + barloc + "\"}";

       /* byte[] data = new byte[0];
        String barcode_data;
        try {
            data = barcode_data1.getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            barcode_data = Base64.encodeToString(data, Base64.DEFAULT);
        }*/

        String barcode_data = "";
        try {
            barcode_data = AESEncryptionDecryption.encrypt(barcode_data1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        Bitmap bitmap;
        try {

            bitmap = encodeAsBitmap(barcode_data, BarcodeFormat.QR_CODE, width, height/2);
            bcView.setImageBitmap(bitmap);

        } catch (WriterException e) {
            e.printStackTrace();
        }

        share.setOnClickListener(v -> {

            Bitmap bitmap1 = takeScreenshot(rv);
            saveImage(bitmap1);
        });
    }

    Bitmap encodeAsBitmap(String contents, BarcodeFormat format, int img_width, int img_height) throws WriterException {
        if (contents == null) {
            return null;
        }
        Map<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result;
        try {
            result = writer.encode(contents, format, img_width, img_height, hints);
        } catch (IllegalArgumentException iae) {
            // Unsupported format
            return null;
        }
        int width = result.getWidth();
        int height = result.getHeight();
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        // Very crude at the moment
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return null;

    }


    public Bitmap takeScreenshot(View v) {
        //View rootView = findViewById(android.R.id.content).getRootView();
        View rootView = v.findViewById(R.id.ln);
        rootView.setDrawingCacheEnabled(true);
        return rootView.getDrawingCache();
    }


    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY + "/tags");
        // have the object build the directory structure, if needed.
        if (!wallpaperDirectory.exists()) {
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, barname + ".jpg");
            f.createNewFile();
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(getContext(),
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            shareImage(f.getAbsoluteFile());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";
    }

    private void shareImage(File file){
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.setType("image/*");

        intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
        intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(intent, "Share Screenshot"));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No App Available", Toast.LENGTH_SHORT).show();
        }
    }
}
