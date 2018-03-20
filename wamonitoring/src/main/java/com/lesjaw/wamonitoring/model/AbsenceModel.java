package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class AbsenceModel {
    private String employee_name,email,date_created,date_created_pulang,latitude,longitude,working_gps,log_status,phone1,phone2,range_loc;

    public AbsenceModel(String employee_name, String email,  String date_created, String date_created_pulang, String latitude,
                        String longitude, String working_gps, String log_status, String phone1, String phone2,String range_loc) {

        this.employee_name = employee_name;
        this.email = email;
        this.date_created = date_created;
        this.date_created_pulang = date_created_pulang;
        this.latitude = latitude;
        this.longitude = longitude;
        this.working_gps = working_gps;
        this.log_status = log_status;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.range_loc = range_loc;

    }


    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getWorking_gps() {
        return working_gps;
    }

    public void setWorking_gps(String working_gps) {
        this.working_gps = working_gps;
    }

    public String getLog_status() {
        return log_status;
    }

    public void setLog_status(String log_status) {
        this.log_status = log_status;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public String getDate_created_pulang() {
        return date_created_pulang;
    }

    public void setDate_created_pulang(String date_created_pulang) {
        this.date_created_pulang = date_created_pulang;
    }

    public String getRange_loc() {
        return range_loc;
    }

    public void setRange_loc(String range_loc) {
        this.range_loc = range_loc;
    }
}
