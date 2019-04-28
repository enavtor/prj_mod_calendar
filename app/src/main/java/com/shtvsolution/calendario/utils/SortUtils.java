package com.shtvsolution.calendario.utils;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

                        Log.d("SORTING", "Unsorted : " + unsortedDate + " -- Sorted: " + sortedDate);

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
}
