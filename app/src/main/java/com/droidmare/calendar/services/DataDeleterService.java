package com.droidmare.calendar.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.droidmare.calendar.views.activities.MainActivity;

import java.lang.ref.WeakReference;

//App's data deleter service declaration
//@author Eduardo on 24/05/2018.

public class DataDeleterService extends IntentService {

    private static String TAG = DataDeleterService.class.getCanonicalName();

    public static boolean dataResetPerformed = false;

    public DataDeleterService() {
        super("DataDeleterService");
    }

    @Override
    public void onHandleIntent(Intent eventIntent) {

        startService(new Intent(getApplicationContext(), UserDataService.class));

        /*EventsPublisher.resetData(getApplicationContext());

        //This thread must be alive until the data reset process is completed, since the Async Tasks' execution depends on the thread that launched them:
        while (!dataResetPerformed) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException interruptedException) {
                Log.e(TAG, "onHandleIntent. InterruptedException: " + interruptedException.getMessage());
            }
        }*/

        Log.d(TAG, "Hoolop");

        //ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
        //if (activityManager != null) activityManager.clearApplicationUserData();
    }
}