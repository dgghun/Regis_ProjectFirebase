package com.dgarcia.project_firebase.services;

import android.app.IntentService;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.Nullable;

import java.util.Date;


public class MyIntentService extends IntentService {

    //TODO-For testing
    //TODO- adapted from:https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";

    //gets class name in case I change the class name.
    private final String className = this.getClass().getSimpleName().toString();

    public MyIntentService() {
        super("MyIntentService"); // Put className here when you are done with testing
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Gets data from the incoming Intent
        String msg = intent.getStringExtra(PARAM_IN_MSG);
        SystemClock.sleep(5000); //sleep
        String resultTxt = msg + " " + new Date().toString();

        //Do stuff
    }
}
