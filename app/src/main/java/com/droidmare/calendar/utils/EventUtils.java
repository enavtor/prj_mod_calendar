package com.droidmare.calendar.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import com.droidmare.R;
import com.droidmare.calendar.events.ActivityEvent;
import com.droidmare.calendar.events.DoctorEvent;
import com.droidmare.calendar.events.StimulusEvent;
import com.droidmare.calendar.events.surveys.TextFeedbackEvent;
import com.droidmare.calendar.events.MedicationEvent;
import com.droidmare.calendar.events.surveys.MoodEvent;
import com.droidmare.calendar.events.personal.PersonalEvent;
import com.droidmare.calendar.events.personal.TextNoFeedbackEvent;
import com.droidmare.calendar.models.EventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.shtvsolution.common.utils.ToastUtils;

import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;

//Utils for creating event objects and alarms
//@author Eduardo on 08/02/2018.

public class EventUtils {

    private static final String TAG = EventUtils.class.getCanonicalName();

    //Default reminder timeout (the reminder will be automatically hidden after 30 seconds):
    private static final long DEFAULT_HIDE_TIME = 30 * 1000;

    //Survey type reminders timeout (the reminder will be automatically hidden after 90 seconds):
    private static final long SURVEY_HIDE_TIME = 90 * 1000;

    //Package and main activity of the reminder module:
    private static final String REMINDER_PACKAGE = "com.droidmare.remainders";
    private static final String REMINDER_MAIN = "com.droidmare.reminders.view.SplashActivity";

    //Integer value for each type of repetition (daily, weekly, on alternate days, etc):
    public static final int DAILY_REPETITION = 0;
    public static final int ALTERNATE_REPETITION = 1;
    public static final int WEEKLY_REPETITION = 2;
    public static final int MONTHLY_REPETITION = 3;
    public static final int ANNUAL_REPETITION = 4;

    //Repetition type default string:
    public static final String DEFAULT_REPETITION_TYPE = "{\"type\":1,\"config\":\"[1]\"}";

    //Field where the event json string will be stored within an intent:
    public static final String EVENT_JSON_FIELD = "eventJsonString";

    //Strings that store the name of the different fields that the intent and jsons containing the event info will have:
    public static final String EVENT_ID_FIELD = "eventId";
    public static final String EVENT_USER_FIELD = "userId";
    public static final String EVENT_TYPE_FIELD = "eventType";
    public static final String EVENT_DESCRIPTION_FIELD = "eventText";

    public static final String EVENT_START_DATE_FIELD = "eventStartDate";
    public static final String EVENT_MINUTE_FIELD = "eventMinute";
    public static final String EVENT_HOUR_FIELD = "eventHour";
    public static final String EVENT_DAY_FIELD = "eventDay";
    public static final String EVENT_MONTH_FIELD = "eventMonth";
    public static final String EVENT_YEAR_FIELD = "eventYear";

    public static final String EVENT_PREV_ALARMS_FIELD = "eventPrevAlarms";
    public static final String EVENT_REP_INTERVAL_FIELD = "eventRepInterval";
    public static final String EVENT_REPETITION_TYPE_FIELD = "eventRepType";
    public static final String EVENT_REPETITION_STOP_FIELD = "eventRepStopDate";

    public static final String EVENT_PENDING_OP_FIELD = "eventPendingOp";
    public static final String EVENT_LAST_UPDATE_FIELD = "eventLastUpdate";
    public static final String EVENT_TIMEOUT_FIELD = "eventTimeOut";

