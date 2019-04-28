package com.shtvsolution.calendario.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;

import com.shtvsolution.R;
import com.shtvsolution.calendario.events.ActivityEvent;
import com.shtvsolution.calendario.events.DoctorEvent;
import com.shtvsolution.calendario.events.StimulusEvent;
import com.shtvsolution.calendario.events.surveys.TextFeedbackEvent;
import com.shtvsolution.calendario.events.measures.MeasureEventBG;
import com.shtvsolution.calendario.events.measures.MeasureEventBP;
import com.shtvsolution.calendario.events.measures.MeasureEventHR;
import com.shtvsolution.calendario.events.measures.MeasureEventXX;
import com.shtvsolution.calendario.events.MedicationEvent;
import com.shtvsolution.calendario.events.surveys.MoodEvent;
import com.shtvsolution.calendario.events.personal.PersonalEvent;
import com.shtvsolution.calendario.events.personal.TextNoFeedbackEvent;
import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.services.UserDataReceiverService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;

import static android.support.v4.content.ContextCompat.startActivity;
import static com.shtvsolution.calendario.utils.ToastUtils.DEFAULT_TOAST_DURATION;
import static com.shtvsolution.calendario.utils.ToastUtils.DEFAULT_TOAST_SIZE;
import static java.lang.Math.pow;

//Utils for creating event objects and alarms
//@author Eduardo on 08/02/2018.

public class EventUtils {

    private static final String TAG = EventUtils.class.getCanonicalName();

    //Default reminder timeout (the reminder will be automatically hidden after 30 seconds):
    public static final long DEFAULT_HIDE_TIME = 30 * 1000;

    //Survey type reminders timeout (the reminder will be automatically hidden after 90 seconds):
    public static final long SURVEY_HIDE_TIME = 90 * 1000;

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
    public static final String EVENT_API_ID_FIELD = "eventApiId";
    public static final String EVENT_USER_FIELD = "eventUser";
    public static final String EVENT_TYPE_FIELD = "eventType";
    public static final String EVENT_MINUTE_FIELD = "eventMinute";
    public static final String EVENT_HOUR_FIELD = "eventHour";
    public static final String EVENT_DAY_FIELD = "eventDay";
    public static final String EVENT_MONTH_FIELD = "eventMonth";
    public static final String EVENT_YEAR_FIELD = "eventYear";
    public static final String EVENT_INTERVAL_FIELD = "eventInterval";
    public static final String EVENT_REPETITION_STOP_FIELD = "eventRepetitionStop";
    public static final String EVENT_INSTANTLY_FIELD = "eventInstantly";
    public static final String EVENT_DESCRIPTION_FIELD = "eventDescription";
    public static final String EVENT_TIMEOUT_FIELD = "eventTimeOut";
    public static final String EVENT_REP_TYPE_FIELD = "eventRepetitionType";
    public static final String EVENT_PREV_ALARMS_FIELD = "eventPrevAlarms";
    public static final String EVENT_LAST_UPDATE_FIELD = "eventLastUpdate";
    public static final String EVENT_PENDING_OP_FIELD = "eventPendingOp";

    //Function for making an event object from a Json:
    public static  EventListItem makeEvent (Context context, JSONObject json, boolean externallyReceived) {
        return makeEvent(context, transformJsonToIntent(json, externallyReceived));
    }

