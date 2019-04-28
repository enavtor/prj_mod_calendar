package com.shtvsolution.calendario.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.utils.DateUtils;
import com.shtvsolution.calendario.utils.EventUtils;
import com.shtvsolution.calendario.utils.NetworkUtils;
import com.shtvsolution.calendario.views.activities.MainActivity;
import com.shtvsolution.database.manager.SQLiteManager;
import com.shtvsolution.database.model.EventItem;
import com.shtvsolution.database.publisher.EventsPublisher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

//Service that synchronizes the events between the STB and the API:
//@author Eduardo on 08/11/2018.

public class ApiSynchronizationService extends Service {

    private static final String TAG = ApiSynchronizationService.class.getCanonicalName();

    public static final String LOCAL_SYNC_OP_SAVE = "save";
    public static final String LOCAL_SYNC_OP_EDIT= "edit";
    public static final String LOCAL_SYNC_OP_DELETE = "delete";

    private static boolean isRunning = false;

    //Server connection timeout in milliseconds
    private static final int SERVER_TIMEOUT = 5000;

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

        database = new SQLiteManager(this, SQLiteManager.DATABASE_NAME,null, SQLiteManager.DATABASE_VERSION);

        UserDataReceiverService.readSharedPrefs(this);

        String patientId = Integer.toString(UserDataReceiverService.getUserId());
        urlForRetrieving = ApiReceiverService.getApiUrl(getApplicationContext()) + "patients/" + patientId + "/events";

