package com.droidmare.calendar.views.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.CalendarGridItem;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventUtils;
import com.droidmare.calendar.views.activities.MainActivity;
import com.droidmare.calendar.views.adapters.events.EventListAdapter;
import com.droidmare.database.publisher.EventsPublisher;

import java.util.ArrayList;

//Event list fragment declaration
//@author Eduardo on 13/02/2018.

public class EventFragment extends Fragment {

    //Name of the events fragment
    public static final String NAME = "event_fragment";

    //Fragment's context:
    private Context fragmentContext;

    private View view;

    private RecyclerView eventList;

    //Listener for handling on calendar grid items clicks:
    private EventListAdapter.ItemClickListener listener;

    //Adapter for the event list inside the view:
    private EventListAdapter adapter;

    //A reference for accessing the calendar fragment:
    private CalendarFragment calFrag;

    //A control variable to know if an event was deleted or modified:
    private Boolean deletedOrModified;

    //A control variable to know if after modifying an event, the views must be reload to follow it or to stay in the selected day:
    private Boolean goAfterEventDay;

    private EventListItem modifiedEvent;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_events, container, false);

        fragmentContext = getActivity();

        //Initialization of control variables:
        deletedOrModified = false;
        goAfterEventDay = false;

        //Initialization of buttons (considering that the event list items are buttons too):
        //newEvent = view.findViewById(R.id.new_event_button);

        setButtonsBehaviour();

        //Definition of the calendar fragment reference:
        calFrag = (CalendarFragment) getFragmentManager().findFragmentByTag(CalendarFragment.NAME);

        //The calendar fragment must be informed that this event fragment can be referenced now:
        calFrag.referenceEventFragment();

        //Initialization of the event list:
        eventList = view.findViewById(R.id.event_list);

        //First of all, the recycler view must be configured:
        eventList.setFocusable(false);
        eventList.setLayoutManager(new LinearLayoutManager(fragmentContext));
        eventList.getRecycledViewPool().setMaxRecycledViews(0,0);

        //Obtaining the calendar item for the current day:
        CalendarGridItem calItem = calFrag.getCurrentDayItem();

        //Initialization of the event list title bar text:
        TextView dateText = view.findViewById(R.id.date_text);
        String formattedDate = DateUtils.formatCurrentDateText(calItem.getDayOfWeek(), calItem.getDayText());
        dateText.setText(formattedDate);

        //Initialization of the adapter:
        adapter = new EventListAdapter(fragmentContext, calItem.getEventList(), listener, false);

        eventList.setAdapter(adapter);

        //Now the events for the current month can be loaded and the calendar view updated
        calFrag.startGridInit();

        return view;
    }

    //Function for defining the buttons behaviour:
    private void setButtonsBehaviour() {

        //Behaviour of event list items when they are clicked:
        listener = new EventListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                adapter.getItem(position).showEventMenu(true);
                adapter.setSelectedPosition(position);
                adapter.notifyItemChanged(position);
            }
        };
    }

    //Method that indicates the event fragment to go to the event day and focus it once the views had been reloaded:
    public void goAfterEventDay() { goAfterEventDay = true; }

    //Function for getting the number of events in the list:
    public int getNumberOfEvents() {
        return adapter.getItemCount();
    }

    public EventListAdapter getAdapter() { return adapter; }

    public RecyclerView getEventList() {
        return eventList;
    }

    //Function for modifying the complete event view:
    public void updateList(String dateText, CalendarGridItem calItem) {

        ArrayList<EventListItem> events = calItem.getEventList();

        if (displayingEventMenu()) dismissEventMenu(true);
        adapter.updateAdapterItems(events);

        //The element that will get the focus from the right edge calendar items is set based on the existence of events:
        if (getNumberOfEvents() > 0) {
            eventList.setFocusable(true);
            calFrag.setNextFocusRight(true);
        }
        else {
            eventList.setFocusable(false);
            calFrag.setNextFocusRight(false);
        }

        //This will happen only when the event list title must be updated:
        if (dateText != null) {
            TextView text = view.findViewById(R.id.date_text);
            text.setText(dateText);
        }

        adapter.notifyDataSetChanged();

        //If the calendar must go after the current day, the focus is relocated over it:
        if (goAfterEventDay) {
            calFrag.focusSelectedItem();
            goAfterEventDay = false;
        }

        //When an event is deleted or modified leaving the event list empty, the focus is relocated on the selected day:
        if (events.size() == 0 && deletedOrModified) {
            deletedOrModified = false;
            ((MainActivity)fragmentContext).findViewById(R.id.ir_show_all_events).requestFocus();
            ((MainActivity)fragmentContext).returnToSelected();
        }
        else if (deletedOrModified) deletedOrModified = false;
    }

    //Function for creating a new event:
    public void createNewEvent(Intent data) {

        //The event is created and added to the selected day:
        EventListItem event = EventUtils.makeEvent(fragmentContext, data);

        if (event != null) addNewEvent(event);
    }

    //Function for updating the date params in order to correctly retrieve the events and update the views:
    private void updateDateParams(EventListItem event, boolean creatingEvent) {
        //Only when the new date is different from the previous one, the corresponding day is selected so that the selected item's background inside the calendar doesn't blink.
        //This happens due to the fact that when a new item is selected, the previously selected day's background is reset, so if the previously selected day is the same as the
        //new one, the background will blink:
        if (DateUtils.notSameDate(event)) {

            DateUtils.currentDay = event.getEventDay();
            DateUtils.currentMonth = event.getEventMonth();
            DateUtils.currentYear = event.getEventYear();

            calFrag.selectNewDay(creatingEvent);
        }
    }

    //Function for adding a new event to the database and update the views:
    private void addNewEvent(EventListItem event) {
        //In order to simplify the code, the function that publishes an event in the database receives an array, not a single event:
        EventListItem[] eventList = {event};

        updateDateParams(event, true);

        EventsPublisher.publishEvents(fragmentContext, eventList);
    }

    //Function for deleting an event from the database and update the views:
    public void deleteEvent(EventListItem event) {
        //It is necessary to indicate that the operation is a delete one:
        deletedOrModified = true;

        updateDateParams(event, false);

        //When an event is deleted, the focus must always be relocated, if possible, on the next event list item:
        relocateFocusAfterDelete();

        //Now the alarm that was set for the event must be cancelled:
        EventUtils.deleteAlarm (fragmentContext, event);

        //First the event is deleted from the database and the application views:
        EventsPublisher.deleteEvent(fragmentContext, event.getEventId());
    }

    //Method that relocates the focus based on the number of elements in the list:
    public void relocateFocusAfterDelete() {
        deletedOrModified = true;
        if (getNumberOfEvents() == 1) adapter.setNextFocusedPosition(-2);
        else setNextFocusedPosition(adapter.getSelectedPosition());
    }

    //Function for modifying the parameters of an existing event inside the database and update the views:
    public void modifyEvent(Intent data){
        //It is necessary to indicate that the operation is a modify one:
        deletedOrModified = true;

        goAfterEventDay = data.getBooleanExtra("goAfterEvent", false);

        //When an event is modified, the focus must always be relocated, if possible, on the next event list item or on the modified event (if neither its day, its month or its year were changed or the user didn't choose to go after it)):
        if (!goAfterEventDay) setNextFocusedPosition(adapter.getSelectedPosition());

        EventListItem originalEvent = adapter.getSelectedItem();

        //After modifying the event the views will go back to or stay in (depending on the calendar view) the selected day's month and year:
        updateDateParams(originalEvent, false);

        modifiedEvent = originalEvent;

        //The update only happens if at least one parameter was modified:
        if (modifiedEvent.updateEventParams(data) || goAfterEventDay) {

            //After modifying the event the views will go to the modified event's new date based on the value of the extra "goAfterEventDay":
            if (goAfterEventDay) updateDateParams(modifiedEvent, false);

            //If the event is the only one in the list and at least its day was changed, the next focused position is set to -2 so the adapter can know it (this is due to the fact that when the last
            //event in the list is modified changing its date (which results on an empty event list), the behaviour of the application regarding the focus relocation is annoyingly buggy, so the way
            //the focus behaves in this situation is set in the MainActivity's dispatchKeyEvent function based on the value of the adapter's nextFocusedPosition variable):
            if ((getNumberOfEvents() == 1 && DateUtils.notSameDate(modifiedEvent)) || goAfterEventDay) setFocusBehaviourAfterEdit();

            //The new alarm will have the same id than the old one and, therefore, will overwrite it:
            EventUtils.makeAlarm(fragmentContext, modifiedEvent);

            ((MainActivity)fragmentContext).sendEditEvent(modifiedEvent);

            //The loading layout will be displayed until the operation ends and the views are refreshed, unless the operation was triggered by the synchronization service:
            ((MainActivity)fragmentContext).displayLoadingLayout(getResources().getString(R.string.loading_layout_modify));
        }
        //Even if the event wasn't updated, the event menu must always be dismissed:
        else dismissEventMenu(false);
    }

    //Method that checks and decides if the event is going to be displayed on the current day after updating the event list and acts consequently:
    private void setFocusBehaviourAfterEdit() {
        long currentDate = DateUtils.getCurrentMillis();
        modifiedEvent.calculateNextRepetition(currentDate);

        //Since an event can be modified and still being displayed in the selected day (as long as it has a repetition),
        //the start and stop dates, as well as the next repetition for the current date must be checked in order to determine
        //if the event is going to stay in the selected date or if the event is not going to be displayed on that day any more:
        long eventStartDate = DateUtils.transformToMillis(0, 0, modifiedEvent.getEventDay(), modifiedEvent.getEventMonth(), modifiedEvent.getEventYear());
        if (modifiedEvent.getIntervalTime() == 0 ||
                (
                        DateUtils.isPrevious(modifiedEvent.getRepetitionStop(), DateUtils.getCurrentMillis()) &&
                                modifiedEvent.getRepetitionStop() != -1
                )
                || eventStartDate > DateUtils.getCurrentMillis()
                || !DateUtils.isSameDay(modifiedEvent.getNextRepetition(), currentDate)
                || goAfterEventDay) {
            //If the event is not going to be displayed on the selected day any more, the adapter must be notified so that the focus behaviour is appropriate
            //once the views have been refreshed and a new day is selected (in this case, if the next focused position is not set to -2, the focus will be
            //relocated on the first event as soon as a day with events is selected, which, indeed, is not the correct focus behaviour for that situation):
            adapter.setNextFocusedPosition(-2);
        }
    }

    //Function for dismissing the event menu (when an event was clicked):
    public void dismissEventMenu(boolean updatedEvent){
        adapter.dismissEventMenu(updatedEvent);
    }

    //Function for getting the last focused button inside the event menu:
    public boolean displayingEventMenu() {
        return adapter.displayingEventMenu();
    }

    //Function for setting the next event in the list that will get the focus:
    public void setNextFocusedPosition(int position){

        int numberOfEvents = getNumberOfEvents();

        //First of all it is necessary to check if the number of events is bigger than one, because if it is not, the list will be empty when the operation ends:
        if (numberOfEvents > 1) {
            //If the number of events is equal to position + 1, the selected element is the last one, so the focus will be relocated on the previous element:
            if (numberOfEvents == position + 1)
                adapter.setNextFocusedPosition(position - 1);

                //Otherwise, the focus will stay on the selected position (that will be occupied by the next element on the list once the operation has ended):
            else adapter.setNextFocusedPosition(position);
        }
    }

    //Function for setting the next event in the list that will get the focus after a local sync operation for deleting is performed:
    public void setNextFocusedAfterSyncDelete(int position){
        deletedOrModified = true;
        if (getNumberOfEvents() == 1) adapter.setNextFocusedPosition(-2);
        else setNextFocusedPosition(position);
    }

    //Function for setting the next event in the list that will get the focus after a local sync operation for editing is performed:
    public void setNextFocusedAfterSyncEdit(int position, EventListItem retrievedEvent){

        deletedOrModified = true;

        //If the event is the only one in the list and at least its day was changed, the next focused position is set to -2 so the adapter can know it (this is due to the fact that when the last
        //event in the list is modified changing its date (which results on an empty event list), the behaviour of the application regarding the focus relocation is annoyingly buggy, so the way
        //the focus behaves in this situation is set in the MainActivity's dispatchKeyEvent function based on the value of the adapter's nextFocusedPosition variable):
        if (getNumberOfEvents() == 1 && DateUtils.notSameDate(retrievedEvent)) {

            //Since an event can be modified and still being displayed in the selected day (as long as it has a repetition),
            //the start and stop dates must be checked in order to determine if the event is going to stay in the selected or not:
            long eventStartDate = DateUtils.transformToMillis(0, 0, retrievedEvent.getEventDay(), retrievedEvent.getEventMonth(), retrievedEvent.getEventYear());

            if (retrievedEvent.getIntervalTime() == 0 || (retrievedEvent.getRepetitionStop() < DateUtils.getCurrentMillis() && retrievedEvent.getRepetitionStop() != -1) || eventStartDate > DateUtils.getCurrentMillis())
                adapter.setNextFocusedPosition(-2);

            else setNextFocusedPosition(position);
        }

        else setNextFocusedPosition(position);
    }
}
