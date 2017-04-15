package com.dgarcia.project_firebase.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.text.format.DateFormat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.dgarcia.project_firebase.VolleySingleton;
import com.dgarcia.project_firebase.model.TestObject;
import com.dgarcia.project_firebase.view_logic.MainFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;


public class VolleyIntentService extends IntentService {

    //TODO-For testing
    //TODO- adapted from:https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";

    //Actions
    public static final String PARAM_ACTION_VOLLEY_TEST_CONNECTION = "volley_test_conn";
    public static final String PARAM_ACTION_VOLLEY_GET = "volley_get";
    public static final String PARAM_ACTION_VOLLEY_POST = "volley_post";
    public static final String PARAM_ACTION_VOLLEY_DELETE = "volley_delete";
    public static final String PARAM_ACTION_SQLite_DELETE_LOCAL = "sql_delete_local";

    final String dfString = "MM/dd/yy  hh:mm:ss a";  // date format string
    final android.text.format.DateFormat dateFormat = new DateFormat();

    private TestObjectSvcSQLiteImpl mTestObjectSvcSQLite; // For local cache

    private final String mUrlConnection = "https://regis-project.firebaseio.com/regis-project/";
    private final String mUrlForMethods = "https://regis-project.firebaseio.com/Volley/TestObjects.json";

    RequestQueue mRequestQueue; // Volley queue


    public VolleyIntentService() {
        super("VolleyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mTestObjectSvcSQLite = new TestObjectSvcSQLiteImpl(this.getApplicationContext());   // get a database
        mRequestQueue = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue(); //Get volley request queue
        String actionString = intent.getAction();

        if (actionString.equals(PARAM_ACTION_VOLLEY_TEST_CONNECTION)){
            CheckConnection();
        }
        else if(actionString.equals((PARAM_ACTION_VOLLEY_GET))){
            Get();
        }
        else if(actionString.equals(PARAM_ACTION_VOLLEY_POST)){
            Post();
        }
        else if(actionString.equals(PARAM_ACTION_VOLLEY_DELETE)){
            Delete();
        }
        else if(actionString.equals(PARAM_ACTION_SQLite_DELETE_LOCAL)){
            DeleteLocalCache();
        }
        else SendBroadcastString("<- ERROR: No service for action (" + actionString + ")");

        //Gets data from the incoming Intent
//        String msg = intent.getStringExtra(PARAM_IN_MSG);
    }


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


    /** CheckConnection()
     * Checks the connection by seeing if we get a string web page response
     */
    public void CheckConnection(){

        // Request a string response from url
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, mUrlConnection,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SendBroadcastString("<- Connected to regis-project.firebaseio.com/regis-project");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SendBroadcastString("<- ERROR:" + error.toString());
            }
        });
        mRequestQueue.add(stringRequest);
    }// END OF volleyCheckConnection()


    /** Get()
     *
     */
    public void Get() {

        //Check if local has data
        if (mTestObjectSvcSQLite.getNumOfRows() >= 1) {
            List<TestObject> testObjects = mTestObjectSvcSQLite.retrieveAllTestObjects();

            for (TestObject testObject : testObjects) {
                SystemClock.sleep(100); // delay for smooth scrolling RecyclerView
                SendBroadcastString("<- (LOCAL) Get:\n<-  ID:" + testObject.getId() + " DATE:" + testObject.getDate());
            }
        }
        else { //Not stored locally check server

            // Request a string response from url
            final JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, mUrlForMethods, null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            jsonParser(response); //Helper method
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    if (error.getMessage().contains("null") || error.getMessage().contains("NULL"))
                        SendBroadcastString("<- No objects. Database is empty.");
                    else
                        SendBroadcastString("<- ERROR:" + error.toString());
                }
            });
            mRequestQueue.add(stringRequest);
        }
    } //END OF volleyGetJson()

    /** jsonParser() Helper Method
     *
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

                mTestObjectSvcSQLite.create(testObject);    // add to local cache

                SendBroadcastString("<- (WEB) Get:\n<- ID:" + testObject.getId() + " DATE:" + testObject.getDate());
            }
        }catch (Exception e){
            SendBroadcastString("<- Exception:" + e.getMessage());
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
        else {

            try {

                final JSONObject jsonAttributes = new JSONObject();   // holds data and id
                jsonAttributes.put("id", testObject.getId());
                jsonAttributes.put("date", testObject.getDate());
                final String testObjStr = "ID:"+testObject.getId() + " DATE:" + testObject.getDate();

                //Send get request
                final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, mUrlForMethods, jsonAttributes,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                SendBroadcastString("<- Posted:\n<- " + testObjStr);
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        SendBroadcastString("<- (WEB) ERROR:" + error.getMessage());
                    }
                });

                mRequestQueue.add(request);
            } catch (Exception e) {
                SendBroadcastString("<- Exception:" + e.getMessage());
            }
        }
    } //END OF Post()


    /** delete()
     *
     */
    public void Delete(){
        int numOfRows = mTestObjectSvcSQLite.deleteAll();

        if(numOfRows == 0) SendBroadcastString("<- (LOCAL) Delete complete!");
        else SendBroadcastString("ERROR: LOCAL delete error!");

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, mUrlForMethods, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SendBroadcastString("<- (WEB) Delete complete");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.getMessage().contains("null") || error.getMessage().contains("NULL"))
                    SendBroadcastString("<- (WEB) Delete complete!");
                else
                    SendBroadcastString("ERROR:" + error.getMessage());
            }});
        mRequestQueue.add(request);
    }


    // Used to delete everything on start and stop. For testing.
    public void DeleteLocalCache(){
        mTestObjectSvcSQLite.deleteAll();
    }

}
