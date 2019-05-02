package com.droidmare.calendar.events;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;
import com.droidmare.reminders.model.Reminder;

//Model for an event item (of type stimulus) declaration
//@author Eduardo on 18/03/2019.

public class StimulusEvent extends EventListItem {

    public StimulusEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    private StimulusEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.STIMULUS_REMINDER;

        externalAppPackage = "mobi.stimulus.stimulustv";
        externalAppActivity = "mobi.stimulus.stimulustv.SplashActivity";

        eventTypeTitle = resources.getString(R.string.stimulus_reminder_title);

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "stimulus_icon.png");
    }

    @Override
    public StimulusEvent getEventCopy (){
        return new StimulusEvent(
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
    }
}
