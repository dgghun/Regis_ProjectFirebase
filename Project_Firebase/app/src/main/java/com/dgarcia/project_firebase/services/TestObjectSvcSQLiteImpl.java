package com.dgarcia.project_firebase.services;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.dgarcia.project_firebase.model.TestObject;

import junit.framework.Test;

import java.util.ArrayList;
import java.util.List;


public class TestObjectSvcSQLiteImpl extends SQLiteOpenHelper{

    private static final String DBNAME = "test_objects.db";
    private static final int DBVERSION = 1;
    private static final String TABLE_TESTOBJECTS = "test_objects";
//    private SQLiteDatabase db;
    private String createTestObjectTable = "create table " + TABLE_TESTOBJECTS + " (id integer primary key autoincrement, date text not null)";

    public TestObjectSvcSQLiteImpl(Context context){
        super(context, DBNAME, null, DBVERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL(createTestObjectTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TESTOBJECTS);
        onCreate(db);
    }


    public TestObject create(TestObject testObject){
        SQLiteDatabase db = this.getWritableDatabase(); //get db object
        ContentValues values = new ContentValues();     // Create object to hold values to go into table
        values.put("date", testObject.getDate());       // add values

        //Insert row into db. Returns row ID of the new row,
        //or -1 if insert failed. Good idea to check returned value.
        long rowIdOfInsertedRecord =  db.insert(TABLE_TESTOBJECTS, null, values);
        db.close();

        if (rowIdOfInsertedRecord == -1) return null;       //insert failed
        else testObject.setId((int)rowIdOfInsertedRecord);  //insert id

        return testObject;
    }


    public TestObject create(TestObject testObject, int id){
        SQLiteDatabase db = this.getWritableDatabase(); //get db object
        ContentValues values = new ContentValues();     // Create object to hold values to go into table
        values.put("date", testObject.getDate());       // add values
        values.put("id", id);

        long rowIdOfInsertedRecord = db.insert(TABLE_TESTOBJECTS, null, values);
        db.close();

        if (rowIdOfInsertedRecord == -1) return null;       //insert failed
        else testObject.setId((int)rowIdOfInsertedRecord);  //insert id

        return testObject;
    }


    public TestObject update(TestObject testObject){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        ContentValues values = new ContentValues();

        //Add values for row update
        values.put("date", testObject.getDate());


        //Tell update how fo find the record to update by id
        int numOfRowsUpdated = sqLiteDatabase.update(TABLE_TESTOBJECTS, values, "id = ?", new String[]{String.valueOf(testObject.getId())});

        //check number of rows updated
        if(numOfRowsUpdated < 1) return null; //failed
        return testObject; //good
    }


    public List<TestObject> retrieveAllTestObjects(){
        List<TestObject> testObjects = new ArrayList<>();
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(TABLE_TESTOBJECTS, new String[]{"id","date"}, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()){
            TestObject testObject = new TestObject();
            testObject.setId(cursor.getInt(0));         //the num passed in is index of column
            testObject.setDate(cursor.getString(1));    //the num passed in is index of column
            testObjects.add(testObject);                //add object to list
            cursor.moveToNext();
        }
        cursor.close();

        return  testObjects;
    }


    public TestObject delete(TestObject testObject){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        int rowDeleted = sqLiteDatabase.delete(TABLE_TESTOBJECTS, "id = " + testObject.getId(), null);
        sqLiteDatabase.close();

        //Check if deleted anything
        if(rowDeleted == 0) return null;    //failed
        else return testObject; //good
    }


    public int deleteAll(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        try {
            sqLiteDatabase.delete(TABLE_TESTOBJECTS, null, null);
        }catch (Exception e){
            Log.e("deleteAll()", e.getMessage());
        }
        return getNumOfRows();
    }

    public int getNumOfRows(){
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();
        return (int) DatabaseUtils.queryNumEntries(sqLiteDatabase, TABLE_TESTOBJECTS);
    }

}
