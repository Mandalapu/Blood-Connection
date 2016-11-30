package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends  AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LatLng currentLocation;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private MarkerOptions userMarkerOptions;
    private Marker userMarker;
    private SharedPreferences prefs;
    //Parameters for making request, used by both the Knn and RangeQuery.
    private String queryType;
    private int userId;
    private int emergencyLevel;
    private int kVal;
    private double rangeVal;
    private long timestamp;
    private Date currentDate;
    private String token;
    private String status;
    private String requestBloodGroup;
    //End of the parameters.
    private RatingBar emergencyLevel_Knn;
    private RatingBar emergencyLevel_Range;
    private Spinner requestBloodGroup_Knn;
    private Spinner requestBloodGroup_Range;
    private Spinner kValueSpinner;
    private TextView rangeValue;
    private Map<String, String> params = new HashMap<>();
    private Toolbar toolbar;
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Constructing the googleApIClient which can be used to for many services including Location.

        if (googleApiClient == null) {
            // ATTENTION: This "addApi(AppIndex.API)"was auto-generated to implement the App Indexing API.
            // See https://g.co/AppIndexing/AndroidStudio for more information.
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .addApi(AppIndex.API).build();
        }
        Log.d("Map", "Successfully built the googleApiClient");
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Log.d("Map", "mapFragement is ready");
        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Get the drawer layout.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();
        //Intialize the navigation view.
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.inflateHeaderView(R.layout.nav_header_sample);
        //Set the fields in the navHeadBar. Retrieve the values from the shared preferences.
        TextView navHeaderName = (TextView) headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderUserName = (TextView) headerView.findViewById(R.id.nav_header_username);
        StringBuilder name = new StringBuilder();
        name.append(prefs.getString("firstName", "firstName")).append(" ,").append(" "+prefs.getString("lastName", "lastName"));
        navHeaderName.setText(name.toString());
        navHeaderUserName.setText(prefs.getString("userName", "userName"));
        requestNewPermissions();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(120 * 1000)        // 240 seconds(4 minutes), in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        // TODO: 11/8/2016  Need to change the  frequency keeping the phone battery in mind.
        //The frequency would be same for updating the current registered users and current user.
        //Getting the widget references and onClickListeners are added for there to perform appropriate actions.
        Button requestButton_Knn = (Button) findViewById(R.id.requestButton_Knn);
        Button requestButton_Range = (Button) findViewById(R.id.requestButton_Range);
        Button requestBloodButton = (Button) findViewById(R.id.requestBloodButton);
        Button requestSubmitButtonKnn = (Button) findViewById(R.id.knnSubmit);
        Button requestSubmitButtonRange = (Button) findViewById(R.id.rangeSubmit);
        final LinearLayout requestButtonsSub = (LinearLayout) findViewById(R.id.requestButtons_sub);
        final RelativeLayout requestLayoutKnn = (RelativeLayout) findViewById(R.id.requestFormLayoutKnn);
        final RelativeLayout requestLayoutRange = (RelativeLayout) findViewById(R.id.requestFormLayoutRange);
        requestBloodButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("OnButtonClicked", "Came inside the request button onclick");
                if( requestButtonsSub.getVisibility() != View.VISIBLE )
                {
                    requestButtonsSub.setVisibility(View.VISIBLE);
                }
            }
        });
        requestButton_Knn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("OnButtonClicked", "RequestButton knn pressed");
                if( requestLayoutKnn.getVisibility() != View.VISIBLE )
                {
                    Log.d("onButtonClicked", "NOT VISIBLE, should be changed");
                    requestLayoutRange.setVisibility(View.INVISIBLE);
                    requestLayoutKnn.setVisibility(View.VISIBLE);
                }
            }
        });
        requestButton_Range.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("OnButtonClicked", "RequestButton range pressed");
                if( requestLayoutRange.getVisibility() != View.VISIBLE )
                {
                    Log.d("onButtonClicked", "NOT VISIBLE, should be changed");
                    requestLayoutKnn.setVisibility(View.INVISIBLE);
                    requestLayoutRange.setVisibility(View.VISIBLE);
                }
            }
        });
        SeekBar seekBar = (SeekBar)findViewById(R.id.rangeSeekBar);
        final TextView seekBarValue = (TextView)findViewById(R.id.textView_rangeValue);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                seekBarValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
        });
        requestSubmitButtonKnn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //queryType = "KnnQuery";
                params.clear();
                params.put("queryType", "KnnQuery");
                userId = prefs.getInt("userId", 0);
                params.put("userId", String.valueOf(userId));
                currentDate = new Date();
                params.put("currentDate", currentDate.toString());
                timestamp = currentDate.getTime();
                params.put("timeStamp", String.valueOf(timestamp));
                token = prefs.getString("token", "");
                params.put("token", token);
                status = "Waiting For Response";
                params.put("status", status);
                //Get values for kVal, emergencyValue, bloodGroupType,
                emergencyLevel_Knn = (RatingBar) findViewById(R.id.emergencyRating_KNN);
                requestBloodGroup_Knn = (Spinner) findViewById(R.id.requestBloodType_KNN);
                kValueSpinner = (Spinner) findViewById(R.id.requestkVal);
                //Extract the values.
                emergencyLevel = (int) emergencyLevel_Knn.getRating();
                params.put("emergencyLevel", String.valueOf(emergencyLevel));
                requestBloodGroup = requestBloodGroup_Knn.getSelectedItem().toString();
                params.put("bloodGroup", requestBloodGroup);
                String kValue = kValueSpinner.getSelectedItem().toString();
                kVal = Integer.parseInt(kValue);
                params.put("kVal", String.valueOf(kVal));
                String requestUrl = constructRequestUrl(params);
                new RequestBlood().execute(requestUrl);
                //these debugging statements should be displayed after the successful execution of the query.
                Log.d(TAG, "QUERY TYPE:" +queryType);
                Log.d(TAG, "User ID: "+userId);
                Log.d(TAG, "Current Date:" +currentDate);
                Log.d(TAG, "time stamp:" +timestamp);
                Log.d(TAG, "token:" +token);
                Log.d(TAG, "emergencyLevel:" +emergencyLevel);
                Log.d(TAG, "requestBloodGroup_Knn: " +requestBloodGroup);
                Log.d(TAG, "kValue: " +kVal);
                requestLayoutRange.setVisibility(View.INVISIBLE);
                requestLayoutKnn.setVisibility(View.INVISIBLE);
                requestButtonsSub.setVisibility(View.INVISIBLE);
            }
        });
        requestSubmitButtonRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                params.clear();
                params.put("queryType", "RangeQuery");
                userId = prefs.getInt("userId", 0);
                params.put("userId", String.valueOf(userId));
                currentDate = new Date();
                params.put("currentDate", currentDate.toString());
                // TODO: 11/24/2016 Parse the date to get more exact time Stamp.
                timestamp = currentDate.getTime();
                params.put("timeStamp", String.valueOf(timestamp));
                token = prefs.getString("token", "");
                params.put("token", token);
                status = "Waiting For Response";
                params.put("status", status);
                //Get values for kVal, emergencyValue, bloodGroupType,
                emergencyLevel_Range = (RatingBar) findViewById(R.id.emergencyRating_Range);
                requestBloodGroup_Range = (Spinner) findViewById(R.id.requestBloodType_Range);
                //Extract the values.
                emergencyLevel = (int) emergencyLevel_Range.getRating();
                params.put("emergencyLevel", String.valueOf(emergencyLevel));
                requestBloodGroup = requestBloodGroup_Range.getSelectedItem().toString();
                params.put("bloodGroup", requestBloodGroup);
                rangeValue = (TextView) findViewById(R.id.textView_rangeValue);
                rangeVal = Double.valueOf(rangeValue.getText().toString());
                params.put("rangeVal", String.valueOf(rangeVal));
                String requestUrl = constructRequestUrl(params);
                new RequestBlood().execute(requestUrl);
                Log.d(TAG, "Returned back to the listener method after async task");
                Log.d(TAG, "QUERY TYPE:" +queryType);
                Log.d(TAG, "User ID: "+userId);
                Log.d(TAG, "Current Date:" +currentDate);
                Log.d(TAG, "time stamp:" +timestamp);
                Log.d(TAG, "token:" +token);
                Log.d(TAG, "emergencyLevelKnn:" +emergencyLevel);
                Log.d(TAG, "requestBloodGroup_Rnage: " +requestBloodGroup);
                Log.d(TAG, "rangeValue: " +rangeVal);
                requestLayoutKnn.setVisibility(View.INVISIBLE);
                requestLayoutRange.setVisibility(View.INVISIBLE);
                requestButtonsSub.setVisibility(View.INVISIBLE);
            }
        });

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Intent nextIntent;
        if (id == R.id.profile) {
            nextIntent = new Intent(MapsActivity.this, MyProfile.class);
            startActivity(nextIntent);
        } else if (id == R.id.requests) {
            nextIntent = new Intent(MapsActivity.this, FragmentSample.class);
            startActivity(nextIntent);
        }
        else if (id == R.id.aboutUs) {
            nextIntent = new Intent(MapsActivity.this, AboutUs.class);
            startActivity(nextIntent);
        }
        else if (id == R.id.awaitingRequests) {
            nextIntent = new Intent(MapsActivity.this, AwaitingRequests.class);
            startActivity(nextIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void requestNewPermissions() {
        Log.d("requestNewPermissions", "Wait untill the permissions are granted");
        Log.d("Count value", Integer.toString(count));
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && count == 0)
        {
            //request for permissions
            Log.d("Location", "Ask for the permissions right now.");
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    PERMISSION_ACCESS_FINE_LOCATION);
            count++;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                Log.d("Permissions", "Got the permissions required for the App");
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // All good!
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                }

                break;
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker at the current location of the user.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d("Map", "Came to on Map ready function");
        Log.d("Map", "Map is ready");
        // Adds a  marker on to some default location when the map is ready.
        currentLocation = new LatLng(34.02187, -118.28);
        // TODO: 11/10/2016  this is the default location set just for the testing purpose. It will appeared
        //only once before clearing the mMap.
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Default Location"));
        //Setting the desired map type.
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        Log.d("Map", "First default location is marked on the map");
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
    }

    /**
     * Once the connection is established with the api client, we can get the location using the fusedLocationServce api which is
     * part of the Location Service api.
     *
     * @param connectionHint
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        try {
            Log.d("Map", "came to onConnected method, this should be after the onMapReady function");
            Log.d("Map", "trying to get the location of the current user");
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            Log.d("Map", "Got the location");
            if (lastLocation != null) {
                //Display the user location on the map as a marker.
                //Need to display on the same map.
                Log.d("Map", "Current location fetched is not null");
                LatLng curLoc = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
                //While getting the location for the first time after the connection is established or re-established.
                userMarkerOptions = new MarkerOptions().position(curLoc);
                userMarker = mMap.addMarker(userMarkerOptions.title("You're here"));
                //mMap.addMarker(userMarkerOptions.title("User current location fetched using the GPS"));
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
                Log.d("Map", "Location fetched is marked on the map");
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 14));
            } else {
                Log.d("Map", "Current location fetched is null, see what happened");
                Log.d("Map", "if this happened, there is possiblitiy that you missed out few permission to be asked in manifest");
                while( ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED ) {
                    requestNewPermissions();
                }
                LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, mLocationRequest, this);
            }
        } catch (SecurityException e) {
            Log.d("Exception", "Exception occured while fetchin the user location");
            e.printStackTrace();
        }
        Log.d("log", "Everything is fine before making the call");
        //This function call here ensures that all the user positions are marked onn the map.
        displayAllUserLocations();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("Connection Failed", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * This method over rides the parent onStart such that it connects to the googleApiClient only when activity is started.
     */
    @Override
    public void onStart() {
        super.onStart();
        Log.d("Map", "onstart method, connect to google Api client");
        if (googleApiClient == null) {
            Log.d("Map", "googleApiClient is null in onstart method");
        }
        googleApiClient.connect();
    }

    /**
     * This method disconnnects with the google Api client after the activity in ended.
     */
    @Override
    public void onStop() {
        Log.d("Map", "onstop method, Disconnected from the google Api Client");
        //TODO: 11/10/2016  see that this method is not crashing the app when user tries to go for another activity from here.
        if (googleApiClient != null && googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d("Map", "onResume method, connect to google Api client");
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    /**
     * Adds markers to the current list of users. It makes a call to the web service o fetch the list of
     * current users. The zoom level is set when the map is first created
     */
    public void displayAllUserLocations() {
        Log.d("Map", "Before calling the async task for fetching the user locations");
        new GetUserLocations().execute();
        return;
    }

    @Override
    public void onLocationChanged(Location location) {
        //Need to handle the method when the location is changed. In future, this method can be made more
        //complex to compare the updated location accuracy and then continue with the result.
        handleNewLocation(location);
    }

    private void handleNewLocation(Location location) {
        //Add a marker on the map which is  nothing but updating the current location.
        if( location == null )
        {
            Log.d("Failure", "Again failed to fetch the current user location");
        }
        LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
        Log.d("CLEAR", "About to clear all the existing markers");
        mMap.clear();
        Log.d("CLEAR", "Successfully cleared all markers");
        //If adding the current user marker for the first time.
        // if( userMarker == null || userMarkerOptions == null )
        //{
        userMarkerOptions = new MarkerOptions().position(currentLocation);
        userMarker = mMap.addMarker(userMarkerOptions.title("You're here"));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        Log.d("on location change", "marker added for current user");
        //}
        //else, we need to update the marker instead of adding a new marker at new location. This makes sure that there is
        //only marker for current user which is changed frequentlyas he moves his location.
        /** else
         {
         Log.d("on location change", "Position set for current user");
         userMarker.setPosition(currentLocation);
         }*/
        //U[pdate the current location of the user according to the frequency time set.
        String urls = constructQuery(location.getLatitude(), location.getLongitude());
        new UpdateUserLocation().execute(urls);
        Log.d("Asynctask", "Succesfully updated the user location in the database");
        Log.d("Success", "Successfully fetched the current user location");
        //Clear the map and set back the all the user locations.
        //mMap.clear();
        //The above call display markers according to the updated location of the users.
        new GetUserLocations().execute();
        //mMap.addMarker(new MarkerOptions().position(currentLocation).title("User current location fetched using the GPS through request"));
    }



    private class GetUserLocations extends AsyncTask<Void, Void, JSONArray> {
        ProgressDialog pdLoading = new ProgressDialog(MapsActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d(TAG,"Fetching all the user locations");
        }

        @Override
        protected JSONArray doInBackground(Void... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String urls = "http://192.168.42.76:8080/GiveAPint/getAllLocations";
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            JSONArray resultArray = new JSONArray();
            try
            {
                Log.d("log", "before making the url call to fetch the locations");
                Log.d("URL received", urls );
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                Log.d("log", "trying to get the output");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","got the output, before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                resultArray = new JSONArray(result.toString());
                Log.d("Result", result.toString());
            }
            catch(MalformedURLException e)
            {
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                Log.d("IOException",e.getMessage());
            }
            catch(JSONException  e)
            {
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
            return resultArray;
            //return;
        }

        @Override
        protected void onPostExecute(JSONArray result) {
            super.onPostExecute(result);
            Log.d("postexecute", result.toString());
            int userId = prefs.getInt("userId", 0);
            Log.d(TAG, "Got all the locations: see the userId stored in the prefs " +userId);
            try {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject item = (JSONObject) result.get(i);
                    // TODO: 11/8/2016  Need to see that the condition is working properly.
                    if( item.getInt("userid") !=  userId ) {
                        Log.d("Result", "New item");
                        Log.d("Result", Integer.toString(item.getInt("userid")));
                        Log.d("Result", Double.toString(item.getDouble("latCoord")));
                        Log.d("Result", Double.toString(item.getDouble("longCoord")));
                        //Adding markers on the map after fetching the user locations from the database
                        LatLng usersLocation = new LatLng(item.getDouble("latCoord"), item.getDouble("longCoord"));
                        mMap.addMarker(new MarkerOptions().position(usersLocation).title(Integer.toString(item.getInt("userid"))));
                        Log.d("Map", "this should change the marker positions");
                    }

                }
            }
            catch(JSONException e)
            {
                Log.d("Exception", "Exception occurred while parsing the result");
            }

        }

    }

    private class UpdateUserLocation extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pdLoading = new ProgressDialog(MapsActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            Log.d(TAG, "Updating the user location");
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String urls = params[0];
            Log.d(TAG, "Came inside the doInBackGround method");
            Log.d(TAG, "Url received as argument is: " +urls);
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            JSONObject resultObject = new JSONObject();
            try
            {
                Log.d("log", "before making the url call update call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","got the output, before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                resultObject = new JSONObject(result.toString());
                Log.d("Result RETURNEd", result.toString());
            }
            catch(MalformedURLException e)
            {
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                Log.d("IOException",e.getMessage());
                e.printStackTrace();
                Log.d("IOEXCEPTION", "Exception occured here");
            }
            catch(JSONException  e)
            {
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
            //return;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            try {
                if ( (result.getString("error").equals("")) ) {
                    pdLoading.setMessage("call success to the server");
                    Log.d("postexecute", result.toString());
                    Log.d("Post Execute", result.toString());
                    Log.d("Success", "Successfully updated the current user location");

                } else {
                    Log.d(TAG, "Error occurred while updating the user location");
                }
            }
            catch(JSONException e)
            {
                Log.d(TAG, "Exception occurred while updating the location");
            }

            pdLoading.dismiss();
        }

    }

    @NonNull
    private String constructQuery(Double newLatitude, Double newLongitude)
    {
        String token = prefs.getString("token", "");
        int userId = prefs.getInt("userId", 0);
        Log.d(TAG, "Triggered the construct query method:");
        Log.d(TAG, "Token and UserId retrieved from the prefs");
        Log.d(TAG, "userid: " +userId);
        Log.d(TAG, "token: "+token);
        StringBuilder result = new StringBuilder("http://192.168.42.76:8080/GiveAPint/updateLocation?");
        try {
            result.append(URLEncoder.encode("userid", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(userId), "UTF-8")).append("&")
                    .append(URLEncoder.encode("token", "UTF-8")).append("=").append(URLEncoder.encode(token, "UTF-8")).append("&")
                    .append(URLEncoder.encode("latCoord", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(newLatitude), "UTF-8")).append("&")
                    .append(URLEncoder.encode("longCoord", "UTF-8")).append("=").append(URLEncoder.encode(String.valueOf(newLongitude), "UTF-8"));
        }
        catch(UnsupportedEncodingException e)
        {
            Log.d("Exception", "Exception occurred while encoding the URL");
            e.printStackTrace();
        }
        Log.d("construct Query output", result.toString());
        return result.toString();
    }

    /**
     * Builds http url for making a request(Knn, Range). This is mapped to "requestBlood" controller method.
     * @param params contains all the parameters required for constructing the url.
     * @return url string.
     */
    @NonNull
    private String constructRequestUrl(Map<String, String> params)
    {
        StringBuilder result = new StringBuilder("http://192.168.42.76:8080/GiveAPint/requestBlood?");
        boolean first = true;
        try {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (first)
                    first = false;
                else
                    result.append("&");

                result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                result.append("=");
                result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
        }
        catch(UnsupportedEncodingException e)
        {
            Log.d("Exception", "Exception occurred while encoding the URL");
            e.printStackTrace();
        }
        Log.d("construct Query output", result.toString());
        return result.toString();
    }

    /**
     * Async call to request the blood, url to the service is passed as an argument and there is no difference in'
     * the implementation for the Knn and Range query as the only difference is the url passed.
     */
    private class RequestBlood extends AsyncTask<String, Void, JSONObject> {
        ProgressDialog pdLoading = new ProgressDialog(MapsActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("Sending request...");
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(String... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            pdLoading.show();
            String urls = params[0];
            Log.d(TAG, "Came inside the doInBackGround method");
            Log.d(TAG, "Url received as argument is: " +urls);
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            JSONObject resultObject = new JSONObject();
            try
            {
                Log.d("log", "before making the url call update call");
                url = new URL(urls);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                Log.d("log","got the output, before parsing the result");
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                resultObject = new JSONObject(result.toString());
                Log.d("Result RETURNEd", result.toString());
            }
            catch(MalformedURLException e)
            {
                Log.d("MalformedException",e.getMessage());
            }
            catch(IOException e)
            {
                Log.d("IOException",e.getMessage());
                e.printStackTrace();
                Log.d("IOEXCEPTION", "Exception occured here");
            }
            catch(JSONException  e)
            {
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
            //return;
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            // TODO: 11/25/2016 Check the error message and display the toast message succesfully.
            try {
                if ( (result.getString("error").equals("")) ) {
                    Toast.makeText(getApplicationContext(), "Request sent", Toast.LENGTH_SHORT).show();
                    Log.d("postexecute", result.toString());
                    Log.d("Post Execute", "Request successfully sent");
                    Log.d("Error Status", "Error value: " +result.getString("error"));

                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Error: "+result.getString("error"), Toast.LENGTH_SHORT).show();
                    Log.d("Error Status", "Error value: " +result.getString("error"));
                }
            }
            catch (JSONException e)
            {
                e.printStackTrace();
                Log.d(TAG, "Exception occurred while parsing the result");
            }
            pdLoading.dismiss();
        }

    }

}

