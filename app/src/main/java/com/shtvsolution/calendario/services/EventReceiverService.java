package com.shtvsolution.calendario.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.utils.EventUtils;
import com.shtvsolution.calendario.views.activities.MainActivity;
import com.shtvsolution.database.publisher.EventsPublisher;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;

//Event receiver service (for receiving events externally) declaration
//@author Eduardo on 04/05/2018.

public class EventReceiverService extends IntentService {

    private static final String TAG = EventReceiverService.class.getCanonicalName();

    //A reference to the main activity, so when the app is running and an external event is added, the views can be updated instantly:
    private static WeakReference<MainActivity> mainActivityReference;
    public static void setMainActivityReference(MainActivity activity) {
        mainActivityReference = new WeakReference<>(activity);
    }

    public static boolean eventReceived = false;

    public EventReceiverService() {
        super("EventReceiverService");
    }

    @Override
    public void onHandleIntent(Intent eventIntent) {

        UserDataReceiverService.readSharedPrefs(this);

        Log.e("myCalendarTag", "Event Received " + UserDataReceiverService.getUserId());

        eventReceived = true;

        int numberOfEvents = eventIntent.getIntExtra("numberOfEvents", -1);

        JSONObject[] eventJsons = new JSONObject[numberOfEvents];

        EventListItem[] retrievedEvents = new EventListItem[numberOfEvents];

        for (int i = 0, j = 0; i < numberOfEvents; i++) {

            try {
                eventJsons[i] = new JSONObject(eventIntent.getStringExtra("event" + i));
                if (eventJsons[i].getInt("intervalTime") != 0 && eventJsons[i].getLong("eventStartDate") >= eventJsons[i].getLong("eventStopDate"))
                    eventJsons[i].put("eventStopDate", -1);
                Log.e("Received event", eventJsons[i].toString());
            } catch (JSONException jse) {
                Log.e(TAG, "onHandelIntent. JSONException: " + jse.getMessage());
            }

            if (eventJsons[i] != null) {
                //Once a JSON has been retrieved from the received intent, it can be converted into an event:
                EventListItem event = EventUtils.makeEvent(this, eventJsons[i], true);
                if (event != null) retrievedEvents[j++] = event;
            }
        }

        //The context used for publishing the events (if the application is running in foreground the context will be the MainActivity so the views can be updated):
        Context contextForPublishing;

        if (MainActivity.isCreated()) contextForPublishing = mainActivityReference.get();

        else contextForPublishing = this;

        EventsPublisher.publishEvents(contextForPublishing, retrievedEvents);

        //The service must wait before stopping itself so the events can be saved on the database:
        while (eventReceived) try {
            Log.d("TESTING", "Storing received events...");
            Thread.sleep(500);
        } catch (InterruptedException ie) {
            Log.e(TAG, "onHandelIntent. InterruptedException: " + ie.getMessage());
        }

        Log.d("TESTING", "All received events stored");

        stopSelf();
    }
}