package com.shtvsolution.calendario.services;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.shtvsolution.database.publisher.EventsPublisher;

//Service that will reset all the alarms on device reboot for the stored events:
//@author Eduardo on 14/11/2018.

public class AlarmResetService extends IntentService {

    private static final String TAG = AlarmResetService.class.getCanonicalName();

    public static boolean resettingEvents = false;

    public AlarmResetService() {
        super("AlarmResetService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        resettingEvents = true;

        UserDataReceiverService.readSharedPrefs(this);

        EventsPublisher.retrieveAndReset(this);

        while (resettingEvents) {

            Log.d("TESTING", "resetting events...");

            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Log.e(TAG, "onHandelIntent. InterruptedException: " + ie.getMessage());
            }
        }

        Log.d("TESTING", "Events reset");
    }
}