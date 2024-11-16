package com.example.bluetoothtextsender;
import org.json.JSONObject;
import android.content.pm.PackageManager;


import android.graphics.Bitmap;
import android.media.session.MediaSession;
import android.os.Bundle;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class NotificationListener extends NotificationListenerService {

    private static final String TAG = "NotificationListener";
    private OutputStream outputStream;  // This should be connected to your microcontroller

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Capture notification details
        String appPackageName = sbn.getPackageName();
        android.app.Notification notification = sbn.getNotification();
        String appName = "Not Found";
        String mapText = "Not Found";

        try {
            // Get the package manager and try to retrieve the application label (name)
            PackageManager packageManager = getPackageManager();
            appName = packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(appPackageName, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            appName = "Unknown App";
            Log.e(TAG, "App not found: " + appPackageName);
        }

        // Get the extras from the notification
        android.os.Bundle extras = notification.extras;
        // Log and iterate through the extras to check for any text-related fields
        StringBuilder logData = new StringBuilder();
        logData.append("Extras:\n");

        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            // If a text-related key is found, assign its value to mapText
            if (key.equals("android.text")) {
                mapText = (value != null) ? value.toString() : "No main text available";
            }
            // Log each key-value pair
            if (value != null) {
                logData.append(key).append(" : ").append(value.toString()).append("\n");
            } else {
                logData.append(key).append(" : null\n");
            }
        }
        Bundle events = sbn.getNotification().extras;
        String title = getNotificationExtra(events, "android.title");
        String text = getNotificationExtra(events, "android.text");
        String subText = getNotificationExtra(events, "android.subText");
        String bigText = getNotificationExtra(events, "android.bigText");


       /* // Extract title and text from notification extras
        String title = sbn.getNotification().extras.getString("android.title");
        String text = sbn.getNotification().extras.getString("android.text");

        String subText = sbn.getNotification().extras.getString("android.subText");
        String bigText = sbn.getNotification().extras.getString("android.bigText");*/

        // Concatenate available fields for sending
        StringBuilder combinedText = new StringBuilder();
        if (title != null) combinedText.append("Title: ").append(title).append("\n");
        if (text != null) combinedText.append("Text: ").append(text).append("\n");
        if (subText != null) combinedText.append("SubText: ").append(subText).append("\n");
        if (bigText != null) combinedText.append("BigText: ").append(bigText);

        //String navigationInstructions = extractNavigationInstructions(sbn);

        String message = combinedText.toString();

        // Log all fields for debugging

        // Log the values
        Log.d(TAG, "App Name: " + appName);
        Log.d(TAG, "Title: " + title);
        Log.d(TAG, "Text: " + text);
        Log.d(TAG, "MapText: " + mapText);
        Log.d(TAG, "Notification Content: " + message);

        Map<String, String> appInfo = new HashMap<>();
        appInfo.put("App Name", appName);
        appInfo.put("Title", title);
        appInfo.put("Text", text);
        appInfo.put("MapText", mapText);

        sendAppNameToMicrocontroller(appInfo);
        // Send the app name to the microcontroller
        //sendAppNameToMicrocontroller(appName);
    }
    private void sendAppNameToMicrocontroller(Map<String, String> appInfo) {
        try {

            // Convert the map to a JSON string
            JSONObject jsonObject = new JSONObject(appInfo);
            String jsonString = jsonObject.toString();


            OutputStream outputStream = BluetoothManager.getInstance().getOutputStream();
            if (outputStream != null) {
                outputStream.write(jsonString.getBytes());
                Log.d(TAG, "Sent app name to microcontroller: " + jsonString);
            } else {
                Log.e(TAG, "OutputStream is null, cannot send data.");
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to send app name to microcontroller", e);
        }
    }
    private String getNotificationExtra(Bundle extras, String key) {
        if (extras.containsKey(key)) {
            return extras.getString(key);
        }
        return "Not available";
    }
    /*@Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Extract the notification object
        android.app.Notification notification = sbn.getNotification();

        // Get the extras from the notification
        android.os.Bundle extras = notification.extras;

        // Variable to hold text data
        String mapText = "No main text available";

        // Log and iterate through the extras to check for any text-related fields
        StringBuilder logData = new StringBuilder();
        logData.append("Extras:\n");

        // Iterate through all keys in extras
        for (String key : extras.keySet()) {
            Object value = extras.get(key);

            // If a text-related key is found, assign its value to mapText
            if (key.equals("android.text")) {
                mapText = (value != null) ? value.toString() : "No main text available";
            }

            // Log each key-value pair
            if (value != null) {
                logData.append(key).append(" : ").append(value.toString()).append("\n");
            } else {
                logData.append(key).append(" : null\n");
            }
        }

        // Log the final data
        Log.d(TAG, "Notification Details from Extras:\n" + logData.toString());

        // Log the mapText that was assigned in the loop
        Log.d(TAG, "MapText: " + mapText);

        // Send the mapText to the microcontroller or handle it further
        sendAppNameToMicrocontroller(logData.toString());
    }*/

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }
}
