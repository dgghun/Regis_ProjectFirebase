package com.dgarcia.project_firebase;


import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.dgarcia.project_firebase.model.TestObject;
import com.dgarcia.project_firebase.services.MyIntentService;
import com.dgarcia.project_firebase.services.VolleySingleton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainFragment extends Fragment{

    private Button mPostButton;
    private Button mGetButton;
    private TestObject testObject;
    private DatabaseReference fireBaseRef;
    private DatabaseReference connectedRef;
    private ValueEventListener mConnectedListener;
    private ValueEventListener mPostListener;
    private ChildEventListener mChildListener;
    private static int count = 0;
    private View view;
    private final String ROOT = "TestObjects", ROOT2 = "MyObjects";
    private final String IN = "<- ";
    private final String OUT = "-> ";
    private final String ERROR_IN = "<- ERROR ";

    //Recycler View variables
    private List<String> mStringList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private StringAdapter mStringAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Volley variables
    RequestQueue mRequestQueue;
    private final String urlForMethods = "https://regis-project.firebaseio.com/Volley/TestObjects.json";


    //Date format variables
    final String dfString = "MM/dd/yy  hh:mm:ss a";  // date format string
    final android.text.format.DateFormat dateFormat = new DateFormat();

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstances){

        view = inflater.inflate(R.layout.fragment_main, container, false);

        setUpRecyclerListener();
        setUpButtons();
        hideButtons(true);

        mRequestQueue = VolleySingleton.getInstance(view.getContext().getApplicationContext()).getRequestQueue(); //Get volley request queue
        volleyCheckConnection("https://regis-project.firebaseio.com/regis-project/"); // test connection, if good unhide buttons
        delete(urlForMethods);

        return view;
    } // END OF onCreate()


    //******************
    //*** METHODS ******
    //******************

    /**
     * LAUNCH SERVICE TEST*****************************************************************************************
     * https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
     */
    private void launchService(){
        String stringInputMsg = "Im a string message";
        Intent msgIntent = new Intent(view.getContext(), MyIntentService.class);
        msgIntent.putExtra(MyIntentService.PARAM_IN_MSG, stringInputMsg);
        view.getContext().startService(msgIntent);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        public static final String ACTION_RESPONSE = "com.dgarcia.project_firebase.intent.action.MESSAGE_PROCESSED";

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, intent.toString(), Toast.LENGTH_SHORT).show();
        }
    }
    //***************************************************************************************************************



    /** hideButtons()
     * Sets button visibility
     * @param hide
     */
    private void hideButtons(Boolean hide){
        if(hide) {
            mPostButton.setVisibility(View.INVISIBLE);
            mGetButton.setVisibility(View.INVISIBLE);
        }
        else {
            mPostButton.setVisibility(View.VISIBLE);
            mGetButton.setVisibility(View.VISIBLE);
        }
    }


    /** setUpButtons()
     * Sets up buttons and listeners
     */
    private void setUpButtons(){
        //Set up POST button
        mPostButton = (Button)view.findViewById(R.id.button_POST);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                testObject = new TestObject(count, dateFormat.format(dfString, new Date()).toString()); //Create new object
                volleyPost(urlForMethods, testObject);
                //fireBaseRef.child("Object " + Integer.toString(testObject.getId())).setValue(testObject); //Add Object via Firebase Android API

            }// END OF onClick()
        }); // END OF setonClickListener()

        //Set up GET button
        mGetButton = (Button)view.findViewById(R.id.button_GET);
        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                volleyGetJson(urlForMethods); // Get object json string
            }
        });
    }


    /** setUpRecyclerListener()
     * Sets up and starts Recycler Listener
     */
    private void setUpRecyclerListener(){
        //RECYCLER VIEW setup
        mRecyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_outputWindow);    //Get handle on recycler view
        mStringAdapter = new StringAdapter(mStringList);    //add string list to custom adapter
        mLayoutManager = new LinearLayoutManager(view.getContext()); // get new layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mStringAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(view.getContext(), new RecyclerTouchListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String s = mStringList.get(position);
                        Toast.makeText(view.getContext(),"Clicked " + s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }


    /** updateRecyclerView()
     * Simple update to Recycler view
     * @param s
     */
    private void updateRecyclerView(String s){
        mStringList.add(s);
        mStringAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(mStringAdapter.getItemCount() - 1);
    }


    /** volleyCheckConnection()
     * Checks the connection by seeing if we get a string web page response
     * @param url
     */
    public void volleyCheckConnection(String url){

        updateRecyclerView(OUT + "Checking connection to:\n" + "    " + url);

        // Request a string response from url
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        updateRecyclerView(IN + "Connection good!");
                        hideButtons(false);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateRecyclerView(ERROR_IN + error.toString());
            }
        });
        mRequestQueue.add(stringRequest);
    }// END OF volleyCheckConnection()


    /** volleyGetJson()
     * GETs a json string from db
     * @param url
     */
    public void volleyGetJson(String url){

        updateRecyclerView(OUT + "Sending GET JSON OBJECT request...");

        // Request a string response from url
        final JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        jsonParser(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateRecyclerView(ERROR_IN + error.toString());
            }
        });
        mRequestQueue.add(stringRequest);

    } //END OF volleyGetJson()

    /** jsonParser()
     * Parses a json string to UI
     * @param jsonStr
     */
    public void jsonParser(JSONObject jsonStr) {

        try {
            JSONArray jsonArray = new JSONArray(jsonStr.names().toString()); // Get all Object names
            int length = jsonStr.length();

            for(int i =0; i < length; i++){
                String jsonObjName = jsonArray.getString(i); // get first object name
                JSONObject jsonObject = jsonStr.getJSONObject(jsonObjName); //get jsonObject from jsonStr by name
                TestObject testObject = new TestObject(Integer.parseInt(jsonObject.getString("id")), jsonObject.getString("date"));
                updateRecyclerView(IN + "(GET) " + jsonObjName +  ", ID: " + testObject.getId() + "\n     DATE: " + testObject.getDate());
            }
        }catch (Exception e){
            updateRecyclerView("Exception:" + e.getMessage());
        }
    }

    /** volleyPost()
     * Posts a test object
     * @param url
     */
    public void volleyPost(String url, final TestObject testObject){
        updateRecyclerView(OUT + "Sending POST request...");

        try{
            final JSONObject jsonAttributes = new JSONObject();   // holds data and id
            jsonAttributes.put("id", testObject.getId());
            jsonAttributes.put("date", testObject.getDate());

            //Send get request
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonAttributes,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            updateRecyclerView(IN + "(POST) ID:"+ testObject.getId() + ", DATE:" + testObject.getDate() +" successful!");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    updateRecyclerView(ERROR_IN + error);
                }
            });

            mRequestQueue.add(request);
        }catch (Exception e){
            updateRecyclerView("Exception:" + e.getMessage());
        }
    }

    /** delete()
     *
     * @param url
     */
    public void delete(String url){
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("DELETE", "Delete complete");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR: ", error.toString());
            }});
        mRequestQueue.add(request);
    }



