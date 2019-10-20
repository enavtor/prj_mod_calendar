package com.droidmare.calendar.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidmare.R;
import com.droidmare.calendar.models.EventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.DialogAddDescriptionActivity;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.database.publisher.EventsPublisher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

//Service in charge of establishing a connection with the API in order to update the events declaration
//@author Eduardo on 04/06/2018.
public class ApiConnectionService extends IntentService {

    //A control variable to know if this service is running:
    public static boolean isCurrentlyRunning = false;

    //A reference to the main activity, so the views can be updated after deleting or modifying an event from the backend:
    private static WeakReference<MainActivity> mainActivityReference;
    public static void setMainActivityReference(MainActivity activity) {
        mainActivityReference = new WeakReference<>(activity);
    }

    //A reference to the main activity, so the views can be updated after deleting or modifying an event from the backend:
    private static WeakReference<DialogAddDescriptionActivity> addDescriptionActivityReference;
    public static void setAddDescriptionActivityReference(DialogAddDescriptionActivity activity) {
        addDescriptionActivityReference = new WeakReference<>(activity);
    }

    private static final String TAG = ApiConnectionService.class.getCanonicalName();

    //API base URL:
    public static final String BASE_URL = "http://192.168.1.49:3000/";
    //public static final String BASE_URL = "http://droidmare-api.localtunnel.me:3000/";

    //Server connection timeout in milliseconds
    private static final int SERVER_TIMEOUT = 5000;

    //Server request methods
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String REQUEST_METHOD_EDIT = "PUT";
    public static final String REQUEST_METHOD_DELETE = "DELETE";
    public static final String REQUEST_METHOD_DELETE_ALL = "DELETE_ALL";
    public static final String REQUEST_MEDICATION = "MEDICATION" ;

    //Content type property name
    private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";

    //Content type property value
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/json";

    private SQLiteManager database;

    private Intent dataIntent;

    private EventJsonObject eventJson;

    private EventListItem eventToSend;

    private static int responseCode;

    public ApiConnectionService() {
        super("ApiConnectionService");
    }

    @Override
    public void onHandleIntent(Intent dataIntent) {

        database = new SQLiteManager(this, SQLiteManager.DATABASE_NAME, null, SQLiteManager.DATABASE_VERSION);

        String urlForPublishing = BASE_URL + "event";

        this.dataIntent = dataIntent;

        String operation = this.dataIntent.getStringExtra("operation");

        if (operation.equals(REQUEST_METHOD_POST) || operation.equals(REQUEST_METHOD_EDIT) || operation.equals(REQUEST_METHOD_DELETE)) {
            eventJson = EventJsonObject.createEventJson(dataIntent.getStringExtra(EventUtils.EVENT_JSON_FIELD));
            eventJson.put(EventUtils.EVENT_USER_FIELD, UserDataService.getUserId());
            eventToSend = EventUtils.makeEvent(getApplicationContext(), eventJson);
        }

        switch (operation) {
            case REQUEST_METHOD_POST:
                sendEvent(urlForPublishing);
                break;
            case REQUEST_METHOD_EDIT:
                modifyEvent(urlForPublishing);
                break;
            case REQUEST_METHOD_DELETE:
                deleteEvent(urlForPublishing);
                break;
            case REQUEST_METHOD_DELETE_ALL:
                deleteAllEvents(urlForPublishing);
                break;
            case REQUEST_MEDICATION:
                requestMedication();
                break;
        }

        database.close();

        isCurrentlyRunning = false;
    }

    private void requestMedication () {

        String url = BASE_URL + "medication/" + UserDataService.getUserId();

        if (addDescriptionActivityReference != null && addDescriptionActivityReference.get() != null)
            addDescriptionActivityReference.get().initDialogList(formatMedicationResponse(sendRequest(null, url, REQUEST_METHOD_GET)));
    }

    private String formatMedicationResponse(String response) {

        StringBuilder formattedMedication = new StringBuilder();

        try{
            JSONObject medicationJson = new JSONObject(response);
            medicationJson = medicationJson.getJSONObject("health");
            JSONArray medicationArray = medicationJson.getJSONArray("prescriptions");

            for (int i = 0; i < medicationArray.length(); i++) {
                JSONObject medication = medicationArray.getJSONObject(i);

                String quantity = medication.getString("quantity");

                if (quantity.equals("")) {
                    quantity = medication.getString("activeSubstance");
                    String[] quantityList = quantity.split("-");

                    for (String auxQuantity : quantityList) {
                        if (Integer.valueOf(auxQuantity) != 0) {
                            quantity = auxQuantity;
                            break;
                        }
                    }
                }

                String totalDose = medication.getString("dose");
                String doseUnits = medication.getString("doseUnits");
                String medicine = medication.getString("drugName");
                String adminMode = medication.getString("administrationMode").toLowerCase();
                String adminForm = medication.getString("administrationForm").toLowerCase();
                String frequency = medication.getString("administrationFrequency").toLowerCase();

                String medicationText = (
                        "- " + getString(R.string.medicine_description_header) +
                                quantity + " " + adminForm +
                                getString(R.string.medicine_description_separator) + medicine + " " +
                                totalDose + doseUnits + " " +
                                adminMode + " " + frequency
                );

                if (formattedMedication.toString().equals("")) formattedMedication.append(medicationText);
                else {
                    String auxMedication = "\n" + medicationText;
                    formattedMedication.append(auxMedication);
                }
            }

        } catch (JSONException jse) {
            Log.e(TAG, "formatMedicationResponse. JSONException: " + jse.getMessage());
        }

        return formattedMedication.toString();
    }

