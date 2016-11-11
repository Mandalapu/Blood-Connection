package com.example.mamanoha.bloodconnection;

import android.app.ProgressDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.DoubleBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleMap mMap;
    private GoogleApiClient googleApiClient;
    private LatLng currentLocation;
    private Location lastLocation;
    private LocationRequest mLocationRequest;
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private final int PERMISSION_ACCESS_FINE_LOCATION = 1;
    private MarkerOptions userMarkerOptions;
    private Marker userMarker;
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Constructing the googleApIClient which can be used to for many services including Location.
        // TODO: 11/10/2016  This is very very important, userid is set to be "1" which should be changed after the first three
        //activities are connected.
        UserInformation.setUserId(1);
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
        requestNewPermissions();
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(60 * 1000)        // 600 seconds(10 minutes), in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        // TODO: 11/8/2016  Need to change the  frequency keeping the phone battery in mind.
        //The frequency would be same for updating the current registered users and current user.

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
        new UpdateUserLocation().execute(location.getLatitude(), location.getLongitude());
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
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("connecting to server...");
            pdLoading.show();
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
            pdLoading.setMessage("call success to the server");
            Log.d("postexecute", result.toString());
            try {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject item = (JSONObject) result.get(i);
                    // TODO: 11/8/2016  Need to add a if condition to not add a duplicate marker for the current user, as his location
                    // location is updated and marked seperately.
                    if( item.getInt("userid") != UserInformation.getUserId() ) {
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
            //this method will be running on UI thread
            pdLoading.dismiss();
        }

    }

    private class UpdateUserLocation extends AsyncTask<Double, Void, JSONObject> {
        ProgressDialog pdLoading = new ProgressDialog(MapsActivity.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //this method will be running on UI thread
            Log.d("log","inside the preexecute method");
            pdLoading.setMessage("connecting to server...");
            pdLoading.show();
        }

        @Override
        protected JSONObject doInBackground(Double... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here
            String urls = "http://192.168.42.76:8080/GiveAPint/updateLocation?";
            JSONArray response = null;
            HttpURLConnection urlConnection = null;
            URL url = null;
            StringBuilder result = new StringBuilder();
            String line;
            JSONObject resultObject = new JSONObject();
            double newLatitude = params[0];
            double newLongitude = params[1];
            Log.d("current Latitude", Double.toString(newLatitude));
            Log.d("Current Longitude", Double.toString(newLongitude));
            try
            {
                Map<String, String> mapParams = new HashMap<>();
                // TODO: 11/8/2016 Make sure that the hard coded values are replaced by the userInformation class variables.
                mapParams.put("userid", Integer.toString(1));
                mapParams.put("token", "5po6gr6lorf48");
                mapParams.put("latCoord", Double.toString(newLatitude));
                mapParams.put("longCoord", Double.toString(newLongitude));
                Log.d("New call", "created the map params for updating the user location");
                urls = constructQuery(urls, mapParams);
                Log.d("log", "before making the url call update call");
                Log.d("URL construcuted", urls);
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


        @NonNull
        private String constructQuery(String url, Map<String, String> params)
        {
            StringBuilder result = new StringBuilder();
            result.append(url);
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
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
            pdLoading.setMessage("call success to the server");
            Log.d("postexecute", result.toString());
            Log.d("Post Execute", result.toString());
            Log.d("Success", "Successfully updated the current user location");
            /**try {
                for (int i = 0; i < result.length(); i++) {
                    JSONObject item = (JSONObject) result.get(i);
                    // TODO: 11/8/2016  Need to add a if condition to not add a duplicate marker for the current user, as his location
                    // location is updated and marked seperately.
                    //if( item.getInt("userid") != UserInformation.getUserId() )
                    //{
                    Log.d("Result", "New item");
                    Log.d("Result", Integer.toString(item.getInt("userid")));
                    Log.d("Result", Double.toString(item.getDouble("latCoord")));
                    Log.d("Result", Double.toString(item.getDouble("longCoord")));
                    //Adding markers on the map after fetching the user locations from the database
                    LatLng usersLocation = new LatLng(item.getDouble("latCoord"), item.getDouble("longCoord"));
                    mMap.addMarker(new MarkerOptions().position(usersLocation).title(Integer.toString(item.getInt("userid"))));
                    Log.d("Map", "Location fetched is marked on the map");
                    // }


                }
            }
            catch(JSONException e)
            {
                Log.d("Exception", "Exception occurred while parsing the result");
            }**/
            //this method will be running on UI thread
            pdLoading.dismiss();
        }

    }
}

