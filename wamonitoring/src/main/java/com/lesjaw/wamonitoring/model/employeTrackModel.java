package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class employeTrackModel {
    private String email, employee_name, latitude, longitude, company_id;

    public employeTrackModel() {
    }

    public employeTrackModel(String email, String employee_name, String latitude, String longitude, String company_id) {
        this.email = email;
        this.employee_name = employee_name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.company_id = company_id;

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
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

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }
}
