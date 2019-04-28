package com.shtvsolution.calendario.events.measures;

import android.content.Context;

import com.shtvsolution.R;
import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.utils.ImageUtils;
import com.shtvsolution.recordatorios.model.Reminder;

//Model for a measure event item (of type blood pressure) declaration
//@author Eduardo on 18/04/2018.

public class MeasureEventBP extends EventListItem {

    public MeasureEventBP(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    private MeasureEventBP(Context cont, long id, long apiId, int user, int hour, int minute, int day, int month, int year, String description, boolean instantly, int interval, String repetitionType, long nextRepetition, long stop, long timeOut, String prevAlarms, long lastUpdate, String pendingOp){
        super(cont, id, apiId, user, hour, minute, day, month, year, description, instantly, interval, repetitionType, nextRepetition, stop, timeOut, prevAlarms, lastUpdate, pendingOp);
    }

    @Override
    public void setNewEvent() {

        reminderType = Reminder.ReminderType.MEASURE_REMINDER_BP;

        externalAppPackage = "com.shtvsolution.medidas";
        externalAppActivity = "com.shtvsolution.medidas.BloodPressureActivity";

        eventTypeTitle = resources.getString(R.string.measure_reminder_title);

        setTitleText();

        eventIcon = ImageUtils.getImageFromAssets(context, "measure_bp_icon.png");
    }

    @Override
    public MeasureEventBP getEventCopy (){
        return new MeasureEventBP (
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
