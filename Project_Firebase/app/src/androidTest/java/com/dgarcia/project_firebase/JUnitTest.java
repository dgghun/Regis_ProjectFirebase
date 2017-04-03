package com.dgarcia.project_firebase;

import android.app.Application;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.SystemClock;
import android.test.ApplicationTestCase;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.dgarcia.project_firebase.model.TestObject;

import static org.junit.Assert.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.datatype.Duration;

import dalvik.annotation.TestTargetClass;

public class JUnitTest extends ApplicationTestCase<Application>{

    private RequestQueue mRequestQueue;
    private JsonObjectRequest mJsonObjectRequest;
    private JsonArrayRequest mJsonArrayRequest;
    private StringRequest mStringRequest;
    private String url = "https://regis-project.firebaseio.com/Volley/TestObjects.json";

    public JUnitTest(){
        super(Application.class);
    }

    @Test
    public void TEST_volleyPOST_GET(){


        Log.e("TEST", "STARTED");
    }

    public void volleyGetJson(String url){


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


    public void volleyPost(String url, TestObject testObject){
        try{
            JSONObject jsonAttributes = new JSONObject();   // holds data and id
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

    public void volleyCheckConnection(String url){
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
    }
}

