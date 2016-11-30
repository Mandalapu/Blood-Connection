package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.DataObjects.Acceptor;
import com.example.mamanoha.bloodconnection.DataObjects.AwaitingRequest;

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

/**
 * Created by Manu on 11/28/2016.
 */

public class AwaitingRequests extends AppCompatActivity {
    private final String TAG = AwaitingRequests.this.getClass().getSimpleName();
    private SharedPreferences prefs;
    private Toolbar toolbar;
    private int userId;
    private String token;
    private TextView noAwaitingRequestsMessage;
    private List<AwaitingRequest> listOfRequests;
    private AwaitingRequestAdapter listAdapter;
    private ProgressDialog pdLoading;
    private ListView awaitingRequestListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        setContentView(R.layout.activity_awaitingrequests);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        awaitingRequestListView = (ListView) findViewById(R.id.awaitingrequests_listview);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Awaiting Requests");
        listOfRequests = new ArrayList<>();
        noAwaitingRequestsMessage = (TextView) findViewById(R.id.noawaitingrequestsMessage);
        userId = prefs.getInt("userId", 0);
        Log.d(TAG, "Retrieved fields from the shared preferences are: " + userId + " " + token);
        token = prefs.getString("token", "invalidTokenFromPrefs");
        String url = constructQueryForAwaitingReq(userId, token);
        pdLoading = new ProgressDialog(AwaitingRequests.this);
        new GetAwaitingRequests().execute(url);

    }

    private String constructQueryForAwaitingReq(int userId, String token) {
        StringBuilder result = new StringBuilder();
        try {
            result.append("http://192.168.42.76:8080/GiveAPint/generateAwaitResults?");
            result.append(URLEncoder.encode("responderId", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(userId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("token", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception while constructing the URL");
        }
        Log.d(TAG, "Url constructed:" + result.toString());
        return result.toString();
    }

    private class GetAwaitingRequests extends AsyncTask<String, Void, JSONObject> {


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
            boolean areAwaitingRequestsEmpty = false;
            listOfRequests.clear();
            Log.d(TAG, "came inside the onPostExecute");
            Log.d(TAG, "Response retrived: " + result.toString());
            try {
                if (!(result.getString("error").equals(""))) {
                    Toast.makeText(getApplicationContext(), "Error: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error while fetching the requests corresponding to the user");
                }
                else {
                    Log.d(TAG, "Call for fetching the request info is successful.");
                    Log.d(TAG, result.toString());

                    //Parse the  result and set the list view adapter. Set the acustom adapter only  when the "resultList"
                    // size is zero.
                    //Construct new Acceptor from the list in the result.
                    JSONArray rawAwaitingRequests = new JSONArray();
                    JSONObject rawRequest = new JSONObject();
                    if (result.get("resultList").equals(null)) {
                        areAwaitingRequestsEmpty = true;
                        Log.d(TAG, "setting areAceptors to be true");
                    } else {
                        rawAwaitingRequests = (JSONArray) result.get("resultList");
                        Log.d(TAG, "rawAawaitingRequests:" +rawAwaitingRequests.toString());
                        Log.d(TAG, "size:" +rawAwaitingRequests.length());

                    }
                    if (areAwaitingRequestsEmpty || rawAwaitingRequests.length() == 0) {
                        // TODO: 11/28/2016 Set No Awaiting requests message if there are no corresponding requests awaiting for this user.
                        areAwaitingRequestsEmpty = true;
                        noAwaitingRequestsMessage.setVisibility(View.VISIBLE);
                    }
                    if( !areAwaitingRequestsEmpty )
                    {
                        Log.d(TAG, "areawaitingrequests not empty------");
                        //Set the adapter. Else just retun from the onPostExecute.
                        // TODO: 11/28/2016 Construct the listOfAwaitingRequests from the rawAwaitingRequests JsonArray.
                        for(int i = 0; i < rawAwaitingRequests.length(); i++ )
                        {
                            /**
                             * requestorFName":"Aravind","requestorLName":"Doodala",
                             * "requestedBG":"AB+","emerLevel":5,"distance":9.780850143745342,
                             * "requestId":2,"requestorId":5}]
                             */
                            rawRequest = (JSONObject) rawAwaitingRequests.get(i);
                            String distance = String.format("%.1f", rawRequest.getDouble("distance"));
                            String fullName = rawRequest.getString("requestorFName") + " " +rawRequest.getString("requestorLName");
                            AwaitingRequest request = new AwaitingRequest(fullName, rawRequest.getString("requestedBG"),
                                    rawRequest.getInt("emerLevel"), distance, rawRequest.getInt("requestId"), rawRequest.getInt("requestorId"));
                            listOfRequests.add(request);
                        }
                        listAdapter = new AwaitingRequestAdapter(AwaitingRequests.this, listOfRequests, new BtnClickListener() {
                            @Override
                            public void onBtnClick(int position) {
                                Log.d(TAG, "Do Nothing");
                                return;

                            }

                            @Override
                            public void onBtnClickWithStatus(int position, String Status) {
                                //Check the status and make async call from here.
                                //Need to retrieve rest of the fields from prefs and arguments for constructing the query.
                                //requestId, response - from position
                                //usrId, token from the prefs
                                int requestId = listOfRequests.get(position).getRequestId();
                                String url = constructQueryForRespond(requestId, userId, token, Status);
                                new RespondToRequest().execute(url, String.valueOf(position));
                            }
                        });

                        awaitingRequestListView.setAdapter(listAdapter);
                    }

                }
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            pdLoading.dismiss();

        }
    }

    private void triggerAsyncTask(String url)
    {
        Log.d(TAG, "Triggered thrid party neutral function");
        Log.d(TAG, "url received and about to make a call:" +url);
       new GetAwaitingRequests().execute(url);


    }


    private String constructQueryForRespond(int requestId, int userId, String token, String response) {
        StringBuilder result = new StringBuilder();
        try {
            result.append("http://192.168.42.76:8080/GiveAPint/respondToRequest?");
            result.append(URLEncoder.encode("requestId", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(requestId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("userId", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(userId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("token", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8")).append("&")
                    .append(URLEncoder.encode("response", "UTF-8")).append("=").append(URLEncoder.encode(response, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception while constructing the URL");
        }
        Log.d(TAG, "Url constructed:" + result.toString());
        return result.toString();
    }

    private class RespondToRequest extends AsyncTask<String, Void, JSONObject> {

        private int position;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d(TAG, "inside the preexecute method");
            pdLoading.setMessage("Saving response");
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {
            //Constructing the query required to make a call to the service.
            pdLoading.setMessage("Saving response");
            pdLoading.show();
            String urls = params[0];
            position = Integer.valueOf(params[1]);
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
            super.onPostExecute(result);
            pdLoading.dismiss();
            Log.d(TAG, "Inside the onPostExecute after saving the response");
            Log.d(TAG, "Response returned is:" +result.toString());
            try
            {
                if ( !(result.getString("error").equals("")) )
                {
                    Log.d(TAG, "Error Occrred while saving the response from the user.");
                    Toast.makeText(getApplicationContext(), "Error Occrred while saving the response", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, result.toString());
                }
                else
                {
                    Log.d(TAG, "Async call responding to the request is successful");
                    Log.d(TAG, "Now calling to  remove the positon");
                    if( listOfRequests.size() >= 1) {
                        listOfRequests.remove(position);
                        listAdapter.notifyDataSetChanged();
                    }
                    else
                    {
                        noAwaitingRequestsMessage.setVisibility(View.VISIBLE);
                    }
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    //AsyncCall here for responding to the request.



}
