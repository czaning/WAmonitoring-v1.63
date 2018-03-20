package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 27/09/2017.
 */

public class DivisionAndID {
    public String div_name;
    public Object div_id;

    public DivisionAndID(String div_name, Object div_id) {
        this.div_id = div_id;
        this.div_name = div_name;
    }

    public Object getId () {
        return div_id;
    }

    @Override
    public String toString() {
        return div_name;
    }


}