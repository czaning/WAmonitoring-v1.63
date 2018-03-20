package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class tagsDailyModel {
    private String employee_name, tag_name, range_loc, division_name, div_id, tgl, jam, tid, checklist_done,
            trid, email, after_photo, before_photo, latitude, longitude;

    public tagsDailyModel() {
    }

    public tagsDailyModel(String employee_name, String tag_name, String range_loc, String division_name, String div_id, String tgl,
                          String jam, String tid, String checklist_done,String trid, String email, String after_photo,
                          String before_photo, String latitude, String longitude) {
        this.employee_name = employee_name;
        this.tag_name = tag_name;
        this.range_loc = range_loc;
        this.division_name = division_name;
        this.tgl = tgl;
        this.jam = jam;
        this.tid = tid;
        this.checklist_done = checklist_done;
        this.trid = trid;
        this.email = email;
        this.after_photo = after_photo;
        this.before_photo = before_photo;
        this.div_id = div_id;
        this.latitude = latitude;
        this.longitude = longitude;

    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getRange_loc() {
        return range_loc;
    }

    public void setRange_loc(String range_loc) {
        this.range_loc = range_loc;
    }

    public String getDivision_name() {
        return division_name;
    }

    public void setDivision_name(String division_name) {
        this.division_name = division_name;
    }

    public String getTgl() {
        return tgl;
    }

    public void setTgl(String tgl) {
        this.tgl = tgl;
    }

    public String getJam() {
        return jam;
    }

    public void setJam(String jam) {
        this.jam = jam;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getChecklist_done() {
        return checklist_done;
    }

    public void setChecklist_done(String checklist_done) {
        this.checklist_done = checklist_done;
    }

    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
        this.trid = trid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAfter_photo() {
        return after_photo;
    }

    public void setAfter_photo(String after_photo) {
        this.after_photo = after_photo;
    }

    public String getBefore_photo() {
        return before_photo;
    }

    public void setBefore_photo(String before_photo) {
        this.before_photo = before_photo;
    }

    public String getDiv_id() {
        return div_id;
    }

    public void setDiv_id(String div_id) {
        this.div_id = div_id;
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
}
