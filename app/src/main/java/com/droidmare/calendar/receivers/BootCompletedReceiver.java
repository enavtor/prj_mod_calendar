package com.droidmare.calendar.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidmare.calendar.services.ApiSynchronizationService;
import com.droidmare.calendar.services.AlarmResetService;
import com.droidmare.common.utils.ServiceUtils;

//A broadcast receiver for boot completed event
//@author Eduardo on 14/11/2018.

public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = BootCompletedReceiver.class.getCanonicalName();

    @Override
    public void onReceive(Context context, Intent bootIntent) {

        if (bootIntent.getAction() != null && bootIntent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {

            Log.d(TAG, "Boot completed intent received");

            Intent servicesIntent = new Intent();

            //The alarm reset service is started (it is better to perform this operation in a service, though it could be directly performed here):
            servicesIntent.setComponent(new ComponentName(context.getPackageName(), AlarmResetService.class.getCanonicalName()));
            ServiceUtils.startService(context, servicesIntent);

            //The api synchronization service is started and will be running until the device is turned off:
            servicesIntent.setComponent(new ComponentName(context.getPackageName(), ApiSynchronizationService.class.getCanonicalName()));
            if (!ApiSynchronizationService.isRunning()) ServiceUtils.startService(context, servicesIntent);
        }
    }
}