/* TODO - DON'T USE BELOW YET. Android Firebase API stuff */
    //    @Override
//    public void onStart(){
//        super.onStart();
//
//         //Temp login for Firebase
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        mAuth.signInWithEmailAndPassword("dgghun@gmail.com", "david123456");
//
//        fireBaseRef = FirebaseDatabase.getInstance().getReference(ROOT2); //get firebase handle
//        fireBaseRef.getRef().removeValue(); //Clear data base
//
//        //Add connected listener
//        connectedRef = FirebaseDatabase.getInstance().getReference(".info/connected");
//        ValueEventListener connectedListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                boolean connected = dataSnapshot.getValue(Boolean.class);
//                if(connected){
//                    Toast.makeText(view.getContext(), "Connected to Firebase", Toast.LENGTH_SHORT).show();
//                }else {
//                    Toast.makeText(view.getContext(), "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        };
//
//
//        //Add value event listener
//        ValueEventListener postListener = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//
//                try {
//                    for(DataSnapshot dbObject : dataSnapshot.getChildren()){
//
//                        mOutputWindow.append(" <-------onDataChange (Name: " + dbObject.getKey() + ")");
//                        mOutputWindow.append(" (ID:" + dbObject.getValue(TestObject.class).getId() + ")");
//                        mOutputWindow.append(" (Date:" + dbObject.getValue(TestObject.class).getDate() + ")" + "\n");
//                        scrollDown(mOutputWindow, view);
//                    }
//                }catch (Exception e){
//                    Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e("ERROR", "loadPost:onCancelled", databaseError.toException());
//                Toast.makeText(view.getContext(), "Failed to load post.", Toast.LENGTH_SHORT).show();
//            }
//        }; //END OF ValueEventListener()
//
//
//        //Add child listener
//        ChildEventListener childListener = new ChildEventListener() {
//            @Override
//            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//                TestObject testObject = dataSnapshot.getValue(TestObject.class);
//                try {
//                    mOutputWindow.append("\n <- onChildAdded (ID:" + testObject.getId() + "-" + testObject.getDate() + ")\n");
//                    scrollDown(mOutputWindow, view);
//                }catch (Exception e){
//                    Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
//                TestObject testObject = dataSnapshot.getValue(TestObject.class);
//                try {
//                    mOutputWindow.append("\n <- onChildChanged (ID:" + testObject.getId() + "-" + testObject.getDate() + ")\n");
//                    scrollDown(mOutputWindow, view);
//                }catch (Exception e){
//                    Toast.makeText(view.getContext(), e.toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onChildRemoved(DataSnapshot dataSnapshot) {
//
//            }
//
//            @Override
//            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        }; //END OF ChildEventListener
//
//        fireBaseRef.addValueEventListener(postListener);
//        fireBaseRef.addChildEventListener(childListener);
//        connectedRef.addValueEventListener(connectedListener);
//
//        //Copy listeners to stop later on
//        mChildListener = childListener;
//        mPostListener = postListener;
//        mConnectedListener = connectedListener;
//
//    } //END OF onStart()
//
//
//    @Override
//    public void onStop(){
//        super.onStop();
//        if(mPostListener != null)
//            fireBaseRef.removeEventListener(mPostListener);
//
//        if(mChildListener != null)
//            fireBaseRef.removeEventListener(mChildListener);
//
//        if(mConnectedListener != null)
//            connectedRef.removeEventListener(mConnectedListener);
//
////        fireBaseRef.getRef().removeValue(); // remove values from db
//    } // END OF onStop()

}// END OF MainFragment()



