package com.droidmare.calendar.views.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.CalendarGridItem;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.SortUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.calendar.views.adapters.calendar.CalendarGridAdapter;
import com.droidmare.calendar.views.adapters.calendar.CalendarDayGridAdapter;
import com.droidmare.calendar.views.adapters.events.EventListAdapter;
import com.droidmare.database.publisher.EventsPublisher;

import java.util.ArrayList;

//calendar fragment declaration
//@author Eduardo on 07/02/2018.

public class CalendarFragment extends Fragment {

    // Name of the calendar fragment
    public static final String NAME = "calendar_fragment";

    //Fragment's context:
    private Context fragmentContext;

    //A reference for accessing the events fragment;
    private EventFragment eventFragment;

    //Month and year selection bar components:
    private LinearLayout prevMonth, nextMonth, prevYear, nextYear;
    private TextView month, year;

    //Listener for handling on calendar grid items clicks:
    private CalendarGridAdapter.ItemClickListener listener;

    //Adapter for the calendar grid:
    private CalendarGridAdapter calendarAdapter;

    //List of items for the calendar adapter:
    private ArrayList<CalendarGridItem> calendarGridList;

    //Control variable to know whether the selected day must be focused after creating as new event:
    private boolean focusSelectedDayOnCreation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        fragmentContext = getActivity();

        //The current day, month and year must be set in first place:
        DateUtils.initDateParameters(getResources());

        //Initialization of the month and year selection bar components:
        prevMonth = view.findViewById(R.id.prev_month);
        nextMonth = view.findViewById(R.id.next_month);
        prevYear = view.findViewById(R.id.prev_year);
        nextYear = view.findViewById(R.id.next_year);

        month = view.findViewById(R.id.month);
        year = view.findViewById(R.id.year);

        setButtonsBehaviour();

        month.setText(DateUtils.getMonth(DateUtils.currentMonth));
        Integer currentYear = DateUtils.currentYear;
        year.setText(currentYear.toString());

        focusSelectedDayOnCreation = false;

        //Initialization of the items list for the adapter:
        initCalendarGrid(null,null);

        //Initialization of the day grid (Columns names for the calendar grid):
        RecyclerView dayGrid = view.findViewById(R.id.day_grid);
        dayGrid.setLayoutManager(new GridLayoutManager(fragmentContext,7));
        dayGrid.setHasFixedSize(true);

        CalendarDayGridAdapter dayAdapter = new CalendarDayGridAdapter(fragmentContext);
        dayGrid.setFocusable(false);
        dayGrid.setAdapter(dayAdapter);

        //Initialization of the calendar grid (for the current month):
        RecyclerView calendarGrid = view.findViewById(R.id.calendar_grid);
        calendarGrid.setLayoutManager(new GridLayoutManager(fragmentContext,7));
        calendarGrid.setHasFixedSize(true);

        calendarAdapter = new CalendarGridAdapter(fragmentContext, listener, calendarGridList);
        calendarGrid.setAdapter(calendarAdapter);


