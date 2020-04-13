package com.droidmare.calendar.views.adapters.dialogs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.R;

import java.util.ArrayList;

//List adapter declaration for the repetition weekly and monthly lists:
//@author Eduardo on 26/03/2019.

public class RepTypeListsAdapter extends RecyclerView.Adapter<RepTypeListsAdapter.ViewHolder> {

    private ArrayList<String> arrayList;

    private LayoutInflater mInflater;

    private ItemClickListener mClickListener;

    private int defaultItem;

    // data is passed into the constructor
    public RepTypeListsAdapter(Context context, ItemClickListener listener, ArrayList<String> dataList) {
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = dataList;
        this.defaultItem = -1;
        setClickListener(listener);
    }

    // inflates the cell layout from xml when needed
    @Override @NonNull
    public RepTypeListsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_repetition_type_lists, parent, false);
        return new RepTypeListsAdapter.ViewHolder(view);
    }

    // sets the default item and notifies a data set change:
    public void setDefaultItem(int defaultItem) {
        this.defaultItem = defaultItem;
        this.notifyDataSetChanged();
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(@NonNull final RepTypeListsAdapter.ViewHolder holder, int position) {

        String item = arrayList.get(position);

        if (item != null) {
            holder.text.setText(arrayList.get(position));
            holder.setAsCommonElement();
        }

        else holder.text.setText("");

        if (position == defaultItem) holder.setAsDefaultElement();

        if ((position == 0 && arrayList.size() == 7) || (position == 2 && arrayList.size() != 7))
            holder.itemView.requestFocus();
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public RelativeLayout selectedBackground;
        public TextView text;
        public boolean selected;

        ViewHolder(final View itemView) {

            super(itemView);

            selected = false;

            selectedBackground = itemView.findViewById(R.id.selected_item_background);

            text = itemView.findViewById(R.id.rep_type_item_text);

            itemView.setOnClickListener(this);
        }

        void setAsCommonElement() {
            this.text.setTextColor(Color.BLACK);
            this.text.setTypeface(Typeface.DEFAULT);
            this.selectedBackground.setVisibility(View.INVISIBLE);
            this.itemView.setFocusable(true);
            this.itemView.setClickable(true);
        }

        void setAsDefaultElement() {
            this.text.setTextColor(Color.WHITE);
            this.text.setTypeface(Typeface.DEFAULT_BOLD);
            this.selectedBackground.setVisibility(View.VISIBLE);
            this.itemView.setClickable(false);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
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
