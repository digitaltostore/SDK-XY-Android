package com.dataxy;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiSelector;
import android.view.View;

import com.dataxy.sample.DataXYSampleActivity;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.List;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.registerIdlingResources;
import static android.support.test.espresso.Espresso.unregisterIdlingResources;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.any;

@RunWith(TestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataXYSampleTest extends Junit4InstrumentationTestCase<DataXYSampleActivity> {

    private static final String MOCK_PROVIDER_NAME = "test";
    private static final long PERMISSION_TIMEOUT = 5000;

    private LocationManager mLocationManager;
    private PendingIntent mPendingIntent;


    public DataXYSampleTest() {
        super(DataXYSampleActivity.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        getActivity();
        Context context = getInstrumentation().getTargetContext();

        if (!PermissionHelper.checkForFineLocationPermission(context)) {
            onView(withText("Ask for permission")).perform(click());

            UiDevice uiDevice = UiDevice.getInstance(getInstrumentation());

            long start = System.currentTimeMillis();

            UiObject authorizePermission = null;
            while ((authorizePermission == null || !authorizePermission.exists()) && (System.currentTimeMillis() - start < PERMISSION_TIMEOUT)) {
                authorizePermission = uiDevice.findObject(new UiSelector().clickable(true).checkable(false).index(1));
                if (authorizePermission != null && authorizePermission.exists()) {
                    authorizePermission.clickAndWaitForNewWindow();
                } else {
                    sleep(300);
                }
            }
            if (authorizePermission == null) {
                Assert.fail();
            }
        }

        Intent intent = new Intent(context, DataXYReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(context, 1664, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.getProvider(MOCK_PROVIDER_NAME) != null) {
            mLocationManager.clearTestProviderEnabled(MOCK_PROVIDER_NAME);
            mLocationManager.removeTestProvider(MOCK_PROVIDER_NAME);
            mLocationManager.removeUpdates(mPendingIntent);
        }
        mLocationManager.addTestProvider(MOCK_PROVIDER_NAME, false, false, false, false, true, false, false, 0, 3);
        mLocationManager.setTestProviderEnabled(MOCK_PROVIDER_NAME, true);


        mLocationManager.requestLocationUpdates(MOCK_PROVIDER_NAME, 1L, 0.00001F, mPendingIntent);

        onView(withText("Enable GPS")).perform(click());
        clearDataBase();
    }


    @Override
    public void tearDown() throws Exception {
        clearDataBase();
        if (mLocationManager != null) {
            mLocationManager.clearTestProviderEnabled(MOCK_PROVIDER_NAME);
            mLocationManager.removeTestProvider(MOCK_PROVIDER_NAME);
            mLocationManager.removeUpdates(mPendingIntent);
            mLocationManager = null;
        }

        DataXYSender.INSTANCE = new DataXYSender();

        super.tearDown();
    }

    private void clearDataBase() {
        Context targetContext = getInstrumentation().getTargetContext();
        new DataXYDataBase(targetContext)
                .open()
                .deleteAllLocations()
                .close();
    }


    @Test
    public void test_0_initialization() throws Throwable {
        assertNotNull("must have advertising id to work properly", DataXYSender.sAdvertisingId);
        assertEquals("clientId", "mappy_dataxy_sample", DataXYSender.sClientId);
    }

    @Test
    public void test_0_mockLocation() throws Throwable {
        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);

        Assert.assertTrue("actual : " + dataBase.getLocationCount() + ", expected : " + 1, dataBase.getLocationCount() == 1);

        dataBase.close();
    }


    @Test
    public void test_1_aggregate() throws Throwable {
        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);
        calendar.setTimeInMillis(3456);
        mockLocation(48.2, 2.84, 42, 22, 3.0F, calendar);
        calendar.setTimeInMillis(5789);
        mockLocation(48.1, 2.84, 42, 22, 2.1F, calendar);

        Assert.assertTrue("actual : " + dataBase.getLocationCount() + ", expected : " + 3, dataBase.getLocationCount() == 3);

        dataBase.close();
    }


    @Test
    public void test_2_aggregate_exceedMax() throws Throwable {
        int previousMax = DataXYReceiver.MAX_LOCATIONS;
        int newMax = 2;

        DataXYReceiver.MAX_LOCATIONS = newMax;

        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);
        calendar.setTimeInMillis(3456);
        mockLocation(48.2, 2.84, 42, 22, 3.0F, calendar);
        calendar.setTimeInMillis(5789);
        mockLocation(48.1, 2.84, 42, 22, 2.1F, calendar);

        Assert.assertTrue("actual : " + dataBase.getLocationCount() + ", expected : " + newMax, dataBase.getLocationCount() == newMax);

        dataBase.close();

        DataXYReceiver.MAX_LOCATIONS = previousMax;
    }

    @Test
    public void test_2_aggregate_close_beforeAMinute() {
        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);
        calendar.setTimeInMillis(3456);
        mockLocation(48.3, 2.85001, 42, 22, 3.0F, calendar);

        Assert.assertTrue("actual : " + dataBase.getLocationCount() + ", expected : " + 1, dataBase.getLocationCount() == 1);

        dataBase.close();
    }

    @Test
    public void test_2_aggregate_close_afterAMinute() {
        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);
        calendar.setTimeInMillis(63456);
        mockLocation(48.3, 2.85001, 42, 22, 3.0F, calendar);

        Assert.assertTrue("actual : " + dataBase.getLocationCount() + ", expected : " + 2, dataBase.getLocationCount() == 2);

        dataBase.close();
    }

    @Test
    public void test_2_aggregate_exceedTime() throws Throwable {
        DataXYSender.INSTANCE = new IDataXYSender() {
            @Override
            public void send(@NonNull List<DataXYLocation> locations, @NonNull Callback callback) {
                callback.onSuccess();
            }

            @Override
            public void setOkHttpClient(OkHttpClient okHttpClient) {
            }
        };
        long previousMax = DataXYReceiver.MIN_TIME_INTERVAL;
        DataXYReceiver.MIN_TIME_INTERVAL = 4000; // 4 seconds

        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);
        calendar.setTimeInMillis(3456);
        mockLocation(48.2, 2.84, 42, 22, 3.0F, calendar);
        calendar.setTimeInMillis(5789);
        mockLocation(48.1, 2.84, 42, 22, 2.1F, calendar);

        Assert.assertTrue("actual : " + dataBase.getLocationCount() + ", expected : " + 0, dataBase.getLocationCount() == 0);

        dataBase.close();

        DataXYReceiver.MIN_TIME_INTERVAL = previousMax;
    }

    private int runSendTest() {
        int previousMax = DataXYReceiver.MIN_LOCATIONS_TO_SEND;
        DataXYReceiver.MIN_LOCATIONS_TO_SEND = 3;
        final DataXYDataBase dataBase = new DataXYDataBase(getActivity())
                .open();

        Assert.assertTrue(dataBase.getLocationCount() == 0);

        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.setTimeInMillis(1123);
        mockLocation(48.3, 2.85, 42, 22, 2.0F, calendar);
        calendar.setTimeInMillis(3456);
        mockLocation(48.2, 2.84, 42, 22, 3.0F, calendar);
        calendar.setTimeInMillis(5789);
        mockLocation(48.1, 2.84, 42, 22, 2.1F, calendar);


        int locationCount = dataBase.getLocationCount();
        DataXYReceiver.MIN_LOCATIONS_TO_SEND = previousMax;
        dataBase.close();

        return locationCount;
    }


    @Test
    public void test_3_send_success() throws Throwable {
        DataXYSender.INSTANCE = new IDataXYSender() {
            @Override
            public void send(@NonNull List<DataXYLocation> locations, @NonNull Callback callback) {
                callback.onSuccess();
            }

            @Override
            public void setOkHttpClient(OkHttpClient okHttpClient) {
            }
        };

        int locationCount = runSendTest();

        Assert.assertTrue("actual : " + locationCount + ", expected : " + 0, locationCount == 0);
    }

    @Test
    public void test_3_send_failure_mappy() throws Throwable {
        DataXYSender.INSTANCE = new IDataXYSender() {
            @Override
            public void send(@NonNull List<DataXYLocation> locations, @NonNull Callback callback) {
                callback.onFailure(false);
            }

            @Override
            public void setOkHttpClient(OkHttpClient okHttpClient) {
            }
        };

        int locationCount = runSendTest();

        Assert.assertTrue("actual : " + locationCount + ", expected : " + 3, locationCount == 3);
    }

    @Test
    public void test_3_send_failure_dts() throws Throwable {
        DataXYSender.INSTANCE = new IDataXYSender() {
            @Override
            public void send(@NonNull List<DataXYLocation> locations, @NonNull Callback callback) {
                callback.onFailure(true);
            }

            @Override
            public void setOkHttpClient(OkHttpClient okHttpClient) {
            }
        };

        int locationCount = runSendTest();

        Assert.assertTrue("actual : " + locationCount + ", expected : " + 0, locationCount == 0);
    }

    @Test
    public void test_4_send() throws Throwable {
        final DataXYSenderIdlingResource idlingResource = new DataXYSenderIdlingResource(2);

        MockWebServer mockWebServer = new MockWebServer();
        // Schedule some responses.
        mockWebServer.setDispatcher(new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                if (path.endsWith("mappy_dataxy_sample")) {
                    String locationJson = readBody(request);

                    Assert.assertEquals("{\"1.123\":{\"lat\":48.300000,\"long\":2.850000,\"alt\":42.0,\"horizontal_accuracy\":22.0,\"speed\":2.0},\"3.456\":{\"lat\":48.200000,\"long\":2.840000,\"alt\":42.0,\"horizontal_accuracy\":22.0,\"speed\":3.0},\"5.789\":{\"lat\":48.100000,\"long\":2.840000,\"alt\":42.0,\"horizontal_accuracy\":22.0,\"speed\":2.1}}", locationJson);
                    idlingResource.increaseCounter();
                    return new MockResponse().setResponseCode(204);
                }
                Assert.fail("unknown request");
                return new MockResponse().setResponseCode(404);
            }
        });
        mockWebServer.start();

        DataXYSender.switchPlatform(false, mockWebServer.url("/").toString());
        DataXYPreferencesHelper.setDTSEnabled(getInstrumentation().getContext(), true);
        runSendTest();

        registerIdlingResources(idlingResource);
        onView(isRoot()).perform(ViewActionUtils.waitAtLeast(100));
        unregisterIdlingResources(idlingResource);

        mockWebServer.shutdown();
        DataXYSender.switchPlatform(false, null);
    }

    @Test
    public void test_4_send_without_dts() throws Throwable {
        final DataXYSenderIdlingResource idlingResource = new DataXYSenderIdlingResource(1);

        MockWebServer mockWebServer = new MockWebServer();
        // Schedule some responses.
        mockWebServer.setDispatcher(new Dispatcher() {
            private int counter = 0;

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                String path = request.getPath();
                if (path.endsWith("mappy_dataxy_sample")) {
                    String locationJson = readBody(request);

                    counter++;
                    if (counter > 1) {
                        Assert.fail("DTS is not disabled");
                    }

                    Assert.assertEquals("{\"1.123\":{\"lat\":48.300000,\"long\":2.850000,\"alt\":42.0,\"horizontal_accuracy\":22.0,\"speed\":2.0},\"3.456\":{\"lat\":48.200000,\"long\":2.840000,\"alt\":42.0,\"horizontal_accuracy\":22.0,\"speed\":3.0},\"5.789\":{\"lat\":48.100000,\"long\":2.840000,\"alt\":42.0,\"horizontal_accuracy\":22.0,\"speed\":2.1}}", locationJson);
                    idlingResource.increaseCounter();
                    return new MockResponse().setResponseCode(204);
                }
                Assert.fail("unknown request");
                return new MockResponse().setResponseCode(404);
            }
        });
        mockWebServer.start();

        DataXYSender.switchPlatform(false, mockWebServer.url("/").toString());
        DataXYPreferencesHelper.setDTSEnabled(getInstrumentation().getContext(), false);

        runSendTest();

        registerIdlingResources(idlingResource);
        onView(isRoot()).perform(ViewActionUtils.waitAtLeast(100));
        unregisterIdlingResources(idlingResource);

        mockWebServer.shutdown();
        DataXYSender.switchPlatform(false, null);
        DataXYPreferencesHelper.setDTSEnabled(getInstrumentation().getContext(), true);
    }

    private String readBody(RecordedRequest request) {
        try {
            final InputStream inputStream = request.getBody().inputStream();

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void mockLocation(double latitude, double longitude, double altitude, float accuracy, float speed, Calendar calendar) {
        Location location = new Location(MOCK_PROVIDER_NAME);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
        location.setAccuracy(accuracy);
        location.setSpeed(speed);
        location.setTime(calendar.getTimeInMillis());
        location.setElapsedRealtimeNanos(calendar.getTimeInMillis() * 1000000);

        mLocationManager.setTestProviderLocation(MOCK_PROVIDER_NAME, location);
        final long millis = 300L;
        onView(isRoot()).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return any(View.class);
            }

            @Override
            public String getDescription() {
                return "wait for at least " + millis + " millis.";
            }

            @Override
            public void perform(final UiController uiController, final View view) {
                uiController.loopMainThreadUntilIdle();
                uiController.loopMainThreadForAtLeast(millis);
            }
        });
    }
}
