package com.droidmare.calendar.views.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.common.utils.DateUtils;
import com.droidmare.calendar.views.adapters.datepicker.DatePickerCalendarAdapter;
import com.droidmare.calendar.views.adapters.datepicker.DatePickerDayGridAdapter;
import com.droidmare.calendar.views.adapters.datepicker.DatePickerTimeListAdapter;
import com.droidmare.common.utils.ToastUtils;

//Activity for selecting a custom date and time when creating/editing an event
//@author Eduardo on 22/03/2019.

public class DialogDatePickerActivity extends AppCompatActivity {

    //Date values:
    private int minute;
    private int hour;
    private int day;
    private int month;
    private int year;

    //Dialog date texts:
    private TextView dateText;
    private TextView hintText;

    //Dialog accept button:
    private LinearLayout acceptSelection;

    //Recycler views:
    private RecyclerView minutesList;
    private RecyclerView hoursList;
    private RecyclerView calendarGrid;

    //Recycler views' adapters:
    private DatePickerTimeListAdapter minutesListAdapter;
    private DatePickerTimeListAdapter hoursListAdapter;
    private DatePickerCalendarAdapter calendarAdapter;

    //Recycler views' adapters listeners:
    private DatePickerCalendarAdapter.ItemClickListener calendarAdapterListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogEventParameters.setLaunchedActivityReference(this);

        setContentView(R.layout.activity_dialog_date_picker);

        Intent intent = getIntent();
        minute = intent.getIntExtra("minute", -1);
        hour = intent.getIntExtra("hour", -1);
        day = intent.getIntExtra("day", -1);
        month = intent.getIntExtra("month", -1);
        year = intent.getIntExtra("year", -1);

        //Initialization of buttons and textViews:
        if (minute == -1) {
            findViewById(R.id.date_picker_full_footer).setVisibility(View.GONE);
            findViewById(R.id.date_picker_short_footer).setVisibility(View.VISIBLE);
            acceptSelection = findViewById(R.id.accept_button_short);
            dateText = findViewById(R.id.date_text_short);
        }

        else {
            acceptSelection = findViewById(R.id.accept_button);
            dateText = findViewById(R.id.date_text);
        }

        hintText = findViewById(R.id.date_picker_hint_text);

        setButtonsBehaviour();
        refreshDateText();

        //Initialization of the calendar grid:
        RecyclerView dayGrid = findViewById(R.id.day_grid);
        dayGrid.setLayoutManager(new GridLayoutManager(this,7));
        dayGrid.setHasFixedSize(true);
        dayGrid.setFocusable(false);
        dayGrid.setClickable(false);

        DatePickerDayGridAdapter dayGridAdapter = new DatePickerDayGridAdapter(this);
        dayGrid.setAdapter(dayGridAdapter);

        calendarGrid = findViewById(R.id.calendar_grid);
        calendarGrid.setLayoutManager(new GridLayoutManager(this,7));

        calendarAdapter = new DatePickerCalendarAdapter(this, day, month, year, calendarAdapterListener);
        calendarGrid.setAdapter(calendarAdapter);

        //Initialization of the minutes list:
        minutesList = findViewById(R.id.minutes_list);
        minutesList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //In order to avoid malfunctions the elements inside the recycler view are not recycled:
        minutesList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        minutesList.setHasFixedSize(true);

        minutesListAdapter = new DatePickerTimeListAdapter(this, getUpdatedMinutesListParameter());
        minutesList.setFocusable(false);
        minutesList.setAdapter(minutesListAdapter);

        //Initialization of the hours list:
        hoursList = findViewById(R.id.hours_list);
        hoursList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //In order to avoid malfunctions the elements inside the recycler view are not recycled:
        hoursList.getRecycledViewPool().setMaxRecycledViews(0, 0);
        hoursList.setHasFixedSize(true);

        hoursListAdapter = new DatePickerTimeListAdapter(this, getUpdatedHoursListParameter());

