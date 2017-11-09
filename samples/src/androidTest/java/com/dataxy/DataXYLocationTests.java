package com.dataxy;

import android.test.AndroidTestCase;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.List;

public class DataXYLocationTests extends AndroidTestCase {
    private static final double LATITUDE_1 = 48.85;
    private static final String LATITUDE_1_JSON = "48.850000";
    private static final double LONGITUDE_1 = 2.33;
    private static final String LONGITUDE_1_JSON = "2.330000";
    private static final double ALTITUDE_1 = 35;
    private static final double ACCURACY_1 = 22;
    private static final float SPEED_1 = 22F;
    private static final long DATE_1 = 557798400386L;
    private static final String DATE_1_JSON = "557798400.386";
    private static final String LATITUDE_2_JSON = "49.000000";
    private static final double LATITUDE_2 = 49;
    private static final String LONGITUDE_2_JSON = "2.300000";
    private static final double LONGITUDE_2 = 2.3;
    private static final double ALTITUDE_2 = 0;
    private static final double ACCURACY_2 = 27;
    private static final float SPEED_2 = 27;
    private static final long DATE_2 = 557798460000L;
    private static final String DATE_2_JSON = "557798460.000";

    private static final DataXYLocation LOCATION_1 = new DataXYLocation();
    private static final DataXYLocation LOCATION_2 = new DataXYLocation();

    private static final List<DataXYLocation> LOCATIONS = new ArrayList<>();

    private static final String JSON_1 = "\"" + DATE_1_JSON + "\":{\"lat\":" + LATITUDE_1_JSON + ",\"long\":" + LONGITUDE_1_JSON + ",\"alt\":" + ALTITUDE_1 + ",\"horizontal_accuracy\":" + ACCURACY_1 + ",\"speed\":" + SPEED_1 + "}";
    private static final String JSON_2 = "\"" + DATE_2_JSON + "\":{\"lat\":" + LATITUDE_2_JSON + ",\"long\":" + LONGITUDE_2_JSON + ",\"horizontal_accuracy\":" + ACCURACY_2 + ",\"speed\":" + SPEED_2 + "}";
    private static final String JSON_LIST = "{" + JSON_1 + "," + JSON_2 + "}";

    static {
        LOCATION_1.mLongitude = LONGITUDE_1;
        LOCATION_1.mLatitude = LATITUDE_1;
        LOCATION_1.mAltitude = ALTITUDE_1;
        LOCATION_1.mAccuracy = ACCURACY_1;
        LOCATION_1.mSpeed = SPEED_1;
        LOCATION_1.mDate = DATE_1;

        LOCATION_2.mLongitude = LONGITUDE_2;
        LOCATION_2.mLatitude = LATITUDE_2;
        LOCATION_2.mAltitude = ALTITUDE_2;
        LOCATION_2.mAccuracy = ACCURACY_2;
        LOCATION_2.mSpeed = SPEED_2;
        LOCATION_2.mDate = DATE_2;

        LOCATIONS.add(LOCATION_1);
        LOCATIONS.add(LOCATION_2);
    }

    public void test_0_toJson() {
        Assert.assertEquals(JSON_1, LOCATION_1.toJson(new StringBuilder()).toString());
    }

    public void test_0_toJson_noAltitude() {
        Assert.assertEquals(JSON_2, LOCATION_2.toJson(new StringBuilder()).toString());
    }

    public void test_1_listToJson() {
        final String jsonList = DataXYLocation.listToJson(LOCATIONS);
        Assert.assertEquals(JSON_LIST, jsonList);
    }
}
