package com.droidmare.calendar.models;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.droidmare.common.models.EventJsonObject;
import com.droidmare.common.utils.DateUtils;
import com.droidmare.common.models.ConstantValues;

//Model for an event item (item inside the event list) declaration
//@author Eduardo on 13/02/2018.

public abstract class EventListItem {

    //The id that the item has inside the database:
    protected String eventId;

    //The type of the reminder:
    protected String eventType;

    //The event type title:
    protected String eventTypeTitle;

    //Item's title text:
    private String titleText;

    //Item's description text:
    protected String descriptionText;

    //The icon for the event:
    protected Drawable eventIcon;

    //The hour for the event alarm:
    protected int eventHour;

    //The minute for the event alarm:
    protected int eventMinute;

    //The day for the event alarm:
    protected int eventDay;

    //The month for the event alarm:
    protected int eventMonth;

    //The year for the event alarm:
    protected int eventYear;

    //The time interval for repeating the alarm (in hours):
    protected int intervalTime;

    //An string formatted as a Json indicating the type of repetition (daily, weekly, alternated, etc) and the
    //configuration of the repetition (for example, if the repetition is weekly, the structure of this field will be:
    //{"type": "2", "config": [1, 3, 5]}, meaning that the alarm will be repeated every monday, wednesday and friday):
    protected String repetitionType;

    //The date (in milliseconds) of the next repetition that is going to take place:
    protected long nextRepetition;

    //End date of the repetition (the repetition starts and ends at the same hour):
    protected long repetitionStop;

    //The external app that must be opened when the reminder is accepted:
    protected String externalAppPackage;
    protected String externalAppActivity;

    //The time out for the reminder (time that the reminder will be shown before auto hiding)
    protected long reminderTimeOut;

    //The list of alarms previous to the reminder date (String structured as a JSONArray):
    protected String previousAlarms;

    //A field indicating whether or not the event has any pending operation with the API:
    protected String pendingOperation;

    //The UTC date of the last update in the API for the event:
    protected long lastApiUpdate;

    //The resources of the application:
    protected Resources resources;

    //The context for creating a new alarm:
    protected Context context;

    //A control variable modified when the event is clicked inside the event list, so the adapter can know that the event menu view must be displayed:
    private boolean showEventMenu;

    //A reference to the original event for the events that are a copy (those created in order to appropriately display the events with repetition):
    private EventListItem originalEvent;

    //A field that indicates if the event is an alarm or an appointment
    private boolean isAlarm;

    public EventListItem (){
        //The id will be set once the event is stored in the database:
        this.eventId = "";
        this.eventType = null;
        this.titleText = null;
        this.descriptionText = null;
        this.eventIcon = null;
        this.eventHour = -1;
        this.eventMinute = -1;
        this.eventDay = -1;
        this.eventMonth = -1;
        this.eventYear = -1;
        this.intervalTime = 0;
        this.repetitionType = "";
        this.nextRepetition = -1;
        this.repetitionStop = -1;
        this.externalAppPackage = null;
        this.externalAppActivity = null;
        this.reminderTimeOut = -1;
        this.previousAlarms = "";
        this.pendingOperation = "";
        this.lastApiUpdate = -1;
        this.context = null;
        this.showEventMenu = false;
        this.originalEvent = null;
        this.isAlarm = true;
    }

    public EventListItem (Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String previousAlarms, String pendingOp, long lastUpdate) {
        this.context = cont;
        this.eventId = id;
        this.eventHour = hour;
        this.eventMinute = minute;
        this.eventDay = day;
        this.eventMonth = month;
        this.eventYear = year;
        this.descriptionText = description;
        this.intervalTime = interval;
        this.repetitionType = repetitionType;
        this.repetitionStop = stop;
        this.reminderTimeOut = timeOut;
        this.previousAlarms = previousAlarms;
        this.pendingOperation = pendingOp;
        this.lastApiUpdate = lastUpdate;
        this.isAlarm = true;

        //Now that the context is set, the resources can be obtained
        setResources();
    }

    protected EventListItem (Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String previousAlarms, String pendingOp, long lastUpdate){
        this.context = cont;
        this.eventId = id;
        this.eventHour = hour;
        this.eventMinute = minute;
        this.eventDay = day;
        this.eventMonth = month;
        this.eventYear = year;
        this.descriptionText = description;
        this.intervalTime = interval;
        this.repetitionType = repetitionType;
        this.nextRepetition = nextRepetition;
        this.repetitionStop = stop;
        this.reminderTimeOut = timeOut;
        this.previousAlarms = previousAlarms;
        this.pendingOperation = pendingOp;
        this.lastApiUpdate = lastUpdate;
        this.isAlarm = true;

        //Now that the context is set, the resources can be obtained
        setResources();
    }

    private void setResources () { resources = context.getResources(); }

    public void setEventId(String id) { eventId = id; }

    public String getEventId() { return eventId; }

