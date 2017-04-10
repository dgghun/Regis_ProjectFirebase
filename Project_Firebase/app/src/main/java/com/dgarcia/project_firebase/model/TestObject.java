package com.dgarcia.project_firebase.model;


import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@IgnoreExtraProperties // <-- Used for firebase
public class TestObject{

    private int id;
    private String Date;

    public TestObject(){

    }

    public TestObject(int id, String date){
        this.id = id;
        this.Date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    @Override
    public String toString() {
        return "TestObject{" +
                "id=" + id +
                ", mDate=" + Date +
                '}';
    }
}
