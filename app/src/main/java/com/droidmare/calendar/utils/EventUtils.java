package com.droidmare.calendar.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.droidmare.R;
import com.droidmare.calendar.events.ActivityEvent;
import com.droidmare.calendar.events.DoctorEvent;
import com.droidmare.calendar.events.StimulusEvent;
import com.droidmare.calendar.events.surveys.TextFeedbackEvent;
import com.droidmare.calendar.events.MedicationEvent;
import com.droidmare.calendar.events.surveys.MoodEvent;
import com.droidmare.calendar.events.personal.PersonalEvent;
import com.droidmare.calendar.events.personal.TextNoFeedbackEvent;
import com.droidmare.calendar.models.EventListItem;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.support.v4.content.ContextCompat.startActivity;
import static com.droidmare.calendar.utils.ToastUtils.DEFAULT_TOAST_DURATION;
import static com.droidmare.calendar.utils.ToastUtils.DEFAULT_TOAST_SIZE;

//Utils for creating event objects and alarms
//@author Eduardo on 08/02/2018.

public class EventUtils {

    private static final String TAG = EventUtils.class.getCanonicalName();

    //Default reminder timeout (the reminder will be automatically hidden after 30 seconds):
    private static final long DEFAULT_HIDE_TIME = 30 * 1000;

    //Survey type reminders timeout (the reminder will be automatically hidden after 90 seconds):
    private static final long SURVEY_HIDE_TIME = 90 * 1000;

    //Package and main activity of the reminder module:
    private static final String REMINDER_PACKAGE = "com.shtvsolution.recordatorios";
    private static final String REMINDER_MAIN = "com.shtvsolution.recordatorios.view.SplashActivity";

    //Integer value for each type of repetition (daily, weekly, on alternate days, etc):
    public static final int DAILY_REPETITION = 0;
    public static final int ALTERNATE_REPETITION = 1;
    public static final int WEEKLY_REPETITION = 2;
    public static final int MONTHLY_REPETITION = 3;
    public static final int ANNUAL_REPETITION = 4;

    //Repetition type default string:
    public static final String DEFAULT_REPETITION_TYPE = "{\"type\":1,\"config\":\"[1]\"}";

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

    //Function for making an event object from a Json:
    public static  EventListItem makeEvent (Context context, JSONObject json) {
        return makeEvent(context, transformJsonToIntent(json));
    }

