package com.example.mamanoha.bloodconnection.Service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;


import com.example.mamanoha.bloodconnection.app.Config;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

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
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        storeRegIdInPref(refreshedToken);
        //String userName = preferences.getString("userName", "");
        // sending reg id to your server
        //sendRegistrationToServer(userName, refreshedToken);

        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(Config.REGISTRATION_COMPLETE);
        registrationComplete.putExtra("token", refreshedToken);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    private void storeRegIdInPref(String token) {
        Log.d("Registration Id:", "Registration completed for the user");
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("regId", token);
        Log.d("Registration id:", token);
        Log.d("Shared Preferences", "Successfully stored the regid in the shared preferences");
        editor.commit();
    }
}