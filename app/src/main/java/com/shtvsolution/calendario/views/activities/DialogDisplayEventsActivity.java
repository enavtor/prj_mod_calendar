package com.shtvsolution.calendario.views.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.shtvsolution.R;
import com.shtvsolution.calendario.models.EventListItem;
import com.shtvsolution.calendario.utils.DateUtils;
import com.shtvsolution.calendario.utils.EventListUtils;
import com.shtvsolution.calendario.views.adapters.events.EventListAdapter;
import com.shtvsolution.database.publisher.EventsPublisher;
import com.shtvsolution.estadisticas.StatisticAPI;
import com.shtvsolution.estadisticas.StatisticService;

import java.util.ArrayList;

//Activity for displaying all the events declaration
//@author Eduardo on 31/05/2018.

public class DialogDisplayEventsActivity extends AppCompatActivity {

    private final static String TAG = DialogDisplayEventsActivity.class.getCanonicalName();

    //Adapter for the event list inside the view:
    private EventListAdapter adapter;

    private StatisticService statistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The main activity needs to know that this activity is running in the event that a synchronization takes place, case in which the running activity might have to be finished   :
        MainActivity.setRunningActivityReference(this);

        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        setContentView(R.layout.activity_dialog_display_events);

        TextView loadingEventsTitle = findViewById(R.id.text_loading_events);
        String loadingDialogTitle = getString(R.string.loading_layout_description) + getString(R.string.loading_layout_events);
        loadingEventsTitle.setText(loadingDialogTitle);

        final Context context = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Log.e(TAG, "onCreate. InterruptedException: " + ie.getMessage());
                } finally {
                    EventsPublisher.retrieveAllEvents(context);
                }
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_DESTROY, getClass().getCanonicalName());
    }

    //Touch events are disabled in order to avoid application's misbehaviour:
    @Override
    public boolean dispatchTouchEvent(MotionEvent event){
        return false;
    }

    //This method will be called once all the events have been retrieved:
    public void finishInitialization(ArrayList<EventListItem> allEvents){

        long currentDate = DateUtils.getTodayMillis();

        for (int i = 0; i < allEvents.size(); i++) {
            EventListItem event = allEvents.get(i);
            event.calculateNextRepetition(currentDate);
            long nextRepetition = event.getNextRepetition();
            if (nextRepetition < currentDate && nextRepetition > 0) {
                allEvents.remove(i--);
            }
        }

        //Initialization of the event list:
        final RecyclerView eventList = EventListUtils.createEventList(this, (RecyclerView)findViewById(R.id.all_events_list));

        //Initialization of the listener for handling on items clicks:
        EventListAdapter.ItemClickListener listener = new EventListAdapter.ItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                //When an event is selected the calendar will be refreshed loading its month and year views:
                EventListItem item = adapter.getItem(position);

                int day = item.getEventDay();
                int month = item.getEventMonth();
                int year = item.getEventYear();

                if (item.getIntervalTime() != 0) {
                    int[] nextRepetitionArray = DateUtils.transformFromMillis(item.getNextRepetition());
                    day = nextRepetitionArray[DateUtils.DAY];
                    month = nextRepetitionArray[DateUtils.MONTH];
                    year = nextRepetitionArray[DateUtils.YEAR];
                }

                DateUtils.currentDay = day;
                DateUtils.currentMonth = month;
                DateUtils.currentYear = year;

                //Once the DateUtils values have been updated, the MainActivity is notified:
                Intent resultIntent = new Intent();

                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        };

        //Initialization of the adapter (the first element must be focused, so the adapter is initialized with
        //a null eventList parameter and then updated after specifying that the first element must be focused):
        adapter = new EventListAdapter(this, null, listener, true);
        adapter.setNextFocusedPosition(0);
        adapter.updateAdapterItems(allEvents);

        eventList.setAdapter(adapter);

        findViewById(R.id.loading_events_layout).setVisibility(View.GONE);
    }
}
