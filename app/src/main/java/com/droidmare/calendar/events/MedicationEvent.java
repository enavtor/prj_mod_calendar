package com.droidmare.calendar.events;

import android.content.Context;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.common.models.ConstantValues;
import com.droidmare.common.utils.ImageUtils;

//Model for an event item (of type medication) declaration
//@author Eduardo on 27/02/2018.

public class MedicationEvent extends EventListItem {

    public MedicationEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    private MedicationEvent(Context cont, String id, int hour, int minute, int day, int month, int year, String description, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, String pendingOp, long lastUpdate){
        super(cont, id, hour, minute, day, month, year, description, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, pendingOp, lastUpdate);
    }

    @Override
    public void setNewEvent() {

        eventType = ConstantValues.MEDICATION_EVENT_TYPE;

        eventTypeTitle = resources.getString(R.string.medication_reminder_title);

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "medication_icon.png");
    }

    @Override
    public MedicationEvent getEventCopy (){
        return new MedicationEvent (
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
