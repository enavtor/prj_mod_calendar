package com.droidmare.calendar.views.adapters.dialogs;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.TypeListItem;

import java.util.ArrayList;

//Type list adapter declaration
//@author Eduardo on 26/02/2018.

public class TypeListAdapter extends RecyclerView.Adapter<TypeListAdapter.ViewHolder> {

    private ArrayList<TypeListItem> arrayList;

    private LayoutInflater mInflater;

    private TypeListAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public TypeListAdapter(Context context, TypeListAdapter.ItemClickListener listener, ArrayList<TypeListItem> typeList) {
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = typeList;
        setClickListener(listener);
    }

    // inflates the cell layout from xml when needed
    @Override
    public TypeListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_type_list, parent, false);
        return new TypeListAdapter.ViewHolder(view);
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(TypeListAdapter.ViewHolder holder, int position) {

        TypeListItem item = arrayList.get(position);

        String typeText = item.getTypeTitle();
        Drawable typeIcon = item.getEventIcon();

        holder.text.setText(typeText);
        holder.icon.setImageDrawable(typeIcon);

        //When the survey event is selected, the hole recycler view changes, and the focus must be relocated on the first element:
        if (item.getType().equals(TypeListItem.eventTypes.MOOD))
            holder.itemView.requestFocus();

        //When the personal event is selected, the hole recycler view changes, and the focus must be relocated on the first element:
        else if (item.getType().equals(TypeListItem.eventTypes.PERSONAL))
            holder.itemView.requestFocus();

        //When the back button is pressed while the measure dialog is being shown, the hole recycler view changes, and the focus must be relocated on the first element:
        else if (item.getType().equals(TypeListItem.eventTypes.ACTIVITY))
            holder.itemView.requestFocus();
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public TypeListItem getEventTypeItem (int position) {
        return arrayList.get(position);
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView text;
        public ImageView icon;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.event_type_title);
            icon = itemView.findViewById(R.id.event_type_icon);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // changes the adapter items and reloads the view:
    public void changeElementsVisibility(ArrayList<TypeListItem> newArray){

        arrayList = newArray;

        notifyDataSetChanged();
    }

    // allows clicks events to be caught
    private void setClickListener(TypeListAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

}
