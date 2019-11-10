package com.droidmare.calendar.services;

import android.content.Intent;

import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.common.services.CommonUserData;

import java.lang.ref.WeakReference;

//User data receiver service declaration
//@author Eduardo on 22/05/2018.
public class UserDataService extends CommonUserData {

    public static final String TAG = UserDataService.class.getCanonicalName();

    public UserDataService() {
        super(TAG);
    }

    //A reference to the synchronization service:
    private static WeakReference<ApiSynchronizationService> syncServiceReference;
    public static void setSyncServiceReference(ApiSynchronizationService service) {
        syncServiceReference = new WeakReference<>(service);
    }

    @Override
    public void onHandleIntent(Intent dataIntent) {

        COMMON_TAG = TAG;

        super.onHandleIntent(dataIntent);

        stopSyncService();

        MainActivity.dataWasReset();
    }

    //Method that stops the ApiSynchronizationService after performing a logout:
    private void stopSyncService() {
        if (syncServiceReference != null && syncServiceReference.get() != null)
            syncServiceReference.get().stopSelf();
    }
}