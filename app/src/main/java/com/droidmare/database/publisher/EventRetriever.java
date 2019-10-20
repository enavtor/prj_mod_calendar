package com.droidmare.database.publisher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.droidmare.calendar.models.EventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.AlarmResetService;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.DialogDisplayEventsActivity;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;

import java.util.ArrayList;
import java.util.Collections;

//Event retriever (for retrieving events based on the month and year) declaration
//@author Eduardo on 07/03/2018.

class EventRetriever extends AsyncTask<String,Void,Void>{

    private static final String TAG = EventRetriever.class.getCanonicalName();

    //The array with all the retrieved events:
    private EventJsonObject[] jsonArray;

    //The array with all the repetitive events:
    private EventJsonObject[] jsonRepetitiveArray;

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
    protected Void doInBackground(String... params) {

        switch (opType) {
            case RETRIEVE_EVENTS:
                getEvents();
                break;
            case RETRIEVE_ALL:
            case RESET_ALARMS:
                retrieveAndResetAll();
                break;
            case DELETE_EVENT:
                database.deleteSingleEvent(params[0]);
                getEvents();
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
            int month = DateUtils.currentMonth;
            int year = DateUtils.currentYear;
            jsonArray = database.getEvents(month, year, false, false, false);
            jsonRepetitiveArray = database.getEvents(month, year, true, false, false);
        } catch (Exception e) {
            Log.e(TAG, "getEvents. Exception: " + e.getMessage());
        }
    }

    //Returns all the retrieved events to the corresponding activity based on the existence of the main activity and the type of operation:
    private void returnEvents(){

        if (opType == EventsPublisher.operationType.RETRIEVE_ALL) {

            EventListItem[] eventArray = EventUtils.jsonArrayToEventArray(context, jsonArray);

            ArrayList <EventListItem> eventList = new ArrayList<>();

            if (eventArray != null) Collections.addAll(eventList, eventArray);

            ((DialogDisplayEventsActivity) context).finishInitialization(eventList);
        }

        else if (MainActivity.isCreated() && context instanceof MainActivity) {

            ArrayList<EventListItem>[] eventListsArray = EventUtils.jsonArrayToEventListArray(context, jsonArray);
            EventListItem[] eventArray = EventUtils.jsonArrayToEventArray(context, jsonRepetitiveArray);

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

        jsonArray = database.getEvents(-1, -1, true, false, displayAll);

        EventListItem[] eventArray = EventUtils.jsonArrayToEventArray(context, jsonArray);

        //When the database has not events, the value of eventArray will be null, so not alarms can be reset:
        if (eventArray != null && opType == EventsPublisher.operationType.RESET_ALARMS)
            com.droidmare.calendar.utils.EventUtils.sendMultipleReminders(context, eventArray, false);
    }
}

