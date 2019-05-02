package com.droidmare.database.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.UserDataReceiverService;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

//SQLite manager (for managing events storing) declaration
//@author Eduardo on 07/03/2018.

public class SQLiteManager extends SQLiteOpenHelper{

    private static final String TAG = SQLiteManager.class.getCanonicalName();

    /** Database name */
    public static final String DATABASE_NAME = "event.sqlite";

    /** Database version */
    public static final int DATABASE_VERSION = 1;

    /** Events table */
    private static final String EVENTS_TABLE = "events";

    /** Events id column */
    private static final String EVENT_ID_COLUMN = "id";

    /** Events api id column */
    private static final String EVENT_API_ID_COLUMN = "apiId";

    /** Events user id column */
    private static final String EVENT_USER_ID_COLUMN = "userId";

    /** Events type column */
    private static final String EVENT_TYPE_COLUMN = "type";

    /** Events hour column */
    private static final String EVENT_HOUR_COLUMN = "hour";

    /** Events minute column */
    private static final String EVENT_MINUTE_COLUMN = "minute";

    /** Events day column */
    private static final String EVENT_DAY_COLUMN = "day";

    /** Events month column */
    private static final String EVENT_MONTH_COLUMN = "month";

    /** Events year column */
    private static final String EVENT_YEAR_COLUMN = "year";

    /** Events description */
    private static final String EVENT_DESCRIPTION_COLUMN = "description";

    /** Events instantlyShown column */
    private static final String EVENT_INSTANTLY_COLUMN = "instantly";

    /** Events interval time field */
    private static final String EVENT_INTERVAL_COLUMN = "interval";

    /** Events repetition stop field */
    private static final String EVENT_REPETITION_STOP_COLUMN = "stop";

    /** Events time out field */
    private static final String EVENT_TIMEOUT_COLUMN = "timeOut";

    /** Events repetition type field */
    private static final String EVENT_REP_TYPE_COLUMN = "repType";

    /** Events previous alarms field */
    private static final String EVENT_PREV_ALARMS_COLUMN = "prevAlarms";

    /** Events last api update field */
    private static final String EVENT_LAST_UPDATE_COLUMN= "lastUpdate";

    /** Events pending operation type */
    private static final String PENDING_OPERATION_COLUMN= "pendingOperation";

    /** SQL query for deleting table */
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + EVENTS_TABLE;

    /** SQL query for creating table */
    private static final String SQL_CREATE_TABLE = "create table " +
            EVENTS_TABLE + " ("+
            EVENT_ID_COLUMN +" integer primary key autoincrement," +
            EVENT_API_ID_COLUMN +" integer not null," +
            EVENT_USER_ID_COLUMN +" integer not null," +
            EVENT_TYPE_COLUMN +" text not null," +
            EVENT_HOUR_COLUMN +" integer not null," +
            EVENT_MINUTE_COLUMN +" integer not null," +
            EVENT_DAY_COLUMN +" integer not null," +
            EVENT_MONTH_COLUMN +" integer not null," +
            EVENT_YEAR_COLUMN +" integer not null," +
            EVENT_DESCRIPTION_COLUMN +" text not null," +
            EVENT_INSTANTLY_COLUMN +" integer not null," +
            EVENT_INTERVAL_COLUMN + " integer not null," +
            EVENT_REPETITION_STOP_COLUMN + " integer not null," +
            EVENT_TIMEOUT_COLUMN + " integer not null," +
            EVENT_REP_TYPE_COLUMN +" text not null," +
            EVENT_PREV_ALARMS_COLUMN +" text not null," +
            EVENT_LAST_UPDATE_COLUMN + " integer not null," +
            PENDING_OPERATION_COLUMN + " text not null);";

