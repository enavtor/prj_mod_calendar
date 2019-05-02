package com.droidmare.calendar.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.TypeListItem;
import com.droidmare.calendar.services.UserDataReceiverService;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.utils.ImageUtils;
import com.droidmare.calendar.views.adapters.dialogs.TypeListAdapter;
import com.droidmare.statistics.StatisticAPI;
import com.droidmare.statistics.StatisticService;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

//New event selection activity declaration
//@author Eduardo on 26/02/2018.

public class DialogNewEventActivity extends AppCompatActivity{

    //List of items for the type list adapter:
    private ArrayList<TypeListItem> eventTypes;

    //List of items for the type list adapter when the measure event is selected:
    private ArrayList<TypeListItem> measureTypes;

    //List of items for the type list adapter when the survey event is selected:
    private ArrayList<TypeListItem> surveyTypes;

    //List of items for the type list adapter when the personal event is selected:
    private ArrayList<TypeListItem> personalTypes;

    //Listener for handling on event type items clicks:
    private TypeListAdapter.ItemClickListener listener;

    //Adapter for the list of types:
    private TypeListAdapter typeAdapter;

    //New event dialog view:
    private LinearLayout dialogView;

    //The dialog's title text:
    private TextView dialogTitle;

    //New event reminder type:
    private TypeListItem.eventTypes eventType;

    //The selected item inside the dialog:
    private TypeListItem selectedItem;

    //New event reminder parameters:
    private int eventHour;
    private int eventMinute;
    private int eventDay;
    private int eventMonth;
    private int eventYear;
    private int eventInterval;
    private long eventRepetitionStop;
    private String eventDescription;
    private String eventPreviousAlarms;
    private String eventRepetitionType;

    //Control variable to know if the measure types dialog is being shown:
    private boolean showingSecondaryDialog;

    //Control variable to know if the selected event is of type measure or stimulus:
    private boolean hasDefaultDescription;

    private StatisticService statistic;

    //A reference to the running activity to close it if this activity is closed:
    private static WeakReference<AppCompatActivity> launchedActivityReference;
    public static void setLaunchedActivityReference(AppCompatActivity activity) {
        launchedActivityReference = new WeakReference<>(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MainActivity.setRunningActivityReference(this);

        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        setContentView(R.layout.activity_dialog_new_event);

        dialogView = findViewById(R.id.new_event_dialog);
        dialogTitle = findViewById(R.id.new_event_dialog_title);

        showingSecondaryDialog = hasDefaultDescription = false;

        //Initialization of the listener for on items click:
        setButtonsBehaviour();

        //Initialization of the new event dialog:
        initEventTypes();
        initEventsDialog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        hasDefaultDescription = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_DESTROY, getClass().getCanonicalName());
        if (launchedActivityReference != null && launchedActivityReference.get() != null)
            launchedActivityReference.get().finish();
    }

    //Touch events are disabled in order to avoid application's misbehaviour:
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if(event.getAction() == KeyEvent.ACTION_DOWN  && (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED || event.getKeyCode() == KeyEvent.KEYCODE_F2 || event.getKeyCode() == KeyEvent.KEYCODE_BACK)) {

            hasDefaultDescription = false;

            //When the measure type dialog is being shown, pressing the back button will dismiss that menu and return to the event type dialog:
            if (showingSecondaryDialog) {

                showingSecondaryDialog = false;

                dialogTitle.setText(R.string.new_event_dialog_title);
                typeAdapter.changeElementsVisibility(eventTypes);

                return true;
            }
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    //This method will receive the selected time in the time picker dialog:
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){

                eventHour = data.getIntExtra(EventUtils.EVENT_HOUR_FIELD, -1);
                eventMinute = data.getIntExtra(EventUtils.EVENT_MINUTE_FIELD, -1);

                eventDay = data.getIntExtra(EventUtils.EVENT_DAY_FIELD, DateUtils.currentDay);
                eventMonth = data.getIntExtra(EventUtils.EVENT_MONTH_FIELD, DateUtils.currentMonth);
                eventYear = data.getIntExtra(EventUtils.EVENT_YEAR_FIELD, DateUtils.currentYear);

                eventInterval = data.getIntExtra(EventUtils.EVENT_INTERVAL_FIELD, 0);

                if (data.hasExtra(EventUtils.EVENT_PREV_ALARMS_FIELD))
                    eventPreviousAlarms = data.getStringExtra(EventUtils.EVENT_PREV_ALARMS_FIELD);

                //The repetition stop is set only if the event has a repetition interval:
                if (eventInterval != 0) {

                    if (data.hasExtra(EventUtils.EVENT_REP_TYPE_FIELD))
                        eventRepetitionType = data.getStringExtra(EventUtils.EVENT_REP_TYPE_FIELD);

                    eventRepetitionStop = data.getLongExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, -1);
                }

