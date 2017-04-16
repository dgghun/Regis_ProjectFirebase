package com.dgarcia.project_firebase.view_logic;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

import com.dgarcia.project_firebase.R;
import com.dgarcia.project_firebase.RecyclerTouchListener;
import com.dgarcia.project_firebase.StringAdapter;
import com.dgarcia.project_firebase.model.TestObject;
import com.dgarcia.project_firebase.services.FirebaseIntentService;
import com.dgarcia.project_firebase.services.VolleyIntentService;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment{

    private Button mPostButton;
    private Button mGetButton;
    private Button mDeleteButton;
    private TestObject testObject;
    private View view;

    //Firebase API variables
    private DatabaseReference fireBaseRef;
    private DatabaseReference connectedRef;
    private ValueEventListener mConnectedListener;
    private ValueEventListener mPostListener;
    private ChildEventListener mChildListener;

    //Recycler View variables
    private List<String> mStringList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private StringAdapter mStringAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Volley variable
    private final String urlForMethods = "https://regis-project.firebaseio.com/Volley/TestObjects.json";

    //Broadcast Receiver variable
    private MyBroadcastReceiver receiver;

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

        //Register broadcast receiver
        IntentFilter intentFilter = new IntentFilter(MyBroadcastReceiver.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyBroadcastReceiver();
        view.getContext().registerReceiver(receiver, intentFilter);

        setUpRecyclerListener();
        setUpButtons();

        launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIREBASE_START); // Start Firebase

        return view;
    } // END OF onCreate()


    /**
     * Broadcast Receiver CLASS
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {

        public static final String ACTION_RESPONSE = "com.dgarcia.project_firebase.intent.action.MESSAGE_PROCESSE";

        @Override
        public void onReceive(Context context, Intent intent) {
//            String intentStr =intent.getStringExtra(VolleyIntentService.PARAM_OUT_MSG); //get the string from the VolleyIntentService

            //Firebase intent message
            String intentStr =intent.getStringExtra(FirebaseIntentService.PARAM_OUT_MSG); //get the string from the FirebaseIntentService
            if(intentStr.contains(FirebaseIntentService.CONNECTED)) hideButtons(false);
            else if (intentStr.contains(FirebaseIntentService.NOT_CONNECTED)) hideButtons(true);

            updateRecyclerView(intentStr);
        }
    }


    //*** METHODS ****************************************************************************************************************************

    /** launchVolleyService()
     *
     * https://code.tutsplus.com/tutorials/android-fundamentals-intentservice-basics--mobile-6183
     */
    private void launchVolleyService(String action){
        String stringInputMsg = "Im a string message from launchVolleyService()";
        Intent msgIntent = new Intent(view.getContext(), VolleyIntentService.class); //msg for service intent

        msgIntent.putExtra(VolleyIntentService.PARAM_IN_MSG, stringInputMsg);   // Put string extra. Note: not being used at the moment.

        msgIntent.setAction(action);    //The service action to perform
        view.getContext().startService(msgIntent); //start service that will return info to broadcast receiver
    }//END OF launchVolleyService()



    private void launchFirebaseService(String action){
        String stringInputMsg = "Im a string message from launchFirebaseService()";
        Intent msgIntent = new Intent(view.getContext(), FirebaseIntentService.class); //msg for service intent

        msgIntent.putExtra(FirebaseIntentService.PARAM_IN_MSG, stringInputMsg);   // Put string extra. Note: not being used at the moment.

        msgIntent.setAction(action);    //The service action to perform
        view.getContext().startService(msgIntent); //start service that will return info to broadcast receiver
    }//END OF LaunchFirebaseService()


    /** hideButtons()
     * Sets button visibility
     * @param hide
     */
    private void hideButtons(Boolean hide){
        if(hide) {
            mPostButton.setVisibility(View.INVISIBLE);
            mGetButton.setVisibility(View.INVISIBLE);
            mDeleteButton.setVisibility(View.INVISIBLE);
        }
        else {
            mPostButton.setVisibility(View.VISIBLE);
            mGetButton.setVisibility(View.VISIBLE);
            mDeleteButton.setVisibility(View.VISIBLE);
        }
    }//END OF hideButtons()


    /** setUpButtons()
     * Sets up buttons and listeners
     */
    private void setUpButtons(){

        //SET UP POST BUTTON
        mPostButton = (Button)view.findViewById(R.id.button_POST);
        mPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecyclerView("-> Posting...");
//                launchVolleyService(VolleyIntentService.PARAM_ACTION_VOLLEY_POST);
                launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIRESBASE_POST);
            }// END OF onClick()
        }); // END OF setonClickListener()


        //SET UP GET BUTTON
        mGetButton = (Button)view.findViewById(R.id.button_GET);
        mGetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecyclerView("-> Getting...");
//                launchVolleyService(VolleyIntentService.PARAM_ACTION_VOLLEY_GET);
                launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIREBASE_GET);
            }
        });


        //SET UP DELETE BUTTON
        mDeleteButton = (Button)view.findViewById(R.id.button_DELETE);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateRecyclerView("-> Deleting entries");
//                launchVolleyService(VolleyIntentService.PARAM_ACTION_VOLLEY_DELETE);
                launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIRESBASE_DELETE);
            }
        });
    }// END OF setUpButtons()


    /** setUpRecyclerListener()
     * Sets up and starts Recycler Listener
     */
    private void setUpRecyclerListener(){
        //RECYCLER VIEW setup
        mRecyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_outputWindow);    //Get handle on recycler view
        mStringAdapter = new StringAdapter(mStringList);    //add string list to custom adapter
        mLayoutManager = new LinearLayoutManager(view.getContext()); // get new layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mStringAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnItemTouchListener(
                new RecyclerTouchListener(view.getContext(), new RecyclerTouchListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String s = mStringList.get(position);
                        Toast.makeText(view.getContext(),"Clicked " + s, Toast.LENGTH_SHORT).show();
                    }
                })
        );
    } // END OF setUpRecyclerListener()


    /** updateRecyclerView()
     * Simple update to Recycler view
     * @param s
     */
    private void updateRecyclerView(String s){
        mStringList.add(s);
        mStringAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(mStringAdapter.getItemCount() - 1);
    } // END OF updateRecyclerView


    @Override
    public void onStart(){
        super.onStart();
//        launchVolleyService(VolleyIntentService.PARAM_ACTION_VOLLEY_GET);
    }

    @Override
    public void onStop() {
        super.onStop();
//        launchVolleyService(VolleyIntentService.PARAM_ACTION_SQLite_DELETE_LOCAL); // delete local cache
        launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIREBASE_STOP);
//        launchFirebaseService(FirebaseIntentService.PARAM_ACTION_SQLite_DELETE_LOCAL);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIREBASE_STOP);
        launchFirebaseService(FirebaseIntentService.PARAM_ACTION_SQLite_DELETE_LOCAL);
    }


}// END OF MainFragment()



