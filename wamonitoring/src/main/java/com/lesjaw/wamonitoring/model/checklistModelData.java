package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 16/09/2017.
 */

public class checklistModelData {
    private String tag_id, checklist, checklist_status, note;
    private boolean cklistBool;

    public checklistModelData() {
    }

    public checklistModelData(String tag_id, String checklist,String checklist_status, String note, boolean cklistBool) {
        this.tag_id = tag_id;
        this.checklist = checklist;
        this.checklist_status = checklist_status;
        this.note = note;
        this.cklistBool = cklistBool;

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

    public String getChecklist_status() {
        return checklist_status;
    }

    public void setChecklist_status(String checklist_status) {
        this.checklist_status = checklist_status;
    }
}
