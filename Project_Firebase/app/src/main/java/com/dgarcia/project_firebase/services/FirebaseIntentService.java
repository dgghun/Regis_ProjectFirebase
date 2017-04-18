package com.dgarcia.project_firebase.services;


import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.DateFormat;

import com.dgarcia.project_firebase.model.TestObject;
import com.dgarcia.project_firebase.view_logic.MainFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

public class FirebaseIntentService extends IntentService{

    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";

    public static final String PARAM_ACTION_FIREBASE_GET = "firebase_get";
    public static final String PARAM_ACTION_FIRESBASE_POST= "firebase_post";
    public static final String PARAM_ACTION_FIRESBASE_DELETE= "firebase_delete";
    public static final String PARAM_ACTION_SQLite_DELETE_LOCAL= "sqlite_delete_local";
    public static final String PARAM_ACTION_FIREBASE_STOP= "firebase_stop";
    public static final String PARAM_ACTION_FIREBASE_START= "firebase_start";

    public static final String CONNECTED = "Connected";
    public static final String NOT_CONNECTED = "Not connected";

    final String dfString = "MM/dd/yy  hh:mm:ss a";  // date format string
    final android.text.format.DateFormat dateFormat = new DateFormat();

    private TestObjectSvcSQLiteImpl mTestObjectSvcSQLite; // For local cache

    //Firebase API variables
    private DatabaseReference fireBaseRef;
    private DatabaseReference connectedRef;
    private ValueEventListener mConnectedListener;
    private ValueEventListener mPostListener;
    private ChildEventListener mChildListener;
    private final String FIREBASE_ROOT_URL = "FirebaseSDK";
    private static Boolean firebaseStarted = false; // Flag indicating if firebase is already started

    // Super class
    public FirebaseIntentService(){
        super("FirebaseIntentService");
    }

    //Main
    @Override
    protected void onHandleIntent(Intent intent) {
        mTestObjectSvcSQLite = new TestObjectSvcSQLiteImpl(this.getApplicationContext());   // get a database
        String actionString = intent.getAction();
//        String msg = intent.getStringExtra(PARAM_IN_MSG); //Gets data from the incoming Intent

        if(actionString.equals(PARAM_ACTION_FIREBASE_START) && !firebaseStarted) {
                StartFirebase();
            firebaseStarted = true;
        }
        else if (actionString.equals(PARAM_ACTION_FIREBASE_STOP))
            StopFirebase();
        else if (actionString.equals(PARAM_ACTION_FIREBASE_GET))
            Get();
        else if (actionString.equals(PARAM_ACTION_FIRESBASE_POST))
            Post();
        else if (actionString.equals(PARAM_ACTION_FIRESBASE_DELETE))
            Delete();
        else if (actionString.equals(PARAM_ACTION_SQLite_DELETE_LOCAL))
            DeleteLocalCache();


    } // END OF onHandledIntent()



