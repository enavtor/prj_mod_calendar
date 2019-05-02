package com.droidmare.calendar.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

//User data receiver service declaration
//@author Eduardo on 22/05/2018.

public class UserDataReceiverService extends IntentService {

    public static final String USERDATA_PREFS = "userDataPrefsFile";
    private static final String TAG = UserDataReceiverService.class.getCanonicalName();

    private static int userId = -1;
    private static String userName;
    private static String avatarUri;
    private static Bitmap avatarImage;
    private static String[] measures;
    private static String freeMeasure;

    private static int pref_id_userId;
    private static String pref_id_userName;
    private static String pref_id_avatarUri;
    private static String pref_id_avatarImage;
    private static String[] pref_id_measures;
    private static String pref_id_freeMeasure;

    public UserDataReceiverService() {
        super("UserDataReceiverService");
    }
    @Override
    public void onHandleIntent(Intent eventIntent) {

        //Log.d("myTag", "onHandleIntent");

        userId = eventIntent.getIntExtra("userId", -1);
        userName = eventIntent.getStringExtra("userName");
        avatarUri = eventIntent.getStringExtra("avatarUri");
        measures = eventIntent.getStringArrayExtra("userMeasures");
        freeMeasure = eventIntent.getStringExtra("freeMeasureName");

        downloadAvatarBitmap();

        Log.d("myCalendarTag", "onHandleIntent " + userId);

        writeSharedPrefs(userId, userName, avatarUri, avatarImage, measures);
    }

    public static void readSharedPrefs(Context context) {
        //Log.d("myTag", "readSharedPrefs");

        SharedPreferences prefs = context.getSharedPreferences(USERDATA_PREFS, MODE_PRIVATE);

        pref_id_userId = prefs.getInt("id_userId", -1);
        pref_id_userName = prefs.getString("id_userName", null);
        pref_id_avatarUri = prefs.getString("id_avatarUri", null);
        pref_id_avatarImage = prefs.getString("id_avatarImage", null);

        HashSet <String> auxSet = (HashSet<String>) prefs.getStringSet("id_measures", new HashSet<String>());

        pref_id_measures = new String[auxSet.size()];
        pref_id_freeMeasure = prefs.getString("id_freeMeasure", null);

        int i = 0;

        for (String measure : auxSet) pref_id_measures[i++] = measure;

        if (pref_id_userId != -1 && pref_id_userName != null && pref_id_avatarUri != null) {
            userId = pref_id_userId;
            userName = pref_id_userName;
            avatarUri = pref_id_avatarUri;
            avatarImage = StringToBitMap(pref_id_avatarImage);
            measures = pref_id_measures;
            freeMeasure = pref_id_freeMeasure;
        }
    }

    private void writeSharedPrefs(int userId,String userName,String avatarUri, Bitmap avatarImage, String[] measures) {
        //Log.d("myTag", "writeSharedPrefs");

        SharedPreferences.Editor editor = getSharedPreferences(USERDATA_PREFS, MODE_PRIVATE).edit();
        editor.putInt("id_userId", userId);
        editor.putString("id_userName", userName);
        editor.putString("id_avatarUri", avatarUri);
        editor.putString("id_avatarImage", BitMapToString(avatarImage));

        Set <String> measureSet = new HashSet <> ();
        measureSet.addAll(Arrays.asList(measures));
        editor.putStringSet("id_measures", measureSet);
        editor.putString("id_freeMeasure", freeMeasure);

        editor.apply();
    }


    //Method that downloads and stores the current user's avatar image:
    private void downloadAvatarBitmap () {
        try {
            InputStream response = new java.net.URL(avatarUri).openStream();
            avatarImage = BitmapFactory.decodeStream(response);
        } catch (MalformedURLException urle) {
            Log.e(TAG,"downloadAvatarBitmap. MalformedURLException: " + urle.getMessage());
        }catch (IOException ioe) {
            Log.e(TAG, "downloadAvatarBitmap. IOException: " + ioe.getMessage());
        }
    }

    //Method that returns the user id:
    public static int getUserId() { return userId; }

    //Method that returns the user name:
    public static String getUserName() { return userName; }

    //Method that returns the user avatar:
    public static Bitmap getAvatarImage() { return avatarImage; }

    //Method that returns the free measure name:
    public static String getFreeMeasure() { return freeMeasure; }

    //Method that resets the user parameters values (logout):
    public static void resetUser() {
        userId = -1;
        userName = null;
        avatarUri = null;
        avatarImage = null;
        measures = null;
        freeMeasure = null;
    }

    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.getMessage();
            return null;
        }
    }

    public static boolean hasAnyMeasure () {
        return (measures != null && measures.length > 0);
    }

    public static boolean hasMeasure (String measure) {

        if (hasAnyMeasure()) {
            for (String auxMeasure : measures)
                if (auxMeasure.equals(measure))
                    return true;
        }

        return false;
    }
}