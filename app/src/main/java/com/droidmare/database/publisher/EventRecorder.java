package com.droidmare.database.publisher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.droidmare.calendar.models.CalEventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.ApiConnectionService;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.common.models.ConstantValues;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.common.models.EventJsonObject;

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

    private String localEventId;

    EventRecorder(Context context, EventsPublisher.operationType operation){
        this.newEvent = null;
        this.context = context;
        this.operationType = operation;
    }

    EventRecorder(Context context, EventsPublisher.operationType operation, String localId){
        this.newEvent = null;
        this.context = context;
        this.operationType = operation;
        this.localEventId = localId;
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
                EventJsonObject eventJson = CalEventJsonObject.createEventJson(event);

                //If the event has no id, the id is assigned after storing it in the database
                if (operationType.equals(EventsPublisher.operationType.CREATE_EVENT)) {
                    String eventLocalId = ConstantValues.LOCAL_ID_HEAD + database.addEvent(eventJson);
                    if (event.getEventId().equals("")) {
                        event.setEventId(eventLocalId);
                        if (events.length == 1) newEvent = event;
                    }
                }

                //If the event has an id, the operation has to be a modify one:
                else {
                    String eventId = event.getEventId();
                    if (localEventId != null) eventId = localEventId;
                    database.updateEvent(eventJson, eventId);
                }

                //Depending on the length of the events array, the operation is storing a new event or a set of events received from the API:
                if (events.length != 1) eventItemArray[index++] = event;
            }
        }

        if (eventItemArray.length != 1) createMultipleReminders();
    }

    //Method that indicates the main activity to send the new event to the API:
    private void sendEvent(EventListItem event){
        if (MainActivity.isCreated() && context instanceof MainActivity)
            ((MainActivity)context).sendOperationRequest(event, ApiConnectionService.REQUEST_METHOD_POST);
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

