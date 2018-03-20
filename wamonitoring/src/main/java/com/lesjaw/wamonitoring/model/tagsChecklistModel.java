package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class tagsChecklistModel {
    private String tcrid, checklist, checklist_status, checklist_note, trid, tid, tagname, division, employee_name,
    email, tgl, jam,comment;
    int type;

    public tagsChecklistModel() {
    }

    public tagsChecklistModel(String tcrid, String checklist, String checklist_status, String checklist_note, String trid,
                              String tid, String tagname, String division, String employee_name, String email,
                              String tgl,String jam, int type, String comment) {
        this.tcrid = tcrid;
        this.checklist = checklist;
        this.checklist_status = checklist_status;
        this.checklist_note = checklist_note;
        this.trid = trid;
        this.tid = tid;
        this.tagname = tagname;
        this.division = division;
        this.employee_name = employee_name;
        this.type = type;
        this.email = email;
        this.tgl = tgl;
        this.jam = jam;
        this.comment = comment;


    }

    public String getTcrid() {
        return tcrid;
    }

    public void setTcrid(String tcrid) {
        this.tcrid = tcrid;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public String getChecklist_status() {
        return checklist_status;
    }

    public void setChecklist_status(String checklist_status) {
        this.checklist_status = checklist_status;
    }

    public String getChecklist_note() {
        return checklist_note;
    }

    public void setChecklist_note(String checklist_note) {
        this.checklist_note = checklist_note;
    }

    public String getTrid() {
        return trid;
    }

    public void setTrid(String trid) {
        this.trid = trid;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getTagname() {
        return tagname;
    }

    public void setTagname(String tagname) {
        this.tagname = tagname;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
