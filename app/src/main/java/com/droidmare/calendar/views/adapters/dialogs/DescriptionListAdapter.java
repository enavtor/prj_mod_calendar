package com.droidmare.calendar.views.adapters.dialogs;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.droidmare.R;

//List adapter declaration for the default descriptions list:
//@author Eduardo on 05/04/2019.

public class DescriptionListAdapter extends RecyclerView.Adapter<DescriptionListAdapter.ViewHolder> {

    private String[] descriptionsList;

    private boolean[] selectedList;

    private boolean firstLoad;

    private LayoutInflater mInflater;

    private Resources res;

    private DescriptionListAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public DescriptionListAdapter(Context context, DescriptionListAdapter.ItemClickListener listener,String description) {
        this.mInflater = LayoutInflater.from(context);
        res = context.getResources();
        this.descriptionsList = description.split("\n");
        selectedList = new boolean[descriptionsList.length];
        firstLoad = true;
        setClickListener(listener);
    }

    // inflates the cell layout from xml when needed
    @Override
    public DescriptionListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_description_list, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(DescriptionListAdapter.ViewHolder holder, int position) {

        holder.text.setText(descriptionsList[position]);

        //The first element top margin is removed:
        if (position == 0) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(params.leftMargin , 0 , params.rightMargin , params.bottomMargin);
            if (firstLoad) {
                firstLoad = false;
                holder.itemView.requestFocus();
            }
        }

        if (isSelected(position)) holder.itemView.setBackground(res.getDrawable(R.drawable.text_item_selected_background));
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return descriptionsList.length;
    }

    public String getDescription(int position) {
        return descriptionsList[position];
    }

    public boolean isSelected(int position) { return selectedList[position]; }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.default_description_text);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }

        public void select(int position) {
            if (selectedList[position]) {
                selectedList[position] = false;
                itemView.setBackground(res.getDrawable(R.drawable.text_item_background));
            }
            else {
                selectedList[position] = true;
                itemView.setBackground(res.getDrawable(R.drawable.text_item_selected_background));
            }
        }
    }

    // allows clicks events to be caught
    private void setClickListener(DescriptionListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
