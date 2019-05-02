package com.droidmare.calendar.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

//Api url receiver service declaration
//@author Eduardo on 17/07/2018.

public class ApiReceiverService extends IntentService {

    public static final String API_PREFS = "userDataPrefsFile";

    private static final String TAG = ApiReceiverService.class.getCanonicalName();

    private static String API_URL;

    public ApiReceiverService() {
        super("ApiReceiverService");
    }

    @Override
    public void onHandleIntent(Intent eventIntent) {

        Log.d(TAG, "onHandleIntent");

        API_URL = eventIntent.getStringExtra("apiUrl");

        writeSharedPrefs(API_URL);
    }

    private static void readSharedPrefs(Context context) {

        Log.d(TAG, "readSharedPrefs");

        SharedPreferences prefs = context.getSharedPreferences(API_PREFS, MODE_PRIVATE);

        String pref_id_api = prefs.getString("id_api", null);

        if (pref_id_api != null) API_URL = pref_id_api;
    }

    private void writeSharedPrefs(String apiUrl) {

        Log.d(TAG, "writeSharedPrefs");

        SharedPreferences.Editor editor = getSharedPreferences(API_PREFS, MODE_PRIVATE).edit();

        editor.putString("id_api", apiUrl);

        editor.apply();
    }

    //Method that returns the user id:
    public static String getApiUrl(Context context) {

        if (API_URL == null) readSharedPrefs(context);

        return API_URL;
    }
}