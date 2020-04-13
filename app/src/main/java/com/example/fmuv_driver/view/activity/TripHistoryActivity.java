package com.example.fmuv_driver.view.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.SharedPref;
import com.example.fmuv_driver.model.pojo.Trip;
import com.example.fmuv_driver.service.Speedometer;
import com.example.fmuv_driver.view.adapter.TripRecyclerViewAdapter;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TripHistoryActivity extends AppCompatActivity {

    private AppViewModel viewModel;
    private ProgressBar progressBar;
    private ViewHelper viewHelper;
    private LinearLayout errorLayout;

    private RecyclerView tripRecyclerView;
    private RecyclerView.Adapter tripRecyclerViewAdapter;
    private List<Trip> tripList = new ArrayList<>();

    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        this.initialize();
    }

    private void initialize() {

        progressBar = findViewById(R.id.progressBar);
        viewHelper = new ViewHelper(this);
        errorLayout = findViewById(R.id.errorLayout);
        this.setViewModelObserver();

        activity = this;

        tripRecyclerView = findViewById(R.id.rvTripList);
        tripRecyclerView.setHasFixedSize(true);
        tripRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.getTrip();
    }

    private void getTrip() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "get_trip_history");
        viewModel.okHttpRequest(data, "GET", "");
    }

    private void startSpeedometerService(Map<String, String> data) {
        Intent intent = new Intent(this, Speedometer.class);
        intent.putExtra("tripId", data.get("trip_id"));
        intent.putExtra("destination", data.get("destination"));
        new SharedPref(this, "loginSession")
                .setValue("tripId", data.get("trip_id"));
        startService(intent);
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                //String type = list.get(0).get("type");
                for (Map<String, String> data: list) {
                    if (data.get("status").equals("Traveling")) {
                        startSpeedometerService(data);
                    }
                }
                progressBar.setVisibility(View.INVISIBLE);
                setTripRecyclerViewItem(list);
                progressBar.setVisibility(View.GONE);
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.exitActivity(activity,"Trip History", "Failed to connect to server.");
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.exitActivity(activity,"Trip History", "Service is not available at the moment.");
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.exitActivity(activity,"Trip History", "Service is not available at the moment.");
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.exitActivity(activity,"Trip History", "No record found.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.exitActivity(activity,"Trip History", "Sorry, something went wrong there.");
            }
        });

        viewModel.getTokenError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.sessionExpiredDialog(new Intent(TripHistoryActivity.this, LoginActivity.class), TripHistoryActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    public void onClickRetry(View view) {
        errorLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        this.getTrip();
    }

//---------------------------------------- RECYCLER VIEW ADAPTER -----------------------------------------
    private void setTripRecyclerViewItem(List<Map<String, String>> list) {
        for (Map<String, String> map : list) {
            Trip trip = new Trip();
            trip.setTripId(map.get("trip_id"));
            trip.setDestination(map.get("destination"));
            trip.setDate(map.get("date"));
            trip.setFrom(map.get("origin"));
            trip.setTo(map.get("destination"));
            trip.setCompany(map.get("company_name"));
            trip.setDeparture(map.get("depart_time"));
            trip.setPassNo(map.get("no_of_pass"));
            trip.setPlate(map.get("plate_no"));
            trip.setStatus(map.get("status"));
            trip.setArrivalTime(map.get("arrival_time"));
            tripList.add(trip);

            tripRecyclerViewAdapter = new RecyclerView.Adapter<ViewHolder>() {

                @NonNull
                @Override
                public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.trip_history_list, parent, false);
                    return new ViewHolder(view);
                }

                @Override
                public int getItemCount() {
                    return tripList.size();
                }

                @Override
                public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
                    Trip trip = tripList.get(position);
                    holder.txtDate.setText(trip.getDate());
                    holder.txtDepart.setText(trip.getDeparture());
                    holder.txtArrive.setText(trip.getArrivalTime());
                    holder.txtPlateNo.setText(trip.getPlate());
                    holder.txtNoPass.setText(trip.getPassNo());
                    holder.txtCompany.setText(trip.getCompany());
                    holder.txtFrom.setText(trip.getFrom());
                    holder.txtTo.setText(trip.getTo());
                }
            };
            tripRecyclerView.setAdapter(tripRecyclerViewAdapter);
        }
    }
    // View holder
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtDate, txtDepart, txtArrive, txtPlateNo, txtNoPass, txtCompany, txtFrom, txtTo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtDepart = itemView.findViewById(R.id.txtDepart);
            txtArrive = itemView.findViewById(R.id.txtArrive);
            txtPlateNo = itemView.findViewById(R.id.txtPlateNo);
            txtNoPass = itemView.findViewById(R.id.txtNoPass);
            txtCompany = itemView.findViewById(R.id.txtCompany);
            txtFrom = itemView.findViewById(R.id.txtFrom);
            txtTo = itemView.findViewById(R.id.txtTo);
        }
    }
}
