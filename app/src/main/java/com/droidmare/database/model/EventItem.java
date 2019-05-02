package com.droidmare.database.model;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.EventUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


//Event item model (for transforming events into JSON objects and JSON Objects into Intents) declaration
//@author Eduardo on 07/03/2018.

public class EventItem {

    private static final String TAG = EventItem.class.getCanonicalName();

    //Method that transforms an event into a JSON Object:
    public static JSONObject eventToJson (EventListItem event) {

        JSONObject eventJson = null;

        try {
            JSONObject auxJson = new JSONObject();
            eventJson = new JSONObject();

            //To obtain the event type, the reminder type must be split since its structure is (EVENTTYPE_REMINDER):
            String [] eventTypeArray = event.getReminderType().toString().split("_REMINDER");
            StringBuilder eventType = new StringBuilder();

            //Due to the fact that measure reminder type structure is as follows (MEASURE_REMINDER_XX), the eventTypeArray could have more than one element:
            for (String anEventTypeArray : eventTypeArray) eventType.append(anEventTypeArray);

            //SQLite doesn't have boolean type, so the booleans must be converted to int:
            int instantly = event.isInstantlyShown() ? 1 : 0;

            auxJson.put(EventUtils.EVENT_ID_FIELD, event.getEventId());
            auxJson.put(EventUtils.EVENT_API_ID_FIELD, event.getEventApiId());
            auxJson.put(EventUtils.EVENT_USER_FIELD, event.getUserId());
            auxJson.put(EventUtils.EVENT_TYPE_FIELD, eventType.toString());
            auxJson.put(EventUtils.EVENT_HOUR_FIELD, event.getEventHour());
            auxJson.put(EventUtils.EVENT_MINUTE_FIELD, event.getEventMinute());
            auxJson.put(EventUtils.EVENT_DAY_FIELD, event.getEventDay());
            auxJson.put(EventUtils.EVENT_MONTH_FIELD, event.getEventMonth());
            auxJson.put(EventUtils.EVENT_YEAR_FIELD, event.getEventYear());
            auxJson.put(EventUtils.EVENT_DESCRIPTION_FIELD, event.getDescriptionText());
            auxJson.put(EventUtils.EVENT_INSTANTLY_FIELD, instantly);
            auxJson.put(EventUtils.EVENT_INTERVAL_FIELD, event.getIntervalTime());
            auxJson.put(EventUtils.EVENT_REPETITION_STOP_FIELD, event.getRepetitionStop());
            auxJson.put(EventUtils.EVENT_TIMEOUT_FIELD, event.getReminderTimeOut());
            auxJson.put(EventUtils.EVENT_REP_TYPE_FIELD, event.getRepetitionType());
            auxJson.put(EventUtils.EVENT_PREV_ALARMS_FIELD, event.getPreviousAlarms());
            auxJson.put(EventUtils.EVENT_LAST_UPDATE_FIELD, event.getLastApiUpdate());
            auxJson.put(EventUtils.EVENT_PENDING_OP_FIELD, event.getPendingOperation());

            eventJson.put("event", auxJson);
        }
        catch (JSONException jse){
            Log.e(TAG,"eventToJson. JSONException: " + jse.getMessage());
        }

        return eventJson;
    }

    //Method that transforms an array of JSON Objects into an array of event lists
    public static ArrayList<EventListItem>[] jsonArrayToEventListArray(Context context, JSONObject[] jsonArray) {

        ArrayList<EventListItem>[] eventsList = new ArrayList[31];

        if (jsonArray != null) {
            for (int i = 0; i < 31; i++)
                eventsList[i] = new ArrayList<>();

            EventListItem event;

            Intent eventData;

            for (JSONObject json : jsonArray) {

                eventData = transformJson (json);

                //Every event is added to its corresponding day:
                int dayOfTheEvent = eventData.getIntExtra(EventUtils.EVENT_DAY_FIELD, -1);
                event = EventUtils.makeEvent(context, eventData);

                if (event != null) eventsList[dayOfTheEvent - 1].add(event);
            }
        }

        return eventsList;
    }