    public String getTitleText() { return titleText; }

    public String getFullTitleText() {
        return getDateText(true, true) + " - " + eventTypeTitle;
    }

    //Function that updates the date parameters of the event and resets the title text and the event (it is used after creating a copy of the event):
    public boolean updateDateParams(long nextRepetition) {

        if (nextRepetition == -1) return false;

        int[] nextRepetitionDate = DateUtils.transformFromMillis(nextRepetition);

        //The minute is always the same as the original event's one, so it is not necessary to change it:
        this.eventHour = nextRepetitionDate[DateUtils.HOUR];
        this.eventDay = nextRepetitionDate[DateUtils.DAY];
        this.eventMonth = nextRepetitionDate[DateUtils.MONTH];
        this.eventYear = nextRepetitionDate[DateUtils.YEAR];

        setTitleText();

        setNewEvent();

        return true;
    }

    //Function for setting the event's title text, formatting the event date appropriately:
    protected void setTitleText () {

        titleText = getDateText(false, false) + " - " + eventTypeTitle;
    }

    //Function for setting the event's date text, formatting it appropriately:
    private String getDateText (boolean getFullDate, boolean getNextRepDate) {

        String dateText = "";
        String timeText;

        if (getFullDate) {

            if (getNextRepDate && intervalTime != 0) {
                int[] nextRepetitionArray = DateUtils.transformFromMillis(getNextRepetition());
                dateText = DateUtils.formatDateText(nextRepetitionArray[DateUtils.DAY], nextRepetitionArray[DateUtils.MONTH], nextRepetitionArray[DateUtils.YEAR]);
                timeText = " - " + DateUtils.formatTimeString(nextRepetitionArray[DateUtils.HOUR]) + ":" + DateUtils.formatTimeString(nextRepetitionArray[DateUtils.MINUTE]);
            }
            else {
                dateText = DateUtils.formatDateText(eventDay, eventMonth, eventYear);
                timeText = " - " + DateUtils.formatTimeString(eventHour) + ":" + DateUtils.formatTimeString(eventMinute);
            }
        }

        else timeText = DateUtils.formatTimeString(eventHour) + ":" + DateUtils.formatTimeString(eventMinute);

        return dateText + timeText;
    }

    public String getDescriptionText() { return descriptionText; }

    public Drawable getEventIcon() { return eventIcon; }

    public String getEventType() { return eventType; }

    public int getEventHour() { return eventHour; }

    public int getEventMinute() { return eventMinute; }

    public int getEventDay() { return eventDay; }

    public int getEventMonth() { return eventMonth; }

    public int getEventYear() { return eventYear; }

    public int getIntervalTime() { return intervalTime; }

    public String getRepetitionType() { return repetitionType; }

    public void setNextRepetition(long nextRepetition) { this.nextRepetition = nextRepetition; }

    //Method that returns the event's next repetition (if nextRepetition value is equals to -1, this method returns the start date millis):
    public long getNextRepetition() {
        long nextRepetition = this.nextRepetition;

        if (nextRepetition <= 0)
            nextRepetition = DateUtils.transformToMillis(eventMinute, eventHour, eventDay, eventMonth, eventYear);

        return nextRepetition;
    }

    public long getRepetitionStop() { return repetitionStop; }

    long getReminderTimeOut() { return reminderTimeOut; }

    String getPreviousAlarms() { return previousAlarms; }

    public String getPendingOperation() {return pendingOperation; }

    public void setPendingOperation(String pendingOp) {pendingOperation = pendingOp; }

    public long getLastApiUpdate() {return lastApiUpdate; }

    public void setLastApiUpdate(long lastUpdate) { lastApiUpdate = lastUpdate; }

    public void showEventMenu(boolean showMenu) {
        this.showEventMenu = showMenu;
    }

    public boolean showEventMenu() { return showEventMenu; }

    protected void notAnAlarm() { isAlarm = false; }

    boolean isAlarm() { return isAlarm; }

    public void referenceOriginalEvent(EventListItem originalEvent) {
        this.originalEvent = originalEvent;
    }

    public EventListItem getOriginalEvent() { return originalEvent; }

    //Function for checking if the item is out of date:
    public boolean outOfDate(boolean displayingAllEvents) {

        boolean outOfDate;

        if (intervalTime != 0 && displayingAllEvents) {
            int[] nextRepArray = DateUtils.transformFromMillis(nextRepetition);
            outOfDate = DateUtils.outOfDate(
                nextRepArray[DateUtils.YEAR],
                nextRepArray[DateUtils.MONTH],
                nextRepArray[DateUtils.DAY],
                nextRepArray[DateUtils.HOUR],
                nextRepArray[DateUtils.MINUTE]
            );
        }

        else outOfDate = DateUtils.outOfDate(eventYear, eventMonth, eventDay, eventHour, eventMinute);

        return outOfDate;
    }

