package com.dgarcia.project_firebase;


import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.dgarcia.project_firebase.model.TestObject;

import org.json.JSONArray;
import org.json.JSONObject;

public class VolleySvcImpl {
    private RequestQueue mRequestQueue;
    private final String urlForMethods = "https://regis-project.firebaseio.com/Volley/TestObjects.json";

    public VolleySvcImpl(Context context){
        mRequestQueue = VolleySingleton.getInstance(context.getApplicationContext()).getRequestQueue(); //Get volley request queue

    }

    /** CheckConnection()
     * Checks the connection by seeing if we get a string web page response
     * @param url
     */
    public void CheckConnection(String url){

        // Request a string response from url
        final StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        mRequestQueue.add(stringRequest);
    }// END OF volleyCheckConnection()


    /** getJson()
     *
     * @param url
     */
    public void GetJson(String url){

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
            }
        }catch (Exception e){
        }
    }


    /** Post()
     *
     * @param url
     * @param testObject
     */
    public void Post(String url, final TestObject testObject){

        try{
            final JSONObject jsonAttributes = new JSONObject();   // holds data and id
            jsonAttributes.put("id", testObject.getId());
            jsonAttributes.put("date", testObject.getDate());

            //Send get request
            final JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonAttributes,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                }
            });

            mRequestQueue.add(request);
        }catch (Exception e){
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
}
