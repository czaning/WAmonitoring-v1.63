package com.lesjaw.wamonitoring.ui.fragment;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.lesjaw.wamonitoring.NetworkRequest;
import com.lesjaw.wamonitoring.R;
import com.lesjaw.wamonitoring.ui.ScalingScannerActivity;
import com.lesjaw.wamonitoring.ui.SplashActivity;
import com.lesjaw.wamonitoring.utils.Config;
import com.lesjaw.wamonitoring.utils.PreferenceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 */
public class TagInfo extends AppWidgetProvider {
    private String mUser,  mLevelUser, mDivision, mCompanyID ;
    AppWidgetManager appWidgetManager;
    RemoteViews views;

    public void updateAppWidget(Context context, int appWidgetId) {

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        PreferenceHelper mPrefHelper = new PreferenceHelper(context);

        mUser = sharedPref.getString("email", "jakarta");
        mDivision = sharedPref.getString("division", "jakarta");
        mCompanyID = sharedPref.getString("company_id", "jakarta");
        mLevelUser = mPrefHelper.getLevelUser();


        views = new RemoteViews(context.getPackageName(), R.layout.tag_info);

        if (mPrefHelper.getLogin().equals("1")) {
            Intent scanBarcode = new Intent(context, ScalingScannerActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, scanBarcode, 0);
            views.setOnClickPendingIntent(R.id.button2, pendingIntent);
        }

        Intent openApps = new Intent(context, SplashActivity.class);
        PendingIntent pendingIntent1 = PendingIntent.getActivity(context, 0, openApps, 0);
        views.setOnClickPendingIntent(R.id.appwidget_text, pendingIntent1);

        getTagThisMonth(context, appWidgetId );


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void getTagThisMonth(Context context, int appWidgetId) {
        appWidgetManager=AppWidgetManager.getInstance(context);

        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_this_month_byEmail.php";
                break;
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {
                    views.setTextViewText(R.id.appwidget_text1, "Tags this month " +result_message);
                    getTagToday(context, appWidgetId );

                } else {
                    views.setTextViewText(R.id.appwidget_text1, "Tags this month 0");

                }

                appWidgetManager.updateAppWidget(appWidgetId, views);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("email", mUser);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(context).addToRequestQueue(jsonObjReq);

    }

    private void getTagToday(Context context, int appWidgetId) {
        String url = null;
        switch (mLevelUser) {
            case "0":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today.php";
                break;
            case "1":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today_byDiv.php";
                break;
            case "2":
                url = Config.DOMAIN + "wamonitoring/get_tags_by_today_byEmail.php";
                break;
        }

        StringRequest jsonObjReq = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jObject = new JSONObject(response);
                String result_code = jObject.getString("success");
                String result_message = jObject.getString("message");

                if (result_code.equals("1")) {

                    views.setTextViewText(R.id.appwidget_text, result_message+" tags by today");
                } else {
                    views.setTextViewText(R.id.appwidget_text, "0 tags by today");

                }


                appWidgetManager.updateAppWidget(appWidgetId, views);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> Log.d("DEBUG", "onErrorResponse: "+error)) {
            protected Map<String, String> getParams() {
                Map<String, String> MyData = new HashMap<>();
                MyData.put("company_id", mCompanyID);
                MyData.put("division", mDivision);
                MyData.put("email", mUser);

                return MyData;
            }
        };

        // Adding request to request queue
        NetworkRequest.getInstance(context).addToRequestQueue(jsonObjReq);

    }
}

