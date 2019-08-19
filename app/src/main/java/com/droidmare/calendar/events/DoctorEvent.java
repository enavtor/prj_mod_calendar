package com.droidmare.calendar.events;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.reminders.model.Reminder;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;

//Model for an event item (of type doctor) declaration
//@author Eduardo on 27/02/2018.

public class DoctorEvent extends EventListItem {

    public DoctorEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private DoctorEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.DOCTOR_REMINDER;

        eventTypeTitle = resources.getString(R.string.doctor_reminder_title);

        notAnAlarm();

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "doctor_icon.png");
    }

    @Override
    public DoctorEvent getEventCopy (){
        DoctorEvent event = new DoctorEvent (
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

        event.notAnAlarm();
        return event;
    }
}
