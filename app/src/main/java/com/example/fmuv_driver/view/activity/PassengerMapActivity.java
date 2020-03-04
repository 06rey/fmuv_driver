package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.service.Speedometer;
import com.example.fmuv_driver.view_model.AppViewModel;
import com.example.fmuv_driver.model.pojo.RouteItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PassengerMapActivity extends AppCompatActivity implements OnMapReadyCallback {
    // MISC
    private String seatNo, tripId;
    private boolean isRouteLineSet = false, isMapIsReady = false;
    // MAP
    private GoogleMap googleMap;
    // OBJECT
    private AppViewModel viewModel;
    private List<RouteItem> routeItemList = new ArrayList<>();
    private LatLng uvLatLng, pickUpLatLng, cameraLatLng;
    private Marker uvExpressMarker;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_passenger_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        initializeAll();
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------------ INITIALIZE  --------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void initializeAll() {
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase(Speedometer.BROADCAST_ID)) {
                    Bundle extra = intent.getExtras();
                    uvLatLng = new LatLng(Double.parseDouble(extra.getString("lat")), Double.parseDouble(extra.getString("lng")));
                    if (isMapIsReady) {
                        setUvLocation();
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("LOCATION"));

        getPrevIntentData();
        setViewModelObserver();
    }

    private void getPrevIntentData() {
        if (getIntent().getStringExtra("mode").equals("seat")) {
            seatNo = getIntent().getStringExtra("seatNo");
            tripId = getIntent().getStringExtra("tripId");
            pickUpLatLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("lat")), Double.parseDouble(getIntent().getStringExtra("lng")));
            cameraLatLng = pickUpLatLng;
        } else {
            tripId = getIntent().getStringExtra("tripId");
            uvLatLng = new LatLng(Double.parseDouble(getIntent().getStringExtra("lat")), Double.parseDouble(getIntent().getStringExtra("lng")));
            cameraLatLng = uvLatLng;
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------------ NETWORK CALL -------------------------------------
    // ---------------------------------------------------------------------------------------------
    private void getRoute() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "get_route");
        data.put("trip_id", tripId);
        viewModel.okHttpRequest(data, "GET", "");
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------------ MAP FUNCTIONS  -----------------------------------
    // ---------------------------------------------------------------------------------------------

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        isMapIsReady = true;
        getRoute();
    }

    private void showSinglePickUpPoint() {

    }

    private void showAllPickUpLocation() {

    }

    private void setRouteMapLine(List<Map<String, String>> list) {
        String transportServiceName = list.get(0).get("company_name");
        LatLng originLatLng = routeItemList.get(0).getOriginLatLng();
        LatLng destinationLatLng = routeItemList.get(0).getDestLatLng();

        String originName = list.get(0).get("origin");
        String destinationName = list.get(0).get("destination");
        googleMap.addMarker(new MarkerOptions().position(originLatLng).title(transportServiceName + " " + originName + " Terminal"));
        googleMap.addMarker(new MarkerOptions().position(destinationLatLng).title(transportServiceName + " " + destinationName + " Terminal"));

        PolylineOptions routePolyLine = routeItemList.get(0).getPolyLineOption();
        routePolyLine.color(getResources().getColor(R.color.lineColor));
        routePolyLine.visible( true );
        routePolyLine.clickable(true);
        googleMap.addPolyline(routePolyLine);

        setUvLocation();

       CameraPosition googlePlex = CameraPosition.builder()
                .target(cameraLatLng)
                .zoom(15)
                .bearing(0)
                .tilt(0)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
    }

    public void setUvLocation() {
        if (uvExpressMarker != null) {
            uvExpressMarker.remove();
        }
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.van_marker);
        MarkerOptions vanMark = new MarkerOptions()
                .title("UV Express Location")
                .position(uvLatLng)
                .icon(icon);
        uvExpressMarker = googleMap.addMarker(vanMark);
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------------- VIEW MODEL OBSERVER  --------------------------------
    // ---------------------------------------------------------------------------------------------

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                setRouteMapLine(list);
            }
        });

        viewModel.getRouteItemList().observe(this, new Observer<List<RouteItem>>() {
            @Override
            public void onChanged(List<RouteItem> routePolyLines) {
                if (!isRouteLineSet) {
                    routeItemList = routePolyLines;
                    isRouteLineSet = true;
                }
            }
        });

        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });
    }
}
