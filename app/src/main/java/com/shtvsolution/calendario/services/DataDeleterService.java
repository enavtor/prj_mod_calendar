package com.shtvsolution.calendario.services;

import android.app.ActivityManager;
import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

//App's data deleter service declaration
//@author Eduardo on 24/05/2018.

public class DataDeleterService extends IntentService {

    public DataDeleterService() {
        super("DataDeleterService");
    }

    @Override
    public void onHandleIntent(Intent eventIntent) {

        UserDataReceiverService.resetUser();

        ((ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE)).clearApplicationUserData();
    }
}