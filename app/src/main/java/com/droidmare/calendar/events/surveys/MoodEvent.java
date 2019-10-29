package com.droidmare.calendar.events.surveys;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.reminders.model.Reminder;
import com.shtvsolution.common.utils.ImageUtils;

//Model for an event item (of type mood) declaration
//@author Eduardo on 27/02/2018.

public class MoodEvent extends EventListItem {

    public MoodEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private MoodEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.MOOD_REMINDER;

        eventTypeTitle = resources.getString(R.string.mood_reminder_title);

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "mood_icon.png");
    }

    @Override
    public MoodEvent getEventCopy (){
        return new MoodEvent (
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
