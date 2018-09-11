package com.reddy98.vishnuvardhan.speedin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.math.BigDecimal;

public class MainActivity extends AppCompatActivity implements GPSCallback
{
    private GPSManager gpsManager = null;
    boolean isGPSEnabled = false;
    LocationManager locationManager;
    double currentSpeed, kmphSpeed;
    double[][] coordinates = new double[2][2];
    private TextView textView;
    private FusedLocationProviderClient mFusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.speed);
        coordinates[0][0] = Integer.MIN_VALUE;
        coordinates[0][1] = Integer.MIN_VALUE;
        coordinates[1][0] = Integer.MIN_VALUE;
        coordinates[1][1] = Integer.MIN_VALUE;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 904);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getCurrentSpeed(View view) {
        textView.setText("Loading ...");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        gpsManager = new GPSManager(this);
        isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
        if (isGPSEnabled) {
            gpsManager.startListening(this);
            gpsManager.setGPSCallback(this);
        } else {
            gpsManager.showSettingsAlert();
        }
    }

    @Override
    public void onGPSUpdate(Location location) {
        double speed = location.getSpeed();
        currentSpeed = round(speed, 3, BigDecimal.ROUND_HALF_UP);
        kmphSpeed = round((currentSpeed * 3.6), 3, BigDecimal.ROUND_HALF_UP);
        textView.setText("speed is " + kmphSpeed);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gpsManager.stopListening();
        gpsManager.setGPSCallback(null);
        gpsManager = null;
    }

    public static double round(double unrounded, int precision, int roundingMode) {
        BigDecimal bd = new BigDecimal(unrounded);
        BigDecimal rounded = bd.setScale(precision, roundingMode);
        return rounded.doubleValue();
    }

    public void getlocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                assert locationManager != null;
                isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                gpsManager = new GPSManager(MainActivity.this);

                if (isGPSEnabled) {
                    if (location != null) {
                        Toast.makeText(MainActivity.this, "Latitude is " + location.getLatitude() + " Longitude is " + location.getLongitude() + "Time is " + System.currentTimeMillis(), Toast.LENGTH_SHORT).show();
                        if (coordinates[0][0] != Integer.MIN_VALUE) {
                            if (coordinates[1][0] != Integer.MIN_VALUE) {
                                coordinates[0][0] = coordinates[1][0];
                                coordinates[0][1] = coordinates[1][1];
                                coordinates[1][0] = location.getLatitude();
                                coordinates[1][1] = location.getLongitude();
                            } else {
                                coordinates[1][0] = location.getLatitude();
                                coordinates[1][1] = location.getLongitude();
                            }
                        } else {
                            coordinates[0][0] = location.getLatitude();
                            coordinates[0][1] = location.getLongitude();
                        }
                    }
                } else {
                    gpsManager.showSettingsAlert();
                }

            }
        });
    }

    public void updateLocation(View view) {
        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(this, 10000);
                View view = findViewById(R.id.speed);
                getlocation(view);
                Log.i("coordinates", "\ncoordinates[0][0] is " + coordinates[0][0] + " coordinates[0][1] is " + coordinates[0][1] + "\n" + "coordinates[1][0] is " + coordinates[1][0] + " coordinates[1][1] is " + coordinates[1][1] + "#########\n");
                getSpeed();
            }
        };
        handler.postDelayed(runnable, 10000);
    }

    public void getSpeed() {
        textView.setText("loading ...");
        double speed;
        float[] results = new float[3];
        results[0] = 100;
        android.location.Location.distanceBetween(coordinates[0][0], coordinates[0][1], coordinates[1][0], coordinates[1][1], results);
        Log.i("android.location", "distance is " + results[0]);
        speed = results[0];
//        speed = distance(coordinates[0][0], coordinates[0][1], coordinates[1][0], coordinates[1][1]);
//        Log.i("Speed", "speed is " + speed);
        textView.setText("" + speed + " Kmph");
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}