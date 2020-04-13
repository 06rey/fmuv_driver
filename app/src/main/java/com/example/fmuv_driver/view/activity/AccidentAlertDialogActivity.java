package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.fmuv_driver.R;
import com.example.fmuv_driver.model.BackgroundHttpRequest;
import com.example.fmuv_driver.view.view_helper.ViewHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class AccidentAlertDialogActivity extends AppCompatActivity {

    private int seconds = 20;
    private TextView txtTimer;
    private Button btnCancel;
    private boolean isStopped = false;
    private ViewHelper viewHelper;

    private String lat, lng, speed, g_force, tripId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accident_alert_dialog);
        setTitle("Find Me UV");
        this.setFinishOnTouchOutside(false);

        viewHelper = new ViewHelper(this);

        txtTimer = findViewById(R.id.txtTimer);
        btnCancel = findViewById(R.id.bbtnCancel);

        txtTimer.setText(String.valueOf(seconds) +" sec");

        lat = getIntent().getStringExtra("lat");
        lng = getIntent().getStringExtra("lng");
        speed = getIntent().getStringExtra("speed");
        g_force = getIntent().getStringExtra("g_force");
        tripId = getIntent().getStringExtra("trip_id");

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStopped = true;
                finish();
            }
        });

        timer();
    }

    private void timer() {
        final Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        seconds--;
                        txtTimer.setText(String.valueOf(seconds) +" sec");
                        if (seconds == 0) {
                            saveAccidentLog();
                            finish();
                        }
                        timer();
                    }
                });
            }
        };
        timer.schedule(timerTask, 1000);
    }

    private void saveAccidentLog() {
        Map<String, String> alert = new HashMap<>();
        alert.put("main", "trip");
        alert.put("sub", "accident_log");
        alert.put("resp", "0");
        alert.put("lat", lat);
        alert.put("lng", lng);
        alert.put("speed", speed);
        alert.put("g_force", g_force);
        alert.put("trip_id", tripId);

        new BackgroundHttpRequest(null).okHttpRequest(this, alert, "GET", "");
    }
}
