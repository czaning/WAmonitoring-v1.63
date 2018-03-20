package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class locEmployeeLastTenModel {
    private String addstring,  timestamps, EmployeeName;
    private double longitude, latitude;


    public locEmployeeLastTenModel( double latitude, double longitude,String addstring, String timestamps, String EmployeeName) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamps = timestamps;
        this.addstring = addstring;
        this.EmployeeName = EmployeeName;

    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(String timestamps) {
        this.timestamps = timestamps;
    }

    public String getAddstring() {
        return addstring;
    }

    public void setAddstring(String addstring) {
        this.addstring = addstring;
    }

    public String getEmployeeName() {
        return EmployeeName;
    }

    public void setEmployeeName(String employeeName) {
        EmployeeName = employeeName;
    }
}
