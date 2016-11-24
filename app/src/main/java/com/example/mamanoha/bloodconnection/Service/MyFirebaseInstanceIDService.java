package com.example.mamanoha.bloodconnection.Service;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.LoginActivity;
import com.example.mamanoha.bloodconnection.MainActivity;
import com.example.mamanoha.bloodconnection.app.Config;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by Manu on 11/12/2016.
 */

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = MyFirebaseInstanceIDService.class.getSimpleName();

    private String urls;
    private SharedPreferences preferences;
    private String token;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();

        // Saving reg id to shared preferences
        storeRegIdInPref(refreshedToken);

        // sending reg id to your server
        sendRegistrationToServer(refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void sendRegistrationToServer(final String token) {
        // sending gcm token to server
        //Need to make a call to service to store the regId.
        this.token = token;
        new UpdateNotification().execute();
        Log.d(TAG, "sendRegistrationToServer: " + token);
        //New Async to update the notification token.
    }

    private class UpdateNotification extends AsyncTask<Void, Void, JSONObject>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            //Constructing the query required to make a call to the service.
            urls = constructQuery();
            Log.d("Changed Query is:", urls);
        }

        private String constructQuery()
        {
            StringBuilder result = new StringBuilder();
            preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String userName = preferences.getString("userName", "");
            try {
                result.append("http://192.168.42.76:8080/GiveAPint/registerNotificationToken?");
                result.append(URLEncoder.encode("userName", "UTF-8")).append("=").append(URLEncoder.encode(userName, "UTF-8")).append("&")
                        .append(URLEncoder.encode("regId", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8"));
            }
            catch(UnsupportedEncodingException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception while constructing the URL");
            }
            Log.d("URL", result.toString());
            return result.toString();
        }

        @Override
        protected JSONObject doInBackground(Void... params)
        {
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            JSONObject resultObject = new JSONObject();
            try
            {
                Log.d("log", "before making the url call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("log", "trying to get the output");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","got the output, before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                resultObject = new JSONObject(result.toString());
                Log.d("Result", result.toString());
            }
            catch(MalformedURLException e)
            {
                e.printStackTrace();
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                e.printStackTrace();
                Log.d("IOException",e.getMessage());
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception occurred while parsing the Json object");
            }
            catch( Exception e)
            {
                Log.d("Exception", "Normal exception occured  while making a call");
                e.printStackTrace();
                //Log.d("Exception", e.getLocalizedMessage());
            }
            finally
            {
                urlConnection.disconnect();
            }
            Log.d("log","about to return the result");
            //Log.d("log", resultArray.toString());
            return resultObject;
        }

        @Override
        protected void onPostExecute(JSONObject result)
        {
            super.onPostExecute(result);
            // TODO: 11/10/2016 need to check if the registration is successful.
            try {
                Log.d("OnPostExecute", "came inside the onPostExecute");
                if (!(result.getString("error").equals(""))) {
                    //If the error string is set in the result. Toast display the error message and then do not propogate
                    // into the next intent.
                    Toast.makeText(getApplicationContext(), "Error: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Successfully inserted or updated the notifications token for a corresponding user");
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception while parsing the result");
            }
        }

    }

    private void storeRegIdInPref(String token) {
        Log.d("Registration Id:", "Registration completed for the user");
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("regId", token);
        Log.d("Shared Preferences", "Successfully stored the regid in the shared preferences");
        editor.commit();
    }
}