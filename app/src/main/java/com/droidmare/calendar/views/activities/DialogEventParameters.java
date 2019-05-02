package com.droidmare.calendar.views.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.TypeListItem;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventListUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.utils.SortUtils;
import com.droidmare.calendar.utils.ToastUtils;
import com.droidmare.calendar.views.adapters.dialogs.RepTypeListsAdapter;
import com.droidmare.calendar.views.adapters.events.EventCalendarAdapter;
import com.droidmare.calendar.views.adapters.events.EventDayAdapter;
import com.droidmare.calendar.views.adapters.dialogs.PrevAlarmsListsAdapter;
import com.droidmare.calendar.views.adapters.dialogs.SelectionListAdapter;
import com.droidmare.statistics.StatisticAPI;
import com.droidmare.statistics.StatisticService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

import static android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN;

//Activity for setting or editing the event parameters declaration
//@author Eduardo on 26/09/2018.

@SuppressLint("SetTextI18n")
public class DialogEventParameters extends AppCompatActivity{

    private static final String TAG = DialogEventParameters.class.getCanonicalName();

    //Event type strings:
    private final String MEDICATION = TypeListItem.eventTypes.MEDICATION.toString();

    //Activities request codes:
    private final int REQUEST_PREV_ALARMS_DATE_PICKER = 0;
    private final int REQUEST_CUSTOM_STOP_DATE_PICKER = 1;
    private final int REQUEST_DELETE_DIALOG = 2;
    private final int REQUEST_DESCRIPTION_DIALOG = 3;

    //Containers indexes:
    private final int EVENT_DESCRIPTION = 0;
    private final int EVENT_DATE = 1;
    private final int EVENT_TIME = 2;
    private final int PREV_ALARMS = 3;
    private final int REPETITION_CHECK = 4;
    private final int REPETITION_INTERVAL = 5;
    private final int REPETITION_TYPE = 6;
    private final int STOP_CHECK = 7;
    private final int STOP_DATE = 8;

    //Previous alarm layout units indexes:
    private final int MINUTE_UNIT = 0;
    private final int HOUR_UNIT = 1;
    private final int DAY_UNIT = 2;
    private final int WEEK_UNIT = 3;
    private final int MONTH_UNIT = 4;

    //Dialog selection values:
    private int eventYear;
    private int eventMonth;
    private int eventDay;
    private int eventHour;
    private int eventMinute;

    private int originalYear;
    private int originalMonth;
    private int originalDay;
    private int originalHour;
    private int originalMinute;

    private int repetitionInterval;
    private int repetitionNumber;

    private int alternateRepetitionValue;
    private int alternateRepetitionHour;

    private int repStopDaysDuration;

    private final int repetitionStopHour = 23;
    private final int repetitionStopMinute = 59;

    private long selectedEventDate;
    private long selectedStopDate;

    private String selectedDescription;

    private JSONObject repetitionType;
    private JSONArray previousAlarms;

    private boolean hasRepetition;

    private int visibleContainerIndex;

    //Dialog containers:
    private LinearLayout[] containersList;
    private String[] containersTitles;

    private TextView[] previousAlarmsLayouts;

    private LinearLayout[] repetitionTypeButtons;
    private RelativeLayout[] repetitionTypeLayouts;

    //Dialog components:
    private RelativeLayout prevStep;
    private RelativeLayout currentPar;
    private RelativeLayout nextStep;

    private LinearLayout acceptButton;
    private LinearLayout cancelButton;

    private LinearLayout dismissValueSelectionButton;
    private LinearLayout dismissUnitSelectionButton;

    private LinearLayout prevMonth;
    private LinearLayout nextMonth;
    private LinearLayout prevYear;
    private LinearLayout nextYear;

    private LinearLayout addDescriptionButton;
    private LinearLayout deleteDescriptionButton;

    private LinearLayout addAlarmButton;
    private LinearLayout customAlarmButton;

    private LinearLayout checkBoxAffirmative;
    private LinearLayout checkBoxNegative;

    private LinearLayout increaseInterval;
    private LinearLayout decreaseInterval;

    private LinearLayout increaseRepNumber;
    private LinearLayout decreaseRepNumber;

    private LinearLayout specialIntervalButton;
    private LinearLayout weeklyRepTypeLayout;

    private LinearLayout increaseAlternateInt;
    private LinearLayout decreaseAlternateInt;

    private LinearLayout increaseAlternateHour;
    private LinearLayout decreaseAlternateHour;

    private LinearLayout increaseRepStopDays;
    private LinearLayout decreaseRepStopDays;
    private LinearLayout customStopDateButton;

    private LinearLayout repTypeListLayout;

    private RelativeLayout prevAlarmsParamsLayout;
    private RelativeLayout prevAlarmsListLayout;

    private LinearLayout unitSelectionLayout;
    private LinearLayout valueSelectionLayout;

    private LinearLayout prevAlarmCustomLayout;

    private TextView currentDateView;

    private TextView prevAlarmsValueBox;
    private TextView prevAlarmsUnitBox;

    private TextView dialogTitle;
    private TextView eventMonthText;
    private TextView eventYearText;

    private TextView repetitionIntervalText;

    private TextView repetitionNumberText;

    private TextView repetitionTypeDescription;
    private TextView repetitionConfigDescription;
    private TextView alternateIntervalHint;
    private TextView alternateIntervalText;
    private TextView alternateHourText;

    private TextView repetitionStopDaysText;
    private TextView repetitionStopDescription;

    private EditText editDescription;

    private LinearLayout followAffirmative;
    private LinearLayout followNegative;

    //RecyclerViews, adapters and arrays for the selection lists:
    private ArrayList<String> hoursArray;
    private ArrayList<String> minutesArray;

    private RelativeLayout timeDivisorFocus;

    private RecyclerView eventHourList;
    private RecyclerView eventMinuteList;

    //Listener for handling on calendar grid items clicks:
    private EventCalendarAdapter.ItemClickListener calendarGridListener;

    //Adapter for the repetition stop date calendar grid:
    private EventCalendarAdapter calendarAdapter;

    //Arrays, recycler views and adapters for the previous alarms lists:
    private ArrayList<String> prevAlarmsValues;
    private ArrayList<String> prevAlarmsUnits;
    private ArrayList<Integer> prevAlarmsValuesPerUnit;

    private RecyclerView prevAlarmsValuesList;
    private RecyclerView prevAlarmsUnitsList;

    private PrevAlarmsListsAdapter prevAlarmsValueListAdapter;
    private PrevAlarmsListsAdapter prevAlarmsUnitListAdapter;

    //Listeners for handling on prev alarms params lists elements clicks:
    private PrevAlarmsListsAdapter.ItemClickListener prevAlarmsValueListListener;
    private PrevAlarmsListsAdapter.ItemClickListener prevAlarmsUnitsListListener;

    //Recycler views and adapters for the repetition type lists:
    private RecyclerView weeklyRepetitionList;
    private RecyclerView monthlyRepetitionGrid;

    private RepTypeListsAdapter weeklyRepetitionListAdapter;
    private RepTypeListsAdapter monthlyRepetitionGridAdapter;

    //Listener for handling on repetition type lists elements clicks:
    private RepTypeListsAdapter.ItemClickListener repetitionTypeListListener;

    //Control variables:
    private boolean irPressed;

    boolean onlyDisplayParameters;

    private boolean editingEvent;

    private boolean isAlarm;

    private boolean displayingSpecialIntervals;

    //Variable that indicates that a layout sub level is being displayed (when adding a previous alarm, for example):
    private boolean inLayoutSubLevel;

    private boolean selectingPrevAlarm;

    private int prevAlarmToDelete;

    private int selectedValue;

    private int selectedUnit;

    private int selectedRepType;

    private int currentDefaultRepConfig;

    private String eventType;

    private ArrayList<Integer> selectedRepTypeConfig;

    private StatisticService statistic;

    //A reference to the running activity to close it if this activity is closed:
    private static WeakReference<AppCompatActivity> launchedActivityReference;
    public static void setLaunchedActivityReference(AppCompatActivity activity) {
        launchedActivityReference = new WeakReference<>(activity);
    }

    public static Drawable icon;
    public static String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        onlyDisplayParameters = getIntent().getBooleanExtra("displayParameters", false);
        editingEvent = getIntent().getBooleanExtra("editingEvent", false);
        isAlarm = getIntent().getBooleanExtra("isAlarm", true);
        eventType = getIntent().getStringExtra("type");

        //The main activity needs to know that this activity is running in the event that a synchronization takes place, case in which the running activity might have to be finished:
        if (editingEvent) MainActivity.setRunningActivityReference(this);
        else DialogNewEventActivity.setLaunchedActivityReference(this);

        inLayoutSubLevel = selectingPrevAlarm = irPressed = false;

        selectedUnit = MINUTE_UNIT;

        selectedRepType = EventUtils.ALTERNATE_REPETITION;

        selectedRepTypeConfig = new ArrayList<>();

        setContentView(R.layout.activity_dialog_event_parameters);

        findViewById(R.id.hours_list_up).setVisibility(View.GONE);
        if (isAlarm) findViewById(R.id.parameters_prev_alarms_container).setVisibility(View.GONE);

        //Initialization and declaration of all elements:
        initializeAttributeValues();

        initializeLayoutContainers();

        initializeLayoutsViews();

