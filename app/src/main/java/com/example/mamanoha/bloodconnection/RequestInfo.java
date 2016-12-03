package com.example.mamanoha.bloodconnection;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.DataObjects.Acceptor;
import com.example.mamanoha.bloodconnection.DataObjects.ProjectConstants;

import org.json.JSONArray;
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
import java.util.ArrayList;
import java.util.List;

public class RequestInfo extends AppCompatActivity {

    private final int PERMISSION_CALL_PHONE = 10;
    private Intent callIntent;
    private final String TAG = RequestInfo.this.getClass().getSimpleName();
    private SharedPreferences prefs;
    //Values retrieved from the previous intent.
    Integer requestId;
    String requestedBloodGroup;
    String requestTime;
    //Values retrived from the shared preferences.
    int userId;
    String token;
    //References to the widget fields in the current layout, values are set once the Async call is made.
    private Button bloodGroupButton;
    private TextView awaitingMessage;
    private TextView textviewTimevalue;
    private TextView noAcceptorsMessage;
    private ListView requestInfoListView;
    private RequestInfoAdapter listAdapter;
    private List<Acceptor> listOfAcceptors;
    private JSONObject rawAcceptor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_info);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Request");
        //Get the id's of the outer fields which are needed to set here in this onPostExecute.
        bloodGroupButton = (Button) findViewById(R.id.button_bloodGroup);
        awaitingMessage = (TextView) findViewById(R.id.awaitingmessage);
        textviewTimevalue = (TextView) findViewById(R.id.textview_timevalue);
        noAcceptorsMessage = (TextView) findViewById(R.id.noacceptors_message);
        requestInfoListView = (ListView) findViewById(R.id.requestinfo_listview);
        listOfAcceptors = new ArrayList<>();
        //awaitingmessage, textview_timevalue

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                requestId = null;
                requestedBloodGroup = null;
                requestTime = null;
            } else {
                requestId = extras.getInt("requestId");
                requestedBloodGroup = extras.getString("requestedBloodGroup");
                requestTime = extras.getString("requestTime");

            }
        } else {
            requestId = (int) savedInstanceState.getSerializable("requestId");
            requestedBloodGroup = (String) savedInstanceState.getSerializable("requestedBloodGroup");
            requestTime = (String) savedInstanceState.getSerializable("requestTime");
        }
        userId = prefs.getInt("userId", 0);
        token = prefs.getString("token", "invalidToken");
        Toast.makeText(getApplicationContext(), "requestId received is" + requestId + ", " + userId + ", " + token, Toast.LENGTH_SHORT).show();
        String url = constructQueryForInfo(requestId, userId, token);
        new GetRequestInfo().execute(url);
    }

    private class GetRequestInfo extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pdLoading = new ProgressDialog(RequestInfo.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d(TAG, "inside the preexecute method");
            pdLoading.setMessage("Fetching data");
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            //Constructing the query required to make a call to the service.
            pdLoading.setMessage("Fetching data");
            pdLoading.show();
            String urls = params[0];
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            JSONObject resultObject = new JSONObject();
            try {
                Log.d(TAG, "before making the url call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d(TAG, "trying to get the output");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d(TAG, "got the output, before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                resultObject = new JSONObject(result.toString());
                Log.d(TAG, result.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d("Exception", "Exception occurred while parsing the Json object");
            } catch (Exception e) {
                Log.d("Exception", "Normal exception occured  while making a call");
                e.printStackTrace();
                //Log.d("Exception", e.getLocalizedMessage());
            } finally {
                urlConnection.disconnect();
            }
            Log.d(TAG, "about to return the result");
            Log.d("log", resultObject.toString());

            return resultObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            pdLoading.setMessage("Fetching data");
            pdLoading.show();
            super.onPostExecute(result);
            boolean areAcceptorsEmpty = false;
            listOfAcceptors.clear();
            try {
                Log.d(TAG, "came inside the onPostExecute");
                if (!(result.getString("error").equals(""))) {
                    Toast.makeText(getApplicationContext(), "Error: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error while fetching the requests corresponding to the user");
                } else {
                    Log.d(TAG, "Call for fetching the request info is successful.");
                    Log.d(TAG, result.toString());

                    //Parse the  result and set the list view adapter. Set the acustom adapter only  when the "acceptedUsers"
                    // size is zero.
                    //Construct new Acceptor from the list in the result.
                    JSONArray rawAcceptors = new JSONArray();
                    if( result.get("acceptedUsers").equals(null) )
                    {
                        areAcceptorsEmpty = true;
                        Log.d(TAG, "setting areAceptors to be true");
                    }
                    else
                    {
                        rawAcceptors = (JSONArray) result.get("acceptedUsers");
                    }
                    //Set the normal fields first.
                    bloodGroupButton.setText(requestedBloodGroup);
                    int awaitingAcceptors = result.getInt("totalNumber") - result.getInt("respondedNumber");
                    String awaitingMessageText = "Awaiting Response from " + awaitingAcceptors + " more users";
                    Log.d(TAG, "Awaiting message set: " + awaitingMessageText);
                    awaitingMessage.setText(awaitingMessageText);
                    textviewTimevalue.setText(requestTime);
                    if ( areAcceptorsEmpty || rawAcceptors.length() == 0) {
                        //No acceptors found at this time, set the message in the noacceptors_message field. do set the listadapter on
                        // list view. Just return the point where this call has been triggered.
                        noAcceptorsMessage.setVisibility(View.VISIBLE);
                        areAcceptorsEmpty = true;
                    }
                    if (!areAcceptorsEmpty) {
                        for (int i = 0; i < rawAcceptors.length(); i++) {
                            rawAcceptor = (JSONObject) rawAcceptors.get(i);
                            //parse rawAcceptor.getDouble(distanceInMiles)
                            String distance = String.format("%.1f", rawAcceptor.getDouble("distanceInMiles"));
                            Acceptor acceptor = new Acceptor(rawAcceptor.getString("firstName"), rawAcceptor.getInt("age"), rawAcceptor.getInt("userId"),
                                    rawAcceptor.getString("bloodGroup"), rawAcceptor.getString("phoneNumber"), distance);
                            listOfAcceptors.add(acceptor);
                        }
                        listAdapter = new RequestInfoAdapter(RequestInfo.this, listOfAcceptors, new BtnClickListener() {
                            @Override
                            public void onBtnClick(int position) {
                                // TODO: 11/27/2016   Get the associated phone number from the position and make a phone call from here.
                                callIntent = new Intent(Intent.ACTION_CALL);
                                if( callIntent == null )
                                {
                                    Log.d(TAG, "Intent is null, should re check it");
                                }
                                String mobile = "tel:"+listOfAcceptors.get(position).getPhoneNumber();
                                mobile = Uri.parse(mobile).toString();
                                Log.d(TAG, "Mobile Number retrieved is: "+mobile);
                                callIntent.setData(Uri.parse(mobile));
                                //callIntent.setData(Uri.parse("tel:0377778888"));
                                Log.d(TAG, "Came back to the onclick listener in RequestInfo");
                                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                                        android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED)
                                {
                                    Log.d(TAG, "If this is triggered, you need to take permissions before making the call");
                                    requestNewPermissions();
                                }
                                Log.d(TAG, "This should start the new activity");
                                startActivity(callIntent);
                                //return;
                            }

                            @Override
                            public void onBtnClickWithStatus(int position, String Status) {
                                Log.d(TAG, "Do Nothing");
                                return;
                            }


                        });
                        requestInfoListView.setAdapter(listAdapter);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "Exception while parsing the result and calling the adapter, onPostExecute");
            }
            pdLoading.dismiss();
        }

    }
    private void requestNewPermissions()
    {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED )
        {
            //request for permissions
            Log.d("Location", "Ask for the permissions right now.");
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CALL_PHONE },
                    PERMISSION_CALL_PHONE);
        }
        return;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CALL_PHONE:
                Log.d("Permissions", "Got the permissions required for the App");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
        return;
    }

    private String constructQueryForInfo(int requestId, int userId, String token)
    {
        StringBuilder result = new StringBuilder(ProjectConstants.URL_HEADER);
        try {
            result.append("/getRequestInfo?");
            result.append(URLEncoder.encode("requestId", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(requestId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("userId", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(userId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("token", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception while constructing the URL");
        }
        Log.d(TAG, "Url constructed:" +result.toString());
        return result.toString();
    }

}
