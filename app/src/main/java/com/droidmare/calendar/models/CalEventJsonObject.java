package com.droidmare.calendar.models;

import com.droidmare.common.models.ConstantValues;
import com.droidmare.common.models.EventJsonObject;

//Model for a event json object (a specialization of JSONObject for building event json objects)
//@author Eduardo on 21/08/2019.

public class CalEventJsonObject extends EventJsonObject {

    public static EventJsonObject createEventJson(EventListItem event) {

        EventJsonObject newEventJson = new EventJsonObject();

        newEventJson.put(ConstantValues.EVENT_ID_FIELD, event.getEventId());
        newEventJson.put(ConstantValues.EVENT_TYPE_FIELD, event.getEventType());
        newEventJson.put(ConstantValues.EVENT_HOUR_FIELD, event.getEventHour());
        newEventJson.put(ConstantValues.EVENT_MINUTE_FIELD, event.getEventMinute());
        newEventJson.put(ConstantValues.EVENT_DAY_FIELD, event.getEventDay());
        newEventJson.put(ConstantValues.EVENT_MONTH_FIELD, event.getEventMonth());
        newEventJson.put(ConstantValues.EVENT_YEAR_FIELD, event.getEventYear());
        newEventJson.put(ConstantValues.EVENT_DESCRIPTION_FIELD, event.getDescriptionText());
        newEventJson.put(ConstantValues.EVENT_REP_INTERVAL_FIELD, event.getIntervalTime());
        newEventJson.put(ConstantValues.EVENT_REPETITION_STOP_FIELD, event.getRepetitionStop());
        newEventJson.put(ConstantValues.EVENT_TIMEOUT_FIELD, event.getReminderTimeOut());
        newEventJson.put(ConstantValues.EVENT_REPETITION_TYPE_FIELD, event.getRepetitionType());
        newEventJson.put(ConstantValues.EVENT_PREV_ALARMS_FIELD, event.getPreviousAlarms());
        newEventJson.put(ConstantValues.EVENT_PENDING_OP_FIELD, event.getPendingOperation());
        newEventJson.put(ConstantValues.EVENT_LAST_UPDATE_FIELD, event.getLastApiUpdate());

        return newEventJson;
    }
}
