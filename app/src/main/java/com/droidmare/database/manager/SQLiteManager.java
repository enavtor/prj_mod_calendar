package com.droidmare.database.manager;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.droidmare.calendar.models.CalEventJsonObject;
import com.droidmare.common.models.ConstantValues;
import com.droidmare.common.models.EventJsonObject;
import com.droidmare.common.utils.DateUtils;

import org.json.JSONException;

import java.util.Calendar;

//SQLite manager (for managing events storing) declaration
//@author Eduardo on 07/03/2018.

public class SQLiteManager extends SQLiteOpenHelper{

    private static final String TAG = SQLiteManager.class.getCanonicalName();

    //Database name:
    public static final String DATABASE_NAME = "event.sqlite";

    //Database version:
    public static final int DATABASE_VERSION = 1;

    //Events table:
    private static final String EVENTS_TABLE = "events";

    //Events local id column:
    private static final String EVENT_LOCAL_ID_COLUMN = "_id";

    //Events actual id column:
    private static final String EVENT_ID_COLUMN = "id";

    //Events type column:
    private static final String EVENT_TYPE_COLUMN = "type";

    //Events hour column:
    private static final String EVENT_HOUR_COLUMN = "hour";

    //Events minute column:
    private static final String EVENT_MINUTE_COLUMN = "minute";

    //Events day column:
    private static final String EVENT_DAY_COLUMN = "day";

    //Events month column:
    private static final String EVENT_MONTH_COLUMN = "month";

    //Events year column:
    private static final String EVENT_YEAR_COLUMN = "year";

    //Events description:
    private static final String EVENT_DESCRIPTION_COLUMN = "description";

    //Events interval time field:
    private static final String EVENT_INTERVAL_COLUMN = "interval";

    //Events repetition stop field:
    private static final String EVENT_REPETITION_STOP_COLUMN = "stop";

    //Events time out field:
    private static final String EVENT_TIMEOUT_COLUMN = "timeOut";

    //Events repetition type field:
    private static final String EVENT_REP_TYPE_COLUMN = "repType";

    //Events previous alarms field:
    private static final String EVENT_PREV_ALARMS_COLUMN = "prevAlarms";

    //Events pending operation type:
    private static final String PENDING_OPERATION_COLUMN= "pendingOperation";

    //Events last api update field:
    private static final String EVENT_LAST_UPDATE_COLUMN= "lastUpdate";

    //SQL query for deleting table:
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + EVENTS_TABLE;

    //SQL query for creating table:
    private static final String SQL_CREATE_TABLE = "create table " +
        EVENTS_TABLE + " ("+
        EVENT_LOCAL_ID_COLUMN +" integer primary key autoincrement," +
        EVENT_ID_COLUMN +" text not null," +
        EVENT_TYPE_COLUMN +" text not null," +
        EVENT_HOUR_COLUMN +" integer not null," +
        EVENT_MINUTE_COLUMN +" integer not null," +
        EVENT_DAY_COLUMN +" integer not null," +
        EVENT_MONTH_COLUMN +" integer not null," +
        EVENT_YEAR_COLUMN +" integer not null," +
        EVENT_DESCRIPTION_COLUMN +" text not null," +
        EVENT_INTERVAL_COLUMN + " integer not null," +
        EVENT_REPETITION_STOP_COLUMN + " integer not null," +
        EVENT_TIMEOUT_COLUMN + " integer not null," +
        EVENT_REP_TYPE_COLUMN +" text not null," +
        EVENT_PREV_ALARMS_COLUMN +" text not null," +
        PENDING_OPERATION_COLUMN + " text not null," +
        EVENT_LAST_UPDATE_COLUMN + " integer not null);";

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
    public long addEvent(EventJsonObject eventJson) {

        SQLiteDatabase database = this.getWritableDatabase();

        //The id is self-generated, so the event object lacks its id until it is added to the database:
        long id = database.insert(EVENTS_TABLE,null, getValuesFromJson(eventJson));

        database.close();

        return id;
    }

    //Updates the data of an existing event:
    public void updateEvent(EventJsonObject eventJson, String id) {

        String[] splitId = id.split(":");

        SQLiteDatabase database = this.getWritableDatabase();

        String selection = EVENT_ID_COLUMN + " = ?";

        if (splitId[0].equals(ConstantValues.LOCAL_ID_HEAD.replace(":", ""))) {
            selection = EVENT_LOCAL_ID_COLUMN + " = ?";
            id = splitId[1];
        }

        String[] selectionArgs = {id};

        database.update(EVENTS_TABLE, getValuesFromJson(eventJson), selection, selectionArgs);

        database.close();
    }

