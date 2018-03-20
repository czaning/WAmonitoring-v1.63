package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class tagsAlertModel {
    private String email, employee_name, company_id, division, date_created, notes, latitude, longitude,
            lokasi, phone1, phone2, panic_id,comment;
int type;
    public tagsAlertModel() {
    }

    public tagsAlertModel(String email, String employee_name, String company_id, String division, String date_created,
                          String notes, String latitude, String longitude, String lokasi, String phone1, String phone2,
                          int type, String panic_id, String comment) {
        this.email = email;
        this.employee_name = employee_name;
        this.company_id = company_id;
        this.division = division;
        this.date_created = date_created;
        this.notes = notes;
        this.latitude = latitude;
        this.longitude = longitude;
        this.lokasi = lokasi;
        this.phone1 = phone1;
        this.phone2 = phone2;
        this.type = type;
        this.panic_id = panic_id;
        this.comment = comment;


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

    public String getCompany_id() {
        return company_id;
    }

    public void setCompany_id(String company_id) {
        this.company_id = company_id;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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

    public String getLokasi() {
        return lokasi;
    }

    public void setLokasi(String lokasi) {
        this.lokasi = lokasi;
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPanic_id() {
        return panic_id;
    }

    public void setPanic_id(String panic_id) {
        this.panic_id = panic_id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}


