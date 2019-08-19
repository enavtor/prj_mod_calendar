package com.droidmare.calendar.events.personal;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;
import com.droidmare.reminders.model.Reminder;

//Model for an event item (of type text feedback) declaration
//@author Eduardo on 7/05/2018.

public class TextNoFeedbackEvent extends EventListItem {

    public TextNoFeedbackEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private TextNoFeedbackEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.TEXTNOFEEDBACK_REMINDER;

        eventTypeTitle = resources.getString(R.string.textnofeedback_reminder_title);

        setTitleText();

        //The icon should be changed in a future:
        eventIcon = ImageUtils.getImageFromAssets(context, "textnofeedback_icon.png");
    }

    @Override
    public TextNoFeedbackEvent getEventCopy (){
        return new TextNoFeedbackEvent (
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
