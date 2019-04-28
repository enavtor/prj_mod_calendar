package com.shtvsolution.calendario.events.personal;

import android.content.Context;

import com.shtvsolution.R;
import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.utils.ImageUtils;
import com.shtvsolution.recordatorios.model.Reminder;

//Model for an event item (of type text feedback) declaration
//@author Eduardo on 7/05/2018.

public class TextNoFeedbackEvent extends EventListItem {

    public TextNoFeedbackEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    private TextNoFeedbackEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
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
