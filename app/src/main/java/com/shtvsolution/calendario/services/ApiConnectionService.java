package com.shtvsolution.calendario.services;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.shtvsolution.R;
import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.utils.DateUtils;
import com.shtvsolution.calendario.utils.EventUtils;
import com.shtvsolution.calendario.views.activities.DialogAddDescriptionActivity;
import com.shtvsolution.calendario.views.activities.MainActivity;
import com.shtvsolution.database.manager.SQLiteManager;
import com.shtvsolution.database.publisher.EventsPublisher;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

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

    //Server connection timeout in milliseconds
    private static final int SERVER_TIMEOUT = 5000;

    //Server request methods
    public static final String REQUEST_METHOD_POST = "POST";
    public static final String REQUEST_METHOD_DELETE = "DELETE";
    public static final String REQUEST_METHOD_DELETE_ALL = "DELETE_ALL";
    public static final String REQUEST_MEDICATION = "MEDICATION" ;

    //Security protocol
    private static final String SECURITY_PROTOCOL_TLS = "TLS";

    //Content type property name
    private static final String CONTENT_TYPE_PROPERTY_NAME = "Content-Type";

    //Content type property value
    private static final String CONTENT_TYPE_PROPERTY_VALUE = "application/json";

    private SQLiteManager database;

    private String operation;

    private Intent eventIntent;

    private JSONObject eventJson;

    private EventListItem eventToSend;

    private int responseCode;

    public ApiConnectionService() {
        super("ApiConnectionService");
    }

    @Override
    public void onHandleIntent(Intent intentData) {

        database = new SQLiteManager(this, SQLiteManager.DATABASE_NAME, null, SQLiteManager.DATABASE_VERSION);

        String patientId = Integer.toString(UserDataReceiverService.getUserId());

        String urlForPublishing = ApiReceiverService.getApiUrl(getApplicationContext()) + "patients/" + patientId + "/events";

        this.eventIntent = intentData;

        operation = eventIntent.getStringExtra("operation");

        if (operation.equals(REQUEST_METHOD_POST) || operation.equals(REQUEST_METHOD_DELETE) || operation.equals(REQUEST_METHOD_DELETE_ALL)) {
            eventJson = transformToJson(eventIntent);
            eventToSend = EventUtils.makeEvent(this, eventIntent);
        }

        switch (operation) {
            case REQUEST_METHOD_POST:
                sendEvent(urlForPublishing);
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

        //Once the request has been sent, the service is stopped:
        stopSelf();
    }

    /**
     * Transforms an Intent into a Json
     *
     * @param eventIntent the intent containing the event's information
     */
    private JSONObject transformToJson(Intent eventIntent) {

        JSONObject eventJson = new JSONObject();

        long eventApiId = eventIntent.getLongExtra(EventUtils.EVENT_API_ID_FIELD, -1);

        String eventType = eventIntent.getStringExtra(EventUtils.EVENT_TYPE_FIELD);

        //The date parameters must be transformed into millis:
        int eventHour = eventIntent.getIntExtra(EventUtils.EVENT_HOUR_FIELD, -1);
        int eventMinute = eventIntent.getIntExtra(EventUtils.EVENT_MINUTE_FIELD, -1);
        int eventDay = eventIntent.getIntExtra(EventUtils.EVENT_DAY_FIELD, DateUtils.currentDay);
        int eventMonth = eventIntent.getIntExtra(EventUtils.EVENT_MONTH_FIELD, DateUtils.currentMonth);
        int eventYear = eventIntent.getIntExtra(EventUtils.EVENT_YEAR_FIELD, DateUtils.currentYear);

        long eventStartDate = DateUtils.transformToMillis(eventMinute, eventHour, eventDay, eventMonth, eventYear);

        String eventDescription = eventIntent.getStringExtra(EventUtils.EVENT_DESCRIPTION_FIELD);
        //boolean instantlyShown = eventIntent.getBooleanExtra(EventUtils.EVENT_INSTANTLY_FIELD, false);
        int intervalTime = eventIntent.getIntExtra(EventUtils.EVENT_INTERVAL_FIELD, 0);
        long repetitionStop = eventIntent.getLongExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, -1);
        long timeOut = eventIntent.getLongExtra(EventUtils.EVENT_TIMEOUT_FIELD, EventUtils.DEFAULT_HIDE_TIME);
        long lastUpdate = eventIntent.getLongExtra(EventUtils.EVENT_LAST_UPDATE_FIELD, -1);

        String eventPrevAlarms =  eventIntent.getStringExtra(EventUtils.EVENT_PREV_ALARMS_FIELD);
        String eventRepType =  eventIntent.getStringExtra(EventUtils.EVENT_REP_TYPE_FIELD);

        try {
            if (operation.equals(REQUEST_METHOD_DELETE) || eventApiId != -1)
                eventJson.put("eventId", eventApiId);
            eventJson.put("eventType", eventType);
            eventJson.put("descriptionText", eventDescription);
            eventJson.put("intervalTime", intervalTime);
            //eventJson.put("eventRepetitionType", eventRepType);
            eventJson.put("eventStartDate", eventStartDate);
            //eventJson.put("eventPrevAlarms", eventPrevAlarms);
            eventJson.put("eventStopDate", repetitionStop);
            eventJson.put("timeOut", timeOut);
            eventJson.put("lastUpdate", lastUpdate);
        } catch (JSONException jse) {
            Log.e(TAG, "transformToJson. JSONException: " + jse.getMessage());
        }

        return eventJson;
    }

    private void requestMedication () {
        try {
            String url = "http://tvassistdem-backend.istc.cnr.it/patients/" + UserDataReceiverService.getUserId() + "/data";
            if (addDescriptionActivityReference != null && addDescriptionActivityReference.get() != null)
                addDescriptionActivityReference.get().initDialogList(formatMedicationResponse(getData(new URL(url))));
        } catch (MalformedURLException mfe) {
            Log.e(TAG, "sendEvent. MalformedURLException: " + mfe.getMessage());
        }
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

        String response;

        try {
            response = sendRequest(eventJson, new URL(urlForPublishing), REQUEST_METHOD_POST);

            if (!response.equals("") && responseCode == 200) {
                eventToSend.setPendingOperation("");
                JSONObject responseJson = new JSONObject(response);
                responseJson = (JSONObject) responseJson.getJSONArray("events").get(0);

                if (eventToSend.getEventApiId() == -1)
                    eventToSend.setEventApiId(responseJson.getLong("eventId"));
                eventToSend.setLastApiUpdate(responseJson.getLong("lastUpdate"));
            }
        } catch (MalformedURLException mfe) {
            Log.e(TAG, "sendEvent. MalformedURLException: " + mfe.getMessage());
        } catch (JSONException jse) {
            Log.e(TAG, "sendEvent. JSONException: " + jse.getMessage());
        } finally {
            if (responseCode != 200) eventToSend.setPendingOperation(REQUEST_METHOD_POST);
            EventListItem[] eventList = {eventToSend};
            EventsPublisher.modifyEvent(getContextToPublish(), eventList);
        }
    }

    /**
     * Starts the request to the api for a delete method
     *
     * @param urlForPublishing the API URL
     */
    private void deleteEvent(String urlForPublishing) {

        try {
            sendRequest(eventJson, new URL(urlForPublishing), REQUEST_METHOD_DELETE);
            if (responseCode == 200) {
                if (mainActivityReference != null && mainActivityReference.get() != null && (eventToSend.getPendingOperation().equals("") || eventToSend.getPendingOperation().equals(REQUEST_METHOD_POST)))
                    mainActivityReference.get().deleteEvent(eventToSend);
                else database.deleteSingleEvent(eventToSend.getEventId());
            }
        } catch (MalformedURLException e) {
            Log.e(TAG, "sendDeleteEvent. MalformedURLException: " + e.getMessage());
        }finally {
            if (responseCode != 200) {
                eventToSend.setPendingOperation(REQUEST_METHOD_DELETE);
                EventListItem[] eventList = {eventToSend};
                mainActivityReference.get().relocateFocusAfterDelete();
                EventsPublisher.modifyEvent(getContextToPublish(), eventList);
            }
        }
    }

    /**
     * Starts the request to the api for a delete method (deleting all the existing events)
     *
     * @param urlForPublishing the API URL
     */
    private void deleteAllEvents(String urlForPublishing) {
        try {
            for (String eventString : eventIntent.getStringArrayExtra("eventStrings")) {
                JSONObject eventJsonObject =  new JSONObject(eventString).getJSONObject("event");
                eventJson.put("eventId", eventJsonObject.getLong("eventApiId"));
                sendRequest(eventJson, new URL(urlForPublishing), REQUEST_METHOD_DELETE);
            }
        } catch (MalformedURLException mfe) {
            Log.e(TAG, "deleteAllEvents. MalformedURLException: " + mfe.getMessage());
        } catch (JSONException jse) {
            Log.e(TAG, "deleteAllEvents. JSONException: " + jse.getMessage());
        }
    }

    private Context getContextToPublish() {
        if (mainActivityReference != null && mainActivityReference.get() != null)
            return mainActivityReference.get();
        else return this;
    }

    private String getData(URL urlObject) {

        String result = "";

        try {
            HttpURLConnection con;
            con = (HttpURLConnection) urlObject.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(10000);
            con.setReadTimeout(10000);

            responseCode = con.getResponseCode();

            BufferedReader bufferedReader;

            bufferedReader = new BufferedReader(
                    new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }

            result = response.toString();

            bufferedReader.close();

        } catch (IOException ie) {
            Log.e(TAG, "getData(). IOException: " + ie.getMessage());
            return result;
        }

        Log.d("GET_DATA", "Response content: " + result);

        return result;
    }

    /**
     * Sends event requests to server
     *
     * @param json JSONObject with event information
     */
    private String sendRequest(JSONObject json, URL serverUrl, String requestMethod) {
        HttpURLConnection connection = null;
        StringBuilder response = new StringBuilder();
        responseCode = -1;

        try {
            this.trustEveryoneSSL();
            //TODO change to HTTPS
            //HttpsURLConnection connection=(HttpsURLConnection)url.openConnection();
            connection = (HttpURLConnection) serverUrl.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setConnectTimeout(SERVER_TIMEOUT);
            connection.setReadTimeout(SERVER_TIMEOUT);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setChunkedStreamingMode(0);
            connection.setRequestProperty(CONTENT_TYPE_PROPERTY_NAME, CONTENT_TYPE_PROPERTY_VALUE);

            OutputStreamWriter os = new OutputStreamWriter(connection.getOutputStream());
            os.write(json.toString());
            os.close();

            responseCode = connection.getResponseCode();

            //If the requested method was a POST one, the event created in the backend is going to be returned as an input stream so the backend id can be saved:
            if (requestMethod.equals(REQUEST_METHOD_POST)) {

                BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String inputLine;

                while ((inputLine = br.readLine()) != null) response.append(inputLine);

                br.close();

                Log.d("POST Request sent", "Sent event: " + json.toString());
            }

            connection.disconnect();

            Log.d(TAG,"Send Request (" + requestMethod + ") destiny to " + serverUrl + ". Response code: " + responseCode);
            Log.d(requestMethod + " Request sent", "Response content: " + response.toString());

            //if(responseCode==RESPONSE_CODE_NOT_FOUND); cancel(true);
        }catch (Exception e){
            if(connection!=null)connection.disconnect();
            Log.d(TAG,"Send Request (" + requestMethod + ") destiny to " + serverUrl + ". Response code: " + responseCode);
            Log.d(requestMethod + " Request sent", "Response content: " + response.toString());
            e.printStackTrace();
        }

        return response.toString();
    }

    /**
     * Forces to accept connection even if the certificate is invalid
     */
    @SuppressLint({"TrustAllX509TrustManager", "BadHostnameVerifier"})
    private void trustEveryoneSSL() {
        try {
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            SSLContext context = SSLContext.getInstance(SECURITY_PROTOCOL_TLS);
            context.init(null, new X509TrustManager[]{new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) { }
                public void checkServerTrusted(X509Certificate[] chain, String authType) { }
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
        } catch (Exception e) {
            Log.e(TAG, "trustEveryoneSSL. Exception: " + e.getMessage());
        }
    }
}