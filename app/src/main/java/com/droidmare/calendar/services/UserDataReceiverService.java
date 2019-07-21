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

    private static int userId = -1;
    private static String userName;
    private static String avatarImage;

    public UserDataReceiverService() { super("UserDataReceiverService"); }

    @Override
    public void onHandleIntent(Intent eventIntent) {

        Log.d(TAG, "onHandleIntent");

        userId = eventIntent.getIntExtra("userId", -1);
        userName = eventIntent.getStringExtra("userName");
        avatarImage = eventIntent.getStringExtra("avatarString");

        Log.d(TAG, "onHandleIntent " + userId);

        writeSharedPrefs();
    }

    private void writeSharedPrefs() {
        Log.d(TAG, "writeSharedPrefs");

        SharedPreferences.Editor editor = getSharedPreferences(USERDATA_PREFS, MODE_PRIVATE).edit();
        editor.putInt("id_userId", userId);
        editor.putString("id_userName", userName);
        editor.putString("id_avatarImage", avatarImage);

        editor.apply();
    }

    public static void readSharedPrefs(Context context) {
        Log.d(TAG, "readSharedPrefs");

        SharedPreferences prefs = context.getSharedPreferences(USERDATA_PREFS, MODE_PRIVATE);

        int pref_id_userId = prefs.getInt("id_userId", -1);
        String pref_id_userName = prefs.getString("id_userName", null);
        String pref_id_avatarImage = prefs.getString("id_avatarImage", null);

        if (pref_id_userId != -1 && pref_id_userName != null && pref_id_avatarImage != null) {
            userId = pref_id_userId;
            userName = pref_id_userName;
            avatarImage = pref_id_avatarImage;
        }
    }

    //Method that returns the user id:
    public static int getUserId() { return userId; }

    //Method that returns the user name:
    public static String getUserName() { return userName; }

    //Method that returns the user avatar:
    public static Bitmap getAvatarImage() { return ImageUtils.decodeBitmapString(avatarImage); }

    //Method that resets the user parameters values (logout):
    public static void resetUser() {
        userId = -1;
        userName = null;
        avatarImage = null;
    }
}