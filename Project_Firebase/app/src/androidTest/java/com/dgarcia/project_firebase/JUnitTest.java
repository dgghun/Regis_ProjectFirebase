package com.dgarcia.project_firebase;

import android.app.Application;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.SystemClock;
import android.test.ApplicationTestCase;
import android.util.Log;

import static org.junit.Assert.*;

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

    public JUnitTest(){
        super(Application.class);
    }

    @Test
    public void test(){
        assertEquals(true, true);
        Log.e("START", "Test started!");



        Log.e("FINISHED", "Test finished!");

    }
}

