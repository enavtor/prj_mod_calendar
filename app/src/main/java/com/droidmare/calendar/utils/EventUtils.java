package com.droidmare.calendar.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidmare.calendar.events.ActivityEvent;
import com.droidmare.calendar.events.DoctorEvent;
import com.droidmare.calendar.events.StimulusEvent;
import com.droidmare.calendar.events.MedicationEvent;
import com.droidmare.calendar.events.PersonalEvent;
import com.droidmare.calendar.events.TextNoFeedbackEvent;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.common.models.ConstantValues;
import com.droidmare.common.models.EventJsonObject;
import com.droidmare.common.utils.DateUtils;
import com.droidmare.common.utils.ServiceUtils;

import java.util.ArrayList;

//Utils for creating event objects and alarms
//@author Eduardo on 08/02/2018.

public class EventUtils {

    //Default reminder timeout (the reminder will be automatically hidden after 30 seconds):
    private static final long DEFAULT_HIDE_TIME = 30 * 1000;

    //Package and main activity of the reminder module:
    private static final String REMINDER_PACKAGE = "com.droidmare.reminders";
    private static final String REMINDER_RECEIVER = "com.droidmare.reminders.services.ReminderReceiverService";

    //Function for making an event object from an Intent:
    public static EventListItem makeEvent (Context context, EventJsonObject eventJson) {

        EventListItem event = null;

        String eventId = eventJson.getString(ConstantValues.EVENT_ID_FIELD, "");

        String eventType = eventJson.getString(ConstantValues.EVENT_TYPE_FIELD, "");

        int eventHour = eventJson.getInt(ConstantValues.EVENT_HOUR_FIELD, -1);
        int eventMinute = eventJson.getInt(ConstantValues.EVENT_MINUTE_FIELD, -1);
        int eventDay = eventJson.getInt(ConstantValues.EVENT_DAY_FIELD, DateUtils.currentDay);
        int eventMonth = eventJson.getInt(ConstantValues.EVENT_MONTH_FIELD, DateUtils.currentMonth);
        int eventYear = eventJson.getInt(ConstantValues.EVENT_YEAR_FIELD, DateUtils.currentYear);

        String eventDescription = eventJson.getString(ConstantValues.EVENT_DESCRIPTION_FIELD, "");
        int intervalTime = eventJson.getInt(ConstantValues.EVENT_REP_INTERVAL_FIELD, 0);

        String repetitionType = eventJson.getRepetitionTypeJson().toString();
        long repetitionStop = eventJson.getLong(ConstantValues.EVENT_REPETITION_STOP_FIELD, -1);

        long reminderTimeOut = eventJson.getLong(ConstantValues.EVENT_TIMEOUT_FIELD, DEFAULT_HIDE_TIME);

        String prevAlarms = eventJson.getPreviousAlarmsArray().toString();

        String pendingOperation = eventJson.getString(ConstantValues.EVENT_PENDING_OP_FIELD, "");

        long lastApiUpdate = eventJson.getLong(ConstantValues.EVENT_LAST_UPDATE_FIELD, -1);

        switch (eventType) {
            case ConstantValues.ACTIVITY_EVENT_TYPE:
                event = new ActivityEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case ConstantValues.DOCTOR_EVENT_TYPE:
                event = new DoctorEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case ConstantValues.MEDICATION_EVENT_TYPE:
                event = new MedicationEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case ConstantValues.PERSONAL_EVENT_TYPE:
                event = new PersonalEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case ConstantValues.STIMULUS_EVENT_TYPE:
                event = new StimulusEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case ConstantValues.TEXTNOFEEDBACK_EVENT_TYPE:
                event = new TextNoFeedbackEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
        }

        //Now the event can be properly initialized:
        if (event != null) event.setNewEvent();

        return event;
    }

    //Function for making an alarm from an event object:
    public static void makeAlarm (Context context, EventListItem event) {

        String[] eventJsonList = {event.createNewReminder()};

        sendReminders(context, eventJsonList, false);
    }

    //Function for making multiple reminders from multiple event objects (so the reminder app can create or delete all the alarms with a single intent):
    public static void sendMultipleReminders(Context context, EventListItem[] eventArray, boolean deleteOperation) {

        String[] eventJsonList = new String[eventArray.length];
        int i = 0;

        for (EventListItem event : eventArray) {
            eventJsonList[i++] = event.createNewReminder();
        }

        sendReminders(context, eventJsonList, deleteOperation);
    }

    //Function for deleting an existing alarm:
    public static void deleteAlarm (Context context, EventListItem event) {

        String[] eventJsonList = {event.createNewReminder()};

        sendReminders(context, eventJsonList, true);
    }

    //Function for starting the reminders external app:
    private static void sendReminders(Context context, String[] eventJsonList, boolean deleteOperation) {

        Intent intent = new Intent();
        intent.setComponent(new ComponentName(REMINDER_PACKAGE, REMINDER_RECEIVER));

        intent.putExtra(ConstantValues.EVENT_JSON_FIELD, eventJsonList);
        intent.putExtra(ConstantValues.DELETE_ALARM_OP, deleteOperation);

        ServiceUtils.startService(context, intent);
    }

    //Method that transforms an array of JSON Objects into an array of event lists
    public static ArrayList<EventListItem>[] jsonArrayToEventListArray(Context context, EventJsonObject[] jsonArray) {

        ArrayList<EventListItem>[] eventsList = new ArrayList[31];

        if (jsonArray != null) {
            for (int i = 0; i < 31; i++)
                eventsList[i] = new ArrayList<>();

            EventListItem event;

            for (EventJsonObject eventJson : jsonArray) {

                //Every event is added to its corresponding day:
                int dayOfTheEvent = eventJson.getInt(ConstantValues.EVENT_DAY_FIELD, -1);
                event = makeEvent(context, eventJson);

                if (event != null) eventsList[dayOfTheEvent - 1].add(event);
            }
        }

        return eventsList;
    }

    //Method that transforms an array of JSON Objects into an array of events:
    public static EventListItem[] jsonArrayToEventArray(Context context, EventJsonObject[] jsonArray) {

        EventListItem[] eventsArray = null;

        if (jsonArray != null) {

            eventsArray = new EventListItem[jsonArray.length];

            EventListItem event;

            int index = 0;

            for (EventJsonObject json : jsonArray) {

                event = makeEvent(context, json);

                if (event != null) eventsArray[index++] = event;
            }
        }

        return eventsArray;
    }
}
