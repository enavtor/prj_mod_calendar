package com.shtvsolution.calendario.views.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.shtvsolution.R;
import com.shtvsolution.calendario.services.ApiSynchronizationService;
import com.shtvsolution.estadisticas.StatisticAPI;
import com.shtvsolution.estadisticas.StatisticService;

//Splash activity declaration
//@author Eduardo on 07/02/2018.

public class SplashActivity extends AppCompatActivity {

    /** App permissions */
    private String[] permissions;

    /** Request code */
    public static final int REQUEST_CODE = 1;

    private StatisticService statistic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        statistic = new StatisticService(this);
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_CREATE, getClass().getCanonicalName());

        setContentView(R.layout.activity_splash);

        try{
            permissions=getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
        }
        catch(PackageManager.NameNotFoundException nnfe){
            nnfe.printStackTrace();
        }

        requestPermissions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        statistic.sendStatistic(StatisticAPI.StatisticType.APP_TRACK, StatisticService.ON_DESTROY, getClass().getCanonicalName());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==REQUEST_CODE) {
            boolean permissionGranted = true;
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    permissionGranted = false;
                    break;
                }
            }
            if (permissionGranted)
                waitAndOpen();
            else
                requestPermissions();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Asks user for app permissions
     */
    public void requestPermissions(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M) {
            try {requestPermissions(permissions,SplashActivity.REQUEST_CODE);}
            catch (Exception ex) {ex.printStackTrace();}
        }
        else waitAndOpen();
    }

    /**
     * Waits and opens MainActivity
     */
    private void waitAndOpen(){
        //If the api synchronization service is not running it must be started:
        if (!ApiSynchronizationService.isRunning())
            startService(new Intent(getApplicationContext(), ApiSynchronizationService.class));

        //Now the MainActivity can be launched:
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