    //Function for making an event object from an Intent:
    public static EventListItem makeEvent (Context context, Intent data) {

        EventListItem event = null;

        long eventId = data.getLongExtra(EVENT_ID_FIELD, -1);

        long eventApiId = data.getLongExtra(EVENT_API_ID_FIELD, -1);

        //The user id is stored when a user logs into the accounts app, which sends the user's events and id to the EventReceiverActivity:
        int userId = data.getIntExtra(EVENT_USER_FIELD, UserDataReceiverService.getUserId());

        String eventType = data.getStringExtra(EVENT_TYPE_FIELD);

        int eventHour = data.getIntExtra(EVENT_HOUR_FIELD, -1);
        int eventMinute = data.getIntExtra(EVENT_MINUTE_FIELD, -1);
        int eventDay = data.getIntExtra(EVENT_DAY_FIELD, DateUtils.currentDay);
        int eventMonth = data.getIntExtra(EVENT_MONTH_FIELD, DateUtils.currentMonth);
        int eventYear = data.getIntExtra(EVENT_YEAR_FIELD, DateUtils.currentYear);

        String eventDescription = data.getStringExtra(EVENT_DESCRIPTION_FIELD);
        boolean instantlyShown = data.getBooleanExtra(EVENT_INSTANTLY_FIELD, false);
        int intervalTime = data.getIntExtra(EVENT_INTERVAL_FIELD, 0);

        String repetitionType = "";
        long repetitionStop = -1;

        if (intervalTime != 0) {
            data.getLongExtra(EVENT_REPETITION_STOP_FIELD, -1);

            //If the event has a repetition interval, the repetition type is initialize:
            if (data.hasExtra(EVENT_REP_TYPE_FIELD) && !data.getStringExtra(EVENT_REP_TYPE_FIELD).equals("")) {
                repetitionType = data.getStringExtra(EVENT_REP_TYPE_FIELD);
            }
            //If the intent has no data for the repetition type, it is initialize as its default value (daily repetition):
            else repetitionType = DEFAULT_REPETITION_TYPE;
        }

        long timeOut = data.getLongExtra(EVENT_TIMEOUT_FIELD, DEFAULT_HIDE_TIME);

        String prevAlarms = "";

        if (data.hasExtra(EVENT_PREV_ALARMS_FIELD)) {
            prevAlarms = data.getStringExtra(EVENT_PREV_ALARMS_FIELD);
        }

        long lastUpdate = data.getLongExtra(EVENT_LAST_UPDATE_FIELD, -1);

        String pendingOperation = "";

        if (data.hasExtra(EVENT_PENDING_OP_FIELD))
            pendingOperation = data.getStringExtra(EVENT_PENDING_OP_FIELD);

        //The user data shared preferences are read so the available measures for the current users can be retrieved:
        UserDataReceiverService.readSharedPrefs(context);

        //The info about the interval and repetition stop is displayed on the log only for those events that have an id:
        if (intervalTime != 0) {
            repetitionStop = data.getLongExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, -1);

            if (repetitionStop != -1) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(repetitionStop);

                //A log entry will be produced with the repetition config:
                /*Log.d("Repetition", "every " + intervalTime + "h " +
                        "until: " + calendar.get(Calendar.DAY_OF_MONTH) +
                        "/" + (calendar.get(Calendar.MONTH) + 1) +
                        "/" + calendar.get(Calendar.YEAR) +
                        " - " + calendar.get(Calendar.HOUR_OF_DAY) +
                        ":" + calendar.get(Calendar.MINUTE));*/
            }
            else; //Log.d("Repetition", "every " + intervalTime + "h" + " (with no ending)");
        }
        else; //Log.d("Repetition", "no repetition set");

        switch (eventType) {
            case "ACTIVITY":
                event = new ActivityEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        timeOut,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "MEASURE_HR":
                if (UserDataReceiverService.hasMeasure(eventType))
                    event = new MeasureEventHR(
                            context,
                            eventId,
                            eventApiId,
                            userId,
                            eventHour,
                            eventMinute,
                            eventDay,
                            eventMonth,
                            eventYear,
                            eventDescription,
                            instantlyShown,
                            intervalTime,
                            repetitionType,
                            repetitionStop,
                            timeOut,
                            prevAlarms,
                            lastUpdate,
                            pendingOperation
                    );
                break;
            case "MEASURE_BP":
                if (UserDataReceiverService.hasMeasure(eventType))
                    event = new MeasureEventBP(
                            context,
                            eventId,
                            eventApiId,
                            userId,
                            eventHour,
                            eventMinute,
                            eventDay,
                            eventMonth,
                            eventYear,
                            eventDescription,
                            instantlyShown,
                            intervalTime,
                            repetitionType,
                            repetitionStop,
                            timeOut,
                            prevAlarms,
                            lastUpdate,
                            pendingOperation
                    );
                break;
            case "MEASURE_BG":
                if (UserDataReceiverService.hasMeasure(eventType))
                    event = new MeasureEventBG(
                            context,
                            eventId,
                            eventApiId,
                            userId,
                            eventHour,
                            eventMinute,
                            eventDay,
                            eventMonth,
                            eventYear,
                            eventDescription,
                            instantlyShown,
                            intervalTime,
                            repetitionType,
                            repetitionStop,
                            timeOut,
                            prevAlarms,
                            lastUpdate,
                            pendingOperation
                    );
                break;
            case "MEASURE_XX":
                if (UserDataReceiverService.hasMeasure(eventType))
                    event = new MeasureEventXX(
                            context,
                            eventId,
                            eventApiId,
                            userId,
                            eventHour,
                            eventMinute,
                            eventDay,
                            eventMonth,
                            eventYear,
                            eventDescription,
                            instantlyShown,
                            intervalTime,
                            repetitionType,
                            repetitionStop,
                            timeOut,
                            prevAlarms,
                            lastUpdate,
                            pendingOperation
                    );
                break;
            case "DOCTOR":
                event = new DoctorEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        timeOut,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "MEDICATION":
                event = new MedicationEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        timeOut,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "PERSONAL":
                event = new PersonalEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        timeOut,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "STIMULUS":
                event = new StimulusEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        timeOut,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "MOOD":
                event = new MoodEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        SURVEY_HIDE_TIME,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "TEXTFEEDBACK":
                event = new TextFeedbackEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        SURVEY_HIDE_TIME,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
                );
                break;
            case "TEXTNOFEEDBACK":
                event = new TextNoFeedbackEvent(
                        context,
                        eventId,
                        eventApiId,
                        userId,
                        eventHour,
                        eventMinute,
                        eventDay,
                        eventMonth,
                        eventYear,
                        eventDescription,
                        instantlyShown,
                        intervalTime,
                        repetitionType,
                        repetitionStop,
                        timeOut,
                        prevAlarms,
                        lastUpdate,
                        pendingOperation
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
    public static Intent transformJsonToIntent (JSONObject json, boolean externallyReceived) {
        Intent eventData = new Intent();

        try {
            if (json.has("event")) json = json.getJSONObject("event");

            if (externallyReceived) eventData.putExtra(EventUtils.EVENT_API_ID_FIELD, json.getLong("eventId"));

            else {
                eventData.putExtra(EventUtils.EVENT_ID_FIELD, json.getLong("eventId"));
                eventData.putExtra(EventUtils.EVENT_API_ID_FIELD, json.getLong("eventApiId"));
            }

            String eventType = json.getString("eventType");
            if (eventType.equals("HOSPITAL")) json.put("eventType", "DOCTOR");

            //The event start date is retrieved as a long and transformed into hour, minute, day, month and year (only if it exists):
            if (json.has("eventStartDate")) {
                long eventStartDate = json.getLong("eventStartDate");

                //If the start date comes in seconds it must be converted to milliseconds:
                if (eventStartDate / pow(10,12) < 1) eventStartDate *= 1000;

                int[] dateArray = DateUtils.transformFromMillis(eventStartDate);

                json.put("eventHour", dateArray[DateUtils.HOUR]);
                json.put("eventMinute", dateArray[DateUtils.MINUTE]);
                json.put("reminderDay", dateArray[DateUtils.DAY]);
                json.put("reminderMonth", dateArray[DateUtils.MONTH]);
                json.put("reminderYear", dateArray[DateUtils.YEAR]);
            }

            eventData.putExtra(EventUtils.EVENT_TYPE_FIELD, json.getString("eventType"));
            eventData.putExtra(EventUtils.EVENT_HOUR_FIELD, json.getInt("eventHour"));
            eventData.putExtra(EventUtils.EVENT_MINUTE_FIELD, json.getInt("eventMinute"));

            if (json.has("reminderDay")){
                eventData.putExtra(EventUtils.EVENT_DAY_FIELD, json.getInt("reminderDay"));
                eventData.putExtra(EventUtils.EVENT_MONTH_FIELD, json.getInt("reminderMonth"));
                eventData.putExtra(EventUtils.EVENT_YEAR_FIELD, json.getInt("reminderYear"));
            }
            else {
                eventData.putExtra(EventUtils.EVENT_DAY_FIELD, json.getInt(EVENT_DAY_FIELD));
                eventData.putExtra(EventUtils.EVENT_MONTH_FIELD, json.getInt(EVENT_MONTH_FIELD));
                eventData.putExtra(EventUtils.EVENT_YEAR_FIELD, json.getInt(EVENT_YEAR_FIELD));
            }

            if (json.has("descriptionText")) {
                eventData.putExtra(EventUtils.EVENT_DESCRIPTION_FIELD, json.getString("descriptionText"));
                //The next four fields are optional, so they may not exist and therefore their existence must be checked before retrieving them from the JSON object:
                if (json.has("instantlyShown"))  eventData.putExtra(EventUtils.EVENT_INSTANTLY_FIELD, json.getBoolean("instantlyShown"));
                if (json.has("intervalTime")) eventData.putExtra(EventUtils.EVENT_INTERVAL_FIELD, json.getInt("intervalTime"));
                if (json.has("eventStopDate")) {
                    long eventStopDate = json.getLong("eventStopDate");
                    //If the stop date comes in seconds it must be converted to milliseconds:
                    if (eventStopDate != -1 && eventStopDate / pow(10,12) < 1) eventStopDate *= 1000;
                    eventData.putExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, eventStopDate);
                }
                if (json.has("timeOut")) eventData.putExtra(EventUtils.EVENT_TIMEOUT_FIELD, json.getLong("timeOut"));
                if (json.has("lastUpdate")) eventData.putExtra(EventUtils.EVENT_LAST_UPDATE_FIELD, json.getLong("lastUpdate"));
            }
            else {
                eventData.putExtra(EventUtils.EVENT_DESCRIPTION_FIELD, json.getString(EVENT_DESCRIPTION_FIELD));
                //The next four fields are optional, so they may not exist and therefore their existence must be checked before retrieving them from the JSON object:
                if (json.has(EVENT_INSTANTLY_FIELD))  {
                    //SQLite doesn't have boolean type, so the booleans were stored as ints and must be reconverted:
                    boolean instantly = json.getInt(EventUtils.EVENT_INSTANTLY_FIELD) != 0;
                    eventData.putExtra(EventUtils.EVENT_INSTANTLY_FIELD, instantly);
                }
                if (json.has(EVENT_INTERVAL_FIELD)) eventData.putExtra(EventUtils.EVENT_INTERVAL_FIELD, json.getInt(EVENT_INTERVAL_FIELD));
                if (json.has(EVENT_REPETITION_STOP_FIELD)) eventData.putExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, json.getLong(EVENT_REPETITION_STOP_FIELD));
                if (json.has(EVENT_TIMEOUT_FIELD)) eventData.putExtra(EventUtils.EVENT_TIMEOUT_FIELD, json.getLong(EVENT_TIMEOUT_FIELD));
                if (json.has(EVENT_REP_TYPE_FIELD)) eventData.putExtra(EventUtils.EVENT_REP_TYPE_FIELD, json.getString(EVENT_REP_TYPE_FIELD));
                if (json.has(EVENT_PREV_ALARMS_FIELD)) eventData.putExtra(EventUtils.EVENT_PREV_ALARMS_FIELD, json.getString(EVENT_PREV_ALARMS_FIELD));
                if (json.has(EVENT_LAST_UPDATE_FIELD)) eventData.putExtra(EventUtils.EVENT_LAST_UPDATE_FIELD, json.getLong(EVENT_LAST_UPDATE_FIELD));
                if (json.has(EVENT_PENDING_OP_FIELD)) eventData.putExtra(EventUtils.EVENT_PENDING_OP_FIELD, json.getString(EVENT_PENDING_OP_FIELD));
            }

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