    //Method that transforms an array of JSON Objects into an array of events:
    public static EventListItem[] jsonArrayToEventArray(Context context, JSONObject[] jsonArray) {

        EventListItem[] eventsArray = null;

        if (jsonArray != null) {

            eventsArray = new EventListItem[jsonArray.length];

            EventListItem event;

            Intent eventData;

            int index = 0;

            for (JSONObject json : jsonArray) {

                eventData = transformJson (json);

                event = EventUtils.makeEvent(context, eventData);

                if (event != null) eventsArray[index++] = (event);
            }
        }

        return eventsArray;
    }

    private static Intent transformJson (JSONObject json){

        Intent eventData = new Intent();

        try {
            JSONObject auxJson = json.getJSONObject("event");

            //SQLite doesn't have boolean type, so the booleans were stored as ints and must be reconverted:
            boolean instantly = auxJson.getInt(EventUtils.EVENT_INSTANTLY_FIELD) != 0;

            eventData.putExtra(EventUtils.EVENT_ID_FIELD, auxJson.getLong(EventUtils.EVENT_ID_FIELD));
            eventData.putExtra(EventUtils.EVENT_API_ID_FIELD, auxJson.getLong(EventUtils.EVENT_API_ID_FIELD));
            eventData.putExtra(EventUtils.EVENT_USER_FIELD, auxJson.getInt(EventUtils.EVENT_USER_FIELD));
            eventData.putExtra(EventUtils.EVENT_TYPE_FIELD, auxJson.getString(EventUtils.EVENT_TYPE_FIELD));
            eventData.putExtra(EventUtils.EVENT_HOUR_FIELD, auxJson.getInt(EventUtils.EVENT_HOUR_FIELD));
            eventData.putExtra(EventUtils.EVENT_MINUTE_FIELD, auxJson.getInt(EventUtils.EVENT_MINUTE_FIELD));
            eventData.putExtra(EventUtils.EVENT_DAY_FIELD, auxJson.getInt(EventUtils.EVENT_DAY_FIELD));
            eventData.putExtra(EventUtils.EVENT_MONTH_FIELD, auxJson.getInt(EventUtils.EVENT_MONTH_FIELD));
            eventData.putExtra(EventUtils.EVENT_YEAR_FIELD, auxJson.getInt(EventUtils.EVENT_YEAR_FIELD));
            eventData.putExtra(EventUtils.EVENT_DESCRIPTION_FIELD, auxJson.getString(EventUtils.EVENT_DESCRIPTION_FIELD));
            eventData.putExtra(EventUtils.EVENT_INSTANTLY_FIELD, instantly);
            eventData.putExtra(EventUtils.EVENT_INTERVAL_FIELD, auxJson.getInt(EventUtils.EVENT_INTERVAL_FIELD));
            eventData.putExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, auxJson.getLong(EventUtils.EVENT_REPETITION_STOP_FIELD));
            eventData.putExtra(EventUtils.EVENT_TIMEOUT_FIELD, auxJson.getLong(EventUtils.EVENT_TIMEOUT_FIELD));
            eventData.putExtra(EventUtils.EVENT_REP_TYPE_FIELD, auxJson.getString(EventUtils.EVENT_REP_TYPE_FIELD));
            eventData.putExtra(EventUtils.EVENT_PREV_ALARMS_FIELD, auxJson.getString(EventUtils.EVENT_PREV_ALARMS_FIELD));
            eventData.putExtra(EventUtils.EVENT_LAST_UPDATE_FIELD, auxJson.getLong(EventUtils.EVENT_LAST_UPDATE_FIELD));
            eventData.putExtra(EventUtils.EVENT_PENDING_OP_FIELD, auxJson.getString(EventUtils.EVENT_PENDING_OP_FIELD));
        } catch (JSONException jse) {
            Log.e(TAG, "jsonToIntent. JSONException: " + jse.getMessage());
        }

        return eventData;
    }
}
