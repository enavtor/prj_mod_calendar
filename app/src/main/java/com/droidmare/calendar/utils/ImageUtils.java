package com.droidmare.calendar.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.TypedValue;

//Utils for managing bitmap images and dp to px transformations
//@author Eduardo on 27/05/2019.
public class ImageUtils {

    public static Bitmap decodeBitmapString(String encodedString) {

        byte[] encodedBitmapByteArray = Base64.decode(encodedString, Base64.DEFAULT);

        return BitmapFactory.decodeByteArray(encodedBitmapByteArray, 0, encodedBitmapByteArray.length);
    }

    public static Drawable getImageFromAssets(Context context, String name) {
        try {
            return Drawable.createFromStream(context.getAssets().open(name), null);
        } catch (Exception e) {
            return null;
        }
    }

    //Method that transforms a dp value into pixels:
    public static int transformDipToPix (Context context, int dpValue) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, r.getDisplayMetrics());
    }
}