package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 06/09/2017.
 */

public class tagsModel {
    private String tag_id, tag_name, tag_location, tag_timer, tag_type, tag_division_name;

    public tagsModel() {
    }

    public tagsModel(String tag_id, String tag_name, String tag_location, String tag_division_name, String tag_timer,
                     String tag_type) {
        this.tag_id = tag_id;
        this.tag_name = tag_name;
        this.tag_location = tag_location;
        this.tag_timer = tag_timer;
        this.tag_type = tag_type;
        this.tag_division_name = tag_division_name;

    }

    public String getTag_id() {
        return tag_id;
    }

    public void setTag_id(String tag_id) {
        this.tag_id = tag_id;
    }

    public String getTag_name() {
        return tag_name;
    }

    public void setTag_name(String tag_name) {
        this.tag_name = tag_name;
    }

    public String getTag_location() {
        return tag_location;
    }

    public void setTag_location(String tag_location) {
        this.tag_location = tag_location;
    }

    public String getTag_timer() {
        return tag_timer;
    }

    public void setTag_timer(String tag_timer) {
        this.tag_timer = tag_timer;
    }

    public String getTag_type() {
        return tag_type;
    }

    public void setTag_type(String tag_type) {
        this.tag_type = tag_type;
    }

    public String getTag_division_name() {
        return tag_division_name;
    }

    public void setTag_division_name(String tag_division_name) {
        this.tag_division_name = tag_division_name;
    }
}
