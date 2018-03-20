package com.lesjaw.wamonitoring.model;

/**
 * Created by lesjaw@gmail.com on 27/09/2017.
 */

public class EmployeeAndEmail {
    public String realname;
    public Object email;

    public EmployeeAndEmail(String realname, Object email) {
        this.email = email;
        this.realname = realname;
    }

    @Override
    public String toString() {
        return realname;
    }
}