        if (onlyDisplayParameters) {

            displayCurrentParameters(false);

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });
        }

        else {
            initializeEventSelectionLists();

            initializeButtonsBehaviour();

            if (!isAlarm) initializePrevAlarmsListsAndArrays();

            initializeRepTypeListsAndArrays();

            //Initialization of the day grid (Columns names for the calendar grid):
            RecyclerView dayGrid = findViewById(R.id.event_calendar_day_grid);
            dayGrid.setLayoutManager(new GridLayoutManager(this, 7));
            dayGrid.setHasFixedSize(true);

            EventDayAdapter dayAdapter = new EventDayAdapter(this);
            dayGrid.setFocusable(false);
            dayGrid.setAdapter(dayAdapter);

            //Initialization of the calendar grid (for the current selected repetition stop month):
            RecyclerView calendarGrid = findViewById(R.id.event_calendar_grid);
            calendarGrid.setLayoutManager(new GridLayoutManager(this, 7));
            calendarGrid.setHasFixedSize(true);

            calendarAdapter = new EventCalendarAdapter(this, eventDay, eventMonth, eventYear, calendarGridListener);
            calendarGrid.setAdapter(calendarAdapter);
        }
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

        if (!onlyDisplayParameters) {
            View focusedView = getCurrentFocus();

            boolean currentParamsVisible = findViewById(R.id.current_parameters_dialog).getVisibility() == View.VISIBLE;

            //If the current parameters is being displayed when the back key is pressed, it is dismissed returning to the set/edit event dialog:
            if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_BACK)) {
                if (currentParamsVisible) {
                    hideCurrentParameters();
                    return true;
                } else if (inLayoutSubLevel) {
                    if (visibleContainerIndex == PREV_ALARMS) {
                        if (selectingPrevAlarm) {
                            if (unitSelectionLayout.getVisibility() == View.VISIBLE)
                                dismissUnitSelectionButton.performClick();
                            if (valueSelectionLayout.getVisibility() == View.VISIBLE)
                                dismissValueSelectionButton.performClick();
                        } else revertPrevAlarmsLayout();

                        return true;
                    } else if (visibleContainerIndex == REPETITION_TYPE) {
                        revertRepetitionTypeLayout();
                        return true;
                    }
                } else {
                    irPressed = true;
                    prevStep.performClick();
                    return true;
                }
            }

            //Controller's ir color keys behaviour when they are pressed (if the current parameters dialog is being displayed, the ir keys behaviour is disabled):
            if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_F4 || event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) && !currentParamsVisible && !inLayoutSubLevel) {
                currentPar.requestFocus();
                currentPar.performClick();
            } else if (event.getAction() == KeyEvent.ACTION_DOWN && (event.getKeyCode() == KeyEvent.KEYCODE_F5 || event.getKeyCode() == KeyEvent.KEYCODE_PROG_BLUE) && !currentParamsVisible && !selectingPrevAlarm) {
                irPressed = true;
                nextStep.performClick();
            }

            //When the focus is over a value increase or decrease button and the center d-pad button is pressed, the values will keep changing until the button is released:
            else if (focusedView != null && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN
                    && (focusedView.getId() == R.id.event_prev_month
                    || focusedView.getId() == R.id.event_next_month
                    || focusedView.getId() == R.id.event_prev_year
                    || focusedView.getId() == R.id.event_next_year
                    || focusedView.getId() == R.id.repetition_interval_increase
                    || focusedView.getId() == R.id.repetition_interval_decrease
                    || focusedView.getId() == R.id.repetition_number_increase
                    || focusedView.getId() == R.id.repetition_number_decrease
                    || focusedView.getId() == R.id.alternate_interval_increase
                    || focusedView.getId() == R.id.alternate_interval_decrease
                    || focusedView.getId() == R.id.med_alternate_hours_increase
                    || focusedView.getId() == R.id.med_alternate_hours_decrease
                    || focusedView.getId() == R.id.med_alternate_interval_increase
                    || focusedView.getId() == R.id.med_alternate_interval_decrease
                    || focusedView.getId() == R.id.rep_days_duration_increase
                    || focusedView.getId() == R.id.rep_days_duration_decrease)) {
                focusedView.performClick();
                return true;
            }

            //Behaviour of the focus when the up key is pressed when the prev step button is focused:
            else if (focusedView != null && focusedView.getId() == prevStep.getId() && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && visibleContainerIndex == EVENT_TIME) {
                    int firstVisibleItemIndex = ((LinearLayoutManager) eventHourList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    eventHourList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1).itemView.requestFocus();
                    return true;
                }
            }

            //Behaviour of the focus when the up key is pressed when the current parameters button is focused:
            else if (focusedView != null && focusedView.getId() == currentPar.getId() && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && visibleContainerIndex == EVENT_TIME) {
                    int firstVisibleItemIndex = ((LinearLayoutManager) eventHourList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    eventHourList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1).itemView.requestFocus();
                    return true;
                }
            }

            //Behaviour of the focus when the up key is pressed when the next step button is focused:
            else if (focusedView != null && focusedView.getId() == nextStep.getId() && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && visibleContainerIndex == EVENT_TIME) {
                    int firstVisibleItemIndex = ((LinearLayoutManager) eventMinuteList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    eventMinuteList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1).itemView.requestFocus();
                    return true;
                }
            }

            //Behaviour of the focus when the down key is pressed when the description text box is focused:
            else if (focusedView != null && focusedView.getId() == editDescription.getId() && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (addDescriptionButton.getVisibility() == View.VISIBLE)
                        addDescriptionButton.requestFocus();
                    else deleteDescriptionButton.requestFocus();
                    return true;
                }
            } else if (focusedView != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                //If a view is focused, the parent is got so it can be checked if it is one of the list recycler views:
                View focusedItemParent = (View) focusedView.getParent();

                if (isSelectionList(focusedItemParent.getId())) {
                    //If, indeed, the parent is one of the selection list recycler views, the adapter and the linear manager are got:
                    RecyclerView.Adapter adapter = ((RecyclerView) focusedItemParent).getAdapter();
                    LinearLayoutManager layoutManager = (LinearLayoutManager) ((RecyclerView) focusedItemParent).getLayoutManager();
                    //Depending on the pressed key the list scrolls upwards or downwards:
                    if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                        //When the d-pad up key is pressed, the list must scroll downwards, keeping the focus on the central element:
                        int firstVisibleItemIndex = layoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastVisibleItemIndex = layoutManager.findLastCompletelyVisibleItemPosition();

                        int focusOffset = ((lastVisibleItemIndex - firstVisibleItemIndex) / 2) - 1;

                        if (firstVisibleItemIndex > 0) {

                            //The text color and style must be changed, since the middle elements' color is black and their style is bold, and the rest of elements' are grey by default:
                            SelectionListAdapter.ViewHolder holder = ((SelectionListAdapter.ViewHolder) ((RecyclerView) focusedItemParent).findViewHolderForAdapterPosition(firstVisibleItemIndex + 1));
                            holder.notCenteredHolder();

                            ((RecyclerView) focusedItemParent).scrollToPosition(firstVisibleItemIndex - 1);
                            holder = ((SelectionListAdapter.ViewHolder) ((RecyclerView) focusedItemParent).findViewHolderForAdapterPosition(firstVisibleItemIndex + focusOffset));

                            holder.itemView.requestFocus();
                            holder.centeredHolder();

                            //Now the corresponding variable values must be updated based on the new centered element value:
                            updateListParameter(focusedItemParent.getId(), holder);
                        }

                        return true;
                    } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                        //When the d-pad down key is pressed, the list must scroll upwards, keeping the focus on the central element:
                        int totalItemCount = adapter.getItemCount();
                        int firstVisibleItemIndex = layoutManager.findFirstCompletelyVisibleItemPosition();
                        int lastVisibleItemIndex = layoutManager.findLastCompletelyVisibleItemPosition();

                        int focusOffset = ((lastVisibleItemIndex - firstVisibleItemIndex) / 2) - 1;

                        if (lastVisibleItemIndex < totalItemCount - 1) {

                            //The text color and style must be changed, since the middle elements' color is black and their style is bold and the rest of elements' are grey by default:
                            SelectionListAdapter.ViewHolder holder = ((SelectionListAdapter.ViewHolder) ((RecyclerView) focusedItemParent).findViewHolderForAdapterPosition(firstVisibleItemIndex + 1));
                            holder.notCenteredHolder();

                            ((RecyclerView) focusedItemParent).scrollToPosition(lastVisibleItemIndex + 1);
                            holder = ((SelectionListAdapter.ViewHolder) ((RecyclerView) focusedItemParent).findViewHolderForAdapterPosition(lastVisibleItemIndex - focusOffset));

                            holder.itemView.requestFocus();
                            holder.centeredHolder();

                            //Now the corresponding variable values must be updated based on the new centered element value:
                            updateListParameter(focusedItemParent.getId(), holder);
                        }

                        return true;
                    }
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    //Method that returns whether or not the parameter focusedViewId is inside one of the selection lists:
    private boolean isSelectionList(int focusedViewId) {
        return focusedViewId == eventHourList.getId() || focusedViewId == eventMinuteList.getId();
    }

    //Method that updates the necessary variables and the stop day selection list elements when needed:
    private void updateListParameter(int listId, SelectionListAdapter.ViewHolder holder) {

        if (listId == eventHourList.getId())
            eventHour = holder.getItemValue();

        else if (listId == eventMinuteList.getId())
            eventMinute = holder.getItemValue();
    }

    //Method that assigns values to the variables based on the contents within the received intent:
    private void initializeAttributeValues() {
        Intent receivedIntent = getIntent();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());

        originalYear = eventYear = receivedIntent.getIntExtra(EventUtils.EVENT_YEAR_FIELD, DateUtils.currentYear);
        originalMonth = eventMonth = receivedIntent.getIntExtra(EventUtils.EVENT_MONTH_FIELD, DateUtils.currentMonth);
        originalDay = eventDay = receivedIntent.getIntExtra(EventUtils.EVENT_DAY_FIELD, DateUtils.currentDay);

        originalHour = eventHour = receivedIntent.getIntExtra(EventUtils.EVENT_HOUR_FIELD, calendar.get(Calendar.HOUR_OF_DAY));
        originalMinute = eventMinute = receivedIntent.getIntExtra(EventUtils.EVENT_MINUTE_FIELD, calendar.get(Calendar.MINUTE));

        previousAlarms = new JSONArray();

        if (receivedIntent.hasExtra(EventUtils.EVENT_PREV_ALARMS_FIELD)) {
            try {
                previousAlarms = new JSONArray(receivedIntent.getStringExtra(EventUtils.EVENT_PREV_ALARMS_FIELD));
            } catch (JSONException jse) {
                Log.e(TAG, "initializeAttributeValues. JSONException: " + jse.getMessage());
            }
        }

        try {
            //If the received intent has no data for the repetition type, the default type is assigned:
            if (receivedIntent.hasExtra(EventUtils.EVENT_REP_TYPE_FIELD) && !receivedIntent.getStringExtra(EventUtils.EVENT_REP_TYPE_FIELD).equals(""))
                repetitionType = new JSONObject(receivedIntent.getStringExtra(EventUtils.EVENT_REP_TYPE_FIELD));

            else repetitionType = new JSONObject(EventUtils.DEFAULT_REPETITION_TYPE);

        } catch (JSONException jse) {
            Log.e(TAG, "initializeAttributeValues. JSONException: " + jse.getMessage());
        }

        repetitionInterval = receivedIntent.getIntExtra(EventUtils.EVENT_INTERVAL_FIELD, 0);

        alternateRepetitionValue = 1;

        selectedStopDate = receivedIntent.getLongExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, -1);

        if (selectedStopDate != -1) repStopDaysDuration = 0;

        selectedDescription = receivedIntent.getStringExtra(EventUtils.EVENT_DESCRIPTION_FIELD);

        hasRepetition = false;
    }

    //Method that assigns all the layout container to their respective variables and that initializes the first visible container and the containers' list:
    private void initializeLayoutContainers() {

        //Date view on the event time selection layout:
        currentDateView = findViewById(R.id.current_date_view);

        if (!isAlarm) {
            //Previous alarms views container initialization:
            previousAlarmsLayouts = new TextView[]{
                    findViewById(R.id.prev_alarm_1),
                    findViewById(R.id.prev_alarm_2),
                    findViewById(R.id.prev_alarm_3),
                    findViewById(R.id.prev_alarm_4)
            };

            setPreviousAlarmsText();
        }

        //Repetition type buttons and layouts containers initialization:
        repTypeListLayout = findViewById(R.id.rep_type_list_layout);

        if (eventType.equals(MEDICATION)) {
            specialIntervalButton = findViewById(R.id.med_special_interval_buttons);
            weeklyRepTypeLayout = findViewById(R.id.med_weekly_rep_type_layout);
        }

        else {
            repetitionTypeButtons = new LinearLayout[] {
                    findViewById(R.id.daily_rep_type_button),
                    findViewById(R.id.alternate_rep_type_button),
                    findViewById(R.id.weekly_rep_type_button),
                    findViewById(R.id.monthly_rep_type_button),
                    findViewById(R.id.annual_rep_type_button)
            };

            repetitionTypeLayouts = new RelativeLayout[] {
                    findViewById(R.id.alternate_rep_type_layout),
                    findViewById(R.id.weekly_rep_type_layout),
                    findViewById(R.id.monthly_rep_type_layout)
            };
        }

        //Dialog containers:
        LinearLayout eventDescriptionSelection = findViewById(R.id.event_description_selection);
        LinearLayout eventDateSelection = findViewById(R.id.event_date_selection);
        LinearLayout eventTimeSelection = findViewById(R.id.event_time_selection);

        LinearLayout eventPreviousAlarms = findViewById(R.id.previous_alarms_selection);

        LinearLayout eventRepeatSelection = findViewById(R.id.event_repeat_selection);

        LinearLayout repetitionIntervalSelection = findViewById(R.id.repetition_interval_selection);

        LinearLayout repetitionTypeSelection;

        if (eventType.equals(MEDICATION))
            repetitionTypeSelection = findViewById(R.id.repetition_type_selection_medication);

        else repetitionTypeSelection = findViewById(R.id.repetition_type_selection_default);

        LinearLayout repetitionDateSelection = findViewById(R.id.repetition_date_selection);

        //All the containers are stored in an array so the previous and next buttons functionality can be easily implemented:
        containersList = new LinearLayout[]{
                eventDescriptionSelection,
                eventDateSelection,
                eventTimeSelection,
                eventPreviousAlarms,
                eventRepeatSelection,
                repetitionIntervalSelection,
                repetitionTypeSelection,
                eventRepeatSelection,
                repetitionDateSelection,
        };

        visibleContainerIndex = 0;

        containersTitles = new String[containersList.length];

        //The title of each container as well as the first visible index is going to be determined by value of editingEvent:
        if (editingEvent) {
            containersTitles[EVENT_DESCRIPTION] = getString(R.string.mod_parameters_dialog_title_0);
            containersTitles[EVENT_DATE] = getString(R.string.mod_parameters_dialog_title_1);
            containersTitles[EVENT_TIME] = getString(R.string.mod_parameters_dialog_title_2);
            containersTitles[PREV_ALARMS] = getString(R.string.mod_parameters_dialog_title_3);
            containersTitles[REPETITION_CHECK] = getString(R.string.mod_parameters_dialog_title_4);
            containersTitles[REPETITION_TYPE] = getString(R.string.mod_parameters_dialog_title_6);
        } else {
            containersTitles[EVENT_DESCRIPTION] = getString(R.string.set_parameters_dialog_title_0);
            containersTitles[EVENT_DATE] = getString(R.string.set_parameters_dialog_title_1);
            containersTitles[EVENT_TIME] = getString(R.string.set_parameters_dialog_title_2);
            containersTitles[PREV_ALARMS] = getString(R.string.set_parameters_dialog_title_3);
            containersTitles[REPETITION_CHECK] = getString(R.string.set_parameters_dialog_title_4);
            containersTitles[REPETITION_TYPE] = getString(R.string.set_parameters_dialog_title_6);
        }

        if (eventType.equals(MEDICATION)) containersTitles[REPETITION_TYPE] = getString(R.string.med_parameters_dialog_title_6);

        //The title of the following containers is always the same:
        containersTitles[REPETITION_INTERVAL] = getString(R.string.parameters_dialog_title_5);
        containersTitles[STOP_CHECK] = getString(R.string.parameters_dialog_title_7);
        containersTitles[STOP_DATE] = getString(R.string.parameters_dialog_title_8);

        //Now the visibility of each container is set based on the index that must be visible at first:
        for (int i = 0; i < containersList.length; i++) {
            if (i == visibleContainerIndex) containersList[i].setVisibility(View.VISIBLE);
            else containersList[i].setVisibility(View.GONE);
        }

        containersList[0].requestFocus();
    }

    //Method for assigning each view to its corresponding attribute:
    private void initializeLayoutsViews() {

        //Buttons initialization:
        prevStep = findViewById(R.id.prev_step_button);
        currentPar = findViewById(R.id.display_current_parameters_button);
        nextStep = findViewById(R.id.next_step_button);

        acceptButton = findViewById(R.id.parameters_affirmative_button);
        cancelButton = findViewById(R.id.parameters_negative_button);

        dismissUnitSelectionButton = findViewById(R.id.dismiss_unit_selection_button);
        dismissValueSelectionButton = findViewById(R.id.dismiss_value_selection_button);

        prevMonth = findViewById(R.id.event_prev_month);
        nextMonth = findViewById(R.id.event_next_month);
        prevYear = findViewById(R.id.event_prev_year);
        nextYear = findViewById(R.id.event_next_year);

        addDescriptionButton = findViewById(R.id.launch_default_descriptions_button);
        deleteDescriptionButton = findViewById(R.id.delete_whole_description_button);

        if (eventType != null && (eventType.equals(MEDICATION))) {
            ((TextView)findViewById(R.id.default_descriptions_button_text)).setText(getString(R.string.description_add_medicine_button));
            addDescriptionButton.setVisibility(View.VISIBLE);
        }

        addAlarmButton = findViewById(R.id.add_alarm_button);
        customAlarmButton = findViewById(R.id.custom_alarm_button);

        increaseInterval = findViewById(R.id.repetition_interval_increase);
        decreaseInterval = findViewById(R.id.repetition_interval_decrease);

        increaseRepNumber = findViewById(R.id.repetition_number_increase);
        decreaseRepNumber = findViewById(R.id.repetition_number_decrease);

        increaseRepStopDays = findViewById(R.id.rep_days_duration_increase);
        decreaseRepStopDays = findViewById(R.id.rep_days_duration_decrease);
        customStopDateButton = findViewById(R.id.custom_stop_date_button);

        followAffirmative = findViewById(R.id.affirmative_button);
        followNegative = findViewById(R.id.negative_button);

        //Prev params view elements:
        prevAlarmsParamsLayout = findViewById(R.id.prev_alarm_params_layout);
        prevAlarmsListLayout = findViewById(R.id.prev_alarms_list_layout);

        prevAlarmCustomLayout = findViewById(R.id.prev_alarm_custom_layout);

        unitSelectionLayout = findViewById(R.id.unit_selection_layout);
        valueSelectionLayout = findViewById(R.id.value_selection_layout);

        //Check boxes initialization:
        checkBoxAffirmative = findViewById(R.id.interval_affirmative_check);
        checkBoxNegative = findViewById(R.id.interval_negative_check);

        //Text views initialization
        dialogTitle = findViewById(R.id.event_parameters_title);
        dialogTitle.setText(containersTitles[visibleContainerIndex]);

        eventMonthText = findViewById(R.id.event_month_text);
        eventMonthText.setText(DateUtils.getMonth(eventMonth));

        eventYearText = findViewById(R.id.event_year_text);
        eventYearText.setText(((Integer)eventYear).toString());

        prevAlarmsValueBox = findViewById(R.id.prev_alarm_value_box);
        prevAlarmsUnitBox = findViewById(R.id.prev_alarm_unit_box);

        repetitionIntervalText = findViewById(R.id.repetition_interval_text);

        repetitionNumberText = findViewById(R.id.repetition_number_text);

        repetitionStopDaysText = findViewById(R.id.rep_days_duration_text);
        repetitionStopDescription = findViewById(R.id.repetition_stop_description);

        if (eventType.equals(MEDICATION)) {
            alternateIntervalText = findViewById(R.id.med_alternate_interval_text);
            increaseAlternateInt = findViewById(R.id.med_alternate_interval_increase);
            decreaseAlternateInt = findViewById(R.id.med_alternate_interval_decrease);

            alternateHourText = findViewById(R.id.med_alternate_hours_text);
            increaseAlternateHour = findViewById(R.id.med_alternate_hours_increase);
            decreaseAlternateHour = findViewById(R.id.med_alternate_hours_decrease);
            calculateAlternateRepHour();
        }

        else {
            alternateIntervalHint = findViewById(R.id.alternate_layout_hint);
            alternateIntervalText = findViewById(R.id.alternate_interval_text);
            increaseAlternateInt = findViewById(R.id.alternate_interval_increase);
            decreaseAlternateInt = findViewById(R.id.alternate_interval_decrease);
        }

        setViewText(repetitionIntervalText, repetitionInterval);
        setViewText(alternateIntervalText, alternateRepetitionValue);

        repetitionTypeDescription = findViewById(R.id.rep_type_description);
        repetitionConfigDescription = findViewById(R.id.rep_config_description);

        //Edition text box initialization:
        editDescription = findViewById(R.id.event_description_text_box);

        if (getIntent().hasExtra("defaultDescription")) {
            String exampleDescription = getIntent().getStringExtra("defaultDescription");
            selectedDescription = "";

            //If the event is of type measure or is a mood survey, the edit description box will show an editable text, otherwise it will show an example hint:
            if (getIntent().getBooleanExtra("hasDefaultDescription", false))
                editDescription.setText(exampleDescription);

            else editDescription.setHint(getResources().getString(R.string.description_edit_hint) + exampleDescription);
        }

        else editDescription.setText(selectedDescription);

        //The cursor is relocated at the end of the current text:
        editDescription.setSelection(editDescription.getText().length());
    }

    //Method that initializes the recycler views for the selection of the event's hour and minute:
    private void initializeEventSelectionLists() {
        //Initialization of the array lists for each adapter:
        initializeEventSelectionArrayLists();

        //Initialization of time divisor and focus layout:
        timeDivisorFocus = findViewById(R.id.time_selection_div_focus);

        if (visibleContainerIndex == EVENT_TIME)
            timeDivisorFocus.setVisibility(View.VISIBLE);

        //Initialization of the hours list:
        eventHourList = findViewById(R.id.event_hour_list);
        eventHourList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //In order to avoid malfunctions the elements inside the recycler view are not recycled:
        eventHourList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        eventHourList.setHasFixedSize(true);

        SelectionListAdapter eventHourListAdapter = new SelectionListAdapter(this, SelectionListAdapter.TYPE_HOUR, hoursArray);

        eventHourList.setFocusable(false);
        eventHourList.setAdapter(eventHourListAdapter);
        //The list is scrolled so the central hour is the current one:
        eventHourList.scrollToPosition(eventHour);

        //Initialization of the minutes list:
        eventMinuteList = findViewById(R.id.event_minute_list);
        eventMinuteList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        //In order to avoid malfunctions the elements inside the recycler view are not recycled:
        eventMinuteList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        eventMinuteList.setHasFixedSize(true);

        SelectionListAdapter eventMinuteListAdapter = new SelectionListAdapter(this, SelectionListAdapter.TYPE_MINUTE, minutesArray);

        eventMinuteList.setFocusable(false);
        eventMinuteList.setAdapter(eventMinuteListAdapter);
        //The list is scrolled so the central minute is the current one:
        eventMinuteList.scrollToPosition(eventMinute);
    }

    //Method for initializing the element arrays for each event selection list:
    private void initializeEventSelectionArrayLists() {
        //Hours array list initialization:
        hoursArray = new ArrayList<>();
        hoursArray.add("");

        for (int hour = 0; hour < 24; hour++)
            hoursArray.add(DateUtils.formatTimeString(hour));

        hoursArray.add("");

        //Minutes array list initialization:
        minutesArray = new ArrayList<>();
        minutesArray.add("");

        for (int minute = 0; minute < 60; minute++)
            minutesArray.add(DateUtils.formatTimeString(minute));

        minutesArray.add("");
    }

    //Method that initializes all the previous alarms layout lists:
    private void initializePrevAlarmsListsAndArrays() {

        prevAlarmsValuesPerUnit = new ArrayList<>();

        prevAlarmsValuesPerUnit.add(MINUTE_UNIT, 59);
        prevAlarmsValuesPerUnit.add(HOUR_UNIT, 23);
        prevAlarmsValuesPerUnit.add(DAY_UNIT, 30);
        prevAlarmsValuesPerUnit.add(WEEK_UNIT, 4);
        prevAlarmsValuesPerUnit.add(MONTH_UNIT, 12);

        prevAlarmsValueListAdapter = new PrevAlarmsListsAdapter(getApplicationContext(), prevAlarmsValueListListener, new ArrayList<String>());
        prevAlarmsUnitListAdapter = new PrevAlarmsListsAdapter(getApplicationContext(), prevAlarmsUnitsListListener, new ArrayList<String>());

        setPrevAlarmsValues();
        setPrevAlarmsUnits(false);

        prevAlarmsValuesList = findViewById(R.id.value_selection_list);
        prevAlarmsValuesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        prevAlarmsValuesList.setAdapter(prevAlarmsValueListAdapter);

        prevAlarmsUnitsList = findViewById(R.id.unit_selection_list);
        prevAlarmsUnitsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
        prevAlarmsUnitsList.setAdapter(prevAlarmsUnitListAdapter);
    }

    //Method that sets the prevAlarmsValues array:
    private void setPrevAlarmsValues() {

        prevAlarmsValues = new ArrayList<>();

        int numberOfValues = prevAlarmsValuesPerUnit.get(selectedUnit);

        for (int i = 1; i <= numberOfValues; i++)
            prevAlarmsValues.add(Integer.toString(i));

        selectedValue = 1;

        prevAlarmsValueBox.setText(Integer.toString(selectedValue));
        prevAlarmsValueListAdapter.updateData(prevAlarmsValues);
    }

    //Method that sets the prevAlarmsUnits array:
    private void setPrevAlarmsUnits(boolean valueHigherThanOne) {

        prevAlarmsUnits = new ArrayList<>();

        String minuteText; String hourText; String dayText; String weekText; String monthText;

        String unitsSuffix = getString(R.string.parameters_units_suffix);

        if (!valueHigherThanOne) {
            minuteText = getString(R.string.parameters_minute_tag);
            hourText = getString(R.string.parameters_hour_tag);
            dayText = getString(R.string.parameters_day_tag);
            weekText = getString(R.string.parameters_week_tag);
            monthText = getString(R.string.parameters_month_tag);
        }

        else {
            minuteText = getString(R.string.parameters_minutes_tag);
            hourText = getString(R.string.parameters_hours_tag);
            dayText = getString(R.string.parameters_days_tag);
            weekText = getString(R.string.parameters_weeks_tag);
            monthText = getString(R.string.parameters_months_tag);
        }

        prevAlarmsUnits.add(minuteText + unitsSuffix);
        prevAlarmsUnits.add(hourText + unitsSuffix);
        prevAlarmsUnits.add(dayText + unitsSuffix);
        prevAlarmsUnits.add(weekText + unitsSuffix);
        prevAlarmsUnits.add(monthText + unitsSuffix);

        prevAlarmsUnitBox.setText(prevAlarmsUnits.get(selectedUnit));
        prevAlarmsUnitListAdapter.updateData(prevAlarmsUnits);
    }

    //Method that initializes the repetition types layout lists:
    private void initializeRepTypeListsAndArrays() {

        ArrayList<String> weeklyRepetitionValues = new ArrayList<>();

        for (int i = 0; i < 7; i++) weeklyRepetitionValues.add(DateUtils.getDayOfWeekText(i).substring(0,3));

        if (eventType.equals(MEDICATION)) weeklyRepetitionList = findViewById(R.id.med_weekly_repetition_list);
        else {
            ArrayList<String> monthlyRepetitionValues = new ArrayList<>();

            for (int i = -1; i <= 33; i++) {
                if (i < 1 || i > 31) monthlyRepetitionValues.add(null);
                else monthlyRepetitionValues.add(Integer.toString(i));
            }

            monthlyRepetitionGrid = findViewById(R.id.monthly_repetition_grid);
            monthlyRepetitionGrid.setLayoutManager(new GridLayoutManager(getApplicationContext(), 7));
            monthlyRepetitionGrid.getRecycledViewPool().setMaxRecycledViews(0, 0);
            monthlyRepetitionGrid.setHasFixedSize(true);

            monthlyRepetitionGridAdapter = new RepTypeListsAdapter(getApplicationContext(), repetitionTypeListListener, monthlyRepetitionValues);

            monthlyRepetitionGrid.setAdapter(monthlyRepetitionGridAdapter);

            weeklyRepetitionList = findViewById(R.id.weekly_repetition_list);
        }

        weeklyRepetitionList.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false));
        weeklyRepetitionList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        weeklyRepetitionList.setHasFixedSize(true);

        weeklyRepetitionListAdapter = new RepTypeListsAdapter(getApplicationContext(), repetitionTypeListListener, weeklyRepetitionValues);

        weeklyRepetitionList.setAdapter(weeklyRepetitionListAdapter);
    }

    //Method that resets the weekly repetition list adapter based on the current day of week:
    private void resetWeeklyRepetitionList() {
        selectedRepTypeConfig = new ArrayList<>();
        currentDefaultRepConfig = DateUtils.getDayOfWeek(eventDay, eventMonth, eventYear);
        selectedRepTypeConfig.add(currentDefaultRepConfig);
        //The getDayOfWeek method returns a number between 1 and 7, but the weekly selection list starts in 0, as any other list, so a unit must be subtracted from the current week day:
        weeklyRepetitionListAdapter.setDefaultItem(currentDefaultRepConfig - 1);
    }

    //Method that resets the monthly repetition list adapter based on the current day of month:
    private void resetMonthlyRepetitionList() {
        currentDefaultRepConfig = eventDay;
        selectedRepTypeConfig.add(currentDefaultRepConfig);
        //The monthly selection list always starts with two empty elements, so if the current day is 4, the position of that element will be the fifth one:
        monthlyRepetitionGridAdapter.setDefaultItem(currentDefaultRepConfig + 1);
    }

    //Method for setting the behaviour of each clickable element inside the dialog:
    private void initializeButtonsBehaviour () {

        prevStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inLayoutSubLevel) {
                    if (visibleContainerIndex == PREV_ALARMS) revertPrevAlarmsLayout();

                    else if (visibleContainerIndex == REPETITION_TYPE) revertRepetitionTypeLayout();
                }

                else {
                    boolean goToPreviousStep = true;

                    if (visibleContainerIndex > REPETITION_CHECK)
                        checkAffirmative();

                    if (visibleContainerIndex == EVENT_DESCRIPTION)
                        finish();

                    else if (visibleContainerIndex == EVENT_DATE) {
                        currentDateView.setVisibility(View.GONE);
                        currentPar.setVisibility(View.VISIBLE);
                        setBackText();
                    }

                    else if (visibleContainerIndex == EVENT_TIME) {
                        timeDivisorFocus.setVisibility(View.GONE);
                    }

                    else if (!isAlarm && visibleContainerIndex == PREV_ALARMS || isAlarm && visibleContainerIndex == REPETITION_CHECK) {
                        timeDivisorFocus.setVisibility(View.VISIBLE);
                        currentDateView.setVisibility(View.VISIBLE);
                        currentPar.setVisibility(View.GONE);
                    }

                    else if (visibleContainerIndex == REPETITION_TYPE && displayingSpecialIntervals) {
                        hideAndRevertSpecialIntervals();
                        goToPreviousStep = false;
                    }

                    else if (visibleContainerIndex == STOP_CHECK && displayingSpecialIntervals) {
                        ((TextView) findViewById(R.id.ir_prev_step_text)).setText(getString(R.string.dismiss_menu_button));
                    }

                    setNextText();

                    if (goToPreviousStep) goToPreviousStep();
                }
            }
        });

        currentPar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayCurrentParameters(false);
            }
        });

        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (inLayoutSubLevel) {
                    if (visibleContainerIndex == PREV_ALARMS) addNewPreviousAlarm();

                    else if (visibleContainerIndex == REPETITION_TYPE) {
                        addNewRepetitionType();
                        revertRepetitionTypeLayout();
                    }
                }

                else {
                    boolean goToNextStep = true;

                    if (visibleContainerIndex == EVENT_DESCRIPTION) {

                        currentDateView.setVisibility(View.VISIBLE);
                        currentPar.setVisibility(View.GONE);

                        updateDateTextView();

                        selectedDescription = editDescription.getText().toString();
                    }

                    else if (visibleContainerIndex == EVENT_DATE || visibleContainerIndex == EVENT_TIME) {

                        selectedEventDate = DateUtils.transformToMillis(eventMinute, eventHour, eventDay, eventMonth, eventYear);

                        if (visibleContainerIndex == EVENT_DATE) {
                            if (startDateNotPreviousToCurrent())
                                timeDivisorFocus.setVisibility(View.VISIBLE);

                            else goToNextStep = false;
                        }

                        else {
                            if (startTimeNotPreviousToCurrent()) {
                                if (timeOrDateChanged()) previousAlarms = new JSONArray();

                                if (!isAlarm) {
                                    for (TextView prevAlarm: previousAlarmsLayouts) {
                                        prevAlarm.setVisibility(View.GONE);
                                    }

                                    setPreviousAlarmsText();
                                    setFinishText();
                                }

                                else if (!hasRepetition) setFinishText();

                                timeDivisorFocus.setVisibility(View.GONE);
                                currentDateView.setVisibility(View.GONE);
                                currentPar.setVisibility(View.VISIBLE);
                            }

                            else goToNextStep = false;
                        }
                    }

                    else if (visibleContainerIndex == PREV_ALARMS) {
                        if (!hasRepetition) setFinishText();
                    }

                    //If the next button is pressed when the interval check container is visible:
                    else if (visibleContainerIndex == REPETITION_CHECK) {
                        if (hasRepetition)  {
                            initializeRepetitionValues();
                        }
                    }

                    //If the next button is pressed when the interval selection container is visible:
                    else if (visibleContainerIndex == REPETITION_INTERVAL) {
                        hasRepetition = repetitionInterval != 0;
                        //Now the repetition type description initial text and the alternate hint text are set:
                        setRepDescriptionText();
                        if (eventType.equals(MEDICATION));
                        else setAlternateIntervalHintText();
                    }

                    else if (visibleContainerIndex == REPETITION_TYPE) {
                        if (eventType.equals(MEDICATION)) addNewRepetitionType();
                    }

                    else if (visibleContainerIndex == STOP_CHECK) {
                        setFinishText();
                        if (hasRepetition) {
                            if (selectedStopDate == -1 || dateChanged()) initializeStopDate();
                            setViewText(repetitionStopDaysText, repStopDaysDuration);
                        }
                        else selectedStopDate = -1;
                    }

                    if ((visibleContainerIndex != containersList.length - 1 && hasRepetition) || (visibleContainerIndex < REPETITION_CHECK && !hasRepetition)) {
                        if (visibleContainerIndex == PREV_ALARMS && !isAlarm) displayCurrentParameters(true);
                        else if (goToNextStep) {
                            setPrevText();
                            goToNextStep();
                        }
                    }

                    else displayCurrentParameters(true);
                }
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (dateChanged() && editingEvent) {
                    findViewById(R.id.current_parameters_dialog).setVisibility(View.GONE);
                    findViewById(R.id.follow_event_dialog).setVisibility(View.VISIBLE);

                    followNegative.requestFocus();
                }

                else sendPickedParameters(false);
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideCurrentParameters();
            }
        });

        dismissUnitSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePrevAlarmViewsFocusability();
                unitSelectionLayout.setVisibility(View.GONE);
                prevAlarmCustomLayout.setVisibility(View.VISIBLE);
                prevAlarmsUnitBox.requestFocus();
            }
        });

        dismissValueSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changePrevAlarmViewsFocusability();
                valueSelectionLayout.setVisibility(View.GONE);
                prevAlarmCustomLayout.setVisibility(View.VISIBLE);
                prevAlarmsValueBox.requestFocus();
            }
        });

        //Behaviour of the previous repetitionStopMonthText button when it is clicked:
        prevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int[] monthAndYear = DateUtils.moveToPreviousMonth(eventMonth, eventYear);

                eventMonth = monthAndYear[0];
                eventYear = monthAndYear[1];

                eventMonthText.setText(DateUtils.getMonth(eventMonth));
                eventYearText.setText(((Integer)eventYear).toString());

                calendarAdapter.updateAdapterItems(eventMonth, eventYear);

                //If the previous selected day was bigger than the number of days of the month, the selectedRepetitionStopDay value must be updated:
                eventDay = calendarAdapter.getSelectedItem();

                updateDateTextView();
            }
        });

        //Behaviour of the next repetitionStopMonthText button when it is clicked:
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int[] monthAndYear = DateUtils.moveToNextMonth(eventMonth, eventYear);

                eventMonth = monthAndYear[0];
                eventYear = monthAndYear[1];

                eventMonthText.setText(DateUtils.getMonth(eventMonth));
                eventYearText.setText(((Integer)eventYear).toString());

                calendarAdapter.updateAdapterItems(eventMonth, eventYear);

                //If the previous selected day was bigger than the number of days of the month, the selectedRepetitionStopDay value must be updated:
                eventDay = calendarAdapter.getSelectedItem();

                updateDateTextView();
            }
        });

        //Behaviour of the previous year button when it is clicked:
        prevYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eventYear = DateUtils.moveToPreviousYear(eventYear);

                eventYearText.setText(((Integer)eventYear).toString());

                calendarAdapter.updateAdapterItems(eventMonth, eventYear);

                //If the previous selected day was bigger than the number of days of the month, the selectedRepetitionStopDay value must be updated:
                eventDay = calendarAdapter.getSelectedItem();

                updateDateTextView();
            }
        });

        //Behaviour of the next year button when it is clicked:
        nextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                eventYear = DateUtils.moveToNextYear(eventYear);

                eventYearText.setText(((Integer)eventYear).toString());

                calendarAdapter.updateAdapterItems(eventMonth, eventYear);

                //If the previous selected day was bigger than the number of days of the month, the selectedRepetitionStopDay value must be updated:
                eventDay = calendarAdapter.getSelectedItem();

                updateDateTextView();
            }
        });

        //Behaviour of the previous alarms layout clickable elements:
        addDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent descriptionIntent = new Intent(getApplicationContext(), DialogAddDescriptionActivity.class);
                descriptionIntent.putExtra("type", eventType);
                startActivityForResult(descriptionIntent, REQUEST_DESCRIPTION_DIALOG);
            }
        });

        deleteDescriptionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               editDescription.setText("");
            }
        });

        //Behaviour of the previous alarms layout clickable elements:
        addAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (previousAlarms.length() < 4) {
                    prevAlarmsParamsLayout.setVisibility(View.VISIBLE);
                    prevAlarmsListLayout.setVisibility(View.GONE);
                    prevAlarmsUnitBox.requestFocus();
                    dialogTitle.setText(getString(R.string.parameters_prev_alarm_title));
                    subLevelDisplayed();
                }
            }
        });

        customAlarmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent datePickerIntent = new Intent(getApplicationContext(), DialogDatePickerActivity.class);

                datePickerIntent.putExtra("minute", eventMinute);
                datePickerIntent.putExtra("hour", eventHour);
                datePickerIntent.putExtra("day", eventDay);
                datePickerIntent.putExtra("month", eventMonth);
                datePickerIntent.putExtra("year", eventYear);

                startActivityForResult(datePickerIntent, REQUEST_PREV_ALARMS_DATE_PICKER);
            }
        });

        if (!isAlarm) {
            for (int i = 0; i < previousAlarmsLayouts.length; i++) {

                final int position = i;
                final TextView prevAlarm = previousAlarmsLayouts[i];

                prevAlarm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        prevAlarmToDelete = position;
                        Intent dialogIntent = new Intent(getApplicationContext(), DialogDeleteActivity.class);
                        dialogIntent.putExtra("deletePrevAlarm", true);
                        startActivityForResult(dialogIntent, REQUEST_DELETE_DIALOG);
                    }
                });
            }

            prevAlarmsValueBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prevAlarmCustomLayout.setVisibility(View.GONE);
                    valueSelectionLayout.setVisibility(View.VISIBLE);
                    prevAlarmsValuesList.scrollToPosition(0);
                    dismissValueSelectionButton.requestFocus();
                    changePrevAlarmViewsFocusability();
                }
            });

            prevAlarmsUnitBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prevAlarmCustomLayout.setVisibility(View.GONE);
                    unitSelectionLayout.setVisibility(View.VISIBLE);
                    prevAlarmsUnitsList.scrollToPosition(0);
                    dismissUnitSelectionButton.requestFocus();
                    changePrevAlarmViewsFocusability();
                }
            });
        }

        //Behaviour of the repetition type dialog buttons:
        if (eventType.equals(MEDICATION)) specialIntervalButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySpecialIntervals();
            }
        });

        else for (int i = 0; i < repetitionTypeButtons.length; i++) {

            final int position = i;
            final LinearLayout repTypeButton = repetitionTypeButtons[i];

            if (repTypeButton != null) {
                repTypeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        selectedRepType = position;

                        if (position != EventUtils.DAILY_REPETITION && position != EventUtils.ANNUAL_REPETITION) {
                            repTypeListLayout.setVisibility(View.GONE);
                            repetitionTypeLayouts[position - 1].setVisibility(View.VISIBLE);
                            repetitionTypeLayouts[position - 1].requestFocus();
                            subLevelDisplayed();

                            if (position == EventUtils.ALTERNATE_REPETITION)
                                dialogTitle.setText(getString(R.string.rep_type_alternate_title));

                            else {
                                dialogTitle.setText(getString(R.string.rep_type_weekly_monthly_title));

                                if (position == EventUtils.WEEKLY_REPETITION)
                                    resetWeeklyRepetitionList();

                                else if (position == EventUtils.MONTHLY_REPETITION)
                                    resetMonthlyRepetitionList();
                            }
                        }

                        else addNewRepetitionType();
                    }
                });
            }
        }

        //Behaviour of the affirmative option inside the follow event dialog:
        followAffirmative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendPickedParameters(true);
            }
        });

        //Behaviour of the negative option inside the follow event dialog:
        followNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendPickedParameters(false);
            }
        });

        //Behaviour of calendar grid items (day cells inside the calendar grid) when they are clicked:
        calendarGridListener = new EventCalendarAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                eventDay = calendarAdapter.getItem(position);

                calendarAdapter.selectNewDay(eventDay);

                updateDateTextView();
            }
        };

        if (!isAlarm) {
            //Behaviour of the previous alarm values list elements when they are clicked:
            prevAlarmsValueListListener = new PrevAlarmsListsAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    changePrevAlarmViewsFocusability();
                    String value = prevAlarmsValues.get(position);
                    selectedValue = Integer.valueOf(value);
                    prevAlarmsValueBox.setText(value);
                    setPrevAlarmsUnits(selectedValue > 1);
                    valueSelectionLayout.setVisibility(View.GONE);
                    prevAlarmCustomLayout.setVisibility(View.VISIBLE);
                    prevAlarmsValueBox.requestFocus();
                }
            };

            //Behaviour of the previous alarm units list elements when they are clicked:
            prevAlarmsUnitsListListener = new PrevAlarmsListsAdapter.ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    changePrevAlarmViewsFocusability();
                    selectedUnit = position;
                    setPrevAlarmsValues();
                    setPrevAlarmsUnits(false);
                    unitSelectionLayout.setVisibility(View.GONE);
                    prevAlarmCustomLayout.setVisibility(View.VISIBLE);
                    prevAlarmsUnitBox.setText(prevAlarmsUnits.get(selectedUnit));
                    prevAlarmsUnitBox.requestFocus();
                }
            };
        }

        repetitionTypeListListener = new RepTypeListsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                RepTypeListsAdapter.ViewHolder holder;

                if (selectedRepType == EventUtils.WEEKLY_REPETITION) {
                    holder = (RepTypeListsAdapter.ViewHolder) weeklyRepetitionList.findViewHolderForAdapterPosition(position);
                    addOrRemoveSelectedRepetitionValue(position + 1);
                }

                else {
                    holder = (RepTypeListsAdapter.ViewHolder) monthlyRepetitionGrid.findViewHolderForAdapterPosition(position);
                    addOrRemoveSelectedRepetitionValue(position - 1);
                }

                if (holder.selected) {
                    holder.selected = false;
                    holder.selectedBackground.setVisibility(View.GONE);
                    holder.text.setTextColor(Color.BLACK);
                    holder.text.setTypeface(Typeface.DEFAULT);
                }

                else {
                    holder.selected = true;
                    holder.selectedBackground.setVisibility(View.VISIBLE);
                    holder.text.setTextColor(Color.WHITE);
                    holder.text.setTypeface(Typeface.DEFAULT_BOLD);
                }
            }
        };

        //Behaviour of the time selection elements:
        increaseInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseInterval();
            }
        });

        decreaseInterval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseInterval();
            }
        });

        increaseRepNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseRepetitions();
            }
        });

        decreaseRepNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseRepetitions();
            }
        });

        increaseRepStopDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increaseStopDays();
            }
        });

        decreaseRepStopDays.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decreaseStopDays();
            }
        });

        customStopDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent datePickerIntent = new Intent(getApplicationContext(), DialogDatePickerActivity.class);

                datePickerIntent.putExtra("day", eventDay);
                datePickerIntent.putExtra("month", eventMonth);
                datePickerIntent.putExtra("year", eventYear);

                startActivityForResult(datePickerIntent, REQUEST_CUSTOM_STOP_DATE_PICKER);
            }
        });

        increaseAlternateInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increaseAlternateInt();
            }
        });

        decreaseAlternateInt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decreaseAlternateInt();
            }
        });

        if (eventType.equals(MEDICATION)) {
            increaseAlternateHour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    increaseAlternateHour();
                }
            });

            decreaseAlternateHour.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    decreaseAlternateHour();
                }
            });
        }

        checkBoxAffirmative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAffirmative();
            }
        });

        checkBoxNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkNegative();
            }
        });
    }

    @Override
    //This method will received a result from the date picker dialog when selecting a custom date for an early alarm:
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_PREV_ALARMS_DATE_PICKER && resultCode == RESULT_OK) {
            int minute = data.getIntExtra("minute", -1);
            int hour = data.getIntExtra("hour", -1);
            int day = data.getIntExtra("day", -1);
            int month = data.getIntExtra("month", -1);
            int year = data.getIntExtra("year", -1);

            String dateText = DateUtils.formatDateText(day, month, year) + " " ;
            String timeText = DateUtils.formatTimeString(hour) + ":" + DateUtils.formatTimeString(minute);
            String prevAlarmDate = dateText + timeText;

            String eventDateText = DateUtils.formatDateText(eventDay, eventMonth, eventYear) + " " ;
            String eventTimeText = DateUtils.formatTimeString(eventHour) + ":" + DateUtils.formatTimeString(eventMinute);
            String eventDate = eventDateText + eventTimeText;

            if (DateUtils.fullDateIsPrevious(prevAlarmDate, eventDate) && !DateUtils.fullDateIsPrevious(prevAlarmDate, DateUtils.getTodayFormattedDate()))
               createPrevAlarm(prevAlarmDate);

            else if (!DateUtils.fullDateIsPrevious(prevAlarmDate, eventDate)) {
                String messageHeader = getString(R.string.parameters_custom_date_error_header) + "\n";
                String messagePrevAlarmDate = getString(R.string.parameters_custom_date_header) + prevAlarmDate + "\n";
                String messageEventDate = getString(R.string.parameters_event_date_header) + eventDate;
                ToastUtils.makeCustomToast(getApplicationContext(), messageHeader + messagePrevAlarmDate + messageEventDate, ToastUtils.DEFAULT_TOAST_SIZE, 10);
            }

            else ToastUtils.makeCustomToast(getApplicationContext(), getString(R.string.parameters_error_creating_prev_alarm), ToastUtils.DEFAULT_TOAST_SIZE, 10);
        }

        if (requestCode == REQUEST_CUSTOM_STOP_DATE_PICKER && resultCode == RESULT_OK) {

            int day = data.getIntExtra("day", -1);
            int month = data.getIntExtra("month", -1);
            int year = data.getIntExtra("year", -1);

            if (!DateUtils.isPrevious(DateUtils.transformToMillis(repetitionStopMinute, repetitionStopHour, day, month, year), selectedEventDate)) {
                selectedStopDate = DateUtils.transformToMillis(repetitionStopMinute, repetitionStopHour, day, month, year);
                repStopDaysDuration = 0;
                setViewText(repetitionStopDaysText, repStopDaysDuration);
            }

            else ToastUtils.makeCustomToast(getApplicationContext(), getString(R.string.parameters_stop_date_error), ToastUtils.DEFAULT_TOAST_SIZE, 10);
        }

        else if (requestCode == REQUEST_DELETE_DIALOG && resultCode == RESULT_OK) {
            previousAlarms.remove(prevAlarmToDelete);
            previousAlarmsLayouts[previousAlarms.length()].setText("");
            previousAlarmsLayouts[previousAlarms.length()].setVisibility(View.GONE);
            setPreviousAlarmsText();
            addAlarmButton.requestFocus();
        }

        else if (requestCode == REQUEST_DESCRIPTION_DIALOG && resultCode == RESULT_OK) {
            String currentDescription = editDescription.getText().toString();

            if (currentDescription.equals("")) currentDescription = data.getStringExtra("description");
            else currentDescription = currentDescription + "\n" + data.getStringExtra("description");

            editDescription.setText(currentDescription);
        }
    }

    //Method that updates the current start date view text inside EVENT_DATE and EVENT_TIME dialogs:
    private void updateDateTextView() {
        DateUtils.formatEventDateText(eventDay, eventMonth, eventYear);
        currentDateView.setText(DateUtils.formattedDateText);
    }

    //Method that returns whether or not the start date of an event is previous to the current one (today). (Not including time):
    private boolean startDateNotPreviousToCurrent() {

        long currentDate = DateUtils.getTodayMillis();

        boolean dateNotPrevious = editingEvent || !DateUtils.isPrevious(selectedEventDate, currentDate);

        if (!dateNotPrevious) {
            int[] todayArray = DateUtils.transformFromMillis(DateUtils.getTodayMillis());

            String currentDateText = DateUtils.formatDateText(todayArray[DateUtils.DAY], todayArray[DateUtils.MONTH], todayArray[DateUtils.YEAR]);

            String messageHeader = getString(R.string.error_start_date) + "\n";
            String messageCurrentDate = getString(R.string.parameters_today_date_header) + currentDateText;
            String message = messageHeader + messageCurrentDate;

            ToastUtils.makeCustomToast(getApplicationContext(), message, ToastUtils.DEFAULT_TOAST_SIZE, 10);
        }

        return dateNotPrevious;
    }

    //Method that returns whether or not the start date of an event is previous to the current one (today). (Including time):
    private boolean startTimeNotPreviousToCurrent() {

        long currentDate = DateUtils.getTodayMillis();

        int[] currentDateArray = DateUtils.transformFromMillis(currentDate);

        int currentHour = currentDateArray[DateUtils.HOUR];
        int currentMinute = currentDateArray[DateUtils.MINUTE];

        boolean timeNotPrevious =  (
            editingEvent ||
            DateUtils.isPrevious(currentDate, selectedEventDate) ||
            DateUtils.isSameDay(currentDate, selectedEventDate) && (
                eventHour > currentHour ||
                eventHour == currentHour && eventMinute >= currentMinute
            )
        );

        if (!timeNotPrevious) {
            int[] todayArray = DateUtils.transformFromMillis(DateUtils.getTodayMillis());

            String currentDateText = DateUtils.formatDateText(todayArray[DateUtils.DAY], todayArray[DateUtils.MONTH], todayArray[DateUtils.YEAR])+ " ";
            String currentTimeText = DateUtils.formatTimeString(todayArray[DateUtils.HOUR]) + ":" + DateUtils.formatTimeString(todayArray[DateUtils.MINUTE]);

            String messageHeader = getString(R.string.error_start_time) + "\n\n";
            String messageCurrentDate = getString(R.string.parameters_today_date_header) + currentDateText + currentTimeText;
            String message = messageHeader + messageCurrentDate;

            ToastUtils.makeCustomToast(getApplicationContext(), message, ToastUtils.DEFAULT_TOAST_SIZE, 15);
        }

        return timeNotPrevious;
    }

    //Method that changes the necessary visibilities to display the special intervals layout for medication alarms:
    private void displaySpecialIntervals() {
        resetWeeklyRepetitionList();
        selectedRepType = EventUtils.WEEKLY_REPETITION;
        displayingSpecialIntervals = true;
        weeklyRepTypeLayout.setVisibility(View.VISIBLE);
        findViewById(R.id.med_alternate_rep_type_layout).setVisibility(View.GONE);
        ((TextView) findViewById(R.id.ir_prev_step_text)).setText(getString(R.string.dismiss_menu_button));
    }

    //Method that reverts the dialog views when the special intervals for medication alarms are being displayed:
    private void hideAndRevertSpecialIntervals() {
        selectedRepType = EventUtils.ALTERNATE_REPETITION;
        displayingSpecialIntervals = false;
        weeklyRepTypeLayout.setVisibility(View.GONE);
        findViewById(R.id.med_alternate_rep_type_layout).setVisibility(View.VISIBLE);
        setPrevText();
    }

    //Method that modifies some views when a sub level layout is being displayed (for example, when adding a new type of repetition):
    private void subLevelDisplayed() {
        inLayoutSubLevel = true;
        currentPar.setVisibility(View.GONE);
        ((TextView) findViewById(R.id.ir_next_step_text)).setText(getString(R.string.accept_button));
        ((TextView) findViewById(R.id.ir_prev_step_text)).setText(getString(R.string.dismiss_menu_button));
    }

    //Method that modifies some views when a sub level layout is hidden:
    private void subLevelHidden() {
        inLayoutSubLevel = false;
        currentPar.setVisibility(View.VISIBLE);

        if (visibleContainerIndex == PREV_ALARMS) setFinishText();
        else setNextText();

        ((TextView) findViewById(R.id.ir_prev_step_text)).setText(getString(R.string.event_parameters_previous));
    }

    //Method that sets the previous alarms views text:
    private void setPreviousAlarmsText() {
        if (previousAlarms != null) {
            String prevAlarmHeader = getString(R.string.parameters_prev_alarm_header);
            for (int i = 0; i < previousAlarms.length(); i++) {
                try {
                    previousAlarmsLayouts[i].setVisibility(View.VISIBLE);
                    previousAlarmsLayouts[i].setText(prevAlarmHeader + (i + 1) + ": " + (previousAlarms.getJSONObject(i)).getString("Alarm"));
                } catch (JSONException jse) {
                    Log.e(TAG, "setPreviousAlarmsText. JSONException: " + jse.getMessage());
                }
            }
        }
    }

    //Method that changes the focusability of several views within the previous alarms layout to block the focus inside a RecyclerView:
    private void changePrevAlarmViewsFocusability() {

        selectingPrevAlarm = !selectingPrevAlarm;

        boolean currentlyFocusable = prevAlarmsValueBox.isFocusable();

        prevAlarmsValueBox.setFocusable(!currentlyFocusable);
        prevAlarmsUnitBox.setFocusable(!currentlyFocusable);
        nextStep.setFocusable(!currentlyFocusable);
        prevStep.setFocusable(!currentlyFocusable);
    }

    //Method that reverts the previous alarms layout to its original visibility:
    private void revertPrevAlarmsLayout() {
        prevAlarmsParamsLayout.setVisibility(View.GONE);
        prevAlarmsListLayout.setVisibility(View.VISIBLE);

        subLevelHidden();

        selectedUnit = MINUTE_UNIT;
        setPrevAlarmsValues();
        setPrevAlarmsUnits(false);

        addAlarmButton.requestFocus();
        dialogTitle.setText(containersTitles[PREV_ALARMS]);
    }

    //Method that reverts the repetition type selection layout to its original visibility:
    private void revertRepetitionTypeLayout() {
        subLevelHidden();

        //The repetition config list and values are reset (if needed):
        if (selectedRepType > EventUtils.DAILY_REPETITION) {

            selectedRepTypeConfig = new ArrayList<>();

            if (selectedRepType == EventUtils.ALTERNATE_REPETITION) {
                alternateRepetitionValue = 1;
                setViewText(alternateIntervalText, alternateRepetitionValue);
            }

            else if (selectedRepType == EventUtils.WEEKLY_REPETITION) weeklyRepetitionListAdapter.notifyDataSetChanged();

            else if (selectedRepType == EventUtils.MONTHLY_REPETITION) monthlyRepetitionGridAdapter.notifyDataSetChanged();

            repetitionTypeButtons[selectedRepType].requestFocus();

            dialogTitle.setText(containersTitles[REPETITION_TYPE]);
        }

        repTypeListLayout.setVisibility(View.VISIBLE);

        for (RelativeLayout repLayout: repetitionTypeLayouts)
            if (repLayout != null) repLayout.setVisibility(View.GONE);
    }

    //Method that adds a new previous alarm:
    private void addNewPreviousAlarm() {

        String prevAlarmDate = null;

        switch (selectedUnit) {
            case MINUTE_UNIT:
                prevAlarmDate = DateUtils.getDecreasedByMinuteDate(eventMinute, eventHour, eventDay, eventMonth, eventYear, selectedValue);
                break;
            case HOUR_UNIT:
                prevAlarmDate = DateUtils.getDecreasedByHourDate(eventMinute, eventHour, eventDay, eventMonth, eventYear, selectedValue);
                break;
            case DAY_UNIT:
                prevAlarmDate = DateUtils.getDecreasedByDayDate(eventMinute, eventHour, eventDay, eventMonth, eventYear, selectedValue);
                break;
            case WEEK_UNIT:
                //Since a week always has 7 days, this calculation will be performed by multiplying the number of weeks by 7 and subtracting that number of days to the current date:
                prevAlarmDate = DateUtils.getDecreasedByDayDate(eventMinute, eventHour, eventDay, eventMonth, eventYear, selectedValue * 7);
                break;
            case MONTH_UNIT:
                prevAlarmDate = DateUtils.getDecreasedByMonthDate(eventMinute, eventHour, eventDay, eventMonth, eventYear, selectedValue);
        }

        if (prevAlarmDate != null && !DateUtils.fullDateIsPrevious(prevAlarmDate, DateUtils.getTodayFormattedDate()))
            createPrevAlarm(prevAlarmDate);

        else ToastUtils.makeCustomToast(getApplicationContext(), getString(R.string.parameters_error_creating_prev_alarm), ToastUtils.DEFAULT_TOAST_SIZE, 10);
    }

    //Method that encapsulates the creation of a early alarm:
    private void createPrevAlarm(String prevAlarmDate) {
        try {
            previousAlarms.put(new JSONObject("{\"Alarm\": " + "\"" + prevAlarmDate + "\"}"));
            previousAlarms = SortUtils.sortPrevAlarms(previousAlarms);
            setPreviousAlarmsText();
        } catch (JSONException jse) {
            Log.e(TAG, "addNewPreviousAlarm. JSONException: " + jse.getMessage());
        }

        revertPrevAlarmsLayout();
    }

    //Method that adds or removes a value to or from the current repetition config list:
    private void addOrRemoveSelectedRepetitionValue(int value) {

        boolean found = false;

        if (selectedRepTypeConfig.contains(value)) selectedRepTypeConfig.remove(Integer.valueOf(value));

        else if (selectedRepTypeConfig.size() > 0) {
            for (int i = 0; i < selectedRepTypeConfig.size(); i++) {
                if (selectedRepTypeConfig.get(i) > value) {
                    selectedRepTypeConfig.add(i, value);
                    found = true;
                    break;
                }
            }

            if (!found) selectedRepTypeConfig.add(value);
        }

        else selectedRepTypeConfig.add(value);
    }

    //Method that adds a new repetition type:
    private void addNewRepetitionType() {

        boolean isDailyOrAnnual = false;

        if (selectedRepType == EventUtils.DAILY_REPETITION || selectedRepType == EventUtils.ANNUAL_REPETITION) {
            isDailyOrAnnual = true;
            selectedRepTypeConfig = new ArrayList<>();
        }

        else if (selectedRepType == EventUtils.ALTERNATE_REPETITION) {
            selectedRepTypeConfig = new ArrayList<>();
            selectedRepTypeConfig.add(alternateRepetitionValue);
        }

        if (isDailyOrAnnual || selectedRepTypeConfig.size() != 0) {
            try {
                repetitionType.put("type", selectedRepType);
                repetitionType.put("config", selectedRepTypeConfig.toString());
                setRepDescriptionText();
            } catch (JSONException jse) {
                Log.e(TAG, "addNewPreviousAlarm. JSONException: " + jse.getMessage());
            }
        }
    }

    //Method that sets the repetition type description text:
    private void setRepDescriptionText() {

        String type = "";
        String config = "";

        int selectedRepType = 0;
        ArrayList<Integer> selectedRepTypeConfig = new ArrayList<>();

        try {
            selectedRepType = repetitionType.getInt("type");
            selectedRepTypeConfig = EventUtils.getRepetitionConfigArray(repetitionType.getString("config"));
        } catch (JSONException jse) {
            Log.e(TAG, "setRepDescriptionText. JSONException: " + jse.getMessage());
        }

        switch (selectedRepType) {
            case EventUtils.DAILY_REPETITION:
                type = getString(R.string.rep_type_description_daily);
                if (repetitionInterval == 1) config = getString(R.string.rep_config_description_daily_1, repetitionInterval);
                else config = getString(R.string.rep_config_description_daily_2, repetitionInterval);
                break;
            case EventUtils.ALTERNATE_REPETITION:
                type = getString(R.string.rep_type_description_alternate);
                int alternateInterval = selectedRepTypeConfig.get(0);
                if (alternateInterval == 1) config = getString(R.string.rep_config_description_alternate_1, selectedRepTypeConfig.get(0));
                else config = getString(R.string.rep_config_description_alternate_2, selectedRepTypeConfig.get(0));
                break;
            case EventUtils.WEEKLY_REPETITION:
                type = getString(R.string.rep_type_description_weekly);

                StringBuilder subConfig = new StringBuilder();
                subConfig.append(DateUtils.getDayOfWeekText(selectedRepTypeConfig.get(0) - 1));

                int lastConfigElement = selectedRepTypeConfig.size() - 1;

                for (int i = 1; i < lastConfigElement; i++) {
                    String auxWeekDay = ", " + DateUtils.getDayOfWeekText(selectedRepTypeConfig.get(i) - 1);
                    subConfig.append(auxWeekDay);
                }

                if (lastConfigElement > 0) {
                    String auxWeekDay = getString(R.string.parameters_rep_type_separator) + " " + DateUtils.getDayOfWeekText(selectedRepTypeConfig.get(lastConfigElement) - 1);
                    subConfig.append(auxWeekDay);
                }

                config = getString(R.string.rep_config_description_weekly) + subConfig.toString();
                break;
            case EventUtils.MONTHLY_REPETITION:
                type = getString(R.string.rep_type_description_monthly);

                String repetitionDays = selectedRepTypeConfig.toString();
                repetitionDays = repetitionDays.replace("[", "");
                repetitionDays = repetitionDays.replace("]", "");

                if (selectedRepTypeConfig.size() > 1) {
                    String lastDay = repetitionDays.split(",")[selectedRepTypeConfig.size() - 1];
                    repetitionDays = repetitionDays.replace("," + lastDay,  getString(R.string.parameters_rep_type_separator) + lastDay);
                }

                config = getString(R.string.rep_config_desc_monthly_header) + repetitionDays + getString(R.string.rep_config_desc_monthly_tail);
                break;
            case EventUtils.ANNUAL_REPETITION:
                type = getString(R.string.rep_type_description_annual);
                String repDate = eventDay + getString(R.string.date_divider_2) + DateUtils.getMonth(eventMonth) + ".";
                config = getString(R.string.rep_config_desc_annual_header) + repDate + getString(R.string.rep_config_desc_annual_tail);
                break;
        }

        repetitionTypeDescription.setText(getString(R.string.rep_type_description_header) + type + ".");
        repetitionConfigDescription.setText(getString(R.string.rep_config_description_header) + config + ".");
    }

    //Method for displaying the current parameters dialog, appropriately setting the value of each parameter's text view:
    private void displayCurrentParameters(boolean displayAcceptButton) {

        Resources res = getResources();

        if (onlyDisplayParameters) {
            findViewById(R.id.parameters_dialog_title).setVisibility(View.GONE);
            findViewById(R.id.parameters_only_title_container).setVisibility(View.VISIBLE);
            ((ImageView)findViewById(R.id.parameters_only_event_icon)).setImageDrawable(icon);
            ((TextView)findViewById(R.id.parameters_only_dialog_title)).setText(title);
        }

        String eventDateText = DateUtils.formatDateText(eventDay, eventMonth, eventYear);
        String eventTimeText = DateUtils.formatTimeString(eventHour) + ":" + DateUtils.formatTimeString(eventMinute);
        StringBuilder prevAlarmsText = new StringBuilder();

        if (previousAlarms != null && previousAlarms.length() != 0) {
            for (int i = 0; i < previousAlarms.length(); i++) {
                String text = previousAlarmsLayouts[i].getText().toString();
                prevAlarmsText.append(text);
                if (i != previousAlarms.length() - 1) prevAlarmsText.append("\n");
            }
        }

        else prevAlarmsText.append(getString(R.string.no_prev_alarms_text));

        if (visibleContainerIndex == EVENT_DESCRIPTION) selectedDescription = editDescription.getText().toString();

        int [] stopArray = DateUtils.transformFromMillis(selectedStopDate);

        String intervalText = DateUtils.formatTimeString(repetitionInterval) + getHourSubtext(repetitionInterval);
        String stopDateText = DateUtils.formatDateText(stopArray[DateUtils.DAY], stopArray[DateUtils.MONTH], stopArray[DateUtils.YEAR]);
        String stopTimeText = DateUtils.formatTimeString(repetitionStopHour) + ":" + DateUtils.formatTimeString(repetitionStopMinute);
        setRepDescriptionText();
        String repetitionType = repetitionTypeDescription.getText().toString();
        String repetitionConfig = repetitionConfigDescription.getText().toString();

        if (repetitionInterval == 0) {
            intervalText = res.getString(R.string.no_interval_text);
            stopDateText = res.getString(R.string.no_stop_text);
            stopTimeText = stopDateText;
            repetitionType = getString(R.string.rep_type_description_header) + intervalText;
            repetitionConfig = getString(R.string.rep_config_description_header) + stopDateText;
        }

        else if (selectedStopDate <= selectedEventDate) {
            stopDateText = res.getString(R.string.no_stop_text);
            stopTimeText = stopDateText;
        }

        ((TextView)findViewById(R.id.parameters_event_current_date)).setText(eventDateText);
        ((TextView)findViewById(R.id.parameters_event_current_time)).setText(eventTimeText);

        final TextView currentDescription = findViewById(R.id.parameters_event_current_desc);

        currentDescription.setMovementMethod(new ScrollingMovementMethod());
        currentDescription.setText(res.getString(R.string.parameters_event_description) + selectedDescription);

        currentDescription.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (currentDescription.canScrollVertically(-1) || currentDescription.canScrollVertically(+1)) {
                    LinearLayout descriptionContainer = findViewById(R.id.current_desc_container);
                    descriptionContainer.setBackground(getDrawable(R.drawable.text_box_background));
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) descriptionContainer.getLayoutParams();
                    params.setMargins(params.leftMargin,0, params.rightMargin, params.bottomMargin);

                    currentDescription.setMovementMethod(new ScrollingMovementMethod());
                    currentDescription.setFocusable(true);
                    int padding = EventListUtils.transformDipToPix(8);
                    currentDescription.setPadding(padding, padding, padding, padding);
                }

                else {
                    LinearLayout descriptionContainer = findViewById(R.id.current_desc_container);
                    descriptionContainer.setBackground(null);
                    RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) descriptionContainer.getLayoutParams();
                    int marginTop = EventListUtils.transformDipToPix(10);
                    params.setMargins(params.leftMargin, marginTop, params.rightMargin, params.bottomMargin);

                    currentDescription.setMovementMethod(null);
                    currentDescription.setFocusable(false);
                    currentDescription.setPadding(0, 0, 0, 0);
                }

                currentDescription.removeOnLayoutChangeListener(this);
            }
        });

        ((TextView) findViewById(R.id.parameters_prev_alarms)).setText(prevAlarmsText);

        //Only alarms wil have repetition, at least for now:
        if (!isAlarm) findViewById(R.id.repetition_parameters_container).setVisibility(View.GONE);

        else {
            ((TextView) findViewById(R.id.parameters_repetition_interval)).setText(intervalText);
            ((TextView) findViewById(R.id.parameters_stop_date)).setText(stopDateText);
            ((TextView) findViewById(R.id.parameters_stop_time)).setText(stopTimeText);
            ((TextView) findViewById(R.id.parameters_repetition_type)).setText(repetitionType);
            ((TextView) findViewById(R.id.parameters_repetition_config)).setText(repetitionConfig);
        }

        findViewById(R.id.event_parameters_dialog).setVisibility(View.GONE);

        findViewById(R.id.current_parameters_dialog).setVisibility(View.VISIBLE);

        TextView hintText = findViewById(R.id.dialog_hint_text);

        if (displayAcceptButton) {
            hintText.setVisibility(View.VISIBLE);

            if (editingEvent) hintText.setText(res.getString(R.string.parameters_dialog_subtitle_mod));
            else hintText.setText(res.getString(R.string.parameters_dialog_subtitle_set));

            acceptButton.setVisibility(View.VISIBLE);

            acceptButton.requestFocus();
        }

        else {
            hintText.setVisibility(View.GONE);
            acceptButton.setVisibility(View.GONE);

            cancelButton.requestFocus();
        }
    }

    //Method for hiding the current parameters dialog:
    private void hideCurrentParameters() {
        findViewById(R.id.current_parameters_dialog).setVisibility(View.GONE);

        findViewById(R.id.event_parameters_dialog).setVisibility(View.VISIBLE);

        currentPar.requestFocus();

        if (irPressed) irPressed = false;
    }

    //Method that checks if the event's date parameters have been modified, which would mean that the follow event dialog must be shown:
    private boolean dateChanged() {
        return  (originalDay != eventDay || originalMonth != eventMonth || originalYear != eventYear);
    }

    //Method that checks if the event's time parameters have been modified:
    private boolean timeOrDateChanged() {
        //If the event is not being edited, this method must always return false:
        return editingEvent && (originalHour != eventHour || originalMinute != eventMinute || dateChanged());
    }

    //Method that sets te prev step button text to the common one (Prev step):
    private void setPrevText() {
        ((TextView)findViewById(R.id.ir_prev_step_text)).setText(getResources().getString(R.string.event_parameters_previous));
    }

    //Method that sets the prev step button to the termination one (back):
    private void setBackText() {
        ((TextView)findViewById(R.id.ir_prev_step_text)).setText(getResources().getString(R.string.back));
    }

    //Method that sets the next step button's text to the common one (Next step):
    private void setNextText() {
        ((TextView)findViewById(R.id.ir_next_step_text)).setText(getResources().getString(R.string.event_parameters_next));
    }

    //Method that sets the next step button's text to the termination one (Finish):
    private void setFinishText() {
        ((TextView)findViewById(R.id.ir_next_step_text)).setText(getResources().getString(R.string.event_parameters_finish));
    }

    //Method that initializes the repetition intervals values and their text views:
    private void initializeRepetitionValues() {
        if (hasRepetition && repetitionInterval == 0) {
            repetitionInterval = 24;
        }

        calculateNumberOfRep();
        calculateRepInterval();
    }

    //Method that initializes the stop date value:
    private void initializeStopDate() {
        repStopDaysDuration = 1;
        selectedStopDate = DateUtils.transformToMillis(repetitionStopMinute, repetitionStopHour, eventDay, eventMonth, eventYear);
    }

    //Method for returning to the previous container inside the dialog:
    private void goToPreviousStep() {

        //The operation must be performed having in mind the minimum visible index:
        if (visibleContainerIndex > 0) {
            containersList[visibleContainerIndex].setVisibility(View.GONE);

            if (isAlarm && visibleContainerIndex == REPETITION_CHECK)
                visibleContainerIndex = visibleContainerIndex - 1;

            containersList[--visibleContainerIndex].setVisibility(View.VISIBLE);

            dialogTitle.setText(containersTitles[visibleContainerIndex]);

            if (irPressed) containersList[visibleContainerIndex].requestFocus();
            else prevStep.requestFocus();

            //The calendarGridListener for focusing the hour or year selection middle element once the recycler view is fully loaded is set just in case it is needed:
            setLoadCompletionListener();
        }
    }

    //Method for advancing to the next container inside the dialog:
    private void goToNextStep() {

        //The operation must be performed having in mind the length of the containers' list:
        if (visibleContainerIndex < containersList.length - 1 && (visibleContainerIndex < REPETITION_CHECK || hasRepetition)) {
            containersList[visibleContainerIndex].setVisibility(View.GONE);

            if (isAlarm && visibleContainerIndex == EVENT_TIME)
                visibleContainerIndex = visibleContainerIndex + 1;

            containersList[++visibleContainerIndex].setVisibility(View.VISIBLE);

            dialogTitle.setText(containersTitles[visibleContainerIndex]);

            //The calendarGridListener for focusing the hour or year selection middle element once the recycler view is fully loaded is set just in case it is needed:
            setLoadCompletionListener();
        }
    }

    //Method that sets the listeners for focusing the hour or year selection lists' middle element when the previous or next operation is performed by pressing the ir keys:
    private void setLoadCompletionListener() {
        //When the displayed container is the one that corresponds to the event time selection one:
        if (visibleContainerIndex == EVENT_TIME) {
            eventHourList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    checkVisibleIndex();
                    eventHourList.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
            });
        }
        //When the displayed container is not any of the previously mentioned:
        else if (irPressed) {
            irPressed = false;
            containersList[visibleContainerIndex].requestFocus();
        }
    }

    //Method that focus the first element of a container or that initializes the selection lists' elements colors and that focuses the first list middle element if an ir key was pressed:
    private void checkVisibleIndex() {
        //When the displayed container is the one that corresponds to the event time selection one:
        if (visibleContainerIndex == EVENT_TIME) {
            int firstVisibleItemIndex = ((LinearLayoutManager) eventHourList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            if (irPressed) {
                eventHourList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1).itemView.requestFocus();
                irPressed = false;
            }
            ((SelectionListAdapter.ViewHolder) eventHourList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1)).centeredHolder();

            firstVisibleItemIndex = ((LinearLayoutManager) eventMinuteList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
            ((SelectionListAdapter.ViewHolder) eventMinuteList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1)).centeredHolder();
        }
        //When a different container is being displayed its first focusable element gets the focus (only
        //if an ir key was pressed or if the container is the first one and an event is being modified):
        else if (irPressed) {
            irPressed = false;
            containersList[visibleContainerIndex].requestFocus();
        }
    }

    //Method that selects the affirmative checkbox and sets the has repetition value to true:
    private void checkAffirmative () {

        Resources res = getResources();

        //When the affirmative check box is not selected, the check boxes' backgrounds and the hasRepetition variable value are changed:
        if (!hasRepetition) {
            checkBoxAffirmative.setBackground(res.getDrawable(R.drawable.edition_checkbox_selected));
            checkBoxNegative.setBackground(res.getDrawable(R.drawable.edition_checkbox_background));
            hasRepetition = true;
            setNextText();
        }
    }

    //Method that selects the negative checkbox and sets the has repetition value to true:
    private void checkNegative () {

        Resources res = getResources();

        //When the negative check box is selected, the check boxes' backgrounds and the hasRepetition variable value are changed:
        if (hasRepetition) {
            checkBoxNegative.setBackground(res.getDrawable(R.drawable.edition_checkbox_selected));
            checkBoxAffirmative.setBackground(res.getDrawable(R.drawable.edition_checkbox_background));
            selectedStopDate = -1;
            hasRepetition = false;
            if (visibleContainerIndex == REPETITION_CHECK) repetitionInterval = 0;
            setFinishText();
        }
    }

    //Method that makes all the necessary operations in order to increase the value of the repetition interval:
    private void increaseInterval () {

        //The maximum repetition interval will be 24 hours:
        if (repetitionInterval == 24) repetitionInterval = 1;
        else ++repetitionInterval;

        //An additional zero will be added when the hour is smaller than 10;
        setViewText(repetitionIntervalText, repetitionInterval);

        calculateNumberOfRep();
    }

    //Method that makes all the necessary operations in order to decrease the value of the repetition interval:
    private void decreaseInterval () {

        //The maximum repetition interval will be 24 hours:
        if (repetitionInterval == 1) repetitionInterval = 24;
        else repetitionInterval--;

        //An additional zero will be added when the hour is smaller than 10;
        setViewText(repetitionIntervalText, repetitionInterval);

        calculateNumberOfRep();
    }

    //Method that makes all the necessary operations in order to increase the value of the number of repetitions:
    private void increaseRepetitions () {

        //The maximum number of repetitions will be 24:
        if (repetitionNumber == 24) repetitionNumber = 1;
        else ++repetitionNumber;

        //An additional zero will be added when the number is smaller than 10;
        setViewText(repetitionNumberText, repetitionNumber);

        calculateRepInterval();
    }

    //Method that makes all the necessary operations in order to decrease the value of the number of repetitions:
    private void decreaseRepetitions () {

        //The maximum number of repetitions will be 24:
        if (repetitionNumber == 1) repetitionNumber = 24;
        else repetitionNumber--;

        //An additional zero will be added when the hour is smaller than 10;
        setViewText(repetitionNumberText, repetitionNumber);

        calculateRepInterval();
    }

    //Method that makes all the necessary operations in order to increase the value of the repetition duration:
    private void increaseStopDays () {

        //The maximum number of days for the repetition will be 360:
        if (repStopDaysDuration == 365 || repStopDaysDuration == 0) {
            initializeStopDate();
        }
        else {
            ++repStopDaysDuration;
            selectedStopDate = DateUtils.getIncreasedByDayDate(selectedStopDate, 1);
        }

        setViewText(repetitionStopDaysText, repStopDaysDuration);
    }

    //Method that makes all the necessary operations in order to decrease the value of the repetition duration:
    private void decreaseStopDays () {

        //The maximum number of repetitions will be 24:
        if (repStopDaysDuration == 1 || repStopDaysDuration == 0) {
            initializeStopDate();
            repStopDaysDuration = 365;
            selectedStopDate = DateUtils.getIncreasedByDayDate(selectedStopDate, repStopDaysDuration);
        }
        else {
            repStopDaysDuration--;
            selectedStopDate = DateUtils.getDecreasedByDayDate(selectedStopDate, 1);
        }

        setViewText(repetitionStopDaysText, repStopDaysDuration);
    }

    //Method that makes all the necessary operations in order to increase the value of the alternate repetition type:
    private void increaseAlternateInt () {

        //The maximum value of the alternate interval will be 30:
        if (alternateRepetitionValue == 30) alternateRepetitionValue = 1;
        else alternateRepetitionValue++;

        setViewText(alternateIntervalText, alternateRepetitionValue);

        if (eventType.equals(MEDICATION)) calculateAlternateRepHour();
        else setAlternateIntervalHintText();
    }

    //Method that makes all the necessary operations in order to decrease the value of the alternate repetition type:
    private void decreaseAlternateInt() {

        //The minimum value of alternate interval will be 2:
        if (alternateRepetitionValue == 1) alternateRepetitionValue = 30;
        else alternateRepetitionValue--;

        setViewText(alternateIntervalText, alternateRepetitionValue);

        if (eventType.equals(MEDICATION)) calculateAlternateRepHour();
        else setAlternateIntervalHintText();
    }

    //Method that makes all the necessary operations in order to increase the value of the alternate repetition type:
    private void increaseAlternateHour () {

        //The maximum value of the alternate interval will be 30:
        if (alternateRepetitionHour == 30 * 24) alternateRepetitionHour = 24;
        else alternateRepetitionHour += 24;

        setViewText(alternateHourText, alternateRepetitionHour);

        calculateAlternateRepValue();
    }

    //Method that makes all the necessary operations in order to decrease the value of the alternate repetition type:
    private void decreaseAlternateHour() {

        //The minimum value of alternate interval will be 2:
        if (alternateRepetitionHour == 24) alternateRepetitionHour = 30 * 24;
        else alternateRepetitionHour -= 24;

        setViewText(alternateHourText, alternateRepetitionHour);

        calculateAlternateRepValue();
    }

    //Method that calculates the number of repetitions based on the repetition interval:
    private void calculateNumberOfRep () {
        repetitionNumber = 24 / repetitionInterval;

        setViewText(repetitionNumberText, repetitionNumber);
    }

    //Method that calculates the repetition interval based on the number of repetitions:
    private void calculateRepInterval() {
        repetitionInterval = 24 / repetitionNumber;

        setViewText(repetitionIntervalText, repetitionInterval);
    }

    //Method that calculates the number of repetitions based on the repetition interval:
    private void calculateAlternateRepValue () {
        alternateRepetitionValue = alternateRepetitionHour / 24;

        setViewText(alternateIntervalText, alternateRepetitionValue);
    }

    //Method that calculates the repetition interval based on the number of repetitions:
    private void calculateAlternateRepHour() {
        alternateRepetitionHour = alternateRepetitionValue * 24;

        setViewText(alternateHourText, alternateRepetitionHour);
    }


    //Method that returns the text Hour or Hours based on the value of the parameter hourValue:
    private String getHourSubtext(int hourValue) {
        String hourSubtext = getResources().getString(R.string.parameters_hours_tag);
        if (hourValue == 1) hourSubtext = getResources().getString(R.string.parameters_hour_tag);

        return " " + hourSubtext;
    }

    //This method sets the text for a numeric incremental text view:
    private void setViewText(TextView view, int value) {

        String viewText = "";

        if (view.getId() == repetitionIntervalText.getId()) {
            if (value == 1) viewText = getString(R.string.parameters_rep_interval_text_1, DateUtils.formatTimeString(value));
            else viewText = getString(R.string.parameters_rep_interval_text_2, DateUtils.formatTimeString(value));
        }

        else if (view.getId() == repetitionNumberText.getId()) {
            if (value == 1) viewText = getString(R.string.parameters_rep_number_text_1, Integer.valueOf(value).toString());
            else viewText = getString(R.string.parameters_rep_number_text_2, Integer.valueOf(value).toString());
        }

        else if (view.getId() == alternateIntervalText.getId()) {
            if (value == 1) viewText = getString(R.string.parameters_alt_interval_text_1);
            else viewText = getString(R.string.parameters_alt_interval_text_2, Integer.valueOf(value).toString());
        }

        else if (alternateHourText != null && view.getId() == alternateHourText.getId()) {
            if (value == 1) viewText = getString(R.string.parameters_rep_interval_text_1, DateUtils.formatTimeString(value));
            else viewText = getString(R.string.parameters_rep_interval_text_2, DateUtils.formatTimeString(value));
        }

        else if (view.getId() == repetitionStopDaysText.getId()) {

            int [] stopArray = DateUtils.transformFromMillis(selectedStopDate);
            String lastRepDate = DateUtils.formatEventDateText(stopArray[DateUtils.DAY], stopArray[DateUtils.MONTH], stopArray[DateUtils.YEAR]);

            if (value == 0) viewText = getString(R.string.parameters_custom_stop_date);

            else if (value == 1) viewText = getString(R.string.parameters_stop_days_text_1, Integer.valueOf(value).toString());

            else viewText = getString(R.string.parameters_stop_days_text_2, Integer.valueOf(value).toString());

            repetitionStopDescription.setText(getString(R.string.parameters_rep_stop_description) + lastRepDate);
        }

        view.setText(viewText);
    }

    //This method sets the alternate interval layout hint text:
    private void setAlternateIntervalHintText() {
        String header;
        String tail = "):";

        if (alternateRepetitionValue == 1) header = getString(R.string.rep_config_description_alternate_1, alternateRepetitionValue);
        else header = getString(R.string.rep_config_description_alternate_2, alternateRepetitionValue);

        if (repetitionInterval == 1) tail = getString(R.string.parameters_rep_interval_text_1, DateUtils.formatTimeString(repetitionInterval)) + tail;
        else tail = getString(R.string.parameters_rep_interval_text_2, DateUtils.formatTimeString(repetitionInterval)) + tail;

        alternateIntervalHint.setText(header + getString(R.string.rep_type_alternate_hint) + tail.toLowerCase());
    }

    //This method sends the selected parameters (to DialogNewEventActivity) so the event can be set or modified accordingly:
    protected void sendPickedParameters (boolean goAfterEvent) {

        Intent returnIntent = new Intent();

        returnIntent.putExtra("goAfterEvent", goAfterEvent);

        returnIntent.putExtra(EventUtils.EVENT_HOUR_FIELD, eventHour);
        returnIntent.putExtra(EventUtils.EVENT_MINUTE_FIELD, eventMinute);

        if (previousAlarms.length() > 0)
            returnIntent.putExtra(EventUtils.EVENT_PREV_ALARMS_FIELD, previousAlarms.toString());

        //The interval and repetition stop values are sent only when there is a repetition interval:
        if (repetitionInterval != 0) {
            returnIntent.putExtra(EventUtils.EVENT_INTERVAL_FIELD, repetitionInterval);

            returnIntent.putExtra(EventUtils.EVENT_REP_TYPE_FIELD, repetitionType.toString());

            returnIntent.putExtra(EventUtils.EVENT_REPETITION_STOP_FIELD, selectedStopDate);
        }

        returnIntent.putExtra(EventUtils.EVENT_DAY_FIELD, eventDay);
        returnIntent.putExtra(EventUtils.EVENT_MONTH_FIELD, eventMonth);
        returnIntent.putExtra(EventUtils.EVENT_YEAR_FIELD, eventYear);

        returnIntent.putExtra(EventUtils.EVENT_DESCRIPTION_FIELD, selectedDescription);

        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }
}
