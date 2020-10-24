package com.dandyhacks.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends AppCompatActivity {

    static final int DEFAULT_INTERVAL_MODIFIER = 30;
    static final int FASTEST_INTERVAL_MODIFIER = 5;
    static final int PERMISSIONS_FINE_LOCATION = 1;

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    Switch sw_gps, sw_locationupdates;

    // Location request is a config file that stores all settings for FusedLocationProviderClient
    LocationRequest locationRequest;

    // Google's API for location services
    FusedLocationProviderClient locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // all UI elements found by ID
        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);
        sw_locationupdates = findViewById(R.id.sw_locationsupdates);

        locationRequest = new LocationRequest();

        // How often the default location check is triggered
        locationRequest.setInterval(1000 * DEFAULT_INTERVAL_MODIFIER);

        // How often the location check runs when set to the fastest interval
        locationRequest.setFastestInterval(1000 * FASTEST_INTERVAL_MODIFIER);

        // Sets the accuracy/power usage for the location operation
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        updateGPS();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_FINE_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) updateGPS();
            else {
                Toast.makeText(this, "This app requires permission to be granted in order to work properly", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateGPS() {

        locationProvider = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        // Checks to see if the app has permissions to access precise (fine) location
        int isFineLocationOn = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        // Int that represents the if a permission is granted
        int permissionsCheck = PackageManager.PERMISSION_GRANTED;

        if (isFineLocationOn == permissionsCheck) {

            // Get last location, on success, it will update UI elements with location data
            locationProvider.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {

                    updateUIValues(location);
                }
            });
        }
        // if our SDK Version is >= the phones version code -- requests permission for location
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        }
    }

    private void updateUIValues(Location location) {

        // updates all text values for UI elements to their respectively values
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));

        // Some phones don't have altitude or speed, so we need to check before calling their methods
        if (location.hasAccuracy()) tv_altitude.setText(String.valueOf(location.getAltitude()));
        else tv_altitude.setText("Not available");

        if (location.hasSpeed()) tv_speed.setText(String.valueOf(location.getSpeed()));
        else tv_speed.setText("Not available");
    }
}