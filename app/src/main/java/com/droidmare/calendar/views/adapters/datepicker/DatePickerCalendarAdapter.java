package com.droidmare.calendar.views.adapters.datepicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.utils.DateUtils;

import java.util.ArrayList;

//Calendar grid adapter for editing the previous alarms
//@author Eduardo on 22/03/2019.

public class DatePickerCalendarAdapter extends RecyclerView.Adapter<DatePickerCalendarAdapter.ViewHolder> {

    private final int DEFAULT_DAY_SIZE_SP = 17;
    private final int SELECTED_DAY_SIZE_SP = 19;

    private ArrayList<Integer> itemList;

    private ArrayList<ViewHolder> viewList;

    private LayoutInflater mInflater;

    private ItemClickListener mClickListener;

    private final Resources res;

    private int selectedPosition;

    private int[] todayDateArray;

    private int todayDayPosition;

    private int[] startDateArray;

    private int startDayPosition;

    private int currentMonth;

    private int currentYear;

    private int numberOfDays;

    private int numberOfEmptyBottomRows;

    // data is passed into the constructor
    public DatePickerCalendarAdapter(Context context, int day, int month, int year, ItemClickListener listener) {
        this.mInflater = LayoutInflater.from(context);
        this.res = context.getResources();
        selectedPosition = -1;
        viewList = new ArrayList<>();
        startDateArray = new int[] {-1, -1, day, month, year};
        updateItemList(month, year);
        selectedPosition = itemList.indexOf(day);
        todayDateArray = DateUtils.transformFromMillis(DateUtils.getTodayMillis());
        setClickListener(listener);
    }

    // updates the adapter items based on the given parameters:
    public void updateAdapterItems(int month, int year) {

        updateItemList(month, year);

        for (int i = 0; i < viewList.size(); i++) {

            Integer item = itemList.get(i);
            ViewHolder holder = viewList.get(i);

            if (item != null){
                holder.text.setText(item.toString());
                holder.text.setFocusable(true);

                if (currentMonth == todayDateArray[DateUtils.MONTH] && currentYear == todayDateArray[DateUtils.YEAR]) {
                    if (item == todayDateArray[DateUtils.DAY])
                        holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_today));
                }

                else if (i == todayDayPosition)
                    holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_calendar));

                if (currentMonth == startDateArray[DateUtils.MONTH] && currentYear == startDateArray[DateUtils.YEAR]) {
                    if (todayDayPosition == startDayPosition && item == todayDateArray[DateUtils.DAY])  {
                        holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_double));
                    }
                    else if (item == startDateArray[DateUtils.DAY])
                        holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_start));
                }

                else if (i == startDayPosition)
                    holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_calendar));

                if (i == selectedPosition) {
                    holder.text.setTextColor(res.getColor(R.color.black));
                    holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SELECTED_DAY_SIZE_SP);
                    holder.text.setTypeface(Typeface.DEFAULT_BOLD);
                }

                else {
                    holder.text.setTextColor(res.getColor(R.color.grey_medium));
                    holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_DAY_SIZE_SP);
                    holder.text.setTypeface(Typeface.DEFAULT);
                }
            }

            else {
                holder.text.setText(null);
                holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_calendar));
                holder.text.setFocusable(false);
            }
        }
    }

    // method that selects a new day inside the calendar view:
    public void selectNewDay(int day) {

        int newSelectedPosition = itemList.indexOf(day);

        viewList.get(selectedPosition).text.setTextColor(res.getColor(R.color.grey_medium));
        viewList.get(selectedPosition).text.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_DAY_SIZE_SP);
        viewList.get(selectedPosition).text.setTypeface(Typeface.DEFAULT);

        viewList.get(newSelectedPosition).text.setTextColor(res.getColor(R.color.black));
        viewList.get(newSelectedPosition).text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SELECTED_DAY_SIZE_SP);
        viewList.get(newSelectedPosition).text.setTypeface(Typeface.DEFAULT_BOLD);

        selectedPosition = newSelectedPosition;
    }

    // method that focuses a specific view based on the parameter position:
    public void focusView(int position) {

        viewList.get(position).text.requestFocus();
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_calendar_grid_date_picker, parent, false);
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
            holder.text.setFocusable(true);

            if (currentMonth == todayDateArray[DateUtils.MONTH] && currentYear == todayDateArray[DateUtils.YEAR]) {
                if (day == todayDateArray[DateUtils.DAY]) {
                    todayDayPosition = position;
                    holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_today));
                }
            }

            if (currentMonth == startDateArray[DateUtils.MONTH] && currentYear == startDateArray[DateUtils.YEAR]) {
                if (day == startDateArray[DateUtils.DAY]) {
                    startDayPosition = position;
                    holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_start));
                }
            }

            if (todayDayPosition == startDayPosition && day == todayDateArray[DateUtils.DAY])  {
                holder.text.setBackground(res.getDrawable(R.drawable.date_picker_focus_double));
            }

            if (position == selectedPosition) {
                holder.text.setTextColor(res.getColor(R.color.black));
                holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, SELECTED_DAY_SIZE_SP);
                holder.text.setTypeface(Typeface.DEFAULT_BOLD);
                holder.itemView.requestFocus();
            }

            else {
                holder.text.setTextColor(res.getColor(R.color.grey_medium));
                holder.text.setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_DAY_SIZE_SP);
                holder.text.setTypeface(Typeface.DEFAULT);
            }
        }

        else {
            holder.text.setText(null);
            holder.text.setFocusable(false);
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return itemList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;
        public ImageView icon;

        ViewHolder(final View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.calendar_item_text);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemView.performClick();
                }
            });

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

    // returns the position of the given view:
    public int returnViewPosition(View view){
        for (int i = 0; i < viewList.size(); i++)
            if (viewList.get(i).text.equals(view)) return i;

        return -1;
    }

    // returns the selected item:
    public int getSelectedItem() { return itemList.get(selectedPosition); }

    // returns the position of the last day:
    public int getLastDayPosition() { return itemList.indexOf(numberOfDays); }

    // returns the number of bottom empty rows:
    public int getNumberOfEmptyBottomRows() { return numberOfEmptyBottomRows; }

    // allows clicks events to be caught
    private void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    private void updateItemList(int month, int year) {

        int monthStartDay;
        int selectedDay = -1;

        currentMonth =  month;
        currentYear = year;

        Integer item;
        Integer day;

        //The selected day's value (if there is a selected day) is stored so that the selectedPosition variable can be appropriately updated:
        if (selectedPosition != -1) selectedDay = itemList.get(selectedPosition);

        monthStartDay = DateUtils.findDayOfWeek(month, year);
        numberOfDays = DateUtils.numberOfDays(month, year);

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

        //It is necessary to know many bottom rows are completely empty in order to set a correct focus behaviour:
        int lastDayPosition = itemList.indexOf(numberOfDays);

        if (lastDayPosition >= 28 && lastDayPosition <= 34) numberOfEmptyBottomRows = 1;
        else if (lastDayPosition >= 35 && lastDayPosition <= 41) numberOfEmptyBottomRows = 0;
        else numberOfEmptyBottomRows = 2;
    }
}