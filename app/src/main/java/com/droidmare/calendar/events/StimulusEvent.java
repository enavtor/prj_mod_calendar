package com.droidmare.calendar.events;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.reminders.model.Reminder;
import com.droidmare.common.utils.ImageUtils;

//Model for an event item (of type stimulus) declaration
//@author Eduardo on 18/03/2019.

public class StimulusEvent extends EventListItem {

    public StimulusEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private StimulusEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        eventType = Reminder.ReminderType.STIMULUS_REMINDER;

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
