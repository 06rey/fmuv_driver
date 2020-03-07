package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.pojo.OverSpeed;
import com.example.fmuv_driver.view.adapter.OverSpeedHistoryAdapter;
import com.example.fmuv_driver.view.view_helper.ViewHelper;
import com.example.fmuv_driver.view_model.AppViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OverSpeedHistoryActivity extends AppCompatActivity {

    private AppViewModel viewModel;
    private ProgressBar progressBar;
    private ViewHelper viewHelper;
    private LinearLayout errorLayout;

    private Activity activity;

    private RecyclerView overSpeedRecyclerView;
    private RecyclerView.Adapter overSpeedRecyclerViewAdapter;
    private List<OverSpeed> overSpeedList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_speed_history);

        viewModel = ViewModelProviders.of(this).get(AppViewModel.class);
        viewModel.initialize(this);
        activity = this;

        progressBar = findViewById(R.id.progressBar);
        viewHelper = new ViewHelper(this);
        errorLayout = findViewById(R.id.errorLayout);

        overSpeedRecyclerView = findViewById(R.id.rvTripList);
        overSpeedRecyclerView.setHasFixedSize(true);
        overSpeedRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        this.setViewModelObserver();

        getTrip();
    }

    private void getTrip() {
        Map<String, String> data = new HashMap<>();
        data.put("resp", "1");
        data.put("main", "trip");
        data.put("sub", "get_over_speed");
        viewModel.okHttpRequest(data, "GET", "");
    }

    private void setViewModelObserver() {
        viewModel.getOkhttpData().observe(this, new Observer<List<Map<String, String>>>() {
            @Override
            public void onChanged(List<Map<String, String>> list) {
                for (Map<String, String> map: list) {
                    OverSpeed overSpeed = new OverSpeed();
                    overSpeed.setRoute(map.get("route_name"));
                    overSpeed.setDate(map.get("date"));
                    overSpeed.setTime(map.get("time"));
                    overSpeed.setSpeed(map.get("speed") + " kph");
                    overSpeedList.add(overSpeed);
                }
                overSpeedRecyclerViewAdapter = new OverSpeedHistoryAdapter(getApplicationContext(), overSpeedList);
                overSpeedRecyclerView.setAdapter(overSpeedRecyclerViewAdapter);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
        // Error observer
        viewModel.getOkhttpConnectionError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getOkHttpServiceError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getOkhttpStatusError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getOkhttpDataError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.exitActivity(activity,"No Result", "Currently you have no over speed logs.");
            }
        });

        viewModel.getOkhttpJsonError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                errorLayout.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getTokenError().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                progressBar.setVisibility(View.INVISIBLE);
                viewHelper.sessionExpiredDialog(new Intent(OverSpeedHistoryActivity.this, LoginActivity.class), OverSpeedHistoryActivity.this);
            }
        });
    }
}
