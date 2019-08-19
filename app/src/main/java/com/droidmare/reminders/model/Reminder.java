package com.droidmare.reminders.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * An API for communicating with the reminder module in order to send a reminder in form of a byte array
 * that the reminder module will transform into a Reminder object again so it can be added to an alarm.
 *
 * @author Eduardo on 28/02/2018
 */

public class Reminder implements Parcelable{

    /** Types of reminder */
    public enum ReminderType{
        MEDICATION_REMINDER,
        ACTIVITY_REMINDER,
        PERSONAL_REMINDER,
        MEASURE_REMINDER_XX,
        MEASURE_REMINDER_BP,
        MEASURE_REMINDER_BG,
        MEASURE_REMINDER_HR,
        DOCTOR_REMINDER,
        TEXTFEEDBACK_REMINDER,
        TEXTNOFEEDBACK_REMINDER,
        CALL_REMINDER,
        MOOD_REMINDER,
        STIMULUS_REMINDER
    }

    /** Type of reminder */
    private ReminderType type;

    /** Number of reminder. Required for adding different alarms in AlarmManager */
    private String number;

    /** Day of reminder */
    private int day;

    /** Month of reminder */
    private int month;

    /** Year of reminder */
    private int year;

    /** Hour of reminder */
    private int hour;

    /** Minute of reminder */
    private int minute;

    /** Additional information of reminder */
    private String additionalInfo;

    /** Additional options for the reminder */
    private Bundle additionalOptions;

    public Reminder(String number, ReminderType type, int day, int month, int year, int hour, int minute, String additionalInfo) {
        this.number = number;
        this.type = type;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.additionalInfo = additionalInfo;
        this.additionalOptions = null;
    }

    public Reminder(String number, ReminderType type, int day, int month, int year, int hour, int minute, String additionalInfo, Bundle options) {
        this.number = number;
        this.type = type;
        this.day = day;
        this.month = month;
        this.year = year;
        this.hour = hour;
        this.minute = minute;
        this.additionalInfo = additionalInfo;
        this.additionalOptions = options;
    }

    private Reminder(Parcel parcel){
        this.number = parcel.readString();
        this.type = (ReminderType)parcel.readSerializable();
        this.day = parcel.readInt();
        this.month = parcel.readInt();
        this.year =parcel.readInt();
        this.hour= parcel.readInt();
        this.minute = parcel.readInt();
        this.additionalInfo = parcel.readString();
        this.additionalOptions = parcel.readBundle();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.number);
        dest.writeSerializable(this.type);
        dest.writeInt(this.day);
        dest.writeInt(this.month);
        dest.writeInt(this.year);
        dest.writeInt(this.hour);
        dest.writeInt(this.minute);
        dest.writeString(this.additionalInfo);
        dest.writeBundle(this.additionalOptions);
    }

    public static final Creator<Reminder> CREATOR = new Creator<Reminder>(){
        public Reminder createFromParcel(Parcel parcel){
            return new Reminder(parcel);
        }

        public Reminder[] newArray(int size){
            return new Reminder[size];
        }
    };

    /**
     * Converts from Parcelable to byte[]
     * @return Conversion to byte[]
     */
    public byte[] marshall() {
        Parcel parcel = Parcel.obtain();
        this.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }
}
