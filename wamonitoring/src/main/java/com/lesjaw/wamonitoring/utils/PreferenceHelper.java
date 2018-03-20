package com.lesjaw.wamonitoring.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Rahman on 12/26/2016.
 */

public class PreferenceHelper {
    private SharedPreferences customCachedPrefs;
    private Context context;
    private int homeThold;

    public PreferenceHelper(Context context) {
        super();
        this.context = context;
        customCachedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        initializePrefs();
    }

    public Context getContex() {
        return context;
    }

    public SharedPreferences getCustomPref() {
        return customCachedPrefs;
    }

    public void setUserName(String division) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("UserNameFirebase", String.valueOf(division));
        mEditor.apply();
    }
    public String getUserName() {
        return (customCachedPrefs.getString("UserNameFirebase", "0"));
    }

    public void setUserEmail(String division) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("FirebaseEmail", String.valueOf(division));
        mEditor.apply();
    }


    public void setGoogle(int gID) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putInt("gID", gID);
        mEditor.apply();
    }

    public int getGoogle() {
        return (customCachedPrefs.getInt("gID", 1));
    }


    public double getPhoneLatitude() {
        return Double.parseDouble(customCachedPrefs.getString("phonelatitude", "0"));
    }

    public void setPhoneLatitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("phonelatitude", String.valueOf(lat));
        mEditor.apply();
    }

    public double getPhoneLongitude() {
        return Double.parseDouble(customCachedPrefs.getString("phonelongitude", "0"));
    }

    public void setPhoneLongitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("phonelongitude", String.valueOf(lat));
        mEditor.apply();
    }

    public double getTagLatitude() {
        return Double.parseDouble(customCachedPrefs.getString("taglatitude", "0"));
    }

    public void setTagLatitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("taglatitude", String.valueOf(lat));
        mEditor.apply();
    }

    public double getTagLongitude() {
        return Double.parseDouble(customCachedPrefs.getString("taglongitude", "0"));
    }

    public void setTagLongitude(double lat) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("taglongitude", String.valueOf(lat));
        mEditor.apply();
    }

    public void setLevelUser(String level_user) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("level_user", String.valueOf(level_user));
        mEditor.apply();
    }

    public String getLevelUser() {
        return (customCachedPrefs.getString("level_user", "0"));
    }

    public void setPackage(String user_package) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("package_user", String.valueOf(user_package));
        mEditor.apply();
    }

    public String getPackage() {
        return (customCachedPrefs.getString("package_user", "0"));
    }


    public void setDivisionFull(String division) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("divisionfull", String.valueOf(division));
        mEditor.apply();
    }

    public String getDivisionFull() {
        return (customCachedPrefs.getString("divisionfull", "0"));
    }

    public void setEmployment(String division) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("employment", String.valueOf(division));
        mEditor.apply();
    }

    public String getEmployment() {
        return (customCachedPrefs.getString("employment", "0"));
    }

    public void setEmployee_status(String division) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("employee_status", String.valueOf(division));
        mEditor.apply();
    }

    public String getEmployee_status() {
        return (customCachedPrefs.getString("employee_status", "0"));
    }

    public void setLastLog(String lastLog) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("lastLog", String.valueOf(lastLog));
        mEditor.apply();
    }

    public String getLastLog() {
        return (customCachedPrefs.getString("lastLog", "0"));
    }

    public void setUserCount(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("user_count", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getUserCount() {
        return (customCachedPrefs.getString("user_count", "0"));
    }

    public void setTagsCount(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("tags_count", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getTagsCount() {
        return (customCachedPrefs.getString("tags_count", "0"));
    }

    public void setUserCountDiv(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("user_countDiv", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getUserCountDiv() {
        return (customCachedPrefs.getString("user_countDiv", "0"));
    }

    public void setTagsCountDiv(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("tags_countDiv", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getTagsCountDiv() {
        return (customCachedPrefs.getString("tags_countDiv", "0"));
    }

    public void setDivName(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("divName", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getDivName() {
        return (customCachedPrefs.getString("divName", "0"));
    }


    public void setDateCreated(long userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("date_created", String.valueOf(userCount));
        mEditor.apply();
    }

    public long getDateCreated() {
        return Long.parseLong(customCachedPrefs.getString("date_created", "0"));
    }

    public void setGroup(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("group", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getGroup() {
        return (customCachedPrefs.getString("group", "0"));
    }

    public void setLogin(String userCount) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("login", String.valueOf(userCount));
        mEditor.apply();
    }

    public String getLogin() {
        return (customCachedPrefs.getString("login", "0"));
    }

    public void setServerTime(long server_time) {
        SharedPreferences.Editor mEditor = customCachedPrefs.edit();
        mEditor.putString("server_time", String.valueOf(server_time));
        mEditor.apply();
    }

    public long getServerTime() {
        return Long.parseLong(customCachedPrefs.getString("server_time", "0"));
    }

    public void initializePrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        homeThold = prefs.getInt("distanceThold", 150);

    }
}
