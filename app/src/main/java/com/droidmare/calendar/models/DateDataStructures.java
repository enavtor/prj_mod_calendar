package com.droidmare.calendar.models;

import android.content.res.Resources;

import com.droidmare.R;

//Model for the date data structures used by DateUtils
//@author Eduardo on 06/03/2018.

public class DateDataStructures {

    private String[] daysOfWeekLetter = new String[7];

    private String[] daysOfWeekText = new String[7];

    private String[] months = new String[12];

    public DateDataStructures(Resources res){

        //Definition of daysOfWeekLetter:
        daysOfWeekLetter[0] = res.getString(R.string.day_1_letter);
        daysOfWeekLetter[1] = res.getString(R.string.day_2_letter);
        daysOfWeekLetter[2] = res.getString(R.string.day_3_letter);
        daysOfWeekLetter[3] = res.getString(R.string.day_4_letter);
        daysOfWeekLetter[4] = res.getString(R.string.day_5_letter);
        daysOfWeekLetter[5] = res.getString(R.string.day_6_letter);
        daysOfWeekLetter[6] = res.getString(R.string.day_7_letter);

        //Definition of daysOfWeekText:
        daysOfWeekText[0] = res.getString(R.string.day_1_text);
        daysOfWeekText[1] = res.getString(R.string.day_2_text);
        daysOfWeekText[2] = res.getString(R.string.day_3_text);
        daysOfWeekText[3] = res.getString(R.string.day_4_text);
        daysOfWeekText[4] = res.getString(R.string.day_5_text);
        daysOfWeekText[5] = res.getString(R.string.day_6_text);
        daysOfWeekText[6] = res.getString(R.string.day_7_text);

        //Definition of monthsText:
        months[0] = res.getString(R.string.month_1_text);
        months[1] = res.getString(R.string.month_2_text);
        months[2] = res.getString(R.string.month_3_text);
        months[3] = res.getString(R.string.month_4_text);
        months[4] = res.getString(R.string.month_5_text);
        months[5] = res.getString(R.string.month_6_text);
        months[6] = res.getString(R.string.month_7_text);
        months[7] = res.getString(R.string.month_8_text);
        months[8] = res.getString(R.string.month_9_text);
        months[9] = res.getString(R.string.month_10_text);
        months[10] = res.getString(R.string.month_11_text);
        months[11] = res.getString(R.string.month_12_text);
    }

    public String[] getDaysOfWeekLetter() {
        return daysOfWeekLetter;
    }

    public String[] getDaysOfWeekText() {
        return daysOfWeekText;
    }

    public String[] getMonths() {
        return months;
    }
}
