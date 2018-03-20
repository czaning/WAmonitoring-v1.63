package com.lesjaw.wamonitoring.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lesjaw@gmail.com on 02/09/2017.
 */

public class dbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "wamonotoring";

    public dbHelper(Context context ) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //All necessary tables you like to create will create here

        String CREATE_TABLE_DIVISION = "CREATE TABLE " + dbNode.TABLE_DIVISION  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_DIVISION_NAME + " TEXT )";


        String CREATE_TABLE_EMPLOYMENT = "CREATE TABLE " + dbNode.TABLE_EMPLOYMENT  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_EMPLOYMENT_NAME + " TEXT )";

        String CREATE_TABLE_EMPLOYEE_STATUS = "CREATE TABLE " + dbNode.TABLE_EMPLOYEE_STATUS  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_EMPLOYEE_STATUS + " TEXT ) ";

        String CREATE_TABLE_DAILY_LOG = "CREATE TABLE " + dbNode.TABLE_DAILY_LOG  + "("
                + dbNode.KEY_ID  + " INTEGER PRIMARY KEY AUTOINCREMENT ,"
                + dbNode.KEY_EMPLOYEE_NAME + " TEXT, "
                + dbNode.KEY_EMPLOYEE_EMAIL + " TEXT, "
                + dbNode.KEY_TAG_TGL + " TEXT, "
                + dbNode.KEY_TAG_JAM + " TEXT, "
                + dbNode.KEY_TAG_NAME + " TEXT, "
                + dbNode.KEY_TAG_CHECK + " TEXT, "
                + dbNode.KEY_TAG_JARAK + " TEXT, )";

        db.execSQL(CREATE_TABLE_DIVISION);
        db.execSQL(CREATE_TABLE_EMPLOYMENT);
        db.execSQL(CREATE_TABLE_EMPLOYEE_STATUS);
        db.execSQL(CREATE_TABLE_DAILY_LOG);

    }

    //FAVORITE = DASHBOARD
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed, all data will be gone!!!
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_DIVISION);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_EMPLOYMENT);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_EMPLOYEE_STATUS);
        db.execSQL("DROP TABLE IF EXISTS " + dbNode.TABLE_DAILY_LOG);

        // Create tables again
        onCreate(db);

    }
}