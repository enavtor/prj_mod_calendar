package com.shtvsolution.calendario.views.adapters.dialogs;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shtvsolution.R;

import java.util.ArrayList;

//List adapter declaration for the previous alarms parameters lists:
//@author Eduardo on 19/03/2019.

public class PrevAlarmsListsAdapter extends RecyclerView.Adapter<PrevAlarmsListsAdapter.ViewHolder> {

    private ArrayList<String> arrayList;

    private LayoutInflater mInflater;

    private PrevAlarmsListsAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public PrevAlarmsListsAdapter(Context context, PrevAlarmsListsAdapter.ItemClickListener listener, ArrayList<String> dataList) {
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = dataList;
        setClickListener(listener);
    }

    // inflates the cell layout from xml when needed
    @Override
    public PrevAlarmsListsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_prev_alarm_params, parent, false);
        return new PrevAlarmsListsAdapter.ViewHolder(view);
    }

    // updates the data inside the recycler view:
    public void updateData(ArrayList<String> dataList) {
        arrayList = dataList;
        notifyDataSetChanged();
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(PrevAlarmsListsAdapter.ViewHolder holder, int position) {

        holder.text.setText(arrayList.get(position));
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.prev_alarm_param_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    private void setClickListener(PrevAlarmsListsAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
