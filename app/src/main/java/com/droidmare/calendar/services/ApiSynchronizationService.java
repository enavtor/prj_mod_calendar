package com.droidmare.calendar.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.droidmare.calendar.models.EventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.utils.NetworkUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.database.publisher.EventsPublisher;

import org.json.JSONArray;
import org.json.JSONException;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

//Service that synchronizes the events between the STB and the API:
//@author Eduardo on 08/11/2018.

public class ApiSynchronizationService extends Service {

    private static final String TAG = ApiSynchronizationService.class.getCanonicalName();

    public static final String LOCAL_SYNC_OP_SAVE = "save";
    public static final String LOCAL_SYNC_OP_EDIT = "edit";
    public static final String LOCAL_SYNC_OP_DELETE = "delete";

    private static boolean isRunning = false;

    private EventListItem[] retrievedEvents;

    private SQLiteManager database;

    private String urlForRetrieving;

    //A reference to the main activity:
    private static WeakReference<MainActivity> mainActivityReference;

    public static void setMainActivityReference(MainActivity activity) {
        mainActivityReference = new WeakReference<>(activity);
    }

    @Override
    public void onCreate() {

        super.onCreate();

        isRunning = true;

        database = new SQLiteManager(this, SQLiteManager.DATABASE_NAME, null, SQLiteManager.DATABASE_VERSION);

        UserDataReceiverService.readSharedPrefs(this);

        urlForRetrieving = ApiConnectionService.BASE_URL + "event/" + UserDataReceiverService.getUserId();

        startSyncTimer();
    }

