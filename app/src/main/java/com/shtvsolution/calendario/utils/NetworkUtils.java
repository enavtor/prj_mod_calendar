package com.shtvsolution.calendario.utils;

import android.content.Context;
import android.net.ConnectivityManager;

//Static class that performs all the operations related to the connection status check:
public class NetworkUtils {

    //The connectivity manager that will be used to access the network status:
    private static ConnectivityManager connectionManager;

    //Method that instantiates the connectivity manager if it is not instantiated and checks the network current state:
    public static boolean isNetworkAvailable(Context context){

        if (connectionManager == null)
            connectionManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        return checkConnectionAvailable();
    }

    //Method that returns whether or not the network connection is available:
    private static boolean checkConnectionAvailable() {

        return (connectionManager != null && connectionManager.getActiveNetworkInfo() != null);
    }
}

