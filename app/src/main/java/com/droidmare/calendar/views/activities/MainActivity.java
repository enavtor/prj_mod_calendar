package com.droidmare.calendar.views.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.EventJsonObject;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.services.ApiConnectionService;
import com.droidmare.calendar.services.ApiSynchronizationService;
import com.droidmare.calendar.services.DateCheckerService;
import com.droidmare.calendar.services.UserDataService;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.utils.HomeKeyUtils;
import com.droidmare.calendar.utils.PackageUtils;
import com.droidmare.calendar.utils.ToastUtils;
import com.droidmare.calendar.views.fragments.CalendarFragment;
import com.droidmare.calendar.views.fragments.EventFragment;
import com.droidmare.database.publisher.EventsPublisher;
import com.droidmare.reminders.model.Reminder;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import static com.droidmare.calendar.utils.ToastUtils.DEFAULT_TOAST_DURATION;
import static com.droidmare.calendar.utils.ToastUtils.DEFAULT_TOAST_SIZE;

//Main activity declaration
//@author Eduardo on 07/02/2018.

public class MainActivity extends AppCompatActivity {

    //Accounts application package:
    private static final String ACCOUNTS_PACKAGE = "com.droidmare.accounts";
    private static final String ACCOUNTS_ACTIVITY = "com.droidmare.accounts.views.activities.MainActivity";

    //Static attribute and method to know if the app is running or not:
    private static boolean isCreated = false;
    public static boolean isCreated() { return isCreated; }

    //Values of the request codes for the activities that are started in this activity:
    private int DISPLAY_EVENTS_REQUEST = 0;
    private int NEW_EVENT_REQUEST = 1;
    private int MODIFY_EVENT_REQUEST = 2;
    private int DELETE_EVENT_REQUEST = 3;

    private EventListItem eventToDelete;

    //Elements for handling the fragments:
    private CalendarFragment calendarFrag;

    private RelativeLayout loadingLayout;

    private HomeKeyUtils homeKeyListener;

    @SuppressLint("StaticFieldLeak")
    private static EventFragment eventFrag;

    //A reference to the running activity to close it if a sync operation takes place and the selected day is going to be affected by it:
    private static WeakReference<AppCompatActivity> runningActivityReference;
    public static void setRunningActivityReference(AppCompatActivity activity) {
        runningActivityReference = new WeakReference<>(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isCreated = true;

        homeKeyListener = new HomeKeyUtils(this);

        homeKeyListener.setOnHomePressedListener(new HomeKeyUtils.OnHomePressedListener() {
            @Override
            public void onHomePressed() { homeKeyPressed(); }

            @Override
            public void onHomeLongPressed() { homeKeyPressed(); }
        });

        homeKeyListener.startWatch();

        setContentView(R.layout.activity_main);
        initMainActivity();

        //The loading layout is initialized so it can be displayed or hidden when needed
        loadingLayout = findViewById(R.id.layout_loading);

        //The api connection service needs tho have a reference to the MainActivity context so the views can be updated after modifying or deleting an event:
        ApiConnectionService.setMainActivityReference(this);

        //The api synchronization service needs tho have a reference to the MainActivity context so the views can be updated after adding, modifying or deleting an event:
        ApiSynchronizationService.setMainActivityReference(this);

        //The date checker service is initialised and the date text set:
        DateCheckerService.setMainActivityReference(this);

        if (!DateCheckerService.isInstantiated) startService(new Intent(getApplicationContext(), DateCheckerService.class));
        else DateCheckerService.setActivitiesDate();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCreated = false;
        homeKeyListener.stopWatch();
    }

    //Touch events are disabled in order to avoid application's misbehaviour:
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        //When a touch event happens while an event menu is being displayed, the menu is dismissed (hidden) and the event view is reset:
        checkAndDismissEventMenu();
        return false;
    }

    private void homeKeyPressed () {
        if (runningActivityReference != null && runningActivityReference.get() != null)
            runningActivityReference.get().finish();
        finish();
    }

