package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.pojo.Seat;
import com.example.fmuv_driver.service.Speedometer;
import com.example.fmuv_driver.utility.AppUtil;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
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
    private String seatNo, tripId = "1";
    private boolean isRouteLineSet = false, isMapIsReady = false;
    // MAP
    private GoogleMap googleMap;
    // OBJECT
    private AppViewModel viewModel;
    private List<RouteItem> routeItemList = new ArrayList<>();
    private LatLng uvLatLng = null, pickUpLatLng, cameraLatLng;
    private Marker uvExpressMarker;
    private List<Seat> seatList = new ArrayList<>();
    private ViewHelper viewHelper;
    private AppUtil appUtil = new AppUtil();
    // VIEW
    private LinearLayout locationLayout;

    private  BroadcastReceiver broadcastReceiver;
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
        // INIT VIEWS
        locationLayout = findViewById(R.id.locationLayout);

        setViewModelObserver();
        getPrevIntentData();
        viewHelper = new ViewHelper(this);
    }

    private void getPrevIntentData() {
        int extraSize = getIntent().getIntExtra("size", 0);
        for (int i=1; i<=extraSize; i++) {
            Seat seat = new Seat();
            seat.setContactNo(getIntent().getStringExtra("contact_no"+String.valueOf(i)));
            seat.setSeatNo(getIntent().getStringExtra("seatNo"+String.valueOf(i)));
            seat.setBookingId(getIntent().getStringExtra("seatBookingId"+String.valueOf(i)));
            double lat = Double.parseDouble(getIntent().getStringExtra("seatLat"+String.valueOf(i)));
            double lng = Double.parseDouble(getIntent().getStringExtra("seatLng"+String.valueOf(i)));
            seat.setPickUpLatLng(new LatLng(lat, lng));
            seatList.add(seat);
        }
        tripId = getIntent().getStringExtra("tripId");
        if (getIntent().getStringExtra("mode").equals("all")) {
            cameraLatLng = uvLatLng;
        } else {
            cameraLatLng =seatList.get(0).getPickUpLatLng();
        }
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------------- DIALOG ACTION  --------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void setDialogAction(final Seat seat) {
        final AlertDialog dialog;
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
        View view = LayoutInflater.from(this).inflate(R.layout.seat_action_dialog, null);
        Button btn1 = view.findViewById(R.id.btn1);
        Button btn2 = view.findViewById(R.id.btn2);
        Button btnSms = view.findViewById(R.id.btnViewInMap);
        Button btnCall = view.findViewById(R.id.btnCall);
        btnSms.setVisibility(View.VISIBLE);
        btnCall.setVisibility(View.VISIBLE);
        btnSms.setText("Send SMS");

        btn1.setText("Pick Passenger");
        alertBuilder.setView(view)
                .setCancelable(false);
        dialog = alertBuilder.create();
        dialog.show();

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPassenger(seat);
                dialog.dismiss();
            }
        });

        btnCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = seat.getContactNo();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        btnSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+ seat.getContactNo()));
                sendIntent.putExtra("sms_body", "Find Me UV Notification:\n");
                startActivity(sendIntent);
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void pickPassenger(Seat seat) {
        seat.getMarker().remove();
        Map<String, String> data = new HashMap<>();
        data.put("resp", "0");
        data.put("main", "trip");
        data.put("sub", "pick_passenger");
        data.put("seat_no", seat.getSeatNo());
        data.put("lat", String.valueOf(seat.getPickUpLatLng().latitude));
        data.put("lng", String.valueOf(seat.getPickUpLatLng().longitude));
        data.put("trip_id", tripId);
        data.put("booking_id", seat.getBookingId());
        viewModel.okHttpRequest(data, "GET", "");
        viewHelper.showMessage("Success", "Successfully picked up passenger.");
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

        // Set googleMap marker click listener
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                String title = marker.getTitle();
                Log.d("DebugLog", "Booking ):--"+ String.valueOf(seatList.size()));
                Log.d("DebugLog", "marker.getTitle():--"+ title);
                if (title.contains("Booking")) {
                    for (Seat seat: seatList) {
                        if (title.equals(seat.getMarkerTitle())) {
                            Log.d("DebugLog", "seat.getMarkerTitle():--"+ seat.getMarkerTitle());
                            setDialogAction(seat);
                        }
                    }
                }
                return false;
            }
        });

        getRoute();
        setPassengerMarker();
    }

    private void setPassengerMarker() {
        int bookNum = 1;
        for (Seat seat: seatList) {
            Marker passengerMarker;
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.map_pin);
            MarkerOptions markerOptions = new MarkerOptions()
                    .title("Booking no."+String.valueOf(bookNum)+" pick up location")
                    .position(seat.getPickUpLatLng())
                    .icon(icon);
            passengerMarker = googleMap.addMarker(markerOptions);
            seat.setMarker(passengerMarker);
            seat.setMarkerTitle(passengerMarker.getTitle());
            bookNum++;
        }
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

        int zoom = 15;
        if (cameraLatLng == null) {
            locationLayout.setVisibility(View.VISIBLE);
            cameraLatLng = routeItemList.get(0).getOriginLatLng();
            zoom = 12;
        }

        CameraPosition googlePlex = CameraPosition.builder()
                .target(cameraLatLng)
                .zoom(zoom)
                .bearing(0)
                .tilt(0)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(googlePlex), 1000, null);
    }

    public void setUvLocation() {
        if (uvExpressMarker != null) {
            uvExpressMarker.remove();
        }
        if (uvLatLng != null) {
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.van_marker);
            MarkerOptions vanMark = new MarkerOptions()
                    .title("UV Express Location")
                    .position(uvLatLng)
                    .icon(icon);
            uvExpressMarker = googleMap.addMarker(vanMark);
        }
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

    // ---------------------------------------------------------------------------------------------
    // ----------------------------------- THIS OVERRIDE METHOD  -----------------------------------
    // ---------------------------------------------------------------------------------------------


    @Override
    protected void onPostResume() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase(Speedometer.BROADCAST_LOCATION)) {
                    Bundle extra = intent.getExtras();
                    uvLatLng = new LatLng(Double.parseDouble(extra.getString("lat")), Double.parseDouble(extra.getString("lng")));
                    if (isMapIsReady) {
                        setUvLocation();
                        locationLayout.setVisibility(View.GONE);
                    }
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("LOCATION"));
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
}
