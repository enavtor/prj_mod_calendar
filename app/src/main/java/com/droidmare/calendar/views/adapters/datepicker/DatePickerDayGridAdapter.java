package com.droidmare.calendar.views.adapters.datepicker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.utils.DateUtils;

import java.util.ArrayList;

//Date picker dialog calendar's day grid adapter declaration
//@author Eduardo on 30/05/2018.

public class DatePickerDayGridAdapter extends RecyclerView.Adapter<DatePickerDayGridAdapter.ViewHolder> {

    private ArrayList<String> arrayList;

    private LayoutInflater mInflater;

    // data is passed into the constructor
    public DatePickerDayGridAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        arrayList = new ArrayList<>();
        initArray();
    }

    // inflates the cell layout from xml when needed
    @Override
    public DatePickerDayGridAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_calendar_grid_date_picker, parent, false);
        return new DatePickerDayGridAdapter.ViewHolder(view);
    }

    // binds the data to the text view in each cell
    @Override
    public void onBindViewHolder(DatePickerDayGridAdapter.ViewHolder holder, int position) {

        String dayText = arrayList.get(position);

        holder.text.setText(dayText);

        holder.itemView.setFocusable(false);
        holder.itemView.setClickable(false);
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return arrayList.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.calendar_item_text);
        }
    }

    //Initialize calendar items array:
    private void initArray() {
        for (int i = 0; i < 7; i++)
            arrayList.add(DateUtils.getDayOfWeekText(i).substring(0, 2));
    }
}