    //Function for making an event object from an Intent:
    public static EventListItem makeEvent (Context context, EventJsonObject eventJson) {

        EventListItem event = null;

        String eventId = eventJson.getString(EVENT_ID_FIELD, "");

        String eventType = eventJson.getString(EVENT_TYPE_FIELD, "");

        int eventHour = eventJson.getInt(EVENT_HOUR_FIELD, -1);
        int eventMinute = eventJson.getInt(EVENT_MINUTE_FIELD, -1);
        int eventDay = eventJson.getInt(EVENT_DAY_FIELD, DateUtils.currentDay);
        int eventMonth = eventJson.getInt(EVENT_MONTH_FIELD, DateUtils.currentMonth);
        int eventYear = eventJson.getInt(EVENT_YEAR_FIELD, DateUtils.currentYear);

        String eventDescription = eventJson.getString(EVENT_DESCRIPTION_FIELD, "");
        int intervalTime = eventJson.getInt(EVENT_REP_INTERVAL_FIELD, 0);

        String repetitionType = eventJson.getRepetitionTypeJson().toString();
        long repetitionStop = eventJson.getLong(EVENT_REPETITION_STOP_FIELD, -1);

        long reminderTimeOut = eventJson.getLong(EVENT_TIMEOUT_FIELD, DEFAULT_HIDE_TIME);

        String prevAlarms = eventJson.getPreviousAlarmsArray().toString();

        String pendingOperation = eventJson.getString(EVENT_PENDING_OP_FIELD, "");

        long lastApiUpdate = eventJson.getLong(EVENT_LAST_UPDATE_FIELD, -1);

        switch (eventType) {
            case "ACTIVITY":
                event = new ActivityEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "DOCTOR":
                event = new DoctorEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "MEDICATION":
                event = new MedicationEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "PERSONAL":
                event = new PersonalEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "STIMULUS":
                event = new StimulusEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, reminderTimeOut, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "MOOD":
                event = new MoodEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, SURVEY_HIDE_TIME, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "TEXTFEEDBACK":
                event = new TextFeedbackEvent(context, eventId, eventHour, eventMinute, eventDay, eventMonth, eventYear, eventDescription,
                    intervalTime, repetitionType, repetitionStop, SURVEY_HIDE_TIME, prevAlarms, pendingOperation, lastApiUpdate
                );
                break;
            case "TEXTNOFEEDBACK":
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
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(REMINDER_PACKAGE, REMINDER_MAIN));

        //Before creating the alarm, it is necessary to check if the reminder exists for the event (and to create it if it doesn't):
        if(event.getReminderBytes() == null) event.createNewReminder();
        intent.putExtra("reminderBytes", event.getReminderBytes());

        startExternalApp(context, intent);
    }

    //Function for making multiple reminders from multiple event objects (so the reminder app can create or delete all the alarms with a single intent):
    public static void sendMultipleReminders(Context context, EventListItem[] eventArray, boolean deleteOperation) {

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(REMINDER_PACKAGE, REMINDER_MAIN));

        int i = 0;

        for (EventListItem event : eventArray) {

            if (event != null) {
                //Before adding the reminder to the intent, it is necessary to check if it exists for the event (and to create it if it doesn't):
                if (event.getReminderBytes() == null) event.createNewReminder();
                intent.putExtra("reminderBytes" + i++, event.getReminderBytes());
            }
        }

        //The number of events that are going to be sent must be storage in the intent, so the reminder app can know it:
        intent.putExtra("numberOfEvents", i);

        //When the operation is a delete one, the intent must include an extra that indicates it:
        if (deleteOperation) intent.putExtra("deleteAlarm", true);

        startExternalApp(context, intent);
    }

    //Function for deleting an existing alarm:
    public static void deleteAlarm (Context context, EventListItem event) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setComponent(new ComponentName(REMINDER_PACKAGE, REMINDER_MAIN));

        //Before deleting the alarm, it is necessary to check if the reminder exists for the event (and to create it if it doesn't):
        if(event.getReminderBytes() == null) event.createNewReminder();
        intent.putExtra("reminderBytes", event.getReminderBytes());

        //For deleting an alarm, an extra must be added to the intent:
        intent.putExtra("deleteAlarm", true);
        startExternalApp(context, intent);
    }

    //Function for starting the reminders external app checking if it is installed before proceeding:
    private static void startExternalApp(Context context, Intent intent) {

        Resources res = context.getResources();

        try {
            startActivity(context, intent, null);
        } catch (Exception exc) {
            //The operation type will be determined based on the existence of the extra "deleteAlarm" within the intent:
            boolean deleteAlarm = intent.getBooleanExtra("deleteAlarm", false);

            //The message will be different depending on the operation type:
            if (deleteAlarm) ToastUtils.makeCustomToast(context, res.getString(R.string.error_deleting_alarm));
            else ToastUtils.makeCustomToast(context, res.getString(R.string.error_setting_alarm));
        }
    }

    //This method transforms the repetition config string, inside repetitionType, into an array list:
    public static ArrayList<Integer> getRepetitionConfigArray(String config) {

        ArrayList<Integer> configList = new ArrayList<>();

        if (config.length() > 2) {
            config = config.replace(" ", "");

            String [] configArray = config.substring(1, config.length() - 1).split(",");

            for (String configElement: configArray) {
                configList.add(Integer.valueOf(configElement));
            }
        }

        return configList;
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
                int dayOfTheEvent = eventJson.getInt(EVENT_DAY_FIELD, -1);
                event = com.droidmare.calendar.utils.EventUtils.makeEvent(context, eventJson);

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
