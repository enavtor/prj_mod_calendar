package com.droidmare.calendar.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Log;

import com.droidmare.calendar.utils.ImageUtils;

//User data receiver service declaration
//@author Eduardo on 22/05/2018.

public class UserDataReceiverService extends IntentService {

    public static final String USERDATA_PREFS = "userDataPrefsFile";

    private static final String TAG = UserDataReceiverService.class.getCanonicalName();

    private static String userId;
    private static String userName;
    private static String avatarImage;

    public UserDataReceiverService() { super("UserDataReceiverService"); }

    @Override
    public void onHandleIntent(Intent eventIntent) {

        Log.d(TAG, "onHandleIntent");

        userId = eventIntent.getStringExtra("userId");
        userName = eventIntent.getStringExtra("userName");
        avatarImage = eventIntent.getStringExtra("avatarString");

        Log.d(TAG, "onHandleIntent " + userId);

        writeSharedPrefs();
    }

    private void writeSharedPrefs() {
        Log.d(TAG, "writeSharedPrefs");

        SharedPreferences.Editor editor = getSharedPreferences(USERDATA_PREFS, MODE_PRIVATE).edit();
        editor.putString("id_userId", userId);
        editor.putString("id_userName", userName);
        editor.putString("id_avatarImage", avatarImage);

        editor.apply();
    }

    public static void readSharedPrefs(Context context) {
        Log.d(TAG, "readSharedPrefs");

        SharedPreferences prefs = context.getSharedPreferences(USERDATA_PREFS, MODE_PRIVATE);

        String pref_id_userId = prefs.getString("id_userId", null);
        String pref_id_userName = prefs.getString("id_userName", null);
        String pref_id_avatarImage = prefs.getString("id_avatarImage", null);

        if (pref_id_userId != null && pref_id_userName != null && pref_id_avatarImage != null) {
            userId = pref_id_userId;
            userName = pref_id_userName;
            avatarImage = pref_id_avatarImage;
        }
    }

    //Method that returns the user id:
    public static String getUserId() { return userId; }

    //Method that returns the user name:
    public static String getUserName() { return userName; }

    //Method that returns the user avatar:
    public static Bitmap getAvatarImage() { return ImageUtils.decodeBitmapString(avatarImage); }

    //Method that resets the user parameters values (logout):
    public static void resetUser() {
        userId = null;
        userName = null;
        avatarImage = null;
    }
}