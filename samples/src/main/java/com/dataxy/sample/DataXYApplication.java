package com.dataxy.sample;

import android.app.Application;
import android.content.Context;

import com.dataxy.DataXY;

public class DataXYApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();
        DataXY.initialize(context);
        DataXY.activateDebugMode(true);
    }
}