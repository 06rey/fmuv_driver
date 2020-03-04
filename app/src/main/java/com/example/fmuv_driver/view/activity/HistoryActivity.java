package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.fmuv_driver.R;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    public void tripHistoryClick(View view) {
        startActivity(new Intent(HistoryActivity.this, TripHistoryActivity.class));
    }

    public void overSpeedClick(View view) {
        startActivity(new Intent(HistoryActivity.this, OverSpeedHistoryActivity.class));
    }

    public void accidentAlertClick(View view) {
        startActivity(new Intent(HistoryActivity.this, AccidentAlertHistoryActivity.class));
    }
}