        startSyncTimer();
    }

    //Method that starts the timer for resynchronizing the events every 5 minutes:
    private void startSyncTimer() {

        //The resynchronization attempt will take place every 30 minutes:
        long resyncPeriod = DateUtils.minutesToMillis(5);

        Timer resyncTimer = new Timer("resyncTimer");

        resyncTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //The sync won't start until a connection is available:
                while (!NetworkUtils.isNetworkAvailable(getApplicationContext())) {

                    Log.d("TESTING", "Waiting for a connection...");

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Log.e(TAG, "onHandelIntent. InterruptedException: " + ie.getMessage());
                    }
                }

                Log.e("TESTING", "Resynchronization schedule started");

                retrieveAndSyncApiEvents();
            }
        }, 0, resyncPeriod);
    }

    @Override
    public void onDestroy() {
        database.close();
        isRunning = false;
        super.onDestroy();
    }

    public static boolean isRunning() { return isRunning; }

    @Nullable @Override
    public IBinder onBind(Intent intent) { return null; }

    //This method retrieves all the events from the Api and starts the synchronization function:
    private void retrieveAndSyncApiEvents() {

        String response = connectAndRetrieve();

        Log.d("TESTING", "Events retrieved from API: " + response);

        try {
            JSONObject responseJson = new JSONObject(response);
            JSONArray eventsJson = responseJson.getJSONArray("events");

            int numberOfRetrieved = 0;

            if (eventsJson != null) numberOfRetrieved = eventsJson.length();

            retrievedEvents = new EventListItem[numberOfRetrieved];

            for (int i = 0; i < numberOfRetrieved; i++) {

                JSONObject eventJson = (JSONObject)eventsJson.get(i);

                if (eventJson.getInt("intervalTime") != 0 && eventJson.getLong("eventStartDate") >= eventJson.getLong("eventStopDate"))
                    eventJson.put("eventStopDate", -1);

                retrievedEvents[i] = EventUtils.makeEvent(this, eventJson, true);
            }

            synchronizeEvents();

        } catch (JSONException jse) {
            Log.e(TAG, "retrieveAndSyncApiEvents. JSONException: " + jse.getMessage());
        }
    }

    //This method establishes a connection to the API and request a GET operation for all the user's events:
    private String connectAndRetrieve() {

        StringBuilder response = new StringBuilder();

        try {
            URL url = new URL(urlForRetrieving);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(SERVER_TIMEOUT);
            connection.setReadTimeout(SERVER_TIMEOUT);

            BufferedReader input = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            String inputLine;

            while ((inputLine = input.readLine()) != null)
                response.append(inputLine);

            input.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response.toString();
    }

    //This method checks all the retrieved events, comparing them to the ones stored in the database and performing all the necessary synchronization operations:
    private void synchronizeEvents() {

        Log.e("TESTING", "Event synchronization started");

        //First of all, all the local events are retrieved:
        JSONObject[] localEventsJson = database.getEvents(-1, -1, true, false);
        EventListItem[] localEvents = EventItem.jsonArrayToEventArray(this, localEventsJson);

        //When a local event is extracted from the array because its api id matches the retrieved one's, it is deleted from the array by putting the last array's
        //element into its position and setting the last occupied position's value to null, so it is necessary to store and modify the actual local last index:
        int localLastIndex = 0;
        if (localEvents != null) localLastIndex = localEvents.length - 1;

        for (EventListItem retrievedEvent: retrievedEvents) {

            EventListItem localEvent = null;

            //The retrieved event api id is searched among the local events in order to obtain the local event that corresponds to the retrieved one:
            if (localEvents != null) for (int i = 0; i <= localLastIndex; i++) {

                EventListItem auxLocalEvent = localEvents[i];

                //If the api id is found, the corresponding local event is deleted from the array:
                if (auxLocalEvent.getEventApiId() == retrievedEvent.getEventApiId()) {
                    localEvent = auxLocalEvent;
                    localEvents[i] = localEvents[localLastIndex];
                    localEvents[localLastIndex] = null;
                    localLastIndex--;
                }
            }

            //If the retrieved event does not exist in the database, it means that it was created in the Api and that, therefore, it must be locally stored:
            if (localEvent == null) {
                Log.e("TESTING", "The event was created in the API");
                Log.e("TESTING", retrievedEvent.eventToString());
                storeEventIntoDatabase(retrievedEvent);
            }

            //On the contrary, if it does exist, it is necessary to check which kind of sync operation must be performed:
            else {
                //The local event id is assigned to the retrieved event in order to perform update operations on the database:
                retrievedEvent.setEventId(localEvent.getEventId());

                Log.d("TESTING", "Retrieved event: " + retrievedEvent.eventToString());
                Log.d("TESTING", "Local event: " + localEvent.eventToString());

                //The following values will be used multiple times, so they are stored in local variables:
                String pendingOperation = localEvent.getPendingOperation();
                long lastLocalUpdate = localEvent.getLastApiUpdate();
                long lastApiUpdate = retrievedEvent.getLastApiUpdate();

                //If there is not a pending operation and the last local update is previous to the API one, the event was modified in the API:
                if (pendingOperation.equals("") && lastLocalUpdate < lastApiUpdate) {
                    Log.e("TESTING", "The event was modified in the API (1)");
                    modifyEventInDatabase(localEvent, retrievedEvent);
                }

                //If the pending operation is a POST one, the event will have to be updated in the API (if the pending operation is a POST one because
                //the event couldn't be sent to the API when created, that event won't be in the retrieved events array, since it is not in the API yet):
                else if (pendingOperation.equals("POST")) {
                    //If the modification takes place locally or remotely depends on the last update of each event (the local and the retrieved one):
                    if (lastLocalUpdate >= lastApiUpdate) {
                        Log.e("TESTING", "The event was locally modified");
                        sendEventToApi(localEvent);
                        pauseService();
                    }
                    else {
                        Log.e("TESTING", "The event was modified in the API (2)");
                        modifyEventInDatabase(localEvent, retrievedEvent);
                    }
                }

                //If the operation is a DELETE one, the event will be deleted from the API unless its local last update is previous to the API one,
                //case in which it will be updated in the local database since the API events updates have priority over the STB ones:
                else if (pendingOperation.equals("DELETE")) {
                    if (lastLocalUpdate >= lastApiUpdate) {
                        Log.e("TESTING", "The event was locally deleted");
                        deleteEventFromApi(localEvent);
                        pauseService();
                    }
                    else {
                        Log.e("TESTING", "The event was modified in the API (3)");
                        modifyEventInDatabase(localEvent, retrievedEvent);
                    }
                }
            }
        }

        //All the events that are still in the local events array must be deleted from the database if they have an api id different from -1, since
        //that will mean that the event was updated to the API and after that deleted from the API, reason why it was not in the retrieved events array:
        if (localEvents != null) for (int i = 0; i <= localLastIndex; i++) {
            EventListItem localEvent = localEvents[i];
            if (localEvent.getEventApiId() != -1) {
                Log.e("TESTING", "The event was deleted from the API");
                Log.e("TESTING", localEvent.eventToString());
                deleteEventFromDatabase(localEvent);
            }

            //If the api id for the local event is equal to -1, it means that the event hasn't been sent to the API yet:
            else {
                Log.e("TESTING", "The event was locally created");
                sendEventToApi(localEvent);
                pauseService();
            }
        }
    }

    //Method that pauses this execution thread in order to allow the completion of API operations:
    private void pauseService() {
        while(ApiConnectionService.isCurrentlyRunning) {
            try {
                Log.d("TESTING", "Connection service is running");
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

    //This method starts the event synchronize service to send an event to the API (to store or update it remotely):
    private void sendEventToApi(EventListItem localEvent){
        ApiConnectionService.isCurrentlyRunning = true;
        Intent eventIntent = EventUtils.transformJsonToIntent(EventItem.eventToJson(localEvent), false);
        eventIntent.setComponent(new ComponentName(getPackageName(), ApiConnectionService.class.getCanonicalName()));
        eventIntent.putExtra("operation", ApiConnectionService.REQUEST_METHOD_POST);
        startService(eventIntent);
    }

    //This method starts the event synchronize service to delete an event from the API:
    private void deleteEventFromApi(EventListItem localEvent){
        ApiConnectionService.isCurrentlyRunning = true;
        Intent eventIntent = EventUtils.transformJsonToIntent(EventItem.eventToJson(localEvent), false);
        eventIntent.setComponent(new ComponentName(getPackageName(), ApiConnectionService.class.getCanonicalName()));
        eventIntent.putExtra("operation", ApiConnectionService.REQUEST_METHOD_DELETE);
        startService(eventIntent);
    }
}