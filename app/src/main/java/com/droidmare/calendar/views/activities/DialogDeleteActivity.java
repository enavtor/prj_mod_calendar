package com.droidmare.calendar.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.droidmare.calendar.utils.ToastUtils;
import com.droidmare.R;

//Activity for displaying a dialog before performing a delete operation
//@author Eduardo on 27/06/2018.

public class DialogDeleteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //The main activity needs to know that this activity is running in the event that a synchronization takes place,
        //case in which the running activity might have to be finished (unless this activity was launched by event params):
        if (!getIntent().hasExtra("deletePrevAlarm"))
            MainActivity.setRunningActivityReference(this);
        else DialogEventParameters.setLaunchedActivityReference(this);

        setContentView(R.layout.activity_dialog_delete_events);

        initializeViews();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN && ToastUtils.cancelCurrentToast()) return true;

        else return super.dispatchKeyEvent(event);
    }


    //Initialization of the activity views and buttons:
    private void initializeViews (){

        TextView title = findViewById(R.id.delete_events_dialog_title);

        LinearLayout affirmative = findViewById(R.id.delete_events_affirmative_button);
        LinearLayout negative = findViewById(R.id.delete_events_negative_button);

        if (getIntent().hasExtra("deleteSingleEvent")) {
            title.setText(getResources().getString(R.string.delete_single_dialog_title));
        }

        else if (getIntent().hasExtra("deletePrevAlarm")) {
            title.setText(getResources().getString(R.string.delete_alarm_dialog_title));
        }

        negative.requestFocus();

        //Behaviour of the affirmative option inside the dialog:
        affirmative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performOperation();
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

    private void performOperation() {
        setResult(RESULT_OK);
        finish();
    }
}
