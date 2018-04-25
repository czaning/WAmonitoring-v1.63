package com.lesjaw.wamonitoring.ui.fragment;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.MapsActivity;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;
import com.muddzdev.styleabletoastlibrary.StyleableToast;

import java.util.Calendar;

public class CreateWorkingPlan extends Fragment {
    private Button btn_submit;
    private EditText tag_name,loc,timeInterval, time_picker;
    private SharedPreferences sharedPref;
    private PreferenceHelper mPrefHelper;
    private static final String TAG = "CreateWorkingPlanActivity";
    private CoordinatorLayout coordinatorLayout;
    StyleableToast styleableToast;
    private ProgressDialog progressDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.activity_input_working_plan, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btn_submit = (Button) view.findViewById(R.id.btn_submit);
        tag_name = (EditText)view.findViewById(R.id.tag_name);
        loc = (EditText)view.findViewById(R.id.loc);
        timeInterval = (EditText)view.findViewById(R.id.time_interval);
        time_picker = (EditText)view.findViewById(R.id.select_date);

        tag_name.setFilters( new InputFilter[] {new InputFilter.LengthFilter(15)});
        mPrefHelper = new PreferenceHelper(getContext());
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        coordinatorLayout=(CoordinatorLayout)view.findViewById(R.id.main_content);
        btn_submit.setOnClickListener(v -> submit());

        String lat = String.valueOf(mPrefHelper.getTagLatitude());
        String lot = String.valueOf(mPrefHelper.getTagLongitude());
        loc.setText(lat+","+lot);

        loc.setOnClickListener(view1 -> new AlertDialog.Builder(getContext())
                .setTitle("Set location for tag")
                .setIcon(R.drawable.ic_info_black_24dp)
                .setMessage("Do you want to autodetect or chosing from map?")
                .setPositiveButton("Map", (dialog, id) -> {

                    Intent i = new Intent(getContext(), MapsActivity.class);
                    i.putExtra("id_caller","CreateTags");
                    startActivity(i);

                })

                .setNeutralButton("Cancel", (dialog, id) -> dialog.cancel())

                .setNegativeButton("Auto", (dialog, id) -> {

                    double lat1 = mPrefHelper.getTagLatitude();
                    double lot1 = mPrefHelper.getTagLongitude();

                    loc.setText(lat1 +","+ lot1);
                })

                .show());


        Calendar myCalendar = Calendar.getInstance();


        time_picker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear,
                                          int dayOfMonth) {
                        // TODO Auto-generated method stub
                        myCalendar.set(Calendar.YEAR, year);
                        myCalendar.set(Calendar.MONTH, monthOfYear);
                        myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
//                        tgl_start.setText(year+"-"+monthOfYear+"-"+dayOfMonth);
                        pickTime();
                    }

                }, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void pickTime() {
        // TODO Auto-generated method stub
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
//                eReminderTime.setText( selectedHour + ":" + selectedMinute);
                Toast.makeText(getContext(), selectedHour + ":" + selectedMinute, Toast.LENGTH_SHORT).show();
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Select Time");
        mTimePicker.show();
    }

    public boolean validate() {
        boolean valid = true;

        String vtagName = tag_name.getText().toString();
        String vLoc = loc.getText().toString();
        String vTimeInterval = timeInterval.getText().toString();

        if (vtagName.isEmpty() || vtagName.length() < 3) {
            tag_name.setError("at least 3 characters");
            valid = false;
        } else {
            tag_name.setError(null);
        }

        if (vLoc.isEmpty() || vLoc.length() < 3) {
            loc.setError("at least 3 characters");
            valid = false;
        } else {
            loc.setError(null);
        }

        if (vTimeInterval.isEmpty() || vTimeInterval.length() < 3) {
            timeInterval.setError("at least 3 characters");
            valid = false;
        } else {
            timeInterval.setError(null);
        }



        return valid;
    }

    public void onLoginFailed() {
        styleableToast = new StyleableToast
                .Builder(getContext())
                .icon(R.drawable.ic_action_info)
                .text("Check field please!")
                .textColor(Color.WHITE)
                .backgroundColor(Color.RED)
                .build();
        styleableToast.show();
        btn_submit.setEnabled(true);
    }

    public void submit() {

        if (!validate()) {
            onLoginFailed();
            return;
        }

//        int packtype = Integer.parseInt(mPrefHelper.getPackage());
//        int tagcount = Integer.parseInt(mPrefHelper.getTagsCount());
//
//        //Log.d(TAG, "submit: "+usercount +" "+tagcount);
//
//        if (packtype==1 && tagcount>=5){
//            snacknotif("tag has reach package limit - 5 tags");
//            return;
//        }
//
//        if (packtype==2 && tagcount>=50){
//            snacknotif("tag has reach package limit - 50 tags");
//            return;
//        }
//
//        if (packtype==3 && tagcount>=150){
//            snacknotif("tag has reach package limit - 150 tags");
//            return;
//        }

        btn_submit.setEnabled(false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Creating tag checkpoint. Please wait...");
        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        String tagName = tag_name.getText().toString();
        String mloc = loc.getText().toString();

    }

    public void snacknotif (String notif){
        TSnackbar snackbar = TSnackbar.make(coordinatorLayout,notif,TSnackbar.LENGTH_INDEFINITE);
        View snackbarView = snackbar.getView();
        snackbarView.setBackgroundColor(Color.parseColor("#FF4081"));
        snackbar.setAction("OK", v -> {

        });
        snackbar.setActionTextColor(Color.BLACK);
        snackbar.show();
    }
    @Override
    public void onPause() {
        super.onPause();
    }
    @Override
    public void onResume() {
        super.onResume();

        loc.setText("");
        final PreferenceHelper mPrefHelper = new PreferenceHelper(getContext());
        String lat = String.valueOf(mPrefHelper.getTagLatitude());
        String lot = String.valueOf(mPrefHelper.getTagLongitude());
        loc.setText(lat+","+lot);
    }
}
