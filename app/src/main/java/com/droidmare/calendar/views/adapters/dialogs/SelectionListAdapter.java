package com.droidmare.calendar.views.adapters.dialogs;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.views.activities.DialogEventParameters;

import java.util.ArrayList;

//Event parameters selection lists adapter declaration
//@author Eduardo on 27/09/2018.

public class SelectionListAdapter extends RecyclerView.Adapter<SelectionListAdapter.ViewHolder> {

    public static int TYPE_HOUR = 0;
    public static int TYPE_MINUTE = 1;

    private ArrayList<String> elementsList;

    private int currentElementsType;

    private LayoutInflater mInflater;

    private int focusedItem;

    private Context context;

    private DialogEventParameters parentActivity;

    // data is passed into the constructor
    public SelectionListAdapter(Context context, int elementsType, ArrayList<String> inputList) {
        this.mInflater = LayoutInflater.from(context);
        currentElementsType = elementsType;
        focusedItem = -1;
        elementsList = inputList;
        this.context = context;
        parentActivity = ((DialogEventParameters)context);
    }

    // inflates the cell layout from xml when needed
    @Override
    public SelectionListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_selection_list, parent, false);
        return new SelectionListAdapter.ViewHolder(view);
    }

    // binds the data to the text view in each cell
    @Override
    public void onBindViewHolder(SelectionListAdapter.ViewHolder holder, int position) {

        if (!elementsList.get(position).equals("")) {
            holder.setFocusLayout();
            holder.text.setText(elementsList.get(position));
            holder.setFocusChangeListener(position);
            if (position == focusedItem) {
                focusedItem = -1;
                holder.centeredHolder();
            }
            else holder.text.setTextColor(holder.res.getColor(R.color.grey_medium_lighter));

            if (currentElementsType == TYPE_HOUR)
                holder.itemView.setNextFocusLeftId(R.id.prev_step_button);

            else if (currentElementsType == TYPE_MINUTE)
                holder.itemView.setNextFocusRightId(R.id.next_step_button);
        }
        else holder.text.setText("");
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return elementsList.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        Resources res = context.getResources();

        public TextView text;
        private RelativeLayout focus;

        ViewHolder(View itemView) {

            super(itemView);

            text = itemView.findViewById(R.id.item_selection_list_text);

            itemView.setOnClickListener(this);
        }

        void setFocusChangeListener(final int position) {
            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {
                    if (hasFocus) {

                        focus.setVisibility(View.VISIBLE);

                        if (position == 1) {
                            changeDownArrowVisibility(View.VISIBLE);
                            changeUpArrowVisibility(View.GONE);
                        }

                        else if (position == getItemCount() - 2) {
                            changeUpArrowVisibility(View.VISIBLE);
                            changeDownArrowVisibility(View.GONE);
                        }

                        else {
                            changeUpArrowVisibility(View.VISIBLE);
                            changeDownArrowVisibility(View.VISIBLE);
                        }
                    }

                    else {
                        focus.setVisibility(View.INVISIBLE);
                        changeUpArrowVisibility(View.GONE);
                        changeDownArrowVisibility(View.GONE);
                    }
                }
            });
        }

        private void setFocusLayout() {
            if (currentElementsType == TYPE_HOUR) focus = parentActivity.findViewById(R.id.hour_selection_focus);

            else if (currentElementsType == TYPE_MINUTE) focus = parentActivity.findViewById(R.id.minute_selection_focus);
        }

        private void changeUpArrowVisibility(int visibility) {

            RelativeLayout upArrow = null;

            if (currentElementsType == TYPE_HOUR) upArrow = parentActivity.findViewById(R.id.hours_list_up);

            else if (currentElementsType == TYPE_MINUTE) upArrow = parentActivity.findViewById(R.id.minutes_list_up);

            if (upArrow != null) upArrow.setVisibility(visibility);
        }

        private void changeDownArrowVisibility(int visibility) {

            RelativeLayout downArrow = null;

            if (currentElementsType == TYPE_HOUR) downArrow = parentActivity.findViewById(R.id.hours_list_down);

            else if (currentElementsType == TYPE_MINUTE) downArrow = parentActivity.findViewById(R.id.minutes_list_down);

            if (downArrow != null) downArrow.setVisibility(visibility);
        }

        public int getItemValue (){
            return Integer.parseInt(text.getText().toString());
        }

        public void centeredHolder() {
            text.setTextColor(res.getColor(R.color.black));
            text.setTypeface(Typeface.DEFAULT_BOLD);
            itemView.setScaleX(1.5f);
            itemView.setScaleY(1.5f);
        }

        public void notCenteredHolder() {
            text.setTextColor(res.getColor(R.color.grey_medium_lighter));
            text.setTypeface(Typeface.DEFAULT);
            itemView.setScaleX(1f);
            itemView.setScaleY(1f);
        }

        @Override
        public void onClick(View view) {}
    }
}