    public SQLiteManager(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    //Adds a new event to the data base:
    public long addEvent(JSONObject eventJson) {

        SQLiteDatabase database = this.getWritableDatabase();

        //The id is self-generated, so the event object lacks its id until it is added to the database:
        long id = database.insert(EVENTS_TABLE,null, getValuesFromJson(eventJson));

        database.close();

        return id;
    }

    //Updates the data of an existing event:
    public void updateEvent(JSONObject eventJson, Long id) {

        SQLiteDatabase database = this.getWritableDatabase();

        String selection = EVENT_ID_COLUMN + " = ?";
        String[] selectionArgs = {id.toString()};

        database.update(EVENTS_TABLE, getValuesFromJson(eventJson), selection, selectionArgs);

        database.close();
    }

    //Gets the cursor for an stored event by its id:
    private Cursor getStoredEventCursor(Long eventId) {
        Integer user = UserDataReceiverService.getUserId();

        String selection = EVENT_ID_COLUMN + " = ?" + " AND " + EVENT_USER_ID_COLUMN + " = ?";
        String[] selectionArgs = {eventId.toString(), user.toString()};

        SQLiteDatabase database = this.getReadableDatabase();

        return database.query(
                EVENTS_TABLE, //Table name
                null, //Projection (null implies that all columns will be returned)
                selection, //Arguments for the where clause
                selectionArgs, //Selection arguments for the where clause
                null, //Group by clause
                null, //Having clause
                null //Order by clause
        );
    }

    //Gets the api last update for an specific event in order to check if the event was updated to add an api id (the stored api id will be equal to -1):
    public long getLastUpdateFor(long eventId){

        long lastUpdate = -1;

        Cursor cursor = getStoredEventCursor(eventId);

        if(cursor.getCount() > 0){

            cursor.moveToFirst();

            lastUpdate = Long.parseLong(cursor.getString(cursor.getColumnIndexOrThrow(EVENT_LAST_UPDATE_COLUMN)));
        }

        cursor.close();

        return lastUpdate;
    }

    //Gets the events for a particular month and year (if month value is equal to -1 then gets all the stored events):
    public JSONObject[] getEvents(int eventMonth, int eventYear, boolean resynchronizing, boolean displayAll){

        JSONObject[] jsonArray = null;

        Integer user = UserDataReceiverService.getUserId();
        Integer month = eventMonth;
        Integer year = eventYear;

        String userIdSelection = EVENT_USER_ID_COLUMN + " = ?";
        String[] userIdSelectionArgs = {user.toString(), "DELETE"};
        String[] userIdSelectionArgsSync = {user.toString()};

        String dateSelection = EVENT_MONTH_COLUMN + " = ?" + " AND " + EVENT_YEAR_COLUMN + " = ?";
        String[] dateSelectionArgs = {month.toString(), year.toString(),"DELETE"};
        String[] dateSelectionArgsSync = {month.toString(), year.toString()};
        String[] dateUserSelectionArgs = {month.toString(), year.toString(), user.toString(), "DELETE"};
        String[] dateUserSelectionArgsSync = {month.toString(), year.toString(), user.toString()};

        String selection = null;
        String[] selectionArgs = null;

        //When the user id stored in the event receiver activity is different from -1, only the events created by that user will be retrieved:
        if (UserDataReceiverService.getUserId() != -1) {
            //Get only specific month and year events:
            if (eventMonth != -1) {
                selection = dateSelection + " AND " + userIdSelection;
                selectionArgs = dateUserSelectionArgs;
                if (resynchronizing) selectionArgs = dateUserSelectionArgsSync;
            }
            //Get all the events:
            else {
                selection = userIdSelection;
                selectionArgs = userIdSelectionArgs;
                if (resynchronizing) selectionArgs = userIdSelectionArgsSync;
            }
        }

        else {
            if (eventMonth != -1) {
                selection = dateSelection;
                selectionArgs = dateSelectionArgs;
                if (resynchronizing) selectionArgs = dateSelectionArgsSync;
            }
        }

        //Those events that were deleted from the database but not from the API are restored in the database with the value "DELETE" in the field PENDING_OPERATION_COLUMN:
        if (!resynchronizing) {
            selection = selection + " AND " + PENDING_OPERATION_COLUMN + " <> ?";

            //The repetitive events must be ignored:
            if (eventMonth != -1) selection = selection + " AND " + EVENT_INTERVAL_COLUMN + " = 0 ";

            else if (displayAll) selection = selection + " AND ( " + EVENT_INTERVAL_COLUMN + " != 0 " + " OR " + getNotOutOfDateSelection() + " ) ";
        }

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                EVENTS_TABLE, //Table name
                null, //Projection (null implies that all columns will be returned)
                selection, //Arguments for the where clause
                selectionArgs, //Selection arguments for the where clause
                null, //Group by clause
                null, //Having clause
                EVENT_YEAR_COLUMN + " asc" + "," +
                        EVENT_MONTH_COLUMN + " asc" + "," +
                        EVENT_DAY_COLUMN + " asc" + "," +
                        EVENT_HOUR_COLUMN + " asc" + "," +
                        EVENT_MINUTE_COLUMN + " asc"
        );

        if(cursor.getCount() > 0){

            jsonArray = new JSONObject[cursor.getCount()];

            cursor.moveToFirst();

            try{
                do {
                    JSONObject event = new JSONObject();

                    event.put("event", getJsonFromCursor(cursor));

                    jsonArray[cursor.getPosition()] = event;

                }while(cursor.moveToNext());
            }
            catch (JSONException jse){
                Log.e(TAG,"getEvents. JSONException: " + jse.getMessage());
            }
        }

        cursor.close();
        database.close();

        return jsonArray;
    }

