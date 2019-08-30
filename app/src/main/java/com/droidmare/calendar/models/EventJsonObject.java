package com.droidmare.calendar.models;

import android.util.Log;

import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//Model for a event json object (a specialization of JSONObject for building event json objects)
//@author Eduardo on 21/08/2018.

public class EventJsonObject extends JSONObject {

    private static final String TAG = EventJsonObject.class.getCanonicalName();

    public static EventJsonObject createEventJson(String jsonString) {

        EventJsonObject newEventJson;

        if (jsonString != null) try {
            newEventJson = new EventJsonObject(jsonString);

            //If the event json has its start date defined as a long, it must be stored as minute, hour, day, month and year separately:
            if (newEventJson.has(EventUtils.EVENT_START_DATE_FIELD) && !newEventJson.has(EventUtils.EVENT_HOUR_FIELD)) {
                long eventStartDate = newEventJson.getLong(EventUtils.EVENT_START_DATE_FIELD);

                int[] dateArray = DateUtils.transformFromMillis(eventStartDate);

                newEventJson.put(EventUtils.EVENT_HOUR_FIELD, dateArray[DateUtils.HOUR]);
                newEventJson.put(EventUtils.EVENT_MINUTE_FIELD, dateArray[DateUtils.MINUTE]);
                newEventJson.put(EventUtils.EVENT_DAY_FIELD, dateArray[DateUtils.DAY]);
                newEventJson.put(EventUtils.EVENT_MONTH_FIELD, dateArray[DateUtils.MONTH]);
                newEventJson.put(EventUtils.EVENT_YEAR_FIELD, dateArray[DateUtils.YEAR]);
            }
            //On the contrary, if the event has no start date defined as a long, it must be set:
            else {
                int[] dateArray = new int[5];

                dateArray[DateUtils.HOUR] = newEventJson.getInt(EventUtils.EVENT_HOUR_FIELD);
                dateArray[DateUtils.MINUTE] = newEventJson.getInt(EventUtils.EVENT_MINUTE_FIELD);
                dateArray[DateUtils.DAY] = newEventJson.getInt(EventUtils.EVENT_DAY_FIELD);
                dateArray[DateUtils.MONTH] = newEventJson.getInt(EventUtils.EVENT_MONTH_FIELD);
                dateArray[DateUtils.YEAR] = newEventJson.getInt(EventUtils.EVENT_YEAR_FIELD);

                newEventJson.put(EventUtils.EVENT_START_DATE_FIELD, DateUtils.getMillisFromArray(dateArray));
            }
        } catch (JSONException jsonException) {
            Log.e(TAG, "createEventJson. JSONException: " + jsonException.getMessage());
            return new EventJsonObject();
        }

        else newEventJson = new EventJsonObject();

        return newEventJson;
    }

    public static EventJsonObject createEventJson(EventListItem event) {

        EventJsonObject newEventJson = new EventJsonObject();

        //To obtain the event type, the reminder type must be split since its structure is (EVENTTYPE_REMINDER):
        String [] eventTypeArray = event.getReminderType().toString().split("_REMINDER");

        newEventJson.put(EventUtils.EVENT_ID_FIELD, event.getEventId());
        newEventJson.put(EventUtils.EVENT_TYPE_FIELD, eventTypeArray[0]);
        newEventJson.put(EventUtils.EVENT_HOUR_FIELD, event.getEventHour());
        newEventJson.put(EventUtils.EVENT_MINUTE_FIELD, event.getEventMinute());
        newEventJson.put(EventUtils.EVENT_DAY_FIELD, event.getEventDay());
        newEventJson.put(EventUtils.EVENT_MONTH_FIELD, event.getEventMonth());
        newEventJson.put(EventUtils.EVENT_YEAR_FIELD, event.getEventYear());
        newEventJson.put(EventUtils.EVENT_DESCRIPTION_FIELD, event.getDescriptionText());
        newEventJson.put(EventUtils.EVENT_REP_INTERVAL_FIELD, event.getIntervalTime());
        newEventJson.put(EventUtils.EVENT_REPETITION_STOP_FIELD, event.getRepetitionStop());
        newEventJson.put(EventUtils.EVENT_TIMEOUT_FIELD, event.getReminderTimeOut());
        newEventJson.put(EventUtils.EVENT_REPETITION_TYPE_FIELD, event.getRepetitionType());
        newEventJson.put(EventUtils.EVENT_PREV_ALARMS_FIELD, event.getPreviousAlarms());
        newEventJson.put(EventUtils.EVENT_PENDING_OP_FIELD, event.getPendingOperation());
        newEventJson.put(EventUtils.EVENT_LAST_UPDATE_FIELD, event.getLastApiUpdate());

        return newEventJson;
    }

