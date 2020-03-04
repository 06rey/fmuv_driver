package com.example.fmuv_driver.view.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.fmuv_driver.R;

public class OverSpeedDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_over_speed_dialog);
        setTitle("Find Me UV");
        this.setFinishOnTouchOutside(false);

        Intent intent = getIntent();
        TextView txtSpeed = findViewById(R.id.txtSpeed);
        txtSpeed.setText(intent.getStringExtra("speed"));
    }

    public void onBtnOvClick(View view) {
        finish();
    }
}
