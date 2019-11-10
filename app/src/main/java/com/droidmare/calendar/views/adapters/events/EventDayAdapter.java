package com.droidmare.calendar.views.adapters.events;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.common.utils.DateUtils;

import java.util.ArrayList;

//Repetition stop calendar day grid adapter declaration
//@author Eduardo on 01/10/2018.

public class EventDayAdapter extends RecyclerView.Adapter<EventDayAdapter.ViewHolder> {

    private ArrayList<String> arrayList;

    private LayoutInflater mInflater;

    // data is passed into the constructor
    public EventDayAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
        arrayList = new ArrayList<>();
        initArray();
    }

    // inflates the cell layout from xml when needed
    @Override
    public EventDayAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_event_day_grid, parent, false);
        return new EventDayAdapter.ViewHolder(view);
    }

    // binds the data to the text view in each cell
    @Override
    public void onBindViewHolder(EventDayAdapter.ViewHolder holder, int position) {

        String dayText = arrayList.get(position);

        holder.text.setText(dayText);
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

            text = itemView.findViewById(R.id.day_text);
        }
    }

    //Initialize calendar items array:
    private void initArray() {
        for (int i = 0; i < 7; i++)
            arrayList.add(DateUtils.getDayOfWeekText(i).substring(0, 2));
    }
}