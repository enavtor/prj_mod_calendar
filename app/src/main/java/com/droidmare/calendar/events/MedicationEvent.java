package com.droidmare.calendar.events;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.events.measures.MeasureEventBP;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.ImageUtils;
import com.droidmare.reminders.model.Reminder;

//Model for an event item (of type medication) declaration
//@author Eduardo on 27/02/2018.

public class MedicationEvent extends EventListItem {

    public MedicationEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    private MedicationEvent(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.MEDICATION_REMINDER;

        eventTypeTitle = resources.getString(R.string.medication_reminder_title);

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "medication_icon.png");
    }

    @Override
    public MedicationEvent getEventCopy (){
        return new MedicationEvent (
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
