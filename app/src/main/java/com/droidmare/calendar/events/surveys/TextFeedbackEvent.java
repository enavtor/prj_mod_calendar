package com.droidmare.calendar.events.surveys;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;
import com.droidmare.reminders.model.Reminder;

//Model for an event item (of type text feedback) declaration
//@author Eduardo on 7/05/2018.

public class TextFeedbackEvent extends EventListItem {

    public TextFeedbackEvent(Context cont, long id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private TextFeedbackEvent(Context cont, long id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.TEXTFEEDBACK_REMINDER;

        eventTypeTitle = resources.getString(R.string.textfeedback_reminder_title);

        setTitleText();

        //The icon should be changed in a future:
        eventIcon = ImageUtils.getImageFromAssets(context, "textfeedback_icon.png");
    }

    @Override
    public TextFeedbackEvent getEventCopy (){
        return new TextFeedbackEvent (
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
