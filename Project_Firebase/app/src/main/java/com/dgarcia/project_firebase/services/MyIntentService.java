package com.dgarcia.project_firebase.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import com.dgarcia.project_firebase.view_logic.MainFragment;

import java.util.Date;


public class MyIntentService extends IntentService {

    //TODO-For testing
    //TODO- adapted from:https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
    public static final String PARAM_IN_MSG = "imsg";
    public static final String PARAM_OUT_MSG = "omsg";
    public static final String PARAM_IN_TEST_CONNECTION = "test_conn";
    private String actionString;

    //gets class name in case I change the class name.
    private final String className = this.getClass().getSimpleName().toString();

    public MyIntentService() {
        super("MyIntentService"); // Put "className" here when you are done with testing
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        actionString = intent.getAction();
        String resultTxt;

        if (actionString.equals(PARAM_IN_TEST_CONNECTION)){
            resultTxt = "Check for connectivity";
        }
        else resultTxt = "Error: onHandleIntent()";

        //Gets data from the incoming Intent
//        String msg = intent.getStringExtra(PARAM_IN_MSG);


        //Do stuff
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(MainFragment.MyBroadcastReceiver.ACTION_RESPONSE);
        broadcastIntent.addCategory(Intent.CATEGORY_DEFAULT);
        broadcastIntent.putExtra(PARAM_OUT_MSG, resultTxt);
        sendBroadcast(broadcastIntent);
    }
}