    //Function for updating the event parameters (just the ones that doesn't depend on the event type):
    public boolean updateEventParams(EventJsonObject eventJson){

        boolean eventUpdated = false;

        int newEventHour = eventJson.getInt(ConstantValues.EVENT_HOUR_FIELD, -1);
        int newEventMinute = eventJson.getInt(ConstantValues.EVENT_MINUTE_FIELD, -1);

        int newIntervalTime = eventJson.getInt(ConstantValues.EVENT_REP_INTERVAL_FIELD, 0);
        long newRepetitionStop = eventJson.getLong(ConstantValues.EVENT_REPETITION_STOP_FIELD, -1);

        int newEventDay = eventJson.getInt(ConstantValues.EVENT_DAY_FIELD, -1);
        int newEventMonth = eventJson.getInt(ConstantValues.EVENT_MONTH_FIELD, -1);
        int newEventYear = eventJson.getInt(ConstantValues.EVENT_YEAR_FIELD, -1);

        String newDescriptionText = eventJson.getString(ConstantValues.EVENT_DESCRIPTION_FIELD, "");

        String newPreviousAlarms = eventJson.getPreviousAlarmsArray().toString();

        String newRepetitionType = eventJson.getRepetitionTypeJson().toString();

        if (eventHour != newEventHour) {
            this.eventHour = newEventHour;
            eventUpdated = true;
        }

        if (eventMinute != newEventMinute) {
            this.eventMinute = newEventMinute;
            eventUpdated = true;
        }

        if (intervalTime != newIntervalTime) {
            this.intervalTime = newIntervalTime;
            eventUpdated = true;
        }

        if (repetitionStop != newRepetitionStop) {
            this.repetitionStop = newRepetitionStop;
            eventUpdated = true;
        }

        if (eventDay != newEventDay) {
            this.eventDay = newEventDay;
            eventUpdated = true;
        }

        if (eventMonth != newEventMonth) {
            this.eventMonth = newEventMonth;
            eventUpdated = true;
        }

        if (eventYear != newEventYear) {
            this.eventYear = newEventYear;
            eventUpdated = true;
        }

        if (!descriptionText.equals(newDescriptionText)) {
            this.descriptionText = newDescriptionText;
            eventUpdated = true;
        }

        if (!previousAlarms.equals(newPreviousAlarms)) {
            this.previousAlarms = newPreviousAlarms;
            eventUpdated = true;
        }

        if (!repetitionType.equals(newRepetitionType)) {
            this.repetitionType = newRepetitionType;
            eventUpdated = true;
        }

        if (eventUpdated) this.setTitleText();

        return eventUpdated;
    }

    //Function for creating a reminder for an event:
    public String createNewReminder() {

        EventJsonObject eventJson = CalEventJsonObject.createEventJson(this);

        //When an external app must be opened, the package and activity are included in the additional options bundle:
        if (externalAppPackage != null) {
            eventJson.put(ConstantValues.PACKAGE_NAME, externalAppPackage);
            eventJson.put(ConstantValues.ACTIVITY_NAME, externalAppActivity);
        }

        return eventJson.toString();
    }

    //Function for transforming an event into a String:
    public String eventToString () {

        String type = "Event_Type: " + eventType;
        String id = "Event_Id: " + eventId;
        String date = "Event_Date: " + getDateText(true, false);
        String description = "Event_Description: " + descriptionText;

        String prevAlarms = "Event_Previous_Alarms: " + previousAlarms.replace("\\", "");

        String repetition = "Event_Has_Repetition: " + false;

        if (intervalTime != 0)
            repetition = "Event_Has_Repetition: " + true + " (Repetition_Interval: " + intervalTime + ", Repetition_Type: " +  repetitionType + ", Next_Repetition: " + nextRepetition + ")";

        String stop = "Event_Has_Repetition_Stop: " + false;

        if (repetitionStop != -1) {

            int [] repetitionStopDate = DateUtils.transformFromMillis(repetitionStop);

            String dateText = DateUtils.formatTimeString(repetitionStopDate[DateUtils.DAY])
                    + "/" + DateUtils.formatTimeString(repetitionStopDate[DateUtils.MONTH] + 1)
                    + "/" + repetitionStopDate[DateUtils.YEAR];

            String timeText = " - " + DateUtils.formatTimeString(repetitionStopDate[DateUtils.HOUR]) + ":" + DateUtils.formatTimeString(repetitionStopDate[DateUtils.MINUTE]);

            stop = "Event_Has_Repetition_Stop: " + true + " (Stop_Date: " + dateText + timeText + ")";
        }

        String SPACE = "  ,,  ";

        return type + SPACE + id + SPACE  + date + SPACE + description + SPACE + prevAlarms + SPACE + repetition + SPACE + stop + SPACE + getLastApiUpdate();
    }

    //Function for defining a new event:
    public abstract void setNewEvent ();

    //Function for returning a copy of the event:
    public abstract EventListItem getEventCopy ();
}