    public EventJsonObject() { super(); }

    private EventJsonObject(String jsonString) throws JSONException {
        super(jsonString);
    }

    @Override
    public JSONObject put(String name, int value) {
        try {
            super.put(name, value);
        } catch (JSONException jsonException) {
            Log.e(TAG, "putInt. JSONException: " + jsonException.getMessage());
            return this;
        }
        return this;
    }

    @Override
    public JSONObject put(String name, long value) {
        try {
            super.put(name, value);
        } catch (JSONException jsonException) {
            Log.e(TAG, "putLong. JSONException: " + jsonException.getMessage());
            return this;
        }
        return this;
    }

    @Override
    public JSONObject put(String name, Object value) {
        try {
            super.put(name, value);
        } catch (JSONException jsonException) {
            Log.e(TAG, "putObject. JSONException: " + jsonException.getMessage());
            return this;
        }
        return this;
    }

    public int getInt(String name, int defaultValue) {

        int fieldValue = defaultValue;

        if (this.has(name)) try {
            fieldValue = this.getInt(name);
        } catch (JSONException jsonException) {
            Log.e(TAG, "getInt. JSONException: " + jsonException.getMessage());
            return fieldValue;
        }

        return fieldValue;
    }

    public long getLong(String name, long defaultValue) {

        long fieldValue = defaultValue;

        if (this.has(name)) try {
            fieldValue = this.getLong(name);
        } catch (JSONException jsonException) {
            Log.e(TAG, "getLong. JSONException: " + jsonException.getMessage());
            return fieldValue;
        }

        return fieldValue;
    }

    public String getString(String name, String defaultValue) {

        String fieldValue;

        if (this.has(name)) try {
            fieldValue = this.getString(name);
            if (defaultValue.equals(EventUtils.DEFAULT_REPETITION_TYPE) && fieldValue.equals(""))
                fieldValue = defaultValue;
        } catch (JSONException jsonException) {
            Log.e(TAG, "getString. JSONException: " + jsonException.getMessage());
            return defaultValue;
        }

        else fieldValue = defaultValue;

        return fieldValue;
    }

    public JSONArray getPreviousAlarmsArray() {

        JSONArray prevAlarmsArray;

        try {
            prevAlarmsArray = new JSONArray(this.getString(EventUtils.EVENT_PREV_ALARMS_FIELD, "[]"));
        } catch (JSONException jse) {
            Log.e(TAG, "getPreviousAlarmsArray. JSONException: " + jse.getMessage());
            return new JSONArray();
        }

        return prevAlarmsArray;
    }

    public JSONObject getRepetitionTypeJson() {

        JSONObject repetitionType = new JSONObject();

        try {
            repetitionType = new JSONObject(this.getString(EventUtils.EVENT_REPETITION_TYPE_FIELD, EventUtils.DEFAULT_REPETITION_TYPE));
        } catch (JSONException jse) {
            Log.e(TAG, "initializeAttributeValues. JSONException: " + jse.getMessage());

        }

        return repetitionType;
    }
}
