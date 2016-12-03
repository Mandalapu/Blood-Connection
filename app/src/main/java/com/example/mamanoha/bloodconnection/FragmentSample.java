package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mamanoha.bloodconnection.DataObjects.ProjectConstants;
import com.example.mamanoha.bloodconnection.DataObjects.UserRequests;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Manu on 11/25/2016.
 */
public class FragmentSample extends AppCompatActivity {
    public static final String TAG = FragmentSample.class.getSimpleName();
    private TextView resulttv;
    private List<UserRequests> myrequests = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat();
    private Toolbar toolbar;
    private ListView myrequestsListView;
    private CustomAdapter listAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myrequests);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("My Requests");
        toolbar.setTitleMarginStart(60);
        setSupportActionBar(toolbar);
        myrequestsListView = (ListView) findViewById(R.id.requests_listview);

        // TODO: 11/26/2016 Create a new Async call to fetch the requests data and set appropriate "loading messages" 
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        int userId = prefs.getInt("userId", 0);
        String token = prefs.getString("token", "dummyToken");
        Log.d(TAG, "Data retrieved from the shared preferences are:" +userId +" " +token);
        String url = constructQueryRequests(userId, token);
        new GetUserRequests().execute(url);
        myrequestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Toast.makeText(getApplicationContext(), "List View Item clicked!", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Position of the list item that is clicked:" +i);
                UserRequests request = myrequests.get(i);
                int requestId = request.getRequestId();
                String bloodGroup = request.getBloodGroup();
                String requestTime = request.getRequestDate();
                Log.d(TAG, "request id corresponding to the request:" +requestId);
                Intent requestInfo = new Intent(FragmentSample.this, RequestInfo.class);
                requestInfo.putExtra("requestId", requestId);
                requestInfo.putExtra("requestedBloodGroup", bloodGroup);
                requestInfo.putExtra("requestTime", requestTime);
                startActivity(requestInfo);


            }
        });
    }
    private String constructQueryRequests(int userId, String token) {
        StringBuilder result = new StringBuilder();
        try {
            result.append(ProjectConstants.URL_HEADER);
            result.append("/getRequestsForUser?");
            result.append(URLEncoder.encode("userId", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(userId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("token", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d(TAG, "Exception while constructing the URL");
        }
        Log.d(TAG, "Url constructed: " +result.toString());
        return result.toString();
    } 
    private class GetUserRequests extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pdLoading = new ProgressDialog(FragmentSample.this);
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
            super.onPostExecute(result);
           // resulttv = (TextView) findViewById(R.id.requests_tv);
            // TODO: 11/26/2016 Need to parse the data and construct all the arraylist required for the customAdapter. 
            // TODO: 11/26/2016 Need to make call to customAdaptor for filling the listView.
            //Clear the list if it has previous call data.
            myrequests.clear();

            try {
                Log.d(TAG, "came inside the onPostExecute");
                if (!(result.getString("error").equals(""))) {
                    Toast.makeText(getApplicationContext(), "Error: " + result.getString("error"), Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "Error while fetching the requests corresponding to the user");
                } else {
                    //Parse the array and form the new fields.
                    JSONArray requests = result.getJSONArray("requests");
                    JSONObject request = new JSONObject();
                    for(int i = 0; i < requests.length(); i++ )
                    {
                        //Get each request as an JSON Object. Form new UserRequests object and append it to the list.
                        request = (JSONObject) requests.get(i);
                        //Need to trigger constructs after parsing the fields more appropriately.
                        //parse the  timestamp to return a date.
                        long timestamp = request.getLong("timestamp");
                        // TODO: 11/27/2016 In future, this hack should be uprroted and replaced with more reliable code to support time zones
                        timestamp += (8*60*60*1000);
                        Date requestedDate = new Date((long)timestamp);
                        String date = sdf.format(requestedDate);
                        Log.d(TAG, "Date returned is:" +date);
                        UserRequests requestItem = new UserRequests(request.getInt("requestId"), request.getString("requestedBloodType"),
                                request.getString("status"), request.getInt("emergencyLevel"), date);
                        myrequests.add(requestItem);

                    }
                    Log.d(TAG, "Successfully got the user requests");
                    Log.d(TAG, "resultant list " +myrequests.toString());
                    //resulttv.setText(result.toString());
                    listAdapter = new CustomAdapter(FragmentSample.this, myrequests);
                    Log.d(TAG, "list Adapter should be set by this point");
                    myrequestsListView.setAdapter(listAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.d(TAG, "Exception while parsing the result and calling the adapter, onPostExecute");
            }
            pdLoading.dismiss();
        }


    }

}
