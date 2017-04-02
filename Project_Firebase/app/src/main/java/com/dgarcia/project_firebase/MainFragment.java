package com.dgarcia.project_firebase;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.support.v7.widget.DividerItemDecoration;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.dgarcia.project_firebase.model.TestObject;
import com.dgarcia.project_firebase.services.VolleySingleton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.spec.ECField;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class MainFragment extends Fragment{

    private Button mPostButton;
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

    //Volley variables
    RequestQueue mRequestQueue;
    JsonObjectRequest mJsonObjectRequest;
    JsonArrayRequest mJsonArrayRequest;
    StringRequest mStringRequest;
    private final int STRING = 0, JSON = 1, TEST = 2, JSONARRAY = 3;
    private static int intTAGS = 1;


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

        //RECYCLER VIEW setup
        mRecyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_outputWindow);    //Get handle on recycler view
        mStringAdapter = new StringAdapter(mStringList);    //add string list to custom adapter
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(view.getContext()); // get new layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mStringAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        startRecyclerListener();

        //Volley
        mRequestQueue = VolleySingleton.getInstance(view.getContext().getApplicationContext()).getRequestQueue(); //Get request queue
        volleyGetString("https://regis-project.firebaseio.com/regis-project/"); // test connection
//        volleyGetString("https://regis-project.firebaseio.com/MyObjects.json");
        volleyGetJson("https://regis-project.firebaseio.com/MyObjects.json");


        //Set up POST button
        mPostButton = (Button)view.findViewById(R.id.button_POST);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                count++;
                testObject = new TestObject(count, dateFormat.format(dfString, new Date()).toString()); //Create new object

                //fireBaseRef.child("Object " + Integer.toString(testObject.getId())).setValue(testObject); //Add Object via Firebase Android API


            }// END OF onClick()
        }); // END OF setonClickListener()


        return view;
    } // END OF onCreate()


    // TODO - DON'T USE BELOW YET. Android Firebase API stuff
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

    //TODO - Finish recycler view adapter
    //https://developer.android.com/samples/RecyclerView/index.html
    //https://developer.android.com/training/material/lists-cards.html


    private void startRecyclerListener(){

        mRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(view.getContext(), new RecyclerTouchListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String s = mStringList.get(position);
                        Toast.makeText(view.getContext(),"Clicked " + s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    private void updateRecyclerView(String s){
        mStringList.add(s);
        mStringAdapter.notifyDataSetChanged();
    }

    // VOLLEY GET
    public void volleyGetString(String url){

        updateRecyclerView(OUT + "Sending GET STRING request...");

        // Request a string response from url
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.contains("DOCTYPE")) updateRecyclerView(IN + "Connection ok!");
                        else jsonParser(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateRecyclerView(ERROR_IN + error.toString());
            }
        });
        mRequestQueue.add(stringRequest);
    }// END OF volleyGET()

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
    }

    public void jsonParser(JSONObject jsonObject) {

        Iterator<String> iterator = jsonObject.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();
            try {
                Object object = jsonObject.get(key);
                String s = jsonObject.names().toString();
                updateRecyclerView(s);

            }catch (Exception e){
                updateRecyclerView("Exception:" + e.getMessage());
            }
        }
    }

    public void jsonParser(String jsonString) {
        try {

            JSONArray jsonArray = new JSONArray(jsonString);
            final int length = jsonArray.length();

            for (int i = 0; i < length; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                updateRecyclerView(IN + object.getString("id") + " " + object.getString("date"));
                mStringAdapter.notifyDataSetChanged();
            }
        }catch (Exception e){
            updateRecyclerView("Exception:" + e.getMessage());
        }
    }
}// END OF MainFragment()



