package com.droidmare.calendar.views.adapters.events;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.common.utils.DateUtils;

import java.util.ArrayList;

//Calendar grid adapter for editing the repetition stop date declaration
//@author Eduardo on 01/10/2018.

public class EventCalendarAdapter extends RecyclerView.Adapter<EventCalendarAdapter.ViewHolder> {

    private ArrayList<Integer> itemList;

    private ArrayList<ViewHolder> viewList;

    private LayoutInflater mInflater;

    private ItemClickListener mClickListener;

    private final Resources res;

    private int currentMonth;
    private int currentYear;

    private int selectedPosition;

    private int[] todayDateArray;

    private int todayDayPosition;

    // data is passed into the constructor
    public EventCalendarAdapter(Context context, int day, int month, int year, ItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.res = context.getResources();
        selectedPosition = -1;
        currentMonth = month;
        currentYear = year;
        viewList = new ArrayList<>();
        updateItemList();
        selectedPosition = itemList.indexOf(day);
        todayDateArray = DateUtils.transformFromMillis(DateUtils.getTodayMillis());
        setClickListener(listener);
    }

    // updates the adapter items based on the given parameters:
    public void updateAdapterItems(int month, int year) {

        currentMonth = month;
        currentYear = year;

        updateItemList();

        for (int i = 0; i < viewList.size(); i++) {

            Integer item = itemList.get(i);
            ViewHolder holder = viewList.get(i);

            if (item != null){
                holder.text.setText(item.toString());
                holder.itemView.setFocusable(true);

                if (currentMonth == todayDateArray[DateUtils.MONTH] && currentYear == todayDateArray[DateUtils.YEAR]) {
                    if (item == todayDateArray[DateUtils.DAY])
                        holder.text.setBackground(res.getDrawable(R.drawable.today_background_frame));
                }

                else if (i == todayDayPosition)
                    holder.text.setBackground(res.getDrawable(R.drawable.parameters_grid_item_divider));

                if (i == selectedPosition) {
                    holder.text.setTextColor(res.getColor(R.color.white));
                    holder.itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected));
                }
                else {
                    holder.text.setTextColor(res.getColor(R.color.black));
                    holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
                }
            }
            else {
                holder.text.setText(null);
                holder.text.setBackground(res.getDrawable(R.drawable.parameters_grid_item_divider));
                holder.itemView.setFocusable(false);
                holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
            }
        }
    }

    // method that selects a new day inside the calendar view:
    public void selectNewDay(int day) {

        int newSelectedPosition = itemList.indexOf(day);

        viewList.get(selectedPosition).text.setTextColor(res.getColor(R.color.black));
        viewList.get(selectedPosition).itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));

        viewList.get(newSelectedPosition).text.setTextColor(res.getColor(R.color.white));
        viewList.get(newSelectedPosition).itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected));

        selectedPosition = newSelectedPosition;
    }

    // method that focuses a specific view based on the parameter position:
    public void focusView(int position) {

        viewList.get(position).itemView.requestFocus();
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_event_calendar_grid, parent, false);
        ViewHolder holder = new ViewHolder(view);
        viewList.add(holder);
        return holder;
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        Integer day = itemList.get(position);

        if (day != null){
            holder.text.setText(day.toString());
            holder.itemView.setFocusable(true);

            if (currentMonth == todayDateArray[DateUtils.MONTH] && currentYear == todayDateArray[DateUtils.YEAR]) {
                if (day == todayDateArray[DateUtils.DAY]) {
                    todayDayPosition = position;
                    holder.text.setBackground(res.getDrawable(R.drawable.today_background_frame));
                }
            }

            if (position == selectedPosition) {
                holder.text.setTextColor(res.getColor(R.color.white));
                holder.itemView.setBackground(res.getDrawable(R.drawable.grid_item_selected));
            }
            else {
                holder.text.setTextColor(res.getColor(R.color.black));
                holder.itemView.setBackground(res.getDrawable(R.drawable.calendar_focus));
            }
        }
        else {
            holder.text.setText(null);
            holder.itemView.setFocusable(false);
        }

        if (position % 7 == 0) holder.itemView.setNextFocusLeftId(R.id.prev_step_button);
        else if (position % 7 == 6) holder.itemView.setNextFocusRightId(R.id.next_step_button);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.set_date_item_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public int getItem(int id) {
        return itemList.get(id);
    }

    // returns the selected item:
    public int getSelectedItem() { return itemList.get(selectedPosition); }

    // allows clicks events to be caught
    private void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private void updateItemList() {

        int monthStartDay;
        int selectedDay = -1;

        Integer item;
        Integer day;

        //The selected day's value (if there is a selected day) is stored so that the selectedPosition variable can be appropriately updated:
        if (selectedPosition != -1) selectedDay = itemList.get(selectedPosition);

        monthStartDay = DateUtils.findDayOfWeek(currentMonth, currentYear);
        int numberOfDays = DateUtils.numberOfDays(currentMonth, currentYear);

        itemList = new ArrayList<>();

        for(int i = 1; i <= (42); i++){

            day = i - monthStartDay + 1;

            if (i < monthStartDay) item = null;

            else if (day > numberOfDays)  {
                item = null;
            }

            else item = day;

            itemList.add(item);
        }

        if (selectedPosition != -1) {
            //If the selected day's value is bigger than the total number of days, it must be changed for the value of numberOfDays:
            if (selectedDay > numberOfDays) selectedPosition = itemList.indexOf(numberOfDays);

            //If it is not, te selected position attribute must be updated according to the variable selectedDay:
            else selectedPosition = itemList.indexOf(selectedDay);
        }
    }
}