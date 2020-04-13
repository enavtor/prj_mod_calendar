package com.droidmare.calendar.services;

import android.content.Intent;
import android.util.Log;

import com.droidmare.database.publisher.EventsPublisher;
import com.droidmare.common.services.CommonIntentService;

//Service that will reset all the alarms on device reboot for the stored events:
//@author Eduardo on 14/11/2018.
public class AlarmResetService extends CommonIntentService {

    public static boolean resettingEvents = false;

    public AlarmResetService() {
        super(AlarmResetService.class.getCanonicalName());
    }

    @Override
    public void onHandleIntent(Intent intent) {

        COMMON_TAG = getClass().getCanonicalName();

        super.onHandleIntent(intent);

        UserDataService.readSharedPrefs(getApplicationContext(), UserDataService.TAG);

        EventsPublisher.retrieveAndReset(this);

        //Since the retrieveAndReset method is executed on an AsyncTask, this thread must be kept alive
        //until it finishes, because teh AsyncTask thread fully depends on the one that launched it:
        while (resettingEvents) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Log.e(COMMON_TAG, "onHandelIntent. InterruptedException: " + ie.getMessage());
            }
        }

        Log.d(COMMON_TAG, "All alarms reset");
    }
}