package com.droidmare.database.publisher;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.EventReceiverService;
import com.droidmare.calendar.services.UserDataReceiverService;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.database.model.EventItem;

import org.json.JSONException;
import org.json.JSONObject;

//Event recorder (for storing events) declaration
//@author Eduardo on 07/03/2018.

class EventRecorder extends AsyncTask<EventListItem,Object,Void>{

    private static final String TAG = EventRecorder.class.getCanonicalName();

    //Since the context is assigned counting on the existence of the main activity, it will never be leaking:
    @SuppressLint("StaticFieldLeak")
    private Context context;

    //The database manager that stores events:
    private SQLiteManager database;

    //The event array reference, so the id can be assigned once they have been stored in the database:
    private EventListItem[] eventItemArray;

    //A reference to the new event:
    private EventListItem newEvent;

    //The event that is going to be sent to the api:
    private EventListItem eventToSend;

    //Whether or not the event should be sent to the api:
    private boolean sendToApi;

    //Whether or not an event was sent to the api:
    private static boolean wasSent = false;

    EventRecorder(Context context){

        this.newEvent = null;
        this.context = context;
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

        if (!EventReceiverService.eventReceived) {

            //A statistic is sent in order to indicate that an event was created:
            if (newEvent != null) sendNewEventStatistic(newEvent);

            //If the event must be sent to the api the views are not reloaded (once the event has been sent the database is updated and the views reloaded):
            if (sendToApi) {
                wasSent = true;
                sendEvent(eventToSend);
            }

            else reloadEvents();
        }

        else EventReceiverService.eventReceived = false;

        super.onPostExecute(null);
    }

    //Function for storing events in the data base:
    private void storeEvents (EventListItem[] events) {

        sendToApi = false;
        eventItemArray = events;
        int index = 0;

        for (EventListItem event: events) {

            if (event != null) {
                JSONObject eventJson = EventItem.eventToJson(event);
                long eventId = event.getEventId();

                long eventLastUpdate = event.getLastApiUpdate();
                long storedLastUpdate = database.getLastUpdateFor(eventId);

                //If the event has no id, the id is assigned after storing it in the database
                if (eventId == -1 && events.length == 1) {
                    event.setEventId(database.addEvent(eventJson));
                    newEvent = event;
                }

                else if (eventId == -1) event.setEventId(database.addEvent(eventJson));

                //If the event has an id, the operation has to be a modify one:
                else database.updateEvent(eventJson, event.getEventId());

                //Depending on the length of the events array, the operation is storing a new event or a set of events received from the API:
                if (events.length == 1) {
                    eventToSend = event;
                    EventUtils.makeAlarm(context, event);
                    //The new event must be sent to the API unless it was received from it or the operation was updating its apiId:
                    sendToApi = (!EventReceiverService.eventReceived && storedLastUpdate == eventLastUpdate && !event.getPendingOperation().equals("DELETE"));
                    //Since the operation for sending an event can fail, case in which the condition storedLastUpdate == eventLastUpdate will be met,
                    //the variable wasSent is checked in order to know if previously to this operation a sending attempt was made and to set the sendToApi
                    //control variable to false, preventing the application from getting stuck into an infinite loop:
                    if (wasSent) wasSent = sendToApi = false;
                }

                else eventItemArray[index++] = event;

                Log.e("Retrieved Event " + UserDataReceiverService.getUserId(), index + " ofifo " + events.length + " (" + event.eventToString() + ")");
            }
        }

        if (eventItemArray.length != 1) createMultipleReminders();
    }

    private void sendNewEventStatistic (EventListItem event) {
        if (MainActivity.isCreated())
            ((MainActivity)context).sendNewEventStatistic(event);
    }

    //Method that indicates the main activity to send the new event to the API:
    private void sendEvent(EventListItem event){
        try {
            JSONObject eventJson = EventItem.eventToJson(event).getJSONObject("event");
            if (MainActivity.isCreated())
                ((MainActivity)context).sendEvent(EventUtils.transformJsonToIntent(eventJson, false));
        } catch (JSONException jse) {
            Log.e(TAG, "sendEvent. JSONException: " + jse.getMessage());
        }
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

