/*
 * This file contains sample code that is licensed according to the BlackBerry Dynamics SDK terms and conditions.
 * Copyright 2019 BlackBerry Limited. All rights reserved.
 */

package com.good.gd.example.appkinetics.saveeditclient;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import android.util.Log;

import com.good.gd.GDAndroid;
import com.good.gd.icc.GDService;
import com.good.gd.icc.GDServiceClient;
import com.good.gd.icc.GDServiceClientListener;
import com.good.gd.icc.GDServiceException;
import com.good.gd.icc.GDServiceListener;

public class SaveEditClientApplication extends Application {

    private static final String TAG = SaveEditClientApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();

        // turn off (suppress) night mode for this app
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        GDAndroid.getInstance().applicationInit(this);

        final GDServiceClientListener clientListener = GDSaveEditClientListener.getInstance();
        final GDServiceListener serviceListener = GDSaveEditClientListener.getInstance();

        try {
            GDServiceClient.setServiceClientListener(clientListener);
            GDService.setServiceListener(serviceListener);
        } catch (final GDServiceException exception) {
            Log.e(TAG, "SaveEditClientApplication::onCreate()  " +
                    "Error Setting GDServiceClientListener -- " + exception.getMessage() + "\n");
        }
    }
}