    /**
     * Starts the request to the api for a post method
     *
     * @param urlForPublishing the API URL
     */
    private void sendEvent(String urlForPublishing) {

        String response = sendRequest(eventJson, urlForPublishing, REQUEST_METHOD_POST);

        String localEventId = eventToSend.getEventId();

        try {
            if (!response.equals("") && responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                eventToSend.setPendingOperation("");
                eventToSend.setEventId(responseJson.getString(EventUtils.EVENT_ID_FIELD));
                eventToSend.setLastApiUpdate(responseJson.getLong(EventUtils.EVENT_LAST_UPDATE_FIELD));
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList, localEventId);
            }
        } catch (JSONException jsonException) {
            Log.e(TAG, "sendPostEvent. JSONException: " + jsonException.getMessage());
        } finally {
            if (responseCode != 200 && !eventToSend.getPendingOperation().equals(REQUEST_METHOD_POST)) {
                eventToSend.setPendingOperation(REQUEST_METHOD_POST);
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList, localEventId);
            }
        }
    }

    /**
     * Starts the request to the api for a post method
     *
     * @param urlForPublishing the API URL
     */
    private void modifyEvent(String urlForPublishing) {

        String response = sendRequest(eventJson, urlForPublishing, REQUEST_METHOD_EDIT);

        try {
            if (!response.equals("") && responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                eventToSend.setPendingOperation("");
                eventToSend.setLastApiUpdate(responseJson.getLong(EventUtils.EVENT_LAST_UPDATE_FIELD));
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList);
            }
        } catch (JSONException jsonException) {
            Log.e(TAG, "modifyEvent. JSONException: " + jsonException.getMessage());
        } finally {
            if (responseCode != 200 && !eventToSend.getPendingOperation().equals(REQUEST_METHOD_EDIT)) {
                eventToSend.setPendingOperation(REQUEST_METHOD_EDIT);
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList);
            }
        }
    }

    /**
     * Starts the request to the api for a delete method
     *
     * @param urlForPublishing the API URL
     */
    private void deleteEvent(String urlForPublishing) {

        sendRequest(eventJson, urlForPublishing, REQUEST_METHOD_DELETE);

        if (responseCode == 200) {
            if (mainActivityReference != null && mainActivityReference.get() != null && (eventToSend.getPendingOperation().equals("") || eventToSend.getPendingOperation().equals(REQUEST_METHOD_POST)))
                mainActivityReference.get().deleteEvent(eventToSend);
            else database.deleteSingleEvent(eventToSend.getEventId());
        }

        if (responseCode != 200 && !eventToSend.getPendingOperation().equals(REQUEST_METHOD_DELETE)) {
            eventToSend.setPendingOperation(REQUEST_METHOD_DELETE);
            EventListItem[] eventList = {eventToSend};
            mainActivityReference.get().relocateFocusAfterDelete();
            EventsPublisher.modifyEvent(getContextToPublish(), eventList);
        }
    }

    /**
     * Starts the request to the api for a delete method (deleting all the existing events)
     *
     * @param urlForPublishing the API URL
     */
    private void deleteAllEvents(String urlForPublishing) {

        try {
            for (String eventString : dataIntent.getStringArrayExtra("eventStrings")) {
                JSONObject eventJsonObject = new JSONObject(eventString).getJSONObject("event");
                eventJsonObject.put("eventId", eventJsonObject.getLong("eventApiId"));
                sendRequest(eventJsonObject, urlForPublishing, REQUEST_METHOD_DELETE);
            }
        } catch (JSONException jse) {
            Log.e(TAG, "deleteAllEvents. JSONException: " + jse.getMessage());
        }
    }

    private Context getContextToPublish() {
        if (mainActivityReference != null && mainActivityReference.get() != null)
            return mainActivityReference.get();
        else return this;
    }

    /**
     * Sends event requests to server
     *
     * @param json JSONObject with event information
     */
    public static String sendRequest(JSONObject json, String apiURL, String requestMethod) {
        URL serverUrl = null;
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        responseCode = -1;

        try {
            serverUrl = new URL(apiURL);
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setConnectTimeout(SERVER_TIMEOUT);
            connection.setReadTimeout(SERVER_TIMEOUT);

            if (!requestMethod.equals(REQUEST_METHOD_GET)) {
                connection.setDoInput(true);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE_PROPERTY_VALUE);

                if (json != null) {
                    OutputStreamWriter os = new OutputStreamWriter(connection.getOutputStream());
                    os.write(json.toString());
                    os.close();
                }
            }

            responseCode = connection.getResponseCode();

            //If the requested method is not a delete operation, the api will send a response:
            if (!requestMethod.equals(REQUEST_METHOD_DELETE)) {

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String inputLine;

                while ((inputLine = br.readLine()) != null) response.append(inputLine);

                br.close();

                if (json!= null) Log.d("POST Request sent", "Sent event: " + json.toString());
            }

            connection.disconnect();

            Log.d(TAG,"Send Request (" + requestMethod + ") destiny to " + serverUrl + ". Response code: " + responseCode);
            Log.d(requestMethod + " Request sent", "Response content: " + response.toString());

        }catch (Exception e){
            if (connection != null) connection.disconnect();
            Log.d(TAG,"Send Request (" + requestMethod + ") destiny to " + serverUrl + ". Response code: " + responseCode);
            Log.d(requestMethod + " Request sent", "Response content: " + response.toString());
            e.printStackTrace();
        }

        Log.d("GET_DATA", "Response content: " + response.toString());

        return response.toString();
    }
}