        hoursList.setFocusable(false);
        hoursList.setAdapter(hoursListAdapter);
    }

    //Navigation along the recycler views is set as follows to avoid some Android bugs:
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        View focusedView = getCurrentFocus();

        if (event.getAction() == KeyEvent.ACTION_DOWN && ToastUtils.cancelCurrentToast()) return true;

        else if (focusedView != null && event.getAction()== KeyEvent.ACTION_DOWN) {
            //If a view is focused, the parent is got so it can be checked if it is one of the list recycler views:
            View focusedItemParent = (View)focusedView.getParent();

            if (focusedItemParent.getId() == minutesList.getId() || focusedItemParent.getId() == hoursList.getId()) {

                //Depending on the pressed key the list scrolls upwards or downwards:
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    if (focusedItemParent.getId() == hoursList.getId() && hour != 0 || focusedItemParent.getId() == minutesList.getId() && minute != 0)
                        decreaseListParameter(focusedItemParent.getId());

                    //When the first visible element is the first one in the list, the focus must return to the calendar grid:
                    else {
                        if (calendarAdapter.getNumberOfEmptyBottomRows() == 2) calendarAdapter.focusView(22);
                        else if (calendarAdapter.getNumberOfEmptyBottomRows() == 1) {
                            if (calendarAdapter.getLastDayPosition() > 29) calendarAdapter.focusView(29);
                            else calendarAdapter.focusView(calendarAdapter.getLastDayPosition());
                        }
                        else calendarAdapter.focusView(calendarAdapter.getLastDayPosition());
                    }

                    return true;
                }
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
                    increaseListParameter(focusedItemParent.getId());
                    return true;
                }

                //When the focused view parent is the hours list, pressing the left key will have no effect at all:
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && focusedItemParent.getId() == hoursList.getId()) return true;
            }


            else if (focusedView.getId() == acceptSelection.getId()){
                //When the accept button is focused and the up key pressed, the focus must return to the calendar grid:
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {

                    if (calendarAdapter.getNumberOfEmptyBottomRows() == 2)
                        calendarAdapter.focusView(26);

                    else if (calendarAdapter.getNumberOfEmptyBottomRows() == 1) {

                        if (calendarAdapter.getLastDayPosition() > 33)
                            calendarAdapter.focusView(33);
                        else calendarAdapter.focusView(calendarAdapter.getLastDayPosition());
                    }

                    else calendarAdapter.focusView(calendarAdapter.getLastDayPosition());

                    return true;
                }
                //If the pressed button is the left key, the focus must be relocated on the minutes list central element:
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && minute != -1) {
                    int firstVisibleItemIndex = ((LinearLayoutManager) minutesList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                    minutesList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1).itemView.requestFocus();
                    return true;
                }
            }

            //When the calendar is focused various different things can happen:
            else if (((View)focusedItemParent.getParent()).getId() == calendarGrid.getId()) {

                int focusedViewPosition = calendarAdapter.returnViewPosition(focusedView);

                //If the d-pad down key is pressed and the selected element is one of the calendar last row's element, the focus must be relocated on the central element inside the hours list:
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN &&  minute != -1) {
                    if ((calendarAdapter.getNumberOfEmptyBottomRows() == 2 && focusedViewPosition > 20 && focusedViewPosition < 28)
                            || (calendarAdapter.getNumberOfEmptyBottomRows() == 1 && focusedViewPosition > 27 && focusedViewPosition < 35)
                            || (calendarAdapter.getNumberOfEmptyBottomRows() == 0 && focusedViewPosition > 34)) {
                        int firstVisibleItemIndex = ((LinearLayoutManager) hoursList.getLayoutManager()).findFirstCompletelyVisibleItemPosition();
                        hoursList.findViewHolderForAdapterPosition(firstVisibleItemIndex + 1).itemView.requestFocus();
                        return true;
                    }
                }
                //If the d-pad left key is pressed and the focused element is on the left edge of the calendar, the month moves to the previous one:
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && focusedViewPosition % 7 == 0) {
                    int[] monthAndYear = DateUtils.moveToPreviousMonth(month, year);
                    month = monthAndYear[0];
                    year = monthAndYear[1];
                    calendarAdapter.updateAdapterItems(month, year);
                    //Given the fact that the selected day may change if the selected item was the last one and the next month has less days than the previous one:
                    day = calendarAdapter.getSelectedItem();
                    refreshDateText();
                    return true;
                }

                //If the d-pad right key is pressed and the focused element is on the right edge of the calendar, the month moves to the next one:
                else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && focusedViewPosition % 7 == 6) {
                    int[] monthAndYear = DateUtils.moveToNextMonth(month, year);
                    month = monthAndYear[0];
                    year = monthAndYear[1];
                    calendarAdapter.updateAdapterItems(month, year);
                    //Given the fact that the selected day may change if the selected item was the last one and the previous month has less days than the next one:
                    day = calendarAdapter.getSelectedItem();
                    refreshDateText();
                    return true;
                }
            }
        }

        return super.dispatchKeyEvent(event);
    }

    //This method sends the date parameters back to the activity that started this one:
    protected void sendPickedParameters () {
        Intent returnIntent = new Intent();

        returnIntent.putExtra("minute", minute);
        returnIntent.putExtra("hour", hour);
        returnIntent.putExtra("day", day);
        returnIntent.putExtra("month", month);
        returnIntent.putExtra("year", year);

        setResult(Activity.RESULT_OK, returnIntent);

        finish();
    }

    //Behaviour of the time picker buttons when they are clicked:
    protected void setButtonsBehaviour () {

        //Behaviour of the accept button
        acceptSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPickedParameters();
            }
        });

        //Behaviour of calendar grid items (day cells inside the calendar grid) when they are clicked:
        calendarAdapterListener = new DatePickerCalendarAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                day = calendarAdapter.getItem(position);

                calendarAdapter.selectNewDay(day);

                refreshDateText();
            }
        };
    }

    //Method that increases the current start minute or hour depending on the focused list:
    private void increaseListParameter(int listId) {

        if (listId == hoursList.getId() && hour < 23) {
            hour++;
            hoursListAdapter.updateItems(getUpdatedHoursListParameter());
        }

        else if (listId == minutesList.getId() && minute < 59) {
            minute++;
            minutesListAdapter.updateItems(getUpdatedMinutesListParameter());
        }

        refreshDateText();
    }

    //Method that decreases the current start minute or hour depending on the focused list:
    private void decreaseListParameter(int listId) {

        if (listId == hoursList.getId() && hour > 0) {
            hour--;
            hoursListAdapter.updateItems(getUpdatedHoursListParameter());
        }

        else if (listId == minutesList.getId() && minute > 0) {
            minute--;
            minutesListAdapter.updateItems(getUpdatedMinutesListParameter());
        }

        refreshDateText();
    }

    //Method that generates a new array for the hours list depending on the value of the current start hour:
    private String[] getUpdatedHoursListParameter() {
        if (hour == 0) return new String[] {"", DateUtils.formatTimeString(hour), DateUtils.formatTimeString(hour + 1)};

        else if (hour == 23) return new String[] {DateUtils.formatTimeString(hour - 1), DateUtils.formatTimeString(hour), ""};

        else return new String[] {DateUtils.formatTimeString(hour - 1), DateUtils.formatTimeString(hour), DateUtils.formatTimeString(hour + 1)};
    }

    //Method that generates a new array for the minutes list depending on the value of the current start minute:
    private String[] getUpdatedMinutesListParameter() {
        if (minute == 0) return new String[] {"", DateUtils.formatTimeString(minute), DateUtils.formatTimeString(minute + 1)};

        else if (minute == 59) return new String[] {DateUtils.formatTimeString(minute - 1), DateUtils.formatTimeString(minute), ""};

        else return new String[] {DateUtils.formatTimeString(minute - 1), DateUtils.formatTimeString(minute), DateUtils.formatTimeString(minute + 1)};
    }

    //Method for refreshing the date text:
    private void refreshDateText () {
        String date = DateUtils.formatDateText(day, month, year);

        String time = "";
        if (minute != -1) time = "\n" + DateUtils.formatTimeString(hour) + ":" + DateUtils.formatTimeString(minute);

        String prevAlarmDate = date + time;

        dateText.setText(prevAlarmDate);
        hintText.setText(DateUtils.formatEventDateText(day, month, year));
    }
}
