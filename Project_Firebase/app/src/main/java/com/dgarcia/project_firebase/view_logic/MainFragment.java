package com.dgarcia.project_firebase.view_logic;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.*;
import android.support.v7.widget.DividerItemDecoration;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import com.dgarcia.project_firebase.R;
import com.dgarcia.project_firebase.RecyclerTouchListener;
import com.dgarcia.project_firebase.StringAdapter;
import com.dgarcia.project_firebase.model.TestObject;
import com.dgarcia.project_firebase.services.FirebaseIntentService;
import com.dgarcia.project_firebase.services.TestObjectSvcSQLiteImpl;
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
    private Button mDoneButton;
    private TestObject testObject;
    private EditText mNameEditText;
    private TextView mErrorLabel;
    private View view;

    //Recycler View variables
    private List<String> mStringList = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private StringAdapter mStringAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    //Volley variable
    private final String urlForMethods = "https://regis-project.firebaseio.com/Volley/TestObjects.json";

    //Broadcast Receiver variable
    private MyBroadcastReceiver receiver;


    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstances){

        view = inflater.inflate(R.layout.fragment_main, container, false);

        Fabric.with(view.getContext(), new Crashlytics()); // For crashlytics.


        //Register broadcast receiver
        IntentFilter intentFilter = new IntentFilter(MyBroadcastReceiver.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyBroadcastReceiver();
        view.getContext().registerReceiver(receiver, intentFilter);

        setUpRecyclerListener();    // Method that sets up the Recycler Listener
        setUpButtons();             // Method that sets up button listeners
        setUpEditTextListener();    // Listeners for Name input. App start up.



        return view;
    } // END OF onCreate()




    /**
     * Broadcast Receiver CLASS
     */
    public class MyBroadcastReceiver extends BroadcastReceiver {

        public static final String ACTION_RESPONSE = "com.dgarcia.project_firebase.intent.action.MESSAGE_PROCESSE";
        ProgressDialog progressDialog;

        @Override
        public void onReceive(Context context, Intent intent) {
//            String intentStr =intent.getStringExtra(VolleyIntentService.PARAM_OUT_MSG); //get the string from the VolleyIntentService

            //Firebase intent message
            String intentStr =intent.getStringExtra(FirebaseIntentService.PARAM_OUT_MSG); //get the string from the FirebaseIntentService
            if(intentStr.contains(FirebaseIntentService.CONNECTED)) {
                hideButtons(false);
                progressDialog.dismiss(); // When connected get rid of progress box
            }
            else if (intentStr.contains(FirebaseIntentService.NOT_CONNECTED)) {
                hideButtons(true);

                // Show progress box when not connected -Firebase SDK ONLY
                ConnectivityManager connectivityManager = (ConnectivityManager) view.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
                if(activeNetworkInfo != null && activeNetworkInfo.isConnected())
                    progressDialog = ProgressDialog.show(view.getContext(), "Please Wait", "Trying to connect to Firebase.", true);
                else progressDialog = ProgressDialog.show(view.getContext(), "No Internet", "Please turn on wifi or data.", true);
                progressDialog.setCancelable(false);
            }

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
     * Sets button visibility for GET, POST & DELETE
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


    /** hideKeyboard()
     *  Hides soft keyboard
     */
    public void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    } // END OF hideKeyboard()


    /** setupEditTextListener()
     *
     */
    public void setUpEditTextListener() {
        mDoneButton = (Button) view.findViewById(R.id.button_DONE);                 // Clicked when user enters name
        mNameEditText = (EditText)view.findViewById(R.id.editTxt_enter_your_name);
        mErrorLabel = (TextView)view.findViewById(R.id.textView_error_name);        // Error label
        final String noNameErr = "No name entered.";
        final String need3LettersErr = "Name must be at least 3 characters.";
        final String noSpacesErr = "no spaces allowed.";

        mDoneButton.setVisibility(View.INVISIBLE);
        hideButtons(true);  // Hide all database button actions until user enters name

        // Used to monitor input of name
        final TextWatcher textWatcher = new TextWatcher() {
            @Override// Before text is changed.
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do stuff
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { // executes every time a character is added/removed
                String str = s.toString();

                if(str.length() > 0 &&  str.contains(" ")){         // no spaces allowed error. Just looking for a first name.
                    mErrorLabel.setText(noSpacesErr);
                    mErrorLabel.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.INVISIBLE);
                } else if(str.length() == 0) {                      // no name entered error.
                    mErrorLabel.setText(noNameErr);
                    mErrorLabel.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.INVISIBLE);
                }else if(str.length() >= 1 && str.length() <= 2) {  // at least 3 characters error. Name must be at least 3 chars long.
                    mErrorLabel.setText(need3LettersErr);
                    mErrorLabel.setVisibility(View.VISIBLE);
                    mDoneButton.setVisibility(View.INVISIBLE);
                }else if(str.length() >= 3){                        // name is good. Show DONE button.
                    mErrorLabel.setVisibility(View.INVISIBLE);
                    mDoneButton.setVisibility(View.VISIBLE);
                }
            }

            @Override // when focus is leaves edit text AND there is a change
            public void afterTextChanged(Editable s) {
                // do stuff
            }
        };

        // If the name EditText has focus start text listener.
        mNameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if(hasFocus){
                    mNameEditText.addTextChangedListener(textWatcher);
                }else{
                    mNameEditText.removeTextChangedListener(textWatcher);
                }
            }
        });

        // If DONE button clicked hide DONE button, show database action buttons, and start Firebase
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
                String s = "Playing as " + mNameEditText.getText().toString();
                mNameEditText.setFilters(new InputFilter[]{ new InputFilter.LengthFilter(s.length())}); // changes the Max length of the EditText to "s" length.
                mNameEditText.setText(s);
                mNameEditText.setFocusable(false);          // Dont allow anymore name changes
                mDoneButton.setVisibility(View.INVISIBLE);
                mErrorLabel.setVisibility(View.INVISIBLE);

                launchFirebaseService(FirebaseIntentService.PARAM_ACTION_FIREBASE_START); // Start Firebase
                hideButtons(false);
                updateRecyclerView("   Ready...");
            }
        });

    } // END OF setUpEditTextListener()



    /** forshCrash()
     *  Crash handler for Crashlytics
     * @param view
     */
    public void forceCrash(View view) {
        throw new RuntimeException("This is a crash");
    }


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

                forceCrash(view);

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
     * Sets up and starts Recycler Listener. If testObject is clicked show item info in new activity.
     */
    private void setUpRecyclerListener(){
        //RECYCLER VIEW setup
        mRecyclerView = (RecyclerView)view.findViewById(R.id.RecyclerView_outputWindow);    //Get handle on recycler view
        mStringAdapter = new StringAdapter(mStringList, view.getContext());    //add string list to custom adapter
        mLayoutManager = new LinearLayoutManager(view.getContext()); // get new layout manager
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        mRecyclerView.setAdapter(mStringAdapter);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));

        mRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(view.getContext(), new RecyclerTouchListener.OnItemClickListener() {
                    @Override public void onItemClick(View view, int position) {
                        String s = mStringList.get(position);   // String of item clicked on

                        if(s.contains("ID:")){  //If the item clicked on is a test object
                            s = s.substring(s.indexOf("ID:") + 3, s.indexOf("DATE")).trim();    //get the id from the string
                            List<TestObject> tObjs = new TestObjectSvcSQLiteImpl(view.getContext()).retrieveAllTestObjects(); //get cached testObjects

                            if(tObjs.size() > 0) {  // if testObjects in list, search for id of the one clicked on.

                                for (TestObject t : tObjs) {
                                    if (t.getId() == Integer.parseInt(s)) { //if id of clicked on is found, start item_info activity
                                        Intent intent1 = new Intent(getActivity(), RecyclerItemInfoActivity.class);
                                        intent1.putExtra(RecyclerItemInfoFragment.PARAM_IN_TESTOBJECT, t);
                                        startActivity(intent1);
                                        getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right); // Fade transition
                                        break;
                                    }

                                }
                            }else // If list is empty no testObjects
                                Toast.makeText(view.getContext(), "No Test Objects available. Cache empty.", Toast.LENGTH_SHORT).show();
                        }else // If item clicked on isnt a testObject
                            Toast.makeText(view.getContext(), "No info for this item.", Toast.LENGTH_SHORT).show();
                    }
                })
        );
    } // END OF setUpRecyclerListener()



    /** updateRecyclerView()
     * Simple update to Recycler view
     * @param s
     */
    private void updateRecyclerView(String s){
        mStringList.add(0,s);   // add to first position in Recycle View
//        mStringList.add(s);   // add to last position in Recycle View

        mStringAdapter.notifyDataSetChanged();

//        mRecyclerView.smoothScrollToPosition(mStringAdapter.getItemCount() - 1);  // Scroll to last position
//        mRecyclerView.smoothScrollToPosition(0);    //Scroll to first position

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
    }

}// END OF MainFragment()



