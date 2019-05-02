package com.droidmare.calendar.models;

import java.util.ArrayList;

//Model for a calendar item (day inside the calendar grid) declaration
//@author Eduardo on 08/02/2018.

public class CalendarGridItem {

    //Item's day number:
    private String dayText;

    //Item's day of week:
    private String dayOfWeek;

    //Item's list of events:
    private ArrayList <EventListItem> eventList;

    public CalendarGridItem(String dText, String dWeekText, ArrayList<EventListItem> events){
        this.dayText = dText;
        this.dayOfWeek = dWeekText;
        this.eventList = events;
    }

    public String getDayText() {

        return dayText;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public ArrayList<EventListItem> getEventList() {
        return eventList;
    }

    public boolean hasEvents () {
        boolean hasEvents = false;

        if (eventList != null) {
            for (int i = 0; i < eventList.size(); i++) {
                if (!eventList.get(i).isAlarm()) {
                    hasEvents = true;
                    break;
                }
            }
        }

        return hasEvents;
    }

    public boolean hasAlarms () {
        boolean hasAlarms = false;

        if (eventList != null) {
            for (int i = 0; i < eventList.size(); i++) {
                if (eventList.get(i).isAlarm()) {
                    hasAlarms = true;
                    break;
                }
            }
        }

        return hasAlarms;
    }
}
