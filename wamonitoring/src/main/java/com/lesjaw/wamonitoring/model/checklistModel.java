package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 16/09/2017.
 */

public class checklistModel {
    private String tag_id, checklist, note, ket;
    private boolean cklistBool;


    public checklistModel(String tag_id, String checklist, String note) {
        this.tag_id = tag_id;
        this.checklist = checklist;
        this.note = note;

    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public boolean getCklistBool() {
        return cklistBool;
    }

    public void setCklistBool(boolean cklistBool) {
        this.cklistBool = cklistBool;
    }

    public String getKet() {
        return ket;
    }

    public void setKet(String ket) {
        this.ket = ket;
    }
}
