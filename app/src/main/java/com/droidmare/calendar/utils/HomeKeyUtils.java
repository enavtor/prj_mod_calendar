package com.droidmare.calendar.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

//Utils for listening to actions over the home key
//@author Eduardo on 02/04/2019.

public class HomeKeyUtils {

    private Context context;

    private IntentFilter filter;

    private OnHomePressedListener homeKeyListener;

    private hemeKeyReceiver homeKeyReceiver;

    public HomeKeyUtils(Context context) {
        this.context = context;
        filter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    //Method that sets the listener:
    public void setOnHomePressedListener(OnHomePressedListener listener) {
        homeKeyListener = listener;
        homeKeyReceiver = new hemeKeyReceiver();
    }

    //Method that starts the listener:
    public void startWatch() {
        if (homeKeyReceiver != null)  context.registerReceiver(homeKeyReceiver, filter);
    }

    //Method that stops the listener:
    public void stopWatch() {
        if (homeKeyReceiver != null)  context.unregisterReceiver(homeKeyReceiver);
    }

    //The broadcast receiver that will register any action over the home key button:
    class hemeKeyReceiver extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action != null && action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

                if (reason != null && homeKeyListener != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) homeKeyListener.onHomePressed();

                    else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) homeKeyListener.onHomeLongPressed();
                }
            }
        }
    }

    //This interface shall be implemented on the MainActivity:
    public interface OnHomePressedListener {
        void onHomePressed();
        void onHomeLongPressed();
    }
}