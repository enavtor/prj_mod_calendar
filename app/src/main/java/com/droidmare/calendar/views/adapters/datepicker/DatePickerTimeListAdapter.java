package com.droidmare.calendar.views.adapters.datepicker;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidmare.R;

import java.util.ArrayList;

//Adapter for the modification of an event's month declaration
//@author Eduardo on 13/04/2018.

public class DatePickerTimeListAdapter extends RecyclerView.Adapter<DatePickerTimeListAdapter.ViewHolder> {

    private String[] itemList;

    private LayoutInflater mInflater;

    private Context context;

    private boolean focusCentralItem;

    // data is passed into the constructor
    public DatePickerTimeListAdapter(Context context, String[] items) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.itemList = items;
    }

    // inflates the cell layout from xml when needed
    @Override @NonNull
    public DatePickerTimeListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_time_list, parent, false);
        return new DatePickerTimeListAdapter.ViewHolder(view);
    }

    // binds the data to the text view in each cell
    @Override
    public void onBindViewHolder(@NonNull DatePickerTimeListAdapter.ViewHolder holder, int position) {

        if (!itemList[position].equals("")) {

            holder.text.setText(itemList[position]);

            if (position == 1) holder.centeredHolder();
        }

        else holder.text.setText("");
    }

    // updates the items on the recycler view:
    public void updateItems(String[] newItems) {
        itemList = newItems;
        focusCentralItem = true;
        notifyDataSetChanged();
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return itemList.length;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.item_time_list_text);
        }

        private void centeredHolder() {
            text.setTextColor(context.getResources().getColor(R.color.black));
            text.setTypeface(Typeface.DEFAULT_BOLD);
            if (focusCentralItem) {
                focusCentralItem = false;
                itemView.requestFocus();
            }
        }
    }
}