    public static void checkAndDismissEventMenu() {
        if (eventFrag.displayingEventMenu())
            eventFrag.dismissEventMenu(false);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        //If the loading layout is being display all the key events are blocked so the user can not perform any operation until the current one is done:
        if (isLoadingLayoutDisplayed() ||  event.getAction() == KeyEvent.ACTION_DOWN && ToastUtils.cancelCurrentToast())
            return false;

        else if(event.getAction() == KeyEvent.ACTION_DOWN) {

            View currentFocused = getCurrentFocus();

            //When the focus is over a value increase or decrease button and the center d-pad button is pressed, the values will keep changing until the button is released:
            if(currentFocused != null && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN
                    && (currentFocused.getId() == R.id.prev_month
                    || currentFocused.getId() == R.id.next_month
                    || currentFocused.getId() == R.id.prev_year
                    || currentFocused.getId() == R.id.next_year)) {
                if (getCurrentFocus() != null) getCurrentFocus().performClick();
                return true;
            }

            //When a event menu is being shown, pressing the back button will dismiss that menu:
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && currentFocused != null) {

                int focusedId = currentFocused.getId();

                if (focusedId == R.id.view_event_button || focusedId == R.id.modify_event_button || focusedId == R.id.delete_event_button || focusedId == R.id.dismiss_menu_button) {
                    eventFrag.dismissEventMenu(false);
                    return true;
                }
            }

            //Controller's ir color keys behaviour when they are pressed:
            else if (event.getKeyCode() == KeyEvent.KEYCODE_F2 || event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED)
                launchAccountsApp();

            else if (event.getKeyCode() == KeyEvent.KEYCODE_F3 || event.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN)
                returnToCurrent();

            else if (event.getKeyCode() == KeyEvent.KEYCODE_F4 || event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW)
                startDisplayEventsDialog();

            else if (event.getKeyCode() == KeyEvent.KEYCODE_F5 || event.getKeyCode() == KeyEvent.KEYCODE_PROG_BLUE) {

                //If an event was created while the focus was on an item inside the event list, the event fragment must be notified:
                View currentFocusParent = null;

                if (currentFocused != null) currentFocusParent = (View) currentFocused.getParent();

                //If the event menu is being displayed, the recycler view will be two levels above the focused view's parent:
                if (eventFrag.displayingEventMenu() && currentFocusParent != null) {
                    currentFocusParent = (View) currentFocusParent.getParent();
                    currentFocusParent = (View) currentFocusParent.getParent();
                }

                if (currentFocusParent != null && currentFocusParent.getId() == eventFrag.getEventList().getId())
                    eventFrag.getAdapter().setNextFocusedPosition(eventFrag.getAdapter().getCurrentFocusedPosition());

                startNewEventDialog();
            }

            else if (currentFocused != null) {

                //When the focus is on the left calendar edge and the left button is pressed, the focus must stay where it is:
                if (((View) currentFocused.getParent()).getId() == R.id.calendar_grid && calendarFrag.getFocusedDayPosition() % 7 == 0 && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    return true;
                }

                //When the focus is on the left calendar edge and the left button is pressed, the focus must stay where it is:
                if (((View) currentFocused.getParent()).getId() == R.id.calendar_grid && calendarFrag.getFocusedDayPosition() % 7 == 0 && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    return true;
                }

                //When the focus is on a calendar's right edge element and the event list is empty, the focus is blocked on the current element:
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && calendarFrag.getFocusedDayPosition() % 7 == 6 && eventFrag.getNumberOfEvents() == 0) {
                    return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    //This method starts a display all events dialog, waiting for the result:
    public void startDisplayEventsDialog() {
        eventFrag.goAfterEventDay();
        Intent intent = new Intent(this, DialogDisplayEventsActivity.class);
        startActivityForResult(intent, DISPLAY_EVENTS_REQUEST);
    }

    //This method starts a new event dialog, waiting for the result:
    public void startNewEventDialog() {
        Intent intent = new Intent(this, DialogNewEventActivity.class);
        intent.putExtra(com.droidmare.calendar.utils.EventUtils.EVENT_MONTH_FIELD, calendarFrag.getSelectedMonth());
        intent.putExtra(com.droidmare.calendar.utils.EventUtils.EVENT_YEAR_FIELD, calendarFrag.getSelectedYear());
        startActivityForResult(intent, NEW_EVENT_REQUEST);
    }

    //This method starts a modify event dialog, waiting for the result:
    public void startModifyEventDialog(EventListItem selectedEvent, boolean onlyDisplayValues) {

        EventJsonObject eventJson = EventJsonObject.createEventJson(selectedEvent);

        Reminder.ReminderType eventType = selectedEvent.getReminderType();

        //Whether or not the selected event is an alarm:
        boolean isAlarm = !(eventType.equals(Reminder.ReminderType.DOCTOR_REMINDER) || eventType.equals(Reminder.ReminderType.PERSONAL_REMINDER));

        Intent intent = new Intent(getApplicationContext(), DialogEventParameters.class);

        intent.putExtra(EventUtils.EVENT_JSON_FIELD, eventJson.toString());

        intent.putExtra("editingEvent", true);
        intent.putExtra("isAlarm", isAlarm);
        intent.putExtra("type", eventType.toString().split("_")[0]);

        if (onlyDisplayValues) {
            intent.putExtra("displayParameters", true);
            DialogEventParameters.title = selectedEvent.getTitleText().split(" - ")[1];
            DialogEventParameters.icon = selectedEvent.getEventIcon();
            startActivity(intent);
        }

        else startActivityForResult(intent, MODIFY_EVENT_REQUEST);
    }

    //This method starts a delete single event confirmation dialog, waiting for the result afterwards:
    public void startDeleteEventDialog(EventListItem eventToDelete) {
        this.eventToDelete = eventToDelete;

        Intent intent = new Intent(this, DialogDeleteActivity.class);

        intent.putExtra("deleteSingleEvent", true);
        startActivityForResult(intent, DELETE_EVENT_REQUEST);
    }

    //This method starts the accounts application so the user can manage his/her account:
    public void launchAccountsApp() {
        Context context = getApplicationContext();

        Intent accountsIntent = PackageUtils.getLaunchIntent(context, ACCOUNTS_PACKAGE);

        if (accountsIntent != null) {
            accountsIntent.setComponent(new ComponentName(ACCOUNTS_PACKAGE, ACCOUNTS_ACTIVITY));
            startActivity(accountsIntent);
        }

        else ToastUtils.makeCustomToast(context, getString(R.string.error_launching_accounts), DEFAULT_TOAST_SIZE, DEFAULT_TOAST_DURATION);
    }

    @Override
    //This method will receive the results from the previous method:
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Resources res = getResources();

        //At this point there are not activities running besides from this one:
        runningActivityReference = null;

        //When an event is selected inside the display all events dialog, the calendar must go to its day:
        if (requestCode == DISPLAY_EVENTS_REQUEST && resultCode == Activity.RESULT_OK) {
            calendarFrag.selectNewDay(false);
            EventsPublisher.retrieveMonthEvents(this);
        }

        //When a new event is selected inside the new event dialog, the event fragment is notified so the operation can be completed:
        else if (requestCode == NEW_EVENT_REQUEST && resultCode == Activity.RESULT_OK) {
            calendarFrag.focusSelectedItem();
            //This operation should be performed in the DialogNewEventActivity, but since CalendarFragment is not accessible from that activity, it is performed here:
            fragmentCreateNewEvent(data);
            //The loading layout will be displayed until the operation ends and the views are refreshed:
            displayLoadingLayout(res.getString(R.string.loading_layout_create));
        }

        //When the new event dialog is dismissed the event fragment must be notified in order to reset the next focused position inside the event list:
        else if (requestCode == NEW_EVENT_REQUEST) {
            eventFrag.getAdapter().setNextFocusedPosition(-1);
        }

        //When an event is modified inside the modify event dialog, the event fragment is notified so the operation can be completed:
        else if (requestCode == MODIFY_EVENT_REQUEST && resultCode == Activity.RESULT_OK) {
            fragmentModifyEvent(data);
        }

        //When the delete all events dialog returns a Ok result and the request was deleting a single event, the event to delete is deleted:
        else if (requestCode == DELETE_EVENT_REQUEST && resultCode == Activity.RESULT_OK) {
            sendDeleteEvent(eventToDelete);
            //The loading layout will be displayed until the operation ends and the views are refreshed:
            displayLoadingLayout(res.getString(R.string.loading_layout_delete_one));
        }
    }

    public void fragmentCreateNewEvent(Intent data) {
        eventFrag.createNewEvent(data);
    }

    public void fragmentModifyEvent(Intent data) {
        eventFrag.modifyEvent(data);
    }

    //This method displays the loading layout by changing its visibility and sets the displayed text:
    public void displayLoadingLayout(String displayedText) {
        loadingLayout.setVisibility(View.VISIBLE);

        TextView loadingText = findViewById(R.id.text_layout_loading);
        loadingText.setText(displayedText);
    }

    //This method hides the loading layout by changing its visibility:
    public void hideLoadingLayout() {
        loadingLayout.setVisibility(View.GONE);
    }

    //This method returns whether the loading layout is being displayed or not:
    private boolean isLoadingLayoutDisplayed() {
        return loadingLayout.getVisibility() == View.VISIBLE;
    }

    //This method starts the api connection service so that the new event can be sent tho the API:
    public void sendPostEvent(EventListItem event){
        Intent dataIntent = new Intent(getApplicationContext(), ApiConnectionService.class);
        dataIntent.putExtra(EventUtils.EVENT_JSON_FIELD, EventJsonObject.createEventJson(event).toString());
        dataIntent.putExtra("operation", ApiConnectionService.REQUEST_METHOD_POST);
        startService(dataIntent);
    }

    //This method starts the api connection service so that the new event can be sent tho the API:
    public void sendEditEvent(EventListItem event){
        Intent dataIntent = new Intent(getApplicationContext(), ApiConnectionService.class);
        dataIntent.putExtra(EventUtils.EVENT_JSON_FIELD, EventJsonObject.createEventJson(event).toString());
        dataIntent.putExtra("operation", ApiConnectionService.REQUEST_METHOD_EDIT);
        startService(dataIntent);
    }

    //This method tells the API to delete an event:
    public void sendDeleteEvent(EventListItem event) {
        Intent dataIntent = new Intent(getApplicationContext(), ApiConnectionService.class);
        dataIntent.putExtra(EventUtils.EVENT_JSON_FIELD, EventJsonObject.createEventJson(event).toString());
        dataIntent.putExtra("operation", ApiConnectionService.REQUEST_METHOD_DELETE);
        startService(dataIntent);
    }

    //This method tells the event fragment to delete an event:
    public void deleteEvent(final EventListItem event) {
        //This method must always be executed in the user interface thread:
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                eventFrag.deleteEvent(event);
            }
        });
    }

    //This method sends the events for the current month to the calendar fragment, so the view can be reloaded:
    public void returnMonthEvents(final ArrayList<EventListItem>[] eventsArray, final ArrayList<EventListItem> repetitiveEventsArray) {
        //This method must always be executed in the user interface thread:
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                calendarFrag.loadMonthEvents(eventsArray, repetitiveEventsArray);
            }
        });
    }

    //This method tells the event fragment to manage the focus relocation after deleting an event:
    public void relocateFocusAfterDelete() {
        eventFrag.relocateFocusAfterDelete();
    }

    //This method tells the event fragment to manage the focus relocation after a synchronization operation took place:
    public void relocateFocusAfterSync(EventListItem localEvent, EventListItem retrievedEvent, String localSyncOp) {

        //A variable to know if the changes will affect the current day's event list is set:
        boolean currentDayAffected = DateUtils.sameDate(localEvent);

        if (retrievedEvent != null) currentDayAffected = (DateUtils.sameDate(localEvent) || DateUtils.isSameDay(localEvent.getNextRepetition(), DateUtils.getCurrentMillis()));

        //If there is an activity running (for example the event parameters dialog) and the modification is going
        //to affect the current selected day's event list it must be finished before getting the current focused view:
        if (runningActivityReference != null && runningActivityReference.get() != null && currentDayAffected)
            runningActivityReference.get().finish();

        View currentFocused = getCurrentFocus();

        //Since the focus relocation is only necessary when the focus is on the event list, the parent of the parent of the current focused view is checked:
        if (currentFocused != null) {

            View currentFocusedParent = (View) currentFocused.getParent();

            if (currentFocusedParent != null) currentFocusedParent = (View) currentFocusedParent.getParent();

            //If the current focused view is one of the event menu buttons, another two parents must be obtained:
            if (eventFrag.displayingEventMenu() && currentFocusedParent != null) {
                currentFocusedParent = (View) currentFocusedParent.getParent();
                if (currentFocusedParent != null) currentFocusedParent = (View) currentFocusedParent.getParent();
            }

            //If, indeed, the final parent of the current focused view is the event list, the focus may need to be relocated:
            if (currentFocusedParent != null && findViewById(R.id.event_list) != null && currentFocusedParent.getId() == findViewById(R.id.event_list).getId()) {

                //The position of the current focused event is obtained:
                int currentFocusedEvent = eventFrag.getAdapter().getCurrentFocusedPosition();

                //If an event was deleted in the API and the focus is located on the deleted event's day list, the focus must be relocated:
                if (localSyncOp.equals(ApiSynchronizationService.LOCAL_SYNC_OP_DELETE) && currentDayAffected)
                    eventFrag.setNextFocusedAfterSyncDelete(currentFocusedEvent);

                    //If an event was modified in the API and the focus is located on the modified event's day list or in a day that falls inside the event repetition, the focus must be relocated:
                else if (localSyncOp.equals(ApiSynchronizationService.LOCAL_SYNC_OP_EDIT) && currentDayAffected)
                    eventFrag.setNextFocusedAfterSyncEdit(currentFocusedEvent, retrievedEvent);

                    //If the operation was creating an event in the API or, the deleted or modified event's day is not the currently selected one, the focus stays where it is:
                else eventFrag.getAdapter().setNextFocusedPosition(currentFocusedEvent);
            }
        }
    }

    //This method will reset the calendar view to the current day's month and year:
    public void returnToCurrent(){
        //The current day must get the focus once the operation has finished:
        eventFrag.goAfterEventDay();

        Calendar currentCalendar = Calendar.getInstance();

        DateUtils.currentDay = currentCalendar.get(Calendar.DAY_OF_MONTH);
        DateUtils.currentMonth = currentCalendar.get(Calendar.MONTH);
        DateUtils.currentYear = currentCalendar.get(Calendar.YEAR);

        calendarFrag.selectNewDay(false);

        EventsPublisher.retrieveMonthEvents(this);
    }

    //This method will reset the calendar view to the selected item's (selected day inside the calendar) month and year:
    public void returnToSelected(){
        //The selected day must get the focus once the operation has finished:
        eventFrag.goAfterEventDay();

        DateUtils.currentMonth = calendarFrag.getSelectedMonth();
        DateUtils.currentYear = calendarFrag.getSelectedYear();

        EventsPublisher.retrieveMonthEvents(this);
    }

    private void initMainActivity(){

        String appVersionNumber = "";

        try{
            appVersionNumber = this.getPackageManager().getPackageInfo(this.getPackageName(),0).versionName;
        }
        catch(PackageManager.NameNotFoundException nfe){
            Log.e("TAG", "onCreate. NameNotFoundException: " + nfe);
        }

        TextView appVersion = findViewById(R.id.version_number);
        appVersion.append(appVersionNumber);

        setButtonsBehaviour();

        //User information:
        setUserInformation();

        //Calendar and events Layout:
        loadFragments();
    }

    //Method for setting the buttons behaviour:
    private void setButtonsBehaviour () {

        findViewById(R.id.ir_delete_launch_accounts_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchAccountsApp();
            }
        });

        findViewById(R.id.ir_move_to_current_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnToCurrent();
            }
        });

        findViewById(R.id.ir_show_all_events_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDisplayEventsDialog();
            }
        });

        findViewById(R.id.ir_add_new_event_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNewEventDialog();
            }
        });

        //Virtual back Button:
        findViewById(R.id.virtual_back_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
            }
        });
    }

    //Method that configures the user information view:
    public void setUserInformation () {
        UserDataService.readSharedPrefs(MainActivity.this.getApplicationContext());

        if (UserDataService.getUserId() != null) {
            final ImageView avatar = findViewById(R.id.user_photo);
            final TextView name = findViewById(R.id.user_name);
            final TextView id = findViewById(R.id.user_id);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    avatar.setImageBitmap(UserDataService.getDecodedAvatar());
                    name.setText(UserDataService.getUserName());
                    id.setText(UserDataService.getUserNickname());
                }
            });
        }
    }

    //Method for setting the date text displayed on the upper right corner of the application:
    public void setDateText () {

        final Calendar calendar = Calendar.getInstance();
        long time =  calendar.getTimeInMillis();
        Locale localeDate = Locale.getDefault();

        SimpleDateFormat simpleDate = new SimpleDateFormat(getString(R.string.date),localeDate);
        String date = simpleDate.format(time);

        final String upperDate = date.substring(0,1).toUpperCase() + date.substring(1);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView dateText = findViewById(R.id.txt_date);
                dateText.setText(upperDate);
            }
        });
    }

    //Method for loading the fragments:
    private void loadFragments () {

        calendarFrag = new CalendarFragment();
        eventFrag = new EventFragment();

        FragmentTransaction fragTrans = getFragmentManager().beginTransaction();

        fragTrans.add(R.id.left, calendarFrag, CalendarFragment.NAME);
        fragTrans.add(R.id.right, eventFrag, EventFragment.NAME);

        fragTrans.commit();
    }
}