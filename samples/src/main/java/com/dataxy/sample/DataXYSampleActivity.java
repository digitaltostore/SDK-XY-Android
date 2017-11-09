package com.dataxy.sample;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dataxy.DataXY;
import com.dataxy.PermissionHelper;

public class DataXYSampleActivity extends FragmentActivity {

    private static final String TAG = DataXYSampleActivity.class.toString();

    private Button mAskerForPermissionButton;
    private Button mToggleGPSButton;

    private LocationManager mLocationManager;
    private boolean mLocationEnabled = false;

    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(TAG,location.getLatitude() + ";" + location.getLongitude());
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (DataXY.isRegistered()) {
            Toast.makeText(DataXYSampleActivity.this, "DataXY is registered", Toast.LENGTH_SHORT).show();
        }

        mAskerForPermissionButton = (Button) findViewById(R.id.main_ask_for_permission);
        mToggleGPSButton = (Button) findViewById(R.id.main_enable_gps);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        mAskerForPermissionButton.setOnClickListener(new PermissionClickListener(this));
        mToggleGPSButton.setOnClickListener(new GPSClickListener(this));

        super.onResume();
    }

    @Override
    protected void onPause() {
        mAskerForPermissionButton.setOnClickListener(null);
        mToggleGPSButton.setOnClickListener(null);
        disableGPS();

        super.onPause();
    }


    @UiThread
    private boolean toggleGPS() {
        if (mLocationEnabled) {
            disableGPS();
        } else {
            enableGPS();
        }
        return mLocationEnabled;
    }

    private void enableGPS() {
        if (PermissionHelper.checkForCoarseLocationPermission(this)
                || PermissionHelper.checkForFineLocationPermission(this)) {
            for (String provider : mLocationManager.getAllProviders()) {
                mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
            }
            mLocationEnabled = true;
        } else {
            Toast.makeText(DataXYSampleActivity.this, "Location Permission is not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableGPS() {
        if (PermissionHelper.checkForCoarseLocationPermission(this)
                || PermissionHelper.checkForFineLocationPermission(this)) {
            this.mLocationManager.removeUpdates(mLocationListener);
        }
        mLocationEnabled = false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (DataXY.onRequestPermissionsResult(this)) {
            Toast.makeText(DataXYSampleActivity.this, "DataXY is registered", Toast.LENGTH_SHORT).show();
        }
    }


    private static class PermissionClickListener implements View.OnClickListener {
        private Activity mActivity;

        PermissionClickListener(Activity activity) {
            mActivity = activity;
        }

        @Override
        public void onClick(View v) {
            if (requestPermission(mActivity)) { // or use PermissionHelper.askForFineLocationPermission(DataXYSampleActivity.this)
                Toast.makeText(mActivity, "Fine Location Permission is already granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * <p>Helper method to request the Fine Location Permission from Activity. Uses {@link PermissionHelper#LOCATION_REQUEST_CODE} as request code.</p>
     *
     * @param activity Activity
     */
    public static boolean requestPermission(Activity activity) {
        //noinspection SimplifiableIfStatement
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PermissionHelper.askForFineLocationPermission(activity);
        }
        return true;
    }

    private static class GPSClickListener implements View.OnClickListener {
        private DataXYSampleActivity mActivity;

        private GPSClickListener(DataXYSampleActivity activity) {
            mActivity = activity;
        }

        @Override
        public void onClick(View v) {
            final boolean isLocationEnabled = mActivity.toggleGPS();
            mActivity.mToggleGPSButton.setText(isLocationEnabled ? "Disable GPS" : "Enable GPS");
            Toast.makeText(mActivity, "GPS is now " + (isLocationEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        }
    }
}