    //Function for making an event object from an Intent:
    public static EventListItem makeEvent (Context context, Intent data) {

        EventListItem event = null;

        long eventId = data.getLongExtra(EVENT_ID_FIELD, -1);

        String eventType = data.getStringExtra(EVENT_TYPE_FIELD);

        int eventHour = data.getIntExtra(EVENT_HOUR_FIELD, -1);
        int eventMinute = data.getIntExtra(EVENT_MINUTE_FIELD, -1);
        int eventDay = data.getIntExtra(EVENT_DAY_FIELD, DateUtils.currentDay);
        int eventMonth = data.getIntExtra(EVENT_MONTH_FIELD, DateUtils.currentMonth);
        int eventYear = data.getIntExtra(EVENT_YEAR_FIELD, DateUtils.currentYear);

        String eventDescription = data.getStringExtra(EVENT_DESCRIPTION_FIELD);
        int intervalTime = data.getIntExtra(EVENT_REP_INTERVAL_FIELD, 0);

        String repetitionType = "";
        long repetitionStop = -1;

        if (intervalTime != 0) {
            repetitionStop = data.getLongExtra(EVENT_REPETITION_STOP_FIELD, -1);

            //If the event has a repetition interval, the repetition type is initialize:
            if (data.hasExtra(EVENT_REPETITION_TYPE_FIELD) && !data.getStringExtra(EVENT_REPETITION_TYPE_FIELD).equals("")) {
                repetitionType = data.getStringExtra(EVENT_REPETITION_TYPE_FIELD);
            }
            //If the intent has no data for the repetition type, it is initialize as its default value (daily repetition):
            else repetitionType = DEFAULT_REPETITION_TYPE;
        }

        long timeOut = data.getLongExtra(EVENT_TIMEOUT_FIELD, DEFAULT_HIDE_TIME);

        String prevAlarms = "";

        if (data.hasExtra(EVENT_PREV_ALARMS_FIELD)) {
            prevAlarms = data.getStringExtra(EVENT_PREV_ALARMS_FIELD);
        }

        String pendingOperation = "";

        if (data.hasExtra(EVENT_PENDING_OP_FIELD))
            pendingOperation = data.getStringExtra(EVENT_PENDING_OP_FIELD);

        long lastApiUpdate = data.getLongExtra(EVENT_LAST_UPDATE_FIELD, -1);

        switch (eventType) {
            case "ACTIVITY":
                event = new ActivityEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    timeOut,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "DOCTOR":
                event = new DoctorEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    timeOut,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "MEDICATION":
                event = new MedicationEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    timeOut,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "PERSONAL":
                event = new PersonalEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    timeOut,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "STIMULUS":
                event = new StimulusEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    timeOut,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "MOOD":
                event = new MoodEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    SURVEY_HIDE_TIME,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "TEXTFEEDBACK":
                event = new TextFeedbackEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    SURVEY_HIDE_TIME,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
                );
                break;
            case "TEXTNOFEEDBACK":
                event = new TextNoFeedbackEvent(
                    context,
                    eventId,
                    eventHour,
                    eventMinute,
                    eventDay,
                    eventMonth,
                    eventYear,
                    eventDescription,
                    intervalTime,
                    repetitionType,
                    repetitionStop,
                    timeOut,
                    prevAlarms,
                    pendingOperation,
                    lastApiUpdate
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
            if (deleteAlarm) ToastUtils.makeCustomToast(context, res.getString(R.string.error_deleting_alarm), DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
            else ToastUtils.makeCustomToast(context, res.getString(R.string.error_setting_alarm), DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
        }
    }

    //Method that transforms the received JSONObject into an Intent:
    public static Intent transformJsonToIntent (JSONObject json) {
        Intent eventData = new Intent();

        try {
            if (json.has("event")) json = json.getJSONObject("event");

            eventData.putExtra(EventUtils.EVENT_ID_FIELD, json.getLong(EVENT_ID_FIELD));
            eventData.putExtra(EventUtils.EVENT_TYPE_FIELD, json.getString(EVENT_TYPE_FIELD));
            eventData.putExtra(EventUtils.EVENT_DESCRIPTION_FIELD, json.getString(EVENT_DESCRIPTION_FIELD));

            //The event start date is retrieved as a long and transformed into hour, minute, day, month and year (only if it exists):
            if (json.has(EVENT_START_DATE_FIELD)) {
                long eventStartDate = json.getLong(EVENT_START_DATE_FIELD);

                int[] dateArray = DateUtils.transformFromMillis(eventStartDate);

                json.put(EVENT_HOUR_FIELD, dateArray[DateUtils.HOUR]);
                json.put(EVENT_MINUTE_FIELD, dateArray[DateUtils.MINUTE]);
                json.put(EVENT_DAY_FIELD, dateArray[DateUtils.DAY]);
                json.put(EVENT_MONTH_FIELD, dateArray[DateUtils.MONTH]);
                json.put(EVENT_YEAR_FIELD, dateArray[DateUtils.YEAR]);
            }

            eventData.putExtra(EventUtils.EVENT_HOUR_FIELD, json.getInt(EVENT_HOUR_FIELD));
            eventData.putExtra(EventUtils.EVENT_MINUTE_FIELD, json.getInt(EVENT_MINUTE_FIELD));
            eventData.putExtra(EventUtils.EVENT_DAY_FIELD, json.getInt(EVENT_DAY_FIELD));
            eventData.putExtra(EventUtils.EVENT_MONTH_FIELD, json.getInt(EVENT_MONTH_FIELD));
            eventData.putExtra(EventUtils.EVENT_YEAR_FIELD, json.getInt(EVENT_YEAR_FIELD));

            if (json.has(EVENT_PREV_ALARMS_FIELD)) eventData.putExtra(EventUtils.EVENT_PREV_ALARMS_FIELD, json.getString(EVENT_PREV_ALARMS_FIELD));
            if (json.has(EVENT_REP_INTERVAL_FIELD)) eventData.putExtra(EventUtils.EVENT_REP_INTERVAL_FIELD, json.getInt(EVENT_REP_INTERVAL_FIELD));
            if (json.has(EVENT_REPETITION_TYPE_FIELD)) eventData.putExtra(EventUtils.EVENT_REPETITION_TYPE_FIELD, json.getString(EVENT_REPETITION_TYPE_FIELD));
            if (json.has(EVENT_REPETITION_STOP_FIELD)) eventData.putExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, json.getLong(EVENT_REPETITION_STOP_FIELD));
            if (json.has(EVENT_PENDING_OP_FIELD)) eventData.putExtra(EventUtils.EVENT_PENDING_OP_FIELD, json.getString(EVENT_PENDING_OP_FIELD));
            if (json.has(EVENT_LAST_UPDATE_FIELD)) eventData.putExtra(EventUtils.EVENT_LAST_UPDATE_FIELD, json.getLong(EVENT_LAST_UPDATE_FIELD));
            if (json.has(EVENT_TIMEOUT_FIELD)) eventData.putExtra(EventUtils.EVENT_TIMEOUT_FIELD, json.getLong(EVENT_TIMEOUT_FIELD));

        } catch (JSONException jse) {
            Log.e(TAG, "transformJsonToIntent. JSONException: " + jse.getMessage());
        }

        return eventData;
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
}
