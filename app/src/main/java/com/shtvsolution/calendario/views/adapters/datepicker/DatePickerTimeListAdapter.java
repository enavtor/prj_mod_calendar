package com.shtvsolution.calendario.views.adapters.datepicker;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shtvsolution.R;
import com.shtvsolution.calendario.utils.DateUtils;

import java.util.ArrayList;

//Adapter for the modification of an event's month declaration
//@author Eduardo on 13/04/2018.

public class DatePickerTimeListAdapter extends RecyclerView.Adapter<DatePickerTimeListAdapter.ViewHolder> {

    private ArrayList<Integer> arrayList;

    private LayoutInflater mInflater;

    private int currentCentralPosition;

    private int numberOfElements;

    private Context context;

    // data is passed into the constructor
    public DatePickerTimeListAdapter(Context context, int currentCentralPosition, int numberOfElements) {
        this.mInflater = LayoutInflater.from(context);
        this.numberOfElements = numberOfElements;
        this.currentCentralPosition = currentCentralPosition;
        this.context = context;
        initArray();
    }

    // inflates the cell layout from xml when needed
    @Override
    public DatePickerTimeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_time_list, parent, false);
        return new DatePickerTimeListAdapter.ViewHolder(view);
    }

    // binds the data to the text view in each cell
    @Override
    public void onBindViewHolder(DatePickerTimeListAdapter.ViewHolder holder, int position) {

        Resources res = context.getResources();

        if (arrayList.get(position) != -1) {
            holder.text.setText(DateUtils.formatTimeString(arrayList.get(position)));
            holder.text.setTextColor(res.getColor(R.color.grey_medium));
            if (position == currentCentralPosition) {
                currentCentralPosition = -1;
                holder.centeredHolder();
            }
        }
        else holder.text.setText("");
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // convenience method for getting data at click position
    public Integer getItem(int id) {
        return arrayList.get(id);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView text;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.item_time_list_text);

            itemView.setOnClickListener(this);
        }

        public int getItemValue (){
            return Integer.parseInt(text.getText().toString());
        }

        public void centeredHolder() {
            text.setTextColor(context.getResources().getColor(R.color.black));
            text.setTypeface(Typeface.DEFAULT_BOLD);
        }

        public void notCenteredHolder() {
            text.setTextColor(context.getResources().getColor(R.color.grey_medium));
            text.setTypeface(Typeface.DEFAULT);
        }

        @Override
        public void onClick(View view) {}
    }

    //Initialize month items array:
    private void initArray() {

        arrayList = new ArrayList<>();

        for (int i = 0; i < numberOfElements; i++) {
            if (i == 0 || i == numberOfElements - 1) arrayList.add(-1);
            else arrayList.add(i - 1);
        }
    }
}