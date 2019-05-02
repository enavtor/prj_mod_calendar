package com.droidmare.calendar.events.personal;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.reminders.model.Reminder;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;

//Model for an event item (of type personal) declaration
//@author Eduardo on 27/02/2018.

public class PersonalEvent extends EventListItem {

    public PersonalEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    private PersonalEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.PERSONAL_REMINDER;

        eventTypeTitle = resources.getString(R.string.personal_reminder_title);

        notAnAlarm();

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "personal_icon.png");
    }

    @Override
    public PersonalEvent getEventCopy (){
        PersonalEvent event = new PersonalEvent (
                this.context,
                this.eventId,
                this.eventApiId,
                this.userId,
                this.eventHour,
                this.eventMinute,
                this.eventDay,
                this.eventMonth,
                this.eventYear,
                this.descriptionText,
                this.instantlyShown,
                this.intervalTime,
                this.repetitionType,
                this.nextRepetition,
                this.repetitionStop,
                this.reminderTimeOut,
                this.previousAlarms,
                this.lastApiUpdate,
                this.pendingOperation
        );

        event.notAnAlarm();
        return event;
    }
}