package com.dataxy.sample;

import android.app.Application;
import android.content.Context;

import com.dataxy.DataXY;

public class DataXYApplication extends Application {

    public static final String DATA_XY_ID = "-1";

    @Override
    public void onCreate() {
        super.onCreate();

        final Context context = getApplicationContext();
        DataXY.initialize(context, DATA_XY_ID);
        DataXY.activateDebugMode(true);
    }
}
