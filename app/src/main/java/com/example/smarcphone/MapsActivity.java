package com.example.smarcphone;

import static android.provider.AlarmClock.EXTRA_MESSAGE;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.example.smarcphone.databinding.ActivityMapsBinding;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import GpsManager.GPSTracker;
import UdpHandler.UdpManager;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, SensorEventListener {

    private GoogleMap mMap;

    private ActivityMapsBinding binding;
    final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GPSTracker gps;
    private LocationSource locationSource;
    private String ip;
    private Timer timer;
    private SensorManager mSensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        ip = getIntent().getStringExtra(EXTRA_MESSAGE);
        timer = new Timer();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener((SensorEventListener) this, mSensorManager.getDefaultSensor(Sensor.REPORTING_MODE_SPECIAL_TRIGGER),
                SensorManager.SENSOR_DELAY_GAME);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                &&
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            askForLocationPermissions();
        } else {
            //do your work
            gps = GPSTracker.getInstance(this);
            mMap.setMyLocationEnabled(true);

            // check if GPS enabled
            if (gps.canGetLocation()) {
                double latitude = gps.getLatitude();
                double longitude = gps.getLongitude();

                LatLng myLocation = new LatLng(latitude, longitude);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 10));


//                TimerTask myTimerTask = new TimerTask() {
//                    @RequiresApi(api = Build.VERSION_CODES.N)
//                    public void run() {
//                        MapsActivity.this.runOnUiThread(new Runnable() {
//                            public void run() {
//
//
//
//                                Log.d(String.valueOf(Log.DEBUG),"Map thread");
//                            }
//                        });
//                    }
//                };
//                timer.schedule(myTimerTask,0,1000);


                try {
                    UdpManager.getInstance().connect(ip);

                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }
        }

    }

    private void askForLocationPermissions() {

        // Should we show an explanation?
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {

            new AlertDialog.Builder(this)
                    .setTitle("Location permessions needed")
                    .setMessage("you need to allow this permission!")
                    .setPositiveButton("Sure", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MapsActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                                        //Do nothing
                        }
                    })
                    .show();

            // Show an expanation to the user *asynchronously* -- don't block
            // this thread waiting for the user's response! After the user
            // sees the explanation, try again to request the permission.

        } else {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);

            // MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        // get the angle around the z-axis rotated
        if(gps != null) {
            float degree = Math.round(sensorEvent.values[0]);
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            LatLng myLocation = new LatLng(latitude, longitude);
//            CameraPosition cameraPosition = new CameraPosition.Builder()
//                    .bearing(degree)
//                    .target(myLocation).zoom(10)
//                    .build();
//            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            GPSTracker.getInstance().setBearing(degree);
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}