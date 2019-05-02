package com.droidmare.calendar.views.adapters.events;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.models.EventListItem;
import com.droidmare.calendar.utils.DateUtils;
import com.droidmare.calendar.utils.EventListUtils;
import com.droidmare.calendar.views.activities.MainActivity;

import java.util.ArrayList;

//Event list adapter declaration
//@author Eduardo on 13/02/2018.

public class EventListAdapter extends RecyclerView.Adapter<EventListAdapter.ViewHolder> {

    private Context context;

    private ArrayList<EventListItem> arrayList;

    private int selectedPosition;

    private int nextFocusedPosition;

    private int focusedViewPosition;

    private boolean displayingEventMenu;

    private boolean eventMenuDismissed;

    private boolean allEventsDisplayed;

    private LayoutInflater mInflater;

    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public EventListAdapter(Context context, ArrayList<EventListItem> eventList, ItemClickListener listener, boolean allEventsDisplayed) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.arrayList = eventList;
        this.eventMenuDismissed = false;
        this.allEventsDisplayed = allEventsDisplayed;
        this.nextFocusedPosition = -1;
        this.focusedViewPosition = -1;
        setClickListener(listener);
    }

    // the adapter items list is updated:
    public void updateAdapterItems(ArrayList<EventListItem> eventList){
        arrayList = eventList;
    }

    // the position of the item on which the click was performed:
    public void setSelectedPosition(int selected){
        selectedPosition = selected;
    }

    public int getSelectedPosition() { return selectedPosition; }

    // the position of the next item that will get the focus:
    public void setNextFocusedPosition(int nextFocus){
        this.nextFocusedPosition = nextFocus;
    }

    public int getNextFocusedPosition() { return nextFocusedPosition; }

    // the item on which the click was performed:
    public EventListItem getSelectedItem() { return arrayList.get(selectedPosition); }

    // the event menu can be dismissed under different circumstances:
    public void dismissEventMenu(boolean updatedEvent) {
        //When an event is modified being the only event on the list, the list will be empty at this point:
        if (arrayList.size() != 0 && arrayList.size() > selectedPosition)
            arrayList.get(selectedPosition).showEventMenu(false);

        eventMenuDismissed = true;
        displayingEventMenu = false;

        //When the menu is dismissed (and not for an update operation) the selected event will be updated (in order to hide the menu):
        if (!updatedEvent) notifyItemChanged(selectedPosition);
    }

    // the last selected menu button may be needed when a touch event happens:
    public boolean displayingEventMenu() {
        return displayingEventMenu;
    }

    // inflates the cell layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_event_list, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the view in each cell
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        EventListItem eventItem = arrayList.get(position);

        Drawable icon = eventItem.getEventIcon();

        final int itemPosition = position;

        holder.eventItemBody.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) focusedViewPosition = itemPosition;
                else focusedViewPosition = -1;
            }
        });

        //If the event is out of date, the title color will be red:
        if (eventItem.outOfDate(allEventsDisplayed)) {
            int color = context.getResources().getColor(R.color.sh_red_medium);
            holder.eventTitle.setTextColor(color);
        } else {
            int color = context.getResources().getColor(R.color.black);
            holder.eventTitle.setTextColor(color);
        }

        holder.eventIcon.setImageDrawable(icon);

        int interval = eventItem.getIntervalTime();

        //The interval icon will be shown only if there is a repetition interval for the event:
        if (interval != 0) {
            holder.repetitionIconHolder.setVisibility(View.VISIBLE);
            holder.intervalTime.setText(DateUtils.formatTimeString(interval));
        } else holder.repetitionIconHolder.setVisibility(View.GONE);

        //When the element is the first one on the list, a top margin of 3dp will be left:
        if (position == 0) holder.firstElementMargin.setVisibility(View.VISIBLE);
        else holder.firstElementMargin.setVisibility(View.GONE);

        //The visibility of the elements depends on the value of the attribute showEventMenu:
        holder.changeElementsVisibility(eventItem.showEventMenu());

        //If the element must get the focus, the focus is relocated:
        if (position == nextFocusedPosition) {
            nextFocusedPosition = -1;
            holder.itemView.requestFocus();
        }
    }

    // total number of cells
    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    // current focused item:
    public int getCurrentFocusedPosition() { return focusedViewPosition; }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        LinearLayout eventItemMenu, viewEventButton, modifyEventButton, deleteEventButton, dismissMenuButton;
        RelativeLayout eventItemBody, firstElementMargin, eventElementsHolder, repetitionIconHolder;
        TextView eventTitle, eventDescription, intervalTime;
        ImageView eventIcon;

        ViewHolder(final View itemView) {

            super(itemView);

            //The first element must have an extra top margin of 3dp:
            firstElementMargin = itemView.findViewById(R.id.first_element_margin);

            //Because of this, the items are not fully focused but only the event_item_body layout, so when that layout is clicked, a click emulation on the whole item's view must be performed:
            eventItemBody = itemView.findViewById(R.id.event_item_body);
            eventItemBody.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    itemView.performClick();
                }
            });

            //Since when the event menu must be shown, the event elements must set invisible, there exists an event elements holder in order to achieve this:
            eventElementsHolder = itemView.findViewById(R.id.event_elements_holder);

            //An overlapping menu of three buttons (for deleting the event, modifying it or dismissing the menu) is hidden within the event layout:
            eventItemMenu = itemView.findViewById(R.id.event_item_menu);

            viewEventButton = itemView.findViewById(R.id.view_event_button);
            modifyEventButton = itemView.findViewById(R.id.modify_event_button);
            deleteEventButton = itemView.findViewById(R.id.delete_event_button);
            dismissMenuButton = itemView.findViewById(R.id.dismiss_menu_button);

            setEventMenuBehaviour();

            //Now the rest of elements can be initialized:
            eventTitle = itemView.findViewById(R.id.item_title);
            eventDescription = itemView.findViewById(R.id.item_description);
            intervalTime = itemView.findViewById(R.id.repetition_interval);
            repetitionIconHolder = itemView.findViewById(R.id.repetition_icon_holder);

            eventIcon = itemView.findViewById(R.id.event_icon);

            itemView.setOnClickListener(this);
        }

        public RelativeLayout getEventItemBody() { return this.eventItemBody; }

        // sets the behaviour of the event menu:
        private void setEventMenuBehaviour(){

            viewEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    EventListItem eventToDisplay = arrayList.get(selectedPosition);
                    ((MainActivity)context).startModifyEventDialog(eventToDisplay, true);
                }
            });

            modifyEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventListItem eventToModify = arrayList.get(selectedPosition);

                    //If the event is a copy of an event with repetition, the event that must be modified is the original one, since the copy doesn't exist in the database:
                    if (eventToModify.getOriginalEvent() != null)
                        eventToModify = eventToModify.getOriginalEvent();

                    ((MainActivity)context).startModifyEventDialog(eventToModify, false);
                }
            });

            deleteEventButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventListItem eventToDelete = arrayList.get(selectedPosition);

                    ((MainActivity)context).startDisplayDeleteAllEventsDialog(eventToDelete);
                }
            });

            dismissMenuButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismissEventMenu(false);
                }
            });
        }

        // changes the visibility of the holder's elements (based on the current visibility of its icon):
        void changeElementsVisibility(boolean showEventMenu){

            if(showEventMenu) {
                displayingEventMenu = true;

                this.eventElementsHolder.setVisibility(View.INVISIBLE);

                //The event description text is set to a blank string to avoid the event container over span:
                this.eventDescription.setText("");

                this.eventItemMenu.setVisibility(View.VISIBLE);
                this.viewEventButton.requestFocusFromTouch();
            }

            else {
                this.eventItemMenu.setVisibility(View.GONE);
                this.eventElementsHolder.setVisibility(View.VISIBLE);

                //If the event menu was dismissed, the focus stays on the event:
                if (eventMenuDismissed) {
                    eventMenuDismissed = false;
                    if (nextFocusedPosition != -2) eventItemBody.requestFocus();
                    else nextFocusedPosition = -1;
                }
            }
        }

        public void setTitleAndDescription(int position) {
            EventListItem event = arrayList.get(position);

            String title;

            if (allEventsDisplayed) title = event.getFullTitleText();
            else title = event.getTitleText();

            eventTitle.setText(EventListUtils.oneLineText(eventTitle, title));
            eventDescription.setText(EventListUtils.oneLineText(eventDescription, event.getDescriptionText()));
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public EventListItem getItem(int id) {
        return arrayList.get(id);
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

