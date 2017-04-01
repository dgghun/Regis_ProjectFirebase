package com.dgarcia.project_firebase.services;

import android.util.Log;

import com.dgarcia.project_firebase.model.TestObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;



public class DataBaseSvcFirebaseImpl implements IDataBaseSvc {

    private DatabaseReference mFb_dataBaseRef;
    private DatabaseReference mFb_connectionRef;
    private ValueEventListener mDataChangeListener;
    private ValueEventListener mConnectionListener;
    private ChildEventListener mChildListener;

    private DataSnapshot mDataSnapShot;
    private TestObject mChildAddedObj;
    private TestObject mChildChangedObj;

    public DataBaseSvcFirebaseImpl(String dbRoot){

        //Temp login for Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("dgghun@gmail.com", "david123456");
        mFb_dataBaseRef = FirebaseDatabase.getInstance().getReference(dbRoot); //Get firebase handle
    }

    @Override
    public TestObject create(TestObject testObject) {

        mFb_dataBaseRef.child("Object " + Integer.toString(testObject.getId())).setValue(testObject); //Add Object
        return mChildAddedObj;
    }

    @Override
    public List<TestObject> retrieveAllTestObjects() {
        return null;
    }

    @Override
    public TestObject update(TestObject testObject) {
        return null;
    }

    @Override
    public TestObject delete(TestObject testObject) {
        return null;
    }

    public void deleteAll(){
        mFb_dataBaseRef.getRef().removeValue(); //clear DB TESTING
    }

    public void startListeners(){

        //Value event listener
        ValueEventListener dataChangeListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mDataSnapShot = dataSnapshot;
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DB_ERROR", databaseError.toString());
            }
        };

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                mChildAddedObj = dataSnapshot.getValue(TestObject.class);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                mChildChangedObj = dataSnapshot.getValue(TestObject.class);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        //attach listener
        mFb_dataBaseRef.addValueEventListener(dataChangeListener);
        mFb_dataBaseRef.addChildEventListener(childEventListener);

        //save listener
        mDataChangeListener = dataChangeListener;
        mChildListener = childEventListener;
    }

    public void stopListeners(){

        if(mDataChangeListener != null) {
            mFb_dataBaseRef.removeEventListener(mDataChangeListener);
            Log.e("TAG", "mDataChangeListener has stopped");
        }
        if(mChildListener != null){
            mFb_dataBaseRef.removeEventListener(mChildListener);
            Log.e("TAG", "mChildListener has stopped");
        }

    }
}
