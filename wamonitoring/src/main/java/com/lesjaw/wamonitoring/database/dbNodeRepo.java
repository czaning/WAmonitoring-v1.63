package com.lesjaw.wamonitoring.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import static com.lesjaw.wamonitoring.database.dbNode.KEY_DIVISION_NAME;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_EMPLOYEE_EMAIL;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_EMPLOYEE_NAME;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_EMPLOYMENT_NAME;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_EMPLOYEE_STATUS;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_TAG_CHECK;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_TAG_JAM;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_TAG_JARAK;
import static com.lesjaw.wamonitoring.database.dbNode.KEY_TAG_TGL;
import static com.lesjaw.wamonitoring.database.dbNode.TABLE_DIVISION;
import static com.lesjaw.wamonitoring.database.dbNode.TABLE_EMPLOYMENT;
import static com.lesjaw.wamonitoring.database.dbNode.TABLE_EMPLOYEE_STATUS;

/**
 * Created by lesjaw@gmail.com on 02/09/2017.
 */

public class dbNodeRepo {
    dbHelper dbHelper;

    public dbNodeRepo(Context context) {
        dbHelper = new dbHelper(context);
    }

    public int insertDivision(dbNode dbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_DIVISION_NAME, dbNode.getDivision_name());

        long Id = db.insert(TABLE_DIVISION, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public int insertEmployment(dbNode dbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EMPLOYMENT_NAME, dbNode.getEmployment_name());

        long Id = db.insert(TABLE_EMPLOYMENT, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public int insertEmploymentStatus(dbNode dbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EMPLOYEE_STATUS, dbNode.getEmployee_status());

        long Id = db.insert(TABLE_EMPLOYEE_STATUS, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }

    public int insertDailyTag(dbNode dbNode) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_EMPLOYEE_NAME, dbNode.getEmployee_name());
        values.put(KEY_DIVISION_NAME, dbNode.getDivision_name());
        values.put(KEY_EMPLOYEE_EMAIL, dbNode.getEmployee_email());
        values.put(KEY_TAG_JAM, dbNode.getTag_jam());
        values.put(KEY_TAG_TGL, dbNode.getTag_tgl());
        values.put(KEY_TAG_JARAK, dbNode.getTag_jarak());
        values.put(KEY_TAG_CHECK, dbNode.getTag_check());

        long Id = db.insert(TABLE_EMPLOYEE_STATUS, null, values);
        db.close(); // Closing database connection
        //Log.d("DEBUG", "insertNode: " + String.valueOf(KEY_NODE_ID));
        return (int) Id;
    }
}
