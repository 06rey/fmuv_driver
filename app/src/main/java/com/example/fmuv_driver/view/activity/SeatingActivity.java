package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.pojo.Seat;
import com.example.fmuv_driver.service.Speedometer;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SeatingActivity extends AppCompatActivity {
    // Object
    private Map<String, TextView> seatMap = new HashMap<>();
    private List<String> seatStatus = new ArrayList<>();

    private List<Seat> seatList = new ArrayList<>();

    private AppViewModel viewModel;
    // View
    private ViewHelper viewHelper;
    private Button btnPassengerMap;
    // Variables
    private boolean isServerEventRunning = false;
    private String lat, lng, tripId, status;

    private BroadcastReceiver broadcastReceiver, internetStatusBroadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seating);

        this.initialize();
        this.prepareSeats();
        this.setViewModelObserver();

    }

    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------- MISC FUNCTIONS -------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void initialize() {
        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        viewHelper = new ViewHelper(this);

        // GET PREV INTENT DATA
        Intent intent = getIntent();
        tripId = intent.getStringExtra("trip_id");
        status = intent.getStringExtra("status");

        // VIEW
        btnPassengerMap = findViewById(R.id.btnPassengerMap);

        setViewListener();
    }

    private void prepareSeats() {

        for(int i=1; i<15; i++) {
            Seat seat = new Seat();
            final String seatNo = String.valueOf(i);
            String txtId = "txtSeat" + seatNo;
            int resID = getResources().getIdentifier(txtId, "id", getPackageName());
            TextView txtSeat = findViewById(resID);
            txtSeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    seatClickAction( Integer.parseInt(seatNo) - 1);
                }
            });
            seat.setTxtSeat(txtSeat);
            seat.setSeatNo(seatNo);
            seat.setStatus("available");
            seat.setPickUpLatLng(null);
            seatList.add(seat);
        }

        if (status.equals("Pending")) {
            disabledButton();
        }
    }

    private void disabledButton() {
        for (Seat seat: seatList) {
            seat.getTxtSeat().setEnabled(false);
        }
        btnPassengerMap.setEnabled(false);
    }

    private void setViewListener() {
        btnPassengerMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passengerMapAll();
            }
        });
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- SEAT BUTTON FUNCTIONS -------------------------------------
    // ---------------------------------------------------------------------------------------------

    private void dropPassenger(Seat seat) {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "0");
        data.put("main", "trip");
        data.put("sub", "drop_passenger");
        data.put("seat_no", seat.getSeatNo());
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("trip_id", tripId);
        viewModel.okHttpRequest(data, "GET", "");

        seat.setStatus("available");
        seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.lineColor));
    }

    private void pickPassenger(Seat seat) {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "0");
        data.put("main", "trip");
        data.put("sub", "pick_passenger");
        data.put("seat_no", seat.getSeatNo());
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("booking_id", seat.getBookingId());
        data.put("trip_id", tripId);
        viewModel.okHttpRequest(data, "GET", "");

        seat.setStatus("on_board");
        seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.light_green));
    }

    private void markSeatAsOccupied(Seat seat) {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "0");
        data.put("main", "trip");
        data.put("sub", "mark_occupied");
        data.put("seat_no", seat.getSeatNo());
        data.put("lat", lat);
        data.put("lng", lng);
        data.put("trip_id", tripId);
        viewModel.okHttpRequest(data, "GET", "");

        seat.setStatus("on_board");
        seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.light_green));
    }

    private void getSeatInfo(boolean init) {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "get_seat_info");
        data.put("trip_id", tripId);

        if (init) {
            viewModel.serverSentInitRequest(data, "GET", "");
        } else {
            viewModel.serverSentEvent(data, "");
        }
    }

    private void seatClickAction(final int index) {
        final Seat seat = seatList.get(index);

        if (!seat.getStatus().equals("reserved")) {
            final AlertDialog dialog;
            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this, R.style.MyAlertDialogStyle);
            View view = LayoutInflater.from(this).inflate(R.layout.seat_action_dialog, null);
            Button btn1 = view.findViewById(R.id.btn1);
            Button btn2 = view.findViewById(R.id.btn2);
            Button btnViewInMap = view.findViewById(R.id.btnViewInMap);

            alertBuilder.setTitle("Select action for seat no. " + seat.getSeatNo())
                    .setView(view)
                    .setCancelable(false);
            dialog = alertBuilder.create();
            dialog.show();

            if (seat.getStatus().equals("booked")) {
                btn1.setText("Pick Passenger");
                btnViewInMap.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        passengerMap(seat);
                    }
                });
                btnViewInMap.setVisibility(View.VISIBLE);
            } else if (seat.getStatus().equals("on_board")) {
                btn1.setText("Drop passenger");
            } else if (seat.getStatus().equals("available")) {
                btn1.setText("Mark Occupied");
            }

            btn1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switch (seat.getStatus()) {
                        case "booked":
                            pickPassenger(seat);
                            break;
                        case "on_board":
                            dropPassenger(seat);
                            break;
                        case "available":
                            markSeatAsOccupied(seat);
                            break;
                    }
                    dialog.dismiss();
                }
            });

            btn2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
    }

    private void seatInfo(List<Map<String, String>> list) {
        for (Seat seat: seatList) {
            int len = list.size();
            for (int i=0; i<len; i++) {
                String seatNo = list.get(i).get("seat_no");
                if (seat.getSeatNo().equals(seatNo)) {
                    String status = list.get(i).get("status");
                    switch (status) {
                        case "booked":
                            seat.setStatus("booked");
                            seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.red));
                            break;
                        case "on_board":
                            seat.setStatus("on_board");
                            seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.light_green));
                            break;
                        case "reserved":
                            seat.setStatus("reserved");
                            seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.yellow));
                            break;
                    }
                    if (!list.get(i).get("pick_lat").equals("null")) {
                        double lat = Double.parseDouble(list.get(i).get("pick_lat"));
                        double lng = Double.parseDouble(list.get(i).get("pick_lng"));
                        LatLng latLng = new LatLng(lat, lng);
                        seat.setPickUpLatLng(latLng);
                    }
                    seat.setBookingId(list.get(i).get("booking_id"));
                    break;
                }
                if (i == len-1) {
                    seat.setStatus("available");
                    seat.getTxtSeat().setBackgroundColor(getResources().getColor(R.color.lineColor));
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // ------------------------------------------ HELPER  ------------------------------------------
    // ---------------------------------------------------------------------------------------------

    // Function to call PassengerMapActivity

    // Called by clicking view pickup button showing seat passenger pick up map
    private void passengerMap(Seat seat) {
        Intent intent = new Intent(SeatingActivity.this, PassengerMapActivity.class);
        intent.putExtra("seatNo1", seat.getSeatNo());
        intent.putExtra("seatLat1", String.valueOf(seat.getPickUpLatLng().latitude));
        intent.putExtra("seatLng1", String.valueOf(seat.getPickUpLatLng().longitude));
        intent.putExtra("seatBookingId1", seat.getBookingId());
        intent.putExtra("tripId", tripId);
        intent.putExtra("size", 1);
        intent.putExtra("mode", "single");
        startActivity(intent);
    }
    //  Called by clicking passenger map button, showing all passenger pick up map
    private void passengerMapAll() {
        Intent intent = new Intent(SeatingActivity.this, PassengerMapActivity.class);
        int key = 0;
        for (Seat seat: seatList) {
            if (seat.getPickUpLatLng() != null) {
                key++;
                intent.putExtra("seatNo"+String.valueOf(key), seat.getSeatNo());
                intent.putExtra("seatBookingId"+String.valueOf(key), seat.getBookingId());
                intent.putExtra("seatLat"+String.valueOf(key), String.valueOf(seat.getPickUpLatLng().latitude));
                intent.putExtra("seatLng"+String.valueOf(key), String.valueOf(seat.getPickUpLatLng().longitude));
            }
        }
        intent.putExtra("tripId", tripId);
        intent.putExtra("size", key);
        intent.putExtra("mode", "all");
        startActivity(intent);
    }

    // ---------------------------------------------------------------------------------------------
    // --------------------------------- NETWORK CALL FUNCTIONS  -----------------------------------
    // ---------------------------------------------------------------------------------------------

    private void setViewModelObserver() {

        viewModel.getServerSentData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                seatInfo(list);
            }
        });

        viewModel.getServerSentInitData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                getSeatInfo(false);
                seatInfo(list);
                isServerEventRunning = true;
            }
        });

        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {

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

        viewModel.getTokenError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                viewHelper.sessionExpiredDialog(new Intent(SeatingActivity.this, LoginActivity.class), SeatingActivity.this);
            }
        });

        viewModel.getSseInitDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getSseInitJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {

            }
        });

        viewModel.getSseInitStatusError().observe(this, new Observer<Boolean>() {
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
        getSeatInfo(true);
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase(Speedometer.BROADCAST_ID)) {
                    Bundle extra = intent.getExtras();
                    lat = extra.getString("lat");
                    lng = extra.getString("lng");
                    Toast.makeText(getApplicationContext(), "UV EXPRESS LOCATION RECEIVE", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter("LOCATION"));

        internetStatusBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equalsIgnoreCase(Speedometer.BROADCAST_INTERNET_STATUS)) {
                    Bundle extra = intent.getExtras();
                    boolean isOnline = extra.getBoolean("isOnline");
                    TextView txtOnlineStatus = findViewById(R.id.txtOnlineStatus);
                    if (isOnline) {
                        txtOnlineStatus.setText("ONLINE");
                        txtOnlineStatus.setTextColor(getResources().getColor(R.color.green));
                    } else {
                        txtOnlineStatus.setText("OFFLINE");
                        txtOnlineStatus.setTextColor(getResources().getColor(R.color.red));
                    }
                }
            }
        };
        registerReceiver(internetStatusBroadcastReceiver, new IntentFilter("BROADCAST_INTERNET_STATUS"));
        super.onPostResume();
    }

    @Override
    protected void onPause() {
        if (isServerEventRunning) {
            viewModel.closeServerEventConnection();
        }
        unregisterReceiver(internetStatusBroadcastReceiver);
        unregisterReceiver(broadcastReceiver);
        getViewModelStore().clear();
        isServerEventRunning = false;
        super.onPause();
    }
}
