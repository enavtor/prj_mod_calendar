package com.droidmare.calendar.events;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.reminders.model.Reminder;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;

//Model for an event item (of type activity) declaration
//@author Eduardo on 27/02/2018.

public class ActivityEvent extends EventListItem {

    public ActivityEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private ActivityEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.ACTIVITY_REMINDER;

        eventTypeTitle = resources.getString(R.string.activity_reminder_title);

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "activity_icon.png");
    }

    @Override
    public ActivityEvent getEventCopy (){
        return new ActivityEvent (
            this.context,
            this.eventId,
            this.eventHour,
            this.eventMinute,
            this.eventDay,
            this.eventMonth,
            this.eventYear,
            this.descriptionText,
            this.intervalTime,
            this.repetitionType,
            this.nextRepetition,
            this.repetitionStop,
            this.reminderTimeOut,
            this.previousAlarms,
            this.pendingOperation,
            this.lastApiUpdate
        );
    }
}
