package com.example.fmuv_driver.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.example.fmuv_driver.handler.ServiceServerEventResponseHandler;
import com.example.fmuv_driver.model.BackgroundHttpRequest;
import com.example.fmuv_driver.utility.AppNotification;
import com.example.fmuv_driver.utility.CheckInternet;
import com.example.fmuv_driver.view.activity.AccidentAlertDialogActivity;
import com.example.fmuv_driver.view.activity.OverSpeedDialogActivity;
import com.example.fmuv_driver.view.activity.TripManagerActivity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Speedometer extends Service implements LocationListener, SensorEventListener {

    private LocationManager locationManager;

    private final static int LOCATION_INTERVAL = 0;
    private final static float LOCATION_DISTANCE = 0;
    public static final String FMUV_NOTIFICATION = "FMUV NOTIFICATION";

    public static final int SPEED_LIMIT = 100;

    public static final String BROADCAST_LOCATION = "LOCATION";
    public static final String BROADCAST_INTERNET_STATUS = "BROADCAST_INTERNET_STATUS";

    private String tripId, destination;
    private AppNotification appNotification;

    private Location uvLocation = new Location("");
    private double uvSpeed;

    // Holds check internet class callback function, cancelled if this service is destroyed
    private CheckInternet checkInternet;
    private Call checkInternetCallBack;

    // Sensor manager
    private SensorManager sensorManager;
    private boolean isAccidentAlertTriggered;
    private int accidentInterval = 30;

    private void initLocationManager() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, this);
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, this);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initLocationManager();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    private void initialize() {
        get_pick_up();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.tripId = intent.getStringExtra("tripId");
        this.destination = intent.getStringExtra("destination");

        // Notification that the trip has started
        Intent notificationIntent = new Intent(this, TripManagerActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);
        appNotification = new AppNotification.Builder(this)
                .setChannel_id(FMUV_NOTIFICATION)
                .setPendingIntent(pendingIntent)
                .setTitle("Traveling to " + destination)
                .setText("Accident alert and over speed detector is running...")
                .build();
        startForeground(1, appNotification.getNotification());
        initialize();

        // Checking driver internet connectivity status callback function
        checkInternet = new CheckInternet(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                checkInternetCallBack = call;
                broadcastInternetStatus(false);
                checkInternet.newCall();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                checkInternetCallBack = call;
                broadcastInternetStatus(true);
                checkInternet.newCall();
            }
        });
        // Update driver internet connectivity status in the db
        updateOnlineStatus();
        // // Checking driver online status
        checkInternet.newCall();


        // Sensor object
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
        // Accident alert dialog interval
        accidentAlertInterval();

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }
    // ======================================= BROADCASTER =========================================

    // Broadcast driver location
    private void broadcastLocation(String lat, String lng) {
        Intent intent = new Intent(BROADCAST_LOCATION);
        Bundle bundle = new Bundle();
        bundle.putString("lat", lat);
        bundle.putString("lng", lng);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    // Broadcast driver internet connectivity
    private void broadcastInternetStatus(boolean isOnline) {
        Intent intent = new Intent(BROADCAST_INTERNET_STATUS);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isOnline", isOnline);
        intent.putExtras(bundle);
        sendBroadcast(intent);
    }

    // ======================================= NETWORK CALL ========================================

    // Http request to update driver internet connectivity. executed every minute using a timer task
    private void updateOnlineStatus() {
        Map<String, String> data = new HashMap<>();

        data.put("resp", "0");
        data.put("main", "trip");
        data.put("sub", "update_online_state");
        data.put("trip_id", tripId);

        new BackgroundHttpRequest(null).okHttpRequest(getApplicationContext(), data, "GET", "update_online_state");

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                updateOnlineStatus();
            }
        };
        timer.schedule(timerTask, 100);
    }

    // Http request to report driver over speed log
    private void reportSpeed(String speedStr) {
        Map<String, String> data = new HashMap<>();

        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "over_speed");
        data.put("mode", "new");
        data.put("speed", speedStr);
        data.put("trip_id", tripId);
        data.put("employee_id", "1");

        new BackgroundHttpRequest(null).okHttpRequest(getApplicationContext(), data, "GET", "over_speed");

    }

    // Http request to update driver's gps location, synchronous update using event source
    private void updateLocation(Location location) {
        Map<String, String> data = new HashMap<>();
        String lat = String.valueOf(location.getLatitude());
        String lng = String.valueOf(location.getLongitude());
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "update_location");
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("trip_id", tripId);

        new BackgroundHttpRequest(null).okHttpRequest(getApplicationContext(), data, "GET", "update_location");
    }

    // Synchronous  Http request to get to be pick up passenger
    private void get_pick_up() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "get_pick_up");
        data.put("mode", "get_pick_up");
        data.put("trip_id", tripId);

        ServiceServerEventResponseHandler serviceServerEventResponseHandler = new ServiceServerEventResponseHandler();
        serviceServerEventResponseHandler.setServiceHttpResponseListener(new ServiceServerEventResponseHandler.ServiceServerEventResponseListener() {
            @Override
            public void onServiceServerEventResponse(List<Map<String, String>> list) {
                String status = list.get(0).get("status");
                if (status.equals("1")) {
                    for (Map<String, String> map: list) {
                        Location pickUpLocation = new Location("");
                        pickUpLocation.setLatitude(Double.parseDouble(map.get("pick_lat")));
                        pickUpLocation.setLongitude(Double.parseDouble(map.get("pick_lng")));
                        //float distance = appUtil;
                    }
                }
            }
        });

        new BackgroundHttpRequest(serviceServerEventResponseHandler).serverSentEvent(getApplicationContext(), data,"sync_request");
    }

    // ======================================= NOTIFICATION ========================================

    private void showAccidentAlert(String g_force) {
        Intent dialogIntent = new Intent(this, AccidentAlertDialogActivity.class);
        dialogIntent.putExtra("lat", String.valueOf(uvLocation.getLatitude()));
        dialogIntent.putExtra("lng", String.valueOf(uvLocation.getLongitude()));
        dialogIntent.putExtra("speed", String.valueOf(uvSpeed));
        dialogIntent.putExtra("g_force", g_force);
        dialogIntent.putExtra("trip_id", tripId);
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
    }

    // Dialog message to alert driver that hi/she committed over speed
    private void setOverSpeedDialog(String speed) {
        Intent dialogIntent = new Intent(this, OverSpeedDialogActivity.class);
        dialogIntent.putExtra("speed", speed + " kph");
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);
        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        MediaPlayer mp = MediaPlayer.create(getApplicationContext(), notification);
        mp.start();
    }



    // ====================================== LOCATION LISTENER ====================================

    // GPS location listener
    @Override
    public void onLocationChanged(Location location) {

        if (location != null) {

            // Get speed location.getSpeed() return meters/second 1mps = 3.6 kph
            float mps = location.getSpeed();
            double speed = mps * 3.6;
            uvSpeed = speed;
            String speedStr = String.valueOf(speed);
            Toast.makeText(getApplicationContext(), "SPEED: "+ speedStr +" kph", Toast.LENGTH_SHORT).show();


            uvLocation = location;
            updateLocation(location);
            // Broadcast location to other activity
            broadcastLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

            // Check if Uv Express is overspeeding
            if (speed > SPEED_LIMIT) {
                speedStr = String.format("%.1f", Float.parseFloat(speedStr));
                setOverSpeedDialog(speedStr);
                reportSpeed(speedStr);
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("DebugLog", "GPS has been enabled!");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("DebugLog", "GPS has been disabled!");
    }

    // =============================== ACCELEROMETER LISTENER AND FUNCTION =========================

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometerEvent(event);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void accidentAlertInterval() {
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (accidentInterval == 0) {
                    isAccidentAlertTriggered = false;
                    accidentInterval = 30;
                }
                accidentInterval--;
                accidentAlertInterval();
            }
        };
        timer.schedule(timerTask, 1000);
    }

    private void getAccelerometerEvent(SensorEvent sensorEvent) {
        float[] values = sensorEvent.values;
        // Movement
        float xAxis = values[0];
        float yAxis = values[1];
        float zAxis = values[2];

        float acceleration = ((xAxis * xAxis) + (yAxis * yAxis) + (zAxis * zAxis)) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        float gForce = acceleration/ (float) 9.80665;

        // ((gForce > 19 && uvSpeed > 20) && !isAccidentAlertTriggered)
        if ((gForce > 1 && uvSpeed > -1) && !isAccidentAlertTriggered) {
            showAccidentAlert(String.valueOf(gForce));
            isAccidentAlertTriggered = true;
        }
    }
}
