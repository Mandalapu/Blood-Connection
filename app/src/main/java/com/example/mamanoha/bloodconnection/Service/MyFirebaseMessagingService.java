package com.example.mamanoha.bloodconnection.Service;

import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Manu on 11/12/2016.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage == null)
            return;

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Notification Body: " + remoteMessage.getNotification().getBody());
            handleNotification(remoteMessage.getNotification().getBody());
        }

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Data Payload: " + remoteMessage.getData().toString());

            try {
                JSONObject json = new JSONObject(remoteMessage.getData().toString());
                handleDataMessage(json);
            } catch (Exception e) {
                Log.d(TAG, "Exception: " + e.getMessage());
            }
        }
    }

    private void handleNotification(String message) {
    }

    private void handleDataMessage(JSONObject json) {
        Log.d(TAG, "push json: " + json.toString());

        try {
            JSONObject data = json.getJSONObject("data");
            String title = data.getString("title");
            String message = data.getString("message");
            boolean isBackground = data.getBoolean("is_background");
            String imageUrl = data.getString("image");
            String timestamp = data.getString("timestamp");
            JSONObject payload = data.getJSONObject("payload");

            Log.d(TAG, "title: " + title);
            Log.d(TAG, "message: " + message);
            Log.d(TAG, "isBackground: " + isBackground);
            Log.d(TAG, "payload: " + payload.toString());
            Log.d(TAG, "imageUrl: " + imageUrl);
            Log.d(TAG, "timestamp: " + timestamp);
        }
        catch(JSONException e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception occurred while parsing the JSON data");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            Log.d(TAG, "Exception occured inside the handle data message");
        }
    }

}