        return view;
    }

    //Once the event fragment exists, this method will be called:
    public void referenceEventFragment() {
        eventFragment = (EventFragment) getFragmentManager().findFragmentByTag(EventFragment.NAME);
    }

    //Function for defining the buttons behaviour:
    private void setButtonsBehaviour(){

        //Behaviour of the previous month button when it is clicked:
        prevMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First the date parameters must move back to the previous month:
                DateUtils.moveToPreviousMonth();
                //Now the calendar view must be updated:
                startGridInit();
            }
        });

        //Behaviour of the next month button when it is clicked:
        nextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First the date parameters must move on to the next month:
                DateUtils.moveToNextMonth();
                //Now the calendar view must be updated:
                startGridInit();
            }
        });

        //Behaviour of the previous year button when it is clicked:
        prevYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First the date parameters must move back to the previous month:
                DateUtils.moveToPreviousYear();
                //Now the calendar view must be updated:
                startGridInit();
            }
        });

        //Behaviour of the next year button when it is clicked:
        nextYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //First the date parameters must move on to the next month:
                DateUtils.moveToNextYear();
                //Now the calendar view must be updated:
                startGridInit();
            }
        });

        //Behaviour of calendar grid items (day cells inside the calendar grid) when they are clicked:
        listener = new CalendarGridAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                CalendarGridItem calendarItem = calendarAdapter.getItem(position);

                String dayOfWeek = calendarItem.getDayOfWeek();
                String dayNumber = calendarItem.getDayText();

                DateUtils.currentDay = Integer.parseInt(dayNumber);

                //The selected position must be stored, along with the corresponding month and year:
                calendarAdapter.selectNewDay(true);

                //The event list title must be updated based on the current day, month and year:
                String formattedDate = DateUtils.formatCurrentDateText(dayOfWeek, dayNumber);

                eventFragment.updateList(formattedDate, calendarItem);
            }
        };
    }

    //Function for selecting a new day inside the calendar grid:
    public void selectNewDay (boolean creatingEvent) {
        //creatingEvent will be true only when the event was created in a different day than the selected one:
        focusSelectedDayOnCreation = creatingEvent;
        calendarAdapter.selectNewDay(false);
    }

    //Function for updating the selected day's event list:
    public void updateSelectedDayEvent (EventListItem eventModification, EventListItem eventOriginal) {
        calendarAdapter.updateSelectedDayEvent(eventModification, eventOriginal);
    }

    //Function for retrieving the month that corresponds to the selected day's one:
    public int getSelectedMonth () {
        return calendarAdapter.getSelectedMonth();
    }

    //Function for retrieving the year that corresponds to the selected day's one:
    public int getSelectedYear () {
        return calendarAdapter.getSelectedYear();
    }

    //Function for getting the item for the current selected day:
    public CalendarGridItem getCurrentDayItem () {

        int position = calendarAdapter.getSelectedPosition();

        return calendarAdapter.getItem(position);
    }

    //Function for getting the position for the current selected day:
    public int getCurrentDayPosition () {

        return calendarAdapter.getSelectedPosition();
    }

    //Function for getting the position for the current focused day:
    public int getFocusedDayPosition () {

        return calendarAdapter.getFocusedPosition();
    }

    //Function for focusing the selected item:
    public void focusSelectedItem () {

        calendarAdapter.focusSelectedItem();
    }

    //Function for setting the element that will get the focus when dpad right button is clicked at the right edge of the calendar grid:
    public void setNextFocusRight (boolean listIsFocusable) {
        calendarAdapter.setNextFocusRight(listIsFocusable);

        //It is also necessary to set the next year button's next focus right:
        if (listIsFocusable) nextYear.setNextFocusRightId(eventFragment.getEventList().getId());
        else nextYear.setNextFocusRightId(nextYear.getId());
    }

    //Function that receives the events of the current month and updates the views:
    public void loadMonthEvents(ArrayList<EventListItem>[] events, ArrayList<EventListItem> repetitiveEvents) {

        //If the event date was modified during its creation, the new day's view must be loaded and the day itself must be selected and focused before refreshing the events:
        if (focusSelectedDayOnCreation) {
            focusSelectedDayOnCreation = false;
            ((MainActivity)fragmentContext).returnToSelected();
        }

        //Otherwise the behaviour will be the default one (the events and vies are refreshed directly):
        else {
            //The month and year texts are always updated:
            month.setText(DateUtils.getMonth(DateUtils.currentMonth));
            String currentYear = Integer.valueOf(DateUtils.currentYear).toString();
            year.setText(currentYear);

            //Now the whole calendar grid must be updated:
            initCalendarGrid(events, repetitiveEvents);

            //The attribute calendarGridList was modified inside the previous method:
            calendarAdapter.updateAdapterItems(calendarGridList);

            //If the loading layout was being displayed, it must be hidden:
            ((MainActivity) fragmentContext).hideLoadingLayout();

            //Since this function will be called when a month or year change is performed but the event list must only be updated when a new day is selected or an event
            //operation (modify, delete, add, go to current, etc) is performed, the event list will be updated only if the month and year coincide with the selected ones:
            if (DateUtils.sameMonthAndYear(calendarAdapter.getSelectedMonth(), calendarAdapter.getSelectedYear()))
                refreshEventList();

            if (focusSelectedDayOnCreation) {
                focusSelectedDayOnCreation = false;
                ((MainActivity) fragmentContext).returnToCurrent();
            }
        }
    }

    //Function for performing an event list update:
    private void refreshEventList () {

        //Given that the information required to update the event list is contained inside each calendar grid item, the first thing that must be done is retrieving the current one:
        CalendarGridItem currentDay = getCurrentDayItem();

        //Now the event list title is generated to match the current day's date:
        String dayOfWeek = currentDay.getDayOfWeek();
        String dayNumber = currentDay.getDayText();

        String formattedDate = DateUtils.formatCurrentDateText(dayOfWeek, dayNumber);

        //The list update operation is performed in the event fragment:
        eventFragment.updateList(formattedDate, currentDay);
    }

    //Starts the grid initialization process (retrieves the month events before loading the grid elements):
    public void startGridInit() {
        EventsPublisher.retrieveMonthEvents(fragmentContext);
    }

    //Function that reloads the calendar grid updating each one of its items based on the retrieved events and the current date:
    private void initCalendarGrid(ArrayList<EventListItem>[] eventsList, ArrayList<EventListItem> repetitiveEvents) {

        int monthStartDay;
        int numberOfDays;

        CalendarGridItem item;
        int day;

        calendarGridList = new ArrayList<>();
        ArrayList<EventListItem> dayEventsList;

        //The first thing that we need to know is the start day of the current month and its number of days:
        monthStartDay = DateUtils.findDayOfWeek();
        numberOfDays = DateUtils.numberOfDays();

        //The number of elements inside the calendar grid is always 42:
        for(int i = 1; i <= (42); i++){

            dayEventsList = new ArrayList<>();

            int dayOfWeek = (i - 1) % 7;

            day = i - monthStartDay + 1;

            //The cells that don't correspond to a day of the current month must be empty:
            if (i < monthStartDay || day > numberOfDays)  item = null;

            else {
                //Array eventsList will be initialized with the events stored in the database, saving in each one of its indexes an array lis with all the
                //events for the corresponding day (in the position 0 all the events for the day 1, in the position 1 all the events for the day 2, etc):
                if (eventsList != null) {
                    //If one day has no events, the corresponding index will be null:
                    if (eventsList[day - 1] != null) dayEventsList = eventsList[day - 1];
                }

                //Now the repetitive events are loaded into each day only if their repetition stop date is posterior to the day's date and their start date is previous to it:
                if (repetitiveEvents != null) {

                    long repetitionStopMillis;

                    for (int repetitionIndex = 0; repetitionIndex < repetitiveEvents.size(); repetitionIndex++) {

                        EventListItem event = repetitiveEvents.get(repetitionIndex);

                        long originalStartDate = DateUtils.transformToMillis(event.getEventMinute(), event.getEventHour(), event.getEventDay(), event.getEventMonth(), event.getEventYear());
                        long currentDayDate = DateUtils.transformToMillis(event.getEventMinute(), 0, day, DateUtils.currentMonth, DateUtils.currentYear);

                        //It is necessary to check if the current day is posterior to the repetition stop date (and act consequently):
                        repetitionStopMillis = event.getRepetitionStop();

                        if (!DateUtils.isPrevious(repetitionStopMillis, currentDayDate) || repetitionStopMillis == -1) {

                            long actualCurrentDate = DateUtils.getTodayMillis();

                            if (DateUtils.isSameDay(currentDayDate, actualCurrentDate))
                                event.calculateNextRepetition(actualCurrentDate);

                            else event.calculateNextRepetition(currentDayDate);

                            long eventNextRepetition = event.getNextRepetition();

                            boolean notEndDate = eventNextRepetition <= repetitionStopMillis;

                            //The repetitive events are not added to the days that are previous to the start date:
                            if ((DateUtils.isSameDay(eventNextRepetition, currentDayDate) && !DateUtils.isPrevious(eventNextRepetition, actualCurrentDate)
                                    || DateUtils.isSameDay(eventNextRepetition, currentDayDate) && DateUtils.isSameDay(eventNextRepetition, originalStartDate)) /*&& notEndDate*/) {
                                //The date that will be shown for the repetitive events outside their original start date is the next repetition time for the corresponding day:
                                EventListItem eventCopy = event.getEventCopy();
                                //The copy of the event needs to reference its original event in order to perform operations and display the start date:
                                eventCopy.referenceOriginalEvent(event);
                                //Now the date params can be updated based on the next repetition's time for the day that is currently being initialized (if the first repetition for the current day is
                                //posterior to the stop date, the function getNextRepetition() will return -1. case in which updateDateParams will return false and the event won't be added to the current day):
                                if (eventCopy.updateDateParams(eventNextRepetition)) {
                                    //Finally the event can be added to the day that is being initialized:
                                    dayEventsList.add(eventCopy);
                                }
                            }
                            //The event is always added to its original start date if the next repetition for that day has already passed:
                            else if (DateUtils.isSameDay(originalStartDate, currentDayDate)) {
                                dayEventsList.add(event);
                            }
                        }

                        //If the event's stop day was reached, the event is deleted from the list so it is not checked fo the next days.
                        //Since deleting an event means decreasing the array's length by one, it is necessary to subtract 1 to the repetitionIndex variable:
                        else {
                            repetitiveEvents.remove(event);
                            repetitionIndex--;
                        }
                    }
                }

                //Now the day can be created and added to the calendar grid, having in mind that first the event list is sorted based on the next repetition of each event:
                item = new CalendarGridItem(Integer.toString(day), DateUtils.getDayOfWeekText(dayOfWeek), SortUtils.sortEventList(dayEventsList));
            }

            calendarGridList.add(item);
        }
    }
}
