package com.dgarcia.project_firebase.services;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.dgarcia.project_firebase.StringAdapter;

import java.util.ArrayList;
import java.util.List;



public class VolleySvc {
    private RequestQueue mRequestQueue;
    private StringRequest mStringRequest;
    private Context mContext;
    private static List TAGS;
    private static int intTAGS = 1;
    private String responseStr;

    //Constructor
    public VolleySvc(Context applicationContext){
        this.mContext = applicationContext;
        this.mRequestQueue = VolleySingleton.getInstance(applicationContext.getApplicationContext()).getRequestQueue(); //Get a RequestQueue
        TAGS = new ArrayList();
    }

    public String GET(String url, List<String> stringList, StringAdapter stringAdapter){
        // Request a string response from the provided URL.
        mStringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                responseStr = response;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                responseStr = error.toString();
            }

        });

        mStringRequest.setTag(intTAGS); // Tag it for cancelling (using int for testing)
        intTAGS++;
        mRequestQueue.add(mStringRequest);

        return responseStr;
    }
}
