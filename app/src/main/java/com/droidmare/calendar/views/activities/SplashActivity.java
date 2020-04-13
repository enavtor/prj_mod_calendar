package com.droidmare.calendar.views.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.droidmare.R;
import com.droidmare.calendar.services.ApiSynchronizationService;
import com.droidmare.common.utils.ServiceUtils;

//Splash activity declaration
//@author Eduardo on 07/02/2018.
public class SplashActivity extends AppCompatActivity {

    private String[] permissions;

    public static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        try {
            permissions = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        }
        catch(PackageManager.NameNotFoundException nameNotFoundExc) {
            nameNotFoundExc.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                requestPermissions();
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_CODE) {

            boolean permissionGranted = true;

            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    permissionGranted = false;
                    break;
                }
            }

            if (permissionGranted) waitAndOpen();
            else requestPermissions();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void requestPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            try {requestPermissions(permissions,SplashActivity.REQUEST_CODE);}
            catch (Exception ex) {ex.printStackTrace();}
        }
        else waitAndOpen();
    }

    private void waitAndOpen(){
        //If the api synchronization service is not running it must be started:
        if (!ApiSynchronizationService.isRunning())
            ServiceUtils.startService(getApplicationContext(), new Intent(getApplicationContext(), ApiSynchronizationService.class));

        //Now the MainActivity can be launched:
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
