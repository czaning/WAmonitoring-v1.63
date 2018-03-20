package com.lesjaw.wamonitoring.database;

/**
 * Created by lesjaw@gmail.com on 02/09/2017.
 */

public class dbNode {

    public static final String TABLE_DIVISION = "table_division";
    public static final String TABLE_EMPLOYMENT = "table_employment";
    public static final String TABLE_EMPLOYEE_STATUS = "table_employment_status";
    public static final String TABLE_DAILY_LOG = "table_daily_log";

    public static final String KEY_ID = "id";
    public static final String KEY_DIVISION_NAME = "division_name";
    public static final String KEY_EMPLOYMENT_NAME = "employment_name";
    public static final String KEY_EMPLOYEE_NAME = "employee_name";
    public static final String KEY_EMPLOYEE_STATUS = "employee_status";
    public static final String KEY_EMPLOYEE_EMAIL = "employee_email";
    public static final String KEY_TAG_TGL = "tag_tgl";
    public static final String KEY_TAG_JAM = "tag_jam";
    public static final String KEY_TAG_NAME = "tag_name";
    public static final String KEY_TAG_CHECK = "tag_check";
    public static final String KEY_TAG_JARAK = "tag_jarak";


    public String division_name, employment_name, employee_status,employee_name, employee_email,tag_tgl,tag_jam,tag_name,tag_check,tag_jarak ;

    public String getDivision_name() {
        return division_name;
    }

    public void setDivision_name(String division_name) {
        this.division_name = division_name;
    }

    public String getEmployment_name() {
        return employment_name;
    }

    public void setEmployment_name(String employment_name) {
        this.employment_name = employment_name;
    }

    public String getEmployee_status() {
        return employee_status;
    }

    public void setEmployee_status(String employment_status) {
        this.employee_status = employment_status;
    }

    public String getEmployee_email() {
        return employee_email;
    }

    public void setEmployee_email(String employee_email) {
        this.employee_email = employee_email;
    }

    public String getTag_tgl() {
        return tag_tgl;
    }

    public void setTag_tgl(String tag_tgl) {
        this.tag_tgl = tag_tgl;
    }

    public String getTag_jam() {
        return tag_jam;
    }

    public void setTag_jam(String tag_jam) {
        this.tag_jam = tag_jam;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getTag_check() {
        return tag_check;
    }

    public void setTag_check(String tag_check) {
        this.tag_check = tag_check;
    }

    public String getTag_jarak() {
        return tag_jarak;
    }

    public void setTag_jarak(String tag_jarak) {
        this.tag_jarak = tag_jarak;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }
}
