package com.example.fmuv_driver.service;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.example.fmuv_driver.model.ServerEventModel;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.utility.AppNotification;
import com.example.fmuv_driver.utility.AppUtil;
import com.example.fmuv_driver.utility.CheckInternet;
import com.example.fmuv_driver.view.activity.OverSpeedDialogActivity;
import com.example.fmuv_driver.view.activity.MessageDialogActivity;
import com.example.fmuv_driver.view.activity.TripManagerActivity;

import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.content.ContextCompat;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class Speedometer extends Service implements LocationListener {

    private LocationManager locationManager;

    private final static int LOCATION_INTERVAL = 0;
    private final static float LOCATION_DISTANCE = 0;
    public static final String FMUV_NOTIFICATION = "FMUV NOTIFICATION";

    public static final int SPEED_LIMIT = 100;

    public static final String BROADCAST_LOCATION = "LOCATION";
    public static final String BROADCAST_INTERNET_STATUS = "BROADCAST_INTERNET_STATUS";

    private String tripId, destination;
    private AppNotification appNotification;

    private long prevMillis = 0;
    private Location prevLocation = null;
    private AppUtil appUtil = new AppUtil();

    private Location uvLocation = new Location("");

    private ServerEventModel serverEventModel;
    private ServiceServerEventResponseHandler serviceServerEventResponseHandler = new ServiceServerEventResponseHandler();

    // Holds check internet class callback function, cancelled if this service is destroyed
    private CheckInternet checkInternet;
    private Call checkInternetCallBack;

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
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        checkInternetCallBack.cancel();
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

    private void setNotificationDialog(String title, String msg, List<Map<String, String>> list) {
        Intent dialogIntent = new Intent(this, MessageDialogActivity.class);
        dialogIntent.putExtra("title", title);
        dialogIntent.putExtra("msg", msg);
        dialogIntent.putExtra("queue_id", list.get(0).get("queue_id"));
        dialogIntent.putExtra("no_of_pass", list.get(0).get("no_of_pass"));
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
        float speed = location.getSpeed() * 18 / 5;
        String speedStr = String.valueOf(speed);

        uvLocation = location;

        // Send driver location to server
        updateLocation(location);
        // Broadcast location to other activity
        broadcastLocation(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

        if (prevLocation != null) {
            long currentMillis = Calendar.getInstance().getTimeInMillis();
            Log.d("DebugLog", "PREV MILLIS: " + String.valueOf(prevMillis));
            Log.d("DebugLog", "CURRENT MILLIS: " + String.valueOf(currentMillis));
            float distance = appUtil.getDistance(prevLocation, location);
            Log.d("DebugLog", "DISTANCE: " + String.valueOf(distance));
            float time = ((currentMillis - prevMillis)/60)/60;
            Log.d("DebugLog", "(("+ String.valueOf(currentMillis) +"-"+ String.valueOf(prevMillis) +")/60)/60 = "+ String.valueOf(time));
            Log.d("DebugLog", "TIME: " + String.valueOf(time));
            float uvSpeed = distance/time;
            Log.d("DebugLog", "SPPED: " + String.valueOf(uvSpeed));

            Toast.makeText(getApplicationContext(), "SPEED: "+ String.valueOf(uvSpeed), Toast.LENGTH_SHORT).show();
        }
        prevLocation = location;
        prevMillis = Calendar.getInstance().getTimeInMillis();

        // Check if Uv Express is overspeeding
        if (speed > SPEED_LIMIT) {
            speedStr = String.format("%.1f", Float.parseFloat(speedStr));
            setOverSpeedDialog(speedStr);
            reportSpeed(speedStr);
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
}