    private String getNotOutOfDateSelection() {

        int minute, hour, day, month, year;

        Calendar calendar = Calendar.getInstance();

        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        return (
            EVENT_YEAR_COLUMN + " > " + year + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " > " + month + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " = " + month + " AND " + EVENT_DAY_COLUMN + " > " + day + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " = " + month + " AND " + EVENT_DAY_COLUMN + " = " + day + " AND " + EVENT_HOUR_COLUMN + " > " + hour + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " = " + month + " AND " + EVENT_DAY_COLUMN + " = " + day + " AND " + EVENT_HOUR_COLUMN + " = " + hour + " AND " + EVENT_MINUTE_COLUMN + " >= " + minute
        );
    }

    //Gets the events for a particular month and year (if month value is equal to -1 then gets all the stored events):
    /*public JSONObject[] getEvents(int eventMonth, int eventYear, boolean resynchronizing){

        JSONObject[] jsonArray = null;

        Integer user = UserDataReceiverService.getUserId();
        Integer month = eventMonth;
        Integer year = eventYear;

        String userIdSelection = EVENT_USER_ID_COLUMN + " = ?";
        String[] userIdSelectionArgs = {user.toString(), "DELETE"};
        String[] userIdSelectionArgsSync = {user.toString()};

        String dateSelection = EVENT_MONTH_COLUMN + " = ?" + " AND " + EVENT_YEAR_COLUMN + " = ?";
        String[] dateSelectionArgs = {month.toString(), year.toString(), "DELETE"};
        String[] dateSelectionArgsSync = {month.toString(), year.toString()};
        String[] dateUserSelectionArgs = {month.toString(), year.toString(), user.toString(), "DELETE"};
        String[] dateUserSelectionArgsSync = {month.toString(), year.toString(), user.toString()};

        String selection = null;
        String[] selectionArgs = null;

        //When the user id stored in the event receiver activity is different from -1, only the events created by that user will be retrieved:
        if (UserDataReceiverService.getUserId() != -1) {
            //Get only specific month and year events:
            if (eventMonth != -1) {
                selection = dateSelection + " AND " + userIdSelection;
                selectionArgs = dateUserSelectionArgs;
                if (resynchronizing) selectionArgs = dateUserSelectionArgsSync;
            }
            //Get all the events:
            else {
                selection = userIdSelection;
                selectionArgs = userIdSelectionArgs;
                if (resynchronizing) selectionArgs = userIdSelectionArgsSync;
            }
        }

        else {
            if (eventMonth != -1) {
                selection = dateSelection;
                selectionArgs = dateSelectionArgs;
                if (resynchronizing) selectionArgs = dateSelectionArgsSync;
            }
        }

        //Those events that were deleted from the database but not from the API are restored in the database with the value "DELETE" in the field PENDING_OPERATION_COLUMN:
        if (!resynchronizing) selection = selection + " AND " + PENDING_OPERATION_COLUMN + " <> ?";

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                EVENTS_TABLE, //Table name
                null, //Projection (null implies that all columns will be returned)
                selection, //Arguments for the where clause
                selectionArgs, //Selection arguments for the where clause
                null, //Group by clause
                null, //Having clause
                EVENT_YEAR_COLUMN + " asc" + "," +
                        EVENT_MONTH_COLUMN + " asc" + "," +
                        EVENT_DAY_COLUMN + " asc" + "," +
                        EVENT_HOUR_COLUMN + " asc" + "," +
                        EVENT_MINUTE_COLUMN + " asc"
        );

        if(cursor.getCount() > 0){

            jsonArray = new JSONObject[cursor.getCount()];

            cursor.moveToFirst();

            try{
                do {
                    JSONObject event = new JSONObject();

                    event.put("event", getJsonFromCursor(cursor));

                    jsonArray[cursor.getPosition()] = event;

                }while(cursor.moveToNext());
            }
            catch (JSONException jse){
                Log.e(TAG,"getEvents. JSONException: " + jse.getMessage());
            }
        }

        cursor.close();
        database.close();

        return jsonArray;
    }*/

