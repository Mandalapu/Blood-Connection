package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.DataObjects.ProjectConstants;
import com.example.mamanoha.bloodconnection.Service.MyFirebaseInstanceIDService;

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

public class LoginActivity extends AppCompatActivity {
    private EditText username;
    private EditText password;
    private String userName;
    private String passcode;
    private String urls;
    private String token;
    private SharedPreferences preferences;
    private MyFirebaseInstanceIDService firebaseInstanceIDService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        username = (EditText)findViewById(R.id.editText1);
        username.requestFocus();
        password = (EditText)findViewById(R.id.editText2);
        Button button1 = (Button) findViewById(R.id.button1);
        button1.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                userName = username.getText().toString();
                passcode = password.getText().toString();
                if( userName.equals("") ) {
                    Toast.makeText(getApplicationContext(), "User name can't be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if( passcode.equals("") ) {
                    Toast.makeText(getApplicationContext(), "Password can't be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
                //If it came here, we can say that the credentials provided are empty and make a  call
                //to service and  get the new token and store it in the shared preferences.
                new loginUser().execute();
            }
        });

        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

        //Listening to Register link.
        registerScreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegistrationActivity.class);
                startActivity(i);
            }
        });

         preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //This token should be sent every time when user login.
        token = preferences.getString("token", "");



    }

    private class loginUser extends AsyncTask<String, Void, JSONObject>
    {
        ProgressDialog pdLoading = new ProgressDialog(LoginActivity.this);

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("connecting to server...");
            //Constructing the query required to make a call to the service.
            urls = constructQuery();
            Log.d("Changed Query is:", urls);
            pdLoading.show();
        }

        private String constructQuery() {
            StringBuilder result = new StringBuilder();
            try {
                result.append(ProjectConstants.URL_HEADER);
                result.append("/loginUser?");
                result.append(URLEncoder.encode("userName", "UTF-8")).append("=").append(URLEncoder.encode(userName, "UTF-8")).append("&")
                        .append(URLEncoder.encode("passcode", "UTF-8")).append("=").append(URLEncoder.encode(passcode, "UTF-8")).append("&")
                        .append(URLEncoder.encode("token", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8"));
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
        protected JSONObject doInBackground(String... strings)
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
            pdLoading.setMessage("call success to the server");
            // TODO: 11/10/2016 need to check if the registration is successful.
            try {
                Log.d("OnPostExecute", "came inside the onPostExecute");
                if ( !(result.getString("error").equals("")) ) {
                    //If the error string is set in the result. Toast display the error message and then do not propogate
                    // into the next intent.
                    Toast.makeText(getApplicationContext(), "Error: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                } else {
                    //TODO Also need to store the information in the shared preferences which will be used by further requests.
                    Log.d("postexecute", result.toString());
                    Log.d("Shared Preferences", "Witing to shared preferences");
                    //Writing to a default file location.
                    SharedPreferences.Editor editor = preferences.edit();
                    //Storing only essential information.
                    editor.putString("token", result.getString("token"));
                    //This key value pair can be used to check if the user is currently login.
                    editor.putBoolean("isUserLogin", true);
                    editor.apply();
                    Log.d("Log", "Successfully written in shared preferences");
                    //Make a call here after successful login to send the regsitration id to the service.
                    firebaseInstanceIDService = new MyFirebaseInstanceIDService();
                    String userName = preferences.getString("userName", "");
                    String regId = preferences.getString("regId", "");
                    Log.d("regId", "retrieved regId from the preferences:" +regId);
                    new UpdateNotification().execute(userName, regId);
                    Toast.makeText(getApplicationContext(), "Login successful", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                    startActivity(intent);
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception while parsing the result");
            }
        }

    }

    private class UpdateNotification extends AsyncTask<String, Void, JSONObject>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
        }

        private String constructQuery(String userName, String regId)
        {
            StringBuilder result = new StringBuilder();
            try {
                result.append(ProjectConstants.URL_HEADER);
                result.append("/registerNotificationToken?");
                result.append(URLEncoder.encode("userName", "UTF-8")).append("=").append(URLEncoder.encode(userName, "UTF-8")).append("&")
                        .append(URLEncoder.encode("regId", "UTF-8")).append("=").append(URLEncoder.encode(regId, "UTF-8"));
            }
            catch(UnsupportedEncodingException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception while constructing the URL");
            }
            Log.d("noti service url", result.toString());
            return result.toString();
        }

        @Override
        protected JSONObject doInBackground(String... params)
        {
            //Constructing the query required to make a call to the service.
            urls = constructQuery(params[0], params[1]);
            Log.d("Changed Query is:", urls);
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
                if ( !(result.getString("error").equals("")) ) {
                    //If the error string is set in the result. Toast display the error message and then do not propogate
                    // into the next intent.
                    //Toast.makeText(getApplicationContext(), "Error: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                    Log.d("Notifications", "Error while inserting or updating the notifications token for a corresponding user");
                } else {
                    Log.d("Notifications", "Successfully inserted or updated the notifications token for a corresponding user");
                }
            }
            catch(JSONException e)
            {
                e.printStackTrace();
                Log.d("Exception", "Exception while parsing the result");
            }
        }

    }

    @Override
    public void onStart()
    {
        super.onStart();
        Log.d("onStart", "in onstart activity");
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        boolean isLogin = preferences.getBoolean("isUserLogin", false);
        Log.d("onStart boolean", Boolean.toString(isLogin));
        String token = preferences.getString("token", "");
        Log.d("onStart token value", token);
        if( isLogin )
        {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
        }
        //else, it shouldn't be any different.Just start the activity and do the stuff.

    }

    @Override
    public void onBackPressed() {

    }
}
