package com.dandyhacks.gpstracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Context;
import android.content.SharedPreferences;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.google.android.gms.location.FusedLocationProviderClient.*;

public class MainActivity extends AppCompatActivity {

    static final int DEFAULT_INTERVAL_MODIFIER = 30;
    static final int FASTEST_INTERVAL_MODIFIER = 5;
    static final int PERMISSIONS_FINE_LOCATION = 1;

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    Switch sw_gps, sw_locationupdates;

    // Location request is a config file that stores all settings for FusedLocationProviderClient
    private LocationRequest locationRequest;

    LocationCallback locationCallback;

    // Google's API for location services
    FusedLocationProviderClient locationProvider;

    private static String uniqueID = null;
    private static final String PREF_UNIQUE_ID = "PREF_UNIQUE_ID";

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
        tv_address = findViewById(R.id.tv_address);
        sw_gps = findViewById(R.id.sw_gps);

        //set all properties of LocationRequest
        locationRequest = new LocationRequest();

        // How often the default location check is triggered
        locationRequest.setInterval(1000 * DEFAULT_INTERVAL_MODIFIER);

        // How often the location check runs when set to the fastest interval
        locationRequest.setFastestInterval(1000 * FASTEST_INTERVAL_MODIFIER);

        // Sets the accuracy/power usage for the location operation
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        //event that is triggered whenever the interval is not
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // save the location
                Location location = locationResult.getLastLocation();
                updateUIValues(location);
            }
        };

        sw_gps.setOnClickListener(new View.OnClickListener() {
            @Override

            public void onClick(View v) {
                if (sw_gps.isChecked()) {
                    //most accurate - use GPS
                    locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                    tv_sensor.setText("Using GPS sensors");
                } else {
                    locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                    tv_sensor.setText(("Using Towers + Wifi"));
                }
            }

        });



        id(this);
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
                    //if location is null then app will go kaput (ex. no signal, no permission)
                    if (location == null) {
                        location = new Location("");
                    } else {
                        updateUIValues(location);
                    }

                }
            });


        }
        // if our SDK Version is >= the phones version code -- requests permission for location
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_FINE_LOCATION);
        } else {
            tv_updates.setText("Error in fnding location");
            tv_lat.setText("Error in fnding location");
            tv_lon.setText("Error in fnding location");
            tv_accuracy.setText("Error in fnding accuracy");
            tv_address.setText("Error in fnding location");
            tv_sensor.setText("Error in finding accuracy");
            tv_altitude.setText("Error in fnding altitude");
            tv_speed.setText("Error in fnding speed");
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
        else {
            tv_speed.setText("Not available");
        }


        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);


        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0) {
                Address fetchedAddress = addresses.get(0);
                StringBuilder strAddress = new StringBuilder();
                for (int i = 0; i < fetchedAddress.getMaxAddressLineIndex(); i++) {
                    strAddress.append(fetchedAddress.getAddressLine(i)).append(" ");
                }

                tv_address.setText(strAddress.toString());

            } else {
                tv_address.setText("Searching current address");;
            }

        } catch (Exception e) {
            tv_address.setText("Unable to get street address");
        }
    }



    /*
    source: https://ssaurel.medium.com/how-to-retrieve-an-unique-id-to-identify-android-devices-6f99fd5369eb
    generates a unique user id
     */
    public synchronized static String id(Context context) {

        if (uniqueID == null) {

            SharedPreferences sharedPrefs = context.getSharedPreferences(PREF_UNIQUE_ID, Context.MODE_PRIVATE);
            uniqueID = sharedPrefs.getString(PREF_UNIQUE_ID, null);
            if (uniqueID == null) {

                uniqueID = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.putString(PREF_UNIQUE_ID, uniqueID);
                editor.commit();
            }
        }

        return uniqueID;
    }
}