package com.droidmare.database.publisher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.AlarmResetService;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.DialogDisplayEventsActivity;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.database.model.EventItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

//Event retriever (for retrieving events based on the month and year) declaration
//@author Eduardo on 07/03/2018.

class EventRetriever extends AsyncTask<Long,Void,Void>{

    private static final String TAG = EventRetriever.class.getCanonicalName();

    //The array with all the retrieved events:
    private JSONObject[] jsonArray;

    //The array with all the repetitive events:
    private JSONObject[] jsonRepetitiveArray;

    //The type of operation for an specific instance:
    private EventsPublisher.operationType opType;

    //Since the context is assigned counting on the existence of the main activity, the context will never be leaking:
    @SuppressLint("StaticFieldLeak")
    private Context context;

    //The database manager that stores the events:
    private SQLiteManager database;

    EventRetriever(Context context, EventsPublisher.operationType operation){

        this.opType = operation;
        this.context = context;
    }

    @Override
    protected void onPreExecute() {

        if(database == null)
            database = new SQLiteManager(this.context, SQLiteManager.DATABASE_NAME,null, SQLiteManager.DATABASE_VERSION);

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Long... params) {

        switch (opType) {
            case RETRIEVE_EVENTS:
                getEvents();
                break;
            case RETRIEVE_ALL:
            case RESET_ALARMS:
                retrieveAndResetAll();
                break;
            case DELETE_EVENTS:
                if (params[0] != null){
                    database.deleteSingleEvent(params[0]);
                    getEvents();
                }
                else {
                    //To delete the events, they must be retrieved first:
                    retrieveAndResetAll();
                    deleteAllAlarms();
                    database.deleteAllEvents();
                    //The returning arrays are set to null so that the views can know that there are not events:
                    jsonArray = null;
                }
                break;
        }

        database.close();

        return null;
    }

    @Override
    protected void onPostExecute(Void params) {

        if (opType != EventsPublisher.operationType.RESET_ALARMS) returnEvents();

        else AlarmResetService.resettingEvents = false;

        super.onPostExecute(null);
    }

    //Retrieves all the events stored in the database for the current month and year:
    private void getEvents (){
        try {
            jsonArray = database.getEvents(DateUtils.currentMonth, DateUtils.currentYear, false, false);
            jsonRepetitiveArray = database.getRepetitiveEvents(DateUtils.currentMonth, DateUtils.currentYear);
        } catch (Exception e) {
            Log.e(TAG, "getEvents. Exception: " + e.getMessage());
        }
    }

    //Returns all the retrieved events to the corresponding activity based on the existence of the main activity and the type of operation:
    private void returnEvents(){

        if (opType == EventsPublisher.operationType.RETRIEVE_ALL) {

            EventListItem[] eventArray = EventItem.jsonArrayToEventArray(context, jsonArray);

            ArrayList <EventListItem> eventList = new ArrayList<>();

            if (eventArray != null) {
                Collections.addAll(eventList, eventArray);
                ((DialogDisplayEventsActivity) context).finishInitialization(eventList);
            }
        }

        else if (MainActivity.isCreated()) {

            ArrayList<EventListItem>[] eventListsArray = EventItem.jsonArrayToEventListArray(context, jsonArray);
            EventListItem[] eventArray = EventItem.jsonArrayToEventArray(context, jsonRepetitiveArray);

            ArrayList<EventListItem> repetitiveEvents = null;

            if (eventArray != null) {
                repetitiveEvents = new ArrayList<>();
                Collections.addAll(repetitiveEvents, eventArray);
            }

            ((MainActivity)context).returnMonthEvents(eventListsArray, repetitiveEvents);
        }
    }

    //Retrieves all the stored events and resets their alarms:
    private void retrieveAndResetAll() {

        boolean displayAll = opType == EventsPublisher.operationType.RETRIEVE_ALL;

        jsonArray = database.getEvents(-1, -1, false, displayAll);

        EventListItem[] eventArray = EventItem.jsonArrayToEventArray(context, jsonArray);

        //When the database has not events, the value of eventArray will be null, so not alarms can be reset:
        if (eventArray != null && opType == EventsPublisher.operationType.RESET_ALARMS)
            EventUtils.sendMultipleReminders(context, eventArray, false);

        //When the operation is deleting all events, the MainActivity must be notified in order to send the request to the api:
        else if (eventArray != null && opType == EventsPublisher.operationType.DELETE_EVENTS) {

            String [] eventStrings = new String [jsonArray.length];

            int i = 0;

            for (JSONObject eventJson : jsonArray) eventStrings[i++] = eventJson.toString();

            ((MainActivity)context).sendDeleteEvents(eventStrings);
        }
    }

    //Method for deleting all the alarms for the retrieved events:
    private void deleteAllAlarms() {
        EventListItem[] eventArray = EventItem.jsonArrayToEventArray(context, jsonArray);

        //When the database has not events, the value of eventArray will be null, so not alarms can be deleted:
        if (eventArray != null) EventUtils.sendMultipleReminders(context, eventArray, true);
    }
}