                eventDescription = data.getStringExtra(EventUtils.EVENT_DESCRIPTION_FIELD);

                //Now we need to send a response with the event type to the main activity,
                //so it can communicate it to the event fragment in order to create a new event:
                sendNewEventType();
            }

            if (resultCode == Activity.RESULT_CANCELED) {
                //If no time is selected, the new event dialog must be displayed again:
                dialogView.setVisibility(View.VISIBLE);
            }
        }
    }

    //This method sends the event type selected so the event fragment can do as needed with it:
    private void sendNewEventType () {
        Intent returnIntent = new Intent();

        returnIntent.putExtra(EventUtils.EVENT_TYPE_FIELD, eventType.toString());
        returnIntent.putExtra(EventUtils.EVENT_HOUR_FIELD, eventHour);
        returnIntent.putExtra(EventUtils.EVENT_MINUTE_FIELD, eventMinute);

        returnIntent.putExtra(EventUtils.EVENT_DAY_FIELD, eventDay);
        returnIntent.putExtra(EventUtils.EVENT_MONTH_FIELD, eventMonth);
        returnIntent.putExtra(EventUtils.EVENT_YEAR_FIELD, eventYear);

        returnIntent.putExtra(EventUtils.EVENT_DESCRIPTION_FIELD, eventDescription);
        returnIntent.putExtra(EventUtils.EVENT_INSTANTLY_FIELD, selectedItem.isInstantlyShown());

        //The previous alarms will be stored within the intent only if the event has previous alarms:
        if (eventPreviousAlarms != null) returnIntent.putExtra(EventUtils.EVENT_PREV_ALARMS_FIELD, eventPreviousAlarms);

        //The interval and repetition stop values are sent only when there is a repetition interval:
        if (eventInterval != 0) {
            returnIntent.putExtra(EventUtils.EVENT_INTERVAL_FIELD, eventInterval);
            if (eventRepetitionType != null) returnIntent.putExtra(EventUtils.EVENT_REP_TYPE_FIELD, eventRepetitionType);
            returnIntent.putExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, eventRepetitionStop);
        }

        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }

    //Behaviour of event type items (elements inside the new event dialog) when they are clicked:
    private void setButtonsBehaviour () {
        listener = new TypeListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                //First we need to store the type of the selected event:
                selectedItem = typeAdapter.getEventTypeItem(position);
                eventType = selectedItem.getType();

                //If the selected item is of type MEASURE, the type list elements visibility must change:
                if (eventType.equals(TypeListItem.eventTypes.MEASURE)) {
                    showingSecondaryDialog = true;
                    //The dialog title and elements are changed:
                    dialogTitle.setText(R.string.new_measure_dialog_title);
                    typeAdapter.changeElementsVisibility(measureTypes);
                }

                //If the selected item is of type SURVEY, the type list elements visibility must change:
                else if (eventType.equals(TypeListItem.eventTypes.SURVEY)) {
                    showingSecondaryDialog = true;
                    //The dialog title and elements are changed:
                    dialogTitle.setText(R.string.new_survey_dialog_title);
                    typeAdapter.changeElementsVisibility(surveyTypes);
                }

                //If the selected item is of type PERSONAL, the type list elements visibility must change:
                else if (eventType.equals(TypeListItem.eventTypes.PERSONAL_EVENT)) {
                    showingSecondaryDialog = true;
                    //The dialog title and elements are changed:
                    dialogTitle.setText(R.string.new_personal_dialog_title);
                    typeAdapter.changeElementsVisibility(personalTypes);
                }

                //If it is a different event, an event setter dialog is created so the user can select the alarm time and the description text:
                else {
                    if (eventType.equals(TypeListItem.eventTypes.MOOD)
                            ||eventType.equals(TypeListItem.eventTypes.MEASURE_BG)
                            ||eventType.equals(TypeListItem.eventTypes.MEASURE_BP)
                            ||eventType.equals(TypeListItem.eventTypes.MEASURE_HR)
                            ||eventType.equals(TypeListItem.eventTypes.MEASURE_XX)
                            ||eventType.equals(TypeListItem.eventTypes.STIMULUS))
                        hasDefaultDescription = true;
                    startNewEventSetter();
                }
            }
        };
    }

    //This method starts a new event setter dialog, waiting for the result:
    private void startNewEventSetter () {

        //Before launching the event setter dialog, the new event dialog must be hidden so it can't be seen behind it:
        dialogView.setVisibility(View.INVISIBLE);

        //Whether or not the selected event is an alarm:
        boolean isAlarm = !(eventType.equals(TypeListItem.eventTypes.DOCTOR) || eventType.equals(TypeListItem.eventTypes.PERSONAL));

        Intent intent = new Intent(this, DialogEventParameters.class);

        Intent parentActivityIntent = getIntent();

        intent.putExtra(EventUtils.EVENT_MONTH_FIELD, parentActivityIntent.getIntExtra(EventUtils.EVENT_MONTH_FIELD, DateUtils.currentMonth));
        intent.putExtra(EventUtils.EVENT_YEAR_FIELD, parentActivityIntent.getIntExtra(EventUtils.EVENT_YEAR_FIELD, DateUtils.currentYear));

        intent.putExtra("hasDefaultDescription", hasDefaultDescription);
        intent.putExtra("defaultDescription", selectedItem.getTypeDescription());
        intent.putExtra("editingEvent", false);
        intent.putExtra("isAlarm", isAlarm);
        intent.putExtra("type", eventType.toString());

        startActivityForResult(intent, 1);
    }

    //Method for initializing the event type list inside the new event dialog:
    private void initEventsDialog() {

        //Recycler view:
        RecyclerView typeList = findViewById(R.id.event_type_list);

        typeList.setLayoutManager(new LinearLayoutManager(this));

        typeAdapter = new TypeListAdapter(this, listener, eventTypes);

        typeList.setFocusable(false);
        typeList.setAdapter(typeAdapter);
    }

    //Method for initializing the event type array:
    private void initEventTypes () {

        this.eventTypes = new ArrayList<>();
        this.measureTypes = new ArrayList<>();
        this.surveyTypes = new ArrayList<>();
        this.personalTypes = new ArrayList<>();

        Resources res = getResources();

        TypeListItem.eventTypes[] eventTypes = TypeListItem.eventTypes.values();

        TypeListItem item;
        String title;
        String description;
        Drawable icon;

        for (TypeListItem.eventTypes type: eventTypes) {
            switch (type) {
                case ACTIVITY:
                    title = res.getString(R.string.activity_event_title);
                    description = res.getString(R.string.activity_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "activity_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.eventTypes.add(item);
                    break;
                case MEASURE:
                    if (UserDataReceiverService.hasAnyMeasure()) {
                        title = res.getString(R.string.measure_event_title);
                        icon = ImageUtils.getImageFromAssets(this, "dmeasure_icon.png");
                        item = new TypeListItem(type, title, null, icon);
                        this.eventTypes.add(item);
                    }
                    break;
                case MEASURE_HR:
                    if (UserDataReceiverService.hasMeasure("MEASURE_HR")) {
                        title = res.getString(R.string.measure_event_hr_title);
                        description = res.getString(R.string.measure_reminder_hr_description);
                        icon = ImageUtils.getImageFromAssets(this, "measure_hr_icon.png");
                        item = new TypeListItem(type, title, description, icon);
                        this.measureTypes.add(item);
                    }
                    break;
                case MEASURE_BP:
                    if (UserDataReceiverService.hasMeasure("MEASURE_BP")) {
                        title = res.getString(R.string.measure_event_bp_title);
                        description = res.getString(R.string.measure_reminder_bp_description);
                        icon = ImageUtils.getImageFromAssets(this, "measure_bp_icon.png");
                        item = new TypeListItem(type, title, description, icon);
                        this.measureTypes.add(item);
                    }
                    break;
                case MEASURE_BG:
                    if (UserDataReceiverService.hasMeasure("MEASURE_BG")) {
                        title = res.getString(R.string.measure_event_bg_title);
                        description = res.getString(R.string.measure_reminder_bg_description);
                        icon = ImageUtils.getImageFromAssets(this, "measure_bg_icon.png");
                        item = new TypeListItem(type, title, description, icon);
                        this.measureTypes.add(item);
                    }
                    break;
                case MEASURE_XX:
                    if (UserDataReceiverService.hasMeasure("MEASURE_XX")) {
                        title = res.getString(R.string.measure_event_xx_title);
                        description = res.getString(R.string.measure_reminder_xx_description) + UserDataReceiverService.getFreeMeasure();
                        icon = ImageUtils.getImageFromAssets(this, "measure_xx_icon.png");
                        item = new TypeListItem(type, title, description, icon);
                        this.measureTypes.add(item);
                    }
                    break;
                case DOCTOR:
                    title = res.getString(R.string.doctor_event_title);
                    description = res.getString(R.string.doctor_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "doctor_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.eventTypes.add(item);
                    break;
                case MEDICATION:
                    title = res.getString(R.string.medication_event_title);
                    description = res.getString(R.string.medication_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "medication_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.eventTypes.add(item);
                    break;
                case PERSONAL_EVENT:
                    title = res.getString(R.string.personal_event_title);
                    icon = ImageUtils.getImageFromAssets(this, "dpersonal_icon.png");
                    item = new TypeListItem(type, title, null, icon);
                    this.eventTypes.add(item);
                    break;
                case PERSONAL:
                    title = res.getString(R.string.personal_appointment_title);
                    description = res.getString(R.string.personal_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "personal_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.personalTypes.add(item);
                    break;
                case TEXTNOFEEDBACK:
                    title = res.getString(R.string.textnofeedback_event_title);
                    description = res.getString(R.string.textnofeedback_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "textnofeedback_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.personalTypes.add(item);
                    break;
                case STIMULUS:
                    title = res.getString(R.string.stimulus_event_title);
                    description = res.getString(R.string.stimulus_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "stimulus_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.eventTypes.add(item);
                    break;
                case SURVEY:
                    title = res.getString(R.string.survey_event_title);
                    icon = ImageUtils.getImageFromAssets(this, "survey_icon.png");
                    item = new TypeListItem(type, title, null, icon);
                    this.eventTypes.add(item);
                    break;
                case MOOD:
                    title = res.getString(R.string.mood_event_title);
                    description  = res.getString(R.string.mood_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "mood_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.surveyTypes.add(item);
                    break;
                case TEXTFEEDBACK:
                    title = res.getString(R.string.textfeedback_event_title);
                    description = res.getString(R.string.textfeedback_reminder_description);
                    icon = ImageUtils.getImageFromAssets(this, "textfeedback_icon.png");
                    item = new TypeListItem(type, title, description, icon);
                    this.surveyTypes.add(item);
                    break;
            }
        }
    }
}
