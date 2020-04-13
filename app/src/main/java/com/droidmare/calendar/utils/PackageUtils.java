package com.droidmare.calendar.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

//Utils for managing packages inside the device
//@author Eduardo on 20/10/2019.

public class PackageUtils {

    public static Intent getLaunchIntent(Context context, String packageName) {
        Intent launchIntent = null;

        if  (isPackageInstalled(context, packageName))
            launchIntent = new Intent(packageName);

        return launchIntent;
    }

    //Method that transforms a dp value into pixels:
    private static boolean isPackageInstalled (Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();

        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }

        return true;
    }
}