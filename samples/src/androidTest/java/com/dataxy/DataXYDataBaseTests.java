package com.dataxy;

import android.location.Location;
import android.support.annotation.Nullable;
import android.test.AndroidTestCase;

import org.junit.Assert;

import java.util.List;

public class DataXYDataBaseTests extends AndroidTestCase {

    private DataXYDataBase mDataBase;

    private static final double LATITUDE_1 = 48.85;
    private static final double LONGITUDE_1 = 2.33;
    private static final double ALTITUDE_1 = 35;
    private static final long DATE_1 = 557798400000L;
    private static final double LATITUDE_2 = 49;
    private static final double LONGITUDE_2 = 2.3;
    private static final double ALTITUDE_2 = 30;
    private static final long DATE_2 = 557798460000L;
    private static final double LATITUDE_3 = 49.15;
    private static final double LONGITUDE_3 = 2.27;
    private static final double ALTITUDE_3 = 25;
    private static final long DATE_3 = 557798520000L;

    private static final Location LOCATION_1 = new Location("test");
    private static final Location LOCATION_2 = new Location("test");
    private static final Location LOCATION_3 = new Location("test");

    static {
        LOCATION_1.setLatitude(LATITUDE_1);
        LOCATION_1.setLongitude(LONGITUDE_1);
        LOCATION_1.setAltitude(ALTITUDE_1);
        LOCATION_1.setTime(DATE_1);

        LOCATION_2.setLatitude(LATITUDE_2);
        LOCATION_2.setLongitude(LONGITUDE_2);
        LOCATION_2.setAltitude(ALTITUDE_2);
        LOCATION_2.setTime(DATE_2);

        LOCATION_3.setLatitude(LATITUDE_3);
        LOCATION_3.setLongitude(LONGITUDE_3);
        LOCATION_3.setAltitude(ALTITUDE_3);
        LOCATION_3.setTime(DATE_3);
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mDataBase = new DataXYDataBase(getContext());
        mDataBase.open();
        mDataBase.deleteAllLocations();
    }

    public void test_0_default() {
        runTest(0);
    }

    public void test_0_insertLocation_1() {
        mDataBase.insertLocation(LOCATION_1);
        runTest(1);
    }

    public void test_0_insertLocation_2() {
        mDataBase.insertLocation(LOCATION_1);
        mDataBase.insertLocation(LOCATION_2);
        runTest(2);
    }

    public void test_2_deleteAllLocations() {
        mDataBase.insertLocation(LOCATION_1);
        mDataBase.insertLocation(LOCATION_2);

        mDataBase.deleteAllLocations();
        runTest(0);
    }

    public void test_3_deleteOldLocations() {
        mDataBase.insertLocation(LOCATION_1);
        mDataBase.insertLocation(LOCATION_2);
        mDataBase.insertLocation(LOCATION_3);

        mDataBase.deleteOldLocations(1);
        runTest(2);

        final List<DataXYLocation> locations = mDataBase.getAllLocations();
        Assert.assertTrue(locations.get(0).mDate == DATE_2);
        Assert.assertTrue(locations.get(1).mDate == DATE_3);
    }

    public void test_4_getAllLocations() {
        mDataBase.insertLocation(LOCATION_1);

        final List<DataXYLocation> locations = mDataBase.getAllLocations();
        final DataXYLocation location_1 = locations.get(0);
        Assert.assertTrue(location_1.mDate == LOCATION_1.getTime());
        Assert.assertTrue(location_1.mLatitude == LOCATION_1.getLatitude());
        Assert.assertTrue(location_1.mLongitude == LOCATION_1.getLongitude());
        Assert.assertTrue(location_1.mAltitude == LOCATION_1.getAltitude());
    }

    private void runTest(int expected) {
        final int locationCount = mDataBase.getLocationCount();
        final List<DataXYLocation> locations = mDataBase.getAllLocations();
        Assert.assertTrue(getNumberMessage(locationCount, expected, locations), locationCount == expected);
        Assert.assertTrue(getNumberMessage(locations.size(), expected, locations), locations.size() == expected);
    }

    @Override
    protected void tearDown() throws Exception {
        mDataBase.deleteAllLocations();
        mDataBase.close();
        mDataBase = null;
        super.tearDown();
    }

    private String getNumberMessage(int actual, int expected, @Nullable Object objectToLog) {
        return "Database does not have the right number of Locations. Actual : " + actual + ", expected : " + expected + (objectToLog == null ? "" : "\n" + objectToLog);
    }
}
