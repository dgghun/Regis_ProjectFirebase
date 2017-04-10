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

        assertEquals(true, false);
        assertEquals(true, true);

        Log.e("TEST", "STARTED");
    }

}

