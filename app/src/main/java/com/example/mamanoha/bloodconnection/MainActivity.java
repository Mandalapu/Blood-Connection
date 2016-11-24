package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.app.Config;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    Button button1, button2;
    TextView tv1;
    ProgressDialog prgDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        button1 = (Button) findViewById(R.id.button);
        button2 = (Button) findViewById(R.id.button2);
        tv1 = (TextView) findViewById(R.id.textView1);
        prgDialog = new ProgressDialog(this);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prgDialog.setMessage("Please wait calling the local asyncmethod...");
                // Set Cancelable as False
                prgDialog.setCancelable(false);
                //Add logic here to make a Async call.
                Log.d("log", "before button clicked");
                new invokeWebService().execute();
                tv1.setVisibility(View.VISIBLE);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("log", "Taking you to the next activity");
                Intent mapIntent = new Intent(MainActivity.this, MapsActivity.class);
                startActivity(mapIntent);
            }
        });

        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    Log.d(TAG, "Firebase registered!");
                    displayFirebaseRegId();

                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received

                    String message = intent.getStringExtra("message");

                    Toast.makeText(getApplicationContext(), "Push notification: " + message, Toast.LENGTH_LONG).show();
                }
            }
        };

        displayFirebaseRegId();
    }

    // Fetches reg id from shared preferences
    // and displays on the screen
    private void displayFirebaseRegId() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String regId = pref.getString("regId", null);
        Log.d(TAG, "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId))
            Log.d(TAG, "reg id is:" +regId);
        else
            Log.d(TAG, "Firebase Reg Id is not received yet!");
    }

    private class invokeWebService extends AsyncTask<Void, Void, String> {
        ProgressDialog pdLoading = new ProgressDialog(MainActivity.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("connecting to server...");
            pdLoading.show();
        }

        @Override
        protected String doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String urls = "http://192.168.42.76:8080/GiveAPint/getAllUsers";
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            try
            {
                Log.d("log", "before making the url call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("log", "trying to get the output");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }
            catch(MalformedURLException e)
            {
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                Log.d("IOException",e.getMessage());
            }
            catch( Exception e)
            {
                Log.d("Exception",e.getMessage());
            }
            finally
            {
                urlConnection.disconnect();
            }
            Log.d("log","about to return the resul to postexceute");
            return result.toString();

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            pdLoading.setMessage("call success to the server");
            Log.d("postexecute", result);
            tv1.setText(result);
            //this method will be running on UI thread
            pdLoading.dismiss();
        }

    }

        @Override
        protected void onResume() {
            super.onResume();

            // register GCM registration complete receiver
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.REGISTRATION_COMPLETE));

            // register new push message receiver
            // by doing this, the activity will be notified each time a new message arrives
            LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                    new IntentFilter(Config.PUSH_NOTIFICATION));
        }

        @Override
        protected void onPause() {
            LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(mRegistrationBroadcastReceiver);
            super.onPause();
        }
}
