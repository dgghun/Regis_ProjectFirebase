package com.dgarcia.project_firebase.services;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.dgarcia.project_firebase.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import static com.google.android.gms.internal.zzt.TAG;


public class HttpAsyncTask extends AsyncTask<String, Void, String> {


    // Runs in UI before background thread is called
//    @Override
//    protected void onPreExecute() {
//        super.onPreExecute();
//        Log.e("onPreExecute", "Started");
//        // Do something like display a progress bar
//    }



    @Override
    protected String doInBackground(String... params) {

        Log.e("doInBackground", "Started doInBacgGround");
        String result = "";
        HttpURLConnection urlConnection = null;

        try{
            URL url = new URL(params[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");

            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String s = "";
            while((s = reader.readLine()) != null){
                result += s;
            }
        }catch (IOException e){
            Log.e("HttpAsyncTask", "EXCEPTION: " + e.getMessage());
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {

        String[] databaseEntries;
        Log.e(TAG, "entering onPostExecute");

        try{
            JSONArray jsonArray = new JSONArray(s);
            final int length = jsonArray.length();
            Log.e(TAG, "Number of entries " + length);

            databaseEntries = new String[length];

            for(int i = 0; i < length; i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Log.e(TAG, jsonObject.getString("id") + " " + "date");
                databaseEntries[i] = jsonObject.getString("id"); //or use "date" ????

                Log.e("PostExecute", "DB Entries: " + databaseEntries[i]);
            }


            //Do stuff...
//            someAdapter = new ArrayAdapter<String>(self, R.layout.some_text_view, someObject);
//            listView.setAdapter(adapter);
//            adapter.notifyDataSetChanged();

        }catch (Exception e){
            e.printStackTrace();
        }
        Log.e(TAG, "exiting onPostExecute");
    }
}
