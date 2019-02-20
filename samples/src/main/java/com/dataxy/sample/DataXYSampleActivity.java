package com.dataxy.sample;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dataxy.DataXY;

public class DataXYSampleActivity extends FragmentActivity {

    private static final String TAG = DataXYSampleActivity.class.toString();

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;

    private Button mAskerForPermissionButton;
    private Button mToggleGPSButton;

    private LocationManager mLocationManager;
    private boolean mLocationEnabled = false;

    LocationListener mLocationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            Log.d(TAG, location.getLatitude() + ";" + location.getLongitude());
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

        DataXY.enable(this, true);

        mAskerForPermissionButton = findViewById(R.id.main_ask_for_permission);
        mToggleGPSButton = findViewById(R.id.main_enable_gps);

        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onResume() {
        mAskerForPermissionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (askForFineLocationPermission()) {
                    Toast.makeText(v.getContext(), "Fine Location Permission is already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mToggleGPSButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final boolean isLocationEnabled = toggleGPS();
                mToggleGPSButton.setText(isLocationEnabled ? "Disable GPS" : "Enable GPS");
                Toast.makeText(v.getContext(), "GPS is now " + (isLocationEnabled ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
            }
        });

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
        if (checkFineLocationPermissionGranted()) {
            for (String provider : mLocationManager.getAllProviders()) {
                //noinspection MissingPermission since check is done in #checkFineLocationPermissionGranted
                mLocationManager.requestLocationUpdates(provider, 0, 0, mLocationListener);
            }
            mLocationEnabled = true;
        } else {
            Toast.makeText(DataXYSampleActivity.this, "Location Permission is not granted", Toast.LENGTH_SHORT).show();
        }
    }

    private void disableGPS() {
        if (checkFineLocationPermissionGranted()) {
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

    /**
     * @return true if fine location permission is granted, false otherwise
     */
    private boolean checkFineLocationPermissionGranted() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * <p>Manage permission request for fine location permission</p>
     *
     * @return true if permission is already granted, false otherwise
     */
    private boolean askForFineLocationPermission() {
        String permissionName = Manifest.permission.ACCESS_FINE_LOCATION;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkFineLocationPermissionGranted()) {
                return true;
            }

            ActivityCompat.requestPermissions(this, new String[]{permissionName}, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }

        final boolean isPermissionInManifest = getPackageManager().checkPermission(permissionName, getPackageName()) == PackageManager.PERMISSION_GRANTED;
        if (!isPermissionInManifest) {
            Log.e(TAG, "You have not granted '" + permissionName + "' permission in your manifest");
        }

        return true;
    }
}