    //Gets the events for a particular month and year (if month value is equal to -1 then it returns all the stored events):
    public EventJsonObject[] getEvents(int month, int year, boolean getRepetitiveEvents, boolean resynchronizing, boolean displayAll){

        EventJsonObject[] jsonArray = null;

        String selection = null;
        String auxSelectionArgs = null;

        String ARGS_SEPARATOR = ";";

        if (!resynchronizing) {
            //Those events that were deleted from the database but not from the API are restored in the database with the value "DELETE" in the field PENDING_OPERATION_COLUMN:
            selection = PENDING_OPERATION_COLUMN + " <> ? ";
            auxSelectionArgs = "DELETE" + ARGS_SEPARATOR;

            //Get only specific month and year events:
            if (month != -1) {
                if (!getRepetitiveEvents) {
                    selection += " AND " + EVENT_MONTH_COLUMN + " = ? " + " AND " + EVENT_YEAR_COLUMN + " = ? " + " AND " + EVENT_INTERVAL_COLUMN + " = ? ";
                    auxSelectionArgs += month + ARGS_SEPARATOR + year + ARGS_SEPARATOR + 0;
                }
                else {
                    selection +=  " AND " + EVENT_INTERVAL_COLUMN + " > ? " + " AND ( " + EVENT_REPETITION_STOP_COLUMN + " >= ? " + " OR " + EVENT_REPETITION_STOP_COLUMN + " = ? ) ";
                    auxSelectionArgs += 0 + ARGS_SEPARATOR + DateUtils.transformToMillis(0, 0, 1, month, year) + ARGS_SEPARATOR + -1;
                }
            }

            //Get all the events (except form those whose date has already passed and have not a repetition):
            else if (displayAll) selection += " AND " + EVENT_INTERVAL_COLUMN + " <> 0 " + " OR " + getNotOutOfDateSelection();
        }

        SQLiteDatabase database = this.getReadableDatabase();

        String[] selectionArgs = auxSelectionArgs != null ? auxSelectionArgs.split(ARGS_SEPARATOR) : null;

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

            jsonArray = new EventJsonObject[cursor.getCount()];

            cursor.moveToFirst();

            do {
                EventJsonObject event = getJsonFromCursor(cursor);

                jsonArray[cursor.getPosition()] = event;

            } while(cursor.moveToNext());
        }

        cursor.close();
        database.close();