    //Gets the repetitive events whose repetition stop date is posterior to the one related to the currentMonth and currentYear parameters:
    public JSONObject[] getRepetitiveEvents(int currentMonth, int currentYear){

        JSONObject[] jsonArray = null;

        Integer user = UserDataReceiverService.getUserId();
        Long date = DateUtils.transformToMillis(0, 0, 1, currentMonth, currentYear);

        String userIdSelection = EVENT_USER_ID_COLUMN + " = ?";

        String dateSelection = EVENT_INTERVAL_COLUMN + " > 0 " + " AND ( " + EVENT_REPETITION_STOP_COLUMN + " >= ? " + " OR " + EVENT_REPETITION_STOP_COLUMN + " = -1 ) ";
        String[] dateSelectionArgs = {date.toString(), "DELETE"};
        String[] dateUserSelectionArgs = {date.toString(), user.toString(), "DELETE"};

        String selection ;
        String[] selectionArgs;

        //When the user id stored in the event receiver activity is different from -1, only the events created by that user will be retrieved:
        if (UserDataReceiverService.getUserId() != -1) {
            selection = dateSelection + " AND " + userIdSelection;
            selectionArgs = dateUserSelectionArgs;
        }

        else {
            selection = dateSelection;
            selectionArgs = dateSelectionArgs;
        }

        selection = selection + " AND " + PENDING_OPERATION_COLUMN + " <> ?";

        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.query(
                EVENTS_TABLE, //Table name
                null, //Projection (null implies that all columns will be returned)
                selection, //Arguments for the where clause
                selectionArgs, //Selection arguments for the where clause
                null, //Group by clause
                null, //Having clause
                EVENT_YEAR_COLUMN + " asc" + "," +
                        EVENT_MONTH_COLUMN + " asc" + "," +
                        EVENT_DAY_COLUMN + " asc" + "," +
                        EVENT_HOUR_COLUMN + " asc" + "," +
                        EVENT_MINUTE_COLUMN + " asc"
        );

        if(cursor.getCount() > 0){

            jsonArray = new JSONObject[cursor.getCount()];

            cursor.moveToFirst();

            try{
                do {
                    JSONObject event = new JSONObject();

                    event.put("event", getJsonFromCursor(cursor));

                    jsonArray[cursor.getPosition()] = event;

                }while(cursor.moveToNext());
            }
            catch (JSONException jse){
                Log.e(TAG,"getEvents. JSONException: " + jse.getMessage());
            }
        }

        cursor.close();
        database.close();

        return jsonArray;
    }

    //Deletes a single event based on the id field:
    public void deleteSingleEvent (long id) {
        SQLiteDatabase database = this.getWritableDatabase();

        database.execSQL("delete from " + EVENTS_TABLE +
                         " where " + EVENT_ID_COLUMN +
                         " = " + id);

        //When the database has not events, the id count is restarted:
        if (hasNotEvents(database)) database.execSQL("update sqlite_sequence set seq = 0 where name = 'events'");

        database.close();
    }

    //Deletes all the events from the database for a specific user, if there is one logged into the accounts app:
    public void deleteAllEvents() {
        SQLiteDatabase database = this.getWritableDatabase();

        String sqlQuery = "delete from " + EVENTS_TABLE;
        int user = UserDataReceiverService.getUserId();

        //If there is a user logged into the app (user != -1), only his or her events will be deleted:
        if (user != -1) sqlQuery = "delete from " + EVENTS_TABLE + " where " + EVENT_USER_ID_COLUMN + " = " + user;

        database.execSQL(sqlQuery);

        //When the database has not events, the id count is restarted:
        if (hasNotEvents(database)) database.execSQL("update sqlite_sequence set seq = 0 where name = 'events'");

        database.close();
    }

    //Returns whether or not the database has events stored:
    private boolean hasNotEvents(SQLiteDatabase database){
        boolean hasNotEvents = true;

        Cursor cursor = database.query(EVENTS_TABLE, //Table name
                new String[]{EVENT_ID_COLUMN}, //Projection
                null, //Filter
                null, //Selection args
                null, //Group by
                null, //Having, like a SQL HAVING clause
                null //Order by
        );

        if(cursor.getCount()>0) hasNotEvents = false;

        cursor.close();

        return hasNotEvents;
    }

