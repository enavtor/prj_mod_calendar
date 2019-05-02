package com.droidmare.calendar.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * Utilities for images
 * @author Carolina on 23/10/2017.
 */
public class ImageUtils {

    /**
     * Gets drawable from assets image
     * @param context App context
     * @param name Filename
     * @return Drawable object with obtained image from assets folder
     */
    public static Drawable getImageFromAssets(Context context, String name){
        try{return Drawable.createFromStream(context.getAssets().open(name),null);}
        catch(Exception e){return null;}
    }


    /**
     * Gets color from String value
     * @param color Color in a String format: <code>#RRGGBB</code> or <code>#AARRGGBB</code>
     * @return Color in integer format
     */
    public static int getColorFromString(String color){
        //TODO Check if format is wright
        return Color.parseColor(color);
    }
}
