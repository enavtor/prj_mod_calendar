package com.droidmare.database.publisher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.UserDataReceiverService;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.database.model.EventItem;

import org.json.JSONObject;

//Event recorder (for storing events) declaration
//@author Eduardo on 07/03/2018.

class EventRecorder extends AsyncTask<EventListItem,Object,Void>{

    //Since the context is assigned counting on the existence of the main activity, it will never be leaking:
    @SuppressLint("StaticFieldLeak")
    private Context context;

    //The database manager that stores events:
    private SQLiteManager database;

    //The event array reference, so the id can be assigned once they have been stored in the database:
    private EventListItem[] eventItemArray;

    //A reference to the new event:
    private EventListItem newEvent;

    //The type of operation that must be performed:
    private EventsPublisher.operationType operationType;

    EventRecorder(Context context, EventsPublisher.operationType operation){
        this.newEvent = null;
        this.context = context;
        this.operationType = operation;
    }

    @Override
    protected void onPreExecute() {
        if(database == null)
            database = new SQLiteManager(this.context, SQLiteManager.DATABASE_NAME,null, SQLiteManager.DATABASE_VERSION);

        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(EventListItem... params) {

        storeEvents(params);

        return null;
    }

    @Override
    protected void onPostExecute(Void param) {

        if (newEvent != null) sendEvent(newEvent);

        else reloadEvents();

        super.onPostExecute(null);
    }

    //Function for storing events in the data base:
    private void storeEvents (EventListItem[] events) {

        eventItemArray = events;
        int index = 0;

        for (EventListItem event: events) {

            if (event != null) {
                JSONObject eventJson = EventItem.eventToJson(event);

                //If the event has no id, the id is assigned after storing it in the database
                if (operationType.equals(EventsPublisher.operationType.CREATE_EVENT)) {
                    String eventLocalId = "LocalId:" + database.addEvent(eventJson);
                    if (event.getEventId().equals("")) {
                        event.setEventId(eventLocalId);
                        if (events.length == 1) newEvent = event;
                    }
                }

                //If the event has an id, the operation has to be a modify one:
                else database.updateEvent(eventJson, event.getEventId());

                //Depending on the length of the events array, the operation is storing a new event or a set of events received from the API:
                if (events.length == 1) EventUtils.makeAlarm(context, event);

                else eventItemArray[index++] = event;
            }
        }

        if (eventItemArray.length != 1) createMultipleReminders();
    }

    //Method that indicates the main activity to send the new event to the API:
    private void sendEvent(EventListItem event){
        if (MainActivity.isCreated() && context instanceof MainActivity)
            ((MainActivity)context).sendPostEvent(event);
    }

    //Method for retrieving the current month's events and reloading the views:
    private void reloadEvents(){

        EventsPublisher.retrieveMonthEvents(context);
    }

    //Method for creating multiple reminders, and the corresponding alarms, for the events that has just been stored (externally):
    private void createMultipleReminders(){

        EventUtils.sendMultipleReminders(context, eventItemArray, false);
    }
}

