package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class commentChecklistModel {
    private String comment,from_email,employee_name,date_update,tcrid;


    public commentChecklistModel(String comment, String from_email, String employee_name,String date_update, String tcrid) {
        this.comment = comment;
        this.from_email = from_email;
        this.employee_name = employee_name;
        this.date_update = date_update;
        this.tcrid = tcrid;

    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getFrom_email() {
        return from_email;
    }

    public void setFrom_email(String from_email) {
        this.from_email = from_email;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getDate_update() {
        return date_update;
    }

    public void setDate_update(String date_update) {
        this.date_update = date_update;
    }

    public String getTcrid() {
        return tcrid;
    }

    public void setTcrid(String tcrid) {
        this.tcrid = tcrid;
    }
}