    //Method that starts the timer for resynchronizing the events every 5 minutes:
    private void startSyncTimer() {

        //The resynchronization attempt will take place every 5 minutes:
        long resyncPeriod = DateUtils.minutesToMillis(5);

        Timer resyncTimer = new Timer("resyncTimer");

        resyncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //The sync won't start until a connection is available:
                while (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {

                    Log.d(TAG, "Waiting for a connection...");

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Log.e(TAG, "onHandelIntent. InterruptedException: " + ie.getMessage());
                    }
                }

                Log.e(TAG, "Resynchronization schedule started");

                retrieveAndSyncApiEvents();
            }
        }, 0, 30000);
    }

    @Override
    public void onDestroy() {
        database.close();
        isRunning = false;
        super.onDestroy();
    }

    public static boolean isRunning() {
        return isRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //This method retrieves all the events from the Api and starts the synchronization function:
    private void retrieveAndSyncApiEvents() {

        String response = ApiConnectionService.sendRequest(null, urlForRetrieving, ApiConnectionService.REQUEST_METHOD_GET);

        Log.d(TAG, "Events retrieved from API: " + response);

        try {
            JSONArray eventsJson = new JSONArray(response);

            int numberOfRetrieved = eventsJson.length();

            retrievedEvents = new EventListItem[numberOfRetrieved];

            for (int i = 0; i < numberOfRetrieved; i++) {

                EventJsonObject eventJson = EventJsonObject.createEventJson(eventsJson.get(i).toString());

                if (eventJson.getInt(EventUtils.EVENT_REP_INTERVAL_FIELD) != 0 && eventJson.getLong(EventUtils.EVENT_START_DATE_FIELD) >= eventJson.getLong(EventUtils.EVENT_REPETITION_STOP_FIELD))
                    eventJson.put(EventUtils.EVENT_REPETITION_STOP_FIELD, -1);

                retrievedEvents[i] = EventUtils.makeEvent(getApplicationContext(), eventJson);
            }

            synchronizeEvents();

        } catch (JSONException jse) {
            Log.e(TAG, "retrieveAndSyncApiEvents. JSONException: " + jse.getMessage());
        }
    }

    //This method checks all the retrieved events, comparing them to the ones stored in the database and performing all the necessary synchronization operations:
    private void synchronizeEvents() {

        Log.e(TAG, "Event synchronization started");

        //First of all, all the local events are retrieved:
        EventJsonObject[] localEventsJson = database.getEvents(-1, -1, true, true, false);
        EventListItem[] localEvents = EventUtils.jsonArrayToEventArray(this, localEventsJson);

        //When a local event is extracted from the array because its api id matches the retrieved one's, it is deleted from the array by putting the last array's
        //element into its position and setting the last occupied position's value to null, so it is necessary to store and modify the actual local last index:
        int localLastIndex = 0;
        if (localEvents != null) localLastIndex = localEvents.length - 1;

        for (EventListItem retrievedEvent : retrievedEvents) {

            EventListItem localEvent = null;

            //The retrieved event api id is searched among the local events in order to obtain the local event that corresponds to the retrieved one:
            if (localEvents != null) for (int i = 0; i <= localLastIndex; i++) {

                EventListItem auxLocalEvent = localEvents[i];

                //If the api id is found, the corresponding local event is deleted from the array:
                if (auxLocalEvent.getEventId().equals(retrievedEvent.getEventId())) {
                    localEvent = auxLocalEvent;
                    localEvents[i] = localEvents[localLastIndex];
                    localEvents[localLastIndex] = null;
                    localLastIndex--;
                }
            }

            //If the retrieved event does not exist in the database, it means that it was created in the Api and that, therefore, it must be locally stored:
            if (localEvent == null) {
                Log.e(TAG, "The event was created in the API");
                Log.e(TAG, retrievedEvent.eventToString());
                storeEventIntoDatabase(retrievedEvent);
            }

            //On the contrary, if it does exist, it is necessary to check which kind of sync operation must be performed:
            else {
                //The local event id is assigned to the retrieved event in order to perform update operations on the database:
                retrievedEvent.setEventId(localEvent.getEventId());

                Log.d(TAG, "Retrieved event: " + retrievedEvent.eventToString());
                Log.d(TAG, "Local event: " + localEvent.eventToString());

                //The following values will be used multiple times, so they are stored in local variables:
                String pendingOperation = localEvent.getPendingOperation();
                long lastLocalUpdate = localEvent.getLastApiUpdate();
                long lastApiUpdate = retrievedEvent.getLastApiUpdate();

                //If there is not a pending operation and the last local update is previous to the API one, the event was modified in the API:
                if (pendingOperation.equals("") && lastLocalUpdate < lastApiUpdate) {
                    Log.e(TAG, "The event was modified in the API (1)");
                    modifyEventInDatabase(localEvent, retrievedEvent);
                }

                //If the pending operation is a PUT one, the event will have to be updated in the API:
                else if (pendingOperation.equals(ApiConnectionService.REQUEST_METHOD_EDIT)) {
                    //If the modification takes place locally or remotely depends on the last update of each event (the local and the retrieved one):
                    if (lastLocalUpdate >= lastApiUpdate) {
                        Log.e(TAG, "The event was locally modified");
                        requestApiOperation(localEvent, pendingOperation);
                        pauseService();
                    }
                    else {
                        Log.e(TAG, "The event was modified in the API (2)");
                        modifyEventInDatabase(localEvent, retrievedEvent);
                    }
                }

                //If the operation is a DELETE one, the event will be deleted from the API unless its local last update is previous to the API one,
                //case in which it will be updated in the local database since the API events updates have priority over the STB ones:
                else if (pendingOperation.equals(ApiConnectionService.REQUEST_METHOD_DELETE)) {
                    if (lastLocalUpdate >= lastApiUpdate) {
                        Log.e(TAG, "The event was locally deleted");
                        requestApiOperation(localEvent, pendingOperation);
                        pauseService();
                    }
                    else {
                        Log.e(TAG, "The event was modified in the API (3)");
                        modifyEventInDatabase(localEvent, retrievedEvent);
                    }
                }
            }
        }

        //All the events that are still in the local events array must be deleted from the database if they have not a pending operation:
        if (localEvents != null) for (int i = 0; i <= localLastIndex; i++) {
            EventListItem localEvent = localEvents[i];
            if (localEvent.getPendingOperation().equals("")) {
                Log.e(TAG, "The event was deleted from the API");
                Log.e(TAG, localEvent.eventToString());
                deleteEventFromDatabase(localEvent);
            }
            //If the event has a POST pending operation it means that the event hasn't been sent to the API yet:
            else if (localEvent.getPendingOperation().equals(ApiConnectionService.REQUEST_METHOD_POST)) {
                Log.e(TAG, "The event was locally created");
                requestApiOperation(localEvent, ApiConnectionService.REQUEST_METHOD_POST);
                pauseService();
            }
        }
    }

    //Method that pauses this execution thread in order to allow the completion of API operations:
    private void pauseService() {
        while (ApiConnectionService.isCurrentlyRunning) {
            try {
                Log.d(TAG, "Connection service is running");
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                Log.e(TAG, "pauseService. InterruptedException: " + ie.getMessage());
            }
        }
    }

    //This method saves an event into the database when it was created in the API:
    private void storeEventIntoDatabase(EventListItem eventToSave) {
        EventListItem[] eventToSaveArray = {eventToSave};

        //The main activity will manage the communication with the event fragment to indicate if the focus must be relocated:
        if (mainActivityReference != null && mainActivityReference.get() != null)
            //In this case the retrievedEvent is irrelevant, since it will not be used:
            mainActivityReference.get().relocateFocusAfterSync(eventToSave, null, LOCAL_SYNC_OP_SAVE);

        EventsPublisher.publishEvents(getContextToPublish(), eventToSaveArray);
    }

    //This method modifies an event in the database when it was modified in the API:
    private void modifyEventInDatabase(EventListItem localEvent, EventListItem retrievedEvent) {
        EventListItem[] eventToModifyArray = {retrievedEvent};

        //The main activity will manage the communication with the event fragment to indicate if the focus must be relocated:
        if (mainActivityReference != null && mainActivityReference.get() != null)
            //In this case the retrievedEvent is not irrelevant, since it will be used to establish if the modification affects the currently selected day's event list:
            mainActivityReference.get().relocateFocusAfterSync(localEvent, retrievedEvent, LOCAL_SYNC_OP_EDIT);

        EventsPublisher.modifyEvent(getContextToPublish(), eventToModifyArray);
    }

    //This method deletes an event from the database when it was deleted from the API:
    private void deleteEventFromDatabase(EventListItem eventToDelete) {

        //The main activity will manage the communication with the event fragment to indicate if the focus must be relocated:
        if (mainActivityReference != null && mainActivityReference.get() != null)
            //In this case the retrievedEvent is irrelevant, since it will not be used:
            mainActivityReference.get().relocateFocusAfterSync(eventToDelete, null, LOCAL_SYNC_OP_DELETE);

        EventsPublisher.deleteEvent(getContextToPublish(), eventToDelete.getEventId());
    }

    //This method returns the context that must be used by the events publisher:
    private Context getContextToPublish() {
        if (mainActivityReference != null && mainActivityReference.get() != null)
            return mainActivityReference.get();
        else return this;
    }

    //This method starts the ApiConnectionService in order to perform the requested operation:
    private void requestApiOperation(EventListItem localEvent, String requestMethod) {
        ApiConnectionService.isCurrentlyRunning = true;
        Intent dataIntent = new Intent(getApplicationContext(), ApiConnectionService.class);
        dataIntent.putExtra(EventUtils.EVENT_JSON_FIELD, EventJsonObject.createEventJson(localEvent).toString());
        dataIntent.putExtra("operation", requestMethod);
        startService(dataIntent);
    }
}