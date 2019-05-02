package com.droidmare.database.publisher;

import android.content.Context;

import com.droidmare.calendar.models.EventListItem;

//Event publisher (for managing event storing and retrieving) declaration
//@author Eduardo on 07/03/2018.

public class EventsPublisher {

    //An enumeration with the different operation types:
    public enum operationType {
        RETRIEVE_EVENTS,
        DELETE_EVENTS,
        RETRIEVE_ALL,
        RESET_ALARMS,
    }

    //Publishes all the events stored inside eventList:
    public static void publishEvents(Context context, EventListItem[] eventList) {
        new EventRecorder(context).execute(eventList);
    }

    //Modifies the event stored inside eventList (the difference with the previous function is that the event inside eventList will have an id):
    public static void modifyEvent(Context context, EventListItem[] eventList) {
        new EventRecorder(context).execute(eventList);
    }

    //Retrieves all the events for the current month and year:
    public static void retrieveMonthEvents(Context context) {
        new EventRetriever(context, operationType.RETRIEVE_EVENTS).execute();
    }

    //Retrieves all the stored events for the current user:
    public static void retrieveAllEvents(Context context){
        new EventRetriever(context, operationType.RETRIEVE_ALL).execute();
    }

    //Retrieves all the stored events for the current user and resets their alarms:
    public static void retrieveAndReset(Context context){
        new EventRetriever(context, operationType.RESET_ALARMS).execute();
    }

    //Deletes a single event:
    public static void deleteEvent(Context context, long id) {
        new EventRetriever(context, operationType.DELETE_EVENTS).execute(id);
    }

    //Deletes all the stored events for the current user:
    public static void deleteAllEvents(Context context) {
        new EventRetriever(context, operationType.DELETE_EVENTS).execute((Long)null);
    }
}