    //Returns a ContentValues object from a Json object:
    private ContentValues getValuesFromJson(JSONObject eventJson) {

        ContentValues values = new ContentValues();

        try {
            JSONObject json = eventJson;
            if (eventJson.has("event"))
                json = eventJson.getJSONObject("event");

            values.put(EVENT_API_ID_COLUMN,json.getString(EventUtils.EVENT_API_ID_FIELD));
            values.put(EVENT_USER_ID_COLUMN,json.getString(EventUtils.EVENT_USER_FIELD));
            values.put(EVENT_TYPE_COLUMN,json.getString(EventUtils.EVENT_TYPE_FIELD));
            values.put(EVENT_HOUR_COLUMN,json.getInt(EventUtils.EVENT_HOUR_FIELD));
            values.put(EVENT_MINUTE_COLUMN,json.getInt(EventUtils.EVENT_MINUTE_FIELD));
            values.put(EVENT_DAY_COLUMN,json.getInt(EventUtils.EVENT_DAY_FIELD));
            values.put(EVENT_MONTH_COLUMN,json.getInt(EventUtils.EVENT_MONTH_FIELD));
            values.put(EVENT_YEAR_COLUMN,json.getInt(EventUtils.EVENT_YEAR_FIELD));
            values.put(EVENT_DESCRIPTION_COLUMN,json.getString(EventUtils.EVENT_DESCRIPTION_FIELD));
            values.put(EVENT_INSTANTLY_COLUMN,json.getInt(EventUtils.EVENT_INSTANTLY_FIELD));
            values.put(EVENT_INTERVAL_COLUMN,json.getInt(EventUtils.EVENT_INTERVAL_FIELD));
            values.put(EVENT_REPETITION_STOP_COLUMN,json.getLong(EventUtils.EVENT_REPETITION_STOP_FIELD));
            values.put(EVENT_TIMEOUT_COLUMN,json.getLong(EventUtils.EVENT_TIMEOUT_FIELD));
            values.put(EVENT_REP_TYPE_COLUMN,json.getString(EventUtils.EVENT_REP_TYPE_FIELD));
            values.put(EVENT_PREV_ALARMS_COLUMN,json.getString(EventUtils.EVENT_PREV_ALARMS_FIELD));
            values.put(EVENT_LAST_UPDATE_COLUMN,json.getLong(EventUtils.EVENT_LAST_UPDATE_FIELD));
            values.put(PENDING_OPERATION_COLUMN,json.getString(EventUtils.EVENT_PENDING_OP_FIELD));
        }
        catch (JSONException jse){
            Log.e(TAG,"getValuesFromJson. JSONException: " + jse.getMessage());
        }

        return values;
    }

    //Returns a Json object from a Cursor object:
    private JSONObject getJsonFromCursor(Cursor cursor) throws JSONException{
        JSONObject auxJson = new JSONObject();

        auxJson.put(EventUtils.EVENT_ID_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_ID_COLUMN)));
        auxJson.put(EventUtils.EVENT_API_ID_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_API_ID_COLUMN)));
        auxJson.put(EventUtils.EVENT_USER_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_USER_ID_COLUMN)));
        auxJson.put(EventUtils.EVENT_TYPE_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_TYPE_COLUMN)));
        auxJson.put(EventUtils.EVENT_HOUR_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_HOUR_COLUMN)));
        auxJson.put(EventUtils.EVENT_MINUTE_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_MINUTE_COLUMN)));
        auxJson.put(EventUtils.EVENT_DAY_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_DAY_COLUMN)));
        auxJson.put(EventUtils.EVENT_MONTH_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_MONTH_COLUMN)));
        auxJson.put(EventUtils.EVENT_YEAR_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_YEAR_COLUMN)));
        auxJson.put(EventUtils.EVENT_DESCRIPTION_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_DESCRIPTION_COLUMN)));
        auxJson.put(EventUtils.EVENT_INSTANTLY_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_INSTANTLY_COLUMN)));
        auxJson.put(EventUtils.EVENT_INTERVAL_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_INTERVAL_COLUMN)));
        auxJson.put(EventUtils.EVENT_REPETITION_STOP_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_REPETITION_STOP_COLUMN)));
        auxJson.put(EventUtils.EVENT_TIMEOUT_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_TIMEOUT_COLUMN)));
        auxJson.put(EventUtils.EVENT_REP_TYPE_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_REP_TYPE_COLUMN)));
        auxJson.put(EventUtils.EVENT_PREV_ALARMS_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_PREV_ALARMS_COLUMN)));
        auxJson.put(EventUtils.EVENT_LAST_UPDATE_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_LAST_UPDATE_COLUMN)));
        auxJson.put(EventUtils.EVENT_PENDING_OP_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(PENDING_OPERATION_COLUMN)));

        return auxJson;
    }
}
