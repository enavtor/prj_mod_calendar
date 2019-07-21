package com.droidmare.calendar.utils;

import android.util.Log;

import com.droidmare.calendar.models.EventListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//Util for sorting an event's previous alarms by date declaration
//@author Eduardo on 20/03/2019.

public class SortUtils {

    private static final String TAG = SortUtils.class.getCanonicalName();

    public static JSONArray sortPrevAlarms(JSONArray unsortedList) {

        try {
            JSONArray sortedList = new JSONArray();

            for (int i = 0; i < unsortedList.length(); i++) {

                JSONObject unsortedAlarm = unsortedList.getJSONObject(i);

                for (int j = 0; j <= sortedList.length(); j++) {

                    if (j != sortedList.length()) {

                        JSONObject sortedAlarm = sortedList.getJSONObject(j);

                        String unsortedDate = unsortedAlarm.getString("Alarm");

                        String sortedDate = sortedAlarm.getString("Alarm");

                        if (DateUtils.fullDateIsPrevious(unsortedDate, sortedDate)) {
                            //Since the method put(index, JSONObject) of JSONArray overwrites the JSONObject in index,
                            //all the objects from index to the end of the array must be moved to the right:
                            for (int n = sortedList.length() - 1; n >= j; n--)
                                sortedList.put(n + 1, sortedList.getJSONObject(n));
                            sortedList.put(j, unsortedAlarm);
                            break;
                        }
                    } else {
                        sortedList.put(unsortedAlarm);
                        break;
                    }
                }
            }

            return sortedList;

        } catch (JSONException jse) {
            Log.e(TAG, "sortPrevAlarms. JSONException: " + jse.getMessage());
        }

        return null;
    }

    public static ArrayList<EventListItem> sortEventList (ArrayList<EventListItem> unsortedList) {

        ArrayList<EventListItem> sortedList = new ArrayList<>();

        for (int i = 0; i < unsortedList.size(); i++) {

            EventListItem unsortedEvent = unsortedList.get(i);

            for (int j = 0; j <= sortedList.size(); j++) {

                if (j != sortedList.size()) {

                    EventListItem sortedEvent = sortedList.get(j);

                    long unsortedRepDate = unsortedEvent.getNextRepetition();

                    long sortedRepDate = sortedEvent.getNextRepetition();

                    if (unsortedRepDate < sortedRepDate) {
                        sortedList.add(j, unsortedEvent);
                        break;
                    }
                } else {
                    sortedList.add(unsortedEvent);
                    break;
                }
            }
        }

        return sortedList;
    }
}
