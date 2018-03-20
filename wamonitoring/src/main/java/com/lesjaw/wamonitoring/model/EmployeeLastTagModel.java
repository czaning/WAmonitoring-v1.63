package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class EmployeeLastTagModel {
    private String counttags,employee_name,tagname,range_loc,division_name, tgl,jam,tid,
            trid, email, last_update, level_user;

    public EmployeeLastTagModel() {
    }

    public EmployeeLastTagModel(String counttags, String employee_name, String tagname, String range_loc, String division_name,
                                String tgl, String jam, String tid, String trid,
                                String email, String last_update, String level_user) {
        this.counttags = counttags;
        this.employee_name = employee_name;
        this.tagname = tagname;
        this.range_loc = range_loc;
        this.division_name = division_name;
        this.tgl = tgl;
        this.jam = jam;
        this.tid = tid;
        this.trid = trid;
        this.email = email;
        this.last_update = last_update;
        this.level_user = level_user;

    }

    public String getCounttags() {
        return counttags;
    }

    public void setCounttags(String counttags) {
        this.counttags = counttags;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getTagname() {
        return tagname;
    }

    public void setTagname(String tagname) {
        this.tagname = tagname;
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

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    public String getLevel_user() {
        return level_user;
    }

    public void setLevel_user(String level_user) {
        this.level_user = level_user;
    }
}
