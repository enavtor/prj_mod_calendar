package com.droidmare.calendar.views.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.droidmare.R;
import com.droidmare.calendar.services.ApiConnectionService;
import com.droidmare.calendar.services.UserDataReceiverService;
import com.droidmare.calendar.views.adapters.dialogs.DescriptionListAdapter;
import com.droidmare.statistics.StatisticAPI;
import com.droidmare.statistics.StatisticService;

//Activity for displaying a dialog with default descriptions for specific events
//@author Eduardo on 05/04/2019.

public class DialogAddDescriptionActivity extends AppCompatActivity {

    private static final String TAG = DialogAddDescriptionActivity.class.getCanonicalName();

    private RelativeLayout loadingDialog;

    private DescriptionListAdapter descriptionListAdapter;

    private String newDescription;

    private StatisticService statistic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DialogEventParameters.setLaunchedActivityReference(this);

        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        setContentView(R.layout.activity_dialog_add_description);

        ApiConnectionService.setAddDescriptionActivityReference(this);

        final String type = getIntent().getStringExtra("type");

        initializeViews(type);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Log.e(TAG, "onCreate. InterruptedException: " + ie.getMessage());
                }
                while(ApiConnectionService.isCurrentlyRunning) {
                    try {
                        Log.d("TESTING", "Connection service is running");
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                        Log.e(TAG, "onCreate. InterruptedException: " + ie.getMessage());
                    }
                }
                ApiConnectionService.isCurrentlyRunning = true;
                startService(new Intent(getApplicationContext(), ApiConnectionService.class).putExtra("operation", type));
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_DESTROY, getClass().getCanonicalName());
    }

    //Initialization of the activity views and buttons:
    private void initializeViews (String type){

        loadingDialog = findViewById(R.id.loading_descriptions_layout);
        TextView loadingDialogText = findViewById(R.id.text_loading);

        TextView title = findViewById(R.id.description_dialog_header_title);

        LinearLayout affirmative = findViewById(R.id.description_dialog_affirmative_button);
        LinearLayout negative = findViewById(R.id.description_dialog_negative_button);

        if (type.equals("MEDICATION")) {
            title.setText(getString(R.string.dialog_add_medicine_title));
            String loadingMedicationText = getString(R.string.loading_layout_description) + getString(R.string.loading_layout_medication);
            loadingDialogText.setText(loadingMedicationText);
        }

        negative.requestFocus();

        //Behaviour of the affirmative option inside the dialog:
        affirmative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendDescription();
            }
        });

        //Behaviour of the negative option inside the dialog:
        negative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setResult(RESULT_CANCELED);
                finish();
            }
        });
    }

    //Method for initializing the descriptions list inside the dialog:
    public void initDialogList(final String descriptions) {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final RecyclerView descriptionList = findViewById(R.id.default_descriptions_list);
                descriptionList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

                DescriptionListAdapter.ItemClickListener listener = new DescriptionListAdapter.ItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        String description = descriptionListAdapter.getDescription(position);

                        DescriptionListAdapter.ViewHolder holder = (DescriptionListAdapter.ViewHolder) descriptionList.findViewHolderForAdapterPosition(position);

                        if (!descriptionListAdapter.isSelected(position)) {
                            if (newDescription == null) newDescription = description;
                            else newDescription = newDescription + "\n" + description;
                        }

                        else {
                            newDescription = newDescription.replace("\n" + description, "");
                            newDescription = newDescription.replace(description + "\n", "");
                            newDescription = newDescription.replace(description, "");
                        }

                        holder.select(position);
                    }
                };

                descriptionListAdapter = new DescriptionListAdapter(getApplicationContext(), listener, descriptions);

                descriptionList.getRecycledViewPool().setMaxRecycledViews(0,0);
                descriptionList.hasFixedSize();
                descriptionList.setFocusable(false);
                descriptionList.setAdapter(descriptionListAdapter);

                loadingDialog.setVisibility(View.GONE);
                descriptionList.setVisibility(View.VISIBLE);
            }
        });
    }

    private void sendDescription() {

        if (newDescription != null && !newDescription.equals("")) {
            Intent descriptionIntent = new Intent();
            descriptionIntent.putExtra("description", newDescription);
            setResult(RESULT_OK, descriptionIntent);
        }

        else setResult(RESULT_CANCELED);

        finish();
    }
}
