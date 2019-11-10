package com.droidmare.calendar.views.adapters.calendar;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.CalendarGridItem;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.common.utils.DateUtils;

import java.util.ArrayList;

//Calendar grid adapter declaration
//@author Eduardo on 15/02/2018.

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.ViewHolder> {

    private ArrayList<CalendarGridItem> itemList;

    private ArrayList<ViewHolder> viewList;

    private LayoutInflater mInflater;

    private ItemClickListener mClickListener;

    private final Resources res;

    private int selectedPosition;
    private int selectedMonth;
    private int selectedYear;

    // data is passed into the constructor
    public CalendarGridAdapter(Context context, ItemClickListener listener, ArrayList<CalendarGridItem> calendarGrid) {
        this.mInflater = LayoutInflater.from(context);
        this.itemList = calendarGrid;
        this.res = context.getResources();
        selectedPosition = DateUtils.getCurrentDayPosition();
        selectedMonth = DateUtils.currentMonth;
        selectedYear =  DateUtils.currentYear;
        viewList = new ArrayList<>();
        setClickListener(listener);
    }

    //Function for setting the element that will get the focus when dpad right button is clicked at the right edge of the calendar grid:
    public void setNextFocusRight (boolean listIsFocusable) {

        //Only elements at the right edge of the calendar must be configured:
        for (int i = 6; i < viewList.size(); i += 7) {
            viewList.get(i).itemView.setNextFocusRightId( R.id.event_list);
            //else viewList.get(i).itemView.setNextFocusRightId( R.id.ir_add_new_event_layout);
        }
    }

    // the adapter items are updated including the view holders (this only happens when the month is changed):
    public void updateAdapterItems (ArrayList<CalendarGridItem> calendarGrid){

        itemList = calendarGrid;

        for (int i = 0; i < viewList.size(); i++) {

            CalendarGridItem item = itemList.get(i);
            CalendarGridAdapter.ViewHolder holder = viewList.get(i);

            if (item != null){

                holder.text.setText(item.getDayText());
                holder.itemView.setFocusable(true);

                boolean hasAlarms = item.hasAlarms();

                //The visibility of the item icon is set based on the existence of events for the corresponding day:
                if (item.hasEvents()) holder.icon.setVisibility(View.VISIBLE);
                else holder.icon.setVisibility(View.INVISIBLE);

                if (i == selectedPosition) {

                    if (DateUtils.sameMonthAndYear(selectedMonth, selectedYear)) {
                        holder.text.setTextColor(res.getColor(R.color.white));

                        if (!hasAlarms) holder.itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected));
                        else holder.itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected_alarms));
                    }
                    else {
                        holder.text.setTextColor(res.getColor(R.color.black));

                        if (!hasAlarms) holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
                        else holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus_alarm));
                    }
                }
                else {
                    if (!hasAlarms) holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
                    else holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus_alarm));
                }
            }
            else {
                holder.text.setTextColor(res.getColor(R.color.black));
                holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
                holder.text.setText(null);
                holder.icon.setVisibility(View.INVISIBLE);
                holder.itemView.setFocusable(false);
            }
        }
    }

    // method that updates the "eventOriginal" object inside the event list of the selected day, replacing it by the "eventModification" parameter;
    public void updateSelectedDayEvent (EventListItem eventModification, EventListItem eventOriginal) {
        ArrayList<EventListItem> selectedDayEvents = getItem(getSelectedPosition()).getEventList();
        selectedDayEvents.set(selectedDayEvents.indexOf(eventOriginal),eventModification);
    }

    // method that selects a new day inside the calendar view:
    public void selectNewDay(boolean clickAction) {

        //If the selected day and the new selected day are the same, the background is not changed given that it would just blink:
        if (selectedPosition != DateUtils.getCurrentDayPosition() || selectedMonth != DateUtils.currentMonth || selectedYear != DateUtils.currentYear) {

            viewList.get(selectedPosition).text.setTextColor(res.getColor(R.color.black));

            boolean hasAlarms = false;

            CalendarGridItem item = itemList.get(selectedPosition);

            if (item != null) hasAlarms = item.hasAlarms();

            if (!hasAlarms) viewList.get(selectedPosition).itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
            else viewList.get(selectedPosition).itemView.setBackground(res.getDrawable(R.drawable.calendar_focus_alarm));

            selectedPosition = DateUtils.getCurrentDayPosition();
            selectedMonth = DateUtils.currentMonth;
            selectedYear = DateUtils.currentYear;

            //The selected day text and background colors will be changed only if the new selection was due to a click action on a calendar item.
            //If it wasn't, the color will change after refreshing the calendar view:
            if (clickAction) {
                viewList.get(selectedPosition).text.setTextColor(res.getColor(R.color.white));

                hasAlarms = false;

                item = itemList.get(selectedPosition);

                if (item != null) hasAlarms = item.hasAlarms();

                if (!hasAlarms) viewList.get(selectedPosition).itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected));
                else viewList.get(selectedPosition).itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected_alarms));
            }
        }
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_calendar_grid, parent, false);
        ViewHolder holder = new ViewHolder(view);
        viewList.add(holder);
        return holder;
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        CalendarGridItem item = itemList.get(position);

        if (item != null){

            holder.text.setText(item.getDayText());
            holder.itemView.setFocusable(true);

            //The visibility of the item icon is set based on the existence of events for the corresponding day:
            if (item.hasEvents()) holder.icon.setVisibility(View.VISIBLE);
            else  holder.icon.setVisibility(View.INVISIBLE);

            if (position == selectedPosition && DateUtils.sameMonthAndYear(selectedMonth, selectedYear)) {
                holder.text.setTextColor(res.getColor(R.color.white));

                if (!item.hasAlarms()) holder.itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected));
                else holder.itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected_alarms));

                holder.itemView.requestFocus();
            }
            else {
                if (!item.hasAlarms()) holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
                else holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus_alarm));
            }
        }
        else holder.itemView.setFocusable(false);

        //Elements at the right edge of the calendar must focus to the event list:
        if (position % 7 == 6) holder.itemView.setNextFocusRightId(R.id.event_list);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // getter for the selected position:
    public int getSelectedPosition () { return selectedPosition; }

    // getter for the focused position:
    public int getFocusedPosition () {

        int i;

        for (i = 0; i < viewList.size(); i++) {
            if (viewList.get(i).itemView.hasFocus())
                break;
        }

        return i;
    }

    // getter for the selected month:
    public int getSelectedMonth () { return selectedMonth; }

    // getter for the selected year:
    public int getSelectedYear () { return selectedYear; }

    // getter for the selected item's view:
    public ViewHolder getSelectedView() { return viewList.get(selectedPosition); }

    // the selected item gets the focus:
    public void focusSelectedItem() {
        ViewHolder holder = viewList.get(selectedPosition);
        holder.itemView.requestFocus();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;
        public ImageView icon;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.item_text);
            icon = itemView.findViewById(R.id.item_icon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public CalendarGridItem getItem(int position) {
        return itemList.get(position);
    }

    // allows clicks events to be caught
    private void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}