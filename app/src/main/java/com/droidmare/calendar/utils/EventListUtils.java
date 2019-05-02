package com.droidmare.calendar.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import com.droidmare.calendar.views.adapters.events.EventListAdapter;

//Utils for creating and configuring an event list recycler view (Focusing on avoiding the over span of the title and description text views)
//@author Eduardo on 11/04/2019.

@SuppressLint("StaticFieldLeak")
public class EventListUtils {

    private static Context givenContext;

    //This method creates a recycler view for an event list and assigns a layout manager, a global layout listener and a scroll listener to manage
    //the description and title lengths, shortening them if they occupy more than a line (for this functionality it is necessary to know the final
    //width that the text views will have, reason why those operations are performed inside the listeners and not in the onBindViewHolder method):
    public static RecyclerView createEventList (Context context, RecyclerView view) {

        givenContext = context;

        final RecyclerView eventList = view;

        final LinearLayoutManager manager = new LinearLayoutManager(context);

        //First of all, the recycler view must be configured:
        eventList.setFocusable(false);
        eventList.setLayoutManager(manager);
        eventList.getRecycledViewPool().setMaxRecycledViews(0,0);

        //Whenever the recycler view is updated, all the initial visible elements' title and description must be set:
        eventList.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {

                int first = manager.findFirstVisibleItemPosition();
                int last = manager.findLastVisibleItemPosition();

                if (first != -1) for (int i = first; i <= last; i++) {
                    EventListAdapter.ViewHolder holder = (EventListAdapter.ViewHolder) eventList.findViewHolderForAdapterPosition(i);
                    holder.setTitleAndDescription(i);
                }
            }
        });

        //Each time the list is scrolled, the new visible element's title and description must be set:
        eventList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int i = -1;

                //If the list is scrolled downwards the new visible element will be the first one:
                if (dy < 0) i = manager.findFirstVisibleItemPosition();
                //On the other hand, if the list is scrolled upwards the new visible element will be the last one:
                else if (dy > 0) i = manager.findLastVisibleItemPosition();

                //Now the texts can be shortened and set:
                if (i != -1) {
                    EventListAdapter.ViewHolder holder = (EventListAdapter.ViewHolder) eventList.findViewHolderForAdapterPosition(i);
                    holder.setTitleAndDescription(i);
                }
            }
        });

        return eventList;
    }

    //The following method shortens the description to ensure it only occupies a line:
    public static String oneLineText(TextView textView, String text) {
        if (occupiesMoreThanOneLine(textView, text)) {

            String ellipsisChar = "...";

            String splitText = text = text.split("\n")[0];
            String auxText = text;

            int maxLength = 1;

            for (; maxLength <= auxText.length(); maxLength++) {
                if (!occupiesMoreThanOneLine(textView, auxText.substring(0, maxLength)))
                    text = auxText.substring(0, maxLength);
                else break;
            }

            int textLength = text.length();

            if (!splitText.equals(text) && text.length() > 4) {
                auxText = text.substring(textLength - 4, textLength);

                if (auxText.substring(0, 1).equals(" "))
                    textLength = textLength - 4;

                else textLength = textLength - 3;
            }

            text = text.substring(0, textLength) + ellipsisChar;
        }
        return text;
    }

    //This method checks if a text occupies more than the specified number of lines:
    private static boolean occupiesMoreThanOneLine(TextView textView, String text) {
        double textWidth = textView.getPaint().measureText(text);
        return ((text.split("\n").length > 1 || textWidth > textView.getMeasuredWidth()) && textView.getMeasuredWidth() != 0);
    }

    //Method that transforms a dp value into pixels:
    public static int transformDipToPix (int dpValue) {
        Resources r = givenContext.getResources();
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics());
    }
}
