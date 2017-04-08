package com.dgarcia.project_firebase.services;

import android.app.IntentService;
import android.content.Intent;
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


public class VolleyIntentService extends IntentService {

    //TODO-For testing
    //TODO- adapted from:https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";

    //Actions
    public static final String PARAM_IN_VOLLEY_TEST_CONNECTION = "volley_test_conn";
    public static final String PARAM_IN_VOLLEY_GET = "volley_get";
    public static final String PARAM_IN_VOLLEY_POST = "volley_post";
    public static final String PARAM_IN_VOLLEY_DELETE = "volley_delete";

    public static int mCount = 1;
    final String dfString = "MM/dd/yy  hh:mm:ss a";  // date format string
    final android.text.format.DateFormat dateFormat = new DateFormat();

    private final String mUrlConnection = "https://regis-project.firebaseio.com/regis-project/";
    private final String mUrlForMethods = "https://regis-project.firebaseio.com/Volley/TestObjects.json";

    RequestQueue mRequestQueue;


    public VolleyIntentService() {
        super("VolleyIntentService"); // Put "className" here when you are done with testing
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        mRequestQueue = VolleySingleton.getInstance(this.getApplicationContext()).getRequestQueue(); //Get volley request queue
        String actionString = intent.getAction();

        if (actionString.equals(PARAM_IN_VOLLEY_TEST_CONNECTION)){
            CheckConnection();
        }
        else if(actionString.equals((PARAM_IN_VOLLEY_GET))){
            GetJson();
        }
        else if(actionString.equals(PARAM_IN_VOLLEY_POST)){
            Post();
        }
        else if(actionString.equals(PARAM_IN_VOLLEY_DELETE)){
            Delete();
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


    /** getJson()
     *
     */
    public void GetJson(){

        // Request a string response from url
        final JsonObjectRequest stringRequest = new JsonObjectRequest(Request.Method.GET, mUrlForMethods, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        jsonParser(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                SendBroadcastString("<- ERROR:" + error.toString());
            }
        });
        mRequestQueue.add(stringRequest);
    } //END OF volleyGetJson()


    /** jsonParser()
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
                SendBroadcastString("<- ID:" + testObject.getId() + " DATE:" + testObject.getDate());
            }
        }catch (Exception e){
            SendBroadcastString("<- Exception:" + e.getMessage());
        }
    }


    /** Post()
     *
     */
    public void Post(){

        try{
            TestObject testObject = new TestObject(mCount, dateFormat.format(dfString, new Date()).toString()); //Create new object
            final JSONObject jsonAttributes = new JSONObject();   // holds data and id
            jsonAttributes.put("id", testObject.getId());
            jsonAttributes.put("date", testObject.getDate());

            //Send get request
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, mUrlForMethods, jsonAttributes,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            SendBroadcastString("<- Post Success!");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    SendBroadcastString("<- ERROR:" + error.getMessage());
                }
            });

            mRequestQueue.add(request);
        }catch (Exception e){
            SendBroadcastString("<- Exception:" + e.getMessage());
        }
        mCount++;
    }


    /** delete()
     *
     */
    public void Delete(){
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, mUrlForMethods, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        SendBroadcastString("<- Delete complete");
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                SendBroadcastString("<- ERROR: " + error.getMessage());
                SendBroadcastString("<- Delete complete!");
            }});
        mRequestQueue.add(request);
    }
}
