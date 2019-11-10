package com.droidmare.calendar.services;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.droidmare.calendar.models.CalEventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.database.manager.SQLiteManager;
import com.droidmare.database.publisher.EventsPublisher;
import com.droidmare.common.models.ConstantValues;
import com.droidmare.common.models.EventJsonObject;
import com.droidmare.common.services.CommonIntentService;

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
public class ApiConnectionService extends CommonIntentService {

    private static final String TAG = ApiConnectionService.class.getCanonicalName();

    //A control variable to know if this service is running:
    public static boolean isCurrentlyRunning = false;

    //A reference to the main activity, so the views can be updated after deleting or modifying an event from the backend:
    private static WeakReference<MainActivity> mainActivityReference;
    public static void setMainActivityReference(MainActivity activity) {
        mainActivityReference = new WeakReference<>(activity);
    }

    //API base URL:
    public static final String BASE_URL = "http://192.168.1.49:3000/";
    //public static final String BASE_URL = "http://droidmare-api.localtunnel.me:3000/";

    //Server connection timeout in milliseconds
    private static final int SERVER_TIMEOUT = 5000;

    public static final String OPERATION_FIELD = "operation";

    //Server request methods
    public static final String REQUEST_METHOD_GET = "GET";
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String REQUEST_METHOD_EDIT = "PUT";
    public static final String REQUEST_METHOD_DELETE = "DELETE";

    //Content type property name
    private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";

    //Content type property value
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/json";

    private SQLiteManager database;

    private EventJsonObject eventJson;

    private EventListItem eventToSend;

    private static int responseCode;

    public ApiConnectionService() { super(TAG); }

    @Override
    public void onHandleIntent(Intent dataIntent) {

        COMMON_TAG = TAG;

        super.onHandleIntent(dataIntent);

        database = new SQLiteManager(getApplicationContext(), SQLiteManager.DATABASE_NAME, null, SQLiteManager.DATABASE_VERSION);

        String urlForPublishing = BASE_URL + "event";

        String operation = dataIntent.getStringExtra(OPERATION_FIELD);

        if (!operation.equals(REQUEST_METHOD_GET)) {
            eventJson = CalEventJsonObject.createEventJson(dataIntent.getStringExtra(ConstantValues.EVENT_JSON_FIELD));
            eventJson.put(ConstantValues.EVENT_USER_FIELD, UserDataService.getUserId());
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
        }

        database.close();

        isCurrentlyRunning = false;
    }

    //Starts the request to the api for a post operation:
    private void sendEvent(String urlForPublishing) {

        String response = sendRequest(eventJson, urlForPublishing, REQUEST_METHOD_POST);

        String localEventId = eventToSend.getEventId();

        try {
            if (!response.equals("") && responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                eventToSend.setPendingOperation("");
                eventToSend.setEventId(responseJson.getString(ConstantValues.EVENT_ID_FIELD));
                eventToSend.setLastApiUpdate(responseJson.getLong(ConstantValues.EVENT_LAST_UPDATE_FIELD));
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList, localEventId);
            }
        } catch (JSONException jsonException) {
            Log.e(COMMON_TAG, "sendOperationRequest. JSONException: " + jsonException.getMessage());
        } finally {
            if (responseCode != 200 && !eventToSend.getPendingOperation().equals(REQUEST_METHOD_POST)) {
                eventToSend.setPendingOperation(REQUEST_METHOD_POST);
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList, localEventId);
            }
        }
    }

    //Starts the request to the api for a post operation:
    private void modifyEvent(String urlForPublishing) {

        String response = sendRequest(eventJson, urlForPublishing, REQUEST_METHOD_EDIT);

        try {
            if (!response.equals("") && responseCode == 200) {
                JSONObject responseJson = new JSONObject(response);
                eventToSend.setPendingOperation("");
                eventToSend.setLastApiUpdate(responseJson.getLong(ConstantValues.EVENT_LAST_UPDATE_FIELD));
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList);
            }
        } catch (JSONException jsonException) {
            Log.e(COMMON_TAG, "modifyEvent. JSONException: " + jsonException.getMessage());
        } finally {
            if (responseCode != 200 && !eventToSend.getPendingOperation().equals(REQUEST_METHOD_EDIT)) {
                eventToSend.setPendingOperation(REQUEST_METHOD_EDIT);
                EventListItem[] eventList = {eventToSend};
                EventsPublisher.modifyEvent(getContextToPublish(), eventList);
            }
        }
    }

    //Starts the request to the api for a delete operation;
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

    private Context getContextToPublish() {
        if (mainActivityReference != null && mainActivityReference.get() != null)
            return mainActivityReference.get();
        else return this;
    }

    //Sends event requests to server in order to perform the operation specified by requestedMethod on the API:
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