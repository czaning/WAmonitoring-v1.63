package com.lesjaw.wamonitoring.ui;

import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.utils.AESEncryptionDecryption;
import com.lesjaw.wamonitoring.utils.NFCManager;

public class writeNFCActivity extends AppCompatActivity {
    private static final String TAG = "WriteNFC";
    private NFCManager nfcMger;
    private NdefMessage message = null;
    Tag currentTag;
    CoordinatorLayout coordinatorLayout;
    Context context;
    TextView label;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_writetag);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.main_content);
        label = (TextView) findViewById(R.id.labelwrite);

        context = getApplicationContext();
        getSupportActionBar().setTitle(R.string.app_name);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        nfcMger = new NFCManager(this);

        Intent i = getIntent();
        String barcode = i.getStringExtra("barcode");
        String barname = i.getStringExtra("barname");
        String bardiv = i.getStringExtra("division");
        String barloc = i.getStringExtra("loc");

        Log.d(TAG, "onCreate: "+barcode+":"+barname+":"+bardiv+":"+barloc);

        // barcode data
        //String barcode_data1 = "{\"code\":\""+barcode+"\",\"name\":\""+barname+ "\",\"division\":\"" + bardiv +
        //        "\",\"loc\":\"" + barloc + "\"}";

        String barcode_data1 = "{\"code\":\""+barcode+"\"}";
        String barcode_data = "";
        try {
            barcode_data = AESEncryptionDecryption.encrypt(barcode_data1);
        } catch (Exception e) {
            e.printStackTrace();
        }


        if (message != null) {
            ProgressDialog dialog = new ProgressDialog(writeNFCActivity.this);
            dialog.setMessage("Make NFC Tag close to the phone, please");
            dialog.show();
        } else {

            message = nfcMger.createTextMessage(barcode_data);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcMger.disableDispatch();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);
    }


    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mMessageReceiver);

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        LocalBroadcastManager.getInstance(this).registerReceiver(
                mMessageReceiver, new IntentFilter("tags"));

        try {
            nfcMger.verifyNFC();
            //nfcMger.enableDispatch();

            Intent nfcIntent = new Intent(this, getClass());
            nfcIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, nfcIntent, 0);
            IntentFilter[] intentFiltersArray = new IntentFilter[] {};
            String[][] techList = new String[][] { { android.nfc.tech.Ndef.class.getName() }, { android.nfc.tech.NdefFormatable.class.getName() } };
            NfcAdapter nfcAdpt = NfcAdapter.getDefaultAdapter(this);
            nfcAdpt.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techList);
        }
        catch(NFCManager.NFCNotSupported nfcnsup) {
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"NFC not supported",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> finish());
            snackbar.show();
        }
        catch(NFCManager.NFCNotEnabled nfcnEn) {
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"NFC not enabled",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> {

            });
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.d("Nfc", "New intent");
        // It is the time to write the tag
        currentTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        if (message != null) {
            // nfcMger.writeTag(currentTag, message);
            nfcMger.createWrite(currentTag, message,context);

            //dialog.dismiss();
            TSnackbar snackbar = TSnackbar.make(coordinatorLayout,"Trying to write NFC tag",TSnackbar.LENGTH_LONG);
            View snackbarView = snackbar.getView();
            snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
            snackbar.setAction("OK", v -> {

            });
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.show();
            message = null;
        }

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("notify");
            Log.d("DEBUG", "onReceive1: " + message);
            if (message != null) {

                TSnackbar snackbar = TSnackbar.make(coordinatorLayout, message, TSnackbar.LENGTH_LONG);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
                snackbar.setAction("OK", v -> {

                });
                snackbar.setActionTextColor(Color.BLACK);
                snackbar.show();

                label.setText(message);

            }
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //Write your logic here
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
