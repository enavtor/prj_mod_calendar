package com.droidmare.calendar.models;

import android.graphics.drawable.Drawable;

//Model for a type list item (item inside the new event dialog) declaration
//@author Eduardo on 26/02/2018.

public class TypeListItem {

    //List of event types:
    public enum eventTypes {
        ACTIVITY,
        DOCTOR,
        MEDICATION,
        PERSONAL_EVENT,
        PERSONAL,
        TEXTNOFEEDBACK,
        STIMULUS
    }

    //Item's type:
    private eventTypes type;

    //Item's type text:
    private String typeTitle;

    //Item's description text:
    private String typeDescription;

    //The icon for the event type:
    private Drawable eventIcon;

    public TypeListItem(eventTypes typeValue, String titleText, String descriptionText, Drawable icon){
        this.type = typeValue;
        this.typeTitle = titleText;
        this.typeDescription = descriptionText;
        this.eventIcon = icon;
    }

    public eventTypes getType() {
        return type;
    }

    public String getTypeTitle() {
        return typeTitle;
    }

    public String getTypeDescription() { return typeDescription; }

    public Drawable getEventIcon() {
        return eventIcon;
    }
}