    /** SendBroadcastString()
     *
     * @param msg
     */
    public void SendBroadcastString(String msg){
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainFragment.MyBroadcastReceiver.ACTION_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, msg);
        sendBroadcast(broadcastIntent);

    }



    /** Get()
     *
     */
    public void Get(){

        //Check if local has data
        if (mTestObjectSvcSQLite.getNumOfRows() >= 1) {
            List<TestObject> testObjects = mTestObjectSvcSQLite.retrieveAllTestObjects();

            for (TestObject testObject : testObjects) {
                SystemClock.sleep(100); // delay for smooth scrolling RecyclerView
                SendBroadcastString("<- (LOCAL) Get:\n<-  ID:" + testObject.getId() + " DATE:" + testObject.getDate());
            }
        }else { // Not stored locally
            SendBroadcastString("<- (LOCAL) Not cached data.");
            //TODO
        }


    }



    /** Post()
     *
     */
    public void Post(){
        TestObject testObject = new TestObject(); //Create new object
        testObject.setDate(dateFormat.format(dfString, new Date()).toString()); //set date
        testObject = mTestObjectSvcSQLite.create(testObject);   // add to local cache

        if(testObject == null)  // if failed to cache
            SendBroadcastString("<- ERROR: couldn't cache locally. Post aborted.");
        else{ // post to firebase
            try {
                fireBaseRef = FirebaseDatabase.getInstance().getReference(FIREBASE_ROOT_URL);
                fireBaseRef.child("TestObject " + Integer.toString(testObject.getId())).setValue(testObject); //Add Object via Firebase Android API
            }catch (Exception e){
                SendBroadcastString("<- Exception: " + e.getMessage());
            }
        }
    }



    /** Delete()
     *
     */
    public void Delete(){
        int numOfRows = mTestObjectSvcSQLite.deleteAll();

        if(numOfRows == 0) SendBroadcastString("<- (LOCAL) Delete complete!");
        else SendBroadcastString("ERROR: LOCAL delete error!");

        fireBaseRef = FirebaseDatabase.getInstance().getReference(FIREBASE_ROOT_URL);
        fireBaseRef.getRef().removeValue(); // remove values from db

    }



    // Used to delete everything on start and stop. For testing.
    public void DeleteLocalCache(){
        mTestObjectSvcSQLite.deleteAll();
    }



    public void StartFirebase(){

        //Temp login for Firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword("dgghun@gmail.com", "david123456");

        fireBaseRef = FirebaseDatabase.getInstance().getReference(FIREBASE_ROOT_URL);


        //Add connected listener
        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
        ValueEventListener connectedListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = dataSnapshot.getValue(Boolean.class);
                if(connected){
                    SendBroadcastString("<- "+ CONNECTED + " to Firebase!");

                }else {
                    SendBroadcastString("<- ERROR: " + NOT_CONNECTED + " to Firebase");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        };


        //Add value event listener
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                try {
                    for(DataSnapshot dbObject : dataSnapshot.getChildren()){
                        TestObject testObject = new TestObject();

                        testObject.setId(dbObject.getValue(TestObject.class).getId());
                        testObject.setDate(dbObject.getValue(TestObject.class).getDate());
//                        SendBroadcastString("<- postListener: " + testObject.getId() + " " + testObject.getDate());

                        if(mTestObjectSvcSQLite.update(testObject) == null) // update local cache
                            mTestObjectSvcSQLite.create(testObject, testObject.getId()); // if not cached, cache it at position of getId()

                    }
                }catch (Exception e){
                    SendBroadcastString("<- Exception: " + e.getMessage());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                SendBroadcastString("<- ERROR: " + databaseError.getMessage());
            }
        }; //END OF ValueEventListener()


        //Add child listener
        ChildEventListener childListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                TestObject testObject = dataSnapshot.getValue(TestObject.class);
                try {
                    SendBroadcastString("<- (WEB) Posted:\n<-  ID:" + testObject.getId() + " DATE:" + testObject.getDate());
                }catch (Exception e){
                    SendBroadcastString("<- Exception: " + e.getMessage());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                TestObject testObject = dataSnapshot.getValue(TestObject.class);
                try {
                    SendBroadcastString("<- (WEB) Updated:\n<-  ID:" + testObject.getId() + " DATE:" + testObject.getDate());
                }catch (Exception e){
//                    SendBroadcastString("<- Exception: " + e.getMessage());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                TestObject testObject = dataSnapshot.getValue(TestObject.class);
                SendBroadcastString("<- (WEB) Deleted:\n<-  ID:" + testObject.getId() + " DATE:" + testObject.getDate());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}

        }; //END OF ChildEventListener

        fireBaseRef.addValueEventListener(postListener);
        fireBaseRef.addChildEventListener(childListener);
        connectedRef.addValueEventListener(connectedListener);

        //Copy listeners to stop later on
        mChildListener = childListener;
        mPostListener = postListener;
        mConnectedListener = connectedListener;

    } // END OF StartFirebase()


    public void StopFirebase(){
        if(mPostListener != null)
            fireBaseRef.removeEventListener(mPostListener);

        if(mChildListener != null)
            fireBaseRef.removeEventListener(mChildListener);

        if(mConnectedListener != null)
            connectedRef.removeEventListener(mConnectedListener);


    } // END OF StopFireabase()
}