        return jsonArray;
    }

    //Method that returns the condition that must be met for an event to be extracted from the database when not outdated events are required:
    private String getNotOutOfDateSelection() {

        int minute, hour, day, month, year;

        Calendar calendar = Calendar.getInstance();

        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);

        //This condition is constructed here and not in the method that requires it due to its length:
        return (
            EVENT_YEAR_COLUMN + " > " + year + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " > " + month + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " = " + month + " AND " + EVENT_DAY_COLUMN + " > " + day + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " = " + month + " AND " + EVENT_DAY_COLUMN + " = " + day + " AND " + EVENT_HOUR_COLUMN + " > " + hour + " OR " +
            EVENT_YEAR_COLUMN + " = " + year + " AND " + EVENT_MONTH_COLUMN + " = " + month + " AND " + EVENT_DAY_COLUMN + " = " + day + " AND " + EVENT_HOUR_COLUMN + " = " + hour + " AND " + EVENT_MINUTE_COLUMN + " >= " + minute
        );
    }

    //Deletes a single event based on the id field:
    public void deleteSingleEvent (String id) {

        SQLiteDatabase database = this.getWritableDatabase();

        String selection = EVENT_ID_COLUMN + " = ?";
        String[] selectionArgs = {id};

        database.delete(EVENTS_TABLE, selection, selectionArgs);

        //When the database has not events, the id count is restarted:
        if (hasNotEvents(database)) database.execSQL("update sqlite_sequence set seq = 0 where name = 'events'");

        database.close();
    }

    //Deletes all the events from the database for a specific user, if there is one logged into the accounts app:
    public void deleteAllEvents() {
        SQLiteDatabase database = this.getWritableDatabase();

        database.execSQL("delete from " + EVENTS_TABLE);

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

        if (cursor.getCount() > 0) hasNotEvents = false;

        cursor.close();

        return hasNotEvents;
    }

    //Returns a ContentValues object from a Json object:
    private ContentValues getValuesFromJson(EventJsonObject eventJson) {

        ContentValues values = new ContentValues();

        try {
            values.put(EVENT_ID_COLUMN, eventJson.getString(ConstantValues.EVENT_ID_FIELD));
            values.put(EVENT_TYPE_COLUMN, eventJson.getString(ConstantValues.EVENT_TYPE_FIELD));
            values.put(EVENT_HOUR_COLUMN, eventJson.getInt(ConstantValues.EVENT_HOUR_FIELD));
            values.put(EVENT_MINUTE_COLUMN, eventJson.getInt(ConstantValues.EVENT_MINUTE_FIELD));
            values.put(EVENT_DAY_COLUMN, eventJson.getInt(ConstantValues.EVENT_DAY_FIELD));
            values.put(EVENT_MONTH_COLUMN, eventJson.getInt(ConstantValues.EVENT_MONTH_FIELD));
            values.put(EVENT_YEAR_COLUMN, eventJson.getInt(ConstantValues.EVENT_YEAR_FIELD));
            values.put(EVENT_DESCRIPTION_COLUMN, eventJson.getString(ConstantValues.EVENT_DESCRIPTION_FIELD));
            values.put(EVENT_INTERVAL_COLUMN, eventJson.getInt(ConstantValues.EVENT_REP_INTERVAL_FIELD));
            values.put(EVENT_REPETITION_STOP_COLUMN, eventJson.getLong(ConstantValues.EVENT_REPETITION_STOP_FIELD));
            values.put(EVENT_TIMEOUT_COLUMN, eventJson.getLong(ConstantValues.EVENT_TIMEOUT_FIELD));
            values.put(EVENT_REP_TYPE_COLUMN, eventJson.getString(ConstantValues.EVENT_REPETITION_TYPE_FIELD));
            values.put(EVENT_PREV_ALARMS_COLUMN, eventJson.getString(ConstantValues.EVENT_PREV_ALARMS_FIELD));
            values.put(PENDING_OPERATION_COLUMN, eventJson.getString(ConstantValues.EVENT_PENDING_OP_FIELD));
            values.put(EVENT_LAST_UPDATE_COLUMN, eventJson.getLong(ConstantValues.EVENT_LAST_UPDATE_FIELD));
        }
        catch (JSONException jse){
            Log.e(TAG,"getValuesFromJson. JSONException: " + jse.getMessage());
        }

        return values;
    }

    //Returns a Json object from a Cursor object:
    private EventJsonObject getJsonFromCursor(Cursor cursor) {
        CalEventJsonObject auxJson = new CalEventJsonObject();

        auxJson.put(ConstantValues.EVENT_ID_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_ID_COLUMN)));
        auxJson.put(ConstantValues.EVENT_TYPE_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_TYPE_COLUMN)));
        auxJson.put(ConstantValues.EVENT_HOUR_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_HOUR_COLUMN)));
        auxJson.put(ConstantValues.EVENT_MINUTE_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_MINUTE_COLUMN)));
        auxJson.put(ConstantValues.EVENT_DAY_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_DAY_COLUMN)));
        auxJson.put(ConstantValues.EVENT_MONTH_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_MONTH_COLUMN)));
        auxJson.put(ConstantValues.EVENT_YEAR_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_YEAR_COLUMN)));
        auxJson.put(ConstantValues.EVENT_DESCRIPTION_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_DESCRIPTION_COLUMN)));
        auxJson.put(ConstantValues.EVENT_REP_INTERVAL_FIELD, cursor.getInt(cursor.getColumnIndexOrThrow(EVENT_INTERVAL_COLUMN)));
        auxJson.put(ConstantValues.EVENT_REPETITION_STOP_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_REPETITION_STOP_COLUMN)));
        auxJson.put(ConstantValues.EVENT_TIMEOUT_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_TIMEOUT_COLUMN)));
        auxJson.put(ConstantValues.EVENT_REPETITION_TYPE_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_REP_TYPE_COLUMN)));
        auxJson.put(ConstantValues.EVENT_PREV_ALARMS_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(EVENT_PREV_ALARMS_COLUMN)));
        auxJson.put(ConstantValues.EVENT_PENDING_OP_FIELD, cursor.getString(cursor.getColumnIndexOrThrow(PENDING_OPERATION_COLUMN)));
        auxJson.put(ConstantValues.EVENT_LAST_UPDATE_FIELD, cursor.getLong(cursor.getColumnIndexOrThrow(EVENT_LAST_UPDATE_COLUMN)));

        return auxJson;
    }
}